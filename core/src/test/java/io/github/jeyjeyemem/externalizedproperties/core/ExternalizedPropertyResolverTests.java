package io.github.jeyjeyemem.externalizedproperties.core;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Collection;
import java.util.Collections;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ExternalizedPropertyResolverTests {
    @Nested
    class ResolveVarArgsMethod {
        @Test
        @DisplayName("should convert null to an empty collection.")
        public void test1() {
            AtomicReference<Collection<String>> propertyNamesCollectionRef = 
                new AtomicReference<>();
            
            ExternalizedPropertyResolver resolver = new ExternalizedPropertyResolver() {
                @Override
                public Optional<ResolvedProperty> resolve(String propertyName) {
                    return Optional.empty();
                }

                @Override
                public ExternalizedPropertyResolverResult resolve(Collection<String> propertyNames) {
                    // Track propertyName collection for assertion.
                    propertyNamesCollectionRef.set(propertyNames);
                    // Dummy result.
                    return new ExternalizedPropertyResolverResult(
                        Collections.emptyList(), 
                        Collections.emptyList()
                    );
                }
            };

            // This shall be converted to a empty collection
            // and delegate to ExternalizedPropertyResolver.resolve(Collection<String>).
            resolver.resolve((String[])null);

            assertNotNull(propertyNamesCollectionRef.get());
            assertTrue(propertyNamesCollectionRef.get().isEmpty());
        }
    }
}
