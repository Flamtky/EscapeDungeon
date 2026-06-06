package presentation;

import com.badlogic.gdx.Input;
import contrib.components.CollideComponent;
import contrib.entities.NPCFactory;
import contrib.hud.DialogUtils;
import contrib.hud.dialogs.ChoiceOption;
import contrib.hud.dialogs.DialogFactory;
import contrib.modules.interaction.Interaction;
import contrib.modules.interaction.InteractionComponent;
import contrib.utils.components.showImage.TransitionSpeed;
import core.Entity;
import core.Game;
import core.level.DungeonLevel;
import core.level.utils.DesignLabel;
import core.level.utils.LevelElement;
import core.network.messages.c2s.DialogResponseMessage;
import core.utils.Point;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/** A compact level that demonstrates the available dialogue and presentation UI options. */
public class PresentationDemoLevel extends DungeonLevel {
  private static final Point PRESENTER_POSITION = new Point(4, 5);
  private static final String PRESENTER_SPRITESHEET = "character/wizard";
  private static final String PRESENTER_PORTRAIT = "office/boss.png";
  private static final String PLAYER_PORTRAIT = "logo/cat_logo_64x64.png";
  private static final String BOOK_IMAGE = "images/open-book.png";
  private static final String DETAIL_IMAGE = "images/magnifying_glass.png";
  private static final String SCROLL_ICON = "items/rpg/item_scroll.png";

  /**
   * Creates the presentation demo level.
   *
   * @param layout the tile layout
   * @param designLabel the visual tile design
   * @param namedPoints custom points from the level file
   */
  public PresentationDemoLevel(
      LevelElement[][] layout, DesignLabel designLabel, Map<String, Point> namedPoints) {
    super(layout, designLabel, namedPoints, "Presentation Demo");
  }

  @Override
  protected void onFirstTick() {
    Entity presenter = createPresenter();
    Game.add(presenter);
    Game.player()
        .ifPresent(
            hero ->
                DialogFactory.showDialogDialog(
                    speakerLine(
                            "Mentor",
                            PRESENTER_PORTRAIT,
                            "[tr speed=1.2]Welcome. This room is a compact demo for presentation"
                                + " dialogs inside the dungeon.")
                        + "[p]"
                        + speakerLine(
                            "Mentor",
                            PRESENTER_PORTRAIT,
                            "Walk to me and press [key code="
                                + Input.Keys.E
                                + "] to open the menu. It demonstrates speaker pages, rich text,"
                                + " multiple answer buttons, and fullscreen images."),
                    () -> {},
                    hero.id()));
  }

  private Entity createPresenter() {
    Entity presenter = NPCFactory.createNPC(PRESENTER_POSITION, PRESENTER_SPRITESHEET);
    presenter.add(
        new InteractionComponent(
            () -> new Interaction((entity, who) -> showMainMenu(who), 2.5f, "Presentation demo")));
    presenter.fetch(CollideComponent.class).ifPresent(collider -> collider.isSolid(true));
    return presenter;
  }

  private void showMainMenu(Entity who) {
    List<ChoiceOption> options =
        List.of(
            ChoiceOption.of("Run complete walkthrough", "walkthrough"),
            ChoiceOption.of("[img=" + SCROLL_ICON + "] Speaker text and images", "rich-dialog"),
            ChoiceOption.of("Large image in text dialog", "text-image"),
            ChoiceOption.of("Multiple answer buttons", "choice-dialog"),
            ChoiceOption.of("Open fullscreen slide", "fullscreen-image"),
            ChoiceOption.of("Close menu", "close"));

    DialogFactory.showMultipleChoiceDialog(
        speakerLine("Mentor", PRESENTER_PORTRAIT, "[tr speed=1.2]What should we present next?"),
        "Presentation options",
        new ArrayList<>(options),
        false,
        payload -> handleMainChoice(payload, who),
        () -> {},
        who.id());
  }

  private void handleMainChoice(DialogResponseMessage.Payload payload, Entity who) {
    switch (stringValue(payload)) {
      case "walkthrough" -> runWalkthrough(who);
      case "rich-dialog" -> showRichDialog(who);
      case "text-image" -> showLargeTextImage(who);
      case "choice-dialog" -> showAnswerOptions(who);
      case "fullscreen-image" -> showFullscreenImage(who);
      default ->
          DialogFactory.showDialogDialog(
              speakerLine("Mentor", PRESENTER_PORTRAIT, "Come back any time for the demo menu."),
              () -> {},
              who.id());
    }
  }

