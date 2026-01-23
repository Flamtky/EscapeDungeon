package analytics;

import contrib.utils.EntityUtils;
import core.Entity;
import core.Game;
import core.System;
import core.components.AnalyticsComponent;
import core.level.Tile;
import core.level.elements.tile.DoorTile;
import core.level.loader.DungeonLoader;
import core.level.utils.DesignLabel;
import core.utils.Point;
import core.utils.logging.DungeonLogger;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Stream;

public class RiddleAnalysisSystem extends System {

  private static final DungeonLogger LOGGER = DungeonLogger.getLogger(RiddleAnalysisSystem.class);
  private static final String MAROOM_LEVEL = "maroom";

  // 5 seconds minimum in riddle to count as attempt
  private static final long RIDDLE_ATTEMPT_THRESHOLD_MS = 5_000;

  private static final Set<RiddleAnalysisSystem.RiddleType> riddles =
      Set.of(
          RiddleAnalysisSystem.RiddleType.ICE_PUZZLE, RiddleAnalysisSystem.RiddleType.PUSH_PUZZLE);

  private final Map<Entity, RiddleAnalysisSystem.RiddleType> currentRiddleMap = new HashMap<>();
  private final Map<RiddleType, RiddleStats> riddleStatsMap = new HashMap<>();
  private final Map<String, Map<Point, RiddleAnalysisSystem.RiddleType>> levelBlockingTilesCache =
      new HashMap<>();

  private String currentLevelName = null;

  @Override
  public void execute() {
    String levelName = DungeonLoader.currentLevel();

    // Detect level change
    if (!levelName.equals(currentLevelName)
        && Game.currentLevel().isPresent()
        && Game.currentLevel().get().finishedLoading()) {
      currentLevelName = levelName;
      if (!MAROOM_LEVEL.equals(levelName)) {
        LOGGER.debug("Left maroom level: {}", levelName);
        currentRiddleMap.clear();
        return;
      }
      LOGGER.debug("Entered maroom level");
      initializeBlockingTilesForLevel(levelName);
    }

    // Only process riddles in maroom level
    if (!MAROOM_LEVEL.equals(levelName)) {
      return;
    }

    Game.allPlayers().forEach(this::processPlayerRiddle);
  }

  public RiddleAnalysisSystem.RiddleType currentRiddle(Entity player) {
    return currentRiddleMap.get(player);
  }

  private void processPlayerRiddle(Entity player) {
    RiddleAnalysisSystem.RiddleType currentRiddle = detectRiddle(player);
    RiddleAnalysisSystem.RiddleType previousRiddle = currentRiddleMap.get(player);

    if (currentRiddle != null) {
      handlePlayerInRiddle(player, currentRiddle, previousRiddle);
    } else {
      handlePlayerOutOfRiddle(player, previousRiddle);
    }
  }

  private void handlePlayerInRiddle(
      Entity player,
      RiddleAnalysisSystem.RiddleType currentRiddle,
      RiddleAnalysisSystem.RiddleType previousRiddle) {
    if (previousRiddle == null || !previousRiddle.equals(currentRiddle)) {
      // Player entered a new riddle
      currentRiddleMap.put(player, currentRiddle);
      newRiddle(player, currentRiddle);
    } else if (isRiddleSolved(currentRiddle)) {
      // Player is still in the same riddle, check if solved
      riddleSolved(player, currentRiddle);
    }
  }

  private void handlePlayerOutOfRiddle(
      Entity player, RiddleAnalysisSystem.RiddleType previousRiddle) {
    // Player is not in any riddle
    if (previousRiddle != null) {
      if (isRiddleSolved(previousRiddle)) {
        // Player has left a riddle (solved)
        riddleSolved(player, previousRiddle);
      } else {
        // Player has left a riddle (unsolved)
        riddleLeft(player, previousRiddle);
      }
      currentRiddleMap.remove(player);
    }
  }

  private void newRiddle(Entity player, RiddleAnalysisSystem.RiddleType riddle) {
    RiddleStats stats = riddleStatsMap.computeIfAbsent(riddle, k -> new RiddleStats());
    stats.solveStartTime = java.lang.System.currentTimeMillis();
    DungeonAnalyticsAPI.logXApiStatement(
        player.fetch(AnalyticsComponent.class).orElseThrow(),
        DungeonAnalyticsAPI.Verb.TRIES,
        "riddle_" + riddle.name.replace(" ", "_").toLowerCase(),
        Map.of("tries", stats.attemptCount),
        Map.of("riddle", riddle.name));
  }

  private void riddleSolved(Entity player, RiddleAnalysisSystem.RiddleType riddle) {
    RiddleStats stats = riddleStatsMap.get(riddle);
    if (stats == null) {
      LOGGER.warn("Riddle solved but no stats found: {}", riddle.name);
      return;
    }
    if (stats.solvedBy.contains(player.id())) {
      return; // Already recorded as solved by this player
    }

    long solveTime = java.lang.System.currentTimeMillis() - stats.solveStartTime;
    stats.solvedBy.add(player.id());

    // Only count as attempt if threshold is exceeded
    if (solveTime >= RIDDLE_ATTEMPT_THRESHOLD_MS) {
      stats.attemptCount++;
    }

    DungeonAnalyticsAPI.logXApiStatement(
        player.fetch(AnalyticsComponent.class).orElseThrow(),
        DungeonAnalyticsAPI.Verb.SOLVED,
        "riddle_" + riddle.name.replace(" ", "_").toLowerCase(),
        Map.of("tries", stats.attemptCount, "solve_time_ms", solveTime),
        Map.of("riddle", riddle.name));
  }

