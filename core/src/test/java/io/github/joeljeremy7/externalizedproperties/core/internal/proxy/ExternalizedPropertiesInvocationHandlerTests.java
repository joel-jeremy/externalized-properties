package io.github.joeljeremy7.externalizedproperties.core.internal.proxy;

import io.github.joeljeremy7.externalizedproperties.core.Convert;
import io.github.joeljeremy7.externalizedproperties.core.Converter;
import io.github.joeljeremy7.externalizedproperties.core.ExpandVariables;
import io.github.joeljeremy7.externalizedproperties.core.ExternalizedProperties;
import io.github.joeljeremy7.externalizedproperties.core.ExternalizedPropertiesException;
import io.github.joeljeremy7.externalizedproperties.core.ExternalizedProperty;
import io.github.joeljeremy7.externalizedproperties.core.Processor;
import io.github.joeljeremy7.externalizedproperties.core.Resolver;
import io.github.joeljeremy7.externalizedproperties.core.TypeReference;
import io.github.joeljeremy7.externalizedproperties.core.UnresolvedPropertiesException;
import io.github.joeljeremy7.externalizedproperties.core.VariableExpander;
import io.github.joeljeremy7.externalizedproperties.core.conversion.converters.DefaultConverter;
import io.github.joeljeremy7.externalizedproperties.core.conversion.converters.PrimitiveConverter;
import io.github.joeljeremy7.externalizedproperties.core.internal.conversion.RootConverter;
import io.github.joeljeremy7.externalizedproperties.core.internal.processing.RootProcessor;
import io.github.joeljeremy7.externalizedproperties.core.internal.resolvers.RootResolver;
import io.github.joeljeremy7.externalizedproperties.core.processing.Decrypt;
import io.github.joeljeremy7.externalizedproperties.core.processing.processors.DecryptProcessor;
import io.github.joeljeremy7.externalizedproperties.core.processing.processors.DecryptProcessor.JceDecryptor;
import io.github.joeljeremy7.externalizedproperties.core.resolvers.MapResolver;
import io.github.joeljeremy7.externalizedproperties.core.testentities.EncryptionUtils;
import io.github.joeljeremy7.externalizedproperties.core.testfixtures.ProxyMethodReference;
import io.github.joeljeremy7.externalizedproperties.core.testfixtures.ProxyMethodUtils;
import io.github.joeljeremy7.externalizedproperties.core.testfixtures.StubResolver;
import io.github.joeljeremy7.externalizedproperties.core.variableexpansion.SimpleVariableExpander;
import io.github.joeljeremy7.externalizedproperties.core.variableexpansion.VariableExpansionException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import javax.crypto.SecretKey;

import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static io.github.joeljeremy7.externalizedproperties.core.testentities.EncryptionUtils.AES_GCM_ALGORITHM;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

// Let ExternalizedProperties.initialize(Class<?> proxyInterface) 
// create the proxy for these test cases.
public class ExternalizedPropertiesInvocationHandlerTests {
    private static final VariableExpander VARIABLE_EXPANDER = 
        new SimpleVariableExpander();
    
    @Nested
    class InvokeMethod {
        @Test
        @DisplayName("should resolve property")
        void test1() throws Throwable {
            Resolver resolver = new MapResolver("property", "test.value.1");
            ExternalizedProperties externalizedProperties = 
                externalizedProperties(resolver);

            Class<ProxyInterface> proxyInterface = ProxyInterface.class;

            Method proxyMethod = ProxyMethodUtils.getMethod(
                proxyInterface,
                ProxyInterface::property
            );

            ExternalizedPropertiesInvocationHandler handler = 
                new ExternalizedPropertiesInvocationHandler(
                    rootResolver(resolver),
                    rootConverter(),
                    variableExpander(),
                    new ProxyMethodFactory(externalizedProperties)
                );

            Object result = handler.invoke(
                externalizedProperties.initialize(proxyInterface), 
                proxyMethod, 
                null
            );

            assertEquals("test.value.1", result);
        }

        @Test
        @DisplayName(
            "should resolve property from map and not from default interface method value"
        )
        void test2() throws Throwable {
            Resolver resolver = new MapResolver(
                "property.with.default.value", 
                "test.value"
            );
            ExternalizedProperties externalizedProperties = 
                externalizedProperties(resolver);

            Class<ProxyInterface> proxyInterface = ProxyInterface.class;
            
            Method proxyMethod = ProxyMethodUtils.getMethod(
                proxyInterface,
                ProxyInterface::propertyWithDefaultValue
            );

            ExternalizedPropertiesInvocationHandler handler = 
                new ExternalizedPropertiesInvocationHandler(
                    rootResolver(resolver),
                    rootConverter(),
                    variableExpander(),
                    new ProxyMethodFactory(externalizedProperties)
                );

            Object result = handler.invoke(
                externalizedProperties.initialize(proxyInterface), 
                proxyMethod, 
                null
            );

            assertEquals("test.value", result);
        }

