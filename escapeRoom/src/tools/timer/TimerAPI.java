package tools.timer;

import core.game.ECSManagement;
import core.game.PreRunConfiguration;

/**
 * Public API for managing an elapsed time timer in the escape room.
 *
 * <p>In multiplayer mode, the timer is authoritative on the server. Server-side callbacks are
 * triggered only on the server, while the UI is synchronized to all clients at regular intervals.
 * Clients use prediction and interpolation for smooth display.
 *
 * <p>Provides methods to start, stop, and resume the timer, as well as register one-time callbacks
 * that trigger after specified elapsed time (server-side only). The timer is displayed in HH:MM:SS
 * format (or MM:SS if hours is zero) in the top-right corner of the screen.
 *
 * <p>Example usage:
 *
 * <pre>{@code
 * TimerAPI.start();
 * TimerAPI.registerCallback(300, () -> System.out.println("5 minutes passed!"));
 * }</pre>
 */
public final class TimerAPI {

  /**
   * Synchronization frequency in seconds.
   *
   * <p>The server broadcasts timer state to all clients at this interval to keep them in sync.
   */
  public static final float SYNC_INTERVAL_SECONDS = 5.0f;

  static {
    TimerDialog.currentUI(); // just to trigger static init
  }

  private TimerAPI() {
    // Utility class
  }

  /**
   * Starts a timer with an initial elapsed time.
   *
   * <p>On the server (including single-player), this starts the authoritative timer system and
   * broadcasts the start command to all clients (if in multiplayer). On clients, the timer will be
   * started via network message.
   *
   * <p>Creates the timer dialog if it doesn't exist and sets the timer to a running state. Elapsed
   * time begins accumulating immediately.
   *
   * @param startTimeSeconds the initial elapsed time in seconds
   */
  public static void start(float startTimeSeconds) {
    if (PreRunConfiguration.isNetworkServer()) {
      ECSManagement.system(TimerSystem.class, timerSystem -> timerSystem.start(startTimeSeconds));
    }
  }

  /**
   * Starts the timer with an initial elapsed time of zero seconds.
   *
   * <p>On the server (including single-player), this starts the authoritative timer system and
   * broadcasts the start command to all clients (if in multiplayer). On clients, the timer will be
   * started via network message.
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
   * <p>On the server (including single-player), this stops the authoritative timer system and
   * broadcasts the stop command to all clients (if in multiplayer). On clients, the timer will be
   * stopped via network message.
   *
   * <p>The timer remains visible on the screen. Use {@link #resume()} to continue counting.
   */
  public static void stop() {
    if (PreRunConfiguration.isNetworkServer()) {
      ECSManagement.system(TimerSystem.class, TimerSystem::stop);
    }
  }

  /**
   * Resumes the timer after it has been stopped.
   *
   * <p>On the server (including single-player), this resumes the authoritative timer system and
   * broadcasts the resume command to all clients (if in multiplayer). On clients, the timer will be
   * resumed via network message.
   *
   * <p>Time accumulation resumes from where it was paused.
   */
  public static void resume() {
    if (PreRunConfiguration.isNetworkServer()) {
      ECSManagement.system(TimerSystem.class, TimerSystem::resume);
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
   * Gets the current elapsed time in seconds.
   *
   * <p>On the server (including single-player), returns the authoritative server time. On clients,
   * returns the predicted local time from the UI.
   *
   * @return the elapsed time in seconds, or 0 if the timer is not active
   */
  public static float elapsedSeconds() {
    if (PreRunConfiguration.isNetworkServer()) {
      var timerSys = ECSManagement.systems().get(TimerSystem.class);
      if (timerSys == null) {
        return 0;
      }
      return ((TimerSystem) timerSys).elapsedSeconds();
    } else {
      TimerUI ui = TimerDialog.currentUI();
      if (ui != null) {
        return ui.elapsedSeconds();
      }
      return 0;
    }
  }

  /**
   * Registers a one-time callback to be executed when the specified elapsed time is reached.
   *
   * <p>Callbacks are only executed on the server side (including single-player). In multiplayer,
   * this method should only be called on the server.
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
    if (PreRunConfiguration.isNetworkServer()) {
      ECSManagement.system(TimerSystem.class, ts -> ts.registerCallback(elapsedSeconds, callback));
    }
  }

  /**
   * Clears all registered callbacks.
   *
   * <p>This removes all pending callbacks that have not yet been triggered. On the server
   * (including single-player), this clears callbacks from the authoritative timer system.
   */
  public static void clearCallbacks() {
    if (PreRunConfiguration.isNetworkServer()) {
      ECSManagement.system(TimerSystem.class, TimerSystem::clearCallbacks);
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
