package io.github.jeyjeyemem.externalizedproperties.core.conversion.converters;

import io.github.jeyjeyemem.externalizedproperties.core.ConversionContext;
import io.github.jeyjeyemem.externalizedproperties.core.ConversionResult;
import io.github.jeyjeyemem.externalizedproperties.core.Converter;
import io.github.jeyjeyemem.externalizedproperties.core.internal.conversion.RootConverter;
import io.github.jeyjeyemem.externalizedproperties.core.proxy.ProxyMethod;
import io.github.jeyjeyemem.externalizedproperties.core.testentities.ProxyMethodUtils;
import io.github.jeyjeyemem.externalizedproperties.core.testentities.proxy.ListProxyInterface;
import io.github.jeyjeyemem.externalizedproperties.core.testentities.proxy.PrimitiveProxyInterface;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class PrimitiveConverterTests {
    @Nested
    class CanConvertToMethod {
        @Test
        @DisplayName("should return false when target type is null.")
        public void test1() {
            PrimitiveConverter handler = handlerToTest();
            boolean canConvert = handler.canConvertTo(null);
            assertFalse(canConvert);
        }

        @Test
        @DisplayName("should return true when target type is an Integer.")
        public void test2() {
            PrimitiveConverter handler = handlerToTest();
            boolean canConvert = handler.canConvertTo(Integer.class);
            assertTrue(canConvert);
        }

        @Test
        @DisplayName("should return true when target type is a primitive int.")
        public void test3() {
            PrimitiveConverter handler = handlerToTest();
            boolean canConvert = handler.canConvertTo(Integer.TYPE);
            assertTrue(canConvert);
        }

        @Test
        @DisplayName("should return true when target type is a Long.")
        public void test4() {
            PrimitiveConverter handler = handlerToTest();
            boolean canConvert = handler.canConvertTo(Long.class);
            assertTrue(canConvert);
        }

        @Test
        @DisplayName("should return true when target type is a primitive long.")
        public void test5() {
            PrimitiveConverter handler = handlerToTest();
            boolean canConvert = handler.canConvertTo(Long.TYPE);
            assertTrue(canConvert);
        }

        @Test
        @DisplayName("should return true when target type is a Float.")
        public void test6() {
            PrimitiveConverter handler = handlerToTest();
            boolean canConvert = handler.canConvertTo(Float.class);
            assertTrue(canConvert);
        }

        @Test
        @DisplayName("should return true when target type is a primitive float.")
        public void test7() {
            PrimitiveConverter handler = handlerToTest();
            boolean canConvert = handler.canConvertTo(Float.TYPE);
            assertTrue(canConvert);
        }

        @Test
        @DisplayName("should return true when target type is a Double.")
        public void test8() {
            PrimitiveConverter handler = handlerToTest();
            boolean canConvert = handler.canConvertTo(Double.class);
            assertTrue(canConvert);
        }

        @Test
        @DisplayName("should return true when target type is a primitive double.")
        public void test9() {
            PrimitiveConverter handler = handlerToTest();
            boolean canConvert = handler.canConvertTo(Double.TYPE);
            assertTrue(canConvert);
        }

        @Test
        @DisplayName("should return false when target type is not a primitive type.")
        public void test10() {
            PrimitiveConverter handler = handlerToTest();
            boolean canConvert = handler.canConvertTo(List.class);
            assertFalse(canConvert);
        }
    }

    @Nested
    class ConvertMethod {
        @Test
        @DisplayName("should throw when context is null.")
        public void test1() {
            PrimitiveConverter handler = handlerToTest();
            assertThrows(IllegalArgumentException.class, () -> handler.convert(null));
        }

        @Test
        @DisplayName("should return skip result when target type is not a primitive type.")
        public void test2() {
            PrimitiveConverter handler = handlerToTest();

            ProxyMethod proxyMethod = 
                ProxyMethodUtils.fromMethod(
                    ListProxyInterface.class,
                    "listProperty" // This method returns a List class
                );
            
            Converter<?> converter = new RootConverter(handler);

            ConversionContext context = new ConversionContext(
                converter,
                proxyMethod,
                "a,b,c"
            );
            
            ConversionResult<?> result =  handler.convert(context);
            assertEquals(ConversionResult.skip(), result);
        }

        @Test
        @DisplayName("should convert value to an Integer or primitive int.")
        public void test3() {
            PrimitiveConverter handler = handlerToTest();

            ProxyMethod wrapperProxyMethod = 
                ProxyMethodUtils.fromMethod(
                    PrimitiveProxyInterface.class,
                    "integerWrapperProperty" // This method returns a Integer wrapper class
                );

            ProxyMethod primitiveProxyMethod = 
                ProxyMethodUtils.fromMethod(
                    PrimitiveProxyInterface.class,
                    "intPrimitiveProperty" // This method returns an int primitive
                );
            
            Converter<?> converter = new RootConverter(handler);

            ConversionContext wrapperContext = new ConversionContext(
                converter,
                wrapperProxyMethod,
                "1"
            );

            ConversionContext primitiveContext = new ConversionContext(
                converter,
                primitiveProxyMethod,
                "2"
            );

            ConversionResult<?> wrapperResult = handler.convert(wrapperContext);
            ConversionResult<?> primitiveResult = handler.convert(primitiveContext);
            
            assertNotNull(wrapperResult);
            assertNotNull(primitiveResult);
            Object wrapperValue = wrapperResult.value();
            Object primitiveValue = primitiveResult.value();

            assertNotNull(wrapperValue);
            assertNotNull(primitiveValue);

            assertTrue(wrapperValue instanceof Integer);
            assertTrue(primitiveValue instanceof Integer);

            assertEquals(1, (Integer)wrapperValue);
            assertEquals(2, (int)primitiveValue);
        }

        @Test
        @DisplayName("should convert value to a Long or primitive long.")
        public void test4() {
            PrimitiveConverter handler = handlerToTest();

            ProxyMethod wrapperProxyMethod = 
                ProxyMethodUtils.fromMethod(
                    PrimitiveProxyInterface.class,
                    "longWrapperProperty" // This method returns a Long wrapper class
                );

            ProxyMethod primitiveProxyMethod = 
                ProxyMethodUtils.fromMethod(
                    PrimitiveProxyInterface.class,
                    "longPrimitiveProperty" // This method returns an long primitive
                );
            
            Converter<?> converter = new RootConverter(handler);

            ConversionContext wrapperContext = new ConversionContext(
                converter,
                wrapperProxyMethod,
                "1"
            );

            ConversionContext primitiveContext = new ConversionContext(
                converter,
                primitiveProxyMethod,
                "2"
            );

            ConversionResult<?> wrapperResult = handler.convert(wrapperContext);
            ConversionResult<?> primitiveResult = handler.convert(primitiveContext);
            
            assertNotNull(wrapperResult);
            assertNotNull(primitiveResult);
            Object wrapperValue = wrapperResult.value();
            Object primitiveValue = primitiveResult.value();
            
            assertNotNull(wrapperValue);
            assertNotNull(primitiveValue);

            assertTrue(wrapperValue instanceof Long);
            assertTrue(primitiveValue instanceof Long);

            assertEquals(1L, (Long)wrapperValue);
            assertEquals(2L, (long)primitiveValue);
        }

        @Test
        @DisplayName("should convert value to a Float or primitive float.")
        public void test5() {
            PrimitiveConverter handler = handlerToTest();

            ProxyMethod wrapperProxyMethod = 
                ProxyMethodUtils.fromMethod(
                    PrimitiveProxyInterface.class,
                    "floatWrapperProperty" // This method returns a Float wrapper class
                );

            ProxyMethod primitiveProxyMethod = 
                ProxyMethodUtils.fromMethod(
                    PrimitiveProxyInterface.class,
                    "floatPrimitiveProperty" // This method returns an float primitive
                );
            
            Converter<?> converter = new RootConverter(handler);

            ConversionContext wrapperContext = new ConversionContext(
                converter,
                wrapperProxyMethod,
                "1.0"
            );

            ConversionContext primitiveContext = new ConversionContext(
                converter,
                primitiveProxyMethod,
                "2.0"
            );

            ConversionResult<?> wrapperResult = handler.convert(wrapperContext);
            ConversionResult<?> primitiveResult = handler.convert(primitiveContext);
            
            assertNotNull(wrapperResult);
            assertNotNull(primitiveResult);
            Object wrapperValue = wrapperResult.value();
            Object primitiveValue = primitiveResult.value();
            
            assertNotNull(wrapperValue);
            assertNotNull(primitiveValue);

            assertTrue(wrapperValue instanceof Float);
            assertTrue(primitiveValue instanceof Float);

            assertEquals(1.0F, (Float)wrapperValue);
            assertEquals(2.0F, (float)primitiveValue);
        }

        @Test
        @DisplayName("should convert value to a Double or primitive double.")
        public void test6() {
            PrimitiveConverter handler = handlerToTest();

            ProxyMethod wrapperProxyMethod = 
                ProxyMethodUtils.fromMethod(
                    PrimitiveProxyInterface.class,
                    "doubleWrapperProperty" // This method returns a Double wrapper class
                );

            ProxyMethod primitiveProxyMethod = 
                ProxyMethodUtils.fromMethod(
                    PrimitiveProxyInterface.class,
                    "doublePrimitiveProperty" // This method returns a double primitive
                );
            
            Converter<?> converter = new RootConverter(handler);

            ConversionContext wrapperContext = new ConversionContext(
                converter,
                wrapperProxyMethod,
                "1.0"
            );

            ConversionContext primitiveContext = new ConversionContext(
                converter,
                primitiveProxyMethod,
                "2.0"
            );

            ConversionResult<?> wrapperResult = handler.convert(wrapperContext);
            ConversionResult<?> primitiveResult = handler.convert(primitiveContext);
            
            assertNotNull(wrapperResult);
            assertNotNull(primitiveResult);
            Object wrapperValue = wrapperResult.value();
            Object primitiveValue = primitiveResult.value();
            
            assertNotNull(wrapperValue);
            assertNotNull(primitiveValue);

            assertTrue(wrapperValue instanceof Double);
            assertTrue(primitiveValue instanceof Double);

            assertEquals(1.0D, (Double)wrapperValue);
            assertEquals(2.0D, (double)primitiveValue);
        }

        @Test
        @DisplayName("should convert value to a Short or primitive short.")
        public void test7() {
            PrimitiveConverter handler = handlerToTest();

            ProxyMethod wrapperProxyMethod = 
                ProxyMethodUtils.fromMethod(
                    PrimitiveProxyInterface.class,
                    "shortWrapperProperty" // This method returns a Short wrapper class
                );

            ProxyMethod primitiveProxyMethod = 
                ProxyMethodUtils.fromMethod(
                    PrimitiveProxyInterface.class,
                    "shortPrimitiveProperty" // This method returns a short primitive
                );
            
            Converter<?> converter = new RootConverter(handler);

            ConversionContext wrapperContext = new ConversionContext(
                converter,
                wrapperProxyMethod,
                "1"
            );

            ConversionContext primitiveContext = new ConversionContext(
                converter,
                primitiveProxyMethod,
                "2"
            );

            ConversionResult<?> wrapperResult = handler.convert(wrapperContext);
            ConversionResult<?> primitiveResult = handler.convert(primitiveContext);
            
            assertNotNull(wrapperResult);
            assertNotNull(primitiveResult);
            Object wrapperValue = wrapperResult.value();
            Object primitiveValue = primitiveResult.value();
            
            assertNotNull(wrapperValue);
            assertNotNull(primitiveValue);

            assertTrue(wrapperValue instanceof Short);
            assertTrue(primitiveValue instanceof Short);

            assertEquals((short)1, (Short)wrapperValue);
            assertEquals((short)2, (short)primitiveValue);
        }

        @Test
        @DisplayName("should convert value to a Boolean or primitive boolean.")
        public void test8() {
            PrimitiveConverter handler = handlerToTest();

            ProxyMethod wrapperProxyMethod = 
                ProxyMethodUtils.fromMethod(
                    PrimitiveProxyInterface.class,
                    "booleanWrapperProperty" // This method returns a Boolean wrapper class
                );

            ProxyMethod primitiveProxyMethod = 
                ProxyMethodUtils.fromMethod(
                    PrimitiveProxyInterface.class,
                    "booleanPrimitiveProperty" // This method returns a boolean primitive
                );
            
            Converter<?> converter = new RootConverter(handler);

            ConversionContext wrapperContext = new ConversionContext(
                converter,
                wrapperProxyMethod,
                "true"
            );

            ConversionContext primitiveContext = new ConversionContext(
                converter,
                primitiveProxyMethod,
                "false"
            );

            ConversionResult<?> wrapperResult = handler.convert(wrapperContext);
            ConversionResult<?> primitiveResult = handler.convert(primitiveContext);
            
            assertNotNull(wrapperResult);
            assertNotNull(primitiveResult);
            Object wrapperValue = wrapperResult.value();
            Object primitiveValue = primitiveResult.value();
            
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
            PrimitiveConverter handler = handlerToTest();

            ProxyMethod wrapperProxyMethodInfo = 
                ProxyMethodUtils.fromMethod(
                    PrimitiveProxyInterface.class,
                    "booleanWrapperProperty" // This method returns a Boolean wrapper class
                );

            ProxyMethod primitiveProxyMethodInfo = 
                ProxyMethodUtils.fromMethod(
                    PrimitiveProxyInterface.class,
                    "booleanPrimitiveProperty" // This method returns a primitive boolean
                );
            
            Converter<?> converter = new RootConverter(handler);

            ConversionContext wrapperContext = new ConversionContext(
                converter,
                wrapperProxyMethodInfo,
                "invalid_boolean"
            );

            ConversionContext primitiveContext = new ConversionContext(
                converter,
                primitiveProxyMethodInfo,
                "invalid_boolean"
            );

            ConversionResult<?> wrapperResult = handler.convert(wrapperContext);
            ConversionResult<?> primitiveResult = handler.convert(primitiveContext);
            
            assertNotNull(wrapperResult);
            assertNotNull(primitiveResult);
            Object wrapperValue = wrapperResult.value();
            Object primitiveValue = primitiveResult.value();
            
            assertNotNull(wrapperValue);
            assertNotNull(primitiveValue);

            assertTrue(wrapperValue instanceof Boolean);
            assertTrue(primitiveValue instanceof Boolean);

            assertEquals(false, (Boolean)wrapperValue);
            assertEquals(false, (boolean)primitiveValue);
        }

        @Test
        @DisplayName("should convert value to a Byte or primitive byte.")
        public void test10() {
            PrimitiveConverter handler = handlerToTest();

            ProxyMethod wrapperProxyMethod = 
                ProxyMethodUtils.fromMethod(
                    PrimitiveProxyInterface.class,
                    "byteWrapperProperty" // This method returns a Byte wrapper class
                );

            ProxyMethod primitiveProxyMethod = 
                ProxyMethodUtils.fromMethod(
                    PrimitiveProxyInterface.class,
                    "bytePrimitiveProperty" // This method returns a byte primitive
                );
            
            Converter<?> converter = new RootConverter(handler);

            ConversionContext wrapperContext = new ConversionContext(
                converter,
                wrapperProxyMethod,
                "1"
            );
            
            ConversionContext primitiveContext = new ConversionContext(
                converter,
                primitiveProxyMethod,
                "2"
            );

            ConversionResult<?> wrapperResult = handler.convert(wrapperContext);
            ConversionResult<?> primitiveResult = handler.convert(primitiveContext);
            
            assertNotNull(wrapperResult);
            assertNotNull(primitiveResult);
            Object wrapperValue = wrapperResult.value();
            Object primitiveValue = primitiveResult.value();
            
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
            PrimitiveConverter handler = handlerToTest();

            ProxyMethod wrapperProxyMethodInfo = 
                ProxyMethodUtils.fromMethod(
                    PrimitiveProxyInterface.class,
                    "byteWrapperProperty" // This method returns a Byte wrapper class
                );

            ProxyMethod primitiveProxyMethodInfo = 
                ProxyMethodUtils.fromMethod(
                    PrimitiveProxyInterface.class,
                    "bytePrimitiveProperty" // This method returns a primitive byte
                );
            
            Converter<?> converter = new RootConverter(handler);

            ConversionContext wrapperContext = new ConversionContext(
                converter,
                wrapperProxyMethodInfo,
                "invalid_byte"
            );

            ConversionContext primitiveContext = new ConversionContext(
                converter,
                primitiveProxyMethodInfo,
                "invalid_byte"
            );
            
            assertThrows(NumberFormatException.class, () -> {
                handler.convert(wrapperContext);
            });
            
            assertThrows(NumberFormatException.class, () -> {
                handler.convert(primitiveContext);
            });
        }

        @Test
        @DisplayName("should throw when property value is not a valid Short/short.")
        public void test12() {
            PrimitiveConverter handler = handlerToTest();

            ProxyMethod wrapperProxyMethodInfo = 
                ProxyMethodUtils.fromMethod(
                    PrimitiveProxyInterface.class,
                    "shortWrapperProperty" // This method returns Short wrapper class
                );

            ProxyMethod primitiveProxyMethodInfo = 
                ProxyMethodUtils.fromMethod(
                    PrimitiveProxyInterface.class,
                    "shortPrimitiveProperty" // This method returns a primitive short
                );
            
            Converter<?> converter = new RootConverter(handler);

            ConversionContext wrapperContext = new ConversionContext(
                converter,
                wrapperProxyMethodInfo,
                "invalid_short"
            );

            ConversionContext primitiveContext = new ConversionContext(
                converter,
                primitiveProxyMethodInfo,
                "invalid_short"
            );
            
            assertThrows(NumberFormatException.class, () -> {
                handler.convert(wrapperContext);
            });
            
            assertThrows(NumberFormatException.class, () -> {
                handler.convert(primitiveContext);
            });
        }

        @Test
        @DisplayName("should throw when property value is not a valid Integer/int.")
        public void test13() {
            PrimitiveConverter handler = handlerToTest();

            ProxyMethod wrapperProxyMethodInfo = 
                ProxyMethodUtils.fromMethod(
                    PrimitiveProxyInterface.class,
                    "integerWrapperProperty" // This method returns an Integer wrapper class
                );

            ProxyMethod primitiveProxyMethodInfo = 
                ProxyMethodUtils.fromMethod(
                    PrimitiveProxyInterface.class,
                    "intPrimitiveProperty" // This method returns a primitive int
                );
            
            Converter<?> converter = new RootConverter(handler);

            ConversionContext wrapperContext = new ConversionContext(
                converter,
                wrapperProxyMethodInfo,
                "invalid_int"
            );

            ConversionContext primitiveContext = new ConversionContext(
                converter,
                primitiveProxyMethodInfo,
                "invalid_int"
            );
            
            assertThrows(NumberFormatException.class, () -> {
                handler.convert(wrapperContext);
            });
            
            assertThrows(NumberFormatException.class, () -> {
                handler.convert(primitiveContext);
            });
        }

        @Test
        @DisplayName("should throw when property value is not a valid Long/long.")
        public void test14() {
            PrimitiveConverter handler = handlerToTest();

            ProxyMethod wrapperProxyMethodInfo = 
                ProxyMethodUtils.fromMethod(
                    PrimitiveProxyInterface.class,
                    "longWrapperProperty" // This method returns Long wrapper class
                );

            ProxyMethod primitiveProxyMethodInfo = 
                ProxyMethodUtils.fromMethod(
                    PrimitiveProxyInterface.class,
                    "longPrimitiveProperty" // This method returns a primitive long
                );
            
            Converter<?> converter = new RootConverter(handler);

            ConversionContext wrapperContext = new ConversionContext(
                converter,
                wrapperProxyMethodInfo,
                "invalid_long"
            );

            ConversionContext primitiveContext = new ConversionContext(
                converter,
                primitiveProxyMethodInfo,
                "invalid_long"
            );
            
            assertThrows(NumberFormatException.class, () -> {
                handler.convert(wrapperContext);
            });
            
            assertThrows(NumberFormatException.class, () -> {
                handler.convert(primitiveContext);
            });
        }

        @Test
        @DisplayName("should throw when property value is not a valid Float/float.")
        public void test15() {
            PrimitiveConverter handler = handlerToTest();

            ProxyMethod wrapperProxyMethodInfo = 
                ProxyMethodUtils.fromMethod(
                    PrimitiveProxyInterface.class,
                    "floatWrapperProperty" // This method returns a Float wrapper class
                );

            ProxyMethod primitiveProxyMethodInfo = 
                ProxyMethodUtils.fromMethod(
                    PrimitiveProxyInterface.class,
                    "floatPrimitiveProperty" // This method returns a primitive float
                );
            
            Converter<?> converter = new RootConverter(handler);

            ConversionContext wrapperContext = new ConversionContext(
                converter,
                wrapperProxyMethodInfo,
                "invalid_float"
            );

            ConversionContext primitiveContext = new ConversionContext(
                converter,
                primitiveProxyMethodInfo,
                "invalid_float"
            );
            
            assertThrows(NumberFormatException.class, () -> {
                handler.convert(wrapperContext);
            });
            
            assertThrows(NumberFormatException.class, () -> {
                handler.convert(primitiveContext);
            });
        }
        
        @Test
        @DisplayName("should throw when property value is not a valid Double/double.")
        public void test16() {
            PrimitiveConverter handler = handlerToTest();

            ProxyMethod wrapperProxyMethodInfo = 
                ProxyMethodUtils.fromMethod(
                    PrimitiveProxyInterface.class,
                    "doubleWrapperProperty" // This method returns a Double wrapper class
                );

            ProxyMethod primitiveProxyMethodInfo = 
                ProxyMethodUtils.fromMethod(
                    PrimitiveProxyInterface.class,
                    "doublePrimitiveProperty" // This method returns a primitive double
                );
            
            Converter<?> converter = new RootConverter(handler);

            ConversionContext wrapperContext = new ConversionContext(
                converter,
                wrapperProxyMethodInfo,
                "invalid_double"
            );

            ConversionContext primitiveContext = new ConversionContext(
                converter,
                primitiveProxyMethodInfo,
                "invalid_double"
            );
            
            assertThrows(NumberFormatException.class, () -> {
                handler.convert(wrapperContext);
            });
            
            assertThrows(NumberFormatException.class, () -> {
                handler.convert(primitiveContext);
            });
        }
    }

    private PrimitiveConverter handlerToTest() {
        return new PrimitiveConverter();
    }
}
