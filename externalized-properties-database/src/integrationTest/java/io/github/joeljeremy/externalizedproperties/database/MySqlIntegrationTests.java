package io.github.joeljeremy.externalizedproperties.database;

import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.joeljeremy.externalizedproperties.database.testentities.JdbcUtils;
import java.sql.SQLException;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

@Testcontainers
public class MySqlIntegrationTests extends DatabaseIntegrationTests {
  static final int NUMBER_OF_TEST_ENTRIES = 2;
  static final DockerImageName MYSQL_IMAGE = DockerImageName.parse("mysql:8.0.28");

  @Container static final MySQLContainer<?> MYSQL_CONTAINER = new MySQLContainer<>(MYSQL_IMAGE);

  @BeforeAll
  static void setup() throws SQLException {
    JdbcUtils.createPropertiesTable(MYSQL_CONTAINER.createConnection(""), NUMBER_OF_TEST_ENTRIES);
  }

  @Override
  String getJdbcConnectionString() {
    return MYSQL_CONTAINER.getJdbcUrl();
  }

  @Override
  String getJdbcUsername() {
    return MYSQL_CONTAINER.getUsername();
  }

  @Override
  String getJdbcPassword() {
    return MYSQL_CONTAINER.getPassword();
  }

  /** Dummy test for junit to be able to detect this test class. */
  @Test
  void detect() {
    assertTrue(true);
  }
}
