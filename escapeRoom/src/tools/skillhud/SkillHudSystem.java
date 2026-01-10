package tools.skillhud;

import contrib.components.SkillComponent;
import core.Entity;
import core.Game;
import core.System;
import core.components.PlayerComponent;
import core.game.WindowEventManager;
import core.utils.logging.DungeonLogger;

/**
 * A system that manages the skill HUD display for player entities.
 *
 * <p>This system automatically creates and displays a skill HUD for entities that have both {@link
 * SkillComponent} and {@link PlayerComponent}. The HUD shows the currently selected skill and its
 * cooldown status.
 *
 * <p>The system runs client-side only ({@link AuthoritativeSide#CLIENT}) and renders the HUD
 * directly to the stage, bypassing the DialogFactory/UIComponent sync mechanism for smooth cooldown
 * display.
 */
public final class SkillHudSystem extends System {

  private static final DungeonLogger LOGGER = DungeonLogger.getLogger(SkillHudSystem.class);

  /** The currently displayed skill HUD UI (single local player only). */
  private SkillHudUI currentUI;

  /** The entity ID that the current UI is bound to. */
  private int boundEntityId = -1;

  /** Creates a new SkillHudSystem. */
  public SkillHudSystem() {
    super(AuthoritativeSide.CLIENT, SkillComponent.class, PlayerComponent.class);

    this.onEntityAdd = this::handleEntityAdd;
    this.onEntityRemove = this::handleEntityRemove;

    // Register window resize listener
    if (!Game.isHeadless()) {
      WindowEventManager.registerWindowRefreshListener(this::handleResize);
    }
  }

  /**
   * Handles when a player entity with skills is added.
   *
   * @param entity the entity that was added
   */
  private void handleEntityAdd(Entity entity) {
    if (Game.isHeadless()) {
      return; // No UI on headless server
    }

    // Only create HUD if we don't already have one
    if (currentUI != null) {
      LOGGER.warn(
          "SkillHudSystem already has a UI for entity {}, ignoring new entity {}",
          boundEntityId,
          entity.id());
      return;
    }

    Game.stage()
        .ifPresent(
            stage -> {
              currentUI = SkillHudUI.create(entity.id());
              boundEntityId = entity.id();
              stage.addActor(currentUI);
              LOGGER.info("Created skill HUD for player entity {}", entity.id());
            });
  }

  /**
   * Handles when a player entity is removed.
   *
   * @param entity the entity that was removed
   */
  private void handleEntityRemove(Entity entity) {
    if (currentUI != null && boundEntityId == entity.id()) {
      currentUI.remove();
      currentUI = null;
      boundEntityId = -1;
      LOGGER.info("Removed skill HUD for player entity {}", entity.id());
    }
  }

  /** Handles window resize events. */
  private void handleResize() {
    if (currentUI != null) {
      currentUI.handleResize();
    }
  }

  @Override
  public void execute() {
    // Validate that bound entity still exists and has required components
    if (currentUI != null && boundEntityId != -1) {
      Game.findEntityById(boundEntityId)
          .ifPresentOrElse(
              entity -> {
                // Check if entity still has required components
                if (!entity.isPresent(SkillComponent.class)
                    || !entity.isPresent(PlayerComponent.class)) {
                  LOGGER.warn(
                      "Entity {} no longer has required components, removing skill HUD",
                      boundEntityId);
                  removeCurrentUI();
                }
              },
              () -> {
                // Entity no longer exists
                LOGGER.warn("Entity {} no longer exists, removing skill HUD", boundEntityId);
                removeCurrentUI();
              });
    }
  }

  /** Removes the current UI from the stage. */
  private void removeCurrentUI() {
    if (currentUI != null) {
      currentUI.remove();
      currentUI = null;
      boundEntityId = -1;
    }
  }

  /**
   * Returns the currently displayed SkillHudUI.
   *
   * @return the current UI, or null if none is displayed
   */
  public SkillHudUI currentUI() {
    return currentUI;
  }

  /**
   * Sets the visibility of the skill HUD.
   *
   * @param visible true to show, false to hide
   */
  public void setVisible(boolean visible) {
    if (currentUI != null) {
      currentUI.setVisible(visible);
    }
  }

  /** SkillHudSystem can't be paused - HUD should always be visible. */
  @Override
  public void stop() {
    // Don't stop - keep running
  }
}
