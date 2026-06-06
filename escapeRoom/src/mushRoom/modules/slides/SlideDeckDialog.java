package mushRoom.modules.slides;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.scenes.scene2d.Action;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Scaling;
import contrib.components.UIComponent;
import contrib.hud.UIUtils;
import contrib.hud.dialogs.DialogCallbackResolver;
import contrib.hud.dialogs.DialogContext;
import contrib.hud.dialogs.DialogContextKeys;
import contrib.hud.dialogs.DialogCreationException;
import contrib.hud.dialogs.DialogFactory;
import contrib.hud.dialogs.HeadlessDialogGroup;
import core.Game;
import core.utils.components.draw.TextureMap;
import core.utils.components.path.SimpleIPath;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import mushRoom.modules.EscapeRoomDialogTypes;

/** Dialog that displays a fullscreen sequence of slide images. */
public final class SlideDeckDialog {

  private static final String SLIDES = "slides";
  private static final String ZOOM_OUT_ON_FINISH = "zoomOutOnFinish";
  private static final String BACKGROUND_TEXTURE = "hud/white.png";
  private static final Color BACKGROUND_COLOR = new Color(0f, 0f, 0f, 0.94f);
  private static final float FINISH_ZOOM_OUT_SCALE = 0.42f;
  private static final float FINISH_ZOOM_OUT_DURATION = 1.15f;
  private static boolean activeDeckOpen;

  private SlideDeckDialog() {}

  /** Registers the slide deck dialog type with the global {@link DialogFactory}. */
  public static void register() {
    DialogFactory.register(EscapeRoomDialogTypes.SLIDE_DECK, SlideDeckDialog::build);
  }

  /**
   * Shows a fullscreen slide deck for the given target entities.
   *
   * @param slidePaths ordered image paths relative to the asset root
   * @param targetEntityIds target entity ids that should see the dialog
   * @return the created {@link UIComponent}, or {@link Optional#empty()} for an empty deck
   */
  public static Optional<UIComponent> show(List<Integer> slidePaths, int... targetEntityIds) {
    return show(slidePaths, false, targetEntityIds);
  }

  /**
   * Shows a fullscreen slide deck for the given target entities.
   *
   * @param slidePaths ordered image paths relative to the asset root
   * @param zoomOutOnFinish whether the last slide should animate before the dialog closes
   * @param targetEntityIds target entity ids that should see the dialog
   * @return the created {@link UIComponent}, or {@link Optional#empty()} for an empty deck
   */
  public static Optional<UIComponent> show(
      List<Integer> slidePaths, boolean zoomOutOnFinish, int... targetEntityIds) {
    Objects.requireNonNull(slidePaths, "slidePaths");
    List<String> slides =
        slidePaths.stream()
            .map(SlideDeckDialog::applyMainPath)
            .filter(SlideDeckDialog::isUsablePath)
            .toList();
    if (slides.isEmpty()) {
      return Optional.empty();
    }
    if (activeDeckOpen) {
      return Optional.empty();
    }

    register();
    DialogContext context =
        DialogContext.builder()
            .type(EscapeRoomDialogTypes.SLIDE_DECK)
            .center(false)
            .put(SLIDES, new ArrayList<>(slides))
            .put(ZOOM_OUT_ON_FINISH, zoomOutOnFinish)
            .build();
    UIComponent ui = DialogFactory.show(context, true, true, targetEntityIds);
    activeDeckOpen = true;
    ui.registerCallback(
        DialogContextKeys.ON_CLOSE,
        data -> {
          activeDeckOpen = false;
          UIUtils.closeDialog(ui);
        });
    return Optional.of(ui);
  }

  private static final String ROOT_PATH = "slides/";
  private static final String FILE_EXTENSION = ".png";

  private static String applyMainPath(Integer path) {
    return ROOT_PATH + path + FILE_EXTENSION;
  }

  /**
   * Builds a slide deck dialog from a {@link DialogContext}.
   *
   * @param ctx dialog context containing the ordered slide path list
   * @return a fullscreen slide deck UI or headless placeholder
   */
  @SuppressWarnings("unchecked")
  public static Group build(DialogContext ctx) {
    List<String> slides = (List<String>) ctx.require(SLIDES, ArrayList.class);
    if (slides.isEmpty()) {
      throw new DialogCreationException("SlideDeckDialog requires at least one slide");
    }

    if (Game.isHeadless()) {
      return new HeadlessDialogGroup("Slide Deck", String.join("\n", slides));
    }

    boolean zoomOutOnFinish = ctx.find(ZOOM_OUT_ON_FINISH, Boolean.class).orElse(false);
    return new SlideDeckUI(ctx.dialogId(), slides, zoomOutOnFinish);
  }

