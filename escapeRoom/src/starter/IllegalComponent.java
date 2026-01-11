package starter;

import core.Component;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Marks the player as being illegal in the escape room prison game.
 *
 * <p>This component allows the guards to detect and take action against players who are not
 * authorized to be in area or is doing something they shouldn't be doing.
 *
 * @param reasons The list of reasons why the player is considered illegal.
 * @see guard.GuardDetectionSystem
 */
public record IllegalComponent(ArrayList<Reason> reasons) implements Component, Serializable {

  /** Constructs an IllegalComponent with a single reason. */
  public IllegalComponent(Reason... reasons) {
    this(new ArrayList<>(List.of(reasons)));
  }

  /**
   * Adds a reason to the IllegalComponent.
   *
   * @param reason The reason to add.
   */
  public void addReason(Reason reason) {
    reasons.add(reason);
  }

  /**
   * Removes a reason from the IllegalComponent.
   *
   * @param reason The reason to remove.
   */
  public void removeReason(Reason reason) {
    reasons.remove(reason);
  }

  /**
   * Checks if the IllegalComponent has a specific reason.
   *
   * @param reason The reason to check.
   * @return true if the reason is present, false otherwise.
   */
  public boolean hasReason(Reason reason) {
    return reasons.contains(reason);
  }

  /**
   * Returns if the player is considered illegal.
   *
   * @return true if the player has any reasons, false otherwise.
   */
  public boolean isIllegal() {
    return !reasons.isEmpty();
  }

  /** Reasons why the player is considered illegal. */
  public enum Reason implements Serializable {
    /** The player is in a restricted area. */
    TRESPASSING,
    /** The player is caught stealing items. */
    STEALING,
    /** The player is destroying walls or property. */
    VANDALISM
  }
}
