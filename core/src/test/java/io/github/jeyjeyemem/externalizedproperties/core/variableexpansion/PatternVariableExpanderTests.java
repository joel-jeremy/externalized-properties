package io.github.jeyjeyemem.externalizedproperties.core.variableexpansion;

import io.github.jeyjeyemem.externalizedproperties.core.ExternalizedProperties;
import io.github.jeyjeyemem.externalizedproperties.core.VariableExpanderProvider;
import io.github.jeyjeyemem.externalizedproperties.core.proxy.ProxyMethod;
import io.github.jeyjeyemem.externalizedproperties.core.testentities.proxy.BasicProxyInterface;
import io.github.jeyjeyemem.externalizedproperties.core.testentities.proxy.NoPropertyNameProxyInterface;
import io.github.jeyjeyemem.externalizedproperties.core.testfixtures.ProxyMethodUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class PatternVariableExpanderTests {
    // Variable pattern: #[variable]
    private static final Pattern CUSTOM_VARIABLE_PATTERN = Pattern.compile("#\\[(.+?)\\]");

    static ExternalizedProperties EXTERNALIZED_PROPERTIES = 
        ExternalizedProperties.builder().withDefaults().build();
    
    static final ProxyMethod STUB_PROXY_METHOD = ProxyMethodUtils.fromMethod(
        BasicProxyInterface.class, 
        "property"
    );

    @Nested
    class Constructor {
        @Test
        @DisplayName("should throw when externalized properties argument is null")
        public void test1() {
            assertThrows(
                IllegalArgumentException.class,
                () -> new PatternVariableExpander(null)
            );
        }

        @Test
        @DisplayName("should throw when variable pattern argument is null")
        public void test2() {
            assertThrows(
                IllegalArgumentException.class,
                () -> new PatternVariableExpander(
                    EXTERNALIZED_PROPERTIES,
                    null
                )
            );
        }
    }

    @Nested
    class ProviderMethod {
        @Test
        @DisplayName("should not return null.")
        public void test1() {
            VariableExpanderProvider<PatternVariableExpander> provider = 
                PatternVariableExpander.provider();

            assertNotNull(provider);
        }

        @Test
        @DisplayName("should return an instance on get.")
        public void test2() {
            VariableExpanderProvider<PatternVariableExpander> provider = 
                PatternVariableExpander.provider();

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
    class ProviderMethodWithVariablePatternOverload {
        @Test
        @DisplayName("should throw when variable suffix is null.")
        public void test1() {
            assertThrows(
                IllegalArgumentException.class, 
                () -> PatternVariableExpander.provider(null)
            );
        }

        @Test
        @DisplayName("should not return null.")
        public void test2() {
            VariableExpanderProvider<PatternVariableExpander> provider = 
                PatternVariableExpander.provider(Pattern.compile("\\$\\{(.+?)\\}"));

            assertNotNull(provider);
        }

        @Test
        @DisplayName("should return an instance on get.")
        public void test3() {
            VariableExpanderProvider<PatternVariableExpander> provider = 
                PatternVariableExpander.provider(Pattern.compile("\\$\\{(.+?)\\}"));

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
            PatternVariableExpander variableExpander = variableExpander();

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
        @DisplayName("should expand variables with values from resolvers")
        public void test2() {
            PatternVariableExpander variableExpander = variableExpander();

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
        @DisplayName("should return same string when there are no variables")
        public void test3() {
            PatternVariableExpander variableExpander =  variableExpander();

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
        public void test4() {
            PatternVariableExpander variableExpander = variableExpander();

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
            "should expand variable with value from resolver using custom prefix and suffix"
        )
        public void test5() {
            PatternVariableExpander variableExpander = variableExpander(
                CUSTOM_VARIABLE_PATTERN
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

    private PatternVariableExpander variableExpander() {
        VariableExpanderProvider<PatternVariableExpander> provider = 
            PatternVariableExpander.provider();

        ExternalizedProperties externalizedProperties =
            ExternalizedProperties.builder()
                .withDefaultResolvers()
                .variableExpander(provider)
                .build();

        return provider.get(externalizedProperties);
    }

    private PatternVariableExpander variableExpander(
            Pattern variablePattern
    ) {
        VariableExpanderProvider<PatternVariableExpander> provider = 
            PatternVariableExpander.provider(variablePattern);

        ExternalizedProperties externalizedProperties =
            ExternalizedProperties.builder()
                .withDefaultResolvers()
                .variableExpander(provider)
                .build();

        return provider.get(externalizedProperties);
    }
}
