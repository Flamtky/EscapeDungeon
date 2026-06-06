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
import com.badlogic.gdx.utils.Disposable;
import contrib.crafting.Recipe;
import contrib.item.Item;
import core.Game;
import core.game.WindowEventManager;
import core.utils.components.path.IPath;
import core.utils.components.path.SimpleIPath;
import java.util.Arrays;
import java.util.stream.IntStream;
import mushRoom.Sounds;

/**
 * UI component representing a crafting book with recipe entries.
 *
 * <p>Displays crafting recipes in a book format with navigation between pages. Each recipe shows
 * the result item and required ingredients with tooltips on hover.
 */
public class CraftingBookUI extends Group implements Disposable {

  // Layout configuration
  private static final int RECIPES_PER_PAGE = 3;
  private static final int PAGES_PER_SPREAD = 2;
  private static final int RECIPES_PER_SPREAD = RECIPES_PER_PAGE * PAGES_PER_SPREAD;
  private static final float ENTRY_SPACING = 60f;

  // Text constants
  private static final String ITEMS_NEEDED_TEXT = "Zutaten:";
  private static final int MAX_INGREDIENTS_PER_ROW = 3;

  // Book dimensions
  private static final float BOOK_WIDTH = 1260f;
  private static final float BOOK_HEIGHT = 900f;
  private static final float PAGE_PAD_TOP = 110f;
  private static final float PAGE_PAD_BOTTOM = 60f;
  private static final float PAGE_PAD_SIDE = 75f;
  private static final float LEFT_PAGE_OFFSET = 50f;
  private static final float RIGHT_PAGE_OFFSET = 10f;

  // Box dimensions
  private static final float RESULT_BOX_SIZE = 140f;
  private static final float INGREDIENT_BOX_SIZE = 60f;
  private static final float BOX_BORDER_WIDTH = 3f;
  private static final int BOX_BORDER_COLOR = 0x000000ff;
  private static final int BOX_BACKGROUND_COLOR = 0x0000000f;

  // Tooltip styling
  private static final int TOOLTIP_BACKGROUND_COLOR = 0xffffffee;
  private static final int BORDER_PADDING = 5;
  private static final int LINE_GAP = 5;

  // Navigation button styling
  private static final float NAVIGATION_BUTTON_MARGIN = 20f;

  // Tooltip positioning
  private static final float TOOLTIP_OFFSET = 15f;

  // Font scaling
  private static final float MIN_PAGE_COUNTER_SCALE = 0.4f;
  private static final float MAX_PAGE_COUNTER_SCALE = 0.5f;
  private static final float EMPTY_MESSAGE_FONT_SCALE = 0.6f;
  private static final float ITEM_NAME_FONT_SCALE = 0.6f;
  private static final float ITEMS_NEEDED_FONT_SCALE = 0.4f;

  // Spacing and padding
  private static final float RECIPE_ROW_PAD_BOTTOM = 20f;
  private static final float RECIPE_ROW_PAD_TOP = 10f;
  private static final float RESULT_ITEM_PAD_RIGHT = 20f;
  private static final float ITEM_NAME_PAD_BOTTOM = 8f;
  private static final float ITEMS_NEEDED_PAD_BOTTOM = 5f;
  private static final float INGREDIENT_PAD = 2f;
  private static final float ITEM_BOX_PADDING_RATIO = 0.1f;
  private static final float EMPTY_MESSAGE_WIDTH = 450f;

  // Layout ratios
  private static final float PAGE_WIDTH_RATIO = 0.5f; // Each page is half the book width

  // Font paths
  private static final IPath FONT_FNT = new SimpleIPath("skin/myFont.fnt");
  private static final IPath FONT_PNG = new SimpleIPath("skin/myFont.png");

  // Static resources (shared across instances)
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

  // UI Components
  private final Image bookImage;
  private final TextButton btnLeft;
  private final TextButton btnRight;
  private final Table leftPageContent;
  private final Table rightPageContent;
  private final Label pageCounterLabel;

