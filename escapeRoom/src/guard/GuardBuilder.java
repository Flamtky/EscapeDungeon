package guard;

import analytics.DungeonAnalyticsAPI;
import contrib.components.AIComponent;
import contrib.components.AttachmentComponent;
import contrib.components.CollideComponent;
import contrib.components.IllegalComponent;
import contrib.components.UIComponent;
import contrib.hud.DialogUtils;
import contrib.hud.dialogs.DialogCallbackResolver;
import contrib.hud.dialogs.DialogContextKeys;
import contrib.systems.EventScheduler;
import contrib.utils.EntityUtils;
import contrib.utils.components.ai.AIUtils;
import contrib.utils.components.ai.fight.AIChaseBehaviour;
import core.Entity;
import core.Game;
import core.components.AnalyticsComponent;
import core.components.PositionComponent;
import core.components.VelocityComponent;
import core.level.utils.LevelUtils;
import core.utils.Direction;
import core.utils.Point;
import core.utils.Vector2;
import core.utils.components.path.SimpleIPath;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import mobs.EscapeRoomMonsterBuilder;
import mushRoom.modules.qte.FollowingIndicatorDialog;
import mushRoom.modules.qte.FollowingIndicatorDifficulty;

/**
 * Builder for creating guard entities in the escape room.
 *
 * <p>Guards are entities that detect players within their view cone and track alertness. They use
 * the knight texture and include components for position, drawing, collision, and alertness.
 *
 * <p>Example usage:
 *
 * <pre>{@code
 * Entity guard = new GuardBuilder()
 *     .viewConeAngle(90f)
 *     .viewRange(15f)
 *     .build(spawnPoint);
 * }</pre>
 *
 * @see AlertnessComponent
 * @see GuardDetectionSystem
 */
public class GuardBuilder extends EscapeRoomMonsterBuilder.Builder {

  /** Default texture path for guard entities. */
  private static final String DEFAULT_TEXTURE_PATH = "character/knight";

  private float viewConeAngle = 45f;
  private float viewRange = 15f;
  private int alertnessThreshold = 100;
  private int alertnessLowerThreshold = 25;
  private boolean stayAlertOnceTriggered = true;
  private String captureFailureTitle;
  private String captureFailureText;
  private Point returnPointAfterCapture;
  private long returnTimeoutMs;
  private BiConsumer<Entity, Entity> onCaptureFinished;

  /** Creates a new GuardBuilder with default settings. */
  public GuardBuilder() {
    // Default constructor
  }

  /**
   * Sets the view cone angle for the guard.
   *
   * @param angle the view cone angle in degrees (full angle, not half-angle)
   * @return this builder for chaining
   */
  public GuardBuilder viewConeAngle(float angle) {
    this.viewConeAngle = angle;
    return this;
  }

  /**
   * Sets the maximum view range for the guard.
   *
   * @param range the view range in tiles
   * @return this builder for chaining
   */
  public GuardBuilder viewRange(float range) {
    this.viewRange = range;
    return this;
  }

  /**
   * Sets the alertness threshold and behavior for the guard.
   *
   * @param threshold the alertness threshold to trigger behavior
   * @param lowerThreshold the alertness level to reset the trigger (ignored if stayOnceTriggered is
   *     true)
   * @param stayOnceTriggered whether the guard stays alert once triggered
   * @return this builder for chaining
   */
  public GuardBuilder alertnessThreshold(
      int threshold, int lowerThreshold, boolean stayOnceTriggered) {
    this.alertnessThreshold = threshold;
    this.alertnessLowerThreshold = lowerThreshold;
    this.stayAlertOnceTriggered = stayOnceTriggered;
    return this;
  }

  /**
   * Sets a custom dialog shown when the capture QTE fails.
   *
   * @param title dialog title
   * @param text dialog text
   * @return this builder for chaining
   */
  public GuardBuilder captureFailureDialog(String title, String text) {
    this.captureFailureTitle = title;
    this.captureFailureText = text;
    return this;
  }

  /**
   * Configures the guard to return to a point after capture completion.
   *
   * @param returnPoint point to return to
   * @param timeoutMs maximum return time before teleport fallback
   * @return this builder for chaining
   */
  public GuardBuilder returnToAfterCapture(Point returnPoint, long timeoutMs) {
    this.returnPointAfterCapture = returnPoint;
    this.returnTimeoutMs = timeoutMs;
    return this;
  }

  /**
   * Sets a callback executed after the capture flow and optional guard return finish.
   *
   * @param callback callback receiving guard and captured player
   * @return this builder for chaining
   */
  public GuardBuilder onCaptureFinished(BiConsumer<Entity, Entity> callback) {
    this.onCaptureFinished = callback;
    return this;
  }

