package io.github.jeyjeyemem.externalizedproperties.resolvers.database;

import org.h2.jdbcx.JdbcDataSource;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class DataSourceConnectionProviderTests {
    @Nested
    class Constructor {
        @Test
        @DisplayName("should throw when data source argument is null")
        public void test1() {
            assertThrows(
                IllegalArgumentException.class, 
                () -> new DataSourceConnectionProvider(null)
            );
        }

        @Test
        @DisplayName(
            "should throw when data source argument is null and credentials are provided"
        )
        public void test2() {
            assertThrows(
                IllegalArgumentException.class, 
                () -> new DataSourceConnectionProvider(
                    null,
                    "user",
                    "my-super-secret-password"
                )
            );
        }

        @Test
        @DisplayName("should throw when username argument is null")
        public void test3() {
            assertThrows(
                IllegalArgumentException.class, 
                () -> new DataSourceConnectionProvider(
                    new JdbcDataSource(),
                    null,
                    "my-super-secret-password"
                )
            );
        }

        @Test
        @DisplayName("should throw when password argument is null")
        public void test4() {
            assertThrows(
                IllegalArgumentException.class, 
                () -> new DataSourceConnectionProvider(
                    new JdbcDataSource(),
                    "username",
                    null
                )
            );
        }
    }

    @Nested
    class GetConnectionMethod {
        @Test
        @DisplayName("should get a connection")
        public void test1() throws SQLException {
            JdbcDataSource h2DataSource = new JdbcDataSource();
            h2DataSource.setUrl("jdbc:h2:mem:test;DB_CLOSE_DELAY=-1");
            h2DataSource.setUser("sa");

            DataSourceConnectionProvider connectionProvider = 
                new DataSourceConnectionProvider(
                    h2DataSource
                );

            Connection connection = connectionProvider.getConnection();

            assertNotNull(connection);
            assertTrue(connection.isValid(0));
        }

        @Test
        @DisplayName("should get a connection with the given username and password")
        public void test2() throws SQLException {
            JdbcDataSource h2DataSource = new JdbcDataSource();
            h2DataSource.setUrl("jdbc:h2:mem:test;DB_CLOSE_DELAY=-1");

            DataSourceConnectionProvider connectionProvider = 
                new DataSourceConnectionProvider(
                    h2DataSource,
                    "sa",
                    ""
                );

            Connection connection = connectionProvider.getConnection();

            assertNotNull(connection);
            assertTrue(connection.isValid(0));
        }
    }
}
