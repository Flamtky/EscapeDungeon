package core.components;

import core.Component;
import core.Entity;
import core.utils.Vector2;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Stream;

/**
 * Allows the associated entity to move in the dungeon by applying forces that affect its velocity.
 *
 * <p>The {@code VelocityComponent} stores the current velocity vector of the entity along the x and
 * y axes, as well as a set of applied forces that influence this velocity. Instead of setting
 * acceleration directly, external systems apply forces using {@link #applyForce(String, Vector2)}.
 * The sum of all forces determines the acceleration and ultimately the current velocity.
 *
 * <p>The velocity system (e.g., {@link core.systems.VelocitySystem}) is responsible for integrating
 * these forces to update the velocity and move the entity accordingly.
 *
 * <p>Use {@link #applyForce(String, Vector2)} to add or update a force acting on the entity. Use
 * {@link #removeForce(String)} to remove a force. Use {@link #currentVelocity()} and {@link
 * #currentVelocity(Vector2)} to get or set the current velocity directly.
 *
 * <p>A positive velocity means the entity moves right (x) or up (y), negative velocity means moving
 * left or down. If both components are zero, the entity is stationary.
 *
 * <p>Use {@link #onWallHit} to set a callback that is executed if the entity collides with a wall.
 *
 * <h3>Usage Examples</h3>
 *
 * <pre>{@code
 * // Create a VelocityComponent with a custom wall-hit callback using the builder
 * VelocityComponent vc = VelocityComponent.builder()
 *     .onWallHit(entity -> System.out.println("Entity hit a wall: " + entity.id()))
 *     .mass(2.0f)
 *     .canEnterOpenPits(true)
 *     .build();
 *
 * // Apply forces like gravity or wind which affect the velocity in the next update
 * vc.applyForce("gravity", new Vector2(0f, -9.8f));
 * vc.applyForce("wind", new Vector2(2f, 0f));
 *
 * // Remove a force when it's no longer applicable
 * vc.removeForce("wind");
 *
 * // Directly set current velocity if needed (overrides forces)
 * vc.currentVelocity(new Vector2(1.0f, 0f)); // move right at speed 1
 * }</pre>
 */
public final class VelocityComponent implements Component {

  /** The default mass of an entity, on no other is configurated. */
  public static final float DEFAULT_MASS = 1;

  private static final Consumer<Entity> DEFAULT_ON_WALL_HIT = e -> {};

  private Consumer<Entity> onWallHit;

  private final Map<String, Vector2> appliedForces = new HashMap<>();

  private Vector2 currentVelocity;

  private float mass;

  private final Map<String, Vector2> modifiers = new HashMap<>();

  private boolean canEnterOpenPits;
  private boolean canEnterWalls;
  private boolean canEnterGitter;
  private boolean canEnterGlasswalls;

  private final boolean isStationary;

  private VelocityComponent(Builder builder) {
    this.onWallHit = builder.onWallHit;
    this.currentVelocity = builder.currentVelocity;
    this.mass = builder.mass;
    this.canEnterOpenPits = builder.canEnterOpenPits;
    this.canEnterWalls = builder.canEnterWalls;
    this.canEnterGitter = builder.canEnterGitter;
    this.canEnterGlasswalls = builder.canEnterGlasswalls;
    this.isStationary = builder.isStationary;
    this.modifiers.putAll(builder.modifiers);
    this.appliedForces.putAll(builder.appliedForces);
  }

  /**
   * Creates a new builder for constructing a VelocityComponent.
   *
   * @return A new Builder instance.
   */
  public static Builder builder() {
    return new Builder();
  }

  /**
   * Creates a default VelocityComponent with no initial velocity and default settings.
   *
   * @return A default VelocityComponent instance.
   */
  public static VelocityComponent defaultMoving() {
    return VelocityComponent.builder().build();
  }

  /**
   * Creates a VelocityComponent with a specified base speed.
   *
   * @param baseSpeed The base speed value to set.
   * @return A VelocityComponent instance with the specified base speed.
   */
  public static VelocityComponent defaultMoving(float baseSpeed) {
    return VelocityComponent.builder().baseSpeed(baseSpeed).build();
  }

