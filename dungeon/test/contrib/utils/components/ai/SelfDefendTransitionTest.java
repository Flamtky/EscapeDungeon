package contrib.utils.components.ai;

import static org.junit.jupiter.api.Assertions.*;

import contrib.components.HealthComponent;
import contrib.utils.components.ai.transition.SelfDefendTransition;
import core.Entity;
import core.utils.components.MissingComponentException;
import org.junit.jupiter.api.Test;

class SelfDefendTransitionTest {

  private final Entity player = new Entity();

  /**
   * Tests if the isInFight method returns false when the current HealthPoints of an entity are
   * equal to its max HealthPoints.
   */
  @Test
  public void isInFightModeHealtpointsAreMax() {
    Entity entity = new Entity();
    HealthComponent hc = new HealthComponent();
    entity.add(hc);
    hc.maximalHealthpoints(10);
    hc.currentHealthpoints(10);
    var defend = new SelfDefendTransition();

    assertFalse(defend.apply(entity, player));
  }

  /**
   * Tests if the isInFight method returns true when the current HealthPoints of an entity are lower
   * than its max HealthPoints.
   */
  @Test
  public void isInFightModeHealthpointsAreLowerThenMax() {
    Entity entity = new Entity();
    HealthComponent hc = new HealthComponent();
    entity.add(hc);
    hc.maximalHealthpoints(10);
    hc.currentHealthpoints(10);
    var defend = new SelfDefendTransition();
    assertFalse(defend.apply(entity, player));
    hc.currentHealthpoints(9);
    assertTrue(defend.apply(entity, player));
  }

  /**
   * Checks the thrown Exception when the required HealthComponent is missing in the provided
   * Entity.
   */
  @Test
  public void isInFightModeHealthComponentMissing() {
    Entity entity = new Entity();
    var defend = new SelfDefendTransition();
    MissingComponentException exception =
        assertThrows(MissingComponentException.class, () -> defend.apply(entity, player));
    assertTrue(exception.getMessage().contains(HealthComponent.class.getName()));
  }
}
