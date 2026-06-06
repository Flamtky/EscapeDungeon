#!/usr/bin/env python3
"""Export xAPI move statements to a DB-free Dungeon replay asset."""

from __future__ import annotations

import argparse
import csv
import io
import json
import subprocess
from collections import defaultdict
from dataclasses import dataclass
from datetime import datetime, timezone
from pathlib import Path
from typing import Any, Iterable
from urllib.parse import urlparse

SCHEMA = "dungeon.replay.v1"
VALID_DIRECTIONS = {"UP", "RIGHT", "DOWN", "LEFT", "NONE"}
MOVE_REPAIR_LOOKAHEAD_MS = 1_000
SNAP_UP_THRESHOLD = 0.99
ADJACENT_MOVE_DISTANCE = 1.01


@dataclass(frozen=True)
class MoveRow:
    """A single successful move row read from xapi_statements."""

    player_id: str
    timestamp: datetime
    x: float
    y: float
    direction: str
    stamina: float | None = None


@dataclass(frozen=True)
class LastRiddleTryMoveRow:
    """A move row within one player's last successful push-riddle try."""

    session_id: str
    player_id: str
    try_start: datetime
    end_time: datetime
    try_count: int
    timestamp: datetime
    x: float
    y: float
    direction: str
    stamina: float | None = None


@dataclass(frozen=True)
class LastSecondsMoveRow:
    """A move row inside one player/session's final replay window."""

    session_id: str
    player_id: str
    window_start: datetime
    window_end: datetime
    timestamp: datetime
    x: float
    y: float
    direction: str
    stamina: float | None = None


def parse_timestamp(value: str) -> datetime:
    """Parse an ISO-like timestamp accepted by Postgres and the replay exporter."""
    return datetime.fromisoformat(value.replace(" ", "T"))


def parse_players(value: str) -> list[str]:
    """Parse a comma-separated player list."""
    players = [player.strip() for player in value.split(",") if player.strip()]
    if not players:
        raise ValueError("At least one player must be specified.")
    return players


def parse_stamina(value: str | None) -> float | None:
    """Parse optional stamina values exported by xAPI move statements."""
    if value is None or value == "":
        return None
    return float(value.replace(",", "."))


def character_class_for(player_id: str) -> str:
    """Infer a Dungeon character class from the exported player id suffix."""
    suffix = player_id.rsplit("#", maxsplit=1)[-1].upper()
    if suffix in {"APPRENTICE", "ROGUE", "WIZARD", "HUNTER", "MUSHROOM_WIZARD"}:
        return suffix
    return "WIZARD"


def build_replay_asset(
    *,
    session_id: str,
    player_ids: list[str],
    start: datetime,
    end: datetime,
    level: str,
    rows: Iterable[MoveRow],
    exported_at: datetime | None = None,
    repair_move_order_enabled: bool = True,
) -> dict[str, Any]:
    """Build the compact replay JSON structure from DB rows."""
    validate_window(start, end)

    grouped: dict[str, list[MoveRow]] = defaultdict(list)
    for row in rows:
        validate_row(row)
        if row.player_id in player_ids:
            grouped[row.player_id].append(row)

    if not grouped:
        raise ValueError("No successful move rows found for the requested replay window.")

    missing_players = [player_id for player_id in player_ids if player_id not in grouped]
    if missing_players:
        raise ValueError("Missing move rows for player(s): " + ", ".join(missing_players))

    duration_ms = millis_between(start, end)
    players = []
    for player_id in player_ids:
        rows = sorted(grouped[player_id], key=lambda event: event.timestamp)
        repaired_rows = repair_move_order(rows) if repair_move_order_enabled else rows
        events = []
        for row, time_slot in zip(repaired_rows, rows, strict=True):
            events.append(replay_event(row, start, time_slot))
        players.append(
            {
                "playerId": player_id,
                "characterClass": character_class_for(player_id),
                "events": events,
            }
        )

    exported_at = exported_at or datetime.now(timezone.utc).replace(tzinfo=None)
    return {
        "schema": SCHEMA,
        "source": {
            "sessionId": session_id,
            "startTime": start.isoformat(),
            "endTime": end.isoformat(),
            "exportedAt": exported_at.isoformat(timespec="seconds"),
        },
        "level": level,
        "durationMs": duration_ms,
        "players": players,
    }


def build_per_player_first_seconds_asset(
    *,
    session_id: str,
    seconds: int,
    level: str,
    rows_by_player: dict[str, list[MoveRow]],
    exported_at: datetime | None = None,
    repair_move_order_enabled: bool = True,
) -> dict[str, Any]:
    """Build one asset with each player normalized to their own first movement timestamp."""
    if seconds <= 0:
        raise ValueError("Seconds must be greater than zero.")
    if not rows_by_player:
        raise ValueError("No successful move rows found.")

    players = []
    for player_id in sorted(rows_by_player):
        rows = sorted(rows_by_player[player_id], key=lambda event: event.timestamp)
        if not rows:
            raise ValueError("Player has no move rows: " + player_id)
        for row in rows:
            validate_row(row)
        first_timestamp = rows[0].timestamp
        repaired_rows = repair_move_order(rows) if repair_move_order_enabled else rows
        events = []
        for row, time_slot in zip(repaired_rows, rows, strict=True):
            events.append(replay_event(row, first_timestamp, time_slot))
        players.append(
            {
                "playerId": player_id,
                "characterClass": character_class_for(player_id),
                "events": events,
            }
        )

    exported_at = exported_at or datetime.now(timezone.utc).replace(tzinfo=None)
    return {
        "schema": SCHEMA,
        "source": {
            "sessionId": session_id,
            "mode": "per-player-first-seconds",
            "seconds": seconds,
            "exportedAt": exported_at.isoformat(timespec="seconds"),
        },
        "level": level,
        "durationMs": seconds * 1000,
        "players": players,
    }


