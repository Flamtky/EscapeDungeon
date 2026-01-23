package network;

import contrib.components.IllegalComponent;
import core.Entity;
import core.network.DefaultSnapshotTranslator;
import core.network.messages.s2c.EntityState;
import guard.AlertnessComponent;

/**
 * Custom SnapshotTranslator for the EscapeRoom subproject.
 *
 * <p>Extends {@link DefaultSnapshotTranslator} to add support for synchronizing {@link
 * AlertnessComponent} data across the network in multiplayer sessions.
 *
 * @see DefaultSnapshotTranslator
 * @see EscapeRoomEntityState
 * @see AlertnessComponent
 */
public class EscapeRoomSnapshotTranslator extends DefaultSnapshotTranslator {

  /**
   * Creates a new EscapeRoomEntityState.Builder for entities with AlertnessComponent, otherwise
   * falls back to the default EntityState.Builder.
   *
   * @param entity the entity to create a builder for
   * @return a new Builder instance appropriate for the entity
   */
  @Override
  protected EntityState.Builder createBuilder(Entity entity) {
    if (entity.isPresent(AlertnessComponent.class) || entity.isPresent(IllegalComponent.class)) {
      return EscapeRoomEntityState.builder();
    }
    return super.createBuilder(entity);
  }

  /**
   * Populates the builder with data from the entity's components, including AlertnessComponent data
   * for EscapeRoomEntityState.Builder.
   *
   * @param entity the entity to extract data from
   * @param builder the builder to populate
   */
  @Override
  protected void populateBuilder(Entity entity, EntityState.Builder builder) {
    super.populateBuilder(entity, builder);

    // Add alertness data if the builder is an EscapeRoomEntityState.Builder
    if (builder instanceof EscapeRoomEntityState.Builder escapeBuilder) {
      entity
          .fetch(AlertnessComponent.class)
          .ifPresent(
              ac -> {
                escapeBuilder.currentAlertness(ac.alertness());
                escapeBuilder.maxAlertness(ac.max());
                escapeBuilder.viewConeAngle(ac.viewConeAngle());
                escapeBuilder.viewRange(ac.viewRange());
                escapeBuilder.decayRate(ac.decayRate());
              });
      entity
          .fetch(IllegalComponent.class)
          .ifPresent(
              illegalComp -> {
                escapeBuilder.isIllegal(illegalComp.isIllegal());
              });
    }
  }

  /**
   * Applies additional entity state from the snapshot to the entity, including AlertnessComponent
   * data from EscapeRoomEntityState.
   *
   * @param entity the entity to apply state to
   * @param state the entity state from the snapshot
   */
  @Override
  protected void applyEntityState(Entity entity, EntityState state) {
    super.applyEntityState(entity, state);

    // Apply alertness data if the state is an EscapeRoomEntityState
    if (state instanceof EscapeRoomEntityState escapeState) {
      escapeState
          .maxAlertness()
          .ifPresent(
              maxAlertness -> {
                AlertnessComponent ac =
                    entity
                        .fetch(AlertnessComponent.class)
                        .orElseGet(
                            () -> {
                              // Create with synced values; viewConeAngle, viewRange, decayRate
                              // default to the values from the snapshot
                              AlertnessComponent newAc =
                                  new AlertnessComponent(
                                      maxAlertness,
                                      escapeState.decayRate().orElse(15f),
                                      escapeState.viewConeAngle().orElse(90f),
                                      escapeState.viewRange().orElse(20f));
                              entity.add(newAc);
                              return newAc;
                            });

                // Update current alertness if present
                escapeState.currentAlertness().ifPresent(ac::alertness);
              });
      escapeState
          .isIllegal()
          .ifPresent(
              isIllegal -> {
                IllegalComponent illegalComp =
                    entity
                        .fetch(IllegalComponent.class)
                        .orElseGet(
                            () -> {
                              IllegalComponent newIllegalComp = new IllegalComponent();
                              entity.add(newIllegalComp);
                              return newIllegalComp;
                            });
                if (isIllegal) {
                  illegalComp.addReason(IllegalComponent.Reason.UNKNOWN);
                } else {
                  illegalComp.removeReason(IllegalComponent.Reason.UNKNOWN);
                }
              });
    }
  }
}
