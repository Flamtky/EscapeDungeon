package replay;

import static org.junit.jupiter.api.Assertions.assertEquals;

import contrib.entities.CharacterClass;
import core.utils.Point;
import org.junit.jupiter.api.Test;

class ReplayPositionOffsetTest {
  @Test
  void appliesClassAwareReplayOffset() {
    assertEquals(
        new Point(2.7f, 3.48f),
        ReplayPositionOffset.apply(new Point(2.2f, 2.98f), CharacterClass.ROGUE));
    assertEquals(
        new Point(2.7f, 3.73f),
        ReplayPositionOffset.apply(new Point(2.2f, 2.98f), CharacterClass.APPRENTICE));
  }
}
