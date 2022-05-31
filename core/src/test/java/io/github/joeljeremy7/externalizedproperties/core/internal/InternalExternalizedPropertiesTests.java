package io.github.joeljeremy7.externalizedproperties.core.internal;

import io.github.joeljeremy7.externalizedproperties.core.Convert;
import io.github.joeljeremy7.externalizedproperties.core.ExpandVariables;
import io.github.joeljeremy7.externalizedproperties.core.ExternalizedProperty;
import io.github.joeljeremy7.externalizedproperties.core.Resolver;
import io.github.joeljeremy7.externalizedproperties.core.TypeReference;
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
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

public class InternalExternalizedPropertiesTests {
    @Nested
    class InitializeMethod {
        @Test
        @DisplayName("should throw when proxy interface argument is null")
        void test1() {
            // Do not resolve any property.
            Resolver resolver = new StubResolver(
                StubResolver.NULL_VALUE_RESOLVER
            );
            
            InternalExternalizedProperties externalizedProperties = 
                internalExternalizedProperties(resolver);

            assertThrows(
                IllegalArgumentException.class, 
                () -> externalizedProperties.initialize(null) 
            );
        }

        @Test
        @DisplayName("should throw when proxy interface argument is not an interface")
        void test2() {
            InternalExternalizedProperties externalizedProperties = 
                internalExternalizedProperties(new StubResolver());

            assertThrows(
                IllegalArgumentException.class, 
                () -> externalizedProperties.initialize(
                    InternalExternalizedPropertiesTests.class
                )
            );
        }

        @Test
        @DisplayName("should throw when proxy interface contains void-returning methods")
        void test3() {
            InternalExternalizedProperties externalizedProperties = 
                internalExternalizedProperties(new StubResolver());

            assertThrows(
                IllegalArgumentException.class, 
                () -> externalizedProperties.initialize(
                    VoidReturnTypeProxyInterface.class
                )
            );
        }

        @Test
        @DisplayName("should throw when proxy interface contains Void-returning methods")
        void test4() {
            InternalExternalizedProperties externalizedProperties = 
                internalExternalizedProperties(new StubResolver());

            assertThrows(
                IllegalArgumentException.class, 
                () -> externalizedProperties.initialize(
                    VoidClassReturnTypeProxyInterface.class
                )
            );
        }

        @Test
        @DisplayName(
            "should throw when @ExternalizedProperty (no value) proxy method does not have " +
            "a single String argument"
        )
        void test5() {
            InternalExternalizedProperties externalizedProperties = 
                internalExternalizedProperties(new StubResolver());

            assertThrows(
                IllegalArgumentException.class, 
                () -> externalizedProperties.initialize(
                    NoStringArgProxyInterface.class
                )
            );
        }

        @Test
        @DisplayName(
            "should throw when @ExternalizedProperty (no value) proxy method have " +
            "multiple arguments"
        )
        void test6() {
            InternalExternalizedProperties externalizedProperties = 
                internalExternalizedProperties(new StubResolver());

            assertThrows(
                IllegalArgumentException.class, 
                () -> externalizedProperties.initialize(
                    MultipleArgsProxyInterface.class
                )
            );
        }

        @Test
        @DisplayName(
            "should throw when @ExternalizedProperty (no value) proxy method have " +
            "a non-String argument"
        )
        void test7() {
            InternalExternalizedProperties externalizedProperties = 
                internalExternalizedProperties(new StubResolver());

            assertThrows(
                IllegalArgumentException.class, 
                () -> externalizedProperties.initialize(
                    InvalidArgTypeProxyInterface.class
                )
            );
        }

        @Test
        @DisplayName(
            "should throw when @Convert proxy method have invalid " +
            "method parameter types"
        )
        void test8() {
            InternalExternalizedProperties externalizedProperties = 
                internalExternalizedProperties(new StubResolver());

            assertThrows(
                IllegalArgumentException.class, 
                () -> externalizedProperties.initialize(
                    InvalidArgsConvertProxy.class
                )
            );
        }

        @Test
        @DisplayName(
            "should throw when @Convert proxy method have no " +
            "method parameters"
        )
        void test9() {
            InternalExternalizedProperties externalizedProperties = 
                internalExternalizedProperties(new StubResolver());

            assertThrows(
                IllegalArgumentException.class, 
                () -> externalizedProperties.initialize(
                    NoArgsConvertProxy.class
                )
            );
        }

