package guard;

import contrib.components.BarDisplayable;
import core.Component;
import core.Entity;
import java.util.*;

/**
 * Component that tracks the alertness level of a guard entity.
 *
 * <p>Alertness increases when the guard detects a player and decays over time when no player is
 * visible. The component supports threshold callbacks that fire once when the alertness crosses a
 * threshold upward. Callbacks are re-armed when alertness drops below the threshold.
 *
 * <p>This component implements {@link BarDisplayable} to display an alertness bar above the guard
 * entity using the existing attribute bar system.
 *
 * @see contrib.systems.AttributeBarSystem
 */
public final class AlertnessComponent implements Component, BarDisplayable {

  /** Default maximum alertness value. */
  private static final float DEFAULT_MAX_ALERTNESS = 100f;

  /** Default decay rate per second when no player is visible. */
  private static final float DEFAULT_DECAY_RATE = 15f;

  /** Default view cone angle in degrees (full angle, not half-angle). */
  private static final float DEFAULT_VIEW_CONE_ANGLE = 90f;

  /** Default maximum view range in tiles. */
  private static final float DEFAULT_VIEW_RANGE = 20f;

  /** Priority for bar stacking (higher = further from entity). */
  private static final int BAR_PRIORITY = 1;

  private float alertness;
  private final float maxAlertness;
  private final float decayRate;
  private final float viewConeAngle;
  private final float viewRange;
  private Entity lastSeenEntity = null;

  private final Map<Float, List<Runnable>> thresholdCallbacks;
  private final Set<Float> triggeredThresholds;

  /**
   * Creates a new AlertnessComponent with default values.
   *
   * <p>Uses default max alertness (100), decay rate (5/s), view cone angle (90°), and view range
   * (20 tiles).
   */
  public AlertnessComponent() {
    this(DEFAULT_MAX_ALERTNESS, DEFAULT_DECAY_RATE, DEFAULT_VIEW_CONE_ANGLE, DEFAULT_VIEW_RANGE);
  }

  /**
   * Creates a new AlertnessComponent with custom view cone settings.
   *
   * @param viewConeAngle the view cone angle in degrees
   * @param viewRange the maximum view range in tiles
   */
  public AlertnessComponent(float viewConeAngle, float viewRange) {
    this(DEFAULT_MAX_ALERTNESS, DEFAULT_DECAY_RATE, viewConeAngle, viewRange);
  }

  /**
   * Creates a new AlertnessComponent with fully customized settings.
   *
   * @param maxAlertness the maximum alertness value
   * @param decayRate the decay rate per second
   * @param viewConeAngle the view cone angle in degrees
   * @param viewRange the maximum view range in tiles
   */
  public AlertnessComponent(
      float maxAlertness, float decayRate, float viewConeAngle, float viewRange) {
    this.alertness = 0f;
    this.maxAlertness = maxAlertness;
    this.decayRate = decayRate;
    this.viewConeAngle = viewConeAngle;
    this.viewRange = viewRange;
    this.thresholdCallbacks = new HashMap<>();
    this.triggeredThresholds = new HashSet<>();
  }

  /**
   * Registers a callback to be executed when alertness crosses the specified threshold upward.
   *
   * <p>The callback fires once when alertness rises above the threshold. It is re-armed when
   * alertness drops below the threshold, allowing it to fire again on the next upward crossing.
   *
   * <p>Multiple callbacks can be registered for the same threshold.
   *
   * @param threshold the alertness value threshold (0 to maxAlertness)
   * @param callback the action to execute when the threshold is crossed upward
   */
  public void registerCallback(float threshold, Runnable callback) {
    thresholdCallbacks.computeIfAbsent(threshold, k -> new ArrayList<>()).add(callback);
  }

  /**
   * Increases the alertness by the specified amount.
   *
   * <p>The alertness is clamped to the maximum value. When alertness crosses a registered threshold
   * upward for the first time, the associated callbacks are fired and the threshold is marked as
   * triggered.
   *
   * @param amount the amount to increase alertness by (should be positive)
   * @param seenEntity the entity that was seen to increase alertness
   */
  public void increaseAlertness(float amount, Entity seenEntity) {
    if (amount <= 0) return;

    float oldAlertness = this.alertness;
    this.alertness = Math.min(this.alertness + amount, maxAlertness);

    this.lastSeenEntity = seenEntity;

    // Check for threshold crossings
    for (Map.Entry<Float, List<Runnable>> entry : thresholdCallbacks.entrySet()) {
      float threshold = entry.getKey();
      // Fire if we crossed upward and haven't triggered yet
      if (oldAlertness < threshold && this.alertness >= threshold) {
        if (!triggeredThresholds.contains(threshold)) {
          triggeredThresholds.add(threshold);
          for (Runnable callback : entry.getValue()) {
            callback.run();
          }
        }
      }
    }
  }

  /**
   * Decreases the alertness based on the decay rate and elapsed time.
   *
   * <p>The alertness is clamped to zero. When alertness drops below a triggered threshold, that
   * threshold is re-armed and can fire again on the next upward crossing.
   *
   * @param deltaTime the time elapsed since the last update in seconds
   */
  public void decayAlertness(float deltaTime) {
    if (deltaTime <= 0) return;

    float oldAlertness = this.alertness;
    this.alertness = Math.max(0f, this.alertness - decayRate * deltaTime);

    // Re-arm thresholds that we dropped below
    for (Float threshold : thresholdCallbacks.keySet()) {
      if (oldAlertness >= threshold && this.alertness < threshold) {
        triggeredThresholds.remove(threshold);
      }
    }
  }

  /**
   * Checks if the guard is fully alerted.
   *
   * @return true if alertness has reached the maximum value
   */
  public boolean isFullyAlerted() {
    return this.alertness >= this.maxAlertness;
  }

  /** Resets the alertness to zero and re-arms all threshold callbacks. */
  public void reset() {
    this.alertness = 0f;
    this.triggeredThresholds.clear();
  }

  /**
   * Returns the current alertness value.
   *
   * @return the current alertness (0 to maxAlertness)
   */
  public float alertness() {
    return alertness;
  }

  /**
   * Returns the view cone angle in degrees.
   *
   * @return the view cone angle
   */
  public float viewConeAngle() {
    return viewConeAngle;
  }

  /**
   * Returns the maximum view range in tiles.
   *
   * @return the view range
   */
  public float viewRange() {
    return viewRange;
  }

  /**
   * Returns the decay rate per second.
   *
   * @return the decay rate
   */
  public float decayRate() {
    return decayRate;
  }

  @Override
  public float current() {
    return alertness;
  }

  @Override
  public float max() {
    return maxAlertness;
  }

  @Override
  public String barStyleName() {
    return "healthbar";
  }

  @Override
  public int barPriority() {
    return BAR_PRIORITY;
  }

  /**
   * Returns the last seen entity that increased the alertness.
   *
   * @return an Optional containing the last seen entity, or empty if none
   */
  public Optional<Entity> lastSeenEntity() {
    return Optional.ofNullable(lastSeenEntity);
  }
}
