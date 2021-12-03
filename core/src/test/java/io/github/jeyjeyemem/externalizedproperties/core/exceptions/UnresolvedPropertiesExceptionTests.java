package io.github.jeyjeyemem.externalizedproperties.core.exceptions;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class UnresolvedPropertiesExceptionTests {
    @Nested
    class Constructor {
        @Test
        @DisplayName("should set unresolved property name")
        public void test1() {
            String unresolvedPropertyName = "test.property";

            UnresolvedPropertiesException unresolvedPropertiesException = 
                new UnresolvedPropertiesException(
                    unresolvedPropertyName, 
                    "test.property cannot be resolved"
                );

            assertTrue(unresolvedPropertiesException.unresolvedPropertyNames().size() == 1);
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
        public void test2() {
            String unresolvedPropertyName = "test.property";

            UnresolvedPropertiesException unresolvedPropertiesException = 
                new UnresolvedPropertiesException(
                    unresolvedPropertyName, 
                    "test.property cannot be resolved",
                    new RuntimeException("cause")
                );

            assertTrue(unresolvedPropertiesException.unresolvedPropertyNames().size() == 1);
            assertEquals(
                unresolvedPropertyName, 
                unresolvedPropertiesException.unresolvedPropertyNames()
                    .stream()
                    .findFirst()
                    .orElse(null)
            );
        }@Test
        @DisplayName("should set unresolved property name")
        public void test3() {
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
        public void test4() {
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
        public void test1() {
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

            verifyUnmodifiableCollection(unmodifiableSet, () -> "this should throw");
        }
    }

    private <T> void verifyUnmodifiableCollection(
            Collection<T> setToVerify, 
            Supplier<T> itemSupplier
    ) {
        assertThrows(
            UnsupportedOperationException.class,
            () -> setToVerify.add(itemSupplier.get())
        );

        assertThrows(
            UnsupportedOperationException.class,
            () -> setToVerify.remove(itemSupplier.get())
        );

        assertThrows(
            UnsupportedOperationException.class,
            () -> setToVerify.addAll(
                Collections.singletonList(itemSupplier.get())
            )
        );

        assertThrows(
            UnsupportedOperationException.class,
            () -> setToVerify.clear()
        );

        assertThrows(
            UnsupportedOperationException.class,
            () -> setToVerify.removeAll(
                Collections.singletonList(itemSupplier.get())
            )
        );

        assertThrows(
            UnsupportedOperationException.class,
            () -> setToVerify.removeIf(r -> true)
        );

        assertThrows(
            UnsupportedOperationException.class,
            () -> setToVerify.retainAll(
                Collections.singletonList(itemSupplier.get())
            )
        );

        assertThrows(
            UnsupportedOperationException.class,
            () -> setToVerify.iterator().remove()
        );
    }
}
