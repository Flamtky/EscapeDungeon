package demoDungeon.level;

import contrib.components.CatapultableComponent;
import contrib.components.CollideComponent;
import contrib.configuration.KeyboardConfig;
import core.Entity;
import core.Game;
import core.components.InputComponent;
import core.components.PositionComponent;
import core.components.VelocityComponent;
import core.level.DungeonLevel;
import core.level.Tile;
import core.level.utils.*;
import core.utils.MissingPlayerException;
import core.utils.Point;
import core.utils.Vector2;

import java.util.*;
import java.util.function.Consumer;

/**
 * The Demolevel.
 *
 * <p>The player has to craft a Healpotion.
 */
public class Dungeon extends DungeonLevel {

  private final Set<Tile> updatedTiles = new HashSet<>();

  /**
   * Creates a new Demo Level.
   *
   * @param layout The layout of the level.
   * @param designLabel The design label of the level.
   * @param namedPoints The custom points of the level.
   */
  public Dungeon(LevelElement[][] layout, DesignLabel designLabel, Map<String, Point> namedPoints) {
    super(layout, designLabel, namedPoints, "Demo");
    changeTileDesignLabel(
        getPoint("beige11").toCoordinate(),
        getPoint("beige12").toCoordinate(),
        DesignLabel.BEIGECASTLE);
    changeTileDesignLabel(
        getPoint("grey11").toCoordinate(),
        getPoint("grey12").toCoordinate(),
        DesignLabel.GREYCASTLE);
    changeTileDesignLabel(
        getPoint("grass11").toCoordinate(), getPoint("grass12").toCoordinate(), DesignLabel.FOREST);
    changeTileDesignLabel(
        getPoint("temple11").toCoordinate(),
        getPoint("temple12").toCoordinate(),
        DesignLabel.TEMPLE);
    changeTileDesignLabel(
        getPoint("forest11").toCoordinate(),
        getPoint("forest12").toCoordinate(),
        DesignLabel.FOREST);
    changeTileDesignLabel(
        getPoint("forest21").toCoordinate(),
        getPoint("forest22").toCoordinate(),
        DesignLabel.FOREST);
    changeTileDesignLabel(
        getPoint("forest21").toCoordinate(),
        getPoint("forest23").toCoordinate(),
        DesignLabel.FOREST);
    changeTileDesignLabel(
        getPoint("forest23").toCoordinate(),
        getPoint("forest24").toCoordinate(),
        DesignLabel.FOREST);
    changeTileDesignLabel(
        getPoint("outside11").toCoordinate(),
        getPoint("outside12").toCoordinate(),
        DesignLabel.DEFAULT);
    changeTileDesignLabel(
        getPoint("outside11").toCoordinate(),
        getPoint("outside13").toCoordinate(),
        DesignLabel.DEFAULT);
    changeTileDesignLabel(
        getPoint("outside13").toCoordinate(),
        getPoint("outside14").toCoordinate(),
        DesignLabel.DEFAULT);

    refreshLevelTextures();
  }

  @Override
  protected void onFirstTick() {
    changeIceTiles(
      getPoint("fire11").toCoordinate(), getPoint("fire12").toCoordinate(), DesignLabel.ICE);
      refreshLevelTextures();
  }

  @Override
  protected void onTick() {
    Entity hero = Game.player().orElseThrow(MissingPlayerException::new);
    PositionComponent pc = hero.fetch(PositionComponent.class).get();
    Point currentPos = pc.position().translate(Vector2.of(1, 0.5));
    VelocityComponent vc = hero.fetch(VelocityComponent.class).get();
    CatapultableComponent catapultableComponent = hero.fetch(CatapultableComponent.class).get();
    Tile currentTile = Game.tileAt(currentPos).get();

    if (currentTile.designLabel() == DesignLabel.ICE) {
      Tile tileInFront = Game.tileAt(currentPos.translate(pc.viewDirection())).get();
      vc.onWallHit((self) -> {
        vc.currentVelocity(Vector2.ZERO);
        catapultableComponent.reactivate().accept(hero);
      });
      if(!tileInFront.levelElement().value()) {
        vc.currentVelocity(Vector2.ZERO);
        catapultableComponent.reactivate().accept(hero);
      }
      else {
        vc.currentVelocity(pc.viewDirection().scale(vc.maxSpeed()));
        catapultableComponent.deactivate().accept(hero);
      }
    }
    else {
      catapultableComponent.reactivate().accept(hero);
      vc.onWallHit(e -> {});
    }

  }

  private void changeTileDesignLabel(Coordinate a, Coordinate b, DesignLabel newDesignLabel) {
    int minX = Math.min(a.x(), b.x());
    int maxX = Math.max(a.x(), b.x());
    int minY = Math.min(a.y(), b.y());
    int maxY = Math.max(a.y(), b.y());

    for (int y = minY; y <= maxY; y++) {
      for (int x = minX; x <= maxX; x++) {
        layout[y][x].designLabel(newDesignLabel);
        updatedTiles.add(layout[y][x]);
      }
    }
  }
  private void changeIceTiles(Coordinate a, Coordinate b, DesignLabel newDesignLabel) {
    int minX = Math.min(a.x(), b.x());
    int maxX = Math.max(a.x(), b.x());
    int minY = Math.min(a.y(), b.y());
    int maxY = Math.max(a.y(), b.y());

    for (int y = minY; y <= maxY; y++) {
      for (int x = minX; x <= maxX; x++) {
        layout[y][x].designLabel(newDesignLabel);
        layout[y][x].tintColor(-1);
        layout[y][x].friction(0);
        updatedTiles.add(layout[y][x]);
      }
    }
  }
}
