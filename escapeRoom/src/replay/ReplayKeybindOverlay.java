package replay;

import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.Align;
import contrib.hud.UIUtils;
import core.game.GameLoop;
import core.game.IResizable;
import core.utils.Scene2dElementFactory;

final class ReplayKeybindOverlay extends Table implements IResizable {
  private static final float LEFT_PAD = 12f;
  private static final float TOP_PAD = 12f;
  private static final int TITLE_FONT_SIZE = 13;
  private static final int ROW_FONT_SIZE = 12;
  private static final String[][] KEYBINDS = {
    {"Space", "Play / pause"},
    {"Left / J", "Back 5s"},
    {"Right / L", "Forward 5s"},
    {"-", "Speed down"},
    {"+", "Speed up"},
    {"1", "Reset speed"},
    {"WASD", "Move camera"},
    {"Wheel", "Zoom camera"},
    {"B", "Stamina bars"},
    {"H / Tab", "Hide / show UI"}
  };

  ReplayKeybindOverlay() {
    Skin skin = UIUtils.defaultSkin();
    setTouchable(Touchable.disabled);
    setBackground(skin.getDrawable("round-dark-gray"));
    defaults().left();
    Label title = Scene2dElementFactory.createLabel("Replay controls", TITLE_FONT_SIZE);
    title.setAlignment(Align.left);
    add(title).colspan(2).padTop(8).padLeft(12).padRight(12).padBottom(5).left().row();
    for (String[] keybind : KEYBINDS) {
      Label key = Scene2dElementFactory.createLabel(keybind[0], ROW_FONT_SIZE);
      key.setAlignment(Align.left);
      Label action = Scene2dElementFactory.createLabel(keybind[1], ROW_FONT_SIZE);
      action.setAlignment(Align.left);
      add(key).width(76).padLeft(12).padRight(10).padBottom(2).left();
      add(action).width(112).padRight(12).padBottom(2).left().row();
    }
    padBottom(8);
    pack();
  }

  @Override
  protected void setStage(com.badlogic.gdx.scenes.scene2d.Stage stage) {
    super.setStage(stage);
    if (stage == null) {
      GameLoop.removeResizable(this);
      return;
    }
    GameLoop.registerResizable(this);
    onResize((int) stage.getWidth(), (int) stage.getHeight());
  }

  @Override
  public void onResize(int width, int height) {
    pack();
    setPosition(width - getWidth() - LEFT_PAD, height - getHeight() - TOP_PAD);
  }
}
