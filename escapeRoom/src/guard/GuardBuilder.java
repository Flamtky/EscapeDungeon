package guard;

import core.Entity;
import core.Game;
import core.utils.Point;
import core.utils.components.path.SimpleIPath;
import java.util.function.Function;
import mobs.EscapeRoomMonsterBuilder;

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
public class GuardBuilder extends EscapeRoomMonsterBuilder.Builder {

  /** Default texture path for guard entities. */
  private static final String DEFAULT_TEXTURE_PATH = "character/knight";

  private float viewConeAngle = 45f;
  private float viewRange = 15f;
  private int alertnessThreshold = 100;
  private boolean stayAlertOnceTriggered = true;

  /** Creates a new GuardBuilder with default settings. */
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
   * Sets the alertness threshold and behavior for the guard.
   *
   * @param threshold the alertness threshold to trigger behavior
   * @param stayOnceTriggered whether the guard stays alert once triggered
   * @return this builder for chaining
   */
  public GuardBuilder alertnessThreshold(int threshold, boolean stayOnceTriggered) {
    this.alertnessThreshold = threshold;
    this.stayAlertOnceTriggered = stayOnceTriggered;
    return this;
  }

  /**
   * Builds the guard entity at the specified spawn position.
   *
   * @param spawnPos the position to spawn the guard
   * @return the constructed guard entity
   */
  public Entity build(Point spawnPos) {
    this.name("Guard");
    this.texture(new SimpleIPath(DEFAULT_TEXTURE_PATH));
    this.health(-1); // no health component by default
    var oldAddToGame = this.addToGame;
    this.addToGame(false); // add manually after adding alertness component
    this.transitionAI(() -> new GuardTransition(alertnessThreshold, stayAlertOnceTriggered));

    Entity guard = super.build(spawnPos);

    // Alertness component with configured view cone settings
    AlertnessComponent ac = new AlertnessComponent(viewConeAngle, viewRange);
    guard.add(ac);

    if (oldAddToGame) {
      Game.add(guard);
    }

    return guard;
  }

  private static class GuardTransition implements Function<Entity, Boolean> {

    private final int threshold;
    private final boolean stayOnceTriggered;
    private boolean triggered = false;

    public GuardTransition(int threshold, boolean stayOnceTriggered) {
      this.threshold = threshold;
      this.stayOnceTriggered = stayOnceTriggered;
    }

    @Override
    public Boolean apply(Entity entity) {
      AlertnessComponent ac =
          entity
              .fetch(AlertnessComponent.class)
              .orElseThrow(() -> new IllegalStateException("Guard missing AlertnessComponent"));

      if (ac.alertness() >= threshold) {
        triggered = stayOnceTriggered || triggered;
        return true;
      }
      return stayOnceTriggered && triggered;
    }
  }
}
