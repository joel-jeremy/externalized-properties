package io.github.jeyjeyemem.externalizedproperties.resolvers.database;

import javax.sql.DataSource;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * An adapter to get JDBC connections from a {@link DataSource}.
 */
public class DataSourceConnectionProvider implements ConnectionProvider {

    private final DataSource dataSource;
    private final String username;
    private final String password;

    /**
     * Constructor.
     * 
     * @param dataSource The data source to get connections from.
     */
    public DataSourceConnectionProvider(DataSource dataSource) {
        this.dataSource = dataSource;
        this.username = null;
        this.password = null;
    }

    /**
     * Constructor.
     * 
     * @param dataSource The data source to get connections from.
     * @param username The username to use when requesting for connections 
     * from the data source.
     * @param password The password to use when requesting for connections 
     * from the data source.
     */
    public DataSourceConnectionProvider(
            DataSource dataSource, 
            String username, 
            String password
    ) {
        if (dataSource == null) {
            throw new IllegalArgumentException("dataSource must not be null.");
        }
        if (username == null) {
            throw new IllegalArgumentException("username must not be null.");
        }
        if (password == null) {
            throw new IllegalArgumentException("password must not be null.");
        }
        this.dataSource = dataSource;
        this.username = username;
        this.password = password;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Connection getConnection() throws SQLException {
        if (username != null && password != null) {
            return dataSource.getConnection(username, password);
        }
        return dataSource.getConnection();
    }
    
}
