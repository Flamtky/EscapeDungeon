package replay;

import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.SelectBox;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Slider;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import contrib.hud.UIUtils;
import core.utils.BaseContainerUI;
import core.utils.Scene2dElementFactory;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.DoubleConsumer;
import java.util.function.LongConsumer;

final class ReplayControlOverlay extends BaseContainerUI {
  static final String SELECT_FILE_ACTION = "__select_replay_file__";

  private static final float CONTROL_HEIGHT = 104f;
  private static final int CONTROL_FONT_SIZE = 18;
  private static final long SKIP_MS = 5_000;
  private static final double[] SPEEDS = {0.1, 0.25, 0.5, 0.75, 1.0, 1.25, 1.5, 1.75, 2.0, 2.5};

  private final TextButton playPauseButton;
  private final TextButton backButton;
  private final TextButton forwardButton;
  private final TextButton zoomInButton;
  private final TextButton zoomOutButton;
  private final Label currentTimeLabel;
  private final Label durationLabel;
  private final SelectBox<ReplaySelection> replayPicker;
  private final Slider speedSlider;
  private final Label speedValueLabel;
  private final Slider timelineSlider;
  private final Label timeLabel;
  private final Label replayLabel;
  private final Label errorLabel;
  private final Consumer<String> replaySelected;
  private boolean updatingControls;
  private boolean wasPlayingBeforeScrub;

