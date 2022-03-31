package io.github.jeyjeyemem.externalizedproperties.core.internal.proxy;

import io.github.jeyjeyemem.externalizedproperties.core.ExternalizedProperties;
import io.github.jeyjeyemem.externalizedproperties.core.ExternalizedPropertiesException;
import io.github.jeyjeyemem.externalizedproperties.core.UnresolvedPropertiesException;
import io.github.jeyjeyemem.externalizedproperties.core.internal.MethodHandleFactory;
import io.github.jeyjeyemem.externalizedproperties.core.testentities.proxy.BasicProxyInterface;
import io.github.jeyjeyemem.externalizedproperties.core.testentities.proxy.DefaultValueProxyInterface;
import io.github.jeyjeyemem.externalizedproperties.core.testentities.proxy.JavaPropertiesProxyInterface;
import io.github.jeyjeyemem.externalizedproperties.core.testentities.proxy.NoAnnotationProxyInterface;
import io.github.jeyjeyemem.externalizedproperties.core.testentities.proxy.OptionalProxyInterface;
import io.github.jeyjeyemem.externalizedproperties.core.testentities.proxy.ThrowingProxyInterface;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class ProxyMethodInvokerTests {
    private final ExternalizedProperties externalizedProperties = 
        ExternalizedProperties.builder()
            .withDefaults()
            .build();
    
    private final MethodHandleFactory methodHandleFactory = new MethodHandleFactory();
    
    @Nested
    class Constructor {
        @Test
        @DisplayName("should throw when proxy argument is null.")
        public void test1() {
            assertThrows(IllegalArgumentException.class, () ->
                new ProxyMethodInvoker(
                    null, 
                    getProxyInterfaceMethod(
                        DefaultValueProxyInterface.class, 
                        "propertyWithDefaultValue"
                    ),
                    externalizedProperties,
                    methodHandleFactory
                )
            );
        }

        @Test
        @DisplayName("should throw when method argument is null.")
        public void test2() {
            assertThrows(IllegalArgumentException.class, () ->
                new ProxyMethodInvoker(
                    proxy(BasicProxyInterface.class),
                    null,
                    externalizedProperties,
                    methodHandleFactory
                )
            );
        }

        @Test
        @DisplayName("should throw when externalized properties argument is null.")
        public void test3() {
            assertThrows(IllegalArgumentException.class, () ->
                new ProxyMethodInvoker(
                    proxy(BasicProxyInterface.class),
                    getProxyInterfaceMethod(
                        BasicProxyInterface.class, 
                        "property"
                    ),
                    null,
                    methodHandleFactory
                )
            );
        }

        @Test
        @DisplayName("should throw when method handle builder argument is null.")
        public void test4() {
            assertThrows(IllegalArgumentException.class, () ->
                new ProxyMethodInvoker(
                    proxy(BasicProxyInterface.class),
                    getProxyInterfaceMethod(
                        BasicProxyInterface.class, 
                        "property"
                    ),
                    externalizedProperties,
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
            ProxyMethodInvoker proxyMethod = proxyMethod(
                DefaultValueProxyInterface.class, 
                "propertyWithDefaultValue"
            );

            Object value = proxyMethod.invokeDefaultInterfaceMethod(new String[0]);

            // See DefaultValueProxyInterface.propertyWithDefaultValue default return value.
            assertEquals("default.value", value);
        }

        @Test
        @DisplayName("should throw when method is not a default interface method.")
        public void test2() {
            ProxyMethodInvoker proxyMethod = proxyMethod(
                BasicProxyInterface.class, 
                "property"
            );

            assertThrows(
                IllegalStateException.class, 
                () -> proxyMethod.invokeDefaultInterfaceMethod(new String[0])
            );
        }

        @Test
        @DisplayName(
            "should rethrow same runtime exception when default interface method throws an exception."
        )
        public void test3() {
            ProxyMethodInvoker proxyMethod = proxyMethod(
                ThrowingProxyInterface.class, 
                "throwRuntimeException"
            );

            assertThrows(
                RuntimeException.class, 
                () -> proxyMethod.invokeDefaultInterfaceMethod(new String[0])
            );
        }

        @Test
        @DisplayName(
            "should wrap non-runtime exception thrown by default interface method."
        )
        public void test4() {
            ProxyMethodInvoker proxyMethod = proxyMethod(
                ThrowingProxyInterface.class, 
                "throwException"
            );

            assertThrows(
                ExternalizedPropertiesException.class, 
                () -> proxyMethod.invokeDefaultInterfaceMethod(new String[0])
            );
        }

        @Test
        @DisplayName("should receive method arguments.")
        public void test5() {
            ProxyMethodInvoker proxyMethod = proxyMethod(
                DefaultValueProxyInterface.class, 
                "propertyWithDefaultValueParameter",
                String.class // Method has one string parameter.
            );

            Object value = proxyMethod.invokeDefaultInterfaceMethod(
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
            ProxyMethodInvoker proxyMethod = proxyMethod(
                DefaultValueProxyInterface.class, 
                "propertyWithDefaultValue"
            );

            Object value = proxyMethod.determineDefaultValueOrThrow(new String[0]);

            // See DefaultValueProxyInterface.propertyWithDefaultValue default return value.
            assertEquals("default.value", value);
        }

        @Test
        @DisplayName(
            "should return an empty Optional when method has an Optional method return type."
        )
        public void test2() {
            ProxyMethodInvoker proxyMethod = proxyMethod(
                OptionalProxyInterface.class, 
                "optionalProperty"
            );

            Object emptyOptional = 
                proxyMethod.determineDefaultValueOrThrow(new String[0]);

            assertEquals(Optional.empty(), emptyOptional);
        }

        @Test
        @DisplayName(
            "should throw when method is not a default interface method " +
            "and does not have an Optional method return type."
        )
        public void test3() {
            ProxyMethodInvoker proxyMethod = proxyMethod(
                BasicProxyInterface.class, 
                "property"
            );

            assertThrows(
                UnresolvedPropertiesException.class, 
                () -> proxyMethod.determineDefaultValueOrThrow(new String[0])
            );
        }
    }

    @Nested
    class ResolvePropertyMethod {
        @Test
        @DisplayName("should return value from resolver.")
        public void test1() {
            ProxyMethodInvoker proxyMethod = proxyMethod(
                JavaPropertiesProxyInterface.class, 
                "javaVersion"
            );

            Object resolvedValue = proxyMethod.resolveProperty(new String[0]);

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
            ProxyMethodInvoker proxyMethod = proxyMethod(
                NoAnnotationProxyInterface.class, 
                "propertyWithNoAnnotationButWithDefaultValue"
            );

            Object resolvedValue = proxyMethod.resolveProperty(new String[0]);
            Object defaultValue = proxyMethod.determineDefaultValueOrThrow(new String[0]);

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
            ProxyMethodInvoker proxyMethod = proxyMethod(
                DefaultValueProxyInterface.class, 
                "propertyWithDefaultValue"
            );

            // property is not in system properties so this should return default value.
            Object resolvedValue = proxyMethod.resolveProperty(new String[0]);
            Object defaultValue = proxyMethod.determineDefaultValueOrThrow(new String[0]);

            // Resolved value must match default value.
            assertEquals(defaultValue, resolvedValue);
        }
    }

    private ProxyMethodInvoker proxyMethod(
            Class<?> proxyInterface, 
            String methodName,
            Class<?>... methodParameters
    ) {
        Method method = getProxyInterfaceMethod(proxyInterface, methodName, methodParameters);
        Object proxy = proxy(proxyInterface);    
        return new ProxyMethodInvoker(
            proxy,
            method,
            externalizedProperties,
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
            return proxyInterface.getDeclaredMethod(name, parameterTypes);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to find property method.", e);
        }
    }
}
