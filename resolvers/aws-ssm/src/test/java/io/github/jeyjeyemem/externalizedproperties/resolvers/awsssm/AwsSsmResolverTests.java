package io.github.jeyjeyemem.externalizedproperties.resolvers.awsssm;

import io.github.jeyjeyemem.externalizedproperties.core.ResolverResult;
import io.github.jeyjeyemem.externalizedproperties.resolvers.awsssm.testentities.StubSsmClient;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import software.amazon.awssdk.services.ssm.SsmClient;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class AwsSsmResolverTests {
    @Nested
    class Constructor {
        @Test
        @DisplayName("should throw when ssm client argument is null")
        void test1() {
            assertThrows(
                IllegalArgumentException.class, 
                () -> new AwsSsmResolver(null)
            );
        }
    }

    @Nested
    class ResolveMethod {
        @Test
        @DisplayName("should throw when propertyName argument is null")
        void test1() {
            AwsSsmResolver awsSsmPropertyResolver = 
                new AwsSsmResolver(new StubSsmClient());

            assertThrows(IllegalArgumentException.class, () -> {
                awsSsmPropertyResolver.resolve((String)null);
            });
        }

        @Test
        @DisplayName("should throw when propertyName argument is empty")
        void test2() {
            AwsSsmResolver awsSsmPropertyResolver = 
                new AwsSsmResolver(new StubSsmClient());
                
            assertThrows(IllegalArgumentException.class, () -> {
                awsSsmPropertyResolver.resolve("");
            });
        }

        @Test
        @DisplayName("should resolve property from AWS SSM")
        void test3() {
            AwsSsmResolver awsSsmPropertyResolver = 
                new AwsSsmResolver(new StubSsmClient());

            Optional<String> resolved = awsSsmPropertyResolver.resolve("/test/property");

            assertTrue(resolved.isPresent());
            assertEquals("test.property.value", resolved.get());
        }

        @Test
        @DisplayName("should return empty Optional when property is not found in AWS SSM")
        void tes4() {
            AwsSsmResolver awsSsmPropertyResolver = 
                new AwsSsmResolver(StubSsmClient.NO_PARAMETERS);

            Optional<String> resolved = awsSsmPropertyResolver.resolve(
                "/non/existing/property"
            );

            assertFalse(resolved.isPresent());
        }
    }

    @Nested
    class ResolveMethodWithCollectionOverload {

        @Test
        @DisplayName("should throw when propertyNames argument is null")
        void test1() {
            AwsSsmResolver awsSsmPropertyResolver = 
                new AwsSsmResolver(new StubSsmClient());

            assertThrows(IllegalArgumentException.class, () -> {
                awsSsmPropertyResolver.resolve((Collection<String>)null);
            });
        }

        @Test
        @DisplayName("should throw when propertyNames argument is empty")
        void test2() {
            AwsSsmResolver awsSsmPropertyResolver = 
                new AwsSsmResolver(new StubSsmClient());
                
            assertThrows(IllegalArgumentException.class, () -> {
                awsSsmPropertyResolver.resolve(Collections.emptyList());
            });
        }

        @Test
        @DisplayName("should resolve all properties from AWS SSM")
        void test3() {
            AwsSsmResolver awsSsmPropertyResolver = 
                new AwsSsmResolver(new StubSsmClient());

            List<String> propertiesToResolve = Arrays.asList(
                "/test/property",
                "/test/property/2"
            );

            ResolverResult result = 
                awsSsmPropertyResolver.resolve(propertiesToResolve);

            assertTrue(result.hasResolvedProperties());
            assertFalse(result.hasUnresolvedProperties());

            assertTrue(result.resolvedPropertyNames().stream()
                .allMatch(resolved -> propertiesToResolve.contains(resolved))
            );

            assertEquals(
                "test.property.value", 
                result.findRequiredProperty("/test/property")
            );

            assertEquals(
                "test.property.2.value", 
                result.findRequiredProperty("/test/property/2")
            );
        }

        @Test
        @DisplayName(
            "should return result with resolved and unresolved properties from AWS SSM"
        )
        void test4() {
            // Only returns properties that starts with test/property
            SsmClient stubSsmClient = new StubSsmClient(
                p -> p.startsWith("/test/property") ? 
                    StubSsmClient.DEFAULT_DELEGATE.apply(p) : 
                    null
            );
            AwsSsmResolver awsSsmPropertyResolver = 
                new AwsSsmResolver(stubSsmClient);

            List<String> propertiesToResolve = Arrays.asList(
                "/test/property", 
                "/test/property/2",
                "/non/existent/property"
            );

            ResolverResult result = 
                awsSsmPropertyResolver.resolve(propertiesToResolve);

            assertTrue(result.hasResolvedProperties());
            assertTrue(result.hasUnresolvedProperties());

            assertEquals(
                "test.property.value", 
                result.findRequiredProperty("/test/property")
            );

            assertEquals(
                "test.property.2.value", 
                result.findRequiredProperty("/test/property/2")
            );

            assertTrue(result.unresolvedPropertyNames().contains("/non/existent/property"));
        }
    }

    @Nested
    class ResolveMethodWithVarArgsOverload {
        @Test
        @DisplayName("should throw when propertyNames argument is null")
        void test1() {
            AwsSsmResolver awsSsmPropertyResolver = 
                new AwsSsmResolver(new StubSsmClient());

            assertThrows(IllegalArgumentException.class, () -> {
                awsSsmPropertyResolver.resolve((String[])null);
            });
        }

        @Test
        @DisplayName("should throw when propertyNames argument is empty")
        void test2() {
            AwsSsmResolver awsSsmPropertyResolver = 
                new AwsSsmResolver(new StubSsmClient());
                
            assertThrows(IllegalArgumentException.class, () -> {
                awsSsmPropertyResolver.resolve(new String[0]);
            });
        }

        @Test
        @DisplayName("should resolve all properties from AWS SSM")
        void test3() {
            AwsSsmResolver awsSsmPropertyResolver = 
                new AwsSsmResolver(new StubSsmClient());

            String[] propertiesToResolve = new String[] {
                "/test/property",
                "/test/property/2"
            };

            ResolverResult result = 
                awsSsmPropertyResolver.resolve(propertiesToResolve);

            assertTrue(result.hasResolvedProperties());
            assertFalse(result.hasUnresolvedProperties());

            assertTrue(result.resolvedPropertyNames().stream()
                .allMatch(resolved -> Arrays.asList(propertiesToResolve).contains(resolved))
            );

            assertEquals(
                "test.property.value", 
                result.findRequiredProperty("/test/property")
            );

            assertEquals(
                "test.property.2.value", 
                result.findRequiredProperty("/test/property/2")
            );
        }

        @Test
        @DisplayName(
            "should return result with resolved and unresolved properties from AWS SSM"
        )
        void test4() {
            // Only returns properties that starts with test/property
            SsmClient stubSsmClient = new StubSsmClient(
                p -> p.startsWith("/test/property") ? 
                    StubSsmClient.DEFAULT_DELEGATE.apply(p) : 
                    null
            );
            AwsSsmResolver awsSsmPropertyResolver = 
                new AwsSsmResolver(stubSsmClient);

            String[] propertiesToResolve = new String[] {
                "/test/property", 
                "/test/property/2",
                "/non/existent/property"
            };

            ResolverResult result = 
                awsSsmPropertyResolver.resolve(propertiesToResolve);

            assertTrue(result.hasResolvedProperties());
            assertTrue(result.hasUnresolvedProperties());

            assertEquals(
                "test.property.value", 
                result.findRequiredProperty("/test/property")
            );

            assertEquals(
                "test.property.2.value", 
                result.findRequiredProperty("/test/property/2")
            );

            assertTrue(result.unresolvedPropertyNames().contains("/non/existent/property"));
        }
    }

}
