package mushRoom.shaders;

import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.Vector3;
import core.systems.CameraSystem;
import core.utils.Rectangle;
import core.utils.components.draw.shader.AbstractShader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/** TorchPostProcessing shader that applies dynamic torch lighting with configurable vignette. */
public class TorchPostProcessing extends AbstractShader {

  private static final String VERT_PATH = "shaders/passthrough.vert";
  private static final String FRAG_PATH = "shaders/torch_optimized_pp.frag";
  private static final int MAX_LIGHTS = 128;
  private static final int MAX_AREAS = 32;

  private Set<Light> lights;
  private List<Rectangle> illuminatedAreas;
  private float viewDistance = 0.75f;
  private float vignetteRadius = 0.35f;
  private float baseDimness = 0.15f;

  /**
   * Custom UniformBinding for int values.
   *
   * @param name The name of the uniform variable in the shader
   * @param value The integer value to bind
   * @see UniformBinding
   */
  private record IntUniform(String name, int value) implements UniformBinding {
    @Override
    public void bind(ShaderProgram program) {
      program.setUniformi(name, value);
    }
  }

  /** Represents a light source with position and radius. */
  public static class Light {
    /** Light position X coordinate. */
    public float x;

    /** Light position Y coordinate. */
    public float y;

    /** Light radius. */
    public float radius;

    /**
     * Constructs a Light with specified position and radius.
     *
     * @param x The X coordinate of the light position
     * @param y The Y coordinate of the light position
     * @param radius The radius of the light
     */
    public Light(float x, float y, float radius) {
      this.x = x;
      this.y = y;
      this.radius = radius;
    }

    /**
     * Sets the position of the light.
     *
     * @param x The X coordinate of the light position
     * @param y The Y coordinate of the light position
     * @return The updated Light instance
     */
    public Light setPosition(float x, float y) {
      this.x = x;
      this.y = y;
      return this;
    }

    /**
     * Sets the radius of the light.
     *
     * @param radius The radius of the light
     * @return The updated Light instance
     */
    public Light setRadius(float radius) {
      this.radius = radius;
      return this;
    }

    /**
     * Checks equality based on position.
     *
     * @param obj The object to compare
     * @return True if positions are equal, false otherwise
     */
    @Override
    public boolean equals(Object obj) {
      if (this == obj) return true;
      if (obj == null || getClass() != obj.getClass()) return false;
      Light other = (Light) obj;
      return Float.compare(other.x, x) == 0 && Float.compare(other.y, y) == 0;
    }

    /**
     * Generates hash code based on position.
     *
     * @return The hash code
     */
    @Override
    public int hashCode() {
      int result = Float.hashCode(x);
      result = 31 * result + Float.hashCode(y);
      return result;
    }
  }

  /** Constructs a TorchPostProcessing shader with an empty light list. */
  public TorchPostProcessing() {
    super(VERT_PATH, FRAG_PATH);
    this.lights = new HashSet<>();
    this.illuminatedAreas = new ArrayList<>();
  }

  @Override
  protected List<UniformBinding> getUniforms(int actualUpscale) {
    List<UniformBinding> uniforms = new ArrayList<>();

    // Filter lights to only include those visible in the camera view
    List<Light> visibleLights = getVisibleLights();

    uniforms.add(new IntUniform("u_lightCount", visibleLights.size()));

    // Add light positions and radii
    for (int i = 0; i < visibleLights.size(); i++) {
      Light light = visibleLights.get(i);
      uniforms.add(
          new Vector3Uniform("u_lights[" + i + "]", new Vector3(light.x, light.y, light.radius)));
    }

    // Add illuminated areas
    uniforms.add(new IntUniform("u_areaCount", Math.min(illuminatedAreas.size(), MAX_AREAS)));
    for (int i = 0; i < Math.min(illuminatedAreas.size(), MAX_AREAS); i++) {
      Rectangle area = illuminatedAreas.get(i);
      // Pack rectangle as vec4: x, y, width, height
      uniforms.add(
          new Vector4Uniform(
              "u_areas[" + i + "]",
              new com.badlogic.gdx.math.Vector4(area.x(), area.y(), area.width(), area.height())));
    }

    uniforms.add(new FloatUniform("u_viewDistance", viewDistance));
    uniforms.add(new FloatUniform("u_vignetteRadius", vignetteRadius));
    uniforms.add(new FloatUniform("u_baseDimness", baseDimness));

    return uniforms;
  }

  /**
   * Filters lights to only include those within the camera view + a margin for smooth transitions.
   *
   * @return List of visible lights (limited to MAX_LIGHTS)
   */
  private List<Light> getVisibleLights() {
    Rectangle cameraBounds = CameraSystem.getCameraWorldBounds();

    // Add margin based on typical light radius
    float margin = 10.0f;
    Rectangle expandedBounds =
        new Rectangle(
            cameraBounds.width() + 2 * margin,
            cameraBounds.height() + 2 * margin,
            cameraBounds.x() - margin,
            cameraBounds.y() - margin);

    return lights.stream()
        .filter(light -> isLightInBounds(light, expandedBounds))
        .limit(MAX_LIGHTS)
        .collect(Collectors.toList());
  }

