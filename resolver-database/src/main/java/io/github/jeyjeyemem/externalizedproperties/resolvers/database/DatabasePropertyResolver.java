package io.github.jeyjeyemem.externalizedproperties.resolvers.database;

import io.github.jeyjeyemem.externalizedproperties.core.ExternalizedPropertyResolver;
import io.github.jeyjeyemem.externalizedproperties.core.ExternalizedPropertyResolverResult;
import io.github.jeyjeyemem.externalizedproperties.core.ResolvedProperty;
import io.github.jeyjeyemem.externalizedproperties.resolvers.database.queryexecutors.SimpleNameValueQueryExecutor;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * {@link ExternalizedPropertyResolver} implementation which resolves requested properties 
 * from a database.
 */
public class DatabasePropertyResolver implements ExternalizedPropertyResolver {
    private final EntityManagerFactory entityManagerFactory;
    private final QueryExecutor queryRunner;

    /**
     * Constructor.
     * 
     * @param entityManagerFactory The entity manager factory.
     */
    public DatabasePropertyResolver(EntityManagerFactory entityManagerFactory) {
        this(
            entityManagerFactory, 
            new SimpleNameValueQueryExecutor()
        );
    }

    /**
     * Constructor.
     * 
     * @param entityManagerFactory The entity manager factory.
     * @param queryRunner The query runner which handles the actual database query.
     */
    public DatabasePropertyResolver(
            EntityManagerFactory entityManagerFactory,
            QueryExecutor queryRunner
    ) {
        if (entityManagerFactory == null) {
            throw new IllegalArgumentException("entityManagerFactory must not be null.");
        }

        if (queryRunner == null) {
            throw new IllegalArgumentException("queryRunner must not be null.");
        }
        
        this.entityManagerFactory = entityManagerFactory;
        this.queryRunner = queryRunner;
    }

    /**
     * Resolve property from database.
     * 
     * @return The {@link ExternalizedPropertyResolverResult} which contains the resolved properties
     * and unresolved properties, if there are any.
     */
    @Override
    public Optional<ResolvedProperty> resolve(String propertyName) {
        if (propertyName == null || propertyName.isEmpty()) {
            throw new IllegalArgumentException("propertyName must not be null or empty.");
        }
        ExternalizedPropertyResolverResult result = getFromDatabase(Arrays.asList(propertyName));
        return result.findResolvedProperty(propertyName);
    }

    /**
     * Resolve properties from database.
     * 
     * @return The {@link ExternalizedPropertyResolverResult} which contains the resolved properties
     * and unresolved properties, if there are any.
     */
    @Override
    public ExternalizedPropertyResolverResult resolve(
            Collection<String> propertyNames
    ) {
        if (propertyNames == null || propertyNames.isEmpty()) {
            throw new IllegalArgumentException("propertyNames must not be null or empty.");
        }
        if (propertyNames.stream().anyMatch(Objects::isNull)) {
            throw new IllegalArgumentException("propertyNames must not contain null values.");
        }
        return getFromDatabase(propertyNames);
    }

    private ExternalizedPropertyResolverResult getFromDatabase(
            Collection<String> propertyNames
    ) {
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        try {
            List<ResolvedProperty> resolvedProperties = 
                queryRunner.queryProperties(entityManager, propertyNames);

            return new ExternalizedPropertyResolverResult(
                propertyNames,
                resolvedProperties
            );
        } finally {
            if (entityManager.isOpen()) {
                entityManager.close();
            }
        }
    }
}