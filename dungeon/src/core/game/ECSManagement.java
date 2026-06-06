package core.game;

import contrib.components.CollideComponent;
import contrib.systems.EventScheduler;
import contrib.systems.HudSystem;
import contrib.systems.LevelTickSystem;
import contrib.systems.PositionSync;
import core.Component;
import core.Entity;
import core.Game;
import core.System;
import core.components.DrawComponent;
import core.components.PlayerComponent;
import core.components.PositionComponent;
import core.components.VelocityComponent;
import core.level.elements.ILevel;
import core.level.utils.Coordinate;
import core.network.messages.s2c.EntityDespawnEvent;
import core.network.messages.s2c.EntitySpawnEvent;
import core.systems.DrawSystem;
import core.systems.LevelSystem;
import core.systems.SoundSystem;
import core.utils.EntityIdProvider;
import core.utils.EntitySystemMapper;
import core.utils.Point;
import core.utils.logging.DungeonLogger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Consumer;
import java.util.stream.Stream;

/**
 * The class responsible for managing the ECS (Entity-Component-System) in the game.
 *
 * <p>It stores the {@link System systems} and the {@link Entity entities}.
 *
 * <p>For Entity management use: {@link #add(Entity)}, {@link #remove(Entity)} or {@link
 * #removeAllEntities()}
 *
 * <p>For System management use: {@link #add(System)}, {@link #remove(Class)} or {@link
 * #removeAllSystems()}
 *
 * <p>Get access via: {@link #levelEntities()}, {@link #systems()}
 *
 * <p>All API methods can also be accessed via the {@link core.Game} class.
 */
public final class ECSManagement {
  private static final DungeonLogger LOGGER = DungeonLogger.getLogger(ECSManagement.class);
  private static final Map<Class<? extends System>, System> SYSTEMS = new LinkedHashMap<>();
  private static final Map<ILevel, Set<EntitySystemMapper>> LEVEL_STORAGE_MAP = new HashMap<>();
  private static Set<EntitySystemMapper> activeEntityStorage = new HashSet<>();

  /** Cache for entity lookups by ID. */
  private static final Map<Integer, Entity> ENTITY_ID_CACHE = new HashMap<>();

  /** Spatial cache: maps tile coordinates to entities at that tile. */
  private static final Map<Coordinate, Set<Entity>> TILE_ENTITY_CACHE = new HashMap<>();

  /** Reverse cache: maps entities to their cached tile coordinate. */
  private static final Map<Entity, Coordinate> ENTITY_TILE_CACHE = new HashMap<>();

  /** Lock for thread-safe access to the spatial cache. */
  private static final ReentrantReadWriteLock tileCacheLock = new ReentrantReadWriteLock();

  private static final Lock tileCacheReadLock = tileCacheLock.readLock();
  private static final Lock tileCacheWriteLock = tileCacheLock.writeLock();

  /** Cached reference to the primary entity mapper (empty filter rules). */
  private static EntitySystemMapper primaryEntityMapper;

  private static int currentTick = 0;

  /**
   * Essential systems that are always added to the game.
   *
   * <p>Essential systems are systems that are required for the game to function properly.
   */
  private static final System[] ESSENTIAL_SYSTEMS = {
    new LevelSystem(),
    new SoundSystem(),
    DrawSystem.getInstance(),
    new EventScheduler(),
    new LevelTickSystem(),
    new HudSystem()
  };

  /**
   * Set to true if a new level was loaded during the current tick. This flag is used to interrupt
   * system execution when a level change occurs.
   */
  private static boolean newLevelLoadedThisTick = false;

  static {
    LEVEL_STORAGE_MAP.put(null, activeEntityStorage);
    primaryEntityMapper = new EntitySystemMapper();
    activeEntityStorage.add(primaryEntityMapper);
    for (System system : ESSENTIAL_SYSTEMS) {
      ECSManagement.add(system);
    }
  }

