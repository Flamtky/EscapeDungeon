package mushRoom.modules.qte;

import com.badlogic.gdx.scenes.scene2d.Group;
import contrib.hud.UIUtils;
import contrib.hud.dialogs.*;
import core.Entity;
import core.Game;
import mushRoom.modules.EscapeRoomDialogTypes;

/**
 * Utility class for managing Following Indicator QTE dialogs.
 *
 * <p>Handles registration of the dialog type and provides methods to open the minigame.
 */
public final class FollowingIndicatorDialog {

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
    ui.registerCallback(DialogContextKeys.ON_CONFIRM, data -> onSuccess.run());
    ui.registerCallback(DialogContextKeys.ON_CANCEL, data -> onFailure.run());

    ui.onClose(
        (uic) -> {
          onFailure.run();
          // Dispose when dialog closes
          UIUtils.closeDialog(ui, true, false);
        });
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
              .accept(null);
        });
    currentUI.onFailure(
        () -> {
          DialogCallbackResolver.createButtonCallback(
                  dialogContext.dialogId(), DialogContextKeys.ON_CANCEL)
              .accept(null);
        });

    return currentUI;
  }
}
