package demoDungeon.level;

import contrib.components.*;
import contrib.crafting.Crafting;
import contrib.crafting.CraftingIngredient;
import contrib.crafting.CraftingResult;
import contrib.crafting.Recipe;
import contrib.entities.MiscFactory;
import contrib.entities.deco.Deco;
import contrib.entities.deco.DecoFactory;
import contrib.hud.DialogUtils;
import contrib.item.concreteItem.*;
import contrib.modules.interaction.IInteractable;
import contrib.modules.interaction.Interaction;
import contrib.modules.interaction.InteractionComponent;
import contrib.utils.components.ai.idle.PatrolWalk;
import core.Entity;
import core.Game;
import core.components.PositionComponent;
import core.level.DungeonLevel;
import core.level.Tile;
import core.level.loader.DungeonLoader;
import core.level.utils.Coordinate;
import core.level.utils.DesignLabel;
import core.level.utils.LevelElement;
import core.systems.DrawSystem;
import core.utils.*;
import core.utils.components.draw.shader.ColorGradeShader;
import escapeDungeon.items.*;
import guard.GuardBuilder;
import hint.*;
import java.util.*;
import mobs.EscapeRoomMonsterBuilder;

/**
 * The Demolevel.
 *
 * <p>The player has to craft a Healpotion.
 */
public class Tutorial extends DungeonLevel {

  private final Tuple<Point[], PatrolWalk.MODE>[] guardCheckPoints;

  private boolean[] showDialogs = {true, true, true, true};

  Map<Entity, boolean[]> enteredAreas = new HashMap<>();

  /**
   * Creates a new Demo Level.
   *
   * @param layout The layout of the level.
   * @param designLabel The design label of the level.
   * @param namedPoints The custom points of the level.
   */
  public Tutorial(
      LevelElement[][] layout, DesignLabel designLabel, Map<String, Point> namedPoints) {
    super(layout, designLabel, namedPoints, "Demo");

    changeTileDesignLabel(
        getPoint("beige11").toCoordinate(),
        getPoint("beige12").toCoordinate(),
        DesignLabel.BEIGECASTLE);
    changeTileDesignLabel(
        getPoint("castle11").toCoordinate(),
        getPoint("castle12").toCoordinate(),
        DesignLabel.GREYCASTLE);
    changeTileDesignLabel(
        getPoint("castle21").toCoordinate(),
        getPoint("castle22").toCoordinate(),
        DesignLabel.GREYCASTLE);

    refreshLevelTextures();

    guardCheckPoints =
        new Tuple[] {
          Tuple.of(
              new Point[] {
                getPoint("guard_cp1"), getPoint("guard_cp2"),
              },
              PatrolWalk.MODE.BACK_AND_FORTH),
          Tuple.of(
              new Point[] {
                getPoint("guard_cp2"), getPoint("guard_cp1"),
              },
              PatrolWalk.MODE.BACK_AND_FORTH),
          Tuple.of(
              new Point[] {
                getPoint("guard_cp1"), getPoint("guard_cp2"),
              },
              PatrolWalk.MODE.BACK_AND_FORTH),
          Tuple.of(
              new Point[] {
                getPoint("guard_cp1"), getPoint("guard_cp2"),
              },
              PatrolWalk.MODE.BACK_AND_FORTH),
        };
  }

  @Override
  protected void onFirstTick() {

    Game.allPlayers()
        .forEach(
            player -> {
              DialogUtils.showTextPopup(
                  "Ihr scheint getrennt worden zu sein. Findet einen Weg wieder zueinander zu finden.",
                  "Tutorial",
                  () -> {},
                  player.id());
              enteredAreas.put(player, new boolean[4]);
            });

    DrawSystem ds = (DrawSystem) Game.systems().get(DrawSystem.class);
    ds.levelShaders()
        .add(
            "gray",
            new ColorGradeShader(0.5f, 0.1f, 0.6f)
                .region(new Rectangle(Vector2.of(getPoint("Test1")), Vector2.of(getPoint("Test2"))))
                .transitionSize(1));
    Game.add(MiscFactory.newCraftingCauldron(getPoint("Cauldron1")));
    Game.add(MiscFactory.newCraftingCauldron(getPoint("Cauldron2")));
    Game.add(DecoFactory.createDeco(getPoint("Stone1"), Deco.Stone));
    Game.add(DecoFactory.createDeco(getPoint("Stone2"), Deco.Stone));
    Game.add(DecoFactory.createDeco(getPoint("Stone3"), Deco.Stone));
    Game.add(DecoFactory.createDeco(getPoint("Stone4"), Deco.Stone));

    Entity stone = MiscFactory.newStone(getPoint("Stone1"), 0);
    stone
        .fetch(InteractionComponent.class)
        .ifPresent(
            oldIC -> {
              InteractionComponent wrapperIC =
                  new InteractionComponent(
                      new IInteractable() {
                        private final IInteractable base = oldIC.interactions();

                        @Override
                        public Interaction look() {
                          return new Interaction(
                              (entity, who) -> {
                                DialogUtils.showTextPopup(
                                    "Vielleicht kann ich ein Werkzeug herstellen, um hier durch zu kommen.",
                                    LOOK_LABEL);
                              },
                              LOOK_LABEL);
                        }

                        @Override
                        public Interaction interact() {
                          return base.interact();
                        }
                      });
              stone.remove(InteractionComponent.class);
              stone.add(wrapperIC);
            });
    Game.add(stone);
    Game.add(
        MiscFactory.newChest(
            Set.of(new HammerHeadItem(), new StickItem(), new TutorialPotionItem()),
            getPoint("Kiste1")));
    Game.add(
        MiscFactory.newChest(
            Set.of(new HammerHeadItem(), new StickItem(), new TutorialPotionItem()),
            getPoint("Chest2")));
    createCrafting();
  }