        @Test
        @DisplayName(
            "should throw when @Convert proxy method does not have " +
            "2 method parameters"
        )
        void test10() {
            InternalExternalizedProperties externalizedProperties = 
                internalExternalizedProperties(new StubResolver());

            assertThrows(
                IllegalArgumentException.class, 
                () -> externalizedProperties.initialize(
                    SingleArgConvertProxy.class
                )
            );
        }

        @Test
        @DisplayName(
            "should throw when @Convert proxy method first parameter " + 
            "is not a String (value to convert)"
        )
        void test11() {
            InternalExternalizedProperties externalizedProperties = 
                internalExternalizedProperties(new StubResolver());

            assertThrows(
                IllegalArgumentException.class, 
                () -> externalizedProperties.initialize(
                    InvalidFirstArgTypeConvertProxy.class
                )
            );
        }

        @Test
        @DisplayName(
            "should throw when @Convert proxy method second parameter " + 
            "is not the target type"
        )
        void test12() {
            InternalExternalizedProperties externalizedProperties = 
                internalExternalizedProperties(new StubResolver());

            assertThrows(
                IllegalArgumentException.class, 
                () -> externalizedProperties.initialize(
                    InvalidSecondArgTypeConvertProxy.class
                )
            );
        }

        @Test
        @DisplayName(
            "should initialize a proxy that handles @ExternalizedProperty annotations"
        )
        void test13() {
            StubResolver resolver = new StubResolver();
            
            InternalExternalizedProperties externalizedProperties = 
                internalExternalizedProperties(resolver);

            ProxyInterface proxy = externalizedProperties.initialize(
                ProxyInterface.class
            );

            assertNotNull(proxy);
            assertTrue(proxy instanceof Proxy);

            assertEquals(
                resolver.valueResolver().apply("property"),
                proxy.property()
            );
        }

        @Test
        @DisplayName("should initialize a proxy that handles @Convert annotations")
        void test14() {
            Resolver resolver = new StubResolver();
            
            InternalExternalizedProperties externalizedProperties = 
                internalExternalizedProperties(resolver);

            ConvertProxy proxy = externalizedProperties.initialize(
                ConvertProxy.class
            );

            assertNotNull(proxy);
            assertTrue(proxy instanceof Proxy);

            // With target type reference
            assertEquals(
                1, 
                (Integer)proxy.convertToTargetTypeReference(
                    "1", 
                    new TypeReference<Integer>(){}
                )
            );

            // With target class
            assertEquals(
                1, 
                (Integer)proxy.convertToTargetClass(
                    "1", 
                    Integer.class
                )
            );

            // With target type
            assertEquals(
                1, 
                (Integer)proxy.convertToTargetType(
                    "1", 
                    (Type)Integer.class
                )
            );
        }

        @Test
        @DisplayName(
            "should throw ClassCastException when value converted to target type reference " +
            "is not assignable to the proxy method return type"
        )
        void test15() {
            Resolver resolver = new StubResolver();
            
            InternalExternalizedProperties externalizedProperties = 
                internalExternalizedProperties(resolver);

            ReturnTypeMismatchConvertProxy proxy = externalizedProperties.initialize(
                ReturnTypeMismatchConvertProxy.class
            );

            // Target type is List<String> but proxy interface method
            // return type is Integer.
            assertThrows(
                ClassCastException.class,
                () -> proxy.convertToTargetTypeReference(
                    "1,2,3",
                    new TypeReference<List<String>>(){}
                )
            );
        }

        @Test
        @DisplayName(
            "should throw ClassCastException when value converted to target class " +
            "is not assignable to the proxy method return type"
        )
        void test16() {
            Resolver resolver = new StubResolver();
            
            InternalExternalizedProperties externalizedProperties = 
                internalExternalizedProperties(resolver);

            ReturnTypeMismatchConvertProxy proxy = externalizedProperties.initialize(
                ReturnTypeMismatchConvertProxy.class
            );

            // Target type is List but proxy interface method
            // return type is Integer.
            assertThrows(
                ClassCastException.class,
                () -> proxy.convertToTargetClass(
                    "1,2,3",
                    List.class
                )
            );
        }

