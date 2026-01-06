package demoDungeon.level;

import com.badlogic.gdx.graphics.Color;
import contrib.components.CollideComponent;
import contrib.components.DecoComponent;
import contrib.components.PressurePlateComponent;
import contrib.components.ProjectileComponent;
import contrib.entities.LeverFactory;
import contrib.entities.deco.Deco;
import contrib.entities.deco.DecoFactory;
import contrib.utils.ICommand;
import core.Entity;
import core.Game;
import core.components.DrawComponent;
import core.components.PositionComponent;
import core.components.VelocityComponent;
import core.level.DungeonLevel;
import core.level.elements.tile.DoorTile;
import core.level.utils.*;
import core.utils.Direction;
import core.utils.Point;
import core.utils.TriConsumer;
import core.utils.Vector2;
import core.utils.components.draw.DepthLayer;
import core.utils.components.path.SimpleIPath;
import java.util.*;
import mushRoom.Sounds;

/**
 * The Demolevel.
 *
 * <p>The player has to craft a Healpotion.
 */
public class Dungeon extends DungeonLevel {

  private final List<Entity> puzzlePushEntities = new ArrayList<>();
  private final Color[] stoneColors = {
    Color.WHITE,
    Color.RED,
    Color.BLUE,
    Color.RED,
    Color.BLUE,
    Color.RED,
    Color.BLUE,
    Color.GREEN,
    Color.BLUE,
    Color.GREEN,
    Color.WHITE,
    Color.RED,
    Color.WHITE,
    Color.WHITE,
    Color.WHITE,
    Color.GREEN,
    Color.BLUE,
    Color.GREEN,
    Color.BLUE,
    Color.RED,
    Color.YELLOW
  };
  private final Color[] plateColors = {
    Color.WHITE,
    Color.RED,
    Color.BLUE,
    Color.RED,
    Color.BLUE,
    Color.RED,
    Color.BLUE,
    Color.GREEN,
    Color.BLUE,
    Color.GREEN,
    Color.WHITE,
    Color.RED,
    Color.WHITE,
    Color.WHITE,
    Color.WHITE,
    Color.GREEN,
    Color.BLUE,
    Color.GREEN,
    Color.BLUE,
    Color.RED,
    Color.YELLOW
  };
  private final Color[] waterColors = {
    Color.GREEN,
    Color.RED,
    Color.BLUE,
    Color.RED,
    Color.RED,
    Color.GREEN,
    Color.BLUE,
    Color.RED,
    Color.BLUE,
    Color.GREEN
  };

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
        getPoint("fire11").toCoordinate(), getPoint("fire12").toCoordinate(), DesignLabel.FIRE);
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
    createPushPuzzle();
  }

  private void createPushPuzzle() {
    createPushPuzzleEntities();

    /* Game.add(
    LeverFactory.createLever(
      getPoint("push-reset"),
      new ICommand() {
        public void execute() {
          resetPushStones();
        }

        public void undo() {}
      }));*/
  }

  private void createPushPuzzleEntities() {
    listPointsIndexed("push_stone")
        .forEach(
            tuple -> {
              Point pos = tuple.a();
              int index = tuple.b();
              Entity pushStone = new Entity("push_stone");
              pushStone.add(new PositionComponent(pos));
              DrawComponent dc = new DrawComponent(new SimpleIPath("objects/push-stone.png"));
              dc.depth(DepthLayer.Player.depth());
              Color tintColor = index < stoneColors.length ? stoneColors[index] : Color.WHITE;
              dc.tintColor(Color.rgba8888(tintColor));
              // dc.shaders().add("outline", new OutlineShader(20));
              pushStone.add(dc);
              pushStone.add(new CollideComponent(Vector2.of(0.05f, 0.05f), Vector2.of(0.9f, 0.9f)));
              pushStone.add(new VelocityComponent(5.0f));
              Game.add(pushStone);
              puzzlePushEntities.add(pushStone);
            });

    listPointsIndexed("push_plate")
        .forEach(
            tuple -> {
              Point platePos = tuple.a();
              int index = tuple.b();
              Point doorPos = getPoint("push_door" + index);
              DoorTile doorTile = (DoorTile) tileAt(doorPos).orElseThrow();
              doorTile.close();
              /*Color tintColor = index < stoneColors.length ? stoneColors[index] : Color.WHITE;
              doorTile.tintColor(Color.rgba8888(tintColor));*/

              Entity pp =
                  LeverFactory.pressurePlate(
                      platePos,
                      1f,
                      new ICommand() {
                        public void execute() {
                          Sounds.DOOR_OPEN_SOUND.play();
                          doorTile.open();
                        }

                        public void undo() {
                          Sounds.DOOR_CLOSE_SOUND.play();
                          doorTile.close();
                        }
                      });
              pp.fetch(DrawComponent.class)
                  .ifPresent(
                      dc -> {
                        Color tColor =
                            index < plateColors.length ? plateColors[index] : Color.WHITE;
                        dc.tintColor(Color.rgba8888(tColor));
                      });
              PressurePlateComponent pressurePlateComponent =
                  pp.fetch(PressurePlateComponent.class).orElseThrow();
              TriConsumer<Entity, Entity, Direction> onCollideEnter =
                  (self, other, dir) -> {
                    self.fetch(DrawComponent.class)
                        .ifPresent(
                            dc -> {
                              boolean colorMatches =
                                  other
                                      .fetch(DrawComponent.class)
                                      .map(odc -> odc.tintColor() == dc.tintColor())
                                      .orElse(false);
                              if (colorMatches) {
                                other
                                    .fetch(VelocityComponent.class)
                                    .ifPresent(vc -> pressurePlateComponent.increase(vc.mass()));
                              }
                            });
                  };
              TriConsumer<Entity, Entity, Direction> onCollideLeave =
                  (self, other, dir) -> {
                    if (other.isPresent(ProjectileComponent.class)) return;
                    self.fetch(DrawComponent.class)
                        .ifPresent(
                            dc -> {
                              boolean colorMatches =
                                  other
                                      .fetch(DrawComponent.class)
                                      .map(odc -> odc.tintColor() == dc.tintColor())
                                      .orElse(false);
                              if (colorMatches) {
                                other
                                    .fetch(VelocityComponent.class)
                                    .ifPresent(vc -> pressurePlateComponent.decrease(vc.mass()));
                              }
                            });
                  };
              pp.add(new CollideComponent(onCollideEnter, onCollideLeave).isSolid(false));

              Game.add(pp);
              puzzlePushEntities.add(pp);
            });

    listPointsIndexed("push_water")
        .forEach(
            tuple -> {
              Point pos = tuple.a();
              int index = tuple.b();
              Entity water = DecoFactory.createDeco(pos, Deco.WaterHigh);
              water.remove(DecoComponent.class);
              water
                  .fetch(DrawComponent.class)
                  .ifPresent(
                      dc -> {
                        Color tintColor =
                            index < waterColors.length ? waterColors[index] : Color.WHITE;
                        dc.tintColor(Color.rgba8888(tintColor));
                      });

              CollideComponent cc = new CollideComponent();
              cc.collideEnter(
                  (self, other, dir) -> {
                    if (other.name().equals("push_stone")) {
                      self.fetch(DrawComponent.class)
                          .ifPresent(
                              dc -> {
                                other
                                    .fetch(DrawComponent.class)
                                    .ifPresent(
                                        odc -> {
                                          odc.tintColor(dc.tintColor());
                                        });
                              });
                    }
                  });
              cc.isSolid(false);
              water.add(cc);
              Game.add(water);
            });
  }

  private void resetPushStones() {
    puzzlePushEntities.forEach(Game::remove);
    puzzlePushEntities.clear();
    createPushPuzzleEntities();
  }

  @Override
  protected void onTick() {}

  private void changeTileDesignLabel(Coordinate a, Coordinate b, DesignLabel newDesignLabel) {
    int minX = Math.min(a.x(), b.x());
    int maxX = Math.max(a.x(), b.x());
    int minY = Math.min(a.y(), b.y());
    int maxY = Math.max(a.y(), b.y());

    for (int y = minY; y <= maxY; y++) {
      for (int x = minX; x <= maxX; x++) {
        layout[y][x].designLabel(newDesignLabel);
      }
    }
  }
}
