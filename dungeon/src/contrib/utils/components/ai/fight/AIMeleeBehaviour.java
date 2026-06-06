package contrib.utils.components.ai.fight;

import contrib.utils.components.ai.ISkillUser;
import contrib.utils.components.skill.Skill;
import core.Entity;
import core.level.utils.LevelUtils;
import java.util.function.BiConsumer;

/**
 * Implements a fight AI. The entity attacks the player if he is in a given range. When the entity
 * is not in range but in fight mode, the entity will be moving towards the player.
 *
 * @see ISkillUser
 */
public class AIMeleeBehaviour extends AIChaseBehaviour
    implements BiConsumer<Entity, Entity>, ISkillUser {
  private final float attackRange;
  private Skill fightSkill;

  /**
   * Attacks the player if he is within the given range. Otherwise, it will move towards the player.
   *
   * @param attackRange Range in which the attack skill should be executed.
   * @param fightSkill Skill to be used when an attack is performed.
   */
  public AIMeleeBehaviour(float attackRange, Skill fightSkill) {
    super();
    this.attackRange = attackRange;
    this.fightSkill = fightSkill;
  }

  @Override
  public void accept(Entity entity, Entity player) {
    if (LevelUtils.entityInRange(entity, player, attackRange)) {
      useSkill(fightSkill, entity);
    } else {
      super.accept(entity, player);
    }
  }

  @Override
  public void useSkill(Skill fightSkill, Entity skillUser) {
    if (fightSkill == null) {
      return;
    }
    fightSkill.execute(skillUser);
  }

  @Override
  public Skill skill() {
    return fightSkill;
  }

  @Override
  public void skill(Skill skill) {
    this.fightSkill = skill;
  }
}
