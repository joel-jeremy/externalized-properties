package io.github.jeyjeyemem.externalizedproperties.core.internal;

import io.github.jeyjeyemem.externalizedproperties.core.ExternalizedPropertyResolver;
import io.github.jeyjeyemem.externalizedproperties.core.TypeReference;
import io.github.jeyjeyemem.externalizedproperties.core.conversion.handlers.DefaultConversionHandler;
import io.github.jeyjeyemem.externalizedproperties.core.exceptions.ConversionException;
import io.github.jeyjeyemem.externalizedproperties.core.exceptions.VariableExpansionException;
import io.github.jeyjeyemem.externalizedproperties.core.internal.invocationhandlers.ExternalizedPropertyInvocationHandler;
import io.github.jeyjeyemem.externalizedproperties.core.testentities.StubExternalizedPropertyResolver;
import io.github.jeyjeyemem.externalizedproperties.core.testentities.proxy.BasicProxyInterface;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Proxy;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class InternalExternalizedPropertiesTests {
    @Nested
    class ResolvePropertyMethod {
        @Test
        @DisplayName("should return resolved property value")
        public void test1() {
            // Just return property name.
            StubExternalizedPropertyResolver resolver =
                new StubExternalizedPropertyResolver();
            
            InternalExternalizedProperties externalizedProperties = 
                internalExternalizedProperties(resolver);

            Optional<String> property = externalizedProperties.resolveProperty("test.property");

            assertTrue(property.isPresent());
            assertEquals("test.property-value", property.get());
        }

        @Test
        @DisplayName("should return empty Optional when property cannot resolved")
        public void test2() {
            // Properties not resolved.
            StubExternalizedPropertyResolver resolver =
                new StubExternalizedPropertyResolver(
                    StubExternalizedPropertyResolver.NULL_VALUE_RESOLVER
                );
            
            InternalExternalizedProperties externalizedProperties = 
                internalExternalizedProperties(resolver);

            Optional<String> property = externalizedProperties.resolveProperty("test.property");

            assertFalse(property.isPresent());
        }
    }

    @Nested
    class ResolvePropertyMethodWithClassOverload {
        @Test
        @DisplayName("should throw when property name argument is null or empty")
        public void test1() {
            // Always returns 1.
            StubExternalizedPropertyResolver resolver =
                new StubExternalizedPropertyResolver(propertyName -> "1");
            
            InternalExternalizedProperties externalizedProperties = 
                internalExternalizedProperties(resolver);

            assertThrows(
                IllegalArgumentException.class, 
                () -> externalizedProperties.resolveProperty(
                    null,
                    Integer.class
                )
            );

            assertThrows(
                IllegalArgumentException.class, 
                () -> externalizedProperties.resolveProperty(
                    "",
                    Integer.class
                )
            );
        }
        @Test
        @DisplayName("should throw when expected type argument is null")
        public void test2() {
            // Always returns 1.
            StubExternalizedPropertyResolver resolver =
                new StubExternalizedPropertyResolver(propertyName -> "1");
            
            InternalExternalizedProperties externalizedProperties = 
                internalExternalizedProperties(resolver);

            assertThrows(
                IllegalArgumentException.class, 
                () -> externalizedProperties.resolveProperty(
                    "test.property",
                    (Class<?>)null
                )
            );
        }

        @Test
        @DisplayName("should convert property to the target class")
        public void test3() {
            // Always returns 1.
            StubExternalizedPropertyResolver resolver =
                new StubExternalizedPropertyResolver(propertyName -> "1");
            
            InternalExternalizedProperties externalizedProperties = 
                internalExternalizedProperties(resolver);

            Optional<Integer> property = externalizedProperties.resolveProperty(
                "test.property",
                Integer.class
            );

            assertTrue(property.isPresent());
            assertEquals(1, property.get());
        }

        @Test
        @DisplayName("should throw when property cannot be converted to the target class")
        public void test4() {
            StubExternalizedPropertyResolver resolver =
                new StubExternalizedPropertyResolver(propertyName -> "invalid_integer");
            
            InternalExternalizedProperties externalizedProperties = 
                internalExternalizedProperties(resolver);
            
            assertThrows(
                ConversionException.class, 
                () -> externalizedProperties.resolveProperty(
                    "test.property",
                    Integer.class
                )
            );
        }
    }

    @Nested
    class ResolvePropertyMethodWithTypeReferenceOverload {
        @Test
        @DisplayName("should throw when property name argument is null or empty")
        public void test1() {
            // Always returns 1.
            StubExternalizedPropertyResolver resolver =
                new StubExternalizedPropertyResolver(propertyName -> "1");
            
            InternalExternalizedProperties externalizedProperties = 
                internalExternalizedProperties(resolver);

            assertThrows(
                IllegalArgumentException.class, 
                () -> externalizedProperties.resolveProperty(
                    null,
                    new TypeReference<List<Integer>>(){}
                )
            );

            assertThrows(
                IllegalArgumentException.class, 
                () -> externalizedProperties.resolveProperty(
                    "",
                    new TypeReference<List<Integer>>(){}
                )
            );
        }
        @Test
        @DisplayName("should throw when expected type argument is null")
        public void test2() {
            // Always returns 1.
            StubExternalizedPropertyResolver resolver =
                new StubExternalizedPropertyResolver(propertyName -> "1");
            
            InternalExternalizedProperties externalizedProperties = 
                internalExternalizedProperties(resolver);

            assertThrows(
                IllegalArgumentException.class, 
                () -> externalizedProperties.resolveProperty(
                    "test.property",
                    (TypeReference<?>)null
                )
            );
        }

        @Test
        @DisplayName("should convert property to the target type reference")
        public void test3() {
            // Always returns 1,2,3.
            StubExternalizedPropertyResolver resolver =
                new StubExternalizedPropertyResolver(propertyName -> "1,2,3");
            
            InternalExternalizedProperties externalizedProperties = 
                internalExternalizedProperties(resolver);

            Optional<List<Integer>> property = externalizedProperties.resolveProperty(
                "test.property",
                new TypeReference<List<Integer>>(){}
            );

            assertTrue(property.isPresent());
            assertIterableEquals(
                Arrays.asList(
                    1, 2, 3
                ), 
                property.get()
            );
        }

        @Test
        @DisplayName("should throw when property cannot be converted to the target type reference")
        public void test4() {
            StubExternalizedPropertyResolver resolver =
                new StubExternalizedPropertyResolver(propertyName -> "invalid_integer");
            
            InternalExternalizedProperties externalizedProperties = 
                internalExternalizedProperties(resolver);
            
            assertThrows(
                ConversionException.class, 
                () -> externalizedProperties.resolveProperty(
                    "test.property",
                    new TypeReference<Integer>(){}
                )
            );
        }
    }

    @Nested
    class ResolvePropertyMethodWithTypeOverload {
        @Test
        @DisplayName("should throw when property name argument is null or empty")
        public void test1() {
            // Always returns 1.
            StubExternalizedPropertyResolver resolver =
                new StubExternalizedPropertyResolver(propertyName -> "1");
            
            InternalExternalizedProperties externalizedProperties = 
                internalExternalizedProperties(resolver);

            assertThrows(
                IllegalArgumentException.class, 
                () -> externalizedProperties.resolveProperty(
                    null,
                    (Type)Integer.class
                )
            );

            assertThrows(
                IllegalArgumentException.class, 
                () -> externalizedProperties.resolveProperty(
                    "",
                    (Type)Integer.class
                )
            );
        }
        @Test
        @DisplayName("should throw when expected type argument is null")
        public void test2() {
            // Always returns 1.
            StubExternalizedPropertyResolver resolver =
                new StubExternalizedPropertyResolver(propertyName -> "1");
            
            InternalExternalizedProperties externalizedProperties = 
                internalExternalizedProperties(resolver);

            assertThrows(
                IllegalArgumentException.class, 
                () -> externalizedProperties.resolveProperty(
                    "test.property",
                    (Type)null
                )
            );
        }

        @Test
        @DisplayName("should convert property to the target type")
        public void test3() {
            // Always returns 1.
            StubExternalizedPropertyResolver resolver =
                new StubExternalizedPropertyResolver(propertyName -> "1");
            
            InternalExternalizedProperties externalizedProperties = 
                internalExternalizedProperties(resolver);

            Optional<Integer> property = externalizedProperties.resolveProperty(
                "test.property",
                (Type)Integer.class
            );

            assertTrue(property.isPresent());
            assertEquals(1, property.get());
        }

        @Test
        @DisplayName("should throw when property cannot be converted to the target type")
        public void test4() {
            StubExternalizedPropertyResolver resolver =
                new StubExternalizedPropertyResolver(propertyName -> "invalid_integer");
            
            InternalExternalizedProperties externalizedProperties = 
                internalExternalizedProperties(resolver);
            
            assertThrows(
                ConversionException.class, 
                () -> externalizedProperties.resolveProperty(
                    "test.property",
                    (Type)Integer.class
                )
            );
        }
    }

    @Nested
    class ExpandVariablesMethod {
        @Test
        @DisplayName("should expand variables in source string")
        public void test1() {
            // Always returns propertyName + .variable.
            StubExternalizedPropertyResolver resolver =
                new StubExternalizedPropertyResolver(
                    propertyName -> propertyName + ".variable"
                );
            
            InternalExternalizedProperties externalizedProperties = 
                internalExternalizedProperties(resolver);

            String result = externalizedProperties.expandVariables("test.property.${myvar}");

            assertNotNull(result);
            assertEquals("test.property.myvar.variable", result);
        }

        @Test
        @DisplayName(
            "should throw when requested variable value in source string cannot be resolved"
        )
        public void test2() {
            // Do not resolve any property.
            StubExternalizedPropertyResolver resolver =
                new StubExternalizedPropertyResolver(
                    StubExternalizedPropertyResolver.NULL_VALUE_RESOLVER
                );
            
            InternalExternalizedProperties externalizedProperties = 
                internalExternalizedProperties(resolver);

            assertThrows(
                VariableExpansionException.class, 
                () -> externalizedProperties.expandVariables(
                    "test.property.${non.existing.var}"
                )
            );
        }
    }

    @Nested
    class ProxyMethod {
        @Test
        @DisplayName("should throw when proxy interface argument is null")
        public void test1() {
            // Do not resolve any property.
            StubExternalizedPropertyResolver resolver =
                new StubExternalizedPropertyResolver(
                    StubExternalizedPropertyResolver.NULL_VALUE_RESOLVER
                );
            
            InternalExternalizedProperties externalizedProperties = 
                internalExternalizedProperties(resolver);

            assertThrows(
                IllegalArgumentException.class, 
                () -> externalizedProperties.proxy(null) 
            );
        }

        @Test
        @DisplayName("should create a proxy")
        public void test2() {
            // Do not resolve any property.
            StubExternalizedPropertyResolver resolver =
                new StubExternalizedPropertyResolver(
                    StubExternalizedPropertyResolver.NULL_VALUE_RESOLVER
                );
            
            InternalExternalizedProperties externalizedProperties = 
                internalExternalizedProperties(resolver);

            BasicProxyInterface proxy = externalizedProperties.proxy(BasicProxyInterface.class);

            assertNotNull(proxy);
            assertTrue(proxy instanceof Proxy);
        }
    }

    @Nested
    class ProxyMethodWithClassLoaderOverload {
        @Test
        @DisplayName("should throw when proxy interface argument is null")
        public void test1() {
            // Do not resolve any property.
            StubExternalizedPropertyResolver resolver =
                new StubExternalizedPropertyResolver(
                    StubExternalizedPropertyResolver.NULL_VALUE_RESOLVER
                );
            
            InternalExternalizedProperties externalizedProperties = 
                internalExternalizedProperties(resolver);

            assertThrows(
                IllegalArgumentException.class, 
                () -> externalizedProperties.proxy(null, getClass().getClassLoader()) 
            );
        }

        @Test
        @DisplayName("should throw when class loader argument is null")
        public void test2() {
            // Do not resolve any property.
            StubExternalizedPropertyResolver resolver =
                new StubExternalizedPropertyResolver(
                    StubExternalizedPropertyResolver.NULL_VALUE_RESOLVER
                );
            
            InternalExternalizedProperties externalizedProperties = 
                internalExternalizedProperties(resolver);

            assertThrows(
                IllegalArgumentException.class, 
                () -> externalizedProperties.proxy(BasicProxyInterface.class, null)
            );
        }

        @Test
        @DisplayName("should create a proxy")
        public void test3() {
            // Do not resolve any property.
            StubExternalizedPropertyResolver resolver =
                new StubExternalizedPropertyResolver(
                    StubExternalizedPropertyResolver.NULL_VALUE_RESOLVER
                );
            
            InternalExternalizedProperties externalizedProperties = 
                internalExternalizedProperties(resolver);

            BasicProxyInterface proxy = externalizedProperties.proxy(
                BasicProxyInterface.class,
                BasicProxyInterface.class.getClassLoader()
            );

            assertNotNull(proxy);
            assertTrue(proxy instanceof Proxy);
        }
    }

    private InternalExternalizedProperties internalExternalizedProperties(
            ExternalizedPropertyResolver resolver
    ) {
        return new InternalExternalizedProperties(
            resolver, 
            new InternalConverter(
                new DefaultConversionHandler()
            ), 
            new InternalVariableExpander(resolver),
            (ep, proxyInterface) -> 
                new ExternalizedPropertyInvocationHandler(ep)
        );
    }
}
