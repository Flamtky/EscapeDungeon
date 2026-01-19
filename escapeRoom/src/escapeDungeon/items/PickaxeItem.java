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

/** An AxeItem can be used to chop down certain trees. */
public class PickaxeItem extends Item {

  private static final String PATH = "items/rpg/pickaxe_silver.png";

  private Entity itemHolder;

  /** Constructs a new AxeItem. */
  public PickaxeItem() {
    super(
        "Spitzhacke",
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
    if (itemHolder != null) {
      itemHolder
          .fetch(SkillComponent.class)
          .ifPresent(
              (sc) ->
                  sc.addSkill(
                      new WallbreakerSkill(
                          "WallbreakerSkill", 1000, Tuple.of(Resource.STAMINA, 10))));
    }
    return super.collect(itemEntity, collector);
  }

  @Override
  public void added(Entity collector) {
    itemHolder = collector;
    Sounds.KEY_ITEM_PICKUP_SOUND.play();
    if (itemHolder != null) {
      itemHolder
          .fetch(SkillComponent.class)
          .ifPresent(
              (sc) ->
                  sc.addSkill(
                      new WallbreakerSkill(
                          "WallbreakerSkill", 1000, Tuple.of(Resource.STAMINA, 10))));
    }
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
