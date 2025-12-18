package mushRoom.modules.qte;

import com.badlogic.gdx.scenes.scene2d.Group;
import contrib.hud.dialogs.DialogContext;
import contrib.hud.dialogs.DialogContextKeys;
import contrib.hud.dialogs.DialogFactory;
import core.Entity;
import core.Game;
import core.game.WindowEventManager;
import mushRoom.modules.EscapeRoomDialogTypes;

/**
 * Utility class for managing Following Indicator QTE dialogs.
 *
 * <p>Handles registration of the dialog type and provides methods to open the minigame.
 */
public final class FollowingIndicatorDialog {

  private static FollowingIndicatorDifficulty currentDifficulty;
  private static Runnable currentOnSuccess;
  private static Runnable currentOnFailure;
  private static FollowingIndicatorUI currentUI;

  static {
    DialogFactory.register(
        EscapeRoomDialogTypes.FOLLOWING_INDICATOR, FollowingIndicatorDialog::build);

    if (!Game.isHeadless()) {
      WindowEventManager.registerWindowRefreshListener(FollowingIndicatorDialog::handleResize);
    }
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
   * @return The opened FollowingIndicatorUI instance
   */
  public static FollowingIndicatorUI openFollowingIndicator(
      Entity user,
      FollowingIndicatorDifficulty difficulty,
      Runnable onSuccess,
      Runnable onFailure) {
    currentDifficulty = difficulty;
    currentOnSuccess = onSuccess;
    currentOnFailure = onFailure;

    DialogContext ctx =
        DialogContext.builder().type(EscapeRoomDialogTypes.FOLLOWING_INDICATOR).build();

    DialogFactory.show(ctx, user.id())
        .onClose(
            ui -> {
              if (currentUI != null && !currentUI.isSuccess() && !currentUI.isFailed()) {
                currentUI.triggerFailure();
              }
              // Dispose when dialog closes
              if (currentUI != null) {
                currentUI.dispose();
                currentUI = null;
              }
            });

    return currentUI;
  }

  private static void handleResize() {
    if (currentUI != null) {
      currentUI.setSize(Game.windowWidth(), Game.windowHeight());
    }
  }

  private static Group build(DialogContext dialogContext) {
    Entity owner = dialogContext.requireEntity(DialogContextKeys.OWNER_ENTITY);
    currentUI = new FollowingIndicatorUI(currentDifficulty, owner);

    if (currentOnSuccess != null) {
      currentUI.onSuccess(currentOnSuccess);
    }
    if (currentOnFailure != null) {
      currentUI.onFailure(currentOnFailure);
    }

    return currentUI;
  }
}
