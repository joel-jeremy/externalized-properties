package io.github.joeljeremy7.externalizedproperties.core.internal.conversion;

import io.github.joeljeremy7.externalizedproperties.core.ConversionResult;
import io.github.joeljeremy7.externalizedproperties.core.ConverterProvider;
import io.github.joeljeremy7.externalizedproperties.core.ExternalizedProperties;
import io.github.joeljeremy7.externalizedproperties.core.ExternalizedProperty;
import io.github.joeljeremy7.externalizedproperties.core.proxy.ProxyMethod;
import io.github.joeljeremy7.externalizedproperties.core.testfixtures.ProxyMethodFactory;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class NoOpConverterTests {
    private static final ProxyMethodFactory<ProxyInterface> PROXY_METHOD_FACTORY =
        new ProxyMethodFactory<>(ProxyInterface.class);

    @Nested
    class ProviderMethod {
        @Test
        @DisplayName("should not return null")
        void test1() {
            ConverterProvider<NoOpConverter> provider = NoOpConverter.provider();
            assertNotNull(provider);
        }

        @Test
        @DisplayName("should not return null on get")
        void test2() {
            ConverterProvider<NoOpConverter> provider = NoOpConverter.provider();
            
            ExternalizedProperties externalizedProperties = 
                ExternalizedProperties.builder()
                    .withDefaultResolvers()
                    .build();
                
            assertNotNull(
                provider.get(
                    externalizedProperties,
                    new RootConverter(externalizedProperties)
                )
            );
        }
    }

    @Nested
    class CanConvertToMethod {
        @Test
        @DisplayName("should always return false")
        public void test1() {
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
        public void test1() {
            ProxyMethod intProxyMethod = PROXY_METHOD_FACTORY.fromMethodReference(
                ProxyInterface::intProperty
            );
            ProxyMethod booleanProxyMethod = PROXY_METHOD_FACTORY.fromMethodReference(
                ProxyInterface::booleanProperty
            );
            ProxyMethod doubleProxyMethod = PROXY_METHOD_FACTORY.fromMethodReference( 
                ProxyInterface::doubleProperty
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

    public static interface ProxyInterface {
        @ExternalizedProperty("property.int")
        int intProperty();
    
        @ExternalizedProperty("property.double")
        double doubleProperty();
    
        @ExternalizedProperty("property.boolean")
        boolean booleanProperty();
    }
}
