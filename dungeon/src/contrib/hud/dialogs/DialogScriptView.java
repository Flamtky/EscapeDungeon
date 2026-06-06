package contrib.hud.dialogs;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.Action;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Scaling;
import contrib.hud.elements.RichLabel;
import core.Game;
import core.utils.FontSpec;
import core.utils.components.draw.TextureMap;
import core.utils.components.path.SimpleIPath;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Reusable scene2d {@link Table} that renders a sequence of {@link DialogEntry} pages parsed from a
 * dialog script (see {@link DialogScript}).
 *
 * <p>Each page is laid out as either:
 *
 * <ul>
 *   <li>A two-column row with a speaker portrait + name on the left and the spoken text (a {@link
 *       RichLabel} with optional typewriter mode) on the right, when the entry has a speaker.
 *   <li>A single full-width text row when the entry has no speaker (the entry's portrait/name
 *       column is omitted entirely; the text expands to occupy the same total width).
 * </ul>
 *
 * <p>Interaction is delegated to the consumer: {@link #advance()} skips the typewriter if it is
 * still revealing, otherwise advances to the next page (or fires the sequence-complete callback
 * when invoked past the last page). Consumers wire their own input listeners to drive the view.
 *
 * <p>This class does not own input, focus, or any chrome beyond the speaker / text layout itself.
 */
final class DialogScriptView extends Table {

  /** Result of an {@link #advance()} call. */
  enum AdvanceResult {
    /** Typewriter was still running; it has been fast-forwarded to completion. */
    SKIPPED_TYPEWRITER,
    /** A new page is now displayed. */
    NEXT_PAGE,
    /**
     * Already on the last page with the typewriter finished; the sequence-complete callback was
     * fired.
     */
    SEQUENCE_COMPLETE
  }

  static final FontSpec NAME_FONT_SPEC = FontSpec.of("fonts/Roboto-SemiBold.ttf", 18, Color.BLACK);
  static final FontSpec TEXT_FONT_SPEC = DialogDesign.DIALOG_FONT_SPEC_NORMAL;

  static final float IMAGE_SIZE = 128f;
  static final float COLUMN_GAP = 16f;
  static final float TEXT_WIDTH = 380f;
  static final float MIN_CONTENT_HEIGHT = 160f;
  static final float NAME_PAD_TOP = 6f;
  private static final float MAX_VIEWPORT_WIDTH_FACTOR = 0.88f;
  private static final float VIEWPORT_SIDE_MARGIN = 64f;
  private static final Pattern IMAGE_BLOCK_WIDTH_PATTERN =
      Pattern.compile("\\[img-block\\b[^]]*\\bwidth=([^\\s\\]]+)");

  /** Total content width when no speaker is present (text spans the full row). */
  static final float FULL_TEXT_WIDTH = IMAGE_SIZE + COLUMN_GAP + TEXT_WIDTH;

  /**
   * Calculates the full dialog script content width for the given text font size.
   *
   * @param textFontSize base font size for speaker text
   * @return the desired full content width in pixels
   */
  static float fullTextWidthForFontSize(int textFontSize) {
    return IMAGE_SIZE + COLUMN_GAP + textWidthForFontSize(textFontSize);
  }

  private static float textWidthForFontSize(int textFontSize) {
    float fontScale = Math.max(1f, textFontSize / (float) TEXT_FONT_SPEC.size());
    float scaledWidth = TEXT_WIDTH * fontScale;
    return Math.min(scaledWidth, maxSpeakerTextWidth());
  }

  private static float maxSpeakerTextWidth() {
    float viewportWidth = Math.max(FULL_TEXT_WIDTH, Game.windowWidth());
    float maxFullWidth =
        Math.max(
            FULL_TEXT_WIDTH,
            Math.min(
                viewportWidth * MAX_VIEWPORT_WIDTH_FACTOR, viewportWidth - VIEWPORT_SIDE_MARGIN));
    return maxFullWidth - IMAGE_SIZE - COLUMN_GAP;
  }

  private final List<DialogEntry> entries;
  private final Map<String, Texture> textureCache = new HashMap<>();

  private final Image speakerImage;
  private final RichLabel nameLabel;
  private final RichLabel textLabel;
  private final Table leftColumn;
  private FontSpec textFontSpec;

  private int currentIndex = -1;
  private Runnable onLastPageRevealed;
  private Runnable onSequenceComplete;
  private boolean lastPageRevealedFired;
  private float speakerColumnPadLeft;
  private float imagePageContentWidth;

  /**
   * Creates a new view by parsing the given script. The first entry is shown immediately.
   *
   * @param script dialog script to parse (must be non-blank)
   */
  DialogScriptView(String script) {
    this(script, TEXT_FONT_SPEC.size());
  }

  /**
   * Creates a new view by parsing the given script and using the given base text font size.
   *
   * @param script dialog script to parse (must be non-blank)
   * @param textFontSize base font size for speaker text
   */
  DialogScriptView(String script, int textFontSize) {
    this(parseEntries(script), TEXT_FONT_SPEC.withSize(textFontSize));
  }

  /**
   * Creates a new view for the given non-empty entries list. The first entry is shown immediately.
   *
   * @param entries the entries to display, must contain at least one entry
   */
  DialogScriptView(List<DialogEntry> entries) {
    this(entries, TEXT_FONT_SPEC);
  }

  private DialogScriptView(List<DialogEntry> entries, FontSpec textFontSpec) {
    Objects.requireNonNull(entries, "entries");
    if (entries.isEmpty()) {
      throw new IllegalArgumentException("entries must not be empty");
    }
    this.entries = entries;
    this.textFontSpec = Objects.requireNonNull(textFontSpec, "textFontSpec");

    speakerImage = new Image();
    speakerImage.setScaling(Scaling.fit);
    speakerImage.setAlign(Align.center);

    nameLabel = new RichLabel("", NAME_FONT_SPEC);

    leftColumn = new Table();
    leftColumn.add(speakerImage).size(IMAGE_SIZE).row();
    leftColumn.add(nameLabel).center().padTop(NAME_PAD_TOP);

    textLabel = new RichLabel("", this.textFontSpec, true);
    textLabel.setWrap(true);

    top().left();

    // Make this view itself a hit target so consumers can attach click listeners that fire even
    // when the click lands on empty space within the view's bounds (e.g. below short text).
    setTouchable(Touchable.enabled);

    showEntry(0);
  }

  private static List<DialogEntry> parseEntries(String script) {
    if (script == null || script.isBlank()) {
      throw new DialogCreationException("DialogScriptView requires a non-blank dialog script");
    }
    List<DialogEntry> parsed = DialogScript.parse(script);
    if (parsed.isEmpty()) {
      throw new DialogCreationException("Dialog script produced no pages");
    }
    return parsed;
  }

  /**
   * Advances the view: skips the typewriter if still running, else moves to the next page, else
   * fires the sequence-complete callback.
   *
   * @return what the call did
   */
  AdvanceResult advance() {
    if (!textLabel.isTypewriterFinished()) {
      textLabel.skipTypewriter();
      return AdvanceResult.SKIPPED_TYPEWRITER;
    }
    if (currentIndex >= entries.size() - 1) {
      if (onSequenceComplete != null) onSequenceComplete.run();
      return AdvanceResult.SEQUENCE_COMPLETE;
    }
    showEntry(currentIndex + 1);
    return AdvanceResult.NEXT_PAGE;
  }

  /**
   * @return whether the currently displayed entry is the last one in the sequence
   */
  boolean isOnLastPage() {
    return currentIndex == entries.size() - 1;
  }

  /**
   * @return whether the currently displayed entry's typewriter has finished revealing
   */
  boolean isCurrentTypewriterFinished() {
    return textLabel.isTypewriterFinished();
  }

  /**
   * @return whether the current page contains only image content without a speaker.
   */
  boolean currentEntryIsImagePage() {
    if (currentIndex < 0 || currentIndex >= entries.size()) {
      return false;
    }
    DialogEntry entry = entries.get(currentIndex);
    return !entry.hasSpeaker() && entry.text().contains("[img-block");
  }

  /**
   * Sets the full content width available to image-only pages.
   *
   * @param value available content width in pixels
   */
  void imagePageContentWidth(float value) {
    float width = Math.max(0f, value);
    if (imagePageContentWidth == width) {
      return;
    }
    imagePageContentWidth = width;
    layoutCurrentEntry();
  }

  /**
   * Sets a callback to run once when the last page's text has been fully revealed (either by the
   * typewriter finishing naturally or by being skipped). Fires at most once per view lifetime.
   *
   * <p>Safe to call before or after the last page is reached; the view re-arms the underlying
   * typewriter callback as needed.
   *
   * @param callback the callback, or {@code null} to clear
   */
  void setOnLastPageRevealed(Runnable callback) {
    this.onLastPageRevealed = callback;
    armLastPageRevealedHook();
  }

  /**
   * Sets a callback fired when {@link #advance()} is called while already on the last page with the
   * typewriter finished.
   *
   * @param callback the callback, or {@code null} to clear
   */
  void setOnSequenceComplete(Runnable callback) {
    this.onSequenceComplete = callback;
  }

  /**
   * Adds an extra left padding to the speaker column (only on pages that actually show a speaker).
   *
   * @param value extra left padding in pixels
   */
  void setSpeakerColumnPadLeft(float value) {
    speakerColumnPadLeft = Math.max(0f, value);
    if (currentIndex >= 0) {
      showEntry(currentIndex);
    }
  }

  /**
   * Changes the base font size of the current speaker text and reflows the dialog.
   *
   * @param textFontSize new base font size in pixels
   */
  void textFontSize(int textFontSize) {
    textFontSpec = TEXT_FONT_SPEC.withSize(textFontSize);
    textLabel.setFontSpec(textFontSpec);
    layoutCurrentEntry();
  }

  /** Clears the per-view texture cache. Called automatically on stage detach by some consumers. */
  void disposeCache() {
    textureCache.clear();
  }

  private void showEntry(int index) {
    currentIndex = index;
    DialogEntry entry = entries.get(index);

    boolean blankName = false;
    if (entry.hasSpeaker()) {
      Texture tex =
          textureCache.computeIfAbsent(
              entry.imagePath(), p -> TextureMap.instance().textureAt(new SimpleIPath(p)));
      speakerImage.setDrawable(new TextureRegionDrawable(tex));
      String speakerName = entry.speakerName() == null ? "" : entry.speakerName();
      blankName = speakerName.isBlank();
      nameLabel.setText(RichLabel.toRichText(speakerName));
      nameLabel.setVisible(!blankName);
    }
    textLabel.setText(RichLabel.toRichText(entry.text()));
    layoutCurrentEntry();

    armLastPageRevealedHook();
  }

  private void layoutCurrentEntry() {
    if (currentIndex < 0) {
      return;
    }
    DialogEntry entry = entries.get(currentIndex);
    boolean blankName =
        entry.hasSpeaker() && (entry.speakerName() == null || entry.speakerName().isBlank());
    // Re-layout cells: presence/absence of speaker may change between entries.
    clearChildren();
    if (entry.hasSpeaker()) {
      leftColumn.clearChildren();
      leftColumn.add(speakerImage).size(IMAGE_SIZE).row();
      if (!blankName) {
        leftColumn.add(nameLabel).center().padTop(NAME_PAD_TOP);
      }
      leftColumn.pack();

      float anchorPadTop = Math.max(0f, (MIN_CONTENT_HEIGHT - leftColumn.getPrefHeight()) / 2f);

      add(leftColumn).top().padLeft(speakerColumnPadLeft).padTop(anchorPadTop).padRight(COLUMN_GAP);
      add(textLabel)
          .width(textWidth(entry, textWidthForFontSize(textFontSpec.size())))
          .minHeight(MIN_CONTENT_HEIGHT)
          .top();
    } else {
      float width =
          currentEntryIsImagePage() && imagePageContentWidth > 0f
              ? imagePageContentWidth
              : textWidth(entry, fullTextWidthForFontSize(textFontSpec.size()));
      add(textLabel).width(width).top();
    }
    invalidateHierarchy();
  }

  private static float textWidth(DialogEntry entry, float defaultWidth) {
    float width = defaultWidth;
    Matcher matcher = IMAGE_BLOCK_WIDTH_PATTERN.matcher(entry.text());
    while (matcher.find()) {
      String spec = matcher.group(1);
      if (spec.endsWith("%")) {
        continue;
      }
      try {
        width = Math.max(width, Float.parseFloat(spec));
      } catch (NumberFormatException ignored) {
        // Keep the default width for malformed markup.
      }
    }
    return width;
  }

  /**
   * Arms the underlying typewriter callback when applicable. Idempotent.
   *
   * <p>Note that {@link RichLabel#isTypewriterFinished()} returns {@code true} immediately after a
   * {@code setText} call, before the next layout pass populates the reveal schedule. To avoid
   * firing the callback prematurely we always register the typewriter hook AND schedule a deferred
   * fallback action that fires once the layout has had a chance to mark the typewriter as "in
   * progress" (or stays "finished" because the entry's text contains no typewriter markup).
   */
  private void armLastPageRevealedHook() {
    if (onLastPageRevealed == null) return;
    if (lastPageRevealedFired) return;
    if (!isOnLastPage()) return;

    textLabel.onTypewriterFinished(this::fireLastPageRevealed);

    addAction(
        new Action() {
          private int framesWaited = 0;

          @Override
          public boolean act(float delta) {
            // Wait two frames so the layout pass has run and applyTypewriterState() has flipped
            // typewriterFinished to false (when typewriter tags are present).
            if (framesWaited < 2) {
              framesWaited++;
              return false;
            }
            if (!lastPageRevealedFired && textLabel.isTypewriterFinished()) {
              fireLastPageRevealed();
            }
            return true;
          }
        });
  }

  private void fireLastPageRevealed() {
    if (lastPageRevealedFired) return;
    lastPageRevealedFired = true;
    if (onLastPageRevealed != null) onLastPageRevealed.run();
  }
}