  /**
   * Creates a stationary VelocityComponent that does not move.
   *
   * @return A stationary VelocityComponent instance.
   */
  public static VelocityComponent stationary() {
    return VelocityComponent.builder().isStationary(true).build();
  }

  /**
   * Gets the base speed from the modifiers.
   *
   * <p>The base speed is calculated as the average of the x and y components of the "baseSpeed"
   *
   * @return The base speed value.
   */
  public double baseSpeed() {
    Vector2 baseSpeed = modifiers("baseSpeed");
    return (baseSpeed.x() + baseSpeed.y()) / 2.0;
  }

  /** Builder class for constructing VelocityComponent instances with fluent API. */
  public static class Builder {
    private Consumer<Entity> onWallHit = DEFAULT_ON_WALL_HIT;
    private Vector2 currentVelocity = Vector2.ZERO;
    private float mass = DEFAULT_MASS;
    private boolean canEnterOpenPits = false;
    private boolean canEnterWalls = false;
    private boolean canEnterGitter = false;
    private boolean canEnterGlasswalls = false;
    private boolean isStationary = false;
    private final Map<String, Vector2> modifiers = new HashMap<>();
    private final Map<String, Vector2> appliedForces = new HashMap<>();

    /**
     * Sets the wall hit callback.
     *
     * @param onWallHit The callback to execute on wall collision.
     * @return This builder instance for chaining.
     */
    public Builder onWallHit(Consumer<Entity> onWallHit) {
      this.onWallHit = onWallHit;
      return this;
    }

    /**
     * Sets the current velocity.
     *
     * @param currentVelocity The initial velocity vector.
     * @return This builder instance for chaining.
     */
    public Builder currentVelocity(Vector2 currentVelocity) {
      this.currentVelocity = currentVelocity;
      return this;
    }

    /**
     * Sets the mass of the entity.
     *
     * @param mass The mass value.
     * @return This builder instance for chaining.
     * @throws IllegalArgumentException if mass is less than or equal to 0
     */
    public Builder mass(float mass) {
      if (mass <= 0) throw new IllegalArgumentException("Mass cannot be 0 or less");
      this.mass = mass;
      return this;
    }

    /**
     * Sets whether the entity can enter open pits.
     *
     * @param canEnterOpenPits true if allowed, false otherwise.
     * @return This builder instance for chaining.
     */
    public Builder canEnterOpenPits(boolean canEnterOpenPits) {
      this.canEnterOpenPits = canEnterOpenPits;
      return this;
    }

    /**
     * Sets whether the entity can enter walls.
     *
     * @param canEnterWalls true if allowed, false otherwise.
     * @return This builder instance for chaining.
     */
    public Builder canEnterWalls(boolean canEnterWalls) {
      this.canEnterWalls = canEnterWalls;
      return this;
    }

    /**
     * Sets whether the entity can enter gitters.
     *
     * @param canEnterGitter true if allowed, false otherwise.
     * @return This builder instance for chaining.
     */
    public Builder canEnterGitter(boolean canEnterGitter) {
      this.canEnterGitter = canEnterGitter;
      return this;
    }

    /**
     * Sets whether the entity can enter glasswalls.
     *
     * @param canEnterGlasswalls true if allowed, false otherwise.
     * @return This builder instance for chaining.
     */
    public Builder canEnterGlasswalls(boolean canEnterGlasswalls) {
      this.canEnterGlasswalls = canEnterGlasswalls;
      return this;
    }

    /**
     * Sets whether the entity is stationary.
     *
     * @param isStationary true if the entity should be stationary, false otherwise.
     * @return This builder instance for chaining.
     */
    public Builder isStationary(boolean isStationary) {
      this.isStationary = isStationary;
      return this;
    }

    /**
     * Adds a modifier to the component.
     *
     * @param key The modifier key.
     * @param value The modifier value.
     * @return This builder instance for chaining.
     */
    public Builder modifier(String key, Vector2 value) {
      this.modifiers.put(key, value);
      return this;
    }

    /**
     * Adds multiple modifiers to the component.
     *
     * @param newModifiers A map of modifiers.
     * @return This builder instance for chaining.
     */
    public Builder modifiers(Map<String, Vector2> newModifiers) {
      this.modifiers.putAll(newModifiers);
      return this;
    }

