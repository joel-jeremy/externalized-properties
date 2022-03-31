package io.github.jeyjeyemem.externalizedproperties.core.conversion.converters;

import io.github.jeyjeyemem.externalizedproperties.core.ConversionContext;
import io.github.jeyjeyemem.externalizedproperties.core.ConversionResult;
import io.github.jeyjeyemem.externalizedproperties.core.Converter;
import io.github.jeyjeyemem.externalizedproperties.core.conversion.ConversionException;
import io.github.jeyjeyemem.externalizedproperties.core.internal.conversion.RootConverter;
import io.github.jeyjeyemem.externalizedproperties.core.proxy.ProxyMethod;
import io.github.jeyjeyemem.externalizedproperties.core.testentities.ProxyMethodUtils;
import io.github.jeyjeyemem.externalizedproperties.core.testentities.proxy.ArrayProxyInterface;
import io.github.jeyjeyemem.externalizedproperties.core.testentities.proxy.PrimitiveProxyInterface;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ArrayConverterTests {

    @Nested
    class CanConvertToMethod {
        @Test
        @DisplayName("should return false when target type is null.")
        public void test1() {
            ArrayConverter converter = converterToTest();
            boolean canConvert = converter.canConvertTo(null);
            assertFalse(canConvert);
        }

        @Test
        @DisplayName("should return true when target type is an array class.")
        public void test2() {
            ArrayConverter converter = converterToTest();
            boolean canConvert = converter.canConvertTo(String[].class);
            assertTrue(canConvert);
        }

        @Test
        @DisplayName("should return false when target type is not an array.")
        public void test3() {
            ArrayConverter converter = converterToTest();
            boolean canConvert = converter.canConvertTo(Integer.class);
            assertFalse(canConvert);
        }
    }

    @Nested
    class ConvertMethod {
        @Test
        @DisplayName("should throw when context is null.")
        public void test1() {
            ArrayConverter converter = converterToTest();
            assertThrows(IllegalArgumentException.class, () -> converter.convert(null));
        }

        @Test
        @DisplayName("should return skip result when target type is not an array.")
        public void test2() {
            ArrayConverter converter = converterToTest();

            // Method return type is an int and not an array e.g. String[]
            ProxyMethod proxyMethod = 
                ProxyMethodUtils.fromMethod(
                    PrimitiveProxyInterface.class,
                    "intPrimitiveProperty"
                );
            
            Converter<?> rootConverter = new RootConverter(converter);

            ConversionResult<?> result = converter.convert(new ConversionContext(
                rootConverter,
                proxyMethod,
                "value1,value2,value3"
            ));

            // Method return type is an int and not an array
            assertEquals(ConversionResult.skip(), result);
        }

        @Test
        @DisplayName("should convert value to an array.")
        public void test3() {
            ArrayConverter converter = converterToTest();

            ProxyMethod proxyMethod = 
                ProxyMethodUtils.fromMethod(
                    ArrayProxyInterface.class,
                    "arrayProperty"
                );
            
            Converter<?> rootConverter = new RootConverter(converter);

            ConversionResult<? extends Object[]> result = 
                converter.convert(new ConversionContext(
                    rootConverter,
                    proxyMethod,
                    "value1,value2,value3"
                ));

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
        public void test4() {
            ArrayConverter converter = converterToTest();
            
            ProxyMethod proxyMethod = 
                ProxyMethodUtils.fromMethod(
                    ArrayProxyInterface.class,
                    "arrayIntegerWrapper"
                );
            
            Converter<?> rootConverter = new RootConverter(
                converter,
                new PrimitiveConverter()
            );
            
            ConversionResult<? extends Object[]> result = converter.convert(
                new ConversionContext(
                    rootConverter,
                    proxyMethod,
                    "1,2,3"
                )
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
        public void test5() {
            ArrayConverter converter = converterToTest();
            
            ProxyMethod proxyMethod = 
                ProxyMethodUtils.fromMethod(
                    ArrayProxyInterface.class,
                    "arrayProperty"
                );
            
            Converter<?> rootConverter = new RootConverter(converter);
            
            ConversionResult<? extends Object[]> result = converter.convert(
                new ConversionContext(
                    rootConverter,
                    proxyMethod,
                    "" // Empty value.
                )
            );
            assertNotNull(result);
            Object[] array = result.value();
            
            assertNotNull(array);
            assertEquals(0, array.length);
        }

        @Test
        @DisplayName("should retain empty values from value.")
        public void test6() {
            ArrayConverter converter = converterToTest();
            
            ProxyMethod proxyMethod = 
                ProxyMethodUtils.fromMethod(
                    ArrayProxyInterface.class,
                    "arrayProperty"
                );
            
            Converter<?> rootConverter = new RootConverter(converter);
            
            ConversionResult<? extends Object[]> result = converter.convert(
                new ConversionContext(
                    rootConverter,
                    proxyMethod,
                    "value1,,value3,,value5" // Has empty values.
                )
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
        public void test7() {
            ArrayConverter converter = converterToTest();
            
            ProxyMethod proxyMethod = 
                ProxyMethodUtils.fromMethod(
                    ArrayProxyInterface.class,
                    "arrayPropertyStripEmpty"
                );
            
            Converter<?> rootConverter = new RootConverter(converter);
            
            ConversionResult<? extends Object[]> result = converter.convert(
                new ConversionContext(
                    rootConverter,
                    proxyMethod,
                    "value1,,value3,,value5" // Has empty values.
                )
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
        public void test8() {
            ArrayConverter converter = converterToTest();
            
            ProxyMethod proxyMethod = 
                ProxyMethodUtils.fromMethod(
                    ArrayProxyInterface.class,
                    "arrayPropertyObject"
                );
            
            Converter<?> rootConverter = new RootConverter(converter);
            
            ConversionResult<? extends Object[]> result = converter.convert(
                new ConversionContext(
                    rootConverter,
                    proxyMethod,
                    "value1,value2,value3"
                )
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
        public void test9() {
            ArrayConverter converter = converterToTest();

            ProxyMethod proxyMethod = 
                ProxyMethodUtils.fromMethod(
                    ArrayProxyInterface.class,
                    "arrayIntegerWrapper"
                );
            
            // No registered converter for Integer.
            Converter<?> rootConverter = new RootConverter(converter);
            
            assertThrows(ConversionException.class, () -> {
                converter.convert(
                    new ConversionContext(
                        rootConverter,
                        proxyMethod,
                        "1,2,3,4,5"
                    )
                );
            });
        }

        @Test
        @DisplayName("should use custom delimiter defined by @Delimiter.")
        public void test10() {
            ArrayConverter converter = converterToTest();
            
            ProxyMethod proxyMethod = 
                ProxyMethodUtils.fromMethod(
                    ArrayProxyInterface.class,
                    "arrayCustomDelimiter"
                );
            
            Converter<?> rootConverter = new RootConverter(converter);
            
            ConversionResult<? extends Object[]> result = converter.convert(
                new ConversionContext(
                    rootConverter,
                    proxyMethod,
                    "value1|value2|value3" // Custom delimiter
                )
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
        public void test11() {
            ArrayConverter converter = converterToTest();
            
            ProxyMethod proxyMethod = 
                ProxyMethodUtils.fromMethod(
                    ArrayProxyInterface.class,
                    "arrayPropertyGeneric" // Returns a generic type array Optional<String>[]
                );
            
            Converter<?> rootConverter = new RootConverter(
                converter,
                new OptionalConverter() // Register additional Optional converter.
            );
            
            ConversionResult<? extends Object[]> result = converter.convert(
                new ConversionContext(
                    rootConverter,
                    proxyMethod,
                    "value1,value2,value3"
                )
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
        public void test12() {
            ArrayConverter converter = converterToTest();
            
            ProxyMethod proxyMethod = 
                ProxyMethodUtils.fromMethod(
                    ArrayProxyInterface.class,
                    "arrayPropertyGenericWildcard" // Returns a generic type array Optional<?>[]
                );
            
            Converter<?> rootConverter = new RootConverter(
                converter,
                new OptionalConverter() // Register additional Optional converter.
            );
            
            ConversionResult<? extends Object[]> result = converter.convert(
                new ConversionContext(
                    rootConverter,
                    proxyMethod,
                    "value1,value2,value3"
                )
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
        public void test13() {
            ArrayConverter converter = converterToTest();
            
            ProxyMethod proxyMethod = 
                ProxyMethodUtils.fromMethod(
                    ArrayProxyInterface.class,
                    "arrayPropertyT" // Returns a generic type array <T> T[]
                );
            
            Converter<?> rootConverter = new RootConverter(converter);
            
            ConversionContext context = 
                new ConversionContext(
                    rootConverter,
                    proxyMethod,
                    "value1,value2,value3"
                );
            
            assertThrows(
                ConversionException.class, 
                () -> converter.convert(context)
            );
        }

        /**
         * Non-proxy tests.
         */

        // @Test
        // @DisplayName("should return skip result when target type is not an array.")
        // public void nonProxyTests1() {
        //     ArrayConverter converter = converterToTest();

        //     // Target type is List and not an array e.g. String[]
        //     Converter<?> rootConverter = new RootConverter(converter);

        //     ConversionResult<?> result = converter.convert(new ConversionContext(
        //         rootConverter,
        //         Integer.class,
        //         "value1,value2,value3"
        //     ));

        //     // Method return type is a List and not an array
        //     assertEquals(ConversionResult.skip(), result);
        // }

        // @Test
        // @DisplayName("should convert value to an array.")
        // public void nonProxyTest2() {
        //     ArrayConverter converter = converterToTest();
            
        //     Converter<?> rootConverter = new RootConverter(converter);
            
        //     ConversionResult<? extends Object[]> result = converter.convert(new ConversionContext(
        //         rootConverter,
        //         String[].class,
        //         "value1,,value3,,value5"
        //     ));

        //     assertNotNull(result);
        //     Object[] array = result.value();
            
        //     // Default: Should use ',' as delimiter and will not strip empty values.
        //     // This will strip trailing empty values though.
        //     assertNotNull(array);
        //     assertEquals(5, array.length);
        //     assertTrue(Arrays.stream(array).allMatch(v -> v instanceof String));
        //     assertArrayEquals(
        //         new String[] { "value1", "", "value3", "", "value5" }, 
        //         array
        //     );
        // }

        // @Test
        // @DisplayName("should convert value according to the array's component type.")
        // public void nonProxyTest3() {
        //     ArrayConverter converter = converterToTest();
            
        //     Converter<?> rootConverter = new RootConverter(
        //         converter,
        //         new PrimitiveConverter()
        //     );
            
        //     ConversionResult<? extends Object[]> result = converter.convert(
        //         new ConversionContext(
        //             rootConverter,
        //             Integer[].class,
        //             "1,2,3"
        //         )
        //     );

        //     assertNotNull(result);
        //     Object[] array = result.value();
            
        //     assertNotNull(array);
        //     assertEquals(3, array.length);
        //     assertTrue(Arrays.stream(array).allMatch(v -> v instanceof Integer));
        //     assertArrayEquals(
        //         new Integer[] { 1, 2, 3 }, 
        //         array
        //     );
        // }

        // @Test
        // @DisplayName("should return empty array when value is empty.")
        // public void nonProxyTest4() {
        //     ArrayConverter converter = converterToTest();
            
        //     Converter<?> rootConverter = new RootConverter(converter);
            
        //     ConversionResult<? extends Object[]> result = converter.convert(
        //         new ConversionContext(
        //             rootConverter,
        //             String[].class,
        //             "" // Empty value.
        //         )
        //     );
        //     assertNotNull(result);
        //     Object[] array = result.value();
            
        //     assertNotNull(array);
        //     assertEquals(0, array.length);
        // }

        // @Test
        // @DisplayName("should retain empty values from value.")
        // public void nonProxyTest5() {
        //     ArrayConverter converter = converterToTest();

        //     Converter<?> rootConverter = new RootConverter(converter);
            
        //     ConversionResult<? extends Object[]> result = converter.convert(
        //         new ConversionContext(
        //             rootConverter,
        //             String[].class,
        //             "value1,,value3,,value5" // Has empty values.
        //         )
        //     );

        //     assertNotNull(result);
        //     Object[] array = result.value();
            
        //     assertNotNull(array);
        //     assertEquals(5, array.length);
        //     assertTrue(Arrays.stream(array).allMatch(v -> v instanceof String));
        //     assertArrayEquals(
        //         new String[] { "value1", "", "value3", "", "value5" }, 
        //         array
        //     );
        // }

        // @Test
        // @DisplayName("should return Strings when array component type is Object.")
        // public void nonProxyTest6() {
        //     ArrayConverter converter = converterToTest();
            
        //     Converter<?> rootConverter = new RootConverter(converter);
            
        //     ConversionResult<? extends Object[]> result = converter.convert(
        //         new ConversionContext(
        //             rootConverter,
        //             Object[].class,
        //             "value1,value2,value3"
        //         )
        //     );

        //     assertNotNull(result);
        //     Object[] array = result.value();
            
        //     assertNotNull(array);
        //     assertEquals(3, array.length);
        //     assertTrue(Arrays.stream(array).allMatch(v -> v instanceof String));
        //     assertArrayEquals(
        //         new String[] { "value1", "value2", "value3" }, 
        //         array
        //     );
        // }

        // @Test
        // @DisplayName(
        //     "should throw when no rootConverter is registered that can handle " + 
        //     "the array's component type."
        // )
        // public void nonProxyTest7() {
        //     ArrayConverter converter = converterToTest();
            
        //     // No registered converter for Integer.
        //     Converter<?> rootConverter = new RootConverter(converter);
            
        //     assertThrows(ConversionException.class, () -> {
        //         converter.convert(
        //             new ConversionContext(
        //                 rootConverter,
        //                 Integer[].class,
        //                 "1,2,3,4,5"
        //             )
        //         );
        //     });
        // }

        // @Test
        // @DisplayName(
        //     "should convert value according to the array's generic component type."
        // )
        // public void nonProxyTest8() {
        //     ArrayConverter converter = converterToTest();
            
        //     Converter<?> rootConverter = new RootConverter(
        //         converter,
        //         new OptionalConverter() // Register additional Optional converter.
        //     );
            
        //     ConversionResult<? extends Object[]> result = converter.convert(
        //         new ConversionContext(
        //             rootConverter,
        //             new TypeReference<Optional<String>[]>(){}.type(),
        //             "value1,value2,value3"
        //         )
        //     );

        //     assertNotNull(result);
        //     Object[] array = result.value();
            
        //     assertNotNull(array);
        //     assertEquals(3, array.length);
        //     assertTrue(Arrays.stream(array).allMatch(v -> v instanceof Optional));
        //     assertArrayEquals(
        //         new Optional[] { 
        //             Optional.of("value1"), 
        //             Optional.of("value2"), 
        //             Optional.of("value3")
        //         }, 
        //         array
        //     );
        // }

        // @Test
        // @DisplayName(
        //     "should convert generic type parameter wildcards to Strings."
        // )
        // public void nonProxyTest9() {
        //     ArrayConverter converter = converterToTest();
            
        //     Converter<?> rootConverter = new RootConverter(
        //         converter,
        //         new OptionalConverter() // Register additional Optional converter.
        //     );
            
        //     ConversionResult<? extends Object[]> result = converter.convert(
        //         new ConversionContext(
        //             rootConverter,
        //             new TypeReference<Optional<?>[]>(){}.type(),
        //             "value1,value2,value3"
        //         )
        //     );

        //     assertNotNull(result);
        //     Object[] array = result.value();
            
        //     assertNotNull(array);
        //     assertEquals(3, array.length);
        //     assertTrue(Arrays.stream(array).allMatch(v -> v instanceof Optional));
        //     assertArrayEquals(
        //         new Optional[] { 
        //             Optional.of("value1"), 
        //             Optional.of("value2"), 
        //             Optional.of("value3")
        //         }, 
        //         array
        //     );
        // }

        // @Test
        // @DisplayName("should throw when target type has a type variable e.g. List<T>.")
        // public <T> void nonProxyTest10() {
        //     ArrayConverter converter = converterToTest();
            
        //     Converter<?> rootConverter = new RootConverter(converter);
            
        //     ConversionContext context = new ConversionContext(
        //         rootConverter,
        //         new TypeReference<T[]>(){}.type(),
        //         "value1,value2,value3"
        //     );
            
        //     assertThrows(
        //         ConversionException.class, 
        //         () -> converter.convert(context)
        //     );
        // }
    }

    private ArrayConverter converterToTest() {
        return new ArrayConverter();
    }
}
