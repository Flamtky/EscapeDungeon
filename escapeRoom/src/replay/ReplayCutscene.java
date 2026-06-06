package replay;

import core.Entity;
import core.Game;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

/** Starts and controls a DB-free replay cutscene loaded from a Dungeon replay asset. */
public final class ReplayCutscene {
  private static final double DEFAULT_REPLAY_SPEED = 1.0;
  private static final boolean DEFAULT_LOOP = false;
  private static final boolean DEFAULT_INTERPOLATE = true;
  private static final double DEFAULT_STEP_INTERPOLATION_SECONDS = 0.25;
  private static final DateTimeFormatter SOURCE_TIME_FORMATTER =
      DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");
  private static final int[] TINT_COLORS = {
    0xFFFFFFFF, 0x80C7FFFF, 0xFFB86CFF, 0xB4F8C8FF, 0xFF8FA3FF, 0xF9F871FF
  };

  private ReplayAsset asset;
  private String assetPath;
  private double replaySpeed;
  private boolean loop;
  private boolean interpolate;
  private double stepInterpolationSeconds;
  private ReplayTimeline timeline;
  private ReplayPlaybackController controller;
  private ReplayGhostSystem replaySystem;
  private Map<String, Entity> ghosts;

  private ReplayCutscene(String assetPath, ReplayAsset asset) {
    this.assetPath = assetPath;
    this.asset = asset;
    this.replaySpeed = DEFAULT_REPLAY_SPEED;
    this.loop = DEFAULT_LOOP;
    this.interpolate = DEFAULT_INTERPOLATE;
    this.stepInterpolationSeconds = DEFAULT_STEP_INTERPOLATION_SECONDS;
    this.ghosts = Map.of();
  }

  /**
   * Loads a replay cutscene from an internal asset path.
   *
   * @param assetPath path inside the assets directory
   * @return a replay cutscene ready to start
   */
  public static ReplayCutscene fromAsset(String assetPath) {
    return new ReplayCutscene(assetPath, ReplayAssetLoader.load(assetPath));
  }

  /**
   * Sets the replay speed multiplier.
   *
   * @param replaySpeed speed multiplier, where {@code 1.0} is real time
   * @return this cutscene
   */
  public ReplayCutscene replaySpeed(double replaySpeed) {
    if (replaySpeed <= 0) {
      throw new IllegalArgumentException("replaySpeed must be greater than 0.");
    }
    this.replaySpeed = replaySpeed;
    if (controller != null) {
      controller.speed(replaySpeed);
    }
    return this;
  }

  /**
   * Sets whether the replay loops after reaching the end.
   *
   * @param loop true to loop the replay
   * @return this cutscene
   */
  public ReplayCutscene loop(boolean loop) {
    this.loop = loop;
    if (controller != null) {
      controller.loop(loop);
    }
    return this;
  }

  /**
   * Starts the replay cutscene.
   *
   * <p>This spawns visual-only ghosts and registers the replay system.
   */
  public void start() {
    if (replaySystem != null) {
      return;
    }

    timeline = new ReplayTimeline(asset, interpolate, stepInterpolationSeconds);
    controller = new ReplayPlaybackController(asset.durationMs(), replaySpeed, loop);
    ghosts = createGhosts();
    ghosts.values().forEach(Game::add);
    replaySystem = new ReplayGhostSystem(timeline, ghosts, controller);
    Game.add(replaySystem);
  }

  /** Stops the replay and removes its ghost entities from the current level. */
  public void stop() {
    if (replaySystem != null) {
      Game.remove(ReplayGhostSystem.class);
      replaySystem = null;
    }
    ghosts.values().forEach(Game::remove);
    ghosts = Map.of();
    controller = null;
    timeline = null;
  }

  /**
   * Indicates whether the replay has reached the end.
   *
   * @return true if playback finished and looping is disabled
   */
  public boolean finished() {
    return replaySystem != null && replaySystem.finished();
  }

  /**
   * Starts or resumes replay playback.
   *
   * @return this cutscene
   */
  public ReplayCutscene play() {
    if (controller != null) {
      controller.play();
    }
    return this;
  }

  /**
   * Pauses replay playback without removing ghosts.
   *
   * @return this cutscene
   */
  public ReplayCutscene pause() {
    if (controller != null) {
      controller.pause();
    }
    return this;
  }

