package tools.timer;

import com.badlogic.gdx.scenes.scene2d.Group;
import contrib.hud.dialogs.DialogContext;
import contrib.hud.dialogs.DialogFactory;
import core.Game;
import core.game.WindowEventManager;
import mushRoom.modules.EscapeRoomDialogTypes;

/**
 * Utility class for managing Timer dialogs.
 *
 * <p>Handles registration of the timer dialog type with the DialogFactory and provides methods to
 * manage the timer UI lifecycle.
 */
public final class TimerDialog {

  private static TimerUI currentUI;

  static {
    DialogFactory.register(EscapeRoomDialogTypes.TIMER, TimerDialog::build);

    if (!Game.isHeadless()) {
      WindowEventManager.registerWindowRefreshListener(TimerDialog::handleResize);
    }
  }

  private TimerDialog() {
    // Utility class
  }

  /** Handles window resize events by updating the timer UI bounds. */
  private static void handleResize() {
    if (currentUI != null) {
      currentUI.setSize(Game.windowWidth(), Game.windowHeight());
    }
  }

  /**
   * Builds and returns a TimerUI instance for the given DialogContext.
   *
   * @param dialogContext the dialog context used to build the UI
   * @return a new TimerUI instance
   */
  private static Group build(DialogContext dialogContext) {
    float startTime = dialogContext.find("startTimeSeconds", Float.class).orElse(0f);

    if (currentUI != null) { // Dispose of existing UI if present
      currentUI.dispose();
      currentUI.remove();
    }
    currentUI = new TimerUI(startTime);
    return currentUI;
  }

  /**
   * Returns the currently active TimerUI instance.
   *
   * @return the current TimerUI, or null if no timer is active
   */
  public static TimerUI currentUI() {
    return currentUI;
  }
}
