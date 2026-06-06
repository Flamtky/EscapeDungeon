package starter;

import contrib.configuration.KeyboardConfig;
import contrib.modules.levelHide.LevelHideSystem;
import contrib.utils.components.Debugger;
import core.Entity;
import core.Game;
import core.components.CameraComponent;
import core.components.PositionComponent;
import core.level.Tile;
import core.level.loader.DungeonLoader;
import core.network.config.NetworkConfig;
import core.utils.Direction;
import core.utils.Point;
import core.utils.Tuple;
import core.utils.components.path.SimpleIPath;
import demoDungeon.level.MADungeonRoom;
import java.io.IOException;
import network.EscapeRoomSnapshotTranslator;
import replay.ReplayControlSystem;
import replay.ReplayFreeCameraSystem;

/** Starter for replaying exported MA xAPI move data without a database connection. */
public class MAReplay {
  private static final int START_LEVEL = 0;

  /**
   * Main method to start the replay.
   *
   * @param args command line arguments
   * @throws IOException if the game configuration cannot be loaded
   */
  public static void main(String[] args) throws IOException {
    configGame();
    onSetup();

    NetworkConfig.SNAPSHOT_TRANSLATOR = new EscapeRoomSnapshotTranslator();
    MASinglePlayer.registerItems();

    Game.windowTitle("MA Replay");
    Game.run();
  }

  private static void onSetup() {
    Game.userOnSetup(
        () -> {
          DungeonLoader.addLevel(Tuple.of("maroom", MADungeonRoom.class));
          Game.add(new LevelHideSystem());
          Game.add(new Debugger());
          DungeonLoader.loadLevel(START_LEVEL);
          Entity freeCamera = createFreeCamera();
          Game.add(new ReplayControlSystem(freeCamera));
        });
  }

  private static Entity createFreeCamera() {
    Point cameraStart = Game.startTile().map(Tile::position).orElse(new Point(0, 0));
    Entity camera = Entity.createLocalEntity("replay_free_camera");
    camera.add(new CameraComponent());
    camera.add(new PositionComponent(cameraStart, Direction.DOWN));
    Game.add(camera);
    Game.add(new ReplayFreeCameraSystem(camera));
    return camera;
  }

  private static void configGame() throws IOException {
    Game.loadConfig(
        new SimpleIPath("dungeon_config.json"),
        KeyboardConfig.class,
        core.configuration.KeyboardConfig.class);
    Game.disableAudio(true);
    Game.tickRate(30);
  }
}
