package io.github.jeyjeyemem.externalizedproperties.core.internal.conversion;

import io.github.jeyjeyemem.externalizedproperties.core.ConversionResult;
import io.github.jeyjeyemem.externalizedproperties.core.Converter;
import io.github.jeyjeyemem.externalizedproperties.core.ConverterProvider;
import io.github.jeyjeyemem.externalizedproperties.core.ExternalizedProperties;
import io.github.jeyjeyemem.externalizedproperties.core.conversion.ConversionException;
import io.github.jeyjeyemem.externalizedproperties.core.conversion.converters.DefaultConverter;
import io.github.jeyjeyemem.externalizedproperties.core.conversion.converters.PrimitiveConverter;
import io.github.jeyjeyemem.externalizedproperties.core.proxy.ProxyMethod;
import io.github.jeyjeyemem.externalizedproperties.core.testentities.proxy.EnumProxyInterface;
import io.github.jeyjeyemem.externalizedproperties.core.testentities.proxy.EnumProxyInterface.TestEnum;
import io.github.jeyjeyemem.externalizedproperties.core.testentities.proxy.PrimitiveProxyInterface;
import io.github.jeyjeyemem.externalizedproperties.core.testfixtures.ProxyMethodUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Type;
import java.util.Arrays;
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
            "should throw when externalized proeprties argument is null."
        )
        public void test1() {
            assertThrows(
                IllegalArgumentException.class, 
                () -> new RootConverter(
                    null,
                    (ep, rootConverter) -> new DefaultConverter(rootConverter)
                )  
            );
        }

        @Test
        @DisplayName(
            "should throw when converters collection argument is null."
        )
        public void test2() {
            assertThrows(
                IllegalArgumentException.class, 
                () -> new RootConverter(
                    ExternalizedProperties.builder().withDefaults().build(),
                    (Collection<ConverterProvider<?>>)null
                )  
            );
        }

        @Test
        @DisplayName(
            "should throw when converters varargs argument is null."
        )
        public void test3() {
            assertThrows(
                IllegalArgumentException.class, 
                () -> new RootConverter(
                    ExternalizedProperties.builder().withDefaults().build(),
                    (ConverterProvider[])null
                )  
            );
        }
    }

    @Nested
    class ProviderMethodWithVarArgsOverload {
        @Test
        @DisplayName("should throw when converter providers argument is null")
        void test1() {
            assertThrows(
                IllegalArgumentException.class, 
                () -> RootConverter.provider((ConverterProvider<?>[])null)
            );
        }

        @Test
        @DisplayName("should not return null")
        void test2() {
            RootConverter.Provider provider = 
                RootConverter.provider(DefaultConverter.provider());

            assertNotNull(provider);
        }

        @Test
        @DisplayName("should not return null on get")
        void test3() {
            RootConverter.Provider provider = 
                RootConverter.provider(DefaultConverter.provider());

            assertNotNull(
                provider.get(ExternalizedProperties.builder().withDefaults().build())
            );
        }
    }

    @Nested
    class ProviderMethodWithCollectionOverload {
        @Test
        @DisplayName("should throw when converter providers argument is null")
        void test1() {
            assertThrows(
                IllegalArgumentException.class, 
                () -> RootConverter.provider((Collection<ConverterProvider<?>>)null)
            );
        }

        @Test
        @DisplayName("should not return null")
        void test2() {
            RootConverter.Provider provider = 
                RootConverter.provider(
                    Arrays.asList(DefaultConverter.provider())
                );

            assertNotNull(provider);
        }

        @Test
        @DisplayName("should not return null on get")
        void test3() {
            RootConverter.Provider provider = 
                RootConverter.provider(
                    Arrays.asList(DefaultConverter.provider())
                );

            assertNotNull(
                provider.get(ExternalizedProperties.builder().withDefaults().build())
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
            ConverterProvider<?> provider = (ep, rc) -> new PrimitiveConverter();
            RootConverter converter = rootConverter(provider);

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
            ConverterProvider<?> provider = (ep, rc) -> new PrimitiveConverter();
            RootConverter converter = rootConverter(provider);

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
            "should throw when proxy method argument is null."
        )
        public void test1() {
            ConverterProvider<?> provider = (ep, rc) -> new PrimitiveConverter();
            RootConverter converter = rootConverter(provider);

            assertThrows(
                IllegalArgumentException.class, 
                () -> converter.convert(null, "valueToConvert", Integer.class)
            );
        }

        @Test
        @DisplayName(
            "should throw when value to convert argument is null."
        )
        public void test2() {
            ConverterProvider<?> provider = (ep, rc) -> new PrimitiveConverter();
            RootConverter converter = rootConverter(provider);

            ProxyMethod proxyMethod = 
                ProxyMethodUtils.fromMethod(
                    PrimitiveProxyInterface.class, 
                    "intPrimitiveProperty"
                );

            assertThrows(
                IllegalArgumentException.class, 
                () -> converter.convert(proxyMethod, null, Integer.class)
            );
        }

        @Test
        @DisplayName(
            "should throw when value target type argument is null."
        )
        public void test3() {
            ConverterProvider<?> provider = (ep, rc) -> new PrimitiveConverter();
            RootConverter converter = rootConverter(provider);

            ProxyMethod proxyMethod = 
                ProxyMethodUtils.fromMethod(
                    PrimitiveProxyInterface.class, 
                    "intPrimitiveProperty"
                );

            assertThrows(
                IllegalArgumentException.class, 
                () -> converter.convert(proxyMethod, "valueToConvert", null)
            );
        }

        @Test
        @DisplayName(
            "should correctly convert to target type."
        )
        public void test4() {
            ConverterProvider<?> provider = (ep, rc) -> new PrimitiveConverter();
            RootConverter converter = rootConverter(provider);

            ProxyMethod proxyMethod = 
                ProxyMethodUtils.fromMethod(
                    PrimitiveProxyInterface.class, 
                    "intPrimitiveProperty"
                );

            ConversionResult<?> result = converter.convert(
                proxyMethod,
                "1"
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
        public void tes54() {
            ConverterProvider<?> provider = (ep, rc) -> new PrimitiveConverter();
            RootConverter converter = rootConverter(provider);

            ProxyMethod proxyMethod = 
                ProxyMethodUtils.fromMethod(
                    EnumProxyInterface.class,
                    "enumProperty"
                );

            // No handler registered to convert to TestEnum.
            assertThrows(
                ConversionException.class, 
                () -> converter.convert(
                    proxyMethod, 
                    TestEnum.ONE.name()
                )
            );
        }

        @Test
        @DisplayName(
            "should wrap and re-throw when handler has thrown an exception."
        )
        public void test6() {
            // Handler that can convert anything but always throws.
            Converter<?> throwingHandler = 
                new Converter<Object>() {

                    @Override
                    public boolean canConvertTo(Class<?> targetType) {
                        return true;
                    }

                    @Override
                    public ConversionResult<Object> convert(
                            ProxyMethod proxyMethod,
                            String valueToConvert,
                            Type targetType
                    ) {
                        throw new RuntimeException("Mr. Stark I don't feel so good...");
                    }
                };
            
            ConverterProvider<?> provider = (ep, rc) -> throwingHandler;
            RootConverter converter = rootConverter(provider);

            ProxyMethod proxyMethod = 
                ProxyMethodUtils.fromMethod(
                    PrimitiveProxyInterface.class,
                    "intPrimitiveProperty"
                );

            assertThrows(
                ConversionException.class, 
                () -> converter.convert(
                    proxyMethod,
                    "1",
                    proxyMethod.returnType()
                )
            );
        }

        @Test
        @DisplayName(
            "should skip to next converter when skip result is returned."
        )
        public void test7() throws InterruptedException {
            Converter<?> handler1 = new Converter<Object>() {
                @Override
                public boolean canConvertTo(Class<?> targetType) {
                    return true;
                }

                @Override
                public ConversionResult<Object> convert(
                        ProxyMethod proxyMethod,
                        String valueToConvert,
                        Type targetType
                ) {
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
                public ConversionResult<Object> convert(
                        ProxyMethod proxyMethod,
                        String valueToConvert,
                        Type targetType
                ) {
                    handler2Latch.countDown();
                    return ConversionResult.of(Integer.parseInt(valueToConvert));
                }
            };

            ConverterProvider<?> provider1 = (ep, rc) -> handler1;
            ConverterProvider<?> provider2 = (ep, rc) -> handler2;
            RootConverter converter = rootConverter(
                provider1,
                provider2
            );

            ProxyMethod proxyMethod = 
                ProxyMethodUtils.fromMethod(
                    PrimitiveProxyInterface.class,
                    "intPrimitiveProperty"
                );

            ConversionResult<?> result = converter.convert(
                proxyMethod,
                "1",
                proxyMethod.returnType()
            );

            Object convertedValue = result.value();
            assertTrue(handler2Latch.await(1, TimeUnit.MINUTES));
            assertNotNull(convertedValue);
            assertTrue(convertedValue instanceof Integer);
            assertEquals(1, convertedValue);
        }
    }

    private RootConverter rootConverter(
            ConverterProvider<?>... converterProviders
    ) {
        return new RootConverter(
            ExternalizedProperties.builder()
                .withDefaultResolvers()
                .converters(converterProviders)
                .build(), 
            converterProviders
        );
    }
}
