package io.github.jeyjeyemem.externalizedproperties.core.conversion.handlers;

import io.github.jeyjeyemem.externalizedproperties.core.ExternalizedPropertyMethodInfo;
import io.github.jeyjeyemem.externalizedproperties.core.TypeReference;
import io.github.jeyjeyemem.externalizedproperties.core.conversion.ConversionContext;
import io.github.jeyjeyemem.externalizedproperties.core.conversion.Converter;
import io.github.jeyjeyemem.externalizedproperties.core.conversion.PropertyMethodConversionContext;
import io.github.jeyjeyemem.externalizedproperties.core.exceptions.ConversionException;
import io.github.jeyjeyemem.externalizedproperties.core.exceptions.ExternalizedPropertiesException;
import io.github.jeyjeyemem.externalizedproperties.core.internal.InternalConverter;
import io.github.jeyjeyemem.externalizedproperties.core.testentities.StubExternalizedPropertyMethodInfo;
import io.github.jeyjeyemem.externalizedproperties.core.testentities.proxy.ListProxyInterface;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class CollectionConversionHandlerTests {
    @Nested
    class CanConvertToMethod {
        @Test
        @DisplayName("should return false when expected type is null.")
        public void test1() {
            CollectionConversionHandler handler = handlerToTest();
            boolean canConvert = handler.canConvertTo(null);
            assertFalse(canConvert);
        }

        @Test
        @DisplayName("should return true when expected type is a List class.")
        public void test2() {
            CollectionConversionHandler handler = handlerToTest();
            boolean canConvert = handler.canConvertTo(List.class);
            assertTrue(canConvert);
        }

        @Test
        @DisplayName("should return true when expected type is a Collection class.")
        public void test3() {
            CollectionConversionHandler handler = handlerToTest();
            boolean canConvert = handler.canConvertTo(Collection.class);
            assertTrue(canConvert);
        }



        @Test
        @DisplayName("should return false when expected type is not a List/Collection class.")
        public void test4() {
            CollectionConversionHandler handler = handlerToTest();
            boolean canConvert = handler.canConvertTo(String.class);
            assertFalse(canConvert);
        }
    }

    @Nested
    class ConvertMethod {
        @Test
        @DisplayName("should throw when context is null.")
        public void test1() {
            CollectionConversionHandler handler = handlerToTest();
            assertThrows(IllegalArgumentException.class, () -> handler.convert(null));
        }

        @Test
        @DisplayName("should convert resolved property to a List.")
        public void test2() {
            CollectionConversionHandler handler = handlerToTest();
            
            Converter converter = new InternalConverter(handler);

            Type listType = new TypeReference<List<String>>(){}.type();

            ConversionContext context = new ConversionContext(
                converter,
                "value1,value2,value3",
                listType
            );

            List<?> list = handler.convert(context);
            
            assertNotNull(list);
            assertTrue(list.size() == 3);
            assertTrue(list.stream().allMatch(v -> v instanceof String));
            assertIterableEquals(
                Arrays.asList("value1", "value2", "value3"), 
                list
            );
        }

        @Test
        @DisplayName("should convert resolved property according to the List's generic type parameter.")
        public void test4() {
            CollectionConversionHandler handler = handlerToTest();
            
            Converter converter = new InternalConverter(
                handler,
                new PrimitiveConversionHandler()
            );

            Type listType = new TypeReference<List<Integer>>(){}.type();

            ConversionContext context = new ConversionContext(
                converter,
                "1,2,3",
                listType
            );
            
            List<?> list = handler.convert(context);
            
            assertNotNull(list);
            assertTrue(list.size() == 3);
            assertTrue(list.stream().allMatch(v -> v instanceof Integer));
            assertIterableEquals(
                Arrays.asList(1, 2, 3), 
                list
            );

        }

        @Test
        @DisplayName("should return String values when List's generic type parameter is a wildcard.")
        public void test5() {
            CollectionConversionHandler handler = handlerToTest();
            
            Converter converter = new InternalConverter(handler);

            Type wildcardListType = new TypeReference<List<?>>(){}.type();

            ConversionContext context = new ConversionContext(
                converter,
                "value1,value2,value3",
                wildcardListType
            );

            List<?> list = handler.convert(context);
            
            assertNotNull(list);
            assertTrue(list.size() == 3);
            assertTrue(list.stream().allMatch(v -> v instanceof String));
            assertIterableEquals(
                Arrays.asList("value1", "value2", "value3"), 
                list
            );

        }

        @Test
        @DisplayName("should return String values when List's generic type parameter is Object.")
        public void test6() {
            CollectionConversionHandler handler = handlerToTest();

            Converter converter = new InternalConverter(handler);
            
            Type listType = new TypeReference<List<Object>>(){}.type();

            ConversionContext context = new ConversionContext(
                converter,
                "value1,value2,value3",
                listType
            );

            List<?> list = handler.convert(context);
            
            assertNotNull(list);
            assertTrue(list.size() == 3);
            assertTrue(list.stream().allMatch(v -> v instanceof String));
            assertIterableEquals(
                Arrays.asList("value1", "value2", "value3"), 
                list
            );
        }

        @Test
        @DisplayName("should return String values when expected type is a raw List.")
        public void test7() {
            CollectionConversionHandler handler = handlerToTest();

            Converter converter = new InternalConverter(handler);

            ConversionContext context = new ConversionContext(
                converter,
                "value1,value2,value3",
                List.class
            );

            List<?> list = handler.convert(context);
            
            assertNotNull(list);
            assertTrue(list.size() == 3);
            assertTrue(list.stream().allMatch(v -> v instanceof String));
            assertIterableEquals(
                Arrays.asList("value1", "value2", "value3"), 
                list
            );
        }

        @Test
        @DisplayName("should return empty List when property value is empty.")
        public void test8() {
            CollectionConversionHandler handler = handlerToTest();
            
            Converter converter = new InternalConverter(handler);

            Type listType = new TypeReference<List<String>>(){}.type();

            ConversionContext context = new ConversionContext(
                converter,
                "", // Empty value.
                listType
            );

            List<?> list = handler.convert(context);
            
            assertNotNull(list);
            assertTrue(list.isEmpty());
        }

        @Test
        @DisplayName("should retain empty values from property value.")
        public void test9() {
            CollectionConversionHandler handler = handlerToTest();
            
            Converter converter = new InternalConverter(handler);

            Type listType = new TypeReference<List<String>>(){}.type();

            ConversionContext context = new ConversionContext(
                converter,
                "value1,,value3,,value5", // Has empty values.
                listType
            );

            List<?> list = handler.convert(context);
            
            assertNotNull(list);
            assertTrue(list.size() == 5);
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
        public void test10() {
            CollectionConversionHandler handler = handlerToTest();
            
            Converter converter = new InternalConverter(handler);

            Type listType = new TypeReference<List<Integer>>(){}.type();
            
            // No registered conversion handler for Integer.
            assertThrows(ExternalizedPropertiesException.class, () -> {
                handler.convert(new ConversionContext(
                    converter,
                    "1,2,3,4,5",
                    listType
                ));
            });
        }

        @Test
        @DisplayName(
            "should convert resolved property according to the List's generic type parameter. " + 
            "Generic type parameter is also a parameterized type e.g. List<Optional<String>>."
        )
        public void test11() {
            CollectionConversionHandler handler = handlerToTest();
            
            Converter converter = new InternalConverter(
                handler,
                new OptionalConversionHandler() // Register additional Optional handler.
            );

            Type listType = new TypeReference<List<Optional<String>>>(){}.type();

            ConversionContext context = new ConversionContext(
                converter,
                "value1,value2,value3",
                listType
            );

            List<?> list = handler.convert(context);
            
            assertNotNull(list);
            assertTrue(list.size() == 3);
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
            "should convert resolved property according to the List's generic type parameter. " + 
            "Generic type parameter is generic array e.g. List<Optional<String>[]>."
        )
        public void test12() {
            CollectionConversionHandler handler = handlerToTest();
            
            Converter converter = new InternalConverter(
                handler,
                new ArrayConversionHandler(), // Register additional array handler.
                new OptionalConversionHandler() // Register additional Optional handler.
            );

            Type listType = new TypeReference<List<Optional<String>[]>>(){}.type();

            ConversionContext context = new ConversionContext(
                converter,
                "value1,value2,value3",
                listType
            );

            List<?> list = handler.convert(context);
            
            assertNotNull(list);
            assertTrue(list.size() == 3);
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
        @DisplayName("should throw when expected type has a type variable e.g. List<T>.")
        public <T> void test13() {
            CollectionConversionHandler handler = handlerToTest();
            
            Converter converter = new InternalConverter(handler);


            Type listType = new TypeReference<List<T>>(){}.type();

            ConversionContext context = new ConversionContext(
                converter,
                "value",
                listType
            );
                
            assertThrows(
                ConversionException.class, 
                () -> handler.convert(context)
            );
        }
    }

    @Nested
    class ConvertMethodWithPropertyMethodConversionContextOverload {
        @Test
        @DisplayName("should throw when context is null.")
        public void test1() {
            CollectionConversionHandler handler = handlerToTest();
            assertThrows(IllegalArgumentException.class, () -> handler.convert(null));
        }

        @Test
        @DisplayName("should convert resolved property to a List.")
        public void test2() {
            CollectionConversionHandler handler = handlerToTest();

            ExternalizedPropertyMethodInfo propertyMethodInfo = 
                StubExternalizedPropertyMethodInfo.fromMethod(
                    ListProxyInterface.class,
                    "listProperty"
                );
            
            Converter converter = new InternalConverter(handler);

            PropertyMethodConversionContext context = 
                new PropertyMethodConversionContext(
                    converter,
                    propertyMethodInfo,
                    "value1,value2,value3"
                );

            List<?> list = handler.convert(context);
            
            assertNotNull(list);
            assertTrue(list.size() == 3);
            assertTrue(list.stream().allMatch(v -> v instanceof String));
            assertIterableEquals(
                Arrays.asList("value1", "value2", "value3"), 
                list
            );
        }

        @Test
        @DisplayName("should convert resolved property to a List using custom delimiter.")
        public void test3() {
            CollectionConversionHandler handler = handlerToTest();

            ExternalizedPropertyMethodInfo propertyMethodInfo = 
                StubExternalizedPropertyMethodInfo.fromMethod(
                    ListProxyInterface.class,
                    "listCustomDelimiter"
                );
            
            Converter converter = new InternalConverter(handler);
            
            PropertyMethodConversionContext context = 
                new PropertyMethodConversionContext(
                    converter,
                    propertyMethodInfo,
                    "value1#value2#value3"
                );

            List<?> list = handler.convert(context);
            
            assertNotNull(list);
            assertTrue(list.size() == 3);
            assertTrue(list.stream().allMatch(v -> v instanceof String));
            assertIterableEquals(
                Arrays.asList("value1", "value2", "value3"), 
                list
            );

        }

        @Test
        @DisplayName("should convert resolved property according to the List's generic type parameter.")
        public void test4() {
            CollectionConversionHandler handler = handlerToTest();

            ExternalizedPropertyMethodInfo propertyMethodInfo = 
                StubExternalizedPropertyMethodInfo.fromMethod(
                    ListProxyInterface.class,
                    "listInteger"
                );
            
            Converter converter = new InternalConverter(
                handler,
                new PrimitiveConversionHandler()
            );

            PropertyMethodConversionContext context = 
                new PropertyMethodConversionContext(
                    converter,
                    propertyMethodInfo,
                    "1,2,3"
                );
            
            List<?> list = handler.convert(context);
            
            assertNotNull(list);
            assertTrue(list.size() == 3);
            assertTrue(list.stream().allMatch(v -> v instanceof Integer));
            assertIterableEquals(
                Arrays.asList(1, 2, 3), 
                list
            );

        }

        @Test
        @DisplayName("should return String values when List's generic type parameter is a wildcard.")
        public void test5() {
            CollectionConversionHandler handler = handlerToTest();

            ExternalizedPropertyMethodInfo propertyMethodInfo = 
                StubExternalizedPropertyMethodInfo.fromMethod(
                    ListProxyInterface.class,
                    "listPropertyWildcard"
                );
            
            Converter converter = new InternalConverter(handler);

            PropertyMethodConversionContext context = 
                new PropertyMethodConversionContext(
                    converter,
                    propertyMethodInfo,
                    "value1,value2,value3"
                );

            List<?> list = handler.convert(context);
            
            assertNotNull(list);
            assertTrue(list.size() == 3);
            assertTrue(list.stream().allMatch(v -> v instanceof String));
            assertIterableEquals(
                Arrays.asList("value1", "value2", "value3"), 
                list
            );

        }

        @Test
        @DisplayName("should return String values when List's generic type parameter is Object.")
        public void test6() {
            CollectionConversionHandler handler = handlerToTest();

            ExternalizedPropertyMethodInfo propertyMethodInfo = 
                StubExternalizedPropertyMethodInfo.fromMethod(
                    ListProxyInterface.class,
                    "listPropertyObject"
                );
            
            Converter converter = new InternalConverter(handler);

            PropertyMethodConversionContext context = 
                new PropertyMethodConversionContext(
                    converter,
                    propertyMethodInfo,
                    "value1,value2,value3"
                );

            List<?> list = handler.convert(context);
            
            assertNotNull(list);
            assertTrue(list.size() == 3);
            assertTrue(list.stream().allMatch(v -> v instanceof String));
            assertIterableEquals(
                Arrays.asList("value1", "value2", "value3"), 
                list
            );

        }

        @Test
        @DisplayName("should return empty List when property value is empty.")
        public void test7() {
            CollectionConversionHandler handler = handlerToTest();

            ExternalizedPropertyMethodInfo propertyMethodInfo = 
                StubExternalizedPropertyMethodInfo.fromMethod(
                    ListProxyInterface.class,
                    "listProperty"
                );
            
            Converter converter = new InternalConverter(handler);

            PropertyMethodConversionContext context = 
                new PropertyMethodConversionContext(
                    converter,
                    propertyMethodInfo,
                    "" // Empty value.
                );

            List<?> list = handler.convert(context);
            
            assertNotNull(list);
            assertTrue(list.isEmpty());
        }

        @Test
        @DisplayName("should retain empty values from property value.")
        public void test8() {
            CollectionConversionHandler handler = handlerToTest();

            ExternalizedPropertyMethodInfo propertyMethodInfo = 
                StubExternalizedPropertyMethodInfo.fromMethod(
                    ListProxyInterface.class,
                    "listProperty"
                );
            
            Converter converter = new InternalConverter(handler);

            PropertyMethodConversionContext context = 
                new PropertyMethodConversionContext(
                    converter,
                    propertyMethodInfo,
                    "value1,,value3,,value5" // Has empty values.
                );

            List<?> list = handler.convert(context);
            
            assertNotNull(list);
            assertTrue(list.size() == 5);
            assertTrue(list.stream().allMatch(v -> v instanceof String));
            assertIterableEquals(
                Arrays.asList("value1", "", "value3", "", "value5"), 
                list
            );

        }

        @Test
        @DisplayName("should strip empty values when annotated with @StripEmptyValues.")
        public void test9() {
            CollectionConversionHandler handler = handlerToTest();

            ExternalizedPropertyMethodInfo propertyMethodInfo = 
                StubExternalizedPropertyMethodInfo.fromMethod(
                    ListProxyInterface.class,
                    "listPropertyStripEmpty"
                );
            
            Converter converter = new InternalConverter(handler);

            PropertyMethodConversionContext context = 
                new PropertyMethodConversionContext(
                    converter,
                    propertyMethodInfo,
                    "value1,,value3,,value5" // Has empty values.
                );

            List<?> list = handler.convert(context);
            
            assertNotNull(list);
            assertTrue(list.size() == 3);
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
            CollectionConversionHandler handler = handlerToTest();

            ExternalizedPropertyMethodInfo propertyMethodInfo = 
                StubExternalizedPropertyMethodInfo.fromMethod(
                    ListProxyInterface.class,
                    "listInteger"
                );
            
            Converter converter = new InternalConverter(handler);
            
            // No registered conversion handler for Integer.
            assertThrows(ExternalizedPropertiesException.class, () -> {
                handler.convert(
                    new PropertyMethodConversionContext(
                        converter,
                        propertyMethodInfo,
                        "1,2,3,4,5"
                    )
                );
            });
        }

        @Test
        @DisplayName(
            "should convert resolved property according to the List's generic type parameter. " + 
            "Generic type parameter is also a parameterized type e.g. List<Optional<String>>."
        )
        public void test11() {
            CollectionConversionHandler handler = handlerToTest();

            ExternalizedPropertyMethodInfo propertyMethodInfo = 
                StubExternalizedPropertyMethodInfo.fromMethod(
                    ListProxyInterface.class,
                    "listPropertyNestedGenerics" // Returns a List<Optional<String>>.
                );
            
            Converter converter = new InternalConverter(
                handler,
                new OptionalConversionHandler() // Register additional Optional handler.
            );

            PropertyMethodConversionContext context = 
                new PropertyMethodConversionContext(
                    converter,
                    propertyMethodInfo,
                    "value1,value2,value3"
                );

            List<?> list = handler.convert(context);
            
            assertNotNull(list);
            assertTrue(list.size() == 3);
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
            "should convert resolved property according to the List's generic type parameter. " + 
            "Generic type parameter is generic array e.g. List<Optional<String>[]>."
        )
        public void test12() {
            CollectionConversionHandler handler = handlerToTest();

            ExternalizedPropertyMethodInfo propertyMethodInfo = 
                StubExternalizedPropertyMethodInfo.fromMethod(
                    ListProxyInterface.class,
                    "listPropertyNestedGenericsArray" // Returns a List<Optional<String>[]>.
                );
            
            Converter converter = new InternalConverter(
                handler,
                new ArrayConversionHandler(), // Register additional array handler.
                new OptionalConversionHandler() // Register additional Optional handler.
            );

            PropertyMethodConversionContext context = 
                new PropertyMethodConversionContext(
                    converter,
                    propertyMethodInfo,
                    "value1,value2,value3"
                );

            List<?> list = handler.convert(context);
            
            assertNotNull(list);
            assertTrue(list.size() == 3);
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
        @DisplayName("should throw when expected type has a type variable e.g. List<T>.")
        public void test13() {
            CollectionConversionHandler handler = handlerToTest();

            ExternalizedPropertyMethodInfo propertyMethodInfo = 
                StubExternalizedPropertyMethodInfo.fromMethod(
                    ListProxyInterface.class,
                    "listPropertyT"
                );
            
            Converter converter = new InternalConverter(handler);

            PropertyMethodConversionContext context = 
                new PropertyMethodConversionContext(
                    converter,
                    propertyMethodInfo,
                    "value"
                );
                
            assertThrows(
                ConversionException.class, 
                () -> handler.convert(context)
            );
        }
    }

    private CollectionConversionHandler handlerToTest() {
        return new CollectionConversionHandler();
    }
}
