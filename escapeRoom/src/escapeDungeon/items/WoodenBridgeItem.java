package escapeDungeon.items;

import contrib.item.Item;
import core.Entity;
import core.utils.components.draw.animation.Animation;
import core.utils.components.draw.animation.AnimationConfig;
import core.utils.components.draw.animation.SpritesheetConfig;
import core.utils.components.path.SimpleIPath;
import mushRoom.Sounds;

/** An AxeItem can be used to chop down certain trees. */
public class WoodenBridgeItem extends Item {

  private static final String PATH = "spritesheets/FG_Cellar.png";

  /** Constructs a new AxeItem. */
  public WoodenBridgeItem() {
    super(
        "Axt",
        "Damit können bestimmte Bäume gefällt werden.",
        new Animation(
            new SimpleIPath(PATH),
            new AnimationConfig(new SpritesheetConfig(384, 480, 1, 1, 16, 16))),
        new Animation(
            new SimpleIPath(PATH),
            new AnimationConfig(new SpritesheetConfig(384, 480, 1, 1, 16, 16))));
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
