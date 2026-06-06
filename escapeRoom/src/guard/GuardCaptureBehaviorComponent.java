package guard;

import core.Component;
import core.Entity;
import core.utils.Point;
import java.util.Optional;
import java.util.function.BiConsumer;

final class GuardCaptureBehaviorComponent implements Component {
  private final String failureTitle;
  private final String failureText;
  private final Point returnPoint;
  private final long returnTimeoutMs;
  private final BiConsumer<Entity, Entity> onCaptureFinished;

  GuardCaptureBehaviorComponent(
      String failureTitle,
      String failureText,
      Point returnPoint,
      long returnTimeoutMs,
      BiConsumer<Entity, Entity> onCaptureFinished) {
    this.failureTitle = failureTitle;
    this.failureText = failureText;
    this.returnPoint = returnPoint;
    this.returnTimeoutMs = returnTimeoutMs;
    this.onCaptureFinished = onCaptureFinished;
  }

  String failureTitle() {
    return failureTitle;
  }

  String failureText() {
    return failureText;
  }

  Optional<Point> returnPoint() {
    return Optional.ofNullable(returnPoint);
  }

  long returnTimeoutMs() {
    return returnTimeoutMs;
  }

  Optional<BiConsumer<Entity, Entity>> onCaptureFinished() {
    return Optional.ofNullable(onCaptureFinished);
  }

  boolean hasFailureDialog() {
    return failureTitle != null && failureText != null;
  }
}
