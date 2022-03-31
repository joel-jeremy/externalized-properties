package io.github.jeyjeyemem.externalizedproperties.core.resolvers;

import io.github.jeyjeyemem.externalizedproperties.core.ResolverResult;
import io.github.jeyjeyemem.externalizedproperties.core.UnresolvedPropertiesException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ClasspathResolverTests {
    @Nested
    class ResolveMethod {
        @Test
        @DisplayName("should throw when property name argument is null or empty.")
        public void test1() {
            ClasspathResolver resolver = resolverToTest();
            assertThrows(
                IllegalArgumentException.class, 
                () -> resolver.resolve((String)null)
            );
        }

        @Test
        @DisplayName(
            "should return empty Optional when property name is not prefixed with classpath:."
        )
        public void test2() {
            ClasspathResolver resolver = resolverToTest();
            Optional<String> result = resolver.resolve("no.classpath.prefix");
            assertFalse(result.isPresent());
        }

        @Test
        @DisplayName("should resolve value from classpath resource.")
        public void test3() throws URISyntaxException, IOException {
            ClasspathResolver resolver = resolverToTest();
            String propertyName = "classpath:test.properties";
            Optional<String> result = resolver.resolve(propertyName);
            String fileContents = readFileContents(propertyName);
            assertTrue(result.isPresent());
            assertEquals(
                fileContents, 
                result.get()
            );
        }

        @Test
        @DisplayName("should throw when classpath resource cannot be found.")
        public void test4() {
            ClasspathResolver resolver = resolverToTest();
            assertThrows(
                UnresolvedPropertiesException.class, 
                () -> resolver.resolve("classpath:non.existent.resource")
            );
        }
    }

    @Nested
    class ResolveMethodWithVarArgsOverload {
        @Test
        @DisplayName("should throw when property names argument is null or empty.")
        public void test1() {
            ClasspathResolver resolver = resolverToTest();
            assertThrows(
                IllegalArgumentException.class, 
                () -> resolver.resolve((String[])null)
            );
        }

        @Test
        @DisplayName(
            "should return result with unresolved properties " + 
            "when property name is not prefixed with classpath:."
        )
        public void test2() {
            ClasspathResolver resolver = resolverToTest();

            String[] propertiesToResolve = new String[] {
                "no.classpath.prefix",
                "no.classpath.prefix.2"
            };

            ResolverResult result = resolver.resolve(propertiesToResolve);

            assertFalse(result.hasResolvedProperties());
            assertTrue(result.hasUnresolvedProperties());
            for (String propertyName : propertiesToResolve) {
                assertTrue(
                    result.unresolvedPropertyNames().contains(propertyName)
                );
            }
        }

        @Test
        @DisplayName("should resolve values from classpath resource.")
        public void test3() throws URISyntaxException, IOException {
            ClasspathResolver resolver = resolverToTest();

            String[] propertiesToResolve = new String[] {
                "classpath:test.properties",
                "classpath:test-2.properties"
            };

            ResolverResult result = resolver.resolve(propertiesToResolve);
            assertTrue(result.hasResolvedProperties());
            assertFalse(result.hasUnresolvedProperties());

            for (String propertyName : propertiesToResolve) {
                String fileContents = readFileContents(propertyName);
                assertEquals(
                    fileContents, 
                    result.findRequiredProperty(propertyName)
                );
            }
        }

        @Test
        @DisplayName("should throw when classpath resource cannot be found.")
        public void test4() {
            ClasspathResolver resolver = resolverToTest();
            String[] propertiesToResolve = new String[] {
                "classpath:test.properties",
                "classpath:non.existent.resource"
            };
            assertThrows(
                UnresolvedPropertiesException.class, 
                () -> resolver.resolve(propertiesToResolve)
            );
        }
    }

    @Nested
    class ResolveMethodWithCollectionOverload {
        @Test
        @DisplayName("should throw when property names argument is null or empty.")
        public void test1() {
            ClasspathResolver resolver = resolverToTest();
            assertThrows(
                IllegalArgumentException.class, 
                () -> resolver.resolve((List<String>)null)
            );
        }

        @Test
        @DisplayName(
            "should return result with unresolved properties " + 
            "when property name is not prefixed with classpath:."
        )
        public void test2() {
            ClasspathResolver resolver = resolverToTest();

            List<String> propertiesToResolve = Arrays.asList(
                "no.classpath.prefix",
                "no.classpath.prefix.2"
            );

            ResolverResult result = resolver.resolve(propertiesToResolve);

            assertFalse(result.hasResolvedProperties());
            assertTrue(result.hasUnresolvedProperties());
            for (String propertyName : propertiesToResolve) {
                assertTrue(
                    result.unresolvedPropertyNames().contains(propertyName)
                );
            }
        }

        @Test
        @DisplayName("should resolve values from classpath resource.")
        public void test3() throws URISyntaxException, IOException {
            ClasspathResolver resolver = resolverToTest();

            List<String> propertiesToResolve = Arrays.asList(
                "classpath:test.properties",
                "classpath:test-2.properties"
            );

            ResolverResult result = resolver.resolve(propertiesToResolve);
            assertTrue(result.hasResolvedProperties());
            assertFalse(result.hasUnresolvedProperties());

            for (String propertyName : propertiesToResolve) {
                String fileContents = readFileContents(propertyName);
                assertEquals(
                    fileContents, 
                    result.findRequiredProperty(propertyName)
                );
            }
        }

        @Test
        @DisplayName("should throw when classpath resource cannot be found.")
        public void test4() {
            ClasspathResolver resolver = resolverToTest();
            List<String> propertiesToResolve = Arrays.asList(
                "classpath:test.properties",
                "classpath:non.existent.resource"
            );
            assertThrows(
                UnresolvedPropertiesException.class, 
                () -> resolver.resolve(propertiesToResolve)
            );
        }
    }

    private String readFileContents(String path) 
            throws URISyntaxException, IOException 
    {
        Path filePath = Paths.get(
            getClass().getClassLoader().getResource(
                path.substring("classpath:".length())
            ).toURI()
        );
        return new String(Files.readAllBytes(filePath));
    }

    private ClasspathResolver resolverToTest() {
        return new ClasspathResolver();
    }
}
