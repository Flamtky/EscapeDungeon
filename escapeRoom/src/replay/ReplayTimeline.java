package replay;

import core.utils.Direction;
import core.utils.Point;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

final class ReplayTimeline {
  private final ReplayAsset asset;
  private final boolean interpolate;
  private final long stepInterpolationMs;

  ReplayTimeline(ReplayAsset asset, boolean interpolate, double stepInterpolationSeconds) {
    if (stepInterpolationSeconds <= 0) {
      throw new IllegalArgumentException("stepInterpolationSeconds must be greater than zero.");
    }
    this.asset = asset;
    this.interpolate = interpolate;
    this.stepInterpolationMs = Math.round(stepInterpolationSeconds * 1000);
  }

  Map<String, ReplayFrame> frameAt(long elapsedMs) {
    long clampedElapsed = Math.max(0, Math.min(elapsedMs, asset.durationMs()));
    Map<String, ReplayFrame> frames = new LinkedHashMap<>();
    for (ReplayTrack track : asset.tracks()) {
      frames.put(track.playerId(), frameFor(track, clampedElapsed));
    }
    return frames;
  }

  long durationMs() {
    return asset.durationMs();
  }

  private ReplayFrame frameFor(ReplayTrack track, long elapsedMs) {
    List<ReplayEvent> events = track.events();
    ReplayEvent firstEvent = events.getFirst();
    if (elapsedMs <= firstEvent.t()) {
      return new ReplayFrame(
          track.playerId(),
          sourcePosition(firstEvent),
          firstEvent.direction(),
          false,
          elapsedMs,
          firstEvent.t(),
          firstEvent.t(),
          firstEvent.stamina());
    }

    ReplayEvent previous = firstEvent;
    for (int i = 1; i < events.size(); i++) {
      ReplayEvent next = events.get(i);
      if (elapsedMs < next.t()) {
        return between(track.playerId(), previous, next, elapsedMs);
      }
      previous = next;
    }

    return new ReplayFrame(
        track.playerId(),
        sourcePosition(previous),
        previous.direction(),
        false,
        elapsedMs,
        previous.t(),
        previous.t(),
        previous.stamina());
  }

  private ReplayFrame between(
      String playerId, ReplayEvent previous, ReplayEvent next, long elapsedMs) {
    long gap = next.t() - previous.t();
    Direction movementDirection = movementDirection(previous, next);
    if (!interpolate || gap <= 0 || movementDirection == Direction.NONE) {
      return heldFrame(playerId, previous, next, elapsedMs);
    }

    long interpolationDuration = Math.min(gap, stepInterpolationMs);
    long interpolationStart = next.t() - interpolationDuration;
    if (elapsedMs < interpolationStart) {
      return heldFrame(playerId, previous, next, elapsedMs);
    }

    float factor = (elapsedMs - interpolationStart) / (float) interpolationDuration;
    Point previousPosition = sourcePosition(previous);
    Point nextPosition = sourcePosition(next);
    Point position =
        new Point(
            lerp(previousPosition.x(), nextPosition.x(), factor),
            lerp(previousPosition.y(), nextPosition.y(), factor));
    Optional<Float> stamina = interpolatedStamina(previous, next, factor);
    return new ReplayFrame(
        playerId, position, movementDirection, true, elapsedMs, previous.t(), next.t(), stamina);
  }

  private static ReplayFrame heldFrame(
      String playerId, ReplayEvent previous, ReplayEvent next, long elapsedMs) {
    return new ReplayFrame(
        playerId,
        sourcePosition(previous),
        previous.direction(),
        false,
        elapsedMs,
        previous.t(),
        next.t(),
        previous.stamina());
  }

  private static Direction movementDirection(ReplayEvent previous, ReplayEvent next) {
    Point previousPosition = sourcePosition(previous);
    Point nextPosition = sourcePosition(next);
    float deltaX = nextPosition.x() - previousPosition.x();
    float deltaY = nextPosition.y() - previousPosition.y();
    if (Math.abs(deltaX) < 0.01f && Math.abs(deltaY) < 0.01f) {
      return Direction.NONE;
    }
    if (Math.abs(deltaX) > 1.01f || Math.abs(deltaY) > 1.01f) {
      return Direction.NONE;
    }
    if (Math.abs(deltaX) >= Math.abs(deltaY)) {
      return deltaX > 0 ? Direction.RIGHT : Direction.LEFT;
    }
    return deltaY > 0 ? Direction.UP : Direction.DOWN;
  }

  private static float lerp(float start, float end, float factor) {
    return start + ((end - start) * factor);
  }

  private static Optional<Float> interpolatedStamina(
      ReplayEvent previous, ReplayEvent next, float factor) {
    if (previous.stamina().isEmpty()) {
      return next.stamina();
    }
    if (next.stamina().isEmpty()) {
      return previous.stamina();
    }
    return Optional.of(
        lerp(previous.stamina().orElseThrow(), next.stamina().orElseThrow(), factor));
  }

  private static Point sourcePosition(ReplayEvent event) {
    return ReplaySourcePosition.snap(event.position());
  }
}
