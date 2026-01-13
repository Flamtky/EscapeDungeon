package core.network.messages.s2c;

import core.level.Tile;
import core.level.elements.tile.DoorTile;
import core.level.utils.Coordinate;
import core.level.utils.DesignLabel;
import core.network.messages.NetworkMessage;
import java.io.Serial;
import java.util.HashMap;
import java.util.Map;

/**
 * Represents the state of a level, including door states and design labels.
 *
 * @param doorStates An array representing the open/closed states of doors in the level.
 * @param designLabels A 2D array representing the design labels of tiles in the level.
 */
public record LevelState(Map<Coordinate, Boolean> doorStates, DesignLabel[][] designLabels)
    implements NetworkMessage {
  @Serial private static final long serialVersionUID = 1L;

  /**
   * Captures the current state of the level, including door states and design labels.
   *
   * @return The current LevelState of the game.
   */
  public static LevelState currentLevelState() {
    return new LevelState(getDoorStates(), getDesignLabels());
  }

  private static Map<Coordinate, Boolean> getDoorStates() {
    var doorTiles = core.Game.currentLevel().get().doorTiles();
    Map<Coordinate, Boolean> doorStates = new HashMap<>();
    for (DoorTile doorTile : doorTiles) {
      doorStates.put(doorTile.coordinate(), doorTile.isOpen());
    }
    return doorStates;
  }

  private static DesignLabel[][] getDesignLabels() {
    var level = core.Game.currentLevel().get();
    int width = level.layout().length;
    int height = level.layout()[0].length;
    DesignLabel[][] designLabels = new DesignLabel[width][height];
    Tile[][] levelLayout = level.layout();
    for (int x = 0; x < width; x++) {
      for (int y = 0; y < height; y++) {
        Tile tile = levelLayout[x][y];
        designLabels[x][y] = tile.designLabel();
      }
    }
    return designLabels;
  }
}
