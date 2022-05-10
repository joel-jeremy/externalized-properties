package io.github.joeljeremy7.externalizedproperties.core.conversion.converters;

import io.github.joeljeremy7.externalizedproperties.core.ConversionResult;
import io.github.joeljeremy7.externalizedproperties.core.ConverterProvider;
import io.github.joeljeremy7.externalizedproperties.core.ExternalizedProperties;
import io.github.joeljeremy7.externalizedproperties.core.ExternalizedProperty;
import io.github.joeljeremy7.externalizedproperties.core.conversion.ConversionException;
import io.github.joeljeremy7.externalizedproperties.core.internal.conversion.RootConverter;
import io.github.joeljeremy7.externalizedproperties.core.proxy.ProxyMethod;
import io.github.joeljeremy7.externalizedproperties.core.testfixtures.ProxyMethodFactory;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class EnumConverterTests {
    private static final ProxyMethodFactory<ProxyInterface> PROXY_METHOD_FACTORY =
        new ProxyMethodFactory<>(ProxyInterface.class);
    
    @Nested
    class ProviderMethod {
        @Test
        @DisplayName("should not return null.")
        void test1() {
            ConverterProvider<EnumConverter> provider = 
                EnumConverter.provider();

            assertNotNull(provider);
        }

        @Test
        @DisplayName("should return an instance on get.")
        void test2() {
            ConverterProvider<EnumConverter> provider = 
                EnumConverter.provider();
            
            ExternalizedProperties externalizedProperties = 
                ExternalizedProperties.builder()
                    .withDefaultResolvers()
                    .converters(provider)
                    .build();
            
            assertNotNull(
                provider.get(
                    externalizedProperties,
                    new RootConverter(externalizedProperties, provider)
                )
            );
        }
    }

    @Nested
    class CanConvertToMethod {
        @Test
        @DisplayName("should return false when target type is null.")
        void test1() {
            EnumConverter converter = converterToTest();
            boolean canConvert = converter.canConvertTo(null);
            assertFalse(canConvert);
        }

        @Test
        @DisplayName(
            "should return true when target type is an enum."
        )
        void test2() {
            EnumConverter converter = converterToTest();
            boolean canConvert = converter.canConvertTo(TestEnum.class);
            assertTrue(canConvert);
        }

        @Test
        @DisplayName(
            "should return false when target type is not an enum."
        )
        void test3() {
            EnumConverter converter = converterToTest();
            boolean canConvert = converter.canConvertTo(String.class);
            assertFalse(canConvert);
        }
    }

    @Nested
    class ConvertMethod {
        @Test
        @DisplayName("should convert resolved property to enum.")
        void test1() {
            EnumConverter converter = converterToTest();

            ProxyMethod proxyMethod = PROXY_METHOD_FACTORY.fromMethodReference(
                ProxyInterface::enumProperty
            );

            ConversionResult<? extends Enum<?>> result = converter.convert(
                proxyMethod,
                TestEnum.ONE.name()
            );
            assertNotNull(result);

            Enum<?> testEnum = result.value();
            assertEquals(TestEnum.ONE, testEnum);
        }

        @Test
        @DisplayName("should throw when property value is not a valid enum value.")
        void test2() {
            EnumConverter converter = converterToTest();

            ProxyMethod proxyMethod = PROXY_METHOD_FACTORY.fromMethodReference(
                ProxyInterface::enumProperty
            );

            assertThrows(ConversionException.class, () -> {
                converter.convert(
                    proxyMethod,
                    "INVALID_ENUM_VALUE"
                );
            });
        }

        @Test
        @DisplayName("should return skipped result when target type is not an enum.")
        void test3() {
            EnumConverter converter = converterToTest();

            ProxyMethod proxyMethod = PROXY_METHOD_FACTORY.fromMethodReference(
                ProxyInterface::notSupportedNotAnEnum
            );
            
            ConversionResult<?> result = converter.convert(
                proxyMethod,
                "1"
            );
            assertEquals(ConversionResult.skip(), result);
        }
    }

    private EnumConverter converterToTest() {
        return new EnumConverter();
    }

    public static interface ProxyInterface {
        @ExternalizedProperty("property.enum")
        TestEnum enumProperty();

        @ExternalizedProperty("property.not.supported")
        int notSupportedNotAnEnum();
    }

    static enum TestEnum {
        NONE,
        ONE,
        TWO,
        THREE
    }
}
