package tools.skillhud;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.scenes.scene2d.Group;
import contrib.components.SkillComponent;
import contrib.components.SkillData;
import core.Entity;
import core.Game;
import core.utils.FontHelper;
import core.utils.logging.DungeonLogger;
import java.util.Optional;

/**
 * A UI component that displays the currently selected skill and its cooldown status.
 *
 * <p>The skill HUD is positioned in the bottom-right corner of the screen and shows:
 *
 * <ul>
 *   <li>A styled rectangular background with the skill name
 *   <li>A radial cooldown overlay showing progress
 *   <li>"READY" text (green) or remaining seconds countdown (orange/red)
 *   <li>Skill slot indicator "(1/N)" if multiple skills exist
 * </ul>
 *
 * <p>Includes a subtle highlight animation when the active skill changes.
 */
public class SkillHudUI extends Group {

  private static final DungeonLogger LOGGER = DungeonLogger.getLogger(SkillHudUI.class);

  // Layout constants
  private static final float BOX_WIDTH = 120f;
  private static final float BOX_HEIGHT = 80f;
  private static final float PADDING_X = 20f;
  private static final float PADDING_Y = 20f;
  private static final float INNER_PADDING = 8f;
  private static final float BORDER_WIDTH = 3f;
  private static final float ARC_RADIUS = 28f;
  private static final int ARC_SEGMENTS = 32;

  // Font sizes
  private static final int SKILL_NAME_FONT_SIZE = 14;
  private static final int STATUS_FONT_SIZE = 12;
  private static final int SLOT_FONT_SIZE = 10;

  // Colors
  private static final Color BG_COLOR = new Color(0.1f, 0.1f, 0.15f, 0.85f);
  private static final Color BORDER_COLOR = new Color(0.4f, 0.35f, 0.25f, 1f);
  private static final Color BORDER_HIGHLIGHT_COLOR = new Color(0.9f, 0.75f, 0.3f, 1f);
  private static final Color READY_COLOR = new Color(0.3f, 0.85f, 0.4f, 1f);
  private static final Color COOLDOWN_COLOR = new Color(0.9f, 0.5f, 0.2f, 1f);
  private static final Color COOLDOWN_LOW_COLOR = new Color(0.85f, 0.25f, 0.2f, 1f);
  private static final Color ARC_BG_COLOR = new Color(0.25f, 0.25f, 0.3f, 0.9f);
  private static final Color ARC_FILL_COLOR = new Color(0.4f, 0.7f, 0.9f, 0.9f);
  private static final Color NO_SKILL_COLOR = new Color(0.5f, 0.5f, 0.5f, 0.8f);

  // Highlight animation
  private static final float HIGHLIGHT_DURATION = 0.4f;

  // Text constants
  private static final String READY_TEXT = "READY";
  private static final String NO_SKILL_TEXT = "No Skill";

  private static ShapeRenderer shapeRenderer;

  private final BitmapFont skillNameFont;
  private final BitmapFont statusFont;
  private final BitmapFont slotFont;
  private final GlyphLayout glyphLayout;

  private int boundEntityId = -1;
  private String lastSkillName = null;
  private int lastSkillCount = 0;
  private float highlightTimer = 0f;
  private boolean markedForRemoval = false;

  /**
   * Creates a new SkillHudUI instance bound to the specified entity.
   *
   * @param entityId the entity ID of the player to track
   * @return a new SkillHudUI instance
   */
  public static SkillHudUI create(int entityId) {
    SkillHudUI ui = new SkillHudUI();
    ui.bindEntity(entityId);
    return ui;
  }

  /** Creates a new SkillHudUI instance. */
  public SkillHudUI() {
    this.skillNameFont =
        FontHelper.getFont(FontHelper.DEFAULT_FONT_PATH, SKILL_NAME_FONT_SIZE, Color.WHITE);
    this.statusFont =
        FontHelper.getFont(FontHelper.DEFAULT_FONT_PATH, STATUS_FONT_SIZE, Color.WHITE);
    this.slotFont = FontHelper.getFont(FontHelper.DEFAULT_FONT_PATH, SLOT_FONT_SIZE, Color.GRAY);
    this.glyphLayout = new GlyphLayout();

    this.setBounds(0, 0, Game.windowWidth(), Game.windowHeight());
  }

  /**
   * Binds this HUD to a specific player entity.
   *
   * @param entityId the entity ID of the player to track
   */
  public void bindEntity(int entityId) {
    this.boundEntityId = entityId;
    this.lastSkillName = null;
    this.lastSkillCount = 0;
    this.markedForRemoval = false;
  }

  /**
   * Returns the bound entity ID.
   *
   * @return the entity ID, or -1 if not bound
   */
  public int entityId() {
    return boundEntityId;
  }

  /**
   * Returns whether this UI is marked for removal.
   *
   * @return true if the UI should be removed
   */
  public boolean isMarkedForRemoval() {
    return markedForRemoval;
  }

