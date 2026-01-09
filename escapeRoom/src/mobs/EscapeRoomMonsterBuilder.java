package mobs;

import contrib.entities.MonsterBuilder;
import guard.GuardBuilder;
import java.util.function.Supplier;

public enum EscapeRoomMonsterBuilder {
  /** A static non-moving guard and shooting monster. */
  GUARD(GuardBuilder::new);

  private final Supplier<Builder> builderSupplier;

  /**
   * Constructor for EscapeRoomMonsterBuilder.
   *
   * @param builderSupplier Supplier for the Builder instance.
   */
  EscapeRoomMonsterBuilder(Supplier<Builder> builderSupplier) {
    this.builderSupplier = builderSupplier;
  }

  /**
   * Returns a new {@link Builder} for this enum constant.
   *
   * <p>Each call to this method returns a new instance of {@link Builder}, allowing for independent
   * configurations.
   *
   * @return a new {@link Builder} instance
   */
  public Builder builder() {
    return builderSupplier.get();
  }

  /** Builder class for creating monster entities. */
  public static class Builder extends MonsterBuilder<Builder> {}
}
