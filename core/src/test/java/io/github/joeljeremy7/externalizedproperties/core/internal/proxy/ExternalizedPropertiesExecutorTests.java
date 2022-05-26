package io.github.joeljeremy7.externalizedproperties.core.internal.proxy;

import io.github.joeljeremy7.externalizedproperties.core.Convert;
import io.github.joeljeremy7.externalizedproperties.core.Converter;
import io.github.joeljeremy7.externalizedproperties.core.ExternalizedProperties;
import io.github.joeljeremy7.externalizedproperties.core.ExternalizedPropertiesException;
import io.github.joeljeremy7.externalizedproperties.core.ExternalizedProperty;
import io.github.joeljeremy7.externalizedproperties.core.Resolver;
import io.github.joeljeremy7.externalizedproperties.core.TypeReference;
import io.github.joeljeremy7.externalizedproperties.core.UnresolvedPropertiesException;
import io.github.joeljeremy7.externalizedproperties.core.VariableExpander;
import io.github.joeljeremy7.externalizedproperties.core.conversion.converters.DefaultConverter;
import io.github.joeljeremy7.externalizedproperties.core.internal.MethodHandleFactory;
import io.github.joeljeremy7.externalizedproperties.core.internal.conversion.RootConverter;
import io.github.joeljeremy7.externalizedproperties.core.internal.processing.RootProcessor;
import io.github.joeljeremy7.externalizedproperties.core.internal.resolvers.RootResolver;
import io.github.joeljeremy7.externalizedproperties.core.resolvers.DefaultResolver;
import io.github.joeljeremy7.externalizedproperties.core.testfixtures.ProxyMethodReference;
import io.github.joeljeremy7.externalizedproperties.core.testfixtures.ProxyMethodUtils;
import io.github.joeljeremy7.externalizedproperties.core.variableexpansion.SimpleVariableExpander;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class ExternalizedPropertiesExecutorTests {
    private static final Resolver RESOLVER = new DefaultResolver();
    private static final Converter<?> CONVERTER = new DefaultConverter();
    private static final VariableExpander VARIABLE_EXPANDER = 
        new SimpleVariableExpander();
    
    private static final ExternalizedProperties EXTERNALIZED_PROPERTIES = 
        ExternalizedProperties.builder()
            .resolvers(RESOLVER)
            .converters(CONVERTER)
            .variableExpander(VARIABLE_EXPANDER)
            .build();
    
    private static final Resolver ROOT_RESOLVER = new RootResolver(
        Arrays.asList(RESOLVER),
        new RootProcessor(),
        VARIABLE_EXPANDER
    );
    private static final Converter<?> ROOT_CONVERTER = new RootConverter(CONVERTER);

    private static final ProxyMethodFactory PROXY_METHOD_FACTORY = 
        new ProxyMethodFactory(EXTERNALIZED_PROPERTIES);
    private static final MethodHandleFactory METHOD_HANDLE_FACTORY = new MethodHandleFactory();
    
    @Nested
    class Constructor {
        @Test
        @DisplayName("should throw when root resolver argument is null.")
        void test1() {
            assertThrows(IllegalArgumentException.class, () ->
                new ExternalizedPropertiesExecutor(
                    null,
                    ROOT_CONVERTER,
                    PROXY_METHOD_FACTORY,
                    METHOD_HANDLE_FACTORY
                )
            );
        }

        @Test
        @DisplayName("should throw when root converter argument is null.")
        void test2() {
            assertThrows(IllegalArgumentException.class, () ->
                new ExternalizedPropertiesExecutor(
                    ROOT_RESOLVER,
                    null,
                    PROXY_METHOD_FACTORY,
                    METHOD_HANDLE_FACTORY
                )
            );
        }

        @Test
        @DisplayName("should throw when proxy method factory argument is null.")
        void test3() {
            assertThrows(IllegalArgumentException.class, () ->
                new ExternalizedPropertiesExecutor(
                    ROOT_RESOLVER,
                    ROOT_CONVERTER,
                    null,
                    METHOD_HANDLE_FACTORY
                )
            );
        }

        @Test
        @DisplayName("should throw when method handle factory argument is null.")
        void test4() {
            assertThrows(IllegalArgumentException.class, () ->
                new ExternalizedPropertiesExecutor(
                    ROOT_RESOLVER,
                    ROOT_CONVERTER,
                    PROXY_METHOD_FACTORY,
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
            ExternalizedPropertiesExecutor proxyMethodExecutor = proxyMethodExecutor();

            Object value = proxyMethodExecutor.invokeDefaultInterfaceMethod(
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

            ExternalizedPropertiesExecutor proxyMethodExecutor = proxyMethodExecutor();

            assertThrows(
                IllegalStateException.class, 
                () -> proxyMethodExecutor.invokeDefaultInterfaceMethod(proxy, method, new String[0])
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
            ExternalizedPropertiesExecutor proxyMethodExecutor = proxyMethodExecutor();

            assertThrows(
                RuntimeException.class, 
                () -> proxyMethodExecutor.invokeDefaultInterfaceMethod(
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
            ExternalizedPropertiesExecutor proxyMethod = proxyMethodExecutor();

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
            ExternalizedPropertiesExecutor proxyMethodExecutor = proxyMethodExecutor();

            Object value = proxyMethodExecutor.invokeDefaultInterfaceMethod(
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
            ExternalizedPropertiesExecutor proxyMethodExecutor = proxyMethodExecutor();

            Object value = proxyMethodExecutor.determineDefaultValueOrThrow(
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
            ExternalizedPropertiesExecutor proxyMethodExecutor = proxyMethodExecutor();

            Object emptyOptional = proxyMethodExecutor.determineDefaultValueOrThrow(
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
            ExternalizedPropertiesExecutor proxyMethodExecutor = proxyMethodExecutor();

            assertThrows(
                UnresolvedPropertiesException.class, 
                () -> proxyMethodExecutor.determineDefaultValueOrThrow(
                    proxy,
                    method,
                    new String[0]
                )
            );
        }
    }

    @Nested
    class HandleMethod {
        @Test
        @DisplayName("should return value from resolver.")
        void test1() {
            Class<ProxyInterface> proxyInterface = ProxyInterface.class;

            Object proxy = proxy(proxyInterface); 
            Method method = ProxyMethodUtils.getMethod(
                proxyInterface,
                ProxyInterface::javaVersion
            );
            ExternalizedPropertiesExecutor proxyMethod = proxyMethodExecutor();

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
            ExternalizedPropertiesExecutor proxyMethodExecutor = proxyMethodExecutor();

            Object resolvedValue = proxyMethodExecutor.handle(
                proxy,
                method,
                new String[0]
            );
            Object defaultValue = proxyMethodExecutor.determineDefaultValueOrThrow(
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
            ExternalizedPropertiesExecutor proxyMethodExecutor = proxyMethodExecutor();

            // property is not in system properties so this should return default value.
            Object resolvedValue = proxyMethodExecutor.handle(
                proxy,
                method,
                new String[0]
            );
            Object defaultValue = proxyMethodExecutor.determineDefaultValueOrThrow(
                proxy,
                method,
                new String[0]
            );

            // Resolved value must match default value.
            assertEquals(defaultValue, resolvedValue);
        }

        @Test
        @DisplayName("should convert value to target type reference")
        void convertTest1() {
            Class<ConvertProxyInterface> proxyInterface = ConvertProxyInterface.class;

            Object proxy = proxy(proxyInterface); 
            Method method = ProxyMethodUtils.getMethod(
                proxyInterface,
                (ProxyMethodReference.WithTwoArgs<ConvertProxyInterface, String, TypeReference<?>, ?>)
                ConvertProxyInterface::convertToTargetTypeReference
            );
            ExternalizedPropertiesExecutor proxyMethodExecutor = proxyMethodExecutor();

            Object convertedValue = proxyMethodExecutor.handle(
                proxy,
                method,
                new Object[] {
                    "1",
                    new TypeReference<Integer>(){}
                }
            );

            assertEquals(1, (Integer)convertedValue);
        }

        @Test
        @DisplayName(
            "should convert value to target type reference " +
            "(Proxy method return type is Object)"
        )
        void convertTest2() {
            Class<ConvertProxyInterface> proxyInterface = ConvertProxyInterface.class;

            Object proxy = proxy(proxyInterface); 
            Method method = ProxyMethodUtils.getMethod(
                proxyInterface,
                (ProxyMethodReference.WithTwoArgs<ConvertProxyInterface, String, TypeReference<?>, ?>)
                ConvertProxyInterface::convertToTargetTypeReferenceObject
            );
            ExternalizedPropertiesExecutor proxyMethodExecutor = proxyMethodExecutor();

            Object convertedValue = proxyMethodExecutor.handle(
                proxy,
                method,
                new Object[] {
                    "1",
                    new TypeReference<Integer>(){}
                }
            );

            assertEquals(1, (Integer)convertedValue);
        }

        @Test
        @DisplayName("should convert value to target class")
        void convertTest3() {
            Class<ConvertProxyInterface> proxyInterface = ConvertProxyInterface.class;

            Object proxy = proxy(proxyInterface); 
            Method method = ProxyMethodUtils.getMethod(
                proxyInterface,
                (ProxyMethodReference.WithTwoArgs<ConvertProxyInterface, String, Class<?>, ?>)
                ConvertProxyInterface::convertToTargetClass
            );
            ExternalizedPropertiesExecutor proxyMethodExecutor = proxyMethodExecutor();

            Object convertedValue = proxyMethodExecutor.handle(
                proxy,
                method,
                new Object[] {
                    "1",
                    Integer.class
                }
            );

            assertEquals(1, (Integer)convertedValue);
        }

        @Test
        @DisplayName(
            "should convert value to target class " +
            "(Proxy method return type is Object)"
        )
        void convertTest4() {
            Class<ConvertProxyInterface> proxyInterface = ConvertProxyInterface.class;

            Object proxy = proxy(proxyInterface); 
            Method method = ProxyMethodUtils.getMethod(
                proxyInterface,
                (ProxyMethodReference.WithTwoArgs<ConvertProxyInterface, String, Class<?>, ?>)
                ConvertProxyInterface::convertToTargetClassObject
            );
            ExternalizedPropertiesExecutor proxyMethodExecutor = proxyMethodExecutor();

            Object convertedValue = proxyMethodExecutor.handle(
                proxy,
                method,
                new Object[] {
                    "1",
                    Integer.class
                }
            );

            assertEquals(1, (Integer)convertedValue);
        }

        @Test
        @DisplayName("should convert value to target type")
        void convertTest5() {
            Class<ConvertProxyInterface> proxyInterface = ConvertProxyInterface.class;

            Object proxy = proxy(proxyInterface); 
            Method method = ProxyMethodUtils.getMethod(
                proxyInterface,
                (ProxyMethodReference.WithTwoArgs<ConvertProxyInterface, String, Type, ?>)
                ConvertProxyInterface::convertToTargetType
            );
            ExternalizedPropertiesExecutor proxyMethodExecutor = proxyMethodExecutor();

            Object convertedValue = proxyMethodExecutor.handle(
                proxy,
                method,
                new Object[] {
                    "1",
                    new TypeReference<Integer>(){}.type()
                }
            );

            assertEquals(1, (Integer)convertedValue);
        }

        @Test
        @DisplayName(
            "should convert value to target type " +
            "(Proxy method return type is Object)"
        )
        void convertTest6() {
            Class<ConvertProxyInterface> proxyInterface = ConvertProxyInterface.class;

            Object proxy = proxy(proxyInterface); 
            Method method = ProxyMethodUtils.getMethod(
                proxyInterface,
                (ProxyMethodReference.WithTwoArgs<ConvertProxyInterface, String, Type, ?>)
                ConvertProxyInterface::convertToTargetTypeObject
            );
            ExternalizedPropertiesExecutor proxyMethodExecutor = proxyMethodExecutor();

            Object convertedValue = proxyMethodExecutor.handle(
                proxy,
                method,
                new Object[] {
                    "1",
                    new TypeReference<Integer>(){}.type()
                }
            );

            assertEquals(1, (Integer)convertedValue);
        }

        @Test
        @DisplayName(
            "should throw when @Convert proxy method invocation's second argument is " +
            "not a valid target type."
        )
        void convertTest7() {
            Class<ConvertProxyInterface> proxyInterface = ConvertProxyInterface.class;

            Object proxy = proxy(proxyInterface); 
            Method method = ProxyMethodUtils.getMethod(
                proxyInterface,
                (ProxyMethodReference.WithTwoArgs<ConvertProxyInterface, String, TypeReference<?>, ?>)
                ConvertProxyInterface::convertToTargetTypeReference
            );
            ExternalizedPropertiesExecutor proxyMethodExecutor = proxyMethodExecutor();

            assertThrows(
                IllegalArgumentException.class, 
                () -> proxyMethodExecutor.handle(
                    proxy,
                    method,
                    new Object[] {
                        "1",
                        // Must only be either TypeReference, Class, or Type.
                        Arrays.asList("invalid")
                    }
                )
            );
        }
    }

    private static ExternalizedPropertiesExecutor proxyMethodExecutor() {
        return new ExternalizedPropertiesExecutor(
            ROOT_RESOLVER,
            ROOT_CONVERTER,
            PROXY_METHOD_FACTORY,
            METHOD_HANDLE_FACTORY
        );
    }

    private static Object proxy(Class<?> proxyInterface) {
        return EXTERNALIZED_PROPERTIES.initialize(proxyInterface);
    }

    private static interface ProxyInterface {
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

    private static interface ConvertProxyInterface {
        @Convert
        <T> T convertToTargetTypeReference(
            String valueToConvert, 
            TypeReference<T> targetType
        );
        @Convert
        <T> T convertToTargetClass(String valueToConvert, Class<T> targetType);
        @Convert
        <T> T convertToTargetType(String valueToConvert, Type targetType);

        @Convert
        Object convertToTargetTypeReferenceObject(
            String valueToConvert, 
            TypeReference<?> targetType
        );
        @Convert
        Object convertToTargetClassObject(String valueToConvert, Class<?> targetType);
        @Convert
        Object convertToTargetTypeObject(String valueToConvert, Type targetType);
    }
}