  /** Handles window resize by updating bounds. */
  public void handleResize() {
    this.setSize(Game.windowWidth(), Game.windowHeight());
  }

  @Override
  public void act(float delta) {
    super.act(delta);

    // Update highlight timer
    if (highlightTimer > 0) {
      highlightTimer = Math.max(0, highlightTimer - delta);
    }

    // Check for skill changes
    Optional<Entity> entityOpt = Game.findEntityById(boundEntityId);
    if (entityOpt.isEmpty()) {
      // Entity no longer exists - will be handled in draw
      return;
    }

    Entity entity = entityOpt.get();
    Optional<SkillComponent> skillCompOpt = entity.fetch(SkillComponent.class);
    if (skillCompOpt.isEmpty()) {
      // No skill component - check if we need to reset state
      if (lastSkillName != null || lastSkillCount > 0) {
        lastSkillName = null;
        lastSkillCount = 0;
        highlightTimer = HIGHLIGHT_DURATION;
      }
      return;
    }

    SkillComponent skillComp = skillCompOpt.get();
    int currentSkillCount = skillComp.skillCount();
    SkillData activeSkillData = skillComp.activeSkillData();

    // Detect skill count changes (add/remove)
    if (currentSkillCount != lastSkillCount) {
      lastSkillCount = currentSkillCount;
      highlightTimer = HIGHLIGHT_DURATION;
    }

    // Detect active skill changes
    String currentSkillName = activeSkillData != null ? activeSkillData.name() : null;
    if ((currentSkillName == null && lastSkillName != null)
        || (currentSkillName != null && !currentSkillName.equals(lastSkillName))) {
      lastSkillName = currentSkillName;
      highlightTimer = HIGHLIGHT_DURATION;
    }
  }

  @Override
  public void draw(Batch batch, float parentAlpha) {
    super.draw(batch, parentAlpha);

    // Check if entity still exists
    Optional<Entity> entityOpt = Game.findEntityById(boundEntityId);
    if (entityOpt.isEmpty()) {
      LOGGER.warn("Bound entity {} no longer exists, marking skill HUD for removal", boundEntityId);
      markedForRemoval = true;
      return;
    }

    Entity entity = entityOpt.get();
    Optional<SkillComponent> skillCompOpt = entity.fetch(SkillComponent.class);

    // Calculate position (bottom-right corner)
    float boxX = Game.windowWidth() - BOX_WIDTH - PADDING_X;
    float boxY = PADDING_Y;

    // End batch to use ShapeRenderer
    batch.end();

    initShapeRenderer();

    // Draw background and border
    drawBackground(boxX, boxY);

    // Resume batch for text rendering
    batch.begin();

    // Draw content based on skill state
    if (skillCompOpt.isEmpty()) {
      drawNoSkillState(batch, boxX, boxY);
    } else {
      SkillComponent skillComp = skillCompOpt.get();
      SkillData activeSkillData = skillComp.activeSkillData();
      int totalSkills = skillComp.skillCount();

      if (totalSkills == 0 || activeSkillData == null) {
        drawNoSkillState(batch, boxX, boxY);
      } else {
        int activeIndex = skillComp.activeSkillIndex() + 1;

        // Draw skill content
        drawSkillContent(batch, boxX, boxY, activeSkillData, activeIndex, totalSkills);
      }
    }
  }

  private void initShapeRenderer() {
    if (shapeRenderer == null) {
      shapeRenderer = new ShapeRenderer();
    }
  }

  private void drawBackground(float boxX, float boxY) {
    shapeRenderer.setProjectionMatrix(Game.stage().orElseThrow().getCamera().combined);

    // Draw filled background
    shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
    shapeRenderer.setColor(BG_COLOR);
    shapeRenderer.rect(boxX, boxY, BOX_WIDTH, BOX_HEIGHT);
    shapeRenderer.end();

    // Draw border with highlight effect
    shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
    Color borderColor = calculateBorderColor();
    shapeRenderer.setColor(borderColor);

    // Draw border lines (multiple passes for thickness effect)
    for (int i = 0; i < (int) BORDER_WIDTH; i++) {
      shapeRenderer.rect(boxX - i, boxY - i, BOX_WIDTH + 2 * i, BOX_HEIGHT + 2 * i);
    }
    shapeRenderer.end();
  }

  private Color calculateBorderColor() {
    if (highlightTimer > 0) {
      float t = highlightTimer / HIGHLIGHT_DURATION;
      // Pulse effect - fade from highlight to normal
      return new Color(
          BORDER_COLOR.r + (BORDER_HIGHLIGHT_COLOR.r - BORDER_COLOR.r) * t,
          BORDER_COLOR.g + (BORDER_HIGHLIGHT_COLOR.g - BORDER_COLOR.g) * t,
          BORDER_COLOR.b + (BORDER_HIGHLIGHT_COLOR.b - BORDER_COLOR.b) * t,
          1f);
    }
    return BORDER_COLOR;
  }

