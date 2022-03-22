package io.github.jeyjeyemem.externalizedproperties.resolvers.database;

import io.github.jeyjeyemem.externalizedproperties.core.ResolverResult;
import io.github.jeyjeyemem.externalizedproperties.core.exceptions.ExternalizedPropertiesException;
import io.github.jeyjeyemem.externalizedproperties.resolvers.database.queryexecutors.AbstractNameValueQueryExecutor;
import io.github.jeyjeyemem.externalizedproperties.resolvers.database.queryexecutors.SimpleNameValueQueryExecutor;
import io.github.jeyjeyemem.externalizedproperties.resolvers.database.testentities.H2Utils;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
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

    static final int NUMBER_OF_TEST_ENTRIES = 2;
    static final String H2_CONNECTION_STRING = 
        H2Utils.buildConnectionString(DatabaseResolverTests.class.getSimpleName());
    static final ConnectionProvider CONNECTION_PROVIDER = 
        H2Utils.createConnectionProvider(H2_CONNECTION_STRING, "sa");
    
    @BeforeAll
    static void setup() throws SQLException {
        createTestDatabaseConfigurationEntries();
    }

    @Nested
    class Constructor {
        @Test
        @DisplayName("should throw when connection provider argument is null")
        void test1() {
            assertThrows(IllegalArgumentException.class, () -> {
                new DatabaseResolver(null, new SimpleNameValueQueryExecutor());
            });
        }

        @Test
        @DisplayName("should throw when query runner is null")
        void test2() {
            assertThrows(IllegalArgumentException.class, () -> {
                new DatabaseResolver(CONNECTION_PROVIDER, null);
            });
        }
    }

    @Nested
    class ResolveMethod {
        @Test
        @DisplayName("should throw when propertyName argument is null")
        void test1() {
            DatabaseResolver databasePropertyResolver = 
                new DatabaseResolver(CONNECTION_PROVIDER);

            assertThrows(IllegalArgumentException.class, () -> {
                databasePropertyResolver.resolve((String)null);
            });
        }

        @Test
        @DisplayName("should throw when propertyName argument is blank")
        void test2() {
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
        void test3() {
            DatabaseResolver databasePropertyResolver = 
                new DatabaseResolver(CONNECTION_PROVIDER);
            
            String propertyName = "test.property.1";

            Optional<String> result = databasePropertyResolver.resolve(propertyName);
            
            assertTrue(result.isPresent());
            assertNotNull(result.get());
        }

        @Test
        @DisplayName("should return empty Optional when property is not found in database")
        void test4() {
            DatabaseResolver databasePropertyResolver = 
                new DatabaseResolver(CONNECTION_PROVIDER);

            String propertyName = "non.existent.property";

            Optional<String> result = databasePropertyResolver.resolve(propertyName);

            assertFalse(result.isPresent());
        }

        @Test
        @DisplayName("should use provided custom query executor")
        void test5() {
            DatabaseResolver databasePropertyResolver = 
                new DatabaseResolver(
                    CONNECTION_PROVIDER,
                    new AbstractNameValueQueryExecutor() {
                        @Override
                        protected String table() {
                            return "custom_properties";
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

            String propertyName = "test.property.1";

            Optional<String> result = databasePropertyResolver.resolve(propertyName);

            assertTrue(result.isPresent());
            assertNotNull(result.get());
        }

        @Test
        @DisplayName("should wrap and propagate any SQL exceptions")
        void test6() {
            // Invalid credentials to simulate SQL exception.
            ConnectionProvider invalidConnectionProvider = 
                H2Utils.createConnectionProvider(
                    H2Utils.buildConnectionString("ResolveMethod.test6", ";USER=sa;PASSWORD=password"), 
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
        void test1() {
            DatabaseResolver databasePropertyResolver = 
                new DatabaseResolver(CONNECTION_PROVIDER);

            assertThrows(IllegalArgumentException.class, () -> {
                databasePropertyResolver.resolve((Collection<String>)null);
            });
        }

        @Test
        @DisplayName("should throw when propertyNames argument is empty")
        void test2() {
            DatabaseResolver databasePropertyResolver = 
                new DatabaseResolver(CONNECTION_PROVIDER);
                
            assertThrows(IllegalArgumentException.class, () -> {
                databasePropertyResolver.resolve(Collections.emptyList());
            });
        }

        @Test
        @DisplayName("should throw when propertyNames argument contains null or blank values")
        void test3() {
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
        void test4() {
            DatabaseResolver databasePropertyResolver = 
                new DatabaseResolver(CONNECTION_PROVIDER);

            List<String> propertiesToResolve = Arrays.asList(
                "test.property.1", 
                "test.property.2"
            );
            
            ResolverResult result = 
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
        void test5() {
            DatabaseResolver databasePropertyResolver = 
                new DatabaseResolver(CONNECTION_PROVIDER);

            List<String> propertiesToResolve = Arrays.asList(
                "test.property.1", 
                "test.property.2",
                "non.existent.property"
            );

            ResolverResult result = 
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
        void test6() {
            DatabaseResolver databasePropertyResolver = 
                new DatabaseResolver(
                    CONNECTION_PROVIDER,
                    new AbstractNameValueQueryExecutor() {
                        @Override
                        protected String table() {
                            return "custom_properties";
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
                "test.property.1",
                "test.property.2"
            );

            ResolverResult result =
                databasePropertyResolver.resolve(propertiesToResolve);

            assertTrue(result.hasResolvedProperties());
            assertFalse(result.hasUnresolvedProperties());
            assertTrue(result.resolvedPropertyNames().stream()
                .allMatch(resolved -> propertiesToResolve.contains(resolved))
            );
        }

        @Test
        @DisplayName("should wrap and propagate any SQL exceptions")
        void test7() {
            // Invalid credentials to simulate SQL exception.
            ConnectionProvider invalidConnectionProvider = 
                H2Utils.createConnectionProvider(
                    H2Utils.buildConnectionString(
                        "ResolveMethodWithCollectionOverload.test7",
                        ";USER=sa;PASSWORD=password"
                    ), 
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
        void test1() {
            DatabaseResolver databasePropertyResolver = 
                new DatabaseResolver(CONNECTION_PROVIDER);

            assertThrows(IllegalArgumentException.class, () -> {
                databasePropertyResolver.resolve((String[])null);
            });
        }

        @Test
        @DisplayName("should throw when propertyNames varargs argument is empty")
        void test2() {
            DatabaseResolver databasePropertyResolver = 
                new DatabaseResolver(CONNECTION_PROVIDER);
                
            assertThrows(IllegalArgumentException.class, () -> {
                databasePropertyResolver.resolve(new String[0]);
            });
        }

        @Test
        @DisplayName("should throw when propertyNames argument contains null or blank values")
        void test3() {
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
        void test4() {
            DatabaseResolver databasePropertyResolver = 
                new DatabaseResolver(CONNECTION_PROVIDER);

            String[] propertiesToResolve = new String[] {
                "test.property.1", 
                "test.property.2"
            };
            
            ResolverResult result = 
                databasePropertyResolver.resolve(propertiesToResolve);
            
            assertTrue(result.hasResolvedProperties());
            assertFalse(result.hasUnresolvedProperties());
            assertTrue(result.resolvedPropertyNames().stream()
                .allMatch(resolved -> Arrays.asList(propertiesToResolve).contains(resolved))
            );
        }

        @Test
        @DisplayName("should return result with resolved and unresolved properties from database")
        void test5() {
            DatabaseResolver databasePropertyResolver = 
                new DatabaseResolver(CONNECTION_PROVIDER);

            String[] propertiesToResolve = new String[] {
                "test.property.1", 
                "test.property.2",
                "non.existent.property"
            };

            ResolverResult result = 
                databasePropertyResolver.resolve(propertiesToResolve);

            assertTrue(result.hasResolvedProperties());
            assertTrue(result.hasUnresolvedProperties());
            assertTrue(result.unresolvedPropertyNames().contains("non.existent.property"));
        }

        @Test
        @DisplayName("should use provided custom query executor")
        void test6() {
            DatabaseResolver databasePropertyResolver = 
                new DatabaseResolver(
                    CONNECTION_PROVIDER,
                    new AbstractNameValueQueryExecutor() {
                        @Override
                        protected String table() {
                            return "custom_properties";
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
                "test.property.1",
                "test.property.2"
            };

            ResolverResult result =
                databasePropertyResolver.resolve(propertiesToResolve);

            assertTrue(result.hasResolvedProperties());
            assertFalse(result.hasUnresolvedProperties());
            assertTrue(result.resolvedPropertyNames().stream()
                .allMatch(resolved -> Arrays.asList(propertiesToResolve).contains(resolved))
            );
        }

        @Test
        @DisplayName("should wrap and propagate any SQL exceptions")
        void test7() {
            // Invalid credentials to simulate SQL exception.
            ConnectionProvider invalidConnectionProvider = 
                H2Utils.createConnectionProvider(
                    H2Utils.buildConnectionString(
                        "ResolveMethodWithVarArgsOverload.test7",
                        ";USER=sa;PASSWORD=password"
                    ), 
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

            H2Utils.createPropertiesTable(
                connection, 
                NUMBER_OF_TEST_ENTRIES
            );
            H2Utils.createPropertiesTable(
                connection, 
                "custom_properties", 
                "config_key", 
                "config_value", 
                NUMBER_OF_TEST_ENTRIES
            );

            connection.commit();
        }
    }
}
