package contrib.utils.components.ai;

import com.badlogic.gdx.ai.pfa.GraphPath;
import contrib.components.CollideComponent;
import contrib.utils.EntityUtils;
import contrib.utils.components.collide.Hitbox;
import core.Entity;
import core.Game;
import core.components.VelocityComponent;
import core.level.Tile;
import core.level.utils.LevelUtils;
import core.utils.Direction;
import core.utils.Point;
import core.utils.Vector2;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.StreamSupport;

/**
 * Utility class for AI-related operations like calculating paths.
 *
 * <p>This class provides functionality for path following with support for entities with
 * collider-based hitboxes. The pathfinding system accounts for the full hitbox bounds rather than
 * just the center point, preventing entities from attempting to navigate through walls when
 * approaching narrow passages (e.g., 1-wide doors).
 *
 * <p>Key features:
 *
 * <ul>
 *   <li>Hitbox-aware path following: checks all four corners of an entity's hitbox
 *   <li>Fallback support: gracefully handles entities without Hitbox colliders
 *   <li>Smart direction calculation: moves entity toward tile center for smooth navigation
 * </ul>
 */
public class AIUtils {

  /**
   * Sets the velocity of the passed entity so that it takes the next necessary step to get to the
   * end of the path.
   *
   * <p>This method accounts for the entity's full hitbox bounds when determining movement
   * direction. Instead of moving toward the next tile based on cardinal directions, it calculates
   * the direction from the entity's current position to the center of the next tile, allowing for
   * smoother navigation especially around corners.
   *
   * <p>For entities with Hitbox colliders, the "current tile" is determined by checking all four
   * corners of the hitbox. This prevents entities from attempting to advance to the next tile until
   * their entire hitbox has cleared the current tile, avoiding collisions at narrow passages.
   *
   * @param entity Entity moving on the path.
   * @param path Path on which the entity moves.
   */
  public static void followPath(final Entity entity, final GraphPath<Tile> path) {
    // entity is already at the end
    if (pathFinishedOrLeft(entity, path)) {
      return;
    }

    // Get the earliest tile in the path that any corner of the entity's hitbox occupies
    Tile currentTile = earliestTileOnPath(entity, path);
    Tile nextTile = findNextTile(path, currentTile);

    // currentTile not in path
    if (nextTile == null || currentTile == null) {
      return;
    }

    // Calculate direction from entity's actual position to the center of the next tile
    // This provides smoother movement around corners compared to tile-to-tile direction
    Point entityPos = EntityUtils.getPosition(entity);
    Point nextTileCenter = nextTile.position().translate(0.5f, 0.5f);
    Vector2 direction =
        Vector2.of(nextTileCenter.x() - entityPos.x(), nextTileCenter.y() - entityPos.y());

    entity
        .fetch(VelocityComponent.class)
        .ifPresent(vc -> vc.applyForce("MOVEMENT", direction.normalize().scale(vc.baseSpeed())));
  }

  /**
   * Checks if the entity is either on the end of the path or has left the path.
   *
   * @param entity Entity to be checked.
   * @param path Path which the entity possibly left or has reached the end of.
   * @return true if the entity is on the end of the path or has left the path, otherwise false.
   */
  public static boolean pathFinishedOrLeft(final Entity entity, final GraphPath<Tile> path) {
    return pathFinished(entity, path) || pathLeft(entity, path);
  }

  /**
   * Checks if the entity is on the end of the path.
   *
   * <p>For entities with a Hitbox collider, this checks if <strong>all four corners</strong> of the
   * hitbox are on the last tile of the path. This ensures the entity has fully reached the
   * destination before the pathfinding is considered complete.
   *
   * <p>For other entities, falls back to center-point detection.
   *
   * @param entity Entity to be checked.
   * @param path Path on which the entity possible reached the end.
   * @return true if the entity is on the last tile of the path; false otherwise.
   */
  public static boolean pathFinished(final Entity entity, final GraphPath<Tile> path) {
    if (path.getCount() == 0) {
      return true;
    }
    Tile lastTile = LevelUtils.lastTile(path);
    Set<Tile> tilesUnderEntity = tilesUnderEntity(entity);
    // All tiles under the entity must be the last tile (entity fully on last tile)
    return !tilesUnderEntity.isEmpty()
        && tilesUnderEntity.stream().allMatch(t -> t.equals(lastTile));
  }

  /**
   * Checks if the entity has left the path.
   *
   * <p>For entities with a Hitbox collider, this checks if <strong>none</strong> of the hitbox
   * corners remain on the path. This means the entity is considered "off path" only when it has
   * completely left the path.
   *
   * <p>For other entities, falls back to center-point detection.
   *
   * @param entity Entity to be checked.
   * @param path Path to be checked.
   * @return true if the entity's current tile is not part of the given path; false otherwise.
   */
  public static boolean pathLeft(final Entity entity, final GraphPath<Tile> path) {
    Set<Tile> tilesUnderEntity = tilesUnderEntity(entity);
    // Entity has left path if none of its tiles are on the path
    return tilesUnderEntity.stream().noneMatch(tile -> onPath(path, tile));
  }

