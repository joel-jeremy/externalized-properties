package io.github.joeljeremy.externalizedproperties.database;

import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.joeljeremy.externalizedproperties.database.testentities.JdbcUtils;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.SQLException;
import java.util.Comparator;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class SqliteIntegrationTests extends DatabaseIntegrationTests {
  static final int NUMBER_OF_TEST_ENTRIES = 2;
  static final Path TEMP_DIR = tempDir();
  static final String SQLITE_USERNAME = "user";
  static final String SQLITE_PASSWORD = "password";
  static final String SQLITE_JDBC_URL = "jdbc:sqlite:" + TEMP_DIR.resolve("test.db");

  @BeforeAll
  static void setup() throws SQLException {
    ConnectionProvider connectionProvider =
        JdbcUtils.createConnectionProvider(SQLITE_JDBC_URL, SQLITE_USERNAME, SQLITE_PASSWORD);
    JdbcUtils.createPropertiesTable(connectionProvider.getConnection(), NUMBER_OF_TEST_ENTRIES);
  }

  @AfterAll
  static void cleanup() throws IOException {
    Files.walk(TEMP_DIR)
        .sorted(Comparator.reverseOrder())
        .forEach(
            path -> {
              try {
                Files.delete(path);
              } catch (IOException e) {
                throw new UncheckedIOException(e);
              }
            });
  }

  @Override
  String getJdbcConnectionString() {
    return SQLITE_JDBC_URL;
  }

  @Override
  String getJdbcUsername() {
    return SQLITE_USERNAME;
  }

  @Override
  String getJdbcPassword() {
    return SQLITE_PASSWORD;
  }

  /** Dummy test for junit to be able to detect this test class. */
  @Test
  void detect() {
    assertTrue(true);
  }

  static Path tempDir() {
    try {
      return Files.createTempDirectory("");
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }
}
