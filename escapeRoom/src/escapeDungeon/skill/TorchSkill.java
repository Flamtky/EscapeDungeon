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
import escapeDungeon.items.TorchItem;
import java.util.concurrent.atomic.AtomicBoolean;
import mushRoom.modules.qte.FollowingIndicatorDialog;
import mushRoom.modules.qte.FollowingIndicatorDifficulty;

/** A skill that allows the caster to create or remove ice walls on ice-designated tiles. */
public class TorchSkill extends CursorSkill {

  private final int maxAmount;
  private final TorchItem torchItem;
  private int placed;

  /**
   * Creates a new cursor-targeted skill with a custom execution behavior.
   *
   * @param name The name of the skill.
   * @param cooldown The cooldown in milliseconds before the skill can be used again.
   * @param maxAmount The maximum amount of walls that can be placed.
   * @param resourceCost Optional resource costs (e.g., mana, energy) required to use this skill.
   */
  public TorchSkill(
      String name,
      long cooldown,
      int maxAmount,
      TorchItem item,
      Tuple<Resource, Integer>... resourceCost) {
    super(name, cooldown, resourceCost);
    this.maxAmount = maxAmount;
    this.torchItem = item;
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
              if (tile.levelElement() == LevelElement.FLOOR
                  && tile.designLabel() == DesignLabel.GREYCASTLE) {
                if (Game.entityAtPoint(point)
                    .anyMatch(e -> e.name().contains("TorchGrayAnimatedPlaced"))) {
                  Game.entityAtPoint(point)
                      .filter(e -> e.name().contains("TorchGrayAnimatedPlaced"))
                      .findFirst()
                      .ifPresent(Game::remove);
                  placed--;
                  torchItem.displayName((maxAmount - placed) + " Fackeln");
                } else if (placed < maxAmount) {
                  FollowingIndicatorDialog.openFollowingIndicator(
                      caster,
                      FollowingIndicatorDifficulty.FAST.apply(RANDOM.nextInt(3, 6)),
                      () -> {
                        Entity torch =
                            DecoFactory.createDeco(tile.position(), Deco.TorchGrayAnimatedPlaced);
                        Game.add(torch);
                        placed++;
                        torchItem.displayName((maxAmount - placed) + " Fackeln");
                      },
                      () -> {});
                }
              }
              success.set(true);
            }));
    return success.get();
  }

  /**
   * Returns the name of the skill.
   *
   * @return the skill name
   */
  @Override
  public String name() {
    return (maxAmount - placed) + " - " + super.name();
  }
}
