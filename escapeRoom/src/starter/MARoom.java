package starter;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;
import contrib.crafting.Crafting;
import contrib.entities.CharacterClass;
import contrib.entities.EntityFactory;
import contrib.modules.levelHide.LevelHideSystem;
import contrib.systems.*;
import contrib.utils.components.Debugger;
import core.Entity;
import core.Game;
import core.level.loader.DungeonLoader;
import core.utils.Tuple;
import core.utils.components.path.SimpleIPath;
import demoDungeon.level.Dungeon;
import guard.GuardDetectionSystem;
import hint.HintLogComponent;
import java.io.IOException;

/**
 * Starter for the Demo Escaperoom Dungeon.
 *
 * <p>Usage: run with the Gradle task {@code runMA}.
 */
public class MARoom {
  private static final boolean DEBUG_MODE = true;
  private static final String BACKGROUND_MUSIC = "sounds/background.wav";
  private static final int START_LEVEL = 0;

  /**
   * Main method to start the game.
   *
   * @param args The arguments passed to the game.
   * @throws IOException If an I/O error occurs.
   */
  public static void main(String[] args) throws IOException {
    configGame();
    onSetup();

    Game.windowTitle("Demo-Room");
    Game.run();
  }

  private static void onSetup() {
    Game.userOnSetup(
        () -> {
          setupMusic();
          DungeonLoader.addLevel(Tuple.of("dungeon", Dungeon.class));
          createSystems();
          Crafting.loadRecipes();
          createHero();
          DungeonLoader.loadLevel(START_LEVEL);
        });
  }

  private static void createHero() {
    Entity hero = EntityFactory.newHero(CharacterClass.ROGUE);
    hero.add(new HintLogComponent());
    Game.add(hero);
  }

  private static void configGame() throws IOException {
    Game.loadConfig(
        new SimpleIPath("dungeon_config.json"),
        contrib.configuration.KeyboardConfig.class,
        core.configuration.KeyboardConfig.class);
    Game.disableAudio(true);
    Game.frameRate(30);
  }

  private static void createSystems() {
    if (DEBUG_MODE) Game.add(new LevelEditorSystem());
    Game.add(new LevelHideSystem());
    Game.add(new CollisionSystem());
    Game.add(new ManaRestoreSystem());
    if (!DEBUG_MODE) Game.add(new StaminaDrainSystem());
    Game.add(new StaminaExhaustionSystem());
    Game.add(new DebugSleepSystem());
    Game.add(new AISystem());
    Game.add(new ProjectileSystem());
    Game.add(new HealthSystem());
    Game.add(new SpikeSystem());
    if (!DEBUG_MODE) Game.add(new FallingSystem());
    Game.add(new PathSystem());
    Game.add(new PitSystem());
    Game.add(new LeverSystem());
    Game.add(new PressurePlateSystem());
    Game.add(new IdleSoundSystem());
    Game.add(new GuardDetectionSystem());
    Game.add(new AttachmentSystem());
    Game.add(new IllegalSystem());
    Game.add(new BedSleepSystem());
    if (DEBUG_MODE) Game.add(new Debugger());
  }

  private static void setupMusic() {
    Music backgroundMusic = Gdx.audio.newMusic(Gdx.files.internal(BACKGROUND_MUSIC));
    backgroundMusic.setLooping(true);
    backgroundMusic.play();
    backgroundMusic.setVolume(.05f);
  }
}
