package io.github.joeljeremy.externalizedproperties.database;

import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.joeljeremy.externalizedproperties.database.testentities.JdbcUtils;
import java.sql.SQLException;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.MSSQLServerContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

@Testcontainers
public class MsSqlServerIntegrationTests extends DatabaseIntegrationTests {
  static final int NUMBER_OF_TEST_ENTRIES = 2;
  static final DockerImageName MSSQL_SERVER_IMAGE =
      DockerImageName.parse("mcr.microsoft.com/mssql/server:2019-CU15-ubuntu-20.04");

  @Container
  static final MSSQLServerContainer<?> MSSQL_SERVER_CONTAINER =
      new MSSQLServerContainer<>(MSSQL_SERVER_IMAGE)
          .withUrlParam("databaseName", "master")
          .withUrlParam("encrypt", "false")
          .acceptLicense();

  @BeforeAll
  static void setup() throws SQLException {
    JdbcUtils.createPropertiesTable(
        MSSQL_SERVER_CONTAINER.createConnection(""), NUMBER_OF_TEST_ENTRIES);
  }

  @Override
  String getJdbcConnectionString() {
    return MSSQL_SERVER_CONTAINER.getJdbcUrl();
  }

  @Override
  String getJdbcUsername() {
    return MSSQL_SERVER_CONTAINER.getUsername();
  }

  @Override
  String getJdbcPassword() {
    return MSSQL_SERVER_CONTAINER.getPassword();
  }

  /** Dummy test for junit to be able to detect this test class. */
  @Test
  void detect() {
    assertTrue(true);
  }
}
