package io.github.jeyjeyemem.externalizedproperties.resolvers.database.queryexecutors;

import io.github.jeyjeyemem.externalizedproperties.resolvers.database.QueryExecutor;
import io.github.jeyjeyemem.externalizedproperties.resolvers.database.entities.SimpleNameValuePropertyEntity;

/**
 * A simple implementation of {@link QueryExecutor} which query properties from a
 * "name" and "value" database column. This will use the simple built-in JPA entity 
 * {@link SimpleNameValuePropertyEntity} to build and run the database queries.
 */
public class SimpleNameValueQueryExecutor extends AbstractNameValueQueryExecutor {

    /** {@inheritDoc} */
    @Override
    protected Class<?> entityClass() {
        return SimpleNameValuePropertyEntity.class;
    }

    /** {@inheritDoc} */
    @Override
    protected String propertyNameColumn() {
        return "name";
    }

    /** {@inheritDoc} */
    @Override
    protected String propertyValueColumn() {
        return "value";
    }
}
