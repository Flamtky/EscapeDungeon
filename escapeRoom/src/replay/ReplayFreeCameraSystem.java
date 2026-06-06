package replay;

import com.badlogic.gdx.Input;
import contrib.utils.components.Debugger;
import core.Entity;
import core.Game;
import core.System;
import core.components.CameraComponent;
import core.components.PositionComponent;
import core.configuration.KeyboardConfig;
import core.systems.InputManager;
import core.utils.Vector2;

/** Moves an invisible replay camera entity independently from player input. */
public final class ReplayFreeCameraSystem extends System {
  private static final float BASE_SPEED = 9f;
  private static final float SHIFT_SPEED_MULTIPLIER = 2.5f;
  private static final int ACCELERATION_BOOST_FRAMES = 10;
  private static final float ACCELERATION_BOOST = 1.75f;
  private static final float SCROLL_ZOOM_STEP = 0.08f;
  private int accelerationBoostFrames;
  private boolean wasMoving;

  /**
   * Creates a replay free-camera system.
   *
   * @param cameraEntity camera entity to move
   */
  public ReplayFreeCameraSystem(Entity cameraEntity) {
    super(AuthoritativeSide.CLIENT, CameraComponent.class, PositionComponent.class);
    if (!cameraEntity.isPresent(CameraComponent.class)
        || !cameraEntity.isPresent(PositionComponent.class)) {
      throw new IllegalArgumentException(
          "cameraEntity must have CameraComponent and PositionComponent.");
    }
  }

  @Override
  public void execute() {
    moveCameraEntity();
    zoomFromScroll();
  }

  @Override
  public void stop() {
    super.stop();
  }

  private void moveCameraEntity() {
    float delta = 1f / Game.tickRate();
    float speed = BASE_SPEED * shiftMultiplier() * core.systems.CameraSystem.camera().zoom;
    Vector2 direction = movementVector();
    if (direction.lengthSquared() == 0) {
      accelerationBoostFrames = 0;
      wasMoving = false;
      return;
    }

    if (!wasMoving) {
      accelerationBoostFrames = ACCELERATION_BOOST_FRAMES;
      wasMoving = true;
    }
    float boostProgress = accelerationBoostFrames / (float) ACCELERATION_BOOST_FRAMES;
    float boost = 1 + ((ACCELERATION_BOOST - 1) * boostProgress);
    accelerationBoostFrames = Math.max(0, accelerationBoostFrames - 1);

    Vector2 movement = direction.scale(speed * boost * delta);
    filteredEntityStream(CameraComponent.class, PositionComponent.class)
        .forEach(
            entity ->
                entity
                    .fetch(PositionComponent.class)
                    .ifPresent(
                        position -> position.position(position.position().translate(movement))));
  }

  private float shiftMultiplier() {
    if (InputManager.isKeyPressed(Input.Keys.SHIFT_LEFT)
        || InputManager.isKeyPressed(Input.Keys.SHIFT_RIGHT)) {
      return SHIFT_SPEED_MULTIPLIER;
    }
    return 1f;
  }

  private Vector2 movementVector() {
    float x = 0;
    float y = 0;
    if (InputManager.isKeyPressed(KeyboardConfig.MOVEMENT_LEFT.value())) {
      x -= 1;
    }
    if (InputManager.isKeyPressed(KeyboardConfig.MOVEMENT_RIGHT.value())) {
      x += 1;
    }
    if (InputManager.isKeyPressed(KeyboardConfig.MOVEMENT_UP.value())) {
      y += 1;
    }
    if (InputManager.isKeyPressed(KeyboardConfig.MOVEMENT_DOWN.value())) {
      y -= 1;
    }
    Vector2 movement = Vector2.of(x, y);
    return movement.lengthSquared() > 1 ? movement.normalize() : movement;
  }

  private void zoomFromScroll() {
    if (Game.stage().map(stage -> stage.getScrollFocus() != null).orElse(false)) {
      InputManager.consumeScrollAmountY();
      return;
    }

    float scrollAmount = InputManager.consumeScrollAmountY();
    if (scrollAmount != 0) {
      Debugger.ZOOM_CAMERA(scrollAmount * SCROLL_ZOOM_STEP);
    }
  }
}
