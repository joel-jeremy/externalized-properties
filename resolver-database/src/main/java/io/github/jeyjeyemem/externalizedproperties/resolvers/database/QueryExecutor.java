package io.github.jeyjeyemem.externalizedproperties.resolvers.database;

import io.github.jeyjeyemem.externalizedproperties.core.ResolvedProperty;
import jakarta.persistence.EntityManager;

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
     * @param entityManager The entity manager.
     * @param propertyNamesToResolve The names of the properties to resolve.
     * @return The list of {@link ResolvedProperty}.
     */
    List<ResolvedProperty> queryProperties(
        EntityManager entityManager, 
        Collection<String> propertyNamesToResolve
    );
}