  private void runWalkthrough(Entity who) {
    DialogFactory.showDialogDialog(
        speakerLine(
                "Mentor",
                PRESENTER_PORTRAIT,
                "[tr speed=1.0]First: an NPC dialogue page. The left column is the speaker portrait"
                    + " and name. Text advances page by page.")
            + "[p]"
            + speakerLine(
                "You", PLAYER_PORTRAIT, "So this is suitable for a talk during a presentation.")
            + "[p]"
            + speakerLine(
                "Mentor",
                PRESENTER_PORTRAIT,
                "Yes. Next I will show rich text with inline and block images."),
        () -> showRichDialog(who),
        who.id());
  }

  private void showRichDialog(Entity who) {
    DialogFactory.showDialogDialog(
        speakerLine(
                "Mentor",
                PRESENTER_PORTRAIT,
                "[tr speed=1.2]RichLabel tags work inside dialogue text: inline icons "
                    + "[img="
                    + DETAIL_IMAGE
                    + "], color [color=#1f77d0]highlights[/color], [shake strength=0.5]emphasis[/shake],"
                    + " and explicit line breaks.[n]The next page uses a block image.")
            + "[p]"
            + speakerLine(
                "Mentor",
                PRESENTER_PORTRAIT,
                "[tr speed=0][align=center]A slide-like image inside the dialogue box:[n]"
                    + "[img-block path="
                    + BOOK_IMAGE
                    + " width=80%]"),
        () -> showAnswerOptions(who),
        who.id());
  }

  private void showAnswerOptions(Entity who) {
    List<ChoiceOption> options =
        List.of(
            ChoiceOption.of("Ask about text pages", "text"),
            ChoiceOption.of("[img=" + DETAIL_IMAGE + "] Ask about images", "images"),
            ChoiceOption.of("Show the fullscreen slide", "fullscreen"),
            ChoiceOption.of("End the walkthrough", "end"));

    DialogFactory.showMultipleChoiceDialog(
        speakerLine(
            "Mentor",
            PRESENTER_PORTRAIT,
            "[tr speed=1.2]Now choose what your player character says. Every button has a label and"
                + " a stable value for the callback."),
        "What do you say?",
        new ArrayList<>(options),
        false,
        payload -> handleAnswerChoice(payload, who),
        () -> {},
        who.id());
  }

  private void showLargeTextImage(Entity who) {
    DialogFactory.showTextDialog(
        "[align=center][size=26]TextDialog image sizing[n]"
            + "[size=20]Block images can now use the full configured content width.[n]"
            + "[img-block path="
            + BOOK_IMAGE
            + " width=100%][n]"
            + "[align=left]For inline images, use the named tag form with scale:"
            + " [img path="
            + DETAIL_IMAGE
            + " scale=2.5]",
        "Large image in TextDialog",
        () -> showMainMenu(who),
        "Back",
        860f,
        null,
        who.id());
  }

  private void handleAnswerChoice(DialogResponseMessage.Payload payload, Entity who) {
    switch (stringValue(payload)) {
      case "text" ->
          DialogFactory.showDialogDialog(
              speakerLine("You", PLAYER_PORTRAIT, "How many pages can one NPC dialogue contain?")
                  + "[p]"
                  + speakerLine(
                      "Mentor",
                      PRESENTER_PORTRAIT,
                      "As many as the script needs. Separate them with [p], and optionally change"
                          + " the speaker at each page."),
              () -> showMainMenu(who),
              who.id());
      case "images" ->
          DialogFactory.showDialogDialog(
              speakerLine(
                  "Mentor",
                  PRESENTER_PORTRAIT,
                  "Use [img=path] for inline images and [img-block path=path width=80%] for a"
                      + " larger image inside the dialog."),
              () -> showMainMenu(who),
              who.id());
      case "fullscreen" -> showFullscreenImage(who);
      default ->
          DialogFactory.showDialogDialog(
              speakerLine("Mentor", PRESENTER_PORTRAIT, "That is the current dialogue toolset."),
              () -> {},
              who.id());
    }
  }

  private void showFullscreenImage(Entity who) {
    DialogUtils.showImagePopUp(BOOK_IMAGE, TransitionSpeed.DISABLED, 1.0f, () -> {}, who.id());
  }

  private static String stringValue(DialogResponseMessage.Payload payload) {
    return payload instanceof DialogResponseMessage.StringValue(String value) ? value : "";
  }

  private static String speakerLine(String name, String imagePath, String text) {
    return "[speaker img=" + imagePath + " name=\"" + name + "\"]" + text;
  }
}
