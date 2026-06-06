package replay;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class ReplayAssetLoaderTest {
  @Test
  void parsesReplayJson() {
    ReplayAsset asset = ReplayAssetLoader.fromJson(validReplayJson());

    assertEquals("dungeon.replay.v1", asset.schema());
    assertEquals("MADungeonRoom", asset.level());
    assertEquals(1_000, asset.durationMs());
    assertEquals(2, asset.tracks().size());
    assertEquals("Feder385955#APPRENTICE", asset.tracks().get(0).playerId());
    assertEquals(2, asset.tracks().get(0).events().size());
    assertEquals(98.75f, asset.tracks().get(0).events().get(0).stamina().orElseThrow());
  }

  @Test
  void rejectsUnknownSchema() {
    String json = validReplayJson().replace("dungeon.replay.v1", "dungeon.replay.v2");

    assertThrows(IllegalArgumentException.class, () -> ReplayAssetLoader.fromJson(json));
  }

  static String validReplayJson() {
    return """
        {
          "schema": "dungeon.replay.v1",
          "source": {
            "sessionId": "38b4ce2d-c93b-4ce8-b149-f504cdfb55a8",
            "startTime": "2026-01-31T11:52:05.000000",
            "endTime": "2026-01-31T11:52:06.000000",
            "exportedAt": "2026-05-18T12:00:00"
          },
          "level": "MADungeonRoom",
          "durationMs": 1000,
          "players": [
            {
              "playerId": "Feder385955#APPRENTICE",
              "characterClass": "APPRENTICE",
              "events": [
                {"t": 0, "x": 32, "y": 26, "dir": "DOWN", "stamina": 98.75},
                {"t": 200, "x": 32, "y": 28, "dir": "DOWN"}
              ]
            },
            {
              "playerId": "Frost643667#ROGUE",
              "characterClass": "ROGUE",
              "events": [
                {"t": 100, "x": 30, "y": 26, "dir": "RIGHT"}
              ]
            }
          ]
        }
        """;
  }
}
