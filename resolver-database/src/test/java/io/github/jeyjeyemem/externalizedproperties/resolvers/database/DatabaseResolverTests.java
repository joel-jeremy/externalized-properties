package io.github.jeyjeyemem.externalizedproperties.resolvers.database;

import io.github.jeyjeyemem.externalizedproperties.core.exceptions.ExternalizedPropertiesException;
import io.github.jeyjeyemem.externalizedproperties.resolvers.database.queryexecutors.AbstractNameValueQueryExecutor;
import io.github.jeyjeyemem.externalizedproperties.resolvers.database.queryexecutors.SimpleNameValueQueryExecutor;
import io.github.jeyjeyemem.externalizedproperties.resolvers.database.testentities.H2DataSourceConnectionProvider;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class DatabaseResolverTests {

    private static final int NUMBER_OF_TEST_ENTRIES = 2;
    // Use DB_CLOSE_DELAY=-1 so that h2 in-memory database contents are not lost when closing. 
    private static final ConnectionProvider CONNECTION_PROVIDER = 
        new H2DataSourceConnectionProvider(
            "jdbc:h2:mem:DatabasePropertyResolverTests;DB_CLOSE_DELAY=-1", 
            "sa", 
            ""
        );
    
    @BeforeAll
    public static void setup() throws SQLException {
        createTestDatabaseConfigurationEntries();
    }

    @Nested
    class Constructor {
        @Test
        @DisplayName("should throw when connection provider argument is null")
        public void test1() {
            assertThrows(IllegalArgumentException.class, () -> {
                new DatabaseResolver(null, new SimpleNameValueQueryExecutor());
            });
        }

        @Test
        @DisplayName("should throw when query runner is null")
        public void test2() {
            assertThrows(IllegalArgumentException.class, () -> {
                new DatabaseResolver(CONNECTION_PROVIDER, null);
            });
        }
    }

    @Nested
    class ResolveMethod {
        @Test
        @DisplayName("should throw when propertyName argument is null")
        public void test1() {
            DatabaseResolver databasePropertyResolver = 
                new DatabaseResolver(CONNECTION_PROVIDER);

            assertThrows(IllegalArgumentException.class, () -> {
                databasePropertyResolver.resolve((String)null);
            });
        }

        @Test
        @DisplayName("should throw when propertyName argument is blank")
        public void test2() {
            DatabaseResolver databasePropertyResolver = 
                new DatabaseResolver(CONNECTION_PROVIDER);
                
            assertThrows(IllegalArgumentException.class, () -> {
                databasePropertyResolver.resolve("");
            });

            assertThrows(IllegalArgumentException.class, () -> {
                databasePropertyResolver.resolve(" ");
            });
        }

        @Test
        @DisplayName("should resolve all properties from database")
        public void test3() {
            DatabaseResolver databasePropertyResolver = 
                new DatabaseResolver(CONNECTION_PROVIDER);
            
            String propertyName = "test.property.1";

            Optional<String> result = databasePropertyResolver.resolve(propertyName);
            
            assertTrue(result.isPresent());
            assertNotNull(result.get());
        }

        @Test
        @DisplayName("should return empty Optional when property is not found in database")
        public void test4() {
            DatabaseResolver databasePropertyResolver = 
                new DatabaseResolver(CONNECTION_PROVIDER);

            String propertyName = "non.existent.property";

            Optional<String> result = databasePropertyResolver.resolve(propertyName);

            assertFalse(result.isPresent());
        }

        @Test
        @DisplayName("should use provided custom query executor")
        public void test5() {
            DatabaseResolver databasePropertyResolver = 
                new DatabaseResolver(
                    CONNECTION_PROVIDER,
                    new AbstractNameValueQueryExecutor() {
                        @Override
                        protected String tableName() {
                            return "custom_properties_table";
                        }

                        @Override
                        protected String propertyNameColumn() {
                            return "config_key";
                        }

                        @Override
                        protected String propertyValueColumn() {
                            return "config_value";
                        }
                    }
                );

            String propertyName = "custom.table.test.property.1";

            Optional<String> result = databasePropertyResolver.resolve(propertyName);

            assertTrue(result.isPresent());
            assertNotNull(result.get());
        }

        @Test
        @DisplayName("should wrap and propagate any SQL exceptions")
        public void test6() {
            // Invalid credentials to simulate SQL exception.
            ConnectionProvider invalidConnectionProvider = 
                new H2DataSourceConnectionProvider(
                    "jdbc:h2:mem:test;DB_CLOSE_DELAY=-1", 
                    "invalid_user", 
                    "invalid_password"
                );
            
            DatabaseResolver databasePropertyResolver = 
                new DatabaseResolver(invalidConnectionProvider);

            String propertyName = "test.property";

            ExternalizedPropertiesException exception = assertThrows(
                ExternalizedPropertiesException.class, 
                () -> databasePropertyResolver.resolve(propertyName)
            );

            assertTrue(exception.getCause() instanceof SQLException);
        }
    }

    @Nested
    class ResolveMethodWithCollectionOverload {
        @Test
        @DisplayName("should throw when propertyNames argument is null")
        public void test1() {
            DatabaseResolver databasePropertyResolver = 
                new DatabaseResolver(CONNECTION_PROVIDER);

            assertThrows(IllegalArgumentException.class, () -> {
                databasePropertyResolver.resolve((Collection<String>)null);
            });
        }

        @Test
        @DisplayName("should throw when propertyNames argument is empty")
        public void test2() {
            DatabaseResolver databasePropertyResolver = 
                new DatabaseResolver(CONNECTION_PROVIDER);
                
            assertThrows(IllegalArgumentException.class, () -> {
                databasePropertyResolver.resolve(Collections.emptyList());
            });
        }

        @Test
        @DisplayName("should throw when propertyNames argument contains null or blank values")
        public void test3() {
            DatabaseResolver databasePropertyResolver = 
                new DatabaseResolver(CONNECTION_PROVIDER);

            List<String> propertiesToResolve = Arrays.asList(
                null, 
                "", 
                " ", 
                "test.property"
            );

            assertThrows(IllegalArgumentException.class, () -> {
                databasePropertyResolver.resolve(propertiesToResolve);
            });
        }

        @Test
        @DisplayName("should resolve all properties from database")
        public void test4() {
            DatabaseResolver databasePropertyResolver = 
                new DatabaseResolver(CONNECTION_PROVIDER);

            List<String> propertiesToResolve = Arrays.asList(
                "test.property.1", 
                "test.property.2"
            );
            
            DatabaseResolver.Result result = 
                databasePropertyResolver.resolve(propertiesToResolve);
            
            assertTrue(result.hasResolvedProperties());
            assertFalse(result.hasUnresolvedProperties());

            assertTrue(result.resolvedPropertyNames().stream()
                .allMatch(resolved -> propertiesToResolve.contains(resolved))
            );

            assertEquals(
                "test/property/value/1", 
                result.findRequiredProperty("test.property.1")
            );

            assertEquals(
                "test/property/value/2", 
                result.findRequiredProperty("test.property.2")
            );
        }

        @Test
        @DisplayName(
            "should return result with resolved and unresolved properties from database"
        )
        public void test5() {
            DatabaseResolver databasePropertyResolver = 
                new DatabaseResolver(CONNECTION_PROVIDER);

            List<String> propertiesToResolve = Arrays.asList(
                "test.property.1", 
                "test.property.2",
                "non.existent.property"
            );

            DatabaseResolver.Result result = 
                databasePropertyResolver.resolve(propertiesToResolve);

            assertTrue(result.hasResolvedProperties());
            assertTrue(result.hasUnresolvedProperties());

            assertEquals(
                "test/property/value/1", 
                result.findRequiredProperty("test.property.1")
            );

            assertEquals(
                "test/property/value/2", 
                result.findRequiredProperty("test.property.2")
            );

            assertTrue(result.unresolvedPropertyNames().contains("non.existent.property"));
        }

        @Test
        @DisplayName("should use provided custom query executor")
        public void test6() {
            DatabaseResolver databasePropertyResolver = 
                new DatabaseResolver(
                    CONNECTION_PROVIDER,
                    new AbstractNameValueQueryExecutor() {
                        @Override
                        protected String tableName() {
                            return "custom_properties_table";
                        }

                        @Override
                        protected String propertyNameColumn() {
                            return "config_key";
                        }

                        @Override
                        protected String propertyValueColumn() {
                            return "config_value";
                        }
                    }
                );

            List<String> propertiesToResolve = Arrays.asList(
                "custom.table.test.property.1",
                "custom.table.test.property.2"
            );

            DatabaseResolver.Result result =
                databasePropertyResolver.resolve(propertiesToResolve);

            assertTrue(result.hasResolvedProperties());
            assertFalse(result.hasUnresolvedProperties());
            assertTrue(result.resolvedPropertyNames().stream()
                .allMatch(resolved -> propertiesToResolve.contains(resolved))
            );
        }

        @Test
        @DisplayName("should wrap and propagate any SQL exceptions")
        public void test7() {
            // Invalid credentials to simulate SQL exception.
            ConnectionProvider invalidConnectionProvider = 
                new H2DataSourceConnectionProvider(
                    "jdbc:h2:mem:test;DB_CLOSE_DELAY=-1", 
                    "invalid_user", 
                    "invalid_password"
                );
            
            DatabaseResolver databasePropertyResolver = 
                new DatabaseResolver(invalidConnectionProvider);


            List<String> propertiesToResolve = Arrays.asList(
                "test.property.1",
                "test.property.2"
            );

            ExternalizedPropertiesException exception = assertThrows(
                ExternalizedPropertiesException.class, 
                () -> databasePropertyResolver.resolve(propertiesToResolve)
            );

            assertTrue(exception.getCause() instanceof SQLException);
        }
    }

    @Nested
    class ResolveMethodWithVarArgsOverload {

        @Test
        @DisplayName("should throw when propertyNames varargs argument is null")
        public void test1() {
            DatabaseResolver databasePropertyResolver = 
                new DatabaseResolver(CONNECTION_PROVIDER);

            assertThrows(IllegalArgumentException.class, () -> {
                databasePropertyResolver.resolve((String[])null);
            });
        }

        @Test
        @DisplayName("should throw when propertyNames varargs argument is empty")
        public void test2() {
            DatabaseResolver databasePropertyResolver = 
                new DatabaseResolver(CONNECTION_PROVIDER);
                
            assertThrows(IllegalArgumentException.class, () -> {
                databasePropertyResolver.resolve(new String[0]);
            });
        }

        @Test
        @DisplayName("should throw when propertyNames argument contains null or blank values")
        public void test3() {
            DatabaseResolver databasePropertyResolver = 
                new DatabaseResolver(CONNECTION_PROVIDER);
            
            String[] propertiesToResolve = new String[] {
                "",
                " ",
                null,
                "test.property.2"
            };
                
            assertThrows(IllegalArgumentException.class, () -> {
                databasePropertyResolver.resolve(propertiesToResolve);
            });
        }

        @Test
        @DisplayName("should resolve all properties from database")
        public void test4() {
            DatabaseResolver databasePropertyResolver = 
                new DatabaseResolver(CONNECTION_PROVIDER);

            String[] propertiesToResolve = new String[] {
                "test.property.1", 
                "test.property.2"
            };
            
            DatabaseResolver.Result result = 
                databasePropertyResolver.resolve(propertiesToResolve);
            
            assertTrue(result.hasResolvedProperties());
            assertFalse(result.hasUnresolvedProperties());
            assertTrue(result.resolvedPropertyNames().stream()
                .allMatch(resolved -> Arrays.asList(propertiesToResolve).contains(resolved))
            );
        }

        @Test
        @DisplayName("should return result with resolved and unresolved properties from database")
        public void test5() {
            DatabaseResolver databasePropertyResolver = 
                new DatabaseResolver(CONNECTION_PROVIDER);

            String[] propertiesToResolve = new String[] {
                "test.property.1", 
                "test.property.2",
                "non.existent.property"
            };

            DatabaseResolver.Result result = 
                databasePropertyResolver.resolve(propertiesToResolve);

            assertTrue(result.hasResolvedProperties());
            assertTrue(result.hasUnresolvedProperties());
            assertTrue(result.unresolvedPropertyNames().contains("non.existent.property"));
        }

        @Test
        @DisplayName("should use provided custom query executor")
        public void test6() {
            DatabaseResolver databasePropertyResolver = 
                new DatabaseResolver(
                    CONNECTION_PROVIDER,
                    new AbstractNameValueQueryExecutor() {
                        @Override
                        protected String tableName() {
                            return "custom_properties_table";
                        }

                        @Override
                        protected String propertyNameColumn() {
                            return "config_key";
                        }

                        @Override
                        protected String propertyValueColumn() {
                            return "config_value";
                        }
                    }
                );

            String[] propertiesToResolve = new String[] {
                "custom.table.test.property.1",
                "custom.table.test.property.2"
            };

            DatabaseResolver.Result result =
                databasePropertyResolver.resolve(propertiesToResolve);

            assertTrue(result.hasResolvedProperties());
            assertFalse(result.hasUnresolvedProperties());
            assertTrue(result.resolvedPropertyNames().stream()
                .allMatch(resolved -> Arrays.asList(propertiesToResolve).contains(resolved))
            );
        }

        @Test
        @DisplayName("should wrap and propagate any SQL exceptions")
        public void test7() {
            // Invalid credentials to simulate SQL exception.
            ConnectionProvider invalidConnectionProvider = 
                new H2DataSourceConnectionProvider(
                    "jdbc:h2:mem:test;DB_CLOSE_DELAY=-1", 
                    "invalid_user", 
                    "invalid_password"
                );
            
            DatabaseResolver databasePropertyResolver = 
                new DatabaseResolver(invalidConnectionProvider);


            String[] propertiesToResolve = new String[] {
                "test.property.1",
                "test.property.2"
            };

            ExternalizedPropertiesException exception = assertThrows(
                ExternalizedPropertiesException.class, 
                () -> databasePropertyResolver.resolve(propertiesToResolve)
            );

            assertTrue(exception.getCause() instanceof SQLException);
        }
    }

    private static void createTestDatabaseConfigurationEntries() throws SQLException {
        try (Connection connection = CONNECTION_PROVIDER.getConnection()) {

            initialExternalizedPropertiesTable(connection);
            initializeCustomPropertiesTable(connection);

            connection.commit();
        }
    }

    private static void initialExternalizedPropertiesTable(
            Connection connection
    ) throws SQLException {
        // Matches the default columns and table name in
        // SimpleNameValueQueryExecutor
        PreparedStatement createTable = connection.prepareStatement(
            "CREATE TABLE IF NOT EXISTS externalized_properties ( " +
            "name VARCHAR(255), " +
            "value VARCHAR(255), " +
            "description VARCHAR(255))"
        );
        createTable.executeUpdate();

        PreparedStatement insert = connection.prepareStatement(
            "INSERT INTO externalized_properties VALUES(?,?,?)"
        );

        for (int i = 1; i <= NUMBER_OF_TEST_ENTRIES; i++) {
            insert.setString(1, "test.property." + i);
            insert.setString(2, "test/property/value/" + i);
            insert.setString(3, "Test property " + i + " description");
            insert.executeUpdate();
        }

        // Custom table with config_key and config_value property columns.
        PreparedStatement createCustomTable = connection.prepareStatement(
            "CREATE TABLE IF NOT EXISTS custom_properties_table ( " +
            "config_key VARCHAR(255), " +
            "config_value VARCHAR(255))"
        );
        createCustomTable.executeUpdate();
    }

    private static void initializeCustomPropertiesTable(
            Connection connection
    ) throws SQLException {
        PreparedStatement insertCustomTableConfig = connection.prepareStatement(
            "INSERT INTO custom_properties_table VALUES(?,?)"
        );

        for (int i = 1; i <= NUMBER_OF_TEST_ENTRIES; i++) {
            insertCustomTableConfig.setString(1, "custom.table.test.property." + i);
            insertCustomTableConfig.setString(2, "custom/table/test/property/value/" + i);
            insertCustomTableConfig.executeUpdate();
        }
    }
}
