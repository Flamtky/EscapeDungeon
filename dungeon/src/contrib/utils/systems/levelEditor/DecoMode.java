package contrib.utils.systems.levelEditor;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import contrib.components.CollideComponent;
import contrib.components.DecoComponent;
import contrib.entities.deco.Deco;
import contrib.entities.deco.DecoFactory;
import contrib.systems.PositionSync;
import contrib.utils.EntityUtils;
import core.Entity;
import core.Game;
import core.components.DrawComponent;
import core.components.PositionComponent;
import core.level.DungeonLevel;
import core.systems.InputManager;
import core.utils.Point;
import core.utils.Rectangle;
import core.utils.Vector2;
import java.util.*;

/** Deco Mode for the Level Editor. Allows placing, removing, and moving decorative entities. */
public class DecoMode extends LevelEditorMode {

  private static final float HOVER_DISTANCE = 0.75f;
  private static final float PREVIEW_ALPHA = 0.5f;

  private static Deco.Category currentCategory = Deco.Category.values()[0];
  private static Deco[] currentCategoryDecos = Deco.byCategory(currentCategory);
  private static int selectedDecoIndex = 0;
  private static SnapMode decoSnapMode = SnapMode.OnGrid;
  private static DecoEntityData decoPreviewEntity = null;
  private static DecoEntityData decoHeldEntity = null;
  private static DecoEntityData decoHoveredEntity = null;
  // When true, ignore snap mode's blocked check and allow placement anywhere
  private static boolean ignoreBlockedCheck = false;

  private boolean rapidFireActive = false;

  /** Constructs the Deco Mode. */
  public DecoMode() {
    super("Deco Mode");
  }

  @Override
  public void execute() {
    // Toggle ignore-blocked-check key
    if (InputManager.isButtonJustPressed(SIXTH)) {
      ignoreBlockedCheck = !ignoreBlockedCheck;
    }

    // Change category
    if (InputManager.isButtonJustPressed(SECONDARY_DOWN)) {
      int idx = currentCategory.ordinal() - 1;
      if (idx < 0) idx = Deco.Category.values().length - 1;
      currentCategory = Deco.Category.values()[idx];
      currentCategoryDecos = Deco.byCategory(currentCategory);
      selectedDecoIndex = 0;
      previewEntityChanged();
    } else if (InputManager.isButtonJustPressed(SECONDARY_UP)) {
      int idx = (currentCategory.ordinal() + 1) % Deco.Category.values().length;
      currentCategory = Deco.Category.values()[idx];
      currentCategoryDecos = Deco.byCategory(currentCategory);
      selectedDecoIndex = 0;
      previewEntityChanged();
    }

    // Change selected deco within category
    if (InputManager.isButtonJustPressed(PRIMARY_UP)) {
      if (currentCategoryDecos.length > 0) {
        selectedDecoIndex = Math.floorMod(selectedDecoIndex + 1, currentCategoryDecos.length);
        previewEntityChanged();
      }
    } else if (InputManager.isButtonJustPressed(PRIMARY_DOWN)) {
      if (currentCategoryDecos.length > 0) {
        selectedDecoIndex = Math.floorMod(selectedDecoIndex - 1, currentCategoryDecos.length);
        previewEntityChanged();
      }
    }

    // Change snap mode
    if (InputManager.isButtonJustPressed(FIFTH)) {
      decoSnapMode = decoSnapMode.nextMode();
    }

    // Mouse interactions:
    // - LMB on deco: pickup deco
    // - LMB anywhere [holding a deco]: place deco
    // - LMB anywhere [not holding a deco]: place new instance of deco
    // - RMB on deco: remove deco
    // - Mouse move [holding a deco]: show preview of deco at cursor position
    Point cursorPos = getCursorPosition();
    Point snapPos = decoSnapMode.getPosition(cursorPos);
    if (InputManager.isButtonJustPressed(Input.Buttons.LEFT)) {
      rapidFireActive = true;

      if (decoHeldEntity != null) {
        // Place held deco
        setPosition(decoHeldEntity.entity, snapPos);
        decoHeldEntity = null;
        setupPreviewEntity(snapPos);
        rapidFireActive = false;
      }
    } else if (InputManager.isButtonJustPressed(Input.Buttons.RIGHT) && decoHeldEntity == null) {
      rapidFireActive = false;
      // Pickup deco on cursor
      Optional<DecoEntityData> clickedDeco = getDecoOnPosition(cursorPos);
      if (clickedDeco.isPresent()) {
        decoHeldEntity = clickedDeco.get();
        removePreviewEntity();
      }
    } else if (InputManager.isButtonPressed(TERTIARY)) {
      rapidFireActive = false;
      // Delete deco on cursor
      getDecoOnPosition(cursorPos).map(DecoEntityData::entity).ifPresent(Game::remove);
      syncPlacedDecos();
    } else if (InputManager.isButtonJustPressed(QUARTERNARY)) {
      rapidFireActive = false;
      // Pipette tool to pick deco type on cursor
      Optional<DecoEntityData> clickedDeco = getDecoOnPosition(cursorPos);
      if (clickedDeco.isPresent()) {
        DecoComponent dc = clickedDeco.get().dc;
        Deco pickedDeco = dc.type();
        // Switch to the category of the picked deco
        currentCategory = pickedDeco.category();
        currentCategoryDecos = Deco.byCategory(currentCategory);
        // Find the index within the category
        for (int i = 0; i < currentCategoryDecos.length; i++) {
          if (currentCategoryDecos[i] == pickedDeco) {
            selectedDecoIndex = i;
            previewEntityChanged();
            break;
          }
        }
      }
    }

    if (InputManager.isButtonPressed(Input.Buttons.LEFT) && rapidFireActive) {
      boolean checkBlocked = decoSnapMode.checkBlocked() && !ignoreBlockedCheck;
      placeDeco(snapPos, checkBlocked);
      if (!checkBlocked) {
        rapidFireActive = false;
      }
    }

    // Update hovered entity
    updateHoveredEntity(cursorPos);

    // Update preview entity position
    if (decoHeldEntity != null) {
      setPosition(decoHeldEntity.entity, snapPos);
      return;
    }

    // No held entity, show preview entity (if available)
    if (decoPreviewEntity != null) {
      setPosition(decoPreviewEntity.entity, snapPos);
    }
  }

