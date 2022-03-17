package io.github.jeyjeyemem.externalizedproperties.core.conversion.converters;

import io.github.jeyjeyemem.externalizedproperties.core.ConversionContext;
import io.github.jeyjeyemem.externalizedproperties.core.ConversionResult;
import io.github.jeyjeyemem.externalizedproperties.core.Converter;
import io.github.jeyjeyemem.externalizedproperties.core.internal.conversion.RootConverter;
import io.github.jeyjeyemem.externalizedproperties.core.proxy.ProxyMethod;
import io.github.jeyjeyemem.externalizedproperties.core.testentities.ProxyMethodUtils;
import io.github.jeyjeyemem.externalizedproperties.core.testentities.proxy.ArrayProxyInterface;
import io.github.jeyjeyemem.externalizedproperties.core.testentities.proxy.EnumProxyInterface;
import io.github.jeyjeyemem.externalizedproperties.core.testentities.proxy.EnumProxyInterface.TestEnum;
import io.github.jeyjeyemem.externalizedproperties.core.testentities.proxy.ListProxyInterface;
import io.github.jeyjeyemem.externalizedproperties.core.testentities.proxy.OptionalProxyInterface;
import io.github.jeyjeyemem.externalizedproperties.core.testentities.proxy.PrimitiveProxyInterface;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class DefaultConverterTests {
    @Nested
    class CanConvertToMethod {
        @Test
        @DisplayName("should return false when target type is null.")
        public void test1() {
            DefaultConverter handler = handlerToTest();
            boolean canConvert = handler.canConvertTo(null);
            assertFalse(canConvert);
        }

        @Test
        @DisplayName("should return true when target type is an Integer.")
        public void test2() {
            DefaultConverter handler = handlerToTest();
            boolean canConvert = handler.canConvertTo(Integer.class);
            assertTrue(canConvert);
        }

        @Test
        @DisplayName("should return true when target type is a primitive int.")
        public void test3() {
            DefaultConverter handler = handlerToTest();
            boolean canConvert = handler.canConvertTo(Integer.TYPE);
            assertTrue(canConvert);
        }

        @Test
        @DisplayName("should return true when target type is a Long.")
        public void test4() {
            DefaultConverter handler = handlerToTest();
            boolean canConvert = handler.canConvertTo(Long.class);
            assertTrue(canConvert);
        }

        @Test
        @DisplayName("should return true when target type is a primitive long.")
        public void test5() {
            DefaultConverter handler = handlerToTest();
            boolean canConvert = handler.canConvertTo(Long.TYPE);
            assertTrue(canConvert);
        }

        @Test
        @DisplayName("should return true when target type is a Float.")
        public void test6() {
            DefaultConverter handler = handlerToTest();
            boolean canConvert = handler.canConvertTo(Float.class);
            assertTrue(canConvert);
        }

        @Test
        @DisplayName("should return true when target type is a primitive float.")
        public void test7() {
            DefaultConverter handler = handlerToTest();
            boolean canConvert = handler.canConvertTo(Float.TYPE);
            assertTrue(canConvert);
        }

        @Test
        @DisplayName("should return true when target type is a Double.")
        public void test8() {
            DefaultConverter handler = handlerToTest();
            boolean canConvert = handler.canConvertTo(Double.class);
            assertTrue(canConvert);
        }

        @Test
        @DisplayName("should return true when target type is a primitive double.")
        public void test9() {
            DefaultConverter handler = handlerToTest();
            boolean canConvert = handler.canConvertTo(Double.TYPE);
            assertTrue(canConvert);
        }

        @Test
        @DisplayName("should return true when target type is a List.")
        public void test10() {
            DefaultConverter handler = handlerToTest();
            boolean canConvert = handler.canConvertTo(List.class);
            assertTrue(canConvert);
        }

        @Test
        @DisplayName("should return true when target type is a Collection.")
        public void test11() {
            DefaultConverter handler = handlerToTest();
            boolean canConvert = handler.canConvertTo(Collection.class);
            assertTrue(canConvert);
        }

        @Test
        @DisplayName("should return true when target type is an Array.")
        public void test12() {
            DefaultConverter handler = handlerToTest();
            boolean canConvert = handler.canConvertTo(String[].class);
            assertTrue(canConvert);
        }

        @Test
        @DisplayName("should return true when target type is an Optional.")
        public void test13() {
            DefaultConverter handler = handlerToTest();
            boolean canConvert = handler.canConvertTo(Optional.class);
            assertTrue(canConvert);
        }

        @Test
        @DisplayName("should return false when target type is not supported.")
        public void test14() {
            DefaultConverter handler = handlerToTest();
            // Not primitive, List/Collection, array or Optional.
            boolean canConvert = handler.canConvertTo(TestEnum.class);
            assertFalse(canConvert);
        }
    }

    @Nested
    class ConvertMethod {
        @Test
        @DisplayName("should throw when context is null.")
        public void test1() {
            DefaultConverter handler = handlerToTest();
            assertThrows(IllegalArgumentException.class, () -> handler.convert(null));
        }

        @Test
        @DisplayName("should return skip result when target type is not supported.")
        public void test2() {
            DefaultConverter handler = handlerToTest();

            // Not primitive, List/Collection, array or Optional.
            ProxyMethod proxyMethod = 
                ProxyMethodUtils.fromMethod(
                    EnumProxyInterface.class,
                    "enumProperty" // This method returns a TestEnum.
                );
            
            Converter<?> converter = new RootConverter(handler);

            ConversionContext context = new ConversionContext(
                converter,
                proxyMethod,
                TestEnum.ONE.name()
            );

            ConversionResult<?> result = handler.convert(context);
            assertEquals(ConversionResult.skip(), result);
        }

        @Test
        @DisplayName("should convert resolved property to an Integer or primitive int.")
        public void test3() {
            DefaultConverter handler = handlerToTest();

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

            assertEquals(1, wrapperValue);
            assertEquals(2, primitiveValue);
        }

        @Test
        @DisplayName("should convert resolved property to a Long or primitive long.")
        public void test4() {
            DefaultConverter handler = handlerToTest();

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

            assertEquals(1L, wrapperValue);
            assertEquals(2L, primitiveValue);
        }

        @Test
        @DisplayName("should convert resolved property to a Float or primitive float.")
        public void test5() {
            DefaultConverter handler = handlerToTest();

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

            assertEquals(1.0F, wrapperValue);
            assertEquals(2.0F, primitiveValue);
        }

        @Test
        @DisplayName("should convert resolved property to a Double or primitive double.")
        public void test6() {
            DefaultConverter handler = handlerToTest();

            ProxyMethod wrapperProxyMethod = 
                ProxyMethodUtils.fromMethod(
                    PrimitiveProxyInterface.class,
                    "doubleWrapperProperty" // This method returns a Double wrapper class
                );

            ProxyMethod primitiveProxyMethod = 
                ProxyMethodUtils.fromMethod(
                    PrimitiveProxyInterface.class,
                    "doublePrimitiveProperty" // This method returns an double primitive
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

            assertEquals(1.0D, wrapperValue);
            assertEquals(2.0D, primitiveValue);
        }

        @Test
        @DisplayName("should convert resolved property to a Short or primitive short.")
        public void test7() {
            DefaultConverter handler = handlerToTest();

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

            assertEquals((short)1, wrapperValue);
            assertEquals((short)2, primitiveValue);
        }

        @Test
        @DisplayName("should convert resolved property to a Boolean or primitive boolean.")
        public void test8() {
            DefaultConverter handler = handlerToTest();

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
        @DisplayName("should convert resolved property to a Byte or primitive byte.")
        public void test9() {
            DefaultConverter handler = handlerToTest();

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
        @DisplayName("should convert resolved property to a List or Collection.")
        public void test10() {
            DefaultConverter handler = handlerToTest();

            ProxyMethod listProxyMethodInfo = 
                ProxyMethodUtils.fromMethod(
                    ListProxyInterface.class,
                    "listProperty" // This method returns a List.
                );

            ProxyMethod collectionProxyMethodInfo = 
                ProxyMethodUtils.fromMethod(
                    ListProxyInterface.class,
                    "collectionProperty" // This method returns a Collection.
                );
            
            Converter<?> converter = new RootConverter(handler);

            ConversionContext listContext = new ConversionContext(
                converter,
                listProxyMethodInfo,
                "a,b,c"
            );

            ConversionContext collectionContext = new ConversionContext(
                converter,
                collectionProxyMethodInfo,
                "c,b,a"
            );

            ConversionResult<?> listResult = handler.convert(listContext);
            ConversionResult<?> collectionResult = handler.convert(collectionContext);
            
            assertNotNull(listResult);
            assertNotNull(collectionResult);
            Object listValue = listResult.value();
            Object collectionValue = collectionResult.value();

            assertNotNull(listValue);
            assertNotNull(collectionValue);

            assertTrue(listValue instanceof List<?>);
            assertTrue(collectionValue instanceof Collection<?>);

            assertIterableEquals(
                Arrays.asList("a", "b", "c"), 
                (List<?>)listValue
            );
            assertIterableEquals(
                Arrays.asList("c", "b", "a"), 
                (Collection<?>)collectionValue
            );
        }

        @Test
        @DisplayName("should convert resolved property to an array.")
        public void test11() {
            DefaultConverter handler = handlerToTest();

            ProxyMethod proxyMethod = 
                ProxyMethodUtils.fromMethod(
                    ArrayProxyInterface.class,
                    "arrayProperty" // This method returns a String[].
                );
            
            Converter<?> converter = new RootConverter(handler);

            ConversionContext context = new ConversionContext(
                converter,
                proxyMethod,
                "a,b,c"
            );
            
            ConversionResult<?> arrayResult = handler.convert(context);

            assertNotNull(arrayResult);
            Object arrayValue = arrayResult.value();
            
            assertNotNull(arrayValue);
            assertTrue(arrayValue.getClass().isArray());
            assertArrayEquals(
                new String[] { "a", "b", "c" }, 
                (String[])arrayValue
            );
        }

        @Test
        @DisplayName("should convert resolved property to an Optional.")
        public void test12() {
            DefaultConverter handler = handlerToTest();

            ProxyMethod proxyMethod = 
                ProxyMethodUtils.fromMethod(
                    OptionalProxyInterface.class,
                    "optionalProperty" // This method returns an Optional.
                );
            
            Converter<?> converter = new RootConverter(handler);

            ConversionContext context = new ConversionContext(
                converter,
                proxyMethod,
                "optional-value"
            );
            
            ConversionResult<?> optionalResult = handler.convert(context);
            assertNotNull(optionalResult);
            Object optionalValue = optionalResult.value();
            
            assertNotNull(optionalValue);
            assertTrue(optionalValue instanceof Optional<?>);

            Optional<?> opt = (Optional<?>)optionalValue;
            assertTrue(opt.isPresent());
            assertEquals("optional-value", opt.get());
        }
    }

    private DefaultConverter handlerToTest() {
        return new DefaultConverter();
    }
}
