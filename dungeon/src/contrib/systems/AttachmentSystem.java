package contrib.systems;

import contrib.components.AttachmentComponent;
import core.Entity;
import core.System;
import core.components.PositionComponent;
import core.utils.components.MissingComponentException;
import core.utils.logging.DungeonLogger;
import java.util.HashMap;
import java.util.Optional;

/**
 * This system takes 2 PositionComponents and attaches them in a way where one PositionComponent
 * copies the position of another one.
 *
 * <p>The copied component will be ether at a fixed point which is the {@link AttachmentComponent}
 * offset or if the offset is Vector2.ZERO it will be infront of the origin component. The distance
 * between these two can be scaled with the scale of the {@link AttachmentComponent}.
 */
public class AttachmentSystem extends System {

  private static final DungeonLogger LOGGER = DungeonLogger.getLogger(AttachmentSystem.class);

  /** Map where the key is the copied component and the value is the origin component. */
  private static HashMap<PositionComponent, PositionComponent> attachmentMap = new HashMap<>();

  /**
   * Creates a new AttachmentSystem.
   *
   * <p>Entities that are being involved in an attachment will be cleared off the HashMap when they
   * are removed.
   */
  public AttachmentSystem() {
    super(AttachmentComponent.class);
    onEntityRemove =
        (entity -> {
          entity
              .fetch(PositionComponent.class)
              .ifPresent(
                  pc -> {
                    attachmentMap.remove(pc);
                    attachmentMap.entrySet().removeIf(entry -> entry.getValue().equals(pc));
                  });
        });
  }

  /** Updates the position of all attached PositionComponents. */
  @Override
  public void execute() {
    filteredEntityStream()
        .map(this::buildDataObject)
        .flatMap(Optional::stream)
        .forEach(this::applyAttachment);
  }

  /**
   * Registers two PositionComponents so the copy is attached to the origin.
   *
   * @param copy The PositionComponent that gets updated each frame.
   * @param origin The PositionComponent that provides the position for the copy.
   */
  public static void registerAttachment(PositionComponent copy, PositionComponent origin) {
    attachmentMap.put(copy, origin);
  }

  /**
   * Unregisters a PositionComponent from the system so it doesn't get updated by it anymore.
   *
   * @param copy The PositionComponent that got updated.
   */
  public static void unregisterAttachment(PositionComponent copy) {
    attachmentMap.remove(copy);
  }

  /**
   * Builds a data object for better handling in the system.
   *
   * @param e The entity that has the {@link AttachmentComponent}.
   * @return A {@link Optional} containing the {@link ASData} or an empty Optional if no origin
   *     {@link PositionComponent} was found.
   */
  private Optional<ASData> buildDataObject(Entity e) {
    PositionComponent copypc =
        e.fetch(PositionComponent.class)
            .orElseThrow(() -> MissingComponentException.build(e, PositionComponent.class));

    AttachmentComponent ac =
        e.fetch(AttachmentComponent.class)
            .orElseThrow(() -> MissingComponentException.build(e, AttachmentComponent.class));

    PositionComponent originpc = attachmentMap.get(copypc);
    if (originpc == null) {
      LOGGER.warn(
          "No origin PositionComponent found for attached entity {}. Removing AttachmentComponent.",
          e.id());
      e.remove(AttachmentComponent.class);
      return Optional.empty();
    }

    return Optional.of(new ASData(e, ac, copypc, originpc));
  }

  /**
   * Applies the attachment logic.
   *
   * <p>if isRotatingWithOrigin from the {@link AttachmentComponent} is true the position of the
   * copied PositionComponent gets set in front of the one origin with a distance of one tile. This
   * can be scaled with the scale of the {@link AttachmentComponent}.
   *
   * <p>if isRotatingWithOrigin from the {@link AttachmentComponent} is false the position of the
   * copied PositionComponent gets set on the origin with an offset. The distance of this can be
   * scaled with the scale of the {@link AttachmentComponent}.
   *
   * @param asData The Attachment Data with the attached Components.
   */
  private void applyAttachment(ASData asData) {
    if (asData.ac.isRotatingWithOrigin()) {
      asData.copypc.position(
          asData
              .originpc
              .position()
              .translate(asData.originpc.viewDirection().scale(asData.ac.getScale())));
      if (asData.ac.isTextureRotating()) {
        switch (asData.originpc.viewDirection()) {
          case UP -> {
            asData.copypc.rotation(90);
          }
          case RIGHT -> {
            asData.copypc.rotation(0);
          }
          case DOWN -> {
            asData.copypc.rotation(270);
          }
          case LEFT -> {
            asData.copypc.rotation(180);
          }
          default -> {}
        }
      }

    } else {
      asData.copypc.position(
          asData.originpc.position().translate(asData.ac.getOffset().scale(asData.ac.getScale())));
    }
    PositionSync.syncPosition(asData.e);
  }

  private record ASData(
      Entity e, AttachmentComponent ac, PositionComponent copypc, PositionComponent originpc) {}
}
