package replay;

import core.utils.Direction;
import core.utils.Point;
import java.util.Optional;

record ReplayEvent(long t, Point position, Direction direction, Optional<Float> stamina) {
  ReplayEvent(long t, Point position, Direction direction) {
    this(t, position, direction, Optional.empty());
  }
}
