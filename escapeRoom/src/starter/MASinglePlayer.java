package starter;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;
import contrib.crafting.Crafting;
import contrib.entities.CharacterClass;
import contrib.entities.EntityFactory;
import contrib.hud.dialogs.DialogContext;
import contrib.hud.dialogs.DialogFactory;
import contrib.item.Item;
import contrib.modules.levelHide.LevelHideSystem;
import contrib.systems.*;
import contrib.utils.components.Debugger;
import core.Entity;
import core.Game;
import core.level.loader.DungeonLoader;
import core.network.config.NetworkConfig;
import core.utils.Tuple;
import core.utils.components.path.SimpleIPath;
import demoDungeon.level.MADungeonRoom;
import escapeDungeon.items.*;
import guard.GuardDetectionSystem;
import hint.HintLogComponent;
import java.io.IOException;
import mushRoom.modules.EscapeRoomDialogTypes;
import network.EscapeRoomSnapshotTranslator;
import tools.timer.*;

/**
 * Starter for the Demo Escaperoom Dungeon.
 *
 * <p>Usage: run with the Gradle task {@code runMA}.
 */
public class MASinglePlayer {
  private static final boolean DEBUG_MODE = true;
  private static final String BACKGROUND_MUSIC = "sounds/background.wav";
  private static final int START_LEVEL = 0;

  /**
   * Main method to start the game.
   *
   * @param args The arguments passed to the game.
   * @throws IOException If an I/O error occurs.
   */
  public static void main(String[] args) throws IOException {
    configGame();
    onSetup();

    NetworkConfig.SNAPSHOT_TRANSLATOR = new EscapeRoomSnapshotTranslator();

    registerItems();

    Game.windowTitle("Demo-Room");
    Game.run();
  }

  private static void onSetup() {
    Game.userOnSetup(
        () -> {
          setupMusic();
          DungeonLoader.addLevel(Tuple.of("maroom", MADungeonRoom.class));
          createSystems();
          createHero();
          Crafting.loadRecipes();
          DungeonLoader.loadLevel(START_LEVEL);
          registerTimerHandlers();
        });
  }

  private static void createHero() {
    Entity hero = EntityFactory.newHero(CharacterClass.ROGUE);
    hero.add(new HintLogComponent());
    Game.add(hero);
  }

  public static void registerItems() {
    Item.registerItem(AxeItem.class);
    Item.registerItem(BlueGemItem.class);
    Item.registerItem(CoalItem.class);
    Item.registerItem(EnvelopeItem.class);
    Item.registerItem(GoldItem.class);
    Item.registerItem(IceWallPlacer.class);
    Item.registerItem(LeafItem.class);
    Item.registerItem(LogItem.class);
    Item.registerItem(MetalItem.class);
    Item.registerItem(PickaxeItem.class);
    Item.registerItem(RedGemItem.class);
    Item.registerItem(RingGoldItem.class);
    Item.registerItem(RingSilverItem.class);
    Item.registerItem(StaminaPotionItem.class);
    Item.registerItem(StickItem.class);
    Item.registerItem(StrengthRingItem.class);
    Item.registerItem(TorchItem.class);
    Item.registerItem(WaterPotionItem.class);
    Item.registerItem(WoodenBridgeItem.class);
  }

  private static void configGame() throws IOException {
    Game.loadConfig(
        new SimpleIPath("dungeon_config.json"),
        contrib.configuration.KeyboardConfig.class,
        core.configuration.KeyboardConfig.class);
    Game.disableAudio(true);
    Game.frameRate(30);
  }

  private static void createSystems() {
    if (DEBUG_MODE) Game.add(new LevelEditorSystem());
    Game.add(new LevelHideSystem());
    Game.add(new CollisionSystem());
    Game.add(new ManaRestoreSystem());
    if (!DEBUG_MODE) Game.add(new StaminaDrainSystem());
    Game.add(new StaminaExhaustionSystem());
    Game.add(new DebugSleepSystem());
    Game.add(new AISystem());
    Game.add(new ProjectileSystem());
    Game.add(new HealthSystem());
    Game.add(new SpikeSystem());
    if (!DEBUG_MODE) Game.add(new FallingSystem());
    Game.add(new PathSystem());
    Game.add(new PitSystem());
    Game.add(new LeverSystem());
    Game.add(new PressurePlateSystem());
    Game.add(new IdleSoundSystem());
    Game.add(new GuardDetectionSystem());
    Game.add(new AttachmentSystem());
    // Game.add(new IllegalSystem());
    Game.add(new BedSleepSystem());
    if (DEBUG_MODE) Game.add(new Debugger());
    Game.add(new TimerSystem());
  }

  private static void setupMusic() {
    Music backgroundMusic = Gdx.audio.newMusic(Gdx.files.internal(BACKGROUND_MUSIC));
    backgroundMusic.setLooping(true);
    backgroundMusic.play();
    backgroundMusic.setVolume(.05f);
  }

  /** Registers network message handlers for timer synchronization. */
  private static void registerTimerHandlers() {
    var dispatcher = Game.network().messageDispatcher();

    // Handle timer command messages (START, STOP, RESUME)
    dispatcher.registerHandler(
        TimerCommandMessage.class,
        (session, msg) -> {
          System.out.println("Received TimerCommandMessage: " + msg.command());
          switch (msg.command()) {
            case START:
              // Create timer dialog if it doesn't exist
              DialogContext ctx =
                  DialogContext.builder()
                      .type(EscapeRoomDialogTypes.TIMER)
                      .put("startTimeSeconds", msg.startTimeSeconds())
                      .build();
              DialogFactory.show(ctx, false, false);

              TimerUI ui = TimerDialog.currentUI();
              if (ui != null) {
                ui.start(msg.startTimeSeconds());
              }
              break;
            case STOP:
              TimerUI stopUi = TimerDialog.currentUI();
              if (stopUi != null) {
                stopUi.stop();
              }
              break;
            case RESUME:
              TimerUI resumeUi = TimerDialog.currentUI();
              if (resumeUi != null) {
                resumeUi.resume();
              }
              break;
          }
        });

    // Handle periodic sync messages
    dispatcher.registerHandler(
        TimerSyncMessage.class,
        (session, msg) -> {
          TimerUI ui = TimerDialog.currentUI();
          if (ui != null) {
            ui.syncFromServer(msg.elapsedSeconds(), msg.running());
          }
        });
  }
}
