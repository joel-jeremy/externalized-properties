package io.github.jeyjeyemem.externalizedproperties.resolvers.database.queryexecutors;

import io.github.jeyjeyemem.externalizedproperties.resolvers.database.DatabaseProperty;
import io.github.jeyjeyemem.externalizedproperties.resolvers.database.QueryExecutor;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Abstract query executor that will build and run a database query based on
 * the specified table name and it's property name and value column mappings.
 */
public abstract class AbstractNameValueQueryExecutor implements QueryExecutor {
    /**
     * Params in order: 
     * <ol>
     *  <li>property name column</li>
     *  <li>property value column</li>
     *  <li>table name</li>
     *  <li>property name column</li>
     *  <li>property names mapped to comma-separated ?</li>
     * </ol>
     */
    private static final String DEFAULT_QUERY_TEMPLATE = "SELECT %s, %s FROM %s WHERE %s IN (%s)";

    /**
     * Query properties from the database.
     * 
     * @param connection The JDBC connection.
     * @param propertyNamesToResolve The names of the properties to resolve from database.
     * @throws SQLException if a database-related error has occurred.
     */
    @Override
    public List<DatabaseProperty> queryProperties(
            Connection connection,
            Collection<String> propertyNamesToResolve
    ) throws SQLException {
        PreparedStatement preparedStatement = prepareStatement(
            connection, 
            propertyNamesToResolve
        );
        return runQuery(preparedStatement);
    }

    /**
     * Name of the database table.
     * 
     * @return The name of the database table.
     */
    protected abstract String tableName();

    /**
     * Name of property name column.
     * 
     * @return The name of the property name column.
     */
    protected abstract String propertyNameColumn();

    /**
     * Name of the property value column.
     * 
     * @return The name of the property value column.
     */
    protected abstract String propertyValueColumn();

    /**
     * Run the query.
     * 
     * @param preparedStatement The prepared statement.
     * @return The list of properties resolved from the database.
     * @throws SQLException if a database-related error has occurred.
     */
    protected List<DatabaseProperty> runQuery(
            PreparedStatement preparedStatement
    ) throws SQLException {
        List<DatabaseProperty> resolvedProperties = new ArrayList<>();
        try (ResultSet resultSet = preparedStatement.executeQuery()) {
            while (resultSet.next()) {
                resolvedProperties.add(mapResult(resultSet));
            }
        }
        return resolvedProperties;
    }

    /**
     * Map the query result to a {@link DatabaseProperty}.
     * 
     * @param resultSet The query result set.
     * @return The mapped {@link DatabaseProperty}.
     * @throws SQLException if a database-related error has occurred.
     */
    protected DatabaseProperty mapResult(ResultSet resultSet) throws SQLException {
        String name = resultSet.getString(propertyNameColumn());
        String value = resultSet.getString(propertyValueColumn());
        return DatabaseProperty.with(name, value);
    }

    /**
     * Prepare statement to query properties from the database.
     * 
     * @param connection The JDBC connection.
     * @param propertyNamesToResolve The names of the properties to 
     * resolve from database.
     * @return The prepared statement to query properties from the database.
     * @throws SQLException if a database-related error has occurred.
     */
    protected PreparedStatement prepareStatement(
            Connection connection,
            Collection<String> propertyNamesToResolve
    ) throws SQLException {
        String query = generateSqlQuery(propertyNamesToResolve);

        PreparedStatement preparedStatement = connection.prepareStatement(query);
        
        configureStatementParameters(preparedStatement, propertyNamesToResolve);

        return preparedStatement;
    }

    /**
     * Generate SQL query to use in querying the properties from database.
     * 
     * @param propertyNamesToResolve The names of the properties to resolve from database.
     * @return The SQL query to use in querying the properties from database.
     */
    protected String generateSqlQuery(Collection<String> propertyNamesToResolve) {
        return String.format(
            DEFAULT_QUERY_TEMPLATE, 
            propertyNameColumnOrThrow(),
            propertyValueColumnOrThrow(),
            tableNameOrThrow(),
            propertyNameColumnOrThrow(),
            buildInClause(propertyNamesToResolve) // Builds ?,?,?...
        );
    }

    /**
     * Set prepared statement parameters.
     * By default this will set all property names to resolve as 
     * string parameters and will set JDBC fetch size to the number 
     * of properties to resolve.
     * 
     * @param preparedStatement The prepared statement.
     * @param propertyNamesToResolve The names of the properties to resolve from database.
     * @throws SQLException if a database-related error has occurred.
     */
    protected void configureStatementParameters(
        PreparedStatement preparedStatement, 
        Collection<String> propertyNamesToResolve
    ) throws SQLException {
        int i = 1;
        for (String propertyName : propertyNamesToResolve) {
            preparedStatement.setString(i++, propertyName);
        }
        preparedStatement.setFetchSize(propertyNamesToResolve.size());
    }

    private String tableNameOrThrow() {
        String tableName = tableName();
        if (tableName == null || tableName.trim().isEmpty()) {
            throw new IllegalStateException(
                "tableName() method must not return null or blank."
            );
        }
        return tableName;
    }

    private String propertyNameColumnOrThrow() {
        String propertyNameColumn = propertyNameColumn();
        if (propertyNameColumn == null || propertyNameColumn.trim().isEmpty()) {
            throw new IllegalStateException(
                "propertyNameColumn() method must not return null or blank."
            );
        }
        return propertyNameColumn;
    }

    private String propertyValueColumnOrThrow() {
        String propertyValueColumn = propertyValueColumn();
        if (propertyValueColumn == null || propertyValueColumn.trim().isEmpty()) {
            throw new IllegalStateException(
                "propertyValueColumn() method must not return null or blank."
            );
        }
        return propertyValueColumn;
    }

    private String buildInClause(Collection<String> propertyNamesToResolve) {
        return propertyNamesToResolve.stream()
            .map(p -> "?")
            .collect(Collectors.joining(","));
    }
}
