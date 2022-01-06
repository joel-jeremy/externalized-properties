package io.github.jeyjeyemem.externalizedproperties.core.internal.conversion;

import io.github.jeyjeyemem.externalizedproperties.core.conversion.ConversionContext;
import io.github.jeyjeyemem.externalizedproperties.core.conversion.ConversionHandler;
import io.github.jeyjeyemem.externalizedproperties.core.conversion.ConversionResult;
import io.github.jeyjeyemem.externalizedproperties.core.conversion.handlers.PrimitiveConversionHandler;
import io.github.jeyjeyemem.externalizedproperties.core.exceptions.ConversionException;
import io.github.jeyjeyemem.externalizedproperties.core.testentities.StubProxyMethodInfo;
import io.github.jeyjeyemem.externalizedproperties.core.testentities.proxy.EnumProxyInterface;
import io.github.jeyjeyemem.externalizedproperties.core.testentities.proxy.EnumProxyInterface.TestEnum;
import io.github.jeyjeyemem.externalizedproperties.core.testentities.proxy.PrimitiveProxyInterface;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Collection;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class InternalConverterTests {
    @Nested
    class Constructor {
        @Test
        @DisplayName(
            "should throw when conversion handlers collection argument is null."
        )
        public void test1() {
            assertThrows(
                IllegalArgumentException.class, 
                () -> new InternalConverter(
                    (Collection<ConversionHandler<?>>)null
                )  
            );
        }

        @Test
        @DisplayName(
            "should throw when conversion handlers varargs argument is null."
        )
        public void test2() {
            assertThrows(
                IllegalArgumentException.class, 
                () -> new InternalConverter(
                    (ConversionHandler<?>[])null
                )  
            );
        }
    }

    @Nested
    class ConvertMethod {
        @Test
        @DisplayName(
            "should throw when context argument is null."
        )
        public void test1() {
            InternalConverter converter = converter(
                new PrimitiveConversionHandler()
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
            InternalConverter converter = converter(
                new PrimitiveConversionHandler()
            );

            StubProxyMethodInfo proxyMethodInfo = 
                StubProxyMethodInfo.fromMethod(
                    PrimitiveProxyInterface.class, 
                    "intPrimitiveProperty"
                );

            Object convertedValue = converter.convert(
                new ConversionContext(
                    converter,
                    proxyMethodInfo,
                    "1"
                )
            );

            assertNotNull(convertedValue);
            assertTrue(convertedValue instanceof Integer);
            assertEquals(1, convertedValue);
        }

        @Test
        @DisplayName(
            "should throw when there is no handler that can convert to target type."
        )
        public void test4() {
            InternalConverter converter = converter(
                new PrimitiveConversionHandler()
            );

            StubProxyMethodInfo proxyMethodInfo = 
                StubProxyMethodInfo.fromMethod(
                    EnumProxyInterface.class,
                    "enumProperty"
                );

            // No handler registered to convert to TestEnum.
            assertThrows(
                ConversionException.class, 
                () -> converter.convert(new ConversionContext(
                    converter,
                    proxyMethodInfo, 
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
            ConversionHandler<?> throwingHandler = 
                new ConversionHandler<Object>() {

                    @Override
                    public boolean canConvertTo(Class<?> targetType) {
                        return true;
                    }

                    @Override
                    public ConversionResult<Object> convert(ConversionContext context) {
                        throw new RuntimeException("Mr. Stark I don't feel so good...");
                    }
                };
            
            InternalConverter converter = converter(throwingHandler);

            StubProxyMethodInfo proxyMethodInfo = 
                StubProxyMethodInfo.fromMethod(
                    PrimitiveProxyInterface.class,
                    "intPrimitiveProperty"
                );

            assertThrows(
                ConversionException.class, 
                () -> converter.convert(new ConversionContext(
                    converter,
                    proxyMethodInfo,
                    "1"
                ))
            );
        }

        @Test
        @DisplayName(
            "should skip to next conversion handler when skip result is returned."
        )
        public void test6() throws InterruptedException {
            ConversionHandler<?> handler1 = new ConversionHandler<Object>() {
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

            ConversionHandler<?> handler2 = new ConversionHandler<Object>() {
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
            
            InternalConverter converter = converter(
                handler1,
                handler2
            );

            StubProxyMethodInfo proxyMethodInfo = 
                StubProxyMethodInfo.fromMethod(
                    PrimitiveProxyInterface.class,
                    "intPrimitiveProperty"
                );

            Object convertedValue = converter.convert(new ConversionContext(
                converter,
                proxyMethodInfo,
                "1"
            ));

            assertTrue(handler2Latch.await(1, TimeUnit.MINUTES));
            assertNotNull(convertedValue);
            assertTrue(convertedValue instanceof Integer);
            assertEquals(1, convertedValue);
        }

        private InternalConverter converter(
                ConversionHandler<?>... conversionHandlers) {
            return new InternalConverter(conversionHandlers);
        }
    }
}
