package escapeDungeon.items;

import contrib.components.SkillComponent;
import contrib.item.Item;
import contrib.utils.components.skill.Resource;
import core.Entity;
import core.utils.Point;
import core.utils.Tuple;
import core.utils.components.draw.animation.Animation;
import core.utils.components.path.SimpleIPath;
import escapeDungeon.components.IceMovementComponent;
import escapeDungeon.skill.IceWallSkill;
import escapeDungeon.skill.TorchSkill;
import mushRoom.Sounds;

import java.util.Optional;

/** An AxeItem can be used to chop down certain trees. */
public class TorchItem extends Item {

  private static final String PATH = "items/rpg/item_torch.png";

  private static Entity itemHolder;

  /** Constructs a new AxeItem. */
  public TorchItem() {
    super(
        "Axt",
        "Damit können bestimmte Bäume gefällt werden.",
        new Animation(new SimpleIPath(PATH)),
        new Animation(new SimpleIPath(PATH)));
  }

  @Override
  public void use(Entity user) {
  }

  @Override
  public boolean collect(Entity itemEntity, Entity collector) {
    itemHolder = collector;
    Sounds.KEY_ITEM_PICKUP_SOUND.play();
    collector
      .fetch(SkillComponent.class)
      .ifPresent(
        (sc) ->
          sc.addSkill(new TorchSkill("TorchSkill", 500, 3, Tuple.of(Resource.MANA, 0))));
    return super.collect(itemEntity, collector);
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
