package escapeDungeon.items;

import contrib.components.InventoryComponent;
import contrib.components.UIComponent;
import contrib.hud.inventory.InventoryGUI;
import contrib.item.Item;
import core.Entity;
import core.utils.Point;
import core.utils.components.draw.animation.Animation;
import core.utils.components.path.SimpleIPath;
import escapeDungeon.components.IceMovementComponent;
import mushRoom.Sounds;
import mushRoom.modules.items.MagicLensItem;

import java.util.Optional;

public class IceWallPlacer extends Item {

  private static final String PATH = "items/rpg/item_magnifying_glass.png";

  private static Entity itemHolder;

  public IceWallPlacer() {
    super(
      "Ein magischer Handschuh",
      "",
      new Animation(new SimpleIPath(PATH)),
      new Animation(new SimpleIPath(PATH)));
  }

  @Override
  public void use(Entity user) {
    createIceWall(user);
  }

  @Override
  public boolean collect(Entity itemEntity, Entity collector) {
    itemHolder = collector;
    Sounds.KEY_ITEM_PICKUP_SOUND.play();
    collector.add(new IceMovementComponent());
    return super.collect(itemEntity, collector);
  }

  @Override
  public Optional<Entity> drop(final Point position) {
    if (itemHolder != null) {
      itemHolder.fetch(IceMovementComponent.class).ifPresent((ic) -> itemHolder.remove(IceMovementComponent.class));
      itemHolder = null;
    }
    return super.drop(position);
  }

  /**
   * Toggles the magic lens effect if the provided entity has the {@link MagicLensItem}.
   *
   * @param player the entity to toggle the magic lens for
   */
  public static void createIceWall(Entity player) {

  }

}