  // Data
  private final Array<RecipeEntry> entries = new Array<>();
  private final Skin skin;
  private int currentPageIndex = 0;
  private long soundHandle = -1;

  // Tooltip state
  private Item hoveredItem = null;

  private final Texture boxTexture;
  private final Texture resultBoxTexture;

  // Scaling state: scale the book down to fit the window when needed (never upscale)
  private float bookScale = 1f;

  // Window refresh listener; Stored so it can be unregistered when UI is disposed
  private final Runnable windowRefreshListener;

  /**
   * Constructs a new CraftingBookUI with the specified skin and book background.
   *
   * @param skin the UI skin to use for labels and buttons
   * @param bookBackground the drawable for the book background image
   */
  public CraftingBookUI(Skin skin, Drawable bookBackground) {
    this.skin = skin;

    boxTexture = createBoxTexture((int) INGREDIENT_BOX_SIZE);
    resultBoxTexture = createBoxTexture((int) RESULT_BOX_SIZE);

    bookImage = new Image(bookBackground);
    addActor(bookImage);

    btnLeft = createNavigationButton("<", this::prevPage);
    btnRight = createNavigationButton(">", this::nextPage);
    addActor(btnLeft);
    addActor(btnRight);

    leftPageContent = createPageTable();
    rightPageContent = createPageTable();
    addActor(leftPageContent);
    addActor(rightPageContent);

    pageCounterLabel = new Label("0 / 0", skin, "blank-black");
    pageCounterLabel.setAlignment(Align.center);

    this.setSize(Game.windowWidth(), Game.windowHeight());
    windowRefreshListener = () -> this.setSize(Game.windowWidth(), Game.windowHeight());
    WindowEventManager.registerWindowRefreshListener(windowRefreshListener);
  }

  /**
   * Adds a recipe entry to the crafting book.
   *
   * @param recipe the recipe to add
   */
  public void addRecipeEntry(Recipe recipe) {
    entries.add(new RecipeEntry(recipe));
    refreshPage();
  }

  /**
   * Adds an empty entry with a message (for when no recipes exist).
   *
   * @param message the message to display
   */
  public void addEmptyEntry(String message) {
    entries.add(new RecipeEntry(message));
    refreshPage();
  }

  @Override
  public void setSize(float width, float height) {
    super.setSize(width, height);
    repositionElements();
  }

  @Override
  public void draw(Batch batch, float parentAlpha) {
    super.draw(batch, parentAlpha);
    drawTooltip(batch);
  }

  private TextButton createNavigationButton(String text, Runnable onClick) {
    TextButton button = new TextButton(text, skin);
    button.addListener(
        new ClickListener() {
          @Override
          public void clicked(InputEvent event, float x, float y) {
            onClick.run();
          }
        });
    return button;
  }

  private Table createPageTable() {
    Table table = new Table();
    table.top();
    return table;
  }

  private Texture createBoxTexture(int size) {
    Pixmap pixmap = new Pixmap(size, size, Pixmap.Format.RGBA8888);
    pixmap.setColor(new Color(BOX_BACKGROUND_COLOR));
    pixmap.fill();
    pixmap.setColor(new Color(BOX_BORDER_COLOR));
    int borderWidth = (int) BOX_BORDER_WIDTH;
    for (int i = 0; i < borderWidth; i++) {
      pixmap.drawRectangle(i, i, size - 2 * i, size - 2 * i);
    }
    Texture texture = new Texture(pixmap);
    pixmap.dispose();
    return texture;
  }

