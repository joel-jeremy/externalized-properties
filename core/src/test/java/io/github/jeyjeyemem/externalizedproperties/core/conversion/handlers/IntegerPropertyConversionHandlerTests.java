package io.github.jeyjeyemem.externalizedproperties.core.conversion.handlers;

import io.github.jeyjeyemem.externalizedproperties.core.ExternalizedPropertyMethodInfo;
import io.github.jeyjeyemem.externalizedproperties.core.ResolvedProperty;
import io.github.jeyjeyemem.externalizedproperties.core.conversion.ResolvedPropertyConversionContext;
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

public class IntegerPropertyConversionHandlerTests {
    @Nested
    class CanConvertToMethod {
        @Test
        @DisplayName("should return false when expected type is null.")
        public void test1() {
            IntegerPropertyConversionHandler handler = handlerToTest();
            boolean canConvert = handler.canConvertTo(null);
            assertFalse(canConvert);
        }

        @Test
        @DisplayName("should return true when expected type is an Integer.")
        public void test2() {
            IntegerPropertyConversionHandler handler = handlerToTest();
            boolean canConvert = handler.canConvertTo(Integer.class);
            assertTrue(canConvert);
        }

        @Test
        @DisplayName("should return true when expected type is a primitive int.")
        public void test3() {
            IntegerPropertyConversionHandler handler = handlerToTest();
            boolean canConvert = handler.canConvertTo(Integer.TYPE);
            assertTrue(canConvert);
        }

        @Test
        @DisplayName("should return false when expected type is not a Integer/int.")
        public void test4() {
            IntegerPropertyConversionHandler handler = handlerToTest();
            boolean canConvert = handler.canConvertTo(String.class);
            assertFalse(canConvert);
        }
    }

    @Nested
    class ConvertMethod {
        @Test
        @DisplayName("should throw when context is null.")
        public void test1() {
            IntegerPropertyConversionHandler handler = handlerToTest();
            assertThrows(IllegalArgumentException.class, () -> handler.convert(null));
        }

        @Test
        @DisplayName("should convert resolved property to an Integer.")
        public void test2() {
            IntegerPropertyConversionHandler handler = handlerToTest();

            ExternalizedPropertyMethodInfo propertyMethodInfo = 
                StubExternalizedPropertyMethodInfo.fromMethod(
                    PrimitiveProxyInterface.class,
                    "integerWrapperProperty" // This method returns a Integer wrapper class
                );
            
            ResolvedPropertyConverter converter = 
                new InternalResolvedPropertyConverter(handler);

            ResolvedPropertyConversionContext context = 
                new ResolvedPropertyConversionContext(
                    converter,
                    propertyMethodInfo,
                    ResolvedProperty.with("property.integer.wrapper", "1")
                );

            Integer integer = handler.convert(context);
            
            assertNotNull(integer);
            assertEquals(1, integer);
        }

        @Test
        @DisplayName("should convert resolved property to a primitive int.")
        public void test3() {
            IntegerPropertyConversionHandler handler = handlerToTest();

            ExternalizedPropertyMethodInfo propertyMethodInfo = 
                StubExternalizedPropertyMethodInfo.fromMethod(
                    PrimitiveProxyInterface.class,
                    "intPrimitiveProperty" // This method returns a primitive int
                );
            
            ResolvedPropertyConverter converter = 
                new InternalResolvedPropertyConverter(handler);

            ResolvedPropertyConversionContext context = 
                new ResolvedPropertyConversionContext(
                    converter,
                    propertyMethodInfo,
                    ResolvedProperty.with("property.integer.primitive", "1")
                );

            int intValue = handler.convert(context);
            
            assertEquals(1, intValue);
        }

        @Test
        @DisplayName("should throw when property value is not a valid Integer.")
        public void test4() {
            IntegerPropertyConversionHandler handler = handlerToTest();

            ExternalizedPropertyMethodInfo propertyMethodInfo = 
                StubExternalizedPropertyMethodInfo.fromMethod(
                    PrimitiveProxyInterface.class,
                    "integerWrapperProperty" // This method returns Integer wrapper class
                );
            
            ResolvedPropertyConverter converter = 
                new InternalResolvedPropertyConverter(handler);

            ResolvedPropertyConversionContext context = 
                new ResolvedPropertyConversionContext(
                    converter,
                    propertyMethodInfo,
                    ResolvedProperty.with("property.integer.wrapper", "invalid_wrapper_integer")
                );

            assertThrows(ResolvedPropertyConversionException.class, () -> {
                handler.convert(context);
            });
        }

        @Test
        @DisplayName("should throw when property value is not a valid primitive int.")
        public void test5() {
            IntegerPropertyConversionHandler handler = handlerToTest();

            ExternalizedPropertyMethodInfo propertyMethodInfo = 
                StubExternalizedPropertyMethodInfo.fromMethod(
                    PrimitiveProxyInterface.class,
                    "intPrimitiveProperty" // This method returns a primitive int
                );
            
            ResolvedPropertyConverter converter = 
                new InternalResolvedPropertyConverter(handler);

            ResolvedPropertyConversionContext context = 
                new ResolvedPropertyConversionContext(
                    converter,
                    propertyMethodInfo,
                    ResolvedProperty.with("property.integer.wrapper", "invalid_primitive_int")
                );

            assertThrows(ResolvedPropertyConversionException.class, () -> {
                handler.convert(context);
            });
        }
    }

    private IntegerPropertyConversionHandler handlerToTest() {
        return new IntegerPropertyConversionHandler();
    }
}
