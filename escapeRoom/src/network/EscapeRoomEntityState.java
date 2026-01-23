package network;

import core.network.messages.s2c.EntityState;
import java.io.Serial;
import java.util.Optional;

/**
 * Extended EntityState that includes alertness data for guard entities.
 *
 * <p>This subclass adds fields for the {@link guard.AlertnessComponent} to synchronize guard
 * alertness state across the network in multiplayer sessions.
 *
 * @see EntityState
 * @see guard.AlertnessComponent
 */
public class EscapeRoomEntityState extends EntityState {
  @Serial private static final long serialVersionUID = 1L;

  private final Float curAlertness;
  private final Float maxAlertness;
  private final Float viewConeAngle;
  private final Float viewRange;
  private final Float decayRate;
  private final Boolean isIllegal;

  /**
   * Constructs an EscapeRoomEntityState using the provided Builder.
   *
   * @param builder the Builder containing the entity's state data
   */
  protected EscapeRoomEntityState(Builder builder) {
    super(builder);
    this.curAlertness = builder.curAlertness;
    this.maxAlertness = builder.maxAlertness;
    this.viewConeAngle = builder.viewConeAngle;
    this.viewRange = builder.viewRange;
    this.decayRate = builder.decayRate;
    this.isIllegal = builder.isIllegal;
  }

  /**
   * Gets the optional current alertness of the entity.
   *
   * @return an Optional containing the current alertness if present, otherwise an empty Optional
   */
  public Optional<Float> currentAlertness() {
    return Optional.ofNullable(curAlertness);
  }

  /**
   * Gets the optional maximum alertness of the entity.
   *
   * @return an Optional containing the maximum alertness if present, otherwise an empty Optional
   */
  public Optional<Float> maxAlertness() {
    return Optional.ofNullable(maxAlertness);
  }

  /**
   * Gets the optional view cone angle of the entity.
   *
   * @return an Optional containing the view cone angle if present, otherwise an empty Optional
   */
  public Optional<Float> viewConeAngle() {
    return Optional.ofNullable(viewConeAngle);
  }

  /**
   * Gets the optional view range of the entity.
   *
   * @return an Optional containing the view range if present, otherwise an empty Optional
   */
  public Optional<Float> viewRange() {
    return Optional.ofNullable(viewRange);
  }

  /**
   * Gets the optional decay rate of the entity.
   *
   * @return an Optional containing the decay rate if present, otherwise an empty Optional
   */
  public Optional<Float> decayRate() {
    return Optional.ofNullable(decayRate);
  }

  /**
   * Gets whether the entity is illegal.
   *
   * @return an Optional containing the illegal status if present, otherwise an empty Optional
   */
  public Optional<Boolean> isIllegal() {
    return Optional.ofNullable(isIllegal);
  }

  /**
   * Creates a new Builder instance for constructing an EscapeRoomEntityState.
   *
   * @return a new Builder instance
   */
  public static Builder builder() {
    return new Builder();
  }

  /**
   * Builder class for constructing EscapeRoomEntityState objects.
   *
   * <p>Extends {@link EntityState.Builder} to add alertness-related fields.
   */
  public static class Builder extends EntityState.Builder {
    protected Float curAlertness;
    protected Float maxAlertness;
    protected Float viewConeAngle;
    protected Float viewRange;
    protected Float decayRate;
    protected Boolean isIllegal;

    /**
     * Sets the current alertness of the entity.
     *
     * @param alertness the current alertness value
     * @return the Builder instance
     */
    public Builder currentAlertness(Float alertness) {
      this.curAlertness = alertness;
      return this;
    }

    /**
     * Sets the maximum alertness of the entity.
     *
     * @param maxAlertness the maximum alertness value
     * @return the Builder instance
     */
    public Builder maxAlertness(Float maxAlertness) {
      this.maxAlertness = maxAlertness;
      return this;
    }

    /**
     * Sets the view cone angle of the entity.
     *
     * @param viewConeAngle the view cone angle in degrees
     * @return the Builder instance
     */
    public Builder viewConeAngle(Float viewConeAngle) {
      this.viewConeAngle = viewConeAngle;
      return this;
    }

    /**
     * Sets the view range of the entity.
     *
     * @param viewRange the view range in tiles
     * @return the Builder instance
     */
    public Builder viewRange(Float viewRange) {
      this.viewRange = viewRange;
      return this;
    }

    /**
     * Sets the decay rate of the entity.
     *
     * @param decayRate the decay rate per second
     * @return the Builder instance
     */
    public Builder decayRate(Float decayRate) {
      this.decayRate = decayRate;
      return this;
    }

    /**
     * Sets whether the entity is illegal.
     *
     * @param isIllegal true if the entity is illegal, false otherwise
     * @return the Builder instance
     */
    public Builder isIllegal(Boolean isIllegal) {
      this.isIllegal = isIllegal;
      return this;
    }

    /**
     * Builds and returns an EscapeRoomEntityState object.
     *
     * @return the constructed EscapeRoomEntityState object
     */
    @Override
    public EscapeRoomEntityState build() {
      return new EscapeRoomEntityState(this);
    }
  }
}
