package io.github.jeyjeyemem.externalizedproperties.core.resolvers;

import io.github.jeyjeyemem.externalizedproperties.core.CacheStrategy;
import io.github.jeyjeyemem.externalizedproperties.core.ExternalizedPropertyResolver;
import io.github.jeyjeyemem.externalizedproperties.core.testentities.StubCacheStrategy;
import io.github.jeyjeyemem.externalizedproperties.core.testentities.StubExternalizedPropertyResolver;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

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

public class CachingPropertyResolverTests {
    @Nested
    class Constructor {
        @Test
        @DisplayName("should throw when decorated argument is null")
        public void test1() {
            assertThrows(
                IllegalArgumentException.class, 
                () -> new CachingPropertyResolver(null, new StubCacheStrategy<>())
            );
        }

        @Test
        @DisplayName("should throw when cache strategy argument is null")
        public void test2() {
            assertThrows(
                IllegalArgumentException.class, 
                () -> new CachingPropertyResolver(
                    new StubExternalizedPropertyResolver(),
                    null
                )
            );
        }
    }

    @Nested
    class ResolveMethod {
        @Test
        @DisplayName("should throw when property name argument is null or empty")
        public void validationTest1() {
            CachingPropertyResolver resolver = resolverToTest(
                new StubExternalizedPropertyResolver()
            );

            assertThrows(
                IllegalArgumentException.class, 
                () -> resolver.resolve((String)null)
            );

            assertThrows(
                IllegalArgumentException.class, 
                () -> resolver.resolve("")
            );
        }

        @Test
        @DisplayName("should resolve property value from the decorated resolver")
        public void test1() {
            String propertyName = "property";
            StubExternalizedPropertyResolver decorated = new StubExternalizedPropertyResolver();

            CachingPropertyResolver resolver = resolverToTest(decorated);

            Optional<String> result = resolver.resolve(propertyName);
            
            assertNotNull(result);
            assertTrue(result.isPresent());
            assertEquals(
                decorated.resolvedProperties().get(propertyName), 
                result.get()
            );
        }

        @Test
        @DisplayName(
            "should return empty Optional " + 
            "when property is not found from the decorated resolver"
        )
        public void test2() {
            CachingPropertyResolver resolver = resolverToTest(
                new SystemPropertyResolver()
            );

            // Not in system properties.
            Optional<String> result = resolver.resolve("nonexistent.property");
            
            assertNotNull(result);
            assertFalse(result.isPresent());
        }

        @Test
        @DisplayName("should cache resolved property")
        public void cacheTest1() {
            String propertyName = "property";
            StubExternalizedPropertyResolver decorated = new StubExternalizedPropertyResolver();
            StubCacheStrategy<String, String> cacheStrategy = new StubCacheStrategy<>();

            CachingPropertyResolver resolver = resolverToTest(
                decorated,
                cacheStrategy
            );

            Optional<String> result = resolver.resolve(propertyName);
            
            assertNotNull(result);
            assertTrue(result.isPresent());

            // Check if property was cached via strategy.
            assertEquals(
                result.get(),
                cacheStrategy.getCache().get(propertyName)
            );
        }

        @Test
        @DisplayName("should not cache unresolved property")
        public void cacheTest2() {
            StubCacheStrategy<String, String> cacheStrategy = new StubCacheStrategy<>();

            CachingPropertyResolver resolver = resolverToTest(
                new SystemPropertyResolver(),
                cacheStrategy
            );

            // Property is not in system properties.
            Optional<String> result = resolver.resolve(
                "nonexistent.property"
            );

            assertNotNull(result);
            assertFalse(result.isPresent());

            // Check if property was cached via strategy.
            assertFalse(cacheStrategy.getCache().containsKey("nonexistent.property"));
        }

