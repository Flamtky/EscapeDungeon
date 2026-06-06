package replay;

import contrib.components.StaminaComponent;
import contrib.entities.CharacterClass;
import core.Entity;
import core.components.DrawComponent;
import core.components.PositionComponent;
import core.systems.VelocitySystem;
import core.utils.Direction;
import core.utils.components.draw.DepthLayer;
import core.utils.components.draw.animation.Animation;
import core.utils.components.draw.state.DirectionalState;
import core.utils.components.draw.state.State;
import core.utils.components.draw.state.StateMachine;
import java.util.Arrays;
import java.util.Map;
import java.util.Optional;

final class ReplayGhostFactory {
  private ReplayGhostFactory() {}

  static Entity create(ReplayTrack track, int tintColor) {
    ReplayEvent firstEvent = track.events().getFirst();
    Entity ghost = Entity.createLocalEntity("replay_" + track.playerId());
    ghost.add(new ReplayGhostComponent(track.playerId(), track.characterClass(), tintColor));
    ghost.add(
        new PositionComponent(
            ReplayPositionOffset.apply(
                ReplaySourcePosition.snap(firstEvent.position()), track.characterClass()),
            firstEvent.direction()));
    initialStamina(track)
        .ifPresent(
            stamina ->
                ghost.add(new StaminaComponent(track.characterClass().stamina(), stamina, 0)));
    ghost.add(drawComponent(track.characterClass(), tintColor));
    return ghost;
  }

  private static Optional<Float> initialStamina(ReplayTrack track) {
    return track.events().stream().map(ReplayEvent::stamina).flatMap(Optional::stream).findFirst();
  }

  private static DrawComponent drawComponent(CharacterClass characterClass, int tintColor) {
    Map<String, Animation> animationMap =
        Animation.loadAnimationSpritesheet(characterClass.textures());
    State idle = new DirectionalState(StateMachine.IDLE_STATE, animationMap);
    State move = new DirectionalState(VelocitySystem.STATE_NAME, animationMap, "run");
    StateMachine stateMachine = new StateMachine(Arrays.asList(idle, move));
    stateMachine.addTransition(idle, VelocitySystem.MOVE_SIGNAL, move);
    stateMachine.addTransition(idle, VelocitySystem.IDLE_SIGNAL, idle);
    stateMachine.addTransition(move, VelocitySystem.MOVE_SIGNAL, move);
    stateMachine.addTransition(move, VelocitySystem.IDLE_SIGNAL, idle);

    DrawComponent drawComponent = new DrawComponent(stateMachine);
    drawComponent.depth(DepthLayer.Player.depth());
    drawComponent.tintColor(tintColor);
    drawComponent.sendSignal(VelocitySystem.IDLE_SIGNAL, Direction.DOWN);
    return drawComponent;
  }
}
