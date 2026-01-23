package analytics;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import io.github.cdimascio.dotenv.Dotenv;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * Manages the database connection pool for the Dungeon Analytics system. It uses HikariCP for
 * performance and Dotenv for secure configuration.
 */
public class DatabaseConnector {

  private static final HikariDataSource dataSource;

  static {
    Dotenv dotenv = Dotenv.load();
    HikariConfig config = new HikariConfig();

    config.setJdbcUrl(dotenv.get("DB_URL"));
    config.setUsername(dotenv.get("DB_USER"));
    config.setPassword(dotenv.get("DB_PASSWORD"));

    // Optimizing for JDK 21 performance
    config.addDataSourceProperty("cachePrepStmts", "true");
    config.addDataSourceProperty("prepStmtCacheSize", "250");
    config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
    config.setMaximumPoolSize(10);
    config.setMinimumIdle(2);

    dataSource = new HikariDataSource(config);
  }

  /**
   * Obtains a connection from the connection pool.
   *
   * @return A valid {@link Connection} object.
   * @throws SQLException If a database access error occurs.
   */
  public static Connection getConnection() throws SQLException {
    return dataSource.getConnection();
  }

  /** Closes the connection pool. Should be called when the application shuts down. */
  public static void close() {
    if (dataSource != null) {
      dataSource.close();
    }
  }
}
