package io.github.jeyjeyemem.externalizedproperties.resolvers.database.testentities;

import io.github.jeyjeyemem.externalizedproperties.resolvers.database.DataSourceConnectionProvider;
import org.h2.jdbcx.JdbcDataSource;

import javax.sql.DataSource;

import java.sql.Connection;
import java.sql.SQLException;

public class H2DataSourceConnectionProvider extends DataSourceConnectionProvider {

    public H2DataSourceConnectionProvider(String url, String user, String password) {
        super(initDataSource(url, user, password));
    }

    @Override
    public Connection getConnection() throws SQLException {
        return super.getConnection();
    }

    private static DataSource initDataSource(String url, String user, String password) {
        JdbcDataSource h2DataSource = new JdbcDataSource();
        h2DataSource.setUrl(url);
        h2DataSource.setUser(user);
        h2DataSource.setPassword(password);
        return h2DataSource;
    }
}
