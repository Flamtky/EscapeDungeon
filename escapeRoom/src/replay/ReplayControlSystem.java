package replay;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.Stage;
import contrib.components.StaminaComponent;
import contrib.utils.components.Debugger;
import core.Entity;
import core.Game;
import core.System;
import core.components.CameraComponent;
import core.components.DrawComponent;
import core.components.PositionComponent;
import core.configuration.KeyboardConfig;
import core.game.WindowEventManager;
import core.systems.CameraSystem;
import core.systems.InputManager;
import core.utils.Point;
import java.awt.FileDialog;
import java.awt.Frame;
import java.awt.GraphicsEnvironment;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.function.Consumer;

/** Client-side control UI for DB-free replay playback. */
public final class ReplayControlSystem extends System {
  private static final Set<ReplayControlSystem> ACTIVE_SYSTEMS = new CopyOnWriteArraySet<>();
  private static final String REPLAY_DIRECTORY = "replays";

  private static final int ASSET_REFRESH_INTERVAL_FRAMES = 60;
  private static final int GHOST_OPAQUE_ALPHA = 0xFF;
  private static final int GHOST_SPECTATE_BACKGROUND_ALPHA = 0x33;
  private static final float ZOOM_STEP = 0.2f;
  private static final long KEY_SKIP_MS = 5_000;

  private ReplayCutscene cutscene;
  private final String replayDirectory;
  private final Entity freeCameraEntity;
  private final Consumer<Boolean> focusChangeListener;
  private ReplayControlOverlay overlay;
  private ReplayKeybindOverlay keybindOverlay;
  private ReplayTargetOverlay targetOverlay;
  private ReplayHoverTooltip tooltip;
  private final Runnable closeRequested;
  private Entity focusedGhost;
  private int assetRefreshCountdown;
  private boolean overlaysVisible = true;

  /**
   * Creates a replay control system.
   *
   * @param cutscene replay cutscene to control
   */
  public ReplayControlSystem(ReplayCutscene cutscene) {
    this(cutscene, REPLAY_DIRECTORY, null, () -> {});
  }

  /** Creates a replay control system without an initially selected replay. */
  public ReplayControlSystem() {
    this(null, REPLAY_DIRECTORY, null, () -> {});
  }

  /**
   * Creates a replay control system without an initially selected replay.
   *
   * @param freeCameraEntity entity that should receive the camera again after target focus ends
   */
  public ReplayControlSystem(Entity freeCameraEntity) {
    this(null, REPLAY_DIRECTORY, freeCameraEntity, () -> {});
  }

  /**
   * Creates a replay control system without an initially selected replay.
   *
   * @param freeCameraEntity entity that should receive the camera again after target focus ends
   * @param closeRequested callback invoked when the replay close hotkey is pressed
   */
  public ReplayControlSystem(Entity freeCameraEntity, Runnable closeRequested) {
    this(null, REPLAY_DIRECTORY, freeCameraEntity, closeRequested);
  }

  /**
   * Creates a replay control system.
   *
   * @param cutscene replay cutscene to control
   * @param freeCameraEntity entity that should receive the camera again after target focus ends
   */
  public ReplayControlSystem(ReplayCutscene cutscene, Entity freeCameraEntity) {
    this(cutscene, REPLAY_DIRECTORY, freeCameraEntity, () -> {});
  }

  private ReplayControlSystem(
      ReplayCutscene cutscene,
      String replayDirectory,
      Entity freeCameraEntity,
      Runnable closeRequested) {
    super(AuthoritativeSide.CLIENT, ReplayGhostComponent.class, PositionComponent.class);
    this.cutscene = cutscene;
    this.replayDirectory = replayDirectory;
    this.freeCameraEntity = freeCameraEntity;
    this.closeRequested = closeRequested == null ? () -> {} : closeRequested;
    this.focusChangeListener = this::handleWindowFocusChange;
    WindowEventManager.registerFocusChangeListener(focusChangeListener);
    ACTIVE_SYSTEMS.add(this);
  }