def build_per_player_last_seconds_asset(
    *,
    seconds: int,
    level: str,
    rows: Iterable[LastSecondsMoveRow],
    exported_at: datetime | None = None,
    prompt: str | None = None,
    session_start_since: datetime | None = None,
    repair_move_order_enabled: bool = True,
) -> dict[str, Any]:
    """Build one asset with each player/session normalized to its own final window."""
    if seconds <= 0:
        raise ValueError("Seconds must be greater than zero.")

    grouped: dict[tuple[str, str, datetime, datetime], list[LastSecondsMoveRow]] = (
        defaultdict(list)
    )
    for row in rows:
        validate_row(
            MoveRow(
                player_id=row.player_id,
                timestamp=row.timestamp,
                x=row.x,
                y=row.y,
                direction=row.direction,
                stamina=row.stamina,
            )
        )
        if row.window_end < row.window_start:
            raise ValueError("Window end timestamp must not be before window start.")
        grouped[(row.session_id, row.player_id, row.window_start, row.window_end)].append(row)

    if not grouped:
        raise ValueError("No successful final-window move rows found.")

    player_counts: dict[str, int] = defaultdict(int)
    for _, player_id, _, _ in grouped:
        player_counts[player_id] += 1

    players = []
    starts = []
    ends = []
    for (session_id, player_id, window_start, window_end), window_rows in sorted(
        grouped.items(), key=lambda item: (item[0][2], item[0][1], item[0][0])
    ):
        starts.append(window_start)
        ends.append(window_end)
        replay_player_id = player_id
        if player_counts[player_id] > 1:
            replay_player_id = f"{player_id}@{session_id[:8]}"

        window_rows = sorted(window_rows, key=lambda event: event.timestamp)
        repaired_rows = (
            repair_move_order(window_rows) if repair_move_order_enabled else window_rows
        )
        events = []
        for row, time_slot in zip(repaired_rows, window_rows, strict=True):
            events.append(replay_event(row, window_start, time_slot))
        players.append(
            {
                "playerId": replay_player_id,
                "characterClass": character_class_for(player_id),
                "events": events,
            }
        )

    exported_at = exported_at or datetime.now(timezone.utc).replace(tzinfo=None)
    asset: dict[str, Any] = {
        "schema": SCHEMA,
        "source": {
            "sessionId": "multiple",
            "mode": "per-player-last-seconds",
            "seconds": seconds,
            "startTime": min(starts).isoformat(),
            "endTime": max(ends).isoformat(),
            "exportedAt": exported_at.isoformat(timespec="seconds"),
        },
        "description": {
            "prompt": prompt
            or f"Last {seconds} seconds for every player/session.",
            "summary": (
                f"One simultaneous replay of every player/session's final {seconds} seconds "
                "of successful movement. Each track is normalized so t=0 is that track's "
                "final replay-window start."
            ),
            "selection": {
                "mode": "per-player-last-seconds",
                "players": [player["playerId"] for player in players],
                "timeWindow": f"per-player final {seconds} seconds",
            },
        },
        "level": level,
        "durationMs": seconds * 1000,
        "players": players,
    }
    if session_start_since is not None:
        asset["source"]["sessionStartSince"] = session_start_since.isoformat()
        asset["description"]["selection"]["sessionStartSince"] = (
            session_start_since.isoformat()
        )
    return asset


def build_last_push_riddle_try_asset(
    *,
    level: str,
    rows: Iterable[LastRiddleTryMoveRow],
    exported_at: datetime | None = None,
    prompt: str | None = None,
    session_limit: int | None = None,
    since: datetime | None = None,
    repair_move_order_enabled: bool = True,
) -> dict[str, Any]:
    """Build one replay containing every successful final push-riddle session try."""
    grouped: dict[tuple[str, str, datetime, datetime, int], list[LastRiddleTryMoveRow]] = (
        defaultdict(list)
    )
    for row in rows:
        validate_row(
            MoveRow(
                player_id=row.player_id,
                timestamp=row.timestamp,
                x=row.x,
                y=row.y,
                direction=row.direction,
                stamina=row.stamina,
            )
        )
        grouped[
            (row.session_id, row.player_id, row.try_start, row.end_time, row.try_count)
        ].append(row)

    if not grouped:
        raise ValueError("No successful final push-riddle try move rows found.")

    player_counts: dict[str, int] = defaultdict(int)
    for _, player_id, _, _, _ in grouped:
        player_counts[player_id] += 1

    players = []
    durations = []
    starts = []
    ends = []
    for (session_id, player_id, try_start, end_time, try_count), segment_rows in sorted(
        grouped.items(), key=lambda item: (item[0][2], item[0][1], item[0][0])
    ):
        starts.append(try_start)
        ends.append(end_time)
        duration_ms = millis_between(try_start, end_time)
        durations.append(duration_ms)
        replay_player_id = player_id
        if player_counts[player_id] > 1:
            replay_player_id = f"{player_id}@{session_id[:8]}"

        segment_rows = sorted(segment_rows, key=lambda event: event.timestamp)
        repaired_rows = (
            repair_move_order(segment_rows) if repair_move_order_enabled else segment_rows
        )
        events = []
        for row, time_slot in zip(repaired_rows, segment_rows, strict=True):
            events.append(replay_event(row, try_start, time_slot))
        players.append(
            {
                "playerId": replay_player_id,
                "characterClass": character_class_for(player_id),
                "events": events,
            }
        )

    exported_at = exported_at or datetime.now(timezone.utc).replace(tzinfo=None)
    asset: dict[str, Any] = {
        "schema": SCHEMA,
        "source": {
            "sessionId": "multiple",
            "mode": "last-successful-push-riddle-try",
            "objectId": "riddle_push_puzzle",
            "riddle": "Push Puzzle",
            "startTime": min(starts).isoformat(),
            "endTime": max(ends).isoformat(),
            "exportedAt": exported_at.isoformat(timespec="seconds"),
        },
        "description": {
            "prompt": prompt
            or "Last successful push-riddle try for every player/session.",
            "summary": (
                "One simultaneous replay of each session's last successful Push Puzzle try. "
                "If any player starts or solves the try, every player with Push Puzzle moves "
                "in that session window is included. Each track is normalized so t=0 is that "
                "session's final successful try start."
            ),
            "selection": {
                "mode": "last-successful-push-riddle-try",
                "players": [player["playerId"] for player in players],
                "timeWindow": "per-session try_start to solved",
            },
        },
        "level": level,
        "durationMs": max(durations),
        "players": players,
    }
    if session_limit is not None:
        asset["source"]["sessionLimit"] = session_limit
        asset["description"]["selection"]["sessionLimit"] = session_limit
    if since is not None:
        asset["source"]["since"] = since.isoformat()
        asset["description"]["selection"]["since"] = since.isoformat()
    return asset


