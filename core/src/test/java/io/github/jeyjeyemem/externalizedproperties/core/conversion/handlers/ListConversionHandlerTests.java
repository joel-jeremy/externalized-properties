package io.github.jeyjeyemem.externalizedproperties.core.conversion.handlers;

import io.github.jeyjeyemem.externalizedproperties.core.conversion.Converter;
import io.github.jeyjeyemem.externalizedproperties.core.TypeReference;
import io.github.jeyjeyemem.externalizedproperties.core.conversion.ConversionContext;
import io.github.jeyjeyemem.externalizedproperties.core.conversion.ConversionResult;
import io.github.jeyjeyemem.externalizedproperties.core.exceptions.ConversionException;
import io.github.jeyjeyemem.externalizedproperties.core.exceptions.ExternalizedPropertiesException;
import io.github.jeyjeyemem.externalizedproperties.core.internal.conversion.InternalConverter;
import io.github.jeyjeyemem.externalizedproperties.core.proxy.ProxyMethodInfo;
import io.github.jeyjeyemem.externalizedproperties.core.testentities.StubProxyMethodInfo;
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

public class ListConversionHandlerTests {
    @Nested
    class Constructor {
        @Test
        @DisplayName("should throw when list factory argument is null.")
        public void test1() {
            assertThrows(
                IllegalArgumentException.class,
                () -> new ListConversionHandler(null)
            );
        }
    }

    @Nested
    class CanConvertToMethod {
        @Test
        @DisplayName("should return false when target type is null.")
        public void test1() {
            ListConversionHandler handler = handlerToTest();
            boolean canConvert = handler.canConvertTo(null);
            assertFalse(canConvert);
        }

        @Test
        @DisplayName("should return true when target type is a List class.")
        public void test2() {
            ListConversionHandler handler = handlerToTest();
            boolean canConvert = handler.canConvertTo(List.class);
            assertTrue(canConvert);
        }

        @Test
        @DisplayName("should return true when target type is a Collection class.")
        public void test3() {
            ListConversionHandler handler = handlerToTest();
            boolean canConvert = handler.canConvertTo(Collection.class);
            assertTrue(canConvert);
        }



        @Test
        @DisplayName("should return false when target type is not a List/Collection class.")
        public void test4() {
            ListConversionHandler handler = handlerToTest();
            boolean canConvert = handler.canConvertTo(String.class);
            assertFalse(canConvert);
        }
    }

    @Nested
    class ConvertMethod {
        @Test
        @DisplayName("should throw when context is null.")
        public void test1() {
            ListConversionHandler handler = handlerToTest();
            assertThrows(IllegalArgumentException.class, () -> handler.convert(null));
        }

