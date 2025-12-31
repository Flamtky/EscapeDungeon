package tools.timer;

import contrib.hud.dialogs.DialogContext;
import contrib.hud.dialogs.DialogFactory;
import mushRoom.modules.EscapeRoomDialogTypes;

/**
 * Public API for managing an elapsed time timer in the escape room.
 *
 * <p>Provides methods to start, stop, resume, and reset the timer, as well as register one-time
 * callbacks that trigger after specified elapsed time. The timer is displayed in HH:MM:SS format
 * (or MM:SS if hours is zero) in the top-right corner of the screen.
 *
 * <p>Example usage:
 *
 * <pre>{@code
 * TimerAPI.start();
 * TimerAPI.registerCallback(300, () -> System.out.println("5 minutes passed!"));
 * }</pre>
 */
public final class TimerAPI {

  static {
    TimerDialog.currentUI(); // just to trigger static init
  }

  private TimerAPI() {
    // Utility class
  }

  /**
   * Starts a timer and displays it on the HUD with an initial elapsed time.
   *
   * <p>Creates the timer dialog if it doesn't exist and sets the timer to a running state. Elapsed
   * time begins accumulating immediately.
   *
   * @param startTimeSeconds the initial elapsed time in seconds
   */
  public static void start(float startTimeSeconds) {
    DialogContext ctx =
        DialogContext.builder()
            .type(EscapeRoomDialogTypes.TIMER)
            .put("startTimeSeconds", startTimeSeconds)
            .build();
    DialogFactory.show(ctx, false, false);

    TimerUI ui = TimerDialog.currentUI();
    if (ui != null) {
      ui.start();
    }
  }

  /**
   * Starts the timer and displays it on the HUD with an initial elapsed time of zero seconds.
   *
   * <p>Creates the timer dialog if it doesn't exist and sets the timer to a running state. Elapsed
   * time begins accumulating immediately.
   */
  public static void start() {
    start(0f);
  }

  /**
   * Stops the timer, pausing time accumulation.
   *
   * <p>The timer remains visible on the screen. Use {@link #resume()} to continue counting.
   */
  public static void stop() {
    TimerUI ui = TimerDialog.currentUI();
    if (ui != null) {
      ui.stop();
    }
  }

  /**
   * Resumes the timer after it has been stopped.
   *
   * <p>Time accumulation resumes from where it was paused.
   */
  public static void resume() {
    TimerUI ui = TimerDialog.currentUI();
    if (ui != null) {
      ui.resume();
    }
  }

  /**
   * Sets the visibility of the timer display.
   *
   * <p>This controls only the visual display of the timer without affecting its running state. Use
   * {@link #stop()} to pause time accumulation.
   *
   * @param visible true to show the timer, false to hide it
   */
  public static void visible(boolean visible) {
    TimerUI ui = TimerDialog.currentUI();
    if (ui != null) {
      ui.setVisible(visible);
    }
  }

  /**
   * Resets the elapsed time to zero.
   *
   * <p>Clears all pending callbacks, allowing them to be registered again if desired. Does not
   * affect the running state of the timer.
   */
  public static void reset() {
    TimerUI ui = TimerDialog.currentUI();
    if (ui != null) {
      ui.reset();
    }
  }

  /**
   * Gets the current elapsed time in seconds.
   *
   * @return the elapsed time in seconds, or 0 if the timer is not active
   */
  public static float elapsedSeconds() {
    TimerUI ui = TimerDialog.currentUI();
    if (ui != null) {
      return ui.elapsedSeconds();
    }
    return 0;
  }

  /**
   * Registers a one-time callback to be executed when the specified elapsed time is reached.
   *
   * <p>Multiple callbacks can be registered for the same elapsed time. Each callback is executed
   * only once and then automatically removed. To trigger the same action multiple times, register a
   * new callback after the first one fires.
   *
   * <p>Example: <br>
   *
   * <pre>
   * TimerAPI.registerCallback(300, () -> System.out.println("5 minutes passed!"));
   * TimerAPI.registerCallback(600, () -> System.out.println("10 minutes passed!"));
   * </pre>
   *
   * @param elapsedSeconds the elapsed time in seconds at which to trigger the callback
   * @param callback the runnable to execute when the time is reached
   */
  public static void registerCallback(long elapsedSeconds, Runnable callback) {
    TimerUI ui = TimerDialog.currentUI();
    if (ui != null) {
      ui.registerCallback(elapsedSeconds, callback);
    }
  }

  /**
   * Clears all registered callbacks.
   *
   * <p>This removes all pending callbacks that have not yet been triggered.
   */
  public static void clearCallbacks() {
    TimerUI ui = TimerDialog.currentUI();
    if (ui != null) {
      ui.clearCallbacks();
    }
  }

  /**
   * Sets the display format for the timer text.
   *
   * <p>The format string should contain {@code %time} as a placeholder for the formatted time
   * (HH:MM:SS or MM:SS). For example: "Current time is: %time" will display as "Current time is:
   * 00:05".
   *
   * <p>By default, the format is set to "%time", displaying only the time.
   *
   * @param format the format string with {@code %time} placeholder
   */
  public static void displayFormat(String format) {
    TimerUI ui = TimerDialog.currentUI();
    if (ui != null) {
      ui.displayFormat(format);
    }
  }
}
