package io.github.jeyjeyemem.externalizedproperties.resolvers.database;

import io.github.jeyjeyemem.externalizedproperties.core.ExternalizedPropertiesException;
import io.github.jeyjeyemem.externalizedproperties.core.Resolver;
import io.github.jeyjeyemem.externalizedproperties.core.ResolverResult;
import io.github.jeyjeyemem.externalizedproperties.resolvers.database.queryexecutors.SimpleNameValueQueryExecutor;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

/**
 * {@link Resolver} implementation which resolves requested properties 
 * from a database.
 */
public class DatabaseResolver implements Resolver {
    private final ConnectionProvider connectionProvider;
    private final QueryExecutor queryExecutor;

    /**
     * Constructor. This will use {@link SimpleNameValueQueryExecutor} to
     * execute queries when resolving properties.
     * 
     * @param connectionProvider The connection provider.
     */
    public DatabaseResolver(ConnectionProvider connectionProvider) {
        this(connectionProvider, new SimpleNameValueQueryExecutor());
    }

    /**
     * Constructor.
     * 
     * @param connectionProvider The connection provider.
     * @param queryExecutor The query executor to resolve properties from the database.
     */
    public DatabaseResolver(
            ConnectionProvider connectionProvider,
            QueryExecutor queryExecutor
    ) {
        if (connectionProvider == null) {
            throw new IllegalArgumentException("connectionProvider must not be null.");
        }
        if (queryExecutor == null) {
            throw new IllegalArgumentException("queryExecutor must not be null.");
        }
        this.connectionProvider = connectionProvider;
        this.queryExecutor = queryExecutor;
    }

    /**
     * Resolve property from database.
     * 
     * @return The {@link ResolverResult} which contains the resolved properties
     * and unresolved properties, if there are any.
     */
    @Override
    public Optional<String> resolve(String propertyName) {
        if (propertyName == null || propertyName.trim().isEmpty()) {
            throw new IllegalArgumentException("propertyName must not be null or empty.");
        }
        
        try {
            ResolverResult result = getFromDatabase(Arrays.asList(propertyName));
            return result.findResolvedProperty(propertyName);
        } catch (SQLException e) {
            throw new ExternalizedPropertiesException(
                "Exception occurred while trying to resolve properties from database.",
                e
            );
        }
    }

    /**
     * Resolve properties from database.
     * 
     * @return The {@link ResolverResult} which contains the resolved properties
     * and unresolved properties, if there are any.
     */
    @Override
    public ResolverResult resolve(Collection<String> propertyNames) {
        if (propertyNames == null || propertyNames.isEmpty()) {
            throw new IllegalArgumentException("propertyNames must not be null or empty.");
        }
        if (propertyNames.stream().anyMatch(pn -> pn == null || pn.trim().isEmpty())) {
            throw new IllegalArgumentException("propertyNames must not contain null or empty values.");
        }
        try {
            return getFromDatabase(propertyNames);
        } catch (SQLException e) {
            throw new ExternalizedPropertiesException(
                "Exception occurred while trying to resolve properties from database.",
                e
            );
        }
    }

    private ResolverResult getFromDatabase(Collection<String> propertyNames) throws SQLException {
        try (Connection connection = connectionProvider.getConnection()) {
            List<DatabaseProperty> resolvedProperties = 
                queryExecutor.queryProperties(connection, propertyNames);

            ResolverResult.Builder resultBuilder = ResolverResult.builder(propertyNames);
            
            resolvedProperties.forEach(resolved -> 
                resultBuilder.add(resolved.name(), resolved.value())
            );
            
            return resultBuilder.build();
        }
    }
}