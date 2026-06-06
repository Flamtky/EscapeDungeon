package starter;

import java.util.Random;

/** Generates unique random names combining German words with 6-digit random numbers. */
public final class RandomNameGenerator {

  private static final Random RANDOM = new Random();

  // spotless:off
  private static final String[] WORDS = {
    // Animals
    "Adder", "Ameise", "Baer", "Barsch", "Biber", "Biene", "Bussard",
    "Dachs", "Drache", "Ente", "Fasan", "Fink", "Fuchs", "Gans", "Gecko",
    "Geier", "Gepard", "Greif", "Habicht", "Hamster", "Hase", "Hecht",
    "Hund", "Karpfen", "Katze", "Kondor", "Kranich", "Krebs", "Marder",
    "Maus", "Meise", "Nashorn", "Nerz", "Panther", "Papagei", "Pfau",
    "Pferd", "Puma", "Rabe", "Reh", "Robbe", "Schwan", "Seehund", "Spatz",
    "Specht", "Sperber", "Storch", "Taube", "Tiger", "Tukan", "Waran",
    "Wespe", "Zebra",

    // Plants & Trees
    "Ahorn", "Bambus", "Birke", "Buche", "Efeu", "Eiche", "Farn",
    "Fichte", "Kiefer", "Moos", "Rose", "Tanne",

    // Minerals & Gems
    "Amber", "Bronze", "Granat", "Granit", "Jade", "Kupfer", "Marmor",
    "Quarz", "Rubin", "Saphir", "Stein", "Topas",

    // Nature & Weather
    "Berg", "Donner", "Dunst", "Feder", "Feuer", "Frost", "Heide",
    "Horizont", "Komet", "Krater", "Mond", "Nordwind", "Regen", "Savanne",
    "Schnee", "Sonne", "Stern", "Steppe", "Sturm", "Taiga", "Tau",
    "Tundra", "Wasser", "Wiese", "Wind", "Zenit",

    // Seasons
    "Herbst", "Sommer", "Winter",

    // Abstract
    "Anker", "Aura", "Echo", "Ehre", "Funke", "Geist", "Harmonie",
    "Kompass", "Kraft", "Mut", "Pfad", "Rune", "Schatten", "Traum",
    "Wunder", "Zauber", "Zeichen"
  };
  // spotless:on

  /**
   * Generates a unique random name combining a German word with a 6-digit random number.
   *
   * @return a randomly generated name (e.g., "Tiger847392")
   */
  public static String generateName() {
    String word = WORDS[RANDOM.nextInt(WORDS.length)];
    int randomNumber = RANDOM.nextInt(1_000_000); // 0-999999
    return word + String.format("%06d", randomNumber); // Pad with leading zeros
  }

  public static void main(String[] args) {
    for (int i = 0; i < 10; i++) {
      System.out.println(generateName());
    }
  }

  private RandomNameGenerator() {
    // Utility class, no instantiation
  }
}
