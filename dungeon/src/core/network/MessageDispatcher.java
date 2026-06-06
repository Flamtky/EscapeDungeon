package core.network;

import core.network.messages.NetworkMessage;
import core.network.server.Session;
import core.utils.logging.DungeonLogger;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.BiConsumer;

/**
 * Manages the dispatching of incoming {@link NetworkMessage}s to their respective handlers. Each
 * message type can have multiple registered handlers. All handlers for a type will be invoked when
 * a message of that type is dispatched.
 */
public final class MessageDispatcher {
  private static final DungeonLogger LOGGER = DungeonLogger.getLogger(MessageDispatcher.class);

  // A thread-safe map to store a list of handlers for each message type.
  private final Map<Class<? extends NetworkMessage>, List<BiConsumer<Session, ?>>> typedHandlers =
      new ConcurrentHashMap<>();

  /**
   * Registers a handler for a specific message type. Multiple handlers can be registered for the
   * same type and will all be invoked when a message of that type is dispatched.
   *
   * @param <T> The type of the message.
   * @param messageType The class of the message type to register the handler for.
   * @param handler The handler to process messages of the given type.
   * @throws IllegalArgumentException if messageType or handler is null.
   */
  public <T extends NetworkMessage> void registerHandler(
      Class<T> messageType, BiConsumer<Session, ? super T> handler) {
    if (messageType == null || handler == null) {
      throw new IllegalArgumentException("Message type and handler must not be null.");
    }
    typedHandlers.computeIfAbsent(messageType, k -> new CopyOnWriteArrayList<>()).add(handler);
    LOGGER.debug("Registered handler for message type: {}", messageType.getSimpleName());
  }

  /**
   * Unregisters a handler for a specific message type.
   *
   * @param <T> The type of the message.
   * @param messageType The class of the message type to unregister the handler for.
   * @param handler The handler to be unregistered.
   * @return true if the handler was successfully unregistered, false otherwise.
   */
  public <T extends NetworkMessage> boolean unregisterHandler(
      Class<T> messageType, BiConsumer<Session, ? super T> handler) {
    if (messageType == null || handler == null) return false;
    List<BiConsumer<Session, ?>> handlers = typedHandlers.get(messageType);
    if (handlers != null && handlers.remove(handler)) {
      LOGGER.debug("Unregistered handler for message type: {}", messageType.getSimpleName());
      return true;
    }
    return false;
  }

  /**
   * Unregisters all handlers for a specific message type.
   *
   * @param <T> The type of the message.
   * @param messageType The class of the message type to unregister all handlers for.
   * @return true if any handlers were removed, false otherwise.
   */
  public <T extends NetworkMessage> boolean unregisterAllHandlers(Class<T> messageType) {
    if (messageType == null) return false;
    List<BiConsumer<Session, ?>> removed = typedHandlers.remove(messageType);
    if (removed != null && !removed.isEmpty()) {
      LOGGER.debug("Unregistered all handlers for message type: {}", messageType.getSimpleName());
      return true;
    }
    return false;
  }

  /**
   * Dispatches a message to all registered handlers based on its type. If no handler is registered
   * for the message type, a log entry is created.
   *
   * @param session The session from which the message was received.
   * @param message The message to be dispatched.
   */
  public void dispatch(Session session, NetworkMessage message) {
    if (message == null) {
      LOGGER.warn("Attempted to dispatch a null message.");
      return;
    }

    List<BiConsumer<Session, ?>> handlers = typedHandlers.get(message.getClass());
    if (handlers != null && !handlers.isEmpty()) {
      for (BiConsumer<Session, ?> handler : handlers) {
        try {
          @SuppressWarnings("unchecked")
          BiConsumer<Session, Object> c = (BiConsumer<Session, Object>) handler;
          c.accept(session, message);
        } catch (Throwable e) {
          LOGGER.error(
              "Error in message handler for message {}: {}",
              message.getClass().getSimpleName(),
              e.getMessage(),
              e);
        }
      }
    } else {
      LOGGER.info(
          "No specific handler registered for message type: " + message.getClass().getSimpleName());
    }
  }
}