  private static boolean isUsablePath(String path) {
    return path != null && !path.isBlank();
  }

  private static final class SlideDeckUI extends Group {
    private final String dialogId;
    private final List<String> slides;
    private final boolean zoomOutOnFinish;
    private final Image background;
    private final Image slideImage;
    private int currentIndex;
    private boolean closing;
    private boolean closed;

    private SlideDeckUI(String dialogId, List<String> slides, boolean zoomOutOnFinish) {
      this.dialogId = dialogId;
      this.slides = List.copyOf(slides);
      this.zoomOutOnFinish = zoomOutOnFinish;
      setTouchable(Touchable.enabled);

      Texture backgroundTexture =
          TextureMap.instance().textureAt(new SimpleIPath(BACKGROUND_TEXTURE));
      background = new Image(new TextureRegionDrawable(new TextureRegion(backgroundTexture)));
      background.setScaling(Scaling.stretch);
      background.setColor(BACKGROUND_COLOR);

      slideImage = new Image();
      slideImage.setScaling(Scaling.fit);
      slideImage.setAlign(Align.center);

      addActor(background);
      addActor(slideImage);
      updateBounds();
      showSlide(0);
      addDeckInputListener();
      claimKeyboardFocus();
    }

    private void addDeckInputListener() {
      addListener(
          new InputListener() {
            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
              if (button != Input.Buttons.LEFT) {
                return false;
              }
              next();
              return true;
            }

            @Override
            public boolean keyDown(InputEvent event, int keycode) {
              if (keycode == Input.Keys.RIGHT) {
                next();
                return true;
              }
              if (keycode == Input.Keys.LEFT) {
                previous();
                return true;
              }
              if (keycode == Input.Keys.ESCAPE) {
                close();
                return true;
              }
              return false;
            }
          });
    }

    private void claimKeyboardFocus() {
      addAction(
          new Action() {
            @Override
            public boolean act(float delta) {
              Stage stage = getStage();
              if (stage != null) {
                stage.setKeyboardFocus(SlideDeckUI.this);
              }
              return false;
            }
          });
    }

    private void next() {
      if (closing) {
        return;
      }
      if (currentIndex >= slides.size() - 1) {
        if (zoomOutOnFinish) {
          finishWithZoomOut();
          return;
        }
        close();
        return;
      }
      showSlide(currentIndex + 1);
    }

    private void previous() {
      if (closing) {
        return;
      }
      if (currentIndex <= 0) {
        return;
      }
      showSlide(currentIndex - 1);
    }

    private void showSlide(int index) {
      currentIndex = index;
      Texture texture = TextureMap.instance().textureAt(new SimpleIPath(slides.get(currentIndex)));
      slideImage.setDrawable(new TextureRegionDrawable(new TextureRegion(texture)));
      slideImage.clearActions();
      slideImage.setScale(1f);
      slideImage.setColor(Color.WHITE);
      background.clearActions();
      background.setColor(BACKGROUND_COLOR);
    }

    private void finishWithZoomOut() {
      closing = true;
      setTouchable(Touchable.disabled);
      slideImage.addAction(
          Actions.parallel(
              Actions.scaleTo(
                  FINISH_ZOOM_OUT_SCALE,
                  FINISH_ZOOM_OUT_SCALE,
                  FINISH_ZOOM_OUT_DURATION,
                  Interpolation.pow2Out),
              Actions.fadeOut(FINISH_ZOOM_OUT_DURATION, Interpolation.pow2Out)));
      background.addAction(Actions.fadeOut(FINISH_ZOOM_OUT_DURATION, Interpolation.pow2Out));
      addAction(
          Actions.sequence(Actions.delay(FINISH_ZOOM_OUT_DURATION), Actions.run(this::close)));
    }

    private void close() {
      if (closed) {
        return;
      }
      closed = true;
      DialogCallbackResolver.createButtonCallback(dialogId, DialogContextKeys.ON_CLOSE)
          .accept(null);
    }

    @Override
    public void act(float delta) {
      super.act(delta);
      updateBounds();
    }

    private void updateBounds() {
      setBounds(0f, 0f, Game.windowWidth(), Game.windowHeight());
      background.setBounds(0f, 0f, getWidth(), getHeight());
      slideImage.setBounds(0f, 0f, getWidth(), getHeight());
      slideImage.setOrigin(getWidth() / 2f, getHeight() / 2f);
    }
  }
}
