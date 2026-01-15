package core.components;

import core.Component;
import core.network.server.ClientState;

import java.util.UUID;

/** Component that holds data for analytics purposes. */
public record AnalyticsComponent(ClientState state, UUID sessionId) implements Component {
}
