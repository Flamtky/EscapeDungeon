package contrib.hud.dialogs;

import com.badlogic.gdx.scenes.scene2d.ui.Cell;
import com.badlogic.gdx.scenes.scene2d.ui.Dialog;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import core.Game;

final class ResponsiveDialogLayout {
  static final float DEFAULT_CONTENT_WIDTH = 820f;
  private static final float MAX_SCREEN_WIDTH_RATIO = 0.9f;
  private static final float MAX_SCREEN_HEIGHT_RATIO = 0.8f;
  private static final float SCREEN_EDGE_MARGIN = 24f;
  private static final float MIN_SCROLLBAR_RESERVE = 32f;
  private static final float SCROLL_PANE_VERTICAL_RESERVE = 24f;

  private ResponsiveDialogLayout() {}

  static float contentWidth(float preferredWidth, float viewportWidth) {
    float maxWidth =
        Math.min(viewportWidth * MAX_SCREEN_WIDTH_RATIO, viewportWidth - SCREEN_EDGE_MARGIN * 2f);
    if (maxWidth <= 0) {
      return preferredWidth;
    }
    return Math.max(1f, Math.min(preferredWidth, maxWidth));
  }

  static float contentWidthForScrollablePane(
      float preferredWidth, ScrollPane pane, float contentInset, float viewportWidth) {
    float actualContentWidth = contentWidth(preferredWidth, viewportWidth);
    return Math.max(1f, actualContentWidth - verticalScrollbarReserve(pane) - contentInset * 2f);
  }

  static float scrollPaneWidthForContent(
      float preferredContentWidth, ScrollPane pane, float viewportWidth) {
    return contentWidth(preferredContentWidth + verticalScrollbarReserve(pane), viewportWidth);
  }

  static float scrollPanePreferredHeight(
      float contentPreferredHeight, ScrollPane pane, float contentInset) {
    return contentPreferredHeight + contentInset * 2f + scrollPaneVerticalReserve(pane);
  }

  static float scrollPaneHeight(
      Dialog dialog,
      Cell<ScrollPane> paneCell,
      float preferredPaneHeight,
      Float configuredMaxPaneHeight,
      float viewportHeight) {
    return scrollPaneHeightForDialogHeight(
        dialog,
        paneCell,
        preferredPaneHeight,
        configuredMaxPaneHeight,
        maxDialogHeight(viewportHeight));
  }

  static float scrollPaneHeightForDialogHeight(
      Dialog dialog,
      Cell<ScrollPane> paneCell,
      float preferredPaneHeight,
      Float configuredMaxPaneHeight,
      float maxDialogHeight) {
    float availablePaneHeight =
        Math.max(1f, maxDialogHeight - dialogChromeHeight(dialog, paneCell));
    if (configuredMaxPaneHeight != null) {
      availablePaneHeight = Math.min(availablePaneHeight, configuredMaxPaneHeight);
    }
    return Math.max(1f, Math.min(preferredPaneHeight, availablePaneHeight));
  }

  static float maxDialogHeightWithInsets(
      float viewportHeight, float topInset, float bottomInset) {
    float height = viewportHeight > 0 ? viewportHeight : Game.windowHeight();
    return Math.max(1f, height - Math.max(0f, topInset) - Math.max(0f, bottomInset));
  }

  private static float verticalScrollbarReserve(ScrollPane pane) {
    ScrollPane.ScrollPaneStyle style = pane.getStyle();
    float reserve = 0f;
    if (style.vScroll != null) {
      reserve = Math.max(reserve, style.vScroll.getMinWidth());
    }
    if (style.vScrollKnob != null) {
      reserve = Math.max(reserve, style.vScrollKnob.getMinWidth());
    }
    return Math.max(MIN_SCROLLBAR_RESERVE, reserve + 8f);
  }

  private static float dialogChromeHeight(Dialog dialog, Cell<ScrollPane> paneCell) {
    paneCell.height(1f);
    dialog.invalidateHierarchy();
    dialog.pack();
    return Math.max(0f, dialog.getHeight() - 1f);
  }

  private static float maxDialogHeight(float viewportHeight) {
    float height = viewportHeight > 0 ? viewportHeight : Game.windowHeight();
    float marginHeight = height - SCREEN_EDGE_MARGIN * 2f;
    float ratioHeight = height * MAX_SCREEN_HEIGHT_RATIO;
    if (marginHeight <= 0) {
      return Math.max(1f, ratioHeight);
    }
    return Math.max(1f, Math.min(ratioHeight, marginHeight));
  }

  private static float scrollPaneVerticalReserve(ScrollPane pane) {
    ScrollPane.ScrollPaneStyle style = pane.getStyle();
    float reserve = SCROLL_PANE_VERTICAL_RESERVE;
    if (style.background != null) {
      reserve += style.background.getTopHeight() + style.background.getBottomHeight();
    }
    return reserve;
  }
}
