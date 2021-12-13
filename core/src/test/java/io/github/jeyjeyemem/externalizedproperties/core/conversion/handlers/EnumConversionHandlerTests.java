package io.github.jeyjeyemem.externalizedproperties.core.conversion.handlers;

import io.github.jeyjeyemem.externalizedproperties.core.conversion.Converter;
import io.github.jeyjeyemem.externalizedproperties.core.conversion.ConversionContext;
import io.github.jeyjeyemem.externalizedproperties.core.exceptions.ConversionException;
import io.github.jeyjeyemem.externalizedproperties.core.internal.InternalConverter;
import io.github.jeyjeyemem.externalizedproperties.core.proxy.ProxyMethodInfo;
import io.github.jeyjeyemem.externalizedproperties.core.testentities.StubProxyMethodInfo;
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

public class EnumConversionHandlerTests {
    @Nested
    class CanConvertToMethod {
        @Test
        @DisplayName("should return false when target type is null.")
        public void test1() {
            EnumConversionHandler<TestEnum> handler = handlerToTest(TestEnum.class);
            boolean canConvert = handler.canConvertTo(null);
            assertFalse(canConvert);
        }

        @Test
        @DisplayName(
            "should return true when target type matches the conversion handler's enum class."
        )
        public void test2() {
            EnumConversionHandler<TestEnum> handler = handlerToTest(TestEnum.class);
            boolean canConvert = handler.canConvertTo(TestEnum.class);
            assertTrue(canConvert);
        }

        @Test
        @DisplayName(
            "should return false when target type does not match the conversion handler's enum class."
        )
        public void test3() {
            EnumConversionHandler<TestEnum> handler = handlerToTest(TestEnum.class);
            boolean canConvert = handler.canConvertTo(String.class);
            assertFalse(canConvert);
        }
    }

    @Nested
    class ConvertMethod {
        @Test
        @DisplayName("should throw when context is null.")
        public void test1() {
            EnumConversionHandler<TestEnum> handler = handlerToTest(TestEnum.class);
            assertThrows(IllegalArgumentException.class, () -> handler.convert(null));
        }

        @Test
        @DisplayName("should convert resolved property to the conversion handler's enum class.")
        public void test2() {
            EnumConversionHandler<TestEnum> handler = handlerToTest(TestEnum.class);

            ProxyMethodInfo proxyMethodInfo = 
                StubProxyMethodInfo.fromMethod(
                    EnumProxyInterface.class,
                    "enumProperty"
                );
            
            Converter converter = new InternalConverter(handler);

            ConversionContext context = new ConversionContext(
                converter,
                proxyMethodInfo,
                TestEnum.ONE.name()
            );

            TestEnum testEnum = handler.convert(context);
            
            assertNotNull(testEnum);
            assertEquals(TestEnum.ONE, testEnum);
        }

        @Test
        @DisplayName("should throw when property value is not a valid enum value.")
        public void test3() {
            EnumConversionHandler<TestEnum> handler = handlerToTest(TestEnum.class);

            ProxyMethodInfo proxyMethodInfo = 
                StubProxyMethodInfo.fromMethod(
                    EnumProxyInterface.class,
                    "enumProperty"
                );
            
            Converter converter = new InternalConverter(handler);

            ConversionContext context = new ConversionContext(
                converter,
                proxyMethodInfo,
                "INVALID_ENUM_VALUE"
            );

            assertThrows(ConversionException.class, () -> {
                handler.convert(context);
            });
        }
    }

    private <T extends Enum<T>> EnumConversionHandler<T> handlerToTest(Class<T> enumClass) {
        return new EnumConversionHandler<>(enumClass);
    }
}
