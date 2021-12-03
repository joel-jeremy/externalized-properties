package io.github.jeyjeyemem.externalizedproperties.resolvers.database.testentities;

import io.github.jeyjeyemem.externalizedproperties.resolvers.database.ConnectionProvider;
import org.h2.jdbcx.JdbcDataSource;

import java.sql.Connection;
import java.sql.SQLException;

public class H2DataSourceConnectionProvider implements ConnectionProvider {

    private final JdbcDataSource dataSource = new JdbcDataSource();

    public H2DataSourceConnectionProvider(String url, String user, String password) {
        dataSource.setUrl(url);
        dataSource.setUser(user);
        dataSource.setPassword(password);
    }

    @Override
    public Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }
    
}
