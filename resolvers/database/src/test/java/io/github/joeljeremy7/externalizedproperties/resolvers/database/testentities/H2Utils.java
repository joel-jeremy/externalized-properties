package io.github.joeljeremy7.externalizedproperties.resolvers.database.testentities;

import io.github.joeljeremy7.externalizedproperties.resolvers.database.ConnectionProvider;
import io.github.joeljeremy7.externalizedproperties.resolvers.database.JdbcConnectionProvider;
import io.github.joeljeremy7.externalizedproperties.resolvers.database.queryexecutors.SimpleNameValueQueryExecutor;
import org.h2.jdbcx.JdbcDataSource;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class H2Utils {
    public static ConnectionProvider createConnectionProvider(
        String url, 
        String user
    ) {
        return createConnectionProvider(url, user, null);
    }

    public static ConnectionProvider createConnectionProvider(
            String url, 
            String user, 
            String password
    ) {
        return new JdbcConnectionProvider(createDataSource(url, user, password));
    }

    public static String buildConnectionString(String databaseName) {
        // Use DB_CLOSE_DELAY=-1 so that h2 in-memory database contents are not lost when closing. 
        return buildConnectionString(databaseName, ";DB_CLOSE_DELAY=-1");
    }

    // Make sure properties always start with ;
    public static String buildConnectionString(String databaseName, String properties) {
        return "jdbc:h2:mem:" + databaseName + properties;
    }

    public static JdbcDataSource createDataSource(String url) {
        return createDataSource(url, null, null);
    }

    public static JdbcDataSource createDataSource(String url, String user) {
        return createDataSource(url, user, null);
    }

    public static JdbcDataSource createDataSource(String url, String user, String password) {
        JdbcDataSource h2DataSource = new JdbcDataSource();
        h2DataSource.setUrl(url);
        h2DataSource.setUser(user);
        h2DataSource.setPassword(password);
        return h2DataSource;
    }

    public static void createPropertiesTable(
            Connection connection,
            int numberOfTestEntries
    ) throws SQLException {
        // Matches the default columns and table name in
        // SimpleNameValueQueryExecutor
        createPropertiesTable(
            connection,
            SimpleNameValueQueryExecutor.TABLE,
            SimpleNameValueQueryExecutor.PROPERTY_NAME_COLUMN,
            SimpleNameValueQueryExecutor.PROPERTY_VALUE_COLUMN,
            numberOfTestEntries
        );
    }

    public static void createPropertiesTable(
            Connection connection,
            String table,
            String propertyNameColumn,
            String propertyValueColumn,
            int numberOfTestEntries
    ) throws SQLException {
        PreparedStatement createTable = connection.prepareStatement(
            "CREATE TABLE IF NOT EXISTS " + table + " (" +
            propertyNameColumn + " VARCHAR(255), " +
            propertyValueColumn + " VARCHAR(255), " +
            "description VARCHAR(255))"
        );
        createTable.executeUpdate();

        populatePropertiesTable(connection, table, numberOfTestEntries);
    }

    public static void populatePropertiesTable(
            Connection connection,
            String table,
            int numberOfTestEntries
    ) throws SQLException {
        PreparedStatement insert = connection.prepareStatement(
            "INSERT INTO " + table + " VALUES(?,?,?)"
        );

        for (int i = 1; i <= numberOfTestEntries; i++) {
            insert.setString(1, "test.property." + i);
            insert.setString(2, "test/property/value/" + i);
            insert.setString(3, "Test property " + i + " description");
            insert.executeUpdate();
        }
    }

    public static void createUser(
            Connection connection,
            String username, 
            String password
    ) throws SQLException {
        // Prepared statement parameters don't work on DDL commands.
        PreparedStatement createUser = connection.prepareStatement(
            "CREATE USER " + username + 
            " PASSWORD '" + password + "' ADMIN"
        );
        createUser.executeUpdate();
    }
}
