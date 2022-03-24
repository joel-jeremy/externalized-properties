package io.github.jeyjeyemem.externalizedproperties.core.internal.proxy;

import io.github.jeyjeyemem.externalizedproperties.core.Converter;
import io.github.jeyjeyemem.externalizedproperties.core.ExternalizedProperties;
import io.github.jeyjeyemem.externalizedproperties.core.Resolver;
import io.github.jeyjeyemem.externalizedproperties.core.exceptions.UnresolvedPropertiesException;
import io.github.jeyjeyemem.externalizedproperties.core.exceptions.VariableExpansionException;
import io.github.jeyjeyemem.externalizedproperties.core.resolvers.CompositeResolver;
import io.github.jeyjeyemem.externalizedproperties.core.resolvers.MapResolver;
import io.github.jeyjeyemem.externalizedproperties.core.testentities.ProxyMethodUtils;
import io.github.jeyjeyemem.externalizedproperties.core.testentities.proxy.BasicProxyInterface;
import io.github.jeyjeyemem.externalizedproperties.core.testentities.proxy.DefaultValueProxyInterface;
import io.github.jeyjeyemem.externalizedproperties.core.testentities.proxy.NoAnnotationProxyInterface;
import io.github.jeyjeyemem.externalizedproperties.core.testentities.proxy.OptionalProxyInterface;
import io.github.jeyjeyemem.externalizedproperties.core.testentities.proxy.PrimitiveProxyInterface;
import io.github.jeyjeyemem.externalizedproperties.core.testentities.proxy.VariableProxyInterface;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

// Let ExternalizedProperties.proxy(Class<?> proxyInterface) 
// create the proxy for these test cases.
public class ExternalizedPropertyInvocationHandlerTests {
    @Nested
    class InvokeMethod {
        @Test
        @DisplayName("should resolve property")
        public void test1() throws Throwable {
            Map<String, String> map = new HashMap<>();
            map.put("property", "test.value.1");

            ExternalizedProperties externalizedProperties = externalizedProperties(map);

            Method proxyMethod = ProxyMethodUtils.getMethod(
                BasicProxyInterface.class,
                "property"
            );

            ExternalizedPropertyInvocationHandler handler = 
                new ExternalizedPropertyInvocationHandler(externalizedProperties);

            Object result = handler.invoke(
                externalizedProperties.proxy(BasicProxyInterface.class), 
                proxyMethod, 
                new Object[0]
            );

            assertEquals("test.value.1", result);
        }
        
        @Test
        @DisplayName(
            "should resolve property from map and not from default interface method value"
        )
        public void test2() throws Throwable {
            Map<String, String> map = new HashMap<>();
            map.put("property.with.default.value", "test.value");

            ExternalizedProperties externalizedProperties = externalizedProperties(map);

            Class<DefaultValueProxyInterface> proxyInterface = 
                DefaultValueProxyInterface.class;
            
            Method proxyMethod = ProxyMethodUtils.getMethod(
                proxyInterface,
                "propertyWithDefaultValue"
            );

            ExternalizedPropertyInvocationHandler handler = 
                new ExternalizedPropertyInvocationHandler(externalizedProperties);

            Object result = handler.invoke(
                externalizedProperties.proxy(proxyInterface), 
                proxyMethod, 
                new Object[0]
            );

            assertEquals("test.value", result);
        }

        @Test
        @DisplayName(
            "should resolve property from map and not from default interface method value parameter"
        )
        public void test3() throws Throwable {
            Map<String, String> map = new HashMap<>();
            map.put("property.with.default.value", "test.value");

            ExternalizedProperties externalizedProperties = externalizedProperties(map);

            Class<DefaultValueProxyInterface> proxyInterface = 
                DefaultValueProxyInterface.class;

            Method proxyMethod = ProxyMethodUtils.getMethod(
                proxyInterface,
                "propertyWithDefaultValueParameter",
                String.class
            );

            ExternalizedPropertyInvocationHandler handler = 
                new ExternalizedPropertyInvocationHandler(externalizedProperties);

            String providedDefaultValue = "provided.default.value";
            Object result = handler.invoke(
                externalizedProperties.proxy(proxyInterface),
                proxyMethod, 
                new Object[] { providedDefaultValue }
            );

            assertEquals("test.value", result);
        }