        @Test
        @DisplayName(
            "should resolve property from map and not from default interface method value parameter"
        )
        void test3() throws Throwable {
            Resolver resolver = new MapResolver(
                "property.with.default.value", 
                "test.value"
            );
            ExternalizedProperties externalizedProperties = 
                externalizedProperties(resolver);

            Class<ProxyInterface> proxyInterface = ProxyInterface.class;

            Method proxyMethod = ProxyMethodUtils.getMethod(
                proxyInterface,
                ProxyInterface::propertyWithDefaultValueParameter
            );

            ExternalizedPropertiesInvocationHandler handler = 
                new ExternalizedPropertiesInvocationHandler(
                    rootResolver(resolver),
                    rootConverter(),
                    variableExpander(),
                    new ProxyMethodFactory(externalizedProperties)
                );

            String providedDefaultValue = "provided.default.value";
            Object result = handler.invoke(
                externalizedProperties.initialize(proxyInterface),
                proxyMethod, 
                new Object[] { providedDefaultValue }
            );

            assertEquals("test.value", result);
        }

        @Test
        @DisplayName("should resolve default value from default interface method")
        void test4() throws Throwable {
            Resolver resolver = new MapResolver(Collections.emptyMap());
            ExternalizedProperties externalizedProperties = 
                externalizedProperties(resolver);

            Class<ProxyInterface> proxyInterface = ProxyInterface.class;

            Method proxyMethod = ProxyMethodUtils.getMethod(
                proxyInterface,
                ProxyInterface::propertyWithDefaultValue
            );

            ExternalizedPropertiesInvocationHandler handler = 
                new ExternalizedPropertiesInvocationHandler(
                    rootResolver(resolver),
                    rootConverter(),
                    variableExpander(),
                    new ProxyMethodFactory(externalizedProperties)
                );

            Object result = handler.invoke(
                externalizedProperties.initialize(proxyInterface), 
                proxyMethod, 
                null
            );

            assertEquals("default.value", result);
        }

        @Test
        @DisplayName("should resolve default value from default interface method parameter")
        void test5() throws Throwable {
            Resolver resolver = new MapResolver(Collections.emptyMap());
            ExternalizedProperties externalizedProperties = 
                externalizedProperties(resolver);

            Class<ProxyInterface> proxyInterface = ProxyInterface.class;

            Method proxyMethod = ProxyMethodUtils.getMethod(
                proxyInterface,
                ProxyInterface::propertyWithDefaultValueParameter
            );

            ExternalizedPropertiesInvocationHandler handler = 
                new ExternalizedPropertiesInvocationHandler(
                    rootResolver(resolver),
                    rootConverter(),
                    variableExpander(),
                    new ProxyMethodFactory(externalizedProperties)
                );

            // Pass default value.
            String providedDefaultValue = "provided.default.value";
            Object result = handler.invoke(
                externalizedProperties.initialize(proxyInterface), 
                proxyMethod, 
                new Object[] { providedDefaultValue }
            );

            assertEquals(providedDefaultValue, result);
        }

        @Test
        @DisplayName(
            "should always return default value from default interface method when not annotated"
        )
        void test6() throws Throwable {
            Resolver resolver = new MapResolver(Collections.emptyMap());
            ExternalizedProperties externalizedProperties = 
                externalizedProperties(resolver);

            Class<ProxyInterface> proxyInterface = ProxyInterface.class;

            Method proxyMethod = ProxyMethodUtils.getMethod(
                proxyInterface,
                ProxyInterface::propertyWithNoAnnotationButWithDefaultValue
            );

            ExternalizedPropertiesInvocationHandler handler = 
                new ExternalizedPropertiesInvocationHandler(
                    rootResolver(resolver),
                    rootConverter(),
                    variableExpander(),
                    new ProxyMethodFactory(externalizedProperties)
                );

            Object result = handler.invoke(
                externalizedProperties.initialize(proxyInterface), 
                proxyMethod, 
                null
            );

            assertEquals("default.value", result);
        }

        @Test
        @DisplayName(
            "should always return default value from default interface method parameter " + 
            "when not annotated"
        )
        void test7() throws Throwable {
            Resolver resolver = new MapResolver(Collections.emptyMap());
            ExternalizedProperties externalizedProperties = 
                externalizedProperties(resolver);

            Class<ProxyInterface> proxyInterface = ProxyInterface.class;

            Method proxyMethod = ProxyMethodUtils.getMethod(
                proxyInterface,
                ProxyInterface::propertyWithNoAnnotationButWithDefaultValueParameter
            );

            ExternalizedPropertiesInvocationHandler handler = 
                new ExternalizedPropertiesInvocationHandler(
                    rootResolver(resolver),
                    rootConverter(),
                    variableExpander(),
                    new ProxyMethodFactory(externalizedProperties)
                );

            // Pass default value.
            String providedDefaultValue = "provided.default.value";
            Object result = handler.invoke(
                externalizedProperties.initialize(proxyInterface), 
                proxyMethod, 
                new Object[] { providedDefaultValue }
            );

            assertEquals(providedDefaultValue, result);
        }

