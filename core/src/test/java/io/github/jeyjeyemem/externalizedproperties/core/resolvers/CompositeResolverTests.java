package io.github.jeyjeyemem.externalizedproperties.core.resolvers;

import io.github.jeyjeyemem.externalizedproperties.core.Resolver;
import io.github.jeyjeyemem.externalizedproperties.core.ResolverResult;
import io.github.jeyjeyemem.externalizedproperties.core.testentities.StubResolver;
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
    class FromMethod {
        @Test
        @DisplayName("should throw when resolvers collection argument is null or empty")
        public void test1() {
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
        @DisplayName("should throw when resolvers varargs argument is null or empty")
        public void test2() {
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
        @DisplayName("should throw when resolvers collection argument is null or empty")
        public void test1() {
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
        @DisplayName("should throw when resolvers varargs argument is null or empty")
        public void test2() {
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
        @DisplayName("should discard any nested composite property resolvers")
        public void test3() {
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
            "when only one resolver remained after the flattening operation"
        )
        public void test4() {
            CompositeResolver resolver = 
                CompositeResolver.from(new SystemPropertyResolver());

            Resolver flattenedResolver =   
                CompositeResolver.flatten(resolver);
            
            assertTrue(flattenedResolver instanceof SystemPropertyResolver);
        }
    }

    @Nested
    class ResolveMethod {
        @Test
        @DisplayName("should throw when property name argument is null or empty")
        public void validationTest1() {
            CompositeResolver compositeResolver = resolverToTest(
                new StubResolver()
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
            StubResolver resolver = new StubResolver();
            
            CompositeResolver compositeResolver = resolverToTest(resolver);

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
    class ResolveMethodWithVarArgsOverload {
        @Test
        @DisplayName("should throw when property names varargs argument is null or empty")
        public void validationTest1() {
            CompositeResolver compositeResolver = resolverToTest(
                new StubResolver()
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
            String[] propertiesToResolve = new String[] { "property.name1", "property.name2" };

            StubResolver resolver = new StubResolver();
            
            CompositeResolver compositeResolver = resolverToTest(resolver);

            ResolverResult result = compositeResolver.resolve(propertiesToResolve);

            assertTrue(result.hasResolvedProperties());
            assertFalse(result.hasUnresolvedProperties());

            for (String propertyName : propertiesToResolve) {
                assertTrue(resolver.resolvedPropertyNames().contains(propertyName));

                assertEquals(
                    resolver.resolvedProperties().get(propertyName), 
                    result.findRequiredProperty(propertyName)
                );
            }
        }

        @Test
        @DisplayName(
            "should return result with unresolved properties " + 
            "when property is not found from any of the child resolvers"
        )
        public void test2() {
            String[] propertiesToResolve = new String[] { 
                "property.nonexistent1",
                "property.nonexistent2"
            };

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

            ResolverResult result = compositeResolver.resolve(
                propertiesToResolve
            );

            assertFalse(result.hasResolvedProperties());
            assertTrue(result.hasUnresolvedProperties());

            for (String propertyName : propertiesToResolve) {  
                assertTrue(result.unresolvedPropertyNames().contains(propertyName));
                assertFalse(result.resolvedPropertyNames().contains(propertyName));
            }
        }

        @Test
        @DisplayName("should be able to resolve property values from one or more child resolvers")
        public void test3() {
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

            ResolverResult result = compositeResolver.resolve(
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
            StubResolver resolver1 = new StubResolver(
                propertyName -> propertyName.endsWith("1") || propertyName.endsWith("2") ? 
                    "resolver-1-result" :
                    null
            );

            StubResolver resolver2 = new StubResolver(
                propertyName -> propertyName.endsWith("2") ? 
                    "resolver-2-result" :
                    null
            );

            StubResolver resolver3 = new StubResolver(
                propertyName -> propertyName.endsWith("2") || propertyName.endsWith("3") ?
                    "resolver-3-result" :
                    null
            );
            
            CompositeResolver compositeResolver = resolverToTest(
                resolver1,
                resolver2,
                resolver3
            );

            ResolverResult result = compositeResolver.resolve(
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
    class ResolveMethodWithCollectionOverload {
        @Test
        @DisplayName("should throw when property names collection argument is null or empty")
        public void validationTest1() {
            CompositeResolver compositeResolver = resolverToTest(
                new StubResolver()
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
        @DisplayName("should resolve property values from the child resolver")
        public void test1() {
            List<String> propertiesToResolve = Arrays.asList(
                "property.name1",
                "property.name2"
            );

            StubResolver resolver = new StubResolver();
            
            CompositeResolver compositeResolver = resolverToTest(resolver);

            ResolverResult result = compositeResolver.resolve(
                propertiesToResolve
            );

            assertTrue(result.hasResolvedProperties());
            assertFalse(result.hasUnresolvedProperties());

            for (String propertyName : propertiesToResolve) {
                assertTrue(resolver.resolvedPropertyNames().contains(propertyName));

                assertEquals(
                    resolver.resolvedProperties().get(propertyName), 
                    result.findRequiredProperty(propertyName)
                );
            }
        }

        @Test
        @DisplayName(
            "should return result with unresolved properties " + 
            "when property is not found from any of the child resolvers"
        )
        public void test2() {
            List<String> propertiesToResolve = Arrays.asList(
                "property.nonexistent1",
                "property.nonexistent2"
            );

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

            ResolverResult result = compositeResolver.resolve(
                propertiesToResolve
            );

            assertFalse(result.hasResolvedProperties());
            assertTrue(result.hasUnresolvedProperties());

            for (String propertyName : propertiesToResolve) {  
                assertTrue(result.unresolvedPropertyNames().contains(propertyName));
                assertFalse(result.resolvedPropertyNames().contains(propertyName));
            }
        }

        @Test
        @DisplayName("should be able to resolve property values from one or more child resolvers")
        public void test3() {
            List<String> propertiesToResolve = Arrays.asList(
                "property.name.1",
                "property.name.2",
                "property.name.3"
            );

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

            ResolverResult result = compositeResolver.resolve(
                propertiesToResolve
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
            List<String> propertiesToResolve = Arrays.asList(
                "property.name.1",
                "property.name.2",
                "property.name.3"
            );
            
            StubResolver resolver1 = new StubResolver(
                propertyName -> propertyName.endsWith("1") || propertyName.endsWith("2") ? 
                    "resolver-1-result" :
                    null
            );

            StubResolver resolver2 = new StubResolver(
                propertyName -> propertyName.endsWith("2") ? 
                    "resolver-2-result" :
                    null
            );

            StubResolver resolver3 = new StubResolver(
                propertyName -> propertyName.endsWith("2") || propertyName.endsWith("3") ?
                    "resolver-3-result" :
                    null
            );
            
            CompositeResolver compositeResolver = resolverToTest(
                resolver1,
                resolver2,
                resolver3
            );

            ResolverResult result = compositeResolver.resolve(
                propertiesToResolve
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

    private CompositeResolver resolverToTest(
            Resolver... resolvers
    ) {
        return CompositeResolver.from(resolvers);
    }
}
