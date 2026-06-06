package replay;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import core.utils.Direction;
import core.utils.Point;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class ReplayTimelineTest {
  @Test
  void interpolatesAcrossShortGaps() {
    ReplayTimeline timeline = new ReplayTimeline(assetWithGap(200), true, 0.25);

    ReplayFrame frame = timeline.frameAt(100).get("player#ROGUE");

    assertEquals(new Point(0.5f, 0), frame.position());
    assertEquals(Direction.RIGHT, frame.direction());
    assertTrue(frame.moving());
  }

  @Test
  void holdsBeforePredictedStepWindowAcrossLongGaps() {
    ReplayTimeline timeline = new ReplayTimeline(assetWithGap(2_000), true, 0.25);

    ReplayFrame frame = timeline.frameAt(1_000).get("player#ROGUE");

    assertEquals(new Point(0, 0), frame.position());
    assertEquals(Direction.RIGHT, frame.direction());
    assertFalse(frame.moving());
  }

  @Test
  void interpolatesInsidePredictedStepWindowAcrossLongGaps() {
    ReplayTimeline timeline = new ReplayTimeline(assetWithGap(2_000), true, 0.25);

    ReplayFrame frame = timeline.frameAt(1_875).get("player#ROGUE");

    assertEquals(new Point(0.5f, 0), frame.position());
    assertEquals(Direction.RIGHT, frame.direction());
    assertTrue(frame.moving());
  }

  @Test
  void infersMovementDirectionFromNextPosition() {
    ReplayTimeline timeline =
        new ReplayTimeline(
            new ReplayAsset(
                "dungeon.replay.v1",
                Map.of(),
                "MADungeonRoom",
                1_000,
                List.of(
                    new ReplayTrack(
                        "player#ROGUE",
                        contrib.entities.CharacterClass.ROGUE,
                        List.of(
                            new ReplayEvent(0, new Point(0, 0), Direction.DOWN),
                            new ReplayEvent(200, new Point(0, 1), Direction.RIGHT))))),
            true,
            0.25);

    ReplayFrame frame = timeline.frameAt(100).get("player#ROGUE");

    assertEquals(Direction.UP, frame.direction());
  }

  @Test
  void doesNotInterpolateNonAdjacentJumps() {
    ReplayTimeline timeline =
        new ReplayTimeline(
            new ReplayAsset(
                "dungeon.replay.v1",
                Map.of(),
                "MADungeonRoom",
                1_000,
                List.of(
                    new ReplayTrack(
                        "player#ROGUE",
                        contrib.entities.CharacterClass.ROGUE,
                        List.of(
                            new ReplayEvent(0, new Point(0, 0), Direction.RIGHT),
                            new ReplayEvent(200, new Point(2, 0), Direction.RIGHT))))),
            true,
            0.25);

    ReplayFrame frame = timeline.frameAt(100).get("player#ROGUE");

    assertEquals(new Point(0, 0), frame.position());
    assertFalse(frame.moving());
  }

  @Test
  void returnsFinalFrameAfterDuration() {
    ReplayTimeline timeline = new ReplayTimeline(assetWithGap(200), true, 0.25);

    ReplayFrame frame = timeline.frameAt(2_000).get("player#ROGUE");

    assertEquals(new Point(1, 0), frame.position());
    assertFalse(frame.moving());
  }

  @Test
  void includesFrameTimingMetadata() {
    ReplayTimeline timeline = new ReplayTimeline(assetWithGap(2_000), true, 0.25);

    ReplayFrame frame = timeline.frameAt(1_000).get("player#ROGUE");

    assertEquals(1_000, frame.currentTimeMs());
    assertEquals(0, frame.lastEventTimeMs());
    assertEquals(2_000, frame.nextEventTimeMs());
  }

  @Test
  void returnsFramesForMultiplePlayers() {
    ReplayTimeline timeline =
        new ReplayTimeline(
            new ReplayAsset(
                "dungeon.replay.v1",
                Map.of(),
                "MADungeonRoom",
                1_000,
                List.of(
                    track("first#ROGUE", new Point(0, 0), new Point(1, 0), 200),
                    track("second#APPRENTICE", new Point(2, 2), new Point(2, 3), 200))),
            true,
            0.25);

    Map<String, ReplayFrame> frames = timeline.frameAt(100);

    assertEquals(2, frames.size());
    assertEquals(new Point(0.5f, 0), frames.get("first#ROGUE").position());
    assertEquals(new Point(2, 2.5f), frames.get("second#APPRENTICE").position());
  }

  @Test
  void interpolatesBetweenSnappedSourcePositions() {
    ReplayTimeline timeline =
        new ReplayTimeline(
            new ReplayAsset(
                "dungeon.replay.v1",
                Map.of(),
                "MADungeonRoom",
                200,
                List.of(track("player#ROGUE", new Point(0.99f, 0), new Point(1.99f, 0), 200))),
            true,
            0.25);

    ReplayFrame frame = timeline.frameAt(100).get("player#ROGUE");

    assertEquals(new Point(1.5f, 0), frame.position());
    assertEquals(Direction.RIGHT, frame.direction());
    assertTrue(frame.moving());
  }

  @Test
  void interpolatesStaminaAcrossInterpolatedMovement() {
    ReplayTimeline timeline =
        new ReplayTimeline(
            new ReplayAsset(
                "dungeon.replay.v1",
                Map.of(),
                "MADungeonRoom",
                200,
                List.of(
                    new ReplayTrack(
                        "player#ROGUE",
                        contrib.entities.CharacterClass.ROGUE,
                        List.of(
                            new ReplayEvent(0, new Point(0, 0), Direction.RIGHT, Optional.of(100f)),
                            new ReplayEvent(
                                200, new Point(1, 0), Direction.RIGHT, Optional.of(80f)))))),
            true,
            0.25);

    ReplayFrame frame = timeline.frameAt(100).get("player#ROGUE");

    assertEquals(90f, frame.stamina().orElseThrow());
  }

  private static ReplayAsset assetWithGap(long gapMs) {
    return new ReplayAsset(
        "dungeon.replay.v1",
        Map.of(),
        "MADungeonRoom",
        gapMs,
        List.of(track("player#ROGUE", new Point(0, 0), new Point(1, 0), gapMs)));
  }

  private static ReplayTrack track(String playerId, Point first, Point second, long secondTime) {
    return new ReplayTrack(
        playerId,
        contrib.entities.CharacterClass.ROGUE,
        List.of(
            new ReplayEvent(0, first, Direction.RIGHT),
            new ReplayEvent(secondTime, second, Direction.RIGHT)));
  }
}
