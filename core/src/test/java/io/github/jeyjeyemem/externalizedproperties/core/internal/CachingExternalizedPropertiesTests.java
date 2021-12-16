package io.github.jeyjeyemem.externalizedproperties.core.internal;

import io.github.jeyjeyemem.externalizedproperties.core.CacheStrategy;
import io.github.jeyjeyemem.externalizedproperties.core.ExternalizedProperties;
import io.github.jeyjeyemem.externalizedproperties.core.ExternalizedPropertiesBuilder;
import io.github.jeyjeyemem.externalizedproperties.core.Processors;
import io.github.jeyjeyemem.externalizedproperties.core.TypeReference;
import io.github.jeyjeyemem.externalizedproperties.core.exceptions.ConversionException;
import io.github.jeyjeyemem.externalizedproperties.core.processing.Base64Decode;
import io.github.jeyjeyemem.externalizedproperties.core.proxy.ProxyMethodInfo;
import io.github.jeyjeyemem.externalizedproperties.core.testentities.StubCacheStrategy;
import io.github.jeyjeyemem.externalizedproperties.core.testentities.StubResolver;
import io.github.jeyjeyemem.externalizedproperties.core.testentities.StubProxyMethodInfo;
import io.github.jeyjeyemem.externalizedproperties.core.testentities.proxy.BasicProxyInterface;
import io.github.jeyjeyemem.externalizedproperties.core.testentities.proxy.PrimitiveProxyInterface;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Type;
import java.util.Base64;
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

            Optional<String> resolved = 
                cachingExternalizedProperties.resolveProperty(propertyName);

            assertTrue(resolved.isPresent());
            assertEquals("cached.value", resolved.get());
        }

        @Test
        @DisplayName(
            "should resolve property from decorated externalized properties " +
            "when property is not in cache"
        )
        public void test2() {
            StubResolver resolver = new StubResolver();

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

            Optional<String> resolved = 
                cachingExternalizedProperties.resolveProperty(propertyName);

            assertTrue(resolved.isPresent());
            assertEquals(propertyName + "-value", resolved.get());
            assertTrue(resolver.resolvedPropertyNames().contains(propertyName));
        }

        @Test
        @DisplayName(
            "should cache resolved properties from decorated externalized properties"
        )
        public void test3() {
            StubResolver resolver = new StubResolver();

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

            Optional<String> resolved = 
                cachingExternalizedProperties.resolveProperty(propertyName);

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
            StubResolver resolver = new StubResolver(
                StubResolver.NULL_VALUE_RESOLVER
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

            Optional<String> resolved = 
                cachingExternalizedProperties.resolveProperty(propertyName);

            assertFalse(resolved.isPresent());
            assertFalse(resolver.resolvedPropertyNames().contains(propertyName));
        }
    }

    @Nested
    class ResolvePropertyMethodWithProcessorsOverload {
        @Test
        @DisplayName("should return cached property value")
        public void test1() {
            ExternalizedProperties decorated = ExternalizedPropertiesBuilder.newBuilder()
                .withDefaults()
                .processors(new Base64Decode())
                .build();

            String propertyName = "test.property";

            CacheStrategy<String, Optional<?>> resolvedPropertyCacheStrategy = 
                new StubCacheStrategy<>();
            resolvedPropertyCacheStrategy.cache(
                propertyName, 
                Optional.of("cached.value")
            );

            CachingExternalizedProperties cachingExternalizedProperties = 
                new CachingExternalizedProperties(
                    decorated,
                    resolvedPropertyCacheStrategy,
                    new StubCacheStrategy<>()
                );

            Optional<String> resolved = cachingExternalizedProperties.resolveProperty(
                propertyName,
                Processors.of(Base64Decode.class)
            );

            assertTrue(resolved.isPresent());
            assertEquals("cached.value", resolved.get());
        }

        @Test
        @DisplayName(
            "should resolve property from decorated externalized properties " +
            "when property is not in cache"
        )
        public void test2() {
            StubResolver resolver = new StubResolver(
                propertyName -> base64Encode(propertyName)
            );

            ExternalizedProperties decorated = ExternalizedPropertiesBuilder.newBuilder()
                .withDefaults()
                .resolvers(resolver)
                .processors(new Base64Decode())
                .build();

            String propertyName = "test.property";

            // Empty cache.
            CachingExternalizedProperties cachingExternalizedProperties = 
                new CachingExternalizedProperties(
                    decorated,
                    new StubCacheStrategy<>(),
                    new StubCacheStrategy<>()
                );

            Optional<String> resolved = cachingExternalizedProperties.resolveProperty(
                propertyName,
                Processors.of(Base64Decode.class)
            );

            assertTrue(resolved.isPresent());
            assertEquals(propertyName, resolved.get());
            assertTrue(resolver.resolvedPropertyNames().contains(propertyName));
        }

        @Test
        @DisplayName(
            "should cache resolved properties from decorated externalized properties"
        )
        public void test3() {
            StubResolver resolver = new StubResolver(
                propertyName -> base64Encode(propertyName)
            );

            ExternalizedProperties decorated = ExternalizedPropertiesBuilder.newBuilder()
                .withDefaults()
                .resolvers(resolver)
                .processors(new Base64Decode())
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

            Optional<String> resolved = cachingExternalizedProperties.resolveProperty(
                propertyName,
                Processors.of(Base64Decode.class)
            );

            assertTrue(resolved.isPresent());
            assertEquals(propertyName, resolved.get());

            Optional<Optional<?>> cached = resolvedPropertiesCacheStrategy.get(propertyName);
            assertTrue(cached.isPresent());
            assertEquals(Optional.of(propertyName), cached.get());
        }
        
        @Test
        @DisplayName(
            "should return empty Optional when decorated externalized properties " +
            "cannot resolve property"
        )
        public void test4() {
            StubResolver resolver = new StubResolver(
                StubResolver.NULL_VALUE_RESOLVER
            );

            ExternalizedProperties decorated = ExternalizedPropertiesBuilder.newBuilder()
                .withDefaults()
                .resolvers(resolver)
                .processors(new Base64Decode())
                .build();

            String propertyName = "test.property";

            // Empty cache.
            CachingExternalizedProperties cachingExternalizedProperties = 
                new CachingExternalizedProperties(
                    decorated,
                    new StubCacheStrategy<>(),
                    new StubCacheStrategy<>()
                );

            Optional<String> resolved = cachingExternalizedProperties.resolveProperty(
                propertyName,
                Processors.of(Base64Decode.class)
            );

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
            StubResolver resolver = new StubResolver(
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
            StubResolver resolver = new StubResolver(
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
            StubResolver resolver = new StubResolver(
                StubResolver.NULL_VALUE_RESOLVER
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
            StubResolver resolver = new StubResolver(
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
    class ResolvePropertyMethodWithProcessorsAndClassOverload {
        @Test
        @DisplayName("should return cached property value")
        public void test1() {
            ExternalizedProperties decorated = ExternalizedPropertiesBuilder.newBuilder()
                .withDefaults()
                .processors(new Base64Decode())
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
                Processors.of(Base64Decode.class),
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
            // Always resolves to base 64 encoded "1".
            StubResolver resolver = new StubResolver(
                propertyName -> base64Encode("1")
            );

            ExternalizedProperties decorated = ExternalizedPropertiesBuilder.newBuilder()
                .withDefaults()
                .resolvers(resolver)
                .processors(new Base64Decode())
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
                Processors.of(Base64Decode.class),
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
            // Always resolves to base 64 encoded "1".
            StubResolver resolver = new StubResolver(
                propertyName -> base64Encode("1")
            );

            ExternalizedProperties decorated = ExternalizedPropertiesBuilder.newBuilder()
                .withDefaults()
                .resolvers(resolver)
                .processors(new Base64Decode())
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
                Processors.of(Base64Decode.class),
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
            StubResolver resolver = new StubResolver(
                StubResolver.NULL_VALUE_RESOLVER
            );

            ExternalizedProperties decorated = ExternalizedPropertiesBuilder.newBuilder()
                .withDefaults()
                .resolvers(resolver)
                .processors(new Base64Decode())
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
                Processors.of(Base64Decode.class),
                Integer.class
            );

            assertFalse(resolved.isPresent());
            assertFalse(resolver.resolvedPropertyNames().contains(propertyName));
        }

        @Test
        @DisplayName("should throw when property cannot be converted to the target class")
        public void test5() {
            StubResolver resolver = new StubResolver(
                propertyName -> base64Encode("invalid_integer")
            );

            ExternalizedProperties decorated = ExternalizedPropertiesBuilder.newBuilder()
                .withDefaults()
                .resolvers(resolver)
                .processors(new Base64Decode())
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
                    Processors.of(Base64Decode.class),
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
            StubResolver resolver = new StubResolver(
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
            StubResolver resolver = new StubResolver(
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
            StubResolver resolver = new StubResolver(
                StubResolver.NULL_VALUE_RESOLVER
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
            StubResolver resolver = new StubResolver(
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
    class ResolvePropertyMethodWithProcessorsAndTypeReferenceOverload {
        @Test
        @DisplayName("should return cached property value")
        public void test1() {
            ExternalizedProperties decorated = ExternalizedPropertiesBuilder.newBuilder()
                .withDefaults()
                .processors(new Base64Decode())
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
                Processors.of(Base64Decode.class),
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
            // Always resolves to base 64 encoded "1".
            StubResolver resolver = new StubResolver(
                propertyName -> base64Encode("1")
            );

            ExternalizedProperties decorated = ExternalizedPropertiesBuilder.newBuilder()
                .withDefaults()
                .resolvers(resolver)
                .processors(new Base64Decode())
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
                Processors.of(Base64Decode.class),
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
            // Always resolves to base 64 encoded "1".
            StubResolver resolver = new StubResolver(
                propertyName -> base64Encode("1")
            );

            ExternalizedProperties decorated = ExternalizedPropertiesBuilder.newBuilder()
                .withDefaults()
                .resolvers(resolver)
                .processors(new Base64Decode())
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
                Processors.of(Base64Decode.class),
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
            StubResolver resolver = new StubResolver(
                StubResolver.NULL_VALUE_RESOLVER
            );

            ExternalizedProperties decorated = ExternalizedPropertiesBuilder.newBuilder()
                .withDefaults()
                .resolvers(resolver)
                .processors(new Base64Decode())
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
                Processors.of(Base64Decode.class),
                new TypeReference<Integer>(){}
            );

            assertFalse(resolved.isPresent());
            assertFalse(resolver.resolvedPropertyNames().contains(propertyName));
        }

        @Test
        @DisplayName("should throw when property cannot be converted to the target class")
        public void test5() {
            StubResolver resolver = new StubResolver(
                propertyName -> base64Encode("invalid_integer")
            );

            ExternalizedProperties decorated = ExternalizedPropertiesBuilder.newBuilder()
                .withDefaults()
                .resolvers(resolver)
                .processors(new Base64Decode())
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
                    Processors.of(Base64Decode.class),
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
            StubResolver resolver = new StubResolver(
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
            StubResolver resolver = new StubResolver(
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
            StubResolver resolver = new StubResolver(
                StubResolver.NULL_VALUE_RESOLVER
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
            StubResolver resolver = new StubResolver(
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
    class ResolvePropertyMethodWithProcessorsAndTypeOverload {
        @Test
        @DisplayName("should return cached property value")
        public void test1() {
            ExternalizedProperties decorated = ExternalizedPropertiesBuilder.newBuilder()
                .withDefaults()
                .processors(new Base64Decode())
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
                Processors.of(Base64Decode.class),
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
            // Always resolves to base 64 encoded "1".
            StubResolver resolver = new StubResolver(
                propertyName -> base64Encode("1")
            );

            ExternalizedProperties decorated = ExternalizedPropertiesBuilder.newBuilder()
                .withDefaults()
                .resolvers(resolver)
                .processors(new Base64Decode())
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
                Processors.of(Base64Decode.class),
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
            // Always resolves to base 64 encoded "1".
            StubResolver resolver = new StubResolver(
                propertyName -> base64Encode("1")
            );

            ExternalizedProperties decorated = ExternalizedPropertiesBuilder.newBuilder()
                .withDefaults()
                .resolvers(resolver)
                .processors(new Base64Decode())
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
                Processors.of(Base64Decode.class),
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
            StubResolver resolver = new StubResolver(
                StubResolver.NULL_VALUE_RESOLVER
            );

            ExternalizedProperties decorated = ExternalizedPropertiesBuilder.newBuilder()
                .withDefaults()
                .resolvers(resolver)
                .processors(new Base64Decode())
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
                Processors.of(Base64Decode.class),
                (Type)Integer.class
            );

            assertFalse(resolved.isPresent());
            assertFalse(resolver.resolvedPropertyNames().contains(propertyName));
        }

        @Test
        @DisplayName("should throw when property cannot be converted to the target class")
        public void test5() {
            StubResolver resolver = new StubResolver(
                propertyName -> base64Encode("invalid_integer")
            );

            ExternalizedProperties decorated = ExternalizedPropertiesBuilder.newBuilder()
                .withDefaults()
                .resolvers(resolver)
                .processors(new Base64Decode())
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
                    Processors.of(Base64Decode.class),
                    (Type)Integer.class
                )
            );
        }
    }

    @Nested
    class ResolvePropertyMethodWithProxyMethodInfoOverload {
        @Test
        @DisplayName("should throw when proxy method info argument is null")
        public void test1() {
            ExternalizedProperties decorated = ExternalizedPropertiesBuilder.newBuilder()
                .withDefaults()
                .build();

            CachingExternalizedProperties cachingExternalizedProperties = 
                new CachingExternalizedProperties(
                    decorated,
                    new StubCacheStrategy<>(),
                    new StubCacheStrategy<>()
                );

            assertThrows(
                IllegalArgumentException.class, 
                () -> cachingExternalizedProperties.resolveProperty((ProxyMethodInfo)null)
            );
        }

        @Test
        @DisplayName("should return cached property value")
        public void test2() {
            ExternalizedProperties decorated = ExternalizedPropertiesBuilder.newBuilder()
                .withDefaults()
                .build();

            String propertyName = "property.integer.primitive";

            CacheStrategy<String, Optional<?>> resolvedPropertyCacheStrategy = 
                new StubCacheStrategy<>();
            resolvedPropertyCacheStrategy.cache(propertyName, Optional.of(1));

            CachingExternalizedProperties cachingExternalizedProperties = 
                new CachingExternalizedProperties(
                    decorated,
                    resolvedPropertyCacheStrategy,
                    new StubCacheStrategy<>()
                );
            
            StubProxyMethodInfo proxyMethodInfo = StubProxyMethodInfo.fromMethod(
                PrimitiveProxyInterface.class, 
                "intPrimitiveProperty"
            );

            Optional<?> resolved = 
                cachingExternalizedProperties.resolveProperty(proxyMethodInfo);

            assertTrue(resolved.isPresent());
            assertEquals(1, (int)resolved.get());
        }

        @Test
        @DisplayName(
            "should resolve property from decorated externalized properties " +
            "when property is not in cache"
        )
        public void test3() {
            // Always resolves to 1.
            StubResolver resolver = new StubResolver(
                propertyName -> "1"
            );

            ExternalizedProperties decorated = ExternalizedPropertiesBuilder.newBuilder()
                .withDefaultConversionHandlers()
                .resolvers(resolver)
                .build();

            // Empty cache.
            CachingExternalizedProperties cachingExternalizedProperties = 
                new CachingExternalizedProperties(
                    decorated,
                    new StubCacheStrategy<>(),
                    new StubCacheStrategy<>()
                );

            StubProxyMethodInfo proxyMethodInfo = StubProxyMethodInfo.fromMethod(
                PrimitiveProxyInterface.class, 
                "intPrimitiveProperty"
            );

            Optional<?> resolved = 
                cachingExternalizedProperties.resolveProperty(proxyMethodInfo);

            assertTrue(resolved.isPresent());
            assertEquals(1, (int)resolved.get());

            String propertyName = "property.integer.primitive";
            assertTrue(resolver.resolvedPropertyNames().contains(propertyName));
        }

        @Test
        @DisplayName(
            "should cache resolved properties from decorated externalized properties"
        )
        public void test4() {
            // Always resolves to 1.
            StubResolver resolver = new StubResolver(
                propertyName -> "1"
            );

            ExternalizedProperties decorated = ExternalizedPropertiesBuilder.newBuilder()
                .withDefaultConversionHandlers()
                .resolvers(resolver)
                .build();

            CacheStrategy<String, Optional<?>> resolvedPropertiesCacheStrategy =
                new StubCacheStrategy<>();

            // Empty cache.
            CachingExternalizedProperties cachingExternalizedProperties = 
                new CachingExternalizedProperties(
                    decorated,
                    resolvedPropertiesCacheStrategy,
                    new StubCacheStrategy<>()
                );

            StubProxyMethodInfo proxyMethodInfo = StubProxyMethodInfo.fromMethod(
                PrimitiveProxyInterface.class, 
                "intPrimitiveProperty"
            );

            Optional<?> resolved = 
                cachingExternalizedProperties.resolveProperty(proxyMethodInfo);

            assertTrue(resolved.isPresent());
            assertEquals(1, (int)resolved.get());

            String propertyName = "property.integer.primitive";
            Optional<Optional<?>> cached = resolvedPropertiesCacheStrategy.get(propertyName);
            assertTrue(cached.isPresent());
            assertEquals(Optional.of(1), cached.get());
        }
        
        @Test
        @DisplayName(
            "should return empty Optional when decorated externalized properties " +
            "cannot resolve property"
        )
        public void test5() {
            StubResolver resolver = new StubResolver(
                StubResolver.NULL_VALUE_RESOLVER
            );

            ExternalizedProperties decorated = ExternalizedPropertiesBuilder.newBuilder()
                .withDefaultConversionHandlers()
                .resolvers(resolver)
                .build();

            // Empty cache.
            CachingExternalizedProperties cachingExternalizedProperties = 
                new CachingExternalizedProperties(
                    decorated,
                    new StubCacheStrategy<>(),
                    new StubCacheStrategy<>()
                );

            StubProxyMethodInfo proxyMethodInfo = StubProxyMethodInfo.fromMethod(
                PrimitiveProxyInterface.class, 
                "intPrimitiveProperty"
            );

            Optional<?> resolved = 
                cachingExternalizedProperties.resolveProperty(proxyMethodInfo);

            assertFalse(resolved.isPresent());

            String propertyName = "property.integer.primitive";
            assertFalse(resolver.resolvedPropertyNames().contains(propertyName));
        }

        @Test
        @DisplayName("should throw when property cannot be converted to the target class")
        public void test6() {
            // Invalid integer.
            StubResolver resolver = new StubResolver(
                propertyName -> "invalid_integer"
            );

            ExternalizedProperties decorated = ExternalizedPropertiesBuilder.newBuilder()
                .withDefaultConversionHandlers()
                .resolvers(resolver)
                .build();

            CachingExternalizedProperties cachingExternalizedProperties = 
                new CachingExternalizedProperties(
                    decorated,
                    new StubCacheStrategy<>(),
                    new StubCacheStrategy<>()
                );
            
            StubProxyMethodInfo proxyMethodInfo = StubProxyMethodInfo.fromMethod(
                PrimitiveProxyInterface.class, 
                "intPrimitiveProperty"
            );

            assertThrows(
                ConversionException.class, 
                () -> cachingExternalizedProperties.resolveProperty(proxyMethodInfo)
            );
        }



        @Test
        @DisplayName(
            "should throw when proxy method info does not have @ExternalizedProperty annotation."
        )
        public void test7() {
            // Invalid integer.
            StubResolver resolver = 
                new StubResolver();

            ExternalizedProperties decorated = ExternalizedPropertiesBuilder.newBuilder()
                .withDefaultConversionHandlers()
                .resolvers(resolver)
                .build();

            CachingExternalizedProperties cachingExternalizedProperties = 
                new CachingExternalizedProperties(
                    decorated,
                    new StubCacheStrategy<>(),
                    new StubCacheStrategy<>()
                );
            
            StubProxyMethodInfo proxyMethodInfo = StubProxyMethodInfo.fromMethod(
                BasicProxyInterface.class, 
                "propertyWithNoAnnotationAndNoDefaultValue"
            );

            assertThrows(
                IllegalArgumentException.class, 
                () -> cachingExternalizedProperties.resolveProperty(proxyMethodInfo)
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
            StubResolver resolver = new StubResolver(
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
            StubResolver resolver = new StubResolver(
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

    private String base64Encode(String value) {
        return new String(Base64.getEncoder().encode(value.getBytes()));
    }
}