  ReplayControlOverlay(
      LongConsumer seekTo,
      Runnable togglePlayback,
      Runnable pausePlayback,
      Runnable playPlayback,
      LongConsumer skipBy,
      DoubleConsumer speedSelected,
      Runnable zoomIn,
      Runnable zoomOut,
      Consumer<String> replaySelected) {
    super(new Table(), true, false);
    this.replaySelected = replaySelected;
    setTouchable(Touchable.childrenOnly);

    Skin skin = UIUtils.defaultSkin();
    Table root = (Table) getContent();
    root.setFillParent(true);
    root.bottom();
    root.setTouchable(Touchable.childrenOnly);

    Table controls = new Table(skin);
    controls.setTouchable(Touchable.enabled);
    controls.setBackground(skin.getDrawable("round-dark-gray"));
    controls.pad(8, 10, 8, 10);
    controls.defaults().pad(3);

    replayPicker = new SelectBox<>(skin, "small");
    replayPicker.addListener(
        new ChangeListener() {
          @Override
          public void changed(ChangeEvent event, com.badlogic.gdx.scenes.scene2d.Actor actor) {
            if (!updatingControls) {
              ReplaySelection selection = replayPicker.getSelected();
              if (selection.fileSelector()) {
                ReplayControlOverlay.this.replaySelected.accept(SELECT_FILE_ACTION);
              } else {
                ReplayControlOverlay.this.replaySelected.accept(selection.assetPath());
              }
            }
          }
        });
    speedSlider = new Slider(0, SPEEDS.length - 1, 1, false, skin, "clean-horizontal");
    speedSlider.addListener(
        new ChangeListener() {
          @Override
          public void changed(ChangeEvent event, com.badlogic.gdx.scenes.scene2d.Actor actor) {
            if (!updatingControls) {
              speedSelected.accept(speedForSlider(speedSlider.getValue()));
            }
          }
        });

    replayLabel = new Label("", skin, "blank-white");
    replayLabel.setFontScale(0.5f);
    errorLabel = new Label("", skin, "blank-white");
    errorLabel.setFontScale(0.42f);
    timeLabel = new Label("00:00 / 00:00", skin, "blank-white");
    timeLabel.setFontScale(0.62f);
    currentTimeLabel = new Label("00:00", skin, "blank-white");
    currentTimeLabel.setFontScale(0.54f);
    durationLabel = new Label("00:00", skin, "blank-white");
    durationLabel.setFontScale(0.54f);
    speedValueLabel = new Label("1x", skin, "blank-white");
    speedValueLabel.setFontScale(0.54f);

    backButton = button("-5s", "clean-blue-outline", () -> skipBy.accept(-SKIP_MS));
    playPauseButton = button("Pause", "clean-green", togglePlayback);
    forwardButton = button("+5s", "clean-blue-outline", () -> skipBy.accept(SKIP_MS));
    zoomInButton = button("Zoom In", "clean-blue-outline", zoomIn);
    zoomOutButton = button("Zoom Out", "clean-blue-outline", zoomOut);

    timelineSlider = new Slider(0, 1, 1, false, skin, "clean-horizontal");
    timelineSlider.addListener(
        new InputListener() {
          @Override
          public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
            wasPlayingBeforeScrub = playPauseButton.getText().toString().equals("Pause");
            if (wasPlayingBeforeScrub) {
              pausePlayback.run();
            }
            return true;
          }

          @Override
          public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
            if (wasPlayingBeforeScrub && timelineSlider.getValue() < timelineSlider.getMaxValue()) {
              playPlayback.run();
            }
          }
        });
    timelineSlider.addListener(
        new ChangeListener() {
          @Override
          public void changed(ChangeEvent event, com.badlogic.gdx.scenes.scene2d.Actor actor) {
            if (!updatingControls) {
              seekTo.accept(Math.round(timelineSlider.getValue()));
            }
          }
        });

    Table replayGroup = new Table();
    replayGroup.defaults().padRight(6);
    replayGroup.add(replayPicker).width(285).height(34);
    replayGroup.add(replayLabel).left().width(125);

    Table speedGroup = new Table();
    speedGroup.defaults().padRight(6);
    Label speedLabel = new Label("Speed", skin, "blank-white");
    speedLabel.setFontScale(0.48f);
    speedGroup.add(speedLabel).width(48).right();
    speedGroup.add(speedSlider).width(230).height(30);
    speedGroup.add(speedValueLabel).width(42).left();

    Table transportGroup = new Table();
    transportGroup.defaults().padRight(4);
    transportGroup.add(backButton).width(78).height(34);
    transportGroup.add(playPauseButton).width(106).height(34);
    transportGroup.add(forwardButton).width(78).height(34);

    Table viewGroup = new Table();
    viewGroup.defaults().padRight(4);
    viewGroup.add(zoomInButton).width(100).height(34);
    viewGroup.add(zoomOutButton).width(108).height(34);

    Table timelineGroup = new Table();
    timelineGroup.defaults().padRight(8);
    timelineGroup.add(currentTimeLabel).width(54).right();
    timelineGroup.add(timelineSlider).expandX().fillX().height(32);
    timelineGroup.add(durationLabel).width(54).left();

    controls.add(replayGroup).left().width(430);
    controls.add(timeLabel).center().width(150);
    controls.add(transportGroup).center().width(280);
    controls.add(viewGroup).right().width(220).row();
    controls.add(speedGroup).left().width(430).height(34);
    controls.add(timelineGroup).colspan(3).expandX().fillX().height(34);
    controls.row();
    controls.add(errorLabel).colspan(4).left().expandX().fillX().height(14);

    root.add(controls).growX().height(CONTROL_HEIGHT);
  }

  void updateState(ReplayCutscene cutscene) {
    updatingControls = true;
    if (cutscene == null) {
      timelineSlider.setRange(0, 1);
      timelineSlider.setValue(0);
      playPauseButton.setText("Play");
      speedSlider.setValue(speedIndex(defaultSpeed()));
      speedValueLabel.setText(formatSpeed(defaultSpeed()));
      timeLabel.setText("00:00 / 00:00");
      currentTimeLabel.setText("00:00");
      durationLabel.setText("00:00");
      replayLabel.setText("No replay");
      updatingControls = false;
      return;
    }

    long duration = Math.max(1, cutscene.durationMs());
    timelineSlider.setRange(0, duration);
    timelineSlider.setValue(Math.min(cutscene.currentTimeMs(), duration));
    playPauseButton.setText(cutscene.playing() ? "Pause" : "Play");
    double speed = nearestSpeed(cutscene.playbackSpeed());
    speedSlider.setValue(speedIndex(speed));
    speedValueLabel.setText(formatSpeed(speed));
    String currentTime = formatWholeTime(cutscene.currentTimeMs());
    String durationText = formatWholeTime(cutscene.durationMs());
    timeLabel.setText(currentTime + " / " + durationText);
    currentTimeLabel.setText(currentTime);
    durationLabel.setText(durationText);
    replayLabel.setText("Players: " + cutscene.playerCount());
    updatingControls = false;
  }

  void replayAssets(List<String> replayAssets, String currentAssetPath) {
    updatingControls = true;
    List<ReplaySelection> selections = new ArrayList<>();
    selections.add(ReplaySelection.none());
    replayAssets.stream().map(ReplaySelection::asset).forEach(selections::add);
    selections.add(ReplaySelection.selectFile());
    replayPicker.setItems(selections.toArray(ReplaySelection[]::new));
    selectReplay(currentAssetPath);
    updatingControls = false;
  }

  void selectReplay(String assetPath) {
    updatingControls = true;
    if (assetPath == null || assetPath.isBlank()) {
      for (ReplaySelection selection : replayPicker.getItems()) {
        if (selection.noReplay()) {
          replayPicker.setSelected(selection);
          updatingControls = false;
          return;
        }
      }
    }
    for (ReplaySelection selection : replayPicker.getItems()) {
      if (!selection.fileSelector() && selection.assetPath().equals(assetPath)) {
        replayPicker.setSelected(selection);
        updatingControls = false;
        return;
      }
    }
    updatingControls = false;
  }

  void error(String error) {
    errorLabel.setText(error == null ? "" : error);
  }

  boolean containsStagePoint(float stageX, float stageY) {
    if (!isVisible()) {
      return false;
    }

    boolean inPlaybackControls =
        stageX >= 0 && stageX <= getWidth() && stageY >= 0 && stageY <= CONTROL_HEIGHT;
    return inPlaybackControls;
  }

  static double nextSpeed(double currentSpeed) {
    for (int i = 0; i < SPEEDS.length; i++) {
      if (Math.abs(SPEEDS[i] - currentSpeed) < 0.01) {
        return SPEEDS[Math.min(SPEEDS.length - 1, i + 1)];
      }
    }
    return 1.0;
  }

  static double previousSpeed(double currentSpeed) {
    for (int i = 0; i < SPEEDS.length; i++) {
      if (Math.abs(SPEEDS[i] - currentSpeed) < 0.01) {
        return SPEEDS[Math.max(0, i - 1)];
      }
    }
    return 1.0;
  }

  static double defaultSpeed() {
    return 1.0;
  }

  static String formatTime(long ms) {
    long totalSeconds = ms / 1_000;
    long minutes = totalSeconds / 60;
    long seconds = totalSeconds % 60;
    long millis = Math.floorMod(ms, 1_000);
    return String.format("%02d:%02d.%03d", minutes, seconds, millis);
  }

  static String formatWholeTime(long ms) {
    long totalSeconds = ms / 1_000;
    long minutes = totalSeconds / 60;
    long seconds = totalSeconds % 60;
    return String.format("%02d:%02d", minutes, seconds);
  }

  private static String formatSpeed(double speed) {
    if (Math.abs(speed - Math.rint(speed)) < 0.01) {
      return Math.round(speed) + "x";
    }
    return speed + "x";
  }

  private static double nearestSpeed(double speed) {
    double nearest = SPEEDS[0];
    for (double candidate : SPEEDS) {
      if (Math.abs(candidate - speed) < Math.abs(nearest - speed)) {
        nearest = candidate;
      }
    }
    return nearest;
  }

  private static double speedForSlider(float sliderValue) {
    int index = Math.round(sliderValue);
    return SPEEDS[Math.max(0, Math.min(SPEEDS.length - 1, index))];
  }

  private static int speedIndex(double speed) {
    double nearest = nearestSpeed(speed);
    for (int i = 0; i < SPEEDS.length; i++) {
      if (Math.abs(SPEEDS[i] - nearest) < 0.01) {
        return i;
      }
    }
    return 4;
  }

  private static TextButton button(String label, String style, Runnable action) {
    TextButton button = Scene2dElementFactory.createButton(label, style, CONTROL_FONT_SIZE);
    button.addListener(
        new ChangeListener() {
          @Override
          public void changed(ChangeEvent event, com.badlogic.gdx.scenes.scene2d.Actor actor) {
            action.run();
          }
        });
    return button;
  }

  private record ReplaySelection(String label, String assetPath, boolean fileSelector) {
    static ReplaySelection none() {
      return new ReplaySelection("No replay selected", "", false);
    }

    static ReplaySelection asset(String assetPath) {
      return new ReplaySelection(Path.of(assetPath).getFileName().toString(), assetPath, false);
    }

    static ReplaySelection selectFile() {
      return new ReplaySelection("Select File...", SELECT_FILE_ACTION, true);
    }

    boolean noReplay() {
      return assetPath.isBlank() && !fileSelector;
    }

    @Override
    public String toString() {
      return label;
    }
  }
}