        @Test
        @DisplayName("should return cached property")
        public void cacheTest3() {
            String propertyName = "property.cached";
            StubExternalizedPropertyResolver decorated = new StubExternalizedPropertyResolver();

            StubCacheStrategy<String, String> cacheStrategy = new StubCacheStrategy<>();
            // Cache values.
            cacheStrategy.cache(propertyName, "cached.value");

            CachingPropertyResolver resolver = resolverToTest(
                decorated,
                cacheStrategy
            );

            // property.cache is not in system properties but is in the strategy cache.
            Optional<String> result = resolver.resolve(propertyName);
            
            assertNotNull(result);
            assertTrue(result.isPresent());
            // Did not go through decorated resolver.
            assertFalse(decorated.resolvedPropertyNames().contains(propertyName));
            // Got property from cache..
            assertEquals(
                cacheStrategy.getCache().get(propertyName), 
                result.get()
            );
        }
    }



    @Nested
    class ResolveMethodWithVarArgsOverload {
        @Test
        @DisplayName("should throw when property names varargs argument is null or empty")
        public void validationTest1() {
            CachingPropertyResolver resolver = resolverToTest(
                new SystemPropertyResolver()
            );

            assertThrows(
                IllegalArgumentException.class, 
                () -> resolver.resolve((String[])null)
            );
            
            assertThrows(
                IllegalArgumentException.class, 
                () -> resolver.resolve(new String[0])
            );
        }

        @Test
        @DisplayName("should throw when property names varargs contain any null or empty values")
        public void validationTest2() {
            CachingPropertyResolver resolver = resolverToTest(
                new SystemPropertyResolver()
            );

            assertThrows(
                IllegalArgumentException.class, 
                () -> resolver.resolve(new String[] { "property", null })
            );
            
            assertThrows(
                IllegalArgumentException.class, 
                () -> resolver.resolve(new String[] { "property", "" })
            );
        }

        @Test
        @DisplayName("should resolve property values from the decorated resolver")
        public void test1() {
            String[] propertyNames = new String[] { "property1", "property2" };
            StubExternalizedPropertyResolver decorated = new StubExternalizedPropertyResolver();
            CachingPropertyResolver resolver = resolverToTest(decorated);

            CachingPropertyResolver.Result result = resolver.resolve(propertyNames);

            assertTrue(result.hasResolvedProperties());
            assertFalse(result.hasUnresolvedProperties());

            // Check if properties were cached via strategy.
            for (String propertyName : propertyNames) {
                assertEquals(
                    decorated.resolvedProperties().get(propertyName), 
                    result.findRequiredProperty(propertyName)
                );
            }
        }

        @Test
        @DisplayName(
            "should return result with unresolved properties " + 
            "when property is not found from the decorated resolver"
        )
        public void test2() {
            String[] nonExistentProperties = new String[] {
                "nonexistent.property1", 
                "nonexistent.property2"
            };
            CachingPropertyResolver resolver = resolverToTest(
                new SystemPropertyResolver()
            );

            // Properties are not in system properties.
            CachingPropertyResolver.Result result = resolver.resolve(
                nonExistentProperties
            );
            
            assertFalse(result.hasResolvedProperties());
            assertTrue(result.hasUnresolvedProperties());

            for (String nonExistentProperty : nonExistentProperties) {
                assertTrue(result.unresolvedPropertyNames().contains(nonExistentProperty));
            }
        }

        @Test
        @DisplayName("should cache resolved properties")
        public void cacheTest1() {
            String[] propertyNames = new String[] { "property1", "property2" };
            StubExternalizedPropertyResolver decorated = new StubExternalizedPropertyResolver();
            StubCacheStrategy<String, String> cacheStrategy = new StubCacheStrategy<>();

            CachingPropertyResolver resolver = resolverToTest(
                decorated,
                cacheStrategy
            );

            CachingPropertyResolver.Result result = resolver.resolve(propertyNames);
            
            assertTrue(result.hasResolvedProperties());
            assertFalse(result.hasUnresolvedProperties());
            
            // Check if properties were cached via strategy.
            for (String propertyName : propertyNames) {
                assertEquals(
                    cacheStrategy.getCache().get(propertyName), 
                    result.findRequiredProperty(propertyName)
                );
            }
        }

