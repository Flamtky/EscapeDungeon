package guard;

import contrib.components.CollideComponent;
import core.Entity;
import core.Game;
import core.components.DrawComponent;
import core.components.PositionComponent;
import core.utils.Direction;
import core.utils.Point;
import core.utils.components.path.SimpleIPath;

/**
 * Builder for creating guard entities in the escape room.
 *
 * <p>Guards are entities that detect players within their view cone and track alertness. They use
 * the knight texture and include components for position, drawing, collision, and alertness.
 *
 * <p>Example usage:
 *
 * <pre>{@code
 * Entity guard = new GuardBuilder()
 *     .viewConeAngle(90f)
 *     .viewRange(15f)
 *     .build(spawnPoint);
 * }</pre>
 *
 * @see AlertnessComponent
 * @see GuardDetectionSystem
 */
public class GuardBuilder {

  /** Default texture path for guard entities. */
  private static final String DEFAULT_TEXTURE_PATH = "character/knight";

  /** Default view direction for guards. */
  private static final Direction DEFAULT_VIEW_DIRECTION = Direction.DOWN;

  private float viewConeAngle = 90f;
  private float viewRange = 20f;
  private Direction viewDirection = DEFAULT_VIEW_DIRECTION;
  private boolean addToGame = false;

  /**
   * Creates a new GuardBuilder with default settings.
   *
   * <p>Default view cone angle is 90 degrees, default view range is 20 tiles.
   */
  public GuardBuilder() {
    // Default constructor
  }

  /**
   * Sets the view cone angle for the guard.
   *
   * @param angle the view cone angle in degrees (full angle, not half-angle)
   * @return this builder for chaining
   */
  public GuardBuilder viewConeAngle(float angle) {
    this.viewConeAngle = angle;
    return this;
  }

  /**
   * Sets the maximum view range for the guard.
   *
   * @param range the view range in tiles
   * @return this builder for chaining
   */
  public GuardBuilder viewRange(float range) {
    this.viewRange = range;
    return this;
  }

  /**
   * Sets the initial view direction for the guard.
   *
   * @param direction the direction the guard faces
   * @return this builder for chaining
   */
  public GuardBuilder viewDirection(Direction direction) {
    this.viewDirection = direction;
    return this;
  }

  /**
   * Configures whether the entity should be automatically added to the game after building.
   *
   * @param add true to add to game automatically, false otherwise
   * @return this builder for chaining
   */
  public GuardBuilder addToGame(boolean add) {
    this.addToGame = add;
    return this;
  }

  /**
   * Builds the guard entity at the specified spawn position.
   *
   * <p>The guard entity will have:
   *
   * <ul>
   *   <li>{@link PositionComponent} - positioned at the spawn point with the configured view
   *       direction
   *   <li>{@link DrawComponent} - using the knight texture
   *   <li>{@link CollideComponent} - for collision detection (solid)
   *   <li>{@link AlertnessComponent} - for detection tracking with configured view cone settings
   * </ul>
   *
   * @param spawnPos the position to spawn the guard at
   * @return the built guard entity
   */
  public Entity build(Point spawnPos) {
    Entity guard = new Entity("Guard");

    // Position component with view direction
    PositionComponent pc = new PositionComponent(spawnPos, viewDirection);
    guard.add(pc);

    // Draw component with knight texture
    DrawComponent dc = new DrawComponent(new SimpleIPath(DEFAULT_TEXTURE_PATH));
    guard.add(dc);

    // Collide component (solid by default)
    CollideComponent cc = new CollideComponent();
    cc.isSolid(true);
    guard.add(cc);

    // Alertness component with configured view cone settings
    AlertnessComponent ac = new AlertnessComponent(viewConeAngle, viewRange);
    guard.add(ac);

    if (addToGame) {
      Game.add(guard);
    }

    return guard;
  }
}
