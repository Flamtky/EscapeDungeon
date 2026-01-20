package mushRoom.modules.qte;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Disposable;
import contrib.components.UIComponent;
import contrib.hud.UIUtils;
import contrib.systems.EventScheduler;
import core.Entity;
import core.Game;
import core.game.WindowEventManager;
import core.utils.FontHelper;
import core.utils.logging.DungeonLogger;
import java.util.Objects;
import java.util.Random;

/**
 * UI component for a Following Indicator QTE minigame.
 *
 * <p>Players must press the correct movement key (W/A/S/D) when the rotating indicator enters the
 * active zone. Upon any key press, the active zone jumps to a new random position and a new random
 * key is selected.
 */
public class FollowingIndicatorUI extends Group implements Disposable {

  private static final DungeonLogger LOGGER = DungeonLogger.getLogger(FollowingIndicatorUI.class);

  /** Radius of the circular track. */
  private static final float TRACK_RADIUS = 180f;

  /** Width of each zone arc in degrees. */
  private static final float ZONE_ARC_WIDTH = 40f;

  /** Indicator line length from center. */
  private static final float INDICATOR_LENGTH = 190f;

  /** Indicator line thickness. */
  private static final float INDICATOR_THICKNESS = 6f;

  /** Active zone color (green). */
  private static final Color ZONE_ACTIVE_COLOR = new Color(0.25f, 0.85f, 0.35f, 0.9f);

  /** Inactive zone color (gray). */
  private static final Color ZONE_INACTIVE_COLOR = new Color(0.4f, 0.4f, 0.4f, 0.5f);

  /** Track background color. */
  private static final Color TRACK_BG_COLOR = new Color(0.15f, 0.15f, 0.15f, 0.9f);

  /** Indicator color. */
  private static final Color INDICATOR_COLOR = new Color(1f, 1f, 1f, 1f);

  /** Delay after success/failure before closing. */
  private static final long DELAY_AFTER_END = 2000;

  /** Delay before triggering success/failure callback. */
  private static final long DELAY_BEFORE_CALLBACK = 500;

  /** Keys that can be pressed (W, A, S, D). */
  private static final int[] VALID_KEYS = {Input.Keys.W, Input.Keys.A, Input.Keys.S, Input.Keys.D};

  private static final String[] KEY_NAMES = {"W", "A", "S", "D"};

  // Font sizes
  private static final int KEY_FONT_SIZE = 64;
  private static final int TITLE_FONT_SIZE = 28;
  private static final int STATUS_FONT_SIZE = 18;
  private static final int FEEDBACK_FONT_SIZE = 22;

  // Layout constants
  private static final float TITLE_TOP_PADDING = 30f;
  private static final float LABEL_TOP_PADDING = 10f;
  private static final float CENTER_Y_OFFSET = 30f;
  private static final float CENTER_CIRCLE_RADIUS = 50f;
  private static final int TRACK_CIRCLE_SEGMENTS = 64;
  private static final int CENTER_CIRCLE_SEGMENTS = 32;
  private static final int ZONE_ARC_SEGMENTS = 20;
  private static final float ZONE_INNER_RADIUS_OFFSET = 25f;
  private static final float ZONE_OUTER_RADIUS_OFFSET = 15f;

  /** Center circle color. */
  private static final Color CENTER_CIRCLE_COLOR = new Color(0.2f, 0.2f, 0.2f, 1f);

  // UI Text constants
  private static final String TITLE_TEXT = "EVADE THE GUARD";
  private static final String SUCCESS_TITLE_TEXT = "ESCAPED!";
  private static final String FAILURE_TITLE_TEXT = "CAUGHT!";
  private static final String HIT_FEEDBACK_TEXT = "HIT!";
  private static final String WRONG_KEY_AND_ZONE_TEXT = "WRONG KEY & ZONE!";
  private static final String WRONG_KEY_TEXT = "WRONG KEY!";
  private static final String WRONG_ZONE_TEXT = "WRONG ZONE!";
  private static final String STATUS_HITS_PREFIX = "Hits: ";
  private static final String STATUS_SEPARATOR = "  |  Misses: ";

  private static ShapeRenderer shapeRenderer;

  private final FollowingIndicatorDifficulty difficulty;
  private final Entity owner;
  private final Random random = new Random();

  // Game state
  private float indicatorAngle = 0f;
  private int activeZoneIndex = 0;
  private int currentKeyIndex = 0;
  private int successCount = 0;
  private int failCount = 0;
  private boolean gameActive = true;
  private boolean inputEnabled = true;
  private boolean isSuccess = false;
  private boolean isFailed = false;

