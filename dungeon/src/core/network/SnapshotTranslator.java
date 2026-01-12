package core.network;

import core.network.messages.s2c.DeltaSnapshotMessage;
import core.network.messages.s2c.SnapshotMessage;
import core.network.server.ClientState;
import java.util.Optional;

/**
 * Translates between ECS state and {@link SnapshotMessage} wire format.
 *
 * <p>Server-side: build a {@link SnapshotMessage} from authoritative entities. Client-side: apply a
 * {@link SnapshotMessage} by dispatching granular updates via {@link MessageDispatcher}.
 *
 * <p>Supports both full snapshots and delta snapshots for bandwidth optimization.
 */
public interface SnapshotTranslator {
  /**
   * Server-side translation: build a full snapshot for the given tick from authoritative entities.
   *
   * @param serverTick the current server tick
   * @return an Optional containing the SnapshotMessage if there are updates, or empty if no new
   *     updates are available
   */
  Optional<SnapshotMessage> translateToSnapshot(int serverTick);

  /**
   * Server-side translation: build a delta snapshot containing only changed entities.
   *
   * <p>Compares current entity states against the client's cached last-sent states and includes
   * only entities that have changed. Also tracks entities that have left the client's visibility
   * range for removal.
   *
   * @param serverTick the current server tick
   * @param client the client state containing cached last-sent states
   * @return an Optional containing the DeltaSnapshotMessage, or empty if no changes
   */
  Optional<DeltaSnapshotMessage> translateToDelta(int serverTick, ClientState client);

  /**
   * Client-side application: convert the full snapshot into granular updates and dispatch them.
   *
   * <p>Must not manipulate game-thread state directly; only dispatch to the provided dispatcher.
   *
   * @param snapshot the received snapshot message
   * @param dispatcher the message dispatcher to handle granular updates
   */
  void applySnapshot(SnapshotMessage snapshot, MessageDispatcher dispatcher);

  /**
   * Client-side application: apply a delta snapshot by updating changed entities and removing
   * despawned ones.
   *
   * @param delta the received delta snapshot message
   * @param dispatcher the message dispatcher to handle granular updates
   */
  void applyDelta(DeltaSnapshotMessage delta, MessageDispatcher dispatcher);
}
