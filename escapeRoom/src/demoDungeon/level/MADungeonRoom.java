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
import hint.*;
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
import petriNet.PetriNetSystem;
import petriNet.PlaceComponent;
import petriNet.TransitionComponent;
import tools.timer.TimerAPI;

/** The MADungeonRoom level. */
public class MADungeonRoom extends DungeonLevel {

  // region Hint Riddles
  // Labyrinth
  // Stamina Potion
  Entity staminaRiddle;
  PlaceComponent staminaRiddlePlace;
  private final String staminaRiddleTitle = "Aus der Puste?";
  private final Hint[] staminaRiddleHints = {
    new Hint(staminaRiddleTitle, "Ein guter Kräutertee soll dabei helfen wieder fit zu werden."),
  };

  Entity staminaRiddle2;
  PlaceComponent staminaRiddle2Place;
  private final String staminaRiddle2Title = "Energie aus der Flasche";
  private final Hint[] staminaRiddle2Hints = {
    new Hint(
        staminaRiddle2Title,
        "Nachdem du einen Ausdauertrank getrunken hast, kannst du das Fläschchen wieder nutzen."),
  };
  // Leaf
  Entity leafRiddle;
  PlaceComponent leafRiddlePlace;
  private final String leafRiddleTitle = "Wie Unkraut";
  private final Hint[] leafRiddleHints = {
    new Hint(leafRiddleTitle, "Die Blätter am Baum wachsen nach."),
  };
  // empty Bottle
  Entity bottleRiddle;
  PlaceComponent bottleRiddlePlace;
  private final String bottleRiddleTitle = "Durst?";
  private final Hint[] bottleRiddleHints = {
    new Hint(bottleRiddleTitle, "Das Fläschchen kann am Wasser nachgefüllt werden."),
  };

  // empty Bottle
  Entity torchRiddle;
  PlaceComponent torchRiddlePlace;
  private final String torchRiddleTitle = "Licht im Dunkeln";
  private final Hint[] torchRiddleHints = {
    new Hint(torchRiddleTitle + " 1", "Finde einen Weg Licht ins Labyrinth zu bringen."),
    new Hint(torchRiddleTitle + " 2", "Fackeln erhellen die Umgebung."),
  };

  // empty Bottle
  Entity coalRiddle;
  PlaceComponent coalRiddlePlace;
  private final String coalRiddleTitle = "Brennmaterialien";
  private final Hint[] coalRiddleHints = {
    new Hint(coalRiddleTitle, "Es ist mehr als nur ein Stück Kohle versteckt."),
  };

  // empty Bottle
  Entity stickRiddle;
  PlaceComponent stickRiddlePlace;
  private final String stickRiddleTitle = "nachwachsende Rohstoffe";
  private final Hint[] stickRiddleHints = {
    new Hint(stickRiddleTitle + " 1", "Stöcker wachsen an Bäumen."),
    new Hint(stickRiddleTitle + " 2", "Versuche mit einem Baum zu interagieren."),
    new Hint(stickRiddleTitle + " 3", "Es werden immer wieder Stöcker vom Baum abfallen."),
  };

  // empty Bottle
  Entity iceRiddle;
  PlaceComponent iceRiddlePlace;
  private final String iceRiddleTitle = "Kalte Gefilde";
  private final Hint[] iceRiddleHints = {
    new Hint(
        iceRiddleTitle,
        "Ich habe gehört tief im Labyrinth ist ein alter Teil des Verlies gänzlich eingefroren."),
  };

  // empty Bottle
  Entity iceRingRiddle;
  PlaceComponent iceRingRiddlePlace;
  private final String iceRingRiddleTitle = "Rutschige Böden";
  private final String blueGemRiddleTitle = "Eisig blau";
  private final Hint[] iceRingRiddleHints = {
    new Hint(
        iceRingRiddleTitle + " 1",
        "Es soll ein Item geben, dass es einem erlaubt auf dem Eis zu laufen."),
    new Hint(iceRingRiddleTitle + " 2", "Nur einer von euch kann dieses Item tragen."),
    new Hint(
        blueGemRiddleTitle + " 1",
        "Ein seltener blauer Edelstein soll am tiefsten Ende des Labyrinths versteckt sein."),
    new Hint(
        blueGemRiddleTitle + " 2",
        "Um zum Edelstein zu gelangen musst du den versteckten Durchgang finden."),
  };