  /**
   * Inform each {@link System} that the given Entity has changes on component bases.
   *
   * <p>If necessary, the {@link System}s will trigger {@link System#triggerOnAdd(Entity)} or {@link
   * System#triggerOnRemove(Entity)}.
   *
   * @param entity the entity that has changes in its Component Collection.
   */
  public static void informAboutChanges(Entity entity) {
    if (primaryEntityMapper == null) {
      // During initialization, fall back to checking all mappers
      boolean exists =
          activeEntityStorage.stream()
              .filter(f -> f.equals(Set.of()))
              .findFirst()
              .map(m -> m.anyMatch(e -> e.equals(entity)))
              .orElse(false);
      if (exists) {
        new ArrayList<>(activeEntityStorage).forEach(f -> f.update(entity));
        LOGGER.info(entity + " informed the Game about component changes.");
      }
      return;
    }
    if (primaryEntityMapper.anyMatch(e -> e.equals(entity))) {
      // Create a copy to avoid ConcurrentModificationException when update triggers add/remove
      new ArrayList<>(activeEntityStorage).forEach(f -> f.update(entity));
      LOGGER.info(entity + " informed the Game about component changes.");
    }
  }

  /**
   * The given entity will be added to the game.
   *
   * <p>If given entity has an id that is already used by another entity, an {@link
   * IllegalArgumentException} will be thrown.
   *
   * <p>For each {@link System}, it will be checked if the {@link System} will process this entity.
   *
   * <p>If necessary, the {@link System} will trigger {@link System#triggerOnAdd(Entity)} .
   *
   * @param entity the entity to add.
   * @return added entity for chaining
   * @throws IllegalArgumentException if an entity with the same id already exists in the game.
   */
  public static Entity add(Entity entity) {
    // Prevent duplicate IDs for different entity instances
    Entity cachedEntity = ENTITY_ID_CACHE.get(entity.id());
    if (cachedEntity != null && cachedEntity != entity) {
      throw new IllegalArgumentException(
          "An Entity with id " + entity.id() + " already exists in the game.");
    }

    // Ensure the provider knows about this id (idempotent).
    EntityIdProvider.ensureRegistered(entity.id());

    // Update cache
    ENTITY_ID_CACHE.put(entity.id(), entity);

    // Update spatial cache for tile-based lookups
    addToTileCache(entity);

    // Create a copy to avoid ConcurrentModificationException when triggerOnAdd adds more entities
    new ArrayList<>(activeEntityStorage).forEach(f -> f.add(entity));
    LOGGER.info(entity + " will be added to the Game.");

    try {
      if (PreRunConfiguration.multiplayerEnabled() && PreRunConfiguration.isNetworkServer()) {
        if (entity.isPresent(PositionComponent.class) && entity.isPresent(DrawComponent.class)) {
          Game.network().broadcast(new EntitySpawnEvent(entity), true);
        }
      }
    } catch (IllegalStateException e) {
      LOGGER.error("Failed to broadcast entity spawn for {}: {}", entity, e.getMessage());
      // Continue without broadcasting, for unit tests
    }

    return entity;
  }

  /**
   * The given entity will be removed from the game.
   *
   * <p>If necessary, the {@link System}s will trigger {@link System#triggerOnAdd(Entity)} .
   *
   * @param entity the entity to remove
   * @return removed entity for chaining
   */
  public static Entity remove(Entity entity) {
    // Create a copy to avoid ConcurrentModificationException when triggerOnRemove modifies entities
    new ArrayList<>(activeEntityStorage).forEach(f -> f.remove(entity));
    EntityIdProvider.unregister(entity.id());
    ENTITY_ID_CACHE.remove(entity.id());

    // Remove from spatial cache
    removeFromTileCache(entity);

    LOGGER.info(entity + " will be removed from the Game.");

    try {
      if (PreRunConfiguration.multiplayerEnabled() && PreRunConfiguration.isNetworkServer()) {
        Game.network()
            .broadcast(new EntityDespawnEvent(entity.id(), "Entity removed from game"), true);
      }
    } catch (IllegalStateException e) {
      LOGGER.error("Failed to broadcast entity despawn for {}: {}", entity, e.getMessage());
      // Continue without broadcasting, for unit tests
    }

    return entity;
  }

