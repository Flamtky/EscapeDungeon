package demoDungeon.level;

import analytics.DungeonAnalyticsAPI;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.utils.TimeUtils;
import contrib.components.*;
import contrib.components.CollideComponent;
import contrib.components.FlyComponent;
import contrib.entities.CharacterClass;
import contrib.entities.LeverFactory;
import contrib.entities.MiscFactory;
import contrib.entities.NPCFactory;
import contrib.entities.deco.Deco;
import contrib.entities.deco.DecoFactory;
import contrib.hud.DialogUtils;
import contrib.hud.dialogs.ChoiceOption;
import contrib.hud.dialogs.DialogContext;
import contrib.hud.dialogs.DialogContextKeys;
import contrib.hud.dialogs.DialogFactory;
import contrib.hud.dialogs.DialogType;
import contrib.item.Item;
import contrib.modules.interaction.Interaction;
import contrib.modules.interaction.InteractionComponent;
import contrib.systems.EventScheduler;
import contrib.systems.PositionSync;
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
import core.network.messages.c2s.DialogResponseMessage;
import core.network.messages.c2s.InputMessage;
import core.systems.CameraSystem;
import core.systems.DrawSystem;
import core.systems.InputManager;
import core.utils.*;
import core.utils.Direction;
import core.utils.Point;
import core.utils.TriConsumer;
import core.utils.Vector2;
import core.utils.components.draw.DepthLayer;
import core.utils.components.draw.TextureGenerator;
import core.utils.components.path.SimpleIPath;
import escapeDungeon.components.AxeComponent;
import escapeDungeon.components.EscapedComponent;
import escapeDungeon.components.IceMovementComponent;
import escapeDungeon.items.*;
import escapeDungeon.skill.SprintSkill;
import guard.GuardBuilder;
import hint.*;
import java.util.*;
import java.util.function.Consumer;
import mobs.EscapeRoomMonsterBuilder;
import mushRoom.Sounds;
import mushRoom.modules.journal.CraftingBookItem;
import mushRoom.modules.lockpick.LockPickDialog;
import mushRoom.modules.lockpick.LockPickDifficulty;
import mushRoom.modules.slides.SlideDeckDialog;
import mushRoom.shaders.TorchPostProcessing;
import petriNet.PetriNetSystem;
import petriNet.PlaceComponent;
import petriNet.TransitionComponent;
import replay.ReplayControlSystem;
import replay.ReplayFreeCameraSystem;
import replay.ReplayPositionOffset;
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
  private final Map<Integer, Entity> pushStonesByIndex = new HashMap<>();
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
  private final List<Entity> mapCameraReturnTargets = new ArrayList<>();
  private final List<Entity> replayCameraReturnTargets = new ArrayList<>();
  private final Map<Entity, Boolean> replayHiddenPlayerVisibility = new HashMap<>();
  private static final String MAP_CAMERA_FREEZE_MODIFIER = "reporter_map_camera_freeze";
  private static final String SCROLL_IMAGE = "items/rpg/item_scroll.png";
  private static final String GUARD_PORTRAIT = "@gen/reporter_guard_idle.png";
  private static final String BLUE_GUARD_PORTRAIT = "@gen/reporter_blue_guard_idle.png";
  private static final String WIZARD_PORTRAIT = "@gen/reporter_wizard_idle.png";
  private static final String APPRENTICE_PORTRAIT = "@gen/reporter_apprentice_idle.png";
  private static final String ROGUE_PORTRAIT = "@gen/reporter_rogue_idle.png";
  private static final String THESIS_IMAGE_BASE = "images/thesis/";
  private static final String SAMPLE_SELECTION_IMAGE =
      THESIS_IMAGE_BASE + "sample_selection_flow.png";
  private static final String AGE_DISTRIBUTION_IMAGE =
      THESIS_IMAGE_BASE + "age_distribution_pie_chart.png";
  private static final String SESSION_DURATION_IMAGE =
      THESIS_IMAGE_BASE + "session_duration_histogram.png";
  private static final String CHALLENGE_OUTCOMES_IMAGE =
      THESIS_IMAGE_BASE + "challenge_outcomes_overview.png";
  private static final String LOCKPICK_MINIGAME_IMAGE =
      THESIS_IMAGE_BASE + "screenshot_minigame.png";
  private static final String HINT_REQUESTS_IMAGE =
      THESIS_IMAGE_BASE + "player_hint_requests_boxplot.png";
  private static final String TEAM_HINTS_COLLABORATION_IMAGE =
      THESIS_IMAGE_BASE + "team_post_collaboration_vs_total_hints.png";
  private static final String PETRINETZ_WALL_ESCAPE_IMAGE =
      THESIS_IMAGE_BASE + "petrinetz_ausbruch_wand.png";
  private static final String PLAYER_CAPTURES_IMAGE =
      THESIS_IMAGE_BASE + "player_captures_boxplot.png";
  private static final String TEAM_BALANCE_CAPTURE_TIME_IMAGE =
      THESIS_IMAGE_BASE + "team_difficulty_balance_vs_capture_time.png";
  private static final String ESCAPE_EXPERIENCE_CAPTURE_IMAGE =
      THESIS_IMAGE_BASE + "escape_room_experience_vs_captured_count.png";
  private static final String PRE_POST_SKILLS_IMAGE =
      THESIS_IMAGE_BASE + "pre_post_future_skills.png";
  private static final String STAMINA_ADAPTABILITY_IMAGE =
      THESIS_IMAGE_BASE + "player_adaptability_vs_avg_stamina.png";
  private static final String LEARNING_PREFERENCES_ENJOYMENT_IMAGE =
      THESIS_IMAGE_BASE + "learning_preferences_vs_enjoyment.png";
  private static final String ENJOYMENT_PROGRESS_IMAGE =
      THESIS_IMAGE_BASE + "player_enjoyment_vs_solved_minus_failed.png";
  private static final String SELF_REGULATION_CAPTURE_IMAGE =
      THESIS_IMAGE_BASE + "player_self_regulation_vs_captured_count.png";
  private static final String IPQ_SUBSCALES_IMAGE = THESIS_IMAGE_BASE + "ipq_subscales_bar.png";
  private static final String MOVEMENT_CAPTURE_MAP_IMAGE =
      THESIS_IMAGE_BASE + "level_layout_with_movement_capture_overlay.jpg";
  private static final String IMAGE_OPTION_MARKER = "[img=items/rpg/item_paper_map.png] ";
  private static final int ZOOM_OUT_SLIDE_DECK_KEY = Input.Keys.NUMPAD_0;
  private static final Map<Integer, List<Integer>> SLIDE_DECKS_BY_NUMPAD =
      Map.ofEntries(
          Map.entry(Input.Keys.NUMPAD_1, List.of(1, 2)),
          Map.entry(Input.Keys.NUMPAD_2, List.of(3, 5, 6)),
          Map.entry(Input.Keys.NUMPAD_3, List.of(7, 8, 9, 10, 11)),
          Map.entry(Input.Keys.NUMPAD_4, List.of(12, 13, 14, 15)),
          Map.entry(Input.Keys.NUMPAD_5, List.of(16, 17, 18)),
          Map.entry(Input.Keys.NUMPAD_6, List.of(20, 21, 22, 23, 24)),
          Map.entry(Input.Keys.NUMPAD_7, List.of()),
          Map.entry(Input.Keys.NUMPAD_8, List.of()),
          Map.entry(Input.Keys.NUMPAD_9, List.of()),
          Map.entry(
              Input.Keys.NUMPAD_0,
              List.of(1, 2, 3, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19)));
  private static final long REPORTER_CAPTURE_ARM_TIMEOUT_MS = 20_000;
  private static final float PUSH_RIDDLE_REPORTER_TRIGGER_X = 71f;

  private static final long LEAF_REWARD_COOLDOWN_MS = 10_000;
  private long nextLeafRewardAtMs;

  private boolean escaped = false;
  private boolean pushRiddleFollowUpAvailable = false;
  private boolean pushRiddleCompletionScheduled = false;
  private boolean mapCameraActive = false;
  private boolean replayModeActive = false;
  private Entity mapCameraEntity;
  private Entity mapCameraUser;
  private Entity replayCameraEntity;
  private Entity replayCameraUser;
  private Entity reporterCaptureGuard;
  private ReplayControlSystem reporterReplayControlSystem;
  private float previousCameraZoom = CameraSystem.DEFAULT_ZOOM_FACTOR;
  private float previousReplayCameraZoom = CameraSystem.DEFAULT_ZOOM_FACTOR;
  private boolean reporterCaptureInProgress = false;
  private int replayModeClosedAtTick = -1;
  private Entity pushRiddleReporterRogue;
  private Entity pushRiddleApprentice;
  private final Set<String> captureKolloquiumSeenTopics = new HashSet<>();

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
    createPlacedTorches();
    Game.add(MiscFactory.newCraftingCauldron(getPoint("crafting0")));
    delayLeafReward();
    setupHints();
    setupReporterPresentation();

    Game.tileAt(new Point(12, 94)).ifPresent(tile -> tile.designLabel(DesignLabel.BEIGECASTLE));
    refreshLevelTextures();
  }

  private void createPlacedTorches() {
    for (int i = 1; i <= 14; i++) {
      Game.add(DecoFactory.createDeco(getPoint("coll_torch" + i), Deco.TorchGrayAnimatedPlaced));
    }
  }

  @Override
  protected void onTick() {
    if (replayToggleKeyPressed()) {
      toggleReporterReplayMode();
    }

    if (mapCameraActive && InputManager.isKeyJustPressed(Input.Keys.ESCAPE)) {
      closeReporterMapCamera();
    }

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
              if (pushRiddleFollowUpAvailable
                  && !pushRiddleCompletionScheduled
                  && pushRiddleApprentice == null
                  && pushRiddleReporterRogue != null
                  && position.x() < PUSH_RIDDLE_REPORTER_TRIGGER_X) {
                pushRiddleCompletionScheduled = true;
                EventScheduler.scheduleAction(
                    () -> solvePushRiddleForReporter(pushRiddleReporterRogue), 10_000);
              }
            });
  }

  private void setupReporterPresentation() {
    registerReporterPortraits();
    Game.add(createReporterMap());
    Game.add(
        createReporterNpc(
            "coll_1",
            "character/knight",
            "Mit der Wache sprechen",
            this::showEscapeRoutesKolloquiumDialog));
    Game.add(
        createReporterNpc(
            "coll_2",
            "character/blue_knight",
            "Mit dem Gefängnisdirektor sprechen",
            this::showDirectorKolloquiumDialog));
    Game.add(
        createReporterNpc(
            "coll_3",
            "character/wizard",
            "Mit dem Zauberer sprechen",
            this::showWizardKolloquiumDialog));
    Game.add(
        createReporterNpc(
            "coll_4",
            "character/char03",
            "Mit dem Apprentice sprechen",
            this::showCraftingKolloquiumDialog));
    Game.add(createPushRiddleReporterNpc());
    Game.add(
        createReporterNpc(
            "coll_6",
            "character/knight",
            "Mit der Wandwache sprechen",
            this::showWallKolloquiumDialog));
    Game.add(
        createReporterNpc(
            "coll_7",
            "character/char03",
            "Vor dem illegalen Bereich sprechen",
            this::showIllegalAreaKolloquiumDialog));
    createReporterCaptureGuard();
    Game.add(
        createReporterNpc(
            "coll_9", "character/rogue", "Mit Rogue sprechen", this::showRogueKolloquiumDialog));
    Game.add(
        createReporterNpc(
            "coll_11", "character/rogue", "Mit Rogue sprechen", this::showIceKolloquiumDialog));
  }

  private void registerReporterPortraits() {
    if (Game.isHeadless()) {
      return;
    }
    TextureGenerator.registerSpritesheetRegionTexture(
        "character/knight/knight.png", 0, 224, 16, 28, GUARD_PORTRAIT);
    TextureGenerator.registerSpritesheetRegionTexture(
        "character/blue_knight/blue_knight.png", 0, 72, 16, 28, BLUE_GUARD_PORTRAIT);
    TextureGenerator.registerSpritesheetRegionTexture(
        "character/wizard/wizard.png", 0, 0, 16, 28, WIZARD_PORTRAIT);
    TextureGenerator.registerSpritesheetRegionTexture(
        "character/char03/char03.png", 0, 0, 32, 32, APPRENTICE_PORTRAIT);
    TextureGenerator.registerSpritesheetRegionTexture(
        "character/rogue/rogue.png", 0, 0, 32, 32, ROGUE_PORTRAIT);
  }

  private Entity createReporterNpc(
      String pointName, String spritePath, String interactionName, Consumer<Entity> onInteract) {
    Entity npc = createNonCollidingNpc(reporterNpcPosition(pointName, spritePath), spritePath);
    npc.add(
        new InteractionComponent(
            () -> new Interaction((entity, who) -> onInteract.accept(who), 2.5f, interactionName)));
    return npc;
  }

  private Point reporterNpcPosition(String pointName, String spritePath) {
    Point position = getPoint(pointName);
    if (spritePath.equals("character/char03")) {
      return offsetReporterPosition(position, CharacterClass.APPRENTICE);
    }
    if (spritePath.equals("character/rogue")) {
      return offsetReporterPosition(position, CharacterClass.ROGUE);
    }
    return position;
  }

  private Entity createPushRiddleReporterNpc() {
    Entity npc =
        createNonCollidingNpc(
            offsetReporterPosition(getPoint("coll_5"), CharacterClass.ROGUE), "character/rogue");
    pushRiddleReporterRogue = npc;
    npc.add(
        new InteractionComponent(
            () ->
                new Interaction(
                    this::showPushRiddleDialog, 2.5f, "Über das Push-Riddle sprechen")));
    return npc;
  }

  private Entity createReporterMap() {
    Entity map = new Entity("reporter_map");
    map.add(new PositionComponent(getPoint("coll_map").translate(0, -1)));
    map.add(new DrawComponent(new SimpleIPath(SCROLL_IMAGE)));
    map.add(new CollideComponent(Vector2.ZERO, Vector2.ONE).isSolid(false));
    map.add(
        new InteractionComponent(
            () ->
                new Interaction(
                    (entity, who) -> showReporterMapDialog(who),
                    2.5f,
                    "Verzauberte Karte ansehen")));
    return map;
  }

  private void createReporterCaptureGuard() {
    GuardBuilder guardBuilder = (GuardBuilder) EscapeRoomMonsterBuilder.GUARD.builder();
    guardBuilder.alertnessThreshold(1, 0, true);
    guardBuilder.viewConeAngle(360f);
    guardBuilder.viewRange(12f);
    guardBuilder.addToGame();
    guardBuilder.speed(3.5f);
    guardBuilder.captureFailureDialog(
        "Einzelhaft",
        "Mach dir nix draus. Du gehörst zu 824 anderen Spieler die auch schon hier festsaßen. Du darfst in 5 Sekunden weiter.");
    guardBuilder.returnToAfterCapture(getPoint("coll_8"), 5_000);
    guardBuilder.onCaptureFinished(
        (guardEntity, playerEntity) -> finishReporterCapture(playerEntity));
    Entity guard = guardBuilder.build(getPoint("coll_8"));
    guard.add(
        new InteractionComponent(
            () ->
                new Interaction(
                    (entity, who) -> showCaptureGuardDialog(who),
                    2.5f,
                    "Mit der illegalen Wache sprechen")));
    reporterCaptureGuard = guard;
  }

  private record KolloquiumTopic(
      String label,
      String value,
      String reporterLine,
      String npcSpeaker,
      String npcPortrait,
      List<String> npcResponses,
      List<String> imagePaths) {}

  private static KolloquiumTopic topic(
      String label,
      String value,
      String reporterLine,
      String npcSpeaker,
      String npcPortrait,
      String... npcResponses) {
    return new KolloquiumTopic(
        label, value, reporterLine, npcSpeaker, npcPortrait, List.of(npcResponses), List.of());
  }

  private static KolloquiumTopic imageTopic(
      String label,
      String value,
      String reporterLine,
      String npcSpeaker,
      String npcPortrait,
      String imagePath,
      String... npcResponses) {
    return imageTopic(
        label, value, reporterLine, npcSpeaker, npcPortrait, List.of(imagePath), npcResponses);
  }

  private static KolloquiumTopic imageTopic(
      String label,
      String value,
      String reporterLine,
      String npcSpeaker,
      String npcPortrait,
      List<String> imagePaths,
      String... npcResponses) {
    return new KolloquiumTopic(
        label, value, reporterLine, npcSpeaker, npcPortrait, List.of(npcResponses), imagePaths);
  }

  private void showKolloquiumMenu(
      Entity who, String speaker, String portrait, String greeting, List<KolloquiumTopic> topics) {
    showKolloquiumMenu(who, speaker, portrait, greeting, topics, false);
  }

  private void showKolloquiumMenu(
      Entity who,
      String speaker,
      String portrait,
      String greeting,
      List<KolloquiumTopic> topics,
      boolean followUp) {
    List<ChoiceOption> options =
        new ArrayList<>(topics.stream().map(MADungeonRoom::choiceFor).toList());
    options.add(ChoiceOption.of("Schließen", "close"));
    showMADialogChoices(
        speakerLine(speaker, portrait, followUp ? "Wählen Sie ein Thema." : greeting),
        "",
        options,
        false,
        payload ->
            topics.stream()
                .filter(topic -> topic.value().equals(stringValue(payload)))
                .findFirst()
                .ifPresent(
                    topic ->
                        showKolloquiumTopic(
                            who,
                            topic,
                            () ->
                                showKolloquiumMenu(
                                    who, speaker, portrait, greeting, topics, true))),
        () -> {},
        who.id());
  }

  private void showKolloquiumTopic(Entity who, KolloquiumTopic topic, Runnable onFinished) {
    showMADialogDialog(topicDialog(who, topic), onFinished, who.id());
  }

  private String topicDialog(Entity who, KolloquiumTopic topic) {
    StringBuilder dialog =
        new StringBuilder(speakerLine("Reporter", reporterPortrait(who), topic.reporterLine()));
    for (int index = 0; index < topic.npcResponses().size(); index++) {
      dialog
          .append("[p]")
          .append(
              speakerLine(
                  topic.npcSpeaker(), topic.npcPortrait(), topic.npcResponses().get(index)));
      if (index == topic.npcResponses().size() - 1) {
        topic.imagePaths().forEach(imagePath -> dialog.append(thesisImage(imagePath)));
      }
    }
    return dialog.toString();
  }

  private static ChoiceOption choiceFor(KolloquiumTopic topic) {
    if (topic.imagePaths().isEmpty()) {
      return ChoiceOption.of(topic.label(), topic.value());
    }
    return imageOption(topic.label(), topic.value());
  }

  private List<KolloquiumTopic> mapTopics() {
    return List.of(
        topic(
            "Levelaufbau",
            "level",
            "Wie ist dieses Gefängnis aufgebaut?",
            "Verzauberte Karte",
            enchantedMapPortrait(),
            "Das Level ist nicht nur Kulisse. Zellen, Innenhof, Bibliothek, Labyrinth,"
                + " Eisbereich, Schieberätsel, Wand-Ausbruch und Ausgang sind so angeordnet,"
                + " dass Wege, Risiken und Ressourcen geplant werden müssen."),
        topic(
            "Legale und illegale Bereiche",
            "legal_illegal",
            "Was bedeutet legal und illegal im Level?",
            "Verzauberte Karte",
            enchantedMapPortrait(),
            "Im legalen Bereich ist Bewegung grundsätzlich erlaubt. Im illegalen Bereich ist"
                + " schon der Aufenthalt riskant. Dort greifen Wachen, Alertness und Verfolgung"
                + " deutlich stärker."),
        topic(
            "Spielstruktur",
            "structure",
            "Wie führt der Level durch das Spiel?",
            "Verzauberte Karte",
            enchantedMapPortrait(),
            "Erst erkunden die Spieler frei, sammeln Items und lernen Crafting. Danach"
                + " verdichtet das Labyrinth den Ablauf. Am Ende gibt es drei Ausbruchspfade:"
                + " Wand, Eisbereich und Schieberätsel."),
        topic(
            "Future-Skill-Bezug",
            "future_skills",
            "Warum ist der Level für Future Skills wichtig?",
            "Verzauberte Karte",
            enchantedMapPortrait(),
            "Future Skills entstehen hier aus Spielsituationen: Zusammenarbeit, Anpassung,"
                + " Selbstregulation und Problem Solving werden durch Wege, Wachen, Ressourcen"
                + " und Rätsel sichtbar."));
  }

  private void showEscapeRoutesKolloquiumDialog(Entity who) {
    showKolloquiumMenu(
        who,
        "Wache",
        guardPortrait(),
        "Ich bin eine Wache dieses Trakts. Ich sichere nach dem Ausbruch die bekannten"
            + " Fluchtwege. Klar ist: Es gab nicht nur eine Schwachstelle.",
        List.of(
            topic(
                ">Drei Ausbruchspfade",
                "paths",
                "Welche Fluchtwege gab es?",
                "Wache",
                guardPortrait(),
                "Es gab drei große Pfade: den Wand-Ausbruch, den Eisbereich und das"
                    + " Schieberätsel. Jeder Pfad fordert andere Mechaniken und andere Formen"
                    + " von Denken."),
            topic(
                "Ein Pfad reicht",
                "one_path",
                "Warum musste nicht alles gelöst werden?",
                "Wache",
                guardPortrait(),
                "Alle drei Wege wären für eine Sitzung zu lang. Ein erfolgreicher Pfad reicht."
                    + " So gibt es Wahlfreiheit, aber die Spielzeit bleibt kontrollierbar."),
            topic(
                "Labyrinth als Vorbereitung",
                "labyrinth",
                "Warum ist das Labyrinth trotzdem zentral?",
                "Wache",
                guardPortrait(),
                "Viele wichtige Items liegen im Labyrinth. Die Ausbruchsideen sind alternativ,"
                    + " aber Ressourcen und Vorbereitung führen die Teams in einen gemeinsamen"
                    + " Risikobereich."),
            topic(
                "Auswertung der Wege",
                "analysis",
                "Warum ist diese Struktur für die Evaluation interessant?",
                "Wache",
                guardPortrait(),
                "Die Daten zeigen, welche Wege gesucht wurden, wo Hinweise nötig waren, wo"
                    + " Teams festhingen und welche Spielparameter die Ausbruchspfade geprägt"
                    + " haben."),
            topic(
                ">Kisten und Ressourcen",
                "resources",
                "Welche Rolle spielten Kisten?",
                "Wache",
                guardPortrait(),
                "Kisten sind Ressourcenzugänge. Sie verbinden Exploration, Inventar und"
                    + " späteres Crafting. Spieler müssen entscheiden, welche Items für den"
                    + " Ausbruch wichtig sein könnten."),
            imageTopic(
                "Lockpick-Minigame",
                "lockpick",
                "Wie funktioniert das Lockpick-Minigame?",
                "Wache",
                guardPortrait(),
                LOCKPICK_MINIGAME_IMAGE,
                "Mehrere Ringe müssen richtig rotiert werden. Das erzeugt ein kurzes"
                    + " Problem-Solving-Element, bevor die Kiste geöffnet und der Loot"
                    + " aufgenommen wird."),
            imageTopic(
                ">Lockpick-Daten",
                "lockpick_data",
                "Was zeigen die Daten?",
                "Wache",
                guardPortrait(),
                CHALLENGE_OUTCOMES_IMAGE,
                "Es gab 387 gelöste und 4 fehlgeschlagene Lock-Pick-Ereignisse. Der"
                    + " Failure-Anteil lag damit bei etwa 1 Prozent. Lockpicking war häufig,"
                    + " aber kaum blockierend.")));
  }

  private void showDirectorKolloquiumDialog(Entity who) {
    showKolloquiumMenu(
        who,
        "Gefängnisdirektor",
        directorPortrait(),
        "Ich bin der Gefängnisdirektor. Nach dem Ausbruch sitze ich hier über den"
            + " Evaluierungsakten. Die Mauern haben versagt, aber die Daten sind vollständig.",
        List.of(
            imageTopic(
                "Stichprobe",
                "sample",
                "Auf welcher Datenbasis beruht die Evaluation?",
                "Gefängnisdirektor",
                directorPortrait(),
                SAMPLE_SELECTION_IMAGE,
                "25 bereinigte Sessions, 46 surveyverknüpfte Spielerfälle und 21 vollständige"
                    + " Team-Sessions bilden die zentrale Grundlage."),
            imageTopic(
                "Demografie",
                "demographics",
                "Wer hat an der Evaluation teilgenommen?",
                "Gefängnisdirektor",
                directorPortrait(),
                AGE_DISTRIBUTION_IMAGE,
                "Die Altersverteilung war gemischt: 39,13 Prozent waren 17 oder jünger,"
                    + " 23,91 Prozent 18 bis 24 und 36,96 Prozent 25 bis 34 Jahre alt. Das ist"
                    + " wichtiger Kontext für die Interpretation."),
            imageTopic(
                "Sessiondauer",
                "duration",
                "Wie lange dauerten die Sessions?",
                "Gefängnisdirektor",
                directorPortrait(),
                SESSION_DURATION_IMAGE,
                "24 abgeschlossene Sessions dauerten im Mittel 80,49 Minuten, Median 77,78"
                    + " Minuten. Die Spannweite lag zwischen 35,11 und 135,31 Minuten. Für das"
                    + " Kolloquium zeigen wir daher ausgewählte Stationen."),
            topic(
                "xAPI-Eventdaten",
                "xapi",
                "Welche Spielereignisse wurden ausgewertet?",
                "Gefängnisdirektor",
                directorPortrait(),
                "Insgesamt wurden 677.437 xAPI-orientierte Statements betrachtet. Dazu gehören"
                    + " Bewegung, Skills, Interaktionen, Lösungen, Items, Fehlversuche, Detection,"
                    + " Captures, Erschöpfung und Hinweise."),
            topic(
                "Survey-Ergebnisse",
                "survey",
                "Wie wurde das Spiel bewertet?",
                "Gefängnisdirektor",
                directorPortrait(),
                "Spielspaß lag bei 4,15, Usability bei 3,98 und Balancierung bei 3,72."
                    + " Collaboration lag bei 4,15, Selbstregulation bei 3,80 und Future Skills"
                    + " gesamt bei 3,70."),
            imageTopic(
                "IPQ / Presence",
                "presence",
                "Was zeigte der IPQ?",
                "Gefängnisdirektor",
                directorPortrait(),
                IPQ_SUBSCALES_IMAGE,
                "Presence gesamt lag bei 2,78. Spatial Presence war mit 3,09 am stärksten."
                    + " Presence ist hier ein Erlebnisindikator, kein direkter Lernnachweis."),
            imageTopic(
                "Pre-Post-Tests",
                "pre_post",
                "Was zeigten die Pre-Post-Tests?",
                "Gefängnisdirektor",
                directorPortrait(),
                PRE_POST_SKILLS_IMAGE,
                "Bei n = 44 stieg Selbstregulation von 3,31 auf 3,80, Holm p = 0,0022."
                    + " Future Skills gesamt stieg von 3,53 auf 3,70, Holm p = 0,028. Das ist"
                    + " eine Selbsteinschätzungsverschiebung, kein harter Kompetenznachweis."),
            imageTopic(
                "Stamina und Anpassungsfähigkeit",
                "stamina_adaptability",
                "Was zeigt Stamina über Anpassungsfähigkeit?",
                "Gefängnisdirektor",
                directorPortrait(),
                STAMINA_ADAPTABILITY_IMAGE,
                "Höhere durchschnittliche Stamina hing mit höherer Post-Anpassungsfähigkeit"
                    + " zusammen: rho 0,5055, p = 0,0005. Das verbindet Spielverhalten mit"
                    + " Selbsteinschätzung."),
            imageTopic(
                "Lernpräferenzen und Spielspaß",
                "learning_enjoyment",
                "Warum sind Lernpräferenzen relevant?",
                "Gefängnisdirektor",
                directorPortrait(),
                LEARNING_PREFERENCES_ENJOYMENT_IMAGE,
                "Präferenz für spielerisches Lernen hing positiv mit Spielspaß zusammen:"
                    + " rho 0,4469, p = 0,0024. Das zeigt, für welche Zielgruppe das Konzept"
                    + " besonders gut passt."),
            topic(
                "Korrelationen",
                "correlations",
                "Welche Zusammenhänge waren wichtig?",
                "Gefängnisdirektor",
                directorPortrait(),
                "Die Korrelationsdiagramme zeigen Zusammenhänge, keine Kausalität:"
                    + " Spielspuren wie Stamina, Captures, Gefangenschaftszeit und"
                    + " Lernpräferenzen werden mit Survey-Werten verbunden."),
            topic(
                "Qualitative Befunde",
                "qualitative",
                "Was ergänzten die Freitextantworten?",
                "Gefängnisdirektor",
                directorPortrait(),
                "Kommunikation, Absprache und Teamwork wurden stark genannt. Gleichzeitig"
                    + " wurden Rätsel, Ressourcen, Orientierung, Wachen und Ausdauer als"
                    + " Herausforderung oder Frustquelle sichtbar."),
            topic(
                "Grenzen der Aussage",
                "limits",
                "Wie vorsichtig muss man die Ergebnisse formulieren?",
                "Gefängnisdirektor",
                directorPortrait(),
                "Die Daten zeigen Hinweise, Zusammenhänge und wahrgenommene Skill-Relevanz."
                    + " Ohne Kontrollgruppe und mit teilweise explorativen Skalen beweisen sie"
                    + " aber keine kausale Future-Skill-Förderung.")));
  }

  private void showWizardKolloquiumDialog(Entity who) {
    showKolloquiumMenu(
        who,
        "Zauberer",
        wizardPortrait(),
        "Ich bin der Zauberer des Gefängnisses. Ich hüte hier die Hinweise. Ich löse keine"
            + " Rätsel, aber ich gebe Hinweise, wenn man mich fragt.",
        List.of(
            topic(
                ">Grundidee des Hilfesystems",
                "help",
                "Warum gibt es das Hilfesystem?",
                "Zauberer",
                wizardPortrait(),
                "Es soll Frust reduzieren, ohne automatisch zu lösen. Spieler müssen Hinweise"
                    + " aktiv anfordern. Dadurch bleiben Exploration und Problem Solving erhalten."),
            imageTopic(
                ">Petrinetz-Logik",
                "petri",
                "Wie werden passende Hinweise ausgewählt?",
                "Zauberer",
                wizardPortrait(),
                PETRINETZ_WALL_ESCAPE_IMAGE,
                "Petrinetze steuern aktive Rätselschritte. Marken zeigen, welche Schritte"
                    + " aktuell relevant sind. Gelöste Schritte deaktivieren alte Hinweise und"
                    + " schalten neue frei."),
            topic(
                "Hint-Tagebuch und Grenzen",
                "limits",
                "Welche Grenzen hatte das System?",
                "Zauberer",
                wizardPortrait(),
                "Hinweise bleiben im teamweiten Tagebuch sichtbar. Das System ist aber nicht"
                    + " vollautomatisch adaptiv und Spieler können nicht gezielt ein bestimmtes"
                    + " Rätsel für Hinweise auswählen."),
            imageTopic(
                "Hint-Statistiken",
                "stats",
                "Wie oft wurden Hinweise genutzt?",
                "Zauberer",
                wizardPortrait(),
                HINT_REQUESTS_IMAGE,
                "Insgesamt gab es 85 Hinweisereignisse. Häufige Themen waren Flucht mit 21,"
                    + " Kalte Gefilde mit 16 und Energie aus der Flasche mit 15 Hinweisen. Teams"
                    + " nutzten im Mittel 3,33 Hinweise."),
            imageTopic(
                ">Hinweise und Zusammenarbeit",
                "hint_collaboration",
                "Bedeutet viel Hinweisnutzung schlechte Zusammenarbeit?",
                "Zauberer",
                wizardPortrait(),
                TEAM_HINTS_COLLABORATION_IMAGE,
                "Nicht automatisch. Team-Hinweise und Post-Collaboration zeigten einen"
                    + " positiven, aber nur explorativen Zusammenhang: rho 0,3895, p = 0,0896."
                    + " Hinweise können auch Teil gemeinsamer Strategie sein.")));
  }

  private void showCraftingKolloquiumDialog(Entity who) {
    showKolloquiumMenu(
        who,
        "Apprentice",
        apprenticePortrait(),
        "Ich bin Apprentice. Ich stehe hier am Crafting-Kessel und prüfe die"
            + " zurückgelassenen Ressourcen. Ohne Planung wird aus Loot kein Ausbruch.",
        List.of(
            topic(
                ">Apprentice-Rolle",
                "role",
                "Welche Rolle hatten Sie im Ausbruch?",
                "Apprentice",
                apprenticePortrait(),
                "Apprentice ist als Socialiser-Rolle aus den Hexad Player Types"
                    + " abgeleitet. Die Rolle unterstützt Orientierung, Planung und"
                    + " Zusammenarbeit. Konkret startet sie mit einem Craftingbuch."),
            topic(
                "Crafting-System",
                "crafting",
                "Wie funktioniert Crafting?",
                "Apprentice",
                apprenticePortrait(),
                "Spieler kombinieren Items im Crafting-Kessel. Daraus entstehen Werkzeuge,"
                    + " Fackeln, Ringe oder Tränke. Crafting macht Ressourcenplanung zu einem"
                    + " zentralen Teil des Spiels."),
            topic(
                ">Ressourcen-Daten",
                "resources",
                "Was sagen die Evaluierungsdaten?",
                "Apprentice",
                apprenticePortrait(),
                "25 Prozent der Teilnehmenden nannten Items und Ressourcen als starke"
                    + " Herausforderung. Ressourcen waren also ein echter Belastungs- und"
                    + " Entscheidungsfaktor.")));
  }

  private void showWallKolloquiumDialog(Entity who) {
    showKolloquiumMenu(
        who,
        "Wache",
        guardPortrait(),
        "Ich bin eine Wache dieses Gefängnisses. Eigentlich sollte ich hier einen gesicherten"
            + " Abschnitt kontrollieren. Jetzt stehe ich an einem Ausbruchspfad.",
        List.of(
            topic(
                ">Konzept des Wand-Ausbruchs",
                "wall",
                "Was ist an dieser Wand passiert?",
                "Wache",
                guardPortrait(),
                "Die Gefangenen konnten hier einen direkten, aber ressourcenbasierten Ausbruch"
                    + " vorbereiten. Der Pfad verbindet Exploration, Crafting und Risiko."),
            topic(
                "Itemkette",
                "items",
                "Welche Items waren nötig?",
                "Wache",
                guardPortrait(),
                "Axt, Spitzhacke, Baumstämme, Seil und Brücke greifen ineinander. Aus"
                    + " mehreren kleinen Schritten entsteht der finale Ausbruchspfad."),
            topic(
                ">Spitzhacke und Risiko",
                "skill",
                "Warum war der Wandabbau gefährlich?",
                "Wache",
                guardPortrait(),
                "Der Spitzhacke-Skill kann eine Wand entfernen, markiert den Spieler aber als"
                    + " illegal, kostet viel Ausdauer und verlangt ein schweres Lockpick-Minigame."),
            topic(
                "Eventdaten",
                "events",
                "Warum ist dieser Pfad für die Evaluation interessant?",
                "Wache",
                guardPortrait(),
                "Hier kommen viele Eventtypen zusammen: Bewegung, Skillnutzung, Interaktion,"
                    + " Itembewegung und Itemnutzung. Die xAPI-Daten zeigen also mehr als nur"
                    + " Laufwege.")));
  }

  private void showIllegalAreaKolloquiumDialog(Entity who) {
    List<KolloquiumTopic> topics =
        List.of(
            topic(
                ">Illegaler Bereich",
                "illegal",
                "Was unterscheidet diesen Bereich?",
                "Apprentice",
                apprenticePortrait(),
                "Hier ist schon der Aufenthalt riskant. Wachen patrouillieren stärker, und der"
                    + " Bereich bereitet das Labyrinth als Belastungsraum vor."),
            topic(
                "Technische Markierung",
                "marking",
                "Wie wird der Bereich umgesetzt?",
                "Apprentice",
                apprenticePortrait(),
                "Trigger-Rechtecke markieren illegale Bereiche. Betritt ein Spieler sie oder"
                    + " wird dort auffällig, kann der illegale Status für die Wachenlogik relevant"
                    + " werden."),
            topic(
                "Future-Skill-Risiko",
                "future",
                "Welche Future Skills werden hier angesprochen?",
                "Apprentice",
                apprenticePortrait(),
                "Selbstregulation, Anpassungsfähigkeit und Zusammenarbeit. Spieler müssen ruhig"
                    + " bleiben, Wege ändern und sich abstimmen."),
            imageTopic(
                "Übergang ins Labyrinth",
                "labyrinth_transition",
                "Was kommt im Labyrinth dazu?",
                "Apprentice",
                apprenticePortrait(),
                MOVEMENT_CAPTURE_MAP_IMAGE,
                "Dort treffen Dunkelheit, Wachen, Ausdauer, Fackeln und wichtige Items"
                    + " zusammen. Die Details dazu erklärt gleich Rogue vor dem Labyrinth."));
    List<ChoiceOption> options =
        new ArrayList<>(topics.stream().map(MADungeonRoom::choiceFor).toList());
    options.add(ChoiceOption.of("Weitergehen", "continue"));
    options.add(ChoiceOption.of("Schließen", "close"));
    showMADialogChoices(
        speakerLine(
            "Apprentice",
            apprenticePortrait(),
            "Wir kennen uns schon vom Crafting-Kessel. Ich stehe jetzt am Eingang zum"
                + " illegalen Bereich. Ab diesem Punkt kann ein falscher Schritt teuer werden."),
        "",
        options,
        false,
        payload ->
            topics.stream()
                .filter(topic -> topic.value().equals(stringValue(payload)))
                .findFirst()
                .ifPresentOrElse(
                    topic ->
                        showKolloquiumTopic(who, topic, () -> showIllegalAreaKolloquiumDialog(who)),
                    () -> {
                      if ("continue".equals(stringValue(payload))) {
                        showIllegalWarningDialog(who);
                      }
                    }),
        () -> {},
        who.id());
  }

  private void showRogueKolloquiumDialog(Entity who) {
    showKolloquiumMenu(
        who,
        "Rogue",
        roguePortrait(),
        "Wir kennen uns schon vom Schieberätsel. Ich stehe jetzt vor dem Labyrinth, weil"
            + " dieser Bereich für den Ausbruch entscheidend war: wichtige Ressourcen, knappe"
            + " Sicht und hoher Druck.",
        List.of(
            topic(
                ">Labyrinth-Konzept",
                "concept",
                "Warum war das Labyrinth so zentral?",
                "Rogue",
                roguePortrait(),
                "Dort liegen wichtige Items für Ausbruchspfade. Gleichzeitig erzeugen Wachen,"
                    + " Dunkelheit, Sackgassen, Fackeln und Ressourcenknappheit starken Druck."),
            topic(
                ">Dunkelheit und Fackeln",
                "darkness",
                "Wie wurde die Dunkelheit umgesetzt?",
                "Rogue",
                roguePortrait(),
                "Shader reduzieren die Helligkeit stark. Fackeln erzeugen lokale Lichtkreise,"
                    + " müssen gecraftet und per QTE platziert werden. Licht wird damit zur"
                    + " Ressource."),
            topic(
                ">Labyrinth-Daten",
                "data",
                "Was sagen die qualitativen Daten?",
                "Rogue",
                roguePortrait(),
                "29,55 Prozent nannten Rätsel und Aufgaben als stärkste Herausforderung."
                    + " Items/Ressourcen und Orientierung/Raumstruktur lagen jeweils bei 25"
                    + " Prozent. Wachen/Capture sowie Ausdauer/Ressourcen waren wichtige"
                    + " Frustquellen."),
            imageTopic(
                ">Heatmap",
                "heatmap",
                "Welche Rolle spielt die Heatmap beim Labyrinth?",
                "Rogue",
                roguePortrait(),
                MOVEMENT_CAPTURE_MAP_IMAGE,
                "Sie zeigt, wo Bewegung und Captures konzentriert waren. Dadurch werden konkrete"
                    + " Levelstellen auswertbar."),
            imageTopic(
                "Capture-Hotspot",
                "hotspot",
                "Welcher Punkt war besonders auffällig?",
                "Rogue",
                roguePortrait(),
                MOVEMENT_CAPTURE_MAP_IMAGE,
                "Der stärkste Hotspot lag bei Koordinate 75,31 mit 16 Captures. Er liegt"
                    + " plausibel im Eingangsbereich des Labyrinths. Ein Hotspot ist aber zuerst"
                    + " ein Analysehinweis, kein automatischer Designfehler."),
            topic(
                "Auswertungsbedeutung",
                "interpretation",
                "Warum ist dieser Bereich für die Forschung wichtig?",
                "Rogue",
                roguePortrait(),
                "Hier treffen viele Spielparameter zusammen: Bewegung, Captures, Hints,"
                    + " Ressourcen, Orientierung und Freitextbefunde. Das zeigt den Wert einer"
                    + " spielparameterbezogenen DEER-Auswertung.")));
  }

  private void showIceKolloquiumDialog(Entity who) {
    showKolloquiumMenu(
        who,
        "Rogue",
        roguePortrait(),
        "Wir sehen uns wieder. Ich prüfe hier den Eisbereich, weil dieser Fluchtweg vor allem"
            + " durch Bewegungsplanung gefährlich wurde. Auf dem Eis entscheidet die Richtung,"
            + " bevor man losrutscht.",
        List.of(
            topic(
                ">Rutschmechanik",
                "slide",
                "Wie funktioniert das Eisrätsel?",
                "Rogue",
                roguePortrait(),
                "Auf dem Eis kann man nicht normal laufen. Der Spieler rutscht geradeaus, bis"
                    + " er eine Wand berührt. Dadurch wird Bewegungsplanung zum Rätsel."),
            topic(
                ">Kooperation mit Wänden",
                "coop",
                "Warum braucht das Rätsel Zusammenarbeit?",
                "Rogue",
                roguePortrait(),
                "Eine Person kann Wände platzieren, die der anderen als Stopppunkte dienen."
                    + " So entsteht kooperative Raumplanung."),
            topic(
                "Umsetzung und Einstieg",
                "entry",
                "Wie wird verhindert, dass Spieler sofort feststecken?",
                "Rogue",
                roguePortrait(),
                "Der Eingang führt die Logik kontrolliert ein. Frühe Schneewände zeigen, dass"
                    + " Rutschen auch als Werkzeug genutzt werden kann."),
            imageTopic(
                ">Ice-Puzzle-Daten",
                "data",
                "Was zeigen die Daten?",
                "Rogue",
                roguePortrait(),
                CHALLENGE_OUTCOMES_IMAGE,
                "Das Ice Puzzle wurde 16-mal gelöst, 0-mal fehlgeschlagen und erzeugte 303"
                    + " Try-Events. Kalte Gefilde war mit 16 Hinweisen ein häufiges Hint-Thema.")));
  }

  private void showPushRiddleKolloquiumDialog(Entity who, boolean solved) {
    showKolloquiumMenu(
        who,
        "Rogue",
        roguePortrait(),
        solved
            ? "Da sind Sie ja wieder. Ich kontrolliere den freigewordenen Ausgang. Der Weg ist"
                + " frei. Aber der Ausgang ist nicht der Anfang der Geschichte, sondern ihr"
                + " Ergebnis."
            : "Ich bin Rogue. Ich prüfe hier das Schieberätsel, weil dieser Fluchtweg viele"
                + " Spuren von Planung und Zusammenarbeit hinterlassen hat.",
        solved
            ? List.of(
                topic(
                    ">Ausbruch als Prozess",
                    "escape",
                    "Was sagt der gelöste Bereich über den Ausbruch?",
                    "Rogue",
                    roguePortrait(),
                    "Der Ausbruch entstand aus vielen Teilschritten: Items finden, craften,"
                        + " Hinweise nutzen, Minigames schaffen, Wachen meiden und zusammenarbeiten."),
                imageTopic(
                    "MARoom-Daten",
                    "maroom",
                    "Wie wurde der finale Ausbruch erfasst?",
                    "Rogue",
                    roguePortrait(),
                    CHALLENGE_OUTCOMES_IMAGE,
                    "MARoom wurde 44-mal gelöst und 0-mal fehlgeschlagen. Der Abschluss ist"
                        + " also der erfolgreiche Endpunkt eines vorher aufgebauten Pfads."),
                imageTopic(
                    ">Fortschritt und Spielspaß",
                    "progress_enjoyment",
                    "Hing Fortschritt mit Spielspaß zusammen?",
                    "Rogue",
                    roguePortrait(),
                    ENJOYMENT_PROGRESS_IMAGE,
                    "Ja. Gelöste minus fehlgeschlagene Challenges hingen positiv mit Spielspaß"
                        + " zusammen: rho 0,3589, p = 0,0167. Spürbarer Fortschritt war also"
                        + " wichtig."),
                topic(
                    ">Gesamtdeutung",
                    "interpretation",
                    "Was ist die wichtigste Deutung?",
                    "Rogue",
                    roguePortrait(),
                    "Nicht nur das Entkommen zählt, sondern der Weg dorthin: Captures, Hints,"
                        + " Try-Events, Items, Skills, Bewegung, Survey und Freitexte ergeben"
                        + " zusammen das Evaluierungsbild."))
            : List.of(
                topic(
                    ">Was nun?",
                    "next",
                    "Was passiert hier als Nächstes?",
                    "Rogue",
                    roguePortrait(),
                    "Ich warte auf Apprentice. Er holt mir einen Ring der Stärke. Ohne den Ring"
                        + " bekomme ich die schweren Steine nicht bewegt. Sobald er zurück ist,"
                        + " können wir das Schieberätsel angehen."),
                topic(
                    ">Rogue-Rolle",
                    "role",
                    "Welche Rolle hatten Sie im Ausbruch?",
                    "Rogue",
                    roguePortrait(),
                    "Rogue ist als Achiever-Rolle aus den Hexad Player Types"
                        + " abgeleitet. Die Rolle steht für Zielorientierung und Fortschritt."
                        + " Konkret umgesetzt wurde sie über die Sprintfähigkeit."),
                topic(
                    "Ring der Stärke",
                    "ring",
                    "Warum brauchen Spieler den Ring der Stärke?",
                    "Rogue",
                    roguePortrait(),
                    "Der Ring erhöht die Masse des Spielers. Erst dadurch kann er schwere Steine"
                        + " schieben. Das Item ist also eine echte Fortschrittsvoraussetzung."),
                topic(
                    "Druckplatten und Farben",
                    "plates",
                    "Wie funktioniert die Rätsellogik?",
                    "Rogue",
                    roguePortrait(),
                    "Steine müssen auf passende Druckplatten geschoben werden. Später aktiviert"
                        + " ein farbiger Stein nur die passende farbige Platte."),
                topic(
                    "Kooperation und Schleusen",
                    "coop",
                    "Warum braucht das Rätsel Zusammenarbeit?",
                    "Rogue",
                    roguePortrait(),
                    "Spieler werden später getrennt. Eine Person muss der anderen Türen öffnen."
                        + " Dadurch werden Kommunikation und Arbeitsteilung mechanisch notwendig."),
                topic(
                    "Playtest-Iteration",
                    "playtest",
                    "Was wurde durch Tests verbessert?",
                    "Rogue",
                    roguePortrait(),
                    "Playtests zeigten Softlocks und Exploits. Wege, Ecken und Schleusen wurden"
                        + " angepasst, damit das Rätsel spielbar bleibt."),
                imageTopic(
                    "Puzzle-Daten",
                    "data",
                    "Was zeigen die Daten?",
                    "Rogue",
                    roguePortrait(),
                    CHALLENGE_OUTCOMES_IMAGE,
                    "Das Push Puzzle wurde 31-mal gelöst, 3-mal fehlgeschlagen und erzeugte 443"
                        + " Try-Events. Viele Versuche zeigen iterative Bearbeitung.")));
  }

  private void showPushRiddleDialog(Entity rogue, Entity who) {
    if (!pushRiddleFollowUpAvailable) {
      pushRiddleFollowUpAvailable = true;
      showPushRiddleKolloquiumDialog(who, false);
      return;
    }

    showPushRiddleKolloquiumDialog(who, pushRiddleApprentice != null);
  }

  private void showCaptureKolloquiumDialog(Entity who) {
    List<KolloquiumTopic> topics = captureKolloquiumTopics();
    if (captureKolloquiumSeenTopics.size() >= topics.size()) {
      showCaptureArrestDialog(who);
      return;
    }

    showCaptureKolloquiumMenu(who, topics);
  }

  private List<KolloquiumTopic> captureKolloquiumTopics() {
    return List.of(
        topic(
            "Wachenlogik",
            "guard_logic",
            "Wie erkennt eine Wache einen Spieler?",
            "Wache",
            guardPortrait(),
            "Wachen nutzen Sichtkegel, Alertness-Counter, Wegpunkte und Navigation. Bei genug"
                + " Alertness wechseln sie von Patrouille zu Verfolgung."),
        imageTopic(
            "Verhaftung und Einzelhaft",
            "capture",
            "Was passiert bei einer Verhaftung?",
            "Wache",
            guardPortrait(),
            PLAYER_CAPTURES_IMAGE,
            "Der Spieler kommt in Einzelhaft. Ein QTE kann ihn sofort befreien. Bei Fehlschlag"
                + " bleibt er 30 Sekunden festgesetzt."),
        imageTopic(
            "QTE-Daten",
            "qte",
            "Was zeigen die Daten zum QTE?",
            "Wache",
            guardPortrait(),
            CHALLENGE_OUTCOMES_IMAGE,
            "Es gab 373 Capture- und 515 Detection-Ereignisse. Das QTE wurde 2.082-mal"
                + " gelöst und 824-mal fehlgeschlagen. Der Failure-Anteil lag bei rund 28"
                + " Prozent."),
        imageTopic(
            "Capture und Selbstregulation",
            "self_regulation",
            "Wie wurden Captures interpretiert?",
            "Wache",
            guardPortrait(),
            SELF_REGULATION_CAPTURE_IMAGE,
            "Captures sind Belastungsindikatoren. Mehr Captures hingen mit niedrigerer"
                + " Post-Selbstregulation zusammen, rho -0,3538. Team-Gefangenschaftszeit hing"
                + " negativ mit Balancierung zusammen."),
        imageTopic(
            "Balancing und Gefangenschaftszeit",
            "balance_capture_time",
            "Was zeigt Gefangenschaftszeit über Balancing?",
            "Wache",
            guardPortrait(),
            TEAM_BALANCE_CAPTURE_TIME_IMAGE,
            "Längere Team-Gefangenschaftszeit hing mit niedrigerer Bewertung von"
                + " Schwierigkeit und Balance zusammen: rho -0,4673, p = 0,0378. Zu viel"
                + " Zeitverlust wird zur Belastung."),
        imageTopic(
            "Vorerfahrung und Festnahmen",
            "experience_captures",
            "Wurden erfahrene Spieler seltener gefasst?",
            "Wache",
            guardPortrait(),
            ESCAPE_EXPERIENCE_CAPTURE_IMAGE,
            "Tendenziell ja. Escape-Room-Erfahrung hing negativ mit Captures zusammen:"
                + " rho -0,3655, p = 0,0125. Erfahrung kann helfen, Risiken und Patrouillen"
                + " besser einzuschätzen."));
  }

  private void showCaptureKolloquiumMenu(Entity who, List<KolloquiumTopic> topics) {
    List<ChoiceOption> options =
        new ArrayList<>(
            topics.stream()
                .filter(topic -> !captureKolloquiumSeenTopics.contains(topic.value()))
                .map(MADungeonRoom::choiceFor)
                .toList());
    options.add(ChoiceOption.of("Schließen", "close"));
    showMADialogChoices(
        speakerLine(
            "Wache",
            guardPortrait(),
            "Ich bin die Wache dieses Patrouillenabschnitts. Ich kontrolliere illegale Bereiche"
                + " und verbotene Aktionen. Wer sich erwischen lässt, landet zuerst in meiner"
                + " Einzelhaft."),
        "",
        options,
        false,
        payload ->
            topics.stream()
                .filter(topic -> topic.value().equals(stringValue(payload)))
                .findFirst()
                .ifPresent(topic -> showCaptureKolloquiumTopic(who, topics, topic)),
        () -> {},
        who.id());
  }

  private void showCaptureKolloquiumTopic(
      Entity who, List<KolloquiumTopic> topics, KolloquiumTopic topic) {
    captureKolloquiumSeenTopics.add(topic.value());
    showMADialogDialog(
        topicDialog(who, topic),
        () -> {
          if (captureKolloquiumSeenTopics.size() >= topics.size()) {
            showCaptureArrestDialog(who);
            return;
          }
          showCaptureKolloquiumMenu(who, topics);
        },
        who.id());
  }

  private void showCaptureArrestDialog(Entity who) {
    showMADialogDialog(
        speakerLine(
                "Wache",
                guardPortrait(),
                "Die Akten sind vollständig. Damit endet die Führung und beginnt der"
                    + " dienstliche Teil.")
            + "[p]"
            + speakerLine(
                "Reporter",
                reporterPortrait(who),
                "Ich notiere: alle Festnahme-Themen geprüft, anschließend Einzelhaft.")
            + "[p]"
            + speakerLine(
                "Wache",
                guardPortrait(),
                "Korrekt. Den Rest klären wir dort, wo diese Fälle hingehören."),
        () -> markReporterIllegal(who),
        who.id());
  }

  private void showReporterMapDialog(Entity who) {
    List<KolloquiumTopic> topics = mapTopics();
    List<ChoiceOption> options =
        new ArrayList<>(topics.stream().map(MADungeonRoom::choiceFor).toList());
    options.add(imageOption("Analysebild anzeigen", "analysis"));
    options.add(ChoiceOption.of("Karte öffnen", "camera"));
    options.add(ChoiceOption.of("Abbrechen", "close"));
    showMADialogChoices(
        speakerLine(
            "Verzauberte Karte",
            enchantedMapPortrait(),
            "Ich bin die verzauberte Karte dieses Gefängnisses. Ich verbinde"
                + " Patrouillenmeldungen mit dem Levelplan. Wer den Ausbruch verstehen will,"
                + " muss zuerst den Aufbau des Gefängnisses verstehen."),
        "",
        options,
        false,
        payload -> handleReporterMapChoice(payload, who, topics),
        () -> {},
        who.id());
  }

  private void handleReporterMapChoice(
      DialogResponseMessage.Payload payload, Entity who, List<KolloquiumTopic> topics) {
    String value = stringValue(payload);
    if ("camera".equals(value)) {
      openReporterMapCamera(who);
      return;
    }
    if ("analysis".equals(value)) {
      showMADialogDialog(
          speakerLine(
                  "Reporter",
                  reporterPortrait(who),
                  "Die Analysekarte zeigt Bewegung und Capture-Schwerpunkte im Level. Sie ist"
                      + " kein eigenständiges Ergebnis, sondern hilft, konkrete Levelstellen"
                      + " auszuwerten.")
              + thesisImage(MOVEMENT_CAPTURE_MAP_IMAGE),
          () -> showReporterMapDialog(who),
          who.id());
      return;
    }

    topics.stream()
        .filter(topic -> topic.value().equals(value))
        .findFirst()
        .ifPresent(topic -> showKolloquiumTopic(who, topic, () -> showReporterMapDialog(who)));
  }

  private void openReporterMapCamera(Entity who) {
    if (mapCameraActive) return;
    mapCameraActive = true;
    mapCameraUser = who;
    previousCameraZoom = CameraSystem.camera().zoom;
    mapCameraReturnTargets.clear();

    Game.allPlayers()
        .forEach(
            player -> {
              if (player.isPresent(CameraComponent.class)) {
                mapCameraReturnTargets.add(player);
                player.remove(CameraComponent.class);
              }
              freezePlayerForMapCamera(player);
              hidePlayerForReplay(player);
            });

    Point levelCenter = levelCenter();
    mapCameraEntity = Entity.createLocalEntity("reporter_map_camera");
    mapCameraEntity.add(new CameraComponent());
    mapCameraEntity.add(new PositionComponent(levelCenter, Direction.DOWN));
    Game.add(mapCameraEntity);
    Game.add(new ReplayFreeCameraSystem(mapCameraEntity));
    CameraSystem.camera().zoom = overviewZoom();
    Game.system(CameraSystem.class, camera -> camera.instantFocus(levelCenter));
    DialogUtils.showTextPopup(
        "Kartenkamera aktiv. Bewege die Kamera frei und drücke ESC, um zum Reporter zurückzukehren.",
        "Dungeon-Karte",
        () -> {},
        who.id());
  }

  private void closeReporterMapCamera() {
    if (!mapCameraActive) return;
    mapCameraActive = false;
    Game.remove(ReplayFreeCameraSystem.class);
    if (mapCameraEntity != null) {
      mapCameraEntity.remove(CameraComponent.class);
      Game.remove(mapCameraEntity);
      mapCameraEntity = null;
    }

    CameraSystem.camera().zoom = previousCameraZoom;
    if (mapCameraReturnTargets.isEmpty() && mapCameraUser != null) {
      mapCameraReturnTargets.add(mapCameraUser);
    }
    mapCameraReturnTargets.forEach(
        target -> {
          if (!target.isPresent(CameraComponent.class)) {
            target.add(new CameraComponent());
          }
        });
    Game.allPlayers().forEach(this::unfreezePlayerAfterMapCamera);
    restoreReplayHiddenPlayers();

    Entity focusTarget =
        mapCameraReturnTargets.isEmpty() ? mapCameraUser : mapCameraReturnTargets.getFirst();
    if (focusTarget != null && focusTarget.isPresent(PositionComponent.class)) {
      Game.system(
          CameraSystem.class, camera -> camera.instantFocus(EntityUtils.getPosition(focusTarget)));
    }
    mapCameraReturnTargets.clear();
    mapCameraUser = null;
  }

  private void toggleReporterReplayMode() {
    if (replayModeActive) {
      closeReporterReplayMode();
      return;
    }
    if (replayModeClosedAtTick == Game.currentTick()) {
      return;
    }
    openReporterReplayMode(currentCameraUser());
  }

  private boolean replayToggleKeyPressed() {
    return InputManager.isKeyJustPressed(Input.Keys.M);
  }

  private Entity currentCameraUser() {
    return Game.allPlayers()
        .filter(player -> player.isPresent(CameraComponent.class))
        .findFirst()
        .or(() -> Game.allPlayers().findFirst())
        .orElse(null);
  }

  private void openReporterReplayMode(Entity who) {
    if (replayModeActive) return;
    if (mapCameraActive) {
      closeReporterMapCamera();
    }
    ReplayControlSystem.closeAll();

    replayModeActive = true;
    replayCameraUser = who;
    previousReplayCameraZoom = CameraSystem.camera().zoom;
    replayCameraReturnTargets.clear();
    Point cameraStart = replayCameraStart(who);

    Game.allPlayers()
        .forEach(
            player -> {
              if (player.isPresent(CameraComponent.class)) {
                replayCameraReturnTargets.add(player);
                player.remove(CameraComponent.class);
              }
              freezePlayerForMapCamera(player);
              hidePlayerForReplay(player);
            });

    replayCameraEntity = Entity.createLocalEntity("reporter_replay_camera");
    replayCameraEntity.add(new CameraComponent());
    replayCameraEntity.add(new PositionComponent(cameraStart, Direction.DOWN));
    Game.add(replayCameraEntity);
    Game.add(new ReplayFreeCameraSystem(replayCameraEntity));
    reporterReplayControlSystem =
        new ReplayControlSystem(replayCameraEntity, this::closeReporterReplayMode);
    Game.add(reporterReplayControlSystem);
    Game.system(CameraSystem.class, camera -> camera.instantFocus(cameraStart));
  }

  private Point replayCameraStart(Entity fallback) {
    return Game.allPlayers()
        .filter(player -> player.isPresent(CameraComponent.class))
        .findFirst()
        .flatMap(player -> player.fetch(PositionComponent.class).map(PositionComponent::position))
        .or(
            () ->
                Optional.ofNullable(fallback)
                    .flatMap(
                        player ->
                            player.fetch(PositionComponent.class).map(PositionComponent::position)))
        .orElseGet(this::levelCenter);
  }

  private void closeReporterReplayMode() {
    if (!replayModeActive) return;
    replayModeActive = false;
    replayModeClosedAtTick = Game.currentTick();

    ReplayControlSystem.closeAll();
    reporterReplayControlSystem = null;
    Game.remove(ReplayControlSystem.class);
    Game.remove(ReplayFreeCameraSystem.class);
    if (replayCameraEntity != null) {
      replayCameraEntity.remove(CameraComponent.class);
      Game.remove(replayCameraEntity);
      replayCameraEntity = null;
    }

    CameraSystem.camera().zoom = previousReplayCameraZoom;
    if (replayCameraReturnTargets.isEmpty() && replayCameraUser != null) {
      replayCameraReturnTargets.add(replayCameraUser);
    }
    replayCameraReturnTargets.forEach(
        target -> {
          if (!target.isPresent(CameraComponent.class)) {
            target.add(new CameraComponent());
          }
        });
    Game.allPlayers().forEach(this::unfreezePlayerAfterMapCamera);
    restoreReplayHiddenPlayers();

    Entity focusTarget =
        replayCameraReturnTargets.isEmpty()
            ? replayCameraUser
            : replayCameraReturnTargets.getFirst();
    if (focusTarget != null && focusTarget.isPresent(PositionComponent.class)) {
      Game.system(
          CameraSystem.class, camera -> camera.instantFocus(EntityUtils.getPosition(focusTarget)));
    }
    replayCameraReturnTargets.clear();
    replayCameraUser = null;
  }

  private void hidePlayerForReplay(Entity player) {
    player
        .fetch(DrawComponent.class)
        .ifPresent(
            drawComponent -> {
              replayHiddenPlayerVisibility.putIfAbsent(player, drawComponent.isVisible());
              drawComponent.setVisible(false);
            });
  }

  private void restoreReplayHiddenPlayers() {
    replayHiddenPlayerVisibility.forEach(
        (player, wasVisible) ->
            player
                .fetch(DrawComponent.class)
                .ifPresent(drawComponent -> drawComponent.setVisible(wasVisible)));
    replayHiddenPlayerVisibility.clear();
  }

  private void freezePlayerForMapCamera(Entity player) {
    player.fetch(InputComponent.class).ifPresent(input -> input.deactivateControls(true));
    player
        .fetch(VelocityComponent.class)
        .ifPresent(
            velocity -> {
              velocity.currentVelocity(Vector2.ZERO);
              velocity.clearForces();
              velocity.modifier(MAP_CAMERA_FREEZE_MODIFIER, 0f);
            });
  }

  private void unfreezePlayerAfterMapCamera(Entity player) {
    player.fetch(InputComponent.class).ifPresent(input -> input.deactivateControls(false));
    player
        .fetch(VelocityComponent.class)
        .ifPresent(
            velocity -> {
              velocity.removeModifier(MAP_CAMERA_FREEZE_MODIFIER);
              velocity.currentVelocity(Vector2.ZERO);
            });
  }

  private Point levelCenter() {
    return new Point(layout()[0].length / 2f, layout().length / 2f);
  }

  private float overviewZoom() {
    float viewportWidth = Math.max(1f, CameraSystem.camera().viewportWidth);
    float viewportHeight = Math.max(1f, CameraSystem.camera().viewportHeight);
    float zoomX = layout()[0].length / viewportWidth;
    float zoomY = layout().length / viewportHeight;
    return Math.max(zoomX, zoomY) * 1.08f;
  }

  private void showIllegalWarningDialog(Entity who) {
    DialogFactory.showYesNoDialog(
        "Ab hier wird der Boden grau. Wachbereich. Ich komme da selbst kaum hinein. Als Reporter"
            + " sollten Sie dort wirklich nicht weitergehen."
            + "\n\nTrotzdem weiter?",
        "",
        () ->
            showMADialogDialog(
                speakerLine(
                    "Apprentice",
                    apprenticePortrait(),
                    "Na gut. Ich habe Sie gewarnt. Sagen Sie nachher nicht, der graue Boden"
                        + " hätte nichts bedeutet."),
                () -> {},
                who.id()),
        () ->
            showMADialogDialog(
                speakerLine(
                    "Apprentice",
                    apprenticePortrait(),
                    "Vernünftig. Graue Bereiche sind für Wachen, kurze Fluchtpläne und Leute,"
                        + " die Papierkram unterschätzen."),
                () -> {},
                who.id()),
        who.id());
  }

  private void showCaptureGuardDialog(Entity who) {
    if (reporterCaptureInProgress) {
      showSimpleDialog(who, "Wache", guardPortrait(), "Einen Moment. Der Vorgang läuft bereits.");
      return;
    }

    showCaptureKolloquiumDialog(who);
  }

  private void markReporterIllegal(Entity who) {
    reporterCaptureInProgress = true;
    IllegalComponent illegalComponent =
        who.fetch(IllegalComponent.class)
            .orElseGet(
                () -> {
                  IllegalComponent component = new IllegalComponent();
                  who.add(component);
                  return component;
                });
    illegalComponent.addReason(IllegalComponent.Reason.UNKNOWN);
    EventScheduler.scheduleAction(
        () -> clearUncaughtReporterCapture(who), REPORTER_CAPTURE_ARM_TIMEOUT_MS);
  }

  private void clearUncaughtReporterCapture(Entity who) {
    if (!reporterCaptureInProgress || who == null || who.isPresent(AttachmentComponent.class)) {
      return;
    }

    boolean stillMarkedForReporterCapture =
        who.fetch(IllegalComponent.class)
            .map(component -> component.hasReason(IllegalComponent.Reason.UNKNOWN))
            .orElse(false);
    if (!stillMarkedForReporterCapture) {
      reporterCaptureInProgress = false;
      return;
    }

    clearReporterIllegalReason(who);
    reporterCaptureInProgress = false;
  }

  private void finishReporterCapture(Entity who) {
    if (who != null) {
      clearReporterIllegalReason(who);
    }
    reporterCaptureInProgress = false;
  }

  private void clearReporterIllegalReason(Entity who) {
    if (who == null) {
      return;
    }
    who.fetch(IllegalComponent.class)
        .ifPresent(component -> component.removeReason(IllegalComponent.Reason.UNKNOWN));
  }

  private void removeReporterCaptureGuard() {
    if (reporterCaptureGuard == null) {
      return;
    }

    Game.remove(reporterCaptureGuard);
    reporterCaptureGuard = null;
  }

  private void showSimpleDialog(Entity who, String speaker, String portrait, String text) {
    showMADialogDialog(speakerLine(speaker, portrait, text), () -> {}, who.id());
  }

  private UIComponent showMADialogDialog(
      String dialog, Runnable onFinished, int... targetEntityIds) {
    return DialogFactory.showDialogDialog(dialog, () -> onFinished.run(), true, targetEntityIds);
  }

  private UIComponent showMADialogChoices(
      String dialog,
      String title,
      List<ChoiceOption> options,
      boolean canCancel,
      Consumer<DialogResponseMessage.Payload> onSelected,
      IVoidFunction onCancel,
      int... targetEntityIds) {
    return DialogFactory.showMultipleChoiceDialog(
        dialog, title, options, canCancel, onSelected, onCancel, true, targetEntityIds);
  }

  private static String stringValue(DialogResponseMessage.Payload payload) {
    return payload instanceof DialogResponseMessage.StringValue(String value) ? value : "";
  }

  private static ChoiceOption imageOption(String label, String value) {
    return ChoiceOption.of(IMAGE_OPTION_MARKER + label, value);
  }

  private static void showSlideDeck(List<Integer> slides, Entity target) {
    showSlideDeck(slides, false, target);
  }

  private static void showSlideDeck(List<Integer> slides, boolean zoomOutOnFinish, Entity target) {
    SlideDeckDialog.show(slides, zoomOutOnFinish, target.id());
  }

  private static String speakerLine(String name, String imagePath, String text) {
    return "[speaker img=" + imagePath + " name=\"" + name + "\"]" + text;
  }

  private static String thesisImage(String imagePath) {
    return "[p][speaker clear][img-block path=" + imagePath + " width=80%]";
  }

  private static String reporterPortrait(Entity who) {
    return who.fetch(CharacterClassComponent.class)
        .map(CharacterClassComponent::characterClass)
        .map(MADungeonRoom::portraitFor)
        .orElse(ROGUE_PORTRAIT);
  }

  private static String portraitFor(CharacterClass characterClass) {
    return switch (characterClass) {
      case APPRENTICE, THE_LAST_HOUR_CHAR03 -> APPRENTICE_PORTRAIT;
      case WIZARD, MUSHROOM_WIZARD -> WIZARD_PORTRAIT;
      default -> ROGUE_PORTRAIT;
    };
  }

  private static String guardPortrait() {
    return GUARD_PORTRAIT;
  }

  private static String enchantedMapPortrait() {
    return SCROLL_IMAGE;
  }

  private static String directorPortrait() {
    return BLUE_GUARD_PORTRAIT;
  }

  private static String wizardPortrait() {
    return WIZARD_PORTRAIT;
  }

  private static String apprenticePortrait() {
    return APPRENTICE_PORTRAIT;
  }

  private static String roguePortrait() {
    return ROGUE_PORTRAIT;
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
        "Wir wechseln aus den Folien in den entwickelten Escape Room.\n\n"
            + "Ich bin als Reporter nach dem Ausbruch im Gefängnis unterwegs.\n\n"
            + "Wir folgen den Spuren: Wege, Mechaniken, NPCs und Evaluierungsdaten.",
        "Kolloquium",
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
                                            if (!ic.hasItem(LeafItem.class) && leafRewardReady()) {
                                              ic.add(new LeafItem());
                                              delayLeafReward();
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

  private boolean leafRewardReady() {
    return TimeUtils.millis() >= nextLeafRewardAtMs;
  }

  private void delayLeafReward() {
    nextLeafRewardAtMs = TimeUtils.millis() + LEAF_REWARD_COOLDOWN_MS;
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
    pushStonesByIndex.put(index, pushStone);
    Game.add(pushStone);
    return pushStone;
  }

  private void solvePushRiddleForReporter(Entity rogue) {
    int[][] solution = {
      {0, 0},
      {1, 1},
      {2, 2},
      {3, 3},
      {4, 4},
      {5, 5},
      {6, 6},
      {7, 7},
      {8, 8},
      {9, 9},
      {11, 11},
      {15, 15},
      {16, 16},
      {17, 17},
      {18, 18},
      {19, 19},
      {20, 20}
    };

    for (int[] placement : solution) {
      int stoneIndex = placement[0];
      int plateIndex = placement[1];
      placePushStoneOnPlate(stoneIndex, plateIndex);
      activatePushPlate(plateIndex);
    }

    removeSolvedPushRiddleExtraStones();
    movePushRiddleInteractionTrigger(rogue);
    spawnPushRiddleApprentice();
  }

  private void removeSolvedPushRiddleExtraStones() {
    int[] removableStoneIndexes = {22, 25, 30, 40, 48, 54};
    for (int stoneIndex : removableStoneIndexes) {
      removePushStone(stoneIndex);
    }
  }

  private void placePushStoneOnPlate(int stoneIndex, int plateIndex) {
    Entity stone =
        Optional.ofNullable(pushStonesByIndex.get(stoneIndex))
            .orElseGet(() -> createStone(stoneIndex, getPoint("push_plate" + plateIndex)));

    stone
        .fetch(PositionComponent.class)
        .ifPresent(position -> position.position(getPoint("push_plate" + plateIndex)));
    stone
        .fetch(DrawComponent.class)
        .ifPresent(
            drawComponent ->
                drawComponent.tintColor(
                    Color.rgba8888(pushStoneSolvedColor(stoneIndex, plateIndex))));
  }

  private Color pushStoneSolvedColor(int stoneIndex, int plateIndex) {
    return switch (stoneIndex) {
      case 5, 15 -> Color.GREEN;
      case 6, 7, 17 -> Color.RED;
      case 16 -> Color.BLUE;
      default -> plateIndex < plateColors.length ? plateColors[plateIndex] : Color.WHITE;
    };
  }

  private void activatePushPlate(int plateIndex) {
    Point platePosition = getPoint("push_plate" + plateIndex);
    Game.levelEntities()
        .filter(entity -> entity.isPresent(PressurePlateComponent.class))
        .filter(
            entity ->
                entity
                    .fetch(PositionComponent.class)
                    .map(position -> position.position().equals(platePosition))
                    .orElse(false))
        .findFirst()
        .flatMap(entity -> entity.fetch(PressurePlateComponent.class))
        .ifPresent(pressurePlate -> pressurePlate.increase(1.3f));
  }

  private Point offsetReporterPosition(Point position, CharacterClass characterClass) {
    return ReplayPositionOffset.apply(position, characterClass).translate(Vector2.of(-1, -1));
  }

  private void teleportReporterNpc(Entity npc, Point position, CharacterClass characterClass) {
    EntityUtils.teleportEntityTo(npc, offsetReporterPosition(position, characterClass));
    PositionSync.syncPosition(npc);
  }

  private void movePushRiddleInteractionTrigger(Entity rogue) {
    teleportReporterNpc(rogue, getPoint("coll_pushgoal"), CharacterClass.ROGUE);
  }

  private void removePushStone(int stoneIndex) {
    Optional.ofNullable(pushStonesByIndex.remove(stoneIndex))
        .ifPresent(
            pushStone -> {
              pushStones1.remove(pushStone);
              pushStones2.remove(pushStone);
              pushStones3.remove(pushStone);
              Game.remove(pushStone);
            });
  }

  private void spawnPushRiddleApprentice() {
    Point apprenticePosition =
        offsetReporterPosition(
            getPoint("coll_pushgoal").translate(1, 0), CharacterClass.APPRENTICE);
    if (pushRiddleApprentice != null) {
      EntityUtils.teleportEntityTo(pushRiddleApprentice, apprenticePosition);
      PositionSync.syncPosition(pushRiddleApprentice);
      return;
    }

    pushRiddleApprentice = createNonCollidingNpc(apprenticePosition, "character/char03");
    Game.add(pushRiddleApprentice);
  }

  private Entity createNonCollidingNpc(Point position, String spritePath) {
    Entity npc = NPCFactory.createNPC(position, spritePath);
    npc.remove(CollideComponent.class);
    return npc;
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
        removePushStones(pushStones1);
        createPushStones(1);
      }
      case 2 -> {
        removePushStones(pushStones2);
        createPushStones(2);
      }
      case 3 -> {
        removePushStones(pushStones3);
        createPushStones(3);
      }
    }
  }

  private void removePushStones(List<Entity> pushStones) {
    pushStonesByIndex.entrySet().removeIf(entry -> pushStones.contains(entry.getValue()));
    pushStones.forEach(Game::remove);
    pushStones.clear();
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
    SLIDE_DECKS_BY_NUMPAD.forEach(
        (key, slides) ->
            inputComp.registerCallback(
                key,
                caller -> showSlideDeck(slides, key == ZOOM_OUT_SLIDE_DECK_KEY, caller),
                false,
                true));
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
        "Wir verlassen das Gefängnis und wechseln zurück zu den Abschlussfolien.\n\n"
            + "Die Arbeit zeigt stark, wie ein DEER anhand von Spielparametern ausgewertet"
            + " werden kann. Future-Skill-Förderung bleibt dagegen vorsichtig und explorativ"
            + " zu formulieren.",
        "Ausgang",
        () -> {
          Game.exit("Level completed - players escaped.");
        });
  }
}
