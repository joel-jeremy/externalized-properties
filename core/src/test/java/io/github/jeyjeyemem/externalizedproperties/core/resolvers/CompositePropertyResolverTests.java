package io.github.jeyjeyemem.externalizedproperties.core.resolvers;

import io.github.jeyjeyemem.externalizedproperties.core.ExternalizedPropertyResolver;
import io.github.jeyjeyemem.externalizedproperties.core.testentities.StubExternalizedPropertyResolver;
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

public class CompositePropertyResolverTests {
    @Nested
    class FromMethod {
        @Test
        @DisplayName("should throw when resolvers collection argument is null or empty")
        public void test1() {
            assertThrows(
                IllegalArgumentException.class, 
                () -> CompositePropertyResolver.from(
                    (Collection<ExternalizedPropertyResolver>)null
                )
            );

            assertThrows(
                IllegalArgumentException.class, 
                () -> CompositePropertyResolver.from(Collections.emptyList())
            );
        }

        @Test
        @DisplayName("should throw when resolvers varargs argument is null or empty")
        public void test2() {
            assertThrows(
                IllegalArgumentException.class, 
                () -> CompositePropertyResolver.from(
                    (ExternalizedPropertyResolver[])null
                )
            );

            assertThrows(
                IllegalArgumentException.class, 
                () -> CompositePropertyResolver.from(new ExternalizedPropertyResolver[0])
            );
        }
    }

    @Nested
    class FlattenMethod {
        @Test
        @DisplayName("should throw when resolvers collection argument is null or empty")
        public void test1() {
            assertThrows(
                IllegalArgumentException.class, 
                () -> CompositePropertyResolver.flatten(
                    (Collection<ExternalizedPropertyResolver>)null
                )
            );

            assertThrows(
                IllegalArgumentException.class, 
                () -> CompositePropertyResolver.flatten(Collections.emptyList())
            );
        }

        @Test
        @DisplayName("should throw when resolvers varargs argument is null or empty")
        public void test2() {
            assertThrows(
                IllegalArgumentException.class, 
                () -> CompositePropertyResolver.flatten(
                    (ExternalizedPropertyResolver[])null
                )
            );

            assertThrows(
                IllegalArgumentException.class, 
                () -> CompositePropertyResolver.flatten(new ExternalizedPropertyResolver[0])
            );
        }

        @Test
        @DisplayName("should discard any nested composite property resolvers")
        public void test3() {
            CompositePropertyResolver resolver1 = 
                CompositePropertyResolver.from(new SystemPropertyResolver());
            CompositePropertyResolver resolver2 =
                CompositePropertyResolver.from(resolver1);
            CompositePropertyResolver resolver3 =
                CompositePropertyResolver.from(new EnvironmentPropertyResolver());
            CompositePropertyResolver resolver4 =
                CompositePropertyResolver.from(resolver3);

            ExternalizedPropertyResolver flattenedResolver =   
                CompositePropertyResolver.flatten(resolver2, resolver4);
            
            assertTrue(flattenedResolver instanceof CompositePropertyResolver);
            
            CompositePropertyResolver compositeResolver = 
                (CompositePropertyResolver)flattenedResolver;

            // Should discard other composite property resolvers but
            // maintain original resolver order.
            int resolverCount = 0;
            for (ExternalizedPropertyResolver resolver : compositeResolver) {
                if (resolverCount == 0) {
                    assertTrue(resolver instanceof SystemPropertyResolver);
                } else if (resolverCount == 1) {
                    assertTrue(resolver instanceof EnvironmentPropertyResolver);
                }
                resolverCount++;
            }

            // There must be only SystemPropertyResolver and EnvironmentPropertyResolver.
            assertEquals(2, resolverCount);
        }

        @Test
        @DisplayName(
            "should return the resolver instance " + 
            "when only one resolver remained after the flattening operation"
        )
        public void test4() {
            CompositePropertyResolver resolver = 
                CompositePropertyResolver.from(new SystemPropertyResolver());

            ExternalizedPropertyResolver flattenedResolver =   
                CompositePropertyResolver.flatten(resolver);
            
            assertTrue(flattenedResolver instanceof SystemPropertyResolver);
        }
    }

    @Nested
    class ResolveMethodSingleProperty {
        @Test
        @DisplayName("should throw when property name argument is null or empty")
        public void validationTest1() {
            CompositePropertyResolver compositeResolver = resolverToTest(
                new StubExternalizedPropertyResolver()
            );

            assertThrows(
                IllegalArgumentException.class, 
                () -> compositeResolver.resolve((String)null)
            );
            
            assertThrows(
                IllegalArgumentException.class, 
                () -> compositeResolver.resolve("")
            );
        }

