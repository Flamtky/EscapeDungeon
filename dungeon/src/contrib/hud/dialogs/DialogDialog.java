package contrib.hud.dialogs;

import com.badlogic.gdx.scenes.scene2d.Action;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Cell;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.Align;
import contrib.configuration.KeyboardConfig;
import contrib.hud.UIUtils;
import core.Game;
import core.utils.BaseContainerUI;
import core.utils.Scene2dElementFactory;
import java.util.List;

/**
 * Package-private builder for a sequenced speaker dialogue ("DialogDialog").
 *
 * <p>Renders a {@link DialogScriptView} (parsed from a single dialog script string via {@link
 * DialogScript}) inside a styled dialog frame.
 *
 * <p>User interaction:
 *
 * <ul>
 *   <li>Any mouse click anywhere on the dialog or pressing the configured interact key (see {@link
 *       contrib.configuration.KeyboardConfig#INTERACT_WORLD}) advances the script view.
 *   <li>If the typewriter is still revealing text, advancing skips to the end of the current
 *       entry's text.
 *   <li>Otherwise, the next page is shown.
 *   <li>After the last page has been confirmed, the {@link DialogContextKeys#ON_CONFIRM} callback
 *       is fired.
 * </ul>
 *
 * <p>Use {@link DialogFactory#showDialogDialog} instead of accessing this class directly.
 */
final class DialogDialog {

  /** Distance in pixels from the top edge of the stage to the top of text pages. */
  private static final float TEXT_PAGE_TOP_OFFSET = 100f;

  /** Distance in pixels from the top edge of the stage to the top of large image pages. */
  private static final float IMAGE_PAGE_TOP_OFFSET = 24f;

  /** Distance in pixels to keep below large image pages. */
  private static final float IMAGE_PAGE_BOTTOM_OFFSET = 8f;

  /** Reserved height for the dialog border and content chrome around large image pages. */
  private static final float IMAGE_PAGE_DIALOG_CHROME_RESERVE = 48f;

  private DialogDialog() {}

  /**
   * Builds a DialogDialog from the given context.
   *
   * <p>On headless servers, returns a {@link HeadlessDialogGroup} placeholder containing all
   * speaker lines concatenated (one per line) so the server can still log/forward the payload.
   *
   * @param ctx The dialog context. Requires {@link DialogContextKeys#DIALOG} as a non-blank {@link
   *     String} script.
   * @return A fully configured DialogDialog or HeadlessDialogGroup.
   */
  static Group build(DialogContext ctx) {
    String script = ctx.require(DialogContextKeys.DIALOG, String.class);
    if (script.isBlank()) {
      throw new DialogCreationException("DialogDialog requires a non-blank dialog script");
    }

    if (Game.isHeadless()) {
      List<DialogEntry> entries =
          DialogScript.parseNonEmpty(script, () -> "DialogDialog script produced no pages");
      return new HeadlessDialogGroup("", DialogScript.toHeadlessText(entries));
    }

    return create(ctx, script);
  }

  private static Group create(DialogContext ctx, String script) {
    Skin skin = UIUtils.defaultSkin();
    boolean textFontSizeControls =
        ctx.find(DialogContextKeys.DIALOG_TEXT_FONT_SIZE_CONTROLS, Boolean.class).orElse(false);

    HandledDialog dialog =
        new HandledDialog("", skin, (d, id) -> true); // no buttons; advance via input listeners
    DialogDesign.setDialogDefaults(dialog, "");

    DialogScriptView scriptView =
        new DialogScriptView(
            script,
            textFontSizeControls
                ? DialogTextFontSizeControls.currentTextFontSize()
                : DialogScriptView.TEXT_FONT_SPEC.size());
    ScrollPane scriptPane = Scene2dElementFactory.createScrollPane(scriptView, false, true);
    scriptPane.setFadeScrollBars(false);
    scriptPane.setScrollbarsOnTop(false);
    Table content = dialog.getContentTable();
    Cell<ScrollPane> scriptPaneCell = content.add(scriptPane);
    content.row();

    scriptView.setOnSequenceComplete(
        () ->
            DialogCallbackResolver.createButtonCallback(
                    ctx.dialogId(), DialogContextKeys.ON_CONFIRM)
                .accept(null));

    Runnable advance =
        () -> {
          DialogScriptView.AdvanceResult result = scriptView.advance();
          if (result == DialogScriptView.AdvanceResult.SEQUENCE_COMPLETE) {
            return;
          }
          updateLayout(
              dialog,
              scriptView,
              scriptPane,
              scriptPaneCell,
              Game.windowWidth(),
              Game.windowHeight());
          updateContainerPlacement(dialog, scriptView);
          Scene2dElementFactory.scrollPaneScrollTo(scriptPane, 0f, 0f);
        };

    dialog.setTouchable(Touchable.enabled);
    dialog.addCaptureListener(
        new InputListener() {
          @Override
          public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
            advance.run();
            event.stop();
            return true;
          }
        });

