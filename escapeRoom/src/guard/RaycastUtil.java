package guard;

import contrib.components.CollideComponent;
import core.Entity;
import core.Game;
import core.level.Tile;
import core.level.utils.Coordinate;
import core.utils.Point;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * Utility class for performing raycasting operations in the dungeon.
 *
 * <p>Provides line-of-sight checks using Bresenham's line algorithm, checking both tile visibility
 * and solid entity colliders along the ray path.
 */
public final class RaycastUtil {

  private RaycastUtil() {
    // Utility class, no instantiation
  }

  /**
   * Result of a raycast operation containing hit information and what blocked the ray.
   *
   * @param hit true if the ray reached its target unobstructed
   * @param blockingTile the tile that blocked the ray, or null if not blocked by a tile
   * @param blockingEntity the entity that blocked the ray, or null if not blocked by an entity
   * @param blockPoint the point where the ray was blocked, or null if not blocked
   */
  public record RaycastResult(
      boolean hit, Tile blockingTile, Entity blockingEntity, Point blockPoint) {

    /**
     * Creates a successful (unblocked) raycast result.
     *
     * @return a result indicating the ray hit its target
     */
    public static RaycastResult success() {
      return new RaycastResult(true, null, null, null);
    }

    /**
     * Creates a result indicating the ray was blocked by a tile.
     *
     * @param tile the blocking tile
     * @param blockPoint the point where blocking occurred
     * @return a result indicating tile blockage
     */
    public static RaycastResult blockedByTile(Tile tile, Point blockPoint) {
      return new RaycastResult(false, tile, null, blockPoint);
    }

    /**
     * Creates a result indicating the ray was blocked by an entity.
     *
     * @param entity the blocking entity
     * @param blockPoint the point where blocking occurred
     * @return a result indicating entity blockage
     */
    public static RaycastResult blockedByEntity(Entity entity, Point blockPoint) {
      return new RaycastResult(false, null, entity, blockPoint);
    }

    /**
     * Creates a result indicating the ray went out of bounds.
     *
     * @param blockPoint the point where the ray went out of bounds
     * @return a result indicating out of bounds
     */
    public static RaycastResult outOfBounds(Point blockPoint) {
      return new RaycastResult(false, null, null, blockPoint);
    }

    /**
     * Returns a human-readable description of what blocked the ray.
     *
     * @return description of the blocking element
     */
    public String blockingDescription() {
      if (hit) {
        return "Clear line of sight";
      }
      if (blockingEntity != null) {
        return "Blocked by entity: " + blockingEntity.name() + " (ID: " + blockingEntity.id() + ")";
      }
      if (blockingTile != null) {
        return "Blocked by tile: "
            + blockingTile.levelElement()
            + " at "
            + blockingTile.coordinate();
      }
      return "Blocked (out of bounds)";
    }
  }

  /**
   * Checks if there is a clear line of sight between two points.
   *
   * @param from the starting point of the ray
   * @param to the target point of the ray
   * @return true if there is an unobstructed line of sight, false otherwise
   */
  public static boolean hasLineOfSight(Point from, Point to) {
    return raycast(from, to).hit();
  }

  /**
   * Performs a raycast between two points and returns detailed result information.
   *
   * <p>Uses Bresenham's line algorithm to step through tiles from the origin to the target. The ray
   * is blocked if:
   *
   * <ul>
   *   <li>A tile along the path cannot be seen through (e.g., walls)
   *   <li>A solid entity's collider intersects the ray path
   * </ul>
   *
   * @param from the starting point of the ray
   * @param to the target point of the ray
   * @return a RaycastResult containing hit status and blocking information
   */
  public static RaycastResult raycast(Point from, Point to) {
    return raycast(from, to, (Entity[]) null);
  }

  /**
   * Performs a raycast between two points and returns detailed result information, ignoring
   * specified entities.
   *
   * <p>Uses Bresenham's line algorithm to step through tiles from the origin to the target. The ray
   * is blocked if:
   *
   * <ul>
   *   <li>A tile along the path cannot be seen through (e.g., walls)
   *   <li>A solid entity's collider intersects the ray path (unless the entity is in the ignore
   *       list)
   * </ul>
   *
   * @param from the starting point of the ray
   * @param to the target point of the ray
   * @param ignoredEntities entities to ignore during the raycast (e.g., the target entity itself)
   * @return a RaycastResult containing hit status and blocking information
   */
  public static RaycastResult raycast(Point from, Point to, Entity... ignoredEntities) {
    Coordinate start = from.toCoordinate();
    Coordinate end = to.toCoordinate();

    int x = start.x();
    int y = start.y();
    int dx = Math.abs(end.x() - start.x());
    int dy = Math.abs(end.y() - start.y());

    int sx = start.x() < end.x() ? 1 : -1;
    int sy = start.y() < end.y() ? 1 : -1;
    int err = dx - dy;

    while (true) {
      Coordinate currentCoord = new Coordinate(x, y);
      Point currentPoint = new Point(x + 0.5f, y + 0.5f); // Center of tile

      // Check tile visibility
      Optional<Tile> tileOpt = Game.tileAt(currentCoord);
      if (tileOpt.isEmpty()) {
        return RaycastResult.outOfBounds(currentPoint);
      }

      Tile tile = tileOpt.get();
      if (!tile.canSeeThrough()) {
        return RaycastResult.blockedByTile(tile, currentPoint);
      }

      // Check for solid entities at this tile that block the ray
      Entity blockingEntity = findBlockingEntity(tile, from, to, ignoredEntities);
      if (blockingEntity != null) {
        return RaycastResult.blockedByEntity(blockingEntity, currentPoint);
      }

      // Check if we reached the target
      if (x == end.x() && y == end.y()) {
        break;
      }

      int e2 = 2 * err;
      if (e2 > -dy) {
        err -= dy;
        x += sx;
      }
      if (e2 < dx) {
        err += dx;
        y += sy;
      }
    }

    return RaycastResult.success();
  }

