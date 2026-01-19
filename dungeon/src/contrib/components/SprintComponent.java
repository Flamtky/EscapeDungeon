package contrib.components;

import core.Component;

/** Component that indicates an entity is currently sprinting, along with the speed multiplier. */
public record SprintComponent(float multiplier) implements Component {}
