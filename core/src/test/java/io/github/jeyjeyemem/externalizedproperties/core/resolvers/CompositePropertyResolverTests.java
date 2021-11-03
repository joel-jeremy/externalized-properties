package io.github.jeyjeyemem.externalizedproperties.core.resolvers;

import io.github.jeyjeyemem.externalizedproperties.core.ExternalizedPropertyResolver;
import io.github.jeyjeyemem.externalizedproperties.core.ExternalizedPropertyResolverResult;
import io.github.jeyjeyemem.externalizedproperties.core.ResolvedProperty;
import io.github.jeyjeyemem.externalizedproperties.core.testentities.StubExternalizedPropertyResolver;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class CompositePropertyResolverTests {
    @Nested
    class Constructor {
        @Test
        @DisplayName("should throw when resolvers collection argument is null or empty")
        public void test1() {
            assertThrows(
                IllegalArgumentException.class, 
                () -> new CompositePropertyResolver(
                    (Collection<ExternalizedPropertyResolver>)null
                )
            );

            assertThrows(
                IllegalArgumentException.class, 
                () -> new CompositePropertyResolver(Collections.emptyList())
            );
        }

        @Test
        @DisplayName("should throw when resolvers varargs argument is null or empty")
        public void test2() {
            assertThrows(
                IllegalArgumentException.class, 
                () -> new CompositePropertyResolver(
                    (ExternalizedPropertyResolver[])null
                )
            );

            assertThrows(
                IllegalArgumentException.class, 
                () -> new CompositePropertyResolver(new ExternalizedPropertyResolver[0])
            );
        }
    }

    @Nested
    class ResolveMethod {
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
        @DisplayName(
            "should throw when property names collection contains any null or empty values"
        )
        public void validationTest3() {
            CompositePropertyResolver resolver = resolverToTest(
                new StubExternalizedPropertyResolver()
            );

            assertThrows(
                IllegalArgumentException.class, 
                () -> resolver.resolve(Arrays.asList("property", null))
            );
            
            assertThrows(
                IllegalArgumentException.class, 
                () -> resolver.resolve(Arrays.asList("property", ""))
            );
        }

        @Test
        @DisplayName("should throw when property names varargs contain any null or empty values")
        public void validationTest4() {
            CompositePropertyResolver resolver = resolverToTest(
                new StubExternalizedPropertyResolver()
            );

            assertThrows(
                IllegalArgumentException.class, 
                () -> resolver.resolve(new String[] { "property", null })
            );
            
            assertThrows(
                IllegalArgumentException.class, 
                () -> resolver.resolve(new String[] { "property", "" })
            );
        }

        @Test
        @DisplayName("should resolve values from the child resolver")
        public void test1() {
            StubExternalizedPropertyResolver resolver = new StubExternalizedPropertyResolver();
            
            CompositePropertyResolver compositeResolver = resolverToTest(resolver);

            ExternalizedPropertyResolverResult result = compositeResolver.resolve("property.name");

            assertTrue(resolver.resolvedPropertyNames().contains("property.name"));

            assertEquals(
                resolver.valueResolver().apply("property.name"), 
                result.findResolvedProperty("property.name")
                    .map(ResolvedProperty::value)
                    .orElse(null)
            );
        }

        @Test
        @DisplayName(
            "should return result with unresolved properties " + 
            "when property is not found from any of the child resolvers"
        )
        public void test2() {
            StubExternalizedPropertyResolver resolver1 = new StubExternalizedPropertyResolver(
                propertyName -> null // Not resolved
            );

            StubExternalizedPropertyResolver resolver2 = new StubExternalizedPropertyResolver(
                propertyName -> null // Not resolved
            );
            
            CompositePropertyResolver compositeResolver = resolverToTest(
                resolver1,
                resolver2
            );

            ExternalizedPropertyResolverResult result = compositeResolver.resolve("property.nonexistent");

            assertFalse(result.hasResolvedProperties());
            assertTrue(result.hasUnresolvedProperties());
            assertTrue(result.unresolvedPropertyNames().contains("property.nonexistent"));

            assertFalse(resolver1.resolvedPropertyNames().contains("property.nonexistent"));
            assertFalse(resolver2.resolvedPropertyNames().contains("property.nonexistent"));
        }

        @Test
        @DisplayName("should be able to resolve values from one or more child resolvers")
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

            ExternalizedPropertyResolverResult result = compositeResolver.resolve(
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
                resolver1.valueResolver().apply("property.name.1"), 
                result.findResolvedProperty("property.name.1")
                    .map(ResolvedProperty::value)
                    .orElse(null)
            );

            // result2 has same value as resolver2.
            assertEquals(
                resolver2.valueResolver().apply("property.name.2"), 
                result.findResolvedProperty("property.name.2")
                    .map(ResolvedProperty::value)
                    .orElse(null)
            );

            // result3 has same value as resolver3.
            assertEquals(
                resolver3.valueResolver().apply("property.name.3"), 
                result.findResolvedProperty("property.name.3")
                    .map(ResolvedProperty::value)
                    .orElse(null)
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

            ExternalizedPropertyResolverResult result = compositeResolver.resolve(
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
                resolver1.valueResolver().apply("property.name.1"), 
                result.findResolvedProperty("property.name.1")
                    .map(ResolvedProperty::value)
                    .orElse(null)
            );

            // result2 has same value as resolver1.
            assertEquals(
                resolver1.valueResolver().apply("property.name.2"), 
                result.findResolvedProperty("property.name.2")
                    .map(ResolvedProperty::value)
                    .orElse(null)
            );

            /**
             * None for resolver2...
             */

            // result3 has same value as resolver3.
            assertEquals(
                resolver3.valueResolver().apply("property.name.3"), 
                result.findResolvedProperty("property.name.3")
                    .map(ResolvedProperty::value)
                    .orElse(null)
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
                new EnvironmentVariablePropertyResolver(),
                new CompositePropertyResolver(new MapPropertyResolver(Collections.emptyMap()))
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
        return new CompositePropertyResolver(externalizedPropertyResolvers);
    }
}