  private void repositionElements() {
    float screenW = getWidth();
    float screenH = getHeight();

    // Compute a scale so the book fits the window; never upscale (max 1f)
    float scaleX = screenW / BOOK_WIDTH;
    float scaleY = screenH / BOOK_HEIGHT;
    bookScale = Math.min(Math.min(scaleX, scaleY), 1f);

    float scaledBookWidth = BOOK_WIDTH * bookScale;
    float scaledBookHeight = BOOK_HEIGHT * bookScale;

    float bookX = (screenW - scaledBookWidth) / 2f;
    float bookY = (screenH - scaledBookHeight) / 2f;

    bookImage.setPosition(bookX, bookY);
    bookImage.setSize(scaledBookWidth, scaledBookHeight);

    // scale page counter font so it's readable at smaller scales
    pageCounterLabel.setFontScale(
        Math.max(MIN_PAGE_COUNTER_SCALE * bookScale, MAX_PAGE_COUNTER_SCALE * bookScale));

    positionNavigationButtons(bookX, bookY, scaledBookWidth, scaledBookHeight);

    // Position pages with their respective offsets
    float pageWidth = scaledBookWidth * PAGE_WIDTH_RATIO;
    float leftPageX = bookX + (LEFT_PAGE_OFFSET * bookScale);
    float leftPageWidth = pageWidth - (LEFT_PAGE_OFFSET * bookScale);

    float rightPageX = bookX + pageWidth + (RIGHT_PAGE_OFFSET * bookScale);
    float rightPageWidth = pageWidth - (RIGHT_PAGE_OFFSET * bookScale);

    positionPageContent(leftPageContent, leftPageX, bookY, leftPageWidth, scaledBookHeight);
    positionPageContent(rightPageContent, rightPageX, bookY, rightPageWidth, scaledBookHeight);

    // Rebuild page contents so all child widgets (item boxes, fonts, paddings) use the new scale
    refreshPage();
  }

  private void positionNavigationButtons(
      float bookX, float bookY, float scaledBookWidth, float scaledBookHeight) {
    float btnMargin = NAVIGATION_BUTTON_MARGIN * bookScale; // margin scales with book
    if (btnLeft.getWidth() == 0) btnLeft.pack();
    if (btnRight.getWidth() == 0) btnRight.pack();

    // center vertically on the book
    float centerY = bookY + scaledBookHeight / 2f - btnLeft.getHeight() / 2f;
    btnLeft.setPosition(bookX - btnLeft.getWidth() - btnMargin, centerY);
    btnRight.setPosition(bookX + scaledBookWidth + btnMargin, centerY);
  }

  private void positionPageContent(Table pageContent, float x, float y, float width, float height) {
    pageContent.setPosition(x, y);
    pageContent.setSize(width, height);
    pageContent.clearListeners();
    pageContent.pad(
        PAGE_PAD_TOP * bookScale,
        PAGE_PAD_SIDE * bookScale,
        PAGE_PAD_BOTTOM * bookScale,
        PAGE_PAD_SIDE * bookScale);
    pageContent.top().left(); // align content to top-left of the page
    pageContent.invalidate();
  }

  private void nextPage() {
    if (currentPageIndex + RECIPES_PER_SPREAD < entries.size) {
      currentPageIndex += RECIPES_PER_SPREAD;
      refreshPage();
    }
    playPageFlipSound();
  }

  private void prevPage() {
    if (currentPageIndex - RECIPES_PER_SPREAD >= 0) {
      currentPageIndex -= RECIPES_PER_SPREAD;
      refreshPage();
    }
    playPageFlipSound();
  }

  private void playPageFlipSound() {
    Game.audio().stopInstance(soundHandle);
    soundHandle = Sounds.FLIP_BOOK_PAGE.play();
  }

  private void refreshPage() {
    leftPageContent.clearChildren();
    rightPageContent.clearChildren();

    int currentVisualPage = (currentPageIndex / RECIPES_PER_SPREAD) + 1;
    int totalVisualPages = Math.max(1, (int) Math.ceil(entries.size / (float) RECIPES_PER_SPREAD));

    addPageEntries(leftPageContent, currentPageIndex);
    addPageEntries(rightPageContent, currentPageIndex + RECIPES_PER_PAGE);

    addFooter(totalVisualPages, currentVisualPage);
    updateButtonVisibility();
  }

