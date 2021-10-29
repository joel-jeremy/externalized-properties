package io.github.jeyjeyemem.externalizedproperties.resolvers.database.queryexecutors;

import io.github.jeyjeyemem.externalizedproperties.core.ResolvedProperty;
import io.github.jeyjeyemem.externalizedproperties.resolvers.database.QueryExecutor;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Tuple;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.metamodel.EntityType;
import org.apache.commons.text.StringSubstitutor;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Abstract query executor that will build and run a database query based on
 * the specified entity class and it's property name and value column mappings.
 */
public abstract class AbstractNameValueQueryExecutor implements QueryExecutor {
    private static final String QUERY_TEMPLATE = 
        "SELECT entity.${propertyNameColumn} as ${propertyNameColumn}, " +
        "entity.${propertyValueColumn} as ${propertyValueColumn} " + 
        "FROM ${entityName} entity " +
        "WHERE entity.${propertyNameColumn} IN :${propertyNamesQueryParameterName}";

    private final ConcurrentHashMap<Class<?>, String> entityNamesByClass = new ConcurrentHashMap<>();

    /**
     * Constructor.
     */
    public AbstractNameValueQueryExecutor() {
        if (entityClass() == null) {
            throw new IllegalStateException("entityClass() must not return null.");
        }

        if (propertyNameColumn() == null) {
            throw new IllegalStateException("propertyNameColumn() must not return null.");
        }

        if (propertyValueColumn() == null) {
            throw new IllegalStateException("propertyValueColumn() must not return null.");
        }
    }

    /**
     * Query properties from the database using the specified entity class
     * and it's property name and value column mappings.
     */
    @Override
    public List<ResolvedProperty> queryProperties(
            EntityManager entityManager,
            Collection<String> propertyNamesToResolve
    ) {
        return runQuery(
            buildQuery(entityManager, propertyNamesToResolve)
        );
    }

    /**
     * The JPA entity class. This must be a valid/managed JPA entity that
     * is registered in the persistence configuration.
     * 
     * @return The entity class that is managed by JPA.
     */
    protected abstract Class<?> entityClass();

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
     * Name of the property names parameter in the JPQL.
     * 
     * @return The name of the property names parameter in the JPQL.
     */
    protected String propertyNamesQueryParameterName() {
        return "propertyNames";
    }

    /**
     * Build a query to resolve the specified property names.
     * 
     * @param entityManager The entity manager for this query.
     * @param propertyNamesToResolve The names of the properties to be resolved from the database.
     * @return The built query.
     */
    protected TypedQuery<Tuple> buildQuery(
            EntityManager entityManager, 
            Collection<String> propertyNamesToResolve
    ) {
        String jpql = buildJpql(entityManager);

        return entityManager.createQuery(jpql, Tuple.class)
            .setParameter(propertyNamesQueryParameterName(), propertyNamesToResolve)
            .setMaxResults(propertyNamesToResolve.size());
    }

    /**
     * Run the built query.
     * 
     * @param query The built query.
     * @return The list of properties resolved from the database.
     */
    protected List<ResolvedProperty> runQuery(TypedQuery<Tuple> query) {
        List<Tuple> result = query.getResultList();
        return result.stream()
            .map(this::mapResult)
            .collect(Collectors.toList());
    }

    /**
     * Map the query result to a {@link ResolvedProperty}.
     * 
     * @param tuple The query result in the form of a tuple.
     * @return The mapped {@link ResolvedProperty}.
     */
    protected ResolvedProperty mapResult(Tuple tuple) {
        String name = tuple.get(propertyNameColumn(), String.class);
        String value = tuple.get(propertyValueColumn(), String.class);
        return ResolvedProperty.with(name, value);
    }

    /**
     * Get name of managed JPA entity.
     * 
     * @param entityManager The entity manager.
     * @param entityClass The entity class.
     * @return The name of the managed JPA entity.
     */
    private Optional<String> getManagedEntityName(EntityManager entityManager, Class<?> entityClass) {
        Set<EntityType<?>> jpaEntities = entityManager.getMetamodel().getEntities();
        // Cache entity names for faster subsequent invocations.
        String entityName = entityNamesByClass.computeIfAbsent(entityClass, 
            ec -> jpaEntities.stream()
                .filter(je -> ec.equals(je.getJavaType()))
                .map(je -> je.getName())
                .findFirst()
                .orElse(null)
        );

        return Optional.ofNullable(entityName);
    }

    private String buildJpql(EntityManager entityManager) {
        String entityName = getManagedEntityName(entityManager, entityClass())
            .orElseThrow(() -> new IllegalStateException(
                "Invalid JPA entity returned by entityClass() method. Please check persistence configuration."
            ));

        Map<String, String> variables = new HashMap<>();
        variables.put("propertyNameColumn", propertyNameColumn());
        variables.put("propertyValueColumn", propertyValueColumn());
        variables.put("entityName", entityName);
        variables.put("propertyNamesQueryParameterName", propertyNamesQueryParameterName());

        return StringSubstitutor.replace(QUERY_TEMPLATE, variables);
    }
}
