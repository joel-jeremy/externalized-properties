package io.github.jeyjeyemem.externalizedproperties.core.variableexpansion;

import io.github.jeyjeyemem.externalizedproperties.core.ExternalizedProperties;
import io.github.jeyjeyemem.externalizedproperties.core.VariableExpanderProvider;
import io.github.jeyjeyemem.externalizedproperties.core.proxy.ProxyMethod;
import io.github.jeyjeyemem.externalizedproperties.core.testfixtures.ProxyMethodUtils;
import io.github.jeyjeyemem.externalizedproperties.core.testentities.proxy.BasicProxyInterface;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;

public class NoOpVariableExpanderTests {
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
            
            ProxyMethod proxyMethod = 
                ProxyMethodUtils.fromMethod(BasicProxyInterface.class, "property");
            
            String result = NoOpVariableExpander.INSTANCE.expandVariables(proxyMethod, value);

            assertSame(value, result);
        }
    }
}