    // Key listener on the dialog itself, only the configured interact key advances.
    dialog.addListener(
        new InputListener() {
          @Override
          public boolean keyDown(InputEvent event, int keycode) {
            if (textFontSizeControls
                && DialogTextFontSizeControls.handleKeyDown(keycode, scriptView)) {
              updateLayout(
                  dialog,
                  scriptView,
                  scriptPane,
                  scriptPaneCell,
                  Game.windowWidth(),
                  Game.windowHeight());
              updateContainerPlacement(dialog, scriptView);
              return true;
            }
            if (keycode != KeyboardConfig.INTERACT_WORLD.value()) {
              return false;
            }
            advance.run();
            return true;
          }
        });

    // Continuously claim keyboard focus so key input keeps reaching us even after mouse activity.
    dialog.addAction(
        new Action() {
          @Override
          public boolean act(float delta) {
            Stage stage = dialog.getStage();
            if (stage != null) {
              stage.setKeyboardFocus(dialog);
            }
            return false; // run forever
          }
        });

    // Wrap in an actor that clears the local texture cache on stage removal. Textures themselves
    // are owned by the TextureMap and must not be disposed here.
    updateLayout(
        dialog, scriptView, scriptPane, scriptPaneCell, Game.windowWidth(), Game.windowHeight());
    return new BaseContainerUI(dialog, Align.top, 0f, topOffset(scriptView), false, true) {
      @Override
      protected void beforePositionContent(float width, float height) {
        setOffset(0f, topOffset(scriptView));
        updateLayout(dialog, scriptView, scriptPane, scriptPaneCell, width, height);
      }

      @Override
      protected void setStage(Stage stage) {
        super.setStage(stage);
        if (stage == null) {
          scriptView.disposeCache();
        }
      }
    };
  }

  private static void updateLayout(
      HandledDialog dialog,
      DialogScriptView scriptView,
      ScrollPane scriptPane,
      Cell<ScrollPane> scriptPaneCell,
      float viewportWidth,
      float viewportHeight) {
    float panePreferredWidth =
        scriptView.currentEntryIsImagePage() ? Float.MAX_VALUE : scriptView.getPrefWidth();
    float paneWidth =
        ResponsiveDialogLayout.scrollPaneWidthForContent(
            panePreferredWidth, scriptPane, viewportWidth);
    float contentWidth =
        ResponsiveDialogLayout.contentWidthForScrollablePane(
            paneWidth, scriptPane, 0f, viewportWidth);
    scriptPaneCell.width(paneWidth);
    scriptView.imagePageContentWidth(contentWidth);
    scriptView.setWidth(contentWidth);
    scriptView.invalidateHierarchy();
    scriptView.validate();
    scriptPane.invalidateHierarchy();
    dialog.invalidateHierarchy();
    dialog.pack();

    float preferredPaneHeight =
        ResponsiveDialogLayout.scrollPanePreferredHeight(scriptView.getPrefHeight(), scriptPane, 0f);
    float paneHeight =
        scriptView.currentEntryIsImagePage()
            ? imagePagePaneHeight(preferredPaneHeight, viewportHeight)
            : ResponsiveDialogLayout.scrollPaneHeight(
                dialog, scriptPaneCell, preferredPaneHeight, null, viewportHeight);
    scriptPaneCell.width(paneWidth).height(paneHeight);
    scriptPane.invalidateHierarchy();
    dialog.invalidateHierarchy();
    dialog.pack();
  }

  private static float topOffset(DialogScriptView scriptView) {
    return scriptView.currentEntryIsImagePage() ? IMAGE_PAGE_TOP_OFFSET : TEXT_PAGE_TOP_OFFSET;
  }

  private static float imagePagePaneHeight(float preferredPaneHeight, float viewportHeight) {
    float maxDialogHeight =
        ResponsiveDialogLayout.maxDialogHeightWithInsets(
            Math.max(viewportHeight, Game.windowHeight()),
            IMAGE_PAGE_TOP_OFFSET,
            IMAGE_PAGE_BOTTOM_OFFSET);
    float availablePaneHeight = Math.max(1f, maxDialogHeight - IMAGE_PAGE_DIALOG_CHROME_RESERVE);
    return Math.max(1f, Math.min(preferredPaneHeight, availablePaneHeight));
  }

  private static void updateContainerPlacement(
      HandledDialog dialog, DialogScriptView scriptView) {
    if (dialog.getParent() instanceof BaseContainerUI container) {
      container.setOffset(0f, topOffset(scriptView));
    }
  }
}
