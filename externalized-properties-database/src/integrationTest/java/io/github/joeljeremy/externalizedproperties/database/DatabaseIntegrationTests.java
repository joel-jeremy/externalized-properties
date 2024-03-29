package io.github.joeljeremy.externalizedproperties.database;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.joeljeremy.externalizedproperties.core.ExternalizedProperties;
import io.github.joeljeremy.externalizedproperties.core.ExternalizedProperty;
import io.github.joeljeremy.externalizedproperties.core.InvocationContext;
import io.github.joeljeremy.externalizedproperties.core.Resolver;
import io.github.joeljeremy.externalizedproperties.core.testfixtures.InvocationContextUtils;
import io.github.joeljeremy.externalizedproperties.core.testfixtures.InvocationContextUtils.InvocationContextTestFactory;
import io.github.joeljeremy.externalizedproperties.database.testentities.JdbcUtils;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

public abstract class DatabaseIntegrationTests {
  static final InvocationContextTestFactory<ProxyInterface> INVOCATION_CONTEXT_FACTORY =
      InvocationContextUtils.testFactory(ProxyInterface.class);

  /**
   * Override if different connection provider is desired.
   *
   * @param jdbcConnectionString The JDBC connection string.
   * @param username The JDBC username.
   * @param password The JDBC password.
   * @return The connection provider.
   */
  ConnectionProvider getConnectionProvider(
      String jdbcConnectionString, String username, String password) {
    return JdbcUtils.createConnectionProvider(jdbcConnectionString, username, password);
  }

  ConnectionProvider getConnectionProvider() {
    return JdbcUtils.createConnectionProvider(
        getJdbcConnectionString(), getJdbcUsername(), getJdbcPassword());
  }

  abstract String getJdbcConnectionString();

  abstract String getJdbcUsername();

  abstract String getJdbcPassword();

  @Nested
  class ResolveMethod {
    final ConnectionProvider connectionProvider = getConnectionProvider();

    @Test
    @DisplayName("should resolve all properties from database")
    void test1() {
      DatabaseResolver databaseResolver = new DatabaseResolver(connectionProvider);
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
      DatabaseResolver databaseResolver = new DatabaseResolver(connectionProvider);
      InvocationContext context =
          INVOCATION_CONTEXT_FACTORY.fromMethodReference(
              ProxyInterface::nonExistentProperty, externalizedProperties(databaseResolver));

      String propertyName = "non.existent.property";

      Optional<String> result = databaseResolver.resolve(context, propertyName);

      assertFalse(result.isPresent());
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
