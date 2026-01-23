package starter;

import com.badlogic.gdx.Input;
import contrib.components.StaminaComponent;
import contrib.systems.HealthSystem;
import core.Entity;
import core.System;
import core.components.DrawComponent;
import core.components.InputComponent;
import core.components.PlayerComponent;
import core.components.PositionComponent;
import core.systems.InputManager;
import java.util.HashMap;
import java.util.Map;

/**
 * A debug system that allows testing stamina recovery through a simulated "sleep" mechanic.
 *
 * <p>When the debug key (F5 by default) is pressed, entities with a {@link PlayerComponent} and
 * {@link StaminaComponent} will enter a sleep state. During sleep, stamina is restored over a
 * configurable duration until fully replenished.
 *
 * <p>While sleeping, stamina drain is effectively bypassed as this system restores stamina faster
 * than it can be drained. The sleep state automatically ends when stamina is fully restored or when
 * the sleep duration expires.
 *
 * <p>This system is intended for testing purposes and can be used as a template for implementing a
 * proper sleep mechanic in the game.
 */
public class DebugSleepSystem extends System {

  /** The keyboard key that triggers the sleep action. */
  private static final int SLEEP_KEY = Input.Keys.F5;

  /**
   * The duration of the sleep state in milliseconds. Stamina will be fully restored over this
   * period.
   */
  private static final long SLEEP_DURATION_MS = 10_000L;

  /**
   * Map tracking entities currently in the sleep state. The value is the system time (in
   * milliseconds) when the sleep started.
   */
  private final Map<Entity, Long> sleepingEntities = new HashMap<>();

  /**
   * Map storing the stamina amount when sleep started. Used to calculate the restoration progress.
   */
  private final Map<Entity, Float> sleepStartStamina = new HashMap<>();

  /**
   * Creates a new {@code DebugSleepSystem}.
   *
   * <p>This system processes all entities that have both a {@link PlayerComponent} and a {@link
   * StaminaComponent}.
   */
  public DebugSleepSystem() {
    super(AuthoritativeSide.CLIENT, PlayerComponent.class, StaminaComponent.class);

    // Clean up when entities are removed
    onEntityRemove =
        entity -> {
          sleepingEntities.remove(entity);
          sleepStartStamina.remove(entity);
        };
  }

  /**
   * Executes the debug sleep system logic.
   *
   * <p>Checks for the sleep key press to initiate sleep, and updates stamina restoration for any
   * currently sleeping entities.
   */
  @Override
  public void execute() {
    // Check for sleep key press
    if (InputManager.isKeyJustPressed(SLEEP_KEY)) {
      filteredEntityStream(PlayerComponent.class, StaminaComponent.class).forEach(this::startSleep);
    }

    // Update sleeping entities
    filteredEntityStream(PlayerComponent.class, StaminaComponent.class)
        .filter(sleepingEntities::containsKey)
        .forEach(this::updateSleep);
  }

  /**
   * Initiates the sleep state for an entity.
   *
   * <p>If the entity is already sleeping, this method does nothing. Otherwise, it records the
   * current time and stamina level to begin the restoration process.
   *
   * @param entity the entity to put to sleep
   */
  private void startSleep(Entity entity) {
    if (sleepingEntities.containsKey(entity)) {
      return; // Already sleeping
    }

    StaminaComponent stamina =
        entity
            .fetch(StaminaComponent.class)
            .orElseThrow(
                () ->
                    new IllegalStateException(
                        "Entity missing StaminaComponent in DebugSleepSystem"));

    // Don't sleep if already at full stamina
    if (stamina.currentAmount() >= stamina.maxAmount()) {
      return;
    }

    long currentTime = java.lang.System.currentTimeMillis();
    sleepingEntities.put(entity, currentTime);
    sleepStartStamina.put(entity, stamina.currentAmount());

    onSleepStart(entity, stamina);
  }

