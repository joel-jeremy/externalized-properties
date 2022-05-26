package io.github.joeljeremy7.externalizedproperties.core.variableexpansion;

import io.github.joeljeremy7.externalizedproperties.core.ExternalizedProperties;
import io.github.joeljeremy7.externalizedproperties.core.ExternalizedProperty;
import io.github.joeljeremy7.externalizedproperties.core.proxy.ProxyMethod;
import io.github.joeljeremy7.externalizedproperties.core.testfixtures.TestProxyMethodFactory;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class PatternVariableExpanderTests {
    private static final ExternalizedProperties EXTERNALIZED_PROPERTIES = 
        ExternalizedProperties.builder().defaults().build();
    private static final TestProxyMethodFactory<ProxyInterface> PROXY_METHOD_FACTORY =
        new TestProxyMethodFactory<>(ProxyInterface.class);
    // Variable pattern: #[variable]
    private static final Pattern CUSTOM_VARIABLE_PATTERN = Pattern.compile("#\\[(.+?)\\]");

    @Nested
    class Constructor {
        @Test
        @DisplayName("should throw when variable pattern argument is null")
        void test1() {
            assertThrows(
                IllegalArgumentException.class,
                () -> new PatternVariableExpander(
                    null
                )
            );
        }
    }

    @Nested
    class ExpandVariablesMethod {
        @Test
        @DisplayName("should return value when value is null or empty")
        void test1() {
            PatternVariableExpander variableExpander = variableExpander();

            ProxyMethod proxyMethod = PROXY_METHOD_FACTORY.fromMethodReference(
                ProxyInterface::propertyJavaVersion,
                EXTERNALIZED_PROPERTIES
            );

            String nullResult = variableExpander.expandVariables(
                proxyMethod, 
                null
            );
            String emptyResult = variableExpander.expandVariables(
                proxyMethod, 
                ""
            );

            assertNull(nullResult);
            assertEquals("", emptyResult);
        }

        @Test
        @DisplayName("should expand variables with values from resolvers")
        void test2() {
            PatternVariableExpander variableExpander = variableExpander();
            
            ProxyMethod proxyMethod = PROXY_METHOD_FACTORY.fromMethodReference(
                ProxyInterface::propertyJavaVersion,
                EXTERNALIZED_PROPERTIES
            );

            String result = variableExpander.expandVariables(
                proxyMethod,
                "property-${java.version}"
            );

            ResolverProxy resolverProxy = 
                EXTERNALIZED_PROPERTIES.initialize(ResolverProxy.class);
            
            String propertyValue = resolverProxy.resolve("java.version");

            assertEquals(
                "property-" + propertyValue, 
                result
            );
        }

        @Test
        @DisplayName("should return same string when there are no variables")
        void test3() {
            PatternVariableExpander variableExpander =  variableExpander();

            ProxyMethod proxyMethod = PROXY_METHOD_FACTORY.fromMethodReference(
                ProxyInterface::propertyNoVariables,
                EXTERNALIZED_PROPERTIES
            );

            String result = variableExpander.expandVariables(
                proxyMethod,
                "property-no-variables"
            );

            assertEquals(
                "property-no-variables", 
                result
            );
        }

        @Test
        @DisplayName("should throw when variable cannot be resolved from any resolvers")
        void test4() {
            PatternVariableExpander variableExpander = variableExpander();
            
            ProxyMethod proxyMethod = PROXY_METHOD_FACTORY.fromMethodReference(
                ProxyInterface::propertyNonExistent,
                EXTERNALIZED_PROPERTIES
            );

            assertThrows(
                VariableExpansionException.class, 
                () -> variableExpander.expandVariables(
                    proxyMethod, 
                    "property-${nonexistent}"
                )
            );
        }
        
        @Test
        @DisplayName(
            "should expand variable with value from resolver using custom prefix and suffix"
        )
        void test5() {
            PatternVariableExpander variableExpander = variableExpander(
                CUSTOM_VARIABLE_PATTERN
            );

            ProxyMethod proxyMethod = PROXY_METHOD_FACTORY.fromMethodReference(
                ProxyInterface::customPrefixSuffix,
                EXTERNALIZED_PROPERTIES
            );

            String result = variableExpander.expandVariables(
                proxyMethod,
                "property-#[java.version]"
            );

            ResolverProxy resolverProxy = 
                EXTERNALIZED_PROPERTIES.initialize(ResolverProxy.class);
            
            String propertyValue = resolverProxy.resolve("java.version");

            assertEquals(
                "property-" + propertyValue, 
                result
            );
        }
    }

    private static PatternVariableExpander variableExpander() {
        return new PatternVariableExpander();
    }

    private static PatternVariableExpander variableExpander(
            Pattern variablePattern
    ) {
        return new PatternVariableExpander(variablePattern);
    }

    private static interface ProxyInterface {
        @ExternalizedProperty("property-${java.version}")
        String propertyJavaVersion();

        @ExternalizedProperty("property-no-variables")
        String propertyNoVariables();

        @ExternalizedProperty("property-${nonexistent}")
        String propertyNonExistent();

        @ExternalizedProperty("property-#[java.version]")
        String customPrefixSuffix();
    }

    private static interface ResolverProxy {
        @ExternalizedProperty
        String resolve(String propertyName);
    }
}
