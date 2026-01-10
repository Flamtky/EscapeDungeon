package contrib.components;

import java.io.Serial;
import java.io.Serializable;

/**
 * Serializable data class representing a skill's display state for network synchronization.
 *
 * <p>This class contains only the information needed to display a skill in the UI, without any
 * behavior. It is used to sync skill state from server to clients.
 *
 * @param name the display name of the skill
 * @param cooldownDurationMs the total cooldown duration in milliseconds
 * @param remainingCooldownMs the remaining cooldown time in milliseconds (0 if ready)
 */
public record SkillData(String name, long cooldownDurationMs, long remainingCooldownMs)
    implements Serializable {

  @Serial private static final long serialVersionUID = 1L;

  /**
   * Checks whether the skill can be used (cooldown has elapsed).
   *
   * @return true if the skill is ready to use
   */
  public boolean canBeUsed() {
    return remainingCooldownMs <= 0;
  }

  /**
   * Returns the cooldown progress as a value between 0.0 and 1.0.
   *
   * <p>A value of 0.0 means the skill was just used and the full cooldown remains. A value of 1.0
   * means the cooldown has fully elapsed and the skill is ready.
   *
   * @return the cooldown progress ratio (0.0 = just used, 1.0 = ready)
   */
  public float cooldownProgress() {
    if (cooldownDurationMs <= 0) {
      return 1.0f;
    }
    if (remainingCooldownMs <= 0) {
      return 1.0f;
    }
    return 1.0f - (float) remainingCooldownMs / cooldownDurationMs;
  }
}
