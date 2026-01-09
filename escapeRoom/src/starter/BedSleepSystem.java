package starter;

import contrib.components.CollideComponent;
import contrib.components.DecoComponent;
import contrib.components.StaminaComponent;
import contrib.entities.deco.Deco;
import contrib.hud.dialogs.DialogFactory;
import contrib.modules.interaction.Interaction;
import contrib.modules.interaction.InteractionComponent;
import contrib.systems.HealthSystem;
import contrib.systems.PositionSync;
import core.Entity;
import core.Game;
import core.System;
import core.components.DrawComponent;
import core.components.InputComponent;
import core.components.PositionComponent;

/**
 * A system that manages bed sleep interactions and stamina restoration during sleep.
 *
 * <p>This system has two responsibilities:
 *
 * <ol>
 *   <li>Override the default bed interaction to trigger proper sleep functionality when a player
 *       interacts with a bed. The player is teleported to the bed, controls are disabled, and the
 *       sleep animation is played.
 *   <li>Process sleeping players (those with a {@link SleepingComponent}) and restore their stamina
 *       at the component's predefined rate until fully replenished, then end the sleep state.
 * </ol>
 *
 * <p>The system listens for bed entities (with {@link DecoComponent} of type {@link Deco#BedRed})
 * being added to the game and replaces their interaction component with custom sleep logic.
 */
public class BedSleepSystem extends System {

  /**
   * Creates a new {@code BedSleepSystem}.
   *
   * <p>This system processes entities with {@link DecoComponent} to detect beds, and entities with
   * {@link SleepingComponent} to handle stamina restoration during sleep.
   */
  public BedSleepSystem() {
    super(DecoComponent.class);

    // Override bed interactions when bed entities are added
    onEntityAdd = this::onEntityAdded;
  }

  /**
   * Called when an entity with a DecoComponent is added to the game.
   *
   * <p>If the entity is a bed (Deco.BedRed), replaces its interaction component with custom sleep
   * logic.
   *
   * @param entity the entity that was added
   */
  private void onEntityAdded(Entity entity) {
    entity
        .fetch(DecoComponent.class)
        .ifPresent(
            decoComponent -> {
              if (decoComponent.type() == Deco.BedRed) {
                overrideBedInteraction(entity);
              }
            });
  }

  /**
   * Replaces the bed's interaction component with custom sleep functionality.
   *
   * <p>The new interaction teleports the player to the bed, disables controls, plays the death
   * animation (as placeholder for sleep), and adds a {@link SleepingComponent} to trigger stamina
   * restoration.
   *
   * @param bed the bed entity to modify
   */
  private void overrideBedInteraction(Entity bed) {
    bed.fetch(CollideComponent.class)
        .ifPresent(collideComponent -> collideComponent.isSolid(false));

    InteractionComponent interaction =
        new InteractionComponent(
            () ->
                new Interaction(
                    this::startSleep, Interaction.DEFAULT_INTERACTION_RADIUS, true, "Sleep"));
    bed.add(interaction);
  }

  /**
   * Initiates the sleep state for a player interacting with a bed.
   *
   * <p>Teleports the player to the bed position, disables controls, plays the death animation (as
   * placeholder for sleep animation), and adds a {@link SleepingComponent} to begin stamina
   * restoration.
   *
   * @param bed the bed entity being interacted with
   * @param player the player entity starting to sleep
   */
  private void startSleep(Entity bed, Entity player) {
    // Check if player is already sleeping
    if (player.isPresent(SleepingComponent.class)) {
      return;
    }

    // Get stamina component
    StaminaComponent stamina = player.fetch(StaminaComponent.class).orElse(null);

    if (stamina == null) {
      return; // Player has no stamina component
    }

    // Don't sleep if already at full stamina
    if (stamina.currentAmount() >= stamina.maxAmount()) {
      DialogFactory.showOkDialog("Ich bin gerade nicht müde.", "Hellwach", () -> {}, player.id());
      return;
    }

    // Teleport player to bed position
    bed.fetch(PositionComponent.class)
        .ifPresent(
            bedPos ->
                player
                    .fetch(PositionComponent.class)
                    .ifPresent(
                        playerPos ->
                            playerPos.position(bedPos.position().translate(-0.2f, -0.25f))));
    PositionSync.syncPosition(player);

    // Disable player controls during sleep
    player.fetch(InputComponent.class).ifPresent(ic -> ic.deactivateControls(true));

    // Play death animation as placeholder for sleep animation
    player
        .fetch(DrawComponent.class)
        .ifPresent(
            dc ->
                player
                    .fetch(PositionComponent.class)
                    .ifPresentOrElse(
                        pc -> dc.sendSignal(HealthSystem.DEATH_SIGNAL, pc.viewDirection()),
                        () -> dc.sendSignal(HealthSystem.DEATH_SIGNAL)));

    // Add sleeping component to trigger stamina restoration
    player.add(new SleepingComponent(stamina.currentAmount()));
  }

  /**
   * Executes the sleep system logic.
   *
   * <p>Processes all players with a {@link SleepingComponent} and restores their stamina at the
   * component's predefined rate. When stamina is fully restored, ends the sleep state.
   */
  @Override
  public void execute() {
    // Process sleeping players
    Game.allPlayers()
        .filter(entity -> entity.fetch(SleepingComponent.class).isPresent())
        .filter(entity -> entity.fetch(StaminaComponent.class).isPresent())
        .forEach(this::updateSleep);
  }

  /**
   * Updates the sleep state for a sleeping entity, restoring stamina over time.
   *
   * <p>Stamina is restored at the rate defined in the entity's {@link SleepingComponent}. When
   * stamina is fully restored, the sleep state ends automatically.
   *
   * @param entity the sleeping entity to update
   */
  private void updateSleep(Entity entity) {
    SleepingComponent sleeping =
        entity
            .fetch(SleepingComponent.class)
            .orElseThrow(
                () -> new IllegalStateException("Entity missing SleepingComponent in updateSleep"));

    StaminaComponent stamina =
        entity
            .fetch(StaminaComponent.class)
            .orElseThrow(
                () -> new IllegalStateException("Entity missing StaminaComponent in updateSleep"));

    // Calculate stamina restoration for this frame
    float restoreAmount = sleeping.staminaRecoveryRate() / Game.frameRate();
    float newStamina = Math.min(stamina.maxAmount(), stamina.currentAmount() + restoreAmount);
    stamina.currentAmount(newStamina);

    // Check if sleep should end (stamina fully restored)
    if (stamina.currentAmount() >= stamina.maxAmount()) {
      endSleep(entity, stamina);
    }
  }

  /**
   * Ends the sleep state for an entity.
   *
   * <p>Removes the {@link SleepingComponent}, ensures stamina is at maximum, re-enables controls,
   * and resets the animation state.
   *
   * @param entity the entity waking up
   * @param stamina the entity's stamina component
   */
  private void endSleep(Entity entity, StaminaComponent stamina) {
    // Remove sleeping component
    entity.remove(SleepingComponent.class);

    // Ensure stamina is at max
    stamina.currentAmount(stamina.maxAmount());

    // Re-enable player controls after sleep
    entity.fetch(InputComponent.class).ifPresent(ic -> ic.deactivateControls(false));

    // Reset animation state to idle
    entity.fetch(DrawComponent.class).ifPresent(DrawComponent::resetState);
  }
}
