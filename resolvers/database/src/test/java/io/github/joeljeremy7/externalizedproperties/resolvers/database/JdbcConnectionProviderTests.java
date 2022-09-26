package io.github.joeljeremy7.externalizedproperties.resolvers.database;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.joeljeremy7.externalizedproperties.resolvers.database.testentities.H2Utils;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;
import javax.sql.DataSource;
import org.h2.jdbcx.JdbcDataSource;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

public class JdbcConnectionProviderTests {
  static final String H2_CONNECTION_STRING =
      H2Utils.buildConnectionString(JdbcConnectionProviderTests.class.getSimpleName());
  static final JdbcDataSource H2_DATA_SOURCE = H2Utils.createDataSource(H2_CONNECTION_STRING, "sa");

  private static final String CUSTOM_H2_USER = "jay";
  private static final String CUSTOM_H2_USER_PASSWORD = "my_super_s3cret_password";

  @BeforeAll
  public static void setup() throws SQLException {
    // Custom user that has a password for testing.
    H2Utils.createUser(H2_DATA_SOURCE.getConnection(), CUSTOM_H2_USER, CUSTOM_H2_USER_PASSWORD);
  }

  @Nested
  class Constructor {
    @Test
    @DisplayName("should throw when data source argument is null")
    void dataSourceTest1() {
      assertThrows(
          IllegalArgumentException.class, () -> new JdbcConnectionProvider((DataSource) null));
    }

    @Test
    @DisplayName("should throw when data source argument is null and credentials are provided")
    void dataSourceTest2() {
      assertThrows(
          IllegalArgumentException.class,
          () ->
              new JdbcConnectionProvider(
                  (DataSource) null, CUSTOM_H2_USER, CUSTOM_H2_USER_PASSWORD));
    }

    @Test
    @DisplayName("should throw when jdbc connection string argument is null")
    void driverManagerTest1() {
      assertThrows(IllegalArgumentException.class, () -> new JdbcConnectionProvider((String) null));
    }

    @Test
    @DisplayName(
        "should throw when jdbc connection string argument is null "
            + "and credentials are provided")
    void driverManagerTest2() {
      assertThrows(
          IllegalArgumentException.class,
          () -> new JdbcConnectionProvider((String) null, CUSTOM_H2_USER, CUSTOM_H2_USER_PASSWORD));
    }

    @Test
    @DisplayName("should throw when properties argument is null")
    void driverManagerTest3() {
      assertThrows(
          IllegalArgumentException.class,
          () -> new JdbcConnectionProvider(H2_CONNECTION_STRING, (Properties) null));
    }

    @Test
    @DisplayName(
        "should throw when jdbc connection string argument is null "
            + "and credentials are provided via properties")
    void driverManagerTest4() {
      Properties props = new Properties();
      props.setProperty("user", CUSTOM_H2_USER);
      props.setProperty("password", CUSTOM_H2_USER_PASSWORD);

      assertThrows(IllegalArgumentException.class, () -> new JdbcConnectionProvider(null, props));
    }
  }

  @Nested
  class GetConnectionMethod {

    @Test
    @DisplayName("should get a connection from data source")
    void dataSourceTest1() throws SQLException {
      JdbcConnectionProvider connectionProvider = new JdbcConnectionProvider(H2_DATA_SOURCE);

      Connection connection = connectionProvider.getConnection();

      assertNotNull(connection);
      assertTrue(connection.isValid(0));
    }

    @Test
    @DisplayName("should get a connection from data source with the given username")
    void dataSourceTest2() throws SQLException {
      JdbcConnectionProvider connectionProvider = new JdbcConnectionProvider(H2_DATA_SOURCE, "sa");

      Connection connection = connectionProvider.getConnection();

      assertNotNull(connection);
      assertTrue(connection.isValid(0));
    }

    @Test
    @DisplayName("should get a connection from data source with the given username and password")
    void dataSourceTest3() throws SQLException {
      JdbcConnectionProvider connectionProvider =
          new JdbcConnectionProvider(H2_DATA_SOURCE, CUSTOM_H2_USER, CUSTOM_H2_USER_PASSWORD);

      Connection connection = connectionProvider.getConnection();

      assertNotNull(connection);
      assertTrue(connection.isValid(0));
    }

    @Test
    @DisplayName("should throw when invalid username was used")
    void dataSourceTest4() throws SQLException {
      JdbcConnectionProvider connectionProvider =
          new JdbcConnectionProvider(H2_DATA_SOURCE, "invalid_username", CUSTOM_H2_USER_PASSWORD);

      assertThrows(SQLException.class, () -> connectionProvider.getConnection());
    }

