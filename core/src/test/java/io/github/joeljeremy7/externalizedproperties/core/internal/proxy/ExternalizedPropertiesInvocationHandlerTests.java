package io.github.joeljeremy7.externalizedproperties.core.internal.proxy;

import io.github.joeljeremy7.externalizedproperties.core.Converter;
import io.github.joeljeremy7.externalizedproperties.core.ConverterFacade;
import io.github.joeljeremy7.externalizedproperties.core.ExternalizedProperties;
import io.github.joeljeremy7.externalizedproperties.core.ExternalizedPropertiesException;
import io.github.joeljeremy7.externalizedproperties.core.ExternalizedProperty;
import io.github.joeljeremy7.externalizedproperties.core.Processor;
import io.github.joeljeremy7.externalizedproperties.core.Resolver;
import io.github.joeljeremy7.externalizedproperties.core.ResolverFacade;
import io.github.joeljeremy7.externalizedproperties.core.TypeReference;
import io.github.joeljeremy7.externalizedproperties.core.UnresolvedPropertyException;
import io.github.joeljeremy7.externalizedproperties.core.VariableExpander;
import io.github.joeljeremy7.externalizedproperties.core.VariableExpanderFacade;
import io.github.joeljeremy7.externalizedproperties.core.conversion.converters.DefaultConverter;
import io.github.joeljeremy7.externalizedproperties.core.conversion.converters.IntegerConverter;
import io.github.joeljeremy7.externalizedproperties.core.internal.InvocationContextFactory;
import io.github.joeljeremy7.externalizedproperties.core.internal.conversion.RootConverter;
import io.github.joeljeremy7.externalizedproperties.core.internal.processing.RootProcessor;
import io.github.joeljeremy7.externalizedproperties.core.internal.resolvers.RootResolver;
import io.github.joeljeremy7.externalizedproperties.core.processing.Decrypt;
import io.github.joeljeremy7.externalizedproperties.core.processing.processors.DecryptProcessor;
import io.github.joeljeremy7.externalizedproperties.core.processing.processors.DecryptProcessor.JceDecryptor;
import io.github.joeljeremy7.externalizedproperties.core.resolvers.MapResolver;
import io.github.joeljeremy7.externalizedproperties.core.testentities.EncryptionUtils;
import io.github.joeljeremy7.externalizedproperties.core.testfixtures.MethodUtils;
import io.github.joeljeremy7.externalizedproperties.core.testfixtures.MethodReference;
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
        @DisplayName(
            "should resolve property using specified externalized property name"
        )
        void test1() throws Throwable {
            Resolver resolver = new MapResolver("property", "test.value.1");
            ExternalizedProperties externalizedProperties = 
                externalizedProperties(resolver);

            Class<ProxyInterface> proxyInterface = ProxyInterface.class;

            Method proxyMethod = MethodUtils.getMethod(
                proxyInterface,
                ProxyInterface::property
            );

            ExternalizedPropertiesInvocationHandler handler = 
                new ExternalizedPropertiesInvocationHandler(
                    rootResolver(resolver),
                    rootConverter(),
                    variableExpander(),
                    new InvocationContextFactory(externalizedProperties)
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
            "should resolve property using method name as externalized property name"
        )
        void test2() throws Throwable {
            Resolver resolver = new MapResolver(
                "propertyWithNoAnnotationAndNoDefaultValue", 
                "test.value.1"
            );
            ExternalizedProperties externalizedProperties = 
                externalizedProperties(resolver);

            Class<ProxyInterface> proxyInterface = ProxyInterface.class;

            Method proxyMethod = MethodUtils.getMethod(
                proxyInterface,
                ProxyInterface::propertyWithNoAnnotationAndNoDefaultValue
            );

            ExternalizedPropertiesInvocationHandler handler = 
                new ExternalizedPropertiesInvocationHandler(
                    rootResolver(resolver),
                    rootConverter(),
                    variableExpander(),
                    new InvocationContextFactory(externalizedProperties)
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
            "should resolve property from resolver and not from default interface method value"
        )
        void test3() throws Throwable {
            Resolver resolver = new MapResolver(
                "property.with.default.value", 
                "test.value"
            );
            ExternalizedProperties externalizedProperties = 
                externalizedProperties(resolver);

            Class<ProxyInterface> proxyInterface = ProxyInterface.class;
            
            Method proxyMethod = MethodUtils.getMethod(
                proxyInterface,
                ProxyInterface::propertyWithDefaultValue
            );

            ExternalizedPropertiesInvocationHandler handler = 
                new ExternalizedPropertiesInvocationHandler(
                    rootResolver(resolver),
                    rootConverter(),
                    variableExpander(),
                    new InvocationContextFactory(externalizedProperties)
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
            "should resolve property from resolver and not from default interface method value parameter"
        )
        void test4() throws Throwable {
            Resolver resolver = new MapResolver(
                "property.with.default.value", 
                "test.value"
            );
            ExternalizedProperties externalizedProperties = 
                externalizedProperties(resolver);

            Class<ProxyInterface> proxyInterface = ProxyInterface.class;

            Method proxyMethod = MethodUtils.getMethod(
                proxyInterface,
                ProxyInterface::propertyWithDefaultValueParameter
            );

            ExternalizedPropertiesInvocationHandler handler = 
                new ExternalizedPropertiesInvocationHandler(
                    rootResolver(resolver),
                    rootConverter(),
                    variableExpander(),
                    new InvocationContextFactory(externalizedProperties)
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
        void test5() throws Throwable {
            Resolver resolver = new MapResolver(Collections.emptyMap());
            ExternalizedProperties externalizedProperties = 
                externalizedProperties(resolver);

            Class<ProxyInterface> proxyInterface = ProxyInterface.class;

            Method proxyMethod = MethodUtils.getMethod(
                proxyInterface,
                ProxyInterface::propertyWithDefaultValue
            );

            ExternalizedPropertiesInvocationHandler handler = 
                new ExternalizedPropertiesInvocationHandler(
                    rootResolver(resolver),
                    rootConverter(),
                    variableExpander(),
                    new InvocationContextFactory(externalizedProperties)
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
        void test6() throws Throwable {
            Resolver resolver = new MapResolver(Collections.emptyMap());
            ExternalizedProperties externalizedProperties = 
                externalizedProperties(resolver);

            Class<ProxyInterface> proxyInterface = ProxyInterface.class;

            Method proxyMethod = MethodUtils.getMethod(
                proxyInterface,
                ProxyInterface::propertyWithDefaultValueParameter
            );

            ExternalizedPropertiesInvocationHandler handler = 
                new ExternalizedPropertiesInvocationHandler(
                    rootResolver(resolver),
                    rootConverter(),
                    variableExpander(),
                    new InvocationContextFactory(externalizedProperties)
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
        void test7() throws Throwable {
            Resolver resolver = new MapResolver(Collections.emptyMap());
            ExternalizedProperties externalizedProperties = 
                externalizedProperties(resolver);

            Class<ProxyInterface> proxyInterface = ProxyInterface.class;

            Method proxyMethod = MethodUtils.getMethod(
                proxyInterface,
                ProxyInterface::propertyWithNoAnnotationButWithDefaultValue
            );

            ExternalizedPropertiesInvocationHandler handler = 
                new ExternalizedPropertiesInvocationHandler(
                    rootResolver(resolver),
                    rootConverter(),
                    variableExpander(),
                    new InvocationContextFactory(externalizedProperties)
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
        void test8() throws Throwable {
            Resolver resolver = new MapResolver(Collections.emptyMap());
            ExternalizedProperties externalizedProperties = 
                externalizedProperties(resolver);

            Class<ProxyInterface> proxyInterface = ProxyInterface.class;

            Method proxyMethod = MethodUtils.getMethod(
                proxyInterface,
                ProxyInterface::propertyWithNoAnnotationButWithDefaultValueParameter
            );

            ExternalizedPropertiesInvocationHandler handler = 
                new ExternalizedPropertiesInvocationHandler(
                    rootResolver(resolver),
                    rootConverter(),
                    variableExpander(),
                    new InvocationContextFactory(externalizedProperties)
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
        void test9() {
            Resolver resolver = new MapResolver(Collections.emptyMap());
            ExternalizedProperties externalizedProperties = 
                externalizedProperties(resolver);

            Class<ProxyInterface> proxyInterface = ProxyInterface.class;

            Method proxyMethod = MethodUtils.getMethod(
                proxyInterface,
                ProxyInterface::property
            );

            ExternalizedPropertiesInvocationHandler handler = 
                new ExternalizedPropertiesInvocationHandler(
                    rootResolver(resolver),
                    rootConverter(),
                    variableExpander(),
                    new InvocationContextFactory(externalizedProperties)
                );

            Object proxy = externalizedProperties.initialize(proxyInterface);

            assertThrows(UnresolvedPropertyException.class, () -> {
                handler.invoke(
                    proxy, 
                    proxyMethod, 
                    null
                );
            });
        }

        @Test
        @DisplayName("should throw when an unannotated non-Optional property cannot be resolved.")
        void test10() {
            Resolver resolver = new MapResolver(Collections.emptyMap());
            ExternalizedProperties externalizedProperties = 
                externalizedProperties(resolver);

            Class<ProxyInterface> proxyInterface = ProxyInterface.class;

            Method proxyMethod = MethodUtils.getMethod(
                proxyInterface,
                ProxyInterface::propertyWithNoAnnotationAndNoDefaultValue
            );

            ExternalizedPropertiesInvocationHandler handler = 
                new ExternalizedPropertiesInvocationHandler(
                    rootResolver(resolver),
                    rootConverter(),
                    variableExpander(),
                    new InvocationContextFactory(externalizedProperties)
                );

            Object proxy = externalizedProperties.initialize(proxyInterface);

            assertThrows(UnresolvedPropertyException.class, () -> {
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
        void test11() throws Throwable {
            Resolver resolver = new MapResolver(Collections.emptyMap());
            ExternalizedProperties externalizedProperties = 
                externalizedProperties(resolver);

            Class<ProxyInterface> proxyInterface = ProxyInterface.class;

            Method proxyMethod = MethodUtils.getMethod(
                proxyInterface,
                ProxyInterface::optionalProperty
            );

            ExternalizedPropertiesInvocationHandler handler = 
                new ExternalizedPropertiesInvocationHandler(
                    rootResolver(resolver),
                    rootConverter(),
                    variableExpander(),
                    new InvocationContextFactory(externalizedProperties)
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
        void test12() throws Throwable {
            Resolver resolver = new MapResolver(Collections.emptyMap());
            ExternalizedProperties externalizedProperties = 
                externalizedProperties(resolver);

            Class<ProxyInterface> proxyInterface = ProxyInterface.class;

            Method proxyMethod = MethodUtils.getMethod(
                proxyInterface,
                ProxyInterface::optionalPropertyWithNoAnnotationAndNoDefaultValue
            );

            ExternalizedPropertiesInvocationHandler handler = 
                new ExternalizedPropertiesInvocationHandler(
                    rootResolver(resolver),
                    rootConverter(),
                    variableExpander(),
                    new InvocationContextFactory(externalizedProperties)
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
            Converter<?> converter = new IntegerConverter();
            ExternalizedProperties externalizedProperties = externalizedProperties(
                resolver,
                converter
            );

            Class<ProxyInterface> proxyInterface = ProxyInterface.class;
            
            Method proxyMethod = MethodUtils.getMethod(
                proxyInterface,
                ProxyInterface::intProperty
            );

            ExternalizedPropertiesInvocationHandler handler = 
                new ExternalizedPropertiesInvocationHandler(
                    rootResolver(resolver),
                    rootConverter(converter),
                    variableExpander(),
                    new InvocationContextFactory(externalizedProperties)
                );

            Object intResult = handler.invoke(
                externalizedProperties.initialize(proxyInterface), 
                proxyMethod,
                null
            );

            assertEquals(1, (int)intResult);
        }

        /**
         * Processing tests.
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
            
            Method proxyMethod = MethodUtils.getMethod(
                proxyInterface,
                ProxyInterface::decryptedProperty
            );

            ExternalizedPropertiesInvocationHandler handler = 
                new ExternalizedPropertiesInvocationHandler(
                    rootResolver(resolver, decryptProcessor),
                    rootConverter(),
                    variableExpander(),
                    new InvocationContextFactory(externalizedProperties)
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

            Method proxyMethod = MethodUtils.getMethod(
                proxyInterface,
                ProxyInterface::variableProperty
            );

            ExternalizedPropertiesInvocationHandler handler = 
                new ExternalizedPropertiesInvocationHandler(
                    rootResolver(resolver),
                    rootConverter(),
                    variableExpander(),
                    new InvocationContextFactory(externalizedProperties)
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

            Method proxyMethod = MethodUtils.getMethod(
                proxyInterface,
                ProxyInterface::variableProperty
            );

            ExternalizedPropertiesInvocationHandler handler = 
                new ExternalizedPropertiesInvocationHandler(
                    rootResolver(resolver),
                    rootConverter(),
                    variableExpander(),
                    new InvocationContextFactory(externalizedProperties)
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
         * @ResolverFacade tests
         */
        
        @Test
        @DisplayName("should resolve property")
        void resolverFacadeTest1() throws Throwable {
            StubResolver resolver = new StubResolver();
            Converter<?> converter = new DefaultConverter();
            ExternalizedProperties externalizedProperties = externalizedProperties(
                resolver,
                converter
            );
            Class<ResolverFacadeProxyInterface> proxyInterface = ResolverFacadeProxyInterface.class;

            Method proxyMethod = MethodUtils.getMethod(
                proxyInterface,
                (MethodReference.WithOneArg<ResolverFacadeProxyInterface, String, ?>)
                ResolverFacadeProxyInterface::resolveString
            );
            
            ExternalizedPropertiesInvocationHandler handler = 
                new ExternalizedPropertiesInvocationHandler(
                    rootResolver(resolver),
                    rootConverter(converter),
                    variableExpander(),
                    new InvocationContextFactory(externalizedProperties)
                );

            Object resolved = handler.invoke(
                externalizedProperties.initialize(proxyInterface), 
                proxyMethod, 
                new Object[] {
                    "property"
                }
            );

            assertEquals(
                resolver.valueResolver().apply("property"), 
                resolved
            );
        }
        
        @Test
        @DisplayName("should resolve property and convert to target type reference")
        void resolverFacadeTest2() throws Throwable {
            // Always returns "1".
            Resolver resolver = new StubResolver(pn -> "1");
            Converter<?> converter = new DefaultConverter();
            ExternalizedProperties externalizedProperties = externalizedProperties(
                resolver,
                converter
            );
            Class<ResolverFacadeProxyInterface> proxyInterface = ResolverFacadeProxyInterface.class;

            Method proxyMethod = MethodUtils.getMethod(
                proxyInterface,
                (MethodReference.WithTwoArgs<ResolverFacadeProxyInterface, String, TypeReference<?>, ?>)
                ResolverFacadeProxyInterface::resolveWithTargetTypeReference
            );
            
            ExternalizedPropertiesInvocationHandler handler = 
                new ExternalizedPropertiesInvocationHandler(
                    rootResolver(resolver),
                    rootConverter(converter),
                    variableExpander(),
                    new InvocationContextFactory(externalizedProperties)
                );

            Object resolved = handler.invoke(
                externalizedProperties.initialize(proxyInterface), 
                proxyMethod, 
                new Object[] {
                    "property",
                    new TypeReference<Integer>(){}
                }
            );

            assertEquals(1, (Integer)resolved);
        }

        @Test
        @DisplayName(
            "should resolve property and convert to target type reference " +
            "(Proxy method return type is Object)"
        )
        void resolverFacadeTest3() throws Throwable {
            // Always returns "1".
            Resolver resolver = new StubResolver(pn -> "1");
            Converter<?> converter = new DefaultConverter();
            ExternalizedProperties externalizedProperties = externalizedProperties(
                resolver,
                converter
            );
            Class<ResolverFacadeProxyInterface> proxyInterface = ResolverFacadeProxyInterface.class;

            Method proxyMethod = MethodUtils.getMethod(
                proxyInterface,
                (MethodReference.WithTwoArgs<ResolverFacadeProxyInterface, String, TypeReference<?>, ?>)
                ResolverFacadeProxyInterface::resolveWithTargetTypeReference
            );

            ExternalizedPropertiesInvocationHandler handler = 
                new ExternalizedPropertiesInvocationHandler(
                    rootResolver(resolver),
                    rootConverter(converter),
                    variableExpander(),
                    new InvocationContextFactory(externalizedProperties)
                );

            Object resolved = handler.invoke(
                externalizedProperties.initialize(proxyInterface),
                proxyMethod,
                new Object[] {
                    "property",
                    new TypeReference<Integer>(){}
                }
            );

            assertEquals(1, (Integer)resolved);
        }

        @Test
        @DisplayName("should resolve property and convert to target class")
        void resolverFacadeTest4() throws Throwable {
            // Always returns "1".
            Resolver resolver = new StubResolver(pn -> "1");
            Converter<?> converter = new DefaultConverter();
            ExternalizedProperties externalizedProperties = externalizedProperties(
                resolver,
                converter
            );
            Class<ResolverFacadeProxyInterface> proxyInterface = ResolverFacadeProxyInterface.class;

            Method method = MethodUtils.getMethod(
                proxyInterface,
                (MethodReference.WithTwoArgs<ResolverFacadeProxyInterface, String, Class<?>, ?>)
                ResolverFacadeProxyInterface::resolveWithTargetClass
            );
            
            ExternalizedPropertiesInvocationHandler handler = 
                new ExternalizedPropertiesInvocationHandler(
                    rootResolver(resolver),
                    rootConverter(converter),
                    variableExpander(),
                    new InvocationContextFactory(externalizedProperties)
                );

            Object resolved = handler.invoke(
                externalizedProperties.initialize(proxyInterface),
                method,
                new Object[] {
                    "property",
                    Integer.class
                }
            );

            assertEquals(1, (Integer)resolved);
        }

        @Test
        @DisplayName(
            "should resolve property and convert to target class " +
            "(Proxy method return type is Object)"
        )
        void resolverFacadeTest5() throws Throwable {
            // Always returns "1".
            Resolver resolver = new StubResolver(pn -> "1");
            Converter<?> converter = new DefaultConverter();
            ExternalizedProperties externalizedProperties = externalizedProperties(
                resolver,
                converter
            );
            Class<ResolverFacadeProxyInterface> proxyInterface = ResolverFacadeProxyInterface.class;

            Method proxyMethod = MethodUtils.getMethod(
                proxyInterface,
                (MethodReference.WithTwoArgs<ResolverFacadeProxyInterface, String, Class<?>, ?>)
                ResolverFacadeProxyInterface::resolveWithTargetClass
            );
            
            ExternalizedPropertiesInvocationHandler handler = 
                new ExternalizedPropertiesInvocationHandler(
                    rootResolver(resolver),
                    rootConverter(converter),
                    variableExpander(),
                    new InvocationContextFactory(externalizedProperties)
                );

            Object resolved = handler.invoke(
                externalizedProperties.initialize(proxyInterface),
                proxyMethod,
                new Object[] {
                    "property",
                    Integer.class
                }
            );

            assertEquals(1, (Integer)resolved);
        }

        @Test
        @DisplayName("should resolve property and convert to target type")
        void resolverFacadeTest6() throws Throwable {
            // Always returns "1".
            Resolver resolver = new StubResolver(pn -> "1");
            Converter<?> converter = new DefaultConverter();
            ExternalizedProperties externalizedProperties = externalizedProperties(
                resolver,
                converter
            );
            Class<ResolverFacadeProxyInterface> proxyInterface = ResolverFacadeProxyInterface.class;

            Method proxyMethod = MethodUtils.getMethod(
                proxyInterface,
                (MethodReference.WithTwoArgs<ResolverFacadeProxyInterface, String, Type, ?>)
                ResolverFacadeProxyInterface::resolveWithTargetType
            );
            
            ExternalizedPropertiesInvocationHandler handler = 
                new ExternalizedPropertiesInvocationHandler(
                    rootResolver(resolver),
                    rootConverter(converter),
                    variableExpander(),
                    new InvocationContextFactory(externalizedProperties)
                );

            Object resolved = handler.invoke(
                externalizedProperties.initialize(proxyInterface),
                proxyMethod,
                new Object[] {
                    "property",
                    (Type)Integer.class
                }
            );

            assertEquals(1, (Integer)resolved);
        }

        @Test
        @DisplayName(
            "should resolve property and convert to target type " +
            "(Proxy method return type is Object)"
        )
        void resolverFacadeTest7() throws Throwable {
            // Always returns "1".
            Resolver resolver = new StubResolver(pn -> "1");
            Converter<?> converter = new DefaultConverter();
            ExternalizedProperties externalizedProperties = externalizedProperties(
                resolver,
                converter
            );
            Class<ResolverFacadeProxyInterface> proxyInterface = ResolverFacadeProxyInterface.class;

            Method proxyMethod = MethodUtils.getMethod(
                proxyInterface,
                (MethodReference.WithTwoArgs<ResolverFacadeProxyInterface, String, Type, ?>)
                ResolverFacadeProxyInterface::resolveWithTargetType
            );
            
            ExternalizedPropertiesInvocationHandler handler = 
                new ExternalizedPropertiesInvocationHandler(
                    rootResolver(resolver),
                    rootConverter(converter),
                    variableExpander(),
                    new InvocationContextFactory(externalizedProperties)
                );

            Object resolved = handler.invoke(
                externalizedProperties.initialize(proxyInterface),
                proxyMethod,
                new Object[] {
                    "property",
                    new TypeReference<Integer>(){}.type()
                }
            );

            assertEquals(1, (Integer)resolved);
        }

        @Test
        @DisplayName("should resolve property and convert to proxy method return type")
        void resolverFacadeTest8() throws Throwable {
            // Always returns "1".
            Resolver resolver = new StubResolver(pn -> "1");
            Converter<?> converter = new DefaultConverter();
            ExternalizedProperties externalizedProperties = externalizedProperties(
                resolver,
                converter
            );
            Class<ResolverFacadeProxyInterface> proxyInterface = ResolverFacadeProxyInterface.class;

            Method proxyMethod = MethodUtils.getMethod(
                proxyInterface,
                ResolverFacadeProxyInterface::resolveInt
            );
            
            ExternalizedPropertiesInvocationHandler handler = 
                new ExternalizedPropertiesInvocationHandler(
                    rootResolver(resolver),
                    rootConverter(converter),
                    variableExpander(),
                    new InvocationContextFactory(externalizedProperties)
                );

            Object resolved = handler.invoke(
                externalizedProperties.initialize(proxyInterface),
                proxyMethod,
                new Object[] {
                    "property"
                }
            );

            assertEquals(1, (Integer)resolved);
        }

        /**
         * @ConverterFacade tests
         */
        
        @Test
        @DisplayName("should convert value to target type reference")
        void converterFacadeTest1() throws Throwable {
            Resolver resolver = new StubResolver();
            Converter<?> converter = new DefaultConverter();
            ExternalizedProperties externalizedProperties = externalizedProperties(
                resolver,
                converter
            );
            Class<ConverterFacadeProxyInterface> proxyInterface = ConverterFacadeProxyInterface.class;

            Method proxyMethod = MethodUtils.getMethod(
                proxyInterface,
                (MethodReference.WithTwoArgs<ConverterFacadeProxyInterface, String, TypeReference<?>, ?>)
                ConverterFacadeProxyInterface::convertToTypeReference
            );
            
            ExternalizedPropertiesInvocationHandler handler = 
                new ExternalizedPropertiesInvocationHandler(
                    rootResolver(resolver),
                    rootConverter(converter),
                    variableExpander(),
                    new InvocationContextFactory(externalizedProperties)
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
        void converterFacadeTest2() throws Throwable {
            Resolver resolver = new StubResolver();
            Converter<?> converter = new DefaultConverter();
            ExternalizedProperties externalizedProperties = externalizedProperties(
                resolver,
                converter
            );
            Class<ConverterFacadeProxyInterface> proxyInterface = ConverterFacadeProxyInterface.class;

            Method proxyMethod = MethodUtils.getMethod(
                proxyInterface,
                (MethodReference.WithTwoArgs<ConverterFacadeProxyInterface, String, TypeReference<?>, ?>)
                ConverterFacadeProxyInterface::convertToTypeReferenceObject
            );

            ExternalizedPropertiesInvocationHandler handler = 
                new ExternalizedPropertiesInvocationHandler(
                    rootResolver(resolver),
                    rootConverter(converter),
                    variableExpander(),
                    new InvocationContextFactory(externalizedProperties)
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
        void converterFacadeTest3() throws Throwable {
            Resolver resolver = new StubResolver();
            Converter<?> converter = new DefaultConverter();
            ExternalizedProperties externalizedProperties = externalizedProperties(
                resolver,
                converter
            );
            Class<ConverterFacadeProxyInterface> proxyInterface = ConverterFacadeProxyInterface.class;

            Method method = MethodUtils.getMethod(
                proxyInterface,
                (MethodReference.WithTwoArgs<ConverterFacadeProxyInterface, String, Class<?>, ?>)
                ConverterFacadeProxyInterface::convertToClass
            );
            
            ExternalizedPropertiesInvocationHandler handler = 
                new ExternalizedPropertiesInvocationHandler(
                    rootResolver(resolver),
                    rootConverter(converter),
                    variableExpander(),
                    new InvocationContextFactory(externalizedProperties)
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
        void converterFacadeTest4() throws Throwable {
            Resolver resolver = new StubResolver();
            Converter<?> converter = new DefaultConverter();
            ExternalizedProperties externalizedProperties = externalizedProperties(
                resolver,
                converter
            );
            Class<ConverterFacadeProxyInterface> proxyInterface = ConverterFacadeProxyInterface.class;

            Method proxyMethod = MethodUtils.getMethod(
                proxyInterface,
                (MethodReference.WithTwoArgs<ConverterFacadeProxyInterface, String, Class<?>, ?>)
                ConverterFacadeProxyInterface::convertToClassObject
            );
            
            ExternalizedPropertiesInvocationHandler handler = 
                new ExternalizedPropertiesInvocationHandler(
                    rootResolver(resolver),
                    rootConverter(converter),
                    variableExpander(),
                    new InvocationContextFactory(externalizedProperties)
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
        void converterFacadeTest5() throws Throwable {
            Resolver resolver = new StubResolver();
            Converter<?> converter = new DefaultConverter();
            ExternalizedProperties externalizedProperties = externalizedProperties(
                resolver,
                converter
            );
            Class<ConverterFacadeProxyInterface> proxyInterface = ConverterFacadeProxyInterface.class;

            Method proxyMethod = MethodUtils.getMethod(
                proxyInterface,
                (MethodReference.WithTwoArgs<ConverterFacadeProxyInterface, String, Type, ?>)
                ConverterFacadeProxyInterface::convertToType
            );
            
            ExternalizedPropertiesInvocationHandler handler = 
                new ExternalizedPropertiesInvocationHandler(
                    rootResolver(resolver),
                    rootConverter(converter),
                    variableExpander(),
                    new InvocationContextFactory(externalizedProperties)
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
        void converterFacadeTest6() throws Throwable {
            Resolver resolver = new StubResolver();
            Converter<?> converter = new DefaultConverter();
            ExternalizedProperties externalizedProperties = externalizedProperties(
                resolver,
                converter
            );
            Class<ConverterFacadeProxyInterface> proxyInterface = ConverterFacadeProxyInterface.class;

            Method proxyMethod = MethodUtils.getMethod(
                proxyInterface,
                (MethodReference.WithTwoArgs<ConverterFacadeProxyInterface, String, Type, ?>)
                ConverterFacadeProxyInterface::convertToTypeObject
            );
            
            ExternalizedPropertiesInvocationHandler handler = 
                new ExternalizedPropertiesInvocationHandler(
                    rootResolver(resolver),
                    rootConverter(converter),
                    variableExpander(),
                    new InvocationContextFactory(externalizedProperties)
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

        @Test
        @DisplayName("should convert value to proxy method return type")
        void converterFacadeTest7() throws Throwable {
            Resolver resolver = new StubResolver();
            Converter<?> converter = new DefaultConverter();
            ExternalizedProperties externalizedProperties = externalizedProperties(
                resolver,
                converter
            );
            Class<ConverterFacadeProxyInterface> proxyInterface = ConverterFacadeProxyInterface.class;

            Method proxyMethod = MethodUtils.getMethod(
                proxyInterface,
                ConverterFacadeProxyInterface::convertToInt
            );
            
            ExternalizedPropertiesInvocationHandler handler = 
                new ExternalizedPropertiesInvocationHandler(
                    rootResolver(resolver),
                    rootConverter(converter),
                    variableExpander(),
                    new InvocationContextFactory(externalizedProperties)
                );

            Object convertedValue = handler.invoke(
                externalizedProperties.initialize(proxyInterface),
                proxyMethod,
                new Object[] {
                    "1"
                }
            );

            assertEquals(1, (Integer)convertedValue);
        }
        
        /**
         *  @VariableExpanderFacade tests 
         */

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

            Method proxyMethod = MethodUtils.getMethod(
                proxyInterface,
                ExpandVariablesProxyInterface::expandVariables
            );
            
            ExternalizedPropertiesInvocationHandler handler = 
                new ExternalizedPropertiesInvocationHandler(
                    rootResolver(resolver),
                    rootConverter(converter),
                    variableExpander(),
                    new InvocationContextFactory(externalizedProperties)
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

            Method proxyMethod = MethodUtils.getMethod(
                proxyInterface,
                ExpandVariablesProxyInterface::expandVariables
            );

            ExternalizedPropertiesInvocationHandler handler = 
                new ExternalizedPropertiesInvocationHandler(
                    rootResolver(resolver),
                    rootConverter(converter),
                    variableExpander(),
                    new InvocationContextFactory(externalizedProperties)
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

            Method method = MethodUtils.getMethod(
                proxyInterface, 
                ProxyInterface::throwRuntimeException
            );

            ExternalizedPropertiesInvocationHandler handler = 
                new ExternalizedPropertiesInvocationHandler(
                    rootResolver(resolver),
                    rootConverter(converter),
                    variableExpander(),
                    new InvocationContextFactory(externalizedProperties)
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
            Method proxyMethod = MethodUtils.getMethod(
                proxyInterface, 
                "throwException"
            );

            ExternalizedPropertiesInvocationHandler handler = 
                new ExternalizedPropertiesInvocationHandler(
                    rootResolver(resolver),
                    rootConverter(converter),
                    variableExpander(),
                    new InvocationContextFactory(externalizedProperties)
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

            Method objectEqualsMethod = MethodUtils.getMethod(
                Object.class,
                "equals",
                Object.class
            );

            ExternalizedPropertiesInvocationHandler handler = 
                new ExternalizedPropertiesInvocationHandler(
                    rootResolver(resolver),
                    rootConverter(),
                    variableExpander(),
                    new InvocationContextFactory(externalizedProperties)
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

            Method objectEqualsMethod = MethodUtils.getMethod(
                Object.class,
                "equals",
                Object.class
            );

            ExternalizedPropertiesInvocationHandler handler = 
                new ExternalizedPropertiesInvocationHandler(
                    rootResolver(resolver),
                    rootConverter(),
                    variableExpander(),
                    new InvocationContextFactory(externalizedProperties)
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

            EqualsMethodOverloadProxyInterface proxy = 
                externalizedProperties.initialize(
                    EqualsMethodOverloadProxyInterface.class
                );

            Method objectEqualsMethodOverload = MethodUtils.getMethod(
                EqualsMethodOverloadProxyInterface.class,
                (MethodReference<EqualsMethodOverloadProxyInterface, Boolean>)
                EqualsMethodOverloadProxyInterface::equals
            );

            ExternalizedPropertiesInvocationHandler handler = 
                new ExternalizedPropertiesInvocationHandler(
                    rootResolver(resolver),
                    rootConverter(),
                    variableExpander(),
                    new InvocationContextFactory(externalizedProperties)
                );

            // equals method treated as proxy method
            // instead of an Object method due to different signature.
            assertThrows(
                UnresolvedPropertyException.class,
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

            Method objectHashCodeMethod = MethodUtils.getMethod(
                Object.class,
                "hashCode"
            );

            ExternalizedPropertiesInvocationHandler handler = 
                new ExternalizedPropertiesInvocationHandler(
                    rootResolver(resolver),
                    rootConverter(),
                    variableExpander(),
                    new InvocationContextFactory(externalizedProperties)
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

            Method objectToStringMethod = MethodUtils.getMethod(
                Object.class,
                "toString"
            );

            ExternalizedPropertiesInvocationHandler handler = 
                new ExternalizedPropertiesInvocationHandler(
                    rootResolver(resolver),
                    rootConverter(),
                    variableExpander(),
                    new InvocationContextFactory(externalizedProperties)
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

    private static interface ResolverFacadeProxyInterface {
        @ResolverFacade
        String resolveString(String propertyName);
        @ResolverFacade
        int resolveInt(String propertyName);
        @ResolverFacade
        <T> T resolveWithTargetTypeReference(String propertyName, TypeReference<T> targetType);
        @ResolverFacade
        <T> T resolveWithTargetClass(String propertyName, Class<T> targetType);
        @ResolverFacade
        Object resolveWithTargetType(String propertyName, Type targetType);
    }

    private static interface ConverterFacadeProxyInterface {
        @ConverterFacade
        <T> T convertToTypeReference(String valueToConvert, TypeReference<T> targetType);
        @ConverterFacade
        <T> T convertToClass(String valueToConvert, Class<T> targetType);
        @ConverterFacade
        <T> T convertToType(String valueToConvert, Type targetType);
        
        @ConverterFacade
        Object convertToTypeReferenceObject(
            String valueToConvert, 
            TypeReference<?> targetType
        );
        @ConverterFacade
        Object convertToClassObject(String valueToConvert, Class<?> targetType);
        @ConverterFacade
        Object convertToTypeObject(String valueToConvert, Type targetType);
        @ConverterFacade
        int convertToInt(String valueToConvert);
    }

    private static interface ExpandVariablesProxyInterface {
        @VariableExpanderFacade
        String expandVariables(String value);
    }

    private static interface OtherProxyInterface {}

    private static interface EqualsMethodOverloadProxyInterface {
        boolean equals();
    }
}