  // empty Bottle
  Entity blueGemRiddle;
  PlaceComponent blueGemRiddlePlace;
  private final Hint[] blueGemRiddleHints = {
    new Hint(
        blueGemRiddleTitle + " 3",
        "Kurz vor dem Edelstein scheint der Weg versperrt aber du kannst durch die Wand hindurch gehen."),
  };

  // empty Bottle
  Entity strengthRiddle;
  PlaceComponent strengthRiddlePlace;
  private final String strengthRiddleTitle = "Sisyphos";
  private final Hint[] strengthRiddleHints = {
    new Hint(
        strengthRiddleTitle + " 1", "Wenn du die Steine bewegen willst musst du stärker werden."),
    new Hint(
        strengthRiddleTitle + " 2",
        "Ich habe gehört es soll ein Item geben, das dich stärker macht."),
  };

  // empty Bottle
  Entity redGemRiddle;
  PlaceComponent redGemRiddlePlace;
  private final Hint[] redGemRiddleHints = {
    new Hint(
        "Morgengrauen",
        "ein roter Stein der besondere Stärke verleiht soll in einem dunklen Labyrinth liegen."),
    new Hint(
        "Abenddämmerung",
        "Ich habe gehört, dass auch ein zweiter mächtiger Stein im Labyrinth verloren gegangen ist."),
  };

  // empty Bottle
  Entity pushRiddle;
  PlaceComponent pushRiddlePlace;
  private final String pushRiddleTitle = "Steine. Farben. Platten.";
  private final Hint[] pushRiddleHints = {
    new Hint(
        pushRiddleTitle + " 1",
        "Pass auf, dass du die Steine nicht in die Ecke schiebst. Du kannst sie nicht zurück ziehen."),
    new Hint(
        pushRiddleTitle + " 2",
        "Wenn ihr beide auf den beiden Druckplatten nebeneinander steht, wird der aktuelle Teil des Rätsels zurückgesetzt."),
    new Hint(
        pushRiddleTitle + " 3", "Die farbigen Flächen auf dem Boden ändern die Farbe der Steine."),
    new Hint(
        pushRiddleTitle + " 4",
        "Am Ende eines Rätselbereichs gibt es einen Checkpoint mit dem ihr später wieder zum Rätsel zurück kommen könnt."),
  };

  // empty Bottle
  Entity wallRiddle;
  PlaceComponent wallRiddlePlace;
  private final String wallRiddleTitle = "Flucht";
  private final Hint[] wallRiddleHints = {
    new Hint(wallRiddleTitle, "Ihr wollt hier raus? Ich habe gehört es gibt verschiedene Wege."),
  };

  // empty Bottle
  Entity wallRiddle2;
  PlaceComponent wallRiddle2Place;
  private final String wallRiddle2Title = "Ausbruch";
  private final Hint[] wallRiddle2Hints = {
    new Hint(wallRiddleTitle + " 1", "Werkzeuge? Was habt ihr damit vor?"),
    new Hint(
        wallRiddleTitle + " 2",
        "Wenn ihr Rohstoffe für eure Werkzeuge braucht müsst ihr euch ins Labyrinth begeben."),
  };

  // empty Bottle
  Entity pickaxeRiddle;
  PlaceComponent pickaxeRiddlePlace;
  private final String pickaxeRiddleTitle = "Tief schürfen";
  private final Hint[] pickaxeRiddleHints = {
    new Hint(
        pickaxeRiddleTitle + " 1",
        "Bergbau ist kein einfaches Unterfangen. Überanstrengt euch nicht."),
    new Hint(
        pickaxeRiddleTitle + " 2",
        "Die Wachen mögen es nicht, wenn ihr euch an der Festung zu schaffen macht."),
  };

  // empty Bottle
  Entity axeRiddle;
  PlaceComponent axeRiddlePlace;
  private final String axeRiddleTitle = "Holz hacken";
  private final Hint[] axeRiddleHints = {
    new Hint(
        axeRiddleTitle + " 1", "Was habt ihr mit der Axt vor? Lasst unseren einzigen Baum heile!"),
    new Hint(
        axeRiddleTitle + " 2",
        "Mit dem richtigen Werkzeug scheint man von einem Baum auch mehr als nur Stöcker zu bekommen."),
  };

