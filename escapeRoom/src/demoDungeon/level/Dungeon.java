package demoDungeon.level;

import contrib.components.CollideComponent;
import contrib.components.FlyComponent;
import contrib.components.InventoryComponent;
import contrib.utils.EntityUtils;
import core.Entity;
import core.Game;
import core.components.DrawComponent;
import core.components.InputComponent;
import core.components.PositionComponent;
import core.components.VelocityComponent;
import core.configuration.KeyboardConfig;
import core.level.DungeonLevel;
import core.level.Tile;
import core.level.utils.*;
import core.network.messages.c2s.InputMessage;
import core.utils.*;
import core.utils.components.draw.DepthLayer;
import core.utils.components.path.SimpleIPath;
import escapeDungeon.components.IceMovementComponent;
import escapeDungeon.items.IceWallPlacer;
import java.util.*;

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
    createPushPuzzleEntities();
    Entity hero = Game.allPlayers().findFirst().orElseThrow();
    hero.fetch(InventoryComponent.class).ifPresent((ic) -> ic.add(new IceWallPlacer()));
  }

  @Override
  protected void onTick() {
    Game.allPlayers()
        .forEach(
            hero -> {
              if (hero.fetch(IceMovementComponent.class).isEmpty()) {
                iceControls(hero);
              }
            });
  }

  private void createPushPuzzleEntities() {
    listPointsIndexed("snow_Wall")
        .forEach(
            tuple -> {
              Point pos = tuple.a();
              Entity snowWall = new Entity("snow_Wall");
              snowWall.add(new PositionComponent(pos));
              DrawComponent dc =
                  new DrawComponent(new SimpleIPath("dungeon/ice/floor/floor_hole.png"));
              dc.depth(DepthLayer.Player.depth());
              CollideComponent cc =
                  new CollideComponent(Vector2.of(0.05f, 0.05f), Vector2.of(0.9f, 0.9f));
              TriConsumer<Entity, Entity, Direction> onCollideEnter =
                  (self, other, dir) -> {
                    if (other.fetch(InputComponent.class).isPresent()) {
                      Game.remove(self);
                    }
                  };
              cc.collideEnter(onCollideEnter);
              snowWall.add(dc);
              snowWall.add(cc);
              Game.add(snowWall);
            });
  }

  private int directionKey(Direction direction) {
    return switch (direction) {
      case UP -> KeyboardConfig.MOVEMENT_UP.value();
      case DOWN -> KeyboardConfig.MOVEMENT_DOWN.value();
      case LEFT -> KeyboardConfig.MOVEMENT_LEFT.value();
      case RIGHT -> KeyboardConfig.MOVEMENT_RIGHT.value();
      default -> -1;
    };
  }

  private void addCallbacks(InputComponent inputComp) {
    inputComp.registerCallback(
        core.configuration.KeyboardConfig.MOVEMENT_UP.value(),
        (caller) ->
            Game.network().sendInput(new InputMessage(InputMessage.Action.MOVE, Direction.UP)));
    inputComp.registerCallback(
        core.configuration.KeyboardConfig.MOVEMENT_DOWN.value(),
        (caller) ->
            Game.network().sendInput(new InputMessage(InputMessage.Action.MOVE, Direction.DOWN)));
    inputComp.registerCallback(
        core.configuration.KeyboardConfig.MOVEMENT_RIGHT.value(),
        (caller) ->
            Game.network().sendInput(new InputMessage(InputMessage.Action.MOVE, Direction.RIGHT)));
    inputComp.registerCallback(
        core.configuration.KeyboardConfig.MOVEMENT_LEFT.value(),
        (caller) ->
            Game.network().sendInput(new InputMessage(InputMessage.Action.MOVE, Direction.LEFT)));
  }

  private void iceControls(Entity hero) {
    PositionComponent pc = hero.fetch(PositionComponent.class).get();
    Point currentPos = EntityUtils.getPosition(hero);
    VelocityComponent vc = hero.fetch(VelocityComponent.class).get();
    InputComponent ic = hero.fetch(InputComponent.class).get();
    Tile currentTile = Game.tileAt(currentPos).get();

    if (currentTile.designLabel() == DesignLabel.ICE) {
      if (hero.fetch(FlyComponent.class).isEmpty()) {
        hero.add(new FlyComponent());
      }
      Tile tileInFront = Game.tileAt(currentPos.translate(pc.viewDirection())).get();

      vc.onWallHit(
          (self) -> {
            vc.currentVelocity(Vector2.ZERO);
            ic.removeCallback(directionKey(pc.viewDirection()));
            ic.deactivateControls(false);
          });
      if (tileInFront.levelElement().value()) {
        vc.currentVelocity(pc.viewDirection().scale(vc.maxSpeed()));
        addCallbacks(ic);
        ic.deactivateControls(true);
      }
    } else {
      addCallbacks(ic);
      ic.deactivateControls(false);
      vc.onWallHit(e -> {});
      hero.remove(FlyComponent.class);
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
        updatedTiles.add(layout[y][x]);
      }
    }
  }
}
