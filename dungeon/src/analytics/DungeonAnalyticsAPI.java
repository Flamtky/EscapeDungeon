package analytics;

import contrib.components.AttachmentComponent;
import contrib.entities.CharacterClass;
import core.Game;
import core.components.AnalyticsComponent;
import core.network.server.ClientState;
import core.utils.JsonHandler;
import core.utils.logging.DungeonLogger;
import java.sql.*;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Service API for logging Educational Escape Room (EER) data to the evaluation database. Supports
 * xAPI statements, user profiles, and survey responses.
 *
 * <p>All database operations are non-blocking except for {@link #startSession(ClientState,
 * String)}, which blocks to return the session UUID synchronously. Other methods execute
 * asynchronously.
 */
public class DungeonAnalyticsAPI {

  private static final boolean ENABLED = true;
  private static final DungeonLogger LOGGER = DungeonLogger.getLogger(DungeonAnalyticsAPI.class);
  private static final ExecutorService EXECUTOR = Executors.newFixedThreadPool(4);

  private static String stateToId(ClientState state) {
    return state.username() + "#" + state.clientId();
  }

  /**
   * Registers a new player or updates an existing player's Hexad profile. This operation is
   * synchronous and blocks until the operation is complete.
   *
   * @param playerState The client's state containing player identifiers.
   * @param characterClass The player's Hexad character class.
   */
  public static void upsertPlayer(ClientState playerState, CharacterClass characterClass) {
    if (!ENABLED) {
      return;
    }
    var sql =
        """
        INSERT INTO players (player_id, hexad_primary_type, hexad_scores)
        VALUES (?, ?, ?::jsonb)
        ON CONFLICT (player_id) DO UPDATE
        SET hexad_primary_type = EXCLUDED.hexad_primary_type,
            hexad_scores = EXCLUDED.hexad_scores;
        """;

    final Map<CharacterClass, String> classToType =
        Map.of(
            CharacterClass.ROGUE, "Achiever",
            CharacterClass.APPRENTICE, "Socialiser");

    try (Connection conn = DatabaseConnector.getConnection();
        PreparedStatement pstmt = conn.prepareStatement(sql)) {
      pstmt.setString(1, stateToId(playerState));
      pstmt.setString(2, classToType.getOrDefault(characterClass, "unknown"));
      pstmt.setString(3, "{}"); // TODO: Replace with actual Hexad scores JSON
      pstmt.executeUpdate();
    } catch (SQLException e) {
      LOGGER.error("Failed to upsert player profile: " + e.getMessage(), e);
    }
  }

  /**
   * Initializes a new gameplay session in the database. This operation is synchronous and blocks
   * until the session UUID is retrieved.
   *
   * @param playerState The client's state containing player identifiers.
   * @param configJson JSON configuration for this session (e.g., difficulty, adaptivity).
   * @return The generated {@link UUID} of the session. Null if the operation fails or analytics is
   *     disabled.
   */
  public static UUID startSession(ClientState playerState, String configJson) {
    if (!ENABLED) {
      return null;
    }
    var sql =
        "INSERT INTO sessions (player_id, session_config) VALUES (?, ?::jsonb) RETURNING session_id";

    try (Connection conn = DatabaseConnector.getConnection();
        PreparedStatement pstmt = conn.prepareStatement(sql)) {
      pstmt.setString(1, stateToId(playerState));
      pstmt.setString(2, configJson);

      try (ResultSet rs = pstmt.executeQuery()) {
        if (rs.next()) {
          var result = (UUID) rs.getObject("session_id");
          if (result == null) {
            throw new SQLException("Failed to retrieve generated session_id");
          }
          return result;
        }
      }
    } catch (SQLException e) {
      LOGGER.error("Failed to start session: " + e.getMessage(), e);
    }
    return null;
  }

  /**
   * Logs an xAPI statement documenting an event during the escape room without any context. This
   * operation is executed asynchronously and does not block the caller.
   *
   * @param ac The AnalyticsComponent containing session and player info.
   * @param verb The action performed (e.g., "solved", "attempted").
   * @param objectId The target of the action (e.g., "puzzle_01").
   * @param resultJsonMap JSON containing metrics, skill tags, and pyramid data.
   */
  public static void logXApiStatement(
      AnalyticsComponent ac, Verb verb, String objectId, Map<String, Object> resultJsonMap) {
    logXApiStatement(ac, verb, objectId, resultJsonMap, null);
  }