        @Test
        @DisplayName("should resolve default value from default interface method")
        public void test4() throws Throwable {
            ExternalizedProperties externalizedProperties = 
                externalizedProperties(Collections.emptyMap());

            Class<DefaultValueProxyInterface> proxyInterface = 
                DefaultValueProxyInterface.class;

            Method proxyMethod = ProxyMethodUtils.getMethod(
                proxyInterface,
                "propertyWithDefaultValue"
            );

            ExternalizedPropertyInvocationHandler handler = 
                new ExternalizedPropertyInvocationHandler(externalizedProperties);

            Object result = handler.invoke(
                externalizedProperties.proxy(proxyInterface), 
                proxyMethod, 
                new Object[0]
            );

            assertEquals("default.value", result);
        }

        @Test
        @DisplayName("should resolve default value from default interface method parameter")
        public void test5() throws Throwable {
            ExternalizedProperties externalizedProperties = 
                externalizedProperties(Collections.emptyMap());

            Class<DefaultValueProxyInterface> proxyInterface = 
                DefaultValueProxyInterface.class;

            Method proxyMethod = ProxyMethodUtils.getMethod(
                proxyInterface,
                "propertyWithDefaultValueParameter",
                String.class
            );

            ExternalizedPropertyInvocationHandler handler = 
                new ExternalizedPropertyInvocationHandler(externalizedProperties);

            // Pass default value.
            String providedDefaultValue = "provided.default.value";
            Object result = handler.invoke(
                externalizedProperties.proxy(proxyInterface), 
                proxyMethod, 
                new Object[] { providedDefaultValue }
            );

            assertEquals(providedDefaultValue, result);
        }

        @Test
        @DisplayName(
            "should always return default value from default interface method when not annotated"
        )
        public void test6() throws Throwable {
            ExternalizedProperties externalizedProperties = 
                externalizedProperties(Collections.emptyMap());

            Class<NoAnnotationProxyInterface> proxyInterface = 
                NoAnnotationProxyInterface.class;

            Method proxyMethod = ProxyMethodUtils.getMethod(
                proxyInterface,
                "propertyWithNoAnnotationButWithDefaultValue"
            );

            ExternalizedPropertyInvocationHandler handler = 
                new ExternalizedPropertyInvocationHandler(externalizedProperties);

            Object result = handler.invoke(
                externalizedProperties.proxy(proxyInterface), 
                proxyMethod, 
                new Object[0]
            );

            assertEquals("default.value", result);
        }

        @Test
        @DisplayName(
            "should always return default value from default interface method parameter " + 
            "when not annotated"
        )
        public void test7() throws Throwable {
            ExternalizedProperties externalizedProperties = 
                externalizedProperties(Collections.emptyMap());

            Class<NoAnnotationProxyInterface> proxyInterface = 
                NoAnnotationProxyInterface.class;

            Method proxyMethod = ProxyMethodUtils.getMethod(
                proxyInterface,
                "propertyWithNoAnnotationButWithDefaultValueParameter",
                String.class
            );

            ExternalizedPropertyInvocationHandler handler = 
                new ExternalizedPropertyInvocationHandler(externalizedProperties);

            // Pass default value.
            String providedDefaultValue = "provided.default.value";
            Object result = handler.invoke(
                externalizedProperties.proxy(proxyInterface), 
                proxyMethod, 
                new Object[] { providedDefaultValue }
            );

            assertEquals(providedDefaultValue, result);
        }

        @Test
        @DisplayName("should throw when an annotated non-Optional property cannot be resolved.")
        public void test8() {
            ExternalizedProperties externalizedProperties = 
                externalizedProperties(Collections.emptyMap());

            Method proxyMethod = ProxyMethodUtils.getMethod(
                BasicProxyInterface.class,
                "property"
            );

            ExternalizedPropertyInvocationHandler handler = 
                new ExternalizedPropertyInvocationHandler(externalizedProperties);

            assertThrows(UnresolvedPropertiesException.class, () -> {
                handler.invoke(
                    externalizedProperties.proxy(BasicProxyInterface.class), 
                    proxyMethod, 
                    new Object[0]
                );
            });
        }

        @Test
        @DisplayName("should throw when an unannotated non-Optional property cannot be resolved.")
        public void test9() {
            ExternalizedProperties externalizedProperties = 
                externalizedProperties(Collections.emptyMap());

            Method proxyMethod = ProxyMethodUtils.getMethod(
                NoAnnotationProxyInterface.class,
                "propertyWithNoAnnotationAndNoDefaultValue"
            );

            ExternalizedPropertyInvocationHandler handler = 
                new ExternalizedPropertyInvocationHandler(externalizedProperties);

            assertThrows(UnresolvedPropertiesException.class, () -> {
                handler.invoke(
                    externalizedProperties.proxy(BasicProxyInterface.class), 
                    proxyMethod, 
                    new Object[0]
                );
            });
        }

