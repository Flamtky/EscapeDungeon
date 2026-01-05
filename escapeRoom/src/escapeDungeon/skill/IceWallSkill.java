package escapeDungeon.skill;

import contrib.utils.components.skill.Resource;
import contrib.utils.components.skill.cursorSkill.CursorSkill;
import core.Entity;
import core.Game;
import core.level.utils.DesignLabel;
import core.level.utils.LevelElement;
import core.utils.Point;
import core.utils.Tuple;

public class IceWallSkill extends CursorSkill {

  private final int maxWallAmount;

  /**
   * Creates a new cursor-targeted skill with a custom execution behavior.
   *
   * @param name The name of the skill.
   * @param cooldown The cooldown in milliseconds before the skill can be used again.
   * @param resourceCost Optional resource costs (e.g., mana, energy) required to use this skill.
   */
  public IceWallSkill(
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
              if (tile.designLabel() == DesignLabel.ICE) {
                if (tile.levelElement() == LevelElement.HOLE) {
                  tile.levelElement(LevelElement.FLOOR);
                  tile.refreshTexture();
                } else if (tile.levelElement() == LevelElement.FLOOR) {
                  if (Game.allTiles(
                              t ->
                                  t.levelElement() == LevelElement.HOLE
                                      && t.designLabel() == DesignLabel.ICE)
                          .size()
                      < maxWallAmount) {
                    tile.levelElement(LevelElement.HOLE);
                    tile.refreshTexture();
                  }
                }
              }
            }));
  }
}
