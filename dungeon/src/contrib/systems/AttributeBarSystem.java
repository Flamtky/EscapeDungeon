package contrib.systems;

import com.badlogic.gdx.scenes.scene2d.ui.ProgressBar;
import contrib.components.BarDisplayable;
import contrib.utils.AttributeBarUtil;
import core.Entity;
import core.System;
import core.components.DrawComponent;
import core.components.PositionComponent;
import core.utils.logging.DungeonLogger;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A system that displays attribute bars above entities.
 *
 * <p>Entities with {@link BarDisplayable}, {@link PositionComponent}, and {@link DrawComponent}
 * will have progress bars rendered above them, showing their current attribute values relative to
 * the maximum. Bars are stacked automatically with configurable gaps, ordered by priority (lower
 * priority = closer to entity).
 *
 * <p>Bars are automatically created when entities are added to the system and removed when entities
 * are removed. The bars are updated each frame to reflect the current attribute values of the
 * entity. entity.
 */
public final class AttributeBarSystem extends System {

  private static final DungeonLogger LOGGER = DungeonLogger.getLogger(AttributeBarSystem.class);

  /**
   * Cached bar data for each entity. Maps entity ID to list of bar entries. Each entry holds the
   * BarDisplayable component reference, its ProgressBar, and vertical offset.
   */
  private final Map<Integer, List<BarEntry>> barCache = new HashMap<>();

  /**
   * Creates a new {@code AttributeBarSystem}.
   *
   * <p>Registers listeners for entity addition and removal. When an entity with the required
   * components is added, attribute bars are created and cached. When the entity is removed, the
   * corresponding bars are removed.
   */
  public AttributeBarSystem() {
    super(AuthoritativeSide.CLIENT, DrawComponent.class, PositionComponent.class);

    this.onEntityRemove =
        entity -> {
          List<BarEntry> entries = barCache.remove(entity.id());
          if (entries != null) {
            entries.forEach(entry -> entry.progressBar().remove());
          }
        };

    this.onEntityAdd =
        entity -> {
          List<BarDisplayable> bars =
              entity
                  .componentStream()
                  .filter(BarDisplayable.class::isInstance)
                  .map(BarDisplayable.class::cast)
                  .sorted(Comparator.comparingInt(BarDisplayable::barPriority))
                  .toList();

          if (bars.isEmpty()) {
            return;
          }

          List<BarEntry> entries = new ArrayList<>();
          Map<Class<? extends BarDisplayable>, ProgressBar> tempMapping = new HashMap<>();

          for (BarDisplayable bar : bars) {
            int priority = bar.barPriority();
            float verticalOffset = priority * AttributeBarUtil.BAR_GAP;
            AttributeBarUtil.addBarToEntity(entity, bar, tempMapping, verticalOffset);
            ProgressBar progressBar = tempMapping.get(bar.getClass());
            if (progressBar != null) {
              entries.add(new BarEntry(bar, progressBar, verticalOffset));
              LOGGER.debug("Added {} bar for entity {}", bar.barStyleName(), entity.id());
            }
          }

          if (!entries.isEmpty()) {
            barCache.put(entity.id(), entries);
          }
        };
  }

  /**
   * Updates all attribute bars for the entities managed by this system.
   *
   * <p>Each cached {@link BarDisplayable} component is queried for current and maximum values, and
   * the corresponding progress bars are updated accordingly.
   */
  @Override
  public void execute() {
    filteredEntityStream().forEach(this::updateBarsForEntity);
  }

  private void updateBarsForEntity(Entity entity) {
    List<BarEntry> entries = barCache.get(entity.id());
    if (entries == null || entries.isEmpty()) {
      return;
    }

    PositionComponent pc = entity.fetch(PositionComponent.class).orElse(null);
    if (pc == null) {
      return;
    }

    boolean isVisible =
        entity.fetch(DrawComponent.class).map(DrawComponent::isVisible).orElse(false);

    for (BarEntry entry : entries) {
      BarDisplayable bar = entry.barDisplayable();
      ProgressBar progressBar = entry.progressBar();

      // Update visibility: only show if entity is visible and bar is not at max
      progressBar.setVisible(isVisible && bar.current() != bar.max());

      // Update position
      AttributeBarUtil.updatePosition(progressBar, pc, entry.verticalOffset());

      // Update value
      progressBar.setValue(bar.current() / bar.max());
    }
  }

  /**
   * Cached entry for a single attribute bar.
   *
   * @param barDisplayable the component providing bar data (live reference)
   * @param progressBar the UI progress bar widget
   * @param verticalOffset the vertical offset for stacking
   */
  private record BarEntry(
      BarDisplayable barDisplayable, ProgressBar progressBar, float verticalOffset) {}

  /** AttributeBarSystem can't be paused. */
  @Override
  public void stop() {
    run = true;
  }
}