  /**
   * Create a new {@link EntitySystemMapper} with the given filter rules.
   *
   * <p>The {@link EntitySystemMapper} will be added to {@link #activeEntityStorage}.
   *
   * <p>All entities in the empty filter (basically every entity in the game) will be tried to add
   * with {@link EntitySystemMapper#add(Entity)}.
   *
   * <p>This function will not check if an {@link EntitySystemMapper} with the same rules already
   * exists. If an {@link EntitySystemMapper} exists, it will not be replaced, and the {@link
   * EntitySystemMapper} created in this function will be lost.
   *
   * @param filter Set of Component classes that define the filter rules.
   * @return the created {@link EntitySystemMapper}.
   */
  private static EntitySystemMapper createNewEntitySystemMapper(
      Set<Class<? extends Component>> filter) {
    EntitySystemMapper mapper = new EntitySystemMapper(filter);
    activeEntityStorage.add(mapper);
    levelEntities().forEach(mapper::add);
    return mapper;
  }

  /**
   * Add a {@link System} to the game.
   *
   * <p>If a System is added to the game, the {@link System#execute} method will be called every
   * frame.
   *
   * <p>Additionally, the system will be informed about all new, changed, and removed entities.
   *
   * <p>The game can only store one system of each system type.
   *
   * @param system the System to add
   * @return an optional that contains the previous existing system of the given system class, if
   *     one exists
   * @see System
   * @see Optional
   */
  public static Optional<System> add(final System system) {
    System currentSystem = SYSTEMS.get(system.getClass());
    SYSTEMS.put(system.getClass(), system);
    // add to existing filter or create new filter if no matching exists
    Optional<EntitySystemMapper> filter =
        activeEntityStorage.stream().filter(f -> f.equals(system.filterRules())).findFirst();
    filter.ifPresentOrElse(
        f -> f.add(system), () -> createNewEntitySystemMapper(system.filterRules()).add(system));
    LOGGER.info("A new {} was added to the game", system.getClass().getName());
    return Optional.ofNullable(currentSystem);
  }

  /**
   * Get the current active {@link EntitySystemMapper}.
   *
   * @return The currently active {@link EntitySystemMapper}
   */
  public static Map<ILevel, Set<EntitySystemMapper>> levelStorageMap() {
    return LEVEL_STORAGE_MAP;
  }

  /**
   * Set the current active {@link EntitySystemMapper}.
   *
   * @param entityStorage The new active {@link EntitySystemMapper}
   */
  public static void activeEntityStorage(final Set<EntitySystemMapper> entityStorage) {
    activeEntityStorage = entityStorage;
    // Update the primary entity mapper reference (mapper with empty filter rules)
    primaryEntityMapper =
        entityStorage.stream().filter(f -> f.equals(Set.of())).findFirst().orElse(null);
    // Rebuild entity ID cache for the new storage
    rebuildEntityIdCache();
    // Rebuild spatial tile cache for the new storage
    rebuildTileCache();
  }

  /** Rebuilds the entity ID cache from the current active entity storage. */
  private static void rebuildEntityIdCache() {
    ENTITY_ID_CACHE.clear();
    if (primaryEntityMapper != null) {
      primaryEntityMapper.forEach(e -> ENTITY_ID_CACHE.put(e.id(), e));
    }
  }

  /** Rebuilds the spatial tile cache from the current active entity storage. */
  private static void rebuildTileCache() {
    tileCacheWriteLock.lock();
    try {
      TILE_ENTITY_CACHE.clear();
      ENTITY_TILE_CACHE.clear();
      if (primaryEntityMapper != null) {
        primaryEntityMapper.forEach(ECSManagement::addToTileCacheInternal);
      }
    } finally {
      tileCacheWriteLock.unlock();
    }
  }

  /**
   * Adds an entity to the spatial tile cache.
   *
   * @param entity the entity to add
   */
  private static void addToTileCache(Entity entity) {
    tileCacheWriteLock.lock();
    try {
      addToTileCacheInternal(entity);
    } finally {
      tileCacheWriteLock.unlock();
    }
  }

