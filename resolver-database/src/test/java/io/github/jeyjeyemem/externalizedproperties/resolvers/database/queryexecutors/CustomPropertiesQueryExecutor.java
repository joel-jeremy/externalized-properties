package io.github.jeyjeyemem.externalizedproperties.resolvers.database.queryexecutors;

import io.github.jeyjeyemem.externalizedproperties.core.ResolvedProperty;
import io.github.jeyjeyemem.externalizedproperties.resolvers.database.QueryExecutor;
import io.github.jeyjeyemem.externalizedproperties.resolvers.database.entities.CustomPropertiesEntity;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public class CustomPropertiesQueryExecutor implements QueryExecutor {

    @Override
    public List<ResolvedProperty> queryProperties(
            EntityManager entityManager,
            Collection<String> configKeys
    ) {
        TypedQuery<CustomPropertiesEntity> query = entityManager.createNamedQuery(
            CustomPropertiesEntity.FIND_BY_CONFIG_KEYS,
            CustomPropertiesEntity.class
        )
        .setParameter("configKeys", configKeys)
        .setMaxResults(configKeys.size());

        List<CustomPropertiesEntity> customProps = query.getResultList();
        return customProps.stream()
            .map(this::mapResult)
            .collect(Collectors.toList());
    }

    private ResolvedProperty mapResult(CustomPropertiesEntity customEntity) {
        return ResolvedProperty.with(
            customEntity.getConfigKey(), 
            customEntity.getConfig()
        );
    }
    
}
