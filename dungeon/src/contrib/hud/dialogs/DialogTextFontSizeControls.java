package contrib.hud.dialogs;

import com.badlogic.gdx.Input;

/** Shared keyboard controls for resizing dialog script text while a dialog is open. */
final class DialogTextFontSizeControls {

  private static final int MIN_TEXT_FONT_SIZE = 12;
  private static final int MAX_TEXT_FONT_SIZE = 40;
  private static final int TEXT_FONT_SIZE_STEP = 2;

  private static int textFontSize = DialogScriptView.TEXT_FONT_SPEC.size();

  private DialogTextFontSizeControls() {}

  static int currentTextFontSize() {
    return textFontSize;
  }

  static boolean handleKeyDown(int keycode, DialogScriptView scriptView) {
    if (keycode == Input.Keys.NUMPAD_ADD) {
      changeTextFontSize(scriptView, TEXT_FONT_SIZE_STEP);
      return true;
    }
    if (keycode == Input.Keys.NUMPAD_SUBTRACT) {
      changeTextFontSize(scriptView, -TEXT_FONT_SIZE_STEP);
      return true;
    }
    return false;
  }

  private static void changeTextFontSize(DialogScriptView scriptView, int delta) {
    textFontSize = Math.max(MIN_TEXT_FONT_SIZE, Math.min(MAX_TEXT_FONT_SIZE, textFontSize + delta));
    scriptView.textFontSize(textFontSize);
  }
}
