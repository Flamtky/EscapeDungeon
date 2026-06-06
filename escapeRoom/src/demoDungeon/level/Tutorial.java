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
import contrib.utils.components.ai.idle.PatrolWalk;
import core.Entity;
import core.Game;
import core.level.DungeonLevel;
import core.level.Tile;
import core.level.utils.Coordinate;
import core.level.utils.DesignLabel;
import core.level.utils.LevelElement;
import core.utils.*;
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
    Game.add(MiscFactory.newCraftingCauldron(getPoint("Cauldron1")));
    Game.add(MiscFactory.newCraftingCauldron(getPoint("Cauldron2")));
    Game.add(DecoFactory.createDeco(getPoint("Stone1"), Deco.Stone));
    Game.add(DecoFactory.createDeco(getPoint("Stone2"), Deco.Stone));
    Game.add(DecoFactory.createDeco(getPoint("Stone3"), Deco.Stone));
    Game.add(DecoFactory.createDeco(getPoint("Stone4"), Deco.Stone));

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
  private final Set<Integer> initedPlayers = new HashSet<>();

  private void handleStartLogic(Entity player) {
    if (initedPlayers.contains(player.id())) {
      return;
    }
    initedPlayers.add(player.id());

    DialogUtils.showTextPopup(
        "Ihr scheint getrennt worden zu sein. Findet einen Weg wieder zueinander zu finden.",
        "Tutorial",
        () -> {},
        player.id());
    enteredAreas.put(player, new boolean[4]);
  }

  @Override
  protected void onTick() {
    Game.allPlayers()
        .forEach(
            player -> {
              handleStartLogic(player);
              player
                  .fetch(CollideComponent.class)
                  .ifPresent(
                      cc -> {
                        Coordinate position = cc.collider().absoluteCenter().toCoordinate();
                        if (showDialogs[0]
                            && position.equals(getPoint("Trigger11").toCoordinate())) {
                          showDialogs[0] = false;
                          DialogUtils.showTextPopup(
                              "Der Weg vor dir scheint versperrt zu sein. Vielleicht finde ich in diesem Haus etwas um hier freizuräumen.",
                              "Umschauen",
                              () -> {},
                              player.id());
                        }
                        if (showDialogs[1]
                            && position.equals(getPoint("Trigger12").toCoordinate())) {
                          showDialogs[1] = false;
                          DialogUtils.showTextPopup(
                              "Der Weg vor dir scheint versperrt zu sein. Vielleicht finde ich in diesem Haus etwas um hier freizuräumen.",
                              "Umschauen",
                              () -> {},
                              player.id());
                        }
                        if (showDialogs[2]
                            && position.equals(getPoint("Trigger21").toCoordinate())) {
                          showDialogs[2] = false;
                          DialogUtils.showTextPopup(
                              "Da steht eine Kiste und ein Kessel zum Craften. Du kannst mit diesen interagieren indem du deine Maus auf diese bewegst und E drückst.",
                              "Crafting",
                              () ->
                                  DialogUtils.showTextPopup(
                                      "Sammel die Items aus der Kiste mit der rechten Maustaste ein und lege sie mit der rechten Maustaste in den Kessel und schau was du damit herstellen kannst.",
                                      "Crafting",
                                      () -> {},
                                      player.id()),
                              player.id());
                        }
                        if (showDialogs[3]
                            && position.equals(getPoint("Trigger22").toCoordinate())) {
                          showDialogs[3] = false;
                          DialogUtils.showTextPopup(
                              "Da steht eine Kiste und ein Kessel zum Craften. Du kannst mit diesen interagieren indem du deine Maus auf diese bewegst und E drückst.",
                              "Crafting",
                              () ->
                                  DialogUtils.showTextPopup(
                                      "Sammel die Items aus der Kiste mit der rechten Maustaste ein und lege sie mit der rechten Maustaste in den Kessel und schau was du damit herstellen kannst.",
                                      "Crafting",
                                      () -> {},
                                      player.id()),
                              player.id());
                        }
                        if (!enteredAreas.get(player)[0]
                            && position.equals(getPoint("Trigger31").toCoordinate())) {
                          enteredAreas.get(player)[0] = true;
                          DialogUtils.showTextPopup(
                              "Ich bin müde und der Weg ist versperrt. Die Betten sehen wirklich gut aus. Interagiere mit dem Bett mit E.",
                              "Schlafen ..ZZzzz",
                              () -> {},
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
                          if (!enteredAreas.get(player)[2]) {
                            enteredAreas.get(player)[2] = true;
                            DialogUtils.showTextPopup(
                                "Der Weg zurück ist versperrt und ich glaube da kommen Wachen, was machen wir jetzt?",
                                "Gefahr!",
                                () -> {},
                                player.id());
                          }
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
                                    Game.exit();
                                    // DungeonLoader.loadNextLevel();
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
