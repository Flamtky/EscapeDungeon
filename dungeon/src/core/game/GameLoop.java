package core.game;

import static com.badlogic.gdx.graphics.GL20.GL_COLOR_BUFFER_BIT;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.backends.headless.HeadlessFiles;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Graphics;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Window;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.Scaling;
import com.badlogic.gdx.utils.SharedLibraryLoader;
import com.badlogic.gdx.utils.viewport.ScalingViewport;
import contrib.components.SkillComponent;
import contrib.components.UIComponent;
import contrib.crafting.Crafting;
import contrib.entities.CharacterClass;
import contrib.entities.HeroBuilder;
import contrib.entities.deco.Deco;
import contrib.entities.deco.DecoFactory;
import contrib.hud.UIUtils;
import contrib.hud.dialogs.DialogFactory;
import contrib.systems.AttributeBarSystem;
import contrib.systems.DebugDrawSystem;
import contrib.systems.EventScheduler;
import contrib.systems.SkillHudSystem;
import contrib.utils.CheckPatternPainter;
import core.Entity;
import core.Game;
import core.System;
import core.components.DrawComponent;
import core.components.PlayerComponent;
import core.components.PositionComponent;
import core.level.loader.DungeonLoader;
import core.level.loader.LevelParser;
import core.network.ConnectionListener;
import core.network.MessageDispatcher;
import core.network.client.ClientNetwork;
import core.network.messages.c2s.InputMessage;
import core.network.messages.s2c.DeltaSnapshotMessage;
import core.network.messages.s2c.DialogCloseMessage;
import core.network.messages.s2c.DialogShowMessage;
import core.network.messages.s2c.EntityDespawnEvent;
import core.network.messages.s2c.EntitySpawnEvent;
import core.network.messages.s2c.GameOverEvent;
import core.network.messages.s2c.LevelChangeEvent;
import core.network.messages.s2c.SnapshotMessage;
import core.sound.player.GdxSoundPlayer;
import core.sound.player.ISoundPlayer;
import core.sound.player.NoSoundPlayer;
import core.systems.CameraSystem;
import core.systems.DrawSystem;
import core.systems.FrictionSystem;
import core.systems.InputManager;
import core.systems.InputSystem;
import core.systems.LevelSystem;
import core.systems.MoveSystem;
import core.systems.PositionSystem;
import core.systems.VelocitySystem;
import core.utils.ClientNamePersistence;
import core.utils.Direction;
import core.utils.EntityIdProvider;
import core.utils.IVoidFunction;
import core.utils.Point;
import core.utils.Tuple;
import core.utils.components.MissingComponentException;
import core.utils.logging.DungeonLogger;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import org.lwjgl.glfw.GLFWNativeWin32;
import org.lwjgl.system.Platform;
import org.lwjgl.system.windows.User32;

/**
 * The Dungeon-GameLoop.
 *
 * <p>This class contains the game loop method that is connected with libGDX. It controls the system
 * flow, will execute the Systems, and triggers the event callbacks configured in the {@link
 * PreRunConfiguration}.
 *
 * <p>Use {@link #run()} to start the game.
 *
 * <p>All API methods can also be accessed via the {@link core.Game} class.
 */
public final class GameLoop extends ScreenAdapter {
  private static final DungeonLogger LOGGER = DungeonLogger.getLogger(GameLoop.class);
  private static ISoundPlayer soundPlayer = new NoSoundPlayer();
  private static final List<IResizable> resizables = new ArrayList<>();
  private static Stage stage;
  private static boolean borderlessWindowedFullscreen = false;
  private static int windowedWidthBeforeBorderless = PreRunConfiguration.windowWidth();
  private static int windowedHeightBeforeBorderless = PreRunConfiguration.windowHeight();
  private static int windowedXBeforeBorderless = 0;
  private static int windowedYBeforeBorderless = 0;
  private static boolean windowedPositionBeforeBorderlessAvailable = false;
  private static boolean borderlessWindowListenersRegistered = false;
  private static long windowStyleBeforeBorderless = 0L;
  private static boolean windowStyleBeforeBorderlessAvailable = false;
  private boolean doSetup = true;
  private float tickAccumulator = 0f;
  private boolean tickOnNextRender = true;
  private static float renderInterpolationAlpha = 0f;

