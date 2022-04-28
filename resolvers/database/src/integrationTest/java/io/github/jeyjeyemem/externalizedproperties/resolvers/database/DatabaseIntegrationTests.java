package io.github.jeyjeyemem.externalizedproperties.resolvers.database;

import io.github.jeyjeyemem.externalizedproperties.core.ExternalizedPropertiesException;
import io.github.jeyjeyemem.externalizedproperties.core.ExternalizedProperty;
import io.github.jeyjeyemem.externalizedproperties.core.proxy.ProxyMethod;
import io.github.jeyjeyemem.externalizedproperties.core.testfixtures.ProxyMethodUtils;
import io.github.jeyjeyemem.externalizedproperties.resolvers.database.testentities.JdbcUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;
import java.util.Optional;

import static org.junit.Assert.assertThrows;
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

    ProxyMethod proxyMethod() {
        return ProxyMethodUtils.fromMethod(
            DatabaseProxyInterface.class, 
            "test.property.1"
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
            ProxyMethod proxyMethod = proxyMethod();
            
            String propertyName = "test.property.1";

            Optional<String> result = databaseResolver.resolve(proxyMethod, propertyName);
            
            assertTrue(result.isPresent());
            assertNotNull(result.get());
        }

        @Test
        @DisplayName("should return empty Optional when property is not found in database")
        void test2() {
            DatabaseResolver databaseResolver = 
                new DatabaseResolver(connectionProvider);
            ProxyMethod proxyMethod = proxyMethod();

            String propertyName = "non.existent.property";

            Optional<String> result = databaseResolver.resolve(proxyMethod, propertyName);

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
            ProxyMethod proxyMethod = proxyMethod();

            String propertyName = "test.property";

            ExternalizedPropertiesException exception = assertThrows(
                ExternalizedPropertiesException.class, 
                () -> databaseResolver.resolve(proxyMethod, propertyName)
            );

            assertTrue(exception.getCause() instanceof SQLException);
        }
    }

    static interface DatabaseProxyInterface {
        @ExternalizedProperty("test.property.1")
        String property1();

        @ExternalizedProperty("test.property.2")
        String property2();

        @ExternalizedProperty("non.existent.property")
        String nonExistentProperty();
    }
}
