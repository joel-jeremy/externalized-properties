package io.github.joeljeremy7.externalizedproperties.core.resolvers;

import io.github.joeljeremy7.externalizedproperties.core.ExternalizedProperties;
import io.github.joeljeremy7.externalizedproperties.core.Resolver;
import io.github.joeljeremy7.externalizedproperties.core.ResolverProvider;
import io.github.joeljeremy7.externalizedproperties.core.proxy.ProxyMethod;
import io.github.joeljeremy7.externalizedproperties.core.testentities.ProxyMethods;
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
    @Nested
    class ProviderMethodWithVarArgsOverload {
        @Test
        @DisplayName("should not return null.")
        public void test1() {
            ResolverProvider<CompositeResolver> provider = 
                CompositeResolver.provider(StubResolver.provider());

            assertNotNull(provider);
        }

        @Test
        @DisplayName("should return an instance on get.")
        public void test2() {
            ResolverProvider<CompositeResolver> provider = 
                CompositeResolver.provider(StubResolver.provider());

            assertNotNull(
                provider.get(ExternalizedProperties.builder().withDefaults().build())
            );
        }
    }

    @Nested
    class ProviderMethodWithCollectionOverload {
        @Test
        @DisplayName("should not return null.")
        public void test1() {
            ResolverProvider<CompositeResolver> provider = 
                CompositeResolver.provider(Arrays.asList(StubResolver.provider()));

            assertNotNull(provider);
        }

        @Test
        @DisplayName("should return an instance on get.")
        public void test2() {
            ResolverProvider<CompositeResolver> provider = 
                CompositeResolver.provider(Arrays.asList(StubResolver.provider()));

            assertNotNull(
                provider.get(ExternalizedProperties.builder().withDefaults().build())
            );
        }
    }

    @Nested
    class FlattenedProviderMethodWithVarArgsOverload {
        @Test
        @DisplayName("should not return null.")
        public void test1() {
            ResolverProvider<Resolver> provider = 
                CompositeResolver.flattenedProvider(StubResolver.provider());

            assertNotNull(provider);
        }

        @Test
        @DisplayName("should return an instance on get.")
        public void test2() {
            ResolverProvider<Resolver> provider = 
                CompositeResolver.flattenedProvider(StubResolver.provider());

            assertNotNull(
                provider.get(ExternalizedProperties.builder().withDefaults().build())
            );
        }

        @Test
        @DisplayName("should return the flattened instance on get.")
        public void test3() {
            ResolverProvider<Resolver> provider = 
                CompositeResolver.flattenedProvider(StubResolver.provider());

            Resolver resolver = 
                provider.get(ExternalizedProperties.builder().withDefaults().build());
            assertNotNull(resolver);
            assertTrue(resolver instanceof StubResolver);
        }

        @Test
        @DisplayName(
            "should return the composite resolver instance on get " +
            "when there are multiple resolvers."
        )
        public void test4() {
            ResolverProvider<Resolver> provider = 
                CompositeResolver.flattenedProvider(
                    StubResolver.provider(),
                    DefaultResolver.provider()
                );

            Resolver resolver = 
                provider.get(ExternalizedProperties.builder().withDefaults().build());
            assertNotNull(resolver);
            assertTrue(resolver instanceof CompositeResolver);
        }
    }

    @Nested
    class FlattenedProviderMethodWithCollectionOverload {
        @Test
        @DisplayName("should not return null.")
        public void test1() {
            ResolverProvider<Resolver> provider = 
                CompositeResolver.flattenedProvider(
                    Arrays.asList(StubResolver.provider())
                );

            assertNotNull(provider);
        }

        @Test
        @DisplayName("should return an instance on get.")
        public void test2() {
            ResolverProvider<Resolver> provider = 
                CompositeResolver.flattenedProvider(
                    Arrays.asList(StubResolver.provider())
                );

            assertNotNull(
                provider.get(ExternalizedProperties.builder().withDefaults().build())
            );
        }

        @Test
        @DisplayName("should return the single remaining resolver instance on get.")
        public void test3() {
            ResolverProvider<Resolver> provider = 
                CompositeResolver.flattenedProvider(
                    Arrays.asList(StubResolver.provider())
                );

            Resolver resolver = 
                provider.get(ExternalizedProperties.builder().withDefaults().build());
            assertNotNull(resolver);
            assertTrue(resolver instanceof StubResolver);
        }

        @Test
        @DisplayName(
            "should return the composite resolver instance on get " +
            "when there are multiple resolvers."
        )
        public void test4() {
            ResolverProvider<Resolver> provider = 
                CompositeResolver.flattenedProvider(Arrays.asList(
                    StubResolver.provider(),
                    DefaultResolver.provider()
                ));

            Resolver resolver = 
                provider.get(ExternalizedProperties.builder().withDefaults().build());
            assertNotNull(resolver);
            assertTrue(resolver instanceof CompositeResolver);
        }
    }

    @Nested
    class FromMethod {
        @Test
        @DisplayName("should throw when resolvers collection argument is null or empty.")
        void test1() {
            assertThrows(
                IllegalArgumentException.class, 
                () -> CompositeResolver.from(
                    (Collection<Resolver>)null
                )
            );

            assertThrows(
                IllegalArgumentException.class, 
                () -> CompositeResolver.from(Collections.emptyList())
            );
        }

        @Test
        @DisplayName("should throw when resolvers varargs argument is null or empty.")
        void test2() {
            assertThrows(
                IllegalArgumentException.class, 
                () -> CompositeResolver.from(
                    (Resolver[])null
                )
            );

            assertThrows(
                IllegalArgumentException.class, 
                () -> CompositeResolver.from(new Resolver[0])
            );
        }
    }

    @Nested
    class FlattenMethod {
        @Test
        @DisplayName("should throw when resolvers collection argument is null or empty.")
        void test1() {
            assertThrows(
                IllegalArgumentException.class, 
                () -> CompositeResolver.flatten(
                    (Collection<Resolver>)null
                )
            );

            assertThrows(
                IllegalArgumentException.class, 
                () -> CompositeResolver.flatten(Collections.emptyList())
            );
        }

        @Test
        @DisplayName("should throw when resolvers varargs argument is null or empty.")
        void test2() {
            assertThrows(
                IllegalArgumentException.class, 
                () -> CompositeResolver.flatten(
                    (Resolver[])null
                )
            );

            assertThrows(
                IllegalArgumentException.class, 
                () -> CompositeResolver.flatten(new Resolver[0])
            );
        }

        @Test
        @DisplayName("should discard any nested composite property resolvers.")
        void test3() {
            CompositeResolver resolver1 = 
                CompositeResolver.from(new SystemPropertyResolver());
            CompositeResolver resolver2 =
                CompositeResolver.from(resolver1);
            CompositeResolver resolver3 =
                CompositeResolver.from(new EnvironmentVariableResolver());
            CompositeResolver resolver4 =
                CompositeResolver.from(resolver3);

            Resolver flattenedResolver =   
                CompositeResolver.flatten(resolver2, resolver4);
            
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
            "should return the resolver instance " + 
            "when only one resolver remained after the flattening operation."
        )
        void test4() {
            CompositeResolver resolver = 
                CompositeResolver.from(new SystemPropertyResolver());

            Resolver flattenedResolver =   
                CompositeResolver.flatten(resolver);
            
            assertTrue(flattenedResolver instanceof SystemPropertyResolver);
        }
    }

    @Nested
    class ResolveMethod {
        // @Test
        // @DisplayName("should throw when proxy method argument null.")
        // void validationTest1() {
        //     CompositeResolver compositeResolver = resolverToTest(
        //         new StubResolver()
        //     );

        //     assertThrows(
        //         IllegalArgumentException.class, 
        //         () -> compositeResolver.resolve(null, "property")
        //     );
        // }

        // @Test
        // @DisplayName("should throw when property name argument is null or empty.")
        // void validationTest2() {
        //     CompositeResolver compositeResolver = resolverToTest(
        //         new StubResolver()
        //     );
        //     ProxyMethod proxyMethod = proxyMethod(compositeResolver);

        //     assertThrows(
        //         IllegalArgumentException.class, 
        //         () -> compositeResolver.resolve(proxyMethod, (String)null)
        //     );
            
        //     assertThrows(
        //         IllegalArgumentException.class, 
        //         () -> compositeResolver.resolve(proxyMethod, "")
        //     );
        // }

        @Test
        @DisplayName("should resolve property value from the child resolver.")
        void test1() {
            StubResolver resolver = new StubResolver();
            
            CompositeResolver compositeResolver = resolverToTest(resolver);
            ProxyMethod proxyMethod = ProxyMethods.property();

            Optional<String> result = compositeResolver.resolve(proxyMethod, "property");

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
            "when property is not found from any of the child resolvers."
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
            ProxyMethod proxyMethod = ProxyMethods.property();

            Optional<String> result = compositeResolver.resolve(
                proxyMethod,
                "property"
            );

            assertFalse(resolver1.resolvedPropertyNames().contains("property"));
            assertNotNull(result);
            assertFalse(result.isPresent());
        }

        @Test
        @DisplayName(
            "should be able to resolve property value from one or more child resolvers."
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
            ProxyMethod proxyMethod = ProxyMethods.property2();

            // Should resolve from resolver2.
            Optional<String> result = compositeResolver.resolve(proxyMethod, "property.2");

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
            "when the property has already been resolved."
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
            ProxyMethod proxyMethod = ProxyMethods.property1();

            Optional<String> result = compositeResolver.resolve(proxyMethod, "property.1");

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
        @DisplayName("should return resolver collection string.")
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

    private CompositeResolver resolverToTest(Resolver... resolvers) {
        return CompositeResolver.from(resolvers);
    }
}
