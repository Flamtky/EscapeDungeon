package tools.timer;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.scenes.scene2d.Group;
import core.Game;
import core.utils.FontHelper;
import java.util.ArrayList;
import java.util.List;

/**
 * A UI component that displays elapsed time in HH:MM:SS format (or MM:SS if hours is zero).
 *
 * <p>The timer is positioned in the top-right corner of the screen and can be started, stopped,
 * reset, and shown/hidden independently. Supports registering callbacks that trigger after a
 * specified elapsed time.
 */
public class TimerUI extends Group {

  private static final int FONT_SIZE = 16;
  private static final Color FONT_COLOR = Color.WHITE;
  private static final float PADDING_X = 2;
  private static final float PADDING_Y = 2;
  private static final String DEFAULT_TIME_FORMAT = "%time";

  private final BitmapFont font;
  private final GlyphLayout glyphLayout;
  private final List<TimerCallback> callbacks;

  private float elapsedTime; // seconds
  private boolean running = false;
  private String displayFormat = DEFAULT_TIME_FORMAT;

  /**
   * Record representing a timer callback.
   *
   * @param triggerTimeSeconds the elapsed time in seconds at which to trigger
   * @param callback the runnable to execute
   */
  private record TimerCallback(long triggerTimeSeconds, Runnable callback) {}

  /**
   * Creates a new TimerUI instance.
   *
   * <p>The timer is initialized in a stopped state and must be started via the {@link TimerAPI}.
   *
   * @param startTimeSeconds the initial elapsed time in seconds
   */
  public TimerUI(float startTimeSeconds) {
    this.font = FontHelper.getFont(FontHelper.DEFAULT_FONT_PATH, FONT_SIZE, FONT_COLOR);
    this.glyphLayout = new GlyphLayout();
    this.callbacks = new ArrayList<>();

    this.elapsedTime = startTimeSeconds;

    this.setBounds(0, 0, Game.windowWidth(), Game.windowHeight());
  }

  /**
   * Creates a new TimerUI instance with an initial elapsed time of zero seconds.
   *
   * <p>The timer is initialized in a stopped state and must be started via the {@link TimerAPI}.
   */
  public TimerUI() {
    this(0f);
  }

  /**
   * Updates the timer each frame, accumulating elapsed time and triggering callbacks.
   *
   * <p>This method is called automatically by the scene2d framework. Time accumulation occurs
   * regardless of game pause state.
   *
   * @param delta the time elapsed since the last frame in seconds
   */
  @Override
  public void act(float delta) {
    super.act(delta);

    if (running) {
      elapsedTime += delta;
      triggerCallbacks();
    }
  }

  /**
   * Renders the timer text to the screen.
   *
   * <p>The timer is positioned in the top-right corner with applied padding.
   *
   * @param batch the batch to draw with
   * @param parentAlpha the alpha value to use for drawing
   */
  @Override
  public void draw(Batch batch, float parentAlpha) {
    super.draw(batch, parentAlpha);

    String timeString = formatTime();
    glyphLayout.setText(font, timeString);

    float x = Game.windowWidth() - glyphLayout.width - PADDING_X;
    float y = Game.windowHeight() - PADDING_Y;

    font.draw(batch, timeString, x, y);
  }

  /** Starts the timer, allowing it to accumulate elapsed time. */
  public void start() {
    running = true;
  }

  /** Stops the timer, pausing time accumulation without hiding the display. */
  public void stop() {
    running = false;
  }

  /** Resumes the timer from its paused state. */
  public void resume() {
    running = true;
  }

  /**
   * Resets the elapsed time to zero and clears all triggered callback states.
   *
   * <p>This allows callbacks to be triggered again if the timer is restarted.
   */
  public void reset() {
    elapsedTime = 0f;
    callbacks.clear();
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
   * Sets the display format for the timer text.
   *
   * <p>The format string should contain {@code %time} as a placeholder for the formatted time
   * (HH:MM:SS or MM:SS). For example: "Current time is: %time" will display as "Current time is:
   * 00:05".
   *
   * @param format the format string with {@code %time} placeholder
   * @return this TimerUI for method chaining
   */
  public TimerUI displayFormat(String format) {
    if (format != null && !format.isEmpty()) {
      this.displayFormat = format;
    }
    return this;
  }

  /**
   * Registers a one-time callback to be executed when the specified elapsed time is reached.
   *
   * <p>Multiple callbacks can be registered for the same elapsed time. Each callback is executed
   * only once and then removed from the callback list.
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

  /**
   * Formats the elapsed time into HH:MM:SS format (or MM:SS if hours is zero).
   *
   * <p>Applies the configured display format, replacing {@code %time} with the formatted time
   * string.
   *
   * @return the formatted time string with applied display format
   */
  private String formatTime() {
    long totalSeconds = (long) elapsedTime;
    long hours = totalSeconds / 3600;
    long minutes = (totalSeconds % 3600) / 60;
    long seconds = totalSeconds % 60;

    String timeString;
    if (hours > 0) {
      timeString = String.format("%02d:%02d:%02d", hours, minutes, seconds);
    } else {
      timeString = String.format("%02d:%02d", minutes, seconds);
    }

    // Apply the display format by replacing %time placeholder
    return displayFormat.replace("%time", timeString);
  }

  /** Disposes of resources used by this timer UI. */
  public void dispose() {
    // Font is managed by FontHelper, no disposal needed
  }
}