  /**
   * Sets {@link Game#currentLevel} to the new level and changes the currently active entity
   * storage.
   *
   * <p>Will remove all Systems using {@link ECSManagement#removeAllSystems()} from the Game. This
   * will trigger {@link System#onEntityRemove} for the old level. Then, it will readd all Systems
   * using {@link ECSManagement#add(System)}, triggering {@link System#onEntityAdd} for the new
   * level.
   *
   * <p>Will re-add the player if they exist.
   */
  public static final IVoidFunction onLevelLoad =
      () -> {
        if (Game.isCheckPatternEnabled())
          Game.currentLevel()
              .ifPresent(level -> CheckPatternPainter.paintCheckerPattern(level.layout()));

        if (!PreRunConfiguration.isNetworkServer()) return; // no authority

        Game.currentLevel().ifPresent(level -> level.finishedLoading(false));

        List<Entity> allPlayers = ECSManagement.allPlayers().toList();
        boolean firstLoad = !ECSManagement.levelStorageMap().containsKey(Game.currentLevel().get());
        allPlayers.forEach(ECSManagement::remove);
        // Remove the systems so that each triggerOnRemove(entity) will be called (basically
        // cleanup).
        Map<Class<? extends System>, System> s = ECSManagement.systems();
        ECSManagement.removeAllSystems();
        ECSManagement.activeEntityStorage(
            ECSManagement.levelStorageMap()
                .computeIfAbsent(Game.currentLevel().orElse(null), k -> new HashSet<>()));
        // readd the systems so that each triggerOnAdd(entity) will be called (basically
        // setup). This will also create new EntitySystemMapper if needed.
        s.values().forEach(ECSManagement::add);

        try {
          allPlayers.forEach(GameLoop::placeOnLevelStart);
        } catch (MissingComponentException e) {
          LOGGER.warn(e.getMessage());
        }
        ECSManagement.allEntities()
            .filter(Entity::isPersistent)
            .map(ECSManagement::remove)
            .forEach(ECSManagement::add);

        Game.currentLevel()
            .ifPresent(
                level -> {
                  // hero pos or default 0,0
                  final Point heroPos =
                      allPlayers.stream()
                          .findFirst()
                          .flatMap(e -> e.fetch(PositionComponent.class))
                          .map(PositionComponent::position)
                          .orElse(new Point(0, 0));
                  final int batch_size = 25;

                  List<Tuple<Deco, Point>> sortedDecos =
                      level.decorations().stream()
                          .sorted(Comparator.comparingDouble(d -> heroPos.distanceSquared(d.b())))
                          .toList();

                  long batches = (sortedDecos.size() + batch_size - 1) / batch_size;

                  for (int i = 0; i < batches; i++) {
                    final int skip = i * batch_size;
                    final int limit = Math.min(batch_size, sortedDecos.size() - skip);
                    int finalI = i;
                    EventScheduler.scheduleAction(
                        () -> {
                          sortedDecos
                              .subList(skip, skip + limit)
                              .forEach(t -> Game.add(DecoFactory.createDeco(t.b(), t.a())));
                          if (finalI == batches - 1) {
                            level.finishedLoading(true);
                          }
                        },
                        15L * i);
                  }
                });

        PreRunConfiguration.userOnLevelLoad().accept(firstLoad);
      };

  // for singleton
  private GameLoop() {}

  /**
   * Starts the dungeon.
   *
   * <p>If multiplayer is enabled and this is a network server, no window will be created, instead
   * the server will run headless.
   *
   * @see PreRunConfiguration
   */
  public static void run() {
    Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
    config.setWindowSizeLimits(
        PreRunConfiguration.windowWidth(), PreRunConfiguration.windowHeight(), 9999, 9999);
    config.setForegroundFPS(PreRunConfiguration.maxFPS());
    config.setResizable(PreRunConfiguration.resizeable());
    config.setTitle(PreRunConfiguration.windowTitle());
    config.setWindowIcon(PreRunConfiguration.logoPath().pathString());
    config.disableAudio(PreRunConfiguration.disableAudio());
    config.setWindowListener(WindowEventManager.windowListener());
    if (SharedLibraryLoader.isMac && Gdx.app == null) {
      org.lwjgl.system.Configuration.GLFW_LIBRARY_NAME.set("glfw_async");
    }
    borderlessWindowedFullscreen = PreRunConfiguration.fullScreen();
    registerBorderlessWindowListeners();
    if (borderlessWindowedFullscreen) {
      configureBorderlessWindowedFullscreen(config);
    } else {
      config.setWindowedMode(PreRunConfiguration.windowWidth(), PreRunConfiguration.windowHeight());
    }

    if (!PreRunConfiguration.multiplayerEnabled() || !PreRunConfiguration.isNetworkServer()) {
      new Lwjgl3Application(
          new com.badlogic.gdx.Game() {
            @Override
            public void create() {
              setScreen(new GameLoop());
            }
          },
          config);
    } else {
      // Server mode does not create a window.
      new GameLoop().setup();
    }
  }

