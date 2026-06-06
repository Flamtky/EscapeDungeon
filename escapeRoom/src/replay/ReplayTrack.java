package replay;

import contrib.entities.CharacterClass;
import java.util.List;

record ReplayTrack(String playerId, CharacterClass characterClass, List<ReplayEvent> events) {
  ReplayTrack {
    events = List.copyOf(events);
  }
}