  /**
   * Builds the guard entity at the specified spawn position.
   *
   * @param spawnPos the position to spawn the guard
   * @return the constructed guard entity
   */
  public Entity build(Point spawnPos) {
    this.name("Guard");
    this.texture(new SimpleIPath(DEFAULT_TEXTURE_PATH));
    this.health(-1); // no health component by default
    var oldAddToGame = this.addToGame;
    this.addToGame(false); // add manually after adding alertness component
    this.fightAI(GuardCaseAI::new);
    this.transitionAI(
        () ->
            new GuardTransition(
                alertnessThreshold, alertnessLowerThreshold, stayAlertOnceTriggered));

    Entity guard = super.build(spawnPos);

    // decrease collider size
    CollideComponent cc = guard.fetch(CollideComponent.class).orElseThrow();
    cc.collider().width(0.5f);
    cc.collider().height(0.5f);
    cc.collider().offset(Vector2.of(0.25f, 0.05f));

    // Alertness component with configured view cone settings
    AlertnessComponent ac = new AlertnessComponent(viewConeAngle, viewRange);
    guard.add(ac);
    if (hasCaptureBehavior()) {
      guard.add(
          new GuardCaptureBehaviorComponent(
              captureFailureTitle,
              captureFailureText,
              returnPointAfterCapture,
              returnTimeoutMs,
              onCaptureFinished));
    }

    if (oldAddToGame) {
      Game.add(guard);
    }

    return guard;
  }

  private boolean hasCaptureBehavior() {
    return captureFailureTitle != null
        || captureFailureText != null
        || returnPointAfterCapture != null
        || onCaptureFinished != null;
  }

  private static class GuardTransition implements BiFunction<Entity, Entity, Boolean> {

    private final int threshold;
    private final int lowerThreshold;
    private final boolean stayOnceTriggered;
    private boolean triggered = false;
    private long lastTriggeredTime = 0;

    /**
     * Creates a GuardTransition with specified thresholds and behavior.
     *
     * @param threshold The alertness threshold to trigger the transition
     * @param lowerThreshold The lower threshold to reset the transition (ignored if
     *     stayOnceTriggered is true)
     * @param stayOnceTriggered Whether to stay triggered once activated
     */
    public GuardTransition(int threshold, int lowerThreshold, boolean stayOnceTriggered) {
      this.threshold = threshold;
      this.lowerThreshold = lowerThreshold;
      this.stayOnceTriggered = stayOnceTriggered;
    }

    @Override
    public Boolean apply(Entity guard, Entity player) {
      if (!isIllegalCaptureTarget(player)) {
        reset();
        return false;
      }

      AlertnessComponent ac =
          guard
              .fetch(AlertnessComponent.class)
              .orElseThrow(() -> new IllegalStateException("Guard missing AlertnessComponent"));

      if (ac.lastSeenEntity().isEmpty() || !ac.lastSeenEntity().get().equals(player)) {
        return false;
      }

      if (ac.alertness() >= threshold && !triggered) {
        triggered = true;
        lastTriggeredTime = System.currentTimeMillis();
        if (!player.isPresent(AnalyticsComponent.class)) return true;
        DungeonAnalyticsAPI.logXApiStatement(
            player.fetch(AnalyticsComponent.class).orElseThrow(),
            DungeonAnalyticsAPI.Verb.DETECTED,
            guard,
            Map.of("alertness", ac.alertness()),
            null);
        return true;
      }

      if (!stayOnceTriggered && ac.alertness() <= lowerThreshold && triggered) {
        triggered = false;
        if (!player.isPresent(AnalyticsComponent.class)) return false;
        DungeonAnalyticsAPI.logXApiStatement(
            player.fetch(AnalyticsComponent.class).orElseThrow(),
            DungeonAnalyticsAPI.Verb.LOST_DETECTION,
            guard,
            Map.of(
                "alertness",
                ac.alertness(),
                "duration_ms",
                System.currentTimeMillis() - lastTriggeredTime),
            null);
      }

      return triggered;
    }

    private boolean isIllegalCaptureTarget(Entity player) {
      return player.fetch(IllegalComponent.class).map(IllegalComponent::isIllegal).orElse(false);
    }

    void reset() {
      triggered = false;
      lastTriggeredTime = 0;
    }
  }

  private static class GuardCaseAI extends AIChaseBehaviour {

    private static final float CLOSE_DISTANCE = 0.75f;
    private static final float RETURN_COMPLETE_DISTANCE = 0.5f;
    private static final long DEFAULT_RETURN_TIMEOUT_MS = 10_000;
    private static final long SOLITARY_RELEASE_DELAY_MS = 5_000;
    private Entity grabbedPlayer = null;
    private Entity lastCapturedPlayer = null;
    private long capturedTime = 0;
    private boolean returningToHome = false;
    private long returnStartedAt = 0;