  @Override
  public void execute() {
    if (Game.isHeadless()) {
      return;
    }
    ensureUi();
    if (overlay == null) {
      return;
    }

    handleKeybinds();
    handleFocusInput();
    ensureFocusedGhostStillExists();
    overlay.updateState(cutscene);
    refreshAssetsIfNeeded();
    if (overlaysVisible) {
      updateHoverTooltip();
    } else if (tooltip != null) {
      tooltip.hide();
    }
  }

  @Override
  public void stop() {
    super.stop();
  }

  /** Removes replay UI, target focus, and any loaded replay ghosts. */
  public void close() {
    super.stop();
    ACTIVE_SYSTEMS.remove(this);
    clearFocus();
    if (cutscene != null) {
      cutscene.stop();
      cutscene = null;
    }
    if (overlay != null) {
      overlay.remove();
      overlay = null;
    }
    if (keybindOverlay != null) {
      keybindOverlay.remove();
      keybindOverlay = null;
    }
    if (targetOverlay != null) {
      targetOverlay.remove();
      targetOverlay = null;
    }
    if (tooltip != null) {
      tooltip.remove();
      tooltip = null;
    }
    WindowEventManager.unregisterFocusChangeListener(focusChangeListener);
  }

  /** Closes every active replay control instance and removes its UI and ghosts. */
  public static void closeAll() {
    ACTIVE_SYSTEMS.forEach(ReplayControlSystem::close);
  }

  private void ensureUi() {
    if (overlay != null) {
      return;
    }

    Game.stage()
        .ifPresent(
            stage -> {
              overlay =
                  new ReplayControlOverlay(
                      this::seekTo,
                      this::togglePlayback,
                      this::pause,
                      this::play,
                      this::skipBy,
                      this::replaySpeed,
                      () -> Debugger.ZOOM_CAMERA(-ZOOM_STEP),
                      () -> Debugger.ZOOM_CAMERA(ZOOM_STEP),
                      this::loadReplay);
              keybindOverlay = new ReplayKeybindOverlay();
              targetOverlay = new ReplayTargetOverlay();
              tooltip = new ReplayHoverTooltip();
              stage.addActor(overlay);
              stage.addActor(keybindOverlay);
              stage.addActor(targetOverlay);
              stage.addActor(tooltip);
              refreshAssets();
            });
  }

  private void seekTo(long elapsedMs) {
    if (cutscene != null) {
      cutscene.seekTo(elapsedMs);
    }
  }

  private void togglePlayback() {
    if (cutscene != null) {
      cutscene.togglePlayback();
    }
  }

  private void pause() {
    if (cutscene != null) {
      cutscene.pause();
    }
  }

  private void play() {
    if (cutscene != null) {
      cutscene.play();
    }
  }

  private void skipBy(long deltaMs) {
    if (cutscene != null) {
      cutscene.skipBy(deltaMs);
    }
  }

  private void replaySpeed(double replaySpeed) {
    if (cutscene != null) {
      cutscene.replaySpeed(replaySpeed);
    }
  }

