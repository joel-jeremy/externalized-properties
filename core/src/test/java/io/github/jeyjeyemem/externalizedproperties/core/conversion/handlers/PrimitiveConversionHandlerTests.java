package io.github.jeyjeyemem.externalizedproperties.core.conversion.handlers;

import io.github.jeyjeyemem.externalizedproperties.core.ExternalizedPropertyMethodInfo;
import io.github.jeyjeyemem.externalizedproperties.core.conversion.Converter;
import io.github.jeyjeyemem.externalizedproperties.core.conversion.PropertyMethodConversionContext;
import io.github.jeyjeyemem.externalizedproperties.core.exceptions.ConversionException;
import io.github.jeyjeyemem.externalizedproperties.core.internal.InternalConverter;
import io.github.jeyjeyemem.externalizedproperties.core.testentities.StubExternalizedPropertyMethodInfo;
import io.github.jeyjeyemem.externalizedproperties.core.testentities.proxy.ListProxyInterface;
import io.github.jeyjeyemem.externalizedproperties.core.testentities.proxy.PrimitiveProxyInterface;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.awt.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class PrimitiveConversionHandlerTests {
    @Nested
    class CanConvertToMethod {
        @Test
        @DisplayName("should return false when expected type is null.")
        public void test1() {
            PrimitiveConversionHandler handler = handlerToTest();
            boolean canConvert = handler.canConvertTo(null);
            assertFalse(canConvert);
        }

        @Test
        @DisplayName("should return true when expected type is an Integer.")
        public void test2() {
            PrimitiveConversionHandler handler = handlerToTest();
            boolean canConvert = handler.canConvertTo(Integer.class);
            assertTrue(canConvert);
        }

        @Test
        @DisplayName("should return true when expected type is a primitive int.")
        public void test3() {
            PrimitiveConversionHandler handler = handlerToTest();
            boolean canConvert = handler.canConvertTo(Integer.TYPE);
            assertTrue(canConvert);
        }

        @Test
        @DisplayName("should return true when expected type is a Long.")
        public void test4() {
            PrimitiveConversionHandler handler = handlerToTest();
            boolean canConvert = handler.canConvertTo(Long.class);
            assertTrue(canConvert);
        }

        @Test
        @DisplayName("should return true when expected type is a primitive long.")
        public void test5() {
            PrimitiveConversionHandler handler = handlerToTest();
            boolean canConvert = handler.canConvertTo(Long.TYPE);
            assertTrue(canConvert);
        }

        @Test
        @DisplayName("should return true when expected type is a Float.")
        public void test6() {
            PrimitiveConversionHandler handler = handlerToTest();
            boolean canConvert = handler.canConvertTo(Float.class);
            assertTrue(canConvert);
        }

        @Test
        @DisplayName("should return true when expected type is a primitive float.")
        public void test7() {
            PrimitiveConversionHandler handler = handlerToTest();
            boolean canConvert = handler.canConvertTo(Float.TYPE);
            assertTrue(canConvert);
        }

        @Test
        @DisplayName("should return true when expected type is a Double.")
        public void test8() {
            PrimitiveConversionHandler handler = handlerToTest();
            boolean canConvert = handler.canConvertTo(Double.class);
            assertTrue(canConvert);
        }

        @Test
        @DisplayName("should return true when expected type is a primitive double.")
        public void test9() {
            PrimitiveConversionHandler handler = handlerToTest();
            boolean canConvert = handler.canConvertTo(Double.TYPE);
            assertTrue(canConvert);
        }

        @Test
        @DisplayName("should return false when expected type is not a primitive type.")
        public void test10() {
            PrimitiveConversionHandler handler = handlerToTest();
            boolean canConvert = handler.canConvertTo(List.class);
            assertFalse(canConvert);
        }
    }

    @Nested
    class ConvertMethod {
        @Test
        @DisplayName("should throw when context is null.")
        public void test1() {
            PrimitiveConversionHandler handler = handlerToTest();
            assertThrows(IllegalArgumentException.class, () -> handler.convert(null));
        }

        @Test
        @DisplayName("should throw when expected type is not a primitive type.")
        public void test2() {
            PrimitiveConversionHandler handler = handlerToTest();

            ExternalizedPropertyMethodInfo propertyMethodInfo = 
                StubExternalizedPropertyMethodInfo.fromMethod(
                    ListProxyInterface.class,
                    "listProperty" // This method returns a List class
                );
            
            Converter converter = 
                new InternalConverter(handler);

            PropertyMethodConversionContext context = 
                new PropertyMethodConversionContext(
                    converter,
                    propertyMethodInfo,
                    "a,b,c",
                    propertyMethodInfo.genericReturnType() // Expected type is a List
                );
            
            assertThrows(
                ConversionException.class, 
                () -> handler.convert(context)
            );
        }

        @Test
        @DisplayName("should convert resolved property to an Integer or primitive int.")
        public void test3() {
            PrimitiveConversionHandler handler = handlerToTest();

            ExternalizedPropertyMethodInfo wrapperPropertyMethod = 
                StubExternalizedPropertyMethodInfo.fromMethod(
                    PrimitiveProxyInterface.class,
                    "integerWrapperProperty" // This method returns a Integer wrapper class
                );

            ExternalizedPropertyMethodInfo primitivePropertyMethod = 
                StubExternalizedPropertyMethodInfo.fromMethod(
                    PrimitiveProxyInterface.class,
                    "intPrimitiveProperty" // This method returns an int primitive
                );
            
            Converter converter = 
                new InternalConverter(handler);

            PropertyMethodConversionContext wrapperContext = 
                new PropertyMethodConversionContext(
                    converter,
                    wrapperPropertyMethod,
                    "1"
                );

            PropertyMethodConversionContext primitiveContext = 
                new PropertyMethodConversionContext(
                    converter,
                    primitivePropertyMethod,
                    "2"
                );

            Object wrapperValue = handler.convert(wrapperContext);
            Object primitiveValue = handler.convert(primitiveContext);
            
            assertNotNull(wrapperValue);
            assertNotNull(primitiveValue);

            assertTrue(wrapperValue instanceof Integer);
            assertTrue(primitiveValue instanceof Integer);

            assertEquals(1, (Integer)wrapperValue);
            assertEquals(2, (int)primitiveValue);
        }

        @Test
        @DisplayName("should convert resolved property to a Long or primitive long.")
        public void test4() {
            PrimitiveConversionHandler handler = handlerToTest();

            ExternalizedPropertyMethodInfo wrapperPropertyMethod = 
                StubExternalizedPropertyMethodInfo.fromMethod(
                    PrimitiveProxyInterface.class,
                    "longWrapperProperty" // This method returns a Long wrapper class
                );

            ExternalizedPropertyMethodInfo primitivePropertyMethod = 
                StubExternalizedPropertyMethodInfo.fromMethod(
                    PrimitiveProxyInterface.class,
                    "longPrimitiveProperty" // This method returns an long primitive
                );
            
            Converter converter = 
                new InternalConverter(handler);

            PropertyMethodConversionContext wrapperContext = 
                new PropertyMethodConversionContext(
                    converter,
                    wrapperPropertyMethod,
                    "1"
                );

            PropertyMethodConversionContext primitiveContext = 
                new PropertyMethodConversionContext(
                    converter,
                    primitivePropertyMethod,
                    "2"
                );

            Object wrapperValue = handler.convert(wrapperContext);
            Object primitiveValue = handler.convert(primitiveContext);
            
            assertNotNull(wrapperValue);
            assertNotNull(primitiveValue);

            assertTrue(wrapperValue instanceof Long);
            assertTrue(primitiveValue instanceof Long);

            assertEquals(1L, (Long)wrapperValue);
            assertEquals(2L, (long)primitiveValue);
        }

        @Test
        @DisplayName("should convert resolved property to a Float or primitive float.")
        public void test5() {
            PrimitiveConversionHandler handler = handlerToTest();

            ExternalizedPropertyMethodInfo wrapperPropertyMethod = 
                StubExternalizedPropertyMethodInfo.fromMethod(
                    PrimitiveProxyInterface.class,
                    "floatWrapperProperty" // This method returns a Float wrapper class
                );

            ExternalizedPropertyMethodInfo primitivePropertyMethod = 
                StubExternalizedPropertyMethodInfo.fromMethod(
                    PrimitiveProxyInterface.class,
                    "floatPrimitiveProperty" // This method returns an float primitive
                );
            
            Converter converter = 
                new InternalConverter(handler);

            PropertyMethodConversionContext wrapperContext = 
                new PropertyMethodConversionContext(
                    converter,
                    wrapperPropertyMethod,
                    "1.0"
                );

            PropertyMethodConversionContext primitiveContext = 
                new PropertyMethodConversionContext(
                    converter,
                    primitivePropertyMethod,
                    "2.0"
                );

            Object wrapperValue = handler.convert(wrapperContext);
            Object primitiveValue = handler.convert(primitiveContext);
            
            assertNotNull(wrapperValue);
            assertNotNull(primitiveValue);

            assertTrue(wrapperValue instanceof Float);
            assertTrue(primitiveValue instanceof Float);

            assertEquals(1.0F, (Float)wrapperValue);
            assertEquals(2.0F, (float)primitiveValue);
        }

        @Test
        @DisplayName("should convert resolved property to a Double or primitive double.")
        public void test6() {
            PrimitiveConversionHandler handler = handlerToTest();

            ExternalizedPropertyMethodInfo wrapperPropertyMethod = 
                StubExternalizedPropertyMethodInfo.fromMethod(
                    PrimitiveProxyInterface.class,
                    "doubleWrapperProperty" // This method returns a Double wrapper class
                );

            ExternalizedPropertyMethodInfo primitivePropertyMethod = 
                StubExternalizedPropertyMethodInfo.fromMethod(
                    PrimitiveProxyInterface.class,
                    "doublePrimitiveProperty" // This method returns a double primitive
                );
            
            Converter converter = 
                new InternalConverter(handler);

            PropertyMethodConversionContext wrapperContext = 
                new PropertyMethodConversionContext(
                    converter,
                    wrapperPropertyMethod,
                    "1.0"
                );

            PropertyMethodConversionContext primitiveContext = 
                new PropertyMethodConversionContext(
                    converter,
                    primitivePropertyMethod,
                    "2.0"
                );

            Object wrapperValue = handler.convert(wrapperContext);
            Object primitiveValue = handler.convert(primitiveContext);
            
            assertNotNull(wrapperValue);
            assertNotNull(primitiveValue);

            assertTrue(wrapperValue instanceof Double);
            assertTrue(primitiveValue instanceof Double);

            assertEquals(1.0D, (Double)wrapperValue);
            assertEquals(2.0D, (double)primitiveValue);
        }

        @Test
        @DisplayName("should convert resolved property to a Short or primitive short.")
        public void test7() {
            PrimitiveConversionHandler handler = handlerToTest();

            ExternalizedPropertyMethodInfo wrapperPropertyMethod = 
                StubExternalizedPropertyMethodInfo.fromMethod(
                    PrimitiveProxyInterface.class,
                    "shortWrapperProperty" // This method returns a Short wrapper class
                );

            ExternalizedPropertyMethodInfo primitivePropertyMethod = 
                StubExternalizedPropertyMethodInfo.fromMethod(
                    PrimitiveProxyInterface.class,
                    "shortPrimitiveProperty" // This method returns a short primitive
                );
            
            Converter converter = 
                new InternalConverter(handler);

            PropertyMethodConversionContext wrapperContext = 
                new PropertyMethodConversionContext(
                    converter,
                    wrapperPropertyMethod,
                    "1"
                );

            PropertyMethodConversionContext primitiveContext = 
                new PropertyMethodConversionContext(
                    converter,
                    primitivePropertyMethod,
                    "2"
                );

            Object wrapperValue = handler.convert(wrapperContext);
            Object primitiveValue = handler.convert(primitiveContext);
            
            assertNotNull(wrapperValue);
            assertNotNull(primitiveValue);

            assertTrue(wrapperValue instanceof Short);
            assertTrue(primitiveValue instanceof Short);

            assertEquals((short)1, (Short)wrapperValue);
            assertEquals((short)2, (short)primitiveValue);
        }

        @Test
        @DisplayName("should convert resolved property to a Boolean or primitive boolean.")
        public void test8() {
            PrimitiveConversionHandler handler = handlerToTest();

            ExternalizedPropertyMethodInfo wrapperPropertyMethod = 
                StubExternalizedPropertyMethodInfo.fromMethod(
                    PrimitiveProxyInterface.class,
                    "booleanWrapperProperty" // This method returns a Boolean wrapper class
                );

            ExternalizedPropertyMethodInfo primitivePropertyMethod = 
                StubExternalizedPropertyMethodInfo.fromMethod(
                    PrimitiveProxyInterface.class,
                    "booleanPrimitiveProperty" // This method returns a boolean primitive
                );
            
            Converter converter = 
                new InternalConverter(handler);

            PropertyMethodConversionContext wrapperContext = 
                new PropertyMethodConversionContext(
                    converter,
                    wrapperPropertyMethod,
                    "true"
                );

            PropertyMethodConversionContext primitiveContext = 
                new PropertyMethodConversionContext(
                    converter,
                    primitivePropertyMethod,
                    "false"
                );

            Object wrapperValue = handler.convert(wrapperContext);
            Object primitiveValue = handler.convert(primitiveContext);
            
            assertNotNull(wrapperValue);
            assertNotNull(primitiveValue);

            assertTrue(wrapperValue instanceof Boolean);
            assertTrue(primitiveValue instanceof Boolean);

            assertEquals(true, (Boolean)wrapperValue);
            assertEquals(false, (boolean)primitiveValue);
        }

        @Test
        @DisplayName("should convert to false when property value is not a valid Boolean/boolean.")
        public void test9() {
            PrimitiveConversionHandler handler = handlerToTest();

            ExternalizedPropertyMethodInfo wrapperPropertyMethodInfo = 
                StubExternalizedPropertyMethodInfo.fromMethod(
                    PrimitiveProxyInterface.class,
                    "booleanWrapperProperty" // This method returns a Boolean wrapper class
                );

            ExternalizedPropertyMethodInfo primitivePropertyMethodInfo = 
                StubExternalizedPropertyMethodInfo.fromMethod(
                    PrimitiveProxyInterface.class,
                    "booleanPrimitiveProperty" // This method returns a primitive boolean
                );
            
            Converter converter = 
                new InternalConverter(handler);

            PropertyMethodConversionContext wrapperContext = 
                new PropertyMethodConversionContext(
                    converter,
                    wrapperPropertyMethodInfo,
                    "invalid_boolean"
                );

            PropertyMethodConversionContext primitiveContext = 
                new PropertyMethodConversionContext(
                    converter,
                    primitivePropertyMethodInfo,
                    "invalid_boolean"
                );

            Object wrapperValue = handler.convert(wrapperContext);
            Object primitiveValue = handler.convert(primitiveContext);
            
            assertNotNull(wrapperValue);
            assertNotNull(primitiveValue);

            assertTrue(wrapperValue instanceof Boolean);
            assertTrue(primitiveValue instanceof Boolean);

            assertEquals(false, (Boolean)wrapperValue);
            assertEquals(false, (boolean)primitiveValue);
        }

        @Test
        @DisplayName("should convert resolved property to a Byte or primitive byte.")
        public void test10() {
            PrimitiveConversionHandler handler = handlerToTest();

            ExternalizedPropertyMethodInfo wrapperPropertyMethod = 
                StubExternalizedPropertyMethodInfo.fromMethod(
                    PrimitiveProxyInterface.class,
                    "byteWrapperProperty" // This method returns a Byte wrapper class
                );

            ExternalizedPropertyMethodInfo primitivePropertyMethod = 
                StubExternalizedPropertyMethodInfo.fromMethod(
                    PrimitiveProxyInterface.class,
                    "bytePrimitiveProperty" // This method returns a byte primitive
                );
            
            Converter converter = 
                new InternalConverter(handler);

            PropertyMethodConversionContext wrapperContext = 
                new PropertyMethodConversionContext(
                    converter,
                    wrapperPropertyMethod,
                    "1"
                );
            
            PropertyMethodConversionContext primitiveContext = 
                new PropertyMethodConversionContext(
                    converter,
                    primitivePropertyMethod,
                    "2"
                );

            Object wrapperValue = handler.convert(wrapperContext);
            Object primitiveValue = handler.convert(primitiveContext);
            
            assertNotNull(wrapperValue);
            assertNotNull(primitiveValue);

            assertTrue(wrapperValue instanceof Byte);
            assertTrue(primitiveValue instanceof Byte);

            assertEquals((byte)1, (Byte)wrapperValue);
            assertEquals((byte)2, (byte)primitiveValue);
        }

        @Test
        @DisplayName("should throw when property value is not a valid Byte/byte.")
        public void test11() {
            PrimitiveConversionHandler handler = handlerToTest();

            ExternalizedPropertyMethodInfo wrapperPropertyMethodInfo = 
                StubExternalizedPropertyMethodInfo.fromMethod(
                    PrimitiveProxyInterface.class,
                    "byteWrapperProperty" // This method returns a Byte wrapper class
                );

            ExternalizedPropertyMethodInfo primitivePropertyMethodInfo = 
                StubExternalizedPropertyMethodInfo.fromMethod(
                    PrimitiveProxyInterface.class,
                    "bytePrimitiveProperty" // This method returns a primitive byte
                );
            
            Converter converter = 
                new InternalConverter(handler);

            PropertyMethodConversionContext wrapperContext = 
                new PropertyMethodConversionContext(
                    converter,
                    wrapperPropertyMethodInfo,
                    "invalid_byte"
                );

            PropertyMethodConversionContext primitiveContext = 
                new PropertyMethodConversionContext(
                    converter,
                    primitivePropertyMethodInfo,
                    "invalid_byte"
                );
            
            assertThrows(ConversionException.class, () -> {
                handler.convert(wrapperContext);
            });
            
            assertThrows(ConversionException.class, () -> {
                handler.convert(primitiveContext);
            });
        }

        @Test
        @DisplayName("should throw when property value is not a valid Short/short.")
        public void test12() {
            PrimitiveConversionHandler handler = handlerToTest();

            ExternalizedPropertyMethodInfo wrapperPropertyMethodInfo = 
                StubExternalizedPropertyMethodInfo.fromMethod(
                    PrimitiveProxyInterface.class,
                    "shortWrapperProperty" // This method returns Short wrapper class
                );

            ExternalizedPropertyMethodInfo primitivePropertyMethodInfo = 
                StubExternalizedPropertyMethodInfo.fromMethod(
                    PrimitiveProxyInterface.class,
                    "shortPrimitiveProperty" // This method returns a primitive short
                );
            
            Converter converter = 
                new InternalConverter(handler);

            PropertyMethodConversionContext wrapperContext = 
                new PropertyMethodConversionContext(
                    converter,
                    wrapperPropertyMethodInfo,
                    "invalid_short"
                );

            PropertyMethodConversionContext primitiveContext = 
                new PropertyMethodConversionContext(
                    converter,
                    primitivePropertyMethodInfo,
                    "invalid_short"
                );
            
            assertThrows(ConversionException.class, () -> {
                handler.convert(wrapperContext);
            });
            
            assertThrows(ConversionException.class, () -> {
                handler.convert(primitiveContext);
            });
        }

        @Test
        @DisplayName("should throw when property value is not a valid Integer/int.")
        public void test13() {
            PrimitiveConversionHandler handler = handlerToTest();

            ExternalizedPropertyMethodInfo wrapperPropertyMethodInfo = 
                StubExternalizedPropertyMethodInfo.fromMethod(
                    PrimitiveProxyInterface.class,
                    "integerWrapperProperty" // This method returns an Integer wrapper class
                );

            ExternalizedPropertyMethodInfo primitivePropertyMethodInfo = 
                StubExternalizedPropertyMethodInfo.fromMethod(
                    PrimitiveProxyInterface.class,
                    "intPrimitiveProperty" // This method returns a primitive int
                );
            
            Converter converter = 
                new InternalConverter(handler);

            PropertyMethodConversionContext wrapperContext = 
                new PropertyMethodConversionContext(
                    converter,
                    wrapperPropertyMethodInfo,
                    "invalid_int"
                );

            PropertyMethodConversionContext primitiveContext = 
                new PropertyMethodConversionContext(
                    converter,
                    primitivePropertyMethodInfo,
                    "invalid_int"
                );
            
            assertThrows(ConversionException.class, () -> {
                handler.convert(wrapperContext);
            });
            
            assertThrows(ConversionException.class, () -> {
                handler.convert(primitiveContext);
            });
        }

        @Test
        @DisplayName("should throw when property value is not a valid Long/long.")
        public void test14() {
            PrimitiveConversionHandler handler = handlerToTest();

            ExternalizedPropertyMethodInfo wrapperPropertyMethodInfo = 
                StubExternalizedPropertyMethodInfo.fromMethod(
                    PrimitiveProxyInterface.class,
                    "longWrapperProperty" // This method returns Long wrapper class
                );

            ExternalizedPropertyMethodInfo primitivePropertyMethodInfo = 
                StubExternalizedPropertyMethodInfo.fromMethod(
                    PrimitiveProxyInterface.class,
                    "longPrimitiveProperty" // This method returns a primitive long
                );
            
            Converter converter = 
                new InternalConverter(handler);

            PropertyMethodConversionContext wrapperContext = 
                new PropertyMethodConversionContext(
                    converter,
                    wrapperPropertyMethodInfo,
                    "invalid_long"
                );

            PropertyMethodConversionContext primitiveContext = 
                new PropertyMethodConversionContext(
                    converter,
                    primitivePropertyMethodInfo,
                    "invalid_long"
                );
            
            assertThrows(ConversionException.class, () -> {
                handler.convert(wrapperContext);
            });
            
            assertThrows(ConversionException.class, () -> {
                handler.convert(primitiveContext);
            });
        }

        @Test
        @DisplayName("should throw when property value is not a valid Float/float.")
        public void test15() {
            PrimitiveConversionHandler handler = handlerToTest();

            ExternalizedPropertyMethodInfo wrapperPropertyMethodInfo = 
                StubExternalizedPropertyMethodInfo.fromMethod(
                    PrimitiveProxyInterface.class,
                    "floatWrapperProperty" // This method returns a Float wrapper class
                );

            ExternalizedPropertyMethodInfo primitivePropertyMethodInfo = 
                StubExternalizedPropertyMethodInfo.fromMethod(
                    PrimitiveProxyInterface.class,
                    "floatPrimitiveProperty" // This method returns a primitive float
                );
            
            Converter converter = 
                new InternalConverter(handler);

            PropertyMethodConversionContext wrapperContext = 
                new PropertyMethodConversionContext(
                    converter,
                    wrapperPropertyMethodInfo,
                    "invalid_float"
                );

            PropertyMethodConversionContext primitiveContext = 
                new PropertyMethodConversionContext(
                    converter,
                    primitivePropertyMethodInfo,
                    "invalid_float"
                );
            
            assertThrows(ConversionException.class, () -> {
                handler.convert(wrapperContext);
            });
            
            assertThrows(ConversionException.class, () -> {
                handler.convert(primitiveContext);
            });
        }
        
        @Test
        @DisplayName("should throw when property value is not a valid Double/double.")
        public void test16() {
            PrimitiveConversionHandler handler = handlerToTest();

            ExternalizedPropertyMethodInfo wrapperPropertyMethodInfo = 
                StubExternalizedPropertyMethodInfo.fromMethod(
                    PrimitiveProxyInterface.class,
                    "doubleWrapperProperty" // This method returns a Double wrapper class
                );

            ExternalizedPropertyMethodInfo primitivePropertyMethodInfo = 
                StubExternalizedPropertyMethodInfo.fromMethod(
                    PrimitiveProxyInterface.class,
                    "doublePrimitiveProperty" // This method returns a primitive double
                );
            
            Converter converter = 
                new InternalConverter(handler);

            PropertyMethodConversionContext wrapperContext = 
                new PropertyMethodConversionContext(
                    converter,
                    wrapperPropertyMethodInfo,
                    "invalid_double"
                );

            PropertyMethodConversionContext primitiveContext = 
                new PropertyMethodConversionContext(
                    converter,
                    primitivePropertyMethodInfo,
                    "invalid_double"
                );
            
            assertThrows(ConversionException.class, () -> {
                handler.convert(wrapperContext);
            });
            
            assertThrows(ConversionException.class, () -> {
                handler.convert(primitiveContext);
            });
        }
    }

    private PrimitiveConversionHandler handlerToTest() {
        return new PrimitiveConversionHandler();
    }
}