    @Override
    public void accept(final Entity guard, final Entity player) {
      if (grabbedPlayer != null) {
        // If player is already grabbed, bring them to the cell
        bringPlayerToCell(guard);
        return;
      }
      if (returningToHome) {
        returnGuardHome(guard);
        return;
      }
      if (!isIllegalCaptureTarget(player)) {
        stopGuardMovement(guard);
        return;
      }

      float distanceToPlayer = AIUtils.distanceBetweenEntities(guard, player);

      // Grab player if close enough and not already grabbed
      if (distanceToPlayer < CLOSE_DISTANCE && !player.isPresent(AttachmentComponent.class)) {
        grabPlayer(guard, player);
        return;
      }

      super.accept(guard, player); // Default chase behavior
    }

    private void grabPlayer(Entity guard, Entity player) {
      if (this.grabbedPlayer != null) {
        return; // Already grabbing a player
      }

      this.grabbedPlayer = player;
      this.capturedTime = System.currentTimeMillis();

      var ac =
          new AttachmentComponent(
              Vector2.of(0.1f, 0f),
              player.fetch(PositionComponent.class).orElseThrow(),
              guard.fetch(PositionComponent.class).orElseThrow());
      player.add(ac);
      player.fetch(CollideComponent.class).ifPresent(cc -> cc.isSolid(false));
      player
          .fetch(UIComponent.class)
          .ifPresent(
              (ui) ->
                  DialogCallbackResolver.createButtonCallback(
                          ui.dialogContext().dialogId(), DialogContextKeys.ON_CLOSE)
                      .accept(null));

      var guardPos = EntityUtils.getPosition(guard);
      var posData = Map.of("x", (int) guardPos.x(), "y", (int) guardPos.y());
      if (!player.isPresent(AnalyticsComponent.class)) return;
      DungeonAnalyticsAPI.logXApiStatement(
          player.fetch(AnalyticsComponent.class).orElseThrow(),
          DungeonAnalyticsAPI.Verb.CAPTURED,
          guard,
          Map.of("position", posData),
          null);
    }

    private void bringPlayerToCell(Entity guard) {
      Point cellPos =
          Game.currentLevel().map(level -> level.namedPoints().get("cell")).orElseThrow();
      Point guardPos = EntityUtils.getPosition(guard);

      var path = LevelUtils.calculatePath(guardPos, cellPos);

      if (path.getCount() <= 1) { // TODO: PathFinished not working here
        // Release player in cell
        FollowingIndicatorDialog.openFollowingIndicator(
            grabbedPlayer,
            FollowingIndicatorDifficulty.HARD,
            () -> releasePlayer(guard),
            () -> showFailureDialogThenReleaseLater(guard));
        guard.fetch(AlertnessComponent.class).ifPresent(AlertnessComponent::reset);
        stopGuardMovement(guard);
        guard.fetch(AIComponent.class).ifPresent(ai -> ai.active(false));
        if (!grabbedPlayer.isPresent(AnalyticsComponent.class)) return;
        DungeonAnalyticsAPI.logXApiStatement(
            grabbedPlayer.fetch(AnalyticsComponent.class).orElseThrow(),
            DungeonAnalyticsAPI.Verb.RELEASED,
            guard,
            Map.of("time_captured_ms", System.currentTimeMillis() - capturedTime),
            null);

        return;
      }

      guard
          .fetch(AlertnessComponent.class)
          .ifPresent(ac -> ac.increaseAlertness(999f, grabbedPlayer)); // keep alert
      guard
          .fetch(VelocityComponent.class)
          .ifPresent(vc -> vc.modifier("sprint", 2.5f)); // increase speed to cell
      AIUtils.followPath(guard, path);
    }

    private void showFailureDialogThenReleaseLater(Entity guard) {
      Entity capturedPlayer = grabbedPlayer;
      if (capturedPlayer == null) {
        return;
      }
      GuardCaptureBehaviorComponent behavior = captureBehavior(guard).orElse(null);
      String title =
          behavior != null && behavior.hasFailureDialog() ? behavior.failureTitle() : "Einzelhaft";
      String text =
          behavior != null && behavior.hasFailureDialog()
              ? behavior.failureText()
              : "Das hat nicht geklappt! Jetzt muss ich kurz warten, bis ich mich wieder bewegen darf.";
      DialogUtils.showTextPopup(
          text,
          title,
          () ->
              EventScheduler.scheduleAction(() -> releasePlayer(guard), SOLITARY_RELEASE_DELAY_MS),
          capturedPlayer.id());
    }

