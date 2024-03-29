package io.github.joeljeremy.externalizedproperties.database;

import io.github.joeljeremy.externalizedproperties.core.ExternalizedPropertiesException;
import io.github.joeljeremy.externalizedproperties.core.InvocationContext;
import io.github.joeljeremy.externalizedproperties.core.Resolver;
import io.github.joeljeremy.externalizedproperties.database.queryexecutors.SimpleNameValueQueryExecutor;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;

/** {@link Resolver} implementation which resolves requested properties from a database. */
public class DatabaseResolver implements Resolver {
  private final ConnectionProvider connectionProvider;
  private final QueryExecutor queryExecutor;

  /**
   * Constructor. This will use {@link SimpleNameValueQueryExecutor} to execute queries when
   * resolving properties.
   *
   * @param connectionProvider The connection provider.
   */
  public DatabaseResolver(ConnectionProvider connectionProvider) {
    this(connectionProvider, new SimpleNameValueQueryExecutor());
  }

  /**
   * Constructor.
   *
   * @param connectionProvider The connection provider.
   * @param queryExecutor The query executor to resolve properties from the database.
   */
  public DatabaseResolver(ConnectionProvider connectionProvider, QueryExecutor queryExecutor) {
    if (connectionProvider == null) {
      throw new IllegalArgumentException("connectionProvider must not be null.");
    }
    if (queryExecutor == null) {
      throw new IllegalArgumentException("queryExecutor must not be null.");
    }
    this.connectionProvider = connectionProvider;
    this.queryExecutor = queryExecutor;
  }

  /**
   * Resolve property from database.
   *
   * @param context The invocation context.
   * @param propertyName The property name.
   * @return The resolved property value. Otherwise, an empty {@link Optional}.
   */
  @Override
  public Optional<String> resolve(InvocationContext context, String propertyName) {
    try {
      return getFromDatabase(propertyName);
    } catch (SQLException e) {
      throw new ExternalizedPropertiesException(
          "Exception occurred while trying to resolve properties from database.", e);
    }
  }

  private Optional<String> getFromDatabase(String propertyName) throws SQLException {
    try (Connection connection = connectionProvider.getConnection()) {
      Map<String, String> resolvedDbProperties =
          queryExecutor.queryProperties(connection, Collections.singletonList(propertyName));

      return Optional.ofNullable(resolvedDbProperties.get(propertyName));
    }
  }
}