  /**
   * Place a new deco at the given position. If there is already a deco at that position, do
   * nothing.
   *
   * @param snapPos the snapped position for placement
   * @param checkBlocked whether to check for existing decos at the position
   */
  private void placeDeco(Point snapPos, boolean checkBlocked) {
    if (decoPreviewEntity == null) return;
    if (checkBlocked && decoOverlapsAny(decoPreviewEntity).isPresent()) {
      return;
    }

    Deco decoType = getSelectedDeco();
    if (decoType == null) return;

    Vector2 offset = getEntityOffset(decoPreviewEntity.entity);
    Point actualPos = snapPos.translate(offset.scale(-1));

    Entity newDeco = DecoFactory.createDeco(actualPos, decoType);
    Game.add(newDeco);
    syncPlacedDecos();
  }

  @Override
  public void onEnter() {
    setupPreviewEntity(new Point(0, 0));
  }

  @Override
  public void onExit() {
    removePreviewEntity();
  }

  @Override
  public String getStatusText() {
    StringBuilder status = new StringBuilder();
    int entityCount = (int) Game.levelEntities(Set.of(DecoComponent.class)).count();
    status.append("Entities: ").append(entityCount);
    status
        .append("\nKategorie [A/D]: ")
        .append(currentCategory.displayName())
        .append(" (")
        .append(currentCategory.ordinal() + 1)
        .append("/")
        .append(Deco.Category.values().length)
        .append(")");
    Deco currentDeco = getSelectedDeco();
    String decoName = currentDeco != null ? currentDeco.name() : "---";
    status
        .append("\nDeco [Q/E]: ")
        .append(decoName)
        .append(" (")
        .append(currentCategoryDecos.length > 0 ? selectedDecoIndex + 1 : 0)
        .append("/")
        .append(currentCategoryDecos.length)
        .append(")");
    status.append("\nSnap Mode: ").append(decoSnapMode.name());
    status.append(" (Ignore-blocked: ").append(ignoreBlockedCheck ? "ON" : "OFF").append(")");
    return status.toString();
  }

  @Override
  public Map<Integer, String> getControls() {
    Map<Integer, String> controls = new LinkedHashMap<>();
    controls.put(SECONDARY_DOWN, "Vorherige Kategorie");
    controls.put(SECONDARY_UP, "Nächste Kategorie");
    controls.put(PRIMARY_UP, "Nächstes Deco");
    controls.put(PRIMARY_DOWN, "Vorheriges Deco");
    controls.put(TERTIARY, "Löschen auf Cursor");
    controls.put(QUARTERNARY, "Pipette (vom Cursor)");
    controls.put(FIFTH, "Grid Snap ändern");
    controls.put(SIXTH, "Toggle Block-Check Override");
    controls.put(Input.Buttons.LEFT, "Deco platzieren");
    controls.put(Input.Buttons.RIGHT, "Deco aufnehmen");
    return controls;
  }

  private void updateHoveredEntity(Point cursorPos) {
    Optional<DecoEntityData> hoveredDeco = getDecoOnPosition(cursorPos);

    boolean isHovering = hoveredDeco.isPresent();
    boolean sameEntity =
        decoHoveredEntity != null
            && hoveredDeco.isPresent()
            && decoHoveredEntity.entity.equals(hoveredDeco.get().entity);
    boolean wasHovering = decoHoveredEntity != null;

    // Not hovering, clear hovered
    if ((!isHovering && wasHovering) || (isHovering && wasHovering && !sameEntity)) {
      setEntityColor(decoHoveredEntity.entity, Color.WHITE);
      decoHoveredEntity = null;
    }
    if (isHovering && !wasHovering || (isHovering && !sameEntity)) {
      decoHoveredEntity = hoveredDeco.get();
      setEntityColor(decoHoveredEntity.entity, Color.YELLOW);
    }
  }

