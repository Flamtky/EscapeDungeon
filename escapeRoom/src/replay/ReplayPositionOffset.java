package replay;

import contrib.entities.CharacterClass;
import core.utils.Point;

/** Applies the same character-specific position offsets used by replay ghosts. */
public final class ReplayPositionOffset {
  private static final float ROGUE_X = 0.5f;
  private static final float ROGUE_Y = 0.5f;
  private static final float APPRENTICE_X = 0.5f;
  private static final float APPRENTICE_Y = 0.75f;
  private static final float DEFAULT_X = APPRENTICE_X;
  private static final float DEFAULT_Y = APPRENTICE_Y;

  private ReplayPositionOffset() {}

  /**
   * Applies the visual gameplay offset for the given character class.
   *
   * @param position base tile or source position
   * @param characterClass character class whose offset should be applied
   * @return adjusted position for rendering and placement
   */
  public static Point apply(Point position, CharacterClass characterClass) {
    return position.translate(xFor(characterClass), yFor(characterClass));
  }

  private static float xFor(CharacterClass characterClass) {
    return characterClass == CharacterClass.ROGUE ? ROGUE_X : DEFAULT_X;
  }

  private static float yFor(CharacterClass characterClass) {
    return characterClass == CharacterClass.ROGUE ? ROGUE_Y : DEFAULT_Y;
  }
}
