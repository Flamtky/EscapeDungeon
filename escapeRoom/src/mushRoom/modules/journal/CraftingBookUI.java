package mushRoom.modules.journal;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Stack;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Array;
import contrib.crafting.Recipe;
import contrib.item.Item;
import core.Game;
import core.utils.components.path.IPath;
import core.utils.components.path.SimpleIPath;
import mushRoom.Sounds;

import java.util.Arrays;

/**
 * UI component representing a crafting book with recipe entries, showing the result and required
 * ingredients with tooltips.
 */
public class CraftingBookUI extends Group {

  private static final String ITEMS_NEEDED_TEXT = "Zutaten:";
  private static final int MAX_INGREDIENTS_PER_ROW = 3;

  private static final float BOOK_WIDTH = 1260f;
  private static final float BOOK_HEIGHT = 900f;

  private static final float PAGE_PAD_TOP = 80f;
  private static final float PAGE_PAD_BOTTOM = 60f;
  private static final float PAGE_PAD_SIDE = 50f;

  private static final float RESULT_BOX_SIZE = 200f;
  private static final float INGREDIENT_BOX_SIZE = 80f;
  private static final float BOX_BORDER_WIDTH = 3f;
  private static final int BOX_BORDER_COLOR = 0x000000ff;
  private static final int BOX_BACKGROUND_COLOR = 0xffffffff;

  private static final int TOOLTIP_BACKGROUND_COLOR = 0xffffffee;
  private static final int BORDER_PADDING = 5;
  private static final int LINE_GAP = 5;

  private static final IPath FONT_FNT = new SimpleIPath("skin/myFont.fnt");
  private static final IPath FONT_PNG = new SimpleIPath("skin/myFont.png");

  private static BitmapFont tooltipFont;
  private static TextureRegion tooltipBackground;

  static {
    if (!Game.isHeadless()) {
      tooltipFont =
          new BitmapFont(
              Gdx.files.internal(FONT_FNT.pathString()),
              Gdx.files.internal(FONT_PNG.pathString()),
              false);

      Pixmap bgPixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
      bgPixmap.drawPixel(0, 0, TOOLTIP_BACKGROUND_COLOR);
      Texture tooltipBgTexture = new Texture(bgPixmap);
      bgPixmap.dispose();
      tooltipBackground = new TextureRegion(tooltipBgTexture, 0, 0, 1, 1);
    }
  }

  // Components
  private final Image bookImage;
  private final TextButton btnLeft;
  private final TextButton btnRight;
  private final Table rightPageContent;
  private final Label pageCounterLabel;

  // Data
  private final Array<RecipeEntry> entries = new Array<>();
  private int currentPageIndex = 0;
  private final Skin skin;
  private long soundHandle = -1;

  // For tooltips
  private Item hoveredItem = null;

  // TODO: Implement proper dispose for boxTexture/resultBoxTexture when dialog system is reworked
  // Box textures (instance-specific)
  private final Texture boxTexture;
  private final Texture resultBoxTexture;

  /**
   * Constructs a new CraftingBookUI with the specified skin and book background.
   *
   * @param skin The UI skin to use
   * @param bookBackground The drawable for the book background
   */
  public CraftingBookUI(Skin skin, Drawable bookBackground) {
    this.skin = skin;

    // Create box textures
    boxTexture = createBoxTexture((int) INGREDIENT_BOX_SIZE);
    resultBoxTexture = createBoxTexture((int) RESULT_BOX_SIZE);

    bookImage = new Image(bookBackground);
    bookImage.setSize(BOOK_WIDTH, BOOK_HEIGHT);
    addActor(bookImage);

    // Navigation Buttons
    btnLeft = new TextButton("<", skin);
    btnRight = new TextButton(">", skin);

    btnLeft.addListener(
        new ClickListener() {
          @Override
          public void clicked(InputEvent event, float x, float y) {
            prevPage();
          }
        });

    btnRight.addListener(
        new ClickListener() {
          @Override
          public void clicked(InputEvent event, float x, float y) {
            nextPage();
          }
        });

    addActor(btnLeft);
    addActor(btnRight);

    // Right Page Container
    rightPageContent = new Table();
    rightPageContent.top();
    addActor(rightPageContent);

    // Initialize Page Counter
    pageCounterLabel = new Label("0 / 0", skin, "blank-black");
    pageCounterLabel.setAlignment(Align.center);

    this.setSize(Game.windowWidth(), Game.windowHeight());
  }

