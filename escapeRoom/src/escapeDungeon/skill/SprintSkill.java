package escapeDungeon.skill;

import contrib.components.CharacterClassComponent;
import contrib.components.StaminaComponent;
import contrib.utils.components.skill.Skill;
import core.Entity;
import core.components.VelocityComponent;

public class SprintSkill extends Skill {

  private static final String NAME = "Sprint";
  private static final float SPRINT_MULTIPLIER = 1.50f;
  private static final float SPRINT_STAMINA_COST = 0.1f;

  /** Creates a new sprint skill. */
  public SprintSkill() {
    super(NAME, 2);
  }

  @Override
  protected void executeSkill(Entity caster) {
    var velocityOpt = caster.fetch(VelocityComponent.class);
    if (velocityOpt.isEmpty()) {
      LOGGER.warn("Entity {} has no VelocityComponent, cannot sprint.", caster);
      return;
    }

    var curSpeed = velocityOpt.get().currentVelocity();
    if (curSpeed.lengthSquared() == 0) {
      return; // Hero is not moving, skip skill
    }

    var staminaOpt = caster.fetch(StaminaComponent.class);
    if (staminaOpt.isEmpty()) {
      LOGGER.warn("Entity {} has no StaminaComponent, cannot sprint.", caster);
      return; // No stamina component, skip consume and scaling
    }

    var charClassOpt = caster.fetch(CharacterClassComponent.class);
    if (charClassOpt.isEmpty()) {
      LOGGER.warn("Entity {} has no CharacterClassComponent, cannot sprint.", caster);
      return; // No character class component, skip consume and scaling
    }

    if (staminaOpt.get().consume(SPRINT_STAMINA_COST)) {
      velocityOpt.get().modifier("sprint", SPRINT_MULTIPLIER);
    }
  }
}
