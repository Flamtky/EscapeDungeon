package starter;

import analytics.RiddleAnalysisSystem;
import contrib.entities.CharacterClass;
import contrib.entities.EntityFactory;
import contrib.entities.HeroController;
import contrib.modules.levelHide.LevelHideSystem;
import contrib.systems.*;
import core.Entity;
import core.Game;
import core.game.ECSManagement;
import core.game.GameLoop;
import core.game.PreRunConfiguration;
import core.level.loader.DungeonLoader;
import core.network.config.NetworkConfig;
import core.network.handler.NettyNetworkHandler;
import core.network.messages.s2c.LevelChangeEvent;
import core.systems.FrictionSystem;
import core.systems.LevelSystem;
import core.systems.MoveSystem;
import core.systems.VelocitySystem;
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

          ECSManagement.system(
              LevelSystem.class,
              levelSystem ->
                  levelSystem.onLevelLoad(
                      () -> {
                        GameLoop.onLevelLoad.execute();
                        Game.network().broadcast(LevelChangeEvent.currentLevel(), true);
                      }));

          if (PreRunConfiguration.isNetworkServer()) {
            ServerConsole.start((NettyNetworkHandler) Game.network());
          }
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
    // Game.add(new PositionSystem());
    Game.add(new VelocitySystem());
    Game.add(new FrictionSystem());
    Game.add(new MoveSystem());
    Game.add(new LevelHideSystem());
    Game.add(new CollisionSystem());
    Game.add(new ManaRestoreSystem());
    Game.add(new StaminaDrainSystem());
    Game.add(new StaminaExhaustionSystem());
    Game.add(new DebugSleepSystem());
    Game.add(new AISystem());
    Game.add(new ProjectileSystem());
    Game.add(new HealthSystem());
    Game.add(new SpikeSystem());
    Game.add(new FallingSystem());
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
    Game.add(new RiddleAnalysisSystem());
  }

  private static void onFrame() {
    HeroController.drainAndApplyInputs();
  }
}
