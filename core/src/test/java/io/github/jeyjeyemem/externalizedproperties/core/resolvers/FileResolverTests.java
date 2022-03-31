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

public class FileResolverTests {
    @Nested
    class ResolveMethod {
        @Test
        @DisplayName("should throw when property name argument is null or empty.")
        public void test1() {
            FileResolver resolver = resolverToTest();
            assertThrows(
                IllegalArgumentException.class, 
                () -> resolver.resolve((String)null)
            );
        }

        @Test
        @DisplayName(
            "should return empty Optional when property name is not prefixed with file:."
        )
        public void test2() {
            FileResolver resolver = resolverToTest();
            Optional<String> result = resolver.resolve("no.file.prefix");
            assertFalse(result.isPresent());
        }

        @Test
        @DisplayName("should resolve value from file.")
        public void test3() throws URISyntaxException, IOException {
            FileResolver resolver = resolverToTest();
            Path testPropertiesFile = Paths.get(
                getClass().getClassLoader().getResource("test.properties").toURI()
            );
            String propertyName = "file:" + testPropertiesFile.toAbsolutePath().toString();
            Optional<String> result = resolver.resolve(propertyName);
            String fileContents = readFileContents(propertyName);
            assertTrue(result.isPresent());
            assertEquals(
                fileContents, 
                result.get()
            );
        }

        @Test
        @DisplayName("should throw when file cannot be found.")
        public void test4() {
            FileResolver resolver = resolverToTest();
            assertThrows(
                UnresolvedPropertiesException.class, 
                () -> resolver.resolve("file:non.existent.file")
            );
        }
    }

    @Nested
    class ResolveMethodWithVarArgsOverload {
        @Test
        @DisplayName("should throw when property names argument is null or empty.")
        public void test1() {
            FileResolver resolver = resolverToTest();
            assertThrows(
                IllegalArgumentException.class, 
                () -> resolver.resolve((String[])null)
            );
        }

        @Test
        @DisplayName(
            "should return result with unresolved properties " + 
            "when property name is not prefixed with file:."
        )
        public void test2() {
            FileResolver resolver = resolverToTest();
            String[] propertiesToResolve = new String[] {
                "no.file.prefix",
                "no.file.prefix.2"
            };

            ResolverResult result = resolver.resolve(propertiesToResolve);

            assertFalse(result.hasResolvedProperties());
            assertTrue(result.hasUnresolvedProperties());
        }

        @Test
        @DisplayName("should resolve values from file.")
        public void test3() throws URISyntaxException, IOException {
            FileResolver resolver = resolverToTest();
            Path testPropertiesFile = Paths.get(
                getClass().getClassLoader().getResource("test.properties").toURI()
            );
            Path test2PropertiesFile = Paths.get(
                getClass().getClassLoader().getResource("test-2.properties").toURI()
            );
            String[] propertiesToResolve = new String[] {
                "file:" + testPropertiesFile.toAbsolutePath().toString(),
                "file:" + test2PropertiesFile.toAbsolutePath().toString(),

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
        @DisplayName("should throw when file cannot be found.")
        public void test4() throws URISyntaxException {
            FileResolver resolver = resolverToTest();
            Path testProperties = Paths.get(
                getClass().getClassLoader().getResource("test.properties").toURI()
            );
            String[] propertiesToResolve = new String[] {
                "file:" + testProperties.toAbsolutePath().toString(),
                "file:/non.existent.resource"
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
            FileResolver resolver = resolverToTest();
            assertThrows(
                IllegalArgumentException.class, 
                () -> resolver.resolve((List<String>)null)
            );
        }

        @Test
        @DisplayName(
            "should return result with unresolved properties " + 
            "when property name is not prefixed with file:."
        )
        public void test2() {
            FileResolver resolver = resolverToTest();
            List<String> propertiesToResolve = Arrays.asList(
                "no.file.prefix",
                "no.file.prefix.2"
            );

            ResolverResult result = resolver.resolve(propertiesToResolve);

            assertFalse(result.hasResolvedProperties());
            assertTrue(result.hasUnresolvedProperties());
        }

        @Test
        @DisplayName("should resolve values from file.")
        public void test3() throws URISyntaxException, IOException {
            FileResolver resolver = resolverToTest();
            Path testPropertiesFile = Paths.get(
                getClass().getClassLoader().getResource("test.properties").toURI()
            );
            Path test2PropertiesFile = Paths.get(
                getClass().getClassLoader().getResource("test-2.properties").toURI()
            );
            List<String> propertiesToResolve = Arrays.asList(
                "file:" + testPropertiesFile.toAbsolutePath().toString(),
                "file:" + test2PropertiesFile.toAbsolutePath().toString()
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
        @DisplayName("should throw when file cannot be found.")
        public void test4() throws URISyntaxException {
            FileResolver resolver = resolverToTest();
            Path testProperties = Paths.get(
                getClass().getClassLoader().getResource("test.properties").toURI()
            );
            List<String> propertiesToResolve = Arrays.asList(
                "file:" + testProperties.toAbsolutePath().toString(),
                "file:/non.existent.resource"
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
        Path filePath = Paths.get(path.substring("file:".length()));
        return new String(Files.readAllBytes(filePath));
    }

    private FileResolver resolverToTest() {
        return new FileResolver();
    }
}
