package io.github.joeljeremy7.externalizedproperties.core;

import io.github.joeljeremy7.externalizedproperties.core.testentities.Asserts;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;

public class UnresolvedPropertiesExceptionTests {
    @Nested
    class Constructor {
        @Test
        @DisplayName("should set unresolved property name")
        void test1() {
            String unresolvedPropertyName = "test.property";

            UnresolvedPropertiesException unresolvedPropertiesException = 
                new UnresolvedPropertiesException(
                    unresolvedPropertyName, 
                    "test.property cannot be resolved"
                );

            assertEquals(1, unresolvedPropertiesException.unresolvedPropertyNames().size());
            assertEquals(
                unresolvedPropertyName, 
                unresolvedPropertiesException.unresolvedPropertyNames()
                    .stream()
                    .findFirst()
                    .orElse(null)
            );
        }

        @Test
        @DisplayName("should set unresolved property name")
        void test2() {
            String unresolvedPropertyName = "test.property";

            UnresolvedPropertiesException unresolvedPropertiesException = 
                new UnresolvedPropertiesException(
                    unresolvedPropertyName, 
                    "test.property cannot be resolved",
                    new RuntimeException("cause")
                );

            assertEquals(1, unresolvedPropertiesException.unresolvedPropertyNames().size());
            assertEquals(
                unresolvedPropertyName, 
                unresolvedPropertiesException.unresolvedPropertyNames()
                    .stream()
                    .findFirst()
                    .orElse(null)
            );
        }
        
        @Test
        @DisplayName("should set unresolved property name")
        void test3() {
            Set<String> unresolvedPropertyNames = new HashSet<>(Arrays.asList(
                "test.property.1",
                "test.property.2"
            ));

            UnresolvedPropertiesException unresolvedPropertiesException = 
                new UnresolvedPropertiesException(
                    unresolvedPropertyNames, 
                    "Properties cannot be resolved: " + unresolvedPropertyNames
                );

            assertEquals(
                unresolvedPropertyNames.size(),
                unresolvedPropertiesException.unresolvedPropertyNames().size()
            );
            assertIterableEquals(
                unresolvedPropertyNames, 
                unresolvedPropertiesException.unresolvedPropertyNames()
            );
        }

        @Test
        @DisplayName("should set unresolved property name")
        void test4() {
            Set<String> unresolvedPropertyNames = new HashSet<>(Arrays.asList(
                "test.property.1",
                "test.property.2"
            ));

            UnresolvedPropertiesException unresolvedPropertiesException = 
                new UnresolvedPropertiesException(
                    unresolvedPropertyNames, 
                    "Properties cannot be resolved: " + unresolvedPropertyNames,
                    new RuntimeException("cause")
                );

            assertEquals(
                unresolvedPropertyNames.size(),
                unresolvedPropertiesException.unresolvedPropertyNames().size()
            );
            assertIterableEquals(
                unresolvedPropertyNames, 
                unresolvedPropertiesException.unresolvedPropertyNames()
            );
        }
    }

    @Nested
    class UnresolvedPropertyNamesMethod {
        @Test
        @DisplayName("should return an unmodifiable set")
        void test1() {
            Set<String> unresolvedPropertyNames = new HashSet<>(Arrays.asList(
                "test.property.1",
                "test.property.2"
            ));

            UnresolvedPropertiesException unresolvedPropertiesException = 
                new UnresolvedPropertiesException(
                    unresolvedPropertyNames, 
                    "Properties cannot be resolved: " + unresolvedPropertyNames
                );


            Set<String> unmodifiableSet = 
                unresolvedPropertiesException.unresolvedPropertyNames();

            Asserts.assertUnmodifiableCollection(unmodifiableSet, () -> "this should throw");
        }
    }
}