        @Test
        @DisplayName("should not cache unresolved properties")
        public void cacheTest2() {
            String[] nonExistentProperties = new String[] {
                "nonexistent.property1", 
                "nonexistent.property2"
            };
            StubCacheStrategy<String, String> cacheStrategy = new StubCacheStrategy<>();

            CachingPropertyResolver resolver = resolverToTest(
                new SystemPropertyResolver(),
                cacheStrategy
            );

            CachingPropertyResolver.Result result = resolver.resolve(
                nonExistentProperties
            );
            
            assertFalse(result.hasResolvedProperties());
            assertTrue(result.hasUnresolvedProperties());

            for (String nonExistentProperty : nonExistentProperties) {
                assertTrue(result.unresolvedPropertyNames().contains(nonExistentProperty));
                assertFalse(cacheStrategy.getCache().containsKey(nonExistentProperty));
            }
        }

        @Test
        @DisplayName("should return cached properties")
        public void cacheTest3() {
            String[] propertyNames = new String[] { "property.cached1", "property.cached2" };
            StubExternalizedPropertyResolver decorated = new StubExternalizedPropertyResolver();

            StubCacheStrategy<String, String> cacheStrategy = new StubCacheStrategy<>();
            for (String propertyName : propertyNames) {
                cacheStrategy.cache(
                    propertyName, 
                    propertyName + "-value"
                );
            }

            CachingPropertyResolver resolver = resolverToTest(
                decorated,
                cacheStrategy
            );

            // property.cache is not in decorated resolver but is in the cache strategy.
            CachingPropertyResolver.Result result = resolver.resolve(propertyNames);
            
            assertTrue(result.hasResolvedProperties());
            assertFalse(result.hasUnresolvedProperties());

            for (String propertyName : propertyNames) {
                assertEquals(
                    cacheStrategy.getCache().get(propertyName), 
                    result.findRequiredProperty(propertyName)
                );

                assertFalse(decorated.resolvedPropertyNames().contains(propertyName));
            }
        }
    }

    @Nested
    class ResolveMethodWithCollectionOverload {
        @Test
        @DisplayName("should throw when property names collection argument is null or empty")
        public void validationTest1() {
            CachingPropertyResolver resolver = resolverToTest(
                new SystemPropertyResolver()
            );

            assertThrows(
                IllegalArgumentException.class, 
                () -> resolver.resolve((Collection<String>)null)
            );
            
            assertThrows(
                IllegalArgumentException.class, 
                () -> resolver.resolve(Collections.emptyList())
            );
        }

        @Test
        @DisplayName(
            "should throw when property names collection contains any null or empty values"
        )
        public void validationTest2() {
            CachingPropertyResolver resolver = resolverToTest(
                new SystemPropertyResolver()
            );

            assertThrows(
                IllegalArgumentException.class, 
                () -> resolver.resolve(Arrays.asList("property", null))
            );
            
            assertThrows(
                IllegalArgumentException.class, 
                () -> resolver.resolve(Arrays.asList("property", ""))
            );
        }

        @Test
        @DisplayName("should resolve property values from the decorated resolver")
        public void test1() {
            List<String> propertyNames = Arrays.asList("property1", "property2");
            StubExternalizedPropertyResolver decorated = new StubExternalizedPropertyResolver();
            CachingPropertyResolver resolver = resolverToTest(decorated);

            CachingPropertyResolver.Result result = resolver.resolve(propertyNames);

            assertTrue(result.hasResolvedProperties());
            assertFalse(result.hasUnresolvedProperties());

            // Check if properties were cached via strategy.
            for (String propertyName : propertyNames) {
                assertEquals(
                    decorated.resolvedProperties().get(propertyName), 
                    result.findRequiredProperty(propertyName)
                );
            }
        }