def validate_window(start: datetime, end: datetime) -> None:
    """Validate that the requested replay time window is non-empty."""
    if end <= start:
        raise ValueError("End timestamp must be after start timestamp.")


def validate_row(row: MoveRow) -> None:
    """Validate one move row before writing it to the asset."""
    if not row.player_id:
        raise ValueError("Move row is missing player_id.")
    if row.x is None or row.y is None:
        raise ValueError(f"Move row for {row.player_id} is missing x or y.")
    direction = row.direction.upper() if row.direction else ""
    if direction not in VALID_DIRECTIONS:
        raise ValueError(f"Invalid direction for {row.player_id}: {row.direction!r}")
    if row.stamina is not None and row.stamina < 0:
        raise ValueError(f"Invalid stamina for {row.player_id}: {row.stamina!r}")


def repair_move_order(rows: Iterable[MoveRow]) -> list[MoveRow]:
    """Repair clear local out-of-order absolute move positions without changing row history."""
    remaining = sorted(rows, key=lambda event: event.timestamp)
    if len(remaining) < 3:
        return remaining

    repaired = [remaining.pop(0)]
    while remaining:
        first = remaining[0]
        if move_is_consistent(repaired[-1], first):
            repaired.append(remaining.pop(0))
            continue

        candidate_index = adjacent_candidate_index(repaired[-1], remaining)
        if candidate_index is None:
            repaired.append(remaining.pop(0))
        else:
            repaired.append(remaining.pop(candidate_index))
    return repaired


def adjacent_candidate_index(current: MoveRow, candidates: list[MoveRow]) -> int | None:
    """Return a unique locally consistent candidate inside the lookahead window."""
    window_start = candidates[0].timestamp
    skipped = candidates[0]
    matches = []
    for index, candidate in enumerate(candidates[1:], start=1):
        if millis_between(window_start, candidate.timestamp) > MOVE_REPAIR_LOOKAHEAD_MS:
            break
        if move_is_consistent(current, candidate) and move_is_consistent(candidate, skipped):
            matches.append(index)
    if len(matches) == 1:
        return matches[0]
    return None


def move_is_consistent(previous: MoveRow, row: MoveRow) -> bool:
    """Return whether a row matches the local snapped position and direction."""
    direction = movement_direction(previous, row)
    if direction is None:
        return False
    return direction == row.direction.upper()


def movement_direction(first: MoveRow, second: MoveRow) -> str | None:
    """Return the replay runtime direction between two snapped positions."""
    first_x, first_y = snapped_position(first)
    second_x, second_y = snapped_position(second)
    dx = second_x - first_x
    dy = second_y - first_y
    abs_dx = abs(dx)
    abs_dy = abs(dy)
    if abs_dx < 0.01 and abs_dy < 0.01:
        return "NONE"
    if abs_dx > ADJACENT_MOVE_DISTANCE or abs_dy > ADJACENT_MOVE_DISTANCE:
        return None
    if abs_dx >= abs_dy:
        return "RIGHT" if dx > 0 else "LEFT"
    return "UP" if dy > 0 else "DOWN"


def snapped_position(row: MoveRow) -> tuple[float, float]:
    """Return the source position snapped with the same threshold as the Java replay runtime."""
    return snap_coordinate(row.x), snap_coordinate(row.y)


