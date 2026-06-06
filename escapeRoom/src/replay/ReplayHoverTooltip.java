package replay;

import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import contrib.hud.UIUtils;

final class ReplayHoverTooltip extends Table {
  private static final float OFFSET = 18f;
  private final Label label;

  ReplayHoverTooltip() {
    Skin skin = UIUtils.defaultSkin();
    setTouchable(Touchable.disabled);
    setBackground(skin.getDrawable("round-dark-gray"));
    label = new Label("", skin, "blank-white");
    label.setFontScale(0.48f);
    label.setWrap(false);
    add(label).pad(10, 12, 10, 12);
    pack();
    setVisible(false);
  }

  void show(String text, float stageX, float stageY, float stageWidth, float stageHeight) {
    label.setText(text);
    pack();
    float x = Math.min(stageX + OFFSET, stageWidth - getWidth() - OFFSET);
    float y = Math.min(stageY + OFFSET, stageHeight - getHeight() - OFFSET);
    setPosition(Math.max(OFFSET, x), Math.max(OFFSET, y));
    setVisible(true);
  }

  void hide() {
    setVisible(false);
  }
}
