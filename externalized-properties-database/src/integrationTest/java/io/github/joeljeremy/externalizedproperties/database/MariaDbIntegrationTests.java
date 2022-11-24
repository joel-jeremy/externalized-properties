package io.github.joeljeremy.externalizedproperties.database;

import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.joeljeremy.externalizedproperties.database.testentities.JdbcUtils;
import java.sql.SQLException;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.MariaDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

@Testcontainers
public class MariaDbIntegrationTests extends DatabaseIntegrationTests {
  static final int NUMBER_OF_TEST_ENTRIES = 2;
  static final DockerImageName MARIADB_IMAGE = DockerImageName.parse("mariadb:10.10");

  @Container
  static final MariaDBContainer<?> MARIADB_CONTAINER = new MariaDBContainer<>(MARIADB_IMAGE);

  @BeforeAll
  static void setup() throws SQLException {
    JdbcUtils.createPropertiesTable(MARIADB_CONTAINER.createConnection(""), NUMBER_OF_TEST_ENTRIES);
  }

  @Override
  String getJdbcConnectionString() {
    return MARIADB_CONTAINER.getJdbcUrl();
  }

  @Override
  String getJdbcUsername() {
    return MARIADB_CONTAINER.getUsername();
  }

  @Override
  String getJdbcPassword() {
    return MARIADB_CONTAINER.getPassword();
  }

  /** Dummy test for junit to be able to detect this test class. */
  @Test
  void detect() {
    assertTrue(true);
  }
}