  /**
   * Updates the sleep state for an entity, restoring stamina over time.
   *
   * <p>The stamina is interpolated from the starting amount to the maximum amount over the sleep
   * duration. When stamina is fully restored or the duration expires, the sleep state ends.
   *
   * @param entity the sleeping entity to update
   */
  private void updateSleep(Entity entity) {
    StaminaComponent stamina =
        entity
            .fetch(StaminaComponent.class)
            .orElseThrow(
                () ->
                    new IllegalStateException(
                        "Entity missing StaminaComponent in DebugSleepSystem"));

    Long sleepStartTime = sleepingEntities.get(entity);
    Float startingStamina = sleepStartStamina.get(entity);

    if (sleepStartTime == null || startingStamina == null) {
      return;
    }

    long currentTime = java.lang.System.currentTimeMillis();
    long elapsedTime = currentTime - sleepStartTime;

    // Calculate restoration progress (0.0 to 1.0)
    float progress = Math.min(1.0f, (float) elapsedTime / SLEEP_DURATION_MS);

    // Calculate target stamina based on progress
    float staminaToRestore = stamina.maxAmount() - startingStamina;
    float newStamina = startingStamina + (staminaToRestore * progress);

    stamina.currentAmount(newStamina);

    // Check if sleep should end
    if (progress >= 1.0f || stamina.currentAmount() >= stamina.maxAmount()) {
      endSleep(entity, stamina);
    }
  }

  /**
   * Ends the sleep state for an entity.
   *
   * <p>Removes the entity from the sleeping tracking maps and ensures stamina is at maximum.
   *
   * @param entity the entity waking up
   * @param stamina the entity's stamina component
   */
  private void endSleep(Entity entity, StaminaComponent stamina) {
    sleepingEntities.remove(entity);
    sleepStartStamina.remove(entity);

    // Ensure stamina is at max
    stamina.currentAmount(stamina.maxAmount());

    onSleepEnd(entity, stamina);
  }

  /**
   * Checks if an entity is currently sleeping.
   *
   * @param entity the entity to check
   * @return {@code true} if the entity is sleeping, {@code false} otherwise
   */
  public boolean isSleeping(Entity entity) {
    return sleepingEntities.containsKey(entity);
  }

  /**
   * Hook method called when an entity starts sleeping.
   *
   * <p>Override this method in subclasses to implement audio or visual feedback (e.g., sleep
   * animation, sound effect) when an entity begins sleeping.
   *
   * <p>The default implementation disables player controls and plays the death animation as a
   * placeholder for a proper sleep animation.
   *
   * @param entity the entity that started sleeping
   * @param stamina the entity's stamina component
   */
  protected void onSleepStart(Entity entity, StaminaComponent stamina) {
    // Disable player controls during sleep
    entity.fetch(InputComponent.class).ifPresent(ic -> ic.deactivateControls(true));

    // Play death animation as placeholder for sleep animation
    entity
        .fetch(DrawComponent.class)
        .ifPresent(
            dc ->
                entity
                    .fetch(PositionComponent.class)
                    .ifPresentOrElse(
                        pc -> dc.sendSignal(HealthSystem.DEATH_SIGNAL, pc.viewDirection()),
                        () -> dc.sendSignal(HealthSystem.DEATH_SIGNAL)));
  }

  /**
   * Hook method called when an entity finishes sleeping.
   *
   * <p>Override this method in subclasses to implement audio or visual feedback (e.g., wake-up
   * animation, sound effect) when an entity finishes sleeping.
   *
   * <p>The default implementation re-enables player controls and resets the animation state to
   * idle.
   *
   * @param entity the entity that finished sleeping
   * @param stamina the entity's stamina component
   */
  protected void onSleepEnd(Entity entity, StaminaComponent stamina) {
    // Re-enable player controls after sleep
    entity.fetch(InputComponent.class).ifPresent(ic -> ic.deactivateControls(false));

    // Reset animation state to idle
    entity.fetch(DrawComponent.class).ifPresent(DrawComponent::resetState);
  }
}