    private void releasePlayer(Entity guard) {
      Entity capturedPlayer = grabbedPlayer;
      if (capturedPlayer == null) {
        return;
      }
      capturedPlayer.remove(AttachmentComponent.class);
      capturedPlayer.fetch(CollideComponent.class).ifPresent(cc -> cc.isSolid(true));
      guard.fetch(AIComponent.class).ifPresent(ai -> ai.active(true));
      Game.tileAt(EntityUtils.getPosition(capturedPlayer))
          .ifPresent(
              tile -> {
                if (tile.isAccessible()) return;

                var dropPos =
                    LevelUtils.randomAccessibleTileInRangeAsPoint(tile.position(), 2)
                        .orElse(tile.position());
                capturedPlayer.fetch(PositionComponent.class).ifPresent(pc -> pc.position(dropPos));
              });
      this.grabbedPlayer = null;
      captureBehavior(guard)
          .flatMap(GuardCaptureBehaviorComponent::returnPoint)
          .ifPresentOrElse(
              ignored -> startReturnHome(guard, capturedPlayer),
              () -> finishCapture(guard, capturedPlayer));
    }

    private void startReturnHome(Entity guard, Entity capturedPlayer) {
      returningToHome = true;
      returnStartedAt = System.currentTimeMillis();
      lastCapturedPlayer = capturedPlayer;
      guard.fetch(AIComponent.class).ifPresent(ai -> ai.active(true));
      guard.fetch(AlertnessComponent.class).ifPresent(AlertnessComponent::reset);
      stopGuardMovement(guard);
    }

    private void returnGuardHome(Entity guard) {
      Optional<GuardCaptureBehaviorComponent> behavior = captureBehavior(guard);
      Optional<Point> returnPoint = behavior.flatMap(GuardCaptureBehaviorComponent::returnPoint);
      if (returnPoint.isEmpty()) {
        finishReturnHome(guard);
        return;
      }

      Point target = returnPoint.orElseThrow();
      Point current = EntityUtils.getPosition(guard);
      if (current.distance(target) <= RETURN_COMPLETE_DISTANCE) {
        finishReturnHome(guard);
        return;
      }

      long timeout =
          behavior
              .map(GuardCaptureBehaviorComponent::returnTimeoutMs)
              .orElse(DEFAULT_RETURN_TIMEOUT_MS);
      if (timeout <= 0 || System.currentTimeMillis() - returnStartedAt >= timeout) {
        guard
            .fetch(PositionComponent.class)
            .ifPresent(
                position -> {
                  position.position(target);
                  position.viewDirection(Direction.DOWN);
                });
        stopGuardMovement(guard);
        finishReturnHome(guard);
        return;
      }

      var path = LevelUtils.calculatePath(current, target);
      if (path.getCount() > 1) {
        AIUtils.followPath(guard, path);
      }
    }

    private void finishReturnHome(Entity guard) {
      returningToHome = false;
      guard.fetch(AlertnessComponent.class).ifPresent(AlertnessComponent::reset);
      guard
          .fetch(PositionComponent.class)
          .ifPresent(position -> position.viewDirection(Direction.DOWN));
      stopGuardMovement(guard);
      guard.fetch(AIComponent.class).ifPresent(ai -> ai.active(true));
      finishCapture(guard, lastCapturedPlayer);
      lastCapturedPlayer = null;
    }

    private void finishCapture(Entity guard, Entity capturedPlayer) {
      resetGuardTransition(guard);
      captureBehavior(guard)
          .flatMap(GuardCaptureBehaviorComponent::onCaptureFinished)
          .ifPresent(callback -> callback.accept(guard, capturedPlayer));
    }

    private Optional<GuardCaptureBehaviorComponent> captureBehavior(Entity guard) {
      return guard.fetch(GuardCaptureBehaviorComponent.class);
    }

    private boolean isIllegalCaptureTarget(Entity player) {
      return player.fetch(IllegalComponent.class).map(IllegalComponent::isIllegal).orElse(false);
    }

    private void resetGuardTransition(Entity guard) {
      guard
          .fetch(AIComponent.class)
          .map(AIComponent::shouldFight)
          .filter(GuardTransition.class::isInstance)
          .map(GuardTransition.class::cast)
          .ifPresent(GuardTransition::reset);
    }

    private void stopGuardMovement(Entity guard) {
      guard
          .fetch(VelocityComponent.class)
          .ifPresent(
              velocity -> {
                velocity.removeModifier("sprint");
                velocity.removeForce("MOVEMENT");
                velocity.currentVelocity(Vector2.ZERO);
              });
    }
  }
}
