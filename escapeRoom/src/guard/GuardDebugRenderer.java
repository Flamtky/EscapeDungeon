package guard;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector3;
import contrib.systems.DebugDrawSystem;
import contrib.utils.RaycastUtil;
import core.systems.CameraSystem;
import core.utils.Point;
import core.utils.Vector2;
import java.util.ArrayList;
import java.util.List;

/**
 * Static utility class for rendering debug rays and view cones for the guard detection system.
 *
 * <p>Rays are registered by the {@link GuardDetectionSystem} and drawn by the {@link
 * DebugDrawSystem}. Rays are cleared at the start of each frame before detection runs.
 *
 * <p>Rays are colored based on whether they hit a target (green) or missed (red). Hovering over a
 * blocked ray shows information about what blocked it.
 */
public final class GuardDebugRenderer {

  /** Color for rays that successfully hit a target (unobstructed). */
  private static final Color HIT_COLOR = Color.GREEN;

  /** Color for rays that missed (obstructed). */
  private static final Color MISS_COLOR = Color.RED;

  /** Color for view cone outline. */
  private static final Color CONE_COLOR = new Color(1f, 1f, 0f, 0.3f);

  /** Color for view cone fill. */
  private static final Color CONE_FILL_COLOR = new Color(1f, 1f, 0f, 0.1f);

  /** Alpha transparency for ray lines. */
  private static final float RAY_ALPHA = 0.7f;

  /** Distance threshold for hover detection (in world units). */
  private static final float HOVER_DISTANCE_THRESHOLD = 0.3f;

  /** Number of segments for drawing view cone arcs. */
  private static final int CONE_SEGMENTS = 32;

  private static final List<DebugRay> rays = new ArrayList<>();
  private static final List<ViewCone> viewCones = new ArrayList<>();

  private static boolean registered = false;

  private GuardDebugRenderer() {
    // Utility class, no instantiation
  }

  /**
   * Ensures the renderer is registered with the DebugDrawSystem.
   *
   * <p>This method is idempotent and can be called multiple times safely.
   */
  public static void ensureRegistered() {
    if (!registered) {
      DebugDrawSystem.registerExternalRenderer(GuardDebugRenderer::drawAll);
      registered = true;
    }
  }

  /**
   * Registers a ray to be drawn in the next debug render pass.
   *
   * @param from the starting point of the ray
   * @param to the end point of the ray
   * @param result the raycast result containing hit/block information
   */
  public static void registerRay(Point from, Point to, RaycastUtil.RaycastResult result) {
    rays.add(new DebugRay(from, to, result));
  }

  /**
   * Registers a view cone to be drawn in the next debug render pass.
   *
   * @param origin the origin point of the cone (guard position)
   * @param direction the direction the cone faces
   * @param angleInDegrees the full angle of the cone in degrees
   * @param range the range/length of the cone
   */
  public static void registerViewCone(
      Point origin, Vector2 direction, float angleInDegrees, float range) {
    viewCones.add(new ViewCone(origin, direction, angleInDegrees, range));
  }

  /**
   * Clears all registered rays and view cones.
   *
   * <p>This should be called at the start of each frame before the detection system runs.
   */
  public static void clearRays() {
    rays.clear();
    viewCones.clear();
  }

  /**
   * Draws all registered debug elements (view cones and rays).
   *
   * @param renderer the ShapeRenderer to use for drawing
   */
  public static void drawAll(ShapeRenderer renderer) {
    drawViewCones(renderer);
    drawRays(renderer);
    drawHoverInfo();
  }

  /**
   * Draws all registered view cones.
   *
   * @param renderer the ShapeRenderer to use for drawing
   */
  private static void drawViewCones(ShapeRenderer renderer) {
    if (viewCones.isEmpty()) {
      return;
    }

    renderer.setProjectionMatrix(CameraSystem.camera().combined);

    // Draw filled cones
    Gdx.gl.glEnable(com.badlogic.gdx.graphics.GL20.GL_BLEND);
    Gdx.gl.glBlendFunc(
        com.badlogic.gdx.graphics.GL20.GL_SRC_ALPHA,
        com.badlogic.gdx.graphics.GL20.GL_ONE_MINUS_SRC_ALPHA);

    renderer.begin(ShapeRenderer.ShapeType.Filled);
    for (ViewCone cone : viewCones) {
      drawConeFilledInternal(renderer, cone);
    }
    renderer.end();

    // Draw cone outlines
    renderer.begin(ShapeRenderer.ShapeType.Line);
    for (ViewCone cone : viewCones) {
      drawConeOutlineInternal(renderer, cone);
    }
    renderer.end();
  }

  private static void drawConeFilledInternal(ShapeRenderer renderer, ViewCone cone) {
    float halfAngle = cone.angleInDegrees() / 2f;
    float dirAngle = (float) Math.atan2(cone.direction().y(), cone.direction().x());

    float startAngle = dirAngle - (float) Math.toRadians(halfAngle);
    float endAngle = dirAngle + (float) Math.toRadians(halfAngle);
    float angleStep = (endAngle - startAngle) / CONE_SEGMENTS;

    renderer.setColor(CONE_FILL_COLOR);

    // Draw filled pie-slice from origin to arc
    for (int i = 0; i < CONE_SEGMENTS; i++) {
      float angle1 = startAngle + i * angleStep;
      float angle2 = startAngle + (i + 1) * angleStep;

      float x1 = cone.origin().x() + (float) Math.cos(angle1) * cone.range();
      float y1 = cone.origin().y() + (float) Math.sin(angle1) * cone.range();
      float x2 = cone.origin().x() + (float) Math.cos(angle2) * cone.range();
      float y2 = cone.origin().y() + (float) Math.sin(angle2) * cone.range();

      // Draw triangle from origin to two arc endpoints (pie slice)
      renderer.triangle(cone.origin().x(), cone.origin().y(), x1, y1, x2, y2);
    }
  }

