package io.github.jeyjeyemem.externalizedproperties.core.conversion.handlers;

import io.github.jeyjeyemem.externalizedproperties.core.ExternalizedPropertyMethodInfo;
import io.github.jeyjeyemem.externalizedproperties.core.ResolvedProperty;
import io.github.jeyjeyemem.externalizedproperties.core.conversion.ResolvedPropertyConversionHandlerContext;
import io.github.jeyjeyemem.externalizedproperties.core.conversion.ResolvedPropertyConverter;
import io.github.jeyjeyemem.externalizedproperties.core.internal.InternalResolvedPropertyConverter;
import io.github.jeyjeyemem.externalizedproperties.core.testentities.StubExternalizedPropertyMethodInfo;
import io.github.jeyjeyemem.externalizedproperties.core.testentities.proxy.OptionalProxyInterface;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class OptionalPropertyConversionHandlerTests {
    @Nested
    class CanConvertToMethod {
        @Test
        @DisplayName("should return true when expected type is null.")
        public void test1() {
            OptionalPropertyConversionHandler handler = handlerToTest();
            boolean canConvert = handler.canConvertTo(null);
            assertFalse(canConvert);
        }

        @Test
        @DisplayName("should return true when expected type is an Optional class.")
        public void test2() {
            OptionalPropertyConversionHandler handler = handlerToTest();
            boolean canConvert = handler.canConvertTo(Optional.class);
            assertTrue(canConvert);
        }

        @Test
        @DisplayName("should return false when expected type is not an Optional class.")
        public void test3() {
            OptionalPropertyConversionHandler handler = handlerToTest();
            boolean canConvert = handler.canConvertTo(String.class);
            assertFalse(canConvert);
        }
    }

    @Nested
    class ConvertMethod {
        @Test
        @DisplayName("should throw when context is null.")
        public void test1() {
            OptionalPropertyConversionHandler handler = handlerToTest();
            assertThrows(IllegalArgumentException.class, () -> handler.convert(null));
        }

        @Test
        @DisplayName("should convert resolved property to an Optional.")
        public void test2() {
            OptionalPropertyConversionHandler handler = handlerToTest();

            ExternalizedPropertyMethodInfo propertyMethodInfo = 
                StubExternalizedPropertyMethodInfo.fromMethod(
                    OptionalProxyInterface.class,
                    "optionalProperty"
                );
            
            ResolvedPropertyConverter converter = 
                new InternalResolvedPropertyConverter(handler);

            ResolvedPropertyConversionHandlerContext context = 
                new ResolvedPropertyConversionHandlerContext(
                    converter,
                    propertyMethodInfo,
                    ResolvedProperty.with("property.optional", "value")
                );

            Optional<?> optional = handler.convert(context);
            
            assertNotNull(optional);
            assertTrue(optional.isPresent());
            assertTrue(optional.get() instanceof String);
            assertEquals("value", optional.get());
        }

        @Test
        @DisplayName("should convert resolved property according to the Optional's generic parameter type.")
        public void test3() {
            OptionalPropertyConversionHandler handler = handlerToTest();

            ExternalizedPropertyMethodInfo propertyMethodInfo = 
                StubExternalizedPropertyMethodInfo.fromMethod(
                    OptionalProxyInterface.class,
                    "nonStringOptionalProperty"
                );
            
            ResolvedPropertyConverter converter = new InternalResolvedPropertyConverter(
                handler,
                new IntegerPropertyConversionHandler()
            );

            ResolvedPropertyConversionHandlerContext context = 
                new ResolvedPropertyConversionHandlerContext(
                    converter,
                    propertyMethodInfo,
                    ResolvedProperty.with("property.optional.nonstring", "1")
                );

            Optional<?> optional = handler.convert(context);
            
            assertNotNull(optional);
            assertTrue(optional.isPresent());
            assertTrue(optional.get() instanceof Integer);
            assertEquals(1, optional.get());
        }

        @Test
        @DisplayName("should return String value when Optional's generic parameter type is Object.")
        public void test4() {
            OptionalPropertyConversionHandler handler = handlerToTest();

            ExternalizedPropertyMethodInfo propertyMethodInfo = 
                StubExternalizedPropertyMethodInfo.fromMethod(
                    OptionalProxyInterface.class,
                    "optionalPropertyObject"
                );
            
            ResolvedPropertyConverter converter = 
                new InternalResolvedPropertyConverter(handler);

            ResolvedPropertyConversionHandlerContext context = 
                new ResolvedPropertyConversionHandlerContext(
                    converter,
                    propertyMethodInfo,
                    ResolvedProperty.with("property.optional.object", "value")
                );

            Optional<?> optional = handler.convert(context);
            
            assertNotNull(optional);
            assertTrue(optional.isPresent());
            assertTrue(optional.get() instanceof String);
            assertEquals("value", optional.get());
        }

        @Test
        @DisplayName("should return String value when Optional's generic parameter type is a wildcard.")
        public void test5() {
            OptionalPropertyConversionHandler handler = handlerToTest();

            ExternalizedPropertyMethodInfo propertyMethodInfo = 
                StubExternalizedPropertyMethodInfo.fromMethod(
                    OptionalProxyInterface.class,
                    "optionalPropertyWildcard"
                );
            
            ResolvedPropertyConverter converter = 
                new InternalResolvedPropertyConverter(handler);

            ResolvedPropertyConversionHandlerContext context = 
                new ResolvedPropertyConversionHandlerContext(
                    converter,
                    propertyMethodInfo,
                    ResolvedProperty.with("property.optional.woldcard", "value")
                );

            Optional<?> optional = handler.convert(context);
            
            assertNotNull(optional);
            assertTrue(optional.isPresent());
            assertTrue(optional.get() instanceof String);
            assertEquals("value", optional.get());
        }
    }

    private OptionalPropertyConversionHandler handlerToTest() {
        return new OptionalPropertyConversionHandler();
    }
}
