package io.github.jeyjeyemem.externalizedproperties.resolvers.database;

import io.github.jeyjeyemem.externalizedproperties.core.ResolverResult;
import io.github.jeyjeyemem.externalizedproperties.core.exceptions.ExternalizedPropertiesException;
import io.github.jeyjeyemem.externalizedproperties.resolvers.database.testentities.JdbcUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.Assert.assertThrows;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public abstract class DatabaseIntegrationTests {

    /**
     * Override if different connection provider is desired.
     * @param jdbcConnectionString The JDBC connection string.
     * @param username The JDBC username.
     * @param password The JDBC password.
     * @return The connection provider.
     */
    ConnectionProvider getConnectionProvider(
        String jdbcConnectionString,
        String username,
        String password
    ) {
        return JdbcUtils.createConnectionProvider(
            jdbcConnectionString, 
            username, 
            password
        );
    }

    ConnectionProvider getConnectionProvider() {
        return JdbcUtils.createConnectionProvider(
            getJdbcConnectionString(), 
            getJdbcUsername(), 
            getJdbcPassword()
        );
    }

    abstract String getJdbcConnectionString();
    abstract String getJdbcUsername();
    abstract String getJdbcPassword();

    @Nested
    class ResolveMethod {
        final ConnectionProvider connectionProvider = getConnectionProvider();

        @Test
        @DisplayName("should resolve all properties from database")
        void test1() {
            DatabaseResolver databaseResolver = 
                new DatabaseResolver(connectionProvider);
            
            String propertyName = "test.property.1";

            Optional<String> result = databaseResolver.resolve(propertyName);
            
            assertTrue(result.isPresent());
            assertNotNull(result.get());
        }

        @Test
        @DisplayName("should return empty Optional when property is not found in database")
        void test2() {
            DatabaseResolver databaseResolver = 
                new DatabaseResolver(connectionProvider);

            String propertyName = "non.existent.property";

            Optional<String> result = databaseResolver.resolve(propertyName);

            assertFalse(result.isPresent());
        }

        @Test
        @DisplayName("should wrap and propagate any SQL exceptions")
        void test3() {
            // Invalid credentials to simulate SQL exception.
            ConnectionProvider invalidConnectionProvider = 
                getConnectionProvider(
                    getJdbcConnectionString(), 
                    "invalid_user", 
                    "invalid_password"
                );
            
            DatabaseResolver databaseResolver = 
                new DatabaseResolver(invalidConnectionProvider);

            String propertyName = "test.property";

            ExternalizedPropertiesException exception = assertThrows(
                ExternalizedPropertiesException.class, 
                () -> databaseResolver.resolve(propertyName)
            );

            assertTrue(exception.getCause() instanceof SQLException);
        }
    }

    @Nested
    class ResolveMethodWithCollectionOverload {
        final ConnectionProvider connectionProvider = getConnectionProvider();

        @Test
        @DisplayName("should resolve all properties from database")
        void test1() {
            DatabaseResolver databaseResolver = 
                new DatabaseResolver(connectionProvider);

            List<String> propertiesToResolve = Arrays.asList(
                "test.property.1", 
                "test.property.2"
            );
            
            ResolverResult result = 
                databaseResolver.resolve(propertiesToResolve);
            
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
        void test2() {
            DatabaseResolver databaseResolver = 
                new DatabaseResolver(connectionProvider);

            List<String> propertiesToResolve = Arrays.asList(
                "test.property.1", 
                "test.property.2",
                "non.existent.property"
            );

            ResolverResult result = 
                databaseResolver.resolve(propertiesToResolve);

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
        @DisplayName("should wrap and propagate any SQL exceptions")
        void test3() {
            // Invalid credentials to simulate SQL exception.
            ConnectionProvider invalidConnectionProvider = 
                getConnectionProvider(
                    getJdbcConnectionString(), 
                    "invalid_user", 
                    "invalid_password"
                );
            
            DatabaseResolver databaseResolver = 
                new DatabaseResolver(invalidConnectionProvider);


            List<String> propertiesToResolve = Arrays.asList(
                "test.property.1",
                "test.property.2"
            );

            ExternalizedPropertiesException exception = assertThrows(
                ExternalizedPropertiesException.class, 
                () -> databaseResolver.resolve(propertiesToResolve)
            );

            assertTrue(exception.getCause() instanceof SQLException);
        }
    }

    @Nested
    class ResolveMethodWithVarArgsOverload {
        final ConnectionProvider connectionProvider = getConnectionProvider();

        @Test
        @DisplayName("should resolve all properties from database")
        void test1() {
            DatabaseResolver databaseResolver = 
                new DatabaseResolver(connectionProvider);

            String[] propertiesToResolve = new String[] {
                "test.property.1", 
                "test.property.2"
            };
            
            ResolverResult result = 
                databaseResolver.resolve(propertiesToResolve);
            
            assertTrue(result.hasResolvedProperties());
            assertFalse(result.hasUnresolvedProperties());
            assertTrue(result.resolvedPropertyNames().stream()
                .allMatch(resolved -> Arrays.asList(propertiesToResolve).contains(resolved))
            );
        }

        @Test
        @DisplayName("should return result with resolved and unresolved properties from database")
        void test2() {
            DatabaseResolver databaseResolver = 
                new DatabaseResolver(connectionProvider);

            String[] propertiesToResolve = new String[] {
                "test.property.1", 
                "test.property.2",
                "non.existent.property"
            };

            ResolverResult result = 
                databaseResolver.resolve(propertiesToResolve);

            assertTrue(result.hasResolvedProperties());
            assertTrue(result.hasUnresolvedProperties());
            assertTrue(result.unresolvedPropertyNames().contains("non.existent.property"));
        }

        @Test
        @DisplayName("should wrap and propagate any SQL exceptions")
        void test3() {
            // Invalid credentials to simulate SQL exception.
            ConnectionProvider invalidConnectionProvider = 
                getConnectionProvider(
                    getJdbcConnectionString(), 
                    "invalid_user", 
                    "invalid_password"
                );
            
            DatabaseResolver databaseResolver = 
                new DatabaseResolver(invalidConnectionProvider);


            String[] propertiesToResolve = new String[] {
                "test.property.1",
                "test.property.2"
            };

            ExternalizedPropertiesException exception = assertThrows(
                ExternalizedPropertiesException.class, 
                () -> databaseResolver.resolve(propertiesToResolve)
            );

            assertTrue(exception.getCause() instanceof SQLException);
        }
    }
}
