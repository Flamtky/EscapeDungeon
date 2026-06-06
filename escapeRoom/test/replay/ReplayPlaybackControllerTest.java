package replay;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.function.LongSupplier;
import org.junit.jupiter.api.Test;

class ReplayPlaybackControllerTest {
  @Test
  void playAdvancesTime() {
    TestClock clock = new TestClock();
    ReplayPlaybackController controller = new ReplayPlaybackController(1_000, 1.0, false, clock);

    clock.advanceMs(250);
    controller.update();

    assertEquals(250, controller.currentTimeMs());
    assertTrue(controller.playing());
  }

  @Test
  void pauseHoldsTime() {
    TestClock clock = new TestClock();
    ReplayPlaybackController controller = new ReplayPlaybackController(1_000, 1.0, false, clock);

    controller.pause();
    clock.advanceMs(250);
    controller.update();

    assertEquals(0, controller.currentTimeMs());
    assertFalse(controller.playing());
  }

  @Test
  void seekClampsToDuration() {
    TestClock clock = new TestClock();
    ReplayPlaybackController controller = new ReplayPlaybackController(1_000, 1.0, false, clock);

    controller.seekTo(-500);
    assertEquals(0, controller.currentTimeMs());

    controller.seekTo(1_500);
    assertEquals(1_000, controller.currentTimeMs());
    assertTrue(controller.finished());
  }

  @Test
  void skipMovesRelativeToCurrentTime() {
    TestClock clock = new TestClock();
    ReplayPlaybackController controller = new ReplayPlaybackController(20_000, 1.0, false, clock);

    controller.seekTo(12_000);
    controller.skipBy(-10_000);
    assertEquals(2_000, controller.currentTimeMs());

    controller.skipBy(10_000);
    assertEquals(12_000, controller.currentTimeMs());
  }

  @Test
  void speedMultiplierAffectsAdvancement() {
    TestClock clock = new TestClock();
    ReplayPlaybackController controller = new ReplayPlaybackController(1_000, 2.0, false, clock);

    clock.advanceMs(100);
    controller.update();

    assertEquals(200, controller.currentTimeMs());
  }

  @Test
  void nonLoopingPlaybackStopsAtDuration() {
    TestClock clock = new TestClock();
    ReplayPlaybackController controller = new ReplayPlaybackController(1_000, 1.0, false, clock);

    clock.advanceMs(1_500);
    controller.update();

    assertEquals(1_000, controller.currentTimeMs());
    assertFalse(controller.playing());
    assertTrue(controller.finished());
  }

  @Test
  void loopingPlaybackWrapsAtDuration() {
    TestClock clock = new TestClock();
    ReplayPlaybackController controller = new ReplayPlaybackController(1_000, 1.0, true, clock);

    clock.advanceMs(1_250);
    controller.update();

    assertEquals(250, controller.currentTimeMs());
    assertTrue(controller.playing());
    assertFalse(controller.finished());
  }

  private static final class TestClock implements LongSupplier {
    private long nanos;

    void advanceMs(long millis) {
      nanos += millis * 1_000_000;
    }

    @Override
    public long getAsLong() {
      return nanos;
    }
  }
}
