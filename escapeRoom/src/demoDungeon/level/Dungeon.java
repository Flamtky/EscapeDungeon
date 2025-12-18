package demoDungeon.level;

import core.level.DungeonLevel;
import core.level.utils.DesignLabel;
import core.level.utils.LevelElement;
import core.utils.Point;
import java.util.*;

/**
 * The Demolevel.
 *
 * <p>The player has to craft a Healpotion.
 */
public class Dungeon extends DungeonLevel {

  /**
   * Creates a new Demo Level.
   *
   * @param layout The layout of the level.
   * @param designLabel The design label of the level.
   * @param namedPoints The custom points of the level.
   */
  public Dungeon(LevelElement[][] layout, DesignLabel designLabel, Map<String, Point> namedPoints) {
    super(layout, designLabel, namedPoints, "Demo");
  }

  @Override
  protected void onFirstTick() {}

  @Override
  protected void onTick() {}
}