        @Test
        @DisplayName("should resolve property value from the child resolver")
        public void test1() {
            StubExternalizedPropertyResolver resolver = new StubExternalizedPropertyResolver();
            
            CompositePropertyResolver compositeResolver = resolverToTest(resolver);

            Optional<String> result = compositeResolver.resolve("property.name");

            assertTrue(resolver.resolvedPropertyNames().contains("property.name"));
            assertNotNull(result);
            assertTrue(result.isPresent());
            assertEquals(
                resolver.resolvedProperties().get("property.name"), 
                result.get()
            );
        }

        @Test
        @DisplayName(
            "should return empty Optional " + 
            "when property is not found from any of the child resolvers"
        )
        public void test2() {
            StubExternalizedPropertyResolver resolver1 = new StubExternalizedPropertyResolver(
                StubExternalizedPropertyResolver.NULL_VALUE_RESOLVER
            );

            StubExternalizedPropertyResolver resolver2 = new StubExternalizedPropertyResolver(
                StubExternalizedPropertyResolver.NULL_VALUE_RESOLVER
            );
            
            CompositePropertyResolver compositeResolver = resolverToTest(
                resolver1,
                resolver2
            );

            Optional<String> result = compositeResolver.resolve(
                "property.nonexistent"
            );

            assertFalse(resolver1.resolvedPropertyNames().contains("property.nonexistent"));
            assertNotNull(result);
            assertFalse(result.isPresent());
        }

        @Test
        @DisplayName("should be able to resolve property value from one or more child resolvers")
        public void test3() {
            StubExternalizedPropertyResolver resolver1 = new StubExternalizedPropertyResolver(
                propertyName -> propertyName.endsWith("1") ? 
                    "resolver-1-result" :
                    null
            );

            StubExternalizedPropertyResolver resolver2 = new StubExternalizedPropertyResolver(
                propertyName -> propertyName.endsWith("2") ? 
                    "resolver-2-result" :
                    null
            );

            StubExternalizedPropertyResolver resolver3 = new StubExternalizedPropertyResolver(
                propertyName -> propertyName.endsWith("3") ?
                    "resolver-3-result" :
                    null
            );
            
            CompositePropertyResolver compositeResolver = resolverToTest(
                resolver1,
                resolver2,
                resolver3
            );

            // Should resolve from resolver2.
            Optional<String> result = compositeResolver.resolve("property.name.2");

            // property.name.2 resolved from resolver2
            assertFalse(resolver2.resolvedPropertyNames().contains("property.name.1"));
            assertTrue(resolver2.resolvedPropertyNames().contains("property.name.2"));
            assertFalse(resolver2.resolvedPropertyNames().contains("property.name.3"));

            assertNotNull(result);
            assertTrue(result.isPresent());
            assertEquals(
                resolver2.resolvedProperties().get("property.name.2"), 
                result.get()
            );
        }

        @Test
        @DisplayName(
            "should skip resolving from downstream resolvers " + 
            "when the property has already been resolved"
        )
        public void test4() {
            StubExternalizedPropertyResolver resolver1 = new StubExternalizedPropertyResolver(
                propertyName -> propertyName.endsWith("1") ? 
                    "resolver-1-result" :
                    null
            );

            StubExternalizedPropertyResolver resolver2 = new StubExternalizedPropertyResolver(
                propertyName -> propertyName.endsWith("2") ? 
                    "resolver-2-result" :
                    null
            );

            StubExternalizedPropertyResolver resolver3 = new StubExternalizedPropertyResolver(
                propertyName -> propertyName.endsWith("3") ?
                    "resolver-3-result" :
                    null
            );
            
            CompositePropertyResolver compositeResolver = resolverToTest(
                resolver1,
                resolver2,
                resolver3
            );

            Optional<String> result = compositeResolver.resolve("property.name.1");

            // property.name.1 and resolved from resolver1 and not from subsequent resolvers.
            assertTrue(resolver1.resolvedPropertyNames().contains("property.name.1"));
            assertFalse(resolver2.resolvedPropertyNames().contains("property.name.1"));
            assertFalse(resolver3.resolvedPropertyNames().contains("property.name.1"));

            assertNotNull(result);
            assertTrue(result.isPresent());
            assertEquals(
                resolver1.resolvedProperties().get("property.name.1"), 
                result.get()
            );
        }
    }

