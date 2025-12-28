package guard;

import contrib.components.CollideComponent;
import contrib.utils.EntityUtils;
import core.Entity;
import core.Game;
import core.System;
import core.components.PositionComponent;
import core.utils.Direction;
import core.utils.Point;
import core.utils.Vector2;
import java.util.ArrayList;
import java.util.List;

/**
 * System that handles guard detection of player entities.
 *
 * <p>For each guard (entity with {@link AlertnessComponent} and {@link PositionComponent}), this
 * system:
 *
 * <ul>
 *   <li>Finds all player entities within view range
 *   <li>Checks if players are within the guard's view cone
 *   <li>Raycasts to the four corners of the player's collider to determine visibility
 *   <li>Increases alertness based on visible corners and distance
 *   <li>Decays alertness when no players are visible
 * </ul>
 *
 * <p>Debug rays are registered with {@link GuardDebugRenderer} for visualization.
 */
public class GuardDetectionSystem extends System {

  /** Factor applied to detection strength calculation. */
  private static final float INCREASE_FACTOR = 250f;

  /** Minimum distance to prevent division issues and extreme detection at close range. */
  private static final float MIN_DISTANCE = 0.5f;

  /** Maximum detection strength per frame to prevent instant alertness. */
  private static final float MAX_DETECTION_PER_FRAME = 50f;

  /**
   * Creates a new GuardDetectionSystem.
   *
   * <p>This system processes entities with both {@link AlertnessComponent} and {@link
   * PositionComponent}.
   */
  public GuardDetectionSystem() {
    super(AlertnessComponent.class, PositionComponent.class);
    // Register debug renderer for visualization
    GuardDebugRenderer.ensureRegistered();
  }

  @Override
  public void execute() {
    // Clear debug rays from previous frame
    GuardDebugRenderer.clearRays();

    // Get delta time for decay calculations (approximate from frame rate)
    float deltaTime = 1f / Game.frameRate();

    // Process each guard
    filteredEntityStream(AlertnessComponent.class, PositionComponent.class)
        .forEach(guard -> processGuard(guard, deltaTime));
  }

  /**
   * Processes detection for a single guard entity.
   *
   * @param guard the guard entity to process
   * @param deltaTime the time elapsed since the last frame in seconds
   */
  private void processGuard(Entity guard, float deltaTime) {
    AlertnessComponent alertness =
        guard
            .fetch(AlertnessComponent.class)
            .orElseThrow(() -> new IllegalStateException("Guard missing AlertnessComponent"));

    PositionComponent guardPosComp =
        guard
            .fetch(PositionComponent.class)
            .orElseThrow(() -> new IllegalStateException("Guard missing PositionComponent"));

    // Use EntityUtils.getPosition for accurate center position
    Point guardPosition = EntityUtils.getPosition(guard);
    Direction viewDir = guardPosComp.viewDirection();
    Vector2 viewDirection = Vector2.of(viewDir.x(), viewDir.y());

    // Register view cone for debug rendering
    GuardDebugRenderer.registerViewCone(
        guardPosition, viewDirection, alertness.viewConeAngle(), alertness.viewRange());

    // Find all players and calculate detection
    float totalDetection =
        Game.allPlayers()
            .map(
                player ->
                    calculatePlayerDetection(
                        guard,
                        guardPosition,
                        viewDirection,
                        alertness,
                        player)) // detection from this player
            .reduce(0f, Float::sum); // sum detection from all players

    // Apply detection or decay
    if (totalDetection > 0f) {
      float cappedDetection = Math.min(totalDetection * deltaTime, MAX_DETECTION_PER_FRAME);
      alertness.increaseAlertness(cappedDetection);
    } else {
      alertness.decayAlertness(deltaTime);
    }
  }

