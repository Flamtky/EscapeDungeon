package escapeDungeon.items;

import contrib.components.SkillComponent;
import contrib.item.Item;
import contrib.utils.components.skill.Resource;
import core.Entity;
import core.utils.Point;
import core.utils.Tuple;
import core.utils.components.draw.animation.Animation;
import core.utils.components.path.SimpleIPath;
import escapeDungeon.skill.WallbreakerSkill;
import java.util.Optional;
import mushRoom.Sounds;
import petriNet.PlaceComponent;

/** An AxeItem can be used to chop down certain trees. */
public class PickaxeItem extends Item {

  private static final String PATH = "items/rpg/pickaxe_silver.png";

  private Entity itemHolder;

  private static PlaceComponent place;

  private static boolean produceOnce = true;

  /** Constructs a new AxeItem. */
  public PickaxeItem() {
    super(
        "Spitzhacke",
        "Lasst das Buddeln beginnen. Du hast eine neue Fähigkeit.",
        new Animation(new SimpleIPath(PATH)),
        new Animation(new SimpleIPath(PATH)));
  }

  public static void placeComponent(PlaceComponent place) {
    PickaxeItem.place = place;
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
      giveSkill(itemHolder);
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
      giveSkill(itemHolder);
    }
  }

  private void giveSkill(Entity entity) {
    entity
        .fetch(SkillComponent.class)
        .ifPresent(
            (sc) -> {
              if (sc.getSkill(WallbreakerSkill.class).isEmpty())
                sc.addSkill(
                    new WallbreakerSkill("Spitzhacke", 10000, Tuple.of(Resource.STAMINA, 100)));
            });
  }

  @Override
  public Optional<Entity> drop(final Point position) {
    if (itemHolder != null) {
      itemHolder
          .fetch(SkillComponent.class)
          .ifPresent((sc) -> sc.removeSkill(WallbreakerSkill.class));
      itemHolder = null;
    }
    return super.drop(position);
  }
}