  private void addPageEntries(Table pageContent, int startIndex) {
    IntStream.range(0, RECIPES_PER_PAGE)
        .forEach(
            i -> {
              int entryIndex = startIndex + i;
              if (entryIndex < entries.size) {
                RecipeEntry entry = entries.get(entryIndex);
                if (entry.isEmptyMessage()) {
                  addEmptyMessageToTable(pageContent, entry.message);
                } else {
                  addRecipeToTable(pageContent, entry.recipe);
                }
              }

              if (i < RECIPES_PER_PAGE - 1) {
                pageContent.add().height(ENTRY_SPACING * bookScale);
                pageContent.row();
              }
            });
  }

  private void addFooter(int totalVisualPages, int currentVisualPage) {
    rightPageContent.add().growY();
    rightPageContent.row();
    pageCounterLabel.setText(currentVisualPage + " / " + totalVisualPages);
    rightPageContent.add(pageCounterLabel).bottom().expandX();
  }

  private void updateButtonVisibility() {
    btnLeft.setVisible(currentPageIndex > 0);
    btnRight.setVisible(currentPageIndex + RECIPES_PER_SPREAD < entries.size);
  }

  private void addEmptyMessageToTable(Table pageContent, String message) {
    Label msgLabel = new Label(message, skin, "blank-black");
    msgLabel.setFontScale(EMPTY_MESSAGE_FONT_SCALE * bookScale);
    msgLabel.setWrap(true);
    msgLabel.setAlignment(Align.center);
    pageContent.add(msgLabel).width(EMPTY_MESSAGE_WIDTH * bookScale).expandY().center();
  }

  private void addRecipeToTable(Table pageContent, Recipe recipe) {
    Arrays.stream(recipe.results())
        .filter(Item.class::isInstance)
        .map(Item.class::cast)
        .findFirst()
        .ifPresent(
            resultItem -> {
              Table recipeRow = createRecipeRow(resultItem, recipe);
              pageContent
                  .add(recipeRow)
                  .left()
                  .padBottom(RECIPE_ROW_PAD_BOTTOM * bookScale)
                  .padTop(RECIPE_ROW_PAD_TOP * bookScale);
              pageContent.row();
            });
  }

  private Table createRecipeRow(Item resultItem, Recipe recipe) {
    Table recipeRow = new Table();

    float resultSize = RESULT_BOX_SIZE * bookScale;
    Stack resultStack = createItemBox(resultItem, resultSize, resultBoxTexture);
    recipeRow.add(resultStack).size(resultSize).padRight(RESULT_ITEM_PAD_RIGHT * bookScale);

    Table ingredientsSection = createIngredientsSection(resultItem, recipe);
    recipeRow.add(ingredientsSection).left();

    return recipeRow;
  }

  private Table createIngredientsSection(Item resultItem, Recipe recipe) {
    Table ingredientsSection = new Table();

    Label itemNameLabel = new Label(resultItem.displayName(), skin, "blank-black");
    itemNameLabel.setFontScale(ITEM_NAME_FONT_SCALE * bookScale);
    itemNameLabel.setAlignment(Align.left);
    ingredientsSection.add(itemNameLabel).left().padBottom(ITEM_NAME_PAD_BOTTOM * bookScale);
    ingredientsSection.row();

    Label itemsNeededLabel = new Label(ITEMS_NEEDED_TEXT, skin, "blank-black");
    itemsNeededLabel.setFontScale(ITEMS_NEEDED_FONT_SCALE * bookScale);
    itemsNeededLabel.setAlignment(Align.left);
    ingredientsSection.add(itemsNeededLabel).left().padBottom(ITEMS_NEEDED_PAD_BOTTOM * bookScale);
    ingredientsSection.row();

    Table ingredientsGrid = createIngredientsGrid(recipe);
    ingredientsSection.add(ingredientsGrid).left();

    return ingredientsSection;
  }

