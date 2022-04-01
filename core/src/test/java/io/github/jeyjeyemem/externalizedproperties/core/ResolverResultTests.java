package io.github.jeyjeyemem.externalizedproperties.core;

import io.github.jeyjeyemem.externalizedproperties.core.testentities.Asserts;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ResolverResultTests {
    @Nested
    class BuilderMethod {
        @Test
        @DisplayName(
            "should throw when requested property names collection argument is null"
        )
        void test1() {
            assertThrows(
                IllegalArgumentException.class, 
                () -> ResolverResult.builder((Collection<String>)null)
            );
        }

        @Test
        @DisplayName("should throw when requested property names varargs argument is null")
        void test2() {
            assertThrows(
                IllegalArgumentException.class, 
                () -> ResolverResult.builder((String[])null)
            );
        }

        @Test
        @DisplayName("should never return null")
        void test3() {
            assertNotNull(ResolverResult.builder("property"));
        }
    }

    @Nested
    class BuilderTests {
        @Nested
        class AddMethod {
            @Test
            @DisplayName("should throw when property name argument is null")
            void test1() {
                String propertyName = null;
                String propertyValue = "property-value";

                ResolverResult.Builder builder = ResolverResult.builder(propertyName);

                assertThrows(
                    IllegalArgumentException.class, 
                    () -> builder.add(propertyName, propertyValue)
                );
            }

            @Test
            @DisplayName("should throw when property name argument is empty")
            void test2() {
                String propertyName = "";
                String propertyValue = "property-value";

                ResolverResult.Builder builder = ResolverResult.builder(propertyName);

                assertThrows(
                    IllegalArgumentException.class, 
                    () -> builder.add(propertyName, propertyValue)
                );
            }

            @Test
            @DisplayName("should throw when property value argument is empty")
            void test3() {
                String propertyName = "property";
                String propertyValue = null;

                ResolverResult.Builder builder = ResolverResult.builder(propertyName);

                assertThrows(
                    IllegalArgumentException.class, 
                    () -> builder.add(propertyName, propertyValue)
                );
            }

            @Test
            @DisplayName("should add property to resolved properties")
            void test4() {
                String propertyName = "property";
                String propertyValue = "property-value";

                ResolverResult.Builder builder = ResolverResult.builder(propertyName);

                builder.add(propertyName, propertyValue);

                ResolverResult result = builder.build();

                assertTrue(result.resolvedProperties().containsKey(propertyName));
                assertEquals(propertyValue, result.resolvedProperties().get(propertyName));
            }
        }

        @Nested
        class AddAllMethod {
            @Test
            @DisplayName("should throw when resolved properties map argument is null")
            void test1() {
                ResolverResult.Builder builder = ResolverResult.builder("property");

                assertThrows(
                    IllegalArgumentException.class, 
                    () -> builder.addAll(null)
                );
            }

            @Test
            @DisplayName("should throw when property name in map argument is null")
            void test2() {
                String propertyName = null;
                String propertyValue = "property-value";

                Map<String, String> props = 
                    Collections.singletonMap(propertyName, propertyValue);

                ResolverResult.Builder builder = ResolverResult.builder(propertyName);

                assertThrows(
                    IllegalArgumentException.class, 
                    () -> builder.addAll(props)
                );
            }

            @Test
            @DisplayName("should throw when property name in map argument is empty")
            void test3() {
                String propertyName = "";
                String propertyValue = "property-value";

                Map<String, String> props = 
                    Collections.singletonMap(propertyName, propertyValue);

                ResolverResult.Builder builder = ResolverResult.builder(propertyName);

                assertThrows(
                    IllegalArgumentException.class, 
                    () -> builder.addAll(props)
                );
            }

            @Test
            @DisplayName("should throw when property value in map argument is empty")
            void test4() {
                String propertyName = "property";
                String propertyValue = null;

                Map<String, String> props = 
                    Collections.singletonMap(propertyName, propertyValue);

                ResolverResult.Builder builder = ResolverResult.builder(propertyName);

                assertThrows(
                    IllegalArgumentException.class, 
                    () -> builder.addAll(props)
                );
            }

            @Test
            @DisplayName("should add property to resolved properties")
            void test5() {
                String propertyName = "property";
                String propertyValue = "property-value";

                Map<String, String> props = 
                    Collections.singletonMap(propertyName, propertyValue);

                ResolverResult.Builder builder = ResolverResult.builder(propertyName);

                builder.addAll(props);

                ResolverResult result = builder.build();

                assertTrue(result.resolvedProperties().containsKey(propertyName));
                assertEquals(propertyValue, result.resolvedProperties().get(propertyName));
            }
        }

        @Nested
        class MapMethod {
            @Test
            @DisplayName("should throw when map function argument is null")
            void test1() {
                ResolverResult.Builder builder = ResolverResult.builder("property");

                assertThrows(
                    IllegalArgumentException.class, 
                    () -> builder.map(null)
                );
            }

            @Test
            @DisplayName("should map property name to value")
            void test2() {
                String propertyName = "property";
                String propertyValue = "property-value";

                Map<String, String> props = 
                    Collections.singletonMap(propertyName, propertyValue);

                ResolverResult.Builder builder = ResolverResult.builder(propertyName);

                builder.map(props::get);

                ResolverResult result = builder.build();

                assertTrue(result.resolvedProperties().containsKey(propertyName));
                assertEquals(propertyValue, result.resolvedProperties().get(propertyName));
            }

            @Test
            @DisplayName(
                "should not add property to resolved properties " + 
                "when map function return value is null"
            )
            void test3() {
                String propertyName = "property";
                String propertyValue = "property-value";

                Map<String, String> props = 
                    Collections.singletonMap(propertyName, propertyValue);

                String unresolvedProperty = "unresolved-property";

                ResolverResult.Builder builder = ResolverResult.builder(
                    propertyName,
                    unresolvedProperty // unresolvedProperty is not in props map.
                );

                builder.map(props::get);

                ResolverResult result = builder.build();

                assertTrue(result.resolvedProperties().containsKey(propertyName));
                assertEquals(propertyValue, result.resolvedProperties().get(propertyName));

                // property should not be in result.
                assertTrue(result.unresolvedPropertyNames().contains(unresolvedProperty));
            }
        }

        @Nested
        class BuildMethod {
            @Test
            @DisplayName("should never return null")
            void test1() {
                assertNotNull(ResolverResult.builder("property").build());
            }
        }
    }

    @Nested
    class ResolvedPropertiesMethod {
        @Test
        @DisplayName("should never return null")
        void test1() {
            ResolverResult result = ResolverResult.builder("property").build();
            assertNotNull(result.resolvedProperties());
        }

        @Test
        @DisplayName("should contain the resolved properties")
        void test2() {
            String propertyName = "property";
            String propertyValue = "property-value";
            
            ResolverResult result = ResolverResult.builder(propertyName)
                .add(propertyName, propertyValue)
                .build();

            assertTrue(result.resolvedProperties().containsKey(propertyName));
            assertEquals(propertyValue, result.resolvedProperties().get(propertyName));
        }

        @Test
        @DisplayName("should not contain unresolved properties")
        void test3() {
            String propertyName = "property";
            
            ResolverResult result = ResolverResult.builder(propertyName).build();
            assertFalse(result.resolvedProperties().containsKey(propertyName));
        }

        @Test
        @DisplayName("should return an unmodifiable map")
        void test4() {
            String propertyName = "property";
            
            ResolverResult result = ResolverResult.builder(propertyName).build();

            Asserts.assertUnmodifiableMap(
                result.resolvedProperties(), 
                () -> "Key - this should throw",
                k -> "Value - thso should throw"
            );
        }
    }

    @Nested
    class ResolvedPropertyNamesMethod {
        @Test
        @DisplayName("should never return null")
        void test1() {
            ResolverResult result = ResolverResult.builder("property").build();
            assertNotNull(result.resolvedPropertyNames());
        }

        @Test
        @DisplayName("should contain resolved properties")
        void test2() {
            String propertyName = "property";
            String propertyValue = "property-value";
            
            ResolverResult result = ResolverResult.builder(propertyName)
                .add(propertyName, propertyValue)
                .build();

            assertTrue(result.resolvedPropertyNames().contains(propertyName));
        }

        @Test
        @DisplayName("should not contain unresolved properties")
        void test3() {
            String propertyName = "property";
            
            ResolverResult result = ResolverResult.builder(propertyName).build();
            assertFalse(result.resolvedPropertyNames().contains(propertyName));
        }

        @Test
        @DisplayName("should return an unmodifiable collection")
        void test4() {
            String propertyName = "property";
            
            ResolverResult result = ResolverResult.builder(propertyName).build();
            
            Asserts.assertUnmodifiableCollection(
                result.resolvedPropertyNames(), 
                () -> "this should throw"
            );
        }
    }

    @Nested
    class UnresolvedPropertyNamesMethod {
        @Test
        @DisplayName("should never return null")
        void test1() {
            ResolverResult result = ResolverResult.builder("property").build();
            assertNotNull(result.unresolvedPropertyNames());
        }

        @Test
        @DisplayName("should contain unresolved properties")
        void test2() {
            String propertyName = "property";
            
            // Nothing added.
            ResolverResult result = ResolverResult.builder(propertyName).build();
            assertTrue(result.unresolvedPropertyNames().contains(propertyName));
        }

        @Test
        @DisplayName("should not contain resolved properties")
        void test3() {
            String propertyName = "property";
            String propertyValue = "property-value";
            
            ResolverResult result = ResolverResult.builder(propertyName)
                .add(propertyName, propertyValue)
                .build();
            
            assertFalse(result.unresolvedPropertyNames().contains(propertyName));
        }

        @Test
        @DisplayName("should return an unmodifiable collection")
        void test4() {
            String propertyName = "property";
            
            // Nothing added.
            ResolverResult result = ResolverResult.builder(propertyName).build();
            
            Asserts.assertUnmodifiableCollection(
                result.unresolvedPropertyNames(), 
                () -> "this should throw"
            );
        }
    }

    @Nested
    class HasResolvedPropertiesMethod {
        @Test
        @DisplayName("should return true when result contains resolved properties")
        void test1() {
            String propertyName = "property";
            String propertyValue = "property-value";

            ResolverResult result = ResolverResult.builder(propertyName)
                .add(propertyName, propertyValue)
                .build();
            
            assertTrue(result.hasResolvedProperties());
        }

        @Test
        @DisplayName("should return false when result does not contain resolved properties")
        void test2() {
            ResolverResult result = ResolverResult.builder("property").build();
            assertFalse(result.hasResolvedProperties());
        }
    }

    @Nested
    class HasUnresolvedPropertiesMethod {
        @Test
        @DisplayName("should return true when result contains unresolved properties")
        void test1() {
            ResolverResult result = ResolverResult.builder("property").build();
            assertTrue(result.hasUnresolvedProperties());
        }

        @Test
        @DisplayName("should return false when result does not contain unresolved properties")
        void test2() {
            String propertyName = "property";
            String propertyValue = "property-value";

            ResolverResult result = ResolverResult.builder(propertyName)
                .add(propertyName, propertyValue)
                .build();
            
            assertFalse(result.hasUnresolvedProperties());
        }
    }

    @Nested
    class FindResolvedPropertyMethod {
        @Test
        @DisplayName("should return resolved property")
        void test1() {
            String propertyName = "property";
            String propertyValue = "property-value";

            ResolverResult result = ResolverResult.builder(propertyName)
                .add(propertyName, propertyValue)
                .build();

            Optional<String> prop = result.findResolvedProperty(propertyName);
            assertTrue(prop.isPresent());
            assertEquals(propertyValue, prop.get());
        }

        @Test
        @DisplayName(
            "should return empty Optional when requested property was not resolved"
        )
        void test2() {
            String propertyName = "property";

            ResolverResult result = ResolverResult.builder(propertyName).build();

            Optional<String> prop = result.findResolvedProperty(propertyName);
            assertFalse(prop.isPresent());
        }
    }

    @Nested
    class FindRequiredPropertyMethod {
        @Test
        @DisplayName("should return required property")
        void test1() {
            String propertyName = "property";
            String propertyValue = "property-value";

            ResolverResult result = ResolverResult.builder(propertyName)
                .add(propertyName, propertyValue)
                .build();

            String prop = result.findRequiredProperty(propertyName);
            assertEquals(propertyValue, prop);
        }

        @Test
        @DisplayName(
            "should throw when required property was not resolved"
        )
        void test2() {
            String propertyName = "property";

            ResolverResult result = ResolverResult.builder(propertyName).build();

            assertThrows(
                UnresolvedPropertiesException.class, 
                () -> result.findRequiredProperty(propertyName)
            );
        }
    }
}