    @Nested
    class ResolveMethodMultipleProperties {
        @Test
        @DisplayName("should throw when property names collection argument is null or empty")
        public void validationTest1() {
            CompositePropertyResolver compositeResolver = resolverToTest(
                new StubExternalizedPropertyResolver()
            );

            assertThrows(
                IllegalArgumentException.class, 
                () -> compositeResolver.resolve((Collection<String>)null)
            );
            
            assertThrows(
                IllegalArgumentException.class, 
                () -> compositeResolver.resolve(Collections.emptyList())
            );
        }

        @Test
        @DisplayName("should throw when property names varargs argument is null or empty")
        public void validationTest2() {
            CompositePropertyResolver compositeResolver = resolverToTest(
                new StubExternalizedPropertyResolver()
            );

            assertThrows(
                IllegalArgumentException.class, 
                () -> compositeResolver.resolve((String[])null)
            );
            
            assertThrows(
                IllegalArgumentException.class, 
                () -> compositeResolver.resolve(new String[0])
            );
        }

        @Test
        @DisplayName("should resolve property values from the child resolver")
        public void test1() {
            StubExternalizedPropertyResolver resolver = new StubExternalizedPropertyResolver();
            
            CompositePropertyResolver compositeResolver = resolverToTest(resolver);

            CompositePropertyResolver.Result result = compositeResolver.resolve(
                "property.name1",
                "property.name2"
            );

            assertTrue(resolver.resolvedPropertyNames().contains("property.name1"));
            assertTrue(resolver.resolvedPropertyNames().contains("property.name2"));

            assertEquals(
                resolver.resolvedProperties().get("property.name1"), 
                result.findRequiredProperty("property.name1")
            );

            assertEquals(
                resolver.resolvedProperties().get("property.name2"), 
                result.findRequiredProperty("property.name2")
            );
        }

        @Test
        @DisplayName(
            "should return result with unresolved properties " + 
            "when property is not found from any of the child resolvers"
        )
        public void test2() {
            StubExternalizedPropertyResolver resolver1 = new StubExternalizedPropertyResolver(
                StubExternalizedPropertyResolver.NULL_VALUE_RESOLVER
            );

            StubExternalizedPropertyResolver resolver2 = new StubExternalizedPropertyResolver(
                StubExternalizedPropertyResolver.NULL_VALUE_RESOLVER
            );
            
            CompositePropertyResolver compositeResolver = resolverToTest(
                resolver1,
                resolver2
            );

            CompositePropertyResolver.Result result = compositeResolver.resolve(
                "property.nonexistent1",
                "property.nonexistent2"
            );

            assertFalse(result.hasResolvedProperties());
            assertTrue(result.hasUnresolvedProperties());
            assertTrue(result.unresolvedPropertyNames().contains("property.nonexistent1"));
            assertTrue(result.unresolvedPropertyNames().contains("property.nonexistent2"));

            assertFalse(resolver1.resolvedPropertyNames().contains("property.nonexistent1"));
            assertFalse(resolver2.resolvedPropertyNames().contains("property.nonexistent2"));
        }

        @Test
        @DisplayName("should be able to resolve property values from one or more child resolvers")
        public void test3() {
            StubExternalizedPropertyResolver resolver1 = new StubExternalizedPropertyResolver(
                propertyName -> propertyName.endsWith("1") ? 
                    "resolver-1-result" :
                    null
            );

            StubExternalizedPropertyResolver resolver2 = new StubExternalizedPropertyResolver(
                propertyName -> propertyName.endsWith("2") ? 
                    "resolver-2-result" :
                    null
            );

            StubExternalizedPropertyResolver resolver3 = new StubExternalizedPropertyResolver(
                propertyName -> propertyName.endsWith("3") ?
                    "resolver-3-result" :
                    null
            );
            
            CompositePropertyResolver compositeResolver = resolverToTest(
                resolver1,
                resolver2,
                resolver3
            );

            CompositePropertyResolver.Result result = compositeResolver.resolve(
                "property.name.1",
                "property.name.2",
                "property.name.3"
            );

            // property.name.1 resolved from resolver1
            assertTrue(resolver1.resolvedPropertyNames().contains("property.name.1"));
            assertFalse(resolver1.resolvedPropertyNames().contains("property.name.2"));
            assertFalse(resolver1.resolvedPropertyNames().contains("property.name.3"));

            // property.name.2 resolved from resolver2
            assertFalse(resolver2.resolvedPropertyNames().contains("property.name.1"));
            assertTrue(resolver2.resolvedPropertyNames().contains("property.name.2"));
            assertFalse(resolver2.resolvedPropertyNames().contains("property.name.3"));

            // property.name.3 resolved from resolver3
            assertFalse(resolver3.resolvedPropertyNames().contains("property.name.1"));
            assertFalse(resolver3.resolvedPropertyNames().contains("property.name.2"));
            assertTrue(resolver3.resolvedPropertyNames().contains("property.name.3"));


            // result1 has same value as resolver1.
            assertEquals(
                resolver1.resolvedProperties().get("property.name.1"), 
                result.findRequiredProperty("property.name.1")
            );

            // result2 has same value as resolver2.
            assertEquals(
                resolver2.resolvedProperties().get("property.name.2"), 
                result.findRequiredProperty("property.name.2")
            );

            // result3 has same value as resolver3.
            assertEquals(
                resolver3.resolvedProperties().get("property.name.3"), 
                result.findRequiredProperty("property.name.3")
            );
        }

