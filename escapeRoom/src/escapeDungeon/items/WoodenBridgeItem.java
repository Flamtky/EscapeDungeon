package escapeDungeon.items;

import contrib.components.SkillComponent;
import contrib.item.Item;
import contrib.utils.components.skill.Resource;
import core.Entity;
import core.utils.Point;
import core.utils.Tuple;
import core.utils.components.draw.animation.Animation;
import core.utils.components.draw.animation.AnimationConfig;
import core.utils.components.draw.animation.SpritesheetConfig;
import core.utils.components.path.SimpleIPath;
import escapeDungeon.skill.BridgeSkill;
import java.util.Optional;
import mushRoom.Sounds;

/** An AxeItem can be used to chop down certain trees. */
public class WoodenBridgeItem extends Item {

  private static final String PATH = "spritesheets/FG_Cellar.png";

  private Entity itemHolder;

  /** Constructs a new AxeItem. */
  public WoodenBridgeItem() {
    super(
        "Hölzerne Brücke",
        "Eine Brücke ins Paradies.",
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
    itemHolder = collector;
    Sounds.KEY_ITEM_PICKUP_SOUND.play();
    if (itemHolder != null) {
      giveSkill(itemHolder);
    }
    return super.collect(itemEntity, collector);
  }

  @Override
  public void added(Entity collector) {
    itemHolder = collector;
    Sounds.KEY_ITEM_PICKUP_SOUND.play();
    if (itemHolder != null) {
      giveSkill(itemHolder);
    }
  }

  private void giveSkill(Entity entity) {
    entity
        .fetch(SkillComponent.class)
        .ifPresent(
            (sc) -> {
              if (sc.getSkill(BridgeSkill.class).isEmpty())
                sc.addSkill(new BridgeSkill("BridgeSkill", 100, Tuple.of(Resource.STAMINA, 10)));
            });
  }

  @Override
  public Optional<Entity> drop(final Point position) {
    if (itemHolder != null) {
      itemHolder.fetch(SkillComponent.class).ifPresent((sc) -> sc.removeSkill(BridgeSkill.class));
      itemHolder = null;
    }
    return super.drop(position);
  }
}
