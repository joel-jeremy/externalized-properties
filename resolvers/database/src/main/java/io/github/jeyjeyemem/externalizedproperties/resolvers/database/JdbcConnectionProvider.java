package io.github.jeyjeyemem.externalizedproperties.resolvers.database;

import javax.sql.DataSource;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * An implementation which gets connections from JDBC.
 */
public class JdbcConnectionProvider implements ConnectionProvider {

    private final ConnectionProvider adapter;

    /**
     * Constructor.
     * 
     * @param dataSource The {@link DataSource} to get connections from.
     */
    public JdbcConnectionProvider(DataSource dataSource) {
        if (dataSource == null) {
            throw new IllegalArgumentException("dataSource must not be null");
        }
        this.adapter = dataSource::getConnection;
    }

    /**
     * Constructor.
     * 
     * @param dataSource The {@link DataSource} to get connections from.
     * @param username The username to use when requesting for connections 
     * from the {@link DataSource}.
     */
    public JdbcConnectionProvider(
            DataSource dataSource,
            String username
    ) {
        this(dataSource, username, null);
    }

    /**
     * Constructor.
     * 
     * @param dataSource The {@link DataSource} to get connections from.
     * @param username The username to use when requesting for connections 
     * from the {@link DataSource}.
     * @param password The password to use when requesting for connections 
     * from the {@link DataSource}.
     */
    public JdbcConnectionProvider(
            DataSource dataSource,
            String username,
            String password
    ) {
        if (dataSource == null) {
            throw new IllegalArgumentException("dataSource must not be null");
        }
        this.adapter = () -> dataSource.getConnection(username, password);
    }

    /**
     * Constructor.
     * 
     * @param jdbcConnectionUrl The JDBC connection URL.
     */
    public JdbcConnectionProvider(
            String jdbcConnectionUrl
    ) {
        if (jdbcConnectionUrl == null) {
            throw new IllegalArgumentException("jdbcConnectionUrl must not be null");
        }
        this.adapter = () -> DriverManager.getConnection(jdbcConnectionUrl);
    }

    /**
     * Constructor.
     * 
     * @param jdbcConnectionString The JDBC connection string.
     * @param username The username to use when requesting for connections 
     * from {@link DriverManager}.
     */
    public JdbcConnectionProvider(
            String jdbcConnectionString,
            String username
    ) {
        this(jdbcConnectionString, username, null);
    }

    /**
     * Constructor.
     * 
     * @param jdbcConnectionString The JDBC connection string.
     * @param username The username to use when requesting for connections 
     * from {@link DriverManager}.
     * @param password The password to use when requesting for connections 
     * from {@link DriverManager}.
     */
    public JdbcConnectionProvider(
            String jdbcConnectionString,
            String username,
            String password
    ) {
        if (jdbcConnectionString == null) {
            throw new IllegalArgumentException("jdbcConnectionString must not be null");
        }
        this.adapter = () -> DriverManager.getConnection(
            jdbcConnectionString,
            username,
            password
        );
    }

    /** {@inheritDoc} */
    @Override
    public Connection getConnection() throws SQLException {
        return adapter.getConnection();
    }
}