        @Test
        @DisplayName(
            "should skip resolving from downstream resolvers " + 
            "when the property has already been resolved"
        )
        public void test4() {
            StubExternalizedPropertyResolver resolver1 = new StubExternalizedPropertyResolver(
                propertyName -> propertyName.endsWith("1") || propertyName.endsWith("2") ? 
                    "resolver-1-result" :
                    null
            );

            StubExternalizedPropertyResolver resolver2 = new StubExternalizedPropertyResolver(
                propertyName -> propertyName.endsWith("2") ? 
                    "resolver-2-result" :
                    null
            );

            StubExternalizedPropertyResolver resolver3 = new StubExternalizedPropertyResolver(
                propertyName -> propertyName.endsWith("2") || propertyName.endsWith("3") ?
                    "resolver-3-result" :
                    null
            );
            
            CompositePropertyResolver compositeResolver = resolverToTest(
                resolver1,
                resolver2,
                resolver3
            );

            CompositePropertyResolver.Result result = compositeResolver.resolve(
                "property.name.1",
                "property.name.2",
                "property.name.3"
            );

            // property.name.1 and property.name.2 resolved from resolver1
            assertTrue(resolver1.resolvedPropertyNames().contains("property.name.1"));
            assertTrue(resolver1.resolvedPropertyNames().contains("property.name.2"));
            assertFalse(resolver1.resolvedPropertyNames().contains("property.name.3"));

            // None resolved from resolver2 since property.name.2 was resolved by resolver1
            assertFalse(resolver2.resolvedPropertyNames().contains("property.name.1"));
            assertFalse(resolver2.resolvedPropertyNames().contains("property.name.2"));
            assertFalse(resolver2.resolvedPropertyNames().contains("property.name.3"));

            // property.name.3 resolved from resolver3
            assertFalse(resolver3.resolvedPropertyNames().contains("property.name.1"));
            assertFalse(resolver3.resolvedPropertyNames().contains("property.name.2"));
            assertTrue(resolver3.resolvedPropertyNames().contains("property.name.3"));

            // result1 has same value as resolver1.
            assertEquals(
                resolver1.resolvedProperties().get("property.name.1"), 
                result.findRequiredProperty("property.name.1")
            );

            // result2 has same value as resolver1.
            assertEquals(
                resolver1.resolvedProperties().get("property.name.2"), 
                result.findRequiredProperty("property.name.2")
            );

            /**
             * None for resolver2...
             */

            // result3 has same value as resolver3.
            assertEquals(
                resolver3.resolvedProperties().get("property.name.3"), 
                result.findRequiredProperty("property.name.3")
            );
        }
    }

    @Nested
    class ToStringMethod {
        @Test
        @DisplayName("should return resolver collection string")
        public void test1() {
            List<ExternalizedPropertyResolver> resolvers = Arrays.asList(
                new SystemPropertyResolver(),
                new EnvironmentPropertyResolver(),
                CompositePropertyResolver.from(new MapPropertyResolver(Collections.emptyMap()))
            );

            CompositePropertyResolver resolver = resolverToTest(
                resolvers.toArray(new ExternalizedPropertyResolver[resolvers.size()])
            );

            assertEquals(resolvers.toString(), resolver.toString());
        }
    }

    private CompositePropertyResolver resolverToTest(
            ExternalizedPropertyResolver... externalizedPropertyResolvers
    ) {
        return CompositePropertyResolver.from(externalizedPropertyResolvers);
    }
}
