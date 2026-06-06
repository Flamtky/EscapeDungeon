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

final class ReplayTargetOverlay extends Table implements IResizable {
  private static final float TOP_PAD = 14f;
  private static final int FONT_SIZE = 13;
  private final Label label;

  ReplayTargetOverlay() {
    Skin skin = UIUtils.defaultSkin();
    setTouchable(Touchable.disabled);
    setBackground(skin.getDrawable("round-dark-gray"));
    label = Scene2dElementFactory.createLabel("", FONT_SIZE);
    label.setAlignment(Align.center);
    add(label).pad(7, 12, 7, 12);
    pack();
    setVisible(false);
  }

  void target(String playerId) {
    label.setText("Target: " + playerId);
    pack();
    setVisible(true);
    if (getStage() != null) {
      onResize((int) getStage().getWidth(), (int) getStage().getHeight());
    }
  }

  void clearTarget() {
    setVisible(false);
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
    setPosition((width - getWidth()) / 2f, height - getHeight() - TOP_PAD);
  }
}
