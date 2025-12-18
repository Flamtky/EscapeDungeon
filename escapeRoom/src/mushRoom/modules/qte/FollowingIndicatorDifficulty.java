package mushRoom.modules.qte;

/**
 * Represents difficulty configuration for the Following Indicator QTE minigame.
 *
 * <p>Defines game parameters like indicator speed, zone count, required successes, and max
 * attempts.
 *
 * <p>Difficulty scales by:
 *
 * <ul>
 *   <li>Faster indicator speed = harder
 *   <li>More zones = harder to track active one
 *   <li>More required successes = longer game
 *   <li>Fewer attempts = less room for error
 * </ul>
 */
public final class FollowingIndicatorDifficulty {
  /** Easy: Slow speed, 3 zones, 3 successes needed3 attempts. */
  public static final FollowingIndicatorDifficulty EASY =
      new FollowingIndicatorDifficulty(120f, 3, 3, 3);

  /** Medium: Moderate speed, 5 zones, 4 successes needed, 3 attempts. */
  public static final FollowingIndicatorDifficulty MEDIUM =
      new FollowingIndicatorDifficulty(180f, 5, 4, 3);

  /** Hard: Fast speed, 6 zones, 5 successes needed, 3 attempts. */
  public static final FollowingIndicatorDifficulty HARD =
      new FollowingIndicatorDifficulty(250f, 6, 5, 3);

  private final float indicatorSpeed;
  private final int zoneCount;
  private final int requiredSuccesses;
  private final int maxAttempts;

  private FollowingIndicatorDifficulty(
      float indicatorSpeed, int zoneCount, int requiredSuccesses, int maxAttempts) {
    if (indicatorSpeed <= 0) throw new IllegalArgumentException("indicatorSpeed must be positive");
    if (zoneCount < 2) throw new IllegalArgumentException("zoneCount must be at least 2");
    if (requiredSuccesses < 1)
      throw new IllegalArgumentException("requiredSuccesses must be at least 1");
    if (maxAttempts < 1) throw new IllegalArgumentException("maxAttempts must be at least 1");

    this.indicatorSpeed = indicatorSpeed;
    this.zoneCount = zoneCount;
    this.requiredSuccesses = requiredSuccesses;
    this.maxAttempts = maxAttempts;
  }

  /**
   * Creates a custom difficulty instance.
   *
   * @param indicatorSpeed degrees per second
   * @param zoneCount number of zones on the circle
   * @param requiredSuccesses hits needed to win
   * @param maxAttempts allowed misses before failure
   * @return new difficulty instance
   */
  public static FollowingIndicatorDifficulty of(
      float indicatorSpeed, int zoneCount, int requiredSuccesses, int maxAttempts) {
    return new FollowingIndicatorDifficulty(
        indicatorSpeed, zoneCount, requiredSuccesses, maxAttempts);
  }

  /** Gets the indicator rotation speed in degrees per second. */
  public float indicatorSpeed() {
    return indicatorSpeed;
  }

  /** Gets the number of zones on the circle. */
  public int zoneCount() {
    return zoneCount;
  }

  /** Gets the number of successful hits required to win. */
  public int requiredSuccesses() {
    return requiredSuccesses;
  }

  /** Gets the maximum number of failed attempts allowed. */
  public int maxAttempts() {
    return maxAttempts;
  }
}
