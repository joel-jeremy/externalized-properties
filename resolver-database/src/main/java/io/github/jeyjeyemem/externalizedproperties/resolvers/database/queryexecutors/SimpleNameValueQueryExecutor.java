package io.github.jeyjeyemem.externalizedproperties.resolvers.database.queryexecutors;

import io.github.jeyjeyemem.externalizedproperties.resolvers.database.QueryExecutor;

/**
 * A simple implementation of {@link QueryExecutor} which query properties from
 * "name" and "value" database columns of the "externalized_properties" table.
 * 
 * @implNote The property columns/table name can be overriden by setting the system property
 * equal to the fully qualified name of this class + the name of the method
 * wanted to be overriden. For example, if {@link #tableName()} value is to be overriden, the 
 * {@code io.github.jeyjeyemem.externalizedproperties.resolvers.database.queryexecutors.SimpleNameValueQueryExecutor.tableName} 
 * system property should be set.
 */
public class SimpleNameValueQueryExecutor extends AbstractNameValueQueryExecutor {

    private static final String TABLE_NAME_SYSTEM_PROPERTY = 
        SimpleNameValueQueryExecutor.class.getName() + ".tableName";

    private static final String PROPERTY_NAME_COLUMN_SYSTEM_PROPERTY = 
        SimpleNameValueQueryExecutor.class.getName() + ".propertyNameColumn";
    
    private static final String PROPERTY_VALUE_COLUMN_SYSTEM_PROPERTY = 
        SimpleNameValueQueryExecutor.class.getName() + ".propertyValueColumn";

    private final String tableName = System.getProperty(
        TABLE_NAME_SYSTEM_PROPERTY,
        "externalized_properties"
    );

    private final String propertyNameColumn = System.getProperty(
        PROPERTY_NAME_COLUMN_SYSTEM_PROPERTY,
        "name"
    );

    private final String propertyValueColumn = System.getProperty(
        PROPERTY_VALUE_COLUMN_SYSTEM_PROPERTY,
        "value"
    );

    /** {@inheritDoc} */
    @Override
    protected String tableName() {
        return tableName;
    }

    /** {@inheritDoc} */
    @Override
    protected String propertyNameColumn() {
        return propertyNameColumn;
    }

    /** {@inheritDoc} */
    @Override
    protected String propertyValueColumn() {
        return propertyValueColumn;
    }
}
