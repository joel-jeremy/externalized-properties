package io.github.joeljeremy7.externalizedproperties.resolvers.database.queryexecutors;

import io.github.joeljeremy7.externalizedproperties.resolvers.database.DatabaseProperty;
import io.github.joeljeremy7.externalizedproperties.resolvers.database.QueryExecutor;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Abstract query executor that will build and run a database query based on
 * the specified table name and it's property name and value column mappings.
 */
public abstract class AbstractNameValueQueryExecutor implements QueryExecutor {
    /** {@inheritDoc} */
    @Override
    public Map<String, String> queryProperties(
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
     * The target database schema. 
     * By default, this will return an empty string.
     * 
     * @return The target database schema.
     */
    protected String schema() {
        return "";
    }

    /**
     * The target database table.
     * 
     * @return The target database table.
     */
    protected abstract String table();

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
     * @return The map of properties resolved from the database.
     * @throws SQLException if a database-related error has occurred.
     */
    protected Map<String, String> runQuery(
            PreparedStatement preparedStatement
    ) throws SQLException {
        Map<String, String> resolved = new HashMap<>();
        try (ResultSet resultSet = preparedStatement.executeQuery()) {
            while (resultSet.next()) {
                DatabaseProperty prop = mapResult(resultSet);
                resolved.put(prop.name(), prop.value());
            }
        }
        return Collections.unmodifiableMap(resolved);
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
     * The resulting SQL query will be processed via {@link Connection#prepareStatement(String)}
     * so it may contain property name placeholders which will then be set via
     * {@link #configureStatementParameters}.
     * 
     * @param propertyNamesToResolve The names of the properties to resolve from database.
     * @return The SQL query to use in querying the properties from database.
     */
    protected String generateSqlQuery(Collection<String> propertyNamesToResolve) {
        String schema = schemaOrThrow();
        if (!schema.trim().isEmpty()) {
            // The order of format arguments:
            // 1. propertyNameColumn
            // 2. propertyValueColumn
            // 3. schema
            // 4. table
            // 5. propertyNameColumn
            // 6. propertyNamesStatementParameter
            return String.format(
                "SELECT t.%s, t.%s FROM %s.%s t WHERE t.%s IN (%s)", 
                /** SELECT */ propertyNameColumnOrThrow(),
                propertyValueColumnOrThrow(),
                /** FROM */ schema,
                tableOrThrow(),
                /** WHERE */ propertyNameColumnOrThrow(),
                /** IN */ buildInClause(propertyNamesToResolve)
            );
        }
        // The order of format arguments:
        // 1. propertyNameColumn
        // 2. propertyValueColumn
        // 3. table
        // 4. propertyNameColumn
        // 5. propertyNamesStatementParameter
        return String.format(
            "SELECT t.%s, t.%s FROM %s t WHERE t.%s IN (%s)", 
            /** SELECT */ propertyNameColumnOrThrow(),
            propertyValueColumnOrThrow(),
            /** FROM */ tableOrThrow(),
            /** WHERE */ propertyNameColumnOrThrow(),
            /** IN */ buildInClause(propertyNamesToResolve)
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

    private String schemaOrThrow() {
        String schema = schema();
        if (schema == null) {
            throw new IllegalStateException(
                "schema() method must not return null."
            );
        }
        return schema;
    }

    private String tableOrThrow() {
        String table = table();
        if (table == null || table.trim().isEmpty()) {
            throw new IllegalStateException(
                "table() method must not return null or blank."
            );
        }
        return table;
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
