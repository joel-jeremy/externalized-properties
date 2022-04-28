package io.github.jeyjeyemem.externalizedproperties.core.variableexpansion;

import io.github.jeyjeyemem.externalizedproperties.core.ExternalizedProperties;
import io.github.jeyjeyemem.externalizedproperties.core.VariableExpanderProvider;
import io.github.jeyjeyemem.externalizedproperties.core.proxy.ProxyMethod;
import io.github.jeyjeyemem.externalizedproperties.core.testentities.proxy.NoPropertyNameProxyInterface;
import io.github.jeyjeyemem.externalizedproperties.core.testentities.proxy.VariableProxyInterface;
import io.github.jeyjeyemem.externalizedproperties.core.testfixtures.ProxyMethodUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class SimpleVariableExpanderTests {

    static ExternalizedProperties EXTERNALIZED_PROPERTIES = 
        ExternalizedProperties.builder().withDefaultResolvers().build();

    static final ProxyMethod STUB_PROXY_METHOD = ProxyMethodUtils.fromMethod(
        VariableProxyInterface.class, 
        "variableProperty"
    );

    @Nested
    class Constructor {
        @Test
        @DisplayName("should throw when externalized properties argument is null")
        public void test1() {
            assertThrows(
                IllegalArgumentException.class, 
                () -> new SimpleVariableExpander(
                    null,
                    "${",
                    "}"
                )
            );
        }

        @Test
        @DisplayName("should throw when variable prefix argument is null")
        public void test2() {
            assertThrows(
                IllegalArgumentException.class, 
                () -> new SimpleVariableExpander(
                    EXTERNALIZED_PROPERTIES,
                    null,
                    "}"
                )
            );
        }

        @Test
        @DisplayName("should throw when variable prefix argument is empty")
        public void test3() {
            assertThrows(
                IllegalArgumentException.class, 
                () -> new SimpleVariableExpander(
                    EXTERNALIZED_PROPERTIES,
                    "",
                    "}"
                )
            );
        }

        @Test
        @DisplayName("should throw when variable suffix argument is null")
        public void test4() {
            assertThrows(
                IllegalArgumentException.class, 
                () -> new SimpleVariableExpander(
                    EXTERNALIZED_PROPERTIES,
                    "${",
                    null
                )
            );
        }

        @Test
        @DisplayName("should throw when variable suffix argument is empty")
        public void test5() {
            assertThrows(
                IllegalArgumentException.class, 
                () -> new SimpleVariableExpander(
                    EXTERNALIZED_PROPERTIES,
                    "${",
                    ""
                )
            );
        }
    }

    @Nested
    class ProviderMethod {
        @Test
        @DisplayName("should not return null.")
        public void test1() {
            VariableExpanderProvider<SimpleVariableExpander> provider = 
                SimpleVariableExpander.provider();

            assertNotNull(provider);
        }

        @Test
        @DisplayName("should return an instance on get.")
        public void test2() {
            VariableExpanderProvider<SimpleVariableExpander> provider = 
                SimpleVariableExpander.provider();

            assertNotNull(
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
    class ProviderMethodWithVariablePrefixAndSuffixOverload {
        @Test
        @DisplayName("should throw when variable prefix is null or empty.")
        public void test1() {
            assertThrows(
                IllegalArgumentException.class, 
                () -> SimpleVariableExpander.provider(null, "}")
            );
            assertThrows(
                IllegalArgumentException.class, 
                () -> SimpleVariableExpander.provider("", "}")
            );
        }

        @Test
        @DisplayName("should throw when variable suffix is null or empty.")
        public void test2() {
            assertThrows(
                IllegalArgumentException.class, 
                () -> SimpleVariableExpander.provider("${", null)
            );
            assertThrows(
                IllegalArgumentException.class, 
                () -> SimpleVariableExpander.provider("${", "")
            );
        }
        @Test
        @DisplayName("should not return null.")
        public void test3() {
            VariableExpanderProvider<SimpleVariableExpander> provider = 
                SimpleVariableExpander.provider("${", "}");

            assertNotNull(provider);
        }

        @Test
        @DisplayName("should return an instance on get.")
        public void test4() {
            VariableExpanderProvider<SimpleVariableExpander> provider = 
                SimpleVariableExpander.provider("${", "}");

            assertNotNull(
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
        @DisplayName("should return value when value is null or empty")
        public void test1() {
            SimpleVariableExpander variableExpander = variableExpander();

            String nullResult = variableExpander.expandVariables(
                STUB_PROXY_METHOD, 
                null
            );
            String emptyResult = variableExpander.expandVariables(
                STUB_PROXY_METHOD, 
                ""
            );

            assertNull(nullResult);
            assertEquals("", emptyResult);
        }

        @Test
        @DisplayName("should expand variable with value from resolver")
        public void test2() {
            SimpleVariableExpander variableExpander = variableExpander();

            String result = variableExpander.expandVariables(
                STUB_PROXY_METHOD, 
                "property-${java.version}"
            );

            NoPropertyNameProxyInterface resolverProxy = 
                EXTERNALIZED_PROPERTIES.proxy(NoPropertyNameProxyInterface.class);
            
            String propertyValue = resolverProxy.resolve("java.version");

            assertEquals(
                "property-" + propertyValue, 
                result
            );
        }

        @Test
        @DisplayName("should expand multiple variables with values from resolvers")
        public void test3() {
            SimpleVariableExpander variableExpander = variableExpander();

            String result = variableExpander.expandVariables(
                STUB_PROXY_METHOD,
                "property-${java.version}-home-${java.home}"
            );

            NoPropertyNameProxyInterface resolverProxy = 
                EXTERNALIZED_PROPERTIES.proxy(NoPropertyNameProxyInterface.class);
            
            String javaVersionProperty = resolverProxy.resolve("java.version");
            String javaHomeProperty = resolverProxy.resolve("java.home");

            assertEquals(
                "property-" + javaVersionProperty + "-home-" + javaHomeProperty, 
                result
            );
        }

        @Test
        @DisplayName("should return original string when there are no variables")
        public void test4() {
            SimpleVariableExpander variableExpander = variableExpander();

            String result = variableExpander.expandVariables(
                STUB_PROXY_METHOD,
                "property-no-variables"
            );

            assertEquals(
                "property-no-variables", 
                result
            );
        }

        @Test
        @DisplayName("should throw when variable cannot be resolved from any resolvers")
        public void test5() {
            SimpleVariableExpander variableExpander = variableExpander();

            assertThrows(
                VariableExpansionException.class, 
                () -> variableExpander.expandVariables(
                    STUB_PROXY_METHOD,
                    "property-${nonexistent}"
                )
            );
        }

        @Test
        @DisplayName(
            "should skip expansion when there is no variable name between " +
            "variable prefix and variable suffix"
        )
        public void test6() {
            SimpleVariableExpander variableExpander = variableExpander();

            String result = variableExpander.expandVariables(
                STUB_PROXY_METHOD, 
                "test-${}"
            );

            assertEquals("test-${}", result);
        }

        @Test
        @DisplayName(
            "should skip expansion " + 
            "when there is there is a variable prefix detected but no variable suffix"
        )
        public void test7() {
            SimpleVariableExpander variableExpander = variableExpander();

            String result = variableExpander.expandVariables(
                STUB_PROXY_METHOD, 
                "test-${variable"
            );

            assertEquals("test-${variable", result);
        }
        
        @Test
        @DisplayName(
            "should expand variable with value from resolver using custom prefix and suffix"
        )
        public void test8() {
            SimpleVariableExpander variableExpander = variableExpander(
                "#[",
                "]"
            );

            String result = variableExpander.expandVariables(
                STUB_PROXY_METHOD,
                "property-#[java.version]"
            );

            NoPropertyNameProxyInterface resolverProxy = 
                EXTERNALIZED_PROPERTIES.proxy(NoPropertyNameProxyInterface.class);
            
            String propertyValue = resolverProxy.resolve("java.version");

            assertEquals(
                "property-" + propertyValue, 
                result
            );
        }
    }

    private SimpleVariableExpander variableExpander() {
        VariableExpanderProvider<SimpleVariableExpander> provider = 
            SimpleVariableExpander.provider();

        ExternalizedProperties externalizedProperties =
            ExternalizedProperties.builder()
                .withDefaultResolvers()
                .variableExpander(provider)
                .build();

        return provider.get(externalizedProperties);
    }

    private SimpleVariableExpander variableExpander(
            String variablePrefix,
            String variableSuffix
    ) {
        VariableExpanderProvider<SimpleVariableExpander> provider = 
            SimpleVariableExpander.provider(variablePrefix, variableSuffix);

        ExternalizedProperties externalizedProperties =
            ExternalizedProperties.builder()
                .withDefaultResolvers()
                .variableExpander(provider)
                .build();

        return provider.get(externalizedProperties);
    }
}
