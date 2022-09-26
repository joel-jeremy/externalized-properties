package io.github.joeljeremy7.externalizedproperties.resolvers.database;

import io.github.joeljeremy7.externalizedproperties.resolvers.database.testentities.JdbcUtils;
import java.sql.SQLException;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

@Testcontainers
public class PostgresqlIntegrationTests extends DatabaseIntegrationTests {

  static final int NUMBER_OF_TEST_ENTRIES = 2;
  static final DockerImageName POSTGRES_IMAGE = DockerImageName.parse("postgres:12.10");

  @Container
  static final PostgreSQLContainer<?> POSTGRESQL_CONTAINER =
      new PostgreSQLContainer<>(POSTGRES_IMAGE);

  @BeforeAll
  static void setup() throws SQLException {
    JdbcUtils.createPropertiesTable(POSTGRESQL_CONTAINER.createConnection(""), 2);
  }

  @Override
  String getJdbcConnectionString() {
    return POSTGRESQL_CONTAINER.getJdbcUrl();
  }

  @Override
  String getJdbcUsername() {
    return POSTGRESQL_CONTAINER.getUsername();
  }

  @Override
  String getJdbcPassword() {
    return POSTGRESQL_CONTAINER.getPassword();
  }

  /** Dummy test for junit to be able to detect this test class. */
  @Test
  void detect() {}
}
