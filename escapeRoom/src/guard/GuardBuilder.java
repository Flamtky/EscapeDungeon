package guard;

import contrib.components.AttachmentComponent;
import contrib.components.CollideComponent;
import contrib.utils.EntityUtils;
import contrib.utils.components.ai.AIUtils;
import contrib.utils.components.ai.fight.AIChaseBehaviour;
import core.Entity;
import core.Game;
import core.components.PositionComponent;
import core.components.VelocityComponent;
import core.level.utils.LevelUtils;
import core.utils.Point;
import core.utils.Vector2;
import core.utils.components.path.SimpleIPath;
import java.util.function.BiFunction;
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
  private int alertnessLowerThreshold = 25;
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
   * @param lowerThreshold the alertness level to reset the trigger (ignored if stayOnceTriggered is
   *     true)
   * @param stayOnceTriggered whether the guard stays alert once triggered
   * @return this builder for chaining
   */
  public GuardBuilder alertnessThreshold(
      int threshold, int lowerThreshold, boolean stayOnceTriggered) {
    this.alertnessThreshold = threshold;
    this.alertnessLowerThreshold = lowerThreshold;
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
    this.fightAI(GuardCaseAI::new);
    this.transitionAI(
        () ->
            new GuardTransition(
                alertnessThreshold, alertnessLowerThreshold, stayAlertOnceTriggered));

    Entity guard = super.build(spawnPos);

    // decrease collider size
    CollideComponent cc = guard.fetch(CollideComponent.class).orElseThrow();
    cc.collider().width(0.5f);
    cc.collider().height(0.5f);
    cc.collider().offset(Vector2.of(0.25f, 0.05f));

    // Alertness component with configured view cone settings
    AlertnessComponent ac = new AlertnessComponent(viewConeAngle, viewRange);
    guard.add(ac);

    if (oldAddToGame) {
      Game.add(guard);
    }

    return guard;
  }

  private static class GuardTransition implements BiFunction<Entity, Entity, Boolean> {

    private final int threshold;
    private final int lowerThreshold;
    private final boolean stayOnceTriggered;
    private boolean triggered = false;

    /**
     * Creates a GuardTransition with specified thresholds and behavior.
     *
     * @param threshold The alertness threshold to trigger the transition
     * @param lowerThreshold The lower threshold to reset the transition (ignored if
     *     stayOnceTriggered is true)
     * @param stayOnceTriggered Whether to stay triggered once activated
     */
    public GuardTransition(int threshold, int lowerThreshold, boolean stayOnceTriggered) {
      this.threshold = threshold;
      this.lowerThreshold = lowerThreshold;
      this.stayOnceTriggered = stayOnceTriggered;
    }

    @Override
    public Boolean apply(Entity guard, Entity player) {
      AlertnessComponent ac =
          guard
              .fetch(AlertnessComponent.class)
              .orElseThrow(() -> new IllegalStateException("Guard missing AlertnessComponent"));

      if (ac.lastSeenEntity().isEmpty() || !ac.lastSeenEntity().get().equals(player)) {
        return false;
      }

      if (ac.alertness() >= threshold) {
        triggered = true;
        return true;
      }

      if (!stayOnceTriggered && ac.alertness() <= lowerThreshold) {
        triggered = false;
      }

      return triggered;
    }
  }

  private static class GuardCaseAI extends AIChaseBehaviour {

    private static final float CLOSE_DISTANCE = 0.75f;
    private Entity grabbedPlayer = null;
    private float oldMaxSpeed = -1f;

    @Override
    public void accept(final Entity guard, final Entity player) {
      if (grabbedPlayer != null) {
        // If player is already grabbed, bring them to the cell
        bringPlayerToCell(guard);
        return;
      }

      float distanceToPlayer = AIUtils.distanceBetweenEntities(guard, player);

      // Grab player if close enough and not already grabbed
      if (distanceToPlayer < CLOSE_DISTANCE && !player.isPresent(AttachmentComponent.class)) {
        grabPlayer(guard, player);
        return;
      }

      super.accept(guard, player); // Default chase behavior
    }

    private void grabPlayer(Entity guard, Entity player) {
      this.grabbedPlayer = player;

      var ac =
          new AttachmentComponent(
              Vector2.of(0.1f, 0f),
              player.fetch(PositionComponent.class).orElseThrow(),
              guard.fetch(PositionComponent.class).orElseThrow());
      player.add(ac);
      player
          .fetch(VelocityComponent.class)
          .ifPresent(
              vc -> {
                oldMaxSpeed = vc.maxSpeed();
                vc.maxSpeed(0f);
              });
      player.fetch(CollideComponent.class).ifPresent(cc -> cc.isSolid(false));
    }

    private void bringPlayerToCell(Entity guard) {
      Point cellPos =
          Game.currentLevel().map(level -> level.namedPoints().get("cell")).orElseThrow();
      Point guardPos = EntityUtils.getPosition(guard);

      var path = LevelUtils.calculatePath(guardPos, cellPos);

      if (path.getCount() <= 1) { // TODO: PathFinished not working here
        // Release player in cell
        grabbedPlayer.fetch(VelocityComponent.class).ifPresent(vc -> vc.maxSpeed(oldMaxSpeed));
        grabbedPlayer.fetch(CollideComponent.class).ifPresent(cc -> cc.isSolid(true));
        grabbedPlayer.remove(AttachmentComponent.class);
        this.grabbedPlayer = null;
        guard.fetch(AlertnessComponent.class).ifPresent(AlertnessComponent::reset);
        guard.fetch(VelocityComponent.class).ifPresent(vc -> vc.maxSpeed(3.5f));
        return;
      }

      guard
          .fetch(AlertnessComponent.class)
          .ifPresent(ac -> ac.increaseAlertness(999f, grabbedPlayer)); // keep alert
      guard
          .fetch(VelocityComponent.class)
          .ifPresent(vc -> vc.maxSpeed(5f)); // increase speed to cell
      AIUtils.followPath(guard, path);
    }
  }
}
