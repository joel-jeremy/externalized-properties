package io.github.jeyjeyemem.externalizedproperties.core.conversion.handlers;

import io.github.jeyjeyemem.externalizedproperties.core.ExternalizedPropertyMethodInfo;
import io.github.jeyjeyemem.externalizedproperties.core.ResolvedProperty;
import io.github.jeyjeyemem.externalizedproperties.core.conversion.ResolvedPropertyConversionContext;
import io.github.jeyjeyemem.externalizedproperties.core.conversion.ResolvedPropertyConverter;
import io.github.jeyjeyemem.externalizedproperties.core.exceptions.ResolvedPropertyConversionException;
import io.github.jeyjeyemem.externalizedproperties.core.internal.InternalResolvedPropertyConverter;
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

public class PrimitivePropertyConversionHandlerTests {
    @Nested
    class CanConvertToMethod {
        @Test
        @DisplayName("should return false when expected type is null.")
        public void test1() {
            PrimitivePropertyConversionHandler handler = handlerToTest();
            boolean canConvert = handler.canConvertTo(null);
            assertFalse(canConvert);
        }

        @Test
        @DisplayName("should return true when expected type is an Integer.")
        public void test2() {
            PrimitivePropertyConversionHandler handler = handlerToTest();
            boolean canConvert = handler.canConvertTo(Integer.class);
            assertTrue(canConvert);
        }

        @Test
        @DisplayName("should return true when expected type is a primitive int.")
        public void test3() {
            PrimitivePropertyConversionHandler handler = handlerToTest();
            boolean canConvert = handler.canConvertTo(Integer.TYPE);
            assertTrue(canConvert);
        }

        @Test
        @DisplayName("should return true when expected type is a Long.")
        public void test4() {
            PrimitivePropertyConversionHandler handler = handlerToTest();
            boolean canConvert = handler.canConvertTo(Long.class);
            assertTrue(canConvert);
        }

        @Test
        @DisplayName("should return true when expected type is a primitive long.")
        public void test5() {
            PrimitivePropertyConversionHandler handler = handlerToTest();
            boolean canConvert = handler.canConvertTo(Long.TYPE);
            assertTrue(canConvert);
        }

        @Test
        @DisplayName("should return true when expected type is a Float.")
        public void test6() {
            PrimitivePropertyConversionHandler handler = handlerToTest();
            boolean canConvert = handler.canConvertTo(Float.class);
            assertTrue(canConvert);
        }

        @Test
        @DisplayName("should return true when expected type is a primitive float.")
        public void test7() {
            PrimitivePropertyConversionHandler handler = handlerToTest();
            boolean canConvert = handler.canConvertTo(Float.TYPE);
            assertTrue(canConvert);
        }

        @Test
        @DisplayName("should return true when expected type is a Double.")
        public void test8() {
            PrimitivePropertyConversionHandler handler = handlerToTest();
            boolean canConvert = handler.canConvertTo(Double.class);
            assertTrue(canConvert);
        }

        @Test
        @DisplayName("should return true when expected type is a primitive double.")
        public void test9() {
            PrimitivePropertyConversionHandler handler = handlerToTest();
            boolean canConvert = handler.canConvertTo(Double.TYPE);
            assertTrue(canConvert);
        }

        @Test
        @DisplayName("should return false when expected type is not a primitive type.")
        public void test10() {
            PrimitivePropertyConversionHandler handler = handlerToTest();
            boolean canConvert = handler.canConvertTo(List.class);
            assertFalse(canConvert);
        }
    }

    @Nested
    class ConvertMethod {
        @Test
        @DisplayName("should throw when context is null.")
        public void test1() {
            PrimitivePropertyConversionHandler handler = handlerToTest();
            assertThrows(IllegalArgumentException.class, () -> handler.convert(null));
        }

        @Test
        @DisplayName("should throw when expected type is not a primitive type.")
        public void test2() {
            PrimitivePropertyConversionHandler handler = handlerToTest();

            ExternalizedPropertyMethodInfo propertyMethodInfo = 
                StubExternalizedPropertyMethodInfo.fromMethod(
                    ListProxyInterface.class,
                    "listProperty" // This method returns a List class
                );
            
            ResolvedPropertyConverter converter = 
                new InternalResolvedPropertyConverter(handler);

            ResolvedPropertyConversionContext context = 
                new ResolvedPropertyConversionContext(
                    converter,
                    propertyMethodInfo,
                    ResolvedProperty.with("property.list", "a,b,c"),
                    List.class, // Expected type is a List.
                    String.class
                );
            
            assertThrows(
                ResolvedPropertyConversionException.class, 
                () -> handler.convert(context)
            );
        }

        @Test
        @DisplayName("should convert resolved property to an Integer or primitive int.")
        public void test3() {
            PrimitivePropertyConversionHandler handler = handlerToTest();

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
            
            ResolvedPropertyConverter converter = 
                new InternalResolvedPropertyConverter(handler);

            ResolvedPropertyConversionContext wrapperContext = 
                new ResolvedPropertyConversionContext(
                    converter,
                    wrapperPropertyMethod,
                    ResolvedProperty.with("property.integer.wrapper", "1")
                );

            ResolvedPropertyConversionContext primitiveContext = 
                new ResolvedPropertyConversionContext(
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
            PrimitivePropertyConversionHandler handler = handlerToTest();

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
            
            ResolvedPropertyConverter converter = 
                new InternalResolvedPropertyConverter(handler);

            ResolvedPropertyConversionContext wrapperContext = 
                new ResolvedPropertyConversionContext(
                    converter,
                    wrapperPropertyMethod,
                    ResolvedProperty.with("property.long.wrapper", "1")
                );

            ResolvedPropertyConversionContext primitiveContext = 
                new ResolvedPropertyConversionContext(
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
            PrimitivePropertyConversionHandler handler = handlerToTest();

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
            
            ResolvedPropertyConverter converter = 
                new InternalResolvedPropertyConverter(handler);

            ResolvedPropertyConversionContext wrapperContext = 
                new ResolvedPropertyConversionContext(
                    converter,
                    wrapperPropertyMethod,
                    ResolvedProperty.with("property.float.wrapper", "1.0")
                );

            ResolvedPropertyConversionContext primitiveContext = 
                new ResolvedPropertyConversionContext(
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
            PrimitivePropertyConversionHandler handler = handlerToTest();

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
            
            ResolvedPropertyConverter converter = 
                new InternalResolvedPropertyConverter(handler);

            ResolvedPropertyConversionContext wrapperContext = 
                new ResolvedPropertyConversionContext(
                    converter,
                    wrapperPropertyMethod,
                    ResolvedProperty.with("property.double.wrapper", "1.0")
                );

            ResolvedPropertyConversionContext primitiveContext = 
                new ResolvedPropertyConversionContext(
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
    }

    private PrimitivePropertyConversionHandler handlerToTest() {
        return new PrimitivePropertyConversionHandler();
    }
}
