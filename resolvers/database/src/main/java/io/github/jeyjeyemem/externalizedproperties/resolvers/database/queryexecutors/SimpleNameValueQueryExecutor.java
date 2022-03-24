package io.github.jeyjeyemem.externalizedproperties.resolvers.database.queryexecutors;

import io.github.jeyjeyemem.externalizedproperties.resolvers.database.QueryExecutor;

/**
 * A simple implementation of {@link QueryExecutor} which query properties from
 * "property_name" and "property_value" database columns of the "externalized_properties" table.
 * 
 * @implNote The property columns/table name can be overriden by setting the system property
 * equal to the fully qualified name of this class + the name of the method
 * wanted to be overriden. For example, if {@link #table()} value is to be overriden, the 
 * {@code io.github.jeyjeyemem.externalizedproperties.resolvers.database.queryexecutors.SimpleNameValueQueryExecutor.table} 
 * system property should be set.
 */
public class SimpleNameValueQueryExecutor extends AbstractNameValueQueryExecutor {

    private static final String SCHEMA_SYSTEM_PROPERTY = 
        SimpleNameValueQueryExecutor.class.getName() + ".schema";

    private static final String TABLE_SYSTEM_PROPERTY = 
        SimpleNameValueQueryExecutor.class.getName() + ".table";

    private static final String PROPERTY_NAME_COLUMN_SYSTEM_PROPERTY = 
        SimpleNameValueQueryExecutor.class.getName() + ".propertyNameColumn";
    
    private static final String PROPERTY_VALUE_COLUMN_SYSTEM_PROPERTY = 
        SimpleNameValueQueryExecutor.class.getName() + ".propertyValueColumn";

    public static final String SCHEMA = System.getProperty(
        SCHEMA_SYSTEM_PROPERTY,
        ""
    );

    public static final String TABLE = System.getProperty(
        TABLE_SYSTEM_PROPERTY,
        "externalized_properties"
    );

    public static final String PROPERTY_NAME_COLUMN = System.getProperty(
        PROPERTY_NAME_COLUMN_SYSTEM_PROPERTY,
        "property_name"
    );

    public static final String PROPERTY_VALUE_COLUMN = System.getProperty(
        PROPERTY_VALUE_COLUMN_SYSTEM_PROPERTY,
        "property_value"
    );

    private final String schema;
    private final String table;
    private final String propertyNameColumn;
    private final String propertyValueColumn;

    /**
     * Default constructor. Default database elements will be used.
     */
    public SimpleNameValueQueryExecutor() {
        this(SCHEMA, TABLE, PROPERTY_NAME_COLUMN, PROPERTY_VALUE_COLUMN);
    }

    /**
     * Constructor to override the target database elements.
     * 
     * @param schema The target schema.
     * @param table The target table.
     * @param propertyNameColumn The property name column.
     * @param propertyValueColumn The property value column.
     */
    public SimpleNameValueQueryExecutor(
            String schema,
            String table,
            String propertyNameColumn,
            String propertyValueColumn
    ) {
        if (schema == null) {
            throw new IllegalArgumentException("schema must not be null.");
        }
        if (table == null || table.isEmpty()) {
            throw new IllegalArgumentException("table must not be null or empty.");
        }
        if (propertyNameColumn == null || propertyNameColumn.isEmpty()) {
            throw new IllegalArgumentException(
                "propertyNameColumn must not be null or empty."
            );
        }
        if (propertyValueColumn == null || propertyValueColumn.isEmpty()) {
            throw new IllegalArgumentException(
                "propertyValueColumn must not be null or empty."
            );
        }
        
        this.schema = schema;
        this.table = table;
        this.propertyNameColumn = propertyNameColumn;
        this.propertyValueColumn = propertyValueColumn;
    }

    /** {@inheritDoc} */
    @Override
    protected String schema() {
        return schema;
    }

    /** {@inheritDoc} */
    @Override
    protected String table() {
        return table;
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