  /**
   * Internal method to add an entity to the tile cache. Must be called with write lock held.
   *
   * @param entity the entity to add
   */
  private static void addToTileCacheInternal(Entity entity) {
    // Sync collider position before calculating tile coordinate
    PositionSync.syncPosition(entity);
    Coordinate coord = getEntityTileCoordinate(entity);
    if (coord == null) {
      return;
    }
    ENTITY_TILE_CACHE.put(entity, coord);
    TILE_ENTITY_CACHE.computeIfAbsent(coord, k -> new HashSet<>()).add(entity);
  }

  /**
   * Removes an entity from the spatial tile cache.
   *
   * @param entity the entity to remove
   */
  private static void removeFromTileCache(Entity entity) {
    tileCacheWriteLock.lock();
    try {
      Coordinate cachedCoord = ENTITY_TILE_CACHE.remove(entity);
      if (cachedCoord != null) {
        Set<Entity> entities = TILE_ENTITY_CACHE.get(cachedCoord);
        if (entities != null) {
          entities.remove(entity);
          if (entities.isEmpty()) {
            TILE_ENTITY_CACHE.remove(cachedCoord);
          }
        }
      }
    } finally {
      tileCacheWriteLock.unlock();
    }
  }

  /**
   * Gets the tile coordinate for an entity using center position calculation. Uses CollideComponent
   * center if available, otherwise DrawComponent center, otherwise raw position.
   *
   * @param entity the entity
   * @return the tile coordinate, or null if entity has no PositionComponent
   */
  private static Coordinate getEntityTileCoordinate(Entity entity) {
    Optional<PositionComponent> pcOpt = entity.fetch(PositionComponent.class);
    if (pcOpt.isEmpty()) {
      return null;
    }
    PositionComponent pc = pcOpt.get();
    Optional<CollideComponent> ccOpt = entity.fetch(CollideComponent.class);
    Optional<DrawComponent> dcOpt = entity.fetch(DrawComponent.class);

    Point position;
    if (ccOpt.isPresent()) {
      position = ccOpt.get().collider().absoluteCenter();
    } else if (dcOpt.isPresent()) {
      DrawComponent dc = dcOpt.get();
      position = pc.position().translate(dc.getWidth() / 2, dc.getHeight() / 2);
    } else {
      position = pc.position();
    }
    return position.toCoordinate();
  }

  /**
   * Gets all entities at the specified tile coordinate with lazy validation for moving entities.
   *
   * <p>Entities without {@link VelocityComponent} are trusted from cache. Entities with {@link
   * VelocityComponent} are validated on-demand and the cache is updated if their tile has changed.
   * This lazy validation approach avoids per-frame cache updates for moving entities.
   *
   * <p><b>Note:</b> For entities without {@link VelocityComponent} that are teleported or have
   * their position changed programmatically, call {@link #refreshEntityTileCache(Entity)} after the
   * position change to update the cache.
   *
   * @param coordinate the tile coordinate to query
   * @return stream of entities at the given tile
   */
  public static Stream<Entity> getEntitiesAtTile(Coordinate coordinate) {
    List<Entity> result = new ArrayList<>();
    List<Entity> toRevalidate = new ArrayList<>();

    // First pass: collect entities and identify those needing revalidation
    tileCacheReadLock.lock();
    try {
      Set<Entity> cachedEntities = TILE_ENTITY_CACHE.get(coordinate);
      if (cachedEntities == null || cachedEntities.isEmpty()) {
        return Stream.empty();
      }
      for (Entity entity : cachedEntities) {
        if (entity.isPresent(VelocityComponent.class)) {
          toRevalidate.add(entity);
        } else {
          result.add(entity);
        }
      }
    } finally {
      tileCacheReadLock.unlock();
    }

    // Second pass: revalidate moving entities (requires write lock if cache update needed)
    if (!toRevalidate.isEmpty()) {
      for (Entity entity : toRevalidate) {
        Coordinate currentCoord = getEntityTileCoordinate(entity);
        if (currentCoord != null && currentCoord.equals(coordinate)) {
          result.add(entity);
        } else {
          // Entity has moved, update cache
          updateEntityTileCache(entity, coordinate, currentCoord);
        }
      }
    }

    return result.stream();
  }

