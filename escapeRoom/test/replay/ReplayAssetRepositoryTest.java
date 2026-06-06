package replay;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import org.junit.jupiter.api.Test;

class ReplayAssetRepositoryTest {
  @Test
  void filtersAndSortsReplayJsonAssets() {
    List<String> assets =
        ReplayAssetRepository.replayAssetsFromLines(
            "replays",
            new String[] {
              "sounds/background.wav",
              "replays/zeta.json",
              "replays/readme.txt",
              "replays/alpha.json",
              "nested/replays/ignored.json",
              "replays/alpha.json"
            });

    assertEquals(List.of("replays/alpha.json", "replays/zeta.json"), assets);
  }
}
