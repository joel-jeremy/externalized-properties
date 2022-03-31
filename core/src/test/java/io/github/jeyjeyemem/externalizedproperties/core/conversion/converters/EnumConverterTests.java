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
            EnumConverter converter = converterToTest();
            boolean canConvert = converter.canConvertTo(null);
            assertFalse(canConvert);
        }

        @Test
        @DisplayName(
            "should return true when target type is an enum."
        )
        public void test2() {
            EnumConverter converter = converterToTest();
            boolean canConvert = converter.canConvertTo(TestEnum.class);
            assertTrue(canConvert);
        }

        @Test
        @DisplayName(
            "should return false when target type is not an enum."
        )
        public void test3() {
            EnumConverter converter = converterToTest();
            boolean canConvert = converter.canConvertTo(String.class);
            assertFalse(canConvert);
        }
    }

    @Nested
    class ConvertMethod {
        @Test
        @DisplayName("should throw when context is null.")
        public void test1() {
            EnumConverter converter = converterToTest();
            assertThrows(IllegalArgumentException.class, () -> converter.convert(null));
        }

        @Test
        @DisplayName("should convert resolved property to enum.")
        public void test2() {
            EnumConverter converter = converterToTest();

            ProxyMethod proxyMethod = 
                ProxyMethodUtils.fromMethod(
                    EnumProxyInterface.class,
                    "enumProperty"
                );
            
            Converter<?> rootConverter = new RootConverter(converter);

            ConversionContext context = new ConversionContext(
                rootConverter,
                proxyMethod,
                TestEnum.ONE.name()
            );

            ConversionResult<? extends Enum<?>> result = converter.convert(context);
            assertNotNull(result);

            Enum<?> testEnum = result.value();
            assertEquals(TestEnum.ONE, testEnum);
        }

        @Test
        @DisplayName("should throw when property value is not a valid enum value.")
        public void test3() {
            EnumConverter converter = converterToTest();

            ProxyMethod proxyMethod = 
                ProxyMethodUtils.fromMethod(
                    EnumProxyInterface.class,
                    "enumProperty"
                );
            
            Converter<?> rootConverter = new RootConverter(converter);

            ConversionContext context = new ConversionContext(
                rootConverter,
                proxyMethod,
                "INVALID_ENUM_VALUE"
            );

            assertThrows(ConversionException.class, () -> {
                converter.convert(context);
            });
        }

        @Test
        @DisplayName("should return skipped result when target type is not an enum.")
        public void test4() {
            EnumConverter converter = converterToTest();

            ProxyMethod proxyMethod = 
                ProxyMethodUtils.fromMethod(
                    PrimitiveProxyInterface.class,
                    "intPrimitiveProperty"
                );
            
            Converter<?> rootConverter = new RootConverter(converter);

            ConversionContext context = new ConversionContext(
                rootConverter,
                proxyMethod,
                "1"
            );
            
            ConversionResult<?> result = converter.convert(context);
            assertEquals(ConversionResult.skip(), result);
        }

        /**
         * Non-proxy tests.
         */

        // @Test
        // @DisplayName("should convert resolved property to enum.")
        // public void nonProxyTest1() {
        //     EnumConverter converter = converterToTest();
            
        //     Converter<?> rootConverter = new RootConverter(converter);

        //     ConversionContext context = new ConversionContext(
        //         rootConverter,
        //         TestEnum.class,
        //         TestEnum.ONE.name()
        //     );

        //     ConversionResult<? extends Enum<?>> result = converter.convert(context);
        //     assertNotNull(result);

        //     Enum<?> testEnum = result.value();
        //     assertEquals(TestEnum.ONE, testEnum);
        // }

        // @Test
        // @DisplayName("should throw when property value is not a valid enum value.")
        // public void nonProxyTest2() {
        //     EnumConverter converter = converterToTest();
            
        //     Converter<?> rootConverter = new RootConverter(converter);

        //     ConversionContext context = new ConversionContext(
        //         rootConverter,
        //         TestEnum.class,
        //         "INVALID_ENUM_VALUE"
        //     );

        //     assertThrows(ConversionException.class, () -> {
        //         converter.convert(context);
        //     });
        // }

        // @Test
        // @DisplayName("should return skipped result when target type is not an enum.")
        // public void nonProxyTest3() {
        //     EnumConverter converter = converterToTest();
            
        //     Converter<?> rootConverter = new RootConverter(converter);

        //     ConversionContext context = new ConversionContext(
        //         rootConverter,
        //         Integer.class,
        //         "1"
        //     );
            
        //     ConversionResult<?> result = converter.convert(context);
        //     assertEquals(ConversionResult.skip(), result);
        // }
    }

    private EnumConverter converterToTest() {
        return new EnumConverter();
    }
}
