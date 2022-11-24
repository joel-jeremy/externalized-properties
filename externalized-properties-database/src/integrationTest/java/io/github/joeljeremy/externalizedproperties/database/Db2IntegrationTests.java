package io.github.joeljeremy.externalizedproperties.database;

import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.joeljeremy.externalizedproperties.database.testentities.JdbcUtils;
import java.sql.SQLException;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.Db2Container;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

@Testcontainers
public class Db2IntegrationTests extends DatabaseIntegrationTests {
  static final int NUMBER_OF_TEST_ENTRIES = 2;
  static final DockerImageName DB2_IMAGE = DockerImageName.parse("ibmcom/db2:11.5.8.0");

  @Container static final Db2Container DB2_CONTAINER = new Db2Container(DB2_IMAGE).acceptLicense();

  @BeforeAll
  static void setup() throws SQLException {
    JdbcUtils.createPropertiesTable(DB2_CONTAINER.createConnection(""), NUMBER_OF_TEST_ENTRIES);
  }

  @Override
  String getJdbcConnectionString() {
    return DB2_CONTAINER.getJdbcUrl();
  }

  @Override
  String getJdbcUsername() {
    return DB2_CONTAINER.getUsername();
  }

  @Override
  String getJdbcPassword() {
    return DB2_CONTAINER.getPassword();
  }

  /** Dummy test for junit to be able to detect this test class. */
  @Test
  void detect() {
    assertTrue(true);
  }
}
