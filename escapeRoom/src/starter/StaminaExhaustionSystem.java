package starter;

import contrib.components.StaminaComponent;
import core.Entity;
import core.System;
import core.components.VelocityComponent;
import java.util.HashMap;
import java.util.Map;

/**
 * A system that manages the exhaustion state of entities based on their stamina levels.
 *
 * <p>When an entity's stamina is depleted (falls to or below the exhaustion threshold), the entity
 * becomes exhausted and their movement speed is reduced. The entity remains exhausted until their
 * stamina recovers above the recovery threshold, at which point normal speed is restored.
 *
 * <p>This two-threshold approach (exhaustion and recovery) prevents flickering between exhausted
 * and non-exhausted states when stamina hovers around a single threshold value.
 *
 * <p>The system stores the original maximum speed for each entity to properly restore it when the
 * entity recovers from exhaustion.
 *
 * <p>Hook methods are provided for subclasses to implement audio or visual feedback when exhaustion
 * state changes.
 */
public class StaminaExhaustionSystem extends System {

  /**
   * The speed multiplier applied when an entity is exhausted. A value of 0.2 means the entity moves
   * at 20% of their normal speed.
   */
  private static final float EXHAUSTED_SPEED_MULTIPLIER = 0.2f;

  /**
   * The stamina threshold (as absolute value) at or below which an entity becomes exhausted. When
   * stamina falls to this value or below, the entity enters the exhausted state.
   */
  private static final float EXHAUSTION_THRESHOLD = 0.0001f;

  /**
   * The stamina percentage (0.0 to 1.0) of max stamina at which an exhausted entity recovers. The
   * entity must reach this percentage of their max stamina to exit the exhausted state.
   */
  private static final float RECOVERY_THRESHOLD_PERCENT = 0.05f;

  /**
   * Map storing the original maximum speed for each entity. Used to restore speed when the entity
   * recovers from exhaustion.
   */
  private final Map<Entity, Float> originalSpeeds = new HashMap<>();

  /**
   * Creates a new {@code StaminaExhaustionSystem}.
   *
   * <p>This system processes all entities that have both a {@link StaminaComponent} and a {@link
   * VelocityComponent}.
   */
  public StaminaExhaustionSystem() {
    super(StaminaComponent.class, VelocityComponent.class);

    // Clean up stored speeds when entities are removed
    onEntityRemove = originalSpeeds::remove;
  }

  /**
   * Executes the exhaustion check for all filtered entities.
   *
   * <p>For each entity, checks if they should become exhausted or recover from exhaustion based on
   * their current stamina levels, and adjusts their movement speed accordingly.
   */
  @Override
  public void execute() {
    filteredEntityStream(StaminaComponent.class, VelocityComponent.class)
        .forEach(this::updateExhaustionState);
  }

  /**
   * Updates the exhaustion state of a single entity.
   *
   * <p>If the entity is not exhausted and their stamina falls at or below the exhaustion threshold,
   * they become exhausted and their speed is reduced.
   *
   * <p>If the entity is exhausted and their stamina rises above the recovery threshold, they
   * recover and their original speed is restored.
   *
   * @param entity the entity to update
   */
  private void updateExhaustionState(Entity entity) {
    StaminaComponent stamina =
        entity
            .fetch(StaminaComponent.class)
            .orElseThrow(
                () ->
                    new IllegalStateException(
                        "Entity missing StaminaComponent in StaminaExhaustionSystem"));

    VelocityComponent velocity =
        entity
            .fetch(VelocityComponent.class)
            .orElseThrow(
                () ->
                    new IllegalStateException(
                        "Entity missing VelocityComponent in StaminaExhaustionSystem"));

    float currentStamina = stamina.currentAmount();
    float maxStamina = stamina.maxAmount();
    float recoveryThreshold = maxStamina * RECOVERY_THRESHOLD_PERCENT;

    if (!stamina.isExhausted()) {
      // Check if entity should become exhausted
      if (currentStamina <= EXHAUSTION_THRESHOLD) {
        becomeExhausted(entity, stamina, velocity);
      }
    } else {
      // Check if entity should recover from exhaustion
      if (currentStamina >= recoveryThreshold) {
        recoverFromExhaustion(entity, stamina, velocity);
      }
    }
  }

  /**
   * Applies the exhaustion state to an entity.
   *
   * <p>Stores the entity's original speed, reduces their maximum speed by the exhaustion
   * multiplier, and marks them as exhausted.
   *
   * @param entity the entity becoming exhausted
   * @param stamina the entity's stamina component
   * @param velocity the entity's velocity component
   */
  private void becomeExhausted(
      Entity entity, StaminaComponent stamina, VelocityComponent velocity) {
    // Store original speed if not already stored
    if (!originalSpeeds.containsKey(entity)) {
      originalSpeeds.put(entity, velocity.maxSpeed());
    }

    // Apply speed reduction
    float originalSpeed = originalSpeeds.get(entity);
    velocity.maxSpeed(originalSpeed * EXHAUSTED_SPEED_MULTIPLIER);

    // Mark as exhausted
    stamina.setExhausted(true);

    // Call hook for subclasses
    onBecomeExhausted(entity, stamina);
  }

  /**
   * Removes the exhaustion state from an entity.
   *
   * <p>Restores the entity's original maximum speed and marks them as no longer exhausted.
   *
   * @param entity the entity recovering from exhaustion
   * @param stamina the entity's stamina component
   * @param velocity the entity's velocity component
   */
  private void recoverFromExhaustion(
      Entity entity, StaminaComponent stamina, VelocityComponent velocity) {
    // Restore original speed
    Float originalSpeed = originalSpeeds.remove(entity);
    if (originalSpeed != null) {
      velocity.maxSpeed(originalSpeed);
    }

    // Mark as no longer exhausted
    stamina.setExhausted(false);

    // Call hook for subclasses
    onRecoverFromExhaustion(entity, stamina);
  }

  /**
   * Hook method called when an entity becomes exhausted.
   *
   * <p>Override this method in subclasses to implement audio or visual feedback (e.g., exhaustion
   * sound, visual indicator) when an entity enters the exhausted state.
   *
   * @param entity the entity that became exhausted
   * @param stamina the entity's stamina component
   */
  protected void onBecomeExhausted(Entity entity, StaminaComponent stamina) {
    // Hook for subclasses to implement audio/visual feedback
  }

  /**
   * Hook method called when an entity recovers from exhaustion.
   *
   * <p>Override this method in subclasses to implement audio or visual feedback (e.g., recovery
   * sound, removing visual indicator) when an entity exits the exhausted state.
   *
   * @param entity the entity that recovered
   * @param stamina the entity's stamina component
   */
  protected void onRecoverFromExhaustion(Entity entity, StaminaComponent stamina) {
    // Hook for subclasses to implement audio/visual feedback
  }
}
