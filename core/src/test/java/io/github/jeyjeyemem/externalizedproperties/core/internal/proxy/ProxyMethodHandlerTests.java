package io.github.jeyjeyemem.externalizedproperties.core.internal.proxy;

import io.github.jeyjeyemem.externalizedproperties.core.Converter;
import io.github.jeyjeyemem.externalizedproperties.core.ConverterProvider;
import io.github.jeyjeyemem.externalizedproperties.core.ExternalizedProperties;
import io.github.jeyjeyemem.externalizedproperties.core.ExternalizedPropertiesException;
import io.github.jeyjeyemem.externalizedproperties.core.Resolver;
import io.github.jeyjeyemem.externalizedproperties.core.ResolverProvider;
import io.github.jeyjeyemem.externalizedproperties.core.UnresolvedPropertiesException;
import io.github.jeyjeyemem.externalizedproperties.core.VariableExpanderProvider;
import io.github.jeyjeyemem.externalizedproperties.core.conversion.converters.DefaultConverter;
import io.github.jeyjeyemem.externalizedproperties.core.internal.MethodHandleFactory;
import io.github.jeyjeyemem.externalizedproperties.core.internal.conversion.RootConverter;
import io.github.jeyjeyemem.externalizedproperties.core.internal.processing.RootProcessor;
import io.github.jeyjeyemem.externalizedproperties.core.internal.resolvers.RootResolver;
import io.github.jeyjeyemem.externalizedproperties.core.resolvers.DefaultResolver;
import io.github.jeyjeyemem.externalizedproperties.core.testentities.proxy.BasicProxyInterface;
import io.github.jeyjeyemem.externalizedproperties.core.testentities.proxy.DefaultValueProxyInterface;
import io.github.jeyjeyemem.externalizedproperties.core.testentities.proxy.SystemPropertiesProxyInterface;
import io.github.jeyjeyemem.externalizedproperties.core.testentities.proxy.NoAnnotationProxyInterface;
import io.github.jeyjeyemem.externalizedproperties.core.testentities.proxy.OptionalProxyInterface;
import io.github.jeyjeyemem.externalizedproperties.core.testentities.proxy.ThrowingProxyInterface;
import io.github.jeyjeyemem.externalizedproperties.core.testfixtures.ProxyMethodUtils;
import io.github.jeyjeyemem.externalizedproperties.core.variableexpansion.SimpleVariableExpander;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class ProxyMethodHandlerTests {
    private final ResolverProvider<?> resolverProvider = ep -> new DefaultResolver();
    private final ConverterProvider<?> converterProvider = 
        (ep, rootConverter) -> new DefaultConverter(rootConverter);
    private final VariableExpanderProvider<?> variableExpanderProvider = 
        ep -> new SimpleVariableExpander(ep);
    private final ExternalizedProperties externalizedProperties = 
        ExternalizedProperties.builder()
            .resolvers(resolverProvider)
            .converters(converterProvider)
            .variableExpander(variableExpanderProvider)
            .build();
    
    private final MethodHandleFactory methodHandleFactory = new MethodHandleFactory();
    
    @Nested
    class Constructor {
        @Test
        @DisplayName("should throw when resolver argument is null.")
        public void test1() {
            assertThrows(IllegalArgumentException.class, () ->
                new ProxyMethodHandler(
                    null,
                    rootConverter(),
                    methodHandleFactory
                )
            );
        }

        @Test
        @DisplayName("should throw when converter argument is null.")
        public void test2() {
            assertThrows(IllegalArgumentException.class, () ->
                new ProxyMethodHandler(
                    rootResolver(),
                    null,
                    methodHandleFactory
                )
            );
        }

        @Test
        @DisplayName("should throw when method handle builder argument is null.")
        public void test3() {
            assertThrows(IllegalArgumentException.class, () ->
                new ProxyMethodHandler(
                    rootResolver(),
                    rootConverter(),
                    null
                )
            );
        }
    }

    @Nested
    class InvokeDefaultInterfaceMethodMethod {
        @Test
        @DisplayName("should invoke default interface method.")
        public void test1() {
            Class<DefaultValueProxyInterface> proxyInterface = 
                DefaultValueProxyInterface.class;

            Object proxy = proxy(proxyInterface);    
            Method method = getProxyInterfaceMethod(proxyInterface, "propertyWithDefaultValue");
            ProxyMethodHandler proxyMethodHandler = proxyMethodHandler();

            Object value = proxyMethodHandler.invokeDefaultInterfaceMethod(
                proxy,
                method,
                new String[0]
            );

            // See DefaultValueProxyInterface.propertyWithDefaultValue default return value.
            assertEquals("default.value", value);
        }

        @Test
        @DisplayName("should throw when method is not a default interface method.")
        public void test2() {
            Class<BasicProxyInterface> proxyInterface = 
                BasicProxyInterface.class;

            Object proxy = proxy(proxyInterface); 
            Method method = getProxyInterfaceMethod(proxyInterface, "property");

            ProxyMethodHandler proxyMethodHandler = proxyMethodHandler();

            assertThrows(
                IllegalStateException.class, 
                () -> proxyMethodHandler.invokeDefaultInterfaceMethod(proxy, method, new String[0])
            );
        }

        @Test
        @DisplayName(
            "should rethrow same runtime exception when default interface method throws an exception."
        )
        public void test3() {
            Class<ThrowingProxyInterface> proxyInterface = 
                ThrowingProxyInterface.class;

            Object proxy = proxy(proxyInterface); 
            Method method = getProxyInterfaceMethod(proxyInterface, "throwRuntimeException");
            ProxyMethodHandler proxyMethodHandler = proxyMethodHandler();

            assertThrows(
                RuntimeException.class, 
                () -> proxyMethodHandler.invokeDefaultInterfaceMethod(
                    proxy,
                    method,
                    new String[0]
                )
            );
        }

        @Test
        @DisplayName(
            "should wrap non-runtime exception thrown by default interface method."
        )
        public void test4() {
            Class<ThrowingProxyInterface> proxyInterface = 
                ThrowingProxyInterface.class;

            Object proxy = proxy(proxyInterface); 
            Method method = getProxyInterfaceMethod(proxyInterface, "throwException");
            ProxyMethodHandler proxyMethod = proxyMethodHandler();

            assertThrows(
                ExternalizedPropertiesException.class, 
                () -> proxyMethod.invokeDefaultInterfaceMethod(
                    proxy,
                    method,
                    new String[0]
                )
            );
        }

        @Test
        @DisplayName("should receive method arguments.")
        public void test5() {
            Class<DefaultValueProxyInterface> proxyInterface = 
                DefaultValueProxyInterface.class;

            Object proxy = proxy(proxyInterface); 
            Method method = getProxyInterfaceMethod(
                proxyInterface,
                "propertyWithDefaultValueParameter",
                String.class // Method has one string parameter.
            );
            ProxyMethodHandler proxyMethodHandler = proxyMethodHandler();

            Object value = proxyMethodHandler.invokeDefaultInterfaceMethod(
                proxy,
                method,
                new String[] { "my-default-value" }
            );

            assertEquals("my-default-value", value);
        }
    }

    @Nested
    class DetermineDefaultValueMethod {
        @Test
        @DisplayName(
            "should return default interface method invocation result " + 
            "when method is a default interface method."
        )
        public void test1() {
            Class<DefaultValueProxyInterface> proxyInterface = 
                DefaultValueProxyInterface.class;

            Object proxy = proxy(proxyInterface); 
            Method method = getProxyInterfaceMethod(
                proxyInterface,
                "propertyWithDefaultValue"
            );
            ProxyMethodHandler proxyMethodHandler = proxyMethodHandler();

            Object value = proxyMethodHandler.determineDefaultValueOrThrow(
                proxy,
                method,
                new String[0]
            );

            // See DefaultValueProxyInterface.propertyWithDefaultValue default return value.
            assertEquals("default.value", value);
        }

        @Test
        @DisplayName(
            "should return an empty Optional when method has an Optional method return type."
        )
        public void test2() {
            Class<OptionalProxyInterface> proxyInterface = 
                OptionalProxyInterface.class;

            Object proxy = proxy(proxyInterface); 
            Method method = getProxyInterfaceMethod(
                proxyInterface,
                "optionalProperty"
            );
            ProxyMethodHandler proxyMethodHandler = proxyMethodHandler();

            Object emptyOptional = proxyMethodHandler.determineDefaultValueOrThrow(
                proxy,
                method,
                new String[0]
            );

            assertEquals(Optional.empty(), emptyOptional);
        }

        @Test
        @DisplayName(
            "should throw when method is not a default interface method " +
            "and does not have an Optional method return type."
        )
        public void test3() {
            Class<BasicProxyInterface> proxyInterface = 
                BasicProxyInterface.class;

            Object proxy = proxy(proxyInterface); 
            Method method = getProxyInterfaceMethod(
                proxyInterface,
                "property"
            );
            ProxyMethodHandler proxyMethodHandler = proxyMethodHandler();

            assertThrows(
                UnresolvedPropertiesException.class, 
                () -> proxyMethodHandler.determineDefaultValueOrThrow(
                    proxy,
                    method,
                    new String[0]
                )
            );
        }
    }

    @Nested
    class ResolvePropertyMethod {
        @Test
        @DisplayName("should return value from resolver.")
        public void test1() {
            Class<SystemPropertiesProxyInterface> proxyInterface = 
                SystemPropertiesProxyInterface.class;

            Object proxy = proxy(proxyInterface); 
            Method method = getProxyInterfaceMethod(
                proxyInterface,
                "javaVersion"
            );
            ProxyMethodHandler proxyMethod = proxyMethodHandler();

            Object resolvedValue = proxyMethod.handle(
                proxy,
                method,    
                new String[0]
            );

            // Since we're using SystemPropertyResolver,
            // resolvedValue must match the java.version system property value.
            assertEquals(System.getProperty("java.version"), resolvedValue);
        }

        @Test
        @DisplayName(
            "should return default value " + 
            "when method is not annotated with @ExternalizedProperty."
        )
        public void test2() {
            Class<NoAnnotationProxyInterface> proxyInterface = 
                NoAnnotationProxyInterface.class;

            Object proxy = proxy(proxyInterface); 
            Method method = getProxyInterfaceMethod(
                proxyInterface,
                "propertyWithNoAnnotationButWithDefaultValue"
            );
            ProxyMethodHandler proxyMethodHandler = proxyMethodHandler();

            Object resolvedValue = proxyMethodHandler.handle(
                proxy,
                method,
                new String[0]
            );
            Object defaultValue = proxyMethodHandler.determineDefaultValueOrThrow(
                proxy,
                method,
                new String[0]
            );

            // Resolved value must match default value.
            assertEquals(defaultValue, resolvedValue);
        }

        @Test
        @DisplayName(
            "should return default value " + 
            "when method is annotated with @ExternalizedProperty " + 
            "but property cannot be resolved via the resolver."
        )
        public void test3() {
            Class<DefaultValueProxyInterface> proxyInterface = 
                DefaultValueProxyInterface.class;

            Object proxy = proxy(proxyInterface); 
            Method method = getProxyInterfaceMethod(
                proxyInterface,
                "propertyWithDefaultValue"
            );
            ProxyMethodHandler proxyMethodHandler = proxyMethodHandler();

            // property is not in system properties so this should return default value.
            Object resolvedValue = proxyMethodHandler.handle(
                proxy,
                method,
                new String[0]
            );
            Object defaultValue = proxyMethodHandler.determineDefaultValueOrThrow(
                proxy,
                method,
                new String[0]
            );

            // Resolved value must match default value.
            assertEquals(defaultValue, resolvedValue);
        }
    }

    private ProxyMethodHandler proxyMethodHandler() {
        return new ProxyMethodHandler(
            rootResolver(),
            rootConverter(),
            methodHandleFactory
        );
    }

    private Object proxy(Class<?> proxyInterface) {
        return externalizedProperties.proxy(proxyInterface);
    }

    private static Method getProxyInterfaceMethod(
            Class<?> proxyInterface, 
            String name, 
            Class<?>... parameterTypes
    ) {
        try {
            return ProxyMethodUtils.getMethod(proxyInterface, name, parameterTypes);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to find property method.", e);
        }
    }

    private Resolver rootResolver() {
        return new RootResolver(
            externalizedProperties,
            Arrays.asList(resolverProvider), 
            ep -> new RootProcessor(ep), 
            variableExpanderProvider
        );
    }

    private Converter<?> rootConverter() {
        return new RootConverter(externalizedProperties, converterProvider);
    }
}
