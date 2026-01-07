package starter;

import core.Component;

/**
 * A component that marks an entity as currently sleeping.
 *
 * <p>When attached to an entity, this component indicates that the entity is in a sleep state.
 * During sleep, stamina is restored at a predefined rate until fully replenished.
 *
 * <p>The component stores the stamina recovery rate and the starting stamina amount when sleep
 * began, allowing the sleep system to calculate and apply stamina restoration over time.
 */
public class SleepingComponent implements Component {

  /** The default stamina recovery rate per second. */
  public static final float DEFAULT_STAMINA_RECOVERY_RATE = 5.0f;

  /** The amount of stamina restored per second while sleeping. */
  private final float staminaRecoveryRate;

  /** The stamina amount when sleep started. Used for progress tracking. */
  private final float startingStamina;

  /**
   * Creates a new {@code SleepingComponent} with the default stamina recovery rate.
   *
   * @param startingStamina the stamina amount when sleep started
   */
  public SleepingComponent(float startingStamina) {
    this(startingStamina, DEFAULT_STAMINA_RECOVERY_RATE);
  }

  /**
   * Creates a new {@code SleepingComponent} with a custom stamina recovery rate.
   *
   * @param startingStamina the stamina amount when sleep started
   * @param staminaRecoveryRate the amount of stamina to restore per second
   */
  public SleepingComponent(float startingStamina, float staminaRecoveryRate) {
    this.startingStamina = startingStamina;
    this.staminaRecoveryRate = staminaRecoveryRate;
  }

  /**
   * Returns the stamina recovery rate per second.
   *
   * @return the amount of stamina restored per second while sleeping
   */
  public float staminaRecoveryRate() {
    return staminaRecoveryRate;
  }

  /**
   * Returns the stamina amount when sleep started.
   *
   * @return the starting stamina value
   */
  public float startingStamina() {
    return startingStamina;
  }
}
