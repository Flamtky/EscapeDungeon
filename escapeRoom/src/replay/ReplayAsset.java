package replay;

import java.util.List;
import java.util.Map;

record ReplayAsset(
    String schema,
    Map<String, Object> source,
    String level,
    long durationMs,
    List<ReplayTrack> tracks) {
  static final String SUPPORTED_SCHEMA = "dungeon.replay.v1";

  ReplayAsset {
    source = Map.copyOf(source);
    tracks = List.copyOf(tracks);
  }
}
