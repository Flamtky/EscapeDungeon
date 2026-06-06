package escapeDungeon.items;

import contrib.components.InventoryComponent;
import contrib.components.StaminaComponent;
import contrib.item.Item;
import contrib.systems.EventScheduler;
import core.Entity;
import core.utils.components.draw.animation.Animation;
import core.utils.components.path.SimpleIPath;
import mushRoom.Sounds;

/** An AxeItem can be used to chop down certain trees. */
public class TutorialPotionItem extends Item {

  private static final String PATH = "items/rpg/potion_lightgreen.png";

  /** Constructs a new AxeItem. */
  public TutorialPotionItem() {
    super(
        "unendlicher Ausdauertrank",
        "Füllt deine Ausdauer wieder auf. Füllt sich nach einiger Zeit wieder auf.",
        new Animation(new SimpleIPath(PATH)),
        new Animation(new SimpleIPath(PATH)));
  }

  @Override
  public void use(Entity user) {
    user.fetch(StaminaComponent.class)
        .ifPresent(
            sc -> {
              if ((sc.maxAmount() - sc.currentAmount()) > 50) {
                sc.restore(sc.maxAmount());
                user.fetch(InventoryComponent.class)
                    .ifPresent(
                        (ic -> {
                          ic.remove(this);
                          ic.add(new EmptyBottleItem());
                          EventScheduler.scheduleAction(
                              () -> {
                                ic.itemOfClass(EmptyBottleItem.class).ifPresent(ic::remove);
                                ic.add(this);
                              },
                              20000);
                        }));
              }
            });
  }

  @Override
  public boolean collect(Entity itemEntity, Entity collector) {
    Sounds.KEY_ITEM_PICKUP_SOUND.play();
    return super.collect(itemEntity, collector);
  }
}