    /**
     * Applies an initial force to the component.
     *
     * @param id Unique identifier of the force.
     * @param force The force vector.
     * @return This builder instance for chaining.
     */
    public Builder applyForce(String id, Vector2 force) {
      this.appliedForces.put(id, force);
      return this;
    }

    /**
     * Constructs the VelocityComponent instance.
     *
     * @return A new VelocityComponent configured with builder settings.
     */
    public VelocityComponent build() {
      return new VelocityComponent(this);
    }

    /**
     * Sets a base speed modifier for the component.
     *
     * @param baseSpeed The base speed value.
     * @return This builder instance for chaining.
     */
    public Builder baseSpeed(float baseSpeed) {
      return baseSpeed(Vector2.of(baseSpeed, baseSpeed));
    }

    /**
     * Sets a base speed modifier for the component.
     *
     * @param baseSpeed The base speed value.
     * @return This builder instance for chaining.
     */
    public Builder baseSpeed(Vector2 baseSpeed) {
      return modifier("baseSpeed", baseSpeed);
    }
  }

  /**
   * Get the current velocity vector.
   *
   * <p>A positive velocity means movement to the right (x) or up (y). Negative values mean movement
   * left or down.
   *
   * @return Current velocity vector.
   */
  public Vector2 currentVelocity() {
    return currentVelocity;
  }

  /**
   * Set the current velocity vector. This value will be used by the velocity system to move the
   * entity.
   *
   * <p>Setting this directly overrides velocity computed from forces.
   *
   * @param newCurrentVelocity The new velocity vector.
   */
  public void currentVelocity(Vector2 newCurrentVelocity) {
    this.currentVelocity = newCurrentVelocity;
  }

  /**
   * Sets the callback to be executed if the entity hits a wall.
   *
   * @param onWallHit The callback consumer.
   */
  public void onWallHit(final Consumer<Entity> onWallHit) {
    this.onWallHit = onWallHit;
  }

  /**
   * Get the callback executed when the entity hits a wall.
   *
   * @return The on-wall-hit callback.
   */
  public Consumer<Entity> onWallHit() {
    return onWallHit;
  }

  /**
   * Set whether the entity can enter open pit tiles.
   *
   * @param canEnterOpenPits true if entity can enter pits, false otherwise.
   */
  public void canEnterOpenPits(boolean canEnterOpenPits) {
    this.canEnterOpenPits = canEnterOpenPits;
  }

  /**
   * Check if the entity can enter open pit tiles.
   *
   * @return true if it can enter pits, false otherwise.
   */
  public boolean canEnterOpenPits() {
    return canEnterOpenPits;
  }

  /**
   * Set whether the entity can enter wall tiles.
   *
   * @param canEnterWalls true if entity can enter walls, false otherwise.
   */
  public void canEnterWalls(boolean canEnterWalls) {
    this.canEnterWalls = canEnterWalls;
  }

  /**
   * Check if the entity can enter wall tiles.
   *
   * @return true if it can wall tiles, false otherwise.
   */
  public boolean canEnterWalls() {
    return canEnterWalls;
  }

  /**
   * Set whether the entity can enter gitter tiles.
   *
   * @param canEnterGitter true if entity can enter gitters, false otherwise.
   */
  public void canEnterGitter(boolean canEnterGitter) {
    this.canEnterGitter = canEnterGitter;
  }

  /**
   * Check if the entity can enter open gitter tiles.
   *
   * @return true if it can enter gitter tiles, false otherwise.
   */
  public boolean canEnterGitter() {
    return canEnterGitter;
  }

  /**
   * Set whether the entity can enter glasswall tiles.
   *
   * @param canEnterGlasswalls true if entity can enter glasswalls, false otherwise.
   */
  public void canEnterGlasswalls(boolean canEnterGlasswalls) {
    this.canEnterGlasswalls = canEnterGlasswalls;
  }

  /**
   * Check if the entity can enter glasswall tiles.
   *
   * @return true if it can enter glasswall tiles, false otherwise.
   */
  public boolean canEnterGlasswalls() {
    return canEnterGlasswalls;
  }

