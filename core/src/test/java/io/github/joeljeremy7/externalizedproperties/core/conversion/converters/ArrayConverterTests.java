package io.github.joeljeremy7.externalizedproperties.core.conversion.converters;

import io.github.joeljeremy7.externalizedproperties.core.ConversionResult;
import io.github.joeljeremy7.externalizedproperties.core.ConverterProvider;
import io.github.joeljeremy7.externalizedproperties.core.ExternalizedProperties;
import io.github.joeljeremy7.externalizedproperties.core.ExternalizedProperty;
import io.github.joeljeremy7.externalizedproperties.core.conversion.ConversionException;
import io.github.joeljeremy7.externalizedproperties.core.conversion.Delimiter;
import io.github.joeljeremy7.externalizedproperties.core.conversion.StripEmptyValues;
import io.github.joeljeremy7.externalizedproperties.core.internal.conversion.RootConverter;
import io.github.joeljeremy7.externalizedproperties.core.proxy.ProxyMethod;
import io.github.joeljeremy7.externalizedproperties.core.testfixtures.ProxyMethodFactory;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ArrayConverterTests {
    private static final ProxyMethodFactory<ProxyInterface> PROXY_METHOD_FACTORY = 
        new ProxyMethodFactory<>(ProxyInterface.class);

    @Nested
    class ProviderMethod {
        @Test
        @DisplayName("should not return null.")
        public void test1() {
            ConverterProvider<ArrayConverter> provider = 
                ArrayConverter.provider();

            assertNotNull(provider);
        }

        @Test
        @DisplayName("should return an instance on get.")
        public void test2() {
            ConverterProvider<ArrayConverter> provider = 
                ArrayConverter.provider();
            
            ExternalizedProperties externalizedProperties = 
                ExternalizedProperties.builder()
                    .withDefaultResolvers()
                    .converters(provider)
                    .build();
            
            assertNotNull(
                provider.get(
                    externalizedProperties,
                    new RootConverter(externalizedProperties, provider)
                )
            );
        }
    }
    
    @Nested
    class CanConvertToMethod {
        @Test
        @DisplayName("should return false when target type is null.")
        void test1() {
            ArrayConverter converter = converterToTest();
            boolean canConvert = converter.canConvertTo(null);
            assertFalse(canConvert);
        }

        @Test
        @DisplayName("should return true when target type is an array class.")
        void test2() {
            ArrayConverter converter = converterToTest();
            boolean canConvert = converter.canConvertTo(String[].class);
            assertTrue(canConvert);
        }

        @Test
        @DisplayName("should return false when target type is not an array.")
        void test3() {
            ArrayConverter converter = converterToTest();
            boolean canConvert = converter.canConvertTo(Integer.class);
            assertFalse(canConvert);
        }
    }

    @Nested
    class ConvertMethod {
        @Test
        @DisplayName("should return skip result when target type is not an array.")
        void test1() {
            ArrayConverter converter = converterToTest();

            // Method return type is an int and not an array e.g. String[]
            ProxyMethod proxyMethod = PROXY_METHOD_FACTORY.fromMethodReference(
                ProxyInterface::notSupportedNotAnArray
            );

            ConversionResult<?> result = converter.convert(
                proxyMethod,
                "value1,value2,value3"
            );

            // Method return type is an int and not an array
            assertEquals(ConversionResult.skip(), result);
        }

        @Test
        @DisplayName("should convert value to an array.")
        void test2() {
            ArrayConverter converter = converterToTest();

            ProxyMethod proxyMethod = PROXY_METHOD_FACTORY.fromMethodReference(
                ProxyInterface::arrayProperty
            );

            ConversionResult<? extends Object[]> result = 
                converter.convert(
                    proxyMethod,
                    "value1,value2,value3"
                );

            assertNotNull(result);
            Object[] array = result.value();

            assertNotNull(array);
            assertEquals(3, array.length);
            assertTrue(Arrays.stream(array).allMatch(v -> v instanceof String));
            assertArrayEquals(
                new String[] { "value1", "value2", "value3" }, 
                array
            );
        }

        @Test
        @DisplayName("should convert value according to the array's component type.")
        void test3() {
            ArrayConverter converter = converterToTest(PrimitiveConverter.provider());

            ProxyMethod proxyMethod = PROXY_METHOD_FACTORY.fromMethodReference(
                ProxyInterface::arrayIntegerWrapper
            );
            
            ConversionResult<? extends Object[]> result = converter.convert(
                proxyMethod,
                "1,2,3"
            );

            assertNotNull(result);
            Object[] array = result.value();
            
            assertNotNull(array);
            assertEquals(3, array.length);
            assertTrue(Arrays.stream(array).allMatch(v -> v instanceof Integer));
            assertArrayEquals(
                new Integer[] { 1, 2, 3 }, 
                array
            );
        }

        @Test
        @DisplayName("should return empty array when value is empty.")
        void test4() {
            ArrayConverter converter = converterToTest();
            
            ProxyMethod proxyMethod = PROXY_METHOD_FACTORY.fromMethodReference(
                ProxyInterface::arrayProperty
            );
            
            ConversionResult<? extends Object[]> result = converter.convert(
                proxyMethod,
                "" // Empty value.
            );
            assertNotNull(result);
            Object[] array = result.value();
            
            assertNotNull(array);
            assertEquals(0, array.length);
        }

        @Test
        @DisplayName("should retain empty values from value.")
        void test5() {
            ArrayConverter converter = converterToTest();
            
            ProxyMethod proxyMethod = PROXY_METHOD_FACTORY.fromMethodReference(
                ProxyInterface::arrayProperty
            );
            
            ConversionResult<? extends Object[]> result = converter.convert(
                proxyMethod,
                "value1,,value3,,value5" // Has empty values.
            );

            assertNotNull(result);
            Object[] array = result.value();
            
            assertNotNull(array);
            assertEquals(5, array.length);
            assertTrue(Arrays.stream(array).allMatch(v -> v instanceof String));
            assertArrayEquals(
                new String[] { "value1", "", "value3", "", "value5" }, 
                array
            );
        }

        @Test
        @DisplayName("should strip empty values when annotated with @StripEmptyValues.")
        void test6() {
            ArrayConverter converter = converterToTest();
            
            ProxyMethod proxyMethod = PROXY_METHOD_FACTORY.fromMethodReference(
                ProxyInterface::arrayPropertyStripEmpty
            );
            
            ConversionResult<? extends Object[]> result = converter.convert(
                proxyMethod,
                "value1,,value3,,value5" // Has empty values.
            );

            assertNotNull(result);
            Object[] array = result.value();
            
            assertNotNull(array);
            assertEquals(3, array.length);
            assertTrue(Arrays.stream(array).allMatch(v -> v instanceof String));
            assertArrayEquals(
                new String[] { "value1", "value3", "value5" }, 
                array
            );
        }

        @Test
        @DisplayName("should return Strings when array component type is Object.")
        void test7() {
            ArrayConverter converter = converterToTest();
            
            ProxyMethod proxyMethod = PROXY_METHOD_FACTORY.fromMethodReference(
                ProxyInterface::arrayPropertyObject
            );
            
            ConversionResult<? extends Object[]> result = converter.convert(
                proxyMethod,
                "value1,value2,value3"
            );

            assertNotNull(result);
            Object[] array = result.value();
            
            assertNotNull(array);
            assertEquals(3, array.length);
            assertTrue(Arrays.stream(array).allMatch(v -> v instanceof String));
            assertArrayEquals(
                new String[] { "value1", "value2", "value3" }, 
                array
            );
        }

        @Test
        @DisplayName(
            "should throw when no rootConverter is registered that can handle " + 
            "the array's component type."
        )
        void test8() {
            // No registered converter for Integer.
            ArrayConverter converter = converterToTest();

            ProxyMethod proxyMethod = PROXY_METHOD_FACTORY.fromMethodReference(
                ProxyInterface::arrayIntegerWrapper
            );
            
            assertThrows(ConversionException.class, () -> {
                converter.convert(
                    proxyMethod,
                    "1,2,3,4,5"
                );
            });
        }

        @Test
        @DisplayName("should use custom delimiter defined by @Delimiter.")
        void test9() {
            ArrayConverter converter = converterToTest();
            
            ProxyMethod proxyMethod = PROXY_METHOD_FACTORY.fromMethodReference(
                ProxyInterface::arrayCustomDelimiter
            );
            
            ConversionResult<? extends Object[]> result = converter.convert(
                proxyMethod,
                "value1|value2|value3" // Custom delimiter
            );

            assertNotNull(result);
            Object[] array = result.value();
            
            assertNotNull(array);
            assertEquals(3, array.length);
            assertTrue(Arrays.stream(array).allMatch(v -> v instanceof String));
            assertArrayEquals(
                new String[] { "value1", "value2", "value3" }, 
                array
            );
        }

        @Test
        @DisplayName(
            "should convert value according to the array's generic component type."
        )
        void test10() {
            ArrayConverter converter = converterToTest(OptionalConverter.provider());
            
            ProxyMethod proxyMethod = PROXY_METHOD_FACTORY.fromMethodReference(
                ProxyInterface::arrayPropertyGeneric // Returns a generic type array Optional<String>[]
            );
            
            ConversionResult<? extends Object[]> result = converter.convert(
                proxyMethod,
                "value1,value2,value3"
            );

            assertNotNull(result);
            Object[] array = result.value();
            
            assertNotNull(array);
            assertEquals(3, array.length);
            assertTrue(Arrays.stream(array).allMatch(v -> v instanceof Optional));
            assertArrayEquals(
                new Optional[] { 
                    Optional.of("value1"), 
                    Optional.of("value2"), 
                    Optional.of("value3")
                }, 
                array
            );
        }

        @Test
        @DisplayName(
            "should convert generic type parameter wildcards to Strings."
        )
        void test11() {
            ArrayConverter converter = converterToTest(OptionalConverter.provider());
            
            ProxyMethod proxyMethod = PROXY_METHOD_FACTORY.fromMethodReference(
                ProxyInterface::arrayPropertyGenericWildcard // Returns a generic type array Optional<?>[]
            );
            
            ConversionResult<? extends Object[]> result = converter.convert(
                proxyMethod,
                "value1,value2,value3"
            );

            assertNotNull(result);
            Object[] array = result.value();
            
            assertNotNull(array);
            assertEquals(3, array.length);
            assertTrue(Arrays.stream(array).allMatch(v -> v instanceof Optional));
            assertArrayEquals(
                new Optional[] { 
                    Optional.of("value1"), 
                    Optional.of("value2"), 
                    Optional.of("value3")
                }, 
                array
            );
        }

        @Test
        @DisplayName("should throw when target type has a type variable e.g. List<T>.")
        void test12() {
            ArrayConverter converter = converterToTest();
            
            ProxyMethod proxyMethod = PROXY_METHOD_FACTORY.fromMethodReference(
                ProxyInterface::arrayPropertyT // Returns a generic type array <T> T[]
            );
            
            assertThrows(
                ConversionException.class, 
                () -> converter.convert(
                    proxyMethod,
                    "value1,value2,value3"
                )
            );
        }
    }

    private ArrayConverter converterToTest(ConverterProvider<?>... additionalConverters) {
        ConverterProvider<ArrayConverter> provider = ArrayConverter.provider();

        List<ConverterProvider<?>> allProviders = new ArrayList<>(
            Arrays.asList(additionalConverters)
        );
        allProviders.add(provider);
        
        ExternalizedProperties externalizedProperties = 
            ExternalizedProperties.builder()
                .withDefaultResolvers()
                .converters(allProviders)
                .build();

        RootConverter rootConverter = new RootConverter(
            externalizedProperties, 
            allProviders
        );
        return provider.get(externalizedProperties, rootConverter);
    }

    public static interface ProxyInterface {
        @ExternalizedProperty("property.array")
        String[] arrayProperty();

        @ExternalizedProperty("property.array.object")
        Object[] arrayPropertyObject();

        @ExternalizedProperty("property.array.custom.delimiter")
        @Delimiter("|")
        String[] arrayCustomDelimiter();

        @ExternalizedProperty("property.array.stripempty")
        @StripEmptyValues
        String[] arrayPropertyStripEmpty();

        @ExternalizedProperty("property.array.integer.wrapper")
        Integer[] arrayIntegerWrapper();

        @ExternalizedProperty("property.array.integer.primitive")
        int[] arrayIntegerPrimitive();

        @ExternalizedProperty("property.array.generic")
        Optional<String>[] arrayPropertyGeneric();

        @ExternalizedProperty("property.array.generic.nested")
        Optional<Optional<String>>[] arrayPropertyNestedGeneric();

        @ExternalizedProperty("property.array.generic.wildcard")
        Optional<?>[] arrayPropertyGenericWildcard();

        @ExternalizedProperty("property.array.T")
        <T> T[] arrayPropertyT();

        @ExternalizedProperty("property.not.supported")
        int notSupportedNotAnArray();
    }
}
