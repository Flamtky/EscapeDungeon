from datetime import datetime
import unittest

from export_xapi_replay import (
    LastSecondsMoveRow,
    LastRiddleTryMoveRow,
    MoveRow,
    build_last_push_riddle_try_asset,
    build_per_player_last_seconds_asset,
    build_per_player_first_seconds_asset,
    build_replay_asset,
    repair_move_order,
)


class ExportXapiReplayTest(unittest.TestCase):
    def test_computes_relative_event_times(self):
        start = datetime.fromisoformat("2026-01-31T11:52:05.000000")
        end = datetime.fromisoformat("2026-01-31T11:52:06.000000")

        asset = build_replay_asset(
            session_id="38b4ce2d-c93b-4ce8-b149-f504cdfb55a8",
            player_ids=["Feder385955#APPRENTICE"],
            start=start,
            end=end,
            level="MADungeonRoom",
            exported_at=datetime.fromisoformat("2026-05-18T12:00:00"),
            rows=[
                MoveRow("Feder385955#APPRENTICE", start, 32, 26, "DOWN"),
                MoveRow(
                    "Feder385955#APPRENTICE",
                    datetime.fromisoformat("2026-01-31T11:52:05.187000"),
                    32,
                    27,
                    "DOWN",
                    98.75,
                ),
            ],
        )

        self.assertEqual(1000, asset["durationMs"])
        self.assertEqual([0, 187], [event["t"] for event in asset["players"][0]["events"]])
        self.assertEqual(
            98.75, asset["players"][0]["events"][1]["stamina"]
        )

    def test_rejects_missing_requested_player(self):
        start = datetime.fromisoformat("2026-01-31T11:52:05.000000")
        end = datetime.fromisoformat("2026-01-31T11:52:06.000000")

        with self.assertRaisesRegex(ValueError, "Missing move rows"):
            build_replay_asset(
                session_id="38b4ce2d-c93b-4ce8-b149-f504cdfb55a8",
                player_ids=["Feder385955#APPRENTICE", "Frost643667#ROGUE"],
                start=start,
                end=end,
                level="MADungeonRoom",
                rows=[MoveRow("Feder385955#APPRENTICE", start, 32, 26, "DOWN")],
            )

    def test_can_preserve_raw_move_order_when_hydrating_existing_assets(self):
        start = datetime.fromisoformat("2026-01-31T11:52:05.000000")
        end = datetime.fromisoformat("2026-01-31T11:52:06.000000")

        asset = build_replay_asset(
            session_id="38b4ce2d-c93b-4ce8-b149-f504cdfb55a8",
            player_ids=["Feder385955#APPRENTICE"],
            start=start,
            end=end,
            level="MADungeonRoom",
            repair_move_order_enabled=False,
            rows=[
                MoveRow("Feder385955#APPRENTICE", start, 1, 0, "RIGHT", 100),
                MoveRow(
                    "Feder385955#APPRENTICE",
                    datetime.fromisoformat("2026-01-31T11:52:05.100000"),
                    3,
                    0,
                    "RIGHT",
                    99,
                ),
                MoveRow(
                    "Feder385955#APPRENTICE",
                    datetime.fromisoformat("2026-01-31T11:52:05.200000"),
                    2,
                    0,
                    "RIGHT",
                    98,
                ),
            ],
        )

        events = asset["players"][0]["events"]
        self.assertEqual([1, 3, 2], [event["x"] for event in events])
        self.assertEqual([100, 99, 98], [event["stamina"] for event in events])
        self.assertNotIn("reordered", events[1])

    def test_builds_combined_per_player_first_seconds_asset(self):
        player_one_start = datetime.fromisoformat("2026-01-31T11:52:05.000000")
        player_two_start = datetime.fromisoformat("2026-01-31T11:53:10.000000")

        asset = build_per_player_first_seconds_asset(
            session_id="all",
            seconds=60,
            level="MADungeonRoom",
            exported_at=datetime.fromisoformat("2026-05-18T12:00:00"),
            rows_by_player={
                "Feder385955#APPRENTICE": [
                    MoveRow("Feder385955#APPRENTICE", player_one_start, 32, 26, "DOWN"),
                    MoveRow(
                        "Feder385955#APPRENTICE",
                        datetime.fromisoformat("2026-01-31T11:52:05.250000"),
                        32,
                        27,
                        "DOWN",
                    ),
                ],
                "Frost643667#ROGUE": [
                    MoveRow("Frost643667#ROGUE", player_two_start, 30, 26, "RIGHT"),
                    MoveRow(
                        "Frost643667#ROGUE",
                        datetime.fromisoformat("2026-01-31T11:53:10.500000"),
                        31,
                        26,
                        "RIGHT",
                    ),
                ],
            },
        )

        self.assertEqual(60000, asset["durationMs"])
        self.assertEqual(2, len(asset["players"]))
        self.assertEqual([0, 250], [event["t"] for event in asset["players"][0]["events"]])
        self.assertEqual([0, 500], [event["t"] for event in asset["players"][1]["events"]])

    def test_builds_combined_per_player_last_seconds_asset(self):
        window_start = datetime.fromisoformat("2026-02-05T13:30:00.000000")
        window_end = datetime.fromisoformat("2026-02-05T13:32:00.000000")
        later_window_start = datetime.fromisoformat("2026-02-08T16:20:00.000000")
        later_window_end = datetime.fromisoformat("2026-02-08T16:22:00.000000")
        session_start_since = datetime.fromisoformat("2026-02-05T00:00:00")

        asset = build_per_player_last_seconds_asset(
            seconds=120,
            level="MADungeonRoom",
            exported_at=datetime.fromisoformat("2026-05-18T12:00:00"),
            prompt="last 120s",
            session_start_since=session_start_since,
            rows=[
                LastSecondsMoveRow(
                    "aaaaaaaa-1111-1111-1111-111111111111",
                    "Frost643667#ROGUE",
                    window_start,
                    window_end,
                    window_start,
                    30,
                    26,
                    "RIGHT",
                ),
                LastSecondsMoveRow(
                    "aaaaaaaa-1111-1111-1111-111111111111",
                    "Frost643667#ROGUE",
                    window_start,
                    window_end,
                    datetime.fromisoformat("2026-02-05T13:30:00.250000"),
                    31,
                    26,
                    "RIGHT",
                ),
                LastSecondsMoveRow(
                    "bbbbbbbb-2222-2222-2222-222222222222",
                    "Frost643667#ROGUE",
                    later_window_start,
                    later_window_end,
                    datetime.fromisoformat("2026-02-08T16:20:00.500000"),
                    32,
                    26,
                    "RIGHT",
                ),
            ],
        )

        self.assertEqual(120000, asset["durationMs"])
        self.assertEqual("per-player-last-seconds", asset["source"]["mode"])
        self.assertEqual(120, asset["source"]["seconds"])
        self.assertEqual("2026-02-05T00:00:00", asset["source"]["sessionStartSince"])
        self.assertEqual("last 120s", asset["description"]["prompt"])
        self.assertEqual(
            "2026-02-05T00:00:00",
            asset["description"]["selection"]["sessionStartSince"],
        )
        self.assertEqual(
            [
                "Frost643667#ROGUE@aaaaaaaa",
                "Frost643667#ROGUE@bbbbbbbb",
            ],
            [player["playerId"] for player in asset["players"]],
        )
        self.assertEqual([0, 250], [event["t"] for event in asset["players"][0]["events"]])
        self.assertEqual([500], [event["t"] for event in asset["players"][1]["events"]])

    def test_builds_last_push_riddle_try_asset_with_relative_tracks(self):
        first_start = datetime.fromisoformat("2026-01-31T11:52:05.000000")
        second_start = datetime.fromisoformat("2026-01-31T11:53:10.000000")
        since = datetime.fromisoformat("2026-02-05T00:00:00")

        asset = build_last_push_riddle_try_asset(
            level="MADungeonRoom",
            exported_at=datetime.fromisoformat("2026-05-18T12:00:00"),
            prompt="last push try",
            session_limit=20,
            since=since,
            rows=[
                LastRiddleTryMoveRow(
                    "session-a",
                    "Feder385955#APPRENTICE",
                    first_start,
                    datetime.fromisoformat("2026-01-31T11:52:15.000000"),
                    3,
                    first_start,
                    32,
                    26,
                    "DOWN",
                ),
                LastRiddleTryMoveRow(
                    "session-a",
                    "Feder385955#APPRENTICE",
                    first_start,
                    datetime.fromisoformat("2026-01-31T11:52:15.000000"),
                    3,
                    datetime.fromisoformat("2026-01-31T11:52:05.250000"),
                    32,
                    27,
                    "DOWN",
                ),
                LastRiddleTryMoveRow(
                    "session-a",
                    "Frost643667#ROGUE",
                    first_start,
                    datetime.fromisoformat("2026-01-31T11:52:15.000000"),
                    3,
                    datetime.fromisoformat("2026-01-31T11:52:06.000000"),
                    30,
                    26,
                    "RIGHT",
                ),
                LastRiddleTryMoveRow(
                    "session-b",
                    "Frost643667#ROGUE",
                    second_start,
                    datetime.fromisoformat("2026-01-31T11:53:30.000000"),
                    5,
                    datetime.fromisoformat("2026-01-31T11:53:10.500000"),
                    30,
                    26,
                    "RIGHT",
                ),
            ],
        )

        self.assertEqual(20000, asset["durationMs"])
        self.assertEqual("last-successful-push-riddle-try", asset["source"]["mode"])
        self.assertEqual(20, asset["source"]["sessionLimit"])
        self.assertEqual("2026-02-05T00:00:00", asset["source"]["since"])
        self.assertEqual("last push try", asset["description"]["prompt"])
        self.assertEqual(20, asset["description"]["selection"]["sessionLimit"])
        self.assertEqual(
            "per-session try_start to solved",
            asset["description"]["selection"]["timeWindow"],
        )
        self.assertEqual([0, 250], [event["t"] for event in asset["players"][0]["events"]])
        self.assertEqual([1000], [event["t"] for event in asset["players"][1]["events"]])
        self.assertEqual([500], [event["t"] for event in asset["players"][2]["events"]])

    def test_repairs_clear_out_of_order_move_inside_lookahead(self):
        start = datetime.fromisoformat("2026-01-31T11:52:05.000000")
        rows = [
            MoveRow("player#ROGUE", start, 1, 0, "RIGHT"),
            MoveRow(
                "player#ROGUE",
                datetime.fromisoformat("2026-01-31T11:52:05.100000"),
                3,
                0,
                "RIGHT",
            ),
            MoveRow(
                "player#ROGUE",
                datetime.fromisoformat("2026-01-31T11:52:05.200000"),
                2,
                0,
                "RIGHT",
            ),
        ]

        repaired = repair_move_order(rows)

        self.assertEqual([1, 2, 3], [row.x for row in repaired])

    def test_keeps_ambiguous_back_and_forth_history_order(self):
        start = datetime.fromisoformat("2026-01-31T11:52:05.000000")
        rows = [
            MoveRow("player#ROGUE", start, 1, 0, "RIGHT"),
            MoveRow(
                "player#ROGUE",
                datetime.fromisoformat("2026-01-31T11:52:05.100000"),
                2,
                0,
                "RIGHT",
            ),
            MoveRow(
                "player#ROGUE",
                datetime.fromisoformat("2026-01-31T11:52:05.200000"),
                1,
                0,
                "LEFT",
            ),
        ]

        repaired = repair_move_order(rows)

        self.assertEqual([1, 2, 1], [row.x for row in repaired])

    def test_keeps_jump_backtrack_when_direction_supports_history(self):
        start = datetime.fromisoformat("2026-01-31T11:52:05.000000")
        rows = [
            MoveRow("player#ROGUE", start, 1, 0, "RIGHT"),
            MoveRow(
                "player#ROGUE",
                datetime.fromisoformat("2026-01-31T11:52:05.100000"),
                3,
                0,
                "RIGHT",
            ),
            MoveRow(
                "player#ROGUE",
                datetime.fromisoformat("2026-01-31T11:52:05.200000"),
                2,
                0,
                "LEFT",
            ),
        ]

        repaired = repair_move_order(rows)

        self.assertEqual([1, 3, 2], [row.x for row in repaired])

    def test_keeps_diagonal_move_runtime_treats_as_adjacent(self):
        start = datetime.fromisoformat("2026-01-31T11:52:05.000000")
        rows = [
            MoveRow("player#ROGUE", start, 0, 0, "RIGHT"),
            MoveRow(
                "player#ROGUE",
                datetime.fromisoformat("2026-01-31T11:52:05.100000"),
                1,
                1,
                "RIGHT",
            ),
            MoveRow(
                "player#ROGUE",
                datetime.fromisoformat("2026-01-31T11:52:05.200000"),
                2,
                1,
                "RIGHT",
            ),
        ]

        repaired = repair_move_order(rows)

        self.assertEqual([(0, 0), (1, 1), (2, 1)], [(row.x, row.y) for row in repaired])

    def test_keeps_out_of_order_move_outside_lookahead(self):
        start = datetime.fromisoformat("2026-01-31T11:52:05.000000")
        rows = [
            MoveRow("player#ROGUE", start, 1, 0, "RIGHT"),
            MoveRow(
                "player#ROGUE",
                datetime.fromisoformat("2026-01-31T11:52:05.100000"),
                3,
                0,
                "RIGHT",
            ),
            MoveRow(
                "player#ROGUE",
                datetime.fromisoformat("2026-01-31T11:52:06.200000"),
                2,
                0,
                "RIGHT",
            ),
        ]

        repaired = repair_move_order(rows)

        self.assertEqual([1, 3, 2], [row.x for row in repaired])

    def test_repaired_asset_keeps_sorted_replay_time_and_source_time(self):
        start = datetime.fromisoformat("2026-01-31T11:52:05.000000")
        end = datetime.fromisoformat("2026-01-31T11:52:06.000000")

        asset = build_replay_asset(
            session_id="session",
            player_ids=["player#ROGUE"],
            start=start,
            end=end,
            level="MADungeonRoom",
            exported_at=datetime.fromisoformat("2026-05-18T12:00:00"),
            rows=[
                MoveRow("player#ROGUE", start, 1, 0, "RIGHT"),
                MoveRow(
                    "player#ROGUE",
                    datetime.fromisoformat("2026-01-31T11:52:05.100000"),
                    3,
                    0,
                    "RIGHT",
                ),
                MoveRow(
                    "player#ROGUE",
                    datetime.fromisoformat("2026-01-31T11:52:05.200000"),
                    2,
                    0,
                    "RIGHT",
                ),
            ],
        )

        events = asset["players"][0]["events"]
        self.assertEqual([0, 100, 200], [event["t"] for event in events])
        self.assertEqual([1, 2, 3], [event["x"] for event in events])
        self.assertEqual(200, events[1]["sourceT"])
        self.assertEqual(100, events[2]["sourceT"])

    def test_repaired_same_timestamp_events_are_marked_reordered(self):
        start = datetime.fromisoformat("2026-01-31T11:52:05.000000")
        end = datetime.fromisoformat("2026-01-31T11:52:06.000000")

        asset = build_replay_asset(
            session_id="session",
            player_ids=["player#ROGUE"],
            start=start,
            end=end,
            level="MADungeonRoom",
            exported_at=datetime.fromisoformat("2026-05-18T12:00:00"),
            rows=[
                MoveRow("player#ROGUE", start, 1, 0, "RIGHT"),
                MoveRow(
                    "player#ROGUE",
                    datetime.fromisoformat("2026-01-31T11:52:05.100000"),
                    3,
                    0,
                    "RIGHT",
                ),
                MoveRow(
                    "player#ROGUE",
                    datetime.fromisoformat("2026-01-31T11:52:05.100000"),
                    2,
                    0,
                    "RIGHT",
                ),
            ],
        )

        events = asset["players"][0]["events"]
        self.assertEqual([1, 2, 3], [event["x"] for event in events])
        self.assertEqual([100, 100], [event["sourceT"] for event in events[1:]])
        self.assertEqual([True, True], [event["reordered"] for event in events[1:]])

    def test_per_player_first_seconds_validates_rows_before_repair(self):
        start = datetime.fromisoformat("2026-01-31T11:52:05.000000")

        with self.assertRaisesRegex(ValueError, "missing x or y"):
            build_per_player_first_seconds_asset(
                session_id="session",
                seconds=20,
                level="MADungeonRoom",
                rows_by_player={
                    "player#ROGUE": [
                        MoveRow("player#ROGUE", start, 1, 0, "RIGHT"),
                        MoveRow(
                            "player#ROGUE",
                            datetime.fromisoformat("2026-01-31T11:52:05.100000"),
                            None,
                            0,
                            "RIGHT",
                        ),
                        MoveRow(
                            "player#ROGUE",
                            datetime.fromisoformat("2026-01-31T11:52:05.200000"),
                            2,
                            0,
                            "RIGHT",
                        ),
                    ]
                },
            )


if __name__ == "__main__":
    unittest.main()
