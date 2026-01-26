package escapeDungeon.items;

import contrib.components.CollideComponent;
import contrib.components.InventoryComponent;
import contrib.item.Item;
import core.Entity;
import core.Game;
import core.level.Tile;
import core.level.utils.Coordinate;
import core.utils.components.draw.animation.Animation;
import core.utils.components.path.SimpleIPath;
import mushRoom.Sounds;

/** An AxeItem can be used to chop down certain trees. */
public class EmptyBottleItem extends Item {

  private static final String PATH = "items/rpg/potion_empty.png";

  /** Constructs a new AxeItem. */
  public EmptyBottleItem() {
    super(
        "Leeres Fläschchen",
        "Durst? Befülle mich am Wasser.",
        new Animation(new SimpleIPath(PATH)),
        new Animation(new SimpleIPath(PATH)));
  }

  @Override
  public void use(Entity user) {
    user.fetch(CollideComponent.class)
        .ifPresent(
            cc -> {
              Coordinate position = cc.collider().absoluteCenter().toCoordinate();
              for (Tile tile : Game.neighbours(Game.tileAt(position).get())) {
                if (Game.entityAtTile(tile).anyMatch(e -> e.name().contains("Water"))) {
                  user.fetch(InventoryComponent.class)
                      .ifPresent(
                          (ic -> {
                            ic.remove(this);
                            ic.add(new WaterPotionItem());
                          }));
                  break;
                }
              }
            });
  }

  @Override
  public boolean collect(Entity itemEntity, Entity collector) {
    Sounds.KEY_ITEM_PICKUP_SOUND.play();
    return super.collect(itemEntity, collector);
  }
}