  private void handleKeybinds() {
    if (replayCloseKeyPressed()) {
      closeRequested.run();
      return;
    }
    if (isAnyKeyJustPressed(Input.Keys.H, Input.Keys.TAB)) {
      overlaysVisible = !overlaysVisible;
      overlay.setVisible(overlaysVisible);
      keybindOverlay.setVisible(overlaysVisible);
      updateTargetOverlay();
      tooltip.hide();
    }
    if (InputManager.isKeyJustPressed(Input.Keys.B)) {
      StaminaComponent.barsVisible(!StaminaComponent.barsVisible());
    }
    if (cutscene == null) {
      return;
    }

    if (InputManager.isKeyJustPressed(Input.Keys.SPACE)) {
      cutscene.togglePlayback();
    }
    if (isAnyKeyJustPressed(Input.Keys.LEFT, Input.Keys.J)) {
      cutscene.skipBy(-KEY_SKIP_MS);
    }
    if (isAnyKeyJustPressed(Input.Keys.RIGHT, Input.Keys.L)) {
      cutscene.skipBy(KEY_SKIP_MS);
    }
    if (isAnyKeyJustPressed(Input.Keys.MINUS, Input.Keys.NUMPAD_SUBTRACT)) {
      cutscene.replaySpeed(ReplayControlOverlay.previousSpeed(cutscene.playbackSpeed()));
    }
    if (isAnyKeyJustPressed(Input.Keys.PLUS, Input.Keys.EQUALS, Input.Keys.NUMPAD_ADD)) {
      cutscene.replaySpeed(ReplayControlOverlay.nextSpeed(cutscene.playbackSpeed()));
    }
    if (InputManager.isKeyJustPressed(Input.Keys.NUM_1)) {
      cutscene.replaySpeed(ReplayControlOverlay.defaultSpeed());
    }
  }

  private static boolean isAnyKeyJustPressed(int... keys) {
    for (int key : keys) {
      if (InputManager.isKeyJustPressed(key)) {
        return true;
      }
    }
    return false;
  }

  private static boolean replayCloseKeyPressed() {
    return InputManager.isKeyJustPressed(Input.Keys.M);
  }

  private void loadReplay(String assetPath) {
    if (ReplayControlOverlay.SELECT_FILE_ACTION.equals(assetPath)) {
      importReplayFile();
      return;
    }
    loadReplay(assetPath, false);
  }

  private void loadReplay(String assetPath, boolean forceReload) {
    String currentAssetPath = currentAssetPath();
    if (assetPath == null || assetPath.isBlank()) {
      unloadReplay();
      return;
    }

    if (assetPath.equals(currentAssetPath)) {
      if (!forceReload) {
        return;
      }
    }

    try {
      clearFocus();
      if (cutscene == null) {
        cutscene =
            ReplayCutscene.fromAsset(assetPath)
                .replaySpeed(ReplayControlOverlay.defaultSpeed())
                .loop(false);
        cutscene.start();
      } else {
        cutscene.loadAsset(assetPath);
      }
      overlay.error("");
      refreshAssets();
    } catch (RuntimeException e) {
      overlay.error(e.getMessage());
      overlay.selectReplay(currentAssetPath());
    }
  }

  private void unloadReplay() {
    clearFocus();
    if (cutscene != null) {
      cutscene.stop();
      cutscene = null;
    }
    if (overlay != null) {
      overlay.error("");
      overlay.selectReplay(null);
    }
    if (tooltip != null) {
      tooltip.hide();
    }
    updateTargetOverlay();
    refreshAssets();
  }

  private void importReplayFile() {
    Optional<Path> selectedFile = chooseReplayFile();
    if (selectedFile.isEmpty()) {
      overlay.selectReplay(currentAssetPath());
      return;
    }

    try {
      String importedAssetPath =
          ReplayAssetRepository.importReplayFile(replayDirectory, selectedFile.get());
      refreshAssets();
      loadReplay(importedAssetPath, true);
    } catch (IOException | RuntimeException e) {
      overlay.error(e.getMessage());
      overlay.selectReplay(currentAssetPath());
    }
  }

  private Optional<Path> chooseReplayFile() {
    if (GraphicsEnvironment.isHeadless()) {
      overlay.error("File selector is not available in headless mode.");
      return Optional.empty();
    }

    FileDialog dialog = new FileDialog((Frame) null, "Select Replay JSON", FileDialog.LOAD);
    dialog.setFile("*.json");
    dialog.setFilenameFilter(
        (directory, fileName) -> fileName.toLowerCase(Locale.ROOT).endsWith(".json"));
    dialog.setVisible(true);
    if (dialog.getFile() == null) {
      return Optional.empty();
    }
    return Optional.of(Path.of(dialog.getDirectory(), dialog.getFile()));
  }