        @Test
        @DisplayName(
            "should throw ClassCastException when value converted to target type " +
            "is not assignable to the proxy method return type"
        )
        void test17() {
            Resolver resolver = new StubResolver();
            
            InternalExternalizedProperties externalizedProperties = 
                internalExternalizedProperties(resolver);

            ReturnTypeMismatchConvertProxy proxy = externalizedProperties.initialize(
                ReturnTypeMismatchConvertProxy.class
            );

            // Target type is List<String> but proxy interface method
            // return type is Integer.
            assertThrows(
                ClassCastException.class,
                () -> proxy.convertToTargetType(
                    "1,2,3",
                    new TypeReference<List<String>>(){}.type()
                )
            );
        }

        @Test
        @DisplayName(
            "should throw ClassCastException when value converted to target type" + 
            "is not assignable to the proxy method return type variable"
        )
        void test18() {
            Resolver resolver = new StubResolver();
            
            InternalExternalizedProperties externalizedProperties = 
                internalExternalizedProperties(resolver);

            ReturnTypeMismatchConvertProxy proxy = externalizedProperties.initialize(
                ReturnTypeMismatchConvertProxy.class
            );

            // Target type is List<String> but proxy interface method
            // return type is Integer.
            assertThrows(
                ClassCastException.class,
                () -> {
                    // Should throw ClassCastException.
                    // Value is a List<String> but return variable was resolved to Integer.
                    Integer mismatch = proxy.convertToTargetTypeWithTypeVariableReturnType(
                        "1,2,3",
                        new TypeReference<List<String>>(){}.type()
                    );

                    fail("Did not throw. Result value: " + mismatch);
                }
            );
        }

        @Test
        @DisplayName("should initialize a proxy that handles @ExpandVariables annotations")
        void test19() {
            StubResolver resolver = new StubResolver();
            
            InternalExternalizedProperties externalizedProperties = 
                internalExternalizedProperties(resolver);

            ExpandVariablesProxy proxy = externalizedProperties.initialize(
                ExpandVariablesProxy.class
            );

            assertNotNull(proxy);
            assertTrue(proxy instanceof Proxy);

            assertEquals(
                resolver.valueResolver().apply("java.version"), 
                proxy.expandVariables("${java.version}")
            );
        }

        @Test
        @DisplayName(
            "should throw when @ExpandVariables proxy method does not have " +
            "any method parameters"
        )
        void test20() {
            InternalExternalizedProperties externalizedProperties = 
                internalExternalizedProperties(new StubResolver());

            assertThrows(
                IllegalArgumentException.class, 
                () -> externalizedProperties.initialize(
                    NoArgsExpandVariablesProxy.class
                )
            );
        }

        @Test
        @DisplayName(
            "should throw when @ExpandVariables proxy method have more than" +
            "1 method parameters"
        )
        void test21() {
            InternalExternalizedProperties externalizedProperties = 
                internalExternalizedProperties(new StubResolver());

            assertThrows(
                IllegalArgumentException.class, 
                () -> externalizedProperties.initialize(
                    InvalidNumberOfArgsExpandVariablesProxy.class
                )
            );
        }

        @Test
        @DisplayName(
            "should throw when @ExpandVariables proxy method have an invalid" +
            "parameter type"
        )
        void test22() {
            InternalExternalizedProperties externalizedProperties = 
                internalExternalizedProperties(new StubResolver());

            assertThrows(
                IllegalArgumentException.class, 
                () -> externalizedProperties.initialize(
                    InvalidArgTypeExpandVariablesProxy.class
                )
            );
        }

        @Test
        @DisplayName(
            "should throw when @ExpandVariables proxy method have an invalid" +
            "return type"
        )
        void test23() {
            InternalExternalizedProperties externalizedProperties = 
                internalExternalizedProperties(new StubResolver());

            assertThrows(
                IllegalArgumentException.class, 
                () -> externalizedProperties.initialize(
                    InvalidReturnTypeExpandVariablesProxy.class
                )
            );
        }
    }

