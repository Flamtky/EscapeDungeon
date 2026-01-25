package escapeDungeon.skill;

import contrib.components.IllegalComponent;
import contrib.systems.EventScheduler;
import contrib.utils.components.skill.Resource;
import contrib.utils.components.skill.cursorSkill.CursorSkill;
import core.Entity;
import core.Game;
import core.level.Tile;
import core.level.utils.LevelElement;
import core.level.utils.LevelUtils;
import core.utils.Point;
import core.utils.Tuple;
import java.util.ArrayList;
import mushRoom.modules.lockpick.LockPickDialog;
import mushRoom.modules.lockpick.LockPickDifficulty;

/** A skill that allows the caster to create or remove ice walls on ice-designated tiles. */
public class WallbreakerSkill extends CursorSkill {

  /**
   * Creates a new cursor-targeted skill with a custom execution behavior.
   *
   * @param name The name of the skill.
   * @param cooldown The cooldown in milliseconds before the skill can be used again.
   * @param resourceCost Optional resource costs (e.g., mana, energy) required to use this skill.
   */
  public WallbreakerSkill(String name, long cooldown, Tuple<Resource, Integer>... resourceCost) {
    super(name, cooldown, resourceCost);
  }

  /**
   * @param caster The entity using the skill.
   * @param point The current cursor position in the game world.
   */
  @Override
  protected boolean executeOnCursor(Entity caster, Point point) {
    if (Game.entityAtPoint(point).anyMatch(e -> e.name().contains("Wall"))) {
      LockPickDialog.openLockPick(
          caster,
          LockPickDifficulty.HARD,
          () -> {
            ArrayList<Tile> entityTiles = new ArrayList<>();
            Game.entityAtPoint(point)
                .filter(e -> e.name().contains("Wall"))
                .forEach(
                    entity -> {
                      Game.remove(entity);
                      entityTiles.addAll(LevelUtils.occupiedTiles(entity));
                    });
            entityTiles.forEach(
                tile -> {
                  tile.levelElement(LevelElement.FLOOR);
                  tile.refreshTexture();
                });
            caster
                .fetch(IllegalComponent.class)
                .ifPresent(
                    ic -> {
                      ic.addReason(IllegalComponent.Reason.VANDALISM);
                      EventScheduler.scheduleAction(
                          () -> ic.removeReason(IllegalComponent.Reason.VANDALISM), 8000);
                    });
            this.setLastUsedToNow();
          },
          () -> {
            caster
                .fetch(IllegalComponent.class)
                .ifPresent(
                    ic -> {
                      ic.addReason(IllegalComponent.Reason.VANDALISM);
                      EventScheduler.scheduleAction(
                          () -> ic.removeReason(IllegalComponent.Reason.VANDALISM), 5000);
                    });
            this.setLastUsedToNow();
          });
      return true;
    }
    return false;
  }
}
