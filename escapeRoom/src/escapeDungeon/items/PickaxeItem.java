package escapeDungeon.items;

import contrib.components.SkillComponent;
import contrib.item.Item;
import contrib.utils.components.skill.Resource;
import core.Entity;
import core.utils.Point;
import core.utils.Tuple;
import core.utils.components.draw.animation.Animation;
import core.utils.components.path.SimpleIPath;
import escapeDungeon.skill.IceWallSkill;
import escapeDungeon.skill.TorchSkill;
import escapeDungeon.skill.WallbreakerSkill;
import mushRoom.Sounds;

import java.util.Optional;

/** An AxeItem can be used to chop down certain trees. */
public class PickaxeItem extends Item {

  private static final String PATH = "items/rpg/pickaxe_silver.png";

  private static Entity itemHolder;

  /** Constructs a new AxeItem. */
  public PickaxeItem() {
    super(
        "Axt",
        "Damit können bestimmte Bäume gefällt werden.",
        new Animation(new SimpleIPath(PATH)),
        new Animation(new SimpleIPath(PATH)));
  }

  @Override
  public void use(Entity user) {
    // Nothing
  }

  @Override
  public boolean collect(Entity itemEntity, Entity collector) {
    itemHolder = collector;
    Sounds.KEY_ITEM_PICKUP_SOUND.play();
    collector
      .fetch(SkillComponent.class)
      .ifPresent(
        (sc) ->
          sc.addSkill(new WallbreakerSkill("WallbreakerSkill", 1000, Tuple.of(Resource.STAMINA, 10))));
    return super.collect(itemEntity, collector);
  }

  @Override
  public Optional<Entity> drop(final Point position) {
    if (itemHolder != null) {
      itemHolder.fetch(SkillComponent.class).ifPresent((sc) -> sc.removeSkill(WallbreakerSkill.class));
      itemHolder = null;
    }
    return super.drop(position);
  }
}