  private void drawNoSkillState(Batch batch, float boxX, float boxY) {
    glyphLayout.setText(skillNameFont, NO_SKILL_TEXT);
    float textX = boxX + (BOX_WIDTH - glyphLayout.width) / 2;
    float textY = boxY + BOX_HEIGHT / 2 + glyphLayout.height / 2;

    skillNameFont.setColor(NO_SKILL_COLOR);
    skillNameFont.draw(batch, NO_SKILL_TEXT, textX, textY);
    skillNameFont.setColor(Color.WHITE);
  }

  private void drawSkillContent(
      Batch batch, float boxX, float boxY, SkillData skillData, int activeIndex, int totalSkills) {

    float centerX = boxX + BOX_WIDTH / 2;
    float arcCenterY = boxY + BOX_HEIGHT / 2 + 5;

    // Draw cooldown arc (behind text)
    batch.end();
    drawCooldownArc(centerX, arcCenterY, skillData);
    batch.begin();

    // Draw skill name at top
    String skillName = skillData.name() != null ? skillData.name() : "Unknown";
    if (skillName.length() > 12) {
      skillName = skillName.substring(0, 10) + "..";
    }
    glyphLayout.setText(skillNameFont, skillName);
    float nameX = boxX + (BOX_WIDTH - glyphLayout.width) / 2;
    float nameY = boxY + BOX_HEIGHT - INNER_PADDING;
    skillNameFont.draw(batch, skillName, nameX, nameY);

    // Draw status text (READY or cooldown)
    drawStatusText(batch, boxX, boxY, skillData);

    // Draw slot indicator if multiple skills
    if (totalSkills > 1) {
      drawSlotIndicator(batch, boxX, boxY, activeIndex, totalSkills);
    }
  }

  private void drawCooldownArc(float centerX, float centerY, SkillData skillData) {
    float progress = skillData.cooldownProgress();

    shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);

    // Draw background circle
    shapeRenderer.setColor(ARC_BG_COLOR);
    drawFilledCircle(centerX, centerY, ARC_RADIUS);

    // Draw progress arc
    if (progress < 1.0f) {
      shapeRenderer.setColor(ARC_FILL_COLOR);
      drawFilledArc(centerX, centerY, ARC_RADIUS - 2, 90, 360 * progress);
    } else {
      // Full circle when ready
      shapeRenderer.setColor(READY_COLOR.cpy().mul(1f, 1f, 1f, 0.6f));
      drawFilledCircle(centerX, centerY, ARC_RADIUS - 2);
    }

    shapeRenderer.end();
  }

  private void drawFilledCircle(float x, float y, float radius) {
    shapeRenderer.circle(x, y, radius, ARC_SEGMENTS);
  }

  private void drawFilledArc(float x, float y, float radius, float startAngle, float degrees) {
    if (degrees <= 0) return;

    float start = startAngle;
    float angleStep = degrees / ARC_SEGMENTS;

    for (int i = 0; i < ARC_SEGMENTS; i++) {
      float angle1 = (float) Math.toRadians(start + i * angleStep);
      float angle2 = (float) Math.toRadians(start + (i + 1) * angleStep);

      float x1 = x + (float) Math.cos(angle1) * radius;
      float y1 = y + (float) Math.sin(angle1) * radius;
      float x2 = x + (float) Math.cos(angle2) * radius;
      float y2 = y + (float) Math.sin(angle2) * radius;

      shapeRenderer.triangle(x, y, x1, y1, x2, y2);
    }
  }

  private void drawStatusText(Batch batch, float boxX, float boxY, SkillData skillData) {
    String statusText;
    Color statusColor;

    if (skillData.canBeUsed()) {
      statusText = READY_TEXT;
      statusColor = READY_COLOR;
    } else {
      long remainingMs = skillData.remainingCooldownMs();
      float remainingSec = remainingMs / 1000f;

      if (remainingSec >= 1.0f) {
        statusText = String.format("%.1fs", remainingSec);
      } else {
        statusText = String.format("%.2fs", remainingSec);
      }

      // Color based on remaining time
      statusColor = remainingSec > 2.0f ? COOLDOWN_COLOR : COOLDOWN_LOW_COLOR;
    }

    glyphLayout.setText(statusFont, statusText);
    float statusX = boxX + (BOX_WIDTH - glyphLayout.width) / 2;
    float statusY = boxY + INNER_PADDING + glyphLayout.height + 2;

    statusFont.setColor(statusColor);
    statusFont.draw(batch, statusText, statusX, statusY);
    statusFont.setColor(Color.WHITE);
  }

  private void drawSlotIndicator(Batch batch, float boxX, float boxY, int current, int total) {
    String slotText = String.format("(%d/%d)", current, total);
    glyphLayout.setText(slotFont, slotText);

    float slotX = boxX + BOX_WIDTH - glyphLayout.width - 4;
    float slotY = boxY + BOX_HEIGHT - 4;

    slotFont.draw(batch, slotText, slotX, slotY);
  }

  /** Disposes of resources used by this UI. */
  public void dispose() {
    // Fonts are managed by FontHelper, no disposal needed
  }
}
