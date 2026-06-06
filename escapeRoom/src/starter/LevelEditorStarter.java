package starter;

import contrib.systems.LevelEditorSystem;
import contrib.utils.components.Debugger;
import core.Entity;
import core.Game;
import core.components.CameraComponent;
import core.components.PositionComponent;
import core.level.Tile;
import core.level.loader.DungeonLoader;
import core.network.config.NetworkConfig;
import core.systems.CameraSystem;
import core.utils.Direction;
import core.utils.Point;
import core.utils.Tuple;
import core.utils.components.path.SimpleIPath;
import demoDungeon.level.MADungeonRoom;
import java.io.IOException;
import network.EscapeRoomSnapshotTranslator;
import replay.ReplayFreeCameraSystem;

/** Starter for editing Escape Room levels with debug tools and a free camera. */
public class LevelEditorStarter {
  private static final int START_LEVEL = 0;

  /**
   * Main method to start the level editor.
   *
   * @param args command line arguments
   * @throws IOException if the game configuration cannot be loaded
   */
  public static void main(String[] args) throws IOException {
    configGame();
    onSetup();

    NetworkConfig.SNAPSHOT_TRANSLATOR = new EscapeRoomSnapshotTranslator();
    MASinglePlayer.registerItems();

    Game.windowTitle("Escape Room Level Editor");
    Game.run();
  }

  private static void onSetup() {
    Game.userOnSetup(
        () -> {
          DungeonLoader.addLevel(Tuple.of("maroom", MADungeonRoom.class));
          Game.add(new LevelEditorSystem());
          Game.add(new Debugger());
          DungeonLoader.loadLevel(START_LEVEL);

          Point cameraStart = startPosition();
          createFreeCamera(cameraStart);
          LevelEditorSystem.active(true);
          Game.system(CameraSystem.class, cameraSystem -> cameraSystem.instantFocus(cameraStart));
        });
  }

  private static Point startPosition() {
    return Game.startTile().map(Tile::position).orElse(new Point(0, 0));
  }

  private static void createFreeCamera(Point position) {
    Entity camera = Entity.createLocalEntity("level_editor_free_camera");
    camera.add(new CameraComponent());
    camera.add(new PositionComponent(position, Direction.DOWN));
    Game.add(camera);
    Game.add(new ReplayFreeCameraSystem(camera));
  }

  private static void configGame() throws IOException {
    Game.loadConfig(
        new SimpleIPath("dungeon_config.json"),
        contrib.configuration.KeyboardConfig.class,
        core.configuration.KeyboardConfig.class);
    Game.disableAudio(true);
    Game.tickRate(30);
  }
}
