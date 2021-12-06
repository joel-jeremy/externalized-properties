package io.github.jeyjeyemem.externalizedproperties.resolvers.database.queryexecutors;

import io.github.jeyjeyemem.externalizedproperties.resolvers.database.ConnectionProvider;
import io.github.jeyjeyemem.externalizedproperties.resolvers.database.DatabaseProperty;
import io.github.jeyjeyemem.externalizedproperties.resolvers.database.testentities.H2DataSourceConnectionProvider;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class AbstractNameValueQueryExecutorTests {
    private static final String PROPERTY_VALUE_COLUMN = "value";
    private static final String PROPERTY_NAME_COLUMN = "name";
    private static final String TABLE_NAME = "externalized_properties";
    private static final int NUMBER_OF_TEST_ENTRIES = 2;
    // Use DB_CLOSE_DELAY=-1 so that h2 in-memory database contents are not lost when closing. 
    private static final ConnectionProvider CONNECTION_PROVIDER = 
        new H2DataSourceConnectionProvider(
            "jdbc:h2:mem:AbstractNameValueQueryExecutorTests;DB_CLOSE_DELAY=-1", 
            "sa", 
            ""
        );

    @BeforeAll
    public static void setup() throws SQLException {
        createTestDatabaseConfigurationEntries();
    }

    @Nested
    class QueryPropertiesMethod {

        @Test
        @DisplayName("should throw when tableName abstract method returns null")
        public void test1() {
            AbstractNameValueQueryExecutor queryExecutor = 
                new AbstractNameValueQueryExecutor() {

                    @Override
                    protected String tableName() {
                        return null; // Null
                    }

                    @Override
                    protected String propertyNameColumn() {
                        return PROPERTY_NAME_COLUMN;
                    }

                    @Override
                    protected String propertyValueColumn() {
                        return PROPERTY_VALUE_COLUMN;
                    }

                };

            assertThrows(IllegalStateException.class, 
                () -> queryExecutor.queryProperties(
                    CONNECTION_PROVIDER.getConnection(),
                    Arrays.asList("test.property.1")
                )
            );
        }

        @Test
        @DisplayName("should throw when tableName abstract method returns an empty string")
        public void test2() {
            AbstractNameValueQueryExecutor queryExecutor = 
                new AbstractNameValueQueryExecutor() {

                    @Override
                    protected String tableName() {
                        return ""; // Empty string
                    }

                    @Override
                    protected String propertyNameColumn() {
                        return PROPERTY_NAME_COLUMN;
                    }

                    @Override
                    protected String propertyValueColumn() {
                        return PROPERTY_VALUE_COLUMN;
                    }

                };

            assertThrows(IllegalStateException.class, 
                () -> queryExecutor.queryProperties(
                    CONNECTION_PROVIDER.getConnection(),
                    Arrays.asList("test.property.1")
                )
            );
        }

        @Test
        @DisplayName("should throw when propertyNameColumn abstract method returns null")
        public void test3() {
            AbstractNameValueQueryExecutor queryExecutor = 
                new AbstractNameValueQueryExecutor() {

                    @Override
                    protected String tableName() {
                        return TABLE_NAME;
                    }

                    @Override
                    protected String propertyNameColumn() {
                        return null; // Null
                    }

                    @Override
                    protected String propertyValueColumn() {
                        return PROPERTY_VALUE_COLUMN;
                    }

                };

            assertThrows(IllegalStateException.class, 
                () -> queryExecutor.queryProperties(
                    CONNECTION_PROVIDER.getConnection(),
                    Arrays.asList("test.property.1")
                )
            );
        }

        @Test
        @DisplayName("should throw when propertyNameColumn abstract method returns an empty string")
        public void test4() {
            AbstractNameValueQueryExecutor queryExecutor = 
                new AbstractNameValueQueryExecutor() {

                    @Override
                    protected String tableName() {
                        return TABLE_NAME;
                    }

                    @Override
                    protected String propertyNameColumn() {
                        return ""; // Empty string
                    }

                    @Override
                    protected String propertyValueColumn() {
                        return PROPERTY_VALUE_COLUMN;
                    }

                };

            assertThrows(IllegalStateException.class, 
                () -> queryExecutor.queryProperties(
                    CONNECTION_PROVIDER.getConnection(),
                    Arrays.asList("test.property.1")
                )
            );
        }

        @Test
        @DisplayName("should throw when propertyValueColumn abstract method returns null")
        public void test5() {
            AbstractNameValueQueryExecutor queryExecutor = 
                new AbstractNameValueQueryExecutor() {

                    @Override
                    protected String tableName() {
                        return TABLE_NAME;
                    }

                    @Override
                    protected String propertyNameColumn() {
                        return PROPERTY_NAME_COLUMN;
                    }

                    @Override
                    protected String propertyValueColumn() {
                        return null; // Null
                    }

                };

            assertThrows(IllegalStateException.class, 
                () -> queryExecutor.queryProperties(
                    CONNECTION_PROVIDER.getConnection(),
                    Arrays.asList("test.property.1")
                )
            );
        }

        @Test
        @DisplayName("should throw when propertyValueColumn abstract method returns an empty string")
        public void test6() {
            AbstractNameValueQueryExecutor queryExecutor = 
                new AbstractNameValueQueryExecutor() {

                    @Override
                    protected String tableName() {
                        return TABLE_NAME;
                    }

                    @Override
                    protected String propertyNameColumn() {
                        return PROPERTY_NAME_COLUMN;
                    }

                    @Override
                    protected String propertyValueColumn() {
                        return ""; // Empty string
                    }

                };

            assertThrows(IllegalStateException.class, 
                () -> queryExecutor.queryProperties(
                    CONNECTION_PROVIDER.getConnection(),
                    Arrays.asList("test.property.1")
                )
            );
        }

        @Test
        @DisplayName("should query requested properties")
        public void test7() throws SQLException {
            AbstractNameValueQueryExecutor queryExecutor = 
                new AbstractNameValueQueryExecutor() {

                    @Override
                    protected String tableName() {
                        return TABLE_NAME;
                    }

                    @Override
                    protected String propertyNameColumn() {
                        return PROPERTY_NAME_COLUMN;
                    }

                    @Override
                    protected String propertyValueColumn() {
                        return PROPERTY_VALUE_COLUMN;
                    }

                };

                List<String> propertiesToQuery = Arrays.asList(
                    "test.property.1",
                    "test.property.2"
                );
                
                List<DatabaseProperty> resolved = queryExecutor.queryProperties(
                    CONNECTION_PROVIDER.getConnection(),
                    propertiesToQuery
                );

                assertEquals(propertiesToQuery.size(), resolved.size());
                
                assertEquals(
                    "test/property/value/1", 
                    resolved.stream()
                        .filter(rp -> rp.name().equals("test.property.1"))
                        .map(DatabaseProperty::value)
                        .findFirst()
                        .orElse(null)
                );

                assertEquals(
                    "test/property/value/2", 
                    resolved.stream()
                        .filter(rp -> rp.name().equals("test.property.2"))
                        .map(DatabaseProperty::value)
                        .findFirst()
                        .orElse(null)
                );
        }
    }

    private static void createTestDatabaseConfigurationEntries() throws SQLException {
        try (Connection connection = CONNECTION_PROVIDER.getConnection()) {

            initialExternalizedPropertiesTable(connection);

            connection.commit();
        }
    }

    private static void initialExternalizedPropertiesTable(
            Connection connection
    ) throws SQLException {
        // Matches the default columns and table name in
        // SimpleNameValueQueryExecutor
        PreparedStatement createTable = connection.prepareStatement(
            "CREATE TABLE IF NOT EXISTS " + TABLE_NAME + " ( " +
            PROPERTY_NAME_COLUMN + " VARCHAR(255), " +
            PROPERTY_VALUE_COLUMN + " VARCHAR(255), " +
            "description VARCHAR(255))"
        );
        createTable.executeUpdate();

        PreparedStatement insert = connection.prepareStatement(
            "INSERT INTO " + TABLE_NAME + " VALUES(?,?,?)"
        );

        for (int i = 1; i <= NUMBER_OF_TEST_ENTRIES; i++) {
            insert.setString(1, "test.property." + i);
            insert.setString(2, "test/property/value/" + i);
            insert.setString(3, "Test property " + i + " description");
            insert.executeUpdate();
        }
    }
}
