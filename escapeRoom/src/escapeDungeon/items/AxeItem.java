package escapeDungeon.items;

import contrib.item.Item;
import core.Entity;
import core.utils.Point;
import core.utils.components.draw.animation.Animation;
import core.utils.components.path.SimpleIPath;
import escapeDungeon.components.AxeComponent;
import java.util.Optional;
import mushRoom.Sounds;
import petriNet.PlaceComponent;

/** An AxeItem can be used to chop down certain trees. */
public class AxeItem extends Item {

  private static final String PATH = "items/rpg/axe_gold.png";

  private Entity itemHolder;

  private static PlaceComponent place;

  private static boolean produceOnce = true;

  /** Constructs a new AxeItem. */
  public AxeItem() {
    super(
        "Axt",
        "Siehst du den Wald mit lauter Bäumen?",
        new Animation(new SimpleIPath(PATH)),
        new Animation(new SimpleIPath(PATH)));
  }

  public static void placeComponent(PlaceComponent place) {
    AxeItem.place = place;
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
    }
    itemHolder = collector;
    Sounds.KEY_ITEM_PICKUP_SOUND.play();
    if (itemHolder != null) {
      itemHolder.add(new AxeComponent());
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
    }
    itemHolder = collector;
    Sounds.KEY_ITEM_PICKUP_SOUND.play();
    if (itemHolder != null) {
      itemHolder.add(new AxeComponent());
    }
  }

  @Override
  public Optional<Entity> drop(final Point position) {
    if (itemHolder != null) {
      itemHolder.fetch(AxeComponent.class).ifPresent((ac) -> itemHolder.remove(AxeComponent.class));
      itemHolder = null;
    }
    return super.drop(position);
  }
}
