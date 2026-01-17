package tools.timer;

import core.network.messages.NetworkMessage;

/**
 * Network message sent from server to clients to synchronize timer state.
 *
 * <p>This message is broadcast periodically to all clients to ensure they stay in sync with the
 * authoritative server timer.
 *
 * @param elapsedSeconds the current elapsed time in seconds on the server
 * @param running true if the timer is currently running, false if stopped
 */
public record TimerSyncMessage(float elapsedSeconds, boolean running) implements NetworkMessage {}
