package io.github.jeyjeyemem.externalizedproperties.core.conversion.handlers;

import io.github.jeyjeyemem.externalizedproperties.core.ExternalizedPropertyMethodInfo;
import io.github.jeyjeyemem.externalizedproperties.core.ResolvedProperty;
import io.github.jeyjeyemem.externalizedproperties.core.conversion.PropertyMethodConversionContext;
import io.github.jeyjeyemem.externalizedproperties.core.conversion.Converter;
import io.github.jeyjeyemem.externalizedproperties.core.exceptions.ConversionException;
import io.github.jeyjeyemem.externalizedproperties.core.internal.InternalConverter;
import io.github.jeyjeyemem.externalizedproperties.core.testentities.StubExternalizedPropertyMethodInfo;
import io.github.jeyjeyemem.externalizedproperties.core.testentities.proxy.ArrayProxyInterface;
import io.github.jeyjeyemem.externalizedproperties.core.testentities.proxy.EnumProxyInterface;
import io.github.jeyjeyemem.externalizedproperties.core.testentities.proxy.ListProxyInterface;
import io.github.jeyjeyemem.externalizedproperties.core.testentities.proxy.OptionalProxyInterface;
import io.github.jeyjeyemem.externalizedproperties.core.testentities.proxy.PrimitiveProxyInterface;
import io.github.jeyjeyemem.externalizedproperties.core.testentities.proxy.EnumProxyInterface.TestEnum;
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

public class DefaultConversionHandlerTests {
    @Nested
    class CanConvertToMethod {
        @Test
        @DisplayName("should return false when expected type is null.")
        public void test1() {
            DefaultConversionHandler handler = handlerToTest();
            boolean canConvert = handler.canConvertTo(null);
            assertFalse(canConvert);
        }

        @Test
        @DisplayName("should return true when expected type is an Integer.")
        public void test2() {
            DefaultConversionHandler handler = handlerToTest();
            boolean canConvert = handler.canConvertTo(Integer.class);
            assertTrue(canConvert);
        }

        @Test
        @DisplayName("should return true when expected type is a primitive int.")
        public void test3() {
            DefaultConversionHandler handler = handlerToTest();
            boolean canConvert = handler.canConvertTo(Integer.TYPE);
            assertTrue(canConvert);
        }

        @Test
        @DisplayName("should return true when expected type is a Long.")
        public void test4() {
            DefaultConversionHandler handler = handlerToTest();
            boolean canConvert = handler.canConvertTo(Long.class);
            assertTrue(canConvert);
        }

        @Test
        @DisplayName("should return true when expected type is a primitive long.")
        public void test5() {
            DefaultConversionHandler handler = handlerToTest();
            boolean canConvert = handler.canConvertTo(Long.TYPE);
            assertTrue(canConvert);
        }

        @Test
        @DisplayName("should return true when expected type is a Float.")
        public void test6() {
            DefaultConversionHandler handler = handlerToTest();
            boolean canConvert = handler.canConvertTo(Float.class);
            assertTrue(canConvert);
        }

        @Test
        @DisplayName("should return true when expected type is a primitive float.")
        public void test7() {
            DefaultConversionHandler handler = handlerToTest();
            boolean canConvert = handler.canConvertTo(Float.TYPE);
            assertTrue(canConvert);
        }

        @Test
        @DisplayName("should return true when expected type is a Double.")
        public void test8() {
            DefaultConversionHandler handler = handlerToTest();
            boolean canConvert = handler.canConvertTo(Double.class);
            assertTrue(canConvert);
        }

        @Test
        @DisplayName("should return true when expected type is a primitive double.")
        public void test9() {
            DefaultConversionHandler handler = handlerToTest();
            boolean canConvert = handler.canConvertTo(Double.TYPE);
            assertTrue(canConvert);
        }

        @Test
        @DisplayName("should return true when expected type is a List.")
        public void test10() {
            DefaultConversionHandler handler = handlerToTest();
            boolean canConvert = handler.canConvertTo(List.class);
            assertTrue(canConvert);
        }

