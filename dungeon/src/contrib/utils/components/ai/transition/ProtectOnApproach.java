package contrib.utils.components.ai.transition;

import core.Entity;
import core.level.utils.LevelUtils;
import java.util.function.BiFunction;

/**
 * Implements an AI that protects an entity if the player is in the given range.
 *
 * <p>Entity will stay in fight mode once entered.
 */
public final class ProtectOnApproach implements BiFunction<Entity, Entity, Boolean> {

  private final float range;
  private final Entity toProtect;
  private boolean isInFight = false;

  /**
   * @param range The range in which the player can come to the protected entity. If the player is
   *     closer than this range to the protected entity, the protecting entity switches to fight
   *     mode.
   * @param toProtect The entity which will be protected.
   */
  public ProtectOnApproach(float range, final Entity toProtect) {
    this.range = range;
    this.toProtect = toProtect;
  }

  /**
   * If protecting entity isn't in fight mode yet, check if player is in range of the protected
   * entity. Once the entity enters fight mode, it stays in that state permanently.
   *
   * @param entity Entity that is protecting.
   * @param player The player entity.
   * @return Optional of player if in fight mode, empty otherwise.
   */
  @Override
  public Boolean apply(final Entity entity, final Entity player) {
    if (!isInFight) {
      isInFight = LevelUtils.entityInRange(toProtect, player, range);
    }
    return isInFight;
  }
}
