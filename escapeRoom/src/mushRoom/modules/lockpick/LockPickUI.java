package mushRoom.modules.lockpick;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Array;
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

/**
 * UI component for a lock-picking minigame with concentric rotating rings.
 *
 * <p>Players must align all ring notches to the target position (top) to unlock. Each ring can be
 * selected and rotated using keyboard controls.
 *
 * <p>Usage:
 *
 * <pre>{@code
 * LockPickUI lockPickUI = LockPickDialog.openLockPick(userEntity, difficulty, onSuccess, onFailure);
 * }</pre>
 *
 * @see LockPickDialog
 */
public class LockPickUI extends Group implements Disposable {

  private static final DungeonLogger LOGGER = DungeonLogger.getLogger(LockPickUI.class);

  /** Texture resolution multiplier for high-quality rendering. */
  private static final float TEXTURE_SCALE = 3f;

  /** Anti-aliasing edge width in scaled pixels. */
  private static final float AA_EDGE_WIDTH = 2f;

  /** Rotation speed in degrees per key press. */
  private static final float ROTATION_SPEED = 3.5f;

  /** Absolute minimum notch angle to ensure gap is beatable. */
  private static final float MIN_NOTCH_ANGLE = 0.5f;

  /** Padding from window edges for layout. */
  private static final float WINDOW_PADDING = 50f;

  /** Height reserved for title and instructions labels. */
  private static final float LABEL_AREA_HEIGHT = 150f;

  /** Predefined ring colors. */
  private static final Color[] RING_COLORS = {
    new Color(0.85f, 0.25f, 0.25f, 1f), // Red
    new Color(0.25f, 0.65f, 0.85f, 1f), // Blue
    new Color(0.25f, 0.85f, 0.35f, 1f), // Green
    new Color(0.95f, 0.75f, 0.15f, 1f), // Yellow
    new Color(0.85f, 0.45f, 0.95f, 1f), // Purple
    new Color(1f, 0.55f, 0.25f, 1f) // Orange
  };

  /** The amount of attempts to try to pick the lock. */
  private static final int ATTEMPTS = 3;

  private static final Color SELECTED_TINT = new Color(1f, 1f, 1f, 1f);
  private static final Color UNSELECTED_TINT = new Color(0.7f, 0.7f, 0.7f, 1f);
  private static final Color GREYSCALE_TINT = new Color(0.5f, 0.5f, 0.5f, 1f);
  private static final long DELAY_AFTER_END = 0; // milliseconds
  private static final long DELAY_BEFORE_CALLBACK = 500; // milliseconds

  // Font sizes
  private static final int TITLE_FONT_SIZE = 32;
  private static final int NORMAL_FONT_SIZE = 20;
  private static final int INSTRUCTION_FONT_SIZE = 18;

  // Layout paddings
  private static final float TITLE_TOP_PADDING = 20f;
  private static final float ATTEMPTS_LABEL_PADDING = 10f;
  private static final float INSTRUCTIONS_BOTTOM_PADDING = 30f;

  // Ring layout factors
  private static final float OUTER_RADIUS_MARGIN_FACTOR = 0.9f;
  private static final float RING_THICKNESS_FACTOR = 0.12f;
  private static final float RING_GAP_FACTOR = 0.015f;
  private static final float BASE_RADIUS_FACTOR = 0.15f;

  // Color generation
  private static final float GOLDEN_ANGLE = 137.5f;
  private static final float COLOR_SATURATION = 0.8f;
  private static final float COLOR_VALUE = 0.85f;

  // Target marker
  private static final float MARKER_Y_OFFSET = 15f;
  private static final float MIN_MARKER_SIZE = 12f;
  private static final float MARKER_SIZE_FACTOR = 0.4f;
  private static final float MARKER_HEIGHT_MULTIPLIER = 1.5f;

  // Rotation control
  private static final float SHIFT_SLOWDOWN_FACTOR = 3f;
  private static final float DELTA_TIME_MULTIPLIER = 60f;

  // UI Text constants
  private static final String TITLE_TEXT = "PICK THE LOCK";
  private static final String ATTEMPTS_FORMAT = "Attempts: %d/%d";
  private static final String INSTRUCTIONS_FULL_TEXT =
      "A/D to rotate (Hold SHIFT for slower) | SPACE to confirm ring | Click to select ";
  private static final String INSTRUCTIONS_SHORT_TEXT =
      "A/D to rotate | SPACE to confirm ring | Click to select";
  private static final String UNLOCKED_TEXT = "UNLOCKED!";
  private static final String LOCK_JAMMED_TEXT = "LOCK JAMMED!";
  private static final String WRONG_ALIGNMENT_TEXT =
      "Wrong alignment! Adjust the highlighted ring.";

