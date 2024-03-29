package io.github.joeljeremy.externalizedproperties.database;

import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.joeljeremy.externalizedproperties.database.testentities.JdbcUtils;
import java.sql.SQLException;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.OracleContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

@Testcontainers
public class OracleIntegrationTests extends DatabaseIntegrationTests {
  static final int NUMBER_OF_TEST_ENTRIES = 2;
  static final DockerImageName ORACLE_IMAGE = DockerImageName.parse("gvenzl/oracle-xe:18.4.0-slim");

  @Container static final OracleContainer ORACLE_CONTAINER = new OracleContainer(ORACLE_IMAGE);

  @BeforeAll
  static void setup() throws SQLException {
    JdbcUtils.createPropertiesTable(ORACLE_CONTAINER.createConnection(""), NUMBER_OF_TEST_ENTRIES);
  }

  @Override
  String getJdbcConnectionString() {
    return ORACLE_CONTAINER.getJdbcUrl();
  }

  @Override
  String getJdbcUsername() {
    return ORACLE_CONTAINER.getUsername();
  }

  @Override
  String getJdbcPassword() {
    return ORACLE_CONTAINER.getPassword();
  }

  /** Dummy test for junit to be able to detect this test class. */
  @Test
  void detect() {
    assertTrue(true);
  }
}