  /**
   * Get the {@link Stage} that can be used to draw HUD elements.
   *
   * @return The configured stage, can be empty.
   */
  public static Optional<Stage> stage() {
    return Optional.ofNullable(stage);
  }

  private static void updateStage(final Stage stage) {
    stage.act(Gdx.graphics.getDeltaTime());
    stage.draw();
  }

  private static void setupStage() {
    stage =
        new Stage(
            new ScalingViewport(
                Scaling.stretch,
                PreRunConfiguration.windowWidth(),
                PreRunConfiguration.windowHeight()),
            new SpriteBatch());
    Gdx.input.setInputProcessor(stage);
    InputManager.init();
  }

  private static void configureBorderlessWindowedFullscreen(Lwjgl3ApplicationConfiguration config) {
    config.setWindowedMode(PreRunConfiguration.windowWidth(), PreRunConfiguration.windowHeight());
    config.setMaximized(true);
    if (!isWindows()) {
      config.setDecorated(false);
      config.setResizable(true);
    }
  }

  private static void registerBorderlessWindowListeners() {
    if (borderlessWindowListenersRegistered) {
      return;
    }

    WindowEventManager.registerWindowCreatedListener(
        window -> {
          if (borderlessWindowedFullscreen && enterNativeBorderlessWindow(window)) {
            window.postRunnable(GameLoop::synchronizeWindowAfterTransition);
          }
        });
    WindowEventManager.registerFocusChangeListener(
        focused -> {
          if (focused && borderlessWindowedFullscreen && Gdx.app != null) {
            Gdx.app.postRunnable(GameLoop::synchronizeWindowAfterTransition);
          }
        });
    borderlessWindowListenersRegistered = true;
  }

  private static void synchronizeWindowAfterTransition() {
    if (Gdx.graphics == null) {
      return;
    }

    int width = Gdx.graphics.getWidth();
    int height = Gdx.graphics.getHeight();
    stage()
        .ifPresent(
            x -> {
              x.getViewport().setWorldSize(width, height);
              x.getViewport().update(width, height, true);
            });
    resizables.forEach(resizable -> resizable.onResize(width, height));
    WindowEventManager.windowListener().refreshRequested();
    ECSManagement.system(DrawSystem.class, DrawSystem::synchronizeWindowSize);
    Gdx.graphics.requestRendering();
  }

  private static void synchronizeWindowAfterTransitionNowAndNextFrame() {
    synchronizeWindowAfterTransition();
    if (Gdx.app != null) {
      Gdx.app.postRunnable(GameLoop::synchronizeWindowAfterTransition);
    }
  }

  private static boolean isWindows() {
    return Platform.get() == Platform.WINDOWS;
  }

  private static boolean enterNativeBorderlessWindow(Lwjgl3Window window) {
    if (!isWindows()) {
      return false;
    }

    long hwnd = GLFWNativeWin32.glfwGetWin32Window(window.getWindowHandle());
    if (hwnd == 0L) {
      return false;
    }

    long currentStyle = User32.GetWindowLongPtr(hwnd, User32.GWL_STYLE);
    if (!windowStyleBeforeBorderlessAvailable) {
      windowStyleBeforeBorderless = currentStyle;
      windowStyleBeforeBorderlessAvailable = true;
    }

    User32.SetWindowLongPtr(hwnd, User32.GWL_STYLE, currentStyle & ~User32.WS_CAPTION);
    applyWindowFrameStyle(hwnd);
    User32.ShowWindow(hwnd, User32.SW_MAXIMIZE);
    return true;
  }

