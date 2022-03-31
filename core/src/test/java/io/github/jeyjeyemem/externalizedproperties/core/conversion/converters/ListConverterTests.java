package io.github.jeyjeyemem.externalizedproperties.core.conversion.converters;

import io.github.jeyjeyemem.externalizedproperties.core.ConversionContext;
import io.github.jeyjeyemem.externalizedproperties.core.ConversionResult;
import io.github.jeyjeyemem.externalizedproperties.core.Converter;
import io.github.jeyjeyemem.externalizedproperties.core.ExternalizedPropertiesException;
import io.github.jeyjeyemem.externalizedproperties.core.conversion.ConversionException;
import io.github.jeyjeyemem.externalizedproperties.core.internal.conversion.RootConverter;
import io.github.jeyjeyemem.externalizedproperties.core.proxy.ProxyMethod;
import io.github.jeyjeyemem.externalizedproperties.core.testentities.ProxyMethodUtils;
import io.github.jeyjeyemem.externalizedproperties.core.testentities.proxy.ListProxyInterface;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.function.IntFunction;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ListConverterTests {
    @Nested
    class Constructor {
        @Test
        @DisplayName("should throw when list factory argument is null.")
        public void test1() {
            assertThrows(
                IllegalArgumentException.class,
                () -> new ListConverter(null)
            );
        }
    }

    @Nested
    class CanConvertToMethod {
        @Test
        @DisplayName("should return false when target type is null.")
        public void test1() {
            ListConverter converter = rootConverterToTest();
            boolean canConvert = converter.canConvertTo(null);
            assertFalse(canConvert);
        }

        @Test
        @DisplayName("should return true when target type is a List class.")
        public void test2() {
            ListConverter converter = rootConverterToTest();
            boolean canConvert = converter.canConvertTo(List.class);
            assertTrue(canConvert);
        }

        @Test
        @DisplayName("should return true when target type is a Collection class.")
        public void test3() {
            ListConverter converter = rootConverterToTest();
            boolean canConvert = converter.canConvertTo(Collection.class);
            assertTrue(canConvert);
        }



        @Test
        @DisplayName("should return false when target type is not a List/Collection class.")
        public void test4() {
            ListConverter converter = rootConverterToTest();
            boolean canConvert = converter.canConvertTo(String.class);
            assertFalse(canConvert);
        }
    }

    @Nested
    class ConvertMethod {
        @Test
        @DisplayName("should throw when context is null.")
        public void test1() {
            ListConverter converter = rootConverterToTest();
            assertThrows(IllegalArgumentException.class, () -> converter.convert(null));
        }

        @Test
        @DisplayName("should convert value to a List.")
        public void test2() {
            ListConverter converter = rootConverterToTest();

            ProxyMethod proxyMethod = 
                ProxyMethodUtils.fromMethod(
                    ListProxyInterface.class,
                    "listProperty"
                );
            
            Converter<?> rootConverter = new RootConverter(converter);

            ConversionContext context = new ConversionContext(
                rootConverter,
                proxyMethod,
                "value1,value2,value3"
            );

            ConversionResult<? extends List<?>> result = converter.convert(context);
            
            assertNotNull(result);
            List<?> list = result.value();
            
            assertNotNull(list);
            assertEquals(3, list.size());
            assertTrue(list.stream().allMatch(v -> v instanceof String));
            assertIterableEquals(
                Arrays.asList("value1", "value2", "value3"), 
                list
            );
        }

        @Test
        @DisplayName(
            "should convert to List<String> when target type has no " + 
            "type parameters i.e. List.class"
        )
        public void test3() {
            ListConverter converter = rootConverterToTest();

            ProxyMethod proxyMethod = 
                ProxyMethodUtils.fromMethod(
                    ListProxyInterface.class,
                    "listInteger"
                );
            
            Converter<?> rootConverter = new RootConverter(converter);

            ConversionContext context = new ConversionContext(
                rootConverter,
                proxyMethod,
                "1,2,3",
                // Override proxy method return type with a raw List
                // No generic type parameter
                List.class
            );
                
            ConversionResult<? extends List<?>> result = converter.convert(context);

            assertNotNull(result);
            List<?> list = result.value();
            
            assertNotNull(list);
            assertEquals(3, list.size());
            // Strings and not Integers.
            assertTrue(list.stream().allMatch(v -> v instanceof String));
            assertIterableEquals(
                Arrays.asList("1", "2", "3"), 
                list
            );
        }

        @Test
        @DisplayName("should convert value to a List using custom delimiter.")
        public void test4() {
            ListConverter converter = rootConverterToTest();

            ProxyMethod proxyMethod = 
                ProxyMethodUtils.fromMethod(
                    ListProxyInterface.class,
                    "listCustomDelimiter"
                );
            
            Converter<?> rootConverter = new RootConverter(converter);
            
            ConversionContext context = new ConversionContext(
                rootConverter,
                proxyMethod,
                "value1#value2#value3"
            );

            ConversionResult<? extends List<?>> result = converter.convert(context);

            assertNotNull(result);
            List<?> list = result.value();
            
            assertNotNull(list);
            assertEquals(3, list.size());
            assertTrue(list.stream().allMatch(v -> v instanceof String));
            assertIterableEquals(
                Arrays.asList("value1", "value2", "value3"), 
                list
            );

        }

        @Test
        @DisplayName("should convert value according to the List's generic type parameter.")
        public void test5() {
            ListConverter converter = rootConverterToTest();

            ProxyMethod proxyMethod = 
                ProxyMethodUtils.fromMethod(
                    ListProxyInterface.class,
                    "listInteger"
                );
            
            Converter<?> rootConverter = new RootConverter(
                converter,
                new PrimitiveConverter()
            );

            ConversionContext context = new ConversionContext(
                rootConverter,
                proxyMethod,
                "1,2,3"
            );
            
            ConversionResult<? extends List<?>> result = converter.convert(context);
            
            assertNotNull(result);
            List<?> list = result.value();
            
            assertNotNull(list);
            assertEquals(3, list.size());
            assertTrue(list.stream().allMatch(v -> v instanceof Integer));
            assertIterableEquals(
                Arrays.asList(1, 2, 3), 
                list
            );

        }

        @Test
        @DisplayName(
            "should return String values when List's generic type parameter is a wildcard."
        )
        public void test6() {
            ListConverter converter = rootConverterToTest();

            ProxyMethod proxyMethod = 
                ProxyMethodUtils.fromMethod(
                    ListProxyInterface.class,
                    "listPropertyWildcard"
                );
            
            Converter<?> rootConverter = new RootConverter(converter);

            ConversionContext context = new ConversionContext(
                rootConverter,
                proxyMethod,
                "value1,value2,value3"
            );

            ConversionResult<? extends List<?>> result = converter.convert(context);
            
            assertNotNull(result);
            List<?> list = result.value();
            
            assertNotNull(list);
            assertEquals(3, list.size());
            assertTrue(list.stream().allMatch(v -> v instanceof String));
            assertIterableEquals(
                Arrays.asList("value1", "value2", "value3"), 
                list
            );

        }

        @Test
        @DisplayName("should return String values when List's generic type parameter is Object.")
        public void test7() {
            ListConverter converter = rootConverterToTest();

            ProxyMethod proxyMethod = 
                ProxyMethodUtils.fromMethod(
                    ListProxyInterface.class,
                    "listPropertyObject"
                );
            
            Converter<?> rootConverter = new RootConverter(converter);

            ConversionContext context = 
                new ConversionContext(
                    rootConverter,
                    proxyMethod,
                    "value1,value2,value3"
                );

            ConversionResult<? extends List<?>> result = converter.convert(context);
        
            assertNotNull(result);
            List<?> list = result.value();
            
            assertNotNull(list);
            assertEquals(3, list.size());
            assertTrue(list.stream().allMatch(v -> v instanceof String));
            assertIterableEquals(
                Arrays.asList("value1", "value2", "value3"), 
                list
            );

        }

        @Test
        @DisplayName("should return empty List when property value is empty.")
        public void test8() {
            ListConverter converter = rootConverterToTest();

            ProxyMethod proxyMethod = 
                ProxyMethodUtils.fromMethod(
                    ListProxyInterface.class,
                    "listProperty"
                );
            
            Converter<?> rootConverter = new RootConverter(converter);

            ConversionContext context = new ConversionContext(
                rootConverter,
                proxyMethod,
                "" // Empty value.
            );

            ConversionResult<? extends List<?>> result = converter.convert(context);
            
            assertNotNull(result);
            List<?> list = result.value();
            
            assertNotNull(list);
            assertTrue(list.isEmpty());
        }

        @Test
        @DisplayName("should retain empty values from property value.")
        public void test9() {
            ListConverter converter = rootConverterToTest();

            ProxyMethod proxyMethod = 
                ProxyMethodUtils.fromMethod(
                    ListProxyInterface.class,
                    "listProperty"
                );
            
            Converter<?> rootConverter = new RootConverter(converter);

            ConversionContext context = 
                new ConversionContext(
                    rootConverter,
                    proxyMethod,
                    "value1,,value3,,value5" // Has empty values.
                );

            ConversionResult<? extends List<?>> result = converter.convert(context);
        
            assertNotNull(result);
            List<?> list = result.value();
            
            assertNotNull(list);
            assertEquals(5, list.size());
            assertTrue(list.stream().allMatch(v -> v instanceof String));
            assertIterableEquals(
                Arrays.asList("value1", "", "value3", "", "value5"), 
                list
            );

        }

        @Test
        @DisplayName("should strip empty values when annotated with @StripEmptyValues.")
        public void test10() {
            ListConverter converter = rootConverterToTest();

            ProxyMethod proxyMethod = 
                ProxyMethodUtils.fromMethod(
                    ListProxyInterface.class,
                    "listPropertyStripEmpty"
                );
            
            Converter<?> rootConverter = new RootConverter(converter);

            ConversionContext context = new ConversionContext(
                rootConverter,
                proxyMethod,
                "value1,,value3,,value5" // Has empty values.
            );

            ConversionResult<? extends List<?>> result = converter.convert(context);
            
            assertNotNull(result);
            List<?> list = result.value();
            
            assertNotNull(list);
            assertEquals(3, list.size());
            assertTrue(list.stream().allMatch(v -> v instanceof String));
            assertIterableEquals(
                Arrays.asList("value1", "value3", "value5"), 
                list
            );
        }

        @Test
        @DisplayName(
            "should throw when no rootConverter is registered that can handle " + 
            "the List's generic type parameter."
        )
        public void test11() {
            ListConverter converter = rootConverterToTest();

            ProxyMethod proxyMethod = 
                ProxyMethodUtils.fromMethod(
                    ListProxyInterface.class,
                    "listInteger"
                );
            
            Converter<?> rootConverter = new RootConverter(converter);
            
            // No registered rootConverter for Integer.
            assertThrows(ExternalizedPropertiesException.class, () -> {
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
        @DisplayName(
            "should convert value according to the List's generic type parameter. " + 
            "Generic type parameter is also a parameterized type e.g. List<Optional<String>>."
        )
        public void test12() {
            ListConverter converter = rootConverterToTest();

            ProxyMethod proxyMethod = 
                ProxyMethodUtils.fromMethod(
                    ListProxyInterface.class,
                    "listPropertyNestedGenerics" // Returns a List<Optional<String>>.
                );
            
            Converter<?> rootConverter = new RootConverter(
                converter,
                new OptionalConverter() // Register additional Optional converter.
            );

            ConversionContext context = new ConversionContext(
                rootConverter,
                proxyMethod,
                "value1,value2,value3"
            );

            ConversionResult<? extends List<?>> result = converter.convert(context);
            
            assertNotNull(result);
            List<?> list = result.value();
            
            assertNotNull(list);
            assertEquals(3, list.size());
            assertTrue(list.stream().allMatch(v -> v instanceof Optional<?>));
            assertIterableEquals(
                Arrays.asList(
                    Optional.of("value1"), 
                    Optional.of("value2"), 
                    Optional.of("value3")
                ), 
                list
            );
        }

        @Test
        @DisplayName(
            "should convert value according to the List's generic type parameter. " + 
            "Generic type parameter is generic array e.g. List<Optional<String>[]>."
        )
        public void test13() {
            ListConverter converter = rootConverterToTest();

            ProxyMethod proxyMethod = 
                ProxyMethodUtils.fromMethod(
                    ListProxyInterface.class,
                    "listPropertyNestedGenericsArray" // Returns a List<Optional<String>[]>.
                );
            
            Converter<?> rootConverter = new RootConverter(
                converter,
                new ArrayConverter(), // Register additional array converter.
                new OptionalConverter() // Register additional Optional converter.
            );

            ConversionContext context = new ConversionContext(
                rootConverter,
                proxyMethod,
                "value1,value2,value3"
            );

            ConversionResult<? extends List<?>> result = converter.convert(context);
            
            assertNotNull(result);
            List<?> list = result.value();
            
            assertNotNull(list);
            assertEquals(3, list.size());
            assertTrue(list.stream().allMatch(v -> v instanceof Optional<?>[]));

            // List is a list of arrays (List<Optional<String>[]>).
            
            Optional<?>[] item1 = (Optional<?>[])list.get(0);
            Optional<?>[] item2 = (Optional<?>[])list.get(1);
            Optional<?>[] item3 = (Optional<?>[])list.get(2);

            assertArrayEquals(
                new Optional<?>[] { Optional.of("value1") }, 
                item1
            );

            assertArrayEquals(
                new Optional<?>[] { Optional.of("value2") }, 
                item2
            );
            
            assertArrayEquals(
                new Optional<?>[] { Optional.of("value3") }, 
                item3
            );
        }

        @Test
        @DisplayName("should throw when target type has a type variable e.g. List<T>.")
        public void test14() {
            ListConverter converter = rootConverterToTest();

            ProxyMethod proxyMethod = 
                ProxyMethodUtils.fromMethod(
                    ListProxyInterface.class,
                    "listPropertyT"
                );
            
            Converter<?> rootConverter = new RootConverter(converter);

            ConversionContext context = new ConversionContext(
                rootConverter,
                proxyMethod,
                "value"
            );
                
            assertThrows(
                ConversionException.class, 
                () -> converter.convert(context)
            );
        }

        /**
         * List factory tests.
         */
        
        @Test
        @DisplayName(
            "should use provided list factory to construct lists/collections."
        )
        public void listFactoryTest1() {
            ListConverter converter = converterToTest(
                // Uses linked list.
                length -> new LinkedList<>()
            );

            ProxyMethod proxyMethod = 
                ProxyMethodUtils.fromMethod(
                    ListProxyInterface.class,
                    "listProperty"
                );
            
            Converter<?> rootConverter = new RootConverter(converter);
            
            ConversionContext context = new ConversionContext(
                rootConverter,
                proxyMethod,
                "value1,value2,value3,value4,value5"
            );

            ConversionResult<? extends List<?>> result = converter.convert(context);
            
            assertNotNull(result);
            List<?> list = result.value();
            
            // Default: Should use ',' as delimiter and will not strip empty values.
            // This will strip trailing empty values though.
            assertNotNull(list);
            assertTrue(list instanceof LinkedList);
            assertEquals(5, list.size());
            assertTrue(list.stream().allMatch(v -> v instanceof String));
            assertIterableEquals(
                Arrays.asList("value1", "value2", "value3", "value4", "value5"), 
                list
            );
        }
        
        @Test
        @DisplayName(
            "should throw when provided list factory returns null."
        )
        public void listFactoryTest2() {
            ListConverter converter = converterToTest(
                // Returns null.
                length -> null
            );

            ProxyMethod proxyMethod = 
                ProxyMethodUtils.fromMethod(
                    ListProxyInterface.class,
                    "listProperty"
                );
            
            Converter<?> rootConverter = new RootConverter(converter);
            
            ConversionContext context = new ConversionContext(
                rootConverter,
                proxyMethod,
                "value1,value2,value3,value4,value5"
            );

            // Throws IllegalStateException if list factory returned null.
            assertThrows(
                IllegalStateException.class, 
                () -> converter.convert(context)
            );
        }

        /**
         * Non-proxy tests
         */
        
        // @Test
        // @DisplayName(
        //     "should convert value to a List."
        // )
        // public void nonProxyTest1() {
        //     ListConverter converter = converterToTest();
            
        //     Converter<?> rootConverter = new RootConverter(converter);
            
        //     ConversionContext context = new ConversionContext(
        //         rootConverter,
        //         List.class,
        //         "value1,,value3,,value5"
        //     );

        //     ConversionResult<? extends List<?>> result = converter.convert(context);
            
        //     assertNotNull(result);
        //     List<?> list = result.value();
            
        //     // Default: Should use ',' as delimiter and will not strip empty values.
        //     // This will strip trailing empty values though.
        //     assertNotNull(list);
        //     assertEquals(5, list.size());
        //     assertTrue(list.stream().allMatch(v -> v instanceof String));
        //     assertIterableEquals(
        //         Arrays.asList("value1", "", "value3", "", "value5"), 
        //         list
        //     );
        // }

        // @Test
        // @DisplayName("should convert value according to the List's generic type parameter.")
        // public void nonProxyTest2() {
        //     ListConverter converter = converterToTest();
            
        //     Converter<?> rootConverter = new RootConverter(
        //         converter,
        //         new PrimitiveConverter()
        //     );

        //     ConversionContext context = new ConversionContext(
        //         rootConverter,
        //         new TypeReference<List<Integer>>(){}.type(),
        //         "1,2,3"
        //     );
            
        //     ConversionResult<? extends List<?>> result = converter.convert(context);
            
        //     assertNotNull(result);
        //     List<?> list = result.value();
            
        //     assertNotNull(list);
        //     assertEquals(3, list.size());
        //     assertTrue(list.stream().allMatch(v -> v instanceof Integer));
        //     assertIterableEquals(
        //         Arrays.asList(1, 2, 3), 
        //         list
        //     );

        // }

        // @Test
        // @DisplayName(
        //     "should return String values when List's generic type parameter is a wildcard."
        // )
        // public void nonProxyTest3() {
        //     ListConverter converter = converterToTest();
            
        //     Converter<?> rootConverter = new RootConverter(converter);

        //     ConversionContext context = new ConversionContext(
        //         rootConverter,
        //         new TypeReference<List<?>>(){}.type(),
        //         "value1,value2,value3"
        //     );

        //     ConversionResult<? extends List<?>> result = converter.convert(context);
            
        //     assertNotNull(result);
        //     List<?> list = result.value();
            
        //     assertNotNull(list);
        //     assertEquals(3, list.size());
        //     assertTrue(list.stream().allMatch(v -> v instanceof String));
        //     assertIterableEquals(
        //         Arrays.asList("value1", "value2", "value3"), 
        //         list
        //     );

        // }

        // @Test
        // @DisplayName("should return String values when List's generic type parameter is Object.")
        // public void nonProxyTest4() {
        //     ListConverter converter = converterToTest();

        //     Converter<?> rootConverter = new RootConverter(converter);

        //     ConversionContext context = new ConversionContext(
        //         rootConverter,
        //         new TypeReference<List<Object>>(){}.type(),
        //         "value1,value2,value3"
        //     );

        //     ConversionResult<? extends List<?>> result = converter.convert(context);
        
        //     assertNotNull(result);
        //     List<?> list = result.value();
            
        //     assertNotNull(list);
        //     assertEquals(3, list.size());
        //     assertTrue(list.stream().allMatch(v -> v instanceof String));
        //     assertIterableEquals(
        //         Arrays.asList("value1", "value2", "value3"), 
        //         list
        //     );

        // }

        // @Test
        // @DisplayName("should return empty List when property value is empty.")
        // public void nonProxyTest5() {
        //     ListConverter converter = converterToTest();
            
        //     Converter<?> rootConverter = new RootConverter(converter);

        //     ConversionContext context = new ConversionContext(
        //         rootConverter,
        //         List.class,
        //         "" // Empty value.
        //     );

        //     ConversionResult<? extends List<?>> result = converter.convert(context);
            
        //     assertNotNull(result);
        //     List<?> list = result.value();
            
        //     assertNotNull(list);
        //     assertTrue(list.isEmpty());
        // }

        // @Test
        // @DisplayName("should retain empty values from property value.")
        // public void nonProxyTest6() {
        //     ListConverter converter = converterToTest();
            
        //     Converter<?> rootConverter = new RootConverter(converter);

        //     ConversionContext context = new ConversionContext(
        //         rootConverter,
        //         List.class,
        //         "value1,,value3,,value5" // Has empty values.
        //     );

        //     ConversionResult<? extends List<?>> result = converter.convert(context);
        
        //     assertNotNull(result);
        //     List<?> list = result.value();
            
        //     assertNotNull(list);
        //     assertEquals(5, list.size());
        //     assertTrue(list.stream().allMatch(v -> v instanceof String));
        //     assertIterableEquals(
        //         Arrays.asList("value1", "", "value3", "", "value5"), 
        //         list
        //     );
        // }

        // @Test
        // @DisplayName(
        //     "should throw when no rootConverter is registered that can handle " + 
        //     "the List's generic type parameter."
        // )
        // public void nonProxyTest7() {
        //     ListConverter converter = converterToTest();
            
        //     Converter<?> rootConverter = new RootConverter(converter);
            
        //     // No registered rootConverter for Integer.
        //     assertThrows(ExternalizedPropertiesException.class, () -> {
        //         converter.convert(
        //             new ConversionContext(
        //                 rootConverter,
        //                 new TypeReference<List<Integer>>(){}.type(),
        //                 "1,2,3,4,5"
        //             )
        //         );
        //     });
        // }

        // @Test
        // @DisplayName(
        //     "should convert value according to the List's generic type parameter. " + 
        //     "Generic type parameter is also a parameterized type e.g. List<Optional<String>>."
        // )
        // public void nonProxyTest8() {
        //     ListConverter converter = converterToTest();
            
        //     Converter<?> rootConverter = new RootConverter(
        //         converter,
        //         new OptionalConverter() // Register additional Optional converter.
        //     );

        //     ConversionContext context = new ConversionContext(
        //         rootConverter,
        //         new TypeReference<List<Optional<String>>>(){}.type(),
        //         "value1,value2,value3"
        //     );

        //     ConversionResult<? extends List<?>> result = converter.convert(context);
            
        //     assertNotNull(result);
        //     List<?> list = result.value();
            
        //     assertNotNull(list);
        //     assertEquals(3, list.size());
        //     assertTrue(list.stream().allMatch(v -> v instanceof Optional<?>));
        //     assertIterableEquals(
        //         Arrays.asList(
        //             Optional.of("value1"), 
        //             Optional.of("value2"), 
        //             Optional.of("value3")
        //         ), 
        //         list
        //     );
        // }

        // @Test
        // @DisplayName(
        //     "should convert value according to the List's generic type parameter. " + 
        //     "Generic type parameter is generic array e.g. List<Optional<String>[]>."
        // )
        // public void nonProxyTest9() {
        //     ListConverter converter = converterToTest();
            
        //     Converter<?> rootConverter = new RootConverter(
        //         converter,
        //         new ArrayConverter(), // Register additional array converter.
        //         new OptionalConverter() // Register additional Optional converter.
        //     );

        //     ConversionContext context = new ConversionContext(
        //         rootConverter,
        //         new TypeReference<List<Optional<String>[]>>(){}.type(),
        //         "value1,value2,value3"
        //     );

        //     ConversionResult<? extends List<?>> result = converter.convert(context);
            
        //     assertNotNull(result);
        //     List<?> list = result.value();
            
        //     assertNotNull(list);
        //     assertEquals(3, list.size());
        //     assertTrue(list.stream().allMatch(v -> v instanceof Optional<?>[]));

        //     // List is a list of arrays (List<Optional<String>[]>).
            
        //     Optional<?>[] item1 = (Optional<?>[])list.get(0);
        //     Optional<?>[] item2 = (Optional<?>[])list.get(1);
        //     Optional<?>[] item3 = (Optional<?>[])list.get(2);

        //     assertArrayEquals(
        //         new Optional<?>[] { Optional.of("value1") }, 
        //         item1
        //     );

        //     assertArrayEquals(
        //         new Optional<?>[] { Optional.of("value2") }, 
        //         item2
        //     );
            
        //     assertArrayEquals(
        //         new Optional<?>[] { Optional.of("value3") }, 
        //         item3
        //     );
        // }

        // @Test
        // @DisplayName("should throw when target type has a type variable e.g. List<T>.")
        // public <T> void nonProxyTest10() {
        //     ListConverter converter = converterToTest();
            
        //     Converter<?> rootConverter = new RootConverter(converter);

        //     ConversionContext context = new ConversionContext(
        //         rootConverter,
        //         new TypeReference<List<T>>(){}.type(),
        //         "value"
        //     );
                
        //     assertThrows(
        //         ConversionException.class, 
        //         () -> converter.convert(context)
        //     );
        // }
    }

    private ListConverter rootConverterToTest() {
        return new ListConverter();
    }

    private ListConverter converterToTest(IntFunction<List<?>> listFactory) {
        return new ListConverter(listFactory);
    }
}
