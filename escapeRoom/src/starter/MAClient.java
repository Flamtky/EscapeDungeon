package starter;

import contrib.hud.dialogs.DialogContext;
import contrib.hud.dialogs.DialogFactory;
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
import mushRoom.modules.EscapeRoomDialogTypes;
import network.EscapeRoomSnapshotTranslator;
import tools.timer.*;

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
    MASinglePlayer.registerItems();
    PreRunConfiguration.username("APPRENTICE");
    //PreRunConfiguration.username("ROGUE");

    // Game Settings
    Game.loadConfig(new SimpleIPath("dungeon_config.json"), KeyboardConfig.class);
    Game.disableAudio(false);
    Game.frameRate(90);
    Game.windowTitle("Prison Escape - " + PreRunConfiguration.username());
    Game.userOnSetup(
        () -> {
          Game.add(new Debugger());
          registerTimerHandlers();
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

  /** Registers network message handlers for timer synchronization. */
  private static void registerTimerHandlers() {
    var dispatcher = Game.network().messageDispatcher();

    // Handle timer command messages (START, STOP, RESUME)
    dispatcher.registerHandler(
        TimerCommandMessage.class,
        (session, msg) -> {
          System.out.println("Received TimerCommandMessage: " + msg.command());
          switch (msg.command()) {
            case START:
              // Create timer dialog if it doesn't exist
              DialogContext ctx =
                  DialogContext.builder()
                      .type(EscapeRoomDialogTypes.TIMER)
                      .put("startTimeSeconds", msg.startTimeSeconds())
                      .build();
              DialogFactory.show(ctx, false, false);

              TimerUI ui = TimerDialog.currentUI();
              if (ui != null) {
                ui.start(msg.startTimeSeconds());
              }
              break;
            case STOP:
              TimerUI stopUi = TimerDialog.currentUI();
              if (stopUi != null) {
                stopUi.stop();
              }
              break;
            case RESUME:
              TimerUI resumeUi = TimerDialog.currentUI();
              if (resumeUi != null) {
                resumeUi.resume();
              }
              break;
          }
        });

    // Handle periodic sync messages
    dispatcher.registerHandler(
        TimerSyncMessage.class,
        (session, msg) -> {
          TimerUI ui = TimerDialog.currentUI();
          if (ui != null) {
            ui.syncFromServer(msg.elapsedSeconds(), msg.running());
          } else {
            var command =
                msg.running()
                    ? TimerCommandMessage.TimerCommand.START
                    : TimerCommandMessage.TimerCommand.STOP;
            dispatcher.dispatch(session, new TimerCommandMessage(command, msg.elapsedSeconds()));
          }
        });
  }
}