  private void handleFocusInput() {
    if (movementInputPressed()) {
      if (focusedGhost != null) {
        clearFocus();
      }
      return;
    }

    if (!InputManager.isButtonJustPressed(Input.Buttons.LEFT)) {
      return;
    }

    Stage stage = Game.stage().orElse(null);
    if (stage == null) {
      return;
    }

    Vector2 stageMouse = stageMousePoint(stage);
    if (overlay.containsStagePoint(stageMouse.x, stageMouse.y)) {
      return;
    }

    hoveredGhost().ifPresent(this::focusGhost);
  }

  private boolean movementInputPressed() {
    return InputManager.isKeyPressed(KeyboardConfig.MOVEMENT_LEFT.value())
        || InputManager.isKeyPressed(KeyboardConfig.MOVEMENT_RIGHT.value())
        || InputManager.isKeyPressed(KeyboardConfig.MOVEMENT_UP.value())
        || InputManager.isKeyPressed(KeyboardConfig.MOVEMENT_DOWN.value());
  }

  private void focusGhost(Entity ghost) {
    if (focusedGhost != null && focusedGhost != ghost) {
      focusedGhost.remove(CameraComponent.class);
    }
    if (freeCameraEntity != null) {
      freeCameraEntity.remove(CameraComponent.class);
    }
    focusedGhost = ghost;
    if (!focusedGhost.isPresent(CameraComponent.class)) {
      focusedGhost.add(new CameraComponent());
    }
    updateTargetOverlay();
    updateGhostOpacity();
  }

  private void clearFocus() {
    if (focusedGhost != null) {
      focusedGhost.remove(CameraComponent.class);
    }
    focusedGhost = null;
    if (freeCameraEntity != null) {
      Vector3 cameraPosition = CameraSystem.camera().position;
      freeCameraEntity
          .fetch(PositionComponent.class)
          .ifPresent(position -> position.position(new Point(cameraPosition.x, cameraPosition.y)));
      if (!freeCameraEntity.isPresent(CameraComponent.class)) {
        freeCameraEntity.add(new CameraComponent());
      }
    }
    updateTargetOverlay();
    updateGhostOpacity();
  }

  private void updateTargetOverlay() {
    if (targetOverlay == null) {
      return;
    }

    if (!overlaysVisible || focusedGhost == null) {
      targetOverlay.clearTarget();
      return;
    }

    focusedGhost
        .fetch(ReplayGhostComponent.class)
        .map(ReplayGhostComponent::playerId)
        .ifPresentOrElse(targetOverlay::target, targetOverlay::clearTarget);
  }

  private void ensureFocusedGhostStillExists() {
    if (focusedGhost == null) {
      return;
    }

    if (!focusedGhost.isPresent(PositionComponent.class)) {
      clearFocus();
      return;
    }

    if (!focusedGhost.isPresent(CameraComponent.class)) {
      focusedGhost.add(new CameraComponent());
    }
    updateGhostOpacity();
  }

  private void updateGhostOpacity() {
    if (cutscene == null) {
      return;
    }
    Optional<ReplayGhostComponent> focusedGhostComponent = focusedGhostComponent();
    Optional<String> partnerId =
        focusedGhostComponent.flatMap(
            ghostComponent -> adjacentPartnerId(ghostComponent.playerId()));
    cutscene
        .ghosts()
        .values()
        .forEach(ghost -> updateGhostOpacity(ghost, focusedGhostComponent, partnerId));
  }

  private void updateGhostOpacity(
      Entity ghost,
      Optional<ReplayGhostComponent> focusedGhostComponent,
      Optional<String> partnerId) {
    ghost
        .fetch(ReplayGhostComponent.class)
        .ifPresent(
            ghostComponent ->
                ghost
                    .fetch(DrawComponent.class)
                    .ifPresent(
                        drawComponent ->
                            drawComponent.tintColor(
                                tintWithAlpha(
                                    ghostComponent.tintColor(),
                                    alphaForGhost(
                                        ghostComponent, focusedGhostComponent, partnerId)))));
  }

