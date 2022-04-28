package io.github.joeljeremy7.externalizedproperties.resolvers.database;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Map;

/**
 * Runs the query to retrieve the specified properties from database.
 * 
 * @apiNote As best practice, implementations should set the 
 * max results on the query to be used in order to efficiently 
 * handle resolution of properties from the database.
 */
public interface QueryExecutor {
    /**
     * Query properties from the database.
     * 
     * @param connection The JDBC connection.
     * @param propertyNamesToResolve The names of the properties to resolve.
     * @return The map of resolved database properties.
     * @throws SQLException if a database-related exception occurred.
     */
    Map<String, String> queryProperties(
        Connection connection, 
        Collection<String> propertyNamesToResolve
    ) throws SQLException;
}