        @Test
        @DisplayName(
            "should return result with unresolved properties " + 
            "when property is not found from the decorated resolver"
        )
        public void test2() {
            List<String> nonExistentProperties = Arrays.asList(
                "nonexistent.property1", 
                "nonexistent.property2"
            );
            CachingPropertyResolver resolver = resolverToTest(
                new SystemPropertyResolver()
            );

            // Properties are not in system properties.
            CachingPropertyResolver.Result result = resolver.resolve(
                nonExistentProperties
            );
            
            assertFalse(result.hasResolvedProperties());
            assertTrue(result.hasUnresolvedProperties());

            for (String nonExistentProperty : nonExistentProperties) {
                assertTrue(result.unresolvedPropertyNames().contains(nonExistentProperty));
            }
        }

        @Test
        @DisplayName("should cache resolved properties")
        public void cacheTest1() {
            List<String> propertyNames = Arrays.asList("property1", "property2");
            StubExternalizedPropertyResolver decorated = new StubExternalizedPropertyResolver();
            StubCacheStrategy<String, String> cacheStrategy = new StubCacheStrategy<>();

            CachingPropertyResolver resolver = resolverToTest(
                decorated,
                cacheStrategy
            );

            CachingPropertyResolver.Result result = resolver.resolve(propertyNames);
            
            assertTrue(result.hasResolvedProperties());
            assertFalse(result.hasUnresolvedProperties());
            
            // Check if properties were cached via strategy.
            for (String propertyName : propertyNames) {
                assertEquals(
                    cacheStrategy.getCache().get(propertyName), 
                    result.findRequiredProperty(propertyName)
                );
            }
        }

        @Test
        @DisplayName("should not cache unresolved properties")
        public void cacheTest2() {
            List<String> nonExistentProperties = Arrays.asList(
                "nonexistent.property1", 
                "nonexistent.property2"
            );
            StubCacheStrategy<String, String> cacheStrategy = new StubCacheStrategy<>();

            CachingPropertyResolver resolver = resolverToTest(
                new SystemPropertyResolver(),
                cacheStrategy
            );

            CachingPropertyResolver.Result result = resolver.resolve(
                nonExistentProperties
            );
            
            assertFalse(result.hasResolvedProperties());
            assertTrue(result.hasUnresolvedProperties());

            for (String nonExistentProperty : nonExistentProperties) {
                assertTrue(result.unresolvedPropertyNames().contains(nonExistentProperty));
                assertFalse(cacheStrategy.getCache().containsKey(nonExistentProperty));
            }
        }

        @Test
        @DisplayName("should return cached properties")
        public void cacheTest3() {
            List<String> propertyNames = Arrays.asList("property.cached1", "property.cached2");
            StubExternalizedPropertyResolver decorated = new StubExternalizedPropertyResolver();

            StubCacheStrategy<String, String> cacheStrategy = new StubCacheStrategy<>();
            for (String propertyName : propertyNames) {
                cacheStrategy.cache(
                    propertyName, 
                    propertyName + "-value"
                );
            }

            CachingPropertyResolver resolver = resolverToTest(
                decorated,
                cacheStrategy
            );

            // property.cache is not in decorated resolver but is in the cache strategy.
            CachingPropertyResolver.Result result = resolver.resolve(propertyNames);
            
            assertTrue(result.hasResolvedProperties());
            assertFalse(result.hasUnresolvedProperties());

            for (String propertyName : propertyNames) {
                assertEquals(
                    cacheStrategy.getCache().get(propertyName), 
                    result.findRequiredProperty(propertyName)
                );

                assertFalse(decorated.resolvedPropertyNames().contains(propertyName));
            }
        }
    }

    private CachingPropertyResolver resolverToTest(
            ExternalizedPropertyResolver decorated
    ) {
        return resolverToTest(
            decorated,
            new StubCacheStrategy<>()
        );
    }

    private CachingPropertyResolver resolverToTest(
            ExternalizedPropertyResolver decorated,
            CacheStrategy<String, String> cacheStrategy
    ) {
        return new CachingPropertyResolver(
            decorated, 
            cacheStrategy
        );
    }
}