        @Test
        @DisplayName("should throw when an annotated non-Optional property cannot be resolved.")
        void test8() {
            Resolver resolver = new MapResolver(Collections.emptyMap());
            ExternalizedProperties externalizedProperties = 
                externalizedProperties(resolver);

            Class<ProxyInterface> proxyInterface = ProxyInterface.class;

            Method proxyMethod = ProxyMethodUtils.getMethod(
                proxyInterface,
                ProxyInterface::property
            );

            ExternalizedPropertiesInvocationHandler handler = 
                new ExternalizedPropertiesInvocationHandler(
                    rootResolver(resolver),
                    rootConverter(),
                    variableExpander(),
                    new ProxyMethodFactory(externalizedProperties)
                );

            Object proxy = externalizedProperties.initialize(proxyInterface);

            assertThrows(UnresolvedPropertiesException.class, () -> {
                handler.invoke(
                    proxy, 
                    proxyMethod, 
                    null
                );
            });
        }

        @Test
        @DisplayName("should throw when an unannotated non-Optional property cannot be resolved.")
        void test9() {
            Resolver resolver = new MapResolver(Collections.emptyMap());
            ExternalizedProperties externalizedProperties = 
                externalizedProperties(resolver);

            Class<ProxyInterface> proxyInterface = ProxyInterface.class;

            Method proxyMethod = ProxyMethodUtils.getMethod(
                proxyInterface,
                ProxyInterface::propertyWithNoAnnotationAndNoDefaultValue
            );

            ExternalizedPropertiesInvocationHandler handler = 
                new ExternalizedPropertiesInvocationHandler(
                    rootResolver(resolver),
                    rootConverter(),
                    variableExpander(),
                    new ProxyMethodFactory(externalizedProperties)
                );

            Object proxy = externalizedProperties.initialize(proxyInterface);

            assertThrows(UnresolvedPropertiesException.class, () -> {
                handler.invoke(
                    proxy, 
                    proxyMethod, 
                    null
                );
            });
        }

        @Test
        @DisplayName(
            "should return empty Optional when an annotated Optional property cannot be resolved."
        )
        void test10() throws Throwable {
            Resolver resolver = new MapResolver(Collections.emptyMap());
            ExternalizedProperties externalizedProperties = 
                externalizedProperties(resolver);

            Class<ProxyInterface> proxyInterface = ProxyInterface.class;

            Method proxyMethod = ProxyMethodUtils.getMethod(
                proxyInterface,
                ProxyInterface::optionalProperty
            );

            ExternalizedPropertiesInvocationHandler handler = 
                new ExternalizedPropertiesInvocationHandler(
                    rootResolver(resolver),
                    rootConverter(),
                    variableExpander(),
                    new ProxyMethodFactory(externalizedProperties)
                );

            Object result = handler.invoke(
                externalizedProperties.initialize(proxyInterface), 
                proxyMethod, 
                null
            );

            assertTrue(result instanceof Optional<?>);

            Optional<?> optional = (Optional<?>)result;

            assertFalse(optional.isPresent());
        }

        @Test
        @DisplayName(
            "should return empty Optional when an unannotated Optional property cannot be resolved."
        )
        void test11() throws Throwable {
            Resolver resolver = new MapResolver(Collections.emptyMap());
            ExternalizedProperties externalizedProperties = 
                externalizedProperties(resolver);

            Class<ProxyInterface> proxyInterface = ProxyInterface.class;

            Method proxyMethod = ProxyMethodUtils.getMethod(
                proxyInterface,
                ProxyInterface::optionalPropertyWithNoAnnotationAndNoDefaultValue
            );

            ExternalizedPropertiesInvocationHandler handler = 
                new ExternalizedPropertiesInvocationHandler(
                    rootResolver(resolver),
                    rootConverter(),
                    variableExpander(),
                    new ProxyMethodFactory(externalizedProperties)
                );

            Object result = handler.invoke(
                externalizedProperties.initialize(proxyInterface), 
                proxyMethod, 
                null
            );

            assertTrue(result instanceof Optional<?>);

            Optional<?> optional = (Optional<?>)result;

            assertFalse(optional.isPresent());
        }

        /**
         * Conversion tests.
         */