  // empty Bottle
  Entity bridgeRiddle;
  PlaceComponent bridgeRiddlePlace;
  private final String bridgeRiddleTitle = "Schwimmen? Was ist das?";
  private final Hint[] bridgeRiddleHints = {
    new Hint(bridgeRiddleTitle + " 1", "Ein Burggraben sagt ihr? Das Wasser ist zu tief für euch."),
    new Hint(bridgeRiddleTitle + " 2", "Wenn ihr doch nur die Baumstämme verbinden könntet."),
    new Hint(
        bridgeRiddleTitle + " 3",
        "Ihr wollt ein Seil? Das werden die Wachen hier sicher nicht einfach rumliegen lassen."),
    new Hint(
        bridgeRiddleTitle + " 4",
        "ES gibt Gerüchte das vor Jahren ein Seil in einer ungenutzten Zelle eingemauert wurde."),
  };

  private void setupHints() {
    Game.add(HintGiverFactory.npc(getPoint("hintGiver")));
    PetriNetSystem petriNetSystem = new PetriNetSystem();
    Game.add(petriNetSystem);

    /// Ausdauertrank Rätsel

    staminaRiddle = new Entity("staminaRiddle");
    staminaRiddle.add(new HintComponent(staminaRiddleHints));
    staminaRiddlePlace = new PlaceComponent();
    staminaRiddle.add(staminaRiddlePlace);
    Game.add(staminaRiddle);
    // This is the first hint so activate it
    staminaRiddlePlace.produce();

    EventScheduler.scheduleAction(() -> staminaRiddlePlace.produce(), 10000);

    leafRiddle = new Entity("Find recipe riddle");
    leafRiddlePlace = new PlaceComponent();
    leafRiddle.add(new HintComponent(leafRiddleHints));
    leafRiddle.add(leafRiddlePlace);
    Game.add(leafRiddle);
    LeafItem.placeComponent(leafRiddlePlace);

    TimerAPI.registerCallback(10, () -> leafRiddlePlace.produce());

    // Craft potion riddle
    bottleRiddle = new Entity("Craft potion riddle");
    bottleRiddlePlace = new PlaceComponent();
    bottleRiddle.add(new HintComponent(bottleRiddleHints));
    bottleRiddle.add(bottleRiddlePlace);
    Game.add(bottleRiddle);

    EventScheduler.scheduleAction(() -> bottleRiddlePlace.produce(), 10000);
    EventScheduler.scheduleAction(() -> bottleRiddlePlace.produce(), 20000);

    staminaRiddle2 = new Entity("staminaRiddle2");
    staminaRiddle2.add(new HintComponent(staminaRiddle2Hints));
    staminaRiddle2Place = new PlaceComponent();
    staminaRiddle2.add(staminaRiddle2Place);
    Game.add(staminaRiddle2);

    PlaceComponent sync1 = new PlaceComponent();

    TransitionComponent t1 = new TransitionComponent();
    petriNetSystem.addInputArc(t1, staminaRiddlePlace, 2);
    petriNetSystem.addOutputArc(t1, sync1);

    TransitionComponent t2 = new TransitionComponent();
    petriNetSystem.addInputArc(t2, bottleRiddlePlace, 2);
    petriNetSystem.addOutputArc(t2, sync1);

    TransitionComponent t3 = new TransitionComponent();
    petriNetSystem.addInputArc(t3, leafRiddlePlace, 2);
    petriNetSystem.addOutputArc(t3, sync1);

    TransitionComponent t4 = new TransitionComponent();
    petriNetSystem.addInputArc(t4, sync1, 3);
    petriNetSystem.addOutputArc(t4, staminaRiddle2Place);

    // Remove Hints Riddle solved
    TransitionComponent t5 = new TransitionComponent();
    petriNetSystem.addInputArc(t5, staminaRiddle2Place, 4);

    ///  Fackelrätsel

    torchRiddle = new Entity("torchRiddle");
    torchRiddle.add(new HintComponent(torchRiddleHints));
    torchRiddlePlace = new PlaceComponent();
    torchRiddle.add(torchRiddlePlace);
    Game.add(torchRiddle);
    TorchItem.placeComponent(torchRiddlePlace);

    coalRiddle = new Entity("coalRiddle");
    coalRiddlePlace = new PlaceComponent();
    coalRiddle.add(new HintComponent(coalRiddleHints));
    coalRiddle.add(coalRiddlePlace);
    Game.add(coalRiddle);
    CoalItem.placeComponent(coalRiddlePlace);

    // Craft potion riddle
    stickRiddle = new Entity("stickRiddle");
    stickRiddlePlace = new PlaceComponent();
    stickRiddle.add(new HintComponent(stickRiddleHints));
    stickRiddle.add(stickRiddlePlace);
    Game.add(stickRiddle);

    TimerAPI.registerCallback(
        240,
        () -> {
          if (stickRiddlePlace.tokenCount() == 0) {
            stickRiddlePlace.produce();
          }
        });

    TransitionComponent t6 = new TransitionComponent();
    petriNetSystem.addInputArc(t6, torchRiddlePlace, 2);

    TransitionComponent t7 = new TransitionComponent();
    petriNetSystem.addInputArc(t7, coalRiddlePlace, 2);

    TransitionComponent t8 = new TransitionComponent();
    petriNetSystem.addInputArc(t8, stickRiddlePlace, 2);

    /// Eisrätsel

    iceRiddle = new Entity("iceRiddle");
    iceRiddle.add(new HintComponent(iceRiddleHints));
    iceRiddlePlace = new PlaceComponent();
    iceRiddle.add(iceRiddlePlace);
    Game.add(iceRiddle);

    iceRiddlePlace.produce();

    iceRingRiddle = new Entity("coalRiddle");
    iceRingRiddlePlace = new PlaceComponent();
    iceRingRiddle.add(new HintComponent(iceRingRiddleHints));
    iceRingRiddle.add(iceRingRiddlePlace);
    Game.add(iceRingRiddle);

    // Craft potion riddle
    blueGemRiddle = new Entity("blueGemRiddle");
    blueGemRiddlePlace = new PlaceComponent();
    blueGemRiddle.add(new HintComponent(blueGemRiddleHints));
    blueGemRiddle.add(blueGemRiddlePlace);
    Game.add(blueGemRiddle);
    BlueGemItem.placeComponent(blueGemRiddlePlace);

    TransitionComponent t9 = new TransitionComponent();
    petriNetSystem.addInputArc(t9, iceRiddlePlace, 2);
    petriNetSystem.addOutputArc(t9, iceRingRiddlePlace);

    TransitionComponent t10 = new TransitionComponent();
    petriNetSystem.addInputArc(t10, iceRingRiddlePlace, 2);
    petriNetSystem.addOutputArc(t10, blueGemRiddlePlace);

    TransitionComponent t11 = new TransitionComponent();
    petriNetSystem.addInputArc(t11, blueGemRiddlePlace, 2);

    /// Stärkerätsel

    strengthRiddle = new Entity("strengthRiddle");
    strengthRiddle.add(new HintComponent(strengthRiddleHints));
    strengthRiddlePlace = new PlaceComponent();
    strengthRiddle.add(strengthRiddlePlace);
    Game.add(strengthRiddle);

    redGemRiddle = new Entity("redGemRiddle");
    redGemRiddlePlace = new PlaceComponent();
    redGemRiddle.add(new HintComponent(redGemRiddleHints));
    redGemRiddle.add(redGemRiddlePlace);
    Game.add(redGemRiddle);
    RedGemItem.placeComponent(redGemRiddlePlace);

    TimerAPI.registerCallback(300, () -> redGemRiddlePlace.produce());

    // Craft potion riddle
    pushRiddle = new Entity("pushRiddle");
    pushRiddlePlace = new PlaceComponent();
    pushRiddle.add(new HintComponent(pushRiddleHints));
    pushRiddle.add(pushRiddlePlace);
    Game.add(pushRiddle);

    TransitionComponent t12 = new TransitionComponent();
    petriNetSystem.addInputArc(t12, strengthRiddlePlace, 2);
    petriNetSystem.addOutputArc(t12, pushRiddlePlace);

    TransitionComponent t13 = new TransitionComponent();
    petriNetSystem.addInputArc(t13, redGemRiddlePlace, 3);
    petriNetSystem.addOutputArc(t13, pushRiddlePlace);

    /// Ausbruchrätsel

    wallRiddle = new Entity("wallRiddle");
    wallRiddle.add(new HintComponent(wallRiddleHints));
    wallRiddlePlace = new PlaceComponent();
    wallRiddle.add(wallRiddlePlace);
    Game.add(wallRiddle);

    wallRiddlePlace.produce();

    wallRiddle2 = new Entity("wallRiddle2");
    wallRiddle2.add(new HintComponent(wallRiddle2Hints));
    wallRiddle2Place = new PlaceComponent();
    wallRiddle2.add(wallRiddle2Place);
    Game.add(wallRiddle2);
    PickaxeItem.placeComponent(wallRiddle2Place);
    AxeItem.placeComponent(wallRiddle2Place);

    TimerAPI.registerCallback(360, () -> wallRiddle2Place.produce());

    pickaxeRiddle = new Entity("pickaxeRiddle");
    pickaxeRiddlePlace = new PlaceComponent();
    pickaxeRiddle.add(new HintComponent(pickaxeRiddleHints));
    pickaxeRiddle.add(pickaxeRiddlePlace);
    Game.add(pickaxeRiddle);

    // Craft potion riddle
    axeRiddle = new Entity("axeRiddle");
    axeRiddlePlace = new PlaceComponent();
    axeRiddle.add(new HintComponent(axeRiddleHints));
    axeRiddle.add(axeRiddlePlace);
    Game.add(axeRiddle);
    LogItem.placeComponent(axeRiddlePlace);

    // Craft potion riddle
    bridgeRiddle = new Entity("bridgeRiddle");
    bridgeRiddlePlace = new PlaceComponent();
    bridgeRiddle.add(new HintComponent(bridgeRiddleHints));
    bridgeRiddle.add(bridgeRiddlePlace);
    Game.add(bridgeRiddle);
    RopeItem.placeComponent(bridgeRiddlePlace);

    TransitionComponent t14 = new TransitionComponent();
    petriNetSystem.addInputArc(t14, wallRiddle2Place, 3);
    petriNetSystem.addOutputArc(t14, pickaxeRiddlePlace);
    petriNetSystem.addOutputArc(t14, axeRiddlePlace);

    TransitionComponent t15 = new TransitionComponent();
    petriNetSystem.addInputArc(t15, axeRiddlePlace, 2);
    petriNetSystem.addOutputArc(t15, bridgeRiddlePlace);

    TransitionComponent t16 = new TransitionComponent();
    petriNetSystem.addInputArc(t16, bridgeRiddlePlace, 2);
  }