  /**
   * Checks if a light is within the expanded camera bounds.
   *
   * @param light The light source to check
   * @param bounds The expanded camera bounds
   * @return True if the light is within bounds, false otherwise
   */
  private boolean isLightInBounds(Light light, Rectangle bounds) {
    float minX = bounds.x();
    float maxX = bounds.x() + bounds.width();
    float minY = bounds.y();
    float maxY = bounds.y() + bounds.height();

    return light.x >= minX && light.x <= maxX && light.y >= minY && light.y <= maxY;
  }

  @Override
  public int padding() {
    return 0;
  }

  @Override
  public Rectangle worldBounds() {
    return null;
  }

  /**
   * Gets the current view distance.
   *
   * @return The view distance
   */
  public float viewDistance() {
    return viewDistance;
  }

  /**
   * Sets the view distance.
   *
   * @param viewDistance The view distance
   * @return The updated TorchPostProcessing instance
   */
  public TorchPostProcessing viewDistance(float viewDistance) {
    this.viewDistance = viewDistance;
    return this;
  }

  /**
   * Gets the vignette radius.
   *
   * @return The vignette radius
   */
  public float vignetteRadius() {
    return vignetteRadius;
  }

  /**
   * Sets the vignette radius.
   *
   * @param vignetteRadius The vignette radius
   * @return The updated TorchPostProcessing instance
   */
  public TorchPostProcessing vignetteRadius(float vignetteRadius) {
    this.vignetteRadius = vignetteRadius;
    return this;
  }

  /**
   * Gets the base dimness level.
   *
   * @return The base dimness level
   */
  public float baseDimness() {
    return baseDimness;
  }

  /**
   * Sets the base dimness level (0.0 to 1.0).
   *
   * @param baseDimness The base dimness level
   * @return The updated TorchPostProcessing instance
   */
  public TorchPostProcessing baseDimness(float baseDimness) {
    this.baseDimness = Math.max(0.0f, Math.min(1.0f, baseDimness));
    return this;
  }

  /**
   * Adds a light source to the shader.
   *
   * @param light The light source to add
   * @return The updated TorchPostProcessing instance
   */
  public TorchPostProcessing addLight(Light light) {
    lights.add(light);
    return this;
  }

  /**
   * Removes a light source from the shader.
   *
   * @param light The light source to remove
   * @return The updated TorchPostProcessing instance
   */
  public TorchPostProcessing removeLight(Light light) {
    lights.remove(light);
    return this;
  }

  /**
   * Clears all light sources.
   *
   * @return The updated TorchPostProcessing instance
   */
  public TorchPostProcessing clearLights() {
    lights.clear();
    return this;
  }

  /**
   * Gets the list of all light sources.
   *
   * @return The list of light sources
   */
  public List<Light> lights() {
    return new ArrayList<>(lights);
  }

  /**
   * Sets all light sources at once.
   *
   * @param lights The list of light sources to set
   * @return The updated TorchPostProcessing instance
   */
  public TorchPostProcessing lights(List<Light> lights) {
    this.lights = new HashSet<>(lights);
    return this;
  }

  /**
   * Adds an illuminated area (always lit rectangle).
   *
   * @param area The rectangle area to illuminate
   * @return The updated TorchPostProcessing instance
   */
  public TorchPostProcessing addArea(Rectangle area) {
    if (illuminatedAreas.size() < MAX_AREAS) {
      illuminatedAreas.add(area);
    }
    return this;
  }

  /**
   * Removes an illuminated area.
   *
   * @param area The rectangle area to remove
   * @return The updated TorchPostProcessing instance
   */
  public TorchPostProcessing removeArea(Rectangle area) {
    illuminatedAreas.remove(area);
    return this;
  }

  /**
   * Clears all illuminated areas.
   *
   * @return The updated TorchPostProcessing instance
   */
  public TorchPostProcessing clearAreas() {
    illuminatedAreas.clear();
    return this;
  }

  /**
   * Gets the list of all illuminated areas.
   *
   * @return The list of illuminated areas
   */
  public List<Rectangle> areas() {
    return new ArrayList<>(illuminatedAreas);
  }

  /**
   * Sets all illuminated areas at once.
   *
   * @param areas The list of illuminated areas to set
   * @return The updated TorchPostProcessing instance
   */
  public TorchPostProcessing areas(List<Rectangle> areas) {
    this.illuminatedAreas = new ArrayList<>(areas);
    return this;
  }
}
