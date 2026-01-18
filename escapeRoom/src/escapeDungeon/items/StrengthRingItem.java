package escapeDungeon.items;

import contrib.item.Item;
import core.Entity;
import core.components.VelocityComponent;
import core.utils.Point;
import core.utils.components.draw.animation.Animation;
import core.utils.components.path.SimpleIPath;
import java.util.Optional;
import mushRoom.Sounds;

public class StrengthRingItem extends Item {

  private static final String PATH = "items/rpg/item_ring_gold_gem_red.png";

  private Entity itemHolder;

  public StrengthRingItem() {
    super(
        "Ring der Stärke",
        "Dieser Ring ermöglicht es neue Dinge zu bewegen.",
        new Animation(new SimpleIPath(PATH)),
        new Animation(new SimpleIPath(PATH)));
  }

  @Override
  public void use(Entity user) {}

  @Override
  public boolean collect(Entity itemEntity, Entity collector) {
    itemHolder = collector;
    Sounds.KEY_ITEM_PICKUP_SOUND.play();
    collector.fetch(VelocityComponent.class).ifPresent((vc) -> vc.mass(1.4f));
    return super.collect(itemEntity, collector);
  }

  @Override
  public Optional<Entity> drop(final Point position) {
    if (itemHolder != null) {
      itemHolder.fetch(VelocityComponent.class).ifPresent((vc) -> vc.mass(1.2f));
      itemHolder = null;
    }
    return super.drop(position);
  }
}
