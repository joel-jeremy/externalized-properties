package io.github.jeyjeyemem.externalizedproperties.core.conversion.handlers;

import io.github.jeyjeyemem.externalizedproperties.core.ExternalizedPropertyMethodInfo;
import io.github.jeyjeyemem.externalizedproperties.core.ResolvedProperty;
import io.github.jeyjeyemem.externalizedproperties.core.conversion.ResolvedPropertyConversionHandlerContext;
import io.github.jeyjeyemem.externalizedproperties.core.conversion.ResolvedPropertyConverter;
import io.github.jeyjeyemem.externalizedproperties.core.exceptions.ResolvedPropertyConversionException;
import io.github.jeyjeyemem.externalizedproperties.core.internal.InternalResolvedPropertyConverter;
import io.github.jeyjeyemem.externalizedproperties.core.testentities.StubExternalizedPropertyMethodInfo;
import io.github.jeyjeyemem.externalizedproperties.core.testentities.proxy.PrimitiveProxyInterface;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class FloatPropertyConversionHandlerTests {
    @Nested
    class CanConvertToMethod {
        @Test
        @DisplayName("should return true when expected type is null.")
        public void test1() {
            FloatPropertyConversionHandler handler = handlerToTest();
            boolean canConvert = handler.canConvertTo(null);
            assertFalse(canConvert);
        }

        @Test
        @DisplayName("should return true when expected type is a Float.")
        public void test2() {
            FloatPropertyConversionHandler handler = handlerToTest();
            boolean canConvert = handler.canConvertTo(Float.class);
            assertTrue(canConvert);
        }

        @Test
        @DisplayName("should return true when expected type is a primitive float.")
        public void test3() {
            FloatPropertyConversionHandler handler = handlerToTest();
            boolean canConvert = handler.canConvertTo(Float.TYPE);
            assertTrue(canConvert);
        }

        @Test
        @DisplayName("should return false when expected type is not a Float/float.")
        public void test4() {
            FloatPropertyConversionHandler handler = handlerToTest();
            boolean canConvert = handler.canConvertTo(String.class);
            assertFalse(canConvert);
        }
    }

    @Nested
    class ConvertMethod {
        @Test
        @DisplayName("should throw when context is null.")
        public void test1() {
            FloatPropertyConversionHandler handler = handlerToTest();
            assertThrows(IllegalArgumentException.class, () -> handler.convert(null));
        }

        @Test
        @DisplayName("should convert resolved property to a Float.")
        public void test2() {
            FloatPropertyConversionHandler handler = handlerToTest();

            ExternalizedPropertyMethodInfo propertyMethodInfo = 
                StubExternalizedPropertyMethodInfo.fromMethod(
                    PrimitiveProxyInterface.class,
                    "floatWrapperProperty" // This method returns a Float wrapper class
                );
            
            ResolvedPropertyConverter converter = 
                new InternalResolvedPropertyConverter(handler);

            ResolvedPropertyConversionHandlerContext context = 
                new ResolvedPropertyConversionHandlerContext(
                    converter,
                    propertyMethodInfo,
                    ResolvedProperty.with("property.float.wrapper", "1.1")
                );

            Float floatValue = handler.convert(context);
            
            assertNotNull(floatValue);
            assertEquals(1.1f, floatValue);
        }

        @Test
        @DisplayName("should convert resolved property to a primitive float.")
        public void test3() {
            FloatPropertyConversionHandler handler = handlerToTest();

            ExternalizedPropertyMethodInfo propertyMethodInfo = 
                StubExternalizedPropertyMethodInfo.fromMethod(
                    PrimitiveProxyInterface.class,
                    "floatPrimitiveProperty" // This method returns a primitive float
                );
            
            ResolvedPropertyConverter converter = 
                new InternalResolvedPropertyConverter(handler);

            ResolvedPropertyConversionHandlerContext context = 
                new ResolvedPropertyConversionHandlerContext(
                    converter,
                    propertyMethodInfo,
                    ResolvedProperty.with("property.float.primitive", "1.1")
                );

            float floatValue = handler.convert(context);
            
            assertEquals(1.1f, floatValue);
        }

        @Test
        @DisplayName("should throw when property value is not a valid Float.")
        public void test4() {
            FloatPropertyConversionHandler handler = handlerToTest();

            ExternalizedPropertyMethodInfo propertyMethodInfo = 
                StubExternalizedPropertyMethodInfo.fromMethod(
                    PrimitiveProxyInterface.class,
                    "floatWrapperProperty" // This method returns Float wrapper class
                );
            
            ResolvedPropertyConverter converter = 
                new InternalResolvedPropertyConverter(handler);

            ResolvedPropertyConversionHandlerContext context = 
                new ResolvedPropertyConversionHandlerContext(
                    converter,
                    propertyMethodInfo,
                    ResolvedProperty.with("property.float.wrapper", "invalid_wrapper_float")
                );

            assertThrows(ResolvedPropertyConversionException.class, () -> {
                handler.convert(context);
            });
        }

        @Test
        @DisplayName("should throw when property value is not a valid primitive float.")
        public void test5() {
            FloatPropertyConversionHandler handler = handlerToTest();

            ExternalizedPropertyMethodInfo propertyMethodInfo = 
                StubExternalizedPropertyMethodInfo.fromMethod(
                    PrimitiveProxyInterface.class,
                    "floatPrimitiveProperty" // This method returns a primitive float
                );
            
            ResolvedPropertyConverter converter = 
                new InternalResolvedPropertyConverter(handler);

            ResolvedPropertyConversionHandlerContext context = 
                new ResolvedPropertyConversionHandlerContext(
                    converter,
                    propertyMethodInfo,
                    ResolvedProperty.with("property.float.wrapper", "invalid_primitive_float")
                );

            assertThrows(ResolvedPropertyConversionException.class, () -> {
                handler.convert(context);
            });
        }
    }

    private FloatPropertyConversionHandler handlerToTest() {
        return new FloatPropertyConversionHandler();
    }
}