  private static void drawConeOutlineInternal(ShapeRenderer renderer, ViewCone cone) {
    float halfAngle = cone.angleInDegrees() / 2f;
    float dirAngle = (float) Math.atan2(cone.direction().y(), cone.direction().x());

    float startAngle = dirAngle - (float) Math.toRadians(halfAngle);
    float endAngle = dirAngle + (float) Math.toRadians(halfAngle);

    renderer.setColor(CONE_COLOR);

    // Draw the two edge lines
    float x1 = cone.origin().x() + (float) Math.cos(startAngle) * cone.range();
    float y1 = cone.origin().y() + (float) Math.sin(startAngle) * cone.range();
    float x2 = cone.origin().x() + (float) Math.cos(endAngle) * cone.range();
    float y2 = cone.origin().y() + (float) Math.sin(endAngle) * cone.range();

    renderer.line(cone.origin().x(), cone.origin().y(), x1, y1);
    renderer.line(cone.origin().x(), cone.origin().y(), x2, y2);

    // Draw the arc
    float angleStep = (endAngle - startAngle) / CONE_SEGMENTS;
    for (int i = 0; i < CONE_SEGMENTS; i++) {
      float angle1 = startAngle + i * angleStep;
      float angle2 = startAngle + (i + 1) * angleStep;

      float ax1 = cone.origin().x() + (float) Math.cos(angle1) * cone.range();
      float ay1 = cone.origin().y() + (float) Math.sin(angle1) * cone.range();
      float ax2 = cone.origin().x() + (float) Math.cos(angle2) * cone.range();
      float ay2 = cone.origin().y() + (float) Math.sin(angle2) * cone.range();

      renderer.line(ax1, ay1, ax2, ay2);
    }
  }

  /**
   * Draws all registered rays using the provided ShapeRenderer.
   *
   * @param renderer the ShapeRenderer to use for drawing
   */
  private static void drawRays(ShapeRenderer renderer) {
    if (rays.isEmpty()) {
      return;
    }

    renderer.setProjectionMatrix(CameraSystem.camera().combined);
    renderer.begin(ShapeRenderer.ShapeType.Line);

    for (DebugRay ray : rays) {
      Color color = ray.result().hit() ? HIT_COLOR : MISS_COLOR;
      renderer.setColor(color.r, color.g, color.b, RAY_ALPHA);
      renderer.line(ray.from().x(), ray.from().y(), ray.to().x(), ray.to().y());
    }

    renderer.end();
  }

  /** Draws hover information for the ray closest to the mouse cursor. */
  private static void drawHoverInfo() {
    // Get mouse position in world coordinates
    Vector3 mouseScreen = new Vector3(Gdx.input.getX(), Gdx.input.getY(), 0);
    Vector3 mouseWorld = CameraSystem.camera().unproject(mouseScreen);
    Point mousePos = new Point(mouseWorld.x, mouseWorld.y);

    // Find the closest blocked ray to the mouse
    DebugRay hoveredRay = null;
    float closestDistance = HOVER_DISTANCE_THRESHOLD;

    for (DebugRay ray : rays) {
      if (ray.result().hit()) {
        continue; // Only show info for blocked rays
      }

      float dist = distanceToLineSegment(mousePos, ray.from(), ray.to());
      if (dist < closestDistance) {
        closestDistance = dist;
        hoveredRay = ray;
      }
    }

    if (hoveredRay != null) {
      String info = hoveredRay.result().blockingDescription();
      DebugDrawSystem.drawTextInWorldCoords(info, mousePos, Color.WHITE);
    }
  }

  /**
   * Calculates the distance from a point to a line segment.
   *
   * @param point the point
   * @param lineStart start of the line segment
   * @param lineEnd end of the line segment
   * @return the shortest distance from the point to the line segment
   */
  private static float distanceToLineSegment(Point point, Point lineStart, Point lineEnd) {
    float dx = lineEnd.x() - lineStart.x();
    float dy = lineEnd.y() - lineStart.y();
    float lengthSquared = dx * dx + dy * dy;

    if (lengthSquared < 0.0001f) {
      // Line segment is essentially a point
      return RaycastUtil.distance(point, lineStart);
    }

    // Project point onto line, clamped to segment
    float t =
        Math.max(
            0,
            Math.min(
                1,
                ((point.x() - lineStart.x()) * dx + (point.y() - lineStart.y()) * dy)
                    / lengthSquared));

    Point projection = new Point(lineStart.x() + t * dx, lineStart.y() + t * dy);
    return RaycastUtil.distance(point, projection);
  }

  /**
   * Returns the number of currently registered rays.
   *
   * @return the ray count
   */
  public static int rayCount() {
    return rays.size();
  }

  /**
   * Record representing a debug ray to be drawn.
   *
   * @param from the starting point
   * @param to the ending point
   * @param result the raycast result with hit/block information
   */
  private record DebugRay(Point from, Point to, RaycastUtil.RaycastResult result) {}

  /**
   * Record representing a view cone to be drawn.
   *
   * @param origin the origin point of the cone
   * @param direction the direction the cone faces
   * @param angleInDegrees the full angle of the cone in degrees
   * @param range the range/length of the cone
   */
  private record ViewCone(Point origin, Vector2 direction, float angleInDegrees, float range) {}
}
