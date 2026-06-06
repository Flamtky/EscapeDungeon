package core.systems;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputProcessor;
import java.util.HashSet;
import java.util.Set;

/**
 * System that tracks input states reliably across all platforms.
 *
 * <p>This system wraps libGDX's InputProcessor to track key and button press states, providing a
 * workaround for platform-specific issues (particularly on macOS) where {@link
 * com.badlogic.gdx.Input#isKeyJustPressed(int)} doesn't work reliably.
 *
 * <p>Usage: Call {@link #init()} after setting up the input processor (e.g., after {@link
 * com.badlogic.gdx.Gdx.Input#setInputProcessor(InputProcessor)}), then use the static methods
 * {@link #isKeyJustPressed(int)}, {@link #isKeyPressed(int)}, {@link #isButtonJustPressed(int)},
 * and {@link #isButtonPressed(int)} instead of the corresponding {@link com.badlogic.gdx.Gdx.Input}
 * methods.
 */
public class InputManager {

  private static final Set<Integer> justPressedKeys = new HashSet<>();
  private static final Set<Integer> pressedKeys = new HashSet<>();
  private static final Set<Integer> justPressedButtons = new HashSet<>();
  private static final Set<Integer> pressedButtons = new HashSet<>();
  private static float scrollAmountX;
  private static float scrollAmountY;
  private static int mouseScreenX;
  private static int mouseScreenY;

  private InputManager() {} // static utility class

  /**
   * Initializes the input state tracking by wrapping the current input processor.
   *
   * <p>This method should be called after setting up the input processor (e.g., after setting the
   * Stage as input processor).
   */
  public static void init() {
    InputProcessor oldProcessor = Gdx.input.getInputProcessor();
    Gdx.input.setInputProcessor(
        new InputProcessor() {
          @Override
          public boolean keyDown(int keycode) {
            justPressedKeys.add(keycode);
            return oldProcessor != null && oldProcessor.keyDown(keycode);
          }

          @Override
          public boolean keyUp(int keycode) {
            justPressedKeys.remove(keycode);
            pressedKeys.remove(keycode);
            return oldProcessor != null && oldProcessor.keyUp(keycode);
          }

          @Override
          public boolean keyTyped(char character) {
            return oldProcessor != null && oldProcessor.keyTyped(character);
          }

          @Override
          public boolean touchDown(int screenX, int screenY, int pointer, int button) {
            updateMousePosition(screenX, screenY);
            justPressedButtons.add(button);
            return oldProcessor != null
                && oldProcessor.touchDown(screenX, screenY, pointer, button);
          }

          @Override
          public boolean touchUp(int screenX, int screenY, int pointer, int button) {
            updateMousePosition(screenX, screenY);
            justPressedButtons.remove(button);
            pressedButtons.remove(button);
            return oldProcessor != null && oldProcessor.touchUp(screenX, screenY, pointer, button);
          }

          @Override
          public boolean touchCancelled(int screenX, int screenY, int pointer, int button) {
            updateMousePosition(screenX, screenY);
            return oldProcessor != null
                && oldProcessor.touchCancelled(screenX, screenY, pointer, button);
          }

          @Override
          public boolean touchDragged(int screenX, int screenY, int pointer) {
            updateMousePosition(screenX, screenY);
            return oldProcessor != null && oldProcessor.touchDragged(screenX, screenY, pointer);
          }

          @Override
          public boolean mouseMoved(int screenX, int screenY) {
            updateMousePosition(screenX, screenY);
            return oldProcessor != null && oldProcessor.mouseMoved(screenX, screenY);
          }

          @Override
          public boolean scrolled(float amountX, float amountY) {
            scrollAmountX += amountX;
            scrollAmountY += amountY;
            return oldProcessor != null && oldProcessor.scrolled(amountX, amountY);
          }
        });
  }

  /**
   * Checks if a key was just pressed in the current frame.
   *
   * @param keycode The key to check.
   * @return true if the key was just pressed, false otherwise.
   */
  public static boolean isKeyJustPressed(int keycode) {
    return justPressedKeys.contains(keycode);
  }

  /**
   * Checks if a key is currently pressed.
   *
   * @param keycode The key to check.
   * @return true if the key is pressed, false otherwise.
   */
  public static boolean isKeyPressed(int keycode) {
    return pressedKeys.contains(keycode);
  }

  /**
   * Checks if a mouse button was just pressed in the current frame.
   *
   * @param button The button to check.
   * @return true if the button was just pressed, false otherwise.
   */
  public static boolean isButtonJustPressed(int button) {
    return justPressedButtons.contains(button);
  }

  /**
   * Checks if a mouse button is currently pressed.
   *
   * @param button The button to check.
   * @return true if the button is pressed, false otherwise.
   */
  public static boolean isButtonPressed(int button) {
    return pressedButtons.contains(button);
  }

  /**
   * Returns the latest tracked mouse X position in screen coordinates.
   *
   * @return mouse X position with origin at the top-left of the window
   */
  public static int mouseScreenX() {
    return mouseScreenX;
  }

  /**
   * Returns the latest tracked mouse Y position in screen coordinates.
   *
   * @return mouse Y position with origin at the top-left of the window
   */
  public static int mouseScreenY() {
    return mouseScreenY;
  }

  /**
   * Consumes the accumulated horizontal scroll amount since the last call.
   *
   * @return accumulated horizontal scroll amount
   */
  public static float consumeScrollAmountX() {
    float amount = scrollAmountX;
    scrollAmountX = 0;
    return amount;
  }

  /**
   * Consumes the accumulated vertical scroll amount since the last call.
   *
   * @return accumulated vertical scroll amount
   */
  public static float consumeScrollAmountY() {
    float amount = scrollAmountY;
    scrollAmountY = 0;
    return amount;
  }

  /** Updates the input states; should be called once per frame. */
  public static void update() {
    // Move justPressed keys/buttons to pressed state and clear justPressed
    pressedKeys.addAll(justPressedKeys);
    justPressedKeys.clear();
    pressedButtons.addAll(justPressedButtons);
    justPressedButtons.clear();
  }

  private static void updateMousePosition(int screenX, int screenY) {
    mouseScreenX = screenX;
    mouseScreenY = screenY;
  }
}
