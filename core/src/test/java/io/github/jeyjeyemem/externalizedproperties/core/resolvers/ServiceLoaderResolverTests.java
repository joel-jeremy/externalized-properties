package io.github.jeyjeyemem.externalizedproperties.core.resolvers;

import io.github.jeyjeyemem.externalizedproperties.core.ResolverResult;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Service loader resolvers are configured in resources/META-INF/services folder.
 */
public class ServiceLoaderResolverTests {
    @Nested
    class Constructor {
        @Test
        @DisplayName("should throw when resolvers argument is null")
        public void test1() {
            assertThrows(
                IllegalArgumentException.class, 
                () -> new ServiceLoaderResolver(null)
            );
        }
    }

    @Nested
    class ResolveMethod {
        @Test
        @DisplayName("should load properties from ServiceLoader resolvers.")
        public void test1() {
            ServiceLoaderResolver resolver = resolverToTest();
            Optional<String> javaVersion = resolver.resolve("java.version");
            Optional<String> pathEnv = resolver.resolve("PATH");

            // From SystemPropertyResolver.
            assertNotNull(javaVersion);
            assertEquals(
                System.getProperty("java.version"), 
                javaVersion.get()
            );

            // From EnvironmentVariableResolver.
            assertNotNull(pathEnv);
            assertEquals(
                System.getenv("PATH"), 
                pathEnv.get()
            );
        }
        
        @Test
        @DisplayName(
            "should return empty Optional when property cannot be resolved from " + 
            "any of the ServiceLoader resolvers."
        )
        public void test2() {
            ServiceLoaderResolver resolver = resolverToTest();
            Optional<String> result = resolver.resolve(
                "nonexisting.property"
            );
            
            assertNotNull(result);
            assertFalse(result.isPresent());
        }
    }

    @Nested
    class ResolveMethodWithVarArgsOverload {
        @Test
        @DisplayName("should resolve values from ServiceLoader resolvers.")
        public void test1() {
            ServiceLoaderResolver resolver = resolverToTest();

            String[] propertiesToResolve = new String[] {
                "java.version", 
                "java.home",
                "PATH"
            };

            ResolverResult result = resolver.resolve(propertiesToResolve);

            assertTrue(result.hasResolvedProperties());
            assertFalse(result.hasUnresolvedProperties());

            // From SystemPropertyResolver.
            assertEquals(
                System.getProperty("java.version"), 
                result.findRequiredProperty("java.version")
            );

            // From SystemPropertyResolver.
            assertEquals(
                System.getProperty("java.home"), 
                result.findRequiredProperty("java.home")
            );

            // From EnvironmentVariableResolver.
            assertEquals(
                System.getenv("PATH"), 
                result.findRequiredProperty("PATH")
            );
        }

        @Test
        @DisplayName(
            "should return result with unresolved properties when property " + 
            "cannot be resolved from any of the ServiceLoader resolvers."
        )
        public void test2() {
            ServiceLoaderResolver resolver = resolverToTest();

            String[] propertiesToResolve = new String[] {
                "nonexisting.property1", 
                "nonexisting.property2"
            };

            ResolverResult result = resolver.resolve(
                propertiesToResolve
            );
            
            assertFalse(result.hasResolvedProperties());
            assertTrue(result.hasUnresolvedProperties());
            
            for (String propertyName : propertiesToResolve) {
                assertTrue(
                    result.unresolvedPropertyNames().contains(propertyName)
                );
            }
        }
    }

    @Nested
    class ResolveMethodWithCollectionOverload {
        @Test
        @DisplayName("should resolve values from ServiceLoader resolvers.")
        public void test1() {
            ServiceLoaderResolver resolver = resolverToTest();

            List<String> propertiesToResolve = Arrays.asList(
                "java.version", 
                "java.home",
                "PATH"
            );

            ResolverResult result = resolver.resolve(propertiesToResolve);

            assertTrue(result.hasResolvedProperties());
            assertFalse(result.hasUnresolvedProperties());

            // From SystemPropertyResolver.
            assertEquals(
                System.getProperty("java.version"), 
                result.findRequiredProperty("java.version")
            );

            // From SystemPropertyResolver.
            assertEquals(
                System.getProperty("java.home"), 
                result.findRequiredProperty("java.home")
            );

            // From EnvironmentVariableResolver.
            assertEquals(
                System.getenv("PATH"), 
                result.findRequiredProperty("PATH")
            );
        }

        @Test
        @DisplayName(
            "should return result with unresolved properties when property " + 
            "cannot be resolved from any of the ServiceLoader resolvers."
        )
        public void test2() {
            ServiceLoaderResolver resolver = resolverToTest();

            List<String> propertiesToResolve = Arrays.asList(
                "nonexisting.property1", 
                "nonexisting.property2"
            );

            ResolverResult result = resolver.resolve(
                propertiesToResolve
            );
            
            assertFalse(result.hasResolvedProperties());
            assertTrue(result.hasUnresolvedProperties());
            
            for (String propertyName : propertiesToResolve) {
                assertTrue(
                    result.unresolvedPropertyNames().contains(propertyName)
                );
            }
        }
    }

    private ServiceLoaderResolver resolverToTest() {
        return new ServiceLoaderResolver();
    }
}
