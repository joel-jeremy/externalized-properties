package io.github.jeyjeyemem.externalizedproperties.core.internal.conversion;

import io.github.jeyjeyemem.externalizedproperties.core.ConversionContext;
import io.github.jeyjeyemem.externalizedproperties.core.ConversionResult;
import io.github.jeyjeyemem.externalizedproperties.core.Converter;
import io.github.jeyjeyemem.externalizedproperties.core.conversion.converters.PrimitiveConverter;
import io.github.jeyjeyemem.externalizedproperties.core.exceptions.ConversionException;
import io.github.jeyjeyemem.externalizedproperties.core.proxy.ProxyMethod;
import io.github.jeyjeyemem.externalizedproperties.core.testentities.ProxyMethodUtils;
import io.github.jeyjeyemem.externalizedproperties.core.testentities.proxy.EnumProxyInterface;
import io.github.jeyjeyemem.externalizedproperties.core.testentities.proxy.EnumProxyInterface.TestEnum;
import io.github.jeyjeyemem.externalizedproperties.core.testentities.proxy.PrimitiveProxyInterface;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Collection;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class RootConverterTests {
    @Nested
    class Constructor {
        @Test
        @DisplayName(
            "should throw when converters collection argument is null."
        )
        public void test1() {
            assertThrows(
                IllegalArgumentException.class, 
                () -> new RootConverter(
                    (Collection<Converter<?>>)null
                )  
            );
        }

        @Test
        @DisplayName(
            "should throw when converters varargs argument is null."
        )
        public void test2() {
            assertThrows(
                IllegalArgumentException.class, 
                () -> new RootConverter(
                    (Converter<?>[])null
                )  
            );
        }
    }

    @Nested
    class CanConvertToMethod {
        @Test
        @DisplayName(
            "should return true when configured converters " + 
            "support conversion to the target type"
        )
        public void test1() {
            RootConverter converter = converter(
                new PrimitiveConverter()
            );

            assertTrue(converter.canConvertTo(Boolean.class));
            assertTrue(converter.canConvertTo(Integer.class));
            assertTrue(converter.canConvertTo(Long.class));
            assertTrue(converter.canConvertTo(Short.class));
            assertTrue(converter.canConvertTo(Float.class));
            assertTrue(converter.canConvertTo(Double.class));
            assertTrue(converter.canConvertTo(Byte.class));
            // Sanity check.
            assertFalse(converter.canConvertTo(List.class));
        }

        @Test
        @DisplayName(
            "should return false when configured converters " + 
            "does not support conversion to the target type"
        )
        public void test2() {
            RootConverter converter = converter(
                new PrimitiveConverter()
            );

            assertFalse(converter.canConvertTo(List.class));
            assertFalse(converter.canConvertTo(Properties.class));
            assertFalse(converter.canConvertTo(String[].class));
            // Sanity check.
            assertTrue(converter.canConvertTo(Boolean.class));
        }
    }

    @Nested
    class ConvertMethod {
        @Test
        @DisplayName(
            "should throw when context argument is null."
        )
        public void test1() {
            RootConverter converter = converter(
                new PrimitiveConverter()
            );

            assertThrows(
                IllegalArgumentException.class, 
                () -> converter.convert(null)
            );
        }

        @Test
        @DisplayName(
            "should correctly convert to target type."
        )
        public void test2() {
            RootConverter converter = converter(
                new PrimitiveConverter()
            );

            ProxyMethod proxyMethod = 
                ProxyMethodUtils.fromMethod(
                    PrimitiveProxyInterface.class, 
                    "intPrimitiveProperty"
                );

            ConversionResult<?> result = converter.convert(
                new ConversionContext(
                    converter,
                    proxyMethod,
                    "1"
                )
            );

            Object convertedValue = result.value();
            assertNotNull(convertedValue);
            assertTrue(convertedValue instanceof Integer);
            assertEquals(1, convertedValue);
        }

        @Test
        @DisplayName(
            "should throw when there is no handler that can convert to target type."
        )
        public void test4() {
            RootConverter converter = converter(
                new PrimitiveConverter()
            );

            ProxyMethod proxyMethod = 
                ProxyMethodUtils.fromMethod(
                    EnumProxyInterface.class,
                    "enumProperty"
                );

            // No handler registered to convert to TestEnum.
            assertThrows(
                ConversionException.class, 
                () -> converter.convert(new ConversionContext(
                    converter,
                    proxyMethod, 
                    TestEnum.ONE.name()
                ))
            );
        }

        @Test
        @DisplayName(
            "should wrap and re-throw when handler has thrown an exception."
        )
        public void test5() {
            // Handler that can convert anything but always throws.
            Converter<?> throwingHandler = 
                new Converter<Object>() {

                    @Override
                    public boolean canConvertTo(Class<?> targetType) {
                        return true;
                    }

                    @Override
                    public ConversionResult<Object> convert(ConversionContext context) {
                        throw new RuntimeException("Mr. Stark I don't feel so good...");
                    }
                };
            
            RootConverter converter = converter(throwingHandler);

            ProxyMethod proxyMethod = 
                ProxyMethodUtils.fromMethod(
                    PrimitiveProxyInterface.class,
                    "intPrimitiveProperty"
                );

            assertThrows(
                ConversionException.class, 
                () -> converter.convert(new ConversionContext(
                    converter,
                    proxyMethod,
                    "1"
                ))
            );
        }

        @Test
        @DisplayName(
            "should skip to next converter when skip result is returned."
        )
        public void test6() throws InterruptedException {
            Converter<?> handler1 = new Converter<Object>() {
                @Override
                public boolean canConvertTo(Class<?> targetType) {
                    return true;
                }

                @Override
                public ConversionResult<Object> convert(ConversionContext context) {
                    // Skipped.
                    return ConversionResult.skip();
                }
            };

            CountDownLatch handler2Latch = new CountDownLatch(1);

            Converter<?> handler2 = new Converter<Object>() {
                @Override
                public boolean canConvertTo(Class<?> targetType) {
                    return true;
                }

                @Override
                public ConversionResult<Object> convert(ConversionContext context) {
                    handler2Latch.countDown();
                    return ConversionResult.of(Integer.parseInt(context.value()));
                }
            };
            
            RootConverter converter = converter(
                handler1,
                handler2
            );

            ProxyMethod proxyMethod = 
                ProxyMethodUtils.fromMethod(
                    PrimitiveProxyInterface.class,
                    "intPrimitiveProperty"
                );

            ConversionResult<?> result = converter.convert(new ConversionContext(
                converter,
                proxyMethod,
                "1"
            ));

            Object convertedValue = result.value();
            assertTrue(handler2Latch.await(1, TimeUnit.MINUTES));
            assertNotNull(convertedValue);
            assertTrue(convertedValue instanceof Integer);
            assertEquals(1, convertedValue);
        }
    }

    private RootConverter converter(
            Converter<?>... converters) {
        return new RootConverter(converters);
    }
}
