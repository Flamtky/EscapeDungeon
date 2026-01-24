package mushRoom.modules.lockpick;

import analytics.DungeonAnalyticsAPI;
import com.badlogic.gdx.scenes.scene2d.Group;
import contrib.hud.UIUtils;
import contrib.hud.dialogs.*;
import contrib.systems.EventScheduler;
import core.Entity;
import core.Game;
import core.components.AnalyticsComponent;
import core.utils.logging.DungeonLogger;
import java.io.Serializable;
import java.util.Map;
import mushRoom.modules.EscapeRoomDialogTypes;

/**
 * Utility class for managing lock-picking dialogs.
 *
 * <p>Handles registration of the lock-pick dialog type and provides methods to open the lock-pick
 * minigame.
 */
public final class LockPickDialog {

  private static final DungeonLogger LOGGER = DungeonLogger.getLogger(LockPickDialog.class);

  /** Delay after success/failure before closing. */
  private static final long DELAY_AFTER_END = 0;

  static {
    DialogFactory.register(EscapeRoomDialogTypes.LOCKPICK, LockPickDialog::build);
  }

  private LockPickDialog() {
    // Utility class
  }

  /**
   * Opens a lock-pick dialog for the given entity.
   *
   * <p>Displays an interactive lock-picking minigame with the specified difficulty. When the dialog
   * is closed or the lock is successfully picked, appropriate callbacks are triggered.
   *
   * @param user The entity attempting to pick the lock
   * @param difficulty The difficulty level for the lock-pick minigame
   * @param onSuccess Callback executed when the lock is successfully picked
   * @param onFailure Callback executed when all attempts are exhausted or dialog is closed
   */
  public static void openLockPick(
      Entity user, LockPickDifficulty difficulty, Runnable onSuccess, Runnable onFailure) {
    DialogContext ctx =
        DialogContext.builder()
            .type(EscapeRoomDialogTypes.LOCKPICK)
            .put("difficulty", difficulty)
            .build();

    // Show and wire up onClose to trigger failure if the lock is still locked
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
    LockPickDifficulty currentDifficulty =
        dialogContext.require("difficulty", LockPickDifficulty.class);

    if (Game.isHeadless()) {
      return new HeadlessDialogGroup("Lock Pick Minigame", "Lock Pick Minigame");
    }

    LockPickUI currentUI = new LockPickUI(currentDifficulty, owner);

    currentUI.onSuccess(
        () ->
            DialogCallbackResolver.createButtonCallback(
                    dialogContext.dialogId(), DialogContextKeys.ON_CONFIRM)
                .accept(new Result(true, currentUI.getAttempt(), currentDifficulty)));
    currentUI.onFailure(
        () ->
            DialogCallbackResolver.createButtonCallback(
                    dialogContext.dialogId(), DialogContextKeys.ON_CANCEL)
                .accept(new Result(false, currentUI.getAttempt(), currentDifficulty)));

    return currentUI;
  }

  /** Helper record to store the result of the lock-pick game. */
  record Result(boolean success, int attemptsNeeded, LockPickDifficulty difficulty)
      implements Serializable {}

  private static void handleAnalysis(Entity user, Serializable data) {
    if (data instanceof Result result) {
      AnalyticsComponent ac = user.fetch(AnalyticsComponent.class).orElse(null);
      if (ac == null) {
        LOGGER.warn("LockPickDialog: User entity has no AnalyticsComponent. result=" + result);
        return;
      }
      DungeonAnalyticsAPI.logXApiStatement(
          ac,
          result.success ? DungeonAnalyticsAPI.Verb.SOLVED : DungeonAnalyticsAPI.Verb.FAILED,
          "lock-pick-minigame",
          Map.of(
              "success",
              result.success,
              "attemptsNeeded",
              result.attemptsNeeded,
              "difficulty",
              Map.of("ringCount", result.difficulty.ringCount()),
              "minNotchWidthDegrees",
              result.difficulty.minNotchWidthDegrees(),
              "maxNotchWidthDegrees",
              result.difficulty.maxNotchWidthDegrees(),
              "maxAttempts",
              LockPickUI.ATTEMPTS));
    } else {
      LOGGER.warn("LockPickDialog: Unexpected result data type: " + data.getClass().getName());
    }
  }
}
