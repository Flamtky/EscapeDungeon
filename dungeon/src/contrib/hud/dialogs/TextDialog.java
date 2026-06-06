package contrib.hud.dialogs;

import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.ui.Cell;
import com.badlogic.gdx.scenes.scene2d.ui.Dialog;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import contrib.hud.UIUtils;
import contrib.hud.elements.RichLabel;
import core.Game;
import core.utils.BaseContainerUI;
import core.utils.Scene2dElementFactory;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;

/**
 * A simple Text Dialog that shows only the provided string in a scrolling pane. Use as alternative
 * to the OkDialog but for longer texts.
 */
public class TextDialog extends Dialog {
  private static final float CONTENT_INSET = 12f;

  private final BiFunction<Dialog, String, Boolean> resultHandler;

  /**
   * Creates an empty text dialog shell for custom content builders.
   *
   * @param title Title for the dialog.
   * @param skin Skin used by the dialog.
   * @param resultHandler Handler invoked when a dialog result is submitted.
   */
  public TextDialog(String title, Skin skin, BiFunction<Dialog, String, Boolean> resultHandler) {
    super(title, skin);
    this.resultHandler = resultHandler;
  }

  /**
   * Creates an empty text dialog shell with a named window style for custom content builders.
   *
   * @param title Title for the dialog.
   * @param skin Skin used by the dialog.
   * @param windowStyleName Name of the window style to use.
   * @param resultHandler Handler invoked when a dialog result is submitted.
   */
  public TextDialog(
      String title,
      Skin skin,
      String windowStyleName,
      BiFunction<Dialog, String, Boolean> resultHandler) {
    super(title, skin, windowStyleName);
    this.resultHandler = resultHandler;
  }

  /**
   * Builds a text dialog from the given context.
   *
   * <p>On headless servers, returns a {@link HeadlessDialogGroup} placeholder.
   *
   * @param ctx The dialog context containing message, buttons, and handlers
   * @return A fully configured text dialog or HeadlessDialogGroup
   */
  static Group build(DialogContext ctx) {
    String text = ctx.require(DialogContextKeys.MESSAGE, String.class);
    String title = ctx.find(DialogContextKeys.TITLE, String.class).orElse("");
    String button =
        ctx.find(DialogContextKeys.CONFIRM_LABEL, String.class).orElse(OkDialog.DEFAULT_OK_BUTTON);
    float contentWidth =
        ctx.find(DialogContextKeys.TEXT_DIALOG_CONTENT_WIDTH, Float.class)
            .orElse(ResponsiveDialogLayout.DEFAULT_CONTENT_WIDTH);
    Float contentHeight =
        ctx.find(DialogContextKeys.TEXT_DIALOG_CONTENT_HEIGHT, Float.class).orElse(null);

    // On headless server, return a placeholder
    if (Game.isHeadless()) {
      List<String> allButtons = new ArrayList<>();
      allButtons.add(button);
      return new HeadlessDialogGroup(title, text, allButtons.toArray(new String[0]));
    }

    return create(ctx, text, button, title, contentWidth, contentHeight);
  }

  /**
   * A simple Text Dialog that shows only the provided string.
   *
   * @param ctx The dialog context
   * @param message The text which should be shown in the middle of the dialog.
   * @param confirmButton Text that the button should have; also the ID for the result handler.
   * @param title Title for the dialog.
   * @param contentWidth preferred width for the scrollable content area.
   * @param contentHeight maximum height for the scrollable content area, or null for an automatic
   *     height up to 80% of the screen.
   * @return The fully configured Dialog, which can then be added where it is needed.
   */
  private static Group create(
      DialogContext ctx,
      String message,
      String confirmButton,
      String title,
      float contentWidth,
      Float contentHeight) {
    Skin skin = UIUtils.defaultSkin();

    Dialog dialog =
        new HandledDialog(
            title,
            skin,
            (d, id) -> {
              if (id.equals(confirmButton)) {
                DialogCallbackResolver.createButtonCallback(
                        ctx.dialogId(), DialogContextKeys.ON_CONFIRM)
                    .accept(null);
              }
              return true;
            });

    DialogDesign.setDialogDefaults(dialog, title);
    Table content = dialog.getContentTable();

    RichLabel label =
        new RichLabel(RichLabel.toRichText(message), DialogDesign.DIALOG_FONT_SPEC_NORMAL);
    label.setWrap(true);

    Table labelTable = new Table();
    labelTable.top().left().pad(CONTENT_INSET);

    ScrollPane pane = Scene2dElementFactory.createScrollPane(labelTable, false, true);
    pane.setFadeScrollBars(false);
    pane.setScrollbarsOnTop(false);
    Cell<RichLabel> labelCell = labelTable.add(label).top().left();
    Cell<ScrollPane> paneCell = content.add(pane).padBottom(10);
    content.row();

    dialog.button(
        confirmButton, confirmButton, skin.get("clean-green", TextButton.TextButtonStyle.class));

    updateTextDialogLayout(
        dialog,
        label,
        labelCell,
        pane,
        paneCell,
        contentWidth,
        contentHeight,
        Game.windowWidth(),
        Game.windowHeight());
    dialog.pack();
    return resizeAwareContainer(
        dialog, label, labelCell, pane, paneCell, contentWidth, contentHeight);
  }

  private static BaseContainerUI resizeAwareContainer(
      Dialog dialog,
      RichLabel label,
      Cell<RichLabel> labelCell,
      ScrollPane pane,
      Cell<ScrollPane> paneCell,
      float contentWidth,
      Float contentHeight) {
    return new BaseContainerUI(dialog) {
      @Override
      protected void beforePositionContent(float width, float height) {
        updateTextDialogLayout(
            dialog, label, labelCell, pane, paneCell, contentWidth, contentHeight, width, height);
      }
    };
  }

  private static void updateTextDialogLayout(
      Dialog dialog,
      RichLabel label,
      Cell<RichLabel> labelCell,
      ScrollPane pane,
      Cell<ScrollPane> paneCell,
      float contentWidth,
      Float contentHeight,
      float viewportWidth,
      float viewportHeight) {
    float actualContentWidth = ResponsiveDialogLayout.contentWidth(contentWidth, viewportWidth);
    float labelWidth =
        ResponsiveDialogLayout.contentWidthForScrollablePane(
            contentWidth, pane, CONTENT_INSET, viewportWidth);

    label.setMaxPrefWidth(labelWidth);
    label.setWidth(labelWidth);
    labelCell.width(labelWidth);
    label.invalidateHierarchy();
    label.validate();

    paneCell.width(actualContentWidth);
    float paneHeight =
        ResponsiveDialogLayout.scrollPaneHeight(
            dialog,
            paneCell,
            ResponsiveDialogLayout.scrollPanePreferredHeight(
                label.getPrefHeight(), pane, CONTENT_INSET),
            contentHeight,
            viewportHeight);
    paneCell.width(actualContentWidth).height(paneHeight);
    pane.invalidateHierarchy();
    dialog.invalidateHierarchy();
    dialog.pack();
  }

  @Override
  protected void result(Object object) {
    if (resultHandler != null && !resultHandler.apply(this, object.toString())) {
      cancel();
    }
  }
}
