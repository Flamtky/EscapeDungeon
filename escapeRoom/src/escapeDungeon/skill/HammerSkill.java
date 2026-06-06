package escapeDungeon.skill;

import contrib.components.StaminaComponent;
import contrib.hud.DialogUtils;
import contrib.utils.components.skill.Resource;
import contrib.utils.components.skill.cursorSkill.CursorSkill;
import core.Entity;
import core.Game;
import core.level.utils.LevelElement;
import core.utils.Point;
import core.utils.Tuple;
import mushRoom.modules.lockpick.LockPickDialog;
import mushRoom.modules.lockpick.LockPickDifficulty;

/** A skill that allows the caster to create or remove ice walls on ice-designated tiles. */
public class HammerSkill extends CursorSkill {

  private int staminaCost = 50;

  /**
   * Creates a new cursor-targeted skill with a custom execution behavior.
   *
   * @param name The name of the skill.
   * @param cooldown The cooldown in milliseconds before the skill can be used again.
   * @param resourceCost Optional resource costs (e.g., mana, energy) required to use this skill.
   */
  public HammerSkill(String name, long cooldown, Tuple<Resource, Integer>... resourceCost) {
    super(name, cooldown, resourceCost);
  }

  /**
   * @param caster The entity using the skill.
   * @param point The current cursor position in the game world.
   * @return whether the skill was successfully executed.
   */
  @Override
  protected boolean executeOnCursor(Entity caster, Point point) {
    StaminaComponent sc = caster.fetch(StaminaComponent.class).get();
    if (Game.entityAtPoint(point).anyMatch(e -> e.name().contains("Stone"))) {
      if (sc.currentAmount() < staminaCost) {
        DialogUtils.showTextPopup(
            "Du hast zu wenig Ausdauer um diese Fähigkeit zu verwenden. "
                + "Trinke einen Ausdauertrank, du kannst den Trank in deinem Inventar mit der rechten Maustaste verwenden.",
            "Erschöpft",
            () -> {},
            caster.id());
      } else {
        LockPickDialog.openLockPick(
            caster,
            LockPickDifficulty.EASY,
            () -> {
              Game.entityAtPoint(point)
                  .filter(e -> e.name().contains("Stone"))
                  .forEach(Game::remove);
              Game.tileAt(point)
                  .ifPresent(
                      (tile -> {
                        tile.levelElement(LevelElement.FLOOR);
                        tile.refreshTexture();
                      }));
              sc.consume(staminaCost);
            },
            () -> {});
      }
      return true;
    }
    return false;
  }
}
