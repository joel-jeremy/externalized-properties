package io.github.joeljeremy7.externalizedproperties.core.variableexpansion;

import io.github.joeljeremy7.externalizedproperties.core.ExternalizedProperties;
import io.github.joeljeremy7.externalizedproperties.core.ExternalizedProperty;
import io.github.joeljeremy7.externalizedproperties.core.proxy.ProxyMethod;
import io.github.joeljeremy7.externalizedproperties.core.testfixtures.TestProxyMethodFactory;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class SimpleVariableExpanderTests {
    private static final ExternalizedProperties EXTERNALIZED_PROPERTIES = 
        ExternalizedProperties.builder().enableDefaultResolvers().build();

    private static final TestProxyMethodFactory<ProxyInterface> PROXY_METHOD_FACTORY =
        new TestProxyMethodFactory<>(ProxyInterface.class);

    @Nested
    class Constructor {
        @Test
        @DisplayName("should throw when variable prefix argument is null")
        void test2() {
            assertThrows(
                IllegalArgumentException.class, 
                () -> new SimpleVariableExpander(
                    null,
                    "}"
                )
            );
        }

        @Test
        @DisplayName("should throw when variable prefix argument is empty")
        void test3() {
            assertThrows(
                IllegalArgumentException.class, 
                () -> new SimpleVariableExpander(
                    "",
                    "}"
                )
            );
        }

        @Test
        @DisplayName("should throw when variable suffix argument is null")
        void test4() {
            assertThrows(
                IllegalArgumentException.class, 
                () -> new SimpleVariableExpander(
                    "${",
                    null
                )
            );
        }

        @Test
        @DisplayName("should throw when variable suffix argument is empty")
        void test5() {
            assertThrows(
                IllegalArgumentException.class, 
                () -> new SimpleVariableExpander(
                    "${",
                    ""
                )
            );
        }
    }

    @Nested
    class ExpandVariablesMethod {
        @Test
        @DisplayName("should return value when value is null or empty")
        void test1() {
            SimpleVariableExpander variableExpander = variableExpander();

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
        @DisplayName("should expand variable with value from resolver")
        void test2() {
            SimpleVariableExpander variableExpander = variableExpander();

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
        @DisplayName("should expand multiple variables with values from resolvers")
        void test3() {
            SimpleVariableExpander variableExpander = variableExpander();

            ProxyMethod proxyMethod = PROXY_METHOD_FACTORY.fromMethodReference(
                ProxyInterface::propertyMultipleVariables,
                EXTERNALIZED_PROPERTIES
            );

            String result = variableExpander.expandVariables(
                proxyMethod,
                "property-${java.version}-home-${java.home}"
            );

            ResolverProxy resolverProxy = 
                EXTERNALIZED_PROPERTIES.initialize(ResolverProxy.class);
            
            String javaVersionProperty = resolverProxy.resolve("java.version");
            String javaHomeProperty = resolverProxy.resolve("java.home");

            assertEquals(
                "property-" + javaVersionProperty + "-home-" + javaHomeProperty, 
                result
            );
        }

        @Test
        @DisplayName("should return original string when there are no variables")
        void test4() {
            SimpleVariableExpander variableExpander = variableExpander();
            
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
        void test5() {
            SimpleVariableExpander variableExpander = variableExpander();

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
            "should skip expansion when there is no variable name between " +
            "variable prefix and variable suffix"
        )
        void test6() {
            SimpleVariableExpander variableExpander = variableExpander();
            
            ProxyMethod proxyMethod = PROXY_METHOD_FACTORY.fromMethodReference(
                ProxyInterface::propertyNoVariableName,
                EXTERNALIZED_PROPERTIES
            );

            String result = variableExpander.expandVariables(
                proxyMethod, 
                "test-${}"
            );

            assertEquals("test-${}", result);
        }

        @Test
        @DisplayName(
            "should skip expansion " + 
            "when there is there is a variable prefix detected but no variable suffix"
        )
        void test7() {
            SimpleVariableExpander variableExpander = variableExpander();

            ProxyMethod proxyMethod = PROXY_METHOD_FACTORY.fromMethodReference(
                ProxyInterface::propertyNoVariableSuffix,
                EXTERNALIZED_PROPERTIES
            );

            String result = variableExpander.expandVariables(
                proxyMethod, 
                "test-${variable"
            );

            assertEquals("test-${variable", result);
        }
        
        @Test
        @DisplayName(
            "should expand variable with value from resolver using custom prefix and suffix"
        )
        void test8() {
            SimpleVariableExpander variableExpander = variableExpander(
                "#[",
                "]"
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

    private static SimpleVariableExpander variableExpander() {
        return new SimpleVariableExpander();
    }

    private static SimpleVariableExpander variableExpander(
            String variablePrefix,
            String variableSuffix
    ) {
        return new SimpleVariableExpander(variablePrefix, variableSuffix);
    }

    private static interface ProxyInterface {
        @ExternalizedProperty("property-${java.version}")
        String propertyJavaVersion();

        @ExternalizedProperty("property-${java.version}-home-${java.home}")
        String propertyMultipleVariables();

        @ExternalizedProperty("property-no-variables")
        String propertyNoVariables();

        @ExternalizedProperty("property-${nonexistent}")
        String propertyNonExistent();

        @ExternalizedProperty("property-#[java.version]")
        String customPrefixSuffix();

        @ExternalizedProperty("test-${}")
        String propertyNoVariableName();

        @ExternalizedProperty("test-${variable")
        String propertyNoVariableSuffix();
    }

    private static interface ResolverProxy {
        @ExternalizedProperty
        String resolve(String propertyName);
    }
}
