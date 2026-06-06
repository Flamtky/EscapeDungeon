package replay;

import contrib.components.StaminaComponent;
import contrib.systems.PositionSync;
import core.Entity;
import core.System;
import core.components.DrawComponent;
import core.components.PositionComponent;
import core.systems.VelocitySystem;
import java.util.Map;

final class ReplayGhostSystem extends System {
  private final ReplayTimeline timeline;
  private final Map<String, Entity> ghosts;
  private final ReplayPlaybackController controller;

  ReplayGhostSystem(
      ReplayTimeline timeline, Map<String, Entity> ghosts, ReplayPlaybackController controller) {
    super(AuthoritativeSide.CLIENT);
    this.timeline = timeline;
    this.ghosts = Map.copyOf(ghosts);
    this.controller = controller;
  }

  @Override
  public void execute() {
    controller.update();
    applyCurrentFrame();
  }

  void applyCurrentFrame() {
    timeline.frameAt(controller.currentTimeMs()).forEach(this::applyFrame);
  }

  boolean finished() {
    return controller.finished();
  }

  private void applyFrame(String playerId, ReplayFrame frame) {
    Entity ghost = ghosts.get(playerId);
    if (ghost == null) {
      return;
    }

    PositionComponent positionComponent = ghost.fetch(PositionComponent.class).orElseThrow();
    ReplayGhostComponent ghostComponent = ghost.fetch(ReplayGhostComponent.class).orElseThrow();
    positionComponent.position(
        ReplayPositionOffset.apply(frame.position(), ghostComponent.characterClass()));
    positionComponent.viewDirection(frame.direction());
    frame
        .stamina()
        .ifPresent(
            stamina ->
                ghost
                    .fetch(StaminaComponent.class)
                    .ifPresent(staminaComponent -> staminaComponent.currentAmount(stamina)));
    PositionSync.syncPosition(ghost);

    ghost
        .fetch(DrawComponent.class)
        .ifPresent(
            drawComponent -> {
              String signal =
                  frame.moving() ? VelocitySystem.MOVE_SIGNAL : VelocitySystem.IDLE_SIGNAL;
              drawComponent.sendSignal(signal, frame.direction());
            });
  }
}