  /**
   * Calculates the detection strength for a single player.
   *
   * @param guard the guard entity
   * @param guardPosition the guard's position
   * @param viewDirection the guard's view direction
   * @param alertness the guard's alertness component
   * @param player the player entity to check
   * @return the detection strength (0 if not detected)
   */
  private float calculatePlayerDetection(
      Entity guard,
      Point guardPosition,
      Vector2 viewDirection,
      AlertnessComponent alertness,
      Entity player) {

    // Get player position using EntityUtils for accurate center position
    Point playerPosition = EntityUtils.getPosition(player);

    // Get player collider corners for raycasting
    List<Point> corners = getPlayerCorners(player);

    // Check if any corner of the player is within view range and view cone
    boolean anyCornerInCone = false;
    if (corners.isEmpty()) {
      // No collider, check center position
      anyCornerInCone = isInViewCone(guardPosition, viewDirection, playerPosition, alertness);
    } else {
      // Check if any corner is within range AND in the view cone
      for (Point corner : corners) {
        if (isInViewCone(guardPosition, viewDirection, corner, alertness)) {
          anyCornerInCone = true;
          break;
        }
      }
    }

    if (!anyCornerInCone) {
      return 0f;
    }

    // Calculate detection based on visible corners
    float distance = RaycastUtil.distance(guardPosition, playerPosition);

    // No collider, check center position
    if (corners.isEmpty()) {
      RaycastUtil.RaycastResult result =
          RaycastUtil.raycast(guardPosition, playerPosition, player, guard);
      GuardDebugRenderer.registerRay(guardPosition, playerPosition, result);
      if (result.hit()) {
        float effectiveDistance = Math.max(distance, MIN_DISTANCE);
        return INCREASE_FACTOR / effectiveDistance;
      }
      return 0f;
    }

    // Raycast only to corners that are within the view range and view cone
    int visibleCorners = 0;
    for (Point corner : corners) {
      // Check both distance and view cone
      boolean inCone = isInViewCone(guardPosition, viewDirection, corner, alertness);

      if (inCone) {
        RaycastUtil.RaycastResult result =
            RaycastUtil.raycast(guardPosition, corner, player, guard);
        GuardDebugRenderer.registerRay(guardPosition, corner, result);
        if (result.hit()) {
          visibleCorners++;
        }
      } else {
        // Corner not in cone or range = automatic miss
        GuardDebugRenderer.registerRay(
            guardPosition, corner, RaycastUtil.RaycastResult.outOfBounds(corner));
      }
    }

    if (visibleCorners == 0) {
      return 0f;
    }

    // Calculate detection strength: (visibleCorners / 4) * (1 / distance) * INCREASE_FACTOR
    float visibility = visibleCorners / (float) corners.size();
    float effectiveDistance = Math.max(distance, MIN_DISTANCE);
    return visibility * (INCREASE_FACTOR / effectiveDistance);
  }

  /**
   * Gets the four corners of a player's collider in world coordinates.
   *
   * @param player the player entity
   * @return list of corner points, or empty list if no collider
   */
  private List<Point> getPlayerCorners(Entity player) {
    List<Point> corners = new ArrayList<>();

    CollideComponent cc = player.fetch(CollideComponent.class).orElse(null);
    if (cc == null) {
      return corners;
    }

    corners.add(cc.collider().absoluteBottomLeft());
    corners.add(cc.collider().absoluteBottomRight());
    corners.add(cc.collider().absoluteTopLeft());
    corners.add(cc.collider().absoluteTopRight());

    return corners;
  }

  /**
   * Checks if a point is within the guard's view range and cone angle.
   *
   * @param guardPosition the guard's position
   * @param viewDirection the guard's view direction
   * @param targetPos the target position to check
   * @param alertness the guard's alertness component
   * @return true if the point is within both range and cone angle
   */
  private boolean isInViewCone(
      Point guardPosition,
      Vector2 viewDirection,
      Point targetPos,
      AlertnessComponent alertness) {
    float distance = RaycastUtil.distance(guardPosition, targetPos);
    return distance <= alertness.viewRange()
        && RaycastUtil.isInViewCone(
            guardPosition,
            viewDirection,
            targetPos,
            alertness.viewConeAngle(),
            alertness.viewRange());
  }
}
