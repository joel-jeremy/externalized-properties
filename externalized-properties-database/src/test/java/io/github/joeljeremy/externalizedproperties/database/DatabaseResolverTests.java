package io.github.joeljeremy.externalizedproperties.database;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.joeljeremy.externalizedproperties.core.ExternalizedProperties;
import io.github.joeljeremy.externalizedproperties.core.ExternalizedPropertiesException;
import io.github.joeljeremy.externalizedproperties.core.ExternalizedProperty;
import io.github.joeljeremy.externalizedproperties.core.InvocationContext;
import io.github.joeljeremy.externalizedproperties.core.Resolver;
import io.github.joeljeremy.externalizedproperties.core.testfixtures.InvocationContextUtils;
import io.github.joeljeremy.externalizedproperties.core.testfixtures.InvocationContextUtils.InvocationContextTestFactory;
import io.github.joeljeremy.externalizedproperties.database.queryexecutors.AbstractNameValueQueryExecutor;
import io.github.joeljeremy.externalizedproperties.database.queryexecutors.SimpleNameValueQueryExecutor;
import io.github.joeljeremy.externalizedproperties.database.testentities.H2Utils;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Optional;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

public class DatabaseResolverTests {
  static final InvocationContextTestFactory<ProxyInterface> INVOCATION_CONTEXT_FACTORY =
      InvocationContextUtils.testFactory(ProxyInterface.class);

  static final int NUMBER_OF_TEST_ENTRIES = 2;
  static final String H2_CONNECTION_STRING =
      H2Utils.buildConnectionString(DatabaseResolverTests.class.getSimpleName());
  static final ConnectionProvider CONNECTION_PROVIDER =
      H2Utils.createConnectionProvider(H2_CONNECTION_STRING, "sa");

  @BeforeAll
  static void setup() throws SQLException {
    createTestDatabaseConfigurationEntries();
  }

  @Nested
  class Constructor {
    @Test
    @DisplayName("should throw when connection provider argument is null")
    void test1() {
      QueryExecutor queryExecutor = new SimpleNameValueQueryExecutor();

      assertThrows(
          IllegalArgumentException.class,
          () -> {
            new DatabaseResolver(null, queryExecutor);
          });
    }

    @Test
    @DisplayName("should throw when query runner is null")
    void test2() {
      assertThrows(
          IllegalArgumentException.class,
          () -> {
            new DatabaseResolver(CONNECTION_PROVIDER, null);
          });
    }
  }

  @Nested
  class ResolveMethod {
    @Test
    @DisplayName("should resolve all properties from database")
    void test1() {
      DatabaseResolver databaseResolver = new DatabaseResolver(CONNECTION_PROVIDER);
      InvocationContext context =
          INVOCATION_CONTEXT_FACTORY.fromMethodReference(
              ProxyInterface::property1, externalizedProperties(databaseResolver));

      String propertyName = "test.property.1";

      Optional<String> result = databaseResolver.resolve(context, propertyName);

      assertTrue(result.isPresent());
      assertNotNull(result.get());
    }

    @Test
    @DisplayName("should return empty Optional when property is not found in database")
    void test2() {
      DatabaseResolver databaseResolver = new DatabaseResolver(CONNECTION_PROVIDER);
      InvocationContext context =
          INVOCATION_CONTEXT_FACTORY.fromMethodReference(
              ProxyInterface::nonExistentProperty, externalizedProperties(databaseResolver));

      String propertyName = "non.existent.property";

      Optional<String> result = databaseResolver.resolve(context, propertyName);

      assertFalse(result.isPresent());
    }

    @Test
    @DisplayName("should use provided custom query executor")
    void test3() {
      DatabaseResolver databaseResolver =
          new DatabaseResolver(
              CONNECTION_PROVIDER,
              new AbstractNameValueQueryExecutor() {
                @Override
                protected String table() {
                  return "custom_properties";
                }

                @Override
                protected String propertyNameColumn() {
                  return "config_key";
                }

                @Override
                protected String propertyValueColumn() {
                  return "config_value";
                }
              });
      InvocationContext context =
          INVOCATION_CONTEXT_FACTORY.fromMethodReference(
              ProxyInterface::property1, externalizedProperties(databaseResolver));

      String propertyName = "test.property.1";

      Optional<String> result = databaseResolver.resolve(context, propertyName);

      assertTrue(result.isPresent());
      assertNotNull(result.get());
    }

    @Test
    @DisplayName("should wrap and propagate any SQL exceptions")
    void test4() {
      // Invalid credentials to simulate SQL exception.
      ConnectionProvider invalidConnectionProvider =
          H2Utils.createConnectionProvider(
              H2Utils.buildConnectionString("ResolveMethod.test6", ";USER=sa;PASSWORD=password"),
              "invalid_user",
              "invalid_password");

      DatabaseResolver databaseResolver = new DatabaseResolver(invalidConnectionProvider);
      InvocationContext context =
          INVOCATION_CONTEXT_FACTORY.fromMethodReference(
              ProxyInterface::property1, externalizedProperties(databaseResolver));

      String propertyName = "test.property.1";

      ExternalizedPropertiesException exception =
          assertThrows(
              ExternalizedPropertiesException.class,
              () -> databaseResolver.resolve(context, propertyName));

      assertTrue(exception.getCause() instanceof SQLException);
    }
  }

  static void createTestDatabaseConfigurationEntries() throws SQLException {
    try (Connection connection = CONNECTION_PROVIDER.getConnection()) {

      H2Utils.createPropertiesTable(connection, NUMBER_OF_TEST_ENTRIES);
      H2Utils.createPropertiesTable(
          connection, "custom_properties", "config_key", "config_value", NUMBER_OF_TEST_ENTRIES);

      connection.commit();
    }
  }

  static ExternalizedProperties externalizedProperties(Resolver... resolvers) {
    return ExternalizedProperties.builder().resolvers(resolvers).build();
  }

  static interface ProxyInterface {
    @ExternalizedProperty("test.property.1")
    String property1();

    @ExternalizedProperty("test.property.2")
    String property2();

    @ExternalizedProperty("non.existent.property")
    String nonExistentProperty();
  }
}
