package contrib.utils.components.ai.fight;

import contrib.utils.components.ai.AIUtils;
import core.Entity;
import core.level.utils.LevelUtils;
import java.util.function.BiConsumer;

/**
 * AI behavior for entities that chase and attack the player.
 *
 * <p>The entity will attempt to move towards the player and attack.
 */
public class AIChaseBehaviour implements BiConsumer<Entity, Entity> {

  /** Creates a new AIChaseBehaviour with the given chase range. */
  public AIChaseBehaviour() {}

  @Override
  public void accept(final Entity entity, final Entity player) {
    AIUtils.followPath(entity, LevelUtils.calculatePath(entity, player));
  }
}
