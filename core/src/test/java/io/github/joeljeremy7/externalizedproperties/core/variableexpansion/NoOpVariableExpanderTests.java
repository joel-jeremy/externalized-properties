package io.github.joeljeremy7.externalizedproperties.core.variableexpansion;

import io.github.joeljeremy7.externalizedproperties.core.ExternalizedProperties;
import io.github.joeljeremy7.externalizedproperties.core.ExternalizedProperty;
import io.github.joeljeremy7.externalizedproperties.core.VariableExpanderProvider;
import io.github.joeljeremy7.externalizedproperties.core.proxy.ProxyMethod;
import io.github.joeljeremy7.externalizedproperties.core.testfixtures.ProxyMethodFactory;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;

public class NoOpVariableExpanderTests {
    private static final ProxyMethodFactory<ProxyInterface> PROXY_METHOD_FACTORY =
        new ProxyMethodFactory<>(ProxyInterface.class);
    
    @Nested
    class ProviderMethod {
        @Test
        @DisplayName("should not return null.")
        public void test1() {
            VariableExpanderProvider<NoOpVariableExpander> provider = 
                NoOpVariableExpander.provider();

            assertNotNull(provider);
        }

        @Test
        @DisplayName("should return an instance on get.")
        public void test2() {
            VariableExpanderProvider<NoOpVariableExpander> provider = 
                NoOpVariableExpander.provider();

            assertEquals(
                NoOpVariableExpander.INSTANCE,
                provider.get(
                    ExternalizedProperties.builder()
                        .withDefaultResolvers()
                        .variableExpander(provider)
                        .build()
                )
            );
        }
    }

    @Nested
    class ExpandVariablesMethod {
        @Test
        @DisplayName("should just return the input value")
        public void test1() {
            String value = "${test}";
            
            ProxyMethod proxyMethod = PROXY_METHOD_FACTORY.fromMethodReference(
                ProxyInterface::variableProperty
            );
            
            String result = NoOpVariableExpander.INSTANCE.expandVariables(proxyMethod, value);

            assertSame(value, result);
        }
    }

    public static interface ProxyInterface {
        @ExternalizedProperty("${test}")
        String variableProperty();
    }
}
