package io.github.jeyjeyemem.externalizedproperties.resolvers.database;

import io.github.jeyjeyemem.externalizedproperties.core.ResolvedProperty;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collection;
import java.util.List;

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
     * @return The list of {@link ResolvedProperty}.
     * @throws SQLException if a database-related exception occurred.
     */
    List<ResolvedProperty> queryProperties(
        Connection connection, 
        Collection<String> propertyNamesToResolve
    ) throws SQLException;
}
