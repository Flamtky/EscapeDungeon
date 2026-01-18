package contrib.utils;

import static contrib.hud.UIUtils.defaultSkin;

import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Container;
import com.badlogic.gdx.scenes.scene2d.ui.ProgressBar;
import contrib.components.BarDisplayable;
import contrib.components.UIComponent;
import contrib.hud.UIUtils;
import contrib.hud.dialogs.DialogContext;
import contrib.hud.dialogs.DialogContextKeys;
import contrib.hud.dialogs.DialogType;
import contrib.hud.dialogs.HeadlessDialogGroup;
import core.Entity;
import core.Game;
import core.components.DrawComponent;
import core.systems.CameraSystem;
import core.utils.Point;
import core.utils.logging.DungeonLogger;
import java.io.Serial;
import java.io.Serializable;
import java.util.Map;

/**
 * Utility class for managing attribute bars (health, mana, energy) for entities.
 *
 * <p>Attribute bars are client-side only visual elements that follow entities. They are created
 * using the standard entity ID system via {@link core.utils.EntityIdProvider}.
 */
public final class AttributeBarUtil {
  private static final DungeonLogger LOGGER = DungeonLogger.getLogger(AttributeBarUtil.class);

  private static final float MIN = 0f;
  private static final float MAX = 1f;
  private static final float STEP_SIZE = 0.01f;
  private static final float UPDATE_DURATION = 0.1f;
  private static final int DEFAULT_BAR_WIDTH = 50;
  private static final int DEFAULT_BAR_HEIGHT = 10;

  /** Gap between stacked bars. */
  public static final float BAR_GAP = DEFAULT_BAR_HEIGHT;

  private AttributeBarUtil() {} // Utility class, no instances

  /**
   * Creates a progress bar for the given entity and maps it in the provided map.
   *
   * <p>The bar entity is created client-side only as a local entity using {@link
   * Entity#createLocalEntity(String)}.
   *
   * @param entity the entity to attach the bar to
   * @param barDisplayable the component providing bar data
   * @param barMapping map from component class to progress bar
   * @param verticalOffset vertical offset above the entity
   */
  public static void addBarToEntity(
      Entity entity,
      BarDisplayable barDisplayable,
      Map<Class<? extends BarDisplayable>, ProgressBar> barMapping,
      float verticalOffset) {
    Entity barEntity = Entity.createLocalEntity(barDisplayable.barStyleName() + "_" + entity.id());

    DialogContext context =
        DialogContext.builder()
            .type(DialogType.DefaultTypes.PROGRESS_BAR)
            .center(false) // we will position it ourselves
            .put(
                DialogContextKeys.PROGRESS_BAR,
                new ProgressBarContext(entity.id(), barDisplayable.barStyleName(), verticalOffset))
            .put(DialogContextKeys.OWNER_ENTITY, barEntity.id())
            .build();
    UIComponent uiComp = new UIComponent(context, false, false);
    barEntity.add(uiComp);
    Game.add(barEntity);

    UIUtils.findTypeInGroup(uiComp.dialog(), ProgressBar.class)
        .ifPresentOrElse(
            bar -> barMapping.put(barDisplayable.getClass(), bar),
            () -> LOGGER.error("Failed to create progress bar for entity {}", entity));
  }

  /**
   * Builds a progress bar group from the given DialogContext.
   *
   * <p>On headless servers, returns a {@link HeadlessDialogGroup} placeholder.
   *
   * @param ctx the DialogContext containing the progress bar
   * @return a Group containing the progress bar
   */
  public static Group buildProgressBar(DialogContext ctx) {
    // On headless server, return placeholder
    if (Game.isHeadless()) {
      return new HeadlessDialogGroup("ProgressBar", null);
    }

    ProgressBarContext barContext =
        ctx.require(DialogContextKeys.PROGRESS_BAR, ProgressBarContext.class);
    ProgressBar bar = createBar(barContext);
    Container<ProgressBar> container = new Container<>(bar);
    container.setLayoutEnabled(false);
    container.pack();
    container.setPosition(0, 0);
    return container;
  }

  private static ProgressBar createBar(ProgressBarContext barContext) {
    Entity entity =
        Game.findEntityById(barContext.entityId())
            .orElseThrow(() -> new RuntimeException("Entity not found: " + barContext.entityId()));
    String styleName = barContext.styleName();
    float verticalOffset = barContext.verticalOffset();
    ProgressBar bar = new ProgressBar(MIN, MAX, STEP_SIZE, false, defaultSkin(), styleName);
    bar.setAnimateDuration(UPDATE_DURATION);
    bar.setSize(DEFAULT_BAR_WIDTH, DEFAULT_BAR_HEIGHT);
    updatePosition(bar, getBarOriginForEntity(entity, verticalOffset));
    bar.setVisible(true);
    return bar;
  }

  public static Point getBarOriginForEntity(Entity entity, float verticalOffset) {
    Vector3 worldCoords =
        new Vector3(
            EntityUtils.getPosition(entity).x(),
            Game.positionOf(entity).map(Point::y).orElse(0f),
            0);
    Vector3 screenCoords = CameraSystem.camera().project(worldCoords);

    Stage stage = Game.stage().orElseThrow(() -> new RuntimeException("No stage available"));
    screenCoords.x = screenCoords.x / stage.getViewport().getScreenWidth() * stage.getWidth();
    screenCoords.x -= DEFAULT_BAR_WIDTH / 2f; // center the bar horizontally
    screenCoords.y = screenCoords.y / stage.getViewport().getScreenHeight() * stage.getHeight();
    // screenCoords.y -= DEFAULT_BAR_HEIGHT; // adjust for bar height

    return new Point(screenCoords.x, screenCoords.y - verticalOffset);
  }

  /**
   * Updates the position of the progress bar to follow the entity.
   *
   * @param bar the progress bar
   * @param pos the position of the entity
   */
  public static void updatePosition(ProgressBar bar, Point pos) {
    bar.setPosition(pos.x(), pos.y());
  }

  /**
   * Updates the value and visibility of the bar for the entity.
   *
   * @param entity the entity
   * @param barDisplayable the component providing bar data
   * @param barMapping map from component class to progress bar
   * @param verticalOffset vertical offset
   */
  public static void updateBar(
      Entity entity,
      contrib.components.BarDisplayable barDisplayable,
      Map<Class<? extends contrib.components.BarDisplayable>, ProgressBar> barMapping,
      float verticalOffset) {
    ProgressBar bar = barMapping.get(barDisplayable.getClass());
    if (bar == null) return;

    bar.setVisible(
        entity.fetch(DrawComponent.class).map(DrawComponent::isVisible).orElse(false)
            && barDisplayable.current() != barDisplayable.max());
    updatePosition(bar, getBarOriginForEntity(entity, verticalOffset));
    bar.setValue(barDisplayable.current() / barDisplayable.max());
  }

  private record ProgressBarContext(int entityId, String styleName, float verticalOffset)
      implements Serializable {
    @Serial private static final long serialVersionUID = 1L;
  }
}