        @Test
        @DisplayName("should convert a non-String property via Converter.")
        public void test10() throws Throwable {
            Map<String, String> map = new HashMap<>();
            map.put("property.integer.wrapper", "1");
            map.put("property.integer.primitive", "2");

            ExternalizedProperties externalizedProperties = externalizedProperties(map);

            PrimitiveProxyInterface proxy = 
                externalizedProperties.proxy(PrimitiveProxyInterface.class);

            Method intWrapperMethod = ProxyMethodUtils.getMethod(
                PrimitiveProxyInterface.class,
                "integerWrapperProperty"
            );

            Method intPrimitiveMethod = ProxyMethodUtils.getMethod(
                PrimitiveProxyInterface.class,
                "intPrimitiveProperty"
            );

            ExternalizedPropertyInvocationHandler handler = 
                new ExternalizedPropertyInvocationHandler(externalizedProperties);

            Object intWrapperResult = handler.invoke(
                proxy, 
                intWrapperMethod,
                new Object[0]
            );

            Object intPrimitiveResult = handler.invoke(
                proxy, 
                intPrimitiveMethod,
                new Object[0]
            );

            assertEquals(1, (Integer)intWrapperResult);
            // Support for primitive types.
            assertEquals(2, (int)intPrimitiveResult);
        }

        /**
         * Variable expansion tests.
         * @throws Throwable
         */

        @Test
        @DisplayName("should expand variable in property name.")
        public void testVariableExpansion1() throws Throwable {
            Map<String, String> map = new HashMap<>();
            String customVariableValue = "custom-variable";
            map.put("custom.variable", customVariableValue);
            map.put("property-" + customVariableValue, "property.value");

            ExternalizedProperties externalizedProperties = externalizedProperties(map);

            Method proxyMethod = ProxyMethodUtils.getMethod(
                VariableProxyInterface.class,
                "variableProperty"
            );

            ExternalizedPropertyInvocationHandler handler = 
                new ExternalizedPropertyInvocationHandler(externalizedProperties);

            Object result = handler.invoke(
                externalizedProperties.proxy(VariableProxyInterface.class), 
                proxyMethod, 
                new Object[0]
            );

            assertEquals("property.value", result);
        }

        @Test
        @DisplayName("should throw when variable value cannot be resolved.")
        public void testVariableExpansion2() {
            Map<String, String> map = new HashMap<>();
            map.put("property-custom-variable", "property.value");

            ExternalizedProperties externalizedProperties = externalizedProperties(map);

            Method proxyMethod = ProxyMethodUtils.getMethod(
                VariableProxyInterface.class,
                "variableProperty"
            );

            ExternalizedPropertyInvocationHandler handler = 
                new ExternalizedPropertyInvocationHandler(externalizedProperties);

            // There is no custom-variable-value property.
            // Property name of VariableProxyInterface.variableProperty() won't be able to be expanded.
            assertThrows(VariableExpansionException.class, 
                () -> handler.invoke(
                    externalizedProperties.proxy(VariableProxyInterface.class), 
                    proxyMethod, 
                    new Object[0]
                )
            );
        }

        /**
         * Optional property test cases.
         * @throws Throwable
         */

        @Test
        @DisplayName("should resolve property")
        public void testOptional1() throws Throwable {
            Map<String, String> map = new HashMap<>();
            map.put("property.optional", "test.value");

            ExternalizedProperties externalizedProperties = externalizedProperties(map);

            Method proxyMethod = ProxyMethodUtils.getMethod(
                OptionalProxyInterface.class,
                "optionalProperty"
            );

            ExternalizedPropertyInvocationHandler handler = 
                new ExternalizedPropertyInvocationHandler(externalizedProperties);

            Object result = handler.invoke(
                externalizedProperties.proxy(OptionalProxyInterface.class), 
                proxyMethod, 
                new Object[0]
            );

            assertTrue(result instanceof Optional<?>);

            Optional<?> optional = (Optional<?>)result;

            assertTrue(optional.isPresent());
            assertEquals("test.value", optional.get());
        }

