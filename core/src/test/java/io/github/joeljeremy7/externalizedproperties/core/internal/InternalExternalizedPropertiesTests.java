package io.github.joeljeremy7.externalizedproperties.core.internal;

import io.github.joeljeremy7.externalizedproperties.core.ExternalizedProperty;
import io.github.joeljeremy7.externalizedproperties.core.ResolverProvider;
import io.github.joeljeremy7.externalizedproperties.core.conversion.converters.DefaultConverter;
import io.github.joeljeremy7.externalizedproperties.core.internal.conversion.RootConverter;
import io.github.joeljeremy7.externalizedproperties.core.internal.processing.RootProcessor;
import io.github.joeljeremy7.externalizedproperties.core.internal.proxy.ExternalizedPropertiesInvocationHandler;
import io.github.joeljeremy7.externalizedproperties.core.internal.resolvers.RootResolver;
import io.github.joeljeremy7.externalizedproperties.core.testfixtures.StubResolver;
import io.github.joeljeremy7.externalizedproperties.core.variableexpansion.SimpleVariableExpander;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Proxy;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class InternalExternalizedPropertiesTests {
    @Nested
    class ProxyMethod {
        @Test
        @DisplayName("should throw when proxy interface argument is null")
        public void test1() {
            // Do not resolve any property.
            ResolverProvider<?> provider = StubResolver.provider(
                StubResolver.NULL_VALUE_RESOLVER
            );
            
            InternalExternalizedProperties externalizedProperties = 
                internalExternalizedProperties(provider);

            assertThrows(
                IllegalArgumentException.class, 
                () -> externalizedProperties.proxy(null) 
            );
        }

        @Test
        @DisplayName("should create a proxy")
        public void test2() {
            // Do not resolve any property.
            ResolverProvider<?> provider = StubResolver.provider(
                StubResolver.NULL_VALUE_RESOLVER
            );
            
            InternalExternalizedProperties externalizedProperties = 
                internalExternalizedProperties(provider);

            ProxyInterface proxy = externalizedProperties.proxy(ProxyInterface.class);

            assertNotNull(proxy);
            assertTrue(proxy instanceof Proxy);
        }
    }

    @Nested
    class ProxyMethodWithClassLoaderOverload {
        @Test
        @DisplayName("should throw when proxy interface argument is null")
        public void test1() {
            InternalExternalizedProperties externalizedProperties = 
                internalExternalizedProperties(StubResolver.provider());

            assertThrows(
                IllegalArgumentException.class, 
                () -> externalizedProperties.proxy(null, getClass().getClassLoader()) 
            );
        }

        @Test
        @DisplayName("should throw when class loader argument is null")
        public void test2() {
            InternalExternalizedProperties externalizedProperties = 
                internalExternalizedProperties(StubResolver.provider());

            assertThrows(
                IllegalArgumentException.class, 
                () -> externalizedProperties.proxy(ProxyInterface.class, null)
            );
        }

        @Test
        @DisplayName("should throw when proxy interface argument is not an interface")
        public void test3() {
            InternalExternalizedProperties externalizedProperties = 
                internalExternalizedProperties(StubResolver.provider());

            assertThrows(
                IllegalArgumentException.class, 
                () -> externalizedProperties.proxy(InternalExternalizedPropertiesTests.class)
            );
        }

        @Test
        @DisplayName("should throw when proxy interface contains void-returning methods")
        public void test4() {
            InternalExternalizedProperties externalizedProperties = 
                internalExternalizedProperties(StubResolver.provider());

            assertThrows(
                IllegalArgumentException.class, 
                () -> externalizedProperties.proxy(VoidReturnTypeProxyInterface.class)
            );
        }

        @Test
        @DisplayName(
            "should throw when proxy interface @ResolverMethod does not have " +
            "a single String argument"
        )
        public void test5() {
            InternalExternalizedProperties externalizedProperties = 
                internalExternalizedProperties(StubResolver.provider());

            assertThrows(
                IllegalArgumentException.class, 
                () -> externalizedProperties.proxy(
                    NoStringArgProxyInterface.class
                )
            );
        }

        @Test
        @DisplayName(
            "should throw when proxy interface @ResolverMethod have " +
            "multiple arguments"
        )
        public void test6() {
            InternalExternalizedProperties externalizedProperties = 
                internalExternalizedProperties(StubResolver.provider());

            assertThrows(
                IllegalArgumentException.class, 
                () -> externalizedProperties.proxy(
                    MultipleArgsProxyInterface.class
                )
            );
        }

        @Test
        @DisplayName(
            "should throw when proxy interface @ResolverMethod have " +
            "non-String argument"
        )
        public void test7() {
            InternalExternalizedProperties externalizedProperties = 
                internalExternalizedProperties(StubResolver.provider());

            assertThrows(
                IllegalArgumentException.class, 
                () -> externalizedProperties.proxy(
                    InvalidArgTypeProxyInterface.class
                )
            );
        }

        @Test
        @DisplayName("should create a proxy")
        public void test8() {
            // Do not resolve any property.
            ResolverProvider<?> provider = StubResolver.provider(
                StubResolver.NULL_VALUE_RESOLVER
            );
            
            InternalExternalizedProperties externalizedProperties = 
                internalExternalizedProperties(provider);

            ProxyInterface proxy = externalizedProperties.proxy(
                ProxyInterface.class,
                ProxyInterface.class.getClassLoader()
            );

            assertNotNull(proxy);
            assertTrue(proxy instanceof Proxy);
        }
    }

    private InternalExternalizedProperties internalExternalizedProperties(
            ResolverProvider<?> resolverProviderToUse
    ) {
        ResolverProvider<RootResolver> rootResolverProvider = RootResolver.provider(
            Arrays.asList(resolverProviderToUse),
            e -> new RootProcessor(e),
            e -> new SimpleVariableExpander(e)
        );

        RootConverter.Provider rootConverterProvider = RootConverter.provider(
            Arrays.asList((e, r) -> new DefaultConverter(r))
        );

        return new InternalExternalizedProperties(
            rootResolverProvider,
            rootConverterProvider,
            (resolver, converter, proxyInterface) -> 
                new ExternalizedPropertiesInvocationHandler(resolver, converter)
        );
    }
    
    private static interface NoStringArgProxyInterface {
        // Invalid: Must have a single String argument
        @ExternalizedProperty
        String resolve();
    }

    private static interface MultipleArgsProxyInterface {
        // Invalid: Must be a single String argument
        @ExternalizedProperty
        String resolve(String arg1, String arg2, int arg3);
    }

    private static interface InvalidArgTypeProxyInterface {
        // Invalid: Must be a single String argument
        @ExternalizedProperty
        String resolve(int invalidMustBeString);
    }
    public static interface ProxyInterface {
        @ExternalizedProperty("property")
        String property();
    }

    static interface VoidReturnTypeProxyInterface {
        // Invalid: Void return types not allowed.
        @ExternalizedProperty("test.invalid.method.void")
        void invalidVoidMethod();
    
        // Invalid: Void return types not allowed.
        @ExternalizedProperty("test.invalid.method.void")
        Void invalidVoidClassMethod();
    }
}