  private Texture createBoxTexture(int size) {
    Pixmap pixmap = new Pixmap(size, size, Pixmap.Format.RGBA8888);
    // Fill with background
    pixmap.setColor(new Color(BOX_BACKGROUND_COLOR));
    pixmap.fill();
    // Draw border
    pixmap.setColor(new Color(BOX_BORDER_COLOR));
    int borderWidth = (int) BOX_BORDER_WIDTH;
    for (int i = 0; i < borderWidth; i++) {
      pixmap.drawRectangle(i, i, size - 2 * i, size - 2 * i);
    }
    Texture texture = new Texture(pixmap);
    pixmap.dispose();
    return texture;
  }

  @Override
  public void setSize(float width, float height) {
    super.setSize(width, height);
    repositionElements();
  }

  private void repositionElements() {
    float screenW = getWidth();
    float screenH = getHeight();

    float bookX = (screenW - BOOK_WIDTH) / 2f;
    float bookY = (screenH - BOOK_HEIGHT) / 2f;
    bookImage.setPosition(bookX, bookY);

    // Position Buttons (Left and Right of the book)
    float btnMargin = 20f;
    if (btnLeft.getWidth() == 0) btnLeft.pack();
    if (btnRight.getWidth() == 0) btnRight.pack();
    btnLeft.setPosition(
        bookX - btnLeft.getWidth() - btnMargin, screenH / 2f - btnLeft.getHeight() / 2f);
    btnRight.setPosition(bookX + BOOK_WIDTH + btnMargin, screenH / 2f - btnRight.getHeight() / 2f);

    // Position the Content Table strictly over the RIGHT page
    float rightPageX = bookX + (BOOK_WIDTH / 2f);
    rightPageContent.setPosition(rightPageX, bookY);
    rightPageContent.setSize(BOOK_WIDTH / 2f, BOOK_HEIGHT);
    rightPageContent.pad(PAGE_PAD_TOP, PAGE_PAD_SIDE, PAGE_PAD_BOTTOM, PAGE_PAD_SIDE);

    rightPageContent.invalidate();
  }

  /**
   * Adds a recipe entry to the crafting book.
   *
   * @param recipe The recipe to add
   */
  public void addRecipeEntry(Recipe recipe) {
    entries.add(new RecipeEntry(recipe));
    refreshPage();
  }

  /**
   * Adds an empty entry with a message (for when no recipes exist).
   *
   * @param message The message to display
   */
  public void addEmptyEntry(String message) {
    entries.add(new RecipeEntry(message));
    refreshPage();
  }

  private void nextPage() {
    if (currentPageIndex + 1 < entries.size) {
      currentPageIndex++;
      refreshPage();
    }
    playPageFlipSound();
  }

  private void prevPage() {
    if (currentPageIndex - 1 >= 0) {
      currentPageIndex--;
      refreshPage();
    }
    playPageFlipSound();
  }

  private void playPageFlipSound() {
    Game.audio().stopInstance(soundHandle);
    soundHandle = Sounds.FLIP_BOOK_PAGE.play();
  }

  private void refreshPage() {
    rightPageContent.clearChildren();

    int currentVisualPage = currentPageIndex + 1;
    int totalVisualPages = Math.max(entries.size, 1);

    if (currentPageIndex < entries.size) {
      RecipeEntry entry = entries.get(currentPageIndex);
      if (entry.isEmptyMessage()) {
        // Just show the message
        Label msgLabel = new Label(entry.message, skin, "blank-black");
        msgLabel.setFontScale(0.6f);
        msgLabel.setWrap(true);
        msgLabel.setAlignment(Align.center);
        rightPageContent.add(msgLabel).width(450f).expandY().center();
      } else {
        addRecipeToTable(entry.recipe);
      }
    }

    rightPageContent.row();

    // Footer
    pageCounterLabel.setText(currentVisualPage + " / " + totalVisualPages);
    rightPageContent.add(pageCounterLabel).bottom().expandX().padTop(20f);

    // Update Button visibility
    btnLeft.setVisible(currentPageIndex > 0);
    btnRight.setVisible(currentPageIndex + 1 < entries.size);
  }

