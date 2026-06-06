package contrib.systems;

import contrib.components.AIComponent;
import core.Entity;
import core.Game;
import core.System;
import core.utils.components.MissingComponentException;

/**
 * Controls the AI.
 *
 * <p>Entities with the {@link AIComponent} will be processed by this system.
 */
public final class AISystem extends System {

  /** Create a new AISystem. */
  public AISystem() {
    super(AIComponent.class);
  }

  @Override
  public void execute() {
    filteredEntityStream(AIComponent.class).forEach(this::executeAI);
  }

  private void executeAI(Entity entity) {
    AIComponent ai =
        entity
            .fetch(AIComponent.class)
            .orElseThrow(() -> MissingComponentException.build(entity, AIComponent.class));

    Game.allPlayers()
        .anyMatch(
            player -> {
              if (ai.shouldFight().apply(entity, player)) {
                ai.fightBehavior().accept(entity, player);
                return true;
              } else {
                ai.idleBehavior().accept(entity);
                return false;
              }
            });
  }
}