  /**
   * Updates the tile cache for an entity that has moved.
   *
   * @param entity the entity that moved
   * @param oldCoord the old tile coordinate
   * @param newCoord the new tile coordinate (can be null if entity no longer has position)
   */
  private static void updateEntityTileCache(
      Entity entity, Coordinate oldCoord, Coordinate newCoord) {
    tileCacheWriteLock.lock();
    try {
      // Remove from old tile
      Set<Entity> oldEntities = TILE_ENTITY_CACHE.get(oldCoord);
      if (oldEntities != null) {
        oldEntities.remove(entity);
        if (oldEntities.isEmpty()) {
          TILE_ENTITY_CACHE.remove(oldCoord);
        }
      }

      // Add to new tile
      if (newCoord != null) {
        ENTITY_TILE_CACHE.put(entity, newCoord);
        TILE_ENTITY_CACHE.computeIfAbsent(newCoord, k -> new HashSet<>()).add(entity);
      } else {
        ENTITY_TILE_CACHE.remove(entity);
      }
    } finally {
      tileCacheWriteLock.unlock();
    }
  }

  /**
   * Refreshes the tile cache for an entity after its position has changed.
   *
   * <p>This method compares the cached tile coordinate with the current tile coordinate and updates
   * the cache if they differ.
   *
   * <p><b>Note:</b> This method is automatically called by {@link
   * contrib.systems.PositionSync#syncPosition} for entities without a {@link VelocityComponent}.
   * Entities with {@link VelocityComponent} are lazily revalidated when {@link
   * #getEntitiesAtTile(Coordinate)} is called. You typically don't need to call this method
   * directly unless you're updating position without going through {@code PositionSync}.
   *
   * @param entity the entity whose tile cache should be refreshed
   */
  public static void refreshEntityTileCache(Entity entity) {
    tileCacheReadLock.lock();
    Coordinate oldCoord;
    try {
      oldCoord = ENTITY_TILE_CACHE.get(entity);
    } finally {
      tileCacheReadLock.unlock();
    }

    Coordinate newCoord = getEntityTileCoordinate(entity);

    // Only update if coordinates changed
    if (oldCoord == null && newCoord != null) {
      // Entity wasn't in cache, add it
      tileCacheWriteLock.lock();
      try {
        ENTITY_TILE_CACHE.put(entity, newCoord);
        TILE_ENTITY_CACHE.computeIfAbsent(newCoord, k -> new HashSet<>()).add(entity);
      } finally {
        tileCacheWriteLock.unlock();
      }
    } else if (oldCoord != null && !oldCoord.equals(newCoord)) {
      // Entity moved to different tile
      updateEntityTileCache(entity, oldCoord, newCoord);
    }
  }

  /**
   * Get all Systems.
   *
   * @return a copy of the map that stores all registered {@link System} in the game.
   */
  public static Map<Class<? extends System>, System> systems() {
    return new LinkedHashMap<>(SYSTEMS);
  }

  /**
   * If a system instance of the specified type is present, performs the given action on it.
   *
   * @param <T> the type of the system, which must extend {@link System}
   * @param s the class object of the desired system type
   * @param c the {@link Consumer} to execute with the system instance if present
   */
  @SuppressWarnings("unchecked")
  public static <T extends System> void system(Class<T> s, Consumer<T> c) {
    if (SYSTEMS.containsKey(s)) {
      c.accept((T) SYSTEMS.get(s));
    } else {
      LOGGER.warn("Tried to access system of type {}, but it is not registered.", s.getName());
    }
  }

  /** Remove all registered systems from the game. */
  public static void removeAllSystems() {
    new HashSet<>(SYSTEMS.keySet()).forEach(ECSManagement::remove);
  }

  /**
   * Use this stream if you want to iterate over all entities in the current level.
   *
   * @return a stream of all entities currently in the level
   */
  public static Stream<Entity> levelEntities() {
    return levelEntities(Set.of());
  }

