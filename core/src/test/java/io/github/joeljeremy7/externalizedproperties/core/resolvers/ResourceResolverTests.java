package io.github.joeljeremy7.externalizedproperties.core.resolvers;

import io.github.joeljeremy7.externalizedproperties.core.ExternalizedProperties;
import io.github.joeljeremy7.externalizedproperties.core.ExternalizedPropertiesException;
import io.github.joeljeremy7.externalizedproperties.core.ExternalizedProperty;
import io.github.joeljeremy7.externalizedproperties.core.InvocationContext;
import io.github.joeljeremy7.externalizedproperties.core.Resolver;
import io.github.joeljeremy7.externalizedproperties.core.resolvers.ResourceResolver.PropertiesReader;
import io.github.joeljeremy7.externalizedproperties.core.resolvers.ResourceResolver.ResourceReader;
import io.github.joeljeremy7.externalizedproperties.core.testentities.resourcereaders.JsonReader;
import io.github.joeljeremy7.externalizedproperties.core.testentities.resourcereaders.XmlReader;
import io.github.joeljeremy7.externalizedproperties.core.testentities.resourcereaders.YamlReader;
import io.github.joeljeremy7.externalizedproperties.core.testfixtures.InvocationContextUtils;
import io.github.joeljeremy7.externalizedproperties.core.testfixtures.InvocationContextUtils.InvocationContextTestFactory;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;
import org.junit.jupiter.params.provider.ArgumentsSource;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ResourceResolverTests {
    private static final InvocationContextTestFactory<ProxyInterface> INVOCATION_CONTEXT_FACTORY =
        InvocationContextUtils.testFactory(ProxyInterface.class);
    private static final ResourceReader PROPERTIES_READER = new PropertiesReader();
    private static final ResourceReader JSON_READER = new JsonReader();
    private static final ResourceReader YAML_READER = new YamlReader();
    private static final ResourceReader XML_READER = new XmlReader();
    
    @Nested
    class FromUrlFactoryMethod {
        @Test
        @DisplayName("should throw when url argument is null")
        void urlTest1() {
            assertThrows(
                IllegalArgumentException.class, 
                () -> ResourceResolver.fromUrl(null)
            );
        }

        @Test
        @DisplayName("should not return null")
        void urlTest2() throws IOException {
            ResourceResolver resolver = ResourceResolver.fromUrl(
                classpathResource("/test.properties")
            );

            assertNotNull(resolver);
        }

        @Test
        @DisplayName("should throw when url argument is null")
        void urlAndReaderOverloadTest1() {
            assertThrows(
                IllegalArgumentException.class, 
                () -> ResourceResolver.fromUrl(null, PROPERTIES_READER)
            );
        }

        @Test
        @DisplayName("should throw when reader argument is null")
        void urlAndReaderOverloadTest2() {
            URL testPropertiesUrl = classpathResource("/test.properties");

            assertThrows(
                IllegalArgumentException.class, 
                () -> ResourceResolver.fromUrl(
                    testPropertiesUrl, 
                    null
                )
            );
        }

        @Test
        @DisplayName("should never return null")
        void urlAndReaderOverloadTest3() throws IOException {
            ResourceResolver resolver = ResourceResolver.fromUrl(
                classpathResource("/test.properties"), 
                PROPERTIES_READER
            );

            assertNotNull(resolver);
        }
    }

    @Nested
    class FromUriFactoryMethod {
        @Test
        @DisplayName("should throw when uri argument is null")
        void uriTest1() {
            assertThrows(
                IllegalArgumentException.class, 
                () -> ResourceResolver.fromUri(null)
            );
        }

        @Test
        @DisplayName("should throw when URI resource does not exist")
        void uriTest2() {
            assertThrows(
                ExternalizedPropertiesException.class, 
                () -> ResourceResolver.fromUri(
                    URI.create("file://non.existent.properties")
                )
            );
        }

        @Test
        @DisplayName("should throw when URI is malformed")
        void uriTest3() {
            assertThrows(
                UncheckedIOException.class, 
                () -> ResourceResolver.fromUri(
                    URI.create("notsupported://malformed")
                )
            );
        }

        @Test
        @DisplayName("should not return null")
        void uriTest4() throws IOException, URISyntaxException {
            ResourceResolver resolver = ResourceResolver.fromUri(
                classpathResourceAsUri("/test.properties")
            );

            assertNotNull(resolver);
        }

        @Test
        @DisplayName("should throw when uri argument is null")
        void uriAndReaderOverloadTest1() {
            assertThrows(
                IllegalArgumentException.class, 
                () -> ResourceResolver.fromUri(null, PROPERTIES_READER)
            );
        }

        @Test
        @DisplayName("should throw when reader argument is null")
        void uriAndReaderOverloadTest2() throws URISyntaxException {
            URI testPropertiesUri = classpathResourceAsUri("/test.properties");

            assertThrows(
                IllegalArgumentException.class, 
                () -> ResourceResolver.fromUri(
                    testPropertiesUri, 
                    null
                )
            );
        }

        @Test
        @DisplayName("should throw when URI resource does not exist")
        void uriAndReaderOverloadTest3() {
            assertThrows(
                ExternalizedPropertiesException.class, 
                () -> ResourceResolver.fromUri(
                    URI.create("file://non.existent.properties"), 
                    PROPERTIES_READER
                )
            );
        }

        @Test
        @DisplayName("should throw when URI is malformed")
        void uriAndReaderOverloadTest4() {
            assertThrows(
                UncheckedIOException.class, 
                () -> ResourceResolver.fromUri(
                    URI.create("notsupported://malformed"),
                    PROPERTIES_READER
                )
            );
        }

        @Test
        @DisplayName("should not return null")
        void uriAndReaderOverloadTest5() throws IOException, URISyntaxException {
            ResourceResolver resolver = ResourceResolver.fromUri(
                classpathResourceAsUri("/test.properties"), 
                PROPERTIES_READER
            );

            assertNotNull(resolver);
        }
    }

    @Nested
    class FromPathFactoryMethod {
        @Test
        @DisplayName("should throw when path argument is null")
        void pathTest1() {
            assertThrows(
                IllegalArgumentException.class, 
                () -> ResourceResolver.fromPath(null)
            );
        }

        @Test
        @DisplayName("should throw when path resource does not exist")
        void pathTest2() {
            assertThrows(
                ExternalizedPropertiesException.class, 
                () -> ResourceResolver.fromPath(
                    Paths.get("path", "to", "non.existent.properties")
                )
            );
        }

        @Test
        @DisplayName("should not return null")
        void pathTest3() throws IOException, URISyntaxException {
            Path testPropertiesPath =
                classpathResourceAsPath("/test.properties");

            ResourceResolver resolver = ResourceResolver.fromPath(
                testPropertiesPath
            );

            assertNotNull(resolver);
        }

        @Test
        @DisplayName("should throw when path argument is null")
        void pathAndReaderOverloadTest1() {
            assertThrows(
                IllegalArgumentException.class, 
                () -> ResourceResolver.fromPath(null, PROPERTIES_READER)
            );
        }

        @Test
        @DisplayName("should throw when reader argument is null")
        void pathAndReaderOverloadTest2() throws URISyntaxException {
            Path testPropertiesPath = 
                classpathResourceAsPath("/test.properties");

            assertThrows(
                IllegalArgumentException.class, 
                () -> ResourceResolver.fromPath(
                    testPropertiesPath, 
                    null
                )
            );
        }

        @Test
        @DisplayName("should throw when path resource does not exist")
        void pathAndReaderOverloadTest3() {
            assertThrows(
                ExternalizedPropertiesException.class, 
                () -> ResourceResolver.fromPath(
                    Paths.get("path", "to", "non.existent.properties"), 
                    PROPERTIES_READER
                )
            );
        }

        @Test
        @DisplayName("should not return null")
        void pathAndReaderOverloadTest4() throws IOException, URISyntaxException {
            Path testPropertiesPath = 
                classpathResourceAsPath("/test.properties");

            ResourceResolver resolver = ResourceResolver.fromPath(
                testPropertiesPath, 
                PROPERTIES_READER
            );

            assertNotNull(resolver);
        }
    }

    @Nested
    class ResolveMethod {
        @Test
        @DisplayName("should resolve loaded properties from URL")
        void test1() throws IOException {
            ResourceResolver resolver = resolverToTest(
                classpathResource("/test.properties")
            );

            InvocationContext context = INVOCATION_CONTEXT_FACTORY.fromMethodReference(
                ProxyInterface::property,
                externalizedProperties(resolver)
            );
            Optional<String> result = 
                resolver.resolve(context, "property");

            assertTrue(result.isPresent());
            // Matches value in test.properties.
            assertEquals("property-value", result.get());
        }

        @Test
        @DisplayName(
            "should return empty Optional when property is not in loaded properties"
        )
        void test2() throws IOException {
            ResourceResolver resolver = resolverToTest(
                classpathResource("/test.properties")
            );

            InvocationContext context = INVOCATION_CONTEXT_FACTORY.fromMethodReference(
                ProxyInterface::property,
                externalizedProperties(resolver)
            );
            Optional<String> result = 
                resolver.resolve(context, "non.existent.property");

            assertFalse(result.isPresent());
        }

        @ParameterizedTest
        @ArgumentsSource(ResourceReaderProvider.class)
        @DisplayName("should resolve loaded properties from URL using specified resource readers")
        void test3(String resourcePath, ResourceReader resourceReader) throws IOException {
            ResourceResolver resolver = resolverToTest(
                classpathResource(resourcePath),
                resourceReader
            );

            InvocationContext context = INVOCATION_CONTEXT_FACTORY.fromMethodReference(
                ProxyInterface::property,
                externalizedProperties(resolver)
            );
            Optional<String> result = 
                resolver.resolve(context, "property");

            assertTrue(result.isPresent());
            // Matches value in test resource file.
            assertEquals("property-value", result.get());
        }

        @Test
        @DisplayName("should resolve raw resource contents loaded from URL")
        void test4() throws IOException {
            URL resourceUrl = classpathResource("/test.properties");
            ResourceResolver resolver = resolverToTest(resourceUrl);

            InvocationContext context = INVOCATION_CONTEXT_FACTORY.fromMethodReference(
                ProxyInterface::property,
                externalizedProperties(resolver)
            );
            Optional<String> result = 
                resolver.resolve(context, resourceUrl.toString());

            assertTrue(result.isPresent());
            // Result is same as the test.properties file contents.
            assertEquals(readAsString(resourceUrl.openStream()), result.get());
        }

        @ParameterizedTest
        @ArgumentsSource(NestedResourceReaderProvider.class)
        @DisplayName("should flatten nested properties (YAML/JSON/XML)")
        void flattenTest1(String resourcePath, ResourceReader resourceReader) throws IOException {
            ResourceResolver resolver = resolverToTest(
                classpathResource(resourcePath),
                resourceReader
            );

            InvocationContext context = INVOCATION_CONTEXT_FACTORY.fromMethodReference(
                ProxyInterface::property,
                externalizedProperties(resolver)
            );
            Optional<String> result = 
                resolver.resolve(context, "property.nested.awesome");

            assertTrue(result.isPresent());
            // Matches value in test-nested resource file.
            assertEquals("property-nested-awesome-value", result.get());
        }

        @Test
        @DisplayName("should flatten JSON array properties into array notation")
        void flattenArrayTest1() throws IOException {
            ResourceResolver resolver = resolverToTest(
                classpathResource("/test-nested.json"),
                JSON_READER
            );

            InvocationContext context = INVOCATION_CONTEXT_FACTORY.fromMethodReference(
                ProxyInterface::property,
                externalizedProperties(resolver)
            );
            Optional<String> result1 = 
                resolver.resolve(context, "property.nested.array[0]");
            Optional<String> result2 = 
                resolver.resolve(context, "property.nested.array[1]");
            Optional<String> result3 = 
                resolver.resolve(context, "property.nested.array[2]");

            assertTrue(result1.isPresent());
            assertTrue(result2.isPresent());
            assertTrue(result3.isPresent());
            // Matches values in test-nested.json.
            assertEquals("1", result1.get());
            assertEquals("2", result2.get());
            assertEquals("3", result3.get());
        }

        @Test
        @DisplayName("should flatten YAML array properties into array notation")
        void flattenArrayTest2() throws IOException {
            ResourceResolver resolver = resolverToTest(
                classpathResource("/test-nested.yaml"),
                YAML_READER
            );

            InvocationContext context = INVOCATION_CONTEXT_FACTORY.fromMethodReference(
                ProxyInterface::property,
                externalizedProperties(resolver)
            );
            Optional<String> result1 = 
                resolver.resolve(context, "property.nested.array[0]");
            Optional<String> result2 = 
                resolver.resolve(context, "property.nested.array[1]");
            Optional<String> result3 = 
                resolver.resolve(context, "property.nested.array[2]");

            assertTrue(result1.isPresent());
            assertTrue(result2.isPresent());
            assertTrue(result3.isPresent());
            // Matches values in test-nested.yaml.
            assertEquals("1", result1.get());
            assertEquals("2", result2.get());
            assertEquals("3", result3.get());
        }

        @Test
        @DisplayName("should flatten XML array properties into array notation")
        void flattenArrayTest3() throws IOException {
            ResourceResolver resolver = resolverToTest(
                classpathResource("/test-nested.xml"),
                XML_READER
            );

            InvocationContext context = INVOCATION_CONTEXT_FACTORY.fromMethodReference(
                ProxyInterface::property,
                externalizedProperties(resolver)
            );
            Optional<String> result1 = 
                resolver.resolve(context, "property.nested.array.value[0]");
            Optional<String> result2 = 
                resolver.resolve(context, "property.nested.array.value[1]");
            Optional<String> result3 = 
                resolver.resolve(context, "property.nested.array.value[2]");

            assertTrue(result1.isPresent());
            assertTrue(result2.isPresent());
            assertTrue(result3.isPresent());
            // Matches values in test-nested.xml.
            assertEquals("1", result1.get());
            assertEquals("2", result2.get());
            assertEquals("3", result3.get());
        }

        @Test
        @DisplayName("should flatten JSON array properties into array notation")
        void flattenArrayTest4() throws IOException {
            ResourceResolver resolver = resolverToTest(
                classpathResource("/test-nested.json"),
                JSON_READER
            );

            InvocationContext context = INVOCATION_CONTEXT_FACTORY.fromMethodReference(
                ProxyInterface::property,
                externalizedProperties(resolver)
            );
            Optional<String> result = 
                resolver.resolve(context, "property.nested.empty-array");

            assertTrue(result.isPresent());
            // Matches value in test-nested.json.
            // Empty arrays when flattened are converted to empty strings.
            assertEquals("", result.get());
        }

        @Test
        @DisplayName("should flatten YAML array properties into array notation")
        void flattenArrayTest5() throws IOException {
            ResourceResolver resolver = resolverToTest(
                classpathResource("/test-nested.yaml"),
                YAML_READER
            );

            InvocationContext context = INVOCATION_CONTEXT_FACTORY.fromMethodReference(
                ProxyInterface::property,
                externalizedProperties(resolver)
            );
            Optional<String> result = 
                resolver.resolve(context, "property.nested.empty-array");

            assertTrue(result.isPresent());
            // Matches value in test-nested.yaml.
            // Empty arrays when flattened are converted to empty strings.
            assertEquals("", result.get());
        }

        @Test
        @DisplayName("should flatten XML array properties into array notation")
        void flattenArrayTest6() throws IOException {
            ResourceResolver resolver = resolverToTest(
                classpathResource("/test-nested.xml"),
                XML_READER
            );

            InvocationContext context = INVOCATION_CONTEXT_FACTORY.fromMethodReference(
                ProxyInterface::property,
                externalizedProperties(resolver)
            );
            Optional<String> result = 
                resolver.resolve(context, "property.nested.empty-array");

            assertTrue(result.isPresent());
            // Matches value in test-nested.xml.
            // Empty arrays when flattened are converted to empty strings.
            assertEquals("", result.get());
        }

        @Test
        @DisplayName("should convert null to empty String")
        void flattenNullTest1() throws IOException {
            ResourceResolver resolver = resolverToTest(
                classpathResource("/test-nested.json"),
                JSON_READER
            );

            InvocationContext context = INVOCATION_CONTEXT_FACTORY.fromMethodReference(
                ProxyInterface::property,
                externalizedProperties(resolver)
            );
            Optional<String> result = 
                resolver.resolve(context, "property.null");

            assertTrue(result.isPresent());
            // Matches value in test-nested.json.
            // null is converted to empty String
            assertEquals("", result.get());
        }

        @Test
        @DisplayName("should convert null to empty String")
        void flattenNullTest2() throws IOException {
            ResourceResolver resolver = resolverToTest(
                classpathResource("/test-nested.yaml"),
                YAML_READER
            );

            InvocationContext context = INVOCATION_CONTEXT_FACTORY.fromMethodReference(
                ProxyInterface::property,
                externalizedProperties(resolver)
            );
            Optional<String> result = 
                resolver.resolve(context, "property.null");

            assertTrue(result.isPresent());
            // Matches value in test-nested.json.
            // null is converted to empty String
            assertEquals("", result.get());
        }

        @Test
        @DisplayName("should convert null to empty String")
        void flattenNullTest3() throws IOException {
            ResourceResolver resolver = resolverToTest(
                classpathResource("/test-nested.xml"),
                XML_READER
            );

            InvocationContext context = INVOCATION_CONTEXT_FACTORY.fromMethodReference(
                ProxyInterface::property,
                externalizedProperties(resolver)
            );
            Optional<String> result = 
                resolver.resolve(context, "property.null");

            assertTrue(result.isPresent());
            // Matches value in test-nested.json.
            // null is converted to empty String
            assertEquals("", result.get());
        }
    }

    private static ResourceResolver resolverToTest(URL url) throws IOException {
        return ResourceResolver.fromUrl(url);
    }

    private static ResourceResolver resolverToTest(URL url, ResourceReader reader) 
            throws IOException {
        return ResourceResolver.fromUrl(url, reader);
    }

    private static String readAsString(InputStream inputStream) throws IOException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int bufferLength;
        while ((bufferLength = inputStream.read(buffer)) != -1) {
            output.write(buffer, 0, bufferLength);
        }
        return output.toString(StandardCharsets.UTF_8.name());
    }
    
    private URL classpathResource(String classpathResource) {
        return getClass().getResource(classpathResource);
    }
    
    private URI classpathResourceAsUri(String classpathResource) throws URISyntaxException {
        return getClass().getResource(classpathResource).toURI();
    }
    
    private Path classpathResourceAsPath(String classpathResource) throws URISyntaxException {
        return Paths.get(getClass().getResource(classpathResource).toURI());
    }
    
    private static ExternalizedProperties externalizedProperties(Resolver... resolvers) {
        return ExternalizedProperties.builder().resolvers(resolvers).build();
    }

    private static interface ProxyInterface {
        @ExternalizedProperty("property")
        String property();
    }

    private static class ResourceReaderProvider implements ArgumentsProvider {
        @Override
        public Stream<? extends Arguments> provideArguments(
                ExtensionContext context
        ) throws Exception {
            // File/resource and reader mappings.
            return Stream.of(
                Arguments.of("/test.yaml", YAML_READER),
                Arguments.of("/test.json", JSON_READER),
                Arguments.of("/test.xml", XML_READER)
            );
        }
    }

    private static class NestedResourceReaderProvider implements ArgumentsProvider {
        @Override
        public Stream<? extends Arguments> provideArguments(
                ExtensionContext context
        ) throws Exception {
            // File/resource and reader mappings.
            return Stream.of(
                Arguments.of("/test-nested.yaml", YAML_READER),
                Arguments.of("/test-nested.json", JSON_READER),
                Arguments.of("/test-nested.xml", XML_READER)
            );
        }
    }
}
