package core.utils;

import core.utils.logging.DungeonLogger;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Persists the generated client name and character class in a root-level file so that restarts
 * reuse the same identity.
 *
 * <p>File format: two lines, first line is name, second line is class.
 */
public final class ClientNamePersistence {
  private static final DungeonLogger LOGGER = DungeonLogger.getLogger(ClientNamePersistence.class);
  private static final String FILE_NAME = "client_name.dat";
  private static final Pattern VALID_NAME = Pattern.compile("[A-Za-z]+\\d{6}");
  private static final Path FILE_PATH = Path.of(FILE_NAME);

  private ClientNamePersistence() {}

  /**
   * Load the persisted name and class if present and valid.
   *
   * @param allowedClasses set of allowed class names (uppercase)
   * @return a Tuple of (name, className), or null if the file does not exist
   * @throws IllegalStateException if the file exists but content is invalid
   */
  public static Tuple<String, String> load(Set<String> allowedClasses) {
    if (!Files.exists(FILE_PATH)) {
      return null;
    }
    try {
      List<String> lines = Files.readAllLines(FILE_PATH, StandardCharsets.UTF_8);
      if (lines.size() < 2) {
        throw new IllegalStateException(
            "Persisted client data is corrupted: expected 2 lines, found " + lines.size());
      }
      String name = lines.get(0).trim();
      String className = lines.get(1).trim();
      if (!VALID_NAME.matcher(name).matches()) {
        throw new IllegalStateException("Persisted client name is corrupted or invalid: " + name);
      }
      if (!allowedClasses.contains(className)) {
        throw new IllegalStateException("Persisted class is invalid or not allowed: " + className);
      }
      return Tuple.of(name, className);
    } catch (IOException e) {
      throw new IllegalStateException("Failed to read persisted client data", e);
    }
  }

  /**
   * Persist a validated name and class to disk.
   *
   * @param name the name to persist
   * @param className the class name to persist
   * @param allowedClasses set of allowed class names (uppercase)
   */
  public static void save(String name, String className, Set<String> allowedClasses) {
    if (!VALID_NAME.matcher(name).matches()) {
      throw new IllegalArgumentException("Client name does not match required pattern: " + name);
    }
    if (!allowedClasses.contains(className)) {
      throw new IllegalArgumentException("Class is not in allowed set: " + className);
    }
    try {
      String content = name + System.lineSeparator() + className + System.lineSeparator();
      Files.writeString(
          FILE_PATH,
          content,
          StandardCharsets.UTF_8,
          StandardOpenOption.CREATE,
          StandardOpenOption.TRUNCATE_EXISTING,
          StandardOpenOption.WRITE);
      System.out.println(
          "Saving client identity (name='"
              + name
              + "', class='"
              + className
              + "') to "
              + FILE_PATH.toAbsolutePath());
    } catch (IOException e) {
      throw new IllegalStateException("Failed to persist client data", e);
    }
  }

  /** Delete the persisted identity file if present. */
  public static void invalidate() {
    try {
      Files.deleteIfExists(FILE_PATH);
      System.out.println("Deleted persisted client identity file " + FILE_PATH.toAbsolutePath());
    } catch (IOException e) {
      LOGGER.warn("Failed to delete client identity file", e);
    }
  }
}
