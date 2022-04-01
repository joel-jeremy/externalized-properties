package io.github.jeyjeyemem.externalizedproperties.core;

import io.github.jeyjeyemem.externalizedproperties.core.testentities.Asserts;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ResolverTests {
    @Nested
    class ResolveMethodWithVarArgsOverload {
        @Test
        @DisplayName("should convert null to an empty collection.")
        public void test1() {
            AtomicReference<Collection<String>> propertyNamesCollectionRef = 
                new AtomicReference<>();
            
            Resolver resolver = new Resolver() {
                @Override
                public Optional<String> resolve(String propertyName) {
                    return Optional.empty();
                }

                @Override
                public ResolverResult resolve(Collection<String> propertyNames) {
                    // Track propertyName collection for assertion.
                    propertyNamesCollectionRef.set(propertyNames);
                    // Empty result.
                    return ResolverResult.EMPTY;
                }
            };

            // This shall be converted to a empty collection
            // and delegate to Resolver.resolve(Collection<String>).
            resolver.resolve((String[])null);

            assertNotNull(propertyNamesCollectionRef.get());
            assertTrue(propertyNamesCollectionRef.get().isEmpty());
        }
    }

    @Nested
    class ResultTests {
        @Nested
        class BuilderMethodWithVarArgsOverload {
            @Test
            @DisplayName("should not return null")
            public void test1() {
                ResolverResult.Builder resultBuilder = 
                    ResolverResult.builder("test.property");

                assertNotNull(resultBuilder);
            }

            @Test
            @DisplayName(
                "should throw when request proeprty names argument is null or empty"
            )
            public void test2() {
                assertThrows(
                    IllegalArgumentException.class,
                    () -> ResolverResult.builder(
                        (String[])null
                    )
                );
                
                assertThrows(
                    IllegalArgumentException.class,
                    () -> ResolverResult.builder(
                        new String[0]
                    )
                );
            }
        }

        @Nested
        class BuilderMethod {
            @Test
            @DisplayName("should not return null")
            public void test1() {
                ResolverResult.Builder resultBuilder = 
                    ResolverResult.builder(
                        Arrays.asList("test.property")
                    );

                assertNotNull(resultBuilder);
            }

            @Test
            @DisplayName(
                "should throw when request proeprty names argument is null or empty"
            )
            public void test2() {
                assertThrows(
                    IllegalArgumentException.class,
                    () -> ResolverResult.builder(
                        (Collection<String>)null
                    )
                );

                assertThrows(
                    IllegalArgumentException.class,
                    () -> ResolverResult.builder(
                        Collections.emptyList()
                    )
                );
            }
        }

        @Nested
        class ResolvedPropertiesMethod {
            @Test
            @DisplayName("should not return null")
            public void test1() {
                Map<String, String> requestedProperties = new HashMap<>();
                requestedProperties.put("test.property.1", "test.property.value.1");
                requestedProperties.put("test.property.2", "test.property.value.2");

                ResolverResult.Builder resultBuilder = 
                    ResolverResult.builder(
                        requestedProperties.keySet()
                    );

                resultBuilder.addAll(requestedProperties);

                ResolverResult result = resultBuilder.build();

                assertNotNull(result.resolvedProperties());
            }

            @Test
            @DisplayName("should return map with all resolved properties")
            public void test2() {
                Map<String, String> requestedProperties = new HashMap<>();
                requestedProperties.put("test.property.1", "test.property.value.1");
                requestedProperties.put("test.property.2", "test.property.value.2");

                ResolverResult.Builder resultBuilder = 
                    ResolverResult.builder(
                        requestedProperties.keySet()
                    );

                resultBuilder.addAll(requestedProperties);

                ResolverResult result = resultBuilder.build();

                assertEquals(requestedProperties, result.resolvedProperties());
            }

            @Test
            @DisplayName("should throw when resolved properties map is modified")
            public void test3() {
                Map<String, String> requestedProperties = new HashMap<>();
                requestedProperties.put("test.property.1", "test.property.value.1");
                requestedProperties.put("test.property.2", "test.property.value.2");

                ResolverResult.Builder resultBuilder = 
                    ResolverResult.builder(
                        requestedProperties.keySet()
                    );

                resultBuilder.addAll(requestedProperties);

                ResolverResult result = resultBuilder.build();

                Map<String, String> resolvedProperties = result.resolvedProperties();

                Asserts.assertUnmodifiableMap(
                    resolvedProperties, 
                    () -> "Key - this should throw", 
                    k -> "Value - this should throw"
                );

                Asserts.assertUnmodifiableCollection(
                    resolvedProperties.entrySet(), 
                    () -> resolvedProperties.entrySet().stream()
                        .findFirst()
                        .get()
                );
                Asserts.assertUnmodifiableCollection(
                    resolvedProperties.keySet(), 
                    () -> "this should throw"
                );
                Asserts.assertUnmodifiableCollection(
                    resolvedProperties.values(), 
                    () -> "this should throw"
                );
            }
        }

        @Nested
        class FindRequiredPropertyMethod {
            @Test
            @DisplayName("should return resolved property")
            public void test1() {
                Map<String, String> requestedProperties = new HashMap<>();
                requestedProperties.put("test.property.1", "test.property.value.1");

                ResolverResult.Builder resultBuilder = 
                    ResolverResult.builder(
                        requestedProperties.keySet()
                    );

                resultBuilder.addAll(requestedProperties);

                ResolverResult result = resultBuilder.build();

                String resolved = result.findRequiredProperty("test.property.1");

                assertNotNull(resolved);
                assertEquals(requestedProperties.get("test.property.1"), resolved);
            }

            @Test
            @DisplayName("should throw when property cannot be found")
            public void test2() {
                Map<String, String> requestedProperties = new HashMap<>();
                requestedProperties.put("test.property.1", "test.property.value.1");

                ResolverResult.Builder resultBuilder = 
                    ResolverResult.builder(
                        requestedProperties.keySet()
                    );

                resultBuilder.addAll(requestedProperties);

                ResolverResult result = resultBuilder.build();

                assertThrows(
                    UnresolvedPropertiesException.class,
                    () -> result.findRequiredProperty("unresolved")  
                );
            }
        }

        @Nested
        class FindResolvedPropertyMethod {
            @Test
            @DisplayName("should return resolved property")
            public void test1() {
                Map<String, String> requestedProperties = new HashMap<>();
                requestedProperties.put("test.property.1", "test.property.value.1");

                ResolverResult.Builder resultBuilder = 
                    ResolverResult.builder(
                        requestedProperties.keySet()
                    );

                resultBuilder.addAll(requestedProperties);

                ResolverResult result = resultBuilder.build();

                Optional<String> resolved = result.findResolvedProperty("test.property.1");

                assertNotNull(resolved);
                assertTrue(resolved.isPresent());
                assertEquals(requestedProperties.get("test.property.1"), resolved.get());
            }

            @Test
            @DisplayName("should return empty Optional when property cannot be found")
            public void test2() {
                Map<String, String> requestedProperties = new HashMap<>();
                requestedProperties.put("test.property.1", "test.property.value.1");

                ResolverResult.Builder resultBuilder = 
                    ResolverResult.builder(
                        requestedProperties.keySet()
                    );

                resultBuilder.addAll(requestedProperties);

                ResolverResult result = resultBuilder.build();

                Optional<String> property = result.findResolvedProperty("unresolved");

                assertNotNull(property);
                assertFalse(property.isPresent());
            }
        }

        @Nested
        class HasResolvedPropertiesMethod {
            @Test
            @DisplayName("should return true when there are any resolved properties")
            public void test1() {
                Map<String, String> requestedProperties = new HashMap<>();
                requestedProperties.put("test.property.1", "test.property.value.1");

                ResolverResult.Builder resultBuilder = 
                    ResolverResult.builder(
                        requestedProperties.keySet()
                    );

                resultBuilder.addAll(requestedProperties);

                ResolverResult result = resultBuilder.build();

                assertTrue(result.hasResolvedProperties());
            }

            @Test
            @DisplayName("should return false when there are no resolved properties")
            public void test2() {
                Map<String, String> requestedProperties = new HashMap<>();
                requestedProperties.put("test.property.1", "test.property.value.1");

                ResolverResult.Builder resultBuilder = 
                    ResolverResult.builder(
                        requestedProperties.keySet()
                    );

                // No resolved properties added to result.

                ResolverResult result = resultBuilder.build();

                assertFalse(result.hasResolvedProperties());
            }
        }

        @Nested
        class HasUnresolvedPropertiesMethod {
            @Test
            @DisplayName("should return true when there are any unresolved properties")
            public void test1() {
                Map<String, String> requestedProperties = new HashMap<>();
                requestedProperties.put("test.property.1", "test.property.value.1");

                ResolverResult.Builder resultBuilder = 
                    ResolverResult.builder(
                        requestedProperties.keySet()
                    );

                // No resolved properties added to result.

                ResolverResult result = resultBuilder.build();

                assertTrue(result.hasUnresolvedProperties());
            }

            @Test
            @DisplayName("should return false when there are no unresolved properties")
            public void test2() {
                Map<String, String> requestedProperties = new HashMap<>();
                requestedProperties.put("test.property.1", "test.property.value.1");

                ResolverResult.Builder resultBuilder = 
                    ResolverResult.builder(
                        requestedProperties.keySet()
                    );

                resultBuilder.addAll(requestedProperties);

                ResolverResult result = resultBuilder.build();

                assertFalse(result.hasUnresolvedProperties());
            }
        }

        @Nested
        class ResolvedPropertyNamesMethod {
            @Test
            @DisplayName("should return names of resolved properties")
            public void test1() {
                Map<String, String> requestedProperties = new HashMap<>();
                requestedProperties.put("test.property.1", "test.property.value.1");
                requestedProperties.put("test.property.2", "test.property.value.2");

                ResolverResult.Builder resultBuilder = 
                    ResolverResult.builder(
                        requestedProperties.keySet()
                    );

                resultBuilder.addAll(requestedProperties);

                ResolverResult result = resultBuilder.build();

                assertTrue(result.resolvedPropertyNames().contains("test.property.1"));
                assertTrue(result.resolvedPropertyNames().contains("test.property.2"));
            }

            @Test
            @DisplayName("should not return names of unresolved properties")
            public void test2() {
                Map<String, String> requestedProperties = new HashMap<>();
                requestedProperties.put("test.property.1", "test.property.value.1");
                requestedProperties.put("test.property.2", "test.property.value.2");

                ResolverResult.Builder resultBuilder = 
                    ResolverResult.builder(
                        requestedProperties.keySet()
                    );

                // No resolved properties added to result.

                ResolverResult result = resultBuilder.build();

                assertFalse(result.resolvedPropertyNames().contains("test.property.1"));
                assertFalse(result.resolvedPropertyNames().contains("test.property.2"));
            }

            @Test
            @DisplayName("should throw when resolved properties set is modified")
            public void test3() {
                Map<String, String> requestedProperties = new HashMap<>();
                requestedProperties.put("test.property.1", "test.property.value.1");
                requestedProperties.put("test.property.2", "test.property.value.2");

                ResolverResult.Builder resultBuilder = 
                    ResolverResult.builder(
                        requestedProperties.keySet()
                    );

                resultBuilder.addAll(requestedProperties);

                ResolverResult result = resultBuilder.build();

                Set<String> resolvedPropertyNames = result.resolvedPropertyNames();

                Asserts.assertUnmodifiableCollection(
                    resolvedPropertyNames, 
                    () -> "this should throw"
                );
            }
        }

        @Nested
        class UnresolvedPropertyNamesMethod {
            @Test
            @DisplayName("should return names of unresolved properties")
            public void test1() {
                Map<String, String> requestedProperties = new HashMap<>();
                requestedProperties.put("test.property.1", "test.property.value.1");
                requestedProperties.put("test.property.2", "test.property.value.2");

                ResolverResult.Builder resultBuilder = 
                    ResolverResult.builder(
                        requestedProperties.keySet()
                    );

                // No resolved properties added to result.

                ResolverResult result = resultBuilder.build();

                assertTrue(result.unresolvedPropertyNames().contains("test.property.1"));
                assertTrue(result.unresolvedPropertyNames().contains("test.property.2"));
            }

            @Test
            @DisplayName("should not return names of resolved properties")
            public void test2() {
                Map<String, String> requestedProperties = new HashMap<>();
                requestedProperties.put("test.property.1", "test.property.value.1");
                requestedProperties.put("test.property.2", "test.property.value.2");

                ResolverResult.Builder resultBuilder = 
                    ResolverResult.builder(
                        requestedProperties.keySet()
                    );

                resultBuilder.addAll(requestedProperties);

                ResolverResult result = resultBuilder.build();

                assertFalse(result.unresolvedPropertyNames().contains("test.property.1"));
                assertFalse(result.unresolvedPropertyNames().contains("test.property.2"));
            }

            @Test
            @DisplayName("should throw when resolved properties set is modified")
            public void test3() {
                Map<String, String> requestedProperties = new HashMap<>();
                requestedProperties.put("test.property.1", "test.property.value.1");
                requestedProperties.put("test.property.2", "test.property.value.2");

                ResolverResult.Builder resultBuilder = 
                    ResolverResult.builder(
                        requestedProperties.keySet()
                    );

                resultBuilder.addAll(requestedProperties);

                ResolverResult result = resultBuilder.build();

                Set<String> resolvedPropertyNames = result.unresolvedPropertyNames();

                Asserts.assertUnmodifiableCollection(
                    resolvedPropertyNames, 
                    () -> "this should throw"
                );
            }
        }
    }

    @Nested
    class ResultBuilderTests {
        @Nested
        class AddMethod {
            @Test
            @DisplayName("should throw when property name argument is null or empty")
            public void test1() {
                ResolverResult.Builder resultBuilder = 
                    ResolverResult.builder(
                        Arrays.asList("test.property")
                    );

                // Null.
                assertThrows(
                    IllegalArgumentException.class, 
                    () -> resultBuilder.add(null, "test.property.value")
                );

                // Empty.
                assertThrows(
                    IllegalArgumentException.class, 
                    () -> resultBuilder.add("", "test.property.value")
                );
            }

            @Test
            @DisplayName("should throw when resolved property value argument is null")
            public void test2() {
                ResolverResult.Builder resultBuilder = 
                    ResolverResult.builder(
                        Arrays.asList("test.property")
                    );

                assertThrows(
                    IllegalArgumentException.class, 
                    () -> resultBuilder.add("test.property", null)
                );
            }

            @Test
            @DisplayName("should add property to result's resolved properties")
            public void test3() {
                ResolverResult.Builder resultBuilder = 
                    ResolverResult.builder(
                        Arrays.asList("test.property")
                    );
                
                resultBuilder.add("test.property", "test.property.value");

                ResolverResult result = resultBuilder.build();
                
                assertTrue(result.resolvedPropertyNames().contains("test.property"));
            }

            @Test
            @DisplayName("should add missing property to result's unresolved properties")
            public void test4() {
                Map<String, String> requestedProperties = new HashMap<>();
                requestedProperties.put("test.property.1", "test.property.value.1");
                requestedProperties.put("test.property.2", "test.property.value.2");
                
                ResolverResult.Builder resultBuilder = 
                    ResolverResult.builder(
                        requestedProperties.keySet()
                    );
                
                resultBuilder.add("test.property.1", "test.property.value.1");

                ResolverResult result = resultBuilder.build();
                
                assertTrue(result.resolvedPropertyNames().contains("test.property.1"));
                // test.property.2 was not resolved.
                assertFalse(result.resolvedPropertyNames().contains("test.property.2"));
                assertTrue(result.unresolvedPropertyNames().contains("test.property.2"));
            }
        }
    }

    @Nested
    class AddAllMethod {
        @Test
        @DisplayName("should throw when resolved proeprties by name map argument is null")
        public void test1() {
            ResolverResult.Builder resultBuilder = 
                ResolverResult.builder(
                    Arrays.asList("test.property")
                );

            // Null.
            assertThrows(
                IllegalArgumentException.class, 
                () -> resultBuilder.addAll(null)
            );

            // Empty.
            assertThrows(
                IllegalArgumentException.class, 
                () -> resultBuilder.add("", "test.property.value")
            );
        }

        @Test
        @DisplayName("should add properties to result's resolved properties")
        public void test2() {
            Map<String, String> requestedProperties = new HashMap<>();
            requestedProperties.put("test.property.1", "test.property.value.1");
            requestedProperties.put("test.property.2", "test.property.value.2");
            requestedProperties.put("test.property.3", "test.property.value.3");
            
            ResolverResult.Builder resultBuilder = 
                ResolverResult.builder(
                    requestedProperties.keySet()
                );

            Map<String, String> resolved = new HashMap<>();
            resolved.put("test.property.1", "test.property.value.1");
            resolved.put("test.property.2", "test.property.value.2");
            resolved.put("test.property.3", "test.property.value.3");
            
            resultBuilder.addAll(resolved);

            ResolverResult result = resultBuilder.build();
            
            assertTrue(result.resolvedPropertyNames().contains("test.property.1"));
            assertTrue(result.resolvedPropertyNames().contains("test.property.2"));
            assertTrue(result.resolvedPropertyNames().contains("test.property.3"));
        }

        @Test
        @DisplayName("should add missing properties to result's unresolved properties")
        public void test3() {
            Map<String, String> requestedProperties = new HashMap<>();
            requestedProperties.put("test.property.1", "test.property.value.1");
            requestedProperties.put("test.property.2", "test.property.value.2");
            requestedProperties.put("test.property.3", "test.property.value.3");

            ResolverResult.Builder resultBuilder = 
                ResolverResult.builder(
                    requestedProperties.keySet()
                );

            // test.property.3 was not resolved.
            Map<String, String> resolved = new HashMap<>();
            resolved.put("test.property.1", "test.property.value.1");
            resolved.put("test.property.2", "test.property.value.2");
            
            resultBuilder.addAll(resolved);

            ResolverResult result = resultBuilder.build();
            
            assertTrue(result.resolvedPropertyNames().contains("test.property.1"));
            assertTrue(result.resolvedPropertyNames().contains("test.property.2"));
            // Not in resolved properties.
            assertFalse(result.resolvedPropertyNames().contains("test.property.3"));
            assertTrue(result.unresolvedPropertyNames().contains("test.property.3"));
        }
    }
}