    @Nested
    class InitializeMethodWithClassLoaderOverload {
        @Test
        @DisplayName("should throw when proxy interface argument is null")
        void test1() {
            InternalExternalizedProperties externalizedProperties = 
                internalExternalizedProperties(new StubResolver());
            ClassLoader classLoader = getClass().getClassLoader();

            assertThrows(
                IllegalArgumentException.class, 
                () -> externalizedProperties.initialize(null, classLoader) 
            );
        }

        @Test
        @DisplayName("should throw when class loader argument is null")
        void test2() {
            InternalExternalizedProperties externalizedProperties = 
                internalExternalizedProperties(new StubResolver());

            assertThrows(
                IllegalArgumentException.class, 
                () -> externalizedProperties.initialize(ProxyInterface.class, null)
            );
        }

        @Test
        @DisplayName("should throw when proxy interface argument is not an interface")
        void test3() {
            InternalExternalizedProperties externalizedProperties = 
                internalExternalizedProperties(new StubResolver());

            assertThrows(
                IllegalArgumentException.class, 
                () -> externalizedProperties.initialize(
                    InternalExternalizedPropertiesTests.class,
                    getClass().getClassLoader()
                )
            );
        }

        @Test
        @DisplayName("should throw when proxy interface contains void-returning methods")
        void test4() {
            InternalExternalizedProperties externalizedProperties = 
                internalExternalizedProperties(new StubResolver());

            assertThrows(
                IllegalArgumentException.class, 
                () -> externalizedProperties.initialize(
                    VoidReturnTypeProxyInterface.class,
                    getClass().getClassLoader()
                )
            );
        }

        @Test
        @DisplayName("should throw when proxy interface contains Void-returning methods")
        void test5() {
            InternalExternalizedProperties externalizedProperties = 
                internalExternalizedProperties(new StubResolver());

            assertThrows(
                IllegalArgumentException.class, 
                () -> externalizedProperties.initialize(
                    VoidClassReturnTypeProxyInterface.class,
                    getClass().getClassLoader()
                )
            );
        }

        @Test
        @DisplayName(
            "should throw when @ExternalizedProperty (no value) proxy method does not have " +
            "a single String argument"
        )
        void test6() {
            InternalExternalizedProperties externalizedProperties = 
                internalExternalizedProperties(new StubResolver());

            assertThrows(
                IllegalArgumentException.class, 
                () -> externalizedProperties.initialize(
                    NoStringArgProxyInterface.class,
                    getClass().getClassLoader()
                )
            );
        }

        @Test
        @DisplayName(
            "should throw when @ExternalizedProperty (no value) proxy method have " +
            "multiple arguments"
        )
        void test7() {
            InternalExternalizedProperties externalizedProperties = 
                internalExternalizedProperties(new StubResolver());

            assertThrows(
                IllegalArgumentException.class, 
                () -> externalizedProperties.initialize(
                    MultipleArgsProxyInterface.class,
                    getClass().getClassLoader()
                )
            );
        }

        @Test
        @DisplayName(
            "should throw when @ExternalizedProperty (no value) proxy method have " +
            "a non-String argument"
        )
        void test8() {
            InternalExternalizedProperties externalizedProperties = 
                internalExternalizedProperties(new StubResolver());

            assertThrows(
                IllegalArgumentException.class, 
                () -> externalizedProperties.initialize(
                    InvalidArgTypeProxyInterface.class,
                    getClass().getClassLoader()
                )
            );
        }

        @Test
        @DisplayName(
            "should throw when @Convert proxy method have invalid " +
            "method parameter types"
        )
        void test9() {
            InternalExternalizedProperties externalizedProperties = 
                internalExternalizedProperties(new StubResolver());

            assertThrows(
                IllegalArgumentException.class, 
                () -> externalizedProperties.initialize(
                    InvalidArgsConvertProxy.class,
                    getClass().getClassLoader()
                )
            );
        }

        @Test
        @DisplayName(
            "should throw when @Convert proxy method have no " +
            "method parameters"
        )
        void test10() {
            InternalExternalizedProperties externalizedProperties = 
                internalExternalizedProperties(new StubResolver());

            assertThrows(
                IllegalArgumentException.class, 
                () -> externalizedProperties.initialize(
                    NoArgsConvertProxy.class,
                    getClass().getClassLoader()
                )
            );
        }

