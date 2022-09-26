package io.github.joeljeremy7.externalizedproperties.resolvers.database;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * JDBC connection provider. This abstraction is provided to enable clients to choose whether to get
 * connections via the JDBC driver manager, data source, or from a connection pool.
 */
public interface ConnectionProvider {
  /**
   * Get a JDBC connection.
   *
   * @return The JDBC connection.
   * @throws SQLException if a database-related error occurred.
   */
  Connection getConnection() throws SQLException;
}
