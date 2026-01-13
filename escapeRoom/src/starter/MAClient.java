package starter;

import contrib.utils.components.Debugger;
import core.Game;
import core.configuration.KeyboardConfig;
import core.game.PreRunConfiguration;
import core.level.loader.DungeonLoader;
import core.network.config.NetworkConfig;
import core.utils.Tuple;
import core.utils.components.path.SimpleIPath;
import demoDungeon.level.MADungeonRoomClient;
import java.io.IOException;
import network.EscapeRoomSnapshotTranslator;

/** The main class for the Multiplayer Client for development and testing purposes. */
public final class MAClient {

  private static boolean firstTick = true;

  /**
   * Main method to start the dev client.
   *
   * @param args command line arguments
   * @throws IOException if an I/O error occurs
   */
  public static void main(String[] args) throws IOException {
    // PreRun configuration for multiplayer client
    PreRunConfiguration.multiplayerEnabled(true);
    PreRunConfiguration.isNetworkServer(false);
    PreRunConfiguration.networkServerAddress("127.0.0.1");
    PreRunConfiguration.networkPort(7777);

    NetworkConfig.SNAPSHOT_TRANSLATOR = new EscapeRoomSnapshotTranslator();

    // PreRunConfiguration.username("APPRENTICE");
    PreRunConfiguration.username("APPRENTICE");

    // Game Settings
    Game.loadConfig(new SimpleIPath("dungeon_config.json"), KeyboardConfig.class);
    Game.disableAudio(false);
    Game.frameRate(90);
    Game.windowTitle("Prison Escape - " + PreRunConfiguration.username());
    Game.userOnSetup(
        () -> {
          Game.add(new Debugger());
        });

    Game.userOnFrame(
        () -> {
          if (firstTick) {
            DungeonLoader.addLevel(Tuple.of("maroom", MADungeonRoomClient.class));
            firstTick = false;
          }
        });

    // Start the game
    Game.run();
  }
}
