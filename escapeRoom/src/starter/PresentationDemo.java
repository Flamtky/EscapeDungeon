package starter;

import contrib.entities.CharacterClass;
import contrib.entities.EntityFactory;
import contrib.systems.CollisionSystem;
import core.Entity;
import core.Game;
import core.level.loader.DungeonLoader;
import core.utils.Tuple;
import core.utils.components.path.SimpleIPath;
import java.io.IOException;
import presentation.PresentationDemoLevel;

/**
 * Starter for the in-game presentation UI demo.
 *
 * <p>Usage: run with the Gradle task {@code runPresentationDemo}.
 */
public class PresentationDemo {
  private static final int START_LEVEL = 0;

  /**
   * Main method to start the presentation demo.
   *
   * @param args The arguments passed to the game.
   * @throws IOException If an I/O error occurs while loading configuration.
   */
  public static void main(String[] args) throws IOException {
    configGame();
    onSetup();

    Game.windowTitle("Presentation Demo");
    MASinglePlayer.registerItems();
    Game.run();
  }

  private static void onSetup() {
    Game.userOnSetup(
        () -> {
          DungeonLoader.addLevel(Tuple.of("presentation", PresentationDemoLevel.class));
          Game.add(new CollisionSystem());
          createHero();
          DungeonLoader.loadLevel(START_LEVEL);
        });
  }

  private static void createHero() {
    Entity hero = EntityFactory.newHero(CharacterClass.WIZARD);
    Game.add(hero);
  }

  private static void configGame() throws IOException {
    Game.loadConfig(
        new SimpleIPath("dungeon_config.json"),
        contrib.configuration.KeyboardConfig.class,
        core.configuration.KeyboardConfig.class);
    Game.disableAudio(true);
    Game.tickRate(60);
    Game.maxFPS(0);
  }
}