  // Zone angles (evenly distributed)
  private float[] zoneStartAngles;
  private float[] zoneEndAngles;

  // Layout
  private float centerX;
  private float centerY;

  // Labels
  private final Label titleLabel;
  private final Label statusLabel;
  private final Label feedbackLabel;

  // Font for center key display
  private final BitmapFont keyFont;
  private final GlyphLayout glyphLayout;

  private Runnable onSuccess = () -> {};
  private Runnable onFailure = () -> {};

  /**
   * Constructs a new FollowingIndicatorUI.
   *
   * @param difficulty The difficulty level
   * @param owner The entity owning this UI
   */
  FollowingIndicatorUI(FollowingIndicatorDifficulty difficulty, Entity owner) {
    this.difficulty = difficulty;
    this.owner = owner;

    if (shapeRenderer == null) {
      shapeRenderer = new ShapeRenderer();
    }

    // Initialize fonts
    keyFont = FontHelper.getDefaultFont(KEY_FONT_SIZE);
    glyphLayout = new GlyphLayout();

    // Create labels table
    Table labelsTable = new Table();
    labelsTable.setFillParent(true);
    labelsTable.top();
    addActor(labelsTable);

    // Label styles
    Label.LabelStyle titleStyle = new Label.LabelStyle();
    titleStyle.font = FontHelper.getDefaultFont(TITLE_FONT_SIZE);
    titleStyle.fontColor = Color.WHITE;

    Label.LabelStyle statusStyle = new Label.LabelStyle();
    statusStyle.font = FontHelper.getDefaultFont(STATUS_FONT_SIZE);
    statusStyle.fontColor = Color.WHITE;

    Label.LabelStyle feedbackStyle = new Label.LabelStyle();
    feedbackStyle.font = FontHelper.getDefaultFont(FEEDBACK_FONT_SIZE);
    feedbackStyle.fontColor = Color.WHITE;

    // Title
    titleLabel = new Label(TITLE_TEXT, titleStyle);
    titleLabel.setAlignment(Align.center);
    labelsTable.add(titleLabel).padTop(TITLE_TOP_PADDING).expandX().row();

    // Status (successes / attempts)
    statusLabel =
        new Label(
            STATUS_HITS_PREFIX
                + "0/"
                + difficulty.requiredSuccesses()
                + STATUS_SEPARATOR
                + "0/"
                + difficulty.maxAttempts(),
            statusStyle);
    statusLabel.setAlignment(Align.center);
    labelsTable.add(statusLabel).padTop(LABEL_TOP_PADDING).expandX().row();

    // Feedback (HIT/MISS)
    feedbackLabel = new Label("", feedbackStyle);
    feedbackLabel.setAlignment(Align.center);
    labelsTable.add(feedbackLabel).padTop(LABEL_TOP_PADDING).expandX().row();

    // Set size and initialize
    setSize(Game.windowWidth(), Game.windowHeight());
    calculateLayout();
    generateZones();
    selectRandomActiveZone();
    selectRandomKey();

    WindowEventManager.registerWindowRefreshListener(this::handleResize);

    LOGGER.info(
        "FollowingIndicatorUI initialized: speed={}, zones={}, required={}, attempts={}",
        difficulty.indicatorSpeed(),
        difficulty.zoneCount(),
        difficulty.requiredSuccesses(),
        difficulty.maxAttempts());
  }

  private void handleResize() {
    setSize(Game.windowWidth(), Game.windowHeight());
  }

  @Override
  public void setSize(float width, float height) {
    super.setSize(width, height);
    calculateLayout();
  }

  private void calculateLayout() {
    centerX = getWidth() / 2f;
    centerY = getHeight() / 2f - CENTER_Y_OFFSET; // Offset down slightly to account for labels
  }

  private void generateZones() {
    int count = difficulty.zoneCount();
    zoneStartAngles = new float[count];
    zoneEndAngles = new float[count];

    float angleStep = 360f / count;
    for (int i = 0; i < count; i++) {
      float centerAngle = i * angleStep;
      zoneStartAngles[i] = normalizeAngle(centerAngle - ZONE_ARC_WIDTH / 2f);
      zoneEndAngles[i] = normalizeAngle(centerAngle + ZONE_ARC_WIDTH / 2f);
    }
  }