  private void riddleLeft(Entity player, RiddleAnalysisSystem.RiddleType riddle) {
    RiddleStats stats = riddleStatsMap.get(riddle);
    if (stats == null) {
      LOGGER.warn("Riddle left but no stats found: {}", riddle.name);
      return;
    }
    long timeInRiddle = java.lang.System.currentTimeMillis() - stats.solveStartTime;

    // Only count as attempt if threshold is exceeded
    if (timeInRiddle >= RIDDLE_ATTEMPT_THRESHOLD_MS) {
      stats.attemptCount++;
    }

    DungeonAnalyticsAPI.logXApiStatement(
        player.fetch(AnalyticsComponent.class).orElseThrow(),
        DungeonAnalyticsAPI.Verb.LEFT,
        "riddle_" + riddle.name.replace(" ", "_").toLowerCase(),
        Map.of("time_in_riddle_ms", timeInRiddle, "tries", stats.attemptCount),
        Map.of("riddle", riddle.name));
  }

  private RiddleAnalysisSystem.RiddleType detectRiddle(Entity player) {
    for (RiddleAnalysisSystem.RiddleType riddle : riddles) {
      if (riddle.isInRiddle(player)) {
        return riddle;
      }
    }
    return null;
  }

  private boolean isRiddleSolved(RiddleAnalysisSystem.RiddleType riddle) {
    String levelName = DungeonLoader.currentLevel();
    if (!MAROOM_LEVEL.equals(levelName)) {
      return false;
    }

    Map<Point, RiddleAnalysisSystem.RiddleType> blockingTiles =
        levelBlockingTilesCache.get(levelName);
    if (blockingTiles == null || blockingTiles.isEmpty()) {
      LOGGER.warn("Blocking tiles not initialized for level: {}", levelName);
      return false;
    }

    return blockingTiles.entrySet().stream()
        .filter(entry -> entry.getValue() == riddle)
        .map(Map.Entry::getKey)
        .allMatch(pos -> isRiddleSolvedAtBlockingTile(riddle, pos));
  }

  private void initializeBlockingTilesForLevel(String levelName) {
    Map<Point, RiddleAnalysisSystem.RiddleType> blockingTiles = new HashMap<>();

    // Get ICE_PUZZLE blocking tile
    Point iceWallPos =
        Game.currentLevel().map(level -> level.namedPoints().get("snow_Wall57")).orElse(null);

    if (iceWallPos != null) {
      blockingTiles.put(iceWallPos, RiddleAnalysisSystem.RiddleType.ICE_PUZZLE);
    } else {
      LOGGER.warn(
          "Missing or invalid named point 'snow_Wall57' for ICE_PUZZLE on level: {}", levelName);
    }

    // Get PUSH_PUZZLE blocking tile
    Point pushDoorPos1 =
        Game.currentLevel().map(level -> level.namedPoints().get("push_door19")).orElse(null);
    Point pushDoorPos2 =
        Game.currentLevel().map(level -> level.namedPoints().get("push_door18")).orElse(null);

    if (pushDoorPos1 != null && pushDoorPos2 != null) {
      blockingTiles.put(pushDoorPos1, RiddleAnalysisSystem.RiddleType.PUSH_PUZZLE);
      blockingTiles.put(pushDoorPos2, RiddleAnalysisSystem.RiddleType.PUSH_PUZZLE);
    } else {
      LOGGER.warn(
          "Missing or invalid named points 'push_door19' or 'push_door18' for PUSH_PUZZLE on level: {}",
          levelName);
    }

    levelBlockingTilesCache.put(levelName, blockingTiles);
  }

  private boolean isRiddleSolvedAtBlockingTile(
      RiddleAnalysisSystem.RiddleType riddle, Point posToCheck) {
    return riddle.isSolved().test(posToCheck);
  }

  /** Inner class to consolidate global riddle statistics. */
  private static class RiddleStats {
    int attemptCount = 0;
    long solveStartTime = 0;
    Set<Integer> solvedBy = new HashSet<>();
  }

  public record RiddleType(DesignLabel designLabel, String name, Predicate<Point> isSolved) {
    static final RiddleAnalysisSystem.RiddleType ICE_PUZZLE =
        new RiddleAnalysisSystem.RiddleType(
            DesignLabel.ICE,
            "Ice Puzzle",
            (posToCheck) -> {
              Stream<Entity> entityOnLastBlockingTile = Game.entityAtPoint(posToCheck);
              return entityOnLastBlockingTile.noneMatch(e -> e.name().contains("snow_Wall"));
            });
    static final RiddleAnalysisSystem.RiddleType PUSH_PUZZLE =
        new RiddleAnalysisSystem.RiddleType(
            DesignLabel.TEMPLE,
            "Push Puzzle",
            (posToCheck) -> {
              DoorTile lastDoor = (DoorTile) Game.tileAt(posToCheck).orElseThrow();
              return lastDoor.isOpen();
            });

    private boolean isInRiddle(Entity player) {
      Point pos = EntityUtils.getPosition(player);
      Tile tile = Game.tileAt(pos).orElseThrow();
      return tile.designLabel() == this.designLabel;
    }
  }
}