        @Test
        @DisplayName("should resolve property from map and not from default interface method value")
        public void testOptional2() throws Throwable {
            Map<String, String> map = new HashMap<>();
            map.put("property.optional.with.default.value", "test.value");

            ExternalizedProperties externalizedProperties = externalizedProperties(map);
            
            Method proxyMethod = ProxyMethodUtils.getMethod(
                OptionalProxyInterface.class,
                "optionalPropertyWithDefaultValue"
            );

            ExternalizedPropertyInvocationHandler handler = 
                new ExternalizedPropertyInvocationHandler(externalizedProperties);

            Object result = handler.invoke(
                externalizedProperties.proxy(OptionalProxyInterface.class), 
                proxyMethod, 
                new Object[0]
            );

            assertTrue(result instanceof Optional<?>);

            Optional<?> optional = (Optional<?>)result;

            assertTrue(optional.isPresent());
            assertEquals("test.value", optional.get());
        }

        @Test
        @DisplayName(
            "should resolve property from map and not from default interface method value parameter"
        )
        public void testOptional3() throws Throwable {
            Map<String, String> map = new HashMap<>();
            map.put("property.optional.with.default.value", "test.value");

            ExternalizedProperties externalizedProperties = externalizedProperties(map);
            
            Method proxyMethod = ProxyMethodUtils.getMethod(
                OptionalProxyInterface.class,
                "optionalPropertyWithDefaultValue"
            );

            ExternalizedPropertyInvocationHandler handler = 
                new ExternalizedPropertyInvocationHandler(externalizedProperties);

            String providedDefaultValue = "provided.default.value";
            Object result = handler.invoke(
                externalizedProperties.proxy(OptionalProxyInterface.class), 
                proxyMethod, 
                new Object[] { providedDefaultValue }
            );

            assertTrue(result instanceof Optional<?>);

            Optional<?> optional = (Optional<?>)result;

            assertTrue(optional.isPresent());
            assertEquals("test.value", optional.get());
        }

        @Test
        @DisplayName("should resolve default value from default interface method")
        public void testOptional4() throws Throwable {
            ExternalizedProperties externalizedProperties = 
                externalizedProperties(Collections.emptyMap());

            Method proxyMethod = ProxyMethodUtils.getMethod(
                OptionalProxyInterface.class,
                "optionalPropertyWithDefaultValue"
            );

            ExternalizedPropertyInvocationHandler handler = 
                new ExternalizedPropertyInvocationHandler(externalizedProperties);

            Object result = handler.invoke(
                externalizedProperties.proxy(OptionalProxyInterface.class), 
                proxyMethod, 
                new Object[0]
            );

            assertTrue(result instanceof Optional<?>);

            Optional<?> optional = (Optional<?>)result;

            assertTrue(optional.isPresent());
            assertEquals("default.value", optional.get());
        }

        @Test
        @DisplayName("should resolve default value from default interface method parameter")
        public void testOptional5() throws Throwable {
            ExternalizedProperties externalizedProperties = 
                externalizedProperties(Collections.emptyMap());

            Method proxyMethod = ProxyMethodUtils.getMethod(
                OptionalProxyInterface.class,
                "optionalPropertyWithDefaultValueParameter",
                String.class
            );

            ExternalizedPropertyInvocationHandler handler = 
                new ExternalizedPropertyInvocationHandler(externalizedProperties);

            String providedDefaultValue = "provided.default.value";
            Object result = handler.invoke(
                externalizedProperties.proxy(OptionalProxyInterface.class), 
                proxyMethod, 
                new Object[] { providedDefaultValue }
            );

            assertTrue(result instanceof Optional<?>);

            Optional<?> optional = (Optional<?>)result;

            assertTrue(optional.isPresent());
            assertEquals(providedDefaultValue, optional.get());
        }

        @Test
        @DisplayName(
            "should always return default value from default interface method when not annotated"
        )
        public void testOptional6() throws Throwable {
            ExternalizedProperties externalizedProperties = 
                externalizedProperties(Collections.emptyMap());

            Method proxyMethod = ProxyMethodUtils.getMethod(
                OptionalProxyInterface.class,
                "optionalPropertyWithNoAnnotationAndWithDefaultValue"
            );

            ExternalizedPropertyInvocationHandler handler = 
                new ExternalizedPropertyInvocationHandler(externalizedProperties);

            Object result = handler.invoke(
                externalizedProperties.proxy(OptionalProxyInterface.class), 
                proxyMethod, 
                new Object[0]
            );

            assertTrue(result instanceof Optional<?>);

            Optional<?> optional = (Optional<?>)result;

            assertTrue(optional.isPresent());
            assertEquals("default.value", optional.get());
        }

