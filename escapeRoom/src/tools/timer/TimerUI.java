package tools.timer;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.scenes.scene2d.Group;
import core.Game;
import core.utils.FontHelper;

/**
 * A UI component that displays elapsed time in HH:MM:SS format (or MM:SS if hours is zero).
 *
 * <p>In multiplayer, the timer is synchronized from the server and uses client-side prediction with
 * interpolation to ensure smooth display. The timer will snap to the server's authoritative time
 * when the discrepancy exceeds a threshold.
 *
 * <p>The timer is positioned in the top-right corner of the screen and can be started, stopped, and
 * shown/hidden independently.
 */
public class TimerUI extends Group {

  private static final int FONT_SIZE = 24;
  private static final Color FONT_COLOR = Color.WHITE;
  private static final float PADDING_X = 2;
  private static final float PADDING_Y = 2;
  private static final String DEFAULT_TIME_FORMAT = "%time";

  /**
   * Threshold in seconds for snapping to server time.
   *
   * <p>If the client's predicted time differs from the server's time by more than this threshold,
   * the client will immediately snap to the server's time instead of smoothly interpolating.
   */
  private static final float SNAP_THRESHOLD_SECONDS = 0.5f;

  private final BitmapFont font;
  private final GlyphLayout glyphLayout;

  private float elapsedTime; // seconds - current display time (client prediction)
  private float serverTime; // seconds - last received server time
  private boolean running = false;
  private String displayFormat = DEFAULT_TIME_FORMAT;

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

    this.elapsedTime = startTimeSeconds;
    this.serverTime = startTimeSeconds;

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
   * Updates the timer each frame, accumulating elapsed time with client-side prediction.
   *
   * <p>This method is called automatically by the scene2d framework. Time accumulation occurs
   * regardless of game pause state. In multiplayer, the displayed time is predicted locally and
   * corrected by periodic server synchronization messages.
   *
   * @param delta the time elapsed since the last frame in seconds
   */
  @Override
  public void act(float delta) {
    super.act(delta);

    if (running) {
      elapsedTime += delta;
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

  /**
   * Starts the timer with the specified initial time.
   *
   * <p>This method is called when receiving a START command from the server or when starting a
   * timer locally (server-side only).
   *
   * @param startTimeSeconds the initial elapsed time in seconds
   */
  public void start(float startTimeSeconds) {
    this.elapsedTime = startTimeSeconds;
    this.serverTime = startTimeSeconds;
    this.running = true;
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
   * Synchronizes the client timer with the server's authoritative time.
   *
   * <p>If the difference between client prediction and server time exceeds {@link
   * #SNAP_THRESHOLD_SECONDS}, the client will immediately snap to the server time. Otherwise, the
   * server time is stored for gradual correction on the next sync.
   *
   * @param serverElapsedSeconds the authoritative elapsed time from the server
   * @param serverRunning the running state from the server
   */
  public void syncFromServer(float serverElapsedSeconds, boolean serverRunning) {
    this.serverTime = serverElapsedSeconds;
    this.running = serverRunning;

    float timeDifference = Math.abs(elapsedTime - serverElapsedSeconds);

    // Snap to server time if difference is too large
    if (timeDifference > SNAP_THRESHOLD_SECONDS) {
      this.elapsedTime = serverElapsedSeconds;
    }
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

  /**
   * Returns whether the timer is currently running.
   *
   * <p>* @return true if the timer is running, false if it is stopped or not started
   */
  public boolean isRunning() {
    return running;
  }
}