  // endregion

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
    Color.YELLOW,
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
    Color.YELLOW,
    Color.RED,
    Color.BLUE,
    Color.RED,
    Color.BLUE,
    Color.GREEN,
    Color.RED,
    Color.RED,
    Color.BLUE,
    Color.GREEN,
    Color.WHITE,
    Color.RED,
    Color.WHITE,
    Color.WHITE,
    Color.WHITE,
    Color.GREEN,
    Color.BLUE,
    Color.RED,
    Color.BLUE,
    Color.RED,
    Color.YELLOW,
    Color.WHITE,
    Color.WHITE,
    Color.WHITE,
    Color.WHITE,
    Color.WHITE,
    Color.WHITE,
    Color.WHITE,
    Color.WHITE
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
    setupHints();
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
              checkPosition(player);
            });

    if (!escaped
        && Game.allPlayers().allMatch(player -> player.isPresent(EscapedComponent.class))) {
      Game.allPlayers().forEach(this::escaped);
    }
  }

  Rectangle labyrinth1 = new Rectangle(getPoint("labyrinth11"), getPoint("labyrinth12"));
  Rectangle ice = new Rectangle(getPoint("fire11"), getPoint("fire12"));
  Rectangle hiddenRoom = new Rectangle(getPoint("hidden11"), getPoint("hidden12"));
  Rectangle push = new Rectangle(getPoint("temple11"), getPoint("temple12"));

  private void checkPosition(Entity player) {
    player
        .fetch(CollideComponent.class)
        .ifPresent(
            cc -> {
              Point position = cc.collider().absoluteCenter();
              if (!torchRiddlePlace.wasConsumed() && torchRiddlePlace.tokenCount() == 0) {
                if (labyrinth1.contains(position)) {
                  torchRiddlePlace.produce();
                }
              }
              if (!iceRiddlePlace.wasConsumed() && iceRiddlePlace.tokenCount() == 0) {
                if (ice.contains(position)) {
                  iceRiddlePlace.produce();
                }
              }
              if (!iceRingRiddlePlace.wasConsumed() && iceRingRiddlePlace.tokenCount() == 0) {
                if (hiddenRoom.contains(position)) {
                  iceRingRiddlePlace.produce();
                }
              }
              if (!strengthRiddlePlace.wasConsumed() && strengthRiddlePlace.tokenCount() == 0) {
                if (push.contains(position)) {
                  strengthRiddlePlace.produce();
                }
              }
            });
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

    if (!player.isPresent(HintLogComponent.class)) {
      player.add(new HintLogComponent(player.id()));
    }

    DialogUtils.showTextPopup(
        "Oh nein! Wo sind wir hier? Sieht aus als wären wir in ein Verlies gebracht worden. "
            + "Es muss hier doch einen Weg raus geben. Wir sollten uns hier mal umschauen.",
        "Gefangen",
        () -> {},
        player.id());
  }

  private void createChests() {
    Game.add(addLockpicking(MiscFactory.newChest(Set.of(new LeafItem()), getPoint("chest0"))));
    Game.add(addLockpicking(MiscFactory.newChest(Set.of(new CoalItem()), getPoint("chest1"))));
    Game.add(
        addLockpicking(MiscFactory.newChest(Set.of(new EmptyBottleItem()), getPoint("chest2"))));
    Game.add(addLockpicking(MiscFactory.newChest(Set.of(new RingGoldItem()), getPoint("chest3"))));
    Game.add(
        addLockpicking(MiscFactory.newChest(Set.of(new EmptyBottleItem()), getPoint("chest4"))));
    Game.add(
        addLockpicking(MiscFactory.newChest(Set.of(new EmptyBottleItem()), getPoint("chest5"))));
    Game.add(
        addLockpicking(MiscFactory.newChest(Set.of(new EmptyBottleItem()), getPoint("chest6"))));
    Game.add(
        addLockpicking(MiscFactory.newChest(Set.of(new RingSilverItem()), getPoint("chest7"))));
    Game.add(addLockpicking(MiscFactory.newChest(Set.of(new GoldItem()), getPoint("chest8"))));
    // Labyrinth
    Game.add(addLockpicking(MiscFactory.newChest(Set.of(new RedGemItem()), getPoint("chest9"))));
    Game.add(addLockpicking(MiscFactory.newChest(Set.of(new BlueGemItem()), getPoint("chest10"))));
    Game.add(addLockpicking(MiscFactory.newChest(Set.of(new MetalItem()), getPoint("chest11"))));
    Game.add(addLockpicking(MiscFactory.newChest(Set.of(new RedGemItem()), getPoint("chest12"))));
    //
    Game.add(addLockpicking(MiscFactory.newChest(Set.of(new CoalItem()), getPoint("chest13"))));
    Game.add(addLockpicking(MiscFactory.newChest(Set.of(new RingGoldItem()), getPoint("chest14"))));
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
                      interactor
                          .fetch(IllegalComponent.class)
                          .ifPresent(
                              ic -> {
                                ic.addReason(IllegalComponent.Reason.STEALING);
                              });

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
                          () ->
                              interactor
                                  .fetch(IllegalComponent.class)
                                  .ifPresent(
                                      ic -> ic.removeReason(IllegalComponent.Reason.STEALING)));
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
      ui.registerCallback(
          DialogContextKeys.ON_CLOSE,
          (data) ->
              interactor
                  .fetch(IllegalComponent.class)
                  .ifPresent(ic -> ic.removeReason(IllegalComponent.Reason.STEALING)));
      interactor.add(ui);
    };
  }

  private void createTreeChest() {
    Entity chest = MiscFactory.newChest(Set.of(new StickItem()), getPoint("TreeChest"));
    chest
        .fetch(DrawComponent.class)
        .ifPresent(
            dc -> {
              dc.depth(DepthLayer.Normal.depth());
            });
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
                                  if (stickRiddlePlace.tokenCount() == 1) {
                                    stickRiddlePlace.produce();
                                  }
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
                  createStone(index, pos);
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
    pushStone.add(new CollideComponent(Vector2.of(0.125f, 0.125f), Vector2.of(0.75f, 0.75f)));
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

              Entity pp;
              if (index == 21) {
                pp =
                    LeverFactory.pressurePlate(
                        platePos,
                        1.2f,
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
                        });
              } else if (index == 22) {
                pp =
                    LeverFactory.pressurePlate(
                        platePos,
                        1.2f,
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
                        });
              } else if (index == 23) {
                pp =
                    LeverFactory.pressurePlate(
                        platePos,
                        1.2f,
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
                        });
              } else if (index == 24) {
                pp =
                    LeverFactory.pressurePlate(
                        platePos,
                        1.2f,
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
                        });
              } else if (index == 25) {
                pp =
                    LeverFactory.pressurePlate(
                        platePos,
                        1.2f,
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
                        });
              } else if (index == 26) {
                pp =
                    LeverFactory.pressurePlate(
                        platePos,
                        1.2f,
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
                        });
              } else {
                Point doorPos = getPoint("push_door0");
                if (index < 24) {
                  doorPos = getPoint("push_door" + index);
                }
                if (index == 27) {
                  doorPos = getPoint("push_door23");
                }
                if (index == 28) {
                  doorPos = getPoint("push_door24");
                }
                DoorTile doorTile = (DoorTile) tileAt(doorPos).orElseThrow();
                doorTile.close();
                DoorTile doorTile2;
                if (index == 3) {
                  Point doorPos2 = getPoint("push_door21");
                  doorTile2 = (DoorTile) tileAt(doorPos2).orElseThrow();
                  doorTile2.close();
                } else if (index == 4) {
                  Point doorPos2 = getPoint("push_door22");
                  doorTile2 = (DoorTile) tileAt(doorPos2).orElseThrow();
                  doorTile2.close();
                } else {
                  doorTile2 = null;
                }
                pp =
                    LeverFactory.pressurePlate(
                        platePos,
                        1.2f,
                        new ICommand() {
                          public void execute() {
                            Sounds.DOOR_OPEN_SOUND.play();
                            doorTile.open();
                            if (index == 3 || index == 4) {
                              doorTile2.open();
                            }
                          }

                          public void undo() {
                            Sounds.DOOR_CLOSE_SOUND.play();
                            doorTile.close();
                            if (index == 3 || index == 4) {
                              doorTile2.close();
                            }
                          }
                        });
              }
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
                                    .ifPresent(
                                        vc -> {
                                          if (plateColors[index] == Color.WHITE) {
                                            other
                                                .fetch(PlayerComponent.class)
                                                .ifPresent(
                                                    pc -> {
                                                      pressurePlateComponent.increase(vc.mass());
                                                    });
                                          } else {
                                            pressurePlateComponent.increase(vc.mass());
                                          }
                                        });
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
                                    .ifPresent(
                                        vc -> {
                                          if (plateColors[index] == Color.WHITE) {
                                            other
                                                .fetch(PlayerComponent.class)
                                                .ifPresent(
                                                    pc -> {
                                                      pressurePlateComponent.decrease(vc.mass());
                                                    });
                                          } else {
                                            pressurePlateComponent.decrease(vc.mass());
                                          }
                                        });
                              }
                            });
                  };
              pp.add(new CollideComponent(onCollideEnter, onCollideLeave).isSolid(false));

              Game.add(pp);
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

  /**
   * Adds the default hero movement callbacks to the given input component.
   *
   * @param inputComp The input component to add the callbacks to.
   */
  public static void addCallbacks(InputComponent inputComp) {
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
    Tile currentTile = Game.tileAt(currentPos).orElse(null);

    if (currentTile == null) {
      return;
    }

    if (hero.isPresent(IceMovementComponent.class)) {
      addCallbacks(ic);
      ic.deactivateControls(false);
      vc.onWallHit(e -> {});
      hero.remove(FlyComponent.class);
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

    DialogUtils.showTextPopup(
        "Du bist entkommen!",
        "ENTKOMMEN!",
        () -> {
          Game.exit("Level completed - players escaped.");
        });
  }
}