        @Test
        @DisplayName(
            "should always return default value from default interface method parameter " + 
            "when not annotated"
        )
        public void testOptional7() throws Throwable {
            ExternalizedProperties externalizedProperties = 
                externalizedProperties(Collections.emptyMap());

            Method proxyMethod = ProxyMethodUtils.getMethod(
                OptionalProxyInterface.class,
                "optionalPropertyWithNoAnnotationAndWithDefaultValueParameter",
                String.class
            );

            ExternalizedPropertyInvocationHandler handler = 
                new ExternalizedPropertyInvocationHandler(externalizedProperties);

            String providedDefaultValue = "provided.default.value";
            Object result = handler.invoke(
                externalizedProperties.proxy(OptionalProxyInterface.class), 
                proxyMethod, 
                new Object[] { providedDefaultValue }
            );

            assertTrue(result instanceof Optional<?>);

            Optional<?> optional = (Optional<?>)result;

            assertTrue(optional.isPresent());
            assertEquals(providedDefaultValue, optional.get());
        }

        @Test
        @DisplayName(
            "should return empty Optional when an annotated Optional property cannot be resolved."
        )
        public void testOptional8() throws Throwable {
            ExternalizedProperties externalizedProperties = 
                externalizedProperties(Collections.emptyMap());

            Method proxyMethod = ProxyMethodUtils.getMethod(
                OptionalProxyInterface.class,
                "optionalProperty"
            );

            ExternalizedPropertyInvocationHandler handler = 
                new ExternalizedPropertyInvocationHandler(externalizedProperties);

            Object result = handler.invoke(
                externalizedProperties.proxy(OptionalProxyInterface.class), 
                proxyMethod, 
                new Object[0]
            );

            assertTrue(result instanceof Optional<?>);

            Optional<?> optional = (Optional<?>)result;

            assertFalse(optional.isPresent());
        }

        @Test
        @DisplayName(
            "should return empty Optional when an unannotated Optional property cannot be resolved."
        )
        public void testOptional9() throws Throwable {
            ExternalizedProperties externalizedProperties = 
                externalizedProperties(Collections.emptyMap());

            Method proxyMethod = ProxyMethodUtils.getMethod(
                OptionalProxyInterface.class,
                "optionalPropertyWithNoAnnotationAndNoDefaultValue"
            );

            ExternalizedPropertyInvocationHandler handler = 
                new ExternalizedPropertyInvocationHandler(externalizedProperties);

            Object result = handler.invoke(
                externalizedProperties.proxy(OptionalProxyInterface.class), 
                proxyMethod, 
                new Object[0]
            );

            assertTrue(result instanceof Optional<?>);

            Optional<?> optional = (Optional<?>)result;

            assertFalse(optional.isPresent());
        }

        @Test
        @DisplayName("should convert a non-String Optional property via Converter.")
        public void testOptional10() throws Throwable {
            Map<String, String> map = new HashMap<>();
            map.put("property.optional.nonstring", "1");

            ExternalizedProperties externalizedProperties = externalizedProperties(map);

            Method proxyMethod = ProxyMethodUtils.getMethod(
                OptionalProxyInterface.class,
                "nonStringOptionalProperty"
            );

            ExternalizedPropertyInvocationHandler handler = 
                new ExternalizedPropertyInvocationHandler(externalizedProperties);

            Object result = handler.invoke(
                externalizedProperties.proxy(OptionalProxyInterface.class), 
                proxyMethod, 
                new Object[0]
            );

            assertTrue(result instanceof Optional<?>);

            Optional<?> optional = (Optional<?>)result;

            assertTrue(optional.isPresent());
            assertTrue(optional.get() instanceof Integer);
            assertEquals(1, (Integer)optional.get());
        }

        @Test
        @DisplayName("should return true when proxy object references are the same")
        public void proxyEqualsMethod1() throws Throwable {
            ExternalizedProperties externalizedProperties = 
                externalizedProperties(Collections.emptyMap());

            BasicProxyInterface proxy = 
                externalizedProperties.proxy(BasicProxyInterface.class);

            Method objectEqualsMethod = 
                ProxyMethodUtils.getMethod(
                    Object.class,
                    "equals",
                    Object.class
                );

            ExternalizedPropertyInvocationHandler handler = 
                new ExternalizedPropertyInvocationHandler(externalizedProperties);

            // proxy == proxy
            Object isEqual = handler.invoke(
                proxy, 
                objectEqualsMethod, 
                new Object[] { proxy }
            );

            assertTrue(isEqual instanceof Boolean);
            assertTrue((boolean)isEqual);
        }