  /** Static ShapeRenderer to avoid creating per frame. */
  private static ShapeRenderer shapeRenderer;

  private final LockPickDifficulty difficulty;
  private final Entity owner;
  private final Array<Float> notchAngles = new Array<>();
  private final Array<Image> ringImages = new Array<>();
  private final Array<Float> ringAngles = new Array<>();
  private final Array<Texture> ringTextures = new Array<>();

  /** Container for ring images to control z-order. */
  private final Group ringsContainer;

  /** Container for labels (above rings). */
  private final Table labelsTable;

  private int selectedRingIndex = 0;
  private int remainingAttempts = ATTEMPTS;
  private boolean isLocked = true;
  private boolean inputEnabled = true;
  private boolean showingFailureHighlight = false;

  private final Label titleLabel;
  private final Label attemptsLabel;
  private final Label instructionsLabel;

  // Calculated layout values
  private float baseRadius;
  private float ringThickness;
  private float ringGap;
  private float centerX;
  private float centerY;

  private Runnable onSuccess = () -> {};
  private Runnable onFailure = () -> {};

  /**
   * Constructs a new LockPickUI with the specified difficulty.
   *
   * @param difficulty The difficulty level determining ring count and notch width
   * @param owner The entity owning this UI
   */
  LockPickUI(LockPickDifficulty difficulty, Entity owner) {
    this.difficulty = difficulty;
    this.owner = owner;

    // Initialize static shape renderer if needed
    if (shapeRenderer == null) {
      shapeRenderer = new ShapeRenderer();
    }

    // Create rings container (added first, so it's below labels)
    ringsContainer = new Group();
    addActor(ringsContainer);

    // Create labels table (added second, so it's above rings)
    labelsTable = new Table();
    labelsTable.setFillParent(true);
    labelsTable.top();
    addActor(labelsTable);

    // Create label styles with properly sized fonts (not scaled)
    Label.LabelStyle titleStyle = new Label.LabelStyle();
    titleStyle.font = FontHelper.getDefaultFont(TITLE_FONT_SIZE);
    titleStyle.fontColor = Color.WHITE;

    Label.LabelStyle normalStyle = new Label.LabelStyle();
    normalStyle.font = FontHelper.getDefaultFont(NORMAL_FONT_SIZE);
    normalStyle.fontColor = Color.WHITE;

    Label.LabelStyle instructionStyle = new Label.LabelStyle();
    instructionStyle.font = FontHelper.getDefaultFont(INSTRUCTION_FONT_SIZE);
    instructionStyle.fontColor = Color.WHITE;

    // Title
    titleLabel = new Label(TITLE_TEXT, titleStyle);
    titleLabel.setAlignment(Align.center);
    labelsTable.add(titleLabel).padTop(TITLE_TOP_PADDING).expandX().row();

    // Attempts label
    attemptsLabel =
        new Label(String.format(ATTEMPTS_FORMAT, remainingAttempts, ATTEMPTS), normalStyle);
    attemptsLabel.setAlignment(Align.center);
    labelsTable.add(attemptsLabel).padTop(ATTEMPTS_LABEL_PADDING).expandX().row();

    // Spacer to push instructions to bottom
    labelsTable.add().expand().row();

    // Instructions at bottom
    instructionsLabel = new Label(INSTRUCTIONS_FULL_TEXT, instructionStyle);
    instructionsLabel.setAlignment(Align.center);
    labelsTable.add(instructionsLabel).padBottom(INSTRUCTIONS_BOTTOM_PADDING).expandX();

    // Set initial size and create rings
    this.setSize(Game.windowWidth(), Game.windowHeight());
    WindowEventManager.registerWindowRefreshListener(this::handleResize);

    // Add click listener for ring selection
    addListener(
        new ClickListener() {
          @Override
          public void clicked(InputEvent event, float x, float y) {
            if (!inputEnabled) return;
            selectRingAtPosition(x, y);
          }
        });

    setTouchable(com.badlogic.gdx.scenes.scene2d.Touchable.enabled);
  }

