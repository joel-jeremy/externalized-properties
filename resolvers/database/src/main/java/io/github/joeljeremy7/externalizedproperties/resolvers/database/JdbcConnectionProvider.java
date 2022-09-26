package io.github.joeljeremy7.externalizedproperties.resolvers.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;
import javax.sql.DataSource;
import org.checkerframework.checker.nullness.qual.Nullable;

/** An implementation which gets connections from JDBC. */
public class JdbcConnectionProvider implements ConnectionProvider {

  private final ConnectionProvider adapter;

  /**
   * Constructor.
   *
   * @param dataSource The {@link DataSource} to get connections from.
   */
  public JdbcConnectionProvider(DataSource dataSource) {
    if (dataSource == null) {
      throw new IllegalArgumentException("dataSource must not be null.");
    }
    this.adapter = dataSource::getConnection;
  }

  /**
   * Constructor.
   *
   * @param dataSource The {@link DataSource} to get connections from.
   * @param username The username to use when requesting for connections from the {@link
   *     DataSource}.
   */
  public JdbcConnectionProvider(DataSource dataSource, String username) {
    this(dataSource, username, null);
  }

  /**
   * Constructor.
   *
   * @param dataSource The {@link DataSource} to get connections from.
   * @param username The username to use when requesting for connections from the {@link
   *     DataSource}.
   * @param password The password to use when requesting for connections from the {@link
   *     DataSource}.
   */
  public JdbcConnectionProvider(
      DataSource dataSource, @Nullable String username, @Nullable String password) {
    if (dataSource == null) {
      throw new IllegalArgumentException("dataSource must not be null.");
    }
    this.adapter = () -> dataSource.getConnection(username, password);
  }

  /**
   * Constructor.
   *
   * @param jdbcConnectionString The JDBC connection URL to use when requesting connections via
   *     {@link DriverManager#getConnection(String)}.
   */
  public JdbcConnectionProvider(String jdbcConnectionString) {
    if (jdbcConnectionString == null) {
      throw new IllegalArgumentException("jdbcConnectionString must not be null.");
    }
    this.adapter = () -> DriverManager.getConnection(jdbcConnectionString);
  }

  /**
   * Constructor.
   *
   * @param jdbcConnectionString The JDBC connection URL to use when requesting connections via
   *     {@link DriverManager#getConnection(String, String, String)}.
   * @param username The username to use when requesting for connections from {@link DriverManager}.
   */
  public JdbcConnectionProvider(String jdbcConnectionString, String username) {
    this(jdbcConnectionString, username, null);
  }

  /**
   * Constructor.
   *
   * @param jdbcConnectionString The JDBC connection URL to use when requesting connections via
   *     {@link DriverManager#getConnection(String, String, String)}.
   * @param username The username to use when requesting for connections from {@link DriverManager}.
   * @param password The password to use when requesting for connections from {@link DriverManager}.
   */
  public JdbcConnectionProvider(
      String jdbcConnectionString, @Nullable String username, @Nullable String password) {
    if (jdbcConnectionString == null) {
      throw new IllegalArgumentException("jdbcConnectionString must not be null.");
    }
    this.adapter = () -> DriverManager.getConnection(jdbcConnectionString, username, password);
  }

  /**
   * Constructor.
   *
   * @param jdbcConnectionString The JDBC connection URL to use when requesting connections via
   *     {@link DriverManager#getConnection(String, Properties)}.
   * @param properties The properties to use when requesting for connections from {@link
   *     DriverManager}.
   */
  public JdbcConnectionProvider(String jdbcConnectionString, Properties properties) {
    if (jdbcConnectionString == null) {
      throw new IllegalArgumentException("jdbcConnectionString must not be null.");
    }
    if (properties == null) {
      throw new IllegalArgumentException("properties must not be null.");
    }
    this.adapter = () -> DriverManager.getConnection(jdbcConnectionString, properties);
  }

  /** {@inheritDoc} */
  @Override
  public Connection getConnection() throws SQLException {
    return adapter.getConnection();
  }
}
