package escapeDungeon.skill;

import contrib.components.DecoComponent;
import contrib.entities.deco.Deco;
import contrib.entities.deco.DecoFactory;
import contrib.utils.EntityUtils;
import contrib.utils.components.skill.Resource;
import contrib.utils.components.skill.cursorSkill.CursorSkill;
import core.Entity;
import core.Game;
import core.level.utils.DesignLabel;
import core.level.utils.LevelElement;
import core.utils.Point;
import core.utils.Tuple;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;

/** A skill that allows the caster to create or remove ice walls on ice-designated tiles. */
public class IceWallSkill extends CursorSkill {

  private final int maxWallAmount;

  /**
   * Creates a new cursor-targeted skill with a custom execution behavior.
   *
   * @param name The name of the skill.
   * @param cooldown The cooldown in milliseconds before the skill can be used again.
   * @param maxWallAmount The maximum amount of walls that can be placed.
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
   * @return whether the skill was successfully executed.
   */
  @Override
  protected boolean executeOnCursor(Entity caster, Point point) {
    AtomicBoolean success = new AtomicBoolean(false);
    Game.tileAt(point)
        .ifPresent(
            (tile -> {
              if (tile.designLabel() == DesignLabel.ICE) {
                if (tile.levelElement() == LevelElement.HOLE) {
                  tile.levelElement(LevelElement.FLOOR);
                  tile.refreshTexture();
                  Game.entityAtTile(tile)
                      .filter(
                          deco ->
                              deco.fetch(DecoComponent.class)
                                  .map(decoComp -> decoComp.type() == Deco.IceWall)
                                  .orElse(false))
                      .forEach(Game::remove);
                } else if (tile.levelElement() == LevelElement.FLOOR) {
                  if (Game.allTiles(
                              t ->
                                  t.levelElement() == LevelElement.HOLE
                                      && t.designLabel() == DesignLabel.ICE)
                          .size()
                      < maxWallAmount) {
                    var playerTiles =
                        Game.allPlayers()
                            .map(EntityUtils::getPosition)
                            .map(Game::tileAt)
                            .flatMap(Optional::stream)
                            .toList();

                    if (playerTiles.contains(tile)) {
                      return; // Prevent placing ice wall on player's own tile
                    }

                    tile.levelElement(LevelElement.HOLE);
                    tile.refreshTexture();
                    var iceWallEntity = DecoFactory.createDeco(tile.position(), Deco.IceWall);
                    Game.add(iceWallEntity);
                  }
                }
              }
              success.set(true);
            }));
    return success.get();
  }
}
