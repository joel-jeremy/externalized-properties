package io.github.jeyjeyemem.externalizedproperties.resolvers.database;

import io.github.jeyjeyemem.externalizedproperties.core.ExternalizedPropertyResolverResult;
import io.github.jeyjeyemem.externalizedproperties.resolvers.database.entities.SimpleNameValuePropertyEntity;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.Persistence;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class DatabasePropertyResolverTests {

    private static final SimpleNameValuePropertyEntity TEST_CONFIG_ENTITY_1 = 
        new SimpleNameValuePropertyEntity(
            "test.property.1", 
            "/test/property/value/1",
            "Test Property 1 Description"
        );

    private static final SimpleNameValuePropertyEntity TEST_CONFIG_ENTITY_2 = 
        new SimpleNameValuePropertyEntity(
            "test.property.2", 
            "/test/property/value/2",
            "Test Property 2 Description"
        );

    private static final EntityManagerFactory ENTITY_MANAGER_FACTORY = 
        Persistence.createEntityManagerFactory("TestExternalizedPropertiesPU");

    @BeforeAll
    public static void setup() {
        createTestDatabaseConfigurationEntries();
    }

    @Nested
    class Constructor {
        @Test
        @DisplayName("should throw when entity manager factory is null")
        public void test1() {
            assertThrows(IllegalArgumentException.class, () -> {
                new DatabasePropertyResolver(null);
            });
        }

        @Test
        @DisplayName("should throw when query runner is null")
        public void test2() {
            assertThrows(IllegalArgumentException.class, () -> {
                new DatabasePropertyResolver(ENTITY_MANAGER_FACTORY, null);
            });
        }
    }

    @Nested
    class ResolvePropertiesMethod {
        @Test
        @DisplayName("should throw when propertyNames argument is null")
        public void test1() {
            DatabasePropertyResolver databasePropertyResolver = 
                new DatabasePropertyResolver(ENTITY_MANAGER_FACTORY);

            assertThrows(IllegalArgumentException.class, () -> {
                databasePropertyResolver.resolve((Collection<String>)null);
            });
        }

        @Test
        @DisplayName("should throw when propertyNames argument is empty")
        public void test2() {
            DatabasePropertyResolver databasePropertyResolver = 
                new DatabasePropertyResolver(ENTITY_MANAGER_FACTORY);
                
            assertThrows(IllegalArgumentException.class, () -> {
                databasePropertyResolver.resolve(Collections.emptyList());
            });
        }

        @Test
        @DisplayName("should resolve all properties from database")
        public void test3() {
            DatabasePropertyResolver databasePropertyResolver = 
                new DatabasePropertyResolver(ENTITY_MANAGER_FACTORY);

            List<String> propertiesToResolve = Arrays.asList(
                TEST_CONFIG_ENTITY_1.getName(), 
                TEST_CONFIG_ENTITY_2.getName()
            );
            
            ExternalizedPropertyResolverResult result = 
                databasePropertyResolver.resolve(propertiesToResolve);
            
            assertTrue(result.hasResolvedProperties());
            assertFalse(result.hasUnresolvedProperties());
            assertTrue(result.resolvedProperties().stream()
                .allMatch(r -> propertiesToResolve.contains(r.name()))
            );
        }

        @Test
        @DisplayName("should return result with resolved and unresolved properties from database")
        public void test4() {
            DatabasePropertyResolver databasePropertyResolver = 
                new DatabasePropertyResolver(ENTITY_MANAGER_FACTORY);

            List<String> propertiesToResolve = Arrays.asList(
                TEST_CONFIG_ENTITY_1.getName(), 
                TEST_CONFIG_ENTITY_2.getName(),
                "non.existent.property"
            );

            ExternalizedPropertyResolverResult result = 
                databasePropertyResolver.resolve(propertiesToResolve);

            assertTrue(result.hasResolvedProperties());
            assertTrue(result.hasUnresolvedProperties());
            assertTrue(result.unresolvedPropertyNames().contains("non.existent.property"));
        }
    }

    @Nested
    class ResolveVarArgsMethod {

        @Test
        @DisplayName("should throw when propertyNames varargs argument is null")
        public void test1() {
            DatabasePropertyResolver databasePropertyResolver = 
                new DatabasePropertyResolver(ENTITY_MANAGER_FACTORY);

            assertThrows(IllegalArgumentException.class, () -> {
                databasePropertyResolver.resolve((String[])null);
            });
        }

        @Test
        @DisplayName("should throw when propertyNames varargs argument is empty")
        public void test2() {
            DatabasePropertyResolver databasePropertyResolver = 
                new DatabasePropertyResolver(ENTITY_MANAGER_FACTORY);
                
            assertThrows(IllegalArgumentException.class, () -> {
                databasePropertyResolver.resolve(new String[0]);
            });
        }

        @Test
        @DisplayName("should resolve all properties from database")
        public void test3() {
            DatabasePropertyResolver databasePropertyResolver = 
                new DatabasePropertyResolver(ENTITY_MANAGER_FACTORY);

            String[] propertiesToResolve = new String[] {
                TEST_CONFIG_ENTITY_1.getName(), 
                TEST_CONFIG_ENTITY_2.getName()
            };
            
            ExternalizedPropertyResolverResult result = 
                databasePropertyResolver.resolve(propertiesToResolve);
            
            assertTrue(result.hasResolvedProperties());
            assertFalse(result.hasUnresolvedProperties());
            assertTrue(result.resolvedProperties().stream()
                .allMatch(r -> Arrays.asList(propertiesToResolve).contains(r.name()))
            );
        }

        @Test
        @DisplayName("should return result with resolved and unresolved properties from database")
        public void test4() {
            DatabasePropertyResolver databasePropertyResolver = 
                new DatabasePropertyResolver(ENTITY_MANAGER_FACTORY);

            String[] propertiesToResolve = new String[] {
                TEST_CONFIG_ENTITY_1.getName(), 
                TEST_CONFIG_ENTITY_2.getName(),
                "non.existent.property"
            };

            ExternalizedPropertyResolverResult result = 
                databasePropertyResolver.resolve(propertiesToResolve);

            assertTrue(result.hasResolvedProperties());
            assertTrue(result.hasUnresolvedProperties());
            assertTrue(result.unresolvedPropertyNames().contains("non.existent.property"));
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