  private static boolean exitNativeBorderlessWindow(Lwjgl3Window window) {
    if (!isWindows()) {
      return false;
    }

    long hwnd = GLFWNativeWin32.glfwGetWin32Window(window.getWindowHandle());
    if (hwnd == 0L) {
      return false;
    }

    User32.ShowWindow(hwnd, User32.SW_RESTORE);
    if (windowStyleBeforeBorderlessAvailable) {
      User32.SetWindowLongPtr(hwnd, User32.GWL_STYLE, windowStyleBeforeBorderless);
      applyWindowFrameStyle(hwnd);
    }
    return true;
  }

  private static void applyWindowFrameStyle(long hwnd) {
    User32.SetWindowPos(
        hwnd,
        0L,
        0,
        0,
        0,
        0,
        User32.SWP_NOMOVE
            | User32.SWP_NOSIZE
            | User32.SWP_NOZORDER
            | User32.SWP_NOACTIVATE
            | User32.SWP_FRAMECHANGED);
  }

  /**
   * Get the current tick of the game.
   *
   * <p>The tick is incremented every game tick, starting from 0 at the beginning of the game.
   *
   * @return the current tick
   */
  public static int currentTick() {
    return ECSManagement.currentTick();
  }

  /**
   * Returns how far rendering is between the previous and current fixed game tick.
   *
   * <p>Rendering can use this value for visual-only interpolation while gameplay logic stays on
   * fixed ticks.
   *
   * @return interpolation factor in the range {@code [0, 1]}
   */
  public static float renderInterpolationAlpha() {
    return renderInterpolationAlpha;
  }

  /**
   * Main render loop.
   *
   * <p>Triggers rendering as fast as libGDX can provide frames and advances fixed-rate game ticks
   * when due.
   *
   * <p>Will trigger {@link #frame} and {@link PreRunConfiguration#userOnFrame()} when a game tick
   * is due.
   *
   * <p>On the first frame, {@link #setup()} and {@link PreRunConfiguration#userOnSetup()} are
   * triggered.
   *
   * @param delta The time since the last rendered frame.
   */
  @Override
  public void render(float delta) {
    if (doSetup) setup();
    runDueGameTicks(delta);
    renderInterpolationAlpha = Math.min(1f, tickAccumulator / tickInterval());
    ECSManagement.system(CameraSystem.class, cameraSystem -> cameraSystem.prepareRender(delta));
    ECSManagement.system(
        DrawSystem.class,
        drawSystem -> DrawSystem.batch().setProjectionMatrix(CameraSystem.camera().combined));
    clearScreen();

    ECSManagement.renderSystems(delta);

    // stage logic
    stage().ifPresent(GameLoop::updateStage);
  }

  private void runDueGameTicks(float delta) {
    tickAccumulator += delta;
    float tickInterval = tickInterval();

    while (tickOnNextRender || tickAccumulator >= tickInterval) {
      gameTick(tickInterval);
      tickAccumulator = Math.max(0f, tickAccumulator - tickInterval);
      tickOnNextRender = false;
    }
  }

  private static float tickInterval() {
    return 1f / PreRunConfiguration.tickRate();
  }

  private void gameTick(float delta) {
    // Drain any inbound network messages on the game thread before running systems.
    try {
      Game.network().pollAndDispatch();
    } catch (Exception e) {
      LOGGER.warn("Error while polling network messages: {}", e.getMessage(), e);
    }
    frame(delta);

    // Execute ECS tick using shared runner. In MP client mode, run render/input/camera only.
    final boolean isMultiplayerClient =
        PreRunConfiguration.multiplayerEnabled() && !PreRunConfiguration.isNetworkServer();
    ECSManagement.executeOneTick(
        isMultiplayerClient ? System.AuthoritativeSide.CLIENT : System.AuthoritativeSide.BOTH);

    InputManager.update();
  }

