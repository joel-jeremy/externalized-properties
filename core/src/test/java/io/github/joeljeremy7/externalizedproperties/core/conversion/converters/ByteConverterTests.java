package io.github.joeljeremy7.externalizedproperties.core.conversion.converters;

import io.github.joeljeremy7.externalizedproperties.core.ConversionResult;
import io.github.joeljeremy7.externalizedproperties.core.ExternalizedProperties;
import io.github.joeljeremy7.externalizedproperties.core.ExternalizedProperty;
import io.github.joeljeremy7.externalizedproperties.core.InvocationContext;
import io.github.joeljeremy7.externalizedproperties.core.testfixtures.InvocationContextUtils;
import io.github.joeljeremy7.externalizedproperties.core.testfixtures.InvocationContextUtils.InvocationContextTestFactory;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ByteConverterTests {
    private static final InvocationContextTestFactory<ProxyInterface> INVOCATION_CONTEXT_FACTORY =
        InvocationContextUtils.testFactory(ProxyInterface.class);

    @Nested
    class CanConvertToMethod {
        @Test
        @DisplayName("should return true when target type is a Byte.")
        void test1() {
            ByteConverter converter = converterToTest();
            boolean canConvert = converter.canConvertTo(Byte.class);
            assertTrue(canConvert);
        }

        @Test
        @DisplayName("should return true when target type is a primitive byte.")
        void test2() {
            ByteConverter converter = converterToTest();
            boolean canConvert = converter.canConvertTo(Byte.TYPE);
            assertTrue(canConvert);
        }
    }

    @Nested
    class ConvertMethod {
        @Test
        @DisplayName("should convert value to a Byte.")
        void test1() {
            ByteConverter converter = converterToTest();

            InvocationContext context = INVOCATION_CONTEXT_FACTORY.fromMethodReference(
                ProxyInterface::byteWrapperProperty, // This method returns a Byte wrapper class
                externalizedProperties(converter)
            );

            ConversionResult<?> result = converter.convert(
                context,
                "1"
            );
            
            assertNotNull(result);
            Object wrapperValue = result.value();
            assertNotNull(wrapperValue);
            assertTrue(wrapperValue instanceof Byte);
            assertEquals((byte)1, (Byte)wrapperValue);
        }

        @Test
        @DisplayName("should convert value to a primitive byte.")
        void test2() {
            ByteConverter converter = converterToTest();
            InvocationContext context = INVOCATION_CONTEXT_FACTORY.fromMethodReference(
                ProxyInterface::bytePrimitiveProperty, // This method returns an byte primitive
                externalizedProperties(converter)
            );

            ConversionResult<?> result = converter.convert(
                context,
                "2"
            );
            
            assertNotNull(result);
            Object primitiveValue = result.value();
            assertNotNull(primitiveValue);
            assertTrue(primitiveValue instanceof Byte);
            assertEquals((byte)2, (byte)primitiveValue);
        }

        @Test
        @DisplayName("should throw when value is not a valid Byte/byte.")
        void test3() {
            ByteConverter converter = converterToTest();
            InvocationContext context = INVOCATION_CONTEXT_FACTORY.fromMethodReference(
                ProxyInterface::bytePrimitiveProperty, // This method returns an byte primitive
                externalizedProperties(converter)
            );

            assertThrows(
                NumberFormatException.class, 
                () -> converter.convert(
                    context,
                    "invalid_value"
                )
            );
        }
    }

    private static ByteConverter converterToTest() {
        return new ByteConverter();
    }

    private static ExternalizedProperties externalizedProperties(
            ByteConverter converterToTest
    ) {
        return ExternalizedProperties.builder()
            .converters(converterToTest)
            .build();
    }

    private static interface ProxyInterface {
        @ExternalizedProperty("property.byte.primitive")
        byte bytePrimitiveProperty();
    
        @ExternalizedProperty("property.byte.wrapper")
        Byte byteWrapperProperty();
    }
}
