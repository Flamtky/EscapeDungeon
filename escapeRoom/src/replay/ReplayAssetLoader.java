package replay;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import contrib.entities.CharacterClass;
import core.utils.Direction;
import core.utils.JsonHandler;
import core.utils.Point;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

final class ReplayAssetLoader {
  private ReplayAssetLoader() {}

  static ReplayAsset load(String assetPath) {
    String json = readAsset(assetPath);
    return fromJson(json);
  }

  private static String readAsset(String assetPath) {
    FileHandle internalAsset = Gdx.files.internal(assetPath);
    if (internalAsset.exists()) {
      return internalAsset.readString();
    }

    Path sourceAsset = ReplayAssetRepository.sourceAssetPath(assetPath);
    if (Files.exists(sourceAsset)) {
      try {
        return Files.readString(sourceAsset);
      } catch (IOException e) {
        throw new IllegalArgumentException("Could not read replay asset: " + assetPath, e);
      }
    }
    return internalAsset.readString();
  }

  static ReplayAsset fromJson(String json) {
    Map<String, Object> root = JsonHandler.readJson(json);
    String schema = requiredString(root, "schema");
    if (!ReplayAsset.SUPPORTED_SCHEMA.equals(schema)) {
      throw new IllegalArgumentException("Unsupported replay schema: " + schema);
    }

    Map<String, Object> source = requiredMap(root, "source");
    String level = requiredString(root, "level");
    long durationMs = requiredLong(root, "durationMs");
    List<ReplayTrack> tracks = parseTracks(requiredList(root, "players"));
    if (tracks.isEmpty()) {
      throw new IllegalArgumentException("Replay asset must contain at least one player track.");
    }
    return new ReplayAsset(schema, source, level, durationMs, tracks);
  }

  private static List<ReplayTrack> parseTracks(List<Object> playerValues) {
    List<ReplayTrack> tracks = new ArrayList<>();
    for (Object playerValue : playerValues) {
      if (!(playerValue instanceof Map<?, ?> playerMapRaw)) {
        throw new IllegalArgumentException("Replay player entry must be an object.");
      }
      Map<String, Object> playerMap = stringObjectMap(playerMapRaw, "player");
      String playerId = requiredString(playerMap, "playerId");
      CharacterClass characterClass =
          CharacterClass.valueOf(requiredString(playerMap, "characterClass"));
      List<ReplayEvent> events = parseEvents(requiredList(playerMap, "events"));
      if (events.isEmpty()) {
        throw new IllegalArgumentException("Replay player has no events: " + playerId);
      }
      tracks.add(new ReplayTrack(playerId, characterClass, events));
    }
    return tracks;
  }

  private static List<ReplayEvent> parseEvents(List<Object> eventValues) {
    List<ReplayEvent> events = new ArrayList<>();
    long previousTime = -1;
    for (Object eventValue : eventValues) {
      if (!(eventValue instanceof Map<?, ?> eventMapRaw)) {
        throw new IllegalArgumentException("Replay event entry must be an object.");
      }
      Map<String, Object> eventMap = stringObjectMap(eventMapRaw, "event");
      long t = requiredLong(eventMap, "t");
      if (t < previousTime) {
        throw new IllegalArgumentException("Replay events must be sorted by relative time.");
      }
      previousTime = t;
      float x = requiredFloat(eventMap, "x");
      float y = requiredFloat(eventMap, "y");
      Direction direction = Direction.fromString(requiredString(eventMap, "dir"));
      events.add(
          new ReplayEvent(t, new Point(x, y), direction, optionalFloat(eventMap, "stamina")));
    }
    return events;
  }

  private static String requiredString(Map<String, Object> map, String key) {
    Object value = map.get(key);
    if (value instanceof String stringValue && !stringValue.isBlank()) {
      return stringValue;
    }
    throw new IllegalArgumentException("Replay field must be a non-empty string: " + key);
  }

  private static long requiredLong(Map<String, Object> map, String key) {
    Object value = map.get(key);
    if (value instanceof Number number) {
      return number.longValue();
    }
    throw new IllegalArgumentException("Replay field must be numeric: " + key);
  }

  private static float requiredFloat(Map<String, Object> map, String key) {
    Object value = map.get(key);
    if (value instanceof Number number) {
      return number.floatValue();
    }
    throw new IllegalArgumentException("Replay field must be numeric: " + key);
  }

  private static Optional<Float> optionalFloat(Map<String, Object> map, String key) {
    Object value = map.get(key);
    if (value == null) {
      return Optional.empty();
    }
    if (value instanceof Number number) {
      return Optional.of(number.floatValue());
    }
    throw new IllegalArgumentException("Replay field must be numeric: " + key);
  }

  private static Map<String, Object> requiredMap(Map<String, Object> map, String key) {
    Object value = map.get(key);
    if (value instanceof Map<?, ?> rawMap) {
      return stringObjectMap(rawMap, key);
    }
    throw new IllegalArgumentException("Replay field must be an object: " + key);
  }

  private static List<Object> requiredList(Map<String, Object> map, String key) {
    Object value = map.get(key);
    if (value instanceof List<?> list) {
      return new ArrayList<>(list);
    }
    throw new IllegalArgumentException("Replay field must be a list: " + key);
  }

  private static Map<String, Object> stringObjectMap(Map<?, ?> rawMap, String label) {
    for (Object key : rawMap.keySet()) {
      if (!(key instanceof String)) {
        throw new IllegalArgumentException("Replay " + label + " object has a non-string key.");
      }
    }

    @SuppressWarnings("unchecked")
    Map<String, Object> typedMap = (Map<String, Object>) rawMap;
    return typedMap;
  }
}
