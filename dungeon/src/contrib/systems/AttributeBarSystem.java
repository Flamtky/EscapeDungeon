package contrib.systems;

import static contrib.hud.UIUtils.defaultSkin;

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
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * A system that displays attribute bars above entities.
 *
 * <p>Entities with {@link BarDisplayable}, {@link PositionComponent}, and {@link DrawComponent}
 * will have progress bars rendered above them, showing their current attribute values relative to
 * the maximum. Bars are stacked automatically with configurable gaps, ordered by priority (lower
 * priority = closer to entity).
 *
 * <p>The system dynamically fetches all {@link BarDisplayable} components each frame, creating new
 * bars for new components and removing bars for removed components. Created progress bars are
 * cached for performance.
 */
public final class AttributeBarSystem extends System {

  private static final DungeonLogger LOGGER = DungeonLogger.getLogger(AttributeBarSystem.class);

  /**
   * Cached bar data for each entity. Maps entity ID to list of bar entries. Each entry holds the
   * BarDisplayable component reference, its ProgressBar, and vertical offset.
   */
  private final Map<Integer, List<BarEntry>> barCache = new HashMap<>();

  static {
    defaultSkin(); // ensure skin is loaded
  }

  /**
   * Creates a new {@code AttributeBarSystem}.
   *
   * <p>Initializes the system to process entities with {@link DrawComponent} and {@link
   * PositionComponent}. The system dynamically fetches components each frame.
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
  }

  /**
   * Updates all attribute bars for the entities managed by this system.
   *
   * <p>Dynamically fetches all {@link BarDisplayable} components for each entity, creating new bars
   * for new components and removing bars for components that no longer exist. Updates cached bar
   * values each frame.
   */
  @Override
  public void execute() {
    filteredEntityStream().parallel().forEach(this::updateBarsForEntity);
  }

  private void updateBarsForEntity(Entity entity) {
    // Fetch all current BarDisplayable components, sorted by priority
    List<BarDisplayable> currentBars =
        entity
            .componentStream()
            .filter(BarDisplayable.class::isInstance)
            .map(BarDisplayable.class::cast)
            .sorted(Comparator.comparingInt(BarDisplayable::barPriority))
            .toList();

    List<BarEntry> cachedEntries = barCache.getOrDefault(entity.id(), new ArrayList<>());

    // Find which components are new and which have been removed
    Set<Class<?>> cachedTypes = new HashSet<>();
    for (BarEntry entry : cachedEntries) {
      cachedTypes.add(entry.barDisplayable().getClass());
    }

    Set<Class<?>> currentTypes = new HashSet<>();
    for (BarDisplayable bar : currentBars) {
      currentTypes.add(bar.getClass());
    }

    // Remove bars for components that no longer exist
    cachedEntries.removeIf(
        entry -> {
          if (!currentTypes.contains(entry.barDisplayable().getClass())) {
            entry.progressBar().remove();
            LOGGER.debug(
                "Removed {} bar for entity {}", entry.barDisplayable().barStyleName(), entity.id());
            return true;
          }
          return false;
        });

    // Add bars for new components
    Map<Class<? extends BarDisplayable>, ProgressBar> tempMapping = new HashMap<>();
    for (BarDisplayable bar : currentBars) {
      if (!cachedTypes.contains(bar.getClass())) {
        int priority = bar.barPriority();
        float verticalOffset = priority * AttributeBarUtil.BAR_GAP;
        AttributeBarUtil.addBarToEntity(entity, bar, tempMapping, verticalOffset);
        ProgressBar progressBar = tempMapping.get(bar.getClass());
        if (progressBar != null) {
          cachedEntries.add(new BarEntry(bar, progressBar, verticalOffset));
          LOGGER.debug("Added {} bar for entity {}", bar.barStyleName(), entity.id());
        }
      }
    }

    if (!cachedEntries.isEmpty()) {
      barCache.put(entity.id(), cachedEntries);
    } else {
      barCache.remove(entity.id());
    }

    // Update all remaining bars
    boolean isVisible =
        entity.fetch(DrawComponent.class).map(DrawComponent::isVisible).orElse(false);

    for (BarEntry entry : cachedEntries) {
      BarDisplayable bar = entry.barDisplayable();
      ProgressBar progressBar = entry.progressBar();

      // Update visibility: only show if entity is visible and bar is not at max
      progressBar.setVisible(isVisible && bar.current() != bar.max());

      // Update position
      AttributeBarUtil.updatePosition(
          progressBar, AttributeBarUtil.getBarOriginForEntity(entity, entry.verticalOffset));

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