  /**
   * Apply or update a force acting on the entity.
   *
   * <p>Multiple forces can be applied simultaneously, identified by unique IDs. The total force
   * affects acceleration and velocity in the velocity system.
   *
   * @param id Unique identifier of the force.
   * @param force The force vector.
   */
  public void applyForce(String id, Vector2 force) {
    removeForce(id);
    appliedForces.put(id, force);
  }

  /**
   * Remove a previously applied force by its ID.
   *
   * @param id The unique identifier of the force to remove.
   */
  public void removeForce(String id) {
    appliedForces.remove(id);
  }

  /**
   * Get the force vector applied for a given ID, if present.
   *
   * @param id The force ID.
   * @return Optional containing the force vector or empty if none found.
   */
  public Optional<Vector2> force(String id) {
    return Optional.ofNullable(appliedForces.get(id));
  }

  /** Remove all applied forces. */
  public void clearForces() {
    appliedForces.clear();
  }

  /**
   * Get a stream of all currently applied forces.
   *
   * @return Stream of force vectors.
   */
  public Stream<Vector2> appliedForcesStream() {
    return appliedForces.values().stream();
  }

  /**
   * Get a copy of all applied forces mapped by their IDs.
   *
   * @return Map of force IDs to force vectors.
   */
  public Map<String, Vector2> appliedForces() {
    return new HashMap<>(appliedForces);
  }

  /**
   * Get the mass of the entity.
   *
   * @return Mass of the entity
   */
  public float mass() {
    return this.mass;
  }

  /**
   * Sets the mass of the entity.
   *
   * <p>Mass must be greater than 0.
   *
   * @param mass the mass to set
   * @throws IllegalArgumentException if mass is less than or equal to 0
   */
  public void mass(float mass) {
    if (mass <= 0) throw new IllegalArgumentException("Mass cannot be 0 or less");
    this.mass = mass;
  }

  /**
   * Get the modifier for a given key.
   *
   * @param key The modifier key.
   * @return The modifier value, or 1.0f if not set.
   */
  public Vector2 modifiers(String key) {
    return modifiers.getOrDefault(key, Vector2.ONE);
  }

  /**
   * Gets the sum of all modifiers.
   *
   * <p>If no modifiers are set, returns Vector2.ONE.
   *
   * <p>Modifiers are summed by multiplying their values together.
   *
   * @return The total modifier value.
   */
  public Vector2 totalModifiers() {
    Vector2 total = Vector2.ONE;
    for (Vector2 modifier : modifiers.values()) {
      total = total.scale(modifier); // Multiply modifiers together
    }
    return total;
  }

  /**
   * Sets a modifier for a given key.
   *
   * <p>If the key already exists, it will be overwritten.
   *
   * @param key The modifier key.
   * @param value The modifier value.
   * @return The VelocityComponent instance for chaining.
   */
  public VelocityComponent modifier(String key, float value) {
    return modifier(key, Vector2.of(value, value));
  }

  /**
   * Sets a modifier for a given key.
   *
   * <p>If the key already exists, it will be overwritten.
   *
   * @param key The modifier key.
   * @param value The modifier value.
   * @return The VelocityComponent instance for chaining.
   */
  public VelocityComponent modifier(String key, Vector2 value) {
    modifiers.put(key, value);
    return this;
  }

  /**
   * Sets multiple modifiers at once.
   *
   * @param newModifiers A map of modifier keys to their values.
   * @return The VelocityComponent instance for chaining.
   */
  public VelocityComponent modifiers(Map<String, Vector2> newModifiers) {
    modifiers.putAll(newModifiers);
    return this;
  }

  /**
   * Remove a modifier for a given key.
   *
   * @param key The modifier key.
   * @return true if the modifier was removed, false if it did not exist.
   */
  public boolean removeModifier(String key) {
    return modifiers.remove(key) != null;
  }

  /**
   * Returns whether the VelocityComponent is stationary and will not move.
   *
   * @return true if the VelocityComponent is stationary, false otherwise
   */
  public boolean isStationary() {
    return isStationary;
  }
}
