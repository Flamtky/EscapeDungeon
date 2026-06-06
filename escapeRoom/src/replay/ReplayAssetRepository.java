package replay;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.TreeSet;
import java.util.stream.Stream;

final class ReplayAssetRepository {
  private static final String ASSET_LIST = "internal_assets.txt";
  private static final String JSON_EXTENSION = ".json";
  private static final Path ESCAPE_ROOM_ASSETS = Path.of("escapeRoom", "assets");

  private ReplayAssetRepository() {}

  static List<String> listReplayAssets(String replayDirectory, String currentAssetPath) {
    TreeSet<String> paths = new TreeSet<>();
    FileHandle assetList = Gdx.files.internal(ASSET_LIST);
    if (assetList.exists()) {
      paths.addAll(replayAssetsFromLines(replayDirectory, assetList.readString().split("\\R")));
    }

    FileHandle replayDir = Gdx.files.internal(replayDirectory);
    if (replayDir.exists()) {
      Arrays.stream(replayDir.list(JSON_EXTENSION))
          .map(file -> replayDirectory + "/" + file.name())
          .forEach(paths::add);
    }
    paths.addAll(replayAssetsFromSourceDirectory(replayDirectory));

    if (currentAssetPath != null && !currentAssetPath.isBlank()) {
      paths.add(currentAssetPath);
    }
    return List.copyOf(paths);
  }

  static List<String> replayAssetsFromLines(String replayDirectory, String[] lines) {
    String prefix = replayDirectory.endsWith("/") ? replayDirectory : replayDirectory + "/";
    return Arrays.stream(lines)
        .map(String::trim)
        .filter(path -> path.startsWith(prefix))
        .filter(path -> path.endsWith(JSON_EXTENSION))
        .sorted()
        .distinct()
        .toList();
  }

  static String importReplayFile(String replayDirectory, Path source) throws IOException {
    if (!isJsonFile(source.getFileName().toString())) {
      throw new IllegalArgumentException("Replay file must be a .json file.");
    }

    Path targetDirectory = sourceReplayDirectory(replayDirectory);
    Files.createDirectories(targetDirectory);
    Path target = targetDirectory.resolve(source.getFileName()).toAbsolutePath().normalize();
    Path normalizedSource = source.toAbsolutePath().normalize();
    if (!Files.exists(target) || !Files.isSameFile(normalizedSource, target)) {
      Files.copy(normalizedSource, target, StandardCopyOption.REPLACE_EXISTING);
    }
    return replayDirectory + "/" + target.getFileName();
  }

  private static List<String> replayAssetsFromSourceDirectory(String replayDirectory) {
    Path sourceDirectory = sourceReplayDirectory(replayDirectory);
    if (!Files.isDirectory(sourceDirectory)) {
      return List.of();
    }

    try (Stream<Path> files = Files.list(sourceDirectory)) {
      return files
          .filter(Files::isRegularFile)
          .map(Path::getFileName)
          .map(Path::toString)
          .filter(ReplayAssetRepository::isJsonFile)
          .map(fileName -> replayDirectory + "/" + fileName)
          .sorted()
          .toList();
    } catch (IOException e) {
      return List.of();
    }
  }

  private static Path sourceReplayDirectory(String replayDirectory) {
    return ESCAPE_ROOM_ASSETS.resolve(replayDirectory).toAbsolutePath().normalize();
  }

  static Path sourceAssetPath(String assetPath) {
    return ESCAPE_ROOM_ASSETS.resolve(assetPath).toAbsolutePath().normalize();
  }

  private static boolean isJsonFile(String fileName) {
    return fileName.toLowerCase(Locale.ROOT).endsWith(JSON_EXTENSION);
  }
}