        @Test
        @DisplayName(
            "should throw when @Convert proxy method does not have " +
            "2 method parameters"
        )
        void test11() {
            InternalExternalizedProperties externalizedProperties = 
                internalExternalizedProperties(new StubResolver());

            assertThrows(
                IllegalArgumentException.class, 
                () -> externalizedProperties.initialize(
                    SingleArgConvertProxy.class,
                    getClass().getClassLoader()
                )
            );
        }

        @Test
        @DisplayName(
            "should throw when @Convert proxy method first parameter " + 
            "is not a String (value to convert)"
        )
        void test12() {
            InternalExternalizedProperties externalizedProperties = 
                internalExternalizedProperties(new StubResolver());

            assertThrows(
                IllegalArgumentException.class, 
                () -> externalizedProperties.initialize(
                    InvalidFirstArgTypeConvertProxy.class,
                    getClass().getClassLoader()
                )
            );
        }

        @Test
        @DisplayName(
            "should throw when @Convert proxy method second parameter " + 
            "is not the target type"
        )
        void test13() {
            InternalExternalizedProperties externalizedProperties = 
                internalExternalizedProperties(new StubResolver());

            assertThrows(
                IllegalArgumentException.class, 
                () -> externalizedProperties.initialize(
                    InvalidSecondArgTypeConvertProxy.class,
                    getClass().getClassLoader()
                )
            );
        }

        @Test
        @DisplayName(
            "should initialize a proxy that handles @ExternalizedProperty annotations"
        )
        void test14() {
            StubResolver resolver = new StubResolver();
            
            InternalExternalizedProperties externalizedProperties = 
                internalExternalizedProperties(resolver);

            ProxyInterface proxy = externalizedProperties.initialize(
                ProxyInterface.class,
                getClass().getClassLoader()
            );

            assertNotNull(proxy);
            assertTrue(proxy instanceof Proxy);

            assertEquals(
                resolver.valueResolver().apply("property"),
                proxy.property()
            );
        }


        @Test
        @DisplayName("should initialize a proxy that handles @Convert annotations")
        void test15() {
            Resolver resolver = new StubResolver();
            
            InternalExternalizedProperties externalizedProperties = 
                internalExternalizedProperties(resolver);

            ConvertProxy proxy = externalizedProperties.initialize(
                ConvertProxy.class,
                getClass().getClassLoader()
            );

            assertNotNull(proxy);
            assertTrue(proxy instanceof Proxy);

            // With target type reference
            assertEquals(
                1, 
                (Integer)proxy.convertToTargetTypeReference(
                    "1", 
                    new TypeReference<Integer>(){}
                )
            );

            // With target class
            assertEquals(
                1, 
                (Integer)proxy.convertToTargetClass(
                    "1", 
                    Integer.class
                )
            );

            // With target type
            assertEquals(
                1, 
                (Integer)proxy.convertToTargetType(
                    "1", 
                    (Type)Integer.class
                )
            );
        }

        @Test
        @DisplayName(
            "should throw ClassCastException when value converted to target type reference " +
            "is not assignable to the proxy method return type"
        )
        void test16() {
            Resolver resolver = new StubResolver();
            
            InternalExternalizedProperties externalizedProperties = 
                internalExternalizedProperties(resolver);

            ReturnTypeMismatchConvertProxy proxy = externalizedProperties.initialize(
                ReturnTypeMismatchConvertProxy.class,
                getClass().getClassLoader()
            );

            // Target type is List<String> but proxy interface method
            // return type is Integer.
            assertThrows(
                ClassCastException.class,
                () -> proxy.convertToTargetTypeReference(
                    "1,2,3",
                    new TypeReference<List<String>>(){}
                )
            );
        }

        @Test
        @DisplayName(
            "should throw ClassCastException when value converted to target class " +
            "is not assignable to the proxy method return type"
        )
        void test17() {
            Resolver resolver = new StubResolver();
            
            InternalExternalizedProperties externalizedProperties = 
                internalExternalizedProperties(resolver);

            ReturnTypeMismatchConvertProxy proxy = externalizedProperties.initialize(
                ReturnTypeMismatchConvertProxy.class,
                getClass().getClassLoader()
            );

            // Target type is List but proxy interface method
            // return type is Integer.
            assertThrows(
                ClassCastException.class,
                () -> proxy.convertToTargetClass(
                    "1,2,3",
                    List.class
                )
            );
        }

