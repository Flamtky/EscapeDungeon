package demoDungeon.level;

import contrib.systems.EventScheduler;
import core.Game;
import core.components.PositionComponent;
import core.level.DungeonLevel;
import core.level.utils.DesignLabel;
import core.level.utils.LevelElement;
import core.systems.DrawSystem;
import core.utils.*;
import java.util.*;
import mushRoom.shaders.TorchPostProcessing;

/** The MADungeonRoom level for the multiplayer-client. */
public class MADungeonRoomClient extends DungeonLevel {

  private boolean dimed = false;
  private TorchPostProcessing torchShader;

  /**
   * Creates a new Demo Level.
   *
   * @param layout The layout of the level.
   * @param designLabel The design label of the level.
   * @param namedPoints The custom points of the level.
   */
  public MADungeonRoomClient(
      LevelElement[][] layout, DesignLabel designLabel, Map<String, Point> namedPoints) {
    super(layout, designLabel, namedPoints, "MARoom");
    Game.system(
        DrawSystem.class,
        (ds) -> {
          // Create global torch shader once
          torchShader = (TorchPostProcessing) new TorchPostProcessing().upscaling(4);
          torchShader.addArea(new Rectangle(getPoint("grass11"), getPoint("grass12")));
          torchShader.addArea(new Rectangle(getPoint("temple11"), getPoint("temple12")));
          torchShader.addArea(new Rectangle(getPoint("fire11"), getPoint("fire12")));
          torchShader.addArea(new Rectangle(getPoint("forest11"), getPoint("forest12")));
          torchShader.addArea(new Rectangle(getPoint("forest21"), getPoint("forest22")));
          torchShader.addArea(new Rectangle(getPoint("forest21"), getPoint("forest23")));
          torchShader.addArea(new Rectangle(getPoint("forest23"), getPoint("forest24")));
          torchShader.addArea(new Rectangle(getPoint("forest25"), getPoint("forest24")));
          torchShader.addArea(new Rectangle(getPoint("poi11"), getPoint("poi12")));
          torchShader.addArea(new Rectangle(getPoint("poi21"), getPoint("poi22")));
          torchShader.addArea(new Rectangle(getPoint("poi31"), getPoint("poi32")));
          ds.sceneShaders().remove("torches");
          ds.sceneShaders().add("torches", torchShader);
        });
  }

  @Override
  protected void onTick() {
    updateTorchShader();
  }

  private final Set<Integer> initLightEntityIds = new HashSet<>();

  private void updateTorchShader() {
    Game.levelEntities()
        .filter(e -> !initLightEntityIds.contains(e.id()))
        .filter(e -> (e.name().contains("Torch") || e.name().contains("Firebox")))
        .forEach(
            e -> {
              float radius = e.name().contains("Torch") ? 5.0f : 7.0f;
              PositionComponent pos = e.fetch(PositionComponent.class).orElseThrow();
              torchShader.addLight(
                  new TorchPostProcessing.Light(
                      pos.position().x() + 0.5f, pos.position().y(), radius));
              initLightEntityIds.add(e.id());
            });

    torchShader
        .lights()
        .forEach(
            light -> {
              if (Game.entityAtPoint(new Point(light.x, light.y)).findAny().isEmpty())
                torchShader.removeLight(light);
            });

    Game.player()
        .map(player -> player.fetch(PositionComponent.class))
        .filter(Optional::isPresent)
        .map(Optional::get)
        .ifPresent(
            pc -> {
              Rectangle labyrinth1 =
                  new Rectangle(getPoint("labyrinth11"), getPoint("labyrinth12"));
              Rectangle labyrinth2 =
                  new Rectangle(getPoint("labyrinth21"), getPoint("labyrinth22"));
              Rectangle labyrinth3 = new Rectangle(getPoint("labyrinth22"), getPoint("fire12"));
              if (labyrinth1.contains(pc.position())
                  || labyrinth2.contains(pc.position())
                  || labyrinth3.contains(pc.position())) {
                if (!dimed) {
                  dimed = true;
                  for (int i = 1; i <= 15; i++) {
                    float dimness = 0.15f - (i * 0.01f);
                    EventScheduler.scheduleAction(
                        () -> {
                          torchShader.baseDimness(dimness);
                        },
                        i * 100);
                  }
                }
              } else {
                if (dimed) {
                  dimed = false;
                  for (int i = 1; i <= 15; i++) {
                    float dimness = 0f + (i * 0.01f);
                    EventScheduler.scheduleAction(
                        () -> {
                          torchShader.baseDimness(dimness);
                        },
                        i * 100);
                  }
                }
              }
            });
  }
}