  private void handleResize() {
    setSize(Game.windowWidth(), Game.windowHeight());
  }

  @Override
  public void setSize(float width, float height) {
    super.setSize(width, height);
    calculateLayout();
    recreateRings();
  }

  /** Calculates layout dimensions based on current window size. */
  private void calculateLayout() {
    float width = getWidth();
    float height = getHeight();

    // Available space for rings (accounting for padding and labels)
    float availableWidth = width - (WINDOW_PADDING * 2);
    float availableHeight = height - (WINDOW_PADDING * 2) - LABEL_AREA_HEIGHT;
    float availableSize = Math.min(availableWidth, availableHeight);

    // Calculate ring dimensions based on available space and ring count
    int ringCount = difficulty.ringCount();

    // Total radius needed = baseRadius + ringCount * (thickness + gap)
    // We want the outermost ring to fit within availableSize / 2
    float maxOuterRadius = availableSize / 2f;

    // Calculate dimensions to fit all rings
    // Leave some margin (10%) for the target marker
    maxOuterRadius *= OUTER_RADIUS_MARGIN_FACTOR;

    // ringThickness + ringGap takes up space, baseRadius is the innermost
    // outerRadius = baseRadius + (ringCount) * (thickness + gap) - gap + thickness
    // Simplified: we need to distribute maxOuterRadius among base + rings
    float totalRingSpace = maxOuterRadius;
    ringThickness =
        totalRingSpace * RING_THICKNESS_FACTOR; // 12% of available for each ring thickness
    ringGap = totalRingSpace * RING_GAP_FACTOR; // 1.5% gap between rings
    baseRadius = totalRingSpace * BASE_RADIUS_FACTOR; // 15% for innermost radius

    // Recalculate to ensure all rings fit
    float neededRadius = baseRadius + ringCount * (ringThickness + ringGap);
    if (neededRadius > maxOuterRadius) {
      float scale = maxOuterRadius / neededRadius;
      baseRadius *= scale;
      ringThickness *= scale;
      ringGap *= scale;
    }

    // Center position
    centerX = width / 2f;
    centerY = height / 2f;
  }

  /** Recreates all ring textures and images based on current layout. */
  private void recreateRings() {
    // Dispose old textures
    for (Texture texture : ringTextures) {
      texture.dispose();
    }
    ringTextures.clear();

    // Clear old images from container
    ringsContainer.clearChildren();
    ringImages.clear();

    // Clear angles only if this is the first creation
    boolean firstCreation = ringAngles.size == 0;
    if (firstCreation) {
      notchAngles.clear();
    }

    int ringCount = difficulty.ringCount();
    float minNotch = difficulty.minNotchWidthDegrees();
    float maxNotch = difficulty.maxNotchWidthDegrees();

    for (int i = 0; i < ringCount; i++) {
      // Randomize starting angle only on first creation
      float startAngle;
      float notchWidth;

      if (firstCreation) {
        startAngle = (float) (Math.random() * 360);
        ringAngles.add(startAngle);

        float randomNotch = minNotch + (float) (Math.random() * (maxNotch - minNotch));
        notchWidth = Math.max(MIN_NOTCH_ANGLE, randomNotch);
        notchAngles.add(notchWidth);
      } else {
        startAngle = ringAngles.get(i);
        notchWidth = notchAngles.get(i);
      }

      // Calculate ring radii
      float innerRadius = baseRadius + i * (ringThickness + ringGap);
      float outerRadius = innerRadius + ringThickness;

      // Get color for this ring
      Color ringColor = getRingColor(i);

      // Create high-resolution ring texture
      Texture ringTexture = createRingTexture(outerRadius, innerRadius, ringColor, notchWidth);
      ringTextures.add(ringTexture);

      // Create image actor with proper scaling
      Image ringImage = new Image(ringTexture);

      // Scale down from high-res texture
      float displaySize = (outerRadius * 2) + 4;
      ringImage.setSize(displaySize, displaySize);

      // Set origin to center for proper rotation
      ringImage.setOrigin(displaySize / 2f, displaySize / 2f);
      ringImage.setRotation(startAngle);

      ringImages.add(ringImage);
      ringsContainer.addActor(ringImage);

      LOGGER.trace(
          "Ring {} created: notchWidth={}°, startAngle={}°, radius={}",
          i,
          notchWidth,
          startAngle,
          outerRadius);
    }

    updateRingPositions();
    updateRingSelection();
  }