  /**
   * Use this stream if you want to iterate over all entities that contain the necessary Components
   * to be processed by the given system.
   *
   * @param system the system that processes the entities.
   * @return a stream of all entities currently in the game that should be processed by the given
   *     system.
   */
  public static Stream<Entity> levelEntities(final System system) {
    return levelEntities(system.filterRules());
  }

  /**
   * Use this stream if you want to iterate over all entities in the current level, that contain the
   * given components.
   *
   * @param filter Set of Component classes that define the filter rules.
   * @return a stream of all entities currently in the level, that contains the given components.
   */
  public static Stream<Entity> levelEntities(Set<Class<? extends Component>> filter) {
    // Fast path: use cached primary mapper for empty filter
    if (filter.isEmpty() && primaryEntityMapper != null) {
      return primaryEntityMapper.stream();
    }

    Optional<EntitySystemMapper> rf =
        activeEntityStorage.stream().filter(f -> f.equals(filter)).findFirst();

    if (rf.isEmpty()) {
      EntitySystemMapper newMapper = createNewEntitySystemMapper(filter);
      return newMapper.stream();
    } else {
      return rf.get().stream();
    }
  }

  /**
   * Searches the current level for the first local player character.
   *
   * <p>A player entity is defined as an entity that has a {@link PlayerComponent} with {@link
   * PlayerComponent#isLocal()} returning true.
   *
   * @return the local player character, can be empty if no local player is present.
   * @see PlayerComponent
   * @see #allPlayers()
   */
  public static Optional<Entity> player() {
    if (allPlayers().count() > 1
        && PreRunConfiguration.multiplayerEnabled()
        && PreRunConfiguration.isNetworkServer()) {
      LOGGER.warn("Multiple player entities detected in level; returning the first one found.");
    }
    return allPlayers()
        .filter(e -> e.fetch(PlayerComponent.class).map(PlayerComponent::isLocal).orElse(false))
        .findFirst();
  }

  /**
   * Searches the current level for all player characters.
   *
   * <p>A player entity is defined as an entity that has a {@link PlayerComponent}.
   *
   * <p>This includes both local and remote player characters.
   *
   * @return a stream of all player characters in the current level
   * @see PlayerComponent
   */
  public static Stream<Entity> allPlayers() {
    return levelEntities(Set.of(PlayerComponent.class));
  }

  /**
   * Remove the stored system of the given class from the game. If the System is successfully
   * removed, the {@link System#triggerOnRemove(Entity)} method of the System will be called for
   * each existing Entity that was associated with the removed System.
   *
   * @param system the class of the system to remove
   */
  public static void remove(final Class<? extends System> system) {
    System systemInstance = SYSTEMS.remove(system);
    if (systemInstance != null) activeEntityStorage.forEach(f -> f.remove(systemInstance));
  }

  /**
   * Remove all entities from the game.
   *
   * <p>This will also remove all entities from each system.
   */
  public static void removeAllEntities() {
    allEntities().forEach(ECSManagement::remove);
    LOGGER.info("All entities will be removed from the game.");
  }

  /**
   * Use this stream if you want to iterate over all entities in the game.
   *
   * <p>This will return <strong>all</strong> entities, not just those in the current level.
   *
   * <p>Use {@link #levelEntities()} instead if you only want the entities of the current level.
   *
   * @return a stream of all entities currently in the game
   */
  public static Stream<Entity> allEntities() {
    Set<Entity> allEntities = new HashSet<>();
    LEVEL_STORAGE_MAP
        .values()
        .forEach(
            entitySystemMappers ->
                entitySystemMappers.forEach(
                    entitySystemMapper -> entitySystemMapper.forEach(allEntities::add)));

    return allEntities.stream();
  }

  /**
   * Finds the entity that contains the given component instance.
   *
   * <p>This searches across all entities in the game, not just those in the current level.
   *
   * @param component the component instance whose owning entity should be located
   * @return an {@link Optional} containing the found entity, or an empty {@code Optional} if none
   *     is found
   */
  public static Optional<Entity> findInAll(final Component component) {
    return allEntities()
        .filter(entity -> entity.fetch(component.getClass()).map(component::equals).orElse(false))
        .findFirst();
  }

