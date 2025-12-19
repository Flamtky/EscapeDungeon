package mushRoom.modules.lockpick;

import com.badlogic.gdx.scenes.scene2d.Group;
import contrib.hud.dialogs.DialogContext;
import contrib.hud.dialogs.DialogContextKeys;
import contrib.hud.dialogs.DialogFactory;
import core.Entity;
import core.Game;
import core.game.WindowEventManager;
import mushRoom.modules.EscapeRoomDialogTypes;

/**
 * Utility class for managing lock-picking dialogs.
 *
 * <p>Handles registration of the lock-pick dialog type and provides methods to open the lock-pick
 * minigame.
 */
public final class LockPickDialog {

  private static LockPickDifficulty currentDifficulty;
  private static Runnable currentOnSuccess;
  private static Runnable currentOnFailure;
  private static LockPickUI currentUI;

  static {
    DialogFactory.register(EscapeRoomDialogTypes.LOCKPICK, LockPickDialog::build);

    if (!Game.isHeadless()) {
      WindowEventManager.registerWindowRefreshListener(LockPickDialog::handleResize);
    }
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
   * @return The opened LockPickUI instance
   */
  public static LockPickUI openLockPick(
      Entity user, LockPickDifficulty difficulty, Runnable onSuccess, Runnable onFailure) {
    currentDifficulty = difficulty;
    currentOnSuccess = onSuccess;
    currentOnFailure = onFailure;

    DialogContext ctx = DialogContext.builder().type(EscapeRoomDialogTypes.LOCKPICK).build();

    // Show and wire up onClose to trigger failure if the lock is still locked
    DialogFactory.show(ctx, user.id())
        .onClose(
            (ui) -> {
              if (currentUI != null && currentUI.isLocked()) {
                currentUI.triggerFailure();
              }
              // Dispose textures when dialog closes
              if (currentUI != null) {
                currentUI.dispose();
                currentUI = null;
              }
            });

    return currentUI;
  }

  /** Handles window resize events to adjust the size of the current lock-pick UI. */
  private static void handleResize() {
    if (currentUI != null) {
      currentUI.setSize(Game.windowWidth(), Game.windowHeight());
    }
  }

  private static Group build(DialogContext dialogContext) {
    Entity owner = dialogContext.requireEntity(DialogContextKeys.OWNER_ENTITY);
    currentUI = new LockPickUI(currentDifficulty, owner);

    if (currentOnSuccess != null) {
      currentUI.onSuccess(currentOnSuccess);
    }
    if (currentOnFailure != null) {
      currentUI.onFailure(currentOnFailure);
    }

    return currentUI;
  }
}
