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

/** An AxeItem can be used to chop down certain trees. */
public class TorchItem extends Item {

  private static final String PATH = "items/rpg/item_torch.png";

  private Entity itemHolder;

  /** Constructs a new AxeItem. */
  public TorchItem() {
    super(
        "20 Fackeln",
        "Bringt Licht ins Dunkel. Du hast eine neue Fähigkeit. Spare Rohstoffe, sammel deine Fackeln auch wieder auf.",
        new Animation(new SimpleIPath(PATH)),
        new Animation(new SimpleIPath(PATH)));
  }

  @Override
  public void use(Entity user) {}

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
              if (sc.getSkill(TorchSkill.class).isEmpty())
                sc.addSkill(new TorchSkill("TorchSkill", 500, 5, Tuple.of(Resource.MANA, 0)));
            });
  }

  @Override
  public Optional<Entity> drop(final Point position) {
    if (itemHolder != null) {
      itemHolder.fetch(SkillComponent.class).ifPresent((sc) -> sc.removeSkill(TorchSkill.class));
      itemHolder = null;
    }
    return super.drop(position);
  }
}
