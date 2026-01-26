package escapeDungeon.items;

import contrib.components.SkillComponent;
import contrib.hud.DialogUtils;
import contrib.item.Item;
import contrib.utils.components.skill.Resource;
import core.Entity;
import core.utils.Point;
import core.utils.Tuple;
import core.utils.components.draw.animation.Animation;
import core.utils.components.path.SimpleIPath;
import escapeDungeon.skill.HammerSkill;
import java.util.Optional;
import mushRoom.Sounds;

/** An AxeItem can be used to chop down certain trees. */
public class HammerItem extends Item {

  private static final String PATH = "items/tools/hammer.png";

  private Entity itemHolder;

  /** Constructs a new AxeItem. */
  public HammerItem() {
    super(
        "Hammer",
        "Ein Werkzeug. Steine nehmt euch in Acht. Du hast eine neue Fähigkeit.",
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
                  sc.addSkill(new HammerSkill("HammerSkill", 1000, Tuple.of(Resource.STAMINA, 0))));
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
                  sc.addSkill(new HammerSkill("HammerSkill", 1000, Tuple.of(Resource.STAMINA, 0))));
      DialogUtils.showTextPopup(
          "Du hast einen Hammer erhalten. Mit diesem Hammer kannst du Stein zerstören. ",
          "Der Hammer",
          () -> {
            DialogUtils.showTextPopup(
                "Unten rechts siehst du deine Fähigkeiten und den Cooldown. Du wechselst zwischen deinen Fähigkeiten mit ?. Du kannst deine Fähigkeit mit der linken Maustaste aktivieren.",
                "Hammer Fähigkeit",
                () -> {},
                itemHolder.id());
          },
          itemHolder.id());
    }
  }

  @Override
  public Optional<Entity> drop(final Point position) {
    if (itemHolder != null) {
      itemHolder.fetch(SkillComponent.class).ifPresent((sc) -> sc.removeSkill(HammerSkill.class));
      itemHolder = null;
    }
    return super.drop(position);
  }
}