        @Test
        @DisplayName("should return false when proxy object references are not same")
        public void proxyEqualsMethod2() throws Throwable {
            ExternalizedProperties externalizedProperties = 
                externalizedProperties(Collections.emptyMap());

            BasicProxyInterface proxy1 = 
                externalizedProperties.proxy(BasicProxyInterface.class);
            BasicProxyInterface proxy2 = 
                externalizedProperties.proxy(BasicProxyInterface.class);

            Method objectEqualsMethod = 
                ProxyMethodUtils.getMethod(
                    Object.class,
                    "equals",
                    Object.class
                );

            ExternalizedPropertyInvocationHandler handler = 
                new ExternalizedPropertyInvocationHandler(externalizedProperties);

            // proxy1 != proxy2
            Object isEqual = handler.invoke(
                proxy1, 
                objectEqualsMethod, 
                new Object[] { proxy2 }
            );

            assertTrue(isEqual instanceof Boolean);
            assertFalse((boolean)isEqual);
        }

        @Test
        @DisplayName(
            "should treat equals method with different signature as a proxy method"
        )
        public void proxyEqualsMethod3() throws Throwable {
            ExternalizedProperties externalizedProperties = 
                externalizedProperties(Collections.emptyMap());

            EqualsProxyInterfaceTest proxy = 
                externalizedProperties.proxy(EqualsProxyInterfaceTest.class);

            Method objectEqualsMethod = 
                ProxyMethodUtils.getMethod(
                    EqualsProxyInterfaceTest.class,
                    "equals"
                );

            ExternalizedPropertyInvocationHandler handler = 
                new ExternalizedPropertyInvocationHandler(externalizedProperties);

            // equals method treated as proxy method
            // instead of an Object method due to different signature.
            assertThrows(
                UnresolvedPropertiesException.class,
                () -> handler.invoke(
                    proxy, 
                    objectEqualsMethod, 
                    new Object[] { proxy }
                )
            );
        }

        @Test
        @DisplayName("should return proxy's identity hash code")
        public void proxyHashCodeMethod() throws Throwable {
            ExternalizedProperties externalizedProperties = 
                externalizedProperties(Collections.emptyMap());

            BasicProxyInterface proxy = 
                externalizedProperties.proxy(BasicProxyInterface.class);

            Method objectHashCodeMethod = 
                ProxyMethodUtils.getMethod(
                    Object.class,
                    "hashCode"
                );

            ExternalizedPropertyInvocationHandler handler = 
                new ExternalizedPropertyInvocationHandler(externalizedProperties);

            Object hashCode = handler.invoke(
                proxy, 
                objectHashCodeMethod, 
                new Object[0]
            );

            assertEquals(
                System.identityHashCode(proxy),
                hashCode
            );
        }

        @Test
        @DisplayName("should return standard Object.toString() format")
        public void proxyToStringMethod() throws Throwable {
            ExternalizedProperties externalizedProperties = 
                externalizedProperties(Collections.emptyMap());

            BasicProxyInterface proxy = 
                externalizedProperties.proxy(BasicProxyInterface.class);

            Method objectToStringMethod = 
                ProxyMethodUtils.getMethod(
                    Object.class,
                    "toString"
                );

            ExternalizedPropertyInvocationHandler handler = 
                new ExternalizedPropertyInvocationHandler(externalizedProperties);

            Object toStringResult = handler.invoke(
                proxy, 
                objectToStringMethod, 
                new Object[0]
            );

            assertEquals(
                objectToStringMethod.invoke(proxy),
                toStringResult
            );
        }
    }

    private static interface EqualsProxyInterfaceTest {
        public boolean equals();
    }

    private ExternalizedProperties externalizedProperties(
            Map<String, String> propertySource,
            Converter<?>... converters
    ) {
        return externalizedProperties(
            Arrays.asList(new MapResolver(propertySource)),
            Arrays.asList(converters)
        );
    }

    private ExternalizedProperties externalizedProperties(
            Collection<Resolver> resolvers,
            Collection<Converter<?>> converters
    ) {
        Resolver resolver = CompositeResolver.flatten(resolvers);
        
        ExternalizedProperties.Builder builder = 
            ExternalizedProperties.builder()
                .resolvers(resolver)
                .converters(converters);

        if (converters.size() == 0) {
            builder.withDefaultConverters();
        }

        return builder.build();
    }
}