  private void setEntityColor(Entity entity, Color color) {
    entity
        .fetch(DrawComponent.class)
        .ifPresent(
            dc -> {
              dc.tintColor(Color.rgba8888(color));
            });
  }

  /**
   * Returns the currently selected Deco from the current category.
   *
   * @return the selected Deco, or null if the category is empty
   */
  private Deco getSelectedDeco() {
    if (currentCategoryDecos.length == 0) return null;
    return currentCategoryDecos[selectedDecoIndex];
  }

  private void setupPreviewEntity(Point pos) {
    Deco selectedDeco = getSelectedDeco();
    if (selectedDeco == null) return;

    Entity deco = DecoFactory.createDeco(pos, selectedDeco);
    deco.fetch(DrawComponent.class)
        .ifPresent(
            dc -> {
              dc.tintColor(Color.rgba8888(1, 1, 1, PREVIEW_ALPHA));
            });
    deco.fetch(CollideComponent.class)
        .ifPresent(
            dc -> {
              dc.isSolid(false);
            });
    Game.add(deco);
    decoPreviewEntity = DecoEntityData.of(deco);
  }

  private void removePreviewEntity() {
    if (decoPreviewEntity == null) return;
    Game.remove(decoPreviewEntity.entity);
    decoPreviewEntity = null;
  }

  private void previewEntityChanged() {
    Point currentPos =
        decoPreviewEntity != null ? decoPreviewEntity.pc.position() : getCursorPosition();
    removePreviewEntity();
    setupPreviewEntity(currentPos);
  }

  private void setPosition(Entity entity, Point position) {
    entity
        .fetch(PositionComponent.class)
        .ifPresent(
            pc -> {
              Vector2 offset = getEntityOffset(entity);
              pc.position(position.translate(offset.scale(-1)));
              PositionSync.syncPosition(entity);
            });
  }

  /**
   * Get the offset of the entity based on its CollideComponent size. If smaller than 1.0f in any
   * dimension, center it within the tile. Otherwise, the bottom-left corner of the collider is
   * used.
   *
   * @param entity The entity to get the offset for
   * @return The offset vector
   */
  private Vector2 getEntityOffset(Entity entity) {
    return entity
        .fetch(CollideComponent.class)
        .map(
            cc -> {
              float x = 0, y = 0;
              if (cc.collider().size().x() < 1.0f) {
                x = -0.5f + cc.collider().size().x() / 2.0f;
              }
              if (cc.collider().size().y() < 1.0f) {
                y = -0.5f + cc.collider().size().y() / 2.0f;
              }
              return cc.collider().offset().add(Vector2.of(x, y));
            })
        .orElse(Vector2.ZERO);
  }

  private Optional<DecoEntityData> getDecoOnPosition(Point position) {
    return getSystem()
        .filteredEntityStream(DecoComponent.class)
        .map(DecoEntityData::of)
        .filter(
            ded ->
                !ded.equals(decoPreviewEntity)
                    && EntityUtils.getPosition(ded.entity).distance(position) < HOVER_DISTANCE)
        .findFirst();
  }

  /**
   * Get the first deco entity found on the exact given position.
   *
   * @param data the deco entity data to check against
   * @return an optional containing the found deco entity data, or empty if none found
   */
  private Optional<DecoEntityData> decoOverlapsAny(DecoEntityData data) {
    return getSystem()
        .filteredEntityStream(DecoComponent.class)
        .map(DecoEntityData::of)
        .filter(ded -> !ded.equals(decoPreviewEntity) && entitiesCollide(data, ded))
        .findFirst();
  }

  private boolean entitiesCollide(DecoEntityData ded1, DecoEntityData ded2) {
    return getEntityBounds(ded1).intersects(getEntityBounds(ded2));
  }

  private Rectangle getEntityBounds(DecoEntityData ded) {
    if (ded.cc != null) {
      return ded.cc.collider().absoluteBounds();
    }
    Point entityPos = ded.pc.position();
    Vector2 size = ded.drawComp.size();
    return new Rectangle(size, Vector2.of(entityPos));
  }

  /** Puts all placed decos into the level handler object for serialization. */
  private void syncPlacedDecos() {
    DungeonLevel level = getLevel();
    level.decorations().clear();
    Game.levelEntities(Set.of(DecoComponent.class))
        .map(DecoEntityData::of)
        .forEach(
            ded -> {
              // Filter out preview and held entities
              if (Objects.equals(ded, decoPreviewEntity) || Objects.equals(ded, decoHeldEntity)) {
                return;
              }
              Point pos = ded.pc.position();
              Deco decoType = ded.dc.type();
              level.addDecoration(decoType, pos);
            });
  }

  private record DecoEntityData(
      Entity entity,
      DecoComponent dc,
      PositionComponent pc,
      DrawComponent drawComp,
      CollideComponent cc) {
    public static DecoEntityData of(Entity entity) {
      return new DecoEntityData(
          entity,
          entity.fetch(DecoComponent.class).orElseThrow(),
          entity.fetch(PositionComponent.class).orElseThrow(),
          entity.fetch(DrawComponent.class).orElseThrow(),
          entity.fetch(CollideComponent.class).orElse(null));
    }
  }
}
