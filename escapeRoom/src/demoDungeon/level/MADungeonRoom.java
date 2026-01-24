package demoDungeon.level;

import analytics.DungeonAnalyticsAPI;
import com.badlogic.gdx.graphics.Color;
import contrib.components.*;
import contrib.components.CollideComponent;
import contrib.components.FlyComponent;
import contrib.entities.CharacterClass;
import contrib.entities.LeverFactory;
import contrib.entities.MiscFactory;
import contrib.entities.deco.Deco;
import contrib.entities.deco.DecoFactory;
import contrib.hud.DialogUtils;
import contrib.hud.dialogs.DialogContext;
import contrib.hud.dialogs.DialogContextKeys;
import contrib.hud.dialogs.DialogType;
import contrib.item.Item;
import contrib.modules.interaction.Interaction;
import contrib.modules.interaction.InteractionComponent;
import contrib.systems.EventScheduler;
import contrib.utils.EntityUtils;
import contrib.utils.ICommand;
import contrib.utils.components.ai.idle.PatrolWalk;
import contrib.utils.components.skill.Skill;
import core.Entity;
import core.Game;
import core.components.*;
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
import escapeDungeon.components.AxeComponent;
import escapeDungeon.components.EscapedComponent;
import escapeDungeon.components.IceMovementComponent;
import escapeDungeon.items.*;
import escapeDungeon.skill.SprintSkill;
import guard.GuardBuilder;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import mobs.EscapeRoomMonsterBuilder;
import mushRoom.Sounds;
import mushRoom.modules.journal.CraftingBookItem;
import mushRoom.modules.lockpick.LockPickDialog;
import mushRoom.modules.lockpick.LockPickDifficulty;
import mushRoom.shaders.TorchPostProcessing;
import tools.timer.TimerAPI;

/** The MADungeonRoom level. */
public class MADungeonRoom extends DungeonLevel {

  private boolean resetPushStones21 = false;
  private boolean resetPushStones22 = false;
  private boolean resetPushStones23 = false;
  private boolean resetPushStones24 = false;
  private boolean resetPushStones25 = false;
  private boolean resetPushStones26 = false;
  private boolean dimed = false;
  private TorchPostProcessing torchShader;
  private final List<Entity> pushStones1 = new ArrayList<>();
  private final List<Entity> pushStones2 = new ArrayList<>();
  private final List<Entity> pushStones3 = new ArrayList<>();
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

  private final Tuple<Point[], PatrolWalk.MODE>[] guardCheckPoints;

  private static final Map<CharacterClass, Class<? extends Skill>> classToSkillMap =
      Map.of(
          CharacterClass.ROGUE, SprintSkill.class
          // Add other mappings as needed
          );
  private static final Map<CharacterClass, Item[]> classToStartingItemsMap =
      Map.of(
          CharacterClass.APPRENTICE, new Item[] {new CraftingBookItem()}
          // Add other mappings as needed
          );

  private final Set<Integer> initedPlayers = new HashSet<>();

  ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
  ;
  ScheduledFuture<?> timer;

  private boolean escaped = false;

