package contrib.components;

import java.io.Serial;
import java.io.Serializable;

/**
 * Serializable data class representing a skill's display state for network synchronization.
 *
 * <p>This class contains only the information needed to display a skill in the UI, without any
 * behavior. It is used to sync skill state from server to clients.
 *
 * <p>Includes a snapshot timestamp for client-side cooldown prediction between sync updates.
 *
 * @param name the display name of the skill
 * @param cooldownDurationMs the total cooldown duration in milliseconds
 * @param remainingCooldownMs the remaining cooldown time in milliseconds (0 if ready)
 * @param snapshotTimeMs the timestamp when this snapshot was created (System.currentTimeMillis())
 */
public record SkillData(
    String name, long cooldownDurationMs, long remainingCooldownMs, long snapshotTimeMs)
    implements Serializable {

  @Serial private static final long serialVersionUID = 1L;

  /**
   * Creates a SkillData with the current time as snapshot timestamp.
   *
   * @param name the display name of the skill
   * @param cooldownDurationMs the total cooldown duration in milliseconds
   * @param remainingCooldownMs the remaining cooldown time in milliseconds
   * @return a new SkillData instance with current timestamp
   */
  public static SkillData now(String name, long cooldownDurationMs, long remainingCooldownMs) {
    return new SkillData(name, cooldownDurationMs, remainingCooldownMs, System.currentTimeMillis());
  }

  /**
   * Returns the predicted remaining cooldown time, accounting for time elapsed since snapshot.
   *
   * <p>This enables smooth client-side countdown between server sync updates.
   *
   * @return the predicted remaining cooldown in milliseconds, minimum 0
   */
  public long predictedRemainingCooldownMs() {
    long elapsed = System.currentTimeMillis() - snapshotTimeMs;
    return Math.max(0, remainingCooldownMs - elapsed);
  }

  /**
   * Checks whether the skill can be used (cooldown has elapsed), using prediction.
   *
   * @return true if the skill is ready to use
   */
  public boolean canBeUsed() {
    return predictedRemainingCooldownMs() <= 0;
  }

  /**
   * Returns the predicted cooldown progress as a value between 0.0 and 1.0.
   *
   * <p>A value of 0.0 means the skill was just used and the full cooldown remains. A value of 1.0
   * means the cooldown has fully elapsed and the skill is ready.
   *
   * <p>Uses client-side prediction for smooth countdown between server syncs.
   *
   * @return the cooldown progress ratio (0.0 = just used, 1.0 = ready)
   */
  public float cooldownProgress() {
    if (cooldownDurationMs <= 0) {
      return 1.0f;
    }
    long predictedRemaining = predictedRemainingCooldownMs();
    if (predictedRemaining <= 0) {
      return 1.0f;
    }
    return 1.0f - (float) predictedRemaining / cooldownDurationMs;
  }
}
