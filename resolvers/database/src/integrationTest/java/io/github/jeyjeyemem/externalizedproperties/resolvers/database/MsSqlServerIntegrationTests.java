package io.github.jeyjeyemem.externalizedproperties.resolvers.database;

import io.github.jeyjeyemem.externalizedproperties.resolvers.database.testentities.JdbcUtils;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.MSSQLServerContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.sql.SQLException;

@Testcontainers
public class MsSqlServerIntegrationTests extends DatabaseIntegrationTests {

    static final DockerImageName MSSQL_SERVER_IMAGE = 
        DockerImageName.parse("mcr.microsoft.com/mssql/server:2019-CU15-ubuntu-20.04");

    @Container
    static final MSSQLServerContainer<?> MSSQL_SERVER_CONTAINER = 
        new MSSQLServerContainer<>(MSSQL_SERVER_IMAGE)
            .withUrlParam("databaseName", "master")
            .withUrlParam("encrypt", "false")
            .acceptLicense();

    @BeforeAll
    static void setup() throws SQLException {
        JdbcUtils.createPropertiesTable(
            MSSQL_SERVER_CONTAINER.createConnection(""), 
            2
        );
    }

    @Override
    String getJdbcConnectionString() {
        return MSSQL_SERVER_CONTAINER.getJdbcUrl();
    }

    @Override
    String getJdbcUsername() {
        return MSSQL_SERVER_CONTAINER.getUsername();
    }

    @Override
    String getJdbcPassword() {
        return MSSQL_SERVER_CONTAINER.getPassword();
    }
    /**
     * Dummy test for junit to be able to detect this test class.
     */
    @Test
    void detect() {}
}
