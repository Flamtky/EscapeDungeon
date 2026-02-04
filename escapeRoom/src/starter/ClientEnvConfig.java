package starter;

import io.github.cdimascio.dotenv.Dotenv;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.List;

/**
 * Loads client network settings (IP and port) from the root .env, falling back to defaults and
 * persisting missing keys.
 */
public final class ClientEnvConfig {
  private static final String KEY_IP = "CLIENT_IP";
  private static final String KEY_PORT = "CLIENT_PORT";
  private static final String DEFAULT_IP = "127.0.0.1";
  private static final String DEFAULT_PORT = "7777";

  private final String serverAddress;
  private final int port;

  private ClientEnvConfig(String serverAddress, int port) {
    this.serverAddress = serverAddress;
    this.port = port;
  }

  /**
   * Loads the client environment configuration from the .env file.
   *
   * @return resolved client env config using defaults if needed
   */
  public static ClientEnvConfig load() {
    Path rootDir = resolveRootDir();
    Dotenv dotenv = Dotenv.configure().directory(rootDir.toString()).ignoreIfMissing().load();

    String ip = valueOrDefault(dotenv.get(KEY_IP), DEFAULT_IP);
    String portString = valueOrDefault(dotenv.get(KEY_PORT), DEFAULT_PORT);

    persistIfMissing(
        rootDir.resolve(".env"),
        dotenv.get(KEY_IP) != null,
        dotenv.get(KEY_PORT) != null,
        ip,
        portString);

    return new ClientEnvConfig(ip, Integer.parseInt(portString));
  }

  /**
   * Returns the configured server address, defaulting if unspecified.
   *
   * @return server address for the client connection
   */
  public String serverAddress() {
    return serverAddress;
  }

  /**
   * Returns the configured network port, defaulting if unspecified.
   *
   * @return port number for the client connection
   */
  public int port() {
    return port;
  }

  private static String valueOrDefault(String value, String defaultValue) {
    return value != null ? value : defaultValue;
  }

  private static Path resolveRootDir() {
    Path dir = Paths.get("").toAbsolutePath();
    while (dir != null) {
      if (Files.exists(dir.resolve("settings.gradle"))) {
        return dir;
      }
      dir = dir.getParent();
    }
    return Paths.get("").toAbsolutePath();
  }

  private static void persistIfMissing(
      Path envPath, boolean hasIp, boolean hasPort, String ip, String portString) {
    try {
      if (Files.notExists(envPath)) {
        Files.write(
            envPath,
            List.of(KEY_IP + "=" + ip, KEY_PORT + "=" + portString),
            StandardCharsets.UTF_8,
            StandardOpenOption.CREATE_NEW);
        return;
      }

      List<String> additions = new java.util.ArrayList<>();
      if (!hasIp) {
        additions.add(KEY_IP + "=" + ip);
      }
      if (!hasPort) {
        additions.add(KEY_PORT + "=" + portString);
      }
      if (!additions.isEmpty()) {
        Files.write(envPath, additions, StandardCharsets.UTF_8, StandardOpenOption.APPEND);
      }
    } catch (IOException ignored) {
      // If persisting fails, continue with in-memory defaults.
    }
  }
}