  /**
   * Finds the entity that contains the given component instance.
   *
   * <p>This searches across all entities in the current level.
   *
   * @param component the component instance whose owning entity should be located
   * @return an {@link Optional} containing the found entity, or an empty {@code Optional} if none
   *     is found
   */
  public static Optional<Entity> findInLevel(final Component component) {
    return levelEntities()
        .filter(entity -> entity.fetch(component.getClass()).map(component::equals).orElse(false))
        .findFirst();
  }

  /**
   * Tries to find the given entity in the game.
   *
   * <p>This searches across all entities in the game, not just those in the current level.
   *
   * @param entity the entity to search for
   * @return {@code true} if the entity is found, {@code false} otherwise
   */
  public static boolean existInAll(Entity entity) {
    return ENTITY_ID_CACHE.containsKey(entity.id())
        && ENTITY_ID_CACHE.get(entity.id()).equals(entity);
  }

  /**
   * Tries to find the given entity in the game.
   *
   * <p>This searches in the current level.
   *
   * @param entity the entity to search for
   * @return {@code true} if the entity is found, {@code false} otherwise
   */
  public static boolean existInLevel(Entity entity) {
    if (primaryEntityMapper == null) {
      return levelEntities().anyMatch(e -> e.equals(entity));
    }
    return primaryEntityMapper.anyMatch(e -> e.equals(entity));
  }

  private static boolean isAuthoritative(System.AuthoritativeSide side, System system) {
    System.AuthoritativeSide systemSide = system.authoritativeSide();
    return side == System.AuthoritativeSide.BOTH
        || systemSide == System.AuthoritativeSide.BOTH
        || systemSide == side;
  }

  /**
   * Returns the current tick number, incremented each time {@link
   * #executeOneTick(System.AuthoritativeSide)} is called.
   *
   * @return the current tick number
   */
  public static int currentTick() {
    return currentTick;
  }

  /**
   * Execute one game tick of the ECS.
   *
   * <p>This will call the {@link System#execute()} method of each registered {@link System} in the
   * game, if the system is running and the required number of ticks has passed since its last
   * execution.
   *
   * <p>If a new level was loaded during this tick, the execution will be interrupted to prevent
   * inconsistencies.
   *
   * @param side the authoritative side for which to execute systems ({@link
   *     System.AuthoritativeSide#BOTH for all systems})
   */
  public static void executeOneTick(System.AuthoritativeSide side) {
    List<System> systemsSnapshot = new ArrayList<>(SYSTEMS.values());

    // Execute logic for each system.
    for (System system : systemsSnapshot) {
      if (newLevelLoadedThisTick) {
        currentTick++;
        return; // Early exit if a new level was loaded this tick.
      }

      if (!isAuthoritative(side, system)) {
        continue;
      }

      system.lastExecuteInFrames(system.lastExecuteInFrames() + 1);

      if (system.isRunning() && system.lastExecuteInFrames() >= system.executeEveryXFrames()) {
        system.execute();
        system.lastExecuteInFrames(0);
      }
    }

    currentTick++;
    newLevelLoadedThisTick = false;
  }

  /**
   * Renders all registered systems once if an OpenGL context is available.
   *
   * <p>This is intentionally independent from {@link #executeOneTick(System.AuthoritativeSide)} so
   * rendering can run at the display loop speed while game logic stays on the configured tick rate.
   *
   * @param delta the time since the last rendered frame
   */
  public static void renderSystems(float delta) {
    if (Game.isHeadless() || Game.windowHeight() <= 0 || Game.windowWidth() <= 0) {
      return;
    }

    List<System> systemsSnapshot = new ArrayList<>(SYSTEMS.values());
    for (System system : systemsSnapshot) {
      system.render(delta);
    }
  }

  /**
   * Finds an entity by its unique ID.
   *
   * @param entityId The unique ID of the entity to find.
   * @return An {@link Optional} containing the found entity, or an empty {@code Optional} if no
   *     entity with the given ID exists.
   */
  public static Optional<Entity> findEntityById(int entityId) {
    return Optional.ofNullable(ENTITY_ID_CACHE.get(entityId));
  }
}
