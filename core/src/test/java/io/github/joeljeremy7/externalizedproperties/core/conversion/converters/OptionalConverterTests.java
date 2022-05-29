package io.github.joeljeremy7.externalizedproperties.core.conversion.converters;

import io.github.joeljeremy7.externalizedproperties.core.ConversionResult;
import io.github.joeljeremy7.externalizedproperties.core.Converter;
import io.github.joeljeremy7.externalizedproperties.core.ExternalizedProperties;
import io.github.joeljeremy7.externalizedproperties.core.ExternalizedProperty;
import io.github.joeljeremy7.externalizedproperties.core.conversion.ConversionException;
import io.github.joeljeremy7.externalizedproperties.core.proxy.ProxyMethod;
import io.github.joeljeremy7.externalizedproperties.core.testfixtures.TestProxyMethodFactory;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class OptionalConverterTests {
    private static final TestProxyMethodFactory<ProxyInterface> PROXY_METHOD_FACTORY =
        new TestProxyMethodFactory<>(ProxyInterface.class);

    @Nested
    class CanConvertToMethod {
        @Test
        @DisplayName("should return false when target type is null.")
        void test1() {
            OptionalConverter converter = converterToTest();
            boolean canConvert = converter.canConvertTo(null);
            assertFalse(canConvert);
        }

        @Test
        @DisplayName("should return true when target type is an Optional class.")
        void test2() {
            OptionalConverter converter = converterToTest();
            boolean canConvert = converter.canConvertTo(Optional.class);
            assertTrue(canConvert);
        }

        @Test
        @DisplayName("should return false when target type is not an Optional class.")
        void test3() {
            OptionalConverter converter = converterToTest();
            boolean canConvert = converter.canConvertTo(String.class);
            assertFalse(canConvert);
        }
    }

    @Nested
    class ConvertMethod {
        @Test
        @DisplayName("should convert value to an Optional.")
        void test1() {
            OptionalConverter converter = converterToTest();

            ProxyMethod proxyMethod = PROXY_METHOD_FACTORY.fromMethodReference(
                ProxyInterface::optionalProperty,
                externalizedProperties(converter)
            );

            ConversionResult<? extends Optional<?>> result = converter.convert(
                proxyMethod,
                "value"
            );

            assertNotNull(result);
            Optional<?> optional = result.value();
            
            assertNotNull(optional);
            assertTrue(optional.isPresent());
            assertTrue(optional.get() instanceof String);
            assertEquals("value", optional.get());
        }

        @Test
        @DisplayName(
            "should convert value according to the Optional's generic type parameter."
        )
        void test2() {
            OptionalConverter converter = converterToTest();

            ProxyMethod proxyMethod = PROXY_METHOD_FACTORY.fromMethodReference(
                ProxyInterface::nonStringOptionalProperty,
                externalizedProperties(converter, new PrimitiveConverter())
            );
            
            ConversionResult<? extends Optional<?>> result = converter.convert(
                proxyMethod,
                "1"
            );

            assertNotNull(result);
            Optional<?> optional = result.value();
            
            assertNotNull(optional);
            assertTrue(optional.isPresent());
            assertTrue(optional.get() instanceof Integer);
            assertEquals(1, optional.get());
        }

        @Test
        @DisplayName(
            "should return String value when target type has no " + 
            "type parameters i.e. Optional.class"
        )
        void test3() {
            OptionalConverter converter = converterToTest();

            ProxyMethod proxyMethod = PROXY_METHOD_FACTORY.fromMethodReference(
                ProxyInterface::nonStringOptionalProperty,
                externalizedProperties(converter)
            );

            ConversionResult<? extends Optional<?>> result = converter.convert(
                proxyMethod,
                "1",
                // Override proxy method return type with a raw Optional
                // No generic type parameter
                Optional.class
            );
            
            assertNotNull(result);
            Optional<?> optional = result.value();

            assertNotNull(optional);
            assertTrue(optional.isPresent());
            // String and not Integer.
            assertTrue(optional.get() instanceof String);
            assertEquals("1", optional.get());
        }

        @Test
        @DisplayName("should return String value when Optional's generic type parameter is Object.")
        void test4() {
            OptionalConverter converter = converterToTest();

            ProxyMethod proxyMethod = PROXY_METHOD_FACTORY.fromMethodReference(
                ProxyInterface::optionalPropertyObject,
                externalizedProperties(converter)
            );

            ConversionResult<? extends Optional<?>> result = converter.convert(
                proxyMethod,
                "value"
            );
            
            assertNotNull(result);
            Optional<?> optional = result.value();

            assertNotNull(optional);
            assertTrue(optional.isPresent());
            assertTrue(optional.get() instanceof String);
            assertEquals("value", optional.get());
        }

        @Test
        @DisplayName(
            "should return String value when Optional's generic type parameter is a wildcard."
        )
        void test5() {
            OptionalConverter converter = converterToTest();

            ProxyMethod proxyMethod = PROXY_METHOD_FACTORY.fromMethodReference(
                ProxyInterface::optionalPropertyWildcard,
                externalizedProperties(converter)
            );

            ConversionResult<? extends Optional<?>> result = converter.convert(
                proxyMethod,
                "value"
            );
            
            assertNotNull(result);
            Optional<?> optional = result.value();

            assertNotNull(optional);
            assertTrue(optional.isPresent());
            assertTrue(optional.get() instanceof String);
            assertEquals("value", optional.get());
        }

        @Test
        @DisplayName("should throw when target type has a type variable e.g. Optional<T>.")
        void test6() {
            OptionalConverter converter = converterToTest();

            ProxyMethod proxyMethod = PROXY_METHOD_FACTORY.fromMethodReference(
                ProxyInterface::optionalPropertyT,
                externalizedProperties(converter)
            );
                
            assertThrows(
                ConversionException.class, 
                () -> converter.convert(proxyMethod, "value")
            );
        }

        @Test
        @DisplayName(
            "should convert value according to the Optional's generic type parameter. " + 
            "Generic type parameter is also a parameterized type e.g. Optional<List<String>>."
        )
        void test7() {
            OptionalConverter converter = converterToTest();

            ProxyMethod proxyMethod = PROXY_METHOD_FACTORY.fromMethodReference(
                ProxyInterface::optionalPropertyNestedGenerics,
                externalizedProperties(converter, new ListConverter())
            );

            ConversionResult<? extends Optional<?>> result = converter.convert(
                proxyMethod,
                "value1,value2,value3"
            );
            
            assertNotNull(result);
            Optional<?> optional = result.value();

            assertNotNull(optional);
            assertTrue(optional.isPresent());
            assertTrue(optional.get() instanceof List<?>);
            assertIterableEquals(
                Arrays.asList("value1", "value2", "value3"), 
                (List<?>)optional.get()
            );
        }
        
        @Test
        @DisplayName(
            "should convert value according to the Optional's generic type parameter. " + 
            "Generic type parameter is a generic array e.g. Optional<Optional<String>[]>."
        )
        void test8() {
            OptionalConverter converter = converterToTest();

            ProxyMethod proxyMethod = PROXY_METHOD_FACTORY.fromMethodReference(
                ProxyInterface::optionalPropertyNestedGenericsArray,
                externalizedProperties(converter, new ArrayConverter())
            );

            ConversionResult<? extends Optional<?>> result = converter.convert(
                proxyMethod,
                "value1,value2,value3"
            );
            
            assertNotNull(result);
            Optional<?> optional = result.value();

            assertNotNull(optional);
            assertTrue(optional.isPresent());
            assertTrue(optional.get() instanceof Optional<?>[]);
            // Optional returns an array (Optional<?>[])
            assertArrayEquals(
                new Optional[] { 
                    Optional.of("value1"), 
                    Optional.of("value2"), 
                    Optional.of("value3") 
                }, 
                (Optional<?>[])optional.get()
            );
        }

        @Test
        @DisplayName(
            "should convert value to an empty Optional when property value is empty."
        )
        void test9() {
            OptionalConverter converter = converterToTest();
            
            ProxyMethod proxyMethod = PROXY_METHOD_FACTORY.fromMethodReference(
                ProxyInterface::optionalProperty,
                externalizedProperties(converter)
            );

            ConversionResult<? extends Optional<?>> result = converter.convert(
                proxyMethod,
                "" // Empty.
            );
            
            assertNotNull(result);
            Optional<?> optional = result.value();
            
            assertNotNull(optional);
            assertFalse(optional.isPresent());
        }
    }

    private static OptionalConverter converterToTest() {
        return new OptionalConverter();
    }

    private static ExternalizedProperties externalizedProperties(
            OptionalConverter converterToTest,
            Converter<?>... additionalConverters
    ) {
        return ExternalizedProperties.builder()
            .converters(converterToTest)
            .converters(additionalConverters)
            .build();
    }

    static interface ProxyInterface {
        @ExternalizedProperty("property.optional")
        Optional<String> optionalProperty();

        @ExternalizedProperty("property.optional.with.default.value")
        default Optional<String> optionalPropertyWithDefaultValue() {
            return Optional.of("default.value");
        }

        @ExternalizedProperty("property.optional.with.default.value")
        default Optional<String> optionalPropertyWithDefaultValueParameter(String defaultValue) {
            return Optional.ofNullable(defaultValue);
        }

        // No annotation with default value.
        default Optional<String> optionalPropertyWithNoAnnotationAndWithDefaultValue() {
            return Optional.of("default.value");
        }

        // No annotation with provided default value.
        default Optional<String> optionalPropertyWithNoAnnotationAndWithDefaultValueParameter(String defaultValue) {
            return Optional.ofNullable(defaultValue);
        }

        // No annotation ano no default value.
        Optional<String> optionalPropertyWithNoAnnotationAndNoDefaultValue();

        @ExternalizedProperty("property.optional.nonstring")
        Optional<Integer> nonStringOptionalProperty();

        @ExternalizedProperty("property.optional.object")
        Optional<Object> optionalPropertyObject();

        @ExternalizedProperty("property.optional.wildcard")
        Optional<?> optionalPropertyWildcard();

        @ExternalizedProperty("property.optional.nested.generics")
        Optional<List<String>> optionalPropertyNestedGenerics();

        @ExternalizedProperty("property.optional.nested.generics.array")
        Optional<Optional<String>[]> optionalPropertyNestedGenericsArray();

        @ExternalizedProperty("property.optional.array")
        Optional<String[]> optionalPropertyArray();

        @ExternalizedProperty("property.optional.T")
        <T> Optional<T> optionalPropertyT();
    }
}
