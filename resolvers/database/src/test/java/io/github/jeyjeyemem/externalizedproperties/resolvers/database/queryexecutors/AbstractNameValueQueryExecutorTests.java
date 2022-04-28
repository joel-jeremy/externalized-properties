package io.github.jeyjeyemem.externalizedproperties.resolvers.database.queryexecutors;

import io.github.jeyjeyemem.externalizedproperties.resolvers.database.ConnectionProvider;
import io.github.jeyjeyemem.externalizedproperties.resolvers.database.testentities.H2Utils;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class AbstractNameValueQueryExecutorTests {
    // Reuse SimpleNameValueQueryExecutor database elements.
    static final String TABLE = SimpleNameValueQueryExecutor.TABLE;
    static final String PROPERTY_NAME_COLUMN = SimpleNameValueQueryExecutor.PROPERTY_NAME_COLUMN;
    static final String PROPERTY_VALUE_COLUMN = SimpleNameValueQueryExecutor.PROPERTY_VALUE_COLUMN;

    static final int NUMBER_OF_TEST_ENTRIES = 2;

    static final String H2_CONNECTION_STRING = H2Utils.buildConnectionString(
        AbstractNameValueQueryExecutorTests.class.getSimpleName()
    );
    
    static final ConnectionProvider CONNECTION_PROVIDER = 
        H2Utils.createConnectionProvider(
            H2_CONNECTION_STRING, 
            "sa"
        );

    @BeforeAll
    static void setup() throws SQLException {
        createTestDatabaseConfigurationEntries();
    }

    @Nested
    class QueryPropertiesMethod {

        @Test
        @DisplayName("should throw when schema abstract method returns null")
        void test1() {
            AbstractNameValueQueryExecutor queryExecutor = 
                new AbstractNameValueQueryExecutor() {

                    @Override
                    protected String schema() {
                        return null; // Null
                    }

                    @Override
                    protected String table() {
                        return TABLE;
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
        @DisplayName("should throw when table abstract method returns null")
        void test2() {
            AbstractNameValueQueryExecutor queryExecutor = 
                new AbstractNameValueQueryExecutor() {

                    @Override
                    protected String table() {
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
        @DisplayName("should throw when table abstract method returns an empty string")
        void test3() {
            AbstractNameValueQueryExecutor queryExecutor = 
                new AbstractNameValueQueryExecutor() {

                    @Override
                    protected String table() {
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
        void test4() {
            AbstractNameValueQueryExecutor queryExecutor = 
                new AbstractNameValueQueryExecutor() {

                    @Override
                    protected String table() {
                        return TABLE;
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
        void test5() {
            AbstractNameValueQueryExecutor queryExecutor = 
                new AbstractNameValueQueryExecutor() {

                    @Override
                    protected String table() {
                        return TABLE;
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
        void test6() {
            AbstractNameValueQueryExecutor queryExecutor = 
                new AbstractNameValueQueryExecutor() {

                    @Override
                    protected String table() {
                        return TABLE;
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
        void test7() {
            AbstractNameValueQueryExecutor queryExecutor = 
                new AbstractNameValueQueryExecutor() {

                    @Override
                    protected String table() {
                        return TABLE;
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
        void test8() throws SQLException {
            AbstractNameValueQueryExecutor queryExecutor = 
                new AbstractNameValueQueryExecutor() {

                    @Override
                    protected String table() {
                        return TABLE;
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
            
            Map<String, String> resolved = queryExecutor.queryProperties(
                CONNECTION_PROVIDER.getConnection(),
                propertiesToQuery
            );

            assertEquals(propertiesToQuery.size(), resolved.size());
            
            assertEquals(
                "test/property/value/1", 
                resolved.get("test.property.1")
            );

            assertEquals(
                "test/property/value/2", 
                resolved.get("test.property.2")
            );
        }

        @Test
        @DisplayName("should query requested properties from the specified schema")
        void test9() throws SQLException {
            AbstractNameValueQueryExecutor queryExecutor = 
                new AbstractNameValueQueryExecutor() {

                    @Override
                    protected String schema() {
                        return "PUBLIC"; // PUBLIC is H2's default schema.
                    }

                    @Override
                    protected String table() {
                        return TABLE;
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
            
            Map<String, String> resolved = queryExecutor.queryProperties(
                CONNECTION_PROVIDER.getConnection(),
                propertiesToQuery
            );

            assertEquals(propertiesToQuery.size(), resolved.size());
            
            assertEquals(
                "test/property/value/1", 
                resolved.get("test.property.1")
            );

            assertEquals(
                "test/property/value/2", 
                resolved.get("test.property.2")
            );
        }

        @Test
        @DisplayName("should throw when schema is invalid")
        void test10() {
            AbstractNameValueQueryExecutor queryExecutor = 
                new AbstractNameValueQueryExecutor() {

                    @Override
                    protected String schema() {
                        return "NON_EXISTENT_SCHEMA"; // No schema with this name
                    }

                    @Override
                    protected String table() {
                        return TABLE;
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

            assertThrows(
                SQLException.class, 
                () -> queryExecutor.queryProperties(
                    CONNECTION_PROVIDER.getConnection(), 
                    propertiesToQuery
                )
            );
        }

        @Test
        @DisplayName("should throw when table is invalid")
        void test11() {
            AbstractNameValueQueryExecutor queryExecutor = 
                new AbstractNameValueQueryExecutor() {

                    @Override
                    protected String table() {
                        return "NON_EXISTENT_TABLE";
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

            assertThrows(
                SQLException.class, 
                () -> queryExecutor.queryProperties(
                    CONNECTION_PROVIDER.getConnection(), 
                    propertiesToQuery
                )
            );
        }

        @Test
        @DisplayName("should throw when property name column is invalid")
        void test12() {
            AbstractNameValueQueryExecutor queryExecutor = 
                new AbstractNameValueQueryExecutor() {

                    @Override
                    protected String table() {
                        return TABLE;
                    }

                    @Override
                    protected String propertyNameColumn() {
                        return "INVALID_COLUMN";
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

            assertThrows(
                SQLException.class, 
                () -> queryExecutor.queryProperties(
                    CONNECTION_PROVIDER.getConnection(), 
                    propertiesToQuery
                )
            );
        }

        @Test
        @DisplayName("should throw when property name column is invalid")
        void test13() {
            AbstractNameValueQueryExecutor queryExecutor = 
                new AbstractNameValueQueryExecutor() {

                    @Override
                    protected String table() {
                        return TABLE;
                    }

                    @Override
                    protected String propertyNameColumn() {
                        return PROPERTY_NAME_COLUMN;
                    }

                    @Override
                    protected String propertyValueColumn() {
                        return "INVALID_COLUMN";
                    }

                };

            List<String> propertiesToQuery = Arrays.asList(
                "test.property.1",
                "test.property.2"
            );

            assertThrows(
                SQLException.class, 
                () -> queryExecutor.queryProperties(
                    CONNECTION_PROVIDER.getConnection(), 
                    propertiesToQuery
                )
            );
        }
    }

    private static void createTestDatabaseConfigurationEntries() throws SQLException {
        try (Connection connection = CONNECTION_PROVIDER.getConnection()) {

            H2Utils.createPropertiesTable(
                connection, 
                NUMBER_OF_TEST_ENTRIES
            );

            connection.commit();
        }
    }
}
