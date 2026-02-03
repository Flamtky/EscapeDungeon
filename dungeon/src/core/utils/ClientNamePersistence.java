package core.utils;

import core.utils.logging.DungeonLogger;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.regex.Pattern;

/** Persists the generated client name in a root-level file so that restarts reuse the same name. */
public final class ClientNamePersistence {
  private static final DungeonLogger LOGGER = DungeonLogger.getLogger(ClientNamePersistence.class);
  private static final String FILE_NAME = "client_name.dat";
  private static final Pattern VALID_NAME = Pattern.compile("[A-Za-z]+\\d{6}");
  private static final Path FILE_PATH = Path.of(FILE_NAME);

  private ClientNamePersistence() {}

  /**
   * Load the persisted name if present and valid.
   *
   * @return the persisted name, or null if the file does not exist
   * @throws IllegalStateException if the file exists but content is invalid
   */
  public static String loadName() {
    if (!Files.exists(FILE_PATH)) {
      return null;
    }
    try {
      String value = Files.readString(FILE_PATH, StandardCharsets.UTF_8).trim();
      if (!VALID_NAME.matcher(value).matches()) {
        throw new IllegalStateException("Persisted client name is corrupted or invalid");
      }
      return value;
    } catch (IOException e) {
      throw new IllegalStateException("Failed to read persisted client name", e);
    }
  }

  /**
   * Persist a validated name to disk.
   *
   * @param name the name to persist
   */
  public static void saveName(String name) {
    if (!VALID_NAME.matcher(name).matches()) {
      throw new IllegalArgumentException("Client name does not match required pattern");
    }
    try {
      Files.writeString(
          FILE_PATH,
          name,
          StandardCharsets.UTF_8,
          StandardOpenOption.CREATE,
          StandardOpenOption.TRUNCATE_EXISTING,
          StandardOpenOption.WRITE);
      System.out.println("Saving client name '" + name + "' to " + FILE_PATH.toAbsolutePath());
    } catch (IOException e) {
      throw new IllegalStateException("Failed to persist client name", e);
    }
  }

  /** Delete the persisted name file if present. */
  public static void invalidate() {
    try {
      Files.deleteIfExists(FILE_PATH);
      System.out.println("Deleted persisted client name file " + FILE_PATH.toAbsolutePath());
    } catch (IOException e) {
      LOGGER.warn("Failed to delete client name file", e);
    }
  }
}