  /**
   * Gets the color for a ring at the given index.
   *
   * @param index The ring index
   * @return The color for this ring
   */
  private Color getRingColor(int index) {
    if (index < RING_COLORS.length) {
      return RING_COLORS[index];
    }
    // Generate color from HSV wheel using golden angle for good distribution
    float hue = (index * GOLDEN_ANGLE) % 360f;
    Color color = new Color();
    // Convert HSV to RGB
    float h = hue / 60f;
    float c = COLOR_SATURATION; // saturation * value
    float x = c * (1 - Math.abs(h % 2 - 1));
    float m = COLOR_VALUE - c; // value - c

    float r, g, b;
    if (h < 1) {
      r = c;
      g = x;
      b = 0;
    } else if (h < 2) {
      r = x;
      g = c;
      b = 0;
    } else if (h < 3) {
      r = 0;
      g = c;
      b = x;
    } else if (h < 4) {
      r = 0;
      g = x;
      b = c;
    } else if (h < 5) {
      r = x;
      g = 0;
      b = c;
    } else {
      r = c;
      g = 0;
      b = x;
    }

    color.set(r + m, g + m, b + m, 1f);
    return color;
  }

  private Texture createRingTexture(
      float outerRadius, float innerRadius, Color color, float notchAngle) {
    // Create texture at higher resolution
    int scaledSize = (int) ((outerRadius * 2 + 4) * TEXTURE_SCALE);
    Pixmap pixmap = new Pixmap(scaledSize, scaledSize, Pixmap.Format.RGBA8888);

    float scaledOuterRadius = outerRadius * TEXTURE_SCALE;
    float scaledInnerRadius = innerRadius * TEXTURE_SCALE;
    float centerX = scaledSize / 2f;
    float centerY = scaledSize / 2f;

    // Draw the ring with anti-aliasing
    for (int x = 0; x < scaledSize; x++) {
      for (int y = 0; y < scaledSize; y++) {
        float dx = x - centerX;
        float dy = y - centerY;
        float distance = (float) Math.sqrt(dx * dx + dy * dy);

        // Check if near ring bounds (with AA margin)
        float distToOuter = scaledOuterRadius - distance;
        float distToInner = distance - scaledInnerRadius;

        if (distToOuter >= -AA_EDGE_WIDTH && distToInner >= -AA_EDGE_WIDTH) {
          // Calculate angle (0° is at bottom of pixmap, which shows as top when rendered)
          float angle = (float) Math.toDegrees(Math.atan2(dx, dy));
          if (angle < 0) angle += 360;

          // Check if in notch
          float halfNotch = notchAngle / 2;
          boolean inNotch = angle < halfNotch || angle > 360 - halfNotch;

          if (!inNotch) {
            // Calculate alpha for anti-aliasing
            float alpha = 1f;

            // Smooth outer edge
            if (distToOuter < AA_EDGE_WIDTH && distToOuter >= 0) {
              alpha = Math.min(alpha, distToOuter / AA_EDGE_WIDTH);
            } else if (distToOuter < 0) {
              continue; // Outside ring
            }

            // Smooth inner edge
            if (distToInner < AA_EDGE_WIDTH && distToInner >= 0) {
              alpha = Math.min(alpha, distToInner / AA_EDGE_WIDTH);
            } else if (distToInner < 0) {
              continue; // Inside hole
            }

            // Smooth notch edges
            float angleToNotchEdge =
                Math.min(Math.abs(angle - halfNotch), Math.abs(angle - (360 - halfNotch)));
            if (angle > 180) {
              angleToNotchEdge = Math.min(angleToNotchEdge, Math.abs(angle - (360 - halfNotch)));
            } else {
              angleToNotchEdge = Math.min(angleToNotchEdge, Math.abs(angle - halfNotch));
            }

            // Convert angle distance to approximate pixel distance at this radius
            float arcLength = (float) Math.toRadians(angleToNotchEdge) * distance;
            if (arcLength < AA_EDGE_WIDTH) {
              alpha = Math.min(alpha, arcLength / AA_EDGE_WIDTH);
            }

            // Apply color with alpha
            if (alpha > 0.01f) {
              pixmap.setColor(new Color(color.r, color.g, color.b, alpha));
              pixmap.drawPixel(x, y);
            }
          }
        }
      }
    }

    // Create texture with linear filtering for smooth scaling
    Texture texture = new Texture(pixmap);
    texture.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
    pixmap.dispose();

    return texture;
  }

