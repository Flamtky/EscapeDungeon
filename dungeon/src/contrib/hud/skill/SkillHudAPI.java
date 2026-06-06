package contrib.hud.skill;

import contrib.systems.SkillHudSystem;
import core.Game;
import core.utils.logging.DungeonLogger;

/**
 * Public API for managing the skill HUD in the escape room.
 *
 * <p>Provides utility methods to control the skill HUD display. The HUD lifecycle is automatically
 * managed by {@link SkillHudSystem}, which creates and removes HUDs based on player entity
 * presence.
 *
 * <p>Example usage:
 *
 * <pre>{@code
 * // Hide the skill HUD temporarily
 * SkillHudAPI.setVisible(false);
 *
 * // Show it again
 * SkillHudAPI.setVisible(true);
 *
 * // Check if visible
 * boolean visible = SkillHudAPI.isVisible();
 * }</pre>
 */
public final class SkillHudAPI {

  private static final DungeonLogger LOGGER = DungeonLogger.getLogger(SkillHudAPI.class);

  private SkillHudAPI() {
    // Utility class
  }

  /**
   * Hides the skill HUD and removes it from the display.
   *
   * <p>Note: The {@link SkillHudSystem} may recreate the HUD if the player entity still exists with
   * the required components. Use {@link #setVisible(boolean)} for temporary hiding.
   */
  public static void hide() {
    getSystem()
        .ifPresent(
            system -> {
              SkillHudUI ui = system.currentUI();
              if (ui != null) {
                ui.remove();
                LOGGER.info("Skill HUD hidden via API");
              }
            });
  }

  /**
   * Sets the visibility of the skill HUD without removing it.
   *
   * <p>This controls only the visual display without affecting the tracked player.
   *
   * @param visible true to show the HUD, false to hide it
   */
  public static void setVisible(boolean visible) {
    getSystem().ifPresent(system -> system.setVisible(visible));
  }

  /**
   * Checks if the skill HUD is currently visible.
   *
   * @return true if the skill HUD is shown, false otherwise
   */
  public static boolean isVisible() {
    return getSystem().map(system -> system.currentUI() != null).orElse(false);
  }

  /**
   * Returns the SkillHudSystem if registered.
   *
   * @return an Optional containing the system, or empty if not registered
   */
  private static java.util.Optional<SkillHudSystem> getSystem() {
    return Game.systems().values().stream()
        .filter(SkillHudSystem.class::isInstance)
        .map(SkillHudSystem.class::cast)
        .findFirst();
  }
}