  /**
   * Finds the next tile in the path after the current tile.
   *
   * @param path The path on which the entity is moving.
   * @param currentTile The tile the entity is currently standing on.
   * @return The next tile in the path after the current tile, or {@code null} if the current tile
   *     is not found or is at the end of the path.
   */
  private static Tile findNextTile(GraphPath<Tile> path, Tile currentTile) {
    return StreamSupport.stream(path.spliterator(), false)
        .dropWhile(t -> !t.equals(currentTile))
        .skip(1)
        .findFirst()
        .orElse(null);
  }

  /**
   * Checks if the current tile is on the given path.
   *
   * @param path The path to be checked.
   * @param currentTile The tile to look for on the path.
   * @return true if the current tile is on the path, otherwise false.
   */
  private static boolean onPath(GraphPath<Tile> path, Tile currentTile) {
    return StreamSupport.stream(path.spliterator(), false).anyMatch(t -> t.equals(currentTile));
  }

  /**
   * Calculates the direction vector from the current tile to the next tile in the path.
   *
   * @param currentTile The tile the entity is currently on.
   * @param nextTile The next tile in the path.
   * @return A direction vector pointing from the current tile to the next tile.
   */
  private static Vector2 calculateDirection(Tile currentTile, Tile nextTile) {
    Vector2 direction = Vector2.ZERO;
    for (Direction dir : currentTile.directionTo(nextTile)) {
      direction = direction.add(dir);
    }
    return direction;
  }

  /**
   * Gets all unique tiles that the entity's hitbox corners occupy.
   *
   * <p><strong>Hitbox-aware path checking:</strong> For entities with a Hitbox collider, this
   * method retrieves tiles at all four corners of the hitbox. This allows the pathfinding system to
   * detect when an entity's full body (not just its center) is on a valid path tile, preventing the
   * entity from attempting to navigate through walls at corners.
   *
   * <p><strong>Fallback behavior:</strong> For entities with other collider types (e.g., Hitcircle)
   * or no CollideComponent, the method falls back to checking the single tile at the entity's
   * center position via {@link EntityUtils#getPosition(Entity)}, which automatically handles
   * DrawComponent bounds if no CollideComponent is present.
   *
   * @param entity The entity to get tiles for.
   * @return A set of all tiles occupied by the entity's hitbox corners, or a single center tile if
   *     hitbox is unavailable. Returns an empty set if the entity is completely outside the level.
   */
  private static Set<Tile> tilesUnderEntity(final Entity entity) {
    Set<Tile> tiles = new HashSet<>();

    Optional<CollideComponent> cco = entity.fetch(CollideComponent.class);
    if (cco.isPresent() && cco.get().collider() instanceof Hitbox hitbox) {
      // Get tiles at all four corners of the hitbox for comprehensive coverage
      List<Point> corners = hitbox.absoluteCorners();
      for (Point corner : corners) {
        Game.tileAt(corner).ifPresent(tiles::add);
      }
    }

    // Fallback to center point if no tiles found (no Hitbox or tiles outside level)
    // This ensures we always have at least one tile to work with
    if (tiles.isEmpty()) {
      Game.tileAt(EntityUtils.getPosition(entity)).ifPresent(tiles::add);
    }

    return tiles;
  }

  /**
   * Finds the earliest tile in the path that any corner of the entity's hitbox occupies.
   *
   * <p><strong>Why corner-based detection?</strong> Using only the center point can cause entities
   * to attempt movement into walls. For example, at a corner near a 1-wide door, the entity's
   * center might appear to be on an accessible tile while the hitbox extends into a wall. By
   * tracking the earliest path tile that any corner occupies, we ensure the entity doesn't advance
   * to the next tile until <em>all</em> corners have cleared the current tile.
   *
   * <p><strong>Fallback:</strong> For entities without a Hitbox collider, this method falls back to
   * center-point detection via {@link #tilesUnderEntity(Entity)}.
   *
   * @param entity The entity to check.
   * @param path The path the entity is following.
   * @return The earliest tile in the path that the entity occupies, or null if none found.
   */
  private static Tile earliestTileOnPath(final Entity entity, final GraphPath<Tile> path) {
    Set<Tile> tilesUnderEntity = tilesUnderEntity(entity);

    // Find the earliest tile in the path that any corner occupies
    return StreamSupport.stream(path.spliterator(), false)
        .filter(tilesUnderEntity::contains)
        .findFirst()
        .orElse(null);
  }

  /**
   * Calculates the distance between two entities based on their positions.
   *
   * @param guard The first entity.
   * @param player The second entity.
   * @return The distance between the two entities.
   */
  public static float distanceBetweenEntities(Entity guard, Entity player) {
    Point guardPos = EntityUtils.getPosition(guard);
    Point playerPos = EntityUtils.getPosition(player);
    return Point.calculateDistance(guardPos, playerPos);
  }
}