  private void updateRingPositions() {
    for (int i = 0; i < ringImages.size; i++) {
      Image ring = ringImages.get(i);
      float ringX = centerX - ring.getWidth() / 2f;
      float ringY = centerY - ring.getHeight() / 2f;
      ring.setPosition(ringX, ringY);
    }
  }

  private void rotateSelectedRing(float degrees) {
    if (selectedRingIndex >= 0 && selectedRingIndex < ringAngles.size) {
      // Clear failure highlight on first rotation input
      if (showingFailureHighlight) {
        showingFailureHighlight = false;
        updateRingSelection();
        instructionsLabel.setText(INSTRUCTIONS_SHORT_TEXT);
        instructionsLabel.setColor(Color.WHITE);
      }

      float newAngle = (ringAngles.get(selectedRingIndex) + degrees) % 360;
      if (newAngle < 0) newAngle += 360;
      ringAngles.set(selectedRingIndex, newAngle);
      ringImages.get(selectedRingIndex).setRotation(newAngle);
    }
  }

  private void submitCurrentRing() {
    if (selectedRingIndex >= ringImages.size - 1) {
      checkAlignment();
    } else {
      selectedRingIndex++;
      updateRingSelection();
    }
  }

  private void selectRingAtPosition(float x, float y) {
    float dx = x - centerX;
    float dy = y - centerY;
    float distance = (float) Math.sqrt(dx * dx + dy * dy);

    // Find which ring was clicked (check from outermost to innermost)
    for (int i = ringImages.size - 1; i >= 0; i--) {
      float innerRadius = baseRadius + i * (ringThickness + ringGap);
      float outerRadius = innerRadius + ringThickness;

      if (distance >= innerRadius && distance <= outerRadius) {
        selectedRingIndex = i;
        updateRingSelection();
        return;
      }
    }
  }

  private void updateRingSelection() {
    for (int i = 0; i < ringImages.size; i++) {
      Image ring = ringImages.get(i);
      if (i == selectedRingIndex) {
        ring.setColor(SELECTED_TINT);
      } else {
        ring.setColor(UNSELECTED_TINT);
      }
    }
  }

  private void checkAlignment() {
    boolean allAligned = true;
    int innermostMisalignedIndex = -1;

    for (int i = 0; i < ringAngles.size; i++) {
      float angle = ringAngles.get(i) % 360;
      if (angle < 0) angle += 360;

      float ringNotchWidth = notchAngles.get(i);
      float tolerance = ringNotchWidth / 2f;

      float distanceFrom180 = Math.abs(angle - 180f);
      boolean aligned = distanceFrom180 <= tolerance;

      LOGGER.trace(
          "Ring {}: angle={}°, distanceFrom180={}°, notchWidth={}°, tolerance={}°, aligned={}",
          i,
          angle,
          distanceFrom180,
          ringNotchWidth,
          tolerance,
          aligned);

      if (!aligned) {
        allAligned = false;
        if (innermostMisalignedIndex == -1) {
          innermostMisalignedIndex = i;
        }
      }
    }

    LOGGER.trace("Alignment check result: allAligned={}", allAligned);

    inputEnabled = false;

    if (allAligned) {
      showSuccessFeedback();
    } else {
      remainingAttempts--;
      attemptsLabel.setText(String.format(ATTEMPTS_FORMAT, remainingAttempts, ATTEMPTS));
      showFailureFeedback(remainingAttempts <= 0, innermostMisalignedIndex);
    }
  }

  private void showSuccessFeedback() {
    for (Image ring : ringImages) {
      ring.setColor(Color.GREEN);
    }
    instructionsLabel.setText(UNLOCKED_TEXT);
    instructionsLabel.setColor(Color.GREEN);

    isLocked = false;
    EventScheduler.scheduleAction(
        () -> {
          triggerSuccess();
          EventScheduler.scheduleAction(
              () -> owner.fetch(UIComponent.class).ifPresent(UIUtils::closeDialog),
              DELAY_AFTER_END);
        },
        DELAY_BEFORE_CALLBACK);
  }

