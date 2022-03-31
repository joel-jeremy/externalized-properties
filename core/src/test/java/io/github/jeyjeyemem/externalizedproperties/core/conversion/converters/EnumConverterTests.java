package io.github.jeyjeyemem.externalizedproperties.core.conversion.converters;

import io.github.jeyjeyemem.externalizedproperties.core.ConversionContext;
import io.github.jeyjeyemem.externalizedproperties.core.ConversionResult;
import io.github.jeyjeyemem.externalizedproperties.core.Converter;
import io.github.jeyjeyemem.externalizedproperties.core.conversion.ConversionException;
import io.github.jeyjeyemem.externalizedproperties.core.internal.conversion.RootConverter;
import io.github.jeyjeyemem.externalizedproperties.core.proxy.ProxyMethod;
import io.github.jeyjeyemem.externalizedproperties.core.testentities.ProxyMethodUtils;
import io.github.jeyjeyemem.externalizedproperties.core.testentities.proxy.EnumProxyInterface;
import io.github.jeyjeyemem.externalizedproperties.core.testentities.proxy.EnumProxyInterface.TestEnum;
import io.github.jeyjeyemem.externalizedproperties.core.testentities.proxy.PrimitiveProxyInterface;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class EnumConverterTests {
    @Nested
    class CanConvertToMethod {
        @Test
        @DisplayName("should return false when target type is null.")
        public void test1() {
            EnumConverter handler = handlerToTest();
            boolean canConvert = handler.canConvertTo(null);
            assertFalse(canConvert);
        }

        @Test
        @DisplayName(
            "should return true when target type is an enum."
        )
        public void test2() {
            EnumConverter handler = handlerToTest();
            boolean canConvert = handler.canConvertTo(TestEnum.class);
            assertTrue(canConvert);
        }

        @Test
        @DisplayName(
            "should return false when target type is not an enum."
        )
        public void test3() {
            EnumConverter handler = handlerToTest();
            boolean canConvert = handler.canConvertTo(String.class);
            assertFalse(canConvert);
        }
    }

    @Nested
    class ConvertMethod {
        @Test
        @DisplayName("should throw when context is null.")
        public void test1() {
            EnumConverter handler = handlerToTest();
            assertThrows(IllegalArgumentException.class, () -> handler.convert(null));
        }

        @Test
        @DisplayName("should convert resolved property to enum.")
        public void test2() {
            EnumConverter handler = handlerToTest();

            ProxyMethod proxyMethod = 
                ProxyMethodUtils.fromMethod(
                    EnumProxyInterface.class,
                    "enumProperty"
                );
            
            Converter<?> converter = new RootConverter(handler);

            ConversionContext context = new ConversionContext(
                converter,
                proxyMethod,
                TestEnum.ONE.name()
            );

            ConversionResult<? extends Enum<?>> result = handler.convert(context);
            assertNotNull(result);

            Enum<?> testEnum = result.value();
            assertEquals(TestEnum.ONE, testEnum);
        }

        @Test
        @DisplayName("should throw when property value is not a valid enum value.")
        public void test3() {
            EnumConverter handler = handlerToTest();

            ProxyMethod proxyMethod = 
                ProxyMethodUtils.fromMethod(
                    EnumProxyInterface.class,
                    "enumProperty"
                );
            
            Converter<?> converter = new RootConverter(handler);

            ConversionContext context = new ConversionContext(
                converter,
                proxyMethod,
                "INVALID_ENUM_VALUE"
            );

            assertThrows(ConversionException.class, () -> {
                handler.convert(context);
            });
        }

        @Test
        @DisplayName("should return skipped result when target type is not an enum.")
        public void test4() {
            EnumConverter handler = handlerToTest();

            ProxyMethod proxyMethod = 
                ProxyMethodUtils.fromMethod(
                    PrimitiveProxyInterface.class,
                    "intPrimitiveProperty"
                );
            
            Converter<?> converter = new RootConverter(handler);

            ConversionContext context = new ConversionContext(
                converter,
                proxyMethod,
                "1"
            );
            
            ConversionResult<?> result = handler.convert(context);
            assertEquals(ConversionResult.skip(), result);
        }

        /**
         * Non-proxy tests.
         */

        // @Test
        // @DisplayName("should convert resolved property to enum.")
        // public void nonProxyTest1() {
        //     EnumConverter handler = handlerToTest();
            
        //     Converter<?> converter = new RootConverter(handler);

        //     ConversionContext context = new ConversionContext(
        //         converter,
        //         TestEnum.class,
        //         TestEnum.ONE.name()
        //     );

        //     ConversionResult<? extends Enum<?>> result = handler.convert(context);
        //     assertNotNull(result);

        //     Enum<?> testEnum = result.value();
        //     assertEquals(TestEnum.ONE, testEnum);
        // }

        // @Test
        // @DisplayName("should throw when property value is not a valid enum value.")
        // public void nonProxyTest2() {
        //     EnumConverter handler = handlerToTest();
            
        //     Converter<?> converter = new RootConverter(handler);

        //     ConversionContext context = new ConversionContext(
        //         converter,
        //         TestEnum.class,
        //         "INVALID_ENUM_VALUE"
        //     );

        //     assertThrows(ConversionException.class, () -> {
        //         handler.convert(context);
        //     });
        // }

        // @Test
        // @DisplayName("should return skipped result when target type is not an enum.")
        // public void nonProxyTest3() {
        //     EnumConverter handler = handlerToTest();
            
        //     Converter<?> converter = new RootConverter(handler);

        //     ConversionContext context = new ConversionContext(
        //         converter,
        //         Integer.class,
        //         "1"
        //     );
            
        //     ConversionResult<?> result = handler.convert(context);
        //     assertEquals(ConversionResult.skip(), result);
        // }
    }

    private EnumConverter handlerToTest() {
        return new EnumConverter();
    }
}