def snap_coordinate(coordinate: float) -> float:
    """Snap coordinates below .99 down and .99 or above up."""
    floored = int(coordinate // 1)
    if coordinate - floored >= SNAP_UP_THRESHOLD:
        return float(floored + 1)
    return float(floored)


def replay_event(row: MoveRow, start: datetime, time_slot: MoveRow | None = None) -> dict[str, Any]:
    """Convert a move row into a replay event, preserving original time when repaired."""
    event_time = (time_slot or row).timestamp
    event = {
        "t": millis_between(start, event_time),
        "x": row.x,
        "y": row.y,
        "dir": row.direction.upper(),
    }
    if row.stamina is not None:
        event["stamina"] = row.stamina
    source_t = millis_between(start, row.timestamp)
    reordered = time_slot is not None and row != time_slot
    if source_t != event["t"] or reordered:
        event["sourceT"] = source_t
    if reordered:
        event["reordered"] = True
    return event


def millis_between(start: datetime, end: datetime) -> int:
    """Return the rounded millisecond difference between two timestamps."""
    return round((end - start).total_seconds() * 1000)


def load_rows(
    *,
    db_url: str,
    session_id: str,
    player_ids: list[str],
    start: datetime,
    end: datetime,
    docker_container: str | None = None,
) -> list[MoveRow]:
    """Load successful move rows from Postgres."""
    if docker_container:
        return load_rows_from_docker(
            docker_container=docker_container,
            db_url=db_url,
            session_id=session_id,
            player_ids=player_ids,
            start=start,
            end=end,
        )

    placeholders = ", ".join(["%s"] * len(player_ids))
    sql = f"""
        SELECT
          player_id,
          timestamp,
          (result->'pos'->>'x')::double precision AS x,
          (result->'pos'->>'y')::double precision AS y,
          result->>'direction' AS direction,
          result->>'stamina' AS stamina
        FROM public.xapi_statements
        WHERE verb = 'moved'
          AND result->>'success' = 'true'
          AND result ? 'pos'
          AND session_id = %s
          AND player_id IN ({placeholders})
          AND timestamp BETWEEN %s AND %s
        ORDER BY timestamp ASC, player_id ASC
    """
    params = [session_id, *player_ids, start, end]
    connection_factory = postgres_connection_factory()
    with connection_factory(db_url) as connection:
        with connection.cursor() as cursor:
            cursor.execute(sql, params)
            return [
                MoveRow(
                    player_id=row[0],
                    timestamp=row[1],
                    x=row[2],
                    y=row[3],
                    direction=row[4],
                    stamina=parse_stamina(row[5]),
                )
                for row in cursor.fetchall()
            ]


def load_per_player_first_seconds_rows(
    *,
    db_url: str,
    seconds: int,
    session_id: str | None = None,
    docker_container: str | None = None,
) -> dict[str, list[MoveRow]]:
    """Load each player's first N seconds of successful move rows."""
    if docker_container:
        return load_per_player_first_seconds_rows_from_docker(
            docker_container=docker_container,
            db_url=db_url,
            seconds=seconds,
            session_id=session_id,
        )

    session_filter = "AND session_id = %s" if session_id else ""
    sql = f"""
        WITH first_move AS (
          SELECT player_id, min(timestamp) AS first_timestamp
          FROM public.xapi_statements statement
          WHERE verb = 'moved'
            AND result->>'success' = 'true'
            AND result ? 'pos'
            {session_filter}
          GROUP BY player_id
        )
        SELECT
          statement.player_id,
          statement.timestamp,
          (statement.result->'pos'->>'x')::double precision AS x,
          (statement.result->'pos'->>'y')::double precision AS y,
          statement.result->>'direction' AS direction,
          statement.result->>'stamina' AS stamina
        FROM public.xapi_statements statement
        JOIN first_move ON first_move.player_id = statement.player_id
        WHERE statement.verb = 'moved'
          AND statement.result->>'success' = 'true'
          AND statement.result ? 'pos'
          AND statement.timestamp >= first_move.first_timestamp
          AND statement.timestamp < first_move.first_timestamp + (%s * interval '1 second')
        ORDER BY statement.player_id ASC, statement.timestamp ASC
    """
    params = [seconds] if not session_id else [session_id, seconds]
    connection_factory = postgres_connection_factory()
    with connection_factory(db_url) as connection:
        with connection.cursor() as cursor:
            cursor.execute(sql, params)
            return group_rows(
                MoveRow(
                    player_id=row[0],
                    timestamp=row[1],
                    x=row[2],
                    y=row[3],
                    direction=row[4],
                    stamina=parse_stamina(row[5]),
                )
                for row in cursor.fetchall()
            )


def load_per_player_last_seconds_rows(
    *,
    db_url: str,
    seconds: int,
    session_id: str | None = None,
    session_start_since: datetime | None = None,
    docker_container: str | None = None,
) -> list[LastSecondsMoveRow]:
    """Load each player/session's final N seconds of successful move rows."""
    validate_seconds(seconds)
    if docker_container:
        return load_per_player_last_seconds_rows_from_docker(
            docker_container=docker_container,
            db_url=db_url,
            seconds=seconds,
            session_id=session_id,
            session_start_since=session_start_since,
        )

    selected_sessions_cte = last_seconds_selected_sessions_sql(
        session_id=session_id,
        session_start_since=session_start_since,
        docker_sql=False,
    )
    sql = f"""
        WITH selected_sessions AS (
          {selected_sessions_cte}
        ),
        player_windows AS (
          SELECT
            statement.session_id::text AS session_id,
            statement.player_id,
            greatest(
              min(statement.timestamp),
              max(statement.timestamp) - (%s * interval '1 second')
            ) AS window_start,
            max(statement.timestamp) AS window_end
          FROM public.xapi_statements statement
          JOIN selected_sessions
            ON selected_sessions.session_id = statement.session_id
          WHERE statement.verb = 'moved'
            AND statement.result->>'success' = 'true'
            AND statement.result ? 'pos'
          GROUP BY statement.session_id, statement.player_id
        )
        SELECT
          player_windows.session_id,
          statement.player_id,
          player_windows.window_start,
          player_windows.window_end,
          statement.timestamp,
          (statement.result->'pos'->>'x')::double precision AS x,
          (statement.result->'pos'->>'y')::double precision AS y,
          statement.result->>'direction' AS direction,
          statement.result->>'stamina' AS stamina
        FROM player_windows
        JOIN public.xapi_statements statement
          ON statement.session_id::text = player_windows.session_id
         AND statement.player_id = player_windows.player_id
        WHERE statement.verb = 'moved'
          AND statement.result->>'success' = 'true'
          AND statement.result ? 'pos'
          AND statement.timestamp BETWEEN player_windows.window_start
            AND player_windows.window_end
        ORDER BY player_windows.window_start ASC, statement.player_id ASC, statement.timestamp ASC
    """
    params = last_seconds_selected_sessions_params(
        session_id=session_id, session_start_since=session_start_since
    )
    params.append(seconds)
    connection_factory = postgres_connection_factory()
    with connection_factory(db_url) as connection:
        with connection.cursor() as cursor:
            cursor.execute(sql, params)
            return [
                LastSecondsMoveRow(
                    session_id=row[0],
                    player_id=row[1],
                    window_start=row[2],
                    window_end=row[3],
                    timestamp=row[4],
                    x=row[5],
                    y=row[6],
                    direction=row[7],
                    stamina=parse_stamina(row[8]),
                )
                for row in cursor.fetchall()
            ]


def load_last_push_riddle_try_rows(
    *,
    db_url: str,
    session_id: str | None = None,
    recent_session_limit: int | None = None,
    since: datetime | None = None,
    docker_container: str | None = None,
) -> list[LastRiddleTryMoveRow]:
    """Load moves for every session's last successful push-riddle try."""
    validate_recent_session_limit(recent_session_limit)
    if docker_container:
        return load_last_push_riddle_try_rows_from_docker(
            docker_container=docker_container,
            db_url=db_url,
            session_id=session_id,
            recent_session_limit=recent_session_limit,
            since=since,
        )

    selected_sessions_cte = selected_sessions_sql(
        session_id=session_id,
        recent_session_limit=recent_session_limit,
        docker_sql=False,
    )
    since_clause = last_push_riddle_since_clause(
        timestamp_expression="try_events.try_start", since=since, docker_sql=False
    )
    sql = f"""
        WITH selected_sessions AS (
          {selected_sessions_cte}
        ),
        try_events AS (
          SELECT
            statement.session_id::text AS session_id,
            statement.player_id,
            statement.timestamp AS try_start,
            COALESCE((statement.result->>'tries')::integer, 0) AS try_count
          FROM public.xapi_statements
          JOIN selected_sessions
            ON selected_sessions.session_id = statement.session_id
          WHERE statement.verb = 'tries'
            AND statement.object_id = 'riddle_push_puzzle'
        ),
        successful_try_events AS (
          SELECT
            try_events.*,
            (
              SELECT min(statement.timestamp)
              FROM public.xapi_statements statement
              WHERE statement.session_id::text = try_events.session_id
                AND statement.verb = 'solved'
                AND statement.object_id = 'riddle_push_puzzle'
                AND statement.timestamp >= try_events.try_start
            ) AS end_time
          FROM try_events
          WHERE 1 = 1
            {since_clause}
        ),
        selected_try AS (
          SELECT DISTINCT ON (session_id)
            session_id,
            player_id AS trigger_player_id,
            try_start,
            end_time,
            try_count
          FROM successful_try_events
          WHERE end_time IS NOT NULL
          ORDER BY session_id, try_start DESC
        )
        SELECT
          selected_try.session_id,
          statement.player_id,
          selected_try.try_start,
          selected_try.end_time,
          selected_try.try_count,
          statement.timestamp,
          (statement.result->'pos'->>'x')::double precision AS x,
          (statement.result->'pos'->>'y')::double precision AS y,
          statement.result->>'direction' AS direction,
          statement.result->>'stamina' AS stamina
        FROM selected_try
        JOIN public.xapi_statements statement
          ON statement.session_id::text = selected_try.session_id
        WHERE statement.verb = 'moved'
          AND statement.result->>'success' = 'true'
          AND statement.result ? 'pos'
          AND statement.context->>'riddle' = 'Push Puzzle'
          AND statement.timestamp BETWEEN selected_try.try_start AND selected_try.end_time
        ORDER BY selected_try.try_start ASC, statement.player_id ASC, statement.timestamp ASC
    """
    params = selected_sessions_params(
        session_id=session_id, recent_session_limit=recent_session_limit
    )
    if since is not None:
        params.append(since)
    connection_factory = postgres_connection_factory()
    with connection_factory(db_url) as connection:
        with connection.cursor() as cursor:
            cursor.execute(sql, params)
            return [
                LastRiddleTryMoveRow(
                    session_id=row[0],
                    player_id=row[1],
                    try_start=row[2],
                    end_time=row[3],
                    try_count=row[4],
                    timestamp=row[5],
                    x=row[6],
                    y=row[7],
                    direction=row[8],
                    stamina=parse_stamina(row[9]),
                )
                for row in cursor.fetchall()
            ]


def load_rows_from_docker(
    *,
    docker_container: str,
    db_url: str,
    session_id: str,
    player_ids: list[str],
    start: datetime,
    end: datetime,
) -> list[MoveRow]:
    """Load successful move rows by running psql inside a Docker container."""
    parsed_url = urlparse(db_url)
    db_user = parsed_url.username or "dungeon_master"
    db_name = parsed_url.path.lstrip("/") or "dungeon_analytics"
    players = ", ".join(sql_literal(player_id) for player_id in player_ids)
    sql = f"""
        COPY (
          SELECT
            player_id,
            timestamp,
            (result->'pos'->>'x')::double precision AS x,
            (result->'pos'->>'y')::double precision AS y,
            result->>'direction' AS direction,
            result->>'stamina' AS stamina
          FROM public.xapi_statements
          WHERE verb = 'moved'
            AND result->>'success' = 'true'
            AND result ? 'pos'
            AND session_id = {sql_literal(session_id)}
            AND player_id IN ({players})
            AND timestamp BETWEEN {sql_literal(start.isoformat(sep=" "))}
              AND {sql_literal(end.isoformat(sep=" "))}
          ORDER BY timestamp ASC, player_id ASC
        ) TO STDOUT WITH CSV HEADER
    """
    result = subprocess.run(
        [
            "docker",
            "exec",
            docker_container,
            "psql",
            "-U",
            db_user,
            "-d",
            db_name,
            "-q",
            "-c",
            sql,
        ],
        capture_output=True,
        check=True,
        text=True,
    )
    return [
        MoveRow(
            player_id=row["player_id"],
            timestamp=parse_timestamp(row["timestamp"]),
            x=float(row["x"]),
            y=float(row["y"]),
            direction=row["direction"],
            stamina=parse_stamina(row["stamina"]),
        )
        for row in csv.DictReader(io.StringIO(result.stdout))
    ]


def load_per_player_first_seconds_rows_from_docker(
    *,
    docker_container: str,
    db_url: str,
    seconds: int,
    session_id: str | None = None,
) -> dict[str, list[MoveRow]]:
    """Load each player's first N seconds using psql inside a Docker container."""
    parsed_url = urlparse(db_url)
    db_user = parsed_url.username or "dungeon_master"
    db_name = parsed_url.path.lstrip("/") or "dungeon_analytics"
    session_filter = (
        f"AND session_id = {sql_literal(session_id)}"
        if session_id
        else ""
    )
    sql = f"""
        COPY (
          WITH first_move AS (
            SELECT player_id, min(timestamp) AS first_timestamp
            FROM public.xapi_statements
            WHERE verb = 'moved'
              AND result->>'success' = 'true'
              AND result ? 'pos'
              {session_filter}
            GROUP BY player_id
          )
          SELECT
            statement.player_id,
            statement.timestamp,
            (statement.result->'pos'->>'x')::double precision AS x,
            (statement.result->'pos'->>'y')::double precision AS y,
            statement.result->>'direction' AS direction,
            statement.result->>'stamina' AS stamina
          FROM public.xapi_statements statement
          JOIN first_move ON first_move.player_id = statement.player_id
          WHERE statement.verb = 'moved'
            AND statement.result->>'success' = 'true'
            AND statement.result ? 'pos'
            AND statement.timestamp >= first_move.first_timestamp
            AND statement.timestamp < first_move.first_timestamp + ({seconds} * interval '1 second')
          ORDER BY statement.player_id ASC, statement.timestamp ASC
        ) TO STDOUT WITH CSV HEADER
    """
    result = subprocess.run(
        [
            "docker",
            "exec",
            docker_container,
            "psql",
            "-U",
            db_user,
            "-d",
            db_name,
            "-q",
            "-c",
            sql,
        ],
        capture_output=True,
        check=True,
        text=True,
    )
    return group_rows(
        MoveRow(
            player_id=row["player_id"],
            timestamp=parse_timestamp(row["timestamp"]),
            x=float(row["x"]),
            y=float(row["y"]),
            direction=row["direction"],
            stamina=parse_stamina(row["stamina"]),
        )
        for row in csv.DictReader(io.StringIO(result.stdout))
    )


def load_per_player_last_seconds_rows_from_docker(
    *,
    docker_container: str,
    db_url: str,
    seconds: int,
    session_id: str | None = None,
    session_start_since: datetime | None = None,
) -> list[LastSecondsMoveRow]:
    """Load each player/session's final N seconds using psql inside a Docker container."""
    validate_seconds(seconds)
    parsed_url = urlparse(db_url)
    db_user = parsed_url.username or "dungeon_master"
    db_name = parsed_url.path.lstrip("/") or "dungeon_analytics"
    selected_sessions_cte = last_seconds_selected_sessions_sql(
        session_id=session_id,
        session_start_since=session_start_since,
        docker_sql=True,
    )
    sql = f"""
        COPY (
          WITH selected_sessions AS (
            {selected_sessions_cte}
          ),
          player_windows AS (
            SELECT
              statement.session_id::text AS session_id,
              statement.player_id,
              greatest(
                min(statement.timestamp),
                max(statement.timestamp) - ({seconds} * interval '1 second')
              ) AS window_start,
              max(statement.timestamp) AS window_end
            FROM public.xapi_statements statement
            JOIN selected_sessions
              ON selected_sessions.session_id = statement.session_id
            WHERE statement.verb = 'moved'
              AND statement.result->>'success' = 'true'
              AND statement.result ? 'pos'
            GROUP BY statement.session_id, statement.player_id
          )
          SELECT
            player_windows.session_id,
            statement.player_id,
            player_windows.window_start,
            player_windows.window_end,
            statement.timestamp,
            (statement.result->'pos'->>'x')::double precision AS x,
            (statement.result->'pos'->>'y')::double precision AS y,
            statement.result->>'direction' AS direction,
            statement.result->>'stamina' AS stamina
          FROM player_windows
          JOIN public.xapi_statements statement
            ON statement.session_id::text = player_windows.session_id
           AND statement.player_id = player_windows.player_id
          WHERE statement.verb = 'moved'
            AND statement.result->>'success' = 'true'
            AND statement.result ? 'pos'
            AND statement.timestamp BETWEEN player_windows.window_start
              AND player_windows.window_end
          ORDER BY player_windows.window_start ASC, statement.player_id ASC, statement.timestamp ASC
        ) TO STDOUT WITH CSV HEADER
    """
    result = subprocess.run(
        [
            "docker",
            "exec",
            docker_container,
            "psql",
            "-U",
            db_user,
            "-d",
            db_name,
            "-q",
            "-c",
            sql,
        ],
        capture_output=True,
        check=True,
        text=True,
    )
    return [
        LastSecondsMoveRow(
            session_id=row["session_id"],
            player_id=row["player_id"],
            window_start=parse_timestamp(row["window_start"]),
            window_end=parse_timestamp(row["window_end"]),
            timestamp=parse_timestamp(row["timestamp"]),
            x=float(row["x"]),
            y=float(row["y"]),
            direction=row["direction"],
            stamina=parse_stamina(row["stamina"]),
        )
        for row in csv.DictReader(io.StringIO(result.stdout))
    ]


def load_last_push_riddle_try_rows_from_docker(
    *,
    docker_container: str,
    db_url: str,
    session_id: str | None = None,
    recent_session_limit: int | None = None,
    since: datetime | None = None,
) -> list[LastRiddleTryMoveRow]:
    """Load every session's last successful push-riddle try using Docker psql."""
    validate_recent_session_limit(recent_session_limit)
    parsed_url = urlparse(db_url)
    db_user = parsed_url.username or "dungeon_master"
    db_name = parsed_url.path.lstrip("/") or "dungeon_analytics"
    selected_sessions_cte = selected_sessions_sql(
        session_id=session_id,
        recent_session_limit=recent_session_limit,
        docker_sql=True,
    )
    since_clause = last_push_riddle_since_clause(
        timestamp_expression="try_events.try_start", since=since, docker_sql=True
    )
    sql = f"""
        COPY (
          WITH selected_sessions AS (
            {selected_sessions_cte}
          ),
          try_events AS (
            SELECT
              statement.session_id::text AS session_id,
              statement.player_id,
              statement.timestamp AS try_start,
              COALESCE((statement.result->>'tries')::integer, 0) AS try_count
            FROM public.xapi_statements statement
            JOIN selected_sessions
              ON selected_sessions.session_id = statement.session_id
            WHERE statement.verb = 'tries'
              AND statement.object_id = 'riddle_push_puzzle'
          ),
          successful_try_events AS (
            SELECT
              try_events.*,
              (
                SELECT min(statement.timestamp)
                FROM public.xapi_statements statement
                WHERE statement.session_id::text = try_events.session_id
                  AND statement.verb = 'solved'
                  AND statement.object_id = 'riddle_push_puzzle'
                  AND statement.timestamp >= try_events.try_start
              ) AS end_time
            FROM try_events
            WHERE 1 = 1
              {since_clause}
          ),
          selected_try AS (
            SELECT DISTINCT ON (session_id)
              session_id,
              player_id AS trigger_player_id,
              try_start,
              end_time,
              try_count
            FROM successful_try_events
            WHERE end_time IS NOT NULL
            ORDER BY session_id, try_start DESC
          )
          SELECT
            selected_try.session_id,
            statement.player_id,
            selected_try.try_start,
            selected_try.end_time,
            selected_try.try_count,
            statement.timestamp,
            (statement.result->'pos'->>'x')::double precision AS x,
            (statement.result->'pos'->>'y')::double precision AS y,
            statement.result->>'direction' AS direction,
            statement.result->>'stamina' AS stamina
          FROM selected_try
          JOIN public.xapi_statements statement
            ON statement.session_id::text = selected_try.session_id
          WHERE statement.verb = 'moved'
            AND statement.result->>'success' = 'true'
            AND statement.result ? 'pos'
            AND statement.context->>'riddle' = 'Push Puzzle'
            AND statement.timestamp BETWEEN selected_try.try_start AND selected_try.end_time
          ORDER BY selected_try.try_start ASC, statement.player_id ASC, statement.timestamp ASC
        ) TO STDOUT WITH CSV HEADER
    """
    result = subprocess.run(
        [
            "docker",
            "exec",
            docker_container,
            "psql",
            "-U",
            db_user,
            "-d",
            db_name,
            "-q",
            "-c",
            sql,
        ],
        capture_output=True,
        check=True,
        text=True,
    )
    return [
        LastRiddleTryMoveRow(
            session_id=row["session_id"],
            player_id=row["player_id"],
            try_start=parse_timestamp(row["try_start"]),
            end_time=parse_timestamp(row["end_time"]),
            try_count=int(row["try_count"]),
            timestamp=parse_timestamp(row["timestamp"]),
            x=float(row["x"]),
            y=float(row["y"]),
            direction=row["direction"],
            stamina=parse_stamina(row["stamina"]),
        )
        for row in csv.DictReader(io.StringIO(result.stdout))
    ]


def group_rows(rows: Iterable[MoveRow]) -> dict[str, list[MoveRow]]:
    """Group move rows by player id."""
    grouped: dict[str, list[MoveRow]] = defaultdict(list)
    for row in rows:
        grouped[row.player_id].append(row)
    return dict(grouped)


def sql_literal(value: str) -> str:
    """Quote a value as a SQL string literal for the Docker psql export path."""
    return "'" + value.replace("'", "''") + "'"


def validate_seconds(seconds: int) -> None:
    """Validate a seconds-based replay window."""
    if seconds <= 0:
        raise ValueError("Seconds must be greater than zero.")


def validate_recent_session_limit(recent_session_limit: int | None) -> None:
    """Validate optional recent-session limit."""
    if recent_session_limit is not None and recent_session_limit <= 0:
        raise ValueError("Recent session limit must be greater than zero.")


def selected_sessions_sql(
    *,
    session_id: str | None,
    recent_session_limit: int | None,
    docker_sql: bool,
) -> str:
    """Return SQL that selects the sessions considered for a replay export."""
    if session_id:
        session_value = sql_literal(session_id) if docker_sql else "%s"
        return f"SELECT {session_value}::uuid AS session_id"

    limit_clause = ""
    if recent_session_limit is not None:
        limit_clause = f"LIMIT {recent_session_limit}" if docker_sql else "LIMIT %s"
    return f"""
          SELECT session_id
          FROM public.xapi_statements
          GROUP BY session_id
          ORDER BY max(timestamp) DESC
          {limit_clause}
    """


def selected_sessions_params(
    *, session_id: str | None, recent_session_limit: int | None
) -> list[str | int]:
    """Return parameters for selected_sessions_sql in direct Postgres mode."""
    if session_id:
        return [session_id]
    if recent_session_limit is not None:
        return [recent_session_limit]
    return []


def last_seconds_selected_sessions_sql(
    *,
    session_id: str | None,
    session_start_since: datetime | None,
    docker_sql: bool,
) -> str:
    """Return SQL that selects sessions for per-player last-seconds exports."""
    if session_id:
        session_value = sql_literal(session_id) if docker_sql else "%s"
        return f"SELECT {session_value}::uuid AS session_id"

    since_clause = ""
    if session_start_since is not None:
        if docker_sql:
            since_clause = (
                "HAVING min(timestamp) >= "
                f"{sql_literal(session_start_since.isoformat(sep=' '))}"
            )
        else:
            since_clause = "HAVING min(timestamp) >= %s"
    return f"""
          SELECT session_id
          FROM public.xapi_statements
          GROUP BY session_id
          {since_clause}
          ORDER BY min(timestamp) ASC
    """


def last_seconds_selected_sessions_params(
    *, session_id: str | None, session_start_since: datetime | None
) -> list[str | datetime]:
    """Return parameters for last_seconds_selected_sessions_sql in direct Postgres mode."""
    if session_id:
        return [session_id]
    if session_start_since is not None:
        return [session_start_since]
    return []


def last_push_riddle_since_clause(
    *, timestamp_expression: str, since: datetime | None, docker_sql: bool
) -> str:
    """Return an optional timestamp cutoff clause for last push-riddle exports."""
    if since is None:
        return ""
    if docker_sql:
        return f"AND {timestamp_expression} >= {sql_literal(since.isoformat(sep=' '))}"
    return f"AND {timestamp_expression} >= %s"


def postgres_connection_factory():
    """Return an available Postgres connection factory."""
    try:
        import psycopg

        return psycopg.connect
    except ImportError:
        try:
            import psycopg2

            return psycopg2.connect
        except ImportError as exc:
            raise RuntimeError(
                "Install psycopg to export from Postgres: "
                "python -m pip install psycopg[binary]"
            ) from exc


def write_asset(asset: dict[str, Any], output: Path) -> None:
    """Write the replay asset JSON."""
    if output.suffix.lower() != ".json":
        raise ValueError("Output path must end in .json.")
    output.parent.mkdir(parents=True, exist_ok=True)
    with output.open("w", encoding="utf-8", newline="\n") as output_file:
        output_file.write(json.dumps(asset, indent=2) + "\n")


def parse_args() -> argparse.Namespace:
    """Parse CLI arguments."""
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--db-url", required=True)
    parser.add_argument(
        "--docker-container",
        help="Optional Docker container name; runs psql inside the container instead of TCP.",
    )
    parser.add_argument("--session-id")
    parser.add_argument("--players", help="Comma-separated player ids.")
    parser.add_argument("--start")
    parser.add_argument("--end")
    parser.add_argument(
        "--all-players-first-seconds",
        type=int,
        help="Export one replay with every player's first N seconds of movement.",
    )
    parser.add_argument(
        "--all-players-last-seconds",
        type=int,
        help=(
            "Export one replay with every player/session's final N seconds of movement."
        ),
    )
    parser.add_argument(
        "--sessions-since",
        help=(
            "When using --all-players-last-seconds, only consider sessions whose first "
            "xAPI statement is at or after this timestamp, for example 2026-02-05."
        ),
    )
    parser.add_argument(
        "--last-push-riddle-try",
        action="store_true",
        help=(
            "Export one replay containing each player/session's last successful Push Puzzle try."
        ),
    )
    parser.add_argument(
        "--last-push-riddle-session-limit",
        type=int,
        help=(
            "When using --last-push-riddle-try, only consider the N most recent sessions by "
            "latest xAPI timestamp. Ignored when --session-id is set."
        ),
    )
    parser.add_argument(
        "--last-push-riddle-since",
        help=(
            "When using --last-push-riddle-try, only consider successful Push Puzzle tries "
            "started at or after this timestamp, for example 2026-02-05."
        ),
    )
    parser.add_argument(
        "--description",
        help="Optional original prompt or description to embed in generated replay metadata.",
    )
    parser.add_argument(
        "--no-repair-move-order",
        action="store_true",
        help=(
            "Preserve raw database move order instead of repairing local out-of-order positions. "
            "Useful when hydrating existing replay assets without changing their event shape."
        ),
    )
    parser.add_argument("--level", required=True)
    parser.add_argument("--output", required=True, type=Path)
    return parser.parse_args()


def main() -> int:
    """Run the xAPI replay export CLI."""
    args = parse_args()
    if args.last_push_riddle_try:
        last_push_riddle_since = (
            parse_timestamp(args.last_push_riddle_since)
            if args.last_push_riddle_since
            else None
        )
        rows = load_last_push_riddle_try_rows(
            db_url=args.db_url,
            session_id=args.session_id,
            recent_session_limit=args.last_push_riddle_session_limit,
            since=last_push_riddle_since,
            docker_container=args.docker_container,
        )
        asset = build_last_push_riddle_try_asset(
            level=args.level,
            rows=rows,
            prompt=args.description,
            session_limit=args.last_push_riddle_session_limit
            if not args.session_id
            else None,
            since=last_push_riddle_since,
            repair_move_order_enabled=not args.no_repair_move_order,
        )
    elif args.all_players_last_seconds:
        session_start_since = (
            parse_timestamp(args.sessions_since) if args.sessions_since else None
        )
        rows = load_per_player_last_seconds_rows(
            db_url=args.db_url,
            seconds=args.all_players_last_seconds,
            session_id=args.session_id,
            session_start_since=session_start_since,
            docker_container=args.docker_container,
        )
        asset = build_per_player_last_seconds_asset(
            seconds=args.all_players_last_seconds,
            level=args.level,
            rows=rows,
            prompt=args.description,
            session_start_since=session_start_since if not args.session_id else None,
            repair_move_order_enabled=not args.no_repair_move_order,
        )
    elif args.all_players_first_seconds:
        rows_by_player = load_per_player_first_seconds_rows(
            db_url=args.db_url,
            seconds=args.all_players_first_seconds,
            session_id=args.session_id,
            docker_container=args.docker_container,
        )
        asset = build_per_player_first_seconds_asset(
            session_id=args.session_id or "all",
            seconds=args.all_players_first_seconds,
            level=args.level,
            rows_by_player=rows_by_player,
            repair_move_order_enabled=not args.no_repair_move_order,
        )
    else:
        if not all([args.session_id, args.players, args.start, args.end]):
            raise ValueError(
                "--session-id, --players, --start, and --end are required "
                "unless an all-player export mode is used."
            )
        start = parse_timestamp(args.start)
        end = parse_timestamp(args.end)
        player_ids = parse_players(args.players)
        rows = load_rows(
            db_url=args.db_url,
            session_id=args.session_id,
            player_ids=player_ids,
            start=start,
            end=end,
            docker_container=args.docker_container,
        )
        asset = build_replay_asset(
            session_id=args.session_id,
            player_ids=player_ids,
            start=start,
            end=end,
            level=args.level,
            rows=rows,
            repair_move_order_enabled=not args.no_repair_move_order,
        )
    write_asset(asset, args.output)

    event_count = sum(len(player["events"]) for player in asset["players"])
    print("Exported replay:")
    print(f"  session: {args.session_id or 'all'}")
    print(f"  level: {args.level}")
    print(f"  durationMs: {asset['durationMs']}")
    print(f"  players: {len(asset['players'])}")
    print(f"  events: {event_count}")
    print(f"  output: {args.output}")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
