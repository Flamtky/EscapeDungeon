package tools.timer;

import core.Game;
import core.System;
import java.util.ArrayList;
import java.util.List;

/**
 * Authoritative timer system that runs on the server.
 *
 * <p>This system manages the server-side timer state, triggers callbacks, and periodically
 * broadcasts synchronization messages to all connected clients. The timer accumulates elapsed time
 * and ensures all clients stay in sync through regular updates at a configurable frequency.
 *
 * <p>The system only runs on the server side ({@link AuthoritativeSide#SERVER}). Clients receive
 * timer updates through {@link TimerSyncMessage} and {@link TimerCommandMessage}.
 */
public class TimerSystem extends System {

  private float elapsedTime = 0f;
  private boolean running = false;
  private float timeSinceLastSync = 0f;
  private final List<TimerCallback> callbacks = new ArrayList<>();

  /**
   * Record representing a timer callback.
   *
   * @param triggerTimeSeconds the elapsed time in seconds at which to trigger
   * @param callback the runnable to execute
   */
  private record TimerCallback(long triggerTimeSeconds, Runnable callback) {}

  /** Creates a new TimerSystem that runs only on the server. */
  public TimerSystem() {
    super(AuthoritativeSide.SERVER);
  }

  /**
   * Updates the timer state and synchronizes with clients.
   *
   * <p>This method is called every tick to accumulate elapsed time, trigger callbacks, and send
   * periodic sync messages to clients. Delta time is calculated from the configured frame rate.
   */
  @Override
  public void execute() {
    if (!running) {
      return;
    }

    float delta = 1f / Game.frameRate();

    elapsedTime += delta;
    timeSinceLastSync += delta;

    triggerCallbacks();

    // Broadcast sync message at configured frequency
    if (timeSinceLastSync >= TimerAPI.SYNC_INTERVAL_SECONDS) {
      broadcastSync();
      timeSinceLastSync = 0f;
    }
  }

  /**
   * Starts the timer with the specified initial elapsed time.
   *
   * <p>Broadcasts a START command to all clients and begins accumulating time on the server.
   *
   * @param startTimeSeconds the initial elapsed time in seconds
   */
  public void start(float startTimeSeconds) {
    this.elapsedTime = startTimeSeconds;
    this.running = true;
    this.timeSinceLastSync = 0f;

    Game.network().broadcast(TimerCommandMessage.start(startTimeSeconds), true);
  }

  /**
   * Stops the timer, pausing time accumulation.
   *
   * <p>Broadcasts a STOP command to all clients.
   */
  public void stop() {
    this.running = false;

    Game.network().broadcast(TimerCommandMessage.stop(), true);
  }

  /**
   * Resumes the timer from its current state.
   *
   * <p>Broadcasts a RESUME command to all clients and resumes time accumulation on the server.
   */
  public void resume() {
    this.running = true;
    this.timeSinceLastSync = 0f;

    Game.network().broadcast(TimerCommandMessage.resume(), true);
    // Send immediate sync to ensure clients have current time
    broadcastSync();
  }

  /**
   * Gets the current elapsed time in seconds.
   *
   * @return the elapsed time in seconds
   */
  public float elapsedSeconds() {
    return elapsedTime;
  }

  /**
   * Registers a one-time callback to be executed when the specified elapsed time is reached.
   *
   * <p>Callbacks are only executed on the server and are not synchronized to clients.
   *
   * @param elapsedSeconds the elapsed time in seconds at which to trigger the callback
   * @param callback the runnable to execute when the time is reached
   */
  public void registerCallback(long elapsedSeconds, Runnable callback) {
    callbacks.add(new TimerCallback(elapsedSeconds, callback));
  }

  /** Clears all registered callbacks. */
  public void clearCallbacks() {
    callbacks.clear();
  }

  /**
   * Checks and triggers all callbacks whose time threshold has been reached.
   *
   * <p>Triggered callbacks are removed from the list after execution.
   */
  private void triggerCallbacks() {
    long currentSeconds = (long) elapsedTime;

    callbacks.removeIf(
        callback -> {
          if (callback.triggerTimeSeconds() <= currentSeconds) {
            callback.callback().run();
            return true; // Remove this callback
          }
          return false; // Keep this callback
        });
  }

  /** Broadcasts the current timer state to all connected clients. */
  private void broadcastSync() {
    Game.network().broadcast(new TimerSyncMessage(elapsedTime, running), true);
  }
}
