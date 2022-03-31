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
            DefaultConverter converter = converterToTest();
            boolean canConvert = converter.canConvertTo(null);
            assertFalse(canConvert);
        }

        @Test
        @DisplayName("should return true when target type is an Integer.")
        public void test2() {
            DefaultConverter converter = converterToTest();
            boolean canConvert = converter.canConvertTo(Integer.class);
            assertTrue(canConvert);
        }

        @Test
        @DisplayName("should return true when target type is a primitive int.")
        public void test3() {
            DefaultConverter converter = converterToTest();
            boolean canConvert = converter.canConvertTo(Integer.TYPE);
            assertTrue(canConvert);
        }

        @Test
        @DisplayName("should return true when target type is a Long.")
        public void test4() {
            DefaultConverter converter = converterToTest();
            boolean canConvert = converter.canConvertTo(Long.class);
            assertTrue(canConvert);
        }

        @Test
        @DisplayName("should return true when target type is a primitive long.")
        public void test5() {
            DefaultConverter converter = converterToTest();
            boolean canConvert = converter.canConvertTo(Long.TYPE);
            assertTrue(canConvert);
        }

        @Test
        @DisplayName("should return true when target type is a Float.")
        public void test6() {
            DefaultConverter converter = converterToTest();
            boolean canConvert = converter.canConvertTo(Float.class);
            assertTrue(canConvert);
        }

        @Test
        @DisplayName("should return true when target type is a primitive float.")
        public void test7() {
            DefaultConverter converter = converterToTest();
            boolean canConvert = converter.canConvertTo(Float.TYPE);
            assertTrue(canConvert);
        }

        @Test
        @DisplayName("should return true when target type is a Double.")
        public void test8() {
            DefaultConverter converter = converterToTest();
            boolean canConvert = converter.canConvertTo(Double.class);
            assertTrue(canConvert);
        }

        @Test
        @DisplayName("should return true when target type is a primitive double.")
        public void test9() {
            DefaultConverter converter = converterToTest();
            boolean canConvert = converter.canConvertTo(Double.TYPE);
            assertTrue(canConvert);
        }

        @Test
        @DisplayName("should return true when target type is a List.")
        public void test10() {
            DefaultConverter converter = converterToTest();
            boolean canConvert = converter.canConvertTo(List.class);
            assertTrue(canConvert);
        }

        @Test
        @DisplayName("should return true when target type is a Collection.")
        public void test11() {
            DefaultConverter converter = converterToTest();
            boolean canConvert = converter.canConvertTo(Collection.class);
            assertTrue(canConvert);
        }

        @Test
        @DisplayName("should return true when target type is an Array.")
        public void test12() {
            DefaultConverter converter = converterToTest();
            boolean canConvert = converter.canConvertTo(String[].class);
            assertTrue(canConvert);
        }

        @Test
        @DisplayName("should return true when target type is an Optional.")
        public void test13() {
            DefaultConverter converter = converterToTest();
            boolean canConvert = converter.canConvertTo(Optional.class);
            assertTrue(canConvert);
        }

        @Test
        @DisplayName("should return false when target type is not supported.")
        public void test14() {
            DefaultConverter converter = converterToTest();
            // Not primitive, List/Collection, array or Optional.
            boolean canConvert = converter.canConvertTo(TestEnum.class);
            assertFalse(canConvert);
        }
    }

    @Nested
    class ConvertMethod {
        @Test
        @DisplayName("should throw when context is null.")
        public void test1() {
            DefaultConverter converter = converterToTest();
            assertThrows(IllegalArgumentException.class, () -> converter.convert(null));
        }

        @Test
        @DisplayName("should return skip result when target type is not supported.")
        public void test2() {
            DefaultConverter converter = converterToTest();

            // Not primitive, List/Collection, array or Optional.
            ProxyMethod proxyMethod = 
                ProxyMethodUtils.fromMethod(
                    EnumProxyInterface.class,
                    "enumProperty" // This method returns a TestEnum.
                );
            
            Converter<?> rootConverter = new RootConverter(converter);

            ConversionContext context = new ConversionContext(
                rootConverter,
                proxyMethod,
                TestEnum.ONE.name()
            );

            ConversionResult<?> result = converter.convert(context);
            assertEquals(ConversionResult.skip(), result);
        }

        @Test
        @DisplayName("should convert resolved property to an Integer or primitive int.")
        public void test3() {
            DefaultConverter converter = converterToTest();

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
            
            Converter<?> rootConverter = new RootConverter(converter);

            ConversionContext wrapperContext = new ConversionContext(
                rootConverter,
                wrapperProxyMethod,
                "1"
            );

            ConversionContext primitiveContext = new ConversionContext(
                rootConverter,
                primitiveProxyMethod,
                "2"
            );

            ConversionResult<?> wrapperResult = converter.convert(wrapperContext);
            ConversionResult<?> primitiveResult = converter.convert(primitiveContext);

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
            DefaultConverter converter = converterToTest();

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
            
            Converter<?> rootConverter = new RootConverter(converter);

            ConversionContext wrapperContext = new ConversionContext(
                rootConverter,
                wrapperProxyMethod,
                "1"
            );

            ConversionContext primitiveContext = new ConversionContext(
                rootConverter,
                primitiveProxyMethod,
                "2"
            );

            ConversionResult<?> wrapperResult = converter.convert(wrapperContext);
            ConversionResult<?> primitiveResult = converter.convert(primitiveContext);
            
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
            DefaultConverter converter = converterToTest();

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
            
            Converter<?> rootConverter = new RootConverter(converter);

            ConversionContext wrapperContext = new ConversionContext(
                rootConverter,
                wrapperProxyMethod,
                "1.0"
            );

            ConversionContext primitiveContext = new ConversionContext(
                rootConverter,
                primitiveProxyMethod,
                "2.0"
            );

            ConversionResult<?> wrapperResult = converter.convert(wrapperContext);
            ConversionResult<?> primitiveResult = converter.convert(primitiveContext);
            
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
            DefaultConverter converter = converterToTest();

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
            
            Converter<?> rootConverter = new RootConverter(converter);

            ConversionContext wrapperContext = new ConversionContext(
                rootConverter,
                wrapperProxyMethod,
                "1.0"
            );

            ConversionContext primitiveContext = new ConversionContext(
                rootConverter,
                primitiveProxyMethod,
                "2.0"
            );

            ConversionResult<?> wrapperResult = converter.convert(wrapperContext);
            ConversionResult<?> primitiveResult = converter.convert(primitiveContext);
            
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
            DefaultConverter converter = converterToTest();

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
            
            Converter<?> rootConverter = new RootConverter(converter);

            ConversionContext wrapperContext = new ConversionContext(
                rootConverter,
                wrapperProxyMethod,
                "1"
            );

            ConversionContext primitiveContext = new ConversionContext(
                rootConverter,
                primitiveProxyMethod,
                "2"
            );

            ConversionResult<?> wrapperResult = converter.convert(wrapperContext);
            ConversionResult<?> primitiveResult = converter.convert(primitiveContext);
            
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
            DefaultConverter converter = converterToTest();

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
            
            Converter<?> rootConverter = new RootConverter(converter);

            ConversionContext wrapperContext = new ConversionContext(
                rootConverter,
                wrapperProxyMethod,
                "true"
            );

            ConversionContext primitiveContext = new ConversionContext(
                rootConverter,
                primitiveProxyMethod,
                "false"
            );

            ConversionResult<?> wrapperResult = converter.convert(wrapperContext);
            ConversionResult<?> primitiveResult = converter.convert(primitiveContext);
            
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
            DefaultConverter converter = converterToTest();

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
            
            Converter<?> rootConverter = new RootConverter(converter);

            ConversionContext wrapperContext = new ConversionContext(
                rootConverter,
                wrapperProxyMethod,
                "1"
            );

            ConversionContext primitiveContext = new ConversionContext(
                rootConverter,
                primitiveProxyMethod,
                "2"
            );

            ConversionResult<?> wrapperResult = converter.convert(wrapperContext);
            ConversionResult<?> primitiveResult = converter.convert(primitiveContext);
            
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
            DefaultConverter converter = converterToTest();

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
            
            Converter<?> rootConverter = new RootConverter(converter);

            ConversionContext listContext = new ConversionContext(
                rootConverter,
                listProxyMethodInfo,
                "a,b,c"
            );

            ConversionContext collectionContext = new ConversionContext(
                rootConverter,
                collectionProxyMethodInfo,
                "c,b,a"
            );

            ConversionResult<?> listResult = converter.convert(listContext);
            ConversionResult<?> collectionResult = converter.convert(collectionContext);
            
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
            DefaultConverter converter = converterToTest();

            ProxyMethod proxyMethod = 
                ProxyMethodUtils.fromMethod(
                    ArrayProxyInterface.class,
                    "arrayProperty" // This method returns a String[].
                );
            
            Converter<?> rootConverter = new RootConverter(converter);

            ConversionContext context = new ConversionContext(
                rootConverter,
                proxyMethod,
                "a,b,c"
            );
            
            ConversionResult<?> arrayResult = converter.convert(context);

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
            DefaultConverter converter = converterToTest();

            ProxyMethod proxyMethod = 
                ProxyMethodUtils.fromMethod(
                    OptionalProxyInterface.class,
                    "optionalProperty" // This method returns an Optional.
                );
            
            Converter<?> rootConverter = new RootConverter(converter);

            ConversionContext context = new ConversionContext(
                rootConverter,
                proxyMethod,
                "optional-value"
            );
            
            ConversionResult<?> optionalResult = converter.convert(context);
            assertNotNull(optionalResult);
            Object optionalValue = optionalResult.value();
            
            assertNotNull(optionalValue);
            assertTrue(optionalValue instanceof Optional<?>);

            Optional<?> opt = (Optional<?>)optionalValue;
            assertTrue(opt.isPresent());
            assertEquals("optional-value", opt.get());
        }
    }

    private DefaultConverter converterToTest() {
        return new DefaultConverter();
    }
}