        @Test
        @DisplayName("should return true when expected type is a Collection.")
        public void test11() {
            DefaultConversionHandler handler = handlerToTest();
            boolean canConvert = handler.canConvertTo(Collection.class);
            assertTrue(canConvert);
        }

        @Test
        @DisplayName("should return true when expected type is an Array.")
        public void test12() {
            DefaultConversionHandler handler = handlerToTest();
            boolean canConvert = handler.canConvertTo(String[].class);
            assertTrue(canConvert);
        }

        @Test
        @DisplayName("should return true when expected type is an Optional.")
        public void test13() {
            DefaultConversionHandler handler = handlerToTest();
            boolean canConvert = handler.canConvertTo(Optional.class);
            assertTrue(canConvert);
        }

        @Test
        @DisplayName("should return false when expected type is not supported.")
        public void test14() {
            DefaultConversionHandler handler = handlerToTest();
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
            DefaultConversionHandler handler = handlerToTest();
            assertThrows(IllegalArgumentException.class, () -> handler.convert(null));
        }

        @Test
        @DisplayName("should throw when expected type is not supported.")
        public void test2() {
            DefaultConversionHandler handler = handlerToTest();

            // Not primitive, List/Collection, array or Optional.
            ExternalizedPropertyMethodInfo propertyMethodInfo = 
                StubExternalizedPropertyMethodInfo.fromMethod(
                    EnumProxyInterface.class,
                    "enumProperty" // This method returns a TestEnum.
                );
            
            Converter converter = 
                new InternalConverter(handler);

            PropertyMethodConversionContext context = 
                new PropertyMethodConversionContext(
                    converter,
                    propertyMethodInfo,
                    ResolvedProperty.with("property.enum", TestEnum.ONE.name()),
                    TestEnum.class // Expected type is a List.
                );
            
            assertThrows(
                ConversionException.class, 
                () -> handler.convert(context)
            );
        }

        @Test
        @DisplayName("should convert resolved property to an Integer or primitive int.")
        public void test3() {
            DefaultConversionHandler handler = handlerToTest();

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
                    ResolvedProperty.with("property.integer.wrapper", "1")
                );

            PropertyMethodConversionContext primitiveContext = 
                new PropertyMethodConversionContext(
                    converter,
                    primitivePropertyMethod,
                    ResolvedProperty.with("property.integer.primitive", "2")
                );

            Object wrapperValue = handler.convert(wrapperContext);
            Object primitiveValue = handler.convert(primitiveContext);
            
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
            DefaultConversionHandler handler = handlerToTest();

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
                    ResolvedProperty.with("property.long.wrapper", "1")
                );

            PropertyMethodConversionContext primitiveContext = 
                new PropertyMethodConversionContext(
                    converter,
                    primitivePropertyMethod,
                    ResolvedProperty.with("property.long.primitive", "2")
                );

