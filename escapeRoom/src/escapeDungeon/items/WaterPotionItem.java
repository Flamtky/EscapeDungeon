package escapeDungeon.items;

import contrib.item.Item;
import core.Entity;
import core.utils.components.draw.animation.Animation;
import core.utils.components.path.SimpleIPath;
import mushRoom.Sounds;

/** An AxeItem can be used to chop down certain trees. */
public class WaterPotionItem extends Item {

  private static final String PATH = "items/rpg/potion_lightblue.png";

  /** Constructs a new AxeItem. */
  public WaterPotionItem() {
    super(
        "Fläschchen Wasser",
        "Wasser alleine schmeckt doch nicht.",
        new Animation(new SimpleIPath(PATH)),
        new Animation(new SimpleIPath(PATH)));
  }

  @Override
  public void use(Entity user) {
    // Nothing
  }

  @Override
  public boolean collect(Entity itemEntity, Entity collector) {
    Sounds.KEY_ITEM_PICKUP_SOUND.play();
    return super.collect(itemEntity, collector);
  }
}
