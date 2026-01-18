package escapeDungeon.skill;

import contrib.components.DecoComponent;
import contrib.entities.deco.Deco;
import contrib.entities.deco.DecoFactory;
import contrib.utils.components.skill.Resource;
import contrib.utils.components.skill.cursorSkill.CursorSkill;
import core.Entity;
import core.Game;
import core.components.PositionComponent;
import core.level.utils.DesignLabel;
import core.level.utils.LevelElement;
import core.utils.Point;
import core.utils.Tuple;
import mushRoom.shaders.TorchPostProcessing;

/** A skill that allows the caster to create or remove ice walls on ice-designated tiles. */
public class TorchSkill extends CursorSkill {

  private final int maxWallAmount;

  /**
   * Creates a new cursor-targeted skill with a custom execution behavior.
   *
   * @param name The name of the skill.
   * @param cooldown The cooldown in milliseconds before the skill can be used again.
   * @param maxWallAmount The maximum amount of walls that can be placed.
   * @param resourceCost Optional resource costs (e.g., mana, energy) required to use this skill.
   */
  public TorchSkill(
      String name, long cooldown, int maxWallAmount, Tuple<Resource, Integer>... resourceCost) {
    super(name, cooldown, resourceCost);
    this.maxWallAmount = maxWallAmount;
  }

  /**
   * @param caster The entity using the skill.
   * @param point The current cursor position in the game world.
   */
  @Override
  protected void executeOnCursor(Entity caster, Point point) {
    Game.tileAt(point)
        .ifPresent(
            (tile -> {
                if (tile.levelElement() == LevelElement.FLOOR) {
                  Entity torch = DecoFactory.createDeco(tile.position(), Deco.TorchGrayAnimated);
                  Game.add(torch);
                }
            }));
  }
}