            Object wrapperValue = handler.convert(wrapperContext);
            Object primitiveValue = handler.convert(primitiveContext);
            
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
            DefaultConversionHandler handler = handlerToTest();

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
                    ResolvedProperty.with("property.float.wrapper", "1.0")
                );

            PropertyMethodConversionContext primitiveContext = 
                new PropertyMethodConversionContext(
                    converter,
                    primitivePropertyMethod,
                    ResolvedProperty.with("property.float.primitive", "2.0")
                );

            Object wrapperValue = handler.convert(wrapperContext);
            Object primitiveValue = handler.convert(primitiveContext);
            
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
            DefaultConversionHandler handler = handlerToTest();

            ExternalizedPropertyMethodInfo wrapperPropertyMethod = 
                StubExternalizedPropertyMethodInfo.fromMethod(
                    PrimitiveProxyInterface.class,
                    "doubleWrapperProperty" // This method returns a Double wrapper class
                );

            ExternalizedPropertyMethodInfo primitivePropertyMethod = 
                StubExternalizedPropertyMethodInfo.fromMethod(
                    PrimitiveProxyInterface.class,
                    "doublePrimitiveProperty" // This method returns an double primitive
                );
            
            Converter converter = 
                new InternalConverter(handler);

            PropertyMethodConversionContext wrapperContext = 
                new PropertyMethodConversionContext(
                    converter,
                    wrapperPropertyMethod,
                    ResolvedProperty.with("property.double.wrapper", "1.0")
                );

            PropertyMethodConversionContext primitiveContext = 
                new PropertyMethodConversionContext(
                    converter,
                    primitivePropertyMethod,
                    ResolvedProperty.with("property.double.primitive", "2.0")
                );

            Object wrapperValue = handler.convert(wrapperContext);
            Object primitiveValue = handler.convert(primitiveContext);
            
            assertNotNull(wrapperValue);
            assertNotNull(primitiveValue);

            assertTrue(wrapperValue instanceof Double);
            assertTrue(primitiveValue instanceof Double);

            assertEquals(1.0D, wrapperValue);
            assertEquals(2.0D, primitiveValue);
        }

        @Test
        @DisplayName("should convert resolved property to a List or Collection.")
        public void test7() {
            DefaultConversionHandler handler = handlerToTest();

            ExternalizedPropertyMethodInfo listPropertyMethod = 
                StubExternalizedPropertyMethodInfo.fromMethod(
                    ListProxyInterface.class,
                    "listProperty" // This method returns a List.
                );

            ExternalizedPropertyMethodInfo collectionPropertyMethod = 
                StubExternalizedPropertyMethodInfo.fromMethod(
                    ListProxyInterface.class,
                    "collectionProperty" // This method returns a Collection.
                );
            
            Converter converter = 
                new InternalConverter(handler);

            PropertyMethodConversionContext wrapperContext = 
                new PropertyMethodConversionContext(
                    converter,
                    listPropertyMethod,
                    ResolvedProperty.with("property.list", "a,b,c")
                );

            PropertyMethodConversionContext primitiveContext = 
                new PropertyMethodConversionContext(
                    converter,
                    collectionPropertyMethod,
                    ResolvedProperty.with("property.collection", "c,b,a")
                );

            Object listValue = handler.convert(wrapperContext);
            Object collectionValue = handler.convert(primitiveContext);
            
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
        public void test8() {
            DefaultConversionHandler handler = handlerToTest();

            ExternalizedPropertyMethodInfo propertyMethod = 
                StubExternalizedPropertyMethodInfo.fromMethod(
                    ArrayProxyInterface.class,
                    "arrayProperty" // This method returns a String[].
                );
            
            Converter converter = 
                new InternalConverter(handler);

            PropertyMethodConversionContext context = 
                new PropertyMethodConversionContext(
                    converter,
                    propertyMethod,
                    ResolvedProperty.with("property.list", "a,b,c")
                );
            
            Object arrayValue = handler.convert(context);
            
            assertNotNull(arrayValue);

            assertTrue(arrayValue.getClass().isArray());
            assertArrayEquals(
                new String[] { "a", "b", "c" }, 
                (String[])arrayValue
            );
        }

        @Test
        @DisplayName("should convert resolved property to an Optional.")
        public void test9() {
            DefaultConversionHandler handler = handlerToTest();

            ExternalizedPropertyMethodInfo propertyMethod = 
                StubExternalizedPropertyMethodInfo.fromMethod(
                    OptionalProxyInterface.class,
                    "optionalProperty" // This method returns an Optional.
                );
            
            Converter converter = 
                new InternalConverter(handler);

            PropertyMethodConversionContext context = 
                new PropertyMethodConversionContext(
                    converter,
                    propertyMethod,
                    ResolvedProperty.with("property.optional", "optional-value")
                );
            
            Object optionalValue = handler.convert(context);
            
            assertNotNull(optionalValue);

            assertTrue(optionalValue instanceof Optional<?>);

            Optional<?> opt = (Optional<?>)optionalValue;
            assertTrue(opt.isPresent());
            assertEquals("optional-value", opt.get());
        }
    }

    private DefaultConversionHandler handlerToTest() {
        return new DefaultConversionHandler();
    }
}
