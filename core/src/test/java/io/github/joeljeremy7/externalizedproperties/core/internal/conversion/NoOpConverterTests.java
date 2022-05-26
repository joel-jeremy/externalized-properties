package io.github.joeljeremy7.externalizedproperties.core.internal.conversion;

import io.github.joeljeremy7.externalizedproperties.core.ConversionResult;
import io.github.joeljeremy7.externalizedproperties.core.Converter;
import io.github.joeljeremy7.externalizedproperties.core.ExternalizedProperties;
import io.github.joeljeremy7.externalizedproperties.core.ExternalizedProperty;
import io.github.joeljeremy7.externalizedproperties.core.proxy.ProxyMethod;
import io.github.joeljeremy7.externalizedproperties.core.testfixtures.TestProxyMethodFactory;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

public class NoOpConverterTests {
    private static final TestProxyMethodFactory<ProxyInterface> PROXY_METHOD_FACTORY =
        new TestProxyMethodFactory<>(ProxyInterface.class);

    @Nested
    class CanConvertToMethod {
        @Test
        @DisplayName("should always return false")
        void test1() {
            assertFalse(
                NoOpConverter.INSTANCE.canConvertTo(int.class)
            );
            assertFalse(
                NoOpConverter.INSTANCE.canConvertTo(List.class)
            );
            assertFalse(
                NoOpConverter.INSTANCE.canConvertTo(Set.class)
            );
            assertFalse(
                NoOpConverter.INSTANCE.canConvertTo(Optional.class)
            );
        }
    }

    @Nested
    class ConvertMethod {
        @Test
        @DisplayName("should always return skip result")
        void test1() {
            ProxyMethod intProxyMethod = PROXY_METHOD_FACTORY.fromMethodReference(
                ProxyInterface::intProperty,
                externalizedProperties()
            );
            ProxyMethod booleanProxyMethod = PROXY_METHOD_FACTORY.fromMethodReference(
                ProxyInterface::booleanProperty,
                externalizedProperties()
            );
            ProxyMethod doubleProxyMethod = PROXY_METHOD_FACTORY.fromMethodReference( 
                ProxyInterface::doubleProperty,
                externalizedProperties()
            );
            
            ConversionResult<?> intResult = 
                NoOpConverter.INSTANCE.convert(
                    intProxyMethod, 
                    "1", 
                    int.class
                );
            
            ConversionResult<?> booleanResult = 
                NoOpConverter.INSTANCE.convert(
                    booleanProxyMethod, 
                    "true", 
                    boolean.class
                );
            
            ConversionResult<?> doubleResult = 
                NoOpConverter.INSTANCE.convert(
                    doubleProxyMethod, 
                    "1.0", 
                    double.class
                );

            ConversionResult<?> skipResult = ConversionResult.skip();

            assertEquals(skipResult, intResult);
            assertEquals(skipResult, booleanResult);
            assertEquals(skipResult, doubleResult);
        }
    }

    private static ExternalizedProperties externalizedProperties(Converter<?>... converters) {
        return ExternalizedProperties.builder()
            .enableDefaultResolvers()
            .converters(converters)
            .build();
    }

    private static interface ProxyInterface {
        @ExternalizedProperty("property.int")
        int intProperty();
    
        @ExternalizedProperty("property.double")
        double doubleProperty();
    
        @ExternalizedProperty("property.boolean")
        boolean booleanProperty();
    }
}