        @Test
        @DisplayName(
            "should throw ClassCastException when value converted to target type " +
            "is not assignable to the proxy method return type"
        )
        void test18() {
            Resolver resolver = new StubResolver();
            
            InternalExternalizedProperties externalizedProperties = 
                internalExternalizedProperties(resolver);

            ReturnTypeMismatchConvertProxy proxy = externalizedProperties.initialize(
                ReturnTypeMismatchConvertProxy.class,
                getClass().getClassLoader()
            );

            // Target type is List<String> but proxy interface method
            // return type is Integer.
            assertThrows(
                ClassCastException.class,
                () -> proxy.convertToTargetType(
                    "1,2,3",
                    new TypeReference<List<String>>(){}.type()
                )
            );
        }

        @Test
        @DisplayName(
            "should throw ClassCastException when value converted to target type" + 
            "is not assignable to the proxy method return type variable"
        )
        void test19() {
            Resolver resolver = new StubResolver();
            
            InternalExternalizedProperties externalizedProperties = 
                internalExternalizedProperties(resolver);

            ReturnTypeMismatchConvertProxy proxy = externalizedProperties.initialize(
                ReturnTypeMismatchConvertProxy.class,
                getClass().getClassLoader()
            );

            // Target type is List<String> but proxy interface method
            // return type is Integer.
            assertThrows(
                ClassCastException.class,
                () -> {
                    // Should throw ClassCastException.
                    // Value is a List<String> but return variable was resolved to Integer.
                    Integer mismatch = proxy.convertToTargetTypeWithTypeVariableReturnType(
                        "1,2,3",
                        new TypeReference<List<String>>(){}.type()
                    );

                    fail("Did not throw. Result value: " + mismatch);
                }
            );
        }
        
        @Test
        @DisplayName("should initialize a proxy that handles @ExpandVariables annotations")
        void test20() {
            StubResolver resolver = new StubResolver();
            
            InternalExternalizedProperties externalizedProperties = 
                internalExternalizedProperties(resolver);

            ExpandVariablesProxy proxy = externalizedProperties.initialize(
                ExpandVariablesProxy.class,
                getClass().getClassLoader()
            );

            assertNotNull(proxy);
            assertTrue(proxy instanceof Proxy);

            assertEquals(
                resolver.valueResolver().apply("property"), 
                proxy.expandVariables("${property}")
            );
        }

        @Test
        @DisplayName(
            "should throw when @ExpandVariables proxy method does not have " +
            "any method parameters"
        )
        void test21() {
            InternalExternalizedProperties externalizedProperties = 
                internalExternalizedProperties(new StubResolver());

            assertThrows(
                IllegalArgumentException.class, 
                () -> externalizedProperties.initialize(
                    NoArgsExpandVariablesProxy.class,
                    getClass().getClassLoader()
                )
            );
        }

        @Test
        @DisplayName(
            "should throw when @ExpandVariables proxy method have more than" +
            "1 method parameters"
        )
        void test22() {
            InternalExternalizedProperties externalizedProperties = 
                internalExternalizedProperties(new StubResolver());

            assertThrows(
                IllegalArgumentException.class, 
                () -> externalizedProperties.initialize(
                    InvalidNumberOfArgsExpandVariablesProxy.class,
                    getClass().getClassLoader()
                )
            );
        }

        @Test
        @DisplayName(
            "should throw when @ExpandVariables proxy method have an invalid" +
            "parameter type"
        )
        void test23() {
            InternalExternalizedProperties externalizedProperties = 
                internalExternalizedProperties(new StubResolver());

            assertThrows(
                IllegalArgumentException.class, 
                () -> externalizedProperties.initialize(
                    InvalidArgTypeExpandVariablesProxy.class,
                    getClass().getClassLoader()
                )
            );
        }

