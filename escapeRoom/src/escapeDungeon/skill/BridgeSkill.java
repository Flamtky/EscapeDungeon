package escapeDungeon.skill;

import contrib.entities.deco.Deco;
import contrib.entities.deco.DecoFactory;
import contrib.utils.components.skill.Resource;
import contrib.utils.components.skill.cursorSkill.CursorSkill;
import core.Entity;
import core.Game;
import core.level.utils.DesignLabel;
import core.level.utils.LevelElement;
import core.utils.Point;
import core.utils.Tuple;

public class BridgeSkill extends CursorSkill {

  /**
   * Creates a new cursor-targeted skill with a custom execution behavior.
   *
   * @param name The name of the skill.
   * @param cooldown The cooldown in milliseconds before the skill can be used again.
   * @param resourceCost Optional resource costs (e.g., mana, energy) required to use this skill.
   */
  public BridgeSkill(String name, long cooldown, Tuple<Resource, Integer>... resourceCost) {
    super(name, cooldown, resourceCost);
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
              if (tile.designLabel() == DesignLabel.WATER) {
                if (tile.levelElement() == LevelElement.WALL) {
                  tile.levelElement(LevelElement.PORTAL);
                  tile.refreshTexture();
                  Game.add(DecoFactory.createDeco(point.floor(), Deco.BRIDGESHorizontalGray));
                }
              }
              if (Game.entityAtPoint(point).anyMatch(e -> e.name().contains("Water"))) {
                if (tile.levelElement() == LevelElement.GITTER) {
                  tile.levelElement(LevelElement.FLOOR);
                  tile.refreshTexture();
                  Game.add(DecoFactory.createDeco(point.floor(), Deco.BRIDGESHorizontalGray));
                }
              }
            }));
  }
}
