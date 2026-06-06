package replay;

import java.util.function.LongSupplier;

final class ReplayPlaybackController {
  private final LongSupplier nanoTimeSource;
  private long durationMs;
  private double speed;
  private boolean loop;
  private boolean playing;
  private boolean finished;
  private long currentTimeMs;
  private long lastUpdateNanos;

  ReplayPlaybackController(long durationMs, double speed, boolean loop) {
    this(durationMs, speed, loop, java.lang.System::nanoTime);
  }

  ReplayPlaybackController(
      long durationMs, double speed, boolean loop, LongSupplier nanoTimeSource) {
    if (durationMs < 0) {
      throw new IllegalArgumentException("durationMs must not be negative.");
    }
    if (speed <= 0) {
      throw new IllegalArgumentException("speed must be greater than 0.");
    }
    this.durationMs = durationMs;
    this.speed = speed;
    this.loop = loop;
    this.nanoTimeSource = nanoTimeSource;
    this.playing = true;
    this.lastUpdateNanos = nanoTimeSource.getAsLong();
  }

  void durationMs(long durationMs) {
    if (durationMs < 0) {
      throw new IllegalArgumentException("durationMs must not be negative.");
    }
    this.durationMs = durationMs;
    seekTo(currentTimeMs);
  }

  void update() {
    long now = nanoTimeSource.getAsLong();
    if (!playing) {
      lastUpdateNanos = now;
      return;
    }

    long deltaNanos = Math.max(0, now - lastUpdateNanos);
    lastUpdateNanos = now;
    long deltaMs = Math.round((deltaNanos / 1_000_000.0) * speed);
    if (deltaMs == 0) {
      return;
    }
    currentTimeMs += deltaMs;
    normalizeCurrentTime();
  }

  void play() {
    if (!loop && currentTimeMs >= durationMs) {
      currentTimeMs = 0;
      finished = false;
    }
    playing = true;
    lastUpdateNanos = nanoTimeSource.getAsLong();
  }

  void pause() {
    playing = false;
    lastUpdateNanos = nanoTimeSource.getAsLong();
  }

  void togglePlayback() {
    if (playing) {
      pause();
    } else {
      play();
    }
  }

  void seekTo(long elapsedMs) {
    currentTimeMs = Math.max(0, Math.min(elapsedMs, durationMs));
    finished = !loop && currentTimeMs >= durationMs;
    if (finished) {
      playing = false;
    }
    lastUpdateNanos = nanoTimeSource.getAsLong();
  }

  void skipBy(long deltaMs) {
    seekTo(currentTimeMs + deltaMs);
  }

  void speed(double speed) {
    if (speed <= 0) {
      throw new IllegalArgumentException("speed must be greater than 0.");
    }
    update();
    this.speed = speed;
  }

  double speed() {
    return speed;
  }

  void loop(boolean loop) {
    this.loop = loop;
    normalizeCurrentTime();
  }

  boolean playing() {
    return playing;
  }

  boolean finished() {
    return finished;
  }

  long currentTimeMs() {
    return currentTimeMs;
  }

  long durationMs() {
    return durationMs;
  }

  private void normalizeCurrentTime() {
    if (durationMs == 0) {
      currentTimeMs = 0;
      finished = !loop;
      playing = loop && playing;
      return;
    }

    if (loop) {
      currentTimeMs = currentTimeMs % durationMs;
      finished = false;
      return;
    }

    if (currentTimeMs >= durationMs) {
      currentTimeMs = durationMs;
      finished = true;
      playing = false;
    } else {
      finished = false;
    }
  }
}
