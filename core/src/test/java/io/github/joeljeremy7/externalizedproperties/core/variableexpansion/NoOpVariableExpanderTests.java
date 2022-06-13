package io.github.joeljeremy7.externalizedproperties.core.variableexpansion;

import io.github.joeljeremy7.externalizedproperties.core.ExternalizedProperties;
import io.github.joeljeremy7.externalizedproperties.core.ExternalizedProperty;
import io.github.joeljeremy7.externalizedproperties.core.InvocationContext;
import io.github.joeljeremy7.externalizedproperties.core.testfixtures.InvocationContextUtils;
import io.github.joeljeremy7.externalizedproperties.core.testfixtures.InvocationContextUtils.InvocationContextTestFactory;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertSame;

public class NoOpVariableExpanderTests {
    private static final ExternalizedProperties EXTERNALIZED_PROPERTIES =
        ExternalizedProperties.builder().defaults().build();
    private static final InvocationContextTestFactory<ProxyInterface> INVOCATION_CONTEXT_FACTORY =
        InvocationContextUtils.testFactory(ProxyInterface.class);
    
    @Nested
    class ExpandVariablesMethod {
        @Test
        @DisplayName("should just return the input value")
        void test1() {
            String value = "${test}";
            
            InvocationContext context = INVOCATION_CONTEXT_FACTORY.fromMethodReference(
                ProxyInterface::variableProperty,
                EXTERNALIZED_PROPERTIES
            );
            
            String result = NoOpVariableExpander.INSTANCE.expandVariables(context, value);

            assertSame(value, result);
        }
    }

    private static interface ProxyInterface {
        @ExternalizedProperty("${test}")
        String variableProperty();
    }
}