  /**
   * Toggles replay playback between playing and paused.
   *
   * @return this cutscene
   */
  public ReplayCutscene togglePlayback() {
    if (controller != null) {
      controller.togglePlayback();
    }
    return this;
  }

  /**
   * Seeks the replay to a relative replay time.
   *
   * @param elapsedMs relative replay time in milliseconds
   * @return this cutscene
   */
  public ReplayCutscene seekTo(long elapsedMs) {
    if (controller != null) {
      controller.seekTo(elapsedMs);
      applyCurrentFrame();
    }
    return this;
  }

  /**
   * Skips the replay by a relative amount.
   *
   * @param deltaMs signed amount of milliseconds to skip
   * @return this cutscene
   */
  public ReplayCutscene skipBy(long deltaMs) {
    if (controller != null) {
      controller.skipBy(deltaMs);
      applyCurrentFrame();
    }
    return this;
  }

  /**
   * Loads another replay asset without restarting the game.
   *
   * @param newAssetPath path inside the assets directory
   * @return this cutscene
   */
  public ReplayCutscene loadAsset(String newAssetPath) {
    ReplayAsset newAsset = ReplayAssetLoader.load(newAssetPath);
    if (!asset.level().equals(newAsset.level())) {
      throw new IllegalArgumentException(
          "Replay level mismatch: expected " + asset.level() + " but got " + newAsset.level());
    }

    boolean wasStarted = replaySystem != null;
    boolean wasPlaying = playing();
    stop();
    assetPath = newAssetPath;
    asset = newAsset;
    if (wasStarted) {
      start();
      if (!wasPlaying) {
        pause();
      }
    }
    return this;
  }

  /**
   * Indicates whether playback is currently advancing.
   *
   * @return true if the replay is playing
   */
  public boolean playing() {
    return controller != null && controller.playing();
  }

  /**
   * Returns the current relative replay time.
   *
   * @return elapsed replay time in milliseconds
   */
  public long currentTimeMs() {
    return controller == null ? 0 : controller.currentTimeMs();
  }

  /**
   * Returns the replay duration.
   *
   * @return duration in milliseconds
   */
  public long durationMs() {
    return asset.durationMs();
  }

  /**
   * Returns the current playback speed multiplier.
   *
   * @return playback speed multiplier
   */
  public double playbackSpeed() {
    return controller == null ? replaySpeed : controller.speed();
  }

  /**
   * Returns the current replay asset path.
   *
   * @return path inside the assets directory
   */
  public String assetPath() {
    return assetPath;
  }

  /**
   * Returns the level name encoded in the replay asset.
   *
   * @return level name
   */
  public String level() {
    return asset.level();
  }

  /**
   * Returns the number of player tracks in the current replay.
   *
   * @return player track count
   */
  public int playerCount() {
    return asset.tracks().size();
  }

  /**
   * Returns a formatted absolute source time for a relative replay timestamp if available.
   *
   * @param elapsedMs relative replay time in milliseconds
   * @return formatted source time
   */
  public Optional<String> sourceTimeAt(long elapsedMs) {
    Object startTime = asset.source().get("startTime");
    if (!(startTime instanceof String startTimeText)) {
      return Optional.empty();
    }
    try {
      LocalDateTime sourceTime =
          LocalDateTime.parse(startTimeText).plusNanos(elapsedMs * 1_000_000);
      return Optional.of(sourceTime.format(SOURCE_TIME_FORMATTER));
    } catch (DateTimeParseException e) {
      return Optional.empty();
    }
  }

  Map<String, Entity> ghosts() {
    return ghosts;
  }

  Optional<ReplayFrame> frameFor(String playerId) {
    if (timeline == null) {
      return Optional.empty();
    }
    return Optional.ofNullable(timeline.frameAt(currentTimeMs()).get(playerId));
  }

  void applyCurrentFrame() {
    if (replaySystem != null) {
      replaySystem.applyCurrentFrame();
    }
  }

  private Map<String, Entity> createGhosts() {
    Map<String, Entity> createdGhosts = new LinkedHashMap<>();
    for (int i = 0; i < asset.tracks().size(); i++) {
      ReplayTrack track = asset.tracks().get(i);
      Entity ghost = ReplayGhostFactory.create(track, TINT_COLORS[i % TINT_COLORS.length]);
      createdGhosts.put(track.playerId(), ghost);
    }
    return createdGhosts;
  }
}