  private void selectRandomActiveZone() {
    int newZone;
    do {
      newZone = random.nextInt(difficulty.zoneCount());
    } while (newZone == activeZoneIndex && difficulty.zoneCount() > 1);
    activeZoneIndex = newZone;
  }

  private void selectRandomKey() {
    currentKeyIndex = random.nextInt(VALID_KEYS.length);
  }

  private float normalizeAngle(float angle) {
    while (angle < 0) angle += 360f;
    while (angle >= 360f) angle -= 360f;
    return angle;
  }

  /**
   * Sets the callback to execute on success.
   *
   * @param onSuccess The success callback
   */
  public void onSuccess(Runnable onSuccess) {
    this.onSuccess = Objects.requireNonNull(onSuccess);
  }

  /**
   * Sets the callback to execute on failure.
   *
   * @param onFailure The failure callback
   */
  public void onFailure(Runnable onFailure) {
    this.onFailure = Objects.requireNonNull(onFailure);
  }

  /** Triggers the failure callback if the game has not yet succeeded or failed. */
  public void triggerFailure() {
    if (!isSuccess && !isFailed) {
      isFailed = true;
      onFailure.run();
    }
  }

  /**
   * Returns true if the game ended in failure.
   *
   * @return true if failed
   */
  public boolean isFailed() {
    return isFailed;
  }

  /**
   * Returns true if the game ended in success.
   *
   * @return true if succeeded
   */
  public boolean isSuccess() {
    return isSuccess;
  }

  @Override
  public void act(float delta) {
    super.act(delta);

    if (!gameActive) return;

    // Update indicator rotation
    indicatorAngle += difficulty.indicatorSpeed() * delta;
    indicatorAngle = normalizeAngle(indicatorAngle);

    if (!inputEnabled) return;

    // Check for key presses
    for (int i = 0; i < VALID_KEYS.length; i++) {
      if (Gdx.input.isKeyJustPressed(VALID_KEYS[i])) {
        handleKeyPress(i);
        break;
      }
    }
  }

  private void handleKeyPress(int pressedKeyIndex) {
    boolean correctKey = pressedKeyIndex == currentKeyIndex;
    boolean inActiveZone = isIndicatorInZone(activeZoneIndex);

    if (correctKey && inActiveZone) {
      // Success
      successCount++;
      feedbackLabel.setText(HIT_FEEDBACK_TEXT);
      feedbackLabel.setColor(Color.GREEN);
      updateStatusLabel();

      if (successCount >= difficulty.requiredSuccesses()) {
        gameActive = false;
        isSuccess = true;
        titleLabel.setText(SUCCESS_TITLE_TEXT);
        inputEnabled = false;
        EventScheduler.scheduleAction(
            () -> {
              onSuccess.run();
              EventScheduler.scheduleAction(
                  () -> owner.fetch(UIComponent.class).ifPresent(UIUtils::closeDialog),
                  DELAY_AFTER_END);
            },
            DELAY_BEFORE_CALLBACK);
        return;
      }
    } else {
      // Failure
      failCount++;
      if (!correctKey && !inActiveZone) {
        feedbackLabel.setText(WRONG_KEY_AND_ZONE_TEXT);
      } else if (!correctKey) {
        feedbackLabel.setText(WRONG_KEY_TEXT);
      } else {
        feedbackLabel.setText(WRONG_ZONE_TEXT);
      }
      feedbackLabel.setColor(Color.RED);
      updateStatusLabel();

      if (failCount >= difficulty.maxAttempts()) {
        gameActive = false;
        isFailed = true;
        titleLabel.setText(FAILURE_TITLE_TEXT);
        inputEnabled = false;
        EventScheduler.scheduleAction(
            () -> {
              onFailure.run();
              EventScheduler.scheduleAction(
                  () -> owner.fetch(UIComponent.class).ifPresent(UIUtils::closeDialog),
                  DELAY_AFTER_END);
            },
            DELAY_BEFORE_CALLBACK);
        return;
      }
    }

    // Move to next round: new zone, new key
    selectRandomActiveZone();
    selectRandomKey();
  }

  private void updateStatusLabel() {
    statusLabel.setText(
        STATUS_HITS_PREFIX
            + successCount
            + "/"
            + difficulty.requiredSuccesses()
            + STATUS_SEPARATOR
            + failCount
            + "/"
            + difficulty.maxAttempts());
  }

