package escapeDungeon.items;

import contrib.components.SkillComponent;
import contrib.item.Item;
import contrib.utils.components.skill.Resource;
import core.Entity;
import core.utils.Point;
import core.utils.Tuple;
import core.utils.components.draw.animation.Animation;
import core.utils.components.path.SimpleIPath;
import escapeDungeon.skill.TorchSkill;
import java.util.Optional;
import mushRoom.Sounds;
import petriNet.PlaceComponent;

/** An AxeItem can be used to chop down certain trees. */
public class TorchItem extends Item {

  private static final String PATH = "items/rpg/item_torch.png";

  private Entity itemHolder;

  private final TorchSkill torchSkill =
      new TorchSkill("Fackeln", 500, 20, Tuple.of(Resource.MANA, 0));

  private static PlaceComponent place;

  private static boolean produceOnce = true;

  /** Constructs a new AxeItem. */
  public TorchItem() {
    super(
        "Fackeln",
        "Bringt Licht ins Dunkel. Du hast eine neue Fähigkeit. Spare Rohstoffe, sammel deine Fackeln auch wieder auf.",
        new Animation(new SimpleIPath(PATH)),
        new Animation(new SimpleIPath(PATH)));
  }

  public static void placeComponent(PlaceComponent place) {
    TorchItem.place = place;
  }

  @Override
  public void use(Entity user) {}

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
              sc.addSkill(torchSkill);
            });
  }

  @Override
  public Optional<Entity> drop(final Point position) {
    if (itemHolder != null) {
      itemHolder.fetch(SkillComponent.class).ifPresent((sc) -> sc.removeSkill(torchSkill));
      itemHolder = null;
    }
    return super.drop(position);
  }
}
