package io.github.joeljeremy7.externalizedproperties.core.conversion.converters;

import io.github.joeljeremy7.externalizedproperties.core.ConversionResult;
import io.github.joeljeremy7.externalizedproperties.core.ExternalizedProperties;
import io.github.joeljeremy7.externalizedproperties.core.ExternalizedProperty;
import io.github.joeljeremy7.externalizedproperties.core.InvocationContext;
import io.github.joeljeremy7.externalizedproperties.core.conversion.ConversionException;
import io.github.joeljeremy7.externalizedproperties.core.testfixtures.InvocationContextUtils;
import io.github.joeljeremy7.externalizedproperties.core.testfixtures.InvocationContextUtils.InvocationContextTestFactory;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class DurationConverterTests {
    private static final InvocationContextTestFactory<ProxyInterface> INVOCATION_CONTEXT_FACTORY =
        InvocationContextUtils.testFactory(ProxyInterface.class);

    @Nested
    class CanConvertToMethod {
        @Test
        @DisplayName("should return true when target type is a Duration")
        void test1() {
            DurationConverter converter = converterToTest();
            boolean canConvert = converter.canConvertTo(Duration.class);
            assertTrue(canConvert);
        }

        @Test
        @DisplayName("should return false when target type is not a Duration")
        void test2() {
            DurationConverter converter = converterToTest();
            boolean canConvert = converter.canConvertTo(Integer.class);
            assertFalse(canConvert);
        }
    }

    @Nested
    class ConvertMethod {
        @Test
        @DisplayName("should convert number value to a Duration (in milliseconds)")
        void test1() {
            DurationConverter converter = converterToTest();

            InvocationContext context = INVOCATION_CONTEXT_FACTORY.fromMethodReference(
                ProxyInterface::durationProperty,
                externalizedProperties(converter)
            );

            ConversionResult<Duration> result = converter.convert(
                context,
                "60"
            );
            
            assertNotNull(result);
            assertEquals(Duration.ofMillis(60), result.value());
        }

        @Test
        @DisplayName("should convert value (in ISO 8601 format) to a Duration")
        void test2() {
            DurationConverter converter = converterToTest();

            InvocationContext context = INVOCATION_CONTEXT_FACTORY.fromMethodReference(
                ProxyInterface::durationProperty,
                externalizedProperties(converter)
            );

            // 2 days and 30 mins
            String iso8601Duration = "P2DT30M";

            ConversionResult<Duration> result = converter.convert(
                context,
                iso8601Duration
            );
            
            assertNotNull(result);
            assertEquals(Duration.parse(iso8601Duration), result.value());
        }

        @Test
        @DisplayName(
            "should convert value (in extended ISO 8601 format - starts with '+') to a Duration"
        )
        void test3() {
            DurationConverter converter = converterToTest();

            InvocationContext context = INVOCATION_CONTEXT_FACTORY.fromMethodReference(
                ProxyInterface::durationProperty,
                externalizedProperties(converter)
            );

            // 2 days and 30 mins
            String iso8601Duration = "+P2DT30M";

            ConversionResult<Duration> result = converter.convert(
                context,
                iso8601Duration
            );
            
            assertNotNull(result);
            assertEquals(Duration.parse(iso8601Duration), result.value());
        }

        @Test
        @DisplayName(
            "should convert value (in extended ISO 8601 format - starts with '-') to a Duration"
        )
        void test4() {
            DurationConverter converter = converterToTest();

            InvocationContext context = INVOCATION_CONTEXT_FACTORY.fromMethodReference(
                ProxyInterface::durationProperty,
                externalizedProperties(converter)
            );

            // 2 days and 30 mins
            String iso8601Duration = "-P2DT30M";

            ConversionResult<Duration> result = converter.convert(
                context,
                iso8601Duration
            );
            
            assertNotNull(result);
            assertEquals(Duration.parse(iso8601Duration), result.value());
        }

        @Test
        @DisplayName("should throw when value is not a valid Duration")
        void test5() {
            DurationConverter converter = converterToTest();
            InvocationContext context = INVOCATION_CONTEXT_FACTORY.fromMethodReference(
                ProxyInterface::durationProperty,
                externalizedProperties(converter)
            );

            assertThrows(
                ConversionException.class, 
                () -> converter.convert(
                    context,
                    "invalid_value"
                )
            );
        }

        @Test
        @DisplayName("should throw when value is empty")
        void test6() {
            DurationConverter converter = converterToTest();
            InvocationContext context = INVOCATION_CONTEXT_FACTORY.fromMethodReference(
                ProxyInterface::durationProperty,
                externalizedProperties(converter)
            );

            assertThrows(
                ConversionException.class, 
                () -> converter.convert(
                    context,
                    ""
                )
            );
        }

        @Test
        @DisplayName(
            "should throw when value is not a valid Duration (no duration values)"
        )
        void test7() {
            DurationConverter converter = converterToTest();
            InvocationContext context = INVOCATION_CONTEXT_FACTORY.fromMethodReference(
                ProxyInterface::durationProperty,
                externalizedProperties(converter)
            );

            assertThrows(
                ConversionException.class, 
                () -> converter.convert(
                    context,
                    "P" // No duration values
                )
            );
        }

        @Test
        @DisplayName("should throw when value is not a valid Duration (no time values)")
        void test8() {
            DurationConverter converter = converterToTest();
            InvocationContext context = INVOCATION_CONTEXT_FACTORY.fromMethodReference(
                ProxyInterface::durationProperty,
                externalizedProperties(converter)
            );

            assertThrows(
                ConversionException.class, 
                () -> converter.convert(
                    context,
                    "P2DT" // No time values
                )
            );
        }

        @Test
        @DisplayName(
            "should throw when value is not a valid Duration " +
            "(no duration values after + sign)"
        )
        void test9() {
            DurationConverter converter = converterToTest();
            InvocationContext context = INVOCATION_CONTEXT_FACTORY.fromMethodReference(
                ProxyInterface::durationProperty,
                externalizedProperties(converter)
            );

            assertThrows(
                ConversionException.class, 
                () -> converter.convert(
                    context,
                    "+" // No duration values
                )
            );
        }

        @Test
        @DisplayName(
            "should throw when value is not a valid Duration " +
            "(no duration values after - sign)"
        )
        void test10() {
            DurationConverter converter = converterToTest();
            InvocationContext context = INVOCATION_CONTEXT_FACTORY.fromMethodReference(
                ProxyInterface::durationProperty,
                externalizedProperties(converter)
            );

            assertThrows(
                ConversionException.class, 
                () -> converter.convert(
                    context,
                    "-" // No duration values
                )
            );
        }

        @Test
        @DisplayName(
            "should throw when value is not a valid Duration" +
            "(value after + sign is invalid)"
        )
        void test11() {
            DurationConverter converter = converterToTest();
            InvocationContext context = INVOCATION_CONTEXT_FACTORY.fromMethodReference(
                ProxyInterface::durationProperty,
                externalizedProperties(converter)
            );

            assertThrows(
                ConversionException.class, 
                () -> converter.convert(
                    context,
                    "+Q" // Invalid duration values
                )
            );
        }

        @Test
        @DisplayName(
            "should throw when value is not a valid Duration" +
            "(value after - sign is invalid)"
        )
        void test12() {
            DurationConverter converter = converterToTest();
            InvocationContext context = INVOCATION_CONTEXT_FACTORY.fromMethodReference(
                ProxyInterface::durationProperty,
                externalizedProperties(converter)
            );

            assertThrows(
                ConversionException.class, 
                () -> converter.convert(
                    context,
                    "-Q" // Invalid duration values
                )
            );
        }
    }

    private static DurationConverter converterToTest() {
        return new DurationConverter();
    }

    private static ExternalizedProperties externalizedProperties(
            DurationConverter converterToTest
    ) {
        return ExternalizedProperties.builder()
            .converters(converterToTest)
            .build();
    }

    private static interface ProxyInterface {
        @ExternalizedProperty("property.duration")
        Duration durationProperty();
    }
}