        @Test
        @DisplayName(
            "should throw when @ExpandVariables proxy method have an invalid" +
            "return type"
        )
        void test24() {
            InternalExternalizedProperties externalizedProperties = 
                internalExternalizedProperties(new StubResolver());

            assertThrows(
                IllegalArgumentException.class, 
                () -> externalizedProperties.initialize(
                    InvalidReturnTypeExpandVariablesProxy.class,
                    getClass().getClassLoader()
                )
            );
        }
    }

    private static InternalExternalizedProperties internalExternalizedProperties(
            Resolver resolverToUse
    ) {
        RootResolver rootResolver = new RootResolver(
            Arrays.asList(resolverToUse),
            new RootProcessor()
        );

        RootConverter rootConverter = new RootConverter(
            new DefaultConverter()
        );

        SimpleVariableExpander variableExpander = new SimpleVariableExpander();

        return new InternalExternalizedProperties(
            rootResolver,
            rootConverter,
            variableExpander,
            (proxyInterface, rr, rc, ve, pmf) -> 
                new ExternalizedPropertiesInvocationHandler(
                    rr, rc, ve, pmf
                )
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

    private static interface ProxyInterface {
        @ExternalizedProperty("property")
        String property();
    }

    private static interface VoidReturnTypeProxyInterface {
        // Invalid: Void return types not allowed.
        @ExternalizedProperty("test.invalid.method.void")
        void invalidVoidMethod();
    }

    private static interface VoidClassReturnTypeProxyInterface {
        // Invalid: Void return types not allowed.
        @ExternalizedProperty("test.invalid.method.void")
        Void invalidVoidClassMethod();
    }

    private static interface ConvertProxy {
        @Convert
        <T> T convertToTargetTypeReference(
            String valueToConvert, 
            TypeReference<T> targetType
        );
        @Convert
        <T> T convertToTargetClass(String valueToConvert, Class<T> targetType);
        @Convert
        <T> T convertToTargetType(String valueToConvert, Type targetType);
    }

    private static interface InvalidArgsConvertProxy {
        // First argument must be a String.
        // Second argument must be one of the ff: TypeReference, Class, Type
        @Convert
        <T> T convert(Integer mustBeString, Double mustBeTargetType);
    }

    private static interface NoArgsConvertProxy {
        // Must have 2 parameters: The value to convert and the target type.
        @Convert
        <T> T convert();
    }

    private static interface SingleArgConvertProxy {
        // Must have 2 parameters: The value to convert and the target type.
        @Convert
        <T> T convert(String valueToConvert);
    }

    private static interface InvalidFirstArgTypeConvertProxy {
        // First parameter must be the value to convert (String)
        @Convert
        <T> T convert(Integer mustBeString, Class<T> targetType);
    }

    private static interface InvalidSecondArgTypeConvertProxy {
        // Second parameter must be the target type.
        // Second argument must be one of the ff: TypeReference, Class, Type
        @Convert
        <T> T convert(String valueToConvert, Integer mustBeTargetType);
    }

    private static interface ReturnTypeMismatchConvertProxy {
        // Return type not assignable with target type.
        @Convert
        Integer convertToTargetTypeReference(
            String valueToConvert, 
            TypeReference<?> targetType
        );

        // Return type not assignable with target type.
        @Convert
        Integer convertToTargetClass(String valueToConvert, Class<?> targetType);

        // Return type not assignable with target type.
        @Convert
        Integer convertToTargetType(String valueToConvert, Type targetType);

        // Return type not assignable with target type.
        @Convert
        <T> T convertToTargetTypeWithTypeVariableReturnType(
            String valueToConvert, 
            Type targetType
        );
    }

    private static interface ExpandVariablesProxy {
        @ExpandVariables
        String expandVariables(String value);
    }

    private static interface InvalidReturnTypeExpandVariablesProxy {
        // Return type must be String.
        @ExpandVariables
        int expandVariables(String value);
    }

    private static interface InvalidArgTypeExpandVariablesProxy {
        // Must have 1 String argument.
        @ExpandVariables
        String expandVariables(int value);
    }

    private static interface NoArgsExpandVariablesProxy {
        // Must have 1 String argument.
        @ExpandVariables
        String expandVariables();
    }

    private static interface InvalidNumberOfArgsExpandVariablesProxy {
        //Must only have 1 String argument,
        @ExpandVariables
        int expandVariables(String value, String mustOnlyBeOneArg);
    }
}