        @Test
        @DisplayName("should convert a non-String property via Converter.")
        void conversionTest1() throws Throwable {
            Resolver resolver = new MapResolver("property.int", "1");
            Converter<?> converter = new PrimitiveConverter();
            ExternalizedProperties externalizedProperties = externalizedProperties(
                resolver,
                converter
            );

            Class<ProxyInterface> proxyInterface = ProxyInterface.class;
            
            Method proxyMethod = ProxyMethodUtils.getMethod(
                proxyInterface,
                ProxyInterface::intProperty
            );

            ExternalizedPropertiesInvocationHandler handler = 
                new ExternalizedPropertiesInvocationHandler(
                    rootResolver(resolver),
                    rootConverter(converter),
                    variableExpander(),
                    new ProxyMethodFactory(externalizedProperties)
                );

            Object intResult = handler.invoke(
                externalizedProperties.initialize(proxyInterface), 
                proxyMethod,
                null
            );

            assertEquals(1, (int)intResult);
        }

        /**
         * Post-processing tests.
         */

        @Test
        @DisplayName("should process resolved property using the specified processor")
        void processingTest1() throws Throwable {
            String plainText = "property";
            SecretKey aesSecretKey = EncryptionUtils.generateAesSecretKey();
            String encryptedBase64 = EncryptionUtils.encryptAesBase64(
                plainText, 
                AES_GCM_ALGORITHM, 
                aesSecretKey,
                EncryptionUtils.DEFAULT_GCM_PARAMETER_SPEC
            );

            Resolver resolver = new MapResolver("property.encrypted", encryptedBase64);
            Processor decryptProcessor = new DecryptProcessor(
                JceDecryptor.factory().symmetric(
                    AES_GCM_ALGORITHM, 
                    aesSecretKey, 
                    EncryptionUtils.DEFAULT_GCM_PARAMETER_SPEC
                )
            );
            ExternalizedProperties externalizedProperties = externalizedProperties(
                resolver,
                decryptProcessor
            );

            Class<ProxyInterface> proxyInterface = ProxyInterface.class;
            
            Method proxyMethod = ProxyMethodUtils.getMethod(
                proxyInterface,
                ProxyInterface::decryptedProperty
            );

            ExternalizedPropertiesInvocationHandler handler = 
                new ExternalizedPropertiesInvocationHandler(
                    rootResolver(resolver, decryptProcessor),
                    rootConverter(),
                    variableExpander(),
                    new ProxyMethodFactory(externalizedProperties)
                );

            Object decryptedResult = handler.invoke(
                externalizedProperties.initialize(proxyInterface), 
                proxyMethod,
                null
            );

            assertEquals(plainText, decryptedResult);
        }

        /**
         * Variable expansion tests.
         */

        @Test
        @DisplayName("should expand variable in property name.")
        void variableExpansionTest1() throws Throwable {
            Map<String, String> map = new HashMap<>();
            String customVariableValue = "custom-variable";
            map.put("custom.variable", customVariableValue);
            map.put("property-" + customVariableValue, "property.value");

            Resolver resolver = new MapResolver(map);
            ExternalizedProperties externalizedProperties = 
                externalizedProperties(resolver);

            Class<ProxyInterface> proxyInterface = ProxyInterface.class;

            Method proxyMethod = ProxyMethodUtils.getMethod(
                proxyInterface,
                ProxyInterface::variableProperty
            );

            ExternalizedPropertiesInvocationHandler handler = 
                new ExternalizedPropertiesInvocationHandler(
                    rootResolver(resolver),
                    rootConverter(),
                    variableExpander(),
                    new ProxyMethodFactory(externalizedProperties)
                );

            Object result = handler.invoke(
                externalizedProperties.initialize(proxyInterface), 
                proxyMethod, 
                null
            );

            assertEquals("property.value", result);
        }

        @Test
        @DisplayName("should throw when variable value cannot be resolved.")
        void variableExpansionTest2() {
            Resolver resolver = new MapResolver(
                "property-custom-variable", 
                "property.value"
            );
            ExternalizedProperties externalizedProperties = 
                externalizedProperties(resolver);

            Class<ProxyInterface> proxyInterface = ProxyInterface.class;

            Method proxyMethod = ProxyMethodUtils.getMethod(
                proxyInterface,
                ProxyInterface::variableProperty
            );

            ExternalizedPropertiesInvocationHandler handler = 
                new ExternalizedPropertiesInvocationHandler(
                    rootResolver(resolver),
                    rootConverter(),
                    variableExpander(),
                    new ProxyMethodFactory(externalizedProperties)
                );

            Object proxy = externalizedProperties.initialize(proxyInterface);

            // There is no custom-variable-value property.
            // Property name of VariableProxyInterface.variableProperty() won't be able to be expanded.
            assertThrows(
                VariableExpansionException.class, 
                () -> handler.invoke(
                    proxy, 
                    proxyMethod, 
                    null
                )
            );
        }

        /**
         * @Convert tests
         */
        