    @Test
    @DisplayName("should throw when invalid password was used")
    void dataSourceTest5() throws SQLException {
      JdbcConnectionProvider connectionProvider =
          new JdbcConnectionProvider(H2_DATA_SOURCE, "sa", "invalid_password");

      assertThrows(SQLException.class, () -> connectionProvider.getConnection());
    }

    @Test
    @DisplayName("should get a connection from driver manager")
    void driverManagerTest1() throws SQLException {
      JdbcConnectionProvider connectionProvider =
          new JdbcConnectionProvider(
              // User is in connection string.
              H2_CONNECTION_STRING + ";USER=sa");

      Connection connection = connectionProvider.getConnection();

      assertNotNull(connection);
      assertTrue(connection.isValid(0));
    }

    @Test
    @DisplayName("should get a connection from driver manager with the given username")
    void driverManagerTest2() throws SQLException {
      JdbcConnectionProvider connectionProvider =
          new JdbcConnectionProvider(H2_CONNECTION_STRING, "sa");

      Connection connection = connectionProvider.getConnection();

      assertNotNull(connection);
      assertTrue(connection.isValid(0));
    }

    @Test
    @DisplayName("should get a connection from driver manager with the given username and password")
    void driverManagerTest3() throws SQLException {
      JdbcConnectionProvider connectionProvider =
          new JdbcConnectionProvider(H2_CONNECTION_STRING, CUSTOM_H2_USER, CUSTOM_H2_USER_PASSWORD);

      Connection connection = connectionProvider.getConnection();

      assertNotNull(connection);
      assertTrue(connection.isValid(0));
    }

    @Test
    @DisplayName("should throw when invalid username was used")
    void driverManagerTest4() throws SQLException {
      JdbcConnectionProvider connectionProvider =
          new JdbcConnectionProvider(H2_CONNECTION_STRING, "invalid_user", CUSTOM_H2_USER_PASSWORD);

      assertThrows(SQLException.class, () -> connectionProvider.getConnection());
    }

    @Test
    @DisplayName("should throw when invalid password was used")
    void driverManagerTest5() throws SQLException {
      JdbcConnectionProvider connectionProvider =
          new JdbcConnectionProvider(H2_CONNECTION_STRING, CUSTOM_H2_USER, "invalid_password");

      assertThrows(SQLException.class, () -> connectionProvider.getConnection());
    }

    @Test
    @DisplayName("should get a connection from driver manager with the given user in properties")
    void driverManagerTest6() throws SQLException {
      Properties props = new Properties();
      props.put("user", "sa");

      JdbcConnectionProvider connectionProvider =
          new JdbcConnectionProvider(H2_CONNECTION_STRING, props);

      Connection connection = connectionProvider.getConnection();

      assertNotNull(connection);
      assertTrue(connection.isValid(0));
    }

    @Test
    @DisplayName(
        "should get a connection from driver manager with the given username "
            + "and password in properties")
    void driverManagerTest7() throws SQLException {
      Properties props = new Properties();
      props.put("user", CUSTOM_H2_USER);
      props.put("password", CUSTOM_H2_USER_PASSWORD);

      JdbcConnectionProvider connectionProvider =
          new JdbcConnectionProvider(H2_CONNECTION_STRING, props);

      Connection connection = connectionProvider.getConnection();

      assertNotNull(connection);
      assertTrue(connection.isValid(0));
    }

    @Test
    @DisplayName("should throw when properties contain an invalid user")
    void driverManagerTest8() throws SQLException {
      Properties props = new Properties();
      props.put("user", "invalid_user");
      props.put("password", CUSTOM_H2_USER_PASSWORD);

      JdbcConnectionProvider connectionProvider =
          new JdbcConnectionProvider(H2_CONNECTION_STRING, props);

      assertThrows(SQLException.class, () -> connectionProvider.getConnection());
    }

    @Test
    @DisplayName("should throw when properties contain an invalid password")
    void driverManagerTest9() throws SQLException {
      Properties props = new Properties();
      props.put("user", CUSTOM_H2_USER);
      props.put("password", "invalid_password");

      JdbcConnectionProvider connectionProvider =
          new JdbcConnectionProvider(H2_CONNECTION_STRING, props);

      assertThrows(SQLException.class, () -> connectionProvider.getConnection());
    }
  }
}