  private void addRecipeToTable(Recipe recipe) {
    // Result box
    if (recipe.results().length > 0 && recipe.results()[0] instanceof Item resultItem) {
      Stack resultStack = createItemBox(resultItem, RESULT_BOX_SIZE, resultBoxTexture);
      rightPageContent.add(resultStack).size(RESULT_BOX_SIZE).padBottom(20f);
      rightPageContent.row();
    }

    // "Items needed:" label
    Label itemsNeededLabel = new Label(ITEMS_NEEDED_TEXT, skin, "blank-black");
    itemsNeededLabel.setFontScale(0.5f);
    itemsNeededLabel.setAlignment(Align.center);
    rightPageContent.add(itemsNeededLabel).padBottom(10f);
    rightPageContent.row();

    // Ingredients grid
    Table ingredientsGrid = new Table();
    Item[] ingredients =
        Arrays.stream(recipe.ingredients())
            .filter(ing -> ing instanceof Item)
            .map(ing -> (Item) ing)
            .toArray(Item[]::new);

    for (int i = 0; i < ingredients.length; i++) {
      Stack ingredientStack = createItemBox(ingredients[i], INGREDIENT_BOX_SIZE, boxTexture);
      ingredientsGrid.add(ingredientStack).size(INGREDIENT_BOX_SIZE).pad(5f);

      if ((i + 1) % MAX_INGREDIENTS_PER_ROW == 0 && i < ingredients.length - 1) {
        ingredientsGrid.row();
      }
    }

    rightPageContent.add(ingredientsGrid).padTop(10f);
  }

  private Stack createItemBox(Item item, float size, Texture bgTexture) {
    Stack stack = new Stack();

    // Background box
    Image boxBg = new Image(new TextureRegionDrawable(new TextureRegion(bgTexture)));
    stack.add(boxBg);

    // Item texture
    Texture itemTexture = item.inventoryAnimation().getSprite().getTexture();
    Image itemImage = new Image(new TextureRegionDrawable(new TextureRegion(itemTexture)));

    // Container to center and pad the item image
    Table container = new Table();
    float padding = size * 0.1f;
    container.add(itemImage).size(size - padding * 2).pad(padding);
    stack.add(container);

    // Add hover listener for tooltip
    stack.addListener(
        new ClickListener() {
          @Override
          public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor) {
            hoveredItem = item;
          }

          @Override
          public void exit(InputEvent event, float x, float y, int pointer, Actor toActor) {
            if (hoveredItem == item) {
              hoveredItem = null;
            }
          }
        });

    return stack;
  }

  @Override
  public void draw(Batch batch, float parentAlpha) {
    super.draw(batch, parentAlpha);
    drawTooltip(batch);
  }

  private void drawTooltip(Batch batch) {
    if (hoveredItem == null || tooltipFont == null) return;

    float mouseX = Gdx.input.getX();
    float mouseY = Gdx.graphics.getHeight() - Gdx.input.getY();

    String title = hoveredItem.displayName();
    String description = hoveredItem.description();

    GlyphLayout layoutName = new GlyphLayout(tooltipFont, title);
    GlyphLayout layoutDesc = new GlyphLayout(tooltipFont, description);

    float width = Math.max(layoutName.width, layoutDesc.width) + BORDER_PADDING * 2;
    float height = layoutName.height + layoutDesc.height + BORDER_PADDING * 2 + LINE_GAP;

    float tooltipX = mouseX + 15;
    float tooltipY = mouseY + 15;

    // Keep tooltip on screen
    if (tooltipX + width > Gdx.graphics.getWidth()) {
      tooltipX = mouseX - width - 15;
    }
    if (tooltipY + height > Gdx.graphics.getHeight()) {
      tooltipY = mouseY - height - 15;
    }

    // Draw background
    batch.draw(tooltipBackground, tooltipX, tooltipY, width, height);

    // Draw text
    tooltipFont.setColor(Color.BLACK);
    tooltipFont.draw(batch, title, tooltipX + BORDER_PADDING, tooltipY + height - BORDER_PADDING);
    tooltipFont.setColor(new Color(0x000000b0));
    tooltipFont.draw(
        batch,
        description,
        tooltipX + BORDER_PADDING,
        tooltipY + height - BORDER_PADDING - layoutName.height - LINE_GAP);
  }

  /** A single recipe entry in the crafting book. */
  private static class RecipeEntry {
    final Recipe recipe;
    final String message;

    RecipeEntry(Recipe recipe) {
      this.recipe = recipe;
      this.message = null;
    }

    RecipeEntry(String message) {
      this.recipe = null;
      this.message = message;
    }

    boolean isEmptyMessage() {
      return recipe == null;
    }
  }
}

