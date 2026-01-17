package starter;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;
import contrib.crafting.Crafting;
import contrib.entities.CharacterClass;
import contrib.entities.EntityFactory;
import contrib.entities.HeroController;
import contrib.modules.levelHide.LevelHideSystem;
import contrib.systems.*;
import contrib.utils.components.Debugger;
import core.Entity;
import core.Game;
import core.game.ECSManagement;
import core.game.GameLoop;
import core.game.PreRunConfiguration;
import core.level.loader.DungeonLoader;
import core.network.config.NetworkConfig;
import core.network.messages.s2c.LevelChangeEvent;
import core.systems.*;
import core.utils.Tuple;
import core.utils.components.path.SimpleIPath;
import demoDungeon.level.MADungeonRoom;
import guard.GuardDetectionSystem;
import hint.HintLogComponent;
import java.io.IOException;
import network.EscapeRoomSnapshotTranslator;
import tools.timer.TimerSystem;

/**
 * Starter for the Demo Escaperoom Dungeon.
 *
 * <p>Usage: run with the Gradle task {@code runMA}.
 */
public class MAServer {
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

    Game.userOnFrame(MAServer::onFrame);

    PreRunConfiguration.multiplayerEnabled(true);
    PreRunConfiguration.isNetworkServer(true);

    NetworkConfig.SNAPSHOT_TRANSLATOR = new EscapeRoomSnapshotTranslator();

    MASinglePlayer.registerItems();

    // Enable snapshot debugging to analyze network payload sizes
    // SnapshotDebugger.enable();

    Game.windowTitle("Demo-Room");
    Game.run();
  }

  private static void onSetup() {
    Game.userOnSetup(
        () -> {
          DungeonLoader.addLevel(Tuple.of("maroom", MADungeonRoom.class));
          createSystems();
          // createHero();
          Crafting.loadRecipes();

          ECSManagement.system(
              LevelSystem.class,
              levelSystem ->
                  levelSystem.onLevelLoad(
                      () -> {
                        GameLoop.onLevelLoad.execute();
                        Game.network().broadcast(LevelChangeEvent.currentLevel(), true);
                      }));
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
    Game.disableAudio(false);
    Game.frameRate(90);
  }

  private static void createSystems() {
    ECSManagement.add(new PositionSystem());
    ECSManagement.add(new VelocitySystem());
    ECSManagement.add(new FrictionSystem());
    ECSManagement.add(new MoveSystem());
    if (DEBUG_MODE && !Game.isHeadless()) Game.add(new LevelEditorSystem());
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
    Game.add(new TimerSystem());
    if (DEBUG_MODE && !Game.isHeadless()) Game.add(new Debugger());
  }

  private static void setupMusic() {
    Music backgroundMusic = Gdx.audio.newMusic(Gdx.files.internal(BACKGROUND_MUSIC));
    backgroundMusic.setLooping(true);
    backgroundMusic.play();
    backgroundMusic.setVolume(.05f);
  }

  private static void onFrame() {
    HeroController.drainAndApplyInputs();
  }
}
