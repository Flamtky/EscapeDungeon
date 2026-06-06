package replay;

import core.utils.Point;

final class ReplaySourcePosition {
  private static final float SNAP_UP_THRESHOLD = 0.99f;

  private ReplaySourcePosition() {}

  static Point snap(Point position) {
    return new Point(snappedCoordinate(position.x()), snappedCoordinate(position.y()));
  }

  private static float snappedCoordinate(float coordinate) {
    float floor = (float) Math.floor(coordinate);
    if (coordinate - floor >= SNAP_UP_THRESHOLD) {
      return floor + 1f;
    }
    return floor;
  }
}
