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
import java.util.Optional;
import mushRoom.Sounds;

/** Item that grants the ability to place ice walls when collected. */
public class IceWallPlacer extends Item {

  private static final String PATH = "items/rpg/item_ring_silver_gem_blue.png";

  private Entity itemHolder;

  /** Constructs a new IceWallPlacer item. */
  public IceWallPlacer() {
    super(
        "magischer Eisring",
        "Du wirst von kalter Magie durchströmt. Du kannst nun das Eis kontrollieren. Du hast eine neue Fähigkeit.",
        new Animation(new SimpleIPath(PATH)),
        new Animation(new SimpleIPath(PATH)));
  }

  @Override
  public void use(Entity user) {}

  @Override
  public boolean collect(Entity itemEntity, Entity collector) {
    itemHolder = collector;
    Sounds.KEY_ITEM_PICKUP_SOUND.play();
    giveSkill(collector);
    return super.collect(itemEntity, collector);
  }

  @Override
  public void added(Entity collector) {
    itemHolder = collector;
    Sounds.KEY_ITEM_PICKUP_SOUND.play();
    giveSkill(collector);
  }

  private void giveSkill(Entity entity) {
    entity.add(new IceMovementComponent());
    entity
        .fetch(SkillComponent.class)
        .ifPresent(
            (sc) -> {
              if (sc.getSkill(IceWallSkill.class).isEmpty())
                sc.addSkill(new IceWallSkill("IceWallSkill", 100, 3, Tuple.of(Resource.MANA, 0)));
            });
  }

  @Override
  public Optional<Entity> drop(final Point position) {
    if (itemHolder != null) {
      itemHolder
          .fetch(IceMovementComponent.class)
          .ifPresent((ic) -> itemHolder.remove(IceMovementComponent.class));
      itemHolder.fetch(SkillComponent.class).ifPresent((sc) -> sc.removeSkill(IceWallSkill.class));
      itemHolder = null;
    }
    return super.drop(position);
  }
}
