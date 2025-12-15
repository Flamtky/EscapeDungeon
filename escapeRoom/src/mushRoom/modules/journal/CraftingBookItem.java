package mushRoom.modules.journal;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import contrib.components.UIComponent;
import contrib.crafting.Crafting;
import contrib.crafting.Recipe;
import contrib.hud.UIUtils;
import contrib.item.Item;
import core.Entity;
import core.Game;
import core.utils.components.draw.TextureMap;
import core.utils.components.draw.animation.Animation;
import core.utils.components.path.SimpleIPath;
import mushRoom.Sounds;

/** Item representing a crafting book that displays all registered crafting recipes. */
public class CraftingBookItem extends Item {

  private static final SimpleIPath INVENTORY_PATH =
      new SimpleIPath("items/rpg/item_book_blue_lines.png");
  private static final String NO_RECIPES_TEXT = "Keine Rezepte bekannt.";

  /** Constructs a new CraftingBookItem. */
  public CraftingBookItem() {
    super(
        "Rezeptbuch",
        "Ein Buch mit allen bekannten Rezepten.",
        new Animation(INVENTORY_PATH),
        new Animation(INVENTORY_PATH),
        1,
        1);
  }

  @Override
  public void use(final Entity user) {
    Game.player().ifPresent(CraftingBookItem::openCraftingBook);
  }

  /**
   * Opens the crafting book UI for the given player.
   *
   * @param player the entity to open the crafting book for
   */
  public static void openCraftingBook(Entity player) {
    Skin skin = UIUtils.defaultSkin();
    Texture bookTex = TextureMap.instance().textureAt(new SimpleIPath("images/open-book.png"));

    CraftingBookUI bookUI =
        new CraftingBookUI(skin, new TextureRegionDrawable(new TextureRegion(bookTex)));

    // Loop over all registered recipes and add their entries
    if (Crafting.recipes().isEmpty()) {
      bookUI.addEmptyEntry(NO_RECIPES_TEXT);
    } else {
      for (Recipe recipe : Crafting.recipes()) {
        bookUI.addRecipeEntry(recipe);
      }
    }

    player.remove(UIComponent.class);
    player.add(new UIComponent(bookUI, true, true));
    Sounds.OPEN_INVENTORY_SOUND.play();
  }
}

