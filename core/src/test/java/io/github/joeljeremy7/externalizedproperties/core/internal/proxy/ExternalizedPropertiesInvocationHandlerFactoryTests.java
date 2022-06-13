package io.github.joeljeremy7.externalizedproperties.core.internal.proxy;

import io.github.joeljeremy7.externalizedproperties.core.Converter;
import io.github.joeljeremy7.externalizedproperties.core.ExternalizedProperties;
import io.github.joeljeremy7.externalizedproperties.core.ExternalizedProperty;
import io.github.joeljeremy7.externalizedproperties.core.Resolver;
import io.github.joeljeremy7.externalizedproperties.core.VariableExpander;
import io.github.joeljeremy7.externalizedproperties.core.conversion.converters.DefaultConverter;
import io.github.joeljeremy7.externalizedproperties.core.internal.InvocationContextFactory;
import io.github.joeljeremy7.externalizedproperties.core.internal.conversion.RootConverter;
import io.github.joeljeremy7.externalizedproperties.core.internal.processing.RootProcessor;
import io.github.joeljeremy7.externalizedproperties.core.internal.resolvers.RootResolver;
import io.github.joeljeremy7.externalizedproperties.core.proxy.InvocationHandlerFactory;
import io.github.joeljeremy7.externalizedproperties.core.testfixtures.StubResolver;
import io.github.joeljeremy7.externalizedproperties.core.variableexpansion.SimpleVariableExpander;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.lang.reflect.InvocationHandler;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ExternalizedPropertiesInvocationHandlerFactoryTests {
    private static final Resolver RESOLVER = new StubResolver();
    private static final Converter<?> CONVERTER = new DefaultConverter();
    private static final VariableExpander VARIABLE_EXPANDER = 
        new SimpleVariableExpander();
    private static final ExternalizedProperties EXTERNALIZED_PROPERTIES =
        ExternalizedProperties.builder()
            .resolvers(RESOLVER)
            .converters(CONVERTER)
            .build();

    private static final Resolver ROOT_RESOLVER = new RootResolver(
        Arrays.asList(RESOLVER), 
        new RootProcessor()
    );
    private static final Converter<?> ROOT_CONVERTER = new RootConverter(
        CONVERTER
    );

    private static final InvocationContextFactory INVOCATION_CONTEXT_FACTORY =
        new InvocationContextFactory(EXTERNALIZED_PROPERTIES);

    @Nested
    class CreateMethod {
        @Test
        @DisplayName("should not return null")
        void test1() {
            InvocationHandlerFactory factory = 
                new ExternalizedPropertiesInvocationHandlerFactory();

            assertNotNull(factory.create(
                ProxyInterface.class, 
                ROOT_RESOLVER, 
                ROOT_CONVERTER, 
                VARIABLE_EXPANDER,
                INVOCATION_CONTEXT_FACTORY
            ));
        }

        @Test
        @DisplayName(
            "should return instance of ExternalizedPropertiesInvocationHandler"
        )
        void test2() {
            InvocationHandlerFactory factory = 
                new ExternalizedPropertiesInvocationHandlerFactory();

            InvocationHandler result = factory.create(
                ProxyInterface.class, 
                ROOT_RESOLVER, 
                ROOT_CONVERTER, 
                VARIABLE_EXPANDER,
                INVOCATION_CONTEXT_FACTORY
            );

            assertTrue(result instanceof ExternalizedPropertiesInvocationHandler);
        }
    }

    private static interface ProxyInterface {
        @ExternalizedProperty("property")
        String property();
    }
}
