package io.github.joeljeremy7.externalizedproperties.core.internal.conversion;

import io.github.joeljeremy7.externalizedproperties.core.ConversionResult;
import io.github.joeljeremy7.externalizedproperties.core.Converter;
import io.github.joeljeremy7.externalizedproperties.core.ExternalizedProperties;
import io.github.joeljeremy7.externalizedproperties.core.ExternalizedProperty;
import io.github.joeljeremy7.externalizedproperties.core.InvocationContext;
import io.github.joeljeremy7.externalizedproperties.core.conversion.ConversionException;
import io.github.joeljeremy7.externalizedproperties.core.conversion.converters.BooleanConverter;
import io.github.joeljeremy7.externalizedproperties.core.conversion.converters.IntegerConverter;
import io.github.joeljeremy7.externalizedproperties.core.conversion.converters.ShortConverter;
import io.github.joeljeremy7.externalizedproperties.core.testfixtures.InvocationContextUtils;
import io.github.joeljeremy7.externalizedproperties.core.testfixtures.InvocationContextUtils.InvocationContextTestFactory;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Type;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Properties;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class RootConverterTests {
    private static final InvocationContextTestFactory<ProxyInterface> INVOCATION_CONTEXT_FACTORY =
        InvocationContextUtils.testFactory(ProxyInterface.class);

    @Nested
    class Constructor {
        @Test
        @DisplayName("should throw when converter collection argument is null")
        void test1() {
            assertThrows(
                IllegalArgumentException.class, 
                () -> new RootConverter(
                    (Collection<Converter<?>>)null
                )  
            );
        }

        @Test
        @DisplayName("should throw when converter varargs argument is null")
        void test2() {
            assertThrows(
                IllegalArgumentException.class, 
                () -> new RootConverter(
                    (Converter[])null
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
        void test1() {
            Converter<?> intConverter = new IntegerConverter();
            Converter<?> booleanConverter = new BooleanConverter();
            Converter<?> shortConverter = new ShortConverter();
            RootConverter rootConverter = rootConverter(
                intConverter,
                booleanConverter,
                shortConverter
            );

            assertTrue(rootConverter.canConvertTo(int.class));
            assertTrue(rootConverter.canConvertTo(boolean.class));
            assertTrue(rootConverter.canConvertTo(short.class));
            // Sanity check.
            assertFalse(rootConverter.canConvertTo(List.class));
        }

        @Test
        @DisplayName(
            "should return false when configured converters " + 
            "does not support conversion to the target type"
        )
        void test2() {
            Converter<?> converter = new BooleanConverter();
            RootConverter rootConverter = rootConverter(converter);

            assertFalse(rootConverter.canConvertTo(List.class));
            assertFalse(rootConverter.canConvertTo(Properties.class));
            assertFalse(rootConverter.canConvertTo(String[].class));
            // Sanity check.
            assertTrue(rootConverter.canConvertTo(Boolean.class));
        }
    }

    @Nested
    class ConvertMethod {
        // @Test
        // @DisplayName("should throw when proxy method argument is null")
        // void test1() {
        //     Converter<?> converter = new PrimitiveConverter();
        //     RootConverter rootConverter = rootConverter(converter);

        //     assertThrows(
        //         IllegalArgumentException.class, 
        //         () -> rootConverter.convert(null, "valueToConvert", Integer.class)
        //     );
        // }

        // @Test
        // @DisplayName("should throw when value to convert argument is null")
        // void test2() {
        //     Converter<?> converter = new PrimitiveConverter();
        //     RootConverter rootConverter = rootConverter(converter);

        //     InvocationContext context = INVOCATION_CONTEXT_FACTORY.fromMethodReference(
        //         ProxyInterface::intProperty,
        //         externalizedProperties(converter)
        //     );

        //     assertThrows(
        //         IllegalArgumentException.class, 
        //         () -> rootConverter.convert(context, null, Integer.class)
        //     );
        // }

        // @Test
        // @DisplayName("should throw when value target type argument is null")
        // void test3() {
        //     Converter<?> converter = new PrimitiveConverter();
        //     RootConverter rootConverter = rootConverter(converter);

        //     InvocationContext context = INVOCATION_CONTEXT_FACTORY.fromMethodReference(
        //         ProxyInterface::intProperty,
        //         externalizedProperties(converter)
        //     );

        //     assertThrows(
        //         IllegalArgumentException.class, 
        //         () -> rootConverter.convert(context, "valueToConvert", null)
        //     );
        // }

        @Test
        @DisplayName("should correctly convert to target type")
        void test1() {
            Converter<?> converter = new IntegerConverter();
            RootConverter rootConverter = rootConverter(converter);

            InvocationContext context = INVOCATION_CONTEXT_FACTORY.fromMethodReference(
                ProxyInterface::intProperty,
                externalizedProperties(converter)
            );

            ConversionResult<?> result = rootConverter.convert(
                context,
                "1"
            );

            Object convertedValue = result.value();
            assertNotNull(convertedValue);
            assertTrue(convertedValue instanceof Integer);
            assertEquals(1, convertedValue);
        }

        @Test
        @DisplayName("should have out-of-the-box support for Optional")
        void test2() {
            // No converters registered.
            RootConverter rootConverter = rootConverter();

            InvocationContext context = INVOCATION_CONTEXT_FACTORY.fromMethodReference(
                ProxyInterface::optionalProperty,
                externalizedProperties()
            );

            String valueToConvert = "value";
            ConversionResult<?> result = rootConverter.convert(
                context,
                valueToConvert
            );

            Object convertedValue = result.value();
            assertNotNull(convertedValue);
            assertTrue(convertedValue instanceof Optional<?>);
            assertEquals(Optional.of(valueToConvert), convertedValue);
        }

        @Test
        @DisplayName(
            "should throw when there is no converter can convert to target type."
        )
        void tes3() {
            Converter<?> converter = new IntegerConverter();
            RootConverter rootConverter = rootConverter(converter);

            InvocationContext context = INVOCATION_CONTEXT_FACTORY.fromMethodReference(
                ProxyInterface::noRegisteredConverter,
                externalizedProperties(converter)
            );

            // No converter registered to convert to List.
            assertThrows(
                ConversionException.class, 
                () -> rootConverter.convert(context, "1,2,3")
            );
        }

        @Test
        @DisplayName("should wrap and re-throw when converter has thrown an exception")
        void test4() {
            // Handler that can convert anything but always throws.
            Converter<?> throwingConverter = new Converter<Object>() {
                @Override
                public boolean canConvertTo(Class<?> targetType) {
                    return true;
                }

                @Override
                public ConversionResult<Object> convert(
                        InvocationContext context,
                        String valueToConvert,
                        Type targetType
                ) {
                    throw new RuntimeException("Mr. Stark, I don't feel so good.");
                }
            };
            
            RootConverter converter = rootConverter(throwingConverter);

            InvocationContext context = INVOCATION_CONTEXT_FACTORY.fromMethodReference(
                ProxyInterface::intProperty,
                externalizedProperties(throwingConverter)
            );

            assertThrows(
                ConversionException.class, 
                () -> converter.convert(context, "1")
            );
        }

        @Test
        @DisplayName("should skip to next converter when skip result is returned")
        void test5() throws InterruptedException {
            Converter<?> converter1 = new Converter<Object>() {
                @Override
                public boolean canConvertTo(Class<?> targetType) {
                    return true;
                }

                @Override
                public ConversionResult<Object> convert(
                        InvocationContext context,
                        String valueToConvert,
                        Type targetType
                ) {
                    // Skipped.
                    return ConversionResult.skip();
                }
            };

            CountDownLatch converter2Latch = new CountDownLatch(1);

            Converter<?> converter2 = new Converter<Object>() {
                @Override
                public boolean canConvertTo(Class<?> targetType) {
                    return true;
                }

                @Override
                public ConversionResult<Object> convert(
                        InvocationContext context,
                        String valueToConvert,
                        Type targetType
                ) {
                    converter2Latch.countDown();
                    return ConversionResult.of(Integer.parseInt(valueToConvert));
                }
            };

            RootConverter rootConverter = rootConverter(
                converter1,
                converter2
            );

            InvocationContext context = INVOCATION_CONTEXT_FACTORY.fromMethodReference(
                ProxyInterface::intProperty,
                externalizedProperties(converter1, converter2)
            );

            ConversionResult<?> result = rootConverter.convert(
                context,
                "1"
            );

            Object convertedValue = result.value();
            assertTrue(converter2Latch.await(1, TimeUnit.MINUTES));
            assertNotNull(convertedValue);
            assertTrue(convertedValue instanceof Integer);
            assertEquals(1, convertedValue);
        }
    }

    private static RootConverter rootConverter(Converter<?>... converters) {
        return new RootConverter(converters);
    }

    private static ExternalizedProperties externalizedProperties(
            Converter<?>... converters
    ) {
        return ExternalizedProperties.builder()
            .converters(converters)
            .build();
    }

    private static interface ProxyInterface {
        @ExternalizedProperty("property.int")
        int intProperty();

        @ExternalizedProperty("property.optional")
        Optional<String> optionalProperty();

        @ExternalizedProperty("no.registered.converter")
        List<String> noRegisteredConverter();
    }
}
