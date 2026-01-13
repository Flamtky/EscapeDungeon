package contrib.systems;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

import contrib.components.AIComponent;
import core.Entity;
import core.Game;
import core.components.PlayerComponent;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class AISystemTest {

  private int updateCounter;
  private AISystem system;
  private Entity entity;
  private Entity player;

  @BeforeEach
  void setup() {
    Game.removeAllEntities();
    Game.removeAllSystems();
    system = new AISystem();
    entity = new Entity();
    entity.add(
        new AIComponent(
            null,
            e -> {},
            (entity, player) -> {
              updateCounter++;
              return false;
            }));
    Game.add(entity);
    updateCounter = 0;
    player = new Entity();
    player.add(new PlayerComponent());
    Game.add(player);
  }

  @AfterEach
  void cleanup() {
    Game.removeAllEntities();
    Game.currentLevel(null);
    Game.removeAllSystems();
  }

  @Test
  void update() {
    system.execute();
    assertEquals(1, updateCounter);
  }

  @Test
  void update_executeFight() {

    BiFunction<Entity, Entity, Boolean> transition = Mockito.mock(BiFunction.class);
    BiConsumer<Entity, Entity> fight = Mockito.mock(BiConsumer.class);
    Consumer<Entity> idle = Mockito.mock(Consumer.class);
    when(transition.apply(entity, player)).thenReturn(true);

    AIComponent component = new AIComponent(fight, idle, transition);
    entity.add(component);
    system.execute();
    verify(fight, times(1)).accept(entity, player);
    verify(idle, never()).accept(entity);
  }

  @Test
  void update_executeIdle() {
    BiFunction<Entity, Entity, Boolean> transition = Mockito.mock(BiFunction.class);
    BiConsumer<Entity, Entity> fight = Mockito.mock(BiConsumer.class);
    Consumer<Entity> idle = Mockito.mock(Consumer.class);
    when(transition.apply(entity, player)).thenReturn(false);

    AIComponent component = new AIComponent(fight, idle, transition);
    entity.add(component);
    system.execute();
    verify(idle, times(1)).accept(entity);
    verify(fight, never()).accept(entity, player);
  }
}
