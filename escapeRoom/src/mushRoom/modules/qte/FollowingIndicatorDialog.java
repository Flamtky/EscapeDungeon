package mushRoom.modules.qte;

import analytics.DungeonAnalyticsAPI;
import com.badlogic.gdx.scenes.scene2d.Group;
import contrib.hud.UIUtils;
import contrib.hud.dialogs.*;
import contrib.systems.EventScheduler;
import core.Entity;
import core.Game;
import core.components.AnalyticsComponent;
import core.utils.Tuple;
import core.utils.logging.DungeonLogger;
import java.io.Serializable;
import java.util.Map;
import mushRoom.modules.EscapeRoomDialogTypes;

/**
 * Utility class for managing Following Indicator QTE dialogs.
 *
 * <p>Handles registration of the dialog type and provides methods to open the minigame.
 */
public final class FollowingIndicatorDialog {

  private static final DungeonLogger LOGGER =
      DungeonLogger.getLogger(FollowingIndicatorDialog.class);

  /** Delay after success/failure before closing. */
  private static final long DELAY_AFTER_END = 2000;

  static {
    DialogFactory.register(
        EscapeRoomDialogTypes.FOLLOWING_INDICATOR, FollowingIndicatorDialog::build);
  }

  private FollowingIndicatorDialog() {
    // Utility class
  }

  /**
   * Opens a Following Indicator QTE dialog for the given entity.
   *
   * @param user The entity attempting the QTE
   * @param difficulty The difficulty level
   * @param onSuccess Callback executed when successfully completed
   * @param onFailure Callback executed when failed
   */
  public static void openFollowingIndicator(
      Entity user,
      FollowingIndicatorDifficulty difficulty,
      Runnable onSuccess,
      Runnable onFailure) {
    DialogContext ctx =
        DialogContext.builder()
            .type(EscapeRoomDialogTypes.FOLLOWING_INDICATOR)
            .put("difficulty", difficulty)
            .build();

    var ui = DialogFactory.show(ctx, user.id());
    ui.registerCallback(
        DialogContextKeys.ON_CONFIRM,
        data -> {
          onSuccess.run();
          handleAnalysis(user, data);
          EventScheduler.scheduleAction(() -> UIUtils.closeDialog(ui), DELAY_AFTER_END);
        });
    ui.registerCallback(
        DialogContextKeys.ON_CANCEL,
        data -> {
          onFailure.run();
          handleAnalysis(user, data);
          EventScheduler.scheduleAction(() -> UIUtils.closeDialog(ui), DELAY_AFTER_END);
        });
    ui.registerCallback(DialogContextKeys.ON_CLOSE, data -> onFailure.run());
  }

  public static Group build(DialogContext dialogContext) {
    Entity owner = dialogContext.requireEntity(DialogContextKeys.OWNER_ENTITY);
    FollowingIndicatorDifficulty difficulty =
        dialogContext.require("difficulty", FollowingIndicatorDifficulty.class);

    if (Game.isHeadless()) {
      return new HeadlessDialogGroup("Following Indicator QTE", "Following Indicator QTE");
    }

    FollowingIndicatorUI currentUI = new FollowingIndicatorUI(difficulty, owner);

    currentUI.onSuccess(
        () -> {
          DialogCallbackResolver.createButtonCallback(
                  dialogContext.dialogId(), DialogContextKeys.ON_CONFIRM)
              .accept(new Result(true, currentUI.getResults(), difficulty));
        });
    currentUI.onFailure(
        () -> {
          DialogCallbackResolver.createButtonCallback(
                  dialogContext.dialogId(), DialogContextKeys.ON_CANCEL)
              .accept(new Result(false, currentUI.getResults(), difficulty));
        });

    return currentUI;
  }

  /** Helper record to store the result of the Following Indicator game. */
  record Result(
      boolean success, Tuple<Integer, Integer> results, FollowingIndicatorDifficulty difficulty)
      implements Serializable {}

  private static void handleAnalysis(Entity user, Serializable data) {
    System.out.println("handleAnalysis called with data: " + data);
    if (data instanceof Result result) {
      AnalyticsComponent ac = user.fetch(AnalyticsComponent.class).orElse(null);
      if (ac == null) {
        LOGGER.warn("LockPickDialog: User entity has no AnalyticsComponent. result=" + result);
        return;
      }
      DungeonAnalyticsAPI.logXApiStatement(
          ac,
          result.success ? DungeonAnalyticsAPI.Verb.SOLVED : DungeonAnalyticsAPI.Verb.FAILED,
          "qte-following-indicator-minigame",
          Map.of(
              "success", result.success,
              "successfulTries", result.results.a(),
              "failedTries", result.results.b(),
              "difficulty",
                  Map.of(
                      "indicatorSpeed", result.difficulty.indicatorSpeed(),
                      "zoneCount", result.difficulty.zoneCount(),
                      "requiredSuccesses", result.difficulty.requiredSuccesses(),
                      "maxAttempts", result.difficulty.maxAttempts())));
    } else {
      LOGGER.warn("LockPickDialog: Unexpected result data type: " + data.getClass().getName());
    }
  }
}