  private void showFailureFeedback(boolean finalFailure, int innermostMisalignedIndex) {
    showingFailureHighlight = true;

    for (int i = 0; i < ringImages.size; i++) {
      Image ring = ringImages.get(i);
      if (i == innermostMisalignedIndex) {
        ring.setColor(SELECTED_TINT);
      } else {
        ring.setColor(GREYSCALE_TINT);
      }
    }

    if (finalFailure) {
      instructionsLabel.setText(LOCK_JAMMED_TEXT);
      instructionsLabel.setColor(Color.RED);

      isLocked = true;
      EventScheduler.scheduleAction(
          () -> {
            // LockPickDialog triggers failure callback by closing
            EventScheduler.scheduleAction(
                () -> owner.fetch(UIComponent.class).ifPresent(UIUtils::closeDialog),
                DELAY_AFTER_END);
          },
          DELAY_BEFORE_CALLBACK);
    } else {
      instructionsLabel.setText(WRONG_ALIGNMENT_TEXT);
      instructionsLabel.setColor(Color.RED);
      selectedRingIndex = innermostMisalignedIndex;
      inputEnabled = true;
    }
  }

  /**
   * Sets the callback to be executed upon successful lock picking.
   *
   * @param onSuccess The callback to execute on success
   */
  public void onSuccess(Runnable onSuccess) {
    Objects.requireNonNull(onSuccess, "onSuccess callback cannot be null");
    this.onSuccess = onSuccess;
  }

  /**
   * Sets the callback to be executed upon failure to pick the lock.
   *
   * @param onFailure The callback to execute on failure
   */
  public void onFailure(Runnable onFailure) {
    Objects.requireNonNull(onFailure, "onFailure callback cannot be null");
    this.onFailure = onFailure;
  }

  /** Triggers the failure callback if the lock is still locked. */
  public void triggerFailure() {
    if (isLocked) {
      onFailure.run();
    }
  }

  /** Triggers the success callback if the lock has been picked. */
  public void triggerSuccess() {
    if (!isLocked) {
      onSuccess.run();
    }
  }

  /**
   * Checks if the lock is still locked.
   *
   * @return true if the lock is locked, false if it has been picked
   */
  public boolean isLocked() {
    return isLocked;
  }

  @Override
  public void draw(Batch batch, float parentAlpha) {
    super.draw(batch, parentAlpha);

    // Draw target marker
    batch.end();

    shapeRenderer.setProjectionMatrix(batch.getProjectionMatrix());
    shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
    shapeRenderer.setColor(Color.WHITE);

    // Calculate marker position (above the outermost ring)
    float outerMostRadius = baseRadius + difficulty.ringCount() * (ringThickness + ringGap);
    float markerY = centerY + outerMostRadius + MARKER_Y_OFFSET;
    float markerSize = Math.max(MIN_MARKER_SIZE, ringThickness * MARKER_SIZE_FACTOR);

    // Draw triangle pointing down
    shapeRenderer.triangle(
        centerX - markerSize,
        markerY + markerSize * MARKER_HEIGHT_MULTIPLIER,
        centerX + markerSize,
        markerY + markerSize * MARKER_HEIGHT_MULTIPLIER,
        centerX,
        markerY);

    shapeRenderer.end();
    batch.begin();
  }

  @Override
  public void act(float delta) {
    super.act(delta);

    if (!inputEnabled) return;

    float curSpeed = ROTATION_SPEED;
    if (Gdx.input.isKeyPressed(Input.Keys.SHIFT_LEFT)) {
      curSpeed /= SHIFT_SLOWDOWN_FACTOR;
    }

    if (Gdx.input.isKeyPressed(Input.Keys.A) || Gdx.input.isKeyPressed(Input.Keys.LEFT)) {
      rotateSelectedRing(curSpeed * delta * DELTA_TIME_MULTIPLIER);
    }
    if (Gdx.input.isKeyPressed(Input.Keys.D) || Gdx.input.isKeyPressed(Input.Keys.RIGHT)) {
      rotateSelectedRing(-curSpeed * delta * DELTA_TIME_MULTIPLIER);
    }

    if (Gdx.input.isKeyJustPressed(Input.Keys.SPACE)) {
      LOGGER.trace("SPACE pressed - submitting ring {}", selectedRingIndex);
      submitCurrentRing();
    }
    if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER)) {
      LOGGER.trace("ENTER pressed - checking alignment");
      checkAlignment();
    }
  }

  /**
   * Disposes of all textures created by this UI.
   *
   * <p>Must be called when the UI is no longer needed to free GPU resources.
   */
  @Override
  public void dispose() {
    for (Texture texture : ringTextures) {
      texture.dispose();
    }
  }
}
