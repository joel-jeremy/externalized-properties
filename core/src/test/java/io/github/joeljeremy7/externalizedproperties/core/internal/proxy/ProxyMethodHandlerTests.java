package io.github.joeljeremy7.externalizedproperties.core.internal.proxy;

import io.github.joeljeremy7.externalizedproperties.core.Converter;
import io.github.joeljeremy7.externalizedproperties.core.ConverterProvider;
import io.github.joeljeremy7.externalizedproperties.core.ExternalizedProperties;
import io.github.joeljeremy7.externalizedproperties.core.ExternalizedPropertiesException;
import io.github.joeljeremy7.externalizedproperties.core.ExternalizedProperty;
import io.github.joeljeremy7.externalizedproperties.core.Resolver;
import io.github.joeljeremy7.externalizedproperties.core.ResolverProvider;
import io.github.joeljeremy7.externalizedproperties.core.UnresolvedPropertiesException;
import io.github.joeljeremy7.externalizedproperties.core.VariableExpanderProvider;
import io.github.joeljeremy7.externalizedproperties.core.conversion.converters.DefaultConverter;
import io.github.joeljeremy7.externalizedproperties.core.internal.MethodHandleFactory;
import io.github.joeljeremy7.externalizedproperties.core.internal.conversion.RootConverter;
import io.github.joeljeremy7.externalizedproperties.core.internal.processing.RootProcessor;
import io.github.joeljeremy7.externalizedproperties.core.internal.resolvers.RootResolver;
import io.github.joeljeremy7.externalizedproperties.core.resolvers.DefaultResolver;
import io.github.joeljeremy7.externalizedproperties.core.testfixtures.ProxyMethodUtils;
import io.github.joeljeremy7.externalizedproperties.core.variableexpansion.SimpleVariableExpander;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class ProxyMethodHandlerTests {
    private final ResolverProvider<?> resolverProvider = DefaultResolver.provider();
    private final ConverterProvider<?> converterProvider = 
        DefaultConverter.provider();
    private final VariableExpanderProvider<?> variableExpanderProvider = 
        SimpleVariableExpander.provider();
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
        void test1() {
            Converter<?> rootConverter = rootConverter();

            assertThrows(IllegalArgumentException.class, () ->
                new ProxyMethodHandler(
                    null,
                    rootConverter,
                    methodHandleFactory
                )
            );
        }

        @Test
        @DisplayName("should throw when converter argument is null.")
        void test2() {
            Resolver rootResolver = rootResolver();

            assertThrows(IllegalArgumentException.class, () ->
                new ProxyMethodHandler(
                    rootResolver,
                    null,
                    methodHandleFactory
                )
            );
        }

        @Test
        @DisplayName("should throw when method handle builder argument is null.")
        void test3() {
            Resolver rootResolver = rootResolver();
            Converter<?> rootConverter = rootConverter();
            
            assertThrows(IllegalArgumentException.class, () ->
                new ProxyMethodHandler(
                    rootResolver,
                    rootConverter,
                    null
                )
            );
        }
    }

    @Nested
    class InvokeDefaultInterfaceMethodMethod {
        @Test
        @DisplayName("should invoke default interface method.")
        void test1() {
            Class<ProxyInterface> proxyInterface = ProxyInterface.class;

            Object proxy = proxy(proxyInterface);    
            Method method = ProxyMethodUtils.getMethod(
                proxyInterface, 
                ProxyInterface::propertyWithDefaultValue
            );
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
        void test2() {
            Class<ProxyInterface> proxyInterface = ProxyInterface.class;

            Object proxy = proxy(proxyInterface); 
            Method method = ProxyMethodUtils.getMethod(
                proxyInterface, 
                ProxyInterface::notFound
            );

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
        void test3() {
            Class<ProxyInterface> proxyInterface = ProxyInterface.class;

            Object proxy = proxy(proxyInterface); 
            Method method = ProxyMethodUtils.getMethod(
                proxyInterface, 
                ProxyInterface::throwRuntimeException
            );
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
        void test4() {
            Class<ProxyInterface> proxyInterface = ProxyInterface.class;

            Object proxy = proxy(proxyInterface); 
            // Can't use method ref here because of checked exception
            Method method = ProxyMethodUtils.getMethod(
                proxyInterface, 
                "throwException"
            );
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
        void test5() {
            Class<ProxyInterface> proxyInterface = ProxyInterface.class;

            Object proxy = proxy(proxyInterface); 
            Method method = ProxyMethodUtils.getMethod(
                proxyInterface,
                ProxyInterface::propertyWithDefaultValueParameter
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
        void test1() {
            Class<ProxyInterface> proxyInterface = ProxyInterface.class;

            Object proxy = proxy(proxyInterface); 
            Method method = ProxyMethodUtils.getMethod(
                proxyInterface,
                ProxyInterface::propertyWithDefaultValue
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
        void test2() {
            Class<ProxyInterface> proxyInterface = ProxyInterface.class;

            Object proxy = proxy(proxyInterface); 
            Method method = ProxyMethodUtils.getMethod(
                proxyInterface,
                ProxyInterface::optionalProperty
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
        void test3() {
            Class<ProxyInterface> proxyInterface = ProxyInterface.class;

            Object proxy = proxy(proxyInterface); 
            Method method = ProxyMethodUtils.getMethod(
                proxyInterface,
                ProxyInterface::notFound
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
        void test1() {
            Class<ProxyInterface> proxyInterface = ProxyInterface.class;

            Object proxy = proxy(proxyInterface); 
            Method method = ProxyMethodUtils.getMethod(
                proxyInterface,
                ProxyInterface::javaVersion
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
        void test2() {
            Class<ProxyInterface> proxyInterface = ProxyInterface.class;

            Object proxy = proxy(proxyInterface); 
            Method method = ProxyMethodUtils.getMethod(
                proxyInterface,
                ProxyInterface::propertyWithNoAnnotationButWithDefaultValue
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
        void test3() {
            Class<ProxyInterface> proxyInterface = ProxyInterface.class;

            Object proxy = proxy(proxyInterface); 
            Method method = ProxyMethodUtils.getMethod(
                proxyInterface,
                ProxyInterface::propertyWithDefaultValue
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

    public static interface ProxyInterface {
        @ExternalizedProperty("java.version")
        String javaVersion();

        @ExternalizedProperty("optional.property")
        Optional<String> optionalProperty();

        @ExternalizedProperty("property.with.default.value")
        default String propertyWithDefaultValue() {
            return "default.value";
        }

        @ExternalizedProperty("property.with.default.value")
        default String propertyWithDefaultValueParameter(String defaultValue) {
            return defaultValue;
        }

        default String propertyWithNoAnnotationButWithDefaultValue() {
            return "default.value";
        }

        @ExternalizedProperty("not.found")
        String notFound();

        @ExternalizedProperty("this.will.throw")
        default String throwRuntimeException() {
            throw new RuntimeException("Oops!");
        }

        @ExternalizedProperty("this.will.throw")
        default String throwException() throws Exception {
            throw new Exception("Oops!");
        }
    }
}
