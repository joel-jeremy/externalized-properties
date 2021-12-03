package io.github.jeyjeyemem.externalizedproperties.core.conversion.handlers;

import io.github.jeyjeyemem.externalizedproperties.core.ExternalizedPropertyMethodInfo;
import io.github.jeyjeyemem.externalizedproperties.core.conversion.Converter;
import io.github.jeyjeyemem.externalizedproperties.core.conversion.PropertyMethodConversionContext;
import io.github.jeyjeyemem.externalizedproperties.core.exceptions.ConversionException;
import io.github.jeyjeyemem.externalizedproperties.core.internal.InternalConverter;
import io.github.jeyjeyemem.externalizedproperties.core.testentities.StubExternalizedPropertyMethodInfo;
import io.github.jeyjeyemem.externalizedproperties.core.testentities.proxy.ArrayProxyInterface;
import io.github.jeyjeyemem.externalizedproperties.core.testentities.proxy.ListProxyInterface;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ArrayConversionHandlerTests {

    @Nested
    class CanConvertToMethod {
        @Test
        @DisplayName("should return false when expected type is null.")
        public void test1() {
            ArrayConversionHandler handler = handlerToTest();
            boolean canConvert = handler.canConvertTo(null);
            assertFalse(canConvert);
        }

        @Test
        @DisplayName("should return true when expected type is an array class.")
        public void test2() {
            ArrayConversionHandler handler = handlerToTest();
            boolean canConvert = handler.canConvertTo(String[].class);
            assertTrue(canConvert);
        }

        @Test
        @DisplayName("should return false when expected type is not an array.")
        public void test3() {
            ArrayConversionHandler handler = handlerToTest();
            boolean canConvert = handler.canConvertTo(String.class);
            assertFalse(canConvert);
        }
    }

    @Nested
    class ConvertMethod {
        @Test
        @DisplayName("should throw when context is null.")
        public void test1() {
            ArrayConversionHandler handler = handlerToTest();
            assertThrows(IllegalArgumentException.class, () -> handler.convert(null));
        }

        @Test
        @DisplayName("should throw when method does not return an array.")
        public void test2() {
            ArrayConversionHandler handler = handlerToTest();

            // Method return type is a List and not an array e.g. String[]
            ExternalizedPropertyMethodInfo propertyMethodInfo = 
                StubExternalizedPropertyMethodInfo.fromMethod(
                    ListProxyInterface.class,
                    "listProperty"
                );
            
            Converter converter = new InternalConverter(
                handler
            );

            // Method return type is a List and not an array
            assertThrows(ConversionException.class, () -> {
                handler.convert(new PropertyMethodConversionContext(
                    converter,
                    propertyMethodInfo,
                    "value1,value2,value3"
                ));
            });
        }

        @Test
        @DisplayName("should convert resolved property to an array.")
        public void test3() {
            ArrayConversionHandler handler = handlerToTest();

            ExternalizedPropertyMethodInfo propertyMethodInfo = 
                StubExternalizedPropertyMethodInfo.fromMethod(
                    ArrayProxyInterface.class,
                    "arrayProperty"
                );
            
            Converter converter = 
                new InternalConverter(handler);

            Object[] array = handler.convert(new PropertyMethodConversionContext(
                converter,
                propertyMethodInfo,
                "value1,value2,value3"
            ));
            
            assertNotNull(array);
            assertTrue(array.length == 3);
            assertTrue(Arrays.stream(array).allMatch(v -> v instanceof String));
            assertArrayEquals(
                new String[] { "value1", "value2", "value3" }, 
                array
            );
        }

        @Test
        @DisplayName("should convert resolved property according to the array's component type.")
        public void test4() {
            ArrayConversionHandler handler = handlerToTest();
            
            ExternalizedPropertyMethodInfo propertyMethodInfo = 
                StubExternalizedPropertyMethodInfo.fromMethod(
                    ArrayProxyInterface.class,
                    "arrayIntegerWrapper"
                );
            
            Converter converter = new InternalConverter(
                handler,
                new PrimitiveConversionHandler()
            );
            
            Object[] array = handler.convert(
                new PropertyMethodConversionContext(
                    converter,
                    propertyMethodInfo,
                    "1,2,3"
                )
            );
            
            assertNotNull(array);
            assertTrue(array.length == 3);
            assertTrue(Arrays.stream(array).allMatch(v -> v instanceof Integer));
            assertArrayEquals(
                new Integer[] { 1, 2, 3 }, 
                array
            );
        }

        @Test
        @DisplayName("should return empty array when property value is empty.")
        public void test5() {
            ArrayConversionHandler handler = handlerToTest();
            
            ExternalizedPropertyMethodInfo propertyMethodInfo = 
                StubExternalizedPropertyMethodInfo.fromMethod(
                    ArrayProxyInterface.class,
                    "arrayProperty"
                );
            
            Converter converter = 
                new InternalConverter(handler);
            
            Object[] array = handler.convert(
                new PropertyMethodConversionContext(
                    converter,
                    propertyMethodInfo,
                    "" // Empty value.
                )
            );
            
            assertNotNull(array);
            assertTrue(array.length == 0);
        }

        @Test
        @DisplayName("should retain empty values from property value.")
        public void test6() {
            ArrayConversionHandler handler = handlerToTest();
            
            ExternalizedPropertyMethodInfo propertyMethodInfo = 
                StubExternalizedPropertyMethodInfo.fromMethod(
                    ArrayProxyInterface.class,
                    "arrayProperty"
                );
            
            Converter converter = 
                new InternalConverter(handler);
            
            Object[] array = handler.convert(
                new PropertyMethodConversionContext(
                    converter,
                    propertyMethodInfo,
                    "value1,,value3,,value5" // Has empty values.
                )
            );
            
            assertNotNull(array);
            assertTrue(array.length == 5);
            assertTrue(Arrays.stream(array).allMatch(v -> v instanceof String));
            assertArrayEquals(
                new String[] { "value1", "", "value3", "", "value5" }, 
                array
            );
        }

        @Test
        @DisplayName("should strip empty values when annotated with @StripEmptyValues.")
        public void test7() {
            ArrayConversionHandler handler = handlerToTest();
            
            ExternalizedPropertyMethodInfo propertyMethodInfo = 
                StubExternalizedPropertyMethodInfo.fromMethod(
                    ArrayProxyInterface.class,
                    "arrayPropertyStripEmpty"
                );
            
            Converter converter = 
                new InternalConverter(handler);
            
            Object[] array = handler.convert(
                new PropertyMethodConversionContext(
                    converter,
                    propertyMethodInfo,
                    "value1,,value3,,value5" // Has empty values.
                )
            );
            
            assertNotNull(array);
            assertTrue(array.length == 3);
            assertTrue(Arrays.stream(array).allMatch(v -> v instanceof String));
            assertArrayEquals(
                new String[] { "value1", "value3", "value5" }, 
                array
            );
        }

        @Test
        @DisplayName("should return Strings when array component type is Object.")
        public void test8() {
            ArrayConversionHandler handler = handlerToTest();
            
            ExternalizedPropertyMethodInfo propertyMethodInfo = 
                StubExternalizedPropertyMethodInfo.fromMethod(
                    ArrayProxyInterface.class,
                    "arrayPropertyObject"
                );
            
            Converter converter = 
                new InternalConverter(handler);
            
            Object[] array = handler.convert(
                new PropertyMethodConversionContext(
                    converter,
                    propertyMethodInfo,
                    "value1,value2,value3"
                )
            );
            
            assertNotNull(array);
            assertTrue(array.length == 3);
            assertTrue(Arrays.stream(array).allMatch(v -> v instanceof String));
            assertArrayEquals(
                new String[] { "value1", "value2", "value3" }, 
                array
            );
        }

        @Test
        @DisplayName(
            "should throw when no conversion handler is registered that can handle " + 
            "the array's component type."
        )
        public void test9() {
            ArrayConversionHandler handler = handlerToTest();

            ExternalizedPropertyMethodInfo propertyMethodInfo = 
                StubExternalizedPropertyMethodInfo.fromMethod(
                    ArrayProxyInterface.class,
                    "arrayIntegerWrapper"
                );
            
            // No registered handler for integer.
            Converter converter = 
                new InternalConverter(handler);
            
            assertThrows(ConversionException.class, () -> {
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
        @DisplayName("should use custom delimiter defined by @Delimiter.")
        public void test10() {
            ArrayConversionHandler handler = handlerToTest();
            
            ExternalizedPropertyMethodInfo propertyMethodInfo = 
                StubExternalizedPropertyMethodInfo.fromMethod(
                    ArrayProxyInterface.class,
                    "arrayCustomDelimiter"
                );
            
            Converter converter = 
                new InternalConverter(handler);
            
            Object[] array = handler.convert(
                new PropertyMethodConversionContext(
                    converter,
                    propertyMethodInfo,
                    "value1|value2|value3" // Custom delimiter
                )
            );
            
            assertNotNull(array);
            assertTrue(array.length == 3);
            assertTrue(Arrays.stream(array).allMatch(v -> v instanceof String));
            assertArrayEquals(
                new String[] { "value1", "value2", "value3" }, 
                array
            );
        }

        @Test
        @DisplayName(
            "should convert resolved property according to the array's generic component type."
        )
        public void test11() {
            ArrayConversionHandler handler = handlerToTest();
            
            ExternalizedPropertyMethodInfo propertyMethodInfo = 
                StubExternalizedPropertyMethodInfo.fromMethod(
                    ArrayProxyInterface.class,
                    "arrayPropertyGeneric" // Returns a generic type array Optional<String>[]
                );
            
            Converter converter = 
                new InternalConverter(
                    handler,
                    new OptionalConversionHandler() // Register additional Optional handler.
                );
            
            Object[] array = handler.convert(
                new PropertyMethodConversionContext(
                    converter,
                    propertyMethodInfo,
                    "value1,value2,value3"
                )
            );
            
            assertNotNull(array);
            assertTrue(array.length == 3);
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
            ArrayConversionHandler handler = handlerToTest();
            
            ExternalizedPropertyMethodInfo propertyMethodInfo = 
                StubExternalizedPropertyMethodInfo.fromMethod(
                    ArrayProxyInterface.class,
                    "arrayPropertyGenericWildcard" // Returns a generic type array Optional<?>[]
                );
            
            Converter converter = 
                new InternalConverter(
                    handler,
                    new OptionalConversionHandler() // Register additional Optional handler.
                );
            
            Object[] array = handler.convert(
                new PropertyMethodConversionContext(
                    converter,
                    propertyMethodInfo,
                    "value1,value2,value3"
                )
            );
            
            assertNotNull(array);
            assertTrue(array.length == 3);
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
        @DisplayName("should throw when expected type has a type variable e.g. List<T>.")
        public void test13() {
            ArrayConversionHandler handler = handlerToTest();
            
            ExternalizedPropertyMethodInfo propertyMethodInfo = 
                StubExternalizedPropertyMethodInfo.fromMethod(
                    ArrayProxyInterface.class,
                    "arrayPropertyT" // Returns a generic type array <T> T[]
                );
            
            Converter converter = 
                new InternalConverter(
                    handler
                );
            
            PropertyMethodConversionContext context = 
                new PropertyMethodConversionContext(
                    converter,
                    propertyMethodInfo,
                    "value1,value2,value3"
                );
            
            assertThrows(
                ConversionException.class, 
                () -> handler.convert(context)
            );
        }
    }

    private ArrayConversionHandler handlerToTest() {
        return new ArrayConversionHandler();
    }
}
