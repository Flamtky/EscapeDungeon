package escapeDungeon.items;

import contrib.item.Item;
import core.Entity;
import core.utils.components.draw.animation.Animation;
import core.utils.components.path.SimpleIPath;
import mushRoom.Sounds;

/** An AxeItem can be used to chop down certain trees. */
public class HammerHeadItem extends Item {

  private static final String PATH = "items/resource/basalt.png";

  /** Constructs a new AxeItem. */
  public HammerHeadItem() {
    super(
        "Hammerkopf",
        "Alleine ziemlich nutzlos",
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
