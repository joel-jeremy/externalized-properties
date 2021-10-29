package io.github.jeyjeyemem.externalizedproperties.core.conversion.handlers;

import io.github.jeyjeyemem.externalizedproperties.core.ExternalizedPropertyMethodInfo;
import io.github.jeyjeyemem.externalizedproperties.core.ResolvedProperty;
import io.github.jeyjeyemem.externalizedproperties.core.conversion.ResolvedPropertyConversionHandlerContext;
import io.github.jeyjeyemem.externalizedproperties.core.conversion.ResolvedPropertyConverter;
import io.github.jeyjeyemem.externalizedproperties.core.exceptions.ResolvedPropertyConversionException;
import io.github.jeyjeyemem.externalizedproperties.core.internal.InternalResolvedPropertyConverter;
import io.github.jeyjeyemem.externalizedproperties.core.testentities.StubExternalizedPropertyMethodInfo;
import io.github.jeyjeyemem.externalizedproperties.core.testentities.proxy.ArrayProxyInterface;
import io.github.jeyjeyemem.externalizedproperties.core.testentities.proxy.ListProxyInterface;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ArrayPropertyConversionHandlerTests {

    @Nested
    class CanConvertToMethod {
        @Test
        @DisplayName("should return true when expected type is null.")
        public void test1() {
            ArrayPropertyConversionHandler handler = handlerToTest();
            boolean canConvert = handler.canConvertTo(null);
            assertFalse(canConvert);
        }

        @Test
        @DisplayName("should return true when expected type is an array class.")
        public void test2() {
            ArrayPropertyConversionHandler handler = handlerToTest();
            boolean canConvert = handler.canConvertTo(String[].class);
            assertTrue(canConvert);
        }

        @Test
        @DisplayName("should return false when expected type is not an array.")
        public void test3() {
            ArrayPropertyConversionHandler handler = handlerToTest();
            boolean canConvert = handler.canConvertTo(String.class);
            assertFalse(canConvert);
        }
    }

    @Nested
    class ConvertMethod {
        @Test
        @DisplayName("should throw when context is null.")
        public void test1() {
            ArrayPropertyConversionHandler handler = handlerToTest();
            assertThrows(IllegalArgumentException.class, () -> handler.convert(null));
        }

        @Test
        @DisplayName("should throw when method does not return an array.")
        public void test2() {
            ArrayPropertyConversionHandler handler = handlerToTest();

            // Method return type is a List and not an array e.g. String[]
            ExternalizedPropertyMethodInfo propertyMethodInfo = 
                StubExternalizedPropertyMethodInfo.fromMethod(
                    ListProxyInterface.class,
                    "listProperty"
                );
            
            ResolvedPropertyConverter converter = new InternalResolvedPropertyConverter(
                handler
            );

            assertThrows(ResolvedPropertyConversionException.class, () -> {
                handler.convert(new ResolvedPropertyConversionHandlerContext(
                    converter,
                    propertyMethodInfo,
                    ResolvedProperty.with("property.list", "value1,value2,value3"), 
                    propertyMethodInfo.returnType(), // Method return type is a List and not an array
                    propertyMethodInfo.genericReturnTypeParameters()
                ));
            });
        }

        @Test
        @DisplayName("should convert resolved property to an array.")
        public void test3() {
            ArrayPropertyConversionHandler handler = handlerToTest();

            ExternalizedPropertyMethodInfo propertyMethodInfo = 
                StubExternalizedPropertyMethodInfo.fromMethod(
                    ArrayProxyInterface.class,
                    "arrayProperty"
                );
            
            ResolvedPropertyConverter converter = 
                new InternalResolvedPropertyConverter(handler);

            Object[] array = handler.convert(new ResolvedPropertyConversionHandlerContext(
                converter,
                propertyMethodInfo,
                ResolvedProperty.with("property.array", "value1,value2,value3")
            ));
            
            assertNotNull(array);
            assertTrue(array.length == 3);
            assertTrue(Arrays.stream(array).allMatch(v -> v instanceof String));
            assertEquals("value1", array[0]);
            assertEquals("value2", array[1]);
            assertEquals("value3", array[2]);
        }

        @Test
        @DisplayName("should convert resolved property according to the array's component type.")
        public void test4() {
            ArrayPropertyConversionHandler handler = handlerToTest();
            
            ExternalizedPropertyMethodInfo propertyMethodInfo = 
                StubExternalizedPropertyMethodInfo.fromMethod(
                    ArrayProxyInterface.class,
                    "arrayInteger"
                );
            
            ResolvedPropertyConverter converter = new InternalResolvedPropertyConverter(
                handler,
                new IntegerPropertyConversionHandler()
            );
            
            Object[] array = handler.convert(
                new ResolvedPropertyConversionHandlerContext(
                    converter,
                    propertyMethodInfo,
                    ResolvedProperty.with("property.array.integer", "1,2,3")
                )
            );
            
            assertNotNull(array);
            assertTrue(array.length == 3);
            assertTrue(Arrays.stream(array).allMatch(v -> v instanceof Integer));
            assertEquals(1, array[0]);
            assertEquals(2, array[1]);
            assertEquals(3, array[2]);
        }

        @Test
        @DisplayName("should return empty array when property value is empty.")
        public void test5() {
            ArrayPropertyConversionHandler handler = handlerToTest();
            
            ExternalizedPropertyMethodInfo propertyMethodInfo = 
                StubExternalizedPropertyMethodInfo.fromMethod(
                    ArrayProxyInterface.class,
                    "arrayProperty"
                );
            
            ResolvedPropertyConverter converter = 
                new InternalResolvedPropertyConverter(handler);
            
            Object[] array = handler.convert(
                new ResolvedPropertyConversionHandlerContext(
                    converter,
                    propertyMethodInfo,
                    ResolvedProperty.with("property.array", "") // Empty value.
                )
            );
            
            assertNotNull(array);
            assertTrue(array.length == 0);
        }

        @Test
        @DisplayName("should retain empty values from property value.")
        public void test6() {
            ArrayPropertyConversionHandler handler = handlerToTest();
            
            ExternalizedPropertyMethodInfo propertyMethodInfo = 
                StubExternalizedPropertyMethodInfo.fromMethod(
                    ArrayProxyInterface.class,
                    "arrayProperty"
                );
            
            ResolvedPropertyConverter converter = 
                new InternalResolvedPropertyConverter(handler);
            
            Object[] array = handler.convert(
                new ResolvedPropertyConversionHandlerContext(
                    converter,
                    propertyMethodInfo,
                    ResolvedProperty.with("property.array", "value1,,value3,,value5") // Has empty values.
                )
            );
            
            assertNotNull(array);
            assertTrue(array.length == 5);
            assertTrue(Arrays.stream(array).allMatch(v -> v instanceof String));
            assertEquals("value1", array[0]);
            assertEquals("", array[1]);
            assertEquals("value3", array[2]);
            assertEquals("", array[3]);
            assertEquals("value5", array[4]);
        }

        @Test
        @DisplayName("should strip empty values when annotated with @StripEmptyValues.")
        public void test7() {
            ArrayPropertyConversionHandler handler = handlerToTest();
            
            ExternalizedPropertyMethodInfo propertyMethodInfo = 
                StubExternalizedPropertyMethodInfo.fromMethod(
                    ArrayProxyInterface.class,
                    "arrayPropertyStripEmpty"
                );
            
            ResolvedPropertyConverter converter = 
                new InternalResolvedPropertyConverter(handler);
            
            Object[] array = handler.convert(
                new ResolvedPropertyConversionHandlerContext(
                    converter,
                    propertyMethodInfo,
                    ResolvedProperty.with(
                        "property.array.stripempty", 
                        "value1,,value3,,value5" // Has empty values. 
                    )
                )
            );
            
            assertNotNull(array);
            assertTrue(array.length == 3);
            assertTrue(Arrays.stream(array).allMatch(v -> v instanceof String));
            assertEquals("value1", array[0]);
            assertEquals("value3", array[1]);
            assertEquals("value5", array[2]);
        }

        @Test
        @DisplayName("should return Strings when array component type is Object.")
        public void test8() {
            ArrayPropertyConversionHandler handler = handlerToTest();
            
            ExternalizedPropertyMethodInfo propertyMethodInfo = 
                StubExternalizedPropertyMethodInfo.fromMethod(
                    ArrayProxyInterface.class,
                    "arrayPropertyObject"
                );
            
            ResolvedPropertyConverter converter = 
                new InternalResolvedPropertyConverter(handler);
            
            Object[] array = handler.convert(
                new ResolvedPropertyConversionHandlerContext(
                    converter,
                    propertyMethodInfo,
                    ResolvedProperty.with(
                        "property.array.object", 
                        "value1,value2,value3"
                    )
                )
            );
            
            assertNotNull(array);
            assertTrue(array.length == 3);
            assertTrue(Arrays.stream(array).allMatch(v -> v instanceof String));
            assertEquals("value1", array[0]);
            assertEquals("value2", array[1]);
            assertEquals("value3", array[2]);
        }

        @Test
        @DisplayName(
            "should throw when no conversion handler is registered that can handle " + 
            "the array's component type."
        )
        public void test9() {
            ArrayPropertyConversionHandler handler = handlerToTest();

            ExternalizedPropertyMethodInfo propertyMethodInfo = 
                StubExternalizedPropertyMethodInfo.fromMethod(
                    ArrayProxyInterface.class,
                    "arrayInteger"
                );
            
            ResolvedPropertyConverter converter = 
                new InternalResolvedPropertyConverter(handler);
            
            assertThrows(ResolvedPropertyConversionException.class, () -> {
                handler.convert(
                    new ResolvedPropertyConversionHandlerContext(
                        converter,
                        propertyMethodInfo,
                        ResolvedProperty.with("property.array.integer", "1,2,3,4,5"), 
                        propertyMethodInfo.returnType(), // No registered handler for integer.
                        propertyMethodInfo.genericReturnTypeParameters()
                    )
                );
            });
        }
    }

    private ArrayPropertyConversionHandler handlerToTest() {
        return new ArrayPropertyConversionHandler();
    }
}
