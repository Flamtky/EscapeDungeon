package analytics;

import contrib.entities.CharacterClass;
import core.components.AnalyticsComponent;
import core.network.server.ClientState;
import core.utils.logging.DungeonLogger;

import java.sql.*;
import java.util.Map;
import java.util.UUID;

/**
 * Service API for logging Educational Escape Room (EER) data to the evaluation database. Supports
 * xAPI statements, user profiles, and survey responses.
 */
public class DungeonAnalyticsAPI {

  private static final boolean ENABLED = true;
  private static final DungeonLogger LOGGER = DungeonLogger.getLogger(DungeonAnalyticsAPI.class);

  private static String stateToId(ClientState state) {
    return state.username() + "#" + state.clientId();
  }

  /**
   * Registers a new player or updates an existing player's Hexad profile.
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

    final Map<CharacterClass, String> classToType = Map.of(
        CharacterClass.ROGUE, "dummy1",
        CharacterClass.APPRENTICE, "dummy2"
    );

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
   * Initializes a new gameplay session in the database.
   *
   * @param playerState The client's state containing player identifiers.
   * @param configJson JSON configuration for this session (e.g., difficulty, adaptivity).
   * @return The generated {@link UUID} of the session. Null if the operation fails or analytics is disabled.
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
          return (UUID) rs.getObject("session_id");
        }
      }
    } catch (SQLException e) {
      LOGGER.error("Failed to start session: " + e.getMessage(), e);
    }
    return null;
  }

  /**
   * Logs an xAPI statement documenting an event during the escape room.
   *
   * @param ac The AnalyticsComponent containing session and player info.
   * @param verb The action performed (e.g., "solved", "attempted").
   * @param objectId The target of the action (e.g., "puzzle_01").
   * @param resultJsonMap JSON containing metrics, skill tags, and pyramid data.
   */
  public static void logXApiStatement(
    AnalyticsComponent ac, Verb verb, String objectId, Map<String, Object> resultJsonMap) {
    if (!ENABLED) {
      return;
    }
    var sql =
        """
            INSERT INTO xapi_statements (session_id, player_id, verb, object_id, result)
            VALUES (?, ?, ?, ?, ?::jsonb)
            """;

    try (Connection conn = DatabaseConnector.getConnection();
        PreparedStatement pstmt = conn.prepareStatement(sql)) {
      pstmt.setObject(1, ac.sessionId());
      pstmt.setString(2, stateToId(ac.state()));
      pstmt.setString(3, verb.toString());
      pstmt.setString(4, objectId);
      pstmt.setString(5, mapToJson(resultJsonMap));
      pstmt.executeUpdate();
    } catch (SQLException e) {
      LOGGER.error("Failed to log xAPI statement: " + e.getMessage(), e);
    }
  }

  private static String mapToJson(Map<String, Object> map) {
    StringBuilder jsonBuilder = new StringBuilder();
    jsonBuilder.append("{");
    int size = map.size();
    int index = 0;
    for (Map.Entry<String, Object> entry : map.entrySet()) {
      jsonBuilder.append("\"").append(entry.getKey()).append("\":");
      Object value = entry.getValue();
      if (value instanceof String) {
        jsonBuilder.append("\"").append(value).append("\"");
      } else {
        jsonBuilder.append(value);
      }
      if (index < size - 1) {
        jsonBuilder.append(",");
      }
      index++;
    }
    jsonBuilder.append("}");
    return jsonBuilder.toString();
  }

  /**
   * Saves a survey response (Pre-Game or Post-Game) to the database.
   *
   * @param sessionId The UUID of the session linked to the survey.
   * @param type The survey type ("pre_game" or "post_game").
   * @param responsesJson JSON containing the student's answers.
   */
  public static void submitSurvey(UUID sessionId, SurveyType type, String responsesJson){
    if (!ENABLED) {
      return;
    }
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
  }

  /**
   * Updates the end timestamp for a session.
   *
   * @param sessionId The UUID of the session to terminate.
   */
  public static void endSession(UUID sessionId) {
    if (!ENABLED) {
      return;
    }
    var sql = "UPDATE sessions SET end_time = CURRENT_TIMESTAMP WHERE session_id = ?";

    try (Connection conn = DatabaseConnector.getConnection();
        PreparedStatement pstmt = conn.prepareStatement(sql)) {
      pstmt.setObject(1, sessionId);
      pstmt.executeUpdate();
    } catch (SQLException e) {
      LOGGER.error("Failed to end session: " + e.getMessage(), e);
    }
  }

  /**
   * Enum representing common verbs used in xAPI statements for the escape room.
   */
  public enum Verb {
    MOVED("moved"),
    INTERACTED("interacted"),
    CHANGED_SKILL("changed_skill"),
    OPENED("opened"),
    CRAFTED("crafted"),
    SOLVED("solved"),
    ATTEMPTED("attempted"),
    HINT_REQUESTED("hint_requested"),
    CAST_SKILL("cast_skill"),
    DROPPED("dropped"),
    MOVED_ITEM("moved_item"),
    USED_ITEM("used_item");

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