  /**
   * Creates a new Demo Level.
   *
   * @param layout The layout of the level.
   * @param designLabel The design label of the level.
   * @param namedPoints The custom points of the level.
   */
  @SuppressWarnings("unchecked")
  public MADungeonRoom(
      LevelElement[][] layout, DesignLabel designLabel, Map<String, Point> namedPoints) {
    super(layout, designLabel, namedPoints, "MARoom");
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
        getPoint("forest25").toCoordinate(),
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
    changeTileDesignLabel(
        getPoint("outside15").toCoordinate(),
        getPoint("outside14").toCoordinate(),
        DesignLabel.WATER);

    refreshLevelTextures();

    //      ds.sceneShaders().remove("torches");
    //      ds.sceneShaders().add("torches", torchShader);
    //    });
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
          torchShader.addArea(new Rectangle(getPoint("forest25"), getPoint("forest24")));
          torchShader.addArea(new Rectangle(getPoint("poi11"), getPoint("poi12")));
          torchShader.addArea(new Rectangle(getPoint("poi21"), getPoint("poi22")));
          torchShader.addArea(new Rectangle(getPoint("poi31"), getPoint("poi32")));
          ds.sceneShaders().remove("torches");
          ds.sceneShaders().add("torches", torchShader);
        });

    guardCheckPoints =
        new Tuple[] {
          // 8
          Tuple.of(
              new Point[] {
                getPoint("guard1_cp1"),
                getPoint("guard1_cp2"),
                getPoint("guard1_cp3"),
                getPoint("guard1_cp4"),
                getPoint("guard1_cp5"),
                getPoint("guard1_cp6"),
                getPoint("guard1_cp7"),
                getPoint("guard1_cp8")
              },
              PatrolWalk.MODE.BACK_AND_FORTH),
          // 9
          Tuple.of(
              new Point[] {
                getPoint("guard2_cp1"),
                getPoint("guard2_cp2"),
                getPoint("guard2_cp3"),
                getPoint("guard2_cp4"),
                getPoint("guard2_cp5"),
                getPoint("guard2_cp6"),
                getPoint("guard2_cp7"),
                getPoint("guard2_cp8"),
                getPoint("guard2_cp9"),
              },
              PatrolWalk.MODE.RANDOM),
          // 11
          Tuple.of(
              new Point[] {
                getPoint("guard3_cp1"),
                getPoint("guard3_cp2"),
                getPoint("guard3_cp3"),
                getPoint("guard3_cp4"),
                getPoint("guard3_cp5"),
                getPoint("guard3_cp6"),
                getPoint("guard3_cp7"),
                getPoint("guard3_cp8"),
                getPoint("guard3_cp9"),
                getPoint("guard3_cp10"),
                getPoint("guard3_cp11"),
              },
              PatrolWalk.MODE.LOOP),
          // 19
          Tuple.of(
              new Point[] {
                getPoint("guard4_cp1"),
                getPoint("guard4_cp2"),
                getPoint("guard4_cp3"),
                getPoint("guard4_cp4"),
                getPoint("guard4_cp5"),
                getPoint("guard4_cp6"),
                getPoint("guard4_cp7"),
                getPoint("guard4_cp8"),
                getPoint("guard4_cp9"),
                getPoint("guard4_cp10"),
                getPoint("guard4_cp11"),
                getPoint("guard4_cp12"),
                getPoint("guard4_cp13"),
                getPoint("guard4_cp14"),
                getPoint("guard4_cp15"),
                getPoint("guard4_cp16"),
                getPoint("guard4_cp17"),
                getPoint("guard4_cp18"),
                getPoint("guard4_cp19"),
              },
              PatrolWalk.MODE.LOOP),
          // 11
          Tuple.of(
              new Point[] {
                getPoint("guard5_cp1"),
                getPoint("guard5_cp2"),
                getPoint("guard5_cp3"),
                getPoint("guard5_cp4"),
                getPoint("guard5_cp5"),
                getPoint("guard5_cp6"),
                getPoint("guard5_cp7"),
                getPoint("guard5_cp8"),
                getPoint("guard5_cp9"),
                getPoint("guard5_cp10"),
                getPoint("guard5_cp11"),
              },
              PatrolWalk.MODE.BACK_AND_FORTH),
          // 23
          Tuple.of(
              new Point[] {
                getPoint("guard6_cp1"),
                getPoint("guard6_cp2"),
                getPoint("guard6_cp3"),
                getPoint("guard6_cp4"),
                getPoint("guard6_cp5"),
                getPoint("guard6_cp6"),
                getPoint("guard6_cp7"),
                getPoint("guard6_cp8"),
                getPoint("guard6_cp9"),
                getPoint("guard6_cp10"),
                getPoint("guard6_cp11"),
                getPoint("guard6_cp12"),
                getPoint("guard6_cp13"),
                getPoint("guard6_cp14"),
                getPoint("guard6_cp15"),
                getPoint("guard6_cp16"),
                getPoint("guard6_cp17"),
                getPoint("guard6_cp18"),
                getPoint("guard6_cp19"),
                getPoint("guard6_cp20"),
                getPoint("guard6_cp21"),
                getPoint("guard6_cp22"),
                getPoint("guard6_cp23"),
              },
              PatrolWalk.MODE.LOOP),
        };
  }

  @Override
  protected void onFirstTick() {
    changeIceTiles(getPoint("fire11").toCoordinate(), getPoint("fire12").toCoordinate());
    refreshLevelTextures();
    createPushPuzzle();
    createIcePuzzleEntities();
    createChests();
    initGuards();
    Game.add(MiscFactory.newCraftingCauldron(getPoint("crafting0")));
    timer = scheduler.schedule(() -> {}, 10, TimeUnit.SECONDS);
  }

  @Override
  protected void onTick() {
    if (!Game.isHeadless()) {
      updateTorchShader();
    }

    if (!TimerAPI.isRunning()) {
      var playerCount = Game.allPlayers().count();
      if (playerCount >= 2) {
        TimerAPI.start();
      }
    }

    Game.allPlayers()
        .forEach(
            player -> {
              iceControls(player);
              checkEscape(player);

              handleStartLogic(player);
            });

    if (!escaped
        && Game.allPlayers().allMatch(player -> player.isPresent(EscapedComponent.class))) {
      Game.allPlayers().forEach(this::escaped);
    }
  }

  private void handleStartLogic(Entity player) {
    if (initedPlayers.contains(player.id())) {
      return;
    }

    initedPlayers.add(player.id());
    // give skills to classes
    classToSkillMap.forEach(
        (charClass, skillCls) -> {
          if (player
              .fetch(CharacterClassComponent.class)
              .map(cc -> cc.characterClass() == charClass)
              .orElse(false)) {
            player
                .fetch(SkillComponent.class)
                .ifPresent(
                    skillComp -> {
                      if (skillComp.getSkill(skillCls).isEmpty()) {
                        try {
                          skillComp.addSkill(skillCls.getDeclaredConstructor().newInstance());
                        } catch (Exception e) {
                          e.printStackTrace();
                        }
                      }
                    });
          }
        });

    // give starting items to classes
    classToStartingItemsMap.forEach(
        (charClass, items) -> {
          if (player
              .fetch(CharacterClassComponent.class)
              .map(cc -> cc.characterClass() == charClass)
              .orElse(false)) {
            player
                .fetch(InventoryComponent.class)
                .ifPresent(
                    invComp -> {
                      for (Item item : items) {
                        if (!invComp.hasItem(item.getClass())) {
                          invComp.add(item);
                        }
                      }
                    });
          }
        });
  }

  private void createChests() {
    Game.add(addLockpicking(MiscFactory.newChest(Set.of(new LeafItem()), getPoint("chest0"))));
    Game.add(addLockpicking(MiscFactory.newChest(Set.of(new CoalItem()), getPoint("chest1"))));
    Game.add(
        addLockpicking(MiscFactory.newChest(Set.of(new EmptyBottleItem()), getPoint("chest2"))));
    Game.add(addLockpicking(MiscFactory.newChest(Set.of(new GoldItem()), getPoint("chest3"))));
    Game.add(
        addLockpicking(MiscFactory.newChest(Set.of(new EmptyBottleItem()), getPoint("chest4"))));
    Game.add(addLockpicking(MiscFactory.newChest(Set.of(new MetalItem()), getPoint("chest5"))));
    Game.add(addLockpicking(MiscFactory.newChest(Set.of(new MetalItem()), getPoint("chest6"))));
    Game.add(
        addLockpicking(MiscFactory.newChest(Set.of(new RingSilverItem()), getPoint("chest7"))));
    Game.add(addLockpicking(MiscFactory.newChest(Set.of(new RingGoldItem()), getPoint("chest0"))));
    Game.add(addLockpicking(MiscFactory.newChest(Set.of(new BlueGemItem()), getPoint("chest9"))));
    Game.add(addLockpicking(MiscFactory.newChest(Set.of(new RedGemItem()), getPoint("chest10"))));
    Game.add(addLockpicking(MiscFactory.newChest(Set.of(new RedGemItem()), getPoint("chest11"))));
    Game.add(addLockpicking(MiscFactory.newChest(Set.of(new RingGoldItem()), getPoint("chest12"))));
    Game.add(addLockpicking(MiscFactory.newChest(Set.of(new CoalItem()), getPoint("chest13"))));
    Game.add(addLockpicking(MiscFactory.newChest(Set.of(new GoldItem()), getPoint("chest14"))));
    Game.add(addLockpicking(MiscFactory.newChest(Set.of(new RopeItem()), getPoint("chest15"))));
    createTreeChest();
  }

  private final Set<Integer> lockpickedChests = new HashSet<>();

  private Entity addLockpicking(Entity chest) {
    chest
        .fetch(InteractionComponent.class)
        .ifPresent(
            (ic) -> {
              chest.remove(InteractionComponent.class);
            });
    chest.add(
        new InteractionComponent(
            () ->
                new Interaction(
                    (interacted, interactor) -> {
                      if (lockpickedChests.contains(interacted.id())) {
                        interactor
                            .fetch(InventoryComponent.class)
                            .ifPresent(openChest(interacted, interactor));
                        return;
                      }

                      LockPickDialog.openLockPick(
                          interactor,
                          LockPickDifficulty.MEDIUM,
                          () -> {
                            lockpickedChests.add(interacted.id());
                            interactor
                                .fetch(InventoryComponent.class)
                                .ifPresent(openChest(interacted, interactor));
                          },
                          () -> {});
                    })));

    return chest;
  }

  private Consumer<InventoryComponent> openChest(Entity interacted, Entity interactor) {
    return (whoIc) -> {
      DialogContext context =
          DialogContext.builder()
              .type(DialogType.DefaultTypes.DUAL_INVENTORY)
              .put(DialogContextKeys.ENTITY, interactor.id())
              .put(DialogContextKeys.SECONDARY_ENTITY, interacted.id())
              .put(DialogContextKeys.OWNER_ENTITY, interactor.id())
              .build();
      UIComponent ui = new UIComponent(context, true, interactor.id());
      interactor.add(ui);
    };
  }

  private void createTreeChest() {
    Entity chest = MiscFactory.newChest(Set.of(new StickItem()), getPoint("TreeChest"));
    chest
        .fetch(InteractionComponent.class)
        .ifPresent(
            (ic) -> {
              chest.remove(InteractionComponent.class);
            });
    chest.add(
        new InteractionComponent(
            () ->
                new Interaction(
                    (interacted, interactor) ->
                        interactor
                            .fetch(InventoryComponent.class)
                            .ifPresent(
                                whoIc -> {
                                  interactor
                                      .fetch(AxeComponent.class)
                                      .ifPresentOrElse(
                                          (ac) -> {
                                            interacted
                                                .fetch(InventoryComponent.class)
                                                .ifPresent(
                                                    (ic) -> {
                                                      if (!ic.hasItem(LogItem.class)) {
                                                        ic.add(new LogItem());
                                                      }
                                                    });
                                          },
                                          () -> {
                                            interacted
                                                .fetch(InventoryComponent.class)
                                                .ifPresent(
                                                    (ic) -> {
                                                      if (ic.hasItem(LogItem.class)) {
                                                        ic.items(LogItem.class).forEach(ic::remove);
                                                      }
                                                    });
                                          });
                                  interacted
                                      .fetch(InventoryComponent.class)
                                      .ifPresent(
                                          (ic) -> {
                                            if (!ic.hasItem(StickItem.class)) {
                                              ic.add(new StickItem());
                                            }
                                            if (!ic.hasItem(LeafItem.class) && timer.isDone()) {
                                              ic.add(new LeafItem());
                                              timer =
                                                  scheduler.schedule(
                                                      () -> {}, 10, TimeUnit.SECONDS);
                                            }
                                          });
                                  DialogContext context =
                                      DialogContext.builder()
                                          .type(DialogType.DefaultTypes.DUAL_INVENTORY)
                                          .put(DialogContextKeys.ENTITY, interactor.id())
                                          .put(DialogContextKeys.SECONDARY_ENTITY, interacted.id())
                                          .put(DialogContextKeys.OWNER_ENTITY, interactor.id())
                                          .build();
                                  UIComponent ui = new UIComponent(context, true, interactor.id());
                                  interactor.add(ui);
                                }))));
    Game.add(chest);
  }

  private void createPushPuzzle() {
    createPushStones(0);
    createPushStones(1);

    createPushStones(2);

    createPushStones(3);

    createPushPuzzleEntities();
    createIcePuzzleEntities();
  }

  private void createPushStones(int riddle) {
    listPointsIndexed("push_stone")
        .forEach(
            tuple -> {
              Point pos = tuple.a();
              int index = tuple.b();
              if (riddle == 0) {
                if (index == 0 || index == 20) {
                  pushStones1.add(createStone(index, pos));
                }
              }
              if (riddle == 1) {
                if (index >= 1 && index <= 6) {
                  pushStones1.add(createStone(index, pos));
                }
              }
              if (riddle == 2) {
                if (index >= 7 && index <= 9) {
                  pushStones2.add(createStone(index, pos));
                }
              }
              if (riddle == 3) {
                if ((index >= 10 && index <= 19) || (index >= 21 && index <= 59)) {
                  pushStones3.add(createStone(index, pos));
                }
              }
            });
  }

  private Entity createStone(int index, Point pos) {
    Entity pushStone = new Entity("push_stone");
    pushStone.add(new PositionComponent(pos));
    DrawComponent dc = new DrawComponent(new SimpleIPath("objects/push-stone.png"));
    dc.depth(DepthLayer.Player.depth());
    Color tintColor = index < stoneColors.length ? stoneColors[index] : Color.WHITE;
    dc.tintColor(Color.rgba8888(tintColor));
    pushStone.add(dc);
    pushStone.add(new CollideComponent(Vector2.of(0.05f, 0.05f), Vector2.of(0.9f, 0.9f)));
    pushStone.add(VelocityComponent.builder().baseSpeed(5).mass(1.3f).build());
    Game.add(pushStone);
    return pushStone;
  }

  private void createPushPuzzleEntities() {
    listPointsIndexed("push_plate")
        .forEach(
            tuple -> {
              Point platePos = tuple.a();
              int index = tuple.b();
              if (index < 21) {
                Point doorPos = getPoint("push_door" + index);
                DoorTile doorTile = (DoorTile) tileAt(doorPos).orElseThrow();
                doorTile.close();

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
              }
              if (index == 21) {
                Game.add(
                    LeverFactory.pressurePlate(
                        platePos,
                        1.4f,
                        new ICommand() {
                          public void execute() {
                            resetPushStones21 = true;
                            if (resetPushStones22) {
                              resetPushStones(3);
                            }
                          }

                          public void undo() {
                            resetPushStones21 = false;
                          }
                        }));
              }
              if (index == 22) {
                Game.add(
                    LeverFactory.pressurePlate(
                        platePos,
                        1.4f,
                        new ICommand() {
                          public void execute() {
                            resetPushStones22 = true;
                            if (resetPushStones21) {
                              resetPushStones(3);
                            }
                          }

                          public void undo() {
                            resetPushStones22 = false;
                          }
                        }));
              }
              if (index == 23) {
                Game.add(
                    LeverFactory.pressurePlate(
                        platePos,
                        1.4f,
                        new ICommand() {
                          public void execute() {
                            resetPushStones23 = true;
                            if (resetPushStones24) {
                              resetPushStones(2);
                            }
                          }

                          public void undo() {
                            resetPushStones23 = false;
                          }
                        }));
              }
              if (index == 24) {
                Game.add(
                    LeverFactory.pressurePlate(
                        platePos,
                        1.4f,
                        new ICommand() {
                          public void execute() {
                            resetPushStones24 = true;
                            if (resetPushStones23) {
                              resetPushStones(2);
                            }
                          }

                          public void undo() {
                            resetPushStones24 = false;
                          }
                        }));
              }
              if (index == 25) {
                Game.add(
                    LeverFactory.pressurePlate(
                        platePos,
                        1.4f,
                        new ICommand() {
                          public void execute() {
                            resetPushStones25 = true;
                            if (resetPushStones26) {
                              resetPushStones(1);
                            }
                          }

                          public void undo() {
                            resetPushStones25 = false;
                          }
                        }));
              }
              if (index == 26) {
                Game.add(
                    LeverFactory.pressurePlate(
                        platePos,
                        1.4f,
                        new ICommand() {
                          public void execute() {
                            resetPushStones26 = true;
                            if (resetPushStones25) {
                              resetPushStones(1);
                            }
                          }

                          public void undo() {
                            resetPushStones26 = false;
                          }
                        }));
              }
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

  private void resetPushStones(int riddle) {
    switch (riddle) {
      case 1 -> {
        pushStones1.forEach(Game::remove);
        pushStones1.clear();
        createPushStones(1);
      }
      case 2 -> {
        pushStones2.forEach(Game::remove);
        pushStones2.clear();
        createPushStones(2);
      }
      case 3 -> {
        pushStones3.forEach(Game::remove);
        pushStones3.clear();
        createPushStones(3);
      }
    }
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
                    other.fetch(FlyComponent.class).ifPresent(fc -> Game.remove(self));
                  };
              cc.collideEnter(onCollideEnter);
              snowWall.add(dc);
              snowWall.add(cc);
              Game.add(snowWall);
            });
  }

  private final Set<Integer> initLightEntityIds = new HashSet<>();

  private void updateTorchShader() {
    Game.levelEntities()
        .filter(e -> !initLightEntityIds.contains(e.id()))
        .filter(e -> (e.name().contains("Torch") || e.name().contains("Firebox")))
        .forEach(
            e -> {
              float radius = e.name().contains("Torch") ? 5.0f : 7.0f;
              PositionComponent pos = e.fetch(PositionComponent.class).orElseThrow();
              torchShader.addLight(
                  new TorchPostProcessing.Light(
                      pos.position().x() + 0.5f, pos.position().y(), radius, e.id()));
              initLightEntityIds.add(e.id());
            });

    torchShader.lights().stream()
        .filter(light -> Game.findEntityById(light.entityId).isEmpty())
        .forEach(
            light -> {
              torchShader.removeLight(light);
              initLightEntityIds.remove(light.entityId);
            });

    Game.player()
        .map(player -> player.fetch(PositionComponent.class))
        .filter(Optional::isPresent)
        .map(Optional::get)
        .ifPresent(
            pc -> {
              Rectangle labyrinth1 =
                  new Rectangle(getPoint("labyrinth11"), getPoint("labyrinth12"));
              Rectangle labyrinth2 =
                  new Rectangle(getPoint("labyrinth21"), getPoint("labyrinth22"));
              Rectangle labyrinth3 = new Rectangle(getPoint("labyrinth22"), getPoint("fire12"));
              if (labyrinth1.contains(pc.position())
                  || labyrinth2.contains(pc.position())
                  || labyrinth3.contains(pc.position())) {
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
    PositionComponent pc = hero.fetch(PositionComponent.class).orElseThrow();
    Point currentPos = EntityUtils.getPosition(hero);
    VelocityComponent vc = hero.fetch(VelocityComponent.class).orElseThrow();
    InputComponent ic = hero.fetch(InputComponent.class).orElseThrow();
    Tile currentTile = Game.tileAt(currentPos).orElseThrow();

    if (!hero.isPresent(IceMovementComponent.class)) {
      return;
    }

    if (currentTile.designLabel() == DesignLabel.ICE) {
      if (hero.fetch(FlyComponent.class).isEmpty()) {
        hero.add(new FlyComponent());
      }
      Tile tileInFront = Game.tileAt(currentPos.translate(pc.viewDirection())).orElseThrow();

      vc.onWallHit(
          (self) -> {
            vc.currentVelocity(Vector2.ZERO);
            ic.removeCallback(directionKey(pc.viewDirection()));
            ic.deactivateControls(false);
          });
      if (tileInFront.levelElement().value()) {
        vc.currentVelocity(pc.viewDirection().scale(vc.baseSpeed()));
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

  private void changeIceTiles(Coordinate a, Coordinate b) {
    int minX = Math.min(a.x(), b.x());
    int maxX = Math.max(a.x(), b.x());
    int minY = Math.min(a.y(), b.y());
    int maxY = Math.max(a.y(), b.y());

    for (int y = minY; y <= maxY; y++) {
      for (int x = minX; x <= maxX; x++) {
        layout[y][x].designLabel(DesignLabel.ICE);
        layout[y][x].tintColor(-1);
      }
    }
  }

  private void initGuards() {
    for (Tuple<Point[], PatrolWalk.MODE> guardCheckPoint : guardCheckPoints) {
      Tile[] checkPoints = new Tile[guardCheckPoint.a().length];
      for (int j = 0; j < guardCheckPoint.a().length; j++) {
        checkPoints[j] = tileAt(guardCheckPoint.a()[j]).orElseThrow();
      }
      createGuards(checkPoints, guardCheckPoint.b());
    }
  }

  private Entity createGuards(Tile[] patrolPoints, PatrolWalk.MODE mode) {
    return ((GuardBuilder) EscapeRoomMonsterBuilder.GUARD.builder())
        .alertnessThreshold(100, 25, false)
        .addToGame()
        .speed(3.5f)
        .idleAI(() -> new PatrolWalk(Arrays.asList(patrolPoints), 5_000, mode))
        .build(this.getPoint("guardSpawn"));
  }

  private void checkEscape(Entity player) {
    player
        .fetch(PositionComponent.class)
        .ifPresent(
            pc -> {
              float x = pc.position().x();
              float y = pc.position().y();
              if (x <= 1 || x >= 188 || y <= 0 || y >= 99) {
                player.add(new EscapedComponent());
              } else {
                player.remove(EscapedComponent.class);
              }
            });
  }

  private void escaped(Entity player) {
    escaped = true;

    player
        .fetch(AnalyticsComponent.class)
        .ifPresent(
            ac -> {
              DungeonAnalyticsAPI.logXApiStatement(
                  ac,
                  DungeonAnalyticsAPI.Verb.SOLVED,
                  this.levelName,
                  Map.of("totalTime", String.valueOf(TimerAPI.elapsedSeconds())));
            });

    DialogUtils.showTextPopup("Du bist entkommen!", "ENTKOMMEN!", Game::exit);
  }
}
