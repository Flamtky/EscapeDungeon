package contrib.utils.components.ai.transition;

import contrib.components.HealthComponent;
import core.Entity;
import core.utils.components.MissingComponentException;
import java.util.function.BiFunction;

/**
 * Implementation of a transition between idle and fight mode. Switches to fight mode when the
 * entity was attacked by another entity.
 */
public final class SelfDefendTransition implements BiFunction<Entity, Entity, Boolean> {

  @Override
  public Boolean apply(final Entity entity, final Entity player) {
    return entity
        .fetch(HealthComponent.class)
        .map(hc -> hc.currentHealthpoints() < hc.maximalHealthpoints())
        .orElseThrow(() -> MissingComponentException.build(entity, HealthComponent.class));
  }
}
