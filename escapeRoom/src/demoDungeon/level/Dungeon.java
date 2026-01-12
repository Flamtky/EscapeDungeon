package demoDungeon.level;

import com.badlogic.gdx.graphics.Color;
import contrib.components.*;
import contrib.components.CollideComponent;
import contrib.components.FlyComponent;
import contrib.components.InventoryComponent;
import contrib.entities.LeverFactory;
import contrib.entities.MiscFactory;
import contrib.entities.deco.Deco;
import contrib.entities.deco.DecoFactory;
import contrib.item.Item;
import contrib.item.concreteItem.ItemPotionWater;
import contrib.systems.EventScheduler;
import contrib.utils.EntityUtils;
import contrib.utils.ICommand;
import core.Entity;
import core.Game;
import core.components.DrawComponent;
import core.components.InputComponent;
import core.components.PositionComponent;
import core.components.VelocityComponent;
import core.configuration.KeyboardConfig;
import core.level.DungeonLevel;
import core.level.Tile;
import core.level.elements.tile.DoorTile;
import core.level.utils.*;
import core.network.messages.c2s.InputMessage;
import core.systems.DrawSystem;
import core.utils.*;
import core.utils.Direction;
import core.utils.Point;
import core.utils.TriConsumer;
import core.utils.Vector2;
import core.utils.components.draw.DepthLayer;
import core.utils.components.path.SimpleIPath;
import escapeDungeon.components.IceMovementComponent;
import escapeDungeon.items.*;

import java.util.*;

import mushRoom.Sounds;
import mushRoom.shaders.TorchPostProcessing;

/**
 * The Demolevel.
 *
 * <p>The player has to craft a Healpotion.
 */
public class Dungeon extends DungeonLevel {

  private boolean dimed = false;
  private boolean torchShaderInitialized = false;
  private TorchPostProcessing torchShader;
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
        getPoint("grey21").toCoordinate(),
        getPoint("grey22").toCoordinate(),
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
        DesignLabel.WATER);
    changeTileDesignLabel(
        getPoint("outside11").toCoordinate(),
        getPoint("outside13").toCoordinate(),
        DesignLabel.WATER);
    changeTileDesignLabel(
        getPoint("outside13").toCoordinate(),
        getPoint("outside14").toCoordinate(),
        DesignLabel.WATER);

    refreshLevelTextures();

    Game.system(
        DrawSystem.class,
        (ds) -> {
          // Create global torch shader once
          torchShader = (TorchPostProcessing) new TorchPostProcessing().upscaling(4);
          torchShader.addArea(new Rectangle(getPoint("grass11"), getPoint("grass12")));
          torchShader.addArea(new Rectangle(getPoint("temple11"), getPoint("temple12")));
          torchShader.addArea(new Rectangle(getPoint("fire11"), getPoint("fire12")));
          torchShader.addArea(new Rectangle(getPoint("forest11"), getPoint("forest12")));
          torchShader.addArea(new Rectangle(getPoint("forest21"), getPoint("forest22")));
          torchShader.addArea(new Rectangle(getPoint("forest21"), getPoint("forest23")));
          torchShader.addArea(new Rectangle(getPoint("forest23"), getPoint("forest24")));
          ds.sceneShaders().add("torches", torchShader);
        });


  }

  @Override
  protected void onFirstTick() {
    changeIceTiles(
        getPoint("fire11").toCoordinate(), getPoint("fire12").toCoordinate(), DesignLabel.ICE);
    refreshLevelTextures();
    createPushPuzzleEntities();
    Entity hero = Game.allPlayers().findFirst().orElseThrow();
    hero.fetch(InventoryComponent.class).ifPresent((ic) -> ic.add(new IceWallPlacer()));
    createPushPuzzle();
    createIcePuzzleEntities();
    createChests();
  }

  private void createChests() {
    Game.add(MiscFactory.newChest(Set.of(new ItemPotionWater()), getPoint("chest0")));
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
  protected void onTick() {
    if (Game.allEntities().count() > 9600 && !torchShaderInitialized) {
      if (torchShader != null) {
        Game.levelEntities()
            .filter(e -> e.name().contains("Torch"))
            .forEach(
                torch -> {
                  torch
                      .fetch(PositionComponent.class)
                      .ifPresent(
                          pos -> {
                            torchShader.addLight(
                                new TorchPostProcessing.Light(
                                    pos.position().x() + 0.5f, pos.position().y(), 5.0f));
                          });
                });
        Game.levelEntities()
            .filter(e -> e.name().contains("Firebox"))
            .forEach(
                torch -> {
                  torch
                      .fetch(PositionComponent.class)
                      .ifPresent(
                          pos -> {
                            torchShader.addLight(
                                new TorchPostProcessing.Light(
                                    pos.position().x() + 0.5f, pos.position().y(), 7.0f));
                          });
                });
      }
      torchShaderInitialized = true;
    }
    Game.player()
        .get()
        .fetch(PositionComponent.class)
        .ifPresent(
            pc -> {
              Rectangle labyrinth1 =
                  new Rectangle(getPoint("labyrinth11"), getPoint("labyrinth12"));
              Rectangle labyrinth12 =
                  new Rectangle(getPoint("labyrinth21"), getPoint("labyrinth22"));
              if (labyrinth1.contains(pc.position()) || labyrinth12.contains(pc.position())) {
                if (!dimed) {
                  dimed = true;
                  for (int i = 1; i <= 15; i++) {
                    float dimness = 0.15f - (i * 0.01f);
                    EventScheduler.scheduleAction(
                        () -> {
                          torchShader.baseDimness(dimness);
                        },
                        i * 100);
                  }
                }
              } else {
                if (dimed) {
                  dimed = false;
                  for (int i = 1; i <= 15; i++) {
                    float dimness = 0f + (i * 0.01f);
                    EventScheduler.scheduleAction(
                        () -> {
                          torchShader.baseDimness(dimness);
                        },
                        i * 100);
                  }
                }
              }
            });

    Game.allPlayers()
        .forEach(
            hero -> {
              if (hero.fetch(IceMovementComponent.class).isEmpty()) {
                iceControls(hero);
              }
            });
  }

  private void createIcePuzzleEntities() {
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
      }
    }
  }
}