  /**
   * Set up the client side of the game.
   *
   * <p>This method should be called only if in single player mode or multiplayer client mode.
   *
   * <p>Will execute {@link LevelSystem#execute()} once to load the first level before the actual
   * game loop starts. This ensures the first level is set at the start of the game loop, even if
   * the {@link LevelSystem} is not executed as the first system in the game loop.
   *
   * <p>It will:
   *
   * <ul>
   *   <li>Create all client relevant systems.
   *   <li>Set up the message handlers for network messages. (If multiplayer is enabled)
   *   <li>Set up connection listeners to reset input sequence on disconnect. (If multiplayer is
   *       enabled)
   *   <li>Set up the stage for HUD rendering.
   * </ul>
   *
   * <p>Will perform some setup.
   */
  private void setupClient() {
    LOGGER.info("Setting up client...");
    doSetup = false;
    if (Gdx.audio != null && !PreRunConfiguration.disableAudio()) {
      AssetManager assetManager = new AssetManager();
      soundPlayer = new GdxSoundPlayer(assetManager);
    }
    createSystems();

    if (PreRunConfiguration.multiplayerEnabled()) {
      DungeonLoader.afterAllLevels(() -> {}); // server controls this
      setupMessageHandlers();
      Game.network()
          .addConnectionListener(
              new ConnectionListener() {
                @Override
                public void onConnected() {}

                @Override
                public void onDisconnected(String reason) {
                  InputMessage.resetSequence();
                }
              });
    }
    setupStage();
  }

  /**
   * Called once at the beginning of the game.
   *
   * <p>It will:
   *
   * <ul>
   *   <li>Set up the client if not in server mode.
   *   <li>Execute the user-defined setup callback.
   *   <li>Execute the LevelSystem to load the initial level.
   *   <li>Start the network handler.
   * </ul>
   *
   * @see PreRunConfiguration#userOnSetup()
   */
  private void setup() {
    LOGGER.info("Setting up game...");
    doSetup = false;
    if (!PreRunConfiguration.multiplayerEnabled() || !PreRunConfiguration.isNetworkServer()) {
      setupClient();
    } else {
      Gdx.files = new HeadlessFiles();
    }

    Crafting.loadRecipes();

    PreRunConfiguration.userOnSetup().execute();
    Game.network().start();

    if (!DungeonLoader.levelOrder().isEmpty()) {
      if (Game.currentLevel().isEmpty()) DungeonLoader.loadLevel(0); // load the first level
    } else LOGGER.warn("No levels found to load!");
  }