  /**
   * Logs an xAPI statement documenting an event during the escape room. This operation is executed
   * asynchronously and does not block the caller.
   *
   * @param ac The AnalyticsComponent containing session and player info.
   * @param verb The action performed (e.g., "solved", "attempted").
   * @param objectId The target of the action (e.g., "puzzle_01").
   * @param resultJsonMap JSON containing metrics, skill tags, and pyramid data.
   * @param context JSON containing additional context for the statement. Can be null.
   */
  public static void logXApiStatement(
      AnalyticsComponent ac,
      Verb verb,
      String objectId,
      Map<String, Object> resultJsonMap,
      Map<String, Object> context) {
    if (!ENABLED) {
      return;
    }

    resultJsonMap = resultJsonMap == null ? Map.of() : resultJsonMap;

    context = context == null ? new HashMap<>() : new HashMap<>(context);

    var riddleSystem = Game.systems().get(RiddleAnalysisSystem.class);
    if (riddleSystem instanceof RiddleAnalysisSystem riddleAnalysisSystem) {
      var currentRiddle =
          riddleAnalysisSystem.currentRiddle(ac.state().playerEntity().orElseThrow());
      if (currentRiddle != null) {
        context.put("riddle", currentRiddle.name());
      }
    }

    if (ac.state()
        .playerEntity()
        .map(e -> e.fetch(AttachmentComponent.class).isPresent())
        .orElse(false)) {
      context.put("captured", true);
    }

    final Map<String, Object> finalResultJsonMap = resultJsonMap;
    final Map<String, Object> finalContext = context;

    EXECUTOR.submit(
        () -> {
          var sql =
              """
              INSERT INTO xapi_statements (session_id, player_id, verb, object_id, result, context)
              VALUES (?, ?, ?, ?, ?::jsonb, ?::jsonb)
              """;

          try (Connection conn = DatabaseConnector.getConnection();
              PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setObject(1, ac.sessionId());
            pstmt.setString(2, stateToId(ac.state()));
            pstmt.setString(3, verb.toString());
            pstmt.setString(4, objectId);
            pstmt.setString(5, JsonHandler.writeJson(finalResultJsonMap, false));
            pstmt.setString(6, JsonHandler.writeJson(finalContext, false));
            pstmt.executeUpdate();
          } catch (SQLException e) {
            LOGGER.error("Failed to log xAPI statement: " + e.getMessage(), e);
          }
        });
  }

  /**
   * Saves a survey response (Pre-Game or Post-Game) to the database. This operation is executed
   * asynchronously and does not block the caller.
   *
   * @param sessionId The UUID of the session linked to the survey.
   * @param type The survey type ("pre_game" or "post_game").
   * @param responsesJson JSON containing the student's answers.
   */
  public static void submitSurvey(UUID sessionId, SurveyType type, String responsesJson) {
    if (!ENABLED) {
      return;
    }
    EXECUTOR.submit(
        () -> {
          var sql =
              "INSERT INTO survey_responses (session_id, survey_type, responses) VALUES (?, ?, ?::jsonb)";

          try (Connection conn = DatabaseConnector.getConnection();
              PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setObject(1, sessionId);
            pstmt.setString(2, type.toString().toLowerCase());
            pstmt.setString(3, responsesJson);
            pstmt.executeUpdate();
          } catch (SQLException e) {
            LOGGER.error("Failed to submit survey: " + e.getMessage(), e);
          }
        });
  }

  /**
   * Updates the end timestamp for a session. This operation is executed asynchronously and does not
   * block the caller.
   *
   * @param sessionId The UUID of the session to terminate.
   */
  public static void endSession(UUID sessionId) {
    if (!ENABLED) {
      return;
    }
    EXECUTOR.submit(
        () -> {
          var sql = "UPDATE sessions SET end_time = CURRENT_TIMESTAMP WHERE session_id = ?";

          try (Connection conn = DatabaseConnector.getConnection();
              PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setObject(1, sessionId);
            pstmt.executeUpdate();
          } catch (SQLException e) {
            LOGGER.error("Failed to end session: " + e.getMessage(), e);
          }
        });
  }

  /** Enum representing common verbs used in xAPI statements for the escape room. */
  public enum Verb {
    MOVED("moved"),
    INTERACTED("interacted"),
    CHANGED_SKILL("changed_skill"),
    OPENED("opened"),
    CRAFTED("crafted"),
    SOLVED("solved"),
    TRIES("tries"),
    HINT_REQUESTED("hint_requested"),
    CAST_SKILL("cast_skill"),
    DROPPED("dropped"),
    MOVED_ITEM("moved_item"),
    USED_ITEM("used_item"),
    LEFT("left"),
    DETECTED("detected"),
    LOST_DETECTION("lost_detection"),
    CAPTURED("captured"),
    RELEASED("released");

    private final String verbString;

    Verb(String verbString) {
      this.verbString = verbString;
    }

    @Override
    public String toString() {
      return verbString;
    }
  }

  public enum SurveyType {
    PRE_GAME,
    POST_GAME
  }
}