  private boolean isIndicatorInZone(int zoneIndex) {
    float start = zoneStartAngles[zoneIndex];
    float end = zoneEndAngles[zoneIndex];
    float angle = indicatorAngle;

    if (start < end) {
      return angle >= start && angle <= end;
    } else {
      // Wraps around 360
      return angle >= start || angle <= end;
    }
  }

  @Override
  public void draw(Batch batch, float parentAlpha) {
    super.draw(batch, parentAlpha);

    batch.end();

    shapeRenderer.setProjectionMatrix(batch.getProjectionMatrix());

    // Draw filled shapes first
    shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);

    // Draw track background
    shapeRenderer.setColor(TRACK_BG_COLOR);
    shapeRenderer.circle(centerX, centerY, TRACK_RADIUS, TRACK_CIRCLE_SEGMENTS);

    // Draw zones as filled arcs
    for (int i = 0; i < difficulty.zoneCount(); i++) {
      boolean isActive = i == activeZoneIndex;
      drawFilledZone(i, isActive);
    }

    // Draw indicator line BEFORE center circle so it appears behind
    shapeRenderer.setColor(INDICATOR_COLOR);
    drawIndicatorLine();

    // Draw center circle ON TOP of the indicator line
    shapeRenderer.setColor(CENTER_CIRCLE_COLOR);
    shapeRenderer.circle(centerX, centerY, CENTER_CIRCLE_RADIUS, CENTER_CIRCLE_SEGMENTS);

    shapeRenderer.end();

    // Draw key letter in center
    batch.begin();
    String keyText = KEY_NAMES[currentKeyIndex];
    glyphLayout.setText(keyFont, keyText);
    float textX = centerX - glyphLayout.width / 2f;
    float textY = centerY + glyphLayout.height / 2f;
    keyFont.setColor(Color.WHITE);
    keyFont.draw(batch, keyText, textX, textY);
  }

  private void drawFilledZone(int zoneIndex, boolean isActive) {
    float startAngle = zoneStartAngles[zoneIndex];
    float endAngle = zoneEndAngles[zoneIndex];

    if (isActive) {
      shapeRenderer.setColor(ZONE_ACTIVE_COLOR);
    } else {
      shapeRenderer.setColor(ZONE_INACTIVE_COLOR);
    }

    // Draw arc as triangles from center
    int segments = ZONE_ARC_SEGMENTS;
    float angleSpan = endAngle - startAngle;
    if (angleSpan < 0) angleSpan += 360f;
    float angleStep = angleSpan / segments;

    float innerRadius = TRACK_RADIUS - ZONE_INNER_RADIUS_OFFSET;
    float outerRadius = TRACK_RADIUS + ZONE_OUTER_RADIUS_OFFSET;

    for (int i = 0; i < segments; i++) {
      float a1 = startAngle + angleStep * i;
      float a2 = startAngle + angleStep * (i + 1);

      float cos1 = (float) Math.cos(Math.toRadians(a1 - 90));
      float sin1 = (float) Math.sin(Math.toRadians(a1 - 90));
      float cos2 = (float) Math.cos(Math.toRadians(a2 - 90));
      float sin2 = (float) Math.sin(Math.toRadians(a2 - 90));

      float x1Inner = centerX + cos1 * innerRadius;
      float y1Inner = centerY + sin1 * innerRadius;
      float x1Outer = centerX + cos1 * outerRadius;
      float y1Outer = centerY + sin1 * outerRadius;
      float x2Inner = centerX + cos2 * innerRadius;
      float y2Inner = centerY + sin2 * innerRadius;
      float x2Outer = centerX + cos2 * outerRadius;
      float y2Outer = centerY + sin2 * outerRadius;

      // Two triangles to form a quad
      shapeRenderer.triangle(x1Inner, y1Inner, x1Outer, y1Outer, x2Outer, y2Outer);
      shapeRenderer.triangle(x1Inner, y1Inner, x2Outer, y2Outer, x2Inner, y2Inner);
    }
  }

  private void drawIndicatorLine() {
    float angleRad = (float) Math.toRadians(indicatorAngle - 90);
    float endX = centerX + (float) Math.cos(angleRad) * INDICATOR_LENGTH;
    float endY = centerY + (float) Math.sin(angleRad) * INDICATOR_LENGTH;

    // Draw as thick line using rectLine
    shapeRenderer.rectLine(centerX, centerY, endX, endY, INDICATOR_THICKNESS);
  }

  /** Disposes of resources. */
  @Override
  public void dispose() {
    System.out.println("Disposing FollowingIndicatorUI");
    // Fonts are managed by FontHelper, no need to dispose
  }
}
