package io.github.jeyjeyemem.externalizedproperties.core.internal.proxy;

import io.github.jeyjeyemem.externalizedproperties.core.Converter;
import io.github.jeyjeyemem.externalizedproperties.core.ConverterProvider;
import io.github.jeyjeyemem.externalizedproperties.core.ExternalizedProperties;
import io.github.jeyjeyemem.externalizedproperties.core.Resolver;
import io.github.jeyjeyemem.externalizedproperties.core.ResolverProvider;
import io.github.jeyjeyemem.externalizedproperties.core.UnresolvedPropertiesException;
import io.github.jeyjeyemem.externalizedproperties.core.conversion.converters.DefaultConverter;
import io.github.jeyjeyemem.externalizedproperties.core.conversion.converters.OptionalConverter;
import io.github.jeyjeyemem.externalizedproperties.core.conversion.converters.PrimitiveConverter;
import io.github.jeyjeyemem.externalizedproperties.core.internal.conversion.NoOpConverter;
import io.github.jeyjeyemem.externalizedproperties.core.internal.conversion.RootConverter;
import io.github.jeyjeyemem.externalizedproperties.core.internal.processing.RootProcessor;
import io.github.jeyjeyemem.externalizedproperties.core.internal.resolvers.RootResolver;
import io.github.jeyjeyemem.externalizedproperties.core.resolvers.MapResolver;
import io.github.jeyjeyemem.externalizedproperties.core.testentities.proxy.BasicProxyInterface;
import io.github.jeyjeyemem.externalizedproperties.core.testentities.proxy.DefaultValueProxyInterface;
import io.github.jeyjeyemem.externalizedproperties.core.testentities.proxy.SystemPropertiesProxyInterface;
import io.github.jeyjeyemem.externalizedproperties.core.testentities.proxy.NoAnnotationProxyInterface;
import io.github.jeyjeyemem.externalizedproperties.core.testentities.proxy.OptionalProxyInterface;
import io.github.jeyjeyemem.externalizedproperties.core.testentities.proxy.PrimitiveProxyInterface;
import io.github.jeyjeyemem.externalizedproperties.core.testentities.proxy.VariableProxyInterface;
import io.github.jeyjeyemem.externalizedproperties.core.testfixtures.ProxyMethodUtils;
import io.github.jeyjeyemem.externalizedproperties.core.variableexpansion.SimpleVariableExpander;
import io.github.jeyjeyemem.externalizedproperties.core.variableexpansion.VariableExpansionException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.util.Arrays;
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
public class ExternalizedPropertiesInvocationHandlerTests {
    @Nested
    class InvokeMethod {
        @Test
        @DisplayName("should resolve property")
        public void test1() throws Throwable {
            Map<String, String> map = new HashMap<>();
            map.put("property", "test.value.1");

            ResolverProvider<?> resolverProvider = ep -> new MapResolver(map);
            ConverterProvider<?> converterProvider = (ep, rc) -> NoOpConverter.INSTANCE;
            ExternalizedProperties externalizedProperties = externalizedProperties(
                resolverProvider,
                converterProvider
            );

            Method proxyMethod = ProxyMethodUtils.getMethod(
                BasicProxyInterface.class,
                "property"
            );

            ExternalizedPropertiesInvocationHandler handler = 
                new ExternalizedPropertiesInvocationHandler(
                    rootResolver(
                        externalizedProperties,
                        resolverProvider
                    ),
                    rootConverter(
                        externalizedProperties,
                        converterProvider
                    )
                );

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

            ResolverProvider<?> resolverProvider = ep -> new MapResolver(map);
            ConverterProvider<?> converterProvider = (ep, rc) -> NoOpConverter.INSTANCE;
            ExternalizedProperties externalizedProperties = externalizedProperties(
                resolverProvider,
                converterProvider
            );

            Class<DefaultValueProxyInterface> proxyInterface = 
                DefaultValueProxyInterface.class;
            
            Method proxyMethod = ProxyMethodUtils.getMethod(
                proxyInterface,
                "propertyWithDefaultValue"
            );

            ExternalizedPropertiesInvocationHandler handler = 
                new ExternalizedPropertiesInvocationHandler(
                    rootResolver(
                        externalizedProperties,
                        resolverProvider
                    ),
                    rootConverter(
                        externalizedProperties,
                        converterProvider
                    )
                );

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

            ResolverProvider<?> resolverProvider = ep -> new MapResolver(map);
            ConverterProvider<?> converterProvider = (ep, rc) -> NoOpConverter.INSTANCE;
            ExternalizedProperties externalizedProperties = externalizedProperties(
                resolverProvider,
                converterProvider
            );

            Class<DefaultValueProxyInterface> proxyInterface = 
                DefaultValueProxyInterface.class;

            Method proxyMethod = ProxyMethodUtils.getMethod(
                proxyInterface,
                "propertyWithDefaultValueParameter",
                String.class
            );

            ExternalizedPropertiesInvocationHandler handler = 
                new ExternalizedPropertiesInvocationHandler(
                    rootResolver(
                        externalizedProperties,
                        resolverProvider
                    ),
                    rootConverter(
                        externalizedProperties,
                        converterProvider
                    )
                );

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
            ResolverProvider<?> resolverProvider = ep -> new MapResolver(Collections.emptyMap());
            ConverterProvider<?> converterProvider = (ep, rc) -> NoOpConverter.INSTANCE;
            ExternalizedProperties externalizedProperties = externalizedProperties(
                resolverProvider,
                converterProvider
            );

            Class<DefaultValueProxyInterface> proxyInterface = 
                DefaultValueProxyInterface.class;

            Method proxyMethod = ProxyMethodUtils.getMethod(
                proxyInterface,
                "propertyWithDefaultValue"
            );

            ExternalizedPropertiesInvocationHandler handler = 
                new ExternalizedPropertiesInvocationHandler(
                    rootResolver(
                        externalizedProperties,
                        resolverProvider
                    ),
                    rootConverter(
                        externalizedProperties,
                        converterProvider
                    )
                );

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
            ResolverProvider<?> resolverProvider = ep -> new MapResolver(Collections.emptyMap());
            ConverterProvider<?> converterProvider = (ep, rc) -> NoOpConverter.INSTANCE;
            ExternalizedProperties externalizedProperties = externalizedProperties(
                resolverProvider,
                converterProvider
            );

            Class<DefaultValueProxyInterface> proxyInterface = 
                DefaultValueProxyInterface.class;

            Method proxyMethod = ProxyMethodUtils.getMethod(
                proxyInterface,
                "propertyWithDefaultValueParameter",
                String.class
            );

            ExternalizedPropertiesInvocationHandler handler = 
                new ExternalizedPropertiesInvocationHandler(
                    rootResolver(
                        externalizedProperties,
                        resolverProvider
                    ),
                    rootConverter(
                        externalizedProperties,
                        converterProvider
                    )
                );

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
            ResolverProvider<?> resolverProvider = ep -> new MapResolver(Collections.emptyMap());
            ConverterProvider<?> converterProvider = (ep, rc) -> NoOpConverter.INSTANCE;
            ExternalizedProperties externalizedProperties = externalizedProperties(
                resolverProvider,
                converterProvider
            );

            Class<NoAnnotationProxyInterface> proxyInterface = 
                NoAnnotationProxyInterface.class;

            Method proxyMethod = ProxyMethodUtils.getMethod(
                proxyInterface,
                "propertyWithNoAnnotationButWithDefaultValue"
            );

            ExternalizedPropertiesInvocationHandler handler = 
                new ExternalizedPropertiesInvocationHandler(
                    rootResolver(
                        externalizedProperties,
                        resolverProvider
                    ),
                    rootConverter(
                        externalizedProperties,
                        converterProvider
                    )
                );

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
            ResolverProvider<?> resolverProvider = ep -> new MapResolver(Collections.emptyMap());
            ConverterProvider<?> converterProvider = (ep, rc) -> NoOpConverter.INSTANCE;
            ExternalizedProperties externalizedProperties = externalizedProperties(
                resolverProvider,
                converterProvider
            );

            Class<NoAnnotationProxyInterface> proxyInterface = 
                NoAnnotationProxyInterface.class;

            Method proxyMethod = ProxyMethodUtils.getMethod(
                proxyInterface,
                "propertyWithNoAnnotationButWithDefaultValueParameter",
                String.class
            );

            ExternalizedPropertiesInvocationHandler handler = 
                new ExternalizedPropertiesInvocationHandler(
                    rootResolver(
                        externalizedProperties,
                        resolverProvider
                    ),
                    rootConverter(
                        externalizedProperties,
                        converterProvider
                    )
                );

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
            ResolverProvider<?> resolverProvider = ep -> new MapResolver(Collections.emptyMap());
            ConverterProvider<?> converterProvider = (ep, rc) -> NoOpConverter.INSTANCE;
            ExternalizedProperties externalizedProperties = externalizedProperties(
                resolverProvider,
                converterProvider
            );

            Method proxyMethod = ProxyMethodUtils.getMethod(
                BasicProxyInterface.class,
                "property"
            );

            ExternalizedPropertiesInvocationHandler handler = 
                new ExternalizedPropertiesInvocationHandler(
                    rootResolver(
                        externalizedProperties,
                        resolverProvider
                    ),
                    rootConverter(
                        externalizedProperties,
                        converterProvider
                    )
                );

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
            ResolverProvider<?> resolverProvider = ep -> new MapResolver(Collections.emptyMap());
            ConverterProvider<?> converterProvider = (ep, rc) -> NoOpConverter.INSTANCE;
            ExternalizedProperties externalizedProperties = externalizedProperties(
                resolverProvider,
                converterProvider
            );

            Method proxyMethod = ProxyMethodUtils.getMethod(
                NoAnnotationProxyInterface.class,
                "propertyWithNoAnnotationAndNoDefaultValue"
            );

            ExternalizedPropertiesInvocationHandler handler = 
                new ExternalizedPropertiesInvocationHandler(
                    rootResolver(
                        externalizedProperties,
                        resolverProvider
                    ),
                    rootConverter(
                        externalizedProperties,
                        converterProvider
                    )
                );

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

            ResolverProvider<?> resolverProvider = ep -> new MapResolver(map);
            ConverterProvider<?> converterProvider = (ep, rc) -> new PrimitiveConverter();
            ExternalizedProperties externalizedProperties = externalizedProperties(
                resolverProvider,
                converterProvider
            );

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

            ExternalizedPropertiesInvocationHandler handler = 
                new ExternalizedPropertiesInvocationHandler(
                    rootResolver(
                        externalizedProperties,
                        resolverProvider
                    ),
                    rootConverter(
                        externalizedProperties,
                        converterProvider
                    )
                );

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

            ResolverProvider<?> resolverProvider = ep -> new MapResolver(map);
            ConverterProvider<?> converterProvider = (ep, rc) -> NoOpConverter.INSTANCE;
            ExternalizedProperties externalizedProperties = externalizedProperties(
                resolverProvider,
                converterProvider
            );

            Method proxyMethod = ProxyMethodUtils.getMethod(
                VariableProxyInterface.class,
                "variableProperty"
            );

            ExternalizedPropertiesInvocationHandler handler = 
                new ExternalizedPropertiesInvocationHandler(
                    rootResolver(
                        externalizedProperties,
                        resolverProvider
                    ),
                    rootConverter(
                        externalizedProperties,
                        converterProvider
                    )
                );

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

            ResolverProvider<?> resolverProvider = ep -> new MapResolver(map);
            ConverterProvider<?> converterProvider = (ep, rc) -> NoOpConverter.INSTANCE;
            ExternalizedProperties externalizedProperties = externalizedProperties(
                resolverProvider,
                converterProvider
            );

            Method proxyMethod = ProxyMethodUtils.getMethod(
                VariableProxyInterface.class,
                "variableProperty"
            );

            ExternalizedPropertiesInvocationHandler handler = 
                new ExternalizedPropertiesInvocationHandler(
                    rootResolver(
                        externalizedProperties,
                        resolverProvider
                    ),
                    rootConverter(
                        externalizedProperties,
                        converterProvider
                    )
                );

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

            ResolverProvider<?> resolverProvider = ep -> new MapResolver(map);
            ConverterProvider<?> converterProvider = (ep, rc) -> new OptionalConverter(rc);
            ExternalizedProperties externalizedProperties = externalizedProperties(
                resolverProvider,
                converterProvider
            );

            Method proxyMethod = ProxyMethodUtils.getMethod(
                OptionalProxyInterface.class,
                "optionalProperty"
            );

            ExternalizedPropertiesInvocationHandler handler = 
                new ExternalizedPropertiesInvocationHandler(
                    rootResolver(
                        externalizedProperties,
                        resolverProvider
                    ),
                    rootConverter(
                        externalizedProperties,
                        converterProvider
                    )
                );

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

            ResolverProvider<?> resolverProvider = ep -> new MapResolver(map);
            ConverterProvider<?> converterProvider = (ep, rc) -> new OptionalConverter(rc);
            ExternalizedProperties externalizedProperties = externalizedProperties(
                resolverProvider,
                converterProvider
            );
            
            Method proxyMethod = ProxyMethodUtils.getMethod(
                OptionalProxyInterface.class,
                "optionalPropertyWithDefaultValue"
            );

            ExternalizedPropertiesInvocationHandler handler = 
                new ExternalizedPropertiesInvocationHandler(
                    rootResolver(
                        externalizedProperties,
                        resolverProvider
                    ),
                    rootConverter(
                        externalizedProperties,
                        converterProvider
                    )
                );

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

            ResolverProvider<?> resolverProvider = ep -> new MapResolver(map);
            ConverterProvider<?> converterProvider = (ep, rc) -> new OptionalConverter(rc);
            ExternalizedProperties externalizedProperties = externalizedProperties(
                resolverProvider,
                converterProvider
            );
            
            Method proxyMethod = ProxyMethodUtils.getMethod(
                OptionalProxyInterface.class,
                "optionalPropertyWithDefaultValue"
            );

            ExternalizedPropertiesInvocationHandler handler = 
                new ExternalizedPropertiesInvocationHandler(
                    rootResolver(
                        externalizedProperties,
                        resolverProvider
                    ),
                    rootConverter(
                        externalizedProperties,
                        converterProvider
                    )
                );

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
            ResolverProvider<?> resolverProvider = ep -> new MapResolver(Collections.emptyMap());
            ConverterProvider<?> converterProvider = (ep, rc) -> NoOpConverter.INSTANCE;
            ExternalizedProperties externalizedProperties = externalizedProperties(
                resolverProvider,
                converterProvider
            );

            Method proxyMethod = ProxyMethodUtils.getMethod(
                OptionalProxyInterface.class,
                "optionalPropertyWithDefaultValue"
            );

            ExternalizedPropertiesInvocationHandler handler = 
                new ExternalizedPropertiesInvocationHandler(
                    rootResolver(
                        externalizedProperties,
                        resolverProvider
                    ),
                    rootConverter(
                        externalizedProperties,
                        converterProvider
                    )
                );

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
            ResolverProvider<?> resolverProvider = ep -> new MapResolver(Collections.emptyMap());
            ConverterProvider<?> converterProvider = (ep, rc) -> NoOpConverter.INSTANCE;
            ExternalizedProperties externalizedProperties = externalizedProperties(
                resolverProvider,
                converterProvider
            );

            Method proxyMethod = ProxyMethodUtils.getMethod(
                OptionalProxyInterface.class,
                "optionalPropertyWithDefaultValueParameter",
                String.class
            );

            ExternalizedPropertiesInvocationHandler handler = 
                new ExternalizedPropertiesInvocationHandler(
                    rootResolver(
                        externalizedProperties,
                        resolverProvider
                    ),
                    rootConverter(
                        externalizedProperties,
                        converterProvider
                    )
                );

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
            ResolverProvider<?> resolverProvider = ep -> new MapResolver(Collections.emptyMap());
            ConverterProvider<?> converterProvider = (ep, rc) -> NoOpConverter.INSTANCE;
            ExternalizedProperties externalizedProperties = externalizedProperties(
                resolverProvider,
                converterProvider
            );

            Method proxyMethod = ProxyMethodUtils.getMethod(
                OptionalProxyInterface.class,
                "optionalPropertyWithNoAnnotationAndWithDefaultValue"
            );

            ExternalizedPropertiesInvocationHandler handler = 
                new ExternalizedPropertiesInvocationHandler(
                    rootResolver(
                        externalizedProperties,
                        resolverProvider
                    ),
                    rootConverter(
                        externalizedProperties,
                        converterProvider
                    )
                );

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
            ResolverProvider<?> resolverProvider = ep -> new MapResolver(Collections.emptyMap());
            ConverterProvider<?> converterProvider = (ep, rc) -> NoOpConverter.INSTANCE;
            ExternalizedProperties externalizedProperties = externalizedProperties(
                resolverProvider,
                converterProvider
            );

            Method proxyMethod = ProxyMethodUtils.getMethod(
                OptionalProxyInterface.class,
                "optionalPropertyWithNoAnnotationAndWithDefaultValueParameter",
                String.class
            );

            ExternalizedPropertiesInvocationHandler handler = 
                new ExternalizedPropertiesInvocationHandler(
                    rootResolver(
                        externalizedProperties,
                        resolverProvider
                    ),
                    rootConverter(
                        externalizedProperties,
                        converterProvider
                    )
                );

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
            ResolverProvider<?> resolverProvider = ep -> new MapResolver(Collections.emptyMap());
            ConverterProvider<?> converterProvider = (ep, rc) -> NoOpConverter.INSTANCE;
            ExternalizedProperties externalizedProperties = externalizedProperties(
                resolverProvider,
                converterProvider
            );

            Method proxyMethod = ProxyMethodUtils.getMethod(
                OptionalProxyInterface.class,
                "optionalProperty"
            );

            ExternalizedPropertiesInvocationHandler handler = 
                new ExternalizedPropertiesInvocationHandler(
                    rootResolver(
                        externalizedProperties,
                        resolverProvider
                    ),
                    rootConverter(
                        externalizedProperties,
                        converterProvider
                    )
                );

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
            ResolverProvider<?> resolverProvider = ep -> new MapResolver(Collections.emptyMap());
            ConverterProvider<?> converterProvider = (ep, rc) -> NoOpConverter.INSTANCE;
            ExternalizedProperties externalizedProperties = externalizedProperties(
                resolverProvider,
                converterProvider
            );

            Method proxyMethod = ProxyMethodUtils.getMethod(
                OptionalProxyInterface.class,
                "optionalPropertyWithNoAnnotationAndNoDefaultValue"
            );

            ExternalizedPropertiesInvocationHandler handler = 
                new ExternalizedPropertiesInvocationHandler(
                    rootResolver(
                        externalizedProperties,
                        resolverProvider
                    ),
                    rootConverter(
                        externalizedProperties,
                        converterProvider
                    )
                );

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

            ResolverProvider<?> resolverProvider = ep -> new MapResolver(map);
            ConverterProvider<?> converterProvider = (ep, rc) -> new DefaultConverter(rc);
            ExternalizedProperties externalizedProperties = externalizedProperties(
                resolverProvider,
                converterProvider
            );

            Method proxyMethod = ProxyMethodUtils.getMethod(
                OptionalProxyInterface.class,
                "nonStringOptionalProperty"
            );

            ExternalizedPropertiesInvocationHandler handler = 
                new ExternalizedPropertiesInvocationHandler(
                    rootResolver(
                        externalizedProperties,
                        resolverProvider
                    ),
                    rootConverter(
                        externalizedProperties,
                        converterProvider
                    )
                );

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
            ResolverProvider<?> resolverProvider = ep -> new MapResolver(Collections.emptyMap());
            ConverterProvider<?> converterProvider = (ep, rc) -> NoOpConverter.INSTANCE;
            ExternalizedProperties externalizedProperties = externalizedProperties(
                resolverProvider,
                converterProvider
            );

            BasicProxyInterface proxy = 
                externalizedProperties.proxy(BasicProxyInterface.class);

            Method objectEqualsMethod = 
                ProxyMethodUtils.getMethod(
                    Object.class,
                    "equals",
                    Object.class
                );

            ExternalizedPropertiesInvocationHandler handler = 
                new ExternalizedPropertiesInvocationHandler(
                    rootResolver(
                        externalizedProperties,
                        resolverProvider
                    ),
                    rootConverter(
                        externalizedProperties,
                        converterProvider
                    )
                );

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
            ResolverProvider<?> resolverProvider = ep -> new MapResolver(Collections.emptyMap());
            ConverterProvider<?> converterProvider = (ep, rc) -> NoOpConverter.INSTANCE;
            ExternalizedProperties externalizedProperties = externalizedProperties(
                resolverProvider,
                converterProvider
            );

            BasicProxyInterface proxy1 = 
                externalizedProperties.proxy(BasicProxyInterface.class);
            SystemPropertiesProxyInterface proxy2 = 
                externalizedProperties.proxy(SystemPropertiesProxyInterface.class);

            Method objectEqualsMethod = 
                ProxyMethodUtils.getMethod(
                    Object.class,
                    "equals",
                    Object.class
                );

            ExternalizedPropertiesInvocationHandler handler = 
                new ExternalizedPropertiesInvocationHandler(
                    rootResolver(
                        externalizedProperties,
                        resolverProvider
                    ),
                    rootConverter(
                        externalizedProperties,
                        converterProvider
                    )
                );

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
            ResolverProvider<?> resolverProvider = ep -> new MapResolver(Collections.emptyMap());
            ConverterProvider<?> converterProvider = (ep, rc) -> NoOpConverter.INSTANCE;
            ExternalizedProperties externalizedProperties = externalizedProperties(
                resolverProvider,
                converterProvider
            );

            EqualsProxyInterfaceTest proxy = 
                externalizedProperties.proxy(EqualsProxyInterfaceTest.class);

            Method objectEqualsMethod = 
                ProxyMethodUtils.getMethod(
                    EqualsProxyInterfaceTest.class,
                    "equals"
                );

            ExternalizedPropertiesInvocationHandler handler = 
                new ExternalizedPropertiesInvocationHandler(
                    rootResolver(
                        externalizedProperties,
                        resolverProvider
                    ),
                    rootConverter(
                        externalizedProperties,
                        converterProvider
                    )
                );

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
            ResolverProvider<?> resolverProvider = ep -> new MapResolver(Collections.emptyMap());
            ConverterProvider<?> converterProvider = (ep, rc) -> NoOpConverter.INSTANCE;
            ExternalizedProperties externalizedProperties = externalizedProperties(
                resolverProvider,
                converterProvider
            );

            BasicProxyInterface proxy = 
                externalizedProperties.proxy(BasicProxyInterface.class);

            Method objectHashCodeMethod = 
                ProxyMethodUtils.getMethod(
                    Object.class,
                    "hashCode"
                );

            ExternalizedPropertiesInvocationHandler handler = 
                new ExternalizedPropertiesInvocationHandler(
                    rootResolver(
                        externalizedProperties,
                        resolverProvider
                    ),
                    rootConverter(
                        externalizedProperties,
                        converterProvider
                    )
                );

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
            ResolverProvider<?> resolverProvider = ep -> new MapResolver(Collections.emptyMap());
            ConverterProvider<?> converterProvider = (ep, rc) -> NoOpConverter.INSTANCE;
            ExternalizedProperties externalizedProperties = externalizedProperties(
                resolverProvider,
                converterProvider
            );

            BasicProxyInterface proxy = 
                externalizedProperties.proxy(BasicProxyInterface.class);

            Method objectToStringMethod = 
                ProxyMethodUtils.getMethod(
                    Object.class,
                    "toString"
                );

            ExternalizedPropertiesInvocationHandler handler = 
                new ExternalizedPropertiesInvocationHandler(
                    rootResolver(
                        externalizedProperties,
                        resolverProvider
                    ),
                    rootConverter(
                        externalizedProperties,
                        converterProvider
                    )
                );

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

    private Resolver rootResolver(
            ExternalizedProperties externalizedProperties,
            ResolverProvider<?>... resolverProviders
    ) {
        return new RootResolver(
            externalizedProperties,
            Arrays.asList(resolverProviders), 
            ep -> new RootProcessor(ep), 
            ep -> new SimpleVariableExpander(ep)
        );
    }

    private Converter<?> rootConverter(
            ExternalizedProperties externalizedProperties,
            ConverterProvider<?>... converterProviders
    ) {
        return new RootConverter(
            externalizedProperties,
            converterProviders
        );
    }

    private ExternalizedProperties externalizedProperties(
            ResolverProvider<?> resolverProvider,
            ConverterProvider<?> converterProvider
    ) { 
        return ExternalizedProperties.builder()
            .resolvers(resolverProvider)
            .converters(converterProvider)
            .build();
    }
}
