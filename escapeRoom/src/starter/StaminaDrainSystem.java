package starter;

import contrib.components.StaminaComponent;
import contrib.systems.DebugDrawSystem;
import core.Entity;
import core.Game;
import core.System;
import core.components.PlayerComponent;
import core.components.VelocityComponent;
import core.utils.Point;

/**
 * A system that drains stamina from entities based on their current action state.
 *
 * <p>Stamina drains at different rates depending on whether the entity is moving or standing still.
 * Moving entities drain stamina faster than idle entities. The drain rates are configurable via
 * private static final constants.
 *
 * <p>This system processes entities that have both a {@link StaminaComponent} and a {@link
 * VelocityComponent}. The velocity is used to determine if the entity is currently moving.
 *
 * <p>The hook method {@link #onStaminaDepleted(Entity, StaminaComponent)} is provided for
 * subclasses to implement audio or visual feedback when stamina is fully depleted.
 *
 * <p>When {@link #SHOW_ETA_DEBUG} is enabled, the system displays the estimated time until stamina
 * depletion in the top-left corner of the screen.
 */
public class StaminaDrainSystem extends System {

  /** Whether to show the stamina ETA debug information on screen. */
  private static final boolean SHOW_ETA_DEBUG = true;

  /** The stamina drain rate per second when the entity is idle (not moving). */
  private static final float IDLE_DRAIN_RATE = 0.5f;

  /** The stamina drain rate per second when the entity is moving. */
  private static final float MOVING_DRAIN_RATE = 3.0f;

  /**
   * The velocity threshold below which an entity is considered idle. Velocities with magnitude
   * below this value are treated as stationary.
   */
  private static final float VELOCITY_THRESHOLD = 0.1f;

  /** The current drain rate being applied, used for ETA calculation. */
  private float currentDrainRate = IDLE_DRAIN_RATE;

  /**
   * Creates a new {@code StaminaDrainSystem}.
   *
   * <p>This system processes all entities that have both a {@link StaminaComponent} and a {@link
   * VelocityComponent}.
   */
  public StaminaDrainSystem() {
    super(StaminaComponent.class, VelocityComponent.class);
  }

  /**
   * Executes the stamina drain logic for all filtered entities.
   *
   * <p>For each entity, determines the drain rate based on movement state and applies the
   * appropriate stamina reduction. Also triggers hook methods when stamina reaches low or depleted
   * levels.
   */
  @Override
  public void execute() {
    filteredEntityStream(StaminaComponent.class, VelocityComponent.class)
        .forEach(this::drainStamina);
  }

  /**
   * Renders the stamina ETA debug information on the screen.
   *
   * <p>When {@link #SHOW_ETA_DEBUG} is enabled, displays the estimated time until stamina depletion
   * in the top-left corner of the screen. The ETA is calculated based on the current stamina level
   * and the active drain rate.
   *
   * @param delta the time since the last frame in seconds
   */
  @Override
  public void render(float delta) {
    if (!SHOW_ETA_DEBUG || Game.isHeadless()) {
      return;
    }

    // Get player's stamina for ETA calculation
    Game.player()
        .flatMap(player -> player.fetch(StaminaComponent.class))
        .ifPresent(
            stamina -> {
              float currentStamina = stamina.currentAmount();
              float etaSeconds = calculateEtaSeconds(currentStamina, currentDrainRate);

              String etaText =
                  String.format("Stamina ETA: %.1fs (Rate: %.1f/s)", etaSeconds, currentDrainRate);

              // Draw in top-left corner
              DebugDrawSystem.drawText(etaText, new Point(10.0f, Game.windowHeight() - 10.0f));
            });
  }

  /**
   * Calculates the estimated time in seconds until stamina is depleted.
   *
   * @param currentStamina the current stamina amount
   * @param drainRate the current drain rate per second
   * @return the estimated time until depletion in seconds, or 0 if already depleted or drain rate
   *     is zero
   */
  private float calculateEtaSeconds(float currentStamina, float drainRate) {
    if (drainRate <= 0 || currentStamina <= 0) {
      return 0;
    }
    return currentStamina / drainRate;
  }

  /**
   * Drains stamina from a single entity based on its current movement state.
   *
   * <p>The drain amount is calculated as: {@code drainRate / Game.frameRate()} to ensure
   * frame-rate-independent behavior.
   *
   * @param entity the entity to drain stamina from
   */
  private void drainStamina(Entity entity) {
    StaminaComponent stamina =
        entity
            .fetch(StaminaComponent.class)
            .orElseThrow(
                () ->
                    new IllegalStateException(
                        "Entity missing StaminaComponent in StaminaDrainSystem"));

    VelocityComponent velocity =
        entity
            .fetch(VelocityComponent.class)
            .orElseThrow(
                () ->
                    new IllegalStateException(
                        "Entity missing VelocityComponent in StaminaDrainSystem"));

    // Determine if entity is moving based on velocity magnitude
    boolean isMoving = velocity.currentVelocity().length() > VELOCITY_THRESHOLD;

    // Calculate drain rate based on movement state
    float drainRate = isMoving ? MOVING_DRAIN_RATE : IDLE_DRAIN_RATE;

    // Store current drain rate for ETA calculation (only for player entities)
    if (entity.isPresent(PlayerComponent.class)) {
      this.currentDrainRate = drainRate;
    }

    // Apply frame-rate-independent drain
    float drainAmount = drainRate / Game.frameRate();

    // Store previous stamina for threshold checks
    float previousStamina = stamina.currentAmount();

    // Drain stamina (don't go below 0)
    float newStamina = Math.max(0, stamina.currentAmount() - drainAmount);
    stamina.currentAmount(newStamina);

    // Check for stamina depletion
    if (previousStamina > 0 && newStamina <= 0) {
      onStaminaDepleted(entity, stamina);
    }
  }

  /**
   * Hook method called when an entity's stamina is fully depleted (reaches zero).
   *
   * <p>Override this method in subclasses to implement audio or visual feedback (e.g., exhaustion
   * sound, screen effect) when stamina is completely drained.
   *
   * <p>This method is called exactly once when stamina first reaches zero.
   *
   * @param entity the entity whose stamina is depleted
   * @param stamina the entity's stamina component
   */
  protected void onStaminaDepleted(Entity entity, StaminaComponent stamina) {
    // Hook for subclasses to implement audio/visual feedback
  }
}
