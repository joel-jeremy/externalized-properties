package io.github.joeljeremy7.externalizedproperties.resolvers.database.testentities;

import com.zaxxer.hikari.HikariDataSource;
import io.github.joeljeremy7.externalizedproperties.resolvers.database.ConnectionProvider;
import io.github.joeljeremy7.externalizedproperties.resolvers.database.JdbcConnectionProvider;
import io.github.joeljeremy7.externalizedproperties.resolvers.database.queryexecutors.SimpleNameValueQueryExecutor;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class JdbcUtils {
  public static ConnectionProvider createConnectionProvider(String url, String user) {
    return createConnectionProvider(url, user, null);
  }

  public static ConnectionProvider createConnectionProvider(
      String jdbcConnectionString, String user, String password) {
    return new JdbcConnectionProvider(createDataSource(jdbcConnectionString, user, password));
  }

  public static HikariDataSource createDataSource(String jdbcConnectionString) {
    return createDataSource(jdbcConnectionString, null, null);
  }

  public static HikariDataSource createDataSource(String jdbcConnectionString, String user) {
    return createDataSource(jdbcConnectionString, user, null);
  }

  public static HikariDataSource createDataSource(
      String jdbcConnectionString, String user, String password) {
    HikariDataSource Db2DataSource = new HikariDataSource();
    Db2DataSource.setJdbcUrl(jdbcConnectionString);
    Db2DataSource.setUsername(user);
    Db2DataSource.setPassword(password);
    return Db2DataSource;
  }

  public static void createPropertiesTable(Connection connection, int numberOfTestEntries)
      throws SQLException {
    // Matches the default columns and table name in
    // SimpleNameValueQueryExecutor
    createPropertiesTable(
        connection,
        SimpleNameValueQueryExecutor.DEFAULT_TABLE,
        SimpleNameValueQueryExecutor.DEFAULT_PROPERTY_NAME_COLUMN,
        SimpleNameValueQueryExecutor.DEFAULT_PROPERTY_VALUE_COLUMN,
        numberOfTestEntries);
  }

  public static void createPropertiesTable(
      Connection connection,
      String table,
      String propertyNameColumn,
      String propertyValueColumn,
      int numberOfTestEntries)
      throws SQLException {
    PreparedStatement createTable =
        connection.prepareStatement(
            "CREATE TABLE "
                + table
                + " ("
                + propertyNameColumn
                + " VARCHAR(255), "
                + propertyValueColumn
                + " VARCHAR(255), "
                + "description VARCHAR(255))");
    createTable.executeUpdate();

    populatePropertiesTable(connection, table, numberOfTestEntries);
  }

  public static void populatePropertiesTable(
      Connection connection, String table, int numberOfTestEntries) throws SQLException {
    PreparedStatement insert =
        connection.prepareStatement("INSERT INTO " + table + " VALUES(?,?,?)");

    for (int i = 1; i <= numberOfTestEntries; i++) {
      insert.setString(1, "test.property." + i);
      insert.setString(2, "test/property/value/" + i);
      insert.setString(3, "Test property " + i + " description");
      insert.executeUpdate();
    }
  }
}
