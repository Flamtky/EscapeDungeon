package replay;

import core.utils.Direction;
import core.utils.Point;
import java.util.Optional;

record ReplayFrame(
    String playerId,
    Point position,
    Direction direction,
    boolean moving,
    long currentTimeMs,
    long lastEventTimeMs,
    long nextEventTimeMs,
    Optional<Float> stamina) {
  ReplayFrame(
      String playerId,
      Point position,
      Direction direction,
      boolean moving,
      long currentTimeMs,
      long lastEventTimeMs,
      long nextEventTimeMs) {
    this(
        playerId,
        position,
        direction,
        moving,
        currentTimeMs,
        lastEventTimeMs,
        nextEventTimeMs,
        Optional.empty());
  }
}
