package escapeDungeon.items;

import contrib.item.Item;
import core.Entity;
import core.utils.components.draw.animation.Animation;
import core.utils.components.path.SimpleIPath;
import mushRoom.Sounds;
import petriNet.PlaceComponent;

/** An AxeItem can be used to chop down certain trees. */
public class RopeItem extends Item {

  private static final String PATH = "items/rpg/item_stick_wizardly.png";

  private static PlaceComponent place;

  private static boolean produceOnce = true;

  /** Constructs a new AxeItem. */
  public RopeItem() {
    super(
        "Seil",
        "Wo kann ich hier klettern? Es sieht alles so flach aus.",
        new Animation(new SimpleIPath(PATH)),
        new Animation(new SimpleIPath(PATH)));
  }

  public static void placeComponent(PlaceComponent place) {
    RopeItem.place = place;
  }

  @Override
  public void use(Entity user) {
    // Nothing
  }

  @Override
  public boolean collect(Entity itemEntity, Entity collector) {
    if (collector != null) {
      if (produceOnce) {
        place.produce();
        produceOnce = false;
      }
      Sounds.KEY_ITEM_PICKUP_SOUND.play();
    }
    return super.collect(itemEntity, collector);
  }

  @Override
  public void added(Entity collector) {
    if (collector != null) {
      if (produceOnce) {
        place.produce();
        produceOnce = false;
      }
      Sounds.KEY_ITEM_PICKUP_SOUND.play();
    }
  }
}