  private void setupMessageHandlers() {
    MessageDispatcher dispatcher = Game.network().messageDispatcher();

    dispatcher.registerHandler(
        EntitySpawnEvent.class,
        (ctx, event) -> {
          LOGGER.info("Received EntitySpawnEvent event: " + event.entityId());

          // check if the entity already exists
          if (EntityIdProvider.isRegistered(event.entityId())) {
            LOGGER.warn(
                "Received spawn event for already existing entity with ID: " + event.entityId());
            return;
          }

          // is hero?
          if (event.playerComponent() != null) {
            PlayerComponent pc = event.playerComponent();
            boolean alreadyGotAHero = Game.player().isPresent();
            boolean isLocal = Objects.equals(pc.playerName(), PreRunConfiguration.username());

            if (alreadyGotAHero) {
              LOGGER.debug("Already got a hero, checking if local player...");
              if (isLocal) {
                LOGGER.warn(
                    "Received spawn event for local player, but we already have a local player! ID: {} ",
                    event.entityId());
                return;
              }
            }

            Entity hero =
                HeroBuilder.builder()
                    .id(event.entityId())
                    .characterClass(CharacterClass.fromByteId(event.characterClassId()))
                    .isLocalPlayer(isLocal)
                    .username(pc.playerName())
                    .build();

            // Apply skill sync data from spawn event for proper cooldown display
            if (event.skillData() != null) {
              hero.fetch(SkillComponent.class).ifPresent(sc -> sc.applySyncData(event.skillData()));
            }

            Game.add(hero);
            return;
          }

          Entity newEntity = new Entity(event.entityId());
          newEntity.add(event.positionComponent());
          if (event.decoComponent() != null) newEntity.add(event.decoComponent());
          if (event.drawComponent() != null) newEntity.add(event.drawComponent());
          newEntity.persistent(event.isPersistent());

          // Apply skill data if present
          if (event.skillData() != null) {
            SkillComponent sc = new SkillComponent();
            sc.applySyncData(event.skillData());
            newEntity.add(sc);
          }

          Game.add(newEntity);
        });

    dispatcher.registerHandler(
        EntityDespawnEvent.class,
        (ctx, event) -> {
          LOGGER.info(
              "Received EntityDespawnEvent event: "
                  + event.entityId()
                  + ", reason: "
                  + event.reason());
          Entity entity = Game.findEntityById(event.entityId()).orElse(null);
          if (entity == null) {
            LOGGER.warn("Received despawn event for unknown entity with ID: " + event.entityId());
            return;
          }
          Game.remove(entity);
        });

    dispatcher.registerHandler(
        LevelChangeEvent.class,
        (ctx, event) -> {
          LOGGER.info("Received LevelChangeEvent event: {}", event.levelName());
          try {
            Game.currentLevel(LevelParser.parseLevel(event.levelData(), event.levelName()));
            Game.player()
                .ifPresent(
                    entity -> {
                      placeOnLevelStart(entity);
                      Game.system(
                          CameraSystem.class,
                          cs -> {
                            Game.positionOf(entity).ifPresent(cs::instantFocus);
                          });
                    });
          } catch (Exception e) {
            LOGGER.error("Failed to handle LevelChangeEvent: {}", e.getMessage(), e);
          }
        });
    dispatcher.registerHandler(
        GameOverEvent.class,
        (ctx, event) -> {
          LOGGER.info("Received GameOverEvent event (reason: {})", event.reason());
          ClientNetwork.invalidateLastSessionFile();
          ClientNamePersistence.invalidate();
          Game.exit(event.reason());
        });
    dispatcher.registerHandler(
        SnapshotMessage.class,
        (ctx, event) -> {
          try {
            Game.network().snapshotTranslator().applySnapshot(event, dispatcher);
          } catch (Exception ignored) {
            LOGGER.warn("Error while applying snapshot message: {}", ignored.getMessage(), ignored);
          }
        });

    dispatcher.registerHandler(
        DeltaSnapshotMessage.class,
        (ctx, event) -> {
          try {
            Game.network().snapshotTranslator().applyDelta(event, dispatcher);
          } catch (Exception ignored) {
            LOGGER.warn("Error while applying delta snapshot: {}", ignored.getMessage(), ignored);
          }
        });

    dispatcher.registerHandler(
        DialogShowMessage.class,
        (ctx, msg) -> {
          LOGGER.debug("Received DialogShowMessage for dialog: {}", msg.context().dialogId());

          DialogFactory.show(msg.context(), false, msg.canBeClosed(), new int[] {});
        });

    dispatcher.registerHandler(
        DialogCloseMessage.class,
        (ctx, msg) -> {
          LOGGER.debug("Received DialogCloseMessage for dialog: {}", msg.dialogId());
          // Find and remove the UiComponent with the given dialogId
          Game.allEntities()
              .filter(
                  e ->
                      e.fetch(UIComponent.class)
                          .map(
                              comp ->
                                  comp.dialogContext() != null
                                      && msg.dialogId().equals(comp.dialogContext().dialogId()))
                          .orElse(false))
              .findFirst()
              .flatMap(e -> e.fetch(UIComponent.class))
              .ifPresent(UIUtils::closeDialog);
        });
  }

  /**
   * Called at the beginning of each frame, before the entities are updated and the systems are
   * executed.
   *
   * <p>This is the place to add basic logic that isn't part of any system.
   *
   * @param delta The time since the last loop.
   */
  private void frame(float delta) {
    fullscreenKey();
    Game.soundPlayer().update(delta);
    PreRunConfiguration.userOnFrame().execute();
  }

  private void fullscreenKey() {
    if (InputManager.isKeyJustPressed(
        core.configuration.KeyboardConfig.TOGGLE_FULLSCREEN.value())) {
      if (borderlessWindowedFullscreen || Gdx.graphics.isFullscreen()) {
        exitBorderlessWindowedFullscreen();
      } else {
        enterBorderlessWindowedFullscreen();
      }
    }
  }

  private void enterBorderlessWindowedFullscreen() {
    if (!(Gdx.graphics instanceof Lwjgl3Graphics lwjglGraphics)) {
      return;
    }

    rememberWindowedMode(lwjglGraphics);
    if (!enterNativeBorderlessWindow(lwjglGraphics.getWindow())) {
      Gdx.graphics.setUndecorated(true);
      Gdx.graphics.setResizable(true);
      lwjglGraphics.getWindow().maximizeWindow();
    }
    borderlessWindowedFullscreen = true;
    synchronizeWindowAfterTransitionNowAndNextFrame();
  }

