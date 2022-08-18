package io.github.joeljeremy7.externalizedproperties.core.resolvers;

import io.github.joeljeremy7.externalizedproperties.core.ExternalizedProperties;
import io.github.joeljeremy7.externalizedproperties.core.ExternalizedProperty;
import io.github.joeljeremy7.externalizedproperties.core.InvocationContext;
import io.github.joeljeremy7.externalizedproperties.core.Resolver;
import io.github.joeljeremy7.externalizedproperties.core.testfixtures.InvocationContextUtils;
import io.github.joeljeremy7.externalizedproperties.core.testfixtures.InvocationContextUtils.InvocationContextTestFactory;
import io.github.joeljeremy7.externalizedproperties.core.testfixtures.StubResolver;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class CompositeResolverTests {
    private static final InvocationContextTestFactory<ProxyInterface> INVOCATION_CONTEXT_FACTORY =
        InvocationContextUtils.testFactory(ProxyInterface.class);

    @Nested
    class FromMethodWithVarargsOverload {
        @Test
        @DisplayName("should throw when resolvers varargs argument is null")
        void test1() {
            assertThrows(
                IllegalArgumentException.class, 
                () -> CompositeResolver.from(
                    (Resolver[])null
                )
            );
        }
        
        @Test
        @DisplayName(
            "should return empty composite resolver when resolvers argument is empty"
        )
        void test2() {
            CompositeResolver compositeResolver = CompositeResolver.from();
            assertEquals(CompositeResolver.EMPTY, compositeResolver);
        }
    }
    @Nested
    class FromMethodWithCollectionOverload {
        @Test
        @DisplayName("should throw when resolvers collection argument is null")
        void test1() {
            assertThrows(
                IllegalArgumentException.class, 
                () -> CompositeResolver.from(
                    (Collection<Resolver>)null
                )
            );
        }

        @Test
        @DisplayName(
            "should return empty composite resolver when resolvers argument is empty"
        )
        void test2() {
            CompositeResolver compositeResolver = CompositeResolver.from(
                Collections.emptyList()
            );
            assertEquals(CompositeResolver.EMPTY, compositeResolver);
        }
    }

    @Nested
    class FlattenMethodWithVarargsOverload {
        @Test
        @DisplayName("should throw when resolvers varargs argument is null")
        void test1() {
            assertThrows(
                IllegalArgumentException.class, 
                () -> CompositeResolver.flatten(
                    (Resolver[])null
                )
            );
        }

        @Test
        @DisplayName("should discard any nested composite property resolvers")
        void test2() {
            CompositeResolver resolver1 = 
                CompositeResolver.from(new SystemPropertyResolver());
            CompositeResolver resolver2 =
                CompositeResolver.from(resolver1);
            CompositeResolver resolver3 =
                CompositeResolver.from(new EnvironmentVariableResolver());
            CompositeResolver resolver4 =
                CompositeResolver.from(resolver3);

            Resolver flattenedResolver = CompositeResolver.flatten(resolver2, resolver4);
            assertTrue(flattenedResolver instanceof CompositeResolver);
            
            CompositeResolver compositeResolver = 
                (CompositeResolver)flattenedResolver;

            // Should discard other composite property resolvers but
            // maintain original resolver order.
            int resolverCount = 0;
            for (Resolver resolver : compositeResolver) {
                if (resolverCount == 0) {
                    assertTrue(resolver instanceof SystemPropertyResolver);
                } else if (resolverCount == 1) {
                    assertTrue(resolver instanceof EnvironmentVariableResolver);
                }
                resolverCount++;
            }

            // There must be only SystemPropertyResolver and EnvironmentPropertyResolver.
            assertEquals(2, resolverCount);
        }

        @Test
        @DisplayName(
            "should return the only remaining resolver instance " + 
            "when only one resolver remains after the flattening operation"
        )
        void test3() {
            CompositeResolver resolver = 
                CompositeResolver.from(new SystemPropertyResolver());

            Resolver flattenedResolver = CompositeResolver.flatten(resolver);
            assertTrue(flattenedResolver instanceof SystemPropertyResolver);
        }

        @Test
        @DisplayName(
            "should return an empty composite resolver when resolvers argument is empty"
        )
        void test4() {
            Resolver flattenedResolver = CompositeResolver.flatten();
            assertTrue(flattenedResolver instanceof CompositeResolver);
            assertEquals(CompositeResolver.EMPTY, flattenedResolver);
        }
    }

    @Nested
    class FlattenMethodWithCollectionOverload {
        @Test
        @DisplayName("should throw when resolvers collection argument is null")
        void test1() {
            assertThrows(
                IllegalArgumentException.class, 
                () -> CompositeResolver.flatten(
                    (Collection<Resolver>)null
                )
            );
        }

        @Test
        @DisplayName("should discard any nested composite property resolvers")
        void test2() {
            CompositeResolver resolver1 = 
                CompositeResolver.from(new SystemPropertyResolver());
            CompositeResolver resolver2 =
                CompositeResolver.from(resolver1);
            CompositeResolver resolver3 =
                CompositeResolver.from(new EnvironmentVariableResolver());
            CompositeResolver resolver4 =
                CompositeResolver.from(resolver3);

            Resolver flattenedResolver = CompositeResolver.flatten(
                Arrays.asList(resolver2, resolver4)
            );
            assertTrue(flattenedResolver instanceof CompositeResolver);
            
            CompositeResolver compositeResolver = 
                (CompositeResolver)flattenedResolver;

            // Should discard other composite property resolvers but
            // maintain original resolver order.
            int resolverCount = 0;
            for (Resolver resolver : compositeResolver) {
                if (resolverCount == 0) {
                    assertTrue(resolver instanceof SystemPropertyResolver);
                } else if (resolverCount == 1) {
                    assertTrue(resolver instanceof EnvironmentVariableResolver);
                }
                resolverCount++;
            }

            // There must be only SystemPropertyResolver and EnvironmentPropertyResolver.
            assertEquals(2, resolverCount);
        }

        @Test
        @DisplayName(
            "should return the only remaining resolver instance " + 
            "when only one resolver remains after the flattening operation"
        )
        void test3() {
            CompositeResolver resolver = 
                CompositeResolver.from(new SystemPropertyResolver());

            Resolver flattenedResolver = CompositeResolver.flatten(
                Collections.singletonList(resolver)
            );
            assertTrue(flattenedResolver instanceof SystemPropertyResolver);
        }

        @Test
        @DisplayName(
            "should return an empty composite resolver when resolvers argument is empty"
        )
        void test4() {
            Resolver flattenedResolver = CompositeResolver.flatten(Collections.emptyList());
            assertTrue(flattenedResolver instanceof CompositeResolver);
            assertEquals(CompositeResolver.EMPTY, flattenedResolver);
        }
    }

    @Nested
    class ResolveMethod {
        @Test
        @DisplayName("should resolve property value from the child resolver")
        void test1() {
            StubResolver resolver = new StubResolver();
            
            CompositeResolver compositeResolver = resolverToTest(resolver);
            InvocationContext context = INVOCATION_CONTEXT_FACTORY.fromMethodReference(
                ProxyInterface::property,
                externalizedProperties(resolver)
            );

            Optional<String> result = compositeResolver.resolve(context, "property");

            assertTrue(resolver.resolvedPropertyNames().contains("property"));
            assertNotNull(result);
            assertTrue(result.isPresent());
            assertEquals(
                resolver.resolvedProperties().get("property"), 
                result.get()
            );
        }

        @Test
        @DisplayName(
            "should return empty Optional " + 
            "when property is not found from any of the child resolvers"
        )
        void test2() {
            StubResolver resolver1 = new StubResolver(
                StubResolver.NULL_VALUE_RESOLVER
            );

            StubResolver resolver2 = new StubResolver(
                StubResolver.NULL_VALUE_RESOLVER
            );
            
            CompositeResolver compositeResolver = resolverToTest(
                resolver1,
                resolver2
            );
            InvocationContext context = INVOCATION_CONTEXT_FACTORY.fromMethodReference(
                ProxyInterface::property,
                externalizedProperties(compositeResolver)
            );

            Optional<String> result = compositeResolver.resolve(
                context,
                "property"
            );

            assertFalse(resolver1.resolvedPropertyNames().contains("property"));
            assertNotNull(result);
            assertFalse(result.isPresent());
        }

        @Test
        @DisplayName(
            "should be able to resolve property value from one or more child resolvers"
        )
        void test3() {
            StubResolver resolver1 = new StubResolver(
                propertyName -> propertyName.endsWith("1") ? 
                    "resolver-1-result" :
                    null
            );

            StubResolver resolver2 = new StubResolver(
                propertyName -> propertyName.endsWith("2") ? 
                    "resolver-2-result" :
                    null
            );

            StubResolver resolver3 = new StubResolver(
                propertyName -> propertyName.endsWith("3") ?
                    "resolver-3-result" :
                    null
            );
            
            CompositeResolver compositeResolver = resolverToTest(
                resolver1,
                resolver2,
                resolver3
            );
            InvocationContext context = INVOCATION_CONTEXT_FACTORY.fromMethodReference(
                ProxyInterface::property2,
                externalizedProperties(compositeResolver)
            );

            // Should resolve from resolver2.
            Optional<String> result = compositeResolver.resolve(context, "property.2");

            // property.2 resolved from resolver2
            assertFalse(resolver2.resolvedPropertyNames().contains("property.1"));
            assertTrue(resolver2.resolvedPropertyNames().contains("property.2"));
            assertFalse(resolver2.resolvedPropertyNames().contains("property.3"));

            assertNotNull(result);
            assertTrue(result.isPresent());
            assertEquals(
                resolver2.resolvedProperties().get("property.2"), 
                result.get()
            );
        }

        @Test
        @DisplayName(
            "should skip resolving from downstream resolvers " + 
            "when the property has already been resolved"
        )
        void test4() {
            StubResolver resolver1 = new StubResolver(
                propertyName -> propertyName.endsWith("1") ? 
                    "resolver-1-result" :
                    null
            );

            StubResolver resolver2 = new StubResolver(
                propertyName -> propertyName.endsWith("2") ? 
                    "resolver-2-result" :
                    null
            );

            StubResolver resolver3 = new StubResolver(
                propertyName -> propertyName.endsWith("3") ?
                    "resolver-3-result" :
                    null
            );
            
            CompositeResolver compositeResolver = resolverToTest(
                resolver1,
                resolver2,
                resolver3
            );
            InvocationContext context = INVOCATION_CONTEXT_FACTORY.fromMethodReference(
                ProxyInterface::property1,
                externalizedProperties(compositeResolver)
            );

            Optional<String> result = compositeResolver.resolve(context, "property.1");

            // property.1 and resolved from resolver1 and not from subsequent resolvers.
            assertTrue(resolver1.resolvedPropertyNames().contains("property.1"));
            assertFalse(resolver2.resolvedPropertyNames().contains("property.1"));
            assertFalse(resolver3.resolvedPropertyNames().contains("property.1"));

            assertNotNull(result);
            assertTrue(result.isPresent());
            assertEquals(
                resolver1.resolvedProperties().get("property.1"), 
                result.get()
            );
        }
    }

    @Nested
    class ToStringMethod {
        @Test
        @DisplayName("should return resolver collection string")
        void test1() {
            List<Resolver> resolvers = Arrays.asList(
                new SystemPropertyResolver(),
                new EnvironmentVariableResolver(),
                CompositeResolver.from(new MapResolver(Collections.emptyMap()))
            );

            CompositeResolver resolver = resolverToTest(
                resolvers.toArray(new Resolver[resolvers.size()])
            );

            assertEquals(resolvers.toString(), resolver.toString());
        }
    }

    private static CompositeResolver resolverToTest(Resolver... resolvers) {
        return CompositeResolver.from(resolvers);
    }
    
    private static ExternalizedProperties externalizedProperties(Resolver... resolvers) {
        return ExternalizedProperties.builder().resolvers(resolvers).build();
    }

    private static interface ProxyInterface {
        @ExternalizedProperty("property")
        String property();

        @ExternalizedProperty("property.1")
        String property1();

        @ExternalizedProperty("property.2")
        String property2();
    }
}
