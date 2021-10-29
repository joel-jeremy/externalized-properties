package io.github.jeyjeyemem.externalizedproperties.resolvers.database;

import io.github.jeyjeyemem.externalizedproperties.core.ExternalizedPropertyResolverResult;
import io.github.jeyjeyemem.externalizedproperties.resolvers.database.entities.CustomPropertiesEntity;
import io.github.jeyjeyemem.externalizedproperties.resolvers.database.queryexecutors.CustomPropertiesQueryExecutor;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.Persistence;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class QueryExecutorTests {
    private static final CustomPropertiesEntity TEST_CONFIG_ENTITY_1 = 
        new CustomPropertiesEntity(
            1, 
            "/test/property/value/1",
            "Test Property 1 Description"
        );

    private static final CustomPropertiesEntity TEST_CONFIG_ENTITY_2 = 
        new CustomPropertiesEntity(
            2, 
            "/test/property/value/2",
            "Test Property 2 Description"
        );

    private static final EntityManagerFactory ENTITY_MANAGER_FACTORY = 
        Persistence.createEntityManagerFactory("CustomPropertiesPU");

    @BeforeAll
    public static void setup() {
        createTestDatabaseConfigurationEntries();
    }

    @Nested
    class DatabasePropertyResolverImplementation {
        @Test
        @DisplayName("should use provided custom query runner")
        public void test1() {
            DatabasePropertyResolver databasePropertyResolver = 
                new DatabasePropertyResolver(
                    ENTITY_MANAGER_FACTORY,
                    new CustomPropertiesQueryExecutor()
                );

            List<String> propertiesToResolve = Arrays.asList(
              TEST_CONFIG_ENTITY_1.getConfigKey(),
              TEST_CONFIG_ENTITY_2.getConfigKey()  
            );

            ExternalizedPropertyResolverResult result =
                databasePropertyResolver.resolve(propertiesToResolve);

            assertTrue(result.hasResolvedProperties());
            assertFalse(result.hasUnresolvedProperties());
            assertTrue(result.resolvedProperties().stream()
                .allMatch(rp -> propertiesToResolve.contains(rp.name()))
            );
        }
    }

    private static void createTestDatabaseConfigurationEntries() {
        EntityManager entityManager = ENTITY_MANAGER_FACTORY.createEntityManager();
        EntityTransaction transaction = entityManager.getTransaction();

        try {
            transaction.begin();
            entityManager.persist(TEST_CONFIG_ENTITY_1);
            entityManager.persist(TEST_CONFIG_ENTITY_2);
            transaction.commit();
        } catch (Exception ex) {
            transaction.rollback();
        }
    }
}
