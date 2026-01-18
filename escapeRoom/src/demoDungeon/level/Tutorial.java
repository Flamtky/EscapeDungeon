package demoDungeon.level;

import contrib.components.*;
import contrib.crafting.Crafting;
import contrib.crafting.CraftingIngredient;
import contrib.crafting.CraftingResult;
import contrib.crafting.Recipe;
import contrib.entities.MiscFactory;
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
import core.level.utils.Coordinate;
import core.level.utils.DesignLabel;
import core.level.utils.LevelElement;
import core.systems.DrawSystem;
import core.utils.*;
import core.utils.components.MissingComponentException;
import core.utils.components.draw.shader.ColorGradeShader;
import guard.GuardBuilder;
import hint.*;
import mobs.EscapeRoomMonsterBuilder;

import java.util.*;

/**
 * The Demolevel.
 *
 * <p>The player has to craft a Healpotion.
 */
public class Tutorial extends DungeonLevel {

  private PositionComponent playerPc;

  private final Tuple<Point[], PatrolWalk.MODE>[] guardCheckPoints;



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
        // 8
        Tuple.of(
          new Point[] {
            getPoint("guard_cp1"),
            getPoint("guard_cp2"),
          },
          PatrolWalk.MODE.BACK_AND_FORTH),
        // 9
      };
  }

  @Override
  protected void onFirstTick() {
    Entity hero = Game.player().orElseThrow(MissingPlayerException::new);
    playerPc =
        hero.fetch(PositionComponent.class)
            .orElseThrow(() -> MissingComponentException.build(hero, PositionComponent.class));

    DialogUtils.showTextPopup(
        "Es scheint als wäre der Weg vor dir versperrt, Untersuche deine Umgebung mit E oder der Rechten Maustaste und finde einen Weg um durch den Wald zu kommen.",
        "Tutorial");
    DrawSystem ds = (DrawSystem) Game.systems().get(DrawSystem.class);
    ds.levelShaders()
        .add(
            "gray",
            new ColorGradeShader(0.5f, 0.1f, 0.6f)
                .region(new Rectangle(Vector2.of(getPoint("Test1")), Vector2.of(getPoint("Test2"))))
                .transitionSize(1));
    Game.add(MiscFactory.newCraftingCauldron(getPoint("Cauldron1")));
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
            Set.of(new ItemWoodenArrow(), new ItemResourceEgg()), getPoint("Kiste1")));
    createCrafting();
  }

  private void createCrafting() {
    CraftingIngredient[] recipeIngredient = {new ItemWoodenArrow(), new ItemResourceEgg()};
    CraftingResult[] recipeResults = {new ItemHammer()};
    Crafting.addRecipe(new Recipe(false, recipeIngredient, recipeResults));
  }

  private boolean guardsInitialized = false;

  @Override
  protected void onTick() {

    float heroX = playerPc.position().x();

    // If the player gets close enough to the boss, the boss escapes
    if (heroX >= 50) {
      if (!guardsInitialized) {
        initGuards();
        guardsInitialized = true;
      }

      //DialogUtils.showTextPopup("Haben wir dich", "Gefangen", () -> Game.exit());
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