        @Test
        @DisplayName("should convert value to a List.")
        public void test2() {
            ListConversionHandler handler = handlerToTest();

            ProxyMethodInfo proxyMethodInfo = 
                StubProxyMethodInfo.fromMethod(
                    ListProxyInterface.class,
                    "listProperty"
                );
            
            Converter converter = new InternalConverter(handler);

            ConversionContext context = new ConversionContext(
                converter,
                proxyMethodInfo,
                "value1,value2,value3"
            );

            ConversionResult<List<?>> result = handler.convert(context);
            
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
        @DisplayName("should convert value to a List using custom delimiter.")
        public void test3() {
            ListConversionHandler handler = handlerToTest();

            ProxyMethodInfo proxyMethodInfo = 
                StubProxyMethodInfo.fromMethod(
                    ListProxyInterface.class,
                    "listCustomDelimiter"
                );
            
            Converter converter = new InternalConverter(handler);
            
            ConversionContext context = new ConversionContext(
                converter,
                proxyMethodInfo,
                "value1#value2#value3"
            );

            ConversionResult<List<?>> result = handler.convert(context);

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
        public void test4() {
            ListConversionHandler handler = handlerToTest();

            ProxyMethodInfo proxyMethodInfo = 
                StubProxyMethodInfo.fromMethod(
                    ListProxyInterface.class,
                    "listInteger"
                );
            
            Converter converter = new InternalConverter(
                handler,
                new PrimitiveConversionHandler()
            );

            ConversionContext context = new ConversionContext(
                converter,
                proxyMethodInfo,
                "1,2,3"
            );
            
            ConversionResult<List<?>> result = handler.convert(context);
            
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
        @DisplayName("should return String values when List's generic type parameter is a wildcard.")
        public void test5() {
            ListConversionHandler handler = handlerToTest();

            ProxyMethodInfo proxyMethodInfo = 
                StubProxyMethodInfo.fromMethod(
                    ListProxyInterface.class,
                    "listPropertyWildcard"
                );
            
            Converter converter = new InternalConverter(handler);

            ConversionContext context = new ConversionContext(
                converter,
                proxyMethodInfo,
                "value1,value2,value3"
            );

            ConversionResult<List<?>> result = handler.convert(context);
            
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
        public void test6() {
            ListConversionHandler handler = handlerToTest();

            ProxyMethodInfo proxyMethodInfo = 
                StubProxyMethodInfo.fromMethod(
                    ListProxyInterface.class,
                    "listPropertyObject"
                );
            
            Converter converter = new InternalConverter(handler);

            ConversionContext context = 
                new ConversionContext(
                    converter,
                    proxyMethodInfo,
                    "value1,value2,value3"
                );

            ConversionResult<List<?>> result = handler.convert(context);
        
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
        public void test7() {
            ListConversionHandler handler = handlerToTest();

            ProxyMethodInfo proxyMethodInfo = 
                StubProxyMethodInfo.fromMethod(
                    ListProxyInterface.class,
                    "listProperty"
                );
            
            Converter converter = new InternalConverter(handler);

            ConversionContext context = new ConversionContext(
                converter,
                proxyMethodInfo,
                "" // Empty value.
            );

            ConversionResult<List<?>> result = handler.convert(context);
            
            assertNotNull(result);
            List<?> list = result.value();
            
            assertNotNull(list);
            assertTrue(list.isEmpty());
        }

        @Test
        @DisplayName("should retain empty values from property value.")
        public void test8() {
            ListConversionHandler handler = handlerToTest();

            ProxyMethodInfo proxyMethodInfo = 
                StubProxyMethodInfo.fromMethod(
                    ListProxyInterface.class,
                    "listProperty"
                );
            
            Converter converter = new InternalConverter(handler);

            ConversionContext context = 
                new ConversionContext(
                    converter,
                    proxyMethodInfo,
                    "value1,,value3,,value5" // Has empty values.
                );

            ConversionResult<List<?>> result = handler.convert(context);
        
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
        public void test9() {
            ListConversionHandler handler = handlerToTest();

            ProxyMethodInfo proxyMethodInfo = 
                StubProxyMethodInfo.fromMethod(
                    ListProxyInterface.class,
                    "listPropertyStripEmpty"
                );
            
            Converter converter = new InternalConverter(handler);

            ConversionContext context = new ConversionContext(
                converter,
                proxyMethodInfo,
                "value1,,value3,,value5" // Has empty values.
            );

            ConversionResult<List<?>> result = handler.convert(context);
            
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
            "should throw when no conversion handler is registered that can handle " + 
            "the List's generic type parameter."
        )
        public void test10() {
            ListConversionHandler handler = handlerToTest();

            ProxyMethodInfo proxyMethodInfo = 
                StubProxyMethodInfo.fromMethod(
                    ListProxyInterface.class,
                    "listInteger"
                );
            
            Converter converter = new InternalConverter(handler);
            
            // No registered conversion handler for Integer.
            assertThrows(ExternalizedPropertiesException.class, () -> {
                handler.convert(
                    new ConversionContext(
                        converter,
                        proxyMethodInfo,
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
        public void test11() {
            ListConversionHandler handler = handlerToTest();

            ProxyMethodInfo proxyMethodInfo = 
                StubProxyMethodInfo.fromMethod(
                    ListProxyInterface.class,
                    "listPropertyNestedGenerics" // Returns a List<Optional<String>>.
                );
            
            Converter converter = new InternalConverter(
                handler,
                new OptionalConversionHandler() // Register additional Optional handler.
            );

            ConversionContext context = new ConversionContext(
                converter,
                proxyMethodInfo,
                "value1,value2,value3"
            );

            ConversionResult<List<?>> result = handler.convert(context);
            
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
        public void test12() {
            ListConversionHandler handler = handlerToTest();

            ProxyMethodInfo proxyMethodInfo = 
                StubProxyMethodInfo.fromMethod(
                    ListProxyInterface.class,
                    "listPropertyNestedGenericsArray" // Returns a List<Optional<String>[]>.
                );
            
            Converter converter = new InternalConverter(
                handler,
                new ArrayConversionHandler(), // Register additional array handler.
                new OptionalConversionHandler() // Register additional Optional handler.
            );

            ConversionContext context = new ConversionContext(
                converter,
                proxyMethodInfo,
                "value1,value2,value3"
            );

            ConversionResult<List<?>> result = handler.convert(context);
            
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
        public void test13() {
            ListConversionHandler handler = handlerToTest();

            ProxyMethodInfo proxyMethodInfo = 
                StubProxyMethodInfo.fromMethod(
                    ListProxyInterface.class,
                    "listPropertyT"
                );
            
            Converter converter = new InternalConverter(handler);

            ConversionContext context = new ConversionContext(
                converter,
                proxyMethodInfo,
                "value"
            );
                
            assertThrows(
                ConversionException.class, 
                () -> handler.convert(context)
            );
        }

        /**
         * Non-proxy tests
         */
        
        @Test
        @DisplayName(
            "should convert value to a List."
        )
        public void nonProxyTest1() {
            ListConversionHandler handler = handlerToTest();
            
            Converter converter = new InternalConverter(handler);
            
            ConversionContext context = new ConversionContext(
                converter,
                List.class,
                "value1,,value3,,value5"
            );

            ConversionResult<List<?>> result = handler.convert(context);
            
            assertNotNull(result);
            List<?> list = result.value();
            
            // Default: Should use ',' as delimiter and will not strip empty values.
            // This will strip trailing empty values though.
            assertNotNull(list);
            assertEquals(5, list.size());
            assertTrue(list.stream().allMatch(v -> v instanceof String));
            assertIterableEquals(
                Arrays.asList("value1", "", "value3", "", "value5"), 
                list
            );
        }

        @Test
        @DisplayName("should convert value according to the List's generic type parameter.")
        public void nonProxyTest2() {
            ListConversionHandler handler = handlerToTest();
            
            Converter converter = new InternalConverter(
                handler,
                new PrimitiveConversionHandler()
            );

            ConversionContext context = new ConversionContext(
                converter,
                new TypeReference<List<Integer>>(){}.type(),
                "1,2,3"
            );
            
            ConversionResult<List<?>> result = handler.convert(context);
            
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
        @DisplayName("should return String values when List's generic type parameter is a wildcard.")
        public void nonProxyTest3() {
            ListConversionHandler handler = handlerToTest();
            
            Converter converter = new InternalConverter(handler);

            ConversionContext context = new ConversionContext(
                converter,
                new TypeReference<List<?>>(){}.type(),
                "value1,value2,value3"
            );

            ConversionResult<List<?>> result = handler.convert(context);
            
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
        public void nonProxyTest4() {
            ListConversionHandler handler = handlerToTest();

            Converter converter = new InternalConverter(handler);

            ConversionContext context = new ConversionContext(
                converter,
                new TypeReference<List<Object>>(){}.type(),
                "value1,value2,value3"
            );

            ConversionResult<List<?>> result = handler.convert(context);
        
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
        public void nonProxyTest5() {
            ListConversionHandler handler = handlerToTest();
            
            Converter converter = new InternalConverter(handler);

            ConversionContext context = new ConversionContext(
                converter,
                List.class,
                "" // Empty value.
            );

            ConversionResult<List<?>> result = handler.convert(context);
            
            assertNotNull(result);
            List<?> list = result.value();
            
            assertNotNull(list);
            assertTrue(list.isEmpty());
        }

        @Test
        @DisplayName("should retain empty values from property value.")
        public void nonProxyTest6() {
            ListConversionHandler handler = handlerToTest();
            
            Converter converter = new InternalConverter(handler);

            ConversionContext context = new ConversionContext(
                converter,
                List.class,
                "value1,,value3,,value5" // Has empty values.
            );

            ConversionResult<List<?>> result = handler.convert(context);
        
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
        @DisplayName(
            "should throw when no conversion handler is registered that can handle " + 
            "the List's generic type parameter."
        )
        public void nonProxyTest7() {
            ListConversionHandler handler = handlerToTest();
            
            Converter converter = new InternalConverter(handler);
            
            // No registered conversion handler for Integer.
            assertThrows(ExternalizedPropertiesException.class, () -> {
                handler.convert(
                    new ConversionContext(
                        converter,
                        new TypeReference<List<Integer>>(){}.type(),
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
        public void nonProxyTest8() {
            ListConversionHandler handler = handlerToTest();
            
            Converter converter = new InternalConverter(
                handler,
                new OptionalConversionHandler() // Register additional Optional handler.
            );

            ConversionContext context = new ConversionContext(
                converter,
                new TypeReference<List<Optional<String>>>(){}.type(),
                "value1,value2,value3"
            );

            ConversionResult<List<?>> result = handler.convert(context);
            
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
        public void nonProxyTest9() {
            ListConversionHandler handler = handlerToTest();
            
            Converter converter = new InternalConverter(
                handler,
                new ArrayConversionHandler(), // Register additional array handler.
                new OptionalConversionHandler() // Register additional Optional handler.
            );

            ConversionContext context = new ConversionContext(
                converter,
                new TypeReference<List<Optional<String>[]>>(){}.type(),
                "value1,value2,value3"
            );

            ConversionResult<List<?>> result = handler.convert(context);
            
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
        public <T> void nonProxyTest10() {
            ListConversionHandler handler = handlerToTest();
            
            Converter converter = new InternalConverter(handler);

            ConversionContext context = new ConversionContext(
                converter,
                new TypeReference<List<T>>(){}.type(),
                "value"
            );
                
            assertThrows(
                ConversionException.class, 
                () -> handler.convert(context)
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
            ListConversionHandler handler = handlerToTest(
                // Uses linked list.
                length -> new LinkedList<>()
            );
            
            Converter converter = new InternalConverter(handler);
            
            ConversionContext context = new ConversionContext(
                converter,
                List.class,
                "value1,,value3,,value5"
            );

            ConversionResult<List<?>> result = handler.convert(context);
            
            assertNotNull(result);
            List<?> list = result.value();
            
            // Default: Should use ',' as delimiter and will not strip empty values.
            // This will strip trailing empty values though.
            assertNotNull(list);
            assertTrue(list instanceof LinkedList);
            assertEquals(5, list.size());
            assertTrue(list.stream().allMatch(v -> v instanceof String));
            assertIterableEquals(
                Arrays.asList("value1", "", "value3", "", "value5"), 
                list
            );
        }
        
        @Test
        @DisplayName(
            "should throw when provided list factory returns null."
        )
        public void listFactoryTest2() {
            ListConversionHandler handler = handlerToTest(
                // Returns null.
                length -> null
            );
            
            Converter converter = new InternalConverter(handler);
            
            ConversionContext context = new ConversionContext(
                converter,
                List.class,
                "value1,,value3,,value5"
            );

            // Throws IllegalStateException if list factory returned null.
            assertThrows(
                IllegalStateException.class, 
                () -> handler.convert(context)
            );
        }
    }

    private ListConversionHandler handlerToTest() {
        return new ListConversionHandler();
    }

    private ListConversionHandler handlerToTest(IntFunction<List<?>> listFactory) {
        return new ListConversionHandler(listFactory);
    }
}
