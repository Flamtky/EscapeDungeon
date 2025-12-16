package mushRoom.modules.lockpick;

/**
 * Represents difficulty configuration for the lock-picking minigame.
 *
 * <p>Defines the number of rings and a notch width range (gap angle in degrees). Each ring receives
 * a random notch width within this range, determining visual gap size and alignment tolerance. The
 * target marker must fall within the notch gap to align.
 *
 * <p>Difficulty scales by:
 *
 * <ul>
 *   <li>More rings = harder
 *   <li>Smaller notch gaps = harder
 * </ul>
 *
 * <p>Predefined difficulties are available as static fields. Custom difficulties can be created via
 * {@link #of(int, float, float)} for runtime-defined configurations.
 */
public final class LockPickDifficulty {
  /** Easy: 3 rings, 35-60° gaps. Wide margins for learning. */
  public static final LockPickDifficulty EASY = new LockPickDifficulty(3, 35f, 60f);

  /** Medium: 4 rings, 15-35° gaps. Moderate timing challenge. */
  public static final LockPickDifficulty MEDIUM = new LockPickDifficulty(4, 15f, 35f);

  /** Hard: 5 rings, 5-22° gaps. Demands precision. */
  public static final LockPickDifficulty HARD = new LockPickDifficulty(5, 5f, 22f);

  private final int ringCount;
  private final float minNotchWidthDegrees;
  private final float maxNotchWidthDegrees;

  private LockPickDifficulty(
      int ringCount, float minNotchWidthDegrees, float maxNotchWidthDegrees) {
    if (ringCount < 1) throw new IllegalArgumentException("ringCount must be positive");
    if (minNotchWidthDegrees <= 0 || maxNotchWidthDegrees <= 0)
      throw new IllegalArgumentException("notch widths must be positive");
    if (minNotchWidthDegrees > maxNotchWidthDegrees)
      throw new IllegalArgumentException("min must not exceed max");
    this.ringCount = ringCount;
    this.minNotchWidthDegrees = minNotchWidthDegrees;
    this.maxNotchWidthDegrees = maxNotchWidthDegrees;
  }

  /**
   * Creates a custom difficulty instance.
   *
   * @param ringCount number of rings (≥1)
   * @param minNotchWidthDegrees minimum gap angle in degrees (>0)
   * @param maxNotchWidthDegrees maximum gap angle in degrees (>0, ≥ min)
   * @return new difficulty instance
   */
  public static LockPickDifficulty of(
      int ringCount, float minNotchWidthDegrees, float maxNotchWidthDegrees) {
    return new LockPickDifficulty(ringCount, minNotchWidthDegrees, maxNotchWidthDegrees);
  }

  public int ringCount() {
    return ringCount;
  }

  public float minNotchWidthDegrees() {
    return minNotchWidthDegrees;
  }

  public float maxNotchWidthDegrees() {
    return maxNotchWidthDegrees;
  }
}