  private void exitBorderlessWindowedFullscreen() {
    if (!(Gdx.graphics instanceof Lwjgl3Graphics lwjglGraphics)) {
      return;
    }

    if (!exitNativeBorderlessWindow(lwjglGraphics.getWindow())) {
      lwjglGraphics.getWindow().restoreWindow();
      Gdx.graphics.setUndecorated(false);
      Gdx.graphics.setResizable(PreRunConfiguration.resizeable());
    }
    Gdx.graphics.setWindowedMode(windowedWidthBeforeBorderless, windowedHeightBeforeBorderless);
    if (windowedPositionBeforeBorderlessAvailable) {
      lwjglGraphics.getWindow().setPosition(windowedXBeforeBorderless, windowedYBeforeBorderless);
    }
    borderlessWindowedFullscreen = false;
    synchronizeWindowAfterTransitionNowAndNextFrame();
  }

  private void rememberWindowedMode(Lwjgl3Graphics lwjglGraphics) {
    if (borderlessWindowedFullscreen || Gdx.graphics.isFullscreen()) {
      return;
    }

    windowedWidthBeforeBorderless = Gdx.graphics.getWidth();
    windowedHeightBeforeBorderless = Gdx.graphics.getHeight();
    windowedXBeforeBorderless = lwjglGraphics.getWindow().getPositionX();
    windowedYBeforeBorderless = lwjglGraphics.getWindow().getPositionY();
    windowedPositionBeforeBorderlessAvailable = true;
  }

  /**
   * Set the position of the given entity to the position of the level-start.
   *
   * <p>A {@link PositionComponent} is needed.
   *
   * @param entity entity to set on the start of the level, normally this is the player.
   */
  private static void placeOnLevelStart(final Entity entity) {
    ECSManagement.add(entity);
    entity
        .fetch(PositionComponent.class)
        .ifPresent(
            pc -> {
              Game.startTile()
                  .ifPresentOrElse(
                      pc::position, () -> LOGGER.warn("No start tile found for the current level"));
              pc.viewDirection(Direction.DOWN); // look down by default
            });

    // reset animations
    entity.fetch(DrawComponent.class).ifPresent(DrawComponent::resetState);
  }

  /**
   * Clear the screen. Removes all.
   *
   * <p>Needs to be called before redraw something.
   */
  private void clearScreen() {
    Gdx.gl.glClearColor(0, 0, 0, 1);
    Gdx.gl.glClear(GL_COLOR_BUFFER_BIT);
  }

  @Override
  public void resize(int width, int height) {
    super.resize(width, height);
    stage()
        .ifPresent(
            x -> {
              x.getViewport().setWorldSize(width, height);
              x.getViewport().update(width, height, true);
            });
    resizables.forEach(resizable -> resizable.onResize(width, height));
    WindowEventManager.windowListener().refreshRequested();
  }

  /**
   * Registers a UI element for resize callbacks.
   *
   * @param resizable element to register
   */
  public static void registerResizable(IResizable resizable) {
    if (!resizables.contains(resizable)) {
      resizables.add(resizable);
    }
  }

  /**
   * Removes a UI element from resize callbacks.
   *
   * @param resizable element to remove
   */
  public static void removeResizable(IResizable resizable) {
    resizables.remove(resizable);
  }

  /**
   * Get the sound player used by the game.
   *
   * @return The sound player.
   */
  public static ISoundPlayer soundPlayer() {
    return soundPlayer;
  }

  /** Create the systems. */
  private void createSystems() {
    ECSManagement.add(new PositionSystem());
    ECSManagement.system(LevelSystem.class, ls -> ls.onLevelLoad(onLevelLoad));
    ECSManagement.add(new CameraSystem());
    ECSManagement.add(new VelocitySystem());
    ECSManagement.add(new FrictionSystem());
    ECSManagement.add(new MoveSystem());
    ECSManagement.add(new InputSystem());
    ECSManagement.add(new DebugDrawSystem());
    ECSManagement.add(new AttributeBarSystem());
    ECSManagement.add(new SkillHudSystem());
  }
}
