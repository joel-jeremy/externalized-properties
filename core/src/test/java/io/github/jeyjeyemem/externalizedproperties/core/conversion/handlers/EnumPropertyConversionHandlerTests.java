package io.github.jeyjeyemem.externalizedproperties.core.conversion.handlers;

import io.github.jeyjeyemem.externalizedproperties.core.ExternalizedPropertyMethodInfo;
import io.github.jeyjeyemem.externalizedproperties.core.ResolvedProperty;
import io.github.jeyjeyemem.externalizedproperties.core.conversion.ResolvedPropertyConversionHandlerContext;
import io.github.jeyjeyemem.externalizedproperties.core.conversion.ResolvedPropertyConverter;
import io.github.jeyjeyemem.externalizedproperties.core.exceptions.ResolvedPropertyConversionException;
import io.github.jeyjeyemem.externalizedproperties.core.internal.InternalResolvedPropertyConverter;
import io.github.jeyjeyemem.externalizedproperties.core.testentities.StubExternalizedPropertyMethodInfo;
import io.github.jeyjeyemem.externalizedproperties.core.testentities.proxy.EnumProxyInterface;
import io.github.jeyjeyemem.externalizedproperties.core.testentities.proxy.EnumProxyInterface.TestEnum;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class EnumPropertyConversionHandlerTests {
    @Nested
    class CanConvertToMethod {
        @Test
        @DisplayName("should return true when expected type is null.")
        public void test1() {
            EnumPropertyConversionHandler<TestEnum> handler = handlerToTest(TestEnum.class);
            boolean canConvert = handler.canConvertTo(null);
            assertFalse(canConvert);
        }

        @Test
        @DisplayName(
            "should return true when expected type matches the conversion handler's enum class."
        )
        public void test2() {
            EnumPropertyConversionHandler<TestEnum> handler = handlerToTest(TestEnum.class);
            boolean canConvert = handler.canConvertTo(TestEnum.class);
            assertTrue(canConvert);
        }

        @Test
        @DisplayName(
            "should return false when expected type does not match the conversion handler's enum class."
        )
        public void test3() {
            EnumPropertyConversionHandler<TestEnum> handler = handlerToTest(TestEnum.class);
            boolean canConvert = handler.canConvertTo(String.class);
            assertFalse(canConvert);
        }
    }

    @Nested
    class ConvertMethod {
        @Test
        @DisplayName("should throw when context is null.")
        public void test1() {
            EnumPropertyConversionHandler<TestEnum> handler = handlerToTest(TestEnum.class);
            assertThrows(IllegalArgumentException.class, () -> handler.convert(null));
        }

        @Test
        @DisplayName("should convert resolved property to the conversion handler's enum class.")
        public void test2() {
            EnumPropertyConversionHandler<TestEnum> handler = handlerToTest(TestEnum.class);

            ExternalizedPropertyMethodInfo propertyMethodInfo = 
                StubExternalizedPropertyMethodInfo.fromMethod(
                    EnumProxyInterface.class,
                    "enumProperty"
                );
            
            ResolvedPropertyConverter converter = 
                new InternalResolvedPropertyConverter(handler);

            ResolvedPropertyConversionHandlerContext context = 
                new ResolvedPropertyConversionHandlerContext(
                    converter,
                    propertyMethodInfo,
                    ResolvedProperty.with("property.enum", "ONE")
                );

            TestEnum testEnum = handler.convert(context);
            
            assertNotNull(testEnum);
            assertEquals(TestEnum.ONE, testEnum);
        }

        @Test
        @DisplayName("should throw when property value is not a valid enum value.")
        public void test3() {
            EnumPropertyConversionHandler<TestEnum> handler = handlerToTest(TestEnum.class);

            ExternalizedPropertyMethodInfo propertyMethodInfo = 
                StubExternalizedPropertyMethodInfo.fromMethod(
                    EnumProxyInterface.class,
                    "enumProperty"
                );
            
            ResolvedPropertyConverter converter = 
                new InternalResolvedPropertyConverter(handler);

            ResolvedPropertyConversionHandlerContext context = 
                new ResolvedPropertyConversionHandlerContext(
                    converter,
                    propertyMethodInfo,
                    ResolvedProperty.with("property.enum", "INVALID_ENUM_VALUE")
                );

            assertThrows(ResolvedPropertyConversionException.class, () -> {
                handler.convert(context);
            });
        }
    }

    private <T extends Enum<T>> EnumPropertyConversionHandler<T> handlerToTest(Class<T> enumClass) {
        return new EnumPropertyConversionHandler<>(enumClass);
    }
}
