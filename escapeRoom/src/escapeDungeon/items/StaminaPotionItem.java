package escapeDungeon.items;

import contrib.components.StaminaComponent;
import contrib.item.Item;
import core.Entity;
import core.utils.components.draw.animation.Animation;
import core.utils.components.path.SimpleIPath;
import mushRoom.Sounds;

/** An AxeItem can be used to chop down certain trees. */
public class StaminaPotionItem extends Item {

  private static final String PATH = "items/rpg/potion_lightgreen.png";

  /** Constructs a new AxeItem. */
  public StaminaPotionItem() {
    super(
        "Ausdauertrank",
        "Damit können bestimmte Bäume gefällt werden.",
        new Animation(new SimpleIPath(PATH)),
        new Animation(new SimpleIPath(PATH)));
  }

  @Override
  public void use(Entity user) {
    user.fetch(StaminaComponent.class).ifPresent(sc -> sc.restore(50));
    super.use(user);
  }

  @Override
  public boolean collect(Entity itemEntity, Entity collector) {
    Sounds.KEY_ITEM_PICKUP_SOUND.play();
    return super.collect(itemEntity, collector);
  }
}
