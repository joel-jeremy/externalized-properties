package io.github.jeyjeyemem.externalizedproperties.core.internal;

import io.github.jeyjeyemem.externalizedproperties.core.CacheStrategy;
import io.github.jeyjeyemem.externalizedproperties.core.ExternalizedProperties;
import io.github.jeyjeyemem.externalizedproperties.core.ExternalizedPropertiesBuilder;
import io.github.jeyjeyemem.externalizedproperties.core.TypeReference;
import io.github.jeyjeyemem.externalizedproperties.core.exceptions.ConversionException;
import io.github.jeyjeyemem.externalizedproperties.core.testentities.StubCacheStrategy;
import io.github.jeyjeyemem.externalizedproperties.core.testentities.StubExternalizedPropertyResolver;
import io.github.jeyjeyemem.externalizedproperties.core.testentities.proxy.BasicProxyInterface;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Type;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class CachingExternalizedPropertiesTests {
    @Nested
    class Constructor {
        @Test
        @DisplayName("should throw when externalized properties argument is null")
        public void test1() {
            assertThrows(
                IllegalArgumentException.class, 
                () -> new CachingExternalizedProperties(
                    null,
                    new StubCacheStrategy<>(),
                    new StubCacheStrategy<>()
                )
            );
        }

        @Test
        @DisplayName("should throw when resolved property cache strategy argument is null")
        public void test2() {
            ExternalizedProperties decorated = ExternalizedPropertiesBuilder.newBuilder()
                .withDefaults()
                .build();

            assertThrows(
                IllegalArgumentException.class, 
                () -> new CachingExternalizedProperties(
                    decorated,
                    null,
                    new StubCacheStrategy<>()
                )
            );
        }

        @Test
        @DisplayName("should throw when variable expansion cache strategy argument is null")
        public void test3() {
            ExternalizedProperties decorated = ExternalizedPropertiesBuilder.newBuilder()
                .withDefaults()
                .build();

            assertThrows(
                IllegalArgumentException.class, 
                () -> new CachingExternalizedProperties(
                    decorated,
                    new StubCacheStrategy<>(),
                    null
                )
            );
        }
    }

    @Nested
    class ResolvePropertyMethod {
        @Test
        @DisplayName("should return cached property value")
        public void test1() {
            ExternalizedProperties decorated = ExternalizedPropertiesBuilder.newBuilder()
                .withDefaults()
                .build();

            String propertyName = "test.property";

            CacheStrategy<String, Optional<?>> resolvedPropertyCacheStrategy = 
                new StubCacheStrategy<>();
            resolvedPropertyCacheStrategy.cache(propertyName, Optional.of("cached.value"));

            CachingExternalizedProperties cachingExternalizedProperties = 
                new CachingExternalizedProperties(
                    decorated,
                    resolvedPropertyCacheStrategy,
                    new StubCacheStrategy<>()
                );

            Optional<String> resolved = cachingExternalizedProperties.resolveProperty(propertyName);

            assertTrue(resolved.isPresent());
            assertEquals("cached.value", resolved.get());
        }

        @Test
        @DisplayName(
            "should resolve property from decorated externalized properties " +
            "when property is not in cache"
        )
        public void test2() {
            StubExternalizedPropertyResolver resolver = new StubExternalizedPropertyResolver();

            ExternalizedProperties decorated = ExternalizedPropertiesBuilder.newBuilder()
                .withDefaults()
                .resolvers(resolver)
                .build();

            String propertyName = "test.property";

            // Empty cache.
            CachingExternalizedProperties cachingExternalizedProperties = 
                new CachingExternalizedProperties(
                    decorated,
                    new StubCacheStrategy<>(),
                    new StubCacheStrategy<>()
                );

            Optional<String> resolved = cachingExternalizedProperties.resolveProperty(propertyName);

            assertTrue(resolved.isPresent());
            assertEquals(propertyName + "-value", resolved.get());
            assertTrue(resolver.resolvedPropertyNames().contains(propertyName));
        }

        @Test
        @DisplayName(
            "should cache resolved properties from decorated externalized properties"
        )
        public void test3() {
            StubExternalizedPropertyResolver resolver = new StubExternalizedPropertyResolver();

            ExternalizedProperties decorated = ExternalizedPropertiesBuilder.newBuilder()
                .withDefaults()
                .resolvers(resolver)
                .build();

            String propertyName = "test.property";

            CacheStrategy<String, Optional<?>> resolvedPropertiesCacheStrategy =
                new StubCacheStrategy<>();

            // Empty cache.
            CachingExternalizedProperties cachingExternalizedProperties = 
                new CachingExternalizedProperties(
                    decorated,
                    resolvedPropertiesCacheStrategy,
                    new StubCacheStrategy<>()
                );

            Optional<String> resolved = cachingExternalizedProperties.resolveProperty(propertyName);

            assertTrue(resolved.isPresent());
            assertEquals(propertyName + "-value", resolved.get());

            Optional<Optional<?>> cached = resolvedPropertiesCacheStrategy.get(propertyName);
            assertTrue(cached.isPresent());
            assertEquals(Optional.of(propertyName + "-value"), cached.get());
        }
        
        @Test
        @DisplayName(
            "should return empty Optional when decorated externalized properties " +
            "cannot resolve property"
        )
        public void test4() {
            StubExternalizedPropertyResolver resolver = new StubExternalizedPropertyResolver(
                StubExternalizedPropertyResolver.NULL_VALUE_RESOLVER
            );

            ExternalizedProperties decorated = ExternalizedPropertiesBuilder.newBuilder()
                .withDefaults()
                .resolvers(resolver)
                .build();

            String propertyName = "test.property";

            // Empty cache.
            CachingExternalizedProperties cachingExternalizedProperties = 
                new CachingExternalizedProperties(
                    decorated,
                    new StubCacheStrategy<>(),
                    new StubCacheStrategy<>()
                );

            Optional<String> resolved = cachingExternalizedProperties.resolveProperty(propertyName);

            assertFalse(resolved.isPresent());
            assertFalse(resolver.resolvedPropertyNames().contains(propertyName));
        }
    }

    @Nested
    class ResolvePropertyMethodWithClassOverload {
        @Test
        @DisplayName("should return cached property value")
        public void test1() {
            ExternalizedProperties decorated = ExternalizedPropertiesBuilder.newBuilder()
                .withDefaults()
                .build();

            String propertyName = "test.property";

            CacheStrategy<String, Optional<?>> resolvedPropertyCacheStrategy = 
                new StubCacheStrategy<>();
            resolvedPropertyCacheStrategy.cache(propertyName, Optional.of(1));

            CachingExternalizedProperties cachingExternalizedProperties = 
                new CachingExternalizedProperties(
                    decorated,
                    resolvedPropertyCacheStrategy,
                    new StubCacheStrategy<>()
                );

            Optional<Integer> resolved = cachingExternalizedProperties.resolveProperty(
                propertyName,
                Integer.class
            );

            assertTrue(resolved.isPresent());
            assertEquals(1, resolved.get());
        }

        @Test
        @DisplayName(
            "should resolve property from decorated externalized properties " +
            "when property is not in cache"
        )
        public void test2() {
            // Always resolves to 1.
            StubExternalizedPropertyResolver resolver = new StubExternalizedPropertyResolver(
                propertyName -> "1"
            );

            ExternalizedProperties decorated = ExternalizedPropertiesBuilder.newBuilder()
                .withDefaults()
                .resolvers(resolver)
                .build();

            String propertyName = "test.property";

            // Empty cache.
            CachingExternalizedProperties cachingExternalizedProperties = 
                new CachingExternalizedProperties(
                    decorated,
                    new StubCacheStrategy<>(),
                    new StubCacheStrategy<>()
                );

            Optional<Integer> resolved = cachingExternalizedProperties.resolveProperty(
                propertyName,
                Integer.class
            );

            assertTrue(resolved.isPresent());
            assertEquals(1, resolved.get());
            assertTrue(resolver.resolvedPropertyNames().contains(propertyName));
        }

        @Test
        @DisplayName(
            "should cache resolved properties from decorated externalized properties"
        )
        public void test3() {
            // Always resolves to 1.
            StubExternalizedPropertyResolver resolver = new StubExternalizedPropertyResolver(
                propertyName -> "1"
            );

            ExternalizedProperties decorated = ExternalizedPropertiesBuilder.newBuilder()
                .withDefaults()
                .resolvers(resolver)
                .build();

            String propertyName = "test.property";

            CacheStrategy<String, Optional<?>> resolvedPropertiesCacheStrategy =
                new StubCacheStrategy<>();

            // Empty cache.
            CachingExternalizedProperties cachingExternalizedProperties = 
                new CachingExternalizedProperties(
                    decorated,
                    resolvedPropertiesCacheStrategy,
                    new StubCacheStrategy<>()
                );

            Optional<Integer> resolved = cachingExternalizedProperties.resolveProperty(
                propertyName,
                Integer.class
            );

            assertTrue(resolved.isPresent());
            assertEquals(1, resolved.get());

            Optional<Optional<?>> cached = resolvedPropertiesCacheStrategy.get(propertyName);
            assertTrue(cached.isPresent());
            assertEquals(Optional.of(1), cached.get());
        }
        
        @Test
        @DisplayName(
            "should return empty Optional when decorated externalized properties " +
            "cannot resolve property"
        )
        public void test4() {
            StubExternalizedPropertyResolver resolver = new StubExternalizedPropertyResolver(
                StubExternalizedPropertyResolver.NULL_VALUE_RESOLVER
            );

            ExternalizedProperties decorated = ExternalizedPropertiesBuilder.newBuilder()
                .withDefaults()
                .resolvers(resolver)
                .build();

            String propertyName = "test.property";

            // Empty cache.
            CachingExternalizedProperties cachingExternalizedProperties = 
                new CachingExternalizedProperties(
                    decorated,
                    new StubCacheStrategy<>(),
                    new StubCacheStrategy<>()
                );

            Optional<Integer> resolved = cachingExternalizedProperties.resolveProperty(
                propertyName,
                Integer.class
            );

            assertFalse(resolved.isPresent());
            assertFalse(resolver.resolvedPropertyNames().contains(propertyName));
        }

        @Test
        @DisplayName("should throw when property cannot be converted to the target class")
        public void test5() {
            StubExternalizedPropertyResolver resolver = new StubExternalizedPropertyResolver(
                propertyName -> "invalid_integer"
            );

            ExternalizedProperties decorated = ExternalizedPropertiesBuilder.newBuilder()
                .withDefaults()
                .resolvers(resolver)
                .build();

            String propertyName = "test.property";

            CachingExternalizedProperties cachingExternalizedProperties = 
                new CachingExternalizedProperties(
                    decorated,
                    new StubCacheStrategy<>(),
                    new StubCacheStrategy<>()
                );
            
            assertThrows(
                ConversionException.class, 
                () -> cachingExternalizedProperties.resolveProperty(
                    propertyName,
                    Integer.class
                )
            );
        }
    }

    @Nested
    class ResolvePropertyMethodWithTypeReferenceOverload {
        @Test
        @DisplayName("should return cached property value")
        public void test1() {
            ExternalizedProperties decorated = ExternalizedPropertiesBuilder.newBuilder()
                .withDefaults()
                .build();

            String propertyName = "test.property";

            CacheStrategy<String, Optional<?>> resolvedPropertyCacheStrategy = 
                new StubCacheStrategy<>();
            resolvedPropertyCacheStrategy.cache(propertyName, Optional.of(1));

            CachingExternalizedProperties cachingExternalizedProperties = 
                new CachingExternalizedProperties(
                    decorated,
                    resolvedPropertyCacheStrategy,
                    new StubCacheStrategy<>()
                );

            Optional<Integer> resolved = cachingExternalizedProperties.resolveProperty(
                propertyName,
                new TypeReference<Integer>(){}
            );

            assertTrue(resolved.isPresent());
            assertEquals(1, resolved.get());
        }

        @Test
        @DisplayName(
            "should resolve property from decorated externalized properties " +
            "when property is not in cache"
        )
        public void test2() {
            // Always resolves to 1.
            StubExternalizedPropertyResolver resolver = new StubExternalizedPropertyResolver(
                propertyName -> "1"
            );

            ExternalizedProperties decorated = ExternalizedPropertiesBuilder.newBuilder()
                .withDefaults()
                .resolvers(resolver)
                .build();

            String propertyName = "test.property";

            // Empty cache.
            CachingExternalizedProperties cachingExternalizedProperties = 
                new CachingExternalizedProperties(
                    decorated,
                    new StubCacheStrategy<>(),
                    new StubCacheStrategy<>()
                );

            Optional<Integer> resolved = cachingExternalizedProperties.resolveProperty(
                propertyName,
                new TypeReference<Integer>(){}
            );

            assertTrue(resolved.isPresent());
            assertEquals(1, resolved.get());
            assertTrue(resolver.resolvedPropertyNames().contains(propertyName));
        }

        @Test
        @DisplayName(
            "should cache resolved properties from decorated externalized properties"
        )
        public void test3() {
            // Always resolves to 1.
            StubExternalizedPropertyResolver resolver = new StubExternalizedPropertyResolver(
                propertyName -> "1"
            );

            ExternalizedProperties decorated = ExternalizedPropertiesBuilder.newBuilder()
                .withDefaults()
                .resolvers(resolver)
                .build();

            String propertyName = "test.property";

            CacheStrategy<String, Optional<?>> resolvedPropertiesCacheStrategy =
                new StubCacheStrategy<>();

            // Empty cache.
            CachingExternalizedProperties cachingExternalizedProperties = 
                new CachingExternalizedProperties(
                    decorated,
                    resolvedPropertiesCacheStrategy,
                    new StubCacheStrategy<>()
                );

            Optional<Integer> resolved = cachingExternalizedProperties.resolveProperty(
                propertyName,
                new TypeReference<Integer>(){}
            );

            assertTrue(resolved.isPresent());
            assertEquals(1, resolved.get());

            Optional<Optional<?>> cached = resolvedPropertiesCacheStrategy.get(propertyName);
            assertTrue(cached.isPresent());
            assertEquals(Optional.of(1), cached.get());
        }
        
        @Test
        @DisplayName(
            "should return empty Optional when decorated externalized properties " +
            "cannot resolve property"
        )
        public void test4() {
            StubExternalizedPropertyResolver resolver = new StubExternalizedPropertyResolver(
                StubExternalizedPropertyResolver.NULL_VALUE_RESOLVER
            );

            ExternalizedProperties decorated = ExternalizedPropertiesBuilder.newBuilder()
                .withDefaults()
                .resolvers(resolver)
                .build();

            String propertyName = "test.property";

            // Empty cache.
            CachingExternalizedProperties cachingExternalizedProperties = 
                new CachingExternalizedProperties(
                    decorated,
                    new StubCacheStrategy<>(),
                    new StubCacheStrategy<>()
                );

            Optional<Integer> resolved = cachingExternalizedProperties.resolveProperty(
                propertyName,
                new TypeReference<Integer>(){}
            );

            assertFalse(resolved.isPresent());
            assertFalse(resolver.resolvedPropertyNames().contains(propertyName));
        }

        @Test
        @DisplayName("should throw when property cannot be converted to the target class")
        public void test5() {
            StubExternalizedPropertyResolver resolver = new StubExternalizedPropertyResolver(
                propertyName -> "invalid_integer"
            );

            ExternalizedProperties decorated = ExternalizedPropertiesBuilder.newBuilder()
                .withDefaults()
                .resolvers(resolver)
                .build();

            String propertyName = "test.property";

            CachingExternalizedProperties cachingExternalizedProperties = 
                new CachingExternalizedProperties(
                    decorated,
                    new StubCacheStrategy<>(),
                    new StubCacheStrategy<>()
                );
            
            assertThrows(
                ConversionException.class, 
                () -> cachingExternalizedProperties.resolveProperty(
                    propertyName,
                    new TypeReference<Integer>(){}
                )
            );
        }
    }

    @Nested
    class ResolvePropertyMethodWithTypeOverload {
        @Test
        @DisplayName("should return cached property value")
        public void test1() {
            ExternalizedProperties decorated = ExternalizedPropertiesBuilder.newBuilder()
                .withDefaults()
                .build();

            String propertyName = "test.property";

            CacheStrategy<String, Optional<?>> resolvedPropertyCacheStrategy = 
                new StubCacheStrategy<>();
            resolvedPropertyCacheStrategy.cache(propertyName, Optional.of(1));

            CachingExternalizedProperties cachingExternalizedProperties = 
                new CachingExternalizedProperties(
                    decorated,
                    resolvedPropertyCacheStrategy,
                    new StubCacheStrategy<>()
                );

            Optional<?> resolved = cachingExternalizedProperties.resolveProperty(
                propertyName,
                (Type)Integer.class
            );

            assertTrue(resolved.isPresent());
            assertEquals(1, resolved.get());
        }

        @Test
        @DisplayName(
            "should resolve property from decorated externalized properties " +
            "when property is not in cache"
        )
        public void test2() {
            // Always resolves to 1.
            StubExternalizedPropertyResolver resolver = new StubExternalizedPropertyResolver(
                propertyName -> "1"
            );

            ExternalizedProperties decorated = ExternalizedPropertiesBuilder.newBuilder()
                .withDefaults()
                .resolvers(resolver)
                .build();

            String propertyName = "test.property";

            // Empty cache.
            CachingExternalizedProperties cachingExternalizedProperties = 
                new CachingExternalizedProperties(
                    decorated,
                    new StubCacheStrategy<>(),
                    new StubCacheStrategy<>()
                );

            Optional<?> resolved = cachingExternalizedProperties.resolveProperty(
                propertyName,
                (Type)Integer.class
            );

            assertTrue(resolved.isPresent());
            assertEquals(1, resolved.get());
            assertTrue(resolver.resolvedPropertyNames().contains(propertyName));
        }

        @Test
        @DisplayName(
            "should cache resolved properties from decorated externalized properties"
        )
        public void test3() {
            // Always resolves to 1.
            StubExternalizedPropertyResolver resolver = new StubExternalizedPropertyResolver(
                propertyName -> "1"
            );

            ExternalizedProperties decorated = ExternalizedPropertiesBuilder.newBuilder()
                .withDefaults()
                .resolvers(resolver)
                .build();

            String propertyName = "test.property";

            CacheStrategy<String, Optional<?>> resolvedPropertiesCacheStrategy =
                new StubCacheStrategy<>();

            // Empty cache.
            CachingExternalizedProperties cachingExternalizedProperties = 
                new CachingExternalizedProperties(
                    decorated,
                    resolvedPropertiesCacheStrategy,
                    new StubCacheStrategy<>()
                );

            Optional<?> resolved = cachingExternalizedProperties.resolveProperty(
                propertyName,
                (Type)Integer.class
            );

            assertTrue(resolved.isPresent());
            assertEquals(1, resolved.get());

            Optional<Optional<?>> cached = resolvedPropertiesCacheStrategy.get(propertyName);
            assertTrue(cached.isPresent());
            assertEquals(Optional.of(1), cached.get());
        }
        
        @Test
        @DisplayName(
            "should return empty Optional when decorated externalized properties " +
            "cannot resolve property"
        )
        public void test4() {
            StubExternalizedPropertyResolver resolver = new StubExternalizedPropertyResolver(
                StubExternalizedPropertyResolver.NULL_VALUE_RESOLVER
            );

            ExternalizedProperties decorated = ExternalizedPropertiesBuilder.newBuilder()
                .withDefaults()
                .resolvers(resolver)
                .build();

            String propertyName = "test.property";

            // Empty cache.
            CachingExternalizedProperties cachingExternalizedProperties = 
                new CachingExternalizedProperties(
                    decorated,
                    new StubCacheStrategy<>(),
                    new StubCacheStrategy<>()
                );

            Optional<?> resolved = cachingExternalizedProperties.resolveProperty(
                propertyName,
                (Type)Integer.class
            );

            assertFalse(resolved.isPresent());
            assertFalse(resolver.resolvedPropertyNames().contains(propertyName));
        }

        @Test
        @DisplayName("should throw when property cannot be converted to the target class")
        public void test5() {
            StubExternalizedPropertyResolver resolver = new StubExternalizedPropertyResolver(
                propertyName -> "invalid_integer"
            );

            ExternalizedProperties decorated = ExternalizedPropertiesBuilder.newBuilder()
                .withDefaults()
                .resolvers(resolver)
                .build();

            String propertyName = "test.property";

            CachingExternalizedProperties cachingExternalizedProperties = 
                new CachingExternalizedProperties(
                    decorated,
                    new StubCacheStrategy<>(),
                    new StubCacheStrategy<>()
                );
            
            assertThrows(
                ConversionException.class, 
                () -> cachingExternalizedProperties.resolveProperty(
                    propertyName,
                    (Type)Integer.class
                )
            );
        }
    }

    @Nested
    class ExpandVariablesMethod {
        @Test
        @DisplayName("should return cached expanded value")
        public void test1() {
            ExternalizedProperties decorated = ExternalizedPropertiesBuilder.newBuilder()
                .withDefaults()
                .build();

            String propertyName = "test.property";
            String sourceString = propertyName + ".${myvar}";

            CacheStrategy<String, String> variableExpansionCacheStrategy = 
                new StubCacheStrategy<>();
            variableExpansionCacheStrategy.cache(sourceString, "cached.expanded.value");

            CachingExternalizedProperties cachingExternalizedProperties = 
                new CachingExternalizedProperties(
                    decorated,
                    new StubCacheStrategy<>(),
                    variableExpansionCacheStrategy
                );

            String expanded = cachingExternalizedProperties.expandVariables(sourceString);

            // Returned cached value instead of going through decorated instance.
            assertEquals("cached.expanded.value", expanded);
        }

        @Test
        @DisplayName(
            "should expand variables via decorated externalized properties " +
            "when property is not in cache"
        )
        public void test2() {
            StubExternalizedPropertyResolver resolver = new StubExternalizedPropertyResolver(
                propertyName -> propertyName + ".variable"
            );

            ExternalizedProperties decorated = ExternalizedPropertiesBuilder.newBuilder()
                .withDefaults()
                .resolvers(resolver)
                .build();

            // Empty cache.
            CachingExternalizedProperties cachingExternalizedProperties = 
                new CachingExternalizedProperties(
                    decorated,
                    new StubCacheStrategy<>(),
                    new StubCacheStrategy<>()
                );

            String propertyName = "test.property";
            String sourceString = propertyName + ".${myvar}";

            String expanded = cachingExternalizedProperties.expandVariables(sourceString);

            assertEquals(propertyName + ".myvar.variable", expanded);
        }

        @Test
        @DisplayName(
            "should cache expanded results from decorated externalized properties"
        )
        public void test3() {
            StubExternalizedPropertyResolver resolver = new StubExternalizedPropertyResolver(
                propertyName -> propertyName + ".variable"
            );

            ExternalizedProperties decorated = ExternalizedPropertiesBuilder.newBuilder()
                .withDefaults()
                .resolvers(resolver)
                .build();
            
            CacheStrategy<String, String> variableExpansionCacheStrategy =
                new StubCacheStrategy<>();

            // Empty cache.
            CachingExternalizedProperties cachingExternalizedProperties = 
                new CachingExternalizedProperties(
                    decorated,
                    new StubCacheStrategy<>(),
                    variableExpansionCacheStrategy
                );

            String propertyName = "test.property";
            String sourceString = propertyName + ".${myvar}";

            String expanded = cachingExternalizedProperties.expandVariables(
                sourceString
            );

            assertEquals(propertyName + ".myvar.variable", expanded);

            Optional<String> cached = variableExpansionCacheStrategy.get(sourceString);
            assertTrue(cached.isPresent());
            assertEquals(propertyName + ".myvar.variable", cached.get());
        }
    }

    @Nested
    class ProxyMethod {
        @Test
        @DisplayName("should delegate to decorated externalized properties")
        public void test1() {
            ExternalizedProperties decorated = ExternalizedPropertiesBuilder.newBuilder()
                .withDefaults()
                .build();

            // Empty cache.
            CachingExternalizedProperties cachingExternalizedProperties = 
                new CachingExternalizedProperties(
                    decorated,
                    new StubCacheStrategy<>(),
                    new StubCacheStrategy<>()
                );

            BasicProxyInterface proxy = cachingExternalizedProperties.proxy(
                BasicProxyInterface.class
            );

            assertNotNull(proxy);
        }
    }

    @Nested
    class ProxyMethodWithClassLoaderOverload {
        @Test
        @DisplayName("should delegate to decorated externalized properties")
        public void test1() {
            ExternalizedProperties decorated = ExternalizedPropertiesBuilder.newBuilder()
                .withDefaults()
                .build();

            // Empty cache.
            CachingExternalizedProperties cachingExternalizedProperties = 
                new CachingExternalizedProperties(
                    decorated,
                    new StubCacheStrategy<>(),
                    new StubCacheStrategy<>()
                );

            BasicProxyInterface proxy = cachingExternalizedProperties.proxy(
                BasicProxyInterface.class,
                BasicProxyInterface.class.getClassLoader()
            );

            assertNotNull(proxy);
        }
    }
}