  private Optional<ReplayGhostComponent> focusedGhostComponent() {
    return focusedGhost == null ? Optional.empty() : focusedGhost.fetch(ReplayGhostComponent.class);
  }

  private Optional<String> adjacentPartnerId(String focusedPlayerId) {
    List<ReplayGhostComponent> ghostComponents =
        cutscene.ghosts().values().stream()
            .map(ghost -> ghost.fetch(ReplayGhostComponent.class))
            .flatMap(Optional::stream)
            .toList();
    for (int index = 0; index < ghostComponents.size(); index++) {
      ReplayGhostComponent ghostComponent = ghostComponents.get(index);
      if (ghostComponent.playerId().equals(focusedPlayerId)) {
        return adjacentPartnerId(ghostComponents, index);
      }
    }
    return Optional.empty();
  }

  private Optional<String> adjacentPartnerId(
      List<ReplayGhostComponent> ghostComponents, int focusedIndex) {
    int partnerIndex = focusedIndex % 2 == 0 ? focusedIndex + 1 : focusedIndex - 1;
    if (partnerIndex < 0 || partnerIndex >= ghostComponents.size()) {
      return Optional.empty();
    }

    ReplayGhostComponent focusedGhostComponent = ghostComponents.get(focusedIndex);
    ReplayGhostComponent partnerCandidate = ghostComponents.get(partnerIndex);
    if (focusedGhostComponent.characterClass() == partnerCandidate.characterClass()) {
      return Optional.empty();
    }
    return Optional.of(partnerCandidate.playerId());
  }

  private int alphaForGhost(
      ReplayGhostComponent ghostComponent,
      Optional<ReplayGhostComponent> focusedGhostComponent,
      Optional<String> partnerId) {
    if (focusedGhostComponent.isEmpty()
        || ghostComponent.playerId().equals(focusedGhostComponent.orElseThrow().playerId())
        || partnerId.map(ghostComponent.playerId()::equals).orElse(false)) {
      return GHOST_OPAQUE_ALPHA;
    }
    return GHOST_SPECTATE_BACKGROUND_ALPHA;
  }

  private static int tintWithAlpha(int tintColor, int alpha) {
    if (tintColor == -1 && alpha == GHOST_OPAQUE_ALPHA) {
      return -1;
    }
    int baseColor = tintColor == -1 ? 0xFFFFFFFF : tintColor;
    return (baseColor & 0xFFFFFF00) | alpha;
  }

  private void refreshAssetsIfNeeded() {
    if (assetRefreshCountdown-- > 0) {
      return;
    }
    refreshAssets();
  }

  private void handleWindowFocusChange(boolean focused) {
    if (!focused) {
      return;
    }

    if (overlay == null) {
      assetRefreshCountdown = 0;
      return;
    }
    refreshAssets();
  }

  private void refreshAssets() {
    assetRefreshCountdown = ASSET_REFRESH_INTERVAL_FRAMES;
    overlay.replayAssets(
        ReplayAssetRepository.listReplayAssets(replayDirectory, currentAssetPath()),
        currentAssetPath());
  }

  private String currentAssetPath() {
    return cutscene == null ? null : cutscene.assetPath();
  }

  private void updateHoverTooltip() {
    Stage stage = Game.stage().orElse(null);
    if (stage == null || tooltip == null) {
      return;
    }

    Vector2 stageMouse = stageMousePoint(stage);
    if (overlay.containsStagePoint(stageMouse.x, stageMouse.y)) {
      tooltip.hide();
      return;
    }

    Optional<Entity> hoveredGhost = hoveredGhost();
    if (hoveredGhost.isEmpty()) {
      tooltip.hide();
      return;
    }

    String tooltipText = tooltipText(hoveredGhost.get());
    tooltip.show(tooltipText, stageMouse.x, stageMouse.y, stage.getWidth(), stage.getHeight());
  }

