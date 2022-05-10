package io.github.joeljeremy7.externalizedproperties.resolvers.database;

import io.github.joeljeremy7.externalizedproperties.core.ExternalizedProperties;
import io.github.joeljeremy7.externalizedproperties.core.ExternalizedPropertiesException;
import io.github.joeljeremy7.externalizedproperties.core.ExternalizedProperty;
import io.github.joeljeremy7.externalizedproperties.core.ResolverProvider;
import io.github.joeljeremy7.externalizedproperties.core.proxy.ProxyMethod;
import io.github.joeljeremy7.externalizedproperties.core.testfixtures.ProxyMethodFactory;
import io.github.joeljeremy7.externalizedproperties.resolvers.database.queryexecutors.AbstractNameValueQueryExecutor;
import io.github.joeljeremy7.externalizedproperties.resolvers.database.queryexecutors.SimpleNameValueQueryExecutor;
import io.github.joeljeremy7.externalizedproperties.resolvers.database.testentities.H2Utils;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class DatabaseResolverTests {
    private static final ProxyMethodFactory<ProxyInterface> PROXY_METHOD_FACTORY =
        new ProxyMethodFactory<>(ProxyInterface.class);
    private static final ExternalizedProperties EXTERNALIZED_PROPERTIES =
        ExternalizedProperties.builder().withDefaults().build();

    private static final int NUMBER_OF_TEST_ENTRIES = 2;
    private static final String H2_CONNECTION_STRING = 
        H2Utils.buildConnectionString(DatabaseResolverTests.class.getSimpleName());
    private static final ConnectionProvider CONNECTION_PROVIDER = 
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
            QueryExecutor queryExecutor = new SimpleNameValueQueryExecutor();

            assertThrows(IllegalArgumentException.class, () -> {
                new DatabaseResolver(null, queryExecutor);
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
    class ProviderMethodWithConnectionProviderOverload {
        @Test
        @DisplayName("should throw when connection provider argument is null.")
        void test1() {
            assertThrows(
                IllegalArgumentException.class, 
                () -> DatabaseResolver.provider(null)
            );
        }

        @Test
        @DisplayName("should not return null.")
        void test2() {
            ResolverProvider<DatabaseResolver> provider = 
                DatabaseResolver.provider(CONNECTION_PROVIDER);

            assertNotNull(provider);
        }

        @Test
        @DisplayName("should return an instance on get.")
        void test3() {
            ResolverProvider<DatabaseResolver> provider = 
                DatabaseResolver.provider(CONNECTION_PROVIDER);

            assertNotNull(provider.get(EXTERNALIZED_PROPERTIES));
        }
    }

    @Nested
    class ProviderMethodWithConnectionProviderAndQueryExecutorOverload {
        @Test
        @DisplayName("should throw when connection provider argument is null.")
        void test1() {
            QueryExecutor queryExecutor = new SimpleNameValueQueryExecutor();

            assertThrows(
                IllegalArgumentException.class, 
                () -> DatabaseResolver.provider(
                    null,
                    queryExecutor
                )
            );
        }

        @Test
        @DisplayName("should throw when query executor argument is null.")
        void test2() {
            assertThrows(
                IllegalArgumentException.class, 
                () -> DatabaseResolver.provider(
                    CONNECTION_PROVIDER,
                    null
                )
            );
        }

        @Test
        @DisplayName("should not return null.")
        void test3() {
            ResolverProvider<DatabaseResolver> provider = 
                DatabaseResolver.provider(
                    CONNECTION_PROVIDER, 
                    new SimpleNameValueQueryExecutor()
                );

            assertNotNull(provider);
        }

        @Test
        @DisplayName("should return an instance on get.")
        void test4() {
            ResolverProvider<DatabaseResolver> provider = 
                DatabaseResolver.provider(
                    CONNECTION_PROVIDER,
                    new SimpleNameValueQueryExecutor()
                );

            assertNotNull(provider.get(EXTERNALIZED_PROPERTIES));
        }
    }

    @Nested
    class ResolveMethod {
        // @Test
        // @DisplayName("should throw when proxy method argument is null")
        // void validationTest1() {
        //     DatabaseResolver databaseResolver = 
        //         new DatabaseResolver(CONNECTION_PROVIDER);

        //     assertThrows(IllegalArgumentException.class, () -> {
        //         databaseResolver.resolve(null, "test.property.1");
        //     });
        // }

        // @Test
        // @DisplayName("should throw when propertyName argument is null")
        // void validationTest2() {
        //     DatabaseResolver databaseResolver = 
        //         new DatabaseResolver(CONNECTION_PROVIDER);
        //     ProxyMethod proxyMethod = proxyMethod(databaseResolver);

        //     assertThrows(IllegalArgumentException.class, () -> {
        //         databaseResolver.resolve(proxyMethod, (String)null);
        //     });
        // }

        // @Test
        // @DisplayName("should throw when property name argument is blank")
        // void validationTest3() {
        //     DatabaseResolver databaseResolver = 
        //         new DatabaseResolver(CONNECTION_PROVIDER);
        //     ProxyMethod proxyMethod = proxyMethod(databaseResolver);
                
        //     assertThrows(IllegalArgumentException.class, () -> {
        //         databaseResolver.resolve(proxyMethod, "");
        //     });

        //     assertThrows(IllegalArgumentException.class, () -> {
        //         databaseResolver.resolve(proxyMethod, " ");
        //     });
        // }

        @Test
        @DisplayName("should resolve all properties from database")
        void test1() {
            DatabaseResolver databaseResolver = 
                new DatabaseResolver(CONNECTION_PROVIDER);
            ProxyMethod proxyMethod = PROXY_METHOD_FACTORY.fromMethodReference(
                ProxyInterface::property1
            );
            
            String propertyName = "test.property.1";

            Optional<String> result = databaseResolver.resolve(proxyMethod, propertyName);
            
            assertTrue(result.isPresent());
            assertNotNull(result.get());
        }

        @Test
        @DisplayName("should return empty Optional when property is not found in database")
        void test2() {
            DatabaseResolver databaseResolver = 
                new DatabaseResolver(CONNECTION_PROVIDER);
            ProxyMethod proxyMethod = PROXY_METHOD_FACTORY.fromMethodReference(
                ProxyInterface::nonExistentProperty
            );

            String propertyName = "non.existent.property";

            Optional<String> result = databaseResolver.resolve(proxyMethod, propertyName);

            assertFalse(result.isPresent());
        }

        @Test
        @DisplayName("should use provided custom query executor")
        void test3() {
            DatabaseResolver databaseResolver = 
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
            ProxyMethod proxyMethod = PROXY_METHOD_FACTORY.fromMethodReference(
                ProxyInterface::property1
            );

            String propertyName = "test.property.1";

            Optional<String> result = databaseResolver.resolve(proxyMethod, propertyName);

            assertTrue(result.isPresent());
            assertNotNull(result.get());
        }

        @Test
        @DisplayName("should wrap and propagate any SQL exceptions")
        void test4() {
            // Invalid credentials to simulate SQL exception.
            ConnectionProvider invalidConnectionProvider = 
                H2Utils.createConnectionProvider(
                    H2Utils.buildConnectionString("ResolveMethod.test6", ";USER=sa;PASSWORD=password"), 
                    "invalid_user", 
                    "invalid_password"
                );
            
            DatabaseResolver databaseResolver = 
                new DatabaseResolver(invalidConnectionProvider);
            ProxyMethod proxyMethod = PROXY_METHOD_FACTORY.fromMethodReference(
                ProxyInterface::property1
            );

            String propertyName = "test.property.1";

            ExternalizedPropertiesException exception = assertThrows(
                ExternalizedPropertiesException.class, 
                () -> databaseResolver.resolve(proxyMethod, propertyName)
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

    public static interface ProxyInterface {
        @ExternalizedProperty("test.property.1")
        String property1();
    
        @ExternalizedProperty("test.property.2")
        String property2();
    
        @ExternalizedProperty("non.existent.property")
        String nonExistentProperty();
    }
}