  /**
   * Finds the first solid entity at the given tile that blocks the ray.
   *
   * @param tile the tile to check for blocking entities
   * @param from the starting point of the ray
   * @param to the target point of the ray
   * @param ignoredEntities entities to ignore (can be null or empty)
   * @return the blocking entity, or null if none found
   */
  private static Entity findBlockingEntity(
      Tile tile, Point from, Point to, Entity... ignoredEntities) {
    Stream<Entity> entitiesAtTile = Game.entityAtTile(tile);

    return entitiesAtTile
        .filter(entity -> !isIgnoredEntity(entity, ignoredEntities))
        .filter(entity -> entity.fetch(CollideComponent.class).isPresent())
        .filter(entity -> entity.fetch(CollideComponent.class).get().isSolid())
        .filter(entity -> entity.fetch(CollideComponent.class).get().collider().collide(from, to))
        .findFirst()
        .orElse(null);
  }

  /**
   * Checks if an entity should be ignored during raycast checks.
   *
   * @param entity the entity to check
   * @param ignoredEntities entities to ignore
   * @return true if the entity is in the ignored list, false otherwise
   */
  private static boolean isIgnoredEntity(Entity entity, Entity... ignoredEntities) {
    if (ignoredEntities == null) {
      return false;
    }
    for (Entity ignored : ignoredEntities) {
      if (ignored != null && entity.id() == ignored.id()) {
        return true;
      }
    }
    return false;
  }

  /**
   * Calculates the distance between two points.
   *
   * @param from the first point
   * @param to the second point
   * @return the Euclidean distance between the points
   */
  public static float distance(Point from, Point to) {
    float dx = to.x() - from.x();
    float dy = to.y() - from.y();
    return (float) Math.sqrt(dx * dx + dy * dy);
  }

  /**
   * Checks if a target point is within the specified view cone.
   *
   * <p>The view cone is defined by:
   *
   * <ul>
   *   <li>Guard position as the origin
   *   <li>View direction from the guard's orientation
   *   <li>Cone angle (full angle, e.g., 90° creates a 45° half-angle on each side)
   *   <li>Circular range (maxRange) - forms a circular boundary at the edge
   * </ul>
   *
   * <p>A point is in the cone if:
   *
   * <ul>
   *   <li>It's within the circular range (distance ≤ maxRange)
   *   <li>AND it's within the angular bounds (angle from center ≤ half cone angle)
   * </ul>
   *
   * <p>This creates a proper cone shape (pie-slice with circular arc), not a triangle.
   *
   * @param guardPos the guard's position
   * @param viewDirection the guard's view direction as a normalized vector
   * @param targetPos the target point to check
   * @param coneAngle the full view cone angle in degrees
   * @param maxRange the circular radius/range of the cone
   * @return true if the target is within the cone, false otherwise
   */
  public static boolean isInViewCone(
      Point guardPos,
      core.utils.Vector2 viewDirection,
      Point targetPos,
      float coneAngle,
      float maxRange) {

    // Calculate vector from guard to target
    float dx = targetPos.x() - guardPos.x();
    float dy = targetPos.y() - guardPos.y();
    float distance = (float) Math.sqrt(dx * dx + dy * dy);

    // Check circular range first (creates the arc boundary)
    if (distance > maxRange || distance < 0.001f) {
      return false;
    }

    // Normalize direction to target
    float targetDirX = dx / distance;
    float targetDirY = dy / distance;

    // Calculate dot product with view direction
    float dot = viewDirection.x() * targetDirX + viewDirection.y() * targetDirY;

    // Calculate angle between view direction and target direction
    // dot = cos(angle), so angle = acos(dot)
    float angleRad = (float) Math.acos(Math.max(-1f, Math.min(1f, dot)));
    float angleDeg = (float) Math.toDegrees(angleRad);

    // Check if within the angular bounds (half cone angle on each side)
    return angleDeg <= coneAngle / 2f;
  }
}