  private Optional<Entity> hoveredGhost() {
    Point mousePoint = worldMousePoint();
    return filteredEntityStream()
        .filter(entity -> isEntityHovered(entity, mousePoint))
        .min(
            Comparator.comparingDouble(
                entity ->
                    entity
                        .fetch(PositionComponent.class)
                        .map(position -> position.position().distanceSquared(mousePoint))
                        .orElse(Double.MAX_VALUE)));
  }

  private Point worldMousePoint() {
    Vector3 mousePos =
        CameraSystem.camera()
            .unproject(new Vector3(InputManager.mouseScreenX(), InputManager.mouseScreenY(), 0));
    return new Point(mousePos.x, mousePos.y);
  }

  private Vector2 stageMousePoint(Stage stage) {
    return stage.screenToStageCoordinates(
        new Vector2(InputManager.mouseScreenX(), InputManager.mouseScreenY()));
  }

  private boolean isEntityHovered(Entity entity, Point mousePoint) {
    final float hoverRadius = 0.5f;
    return entity
        .fetch(PositionComponent.class)
        .map(
            positionComponent ->
                entity
                    .fetch(DrawComponent.class)
                    .map(
                        drawComponent ->
                            isInsideDrawBounds(positionComponent, drawComponent, mousePoint))
                    .orElseGet(
                        () -> positionComponent.position().distance(mousePoint) < hoverRadius))
        .orElse(false);
  }

  private boolean isInsideDrawBounds(
      PositionComponent positionComponent, DrawComponent drawComponent, Point mousePoint) {
    Point bottomLeft = positionComponent.position();
    return bottomLeft.x() <= mousePoint.x()
        && mousePoint.x() <= bottomLeft.x() + drawComponent.getWidth()
        && bottomLeft.y() <= mousePoint.y()
        && mousePoint.y() <= bottomLeft.y() + drawComponent.getHeight();
  }

  private String tooltipText(Entity ghost) {
    ReplayGhostComponent ghostComponent = ghost.fetch(ReplayGhostComponent.class).orElseThrow();
    Optional<ReplayFrame> frame = cutscene.frameFor(ghostComponent.playerId());
    long currentTimeMs = frame.map(ReplayFrame::currentTimeMs).orElse(cutscene.currentTimeMs());
    StringBuilder text = new StringBuilder();
    text.append("Player: ").append(ghostComponent.playerId());
    text.append('\n').append("Class: ").append(ghostComponent.characterClass());
    cutscene
        .sourceTimeAt(currentTimeMs)
        .ifPresent(sourceTime -> text.append('\n').append("Source: ").append(sourceTime));
    frame.ifPresent(
        replayFrame ->
            text.append('\n')
                .append("DB pos: ")
                .append(formatPoint(replayFrame.position()))
                .append('\n')
                .append("Stamina: ")
                .append(formatStamina(ghost, replayFrame.stamina()))
                .append('\n')
                .append("Last input: ")
                .append(ReplayControlOverlay.formatWholeTime(replayFrame.lastEventTimeMs())));
    return text.toString();
  }

  private static String formatPoint(Point point) {
    return String.format(Locale.ROOT, "%.2f, %.2f", point.x(), point.y());
  }

  private static String formatStamina(Entity ghost, Optional<Float> stamina) {
    if (stamina.isEmpty()) {
      return "-";
    }

    Optional<Float> maxStamina = ghost.fetch(StaminaComponent.class).map(StaminaComponent::max);
    if (maxStamina.isEmpty() || maxStamina.orElseThrow() <= 0) {
      return String.format(Locale.ROOT, "%.1f", stamina.orElseThrow());
    }

    float current = stamina.orElseThrow();
    float max = maxStamina.orElseThrow();
    float percent = current / max * 100f;
    return String.format(Locale.ROOT, "%.1f/%.1f (%.0f%%)", current, max, percent);
  }
}