        @Test
        @DisplayName("should convert value to target type reference")
        void convertTest1() throws Throwable {
            Resolver resolver = new StubResolver();
            Converter<?> converter = new DefaultConverter();
            ExternalizedProperties externalizedProperties = externalizedProperties(
                resolver,
                converter
            );
            Class<ConvertProxyInterface> proxyInterface = ConvertProxyInterface.class;

            Method proxyMethod = ProxyMethodUtils.getMethod(
                proxyInterface,
                (ProxyMethodReference.WithTwoArgs<ConvertProxyInterface, String, TypeReference<?>, ?>)
                ConvertProxyInterface::convertToTypeReference
            );
            
            ExternalizedPropertiesInvocationHandler handler = 
                new ExternalizedPropertiesInvocationHandler(
                    rootResolver(resolver),
                    rootConverter(converter),
                    variableExpander(),
                    new ProxyMethodFactory(externalizedProperties)
                );

            Object convertedValue = handler.invoke(
                externalizedProperties.initialize(proxyInterface), 
                proxyMethod, 
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
        void convertTest2() throws Throwable {
            Resolver resolver = new StubResolver();
            Converter<?> converter = new DefaultConverter();
            ExternalizedProperties externalizedProperties = externalizedProperties(
                resolver,
                converter
            );
            Class<ConvertProxyInterface> proxyInterface = ConvertProxyInterface.class;

            Method proxyMethod = ProxyMethodUtils.getMethod(
                proxyInterface,
                (ProxyMethodReference.WithTwoArgs<ConvertProxyInterface, String, TypeReference<?>, ?>)
                ConvertProxyInterface::convertToTypeReferenceObject
            );

            ExternalizedPropertiesInvocationHandler handler = 
                new ExternalizedPropertiesInvocationHandler(
                    rootResolver(resolver),
                    rootConverter(converter),
                    variableExpander(),
                    new ProxyMethodFactory(externalizedProperties)
                );

            Object convertedValue = handler.invoke(
                externalizedProperties.initialize(proxyInterface),
                proxyMethod,
                new Object[] {
                    "1",
                    new TypeReference<Integer>(){}
                }
            );

            assertEquals(1, (Integer)convertedValue);
        }

        @Test
        @DisplayName("should convert value to target class")
        void convertTest3() throws Throwable {
            Resolver resolver = new StubResolver();
            Converter<?> converter = new DefaultConverter();
            ExternalizedProperties externalizedProperties = externalizedProperties(
                resolver,
                converter
            );
            Class<ConvertProxyInterface> proxyInterface = ConvertProxyInterface.class;

            Method method = ProxyMethodUtils.getMethod(
                proxyInterface,
                (ProxyMethodReference.WithTwoArgs<ConvertProxyInterface, String, Class<?>, ?>)
                ConvertProxyInterface::convertToClass
            );
            
            ExternalizedPropertiesInvocationHandler handler = 
                new ExternalizedPropertiesInvocationHandler(
                    rootResolver(resolver),
                    rootConverter(converter),
                    variableExpander(),
                    new ProxyMethodFactory(externalizedProperties)
                );

            Object convertedValue = handler.invoke(
                externalizedProperties.initialize(proxyInterface),
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
        void convertTest4() throws Throwable {
            Resolver resolver = new StubResolver();
            Converter<?> converter = new DefaultConverter();
            ExternalizedProperties externalizedProperties = externalizedProperties(
                resolver,
                converter
            );
            Class<ConvertProxyInterface> proxyInterface = ConvertProxyInterface.class;

            Method proxyMethod = ProxyMethodUtils.getMethod(
                proxyInterface,
                (ProxyMethodReference.WithTwoArgs<ConvertProxyInterface, String, Class<?>, ?>)
                ConvertProxyInterface::convertToClassObject
            );
            
            ExternalizedPropertiesInvocationHandler handler = 
                new ExternalizedPropertiesInvocationHandler(
                    rootResolver(resolver),
                    rootConverter(converter),
                    variableExpander(),
                    new ProxyMethodFactory(externalizedProperties)
                );

            Object convertedValue = handler.invoke(
                externalizedProperties.initialize(proxyInterface),
                proxyMethod,
                new Object[] {
                    "1",
                    Integer.class
                }
            );

            assertEquals(1, (Integer)convertedValue);
        }

        @Test
        @DisplayName("should convert value to target type")
        void convertTest5() throws Throwable {
            Resolver resolver = new StubResolver();
            Converter<?> converter = new DefaultConverter();
            ExternalizedProperties externalizedProperties = externalizedProperties(
                resolver,
                converter
            );
            Class<ConvertProxyInterface> proxyInterface = ConvertProxyInterface.class;

            Method proxyMethod = ProxyMethodUtils.getMethod(
                proxyInterface,
                (ProxyMethodReference.WithTwoArgs<ConvertProxyInterface, String, Type, ?>)
                ConvertProxyInterface::convertToType
            );
            
            ExternalizedPropertiesInvocationHandler handler = 
                new ExternalizedPropertiesInvocationHandler(
                    rootResolver(resolver),
                    rootConverter(converter),
                    variableExpander(),
                    new ProxyMethodFactory(externalizedProperties)
                );

            Object convertedValue = handler.invoke(
                externalizedProperties.initialize(proxyInterface),
                proxyMethod,
                new Object[] {
                    "1",
                    (Type)Integer.class
                }
            );

            assertEquals(1, (Integer)convertedValue);
        }

        @Test
        @DisplayName(
            "should convert value to target type " +
            "(Proxy method return type is Object)"
        )
        void convertTest6() throws Throwable {
            Resolver resolver = new StubResolver();
            Converter<?> converter = new DefaultConverter();
            ExternalizedProperties externalizedProperties = externalizedProperties(
                resolver,
                converter
            );
            Class<ConvertProxyInterface> proxyInterface = ConvertProxyInterface.class;

            Method proxyMethod = ProxyMethodUtils.getMethod(
                proxyInterface,
                (ProxyMethodReference.WithTwoArgs<ConvertProxyInterface, String, Type, ?>)
                ConvertProxyInterface::convertToTypeObject
            );
            
            ExternalizedPropertiesInvocationHandler handler = 
                new ExternalizedPropertiesInvocationHandler(
                    rootResolver(resolver),
                    rootConverter(converter),
                    variableExpander(),
                    new ProxyMethodFactory(externalizedProperties)
                );

            Object convertedValue = handler.invoke(
                externalizedProperties.initialize(proxyInterface),
                proxyMethod,
                new Object[] {
                    "1",
                    new TypeReference<Integer>(){}.type()
                }
            );

            assertEquals(1, (Integer)convertedValue);
        }
        
        /** @ExpandVariables tests */

        @Test
        @DisplayName("should expand variables in value")
        void expandVariablesTest1() throws Throwable {
            StubResolver resolver = new StubResolver(System::getProperty);
            Converter<?> converter = new DefaultConverter();
            ExternalizedProperties externalizedProperties = externalizedProperties(
                resolver,
                converter
            );
            Class<ExpandVariablesProxyInterface> proxyInterface = 
                ExpandVariablesProxyInterface.class;

            Method proxyMethod = ProxyMethodUtils.getMethod(
                proxyInterface,
                ExpandVariablesProxyInterface::expandVariables
            );
            
            ExternalizedPropertiesInvocationHandler handler = 
                new ExternalizedPropertiesInvocationHandler(
                    rootResolver(resolver),
                    rootConverter(converter),
                    variableExpander(),
                    new ProxyMethodFactory(externalizedProperties)
                );

            Object expanded = handler.invoke(
                externalizedProperties.initialize(proxyInterface),
                proxyMethod,
                new Object[] { "${java.version}" }
            );

            assertEquals(
                resolver.valueResolver().apply("java.version"), 
                expanded
            );
        }

        @Test
        @DisplayName("should return same value when there are no variables")
        void expandVariablesTest2() throws Throwable {
            StubResolver resolver = new StubResolver(System::getProperty);
            Converter<?> converter = new DefaultConverter();
            ExternalizedProperties externalizedProperties = externalizedProperties(
                resolver,
                converter
            );
            Class<ExpandVariablesProxyInterface> proxyInterface = 
                ExpandVariablesProxyInterface.class;

            Method proxyMethod = ProxyMethodUtils.getMethod(
                proxyInterface,
                ExpandVariablesProxyInterface::expandVariables
            );

            ExternalizedPropertiesInvocationHandler handler = 
                new ExternalizedPropertiesInvocationHandler(
                    rootResolver(resolver),
                    rootConverter(converter),
                    variableExpander(),
                    new ProxyMethodFactory(externalizedProperties)
                );

            String value = "no-variables-here";
            Object expanded = handler.invoke(
                externalizedProperties.initialize(proxyInterface),
                proxyMethod,
                new Object[] { value }
            );

            assertEquals(value, expanded);
        }

        /** Re-throw exception tests. */

        @Test
        @DisplayName(
            "should rethrow same runtime exception when default interface method " + 
            "throws an exception."
        )
        void rethrowExceptionsTest1() {
            StubResolver resolver = new StubResolver(StubResolver.NULL_VALUE_RESOLVER);
            Converter<?> converter = new DefaultConverter();
            ExternalizedProperties externalizedProperties = externalizedProperties(
                resolver,
                converter
            );
            Class<ProxyInterface> proxyInterface = ProxyInterface.class;

            Method method = ProxyMethodUtils.getMethod(
                proxyInterface, 
                ProxyInterface::throwRuntimeException
            );

            ExternalizedPropertiesInvocationHandler handler = 
                new ExternalizedPropertiesInvocationHandler(
                    rootResolver(resolver),
                    rootConverter(converter),
                    variableExpander(),
                    new ProxyMethodFactory(externalizedProperties)
                );

            assertThrows(
                RuntimeException.class, 
                () -> handler.invoke(
                    externalizedProperties.initialize(proxyInterface), 
                    method,
                    new String[0]
                )
            );
        }

        @Test
        @DisplayName(
            "should wrap checked exceptions thrown by default interface method."
        )
        void rethrowExceptionsTest2() {
            StubResolver resolver = new StubResolver(StubResolver.NULL_VALUE_RESOLVER);
            Converter<?> converter = new DefaultConverter();
            ExternalizedProperties externalizedProperties = externalizedProperties(
                resolver,
                converter
            );
            Class<ProxyInterface> proxyInterface = ProxyInterface.class;

            // Can't use method ref here because of checked exception
            Method proxyMethod = ProxyMethodUtils.getMethod(
                proxyInterface, 
                "throwException"
            );

            ExternalizedPropertiesInvocationHandler handler = 
                new ExternalizedPropertiesInvocationHandler(
                    rootResolver(resolver),
                    rootConverter(converter),
                    variableExpander(),
                    new ProxyMethodFactory(externalizedProperties)
                );

            assertThrows(
                ExternalizedPropertiesException.class, 
                () -> handler.invoke(
                    externalizedProperties.initialize(proxyInterface), 
                    proxyMethod,
                    new String[0]
                )
            );
        }

        @Test
        @DisplayName("should return true when proxy object references are the same")
        void proxyEqualsTest1() throws Throwable {
            Resolver resolver = new MapResolver(Collections.emptyMap());
            ExternalizedProperties externalizedProperties = 
                externalizedProperties(resolver);

            Class<ProxyInterface> proxyInterface = ProxyInterface.class;

            Method objectEqualsMethod = ProxyMethodUtils.getMethod(
                Object.class,
                "equals",
                Object.class
            );

            ExternalizedPropertiesInvocationHandler handler = 
                new ExternalizedPropertiesInvocationHandler(
                    rootResolver(resolver),
                    rootConverter(),
                    variableExpander(),
                    new ProxyMethodFactory(externalizedProperties)
                );
            
            ProxyInterface proxy = externalizedProperties.initialize(proxyInterface);

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
        void proxyEqualsTest2() throws Throwable {
            Resolver resolver = new MapResolver(Collections.emptyMap());
            ExternalizedProperties externalizedProperties = 
                externalizedProperties(resolver);

            ProxyInterface proxy1 = 
                externalizedProperties.initialize(ProxyInterface.class);
            OtherProxyInterface proxy2 = 
                externalizedProperties.initialize(OtherProxyInterface.class);

            Method objectEqualsMethod = ProxyMethodUtils.getMethod(
                Object.class,
                "equals",
                Object.class
            );

            ExternalizedPropertiesInvocationHandler handler = 
                new ExternalizedPropertiesInvocationHandler(
                    rootResolver(resolver),
                    rootConverter(),
                    variableExpander(),
                    new ProxyMethodFactory(externalizedProperties)
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
        void proxyEqualsTest3() throws Throwable {
            Resolver resolver = new MapResolver(Collections.emptyMap());
            ExternalizedProperties externalizedProperties = 
                externalizedProperties(resolver);

            ProxyInterfaceWithEqualsMethodOverload proxy = 
                externalizedProperties.initialize(
                    ProxyInterfaceWithEqualsMethodOverload.class
                );

            Method objectEqualsMethodOverload = ProxyMethodUtils.getMethod(
                ProxyInterfaceWithEqualsMethodOverload.class,
                (ProxyMethodReference<ProxyInterfaceWithEqualsMethodOverload, Boolean>)
                ProxyInterfaceWithEqualsMethodOverload::equals
            );

            ExternalizedPropertiesInvocationHandler handler = 
                new ExternalizedPropertiesInvocationHandler(
                    rootResolver(resolver),
                    rootConverter(),
                    variableExpander(),
                    new ProxyMethodFactory(externalizedProperties)
                );

            // equals method treated as proxy method
            // instead of an Object method due to different signature.
            assertThrows(
                UnresolvedPropertiesException.class,
                () -> handler.invoke(
                    proxy, 
                    objectEqualsMethodOverload, 
                    new Object[] { proxy }
                )
            );
        }

        @Test
        @DisplayName("should return proxy's identity hash code")
        void proxyHashCodeTest1() throws Throwable {
            Resolver resolver = new MapResolver(Collections.emptyMap());
            ExternalizedProperties externalizedProperties = 
                externalizedProperties(resolver);

            ProxyInterface proxy = 
                externalizedProperties.initialize(ProxyInterface.class);

            Method objectHashCodeMethod = ProxyMethodUtils.getMethod(
                Object.class,
                "hashCode"
            );

            ExternalizedPropertiesInvocationHandler handler = 
                new ExternalizedPropertiesInvocationHandler(
                    rootResolver(resolver),
                    rootConverter(),
                    variableExpander(),
                    new ProxyMethodFactory(externalizedProperties)
                );

            Object hashCode = handler.invoke(
                proxy, 
                objectHashCodeMethod, 
                null
            );

            assertEquals(
                System.identityHashCode(proxy),
                hashCode
            );
        }

        @Test
        @DisplayName("should return standard Object.toString() format")
        void proxyToStringTest1() throws Throwable {
            Resolver resolver = new MapResolver(Collections.emptyMap());
            ExternalizedProperties externalizedProperties = 
                externalizedProperties(resolver);

            ProxyInterface proxy = 
                externalizedProperties.initialize(ProxyInterface.class);

            Method objectToStringMethod = ProxyMethodUtils.getMethod(
                Object.class,
                "toString"
            );

            ExternalizedPropertiesInvocationHandler handler = 
                new ExternalizedPropertiesInvocationHandler(
                    rootResolver(resolver),
                    rootConverter(),
                    variableExpander(),
                    new ProxyMethodFactory(externalizedProperties)
                );

            Object toStringResult = handler.invoke(
                proxy, 
                objectToStringMethod, 
                null
            );

            assertEquals(
                objectToStringMethod.invoke(proxy),
                toStringResult
            );
        }
    }

    private static ExternalizedProperties externalizedProperties(
            Resolver resolver
    ) { 
        return ExternalizedProperties.builder()
            .resolvers(resolver)
            .variableExpander(variableExpander())
            .build();
    }

    private static ExternalizedProperties externalizedProperties(
            Resolver resolver,
            Processor processor
    ) { 
        return ExternalizedProperties.builder()
            .resolvers(resolver)
            .processors(processor)
            .variableExpander(variableExpander())
            .build();
    }

    private static ExternalizedProperties externalizedProperties(
            Resolver resolver,
            Converter<?> converter
    ) { 
        return ExternalizedProperties.builder()
            .resolvers(resolver)
            .converters(converter)
            .variableExpander(variableExpander())
            .build();
    }

    private static Resolver rootResolver(
            Resolver resolver,
            Processor processor
    ) {
        return new RootResolver(
            Arrays.asList(resolver), 
            new RootProcessor(processor)
        );
    }

    private static Resolver rootResolver(Resolver... resolvers) {
        return new RootResolver(
            Arrays.asList(resolvers), 
            new RootProcessor()
        );
    }
        
    private static Converter<?> rootConverter(Converter<?>... converters) {
        return new RootConverter(converters);
    }

    private static VariableExpander variableExpander() {
        return VARIABLE_EXPANDER;
    }

    private static interface ProxyInterface {
        @ExternalizedProperty("property")
        String property();

        @ExternalizedProperty("property.with.default.value")
        default String propertyWithDefaultValueParameter(String defaultValue) {
            return defaultValue;
        }

        @ExternalizedProperty("property.with.default.value")
        default String propertyWithDefaultValue() {
            return "default.value";
        }

        // No annotation with constant default value.
        default String propertyWithNoAnnotationButWithDefaultValue() {
            return "default.value";
        }

        // No annotation but with default value parameter.
        default String propertyWithNoAnnotationButWithDefaultValueParameter(String defaultValue) {
            return defaultValue;
        }
    
        // No annotation and no default value.
        String propertyWithNoAnnotationAndNoDefaultValue();

        @ExternalizedProperty("property.int")
        int intProperty();

        @ExternalizedProperty("property-${custom.variable}")
        String variableProperty();

        @ExternalizedProperty("property.optional")
        Optional<String> optionalProperty();
    
        // No annotation ano no default value.
        Optional<String> optionalPropertyWithNoAnnotationAndNoDefaultValue();

        @ExternalizedProperty("this.will.throw")
        default String throwRuntimeException() {
            throw new RuntimeException("Oops!");
        }

        @ExternalizedProperty("this.will.throw")
        default String throwException() throws Exception {
            throw new Exception("Oops!");
        }

        @ExternalizedProperty("property.encrypted")
        @Decrypt(AES_GCM_ALGORITHM)
        String decryptedProperty();
    }

    private static interface ConvertProxyInterface {
        @Convert
        <T> T convertToTypeReference(String valueToConvert, TypeReference<T> targetType);
        @Convert
        <T> T convertToClass(String valueToConvert, Class<T> targetType);
        @Convert
        <T> T convertToType(String valueToConvert, Type targetType);
        
        @Convert
        Object convertToTypeReferenceObject(
            String valueToConvert, 
            TypeReference<?> targetType
        );
        @Convert
        Object convertToClassObject(String valueToConvert, Class<?> targetType);
        @Convert
        Object convertToTypeObject(String valueToConvert, Type targetType);
    }

    private static interface ExpandVariablesProxyInterface {
        @ExpandVariables
        String expandVariables(String value);
    }

    private static interface OtherProxyInterface {}

    private static interface ProxyInterfaceWithEqualsMethodOverload {
        boolean equals();
    }
}
