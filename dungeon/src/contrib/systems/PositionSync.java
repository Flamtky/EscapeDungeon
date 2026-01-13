package contrib.systems;

import contrib.components.CollideComponent;
import core.Entity;
import core.components.PositionComponent;
import core.components.VelocityComponent;
import core.game.ECSManagement;

/**
 * This system syncs the position of entities with other components that rely on mid-frame position
 * updates.
 *
 * <p>Currently updating components:
 *
 * <ul>
 *   <li>{@link CollideComponent} - Syncs the collider's position with the entity's position.
 * </ul>
 */
public class PositionSync {

  /** Creates a new PositionSyncSystem. */
  public PositionSync() {}

  /**
   * Sync the position of the given entity with its relevant components.
   *
   * <p>For entities without a {@link VelocityComponent}, the tile cache is also refreshed. This
   * handles the case where non-moving entities are teleported or have their position changed
   * programmatically. Entities with {@link VelocityComponent} are lazily revalidated when queried
   * via {@link ECSManagement#getEntitiesAtTile}.
   *
   * @param e The entity to sync the position for.
   */
  public static void syncPosition(Entity e) {
    e.fetch(PositionComponent.class)
        .ifPresent(
            pc -> {
              // CollideComponent
              e.fetch(CollideComponent.class)
                  .ifPresent(
                      cc -> {
                        cc.collider().position(pc.position());
                        cc.collider().scale(pc.scale());
                      });
            });

    // Refresh tile cache for non-velocity entities (assumed teleportation)
    if (!e.isPresent(VelocityComponent.class)) {
      ECSManagement.refreshEntityTileCache(e);
    }
  }
}
