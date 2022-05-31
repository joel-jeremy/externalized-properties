package io.github.joeljeremy7.externalizedproperties.core.variableexpansion;

import io.github.joeljeremy7.externalizedproperties.core.ExternalizedProperties;
import io.github.joeljeremy7.externalizedproperties.core.ExternalizedProperty;
import io.github.joeljeremy7.externalizedproperties.core.proxy.ProxyMethod;
import io.github.joeljeremy7.externalizedproperties.core.testfixtures.TestProxyMethodFactory;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertSame;

public class NoOpVariableExpanderTests {
    private static final ExternalizedProperties EXTERNALIZED_PROPERTIES =
        ExternalizedProperties.builder().defaults().build();
    private static final TestProxyMethodFactory<ProxyInterface> PROXY_METHOD_FACTORY =
        new TestProxyMethodFactory<>(ProxyInterface.class);
    
    @Nested
    class ExpandVariablesMethod {
        @Test
        @DisplayName("should just return the input value")
        void test1() {
            String value = "${test}";
            
            ProxyMethod proxyMethod = PROXY_METHOD_FACTORY.fromMethodReference(
                ProxyInterface::variableProperty,
                EXTERNALIZED_PROPERTIES
            );
            
            String result = NoOpVariableExpander.INSTANCE.expandVariables(proxyMethod, value);

            assertSame(value, result);
        }
    }

    private static interface ProxyInterface {
        @ExternalizedProperty("${test}")
        String variableProperty();
    }
}
