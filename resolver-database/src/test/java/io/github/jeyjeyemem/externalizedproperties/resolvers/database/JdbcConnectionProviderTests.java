package io.github.jeyjeyemem.externalizedproperties.resolvers.database;

import io.github.jeyjeyemem.externalizedproperties.resolvers.database.testentities.H2Utils;
import org.h2.jdbcx.JdbcDataSource;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import javax.sql.DataSource;

import java.sql.Connection;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class JdbcConnectionProviderTests {
    static final String H2_CONNECTION_STRING = 
        H2Utils.buildConnectionString(JdbcConnectionProviderTests.class.getSimpleName());
    static final JdbcDataSource H2_DATA_SOURCE = 
        H2Utils.createDataSource(H2_CONNECTION_STRING, "sa");

    @Nested
    class Constructor {
        @Test
        @DisplayName("should throw when data source argument is null")
        void dataSourceTest1() {
            assertThrows(
                IllegalArgumentException.class, 
                () -> new JdbcConnectionProvider((DataSource)null)
            );
        }

        @Test
        @DisplayName(
            "should throw when data source argument is null and credentials are provided"
        )
        void dataSourceTest2() {
            assertThrows(
                IllegalArgumentException.class, 
                () -> new JdbcConnectionProvider(
                    (DataSource)null,
                    "user",
                    "my-super-secret-password"
                )
            );
        }

        @Test
        @DisplayName("should throw when jdbc connection url argument is null")
        void driverManagerTest1() {
            assertThrows(
                IllegalArgumentException.class, 
                () -> new JdbcConnectionProvider((String)null)
            );
        }

        @Test
        @DisplayName(
            "should throw when jdbc connection url argument is null " + 
            "and credentials are provided"
        )
        void driverManagerTest2() {
            assertThrows(
                IllegalArgumentException.class, 
                () -> new JdbcConnectionProvider(
                    (String)null,
                    "user",
                    "my-super-secret-password"
                )
            );
        }
    }

    @Nested
    class GetConnectionMethod {
        @Test
        @DisplayName("should get a connection from data source")
        void dataSourceTest1() throws SQLException {
            JdbcConnectionProvider connectionProvider = 
                new JdbcConnectionProvider(
                    H2_DATA_SOURCE
                );

            Connection connection = connectionProvider.getConnection();

            assertNotNull(connection);
            assertTrue(connection.isValid(0));
        }

        @Test
        @DisplayName(
            "should get a connection from data source with the given username and password"
        )
        void dataSourceTest2() throws SQLException {
            JdbcConnectionProvider connectionProvider = 
                new JdbcConnectionProvider(
                    H2_DATA_SOURCE,
                    "sa"
                );

            Connection connection = connectionProvider.getConnection();

            assertNotNull(connection);
            assertTrue(connection.isValid(0));
        }

        @Test
        @DisplayName(
            "should throw when invalid credentials were used"
        )
        void dataSourceTest3() throws SQLException {
            JdbcConnectionProvider connectionProvider = 
                new JdbcConnectionProvider(
                    H2_DATA_SOURCE,
                    "sa",
                    "invalid_password"
                );

            assertThrows(SQLException.class, () -> connectionProvider.getConnection());
        }

        @Test
        @DisplayName("should get a connection from driver manager")
        void driverManagerTest1() throws SQLException {
            JdbcConnectionProvider connectionProvider = 
                new JdbcConnectionProvider(
                    // User is in connection string.
                    H2_CONNECTION_STRING + ";USER=sa"
                );

            Connection connection = connectionProvider.getConnection();

            assertNotNull(connection);
            assertTrue(connection.isValid(0));
        }

        @Test
        @DisplayName(
            "should get a connection from driver manager with the given username and password"
        )
        void driverManagerTest2() throws SQLException {
            JdbcConnectionProvider connectionProvider = 
                new JdbcConnectionProvider(
                    H2_CONNECTION_STRING,
                    "sa"
                );

            Connection connection = connectionProvider.getConnection();

            assertNotNull(connection);
            assertTrue(connection.isValid(0));
        }

        @Test
        @DisplayName(
            "should throw when invalid credentials were used"
        )
        void driverManagerTest3() throws SQLException {
            JdbcConnectionProvider connectionProvider = 
                new JdbcConnectionProvider(
                    H2_CONNECTION_STRING,
                    "invalid_user",
                    "invalid_password"
                );

            assertThrows(SQLException.class, () -> connectionProvider.getConnection());
        }
    }
}
