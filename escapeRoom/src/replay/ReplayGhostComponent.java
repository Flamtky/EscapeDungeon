package replay;

import contrib.entities.CharacterClass;
import core.Component;

record ReplayGhostComponent(String playerId, CharacterClass characterClass, int tintColor)
    implements Component {}
