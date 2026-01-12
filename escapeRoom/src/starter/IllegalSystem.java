package starter;

import com.badlogic.gdx.graphics.Color;
import core.Entity;
import core.Game;
import core.System;
import core.components.DrawComponent;
import core.components.PlayerComponent;
import core.level.Tile;
import core.level.utils.DesignLabel;
import core.utils.components.draw.shader.OutlineShader;

/**
 * System that checks if players are in legal areas of the escape room prison game.
 *
 * <p>This system updates the IllegalComponent of players based on their current location. If a
 * player is found to be in an illegal area, they are marked as illegal for trespassing.
 *
 * @see guard.GuardDetectionSystem
 */
public class IllegalSystem extends System {

  private static final DesignLabel[] LEGAL_DESIGN_FLOORS = {
    DesignLabel.FOREST, DesignLabel.BEIGECASTLE
  };

  private static final String ILLEGAL_SHADER_KEY = "illegalGlow";
  private static final Color ILLEGAL_OUTLINE_COLOR = Color.RED;
  private static final int ILLEGAL_OUTLINE_WIDTH = 1;

  /** Constructs an IllegalSystem that processes PlayerComponent entities. */
  public IllegalSystem() {
    super(PlayerComponent.class);
  }

  @Override
  public void execute() {
    filteredEntityStream().forEach(this::handlePlayer);
  }

  @Override
  public void render(float delta) {
    filteredEntityStream().forEach(IllegalSystem::outlineIfIllegal);
  }

  private void handlePlayer(final Entity player) {
    Tile playerTile = Game.tileAtEntity(player).orElse(null);
    if (playerTile == null) return;

    IllegalComponent illegalComponent =
        player
            .fetch(IllegalComponent.class)
            .orElseGet(
                () -> {
                  IllegalComponent ic = new IllegalComponent();
                  player.add(ic);
                  return ic;
                });
    if (!isLegalFloor(playerTile)) {
      illegalComponent.addReason(IllegalComponent.Reason.TRESPASSING);
    } else {
      illegalComponent.removeReason(IllegalComponent.Reason.TRESPASSING);
    }
  }

  private static void outlineIfIllegal(Entity player) {
    if (!Game.isHeadless()) { // won't work in mp
      player
          .fetch(DrawComponent.class)
          .ifPresent(
              drawComponent -> {
                player
                    .fetch(IllegalComponent.class)
                    .ifPresent(
                        ic -> {
                          if (ic.isIllegal()) {
                            drawComponent
                                .shaders()
                                .add(
                                    ILLEGAL_SHADER_KEY,
                                    new OutlineShader(
                                        ILLEGAL_OUTLINE_WIDTH, ILLEGAL_OUTLINE_COLOR));
                          } else {
                            drawComponent.shaders().remove(ILLEGAL_SHADER_KEY);
                          }
                        });
              });
    }
  }

  private boolean isLegalFloor(final Tile tile) {
    for (DesignLabel legalFloor : LEGAL_DESIGN_FLOORS) {
      if (tile.designLabel() == legalFloor) {
        return true;
      }
    }
    return false;
  }
}
