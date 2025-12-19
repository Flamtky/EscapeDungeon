package contrib.components;

import core.Component;

/**
 * A component representing an stamina pool for an entity.
 *
 * <p>This component manages stamina values, including the maximum amount, current amount, and the
 * natural restoration rate per second. It provides methods for consuming, restoring, and modifying
 * energy.
 */
public class StaminaComponent implements Component, BarDisplayable {

  /** The maximum amount of stamina the entity can have. */
  private float maxAmount;

  /** The current amount of stamina available to the entity. */
  private float currentAmount;

  /** The amount of stamina restored per second. */
  private float restorePerSecond;

  /** Whether the entity is currently in an exhausted state due to depleted stamina. */
  private boolean exhausted;

  /**
   * Creates a new {@code EnergyComponent} with the given maximum stamina, initial stamina, and
   * restoration rate.
   *
   * @param maxAmount the maximum stamina capacity
   * @param currentAmount the initial stamina amount (capped at {@code maxAmount})
   * @param restorePerSecond the amount of stamina restored per second
   */
  public StaminaComponent(float maxAmount, float currentAmount, float restorePerSecond) {
    this.maxAmount = maxAmount;
    this.currentAmount = Math.min(currentAmount, maxAmount);
    this.restorePerSecond = restorePerSecond;
    this.exhausted = false;
  }

  /**
   * Returns the maximum stamina capacity.
   *
   * @return the maximum amount of stamina
   */
  public float maxAmount() {
    return maxAmount;
  }

  /**
   * Returns the current stamina amount.
   *
   * @return the current stamina value
   */
  public float currentAmount() {
    return currentAmount;
  }

  /**
   * Sets a new maximum stamina capacity.
   *
   * <p>If the current stamina exceeds the new maximum, it will be reduced to match the maximum.
   *
   * @param maxAmount the new maximum stamina
   */
  public void maxAmount(float maxAmount) {
    this.maxAmount = maxAmount;
    if (currentAmount > maxAmount) {
      currentAmount = maxAmount;
    }
  }

  /**
   * Sets the current stamina amount.
   *
   * <p>The value is capped so that it does not exceed the maximum stamina.
   *
   * @param currentAmount the new current stamina
   */
  public void currentAmount(float currentAmount) {
    this.currentAmount = Math.min(currentAmount, maxAmount);
  }

  /**
   * Increases the maximum stamina capacity by the given amount.
   *
   * @param amount the amount to increase
   */
  public void increaseMaxAmount(float amount) {
    maxAmount(maxAmount + amount);
  }

  /**
   * Decreases the maximum stamina capacity by the given amount.
   *
   * <p>The new maximum will not fall below zero.
   *
   * @param amount the amount to decrease
   */
  public void decreaseMaxAmount(float amount) {
    maxAmount(Math.max(0, maxAmount - amount));
  }

  /**
   * Consumes a given amount of stamina if enough is available.
   *
   * @param amount the amount of stamina to consume
   * @return {@code true} if the stamina was successfully consumed, {@code false} if there was not
   *     enough stamina
   */
  public boolean consume(float amount) {
    if (amount <= currentAmount) {
      currentAmount -= amount;
      return true;
    }
    return false;
  }

  /**
   * Restores a given amount of stamina to the pool.
   *
   * <p>The current stamina will not exceed the maximum stamina capacity.
   *
   * @param amount the amount of stamina to restore
   */
  public void restore(float amount) {
    currentAmount = Math.min(currentAmount + amount, maxAmount);
  }

  /**
   * Returns the stamina restored per second.
   *
   * @return the restoration rate per second
   */
  public float restorePerSecond() {
    return restorePerSecond;
  }

  /**
   * Sets the stamina restored per second.
   *
   * @param restorePerSecond the new restoration rate
   */
  public void restorePerSecond(float restorePerSecond) {
    this.restorePerSecond = restorePerSecond;
  }

  @Override
  public float current() {
    return currentAmount();
  }

  @Override
  public float max() {
    return maxAmount();
  }

  @Override
  public String barStyleName() {
    return "staminabar";
  }

  @Override
  public int barPriority() {
    return 2;
  }

  /**
   * Returns whether the entity is currently in an exhausted state.
   *
   * <p>An entity becomes exhausted when their stamina is fully depleted and remains exhausted until
   * stamina recovers above a certain threshold.
   *
   * @return {@code true} if the entity is exhausted, {@code false} otherwise
   */
  public boolean isExhausted() {
    return exhausted;
  }

  /**
   * Sets the exhausted state of the entity.
   *
   * <p>This is typically called by systems that manage stamina depletion and recovery to track when
   * the entity should have reduced capabilities (e.g., slower movement speed).
   *
   * @param exhausted {@code true} to mark the entity as exhausted, {@code false} otherwise
   */
  public void setExhausted(boolean exhausted) {
    this.exhausted = exhausted;
  }
}