  private void createCrafting() {
    Crafting.clearRecipes();
    CraftingIngredient[] recipeIngredient = {new HammerHeadItem(), new StickItem()};
    CraftingResult[] recipeResults = {new HammerItem()};
    Crafting.addRecipe(new Recipe(false, recipeIngredient, recipeResults));
  }

  private boolean guardsInitialized = false;
  private int exitArea = 0;

  @Override
  protected void onTick() {

    // DialogUtils.showTextPopup("Haben wir dich", "Gefangen", () -> Game.exit());

    Game.allPlayers()
        .forEach(
            player -> {
              player
                  .fetch(CollideComponent.class)
                  .ifPresent(
                      cc -> {
                        Coordinate position = cc.collider().absoluteCenter().toCoordinate();
                        if (showDialogs[0]
                            && position.equals(getPoint("Trigger11").toCoordinate())) {
                          DialogUtils.showTextPopup(
                              "Der Weg vor dir scheint versperrt zu sein. Vielleicht finde ich in diesem Haus etwas um hier freizuräumen.",
                              "Umschauen",
                              () -> showDialogs[0] = false,
                              player.id());
                        }
                        if (showDialogs[1]
                            && position.equals(getPoint("Trigger12").toCoordinate())) {
                          DialogUtils.showTextPopup(
                              "Der Weg vor dir scheint versperrt zu sein. Vielleicht finde ich in diesem Haus etwas um hier freizuräumen.",
                              "Umschauen",
                              () -> showDialogs[1] = false,
                              player.id());
                        }
                        if (showDialogs[2]
                            && position.equals(getPoint("Trigger21").toCoordinate())) {
                          DialogUtils.showTextPopup(
                              "Da steht eine Kiste und ein Kessel zum Craften. Du kannst mit diesen interagieren indem du deine Maus auf diese bewegst und E drückst.",
                              "Crafting",
                              () -> {
                                showDialogs[2] = false;
                                DialogUtils.showTextPopup(
                                    "Sammel die Items aus der Kiste ein und schau, was du damit im Kessel herstellen kannst.",
                                    "Crafting",
                                    () -> {},
                                    player.id());
                              },
                              player.id());
                        }
                        if (showDialogs[3]
                            && position.equals(getPoint("Trigger22").toCoordinate())) {
                          DialogUtils.showTextPopup(
                              "Da steht eine Kiste und ein Kessel zum Craften. Du kannst mit diesen interagieren indem du deine Maus auf diese bewegst und E drückst.",
                              "Crafting",
                              () -> {
                                showDialogs[3] = false;
                                DialogUtils.showTextPopup(
                                    "Sammel die Items aus der Kiste ein und schau, was du damit im Kessel herstellen kannst.",
                                    "Crafting",
                                    () -> {},
                                    player.id());
                              },
                              player.id());
                        }
                        if (!enteredAreas.get(player)[0]
                            && position.equals(getPoint("Trigger31").toCoordinate())) {
                          DialogUtils.showTextPopup(
                              "Ich bin müde und der Weg ist versperrt. Die Betten sehen wirklich gut aus.",
                              "Schlafen ..ZZzzz",
                              () -> enteredAreas.get(player)[0] = true,
                              player.id());
                        }
                        if (!enteredAreas.get(player)[1] && position.x() > 45) {
                          enteredAreas.get(player)[1] = true;
                          exitArea++;
                        } else if (enteredAreas.get(player)[1] && position.x() < 45) {
                          enteredAreas.get(player)[1] = false;
                          exitArea--;
                        }

                        if (exitArea == enteredAreas.size()) {
                          Game.tileAt(getPoint("hole1")).get().levelElement(LevelElement.SKIP);
                          Game.tileAt(getPoint("hole2")).get().levelElement(LevelElement.SKIP);
                          Game.tileAt(getPoint("hole3")).get().levelElement(LevelElement.SKIP);
                          Game.tileAt(getPoint("hole4")).get().levelElement(LevelElement.SKIP);
                          if (!guardsInitialized) {
                            initGuards();
                            guardsInitialized = true;
                          }
                        }

                        player
                            .fetch(AttachmentComponent.class)
                            .ifPresent(
                                ac -> {
                                  if (position.x() >= 59) {
                                    player.remove(AttachmentComponent.class);
                                    player
                                        .fetch(PositionComponent.class)
                                        .ifPresent(
                                            pos -> {
                                              DungeonLoader.loadNextLevel();
                                            });
                                  }
                                });
                      });
            });
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
        .idleAI(() -> new PatrolWalk(Arrays.asList(patrolPoints), 500, mode))
        .build(this.getPoint("guard_cp0"));
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
}