  private Table createIngredientsGrid(Recipe recipe) {
    Table ingredientsGrid = new Table();

    Item[] ingredients =
        Arrays.stream(recipe.ingredients())
            .filter(Item.class::isInstance)
            .map(Item.class::cast)
            .toArray(Item[]::new);

    int boxCount = 0;
    for (int i = 0; i < ingredients.length; i++) {
      Item ingredient = ingredients[i];
      // Create one box for each unit in the item's stack size
      int stackSize = ingredient.getAmount();

      for (int j = 0; j < stackSize; j++) {
        float ingredientSize = INGREDIENT_BOX_SIZE * bookScale;
        Stack ingredientStack = createItemBox(ingredient, ingredientSize, boxTexture);
        ingredientsGrid.add(ingredientStack).size(ingredientSize).pad(INGREDIENT_PAD * bookScale);

        boxCount++;

        // Wrap to next row after MAX_INGREDIENTS_PER_ROW boxes, but not after the last box
        if (boxCount % MAX_INGREDIENTS_PER_ROW == 0 && boxCount < getTotalBoxCount(ingredients)) {
          ingredientsGrid.row();
        }
      }
    }

    return ingredientsGrid;
  }

  /**
   * Calculates the total number of ingredient boxes that will be displayed based on each
   * ingredient's stack size.
   *
   * @param ingredients the array of ingredients
   * @return the total number of boxes to be rendered
   */
  private int getTotalBoxCount(Item[] ingredients) {
    int total = 0;
    for (Item ingredient : ingredients) {
      total += ingredient.stackSize();
    }
    return total;
  }

  private Stack createItemBox(Item item, float size, Texture bgTexture) {
    Stack stack = new Stack();

    Image boxBg = new Image(new TextureRegionDrawable(new TextureRegion(bgTexture)));
    boxBg.setSize(size, size);
    stack.add(boxBg);

    Image itemImage = new Image(new TextureRegionDrawable(item.inventoryAnimation().update()));

    Table container = new Table();
    float padding = size * ITEM_BOX_PADDING_RATIO;
    itemImage.setSize(size - padding * 2, size - padding * 2);
    container.add(itemImage).size(size - padding * 2).pad(padding);
    stack.add(container);

    stack.addListener(createTooltipListener(item));

    return stack;
  }

  private ClickListener createTooltipListener(Item item) {
    return new ClickListener() {
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
    };
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

    float tooltipX = calculateTooltipX(mouseX, width);
    float tooltipY = calculateTooltipY(mouseY, height);

    batch.draw(tooltipBackground, tooltipX, tooltipY, width, height);

    tooltipFont.setColor(Color.BLACK);
    tooltipFont.draw(batch, title, tooltipX + BORDER_PADDING, tooltipY + height - BORDER_PADDING);
    tooltipFont.setColor(new Color(0x000000b0));
    tooltipFont.draw(
        batch,
        description,
        tooltipX + BORDER_PADDING,
        tooltipY + height - BORDER_PADDING - layoutName.height - LINE_GAP);
  }

  private float calculateTooltipX(float mouseX, float width) {
    float tooltipX = mouseX + TOOLTIP_OFFSET;
    if (tooltipX + width > Gdx.graphics.getWidth()) {
      tooltipX = mouseX - width - TOOLTIP_OFFSET;
    }
    return tooltipX;
  }

  private float calculateTooltipY(float mouseY, float height) {
    float tooltipY = mouseY + TOOLTIP_OFFSET;
    if (tooltipY + height > Gdx.graphics.getHeight()) {
      tooltipY = mouseY - height - TOOLTIP_OFFSET;
    }
    return tooltipY;
  }

  /** Disposes of textures and resources created by this UI component. */
  @Override
  public void dispose() {
    if (windowRefreshListener != null) {
      WindowEventManager.unregisterWindowRefreshListener(windowRefreshListener);
    }

    // Dispose of instance-level resources
    if (boxTexture != null) {
      boxTexture.dispose();
    }
    if (resultBoxTexture != null) {
      resultBoxTexture.dispose();
    }
  }

  /**
   * Represents a single entry in the crafting book.
   *
   * <p>Can either contain a recipe or an empty message for display purposes.
   */
  private static class RecipeEntry {
    private final Recipe recipe;
    private final String message;

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
