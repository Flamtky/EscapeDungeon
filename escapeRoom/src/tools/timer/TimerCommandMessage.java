package tools.timer;

import core.network.messages.NetworkMessage;

/**
 * Network message sent from server to clients to execute timer commands.
 *
 * <p>Commands include START, STOP, and RESUME operations that change the timer state.
 *
 * @param command the command to execute
 * @param startTimeSeconds the initial elapsed time in seconds (only used for START command)
 */
public record TimerCommandMessage(TimerCommand command, float startTimeSeconds)
    implements NetworkMessage {

  /** Timer commands that can be broadcast to clients. */
  public enum TimerCommand {
    /** Start the timer with a specific initial time */
    START,
    /** Stop/pause the timer */
    STOP,
    /** Resume the timer from its current state */
    RESUME
  }

  /**
   * Creates a START command message.
   *
   * @param startTimeSeconds the initial elapsed time in seconds
   * @return a new TimerCommandMessage with START command
   */
  public static TimerCommandMessage start(float startTimeSeconds) {
    return new TimerCommandMessage(TimerCommand.START, startTimeSeconds);
  }

  /**
   * Creates a STOP command message.
   *
   * @return a new TimerCommandMessage with STOP command
   */
  public static TimerCommandMessage stop() {
    return new TimerCommandMessage(TimerCommand.STOP, 0f);
  }

  /**
   * Creates a RESUME command message.
   *
   * @return a new TimerCommandMessage with RESUME command
   */
  public static TimerCommandMessage resume() {
    return new TimerCommandMessage(TimerCommand.RESUME, 0f);
  }
}
