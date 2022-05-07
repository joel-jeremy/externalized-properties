package io.github.joeljeremy7.externalizedproperties.core.resolvers;

import io.github.joeljeremy7.externalizedproperties.core.ExternalizedProperty;
import io.github.joeljeremy7.externalizedproperties.core.proxy.ProxyMethod;
import io.github.joeljeremy7.externalizedproperties.core.resolvers.ResourceResolver.PropertiesReader;
import io.github.joeljeremy7.externalizedproperties.core.resolvers.ResourceResolver.ResourceReader;
import io.github.joeljeremy7.externalizedproperties.core.resolvers.resourcereaders.JsonReader;
import io.github.joeljeremy7.externalizedproperties.core.resolvers.resourcereaders.XmlReader;
import io.github.joeljeremy7.externalizedproperties.core.resolvers.resourcereaders.YamlReader;
import io.github.joeljeremy7.externalizedproperties.core.testfixtures.ProxyMethodFactory;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ResourceResolverTests {
    private static final ProxyMethodFactory<ProxyInterface> PROXY_METHOD_FACTORY =
        new ProxyMethodFactory<>(ProxyInterface.class);
    
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
        @DisplayName("should throw when URL resource does not exist")
        void urlTest2() {
            assertThrows(
                IOException.class, 
                () -> ResourceResolver.fromUrl(
                    new URL("file://non.existent.properties")
                )
            );
        }

        @Test
        @DisplayName("should not return null")
        void urlTest3() throws IOException {
            ResourceResolver resolver = ResourceResolver.fromUrl(
                getClass().getResource("/test.properties")
            );

            assertNotNull(resolver);
        }

        @Test
        @DisplayName("should throw when url argument is null")
        void urlAndReaderOverloadTest1() {
            assertThrows(
                IllegalArgumentException.class, 
                () -> ResourceResolver.fromUrl(null, new PropertiesReader())
            );
        }

        @Test
        @DisplayName("should throw when url argument is null")
        void urlAndReaderOverloadTest2() {
            assertThrows(
                IllegalArgumentException.class, 
                () -> ResourceResolver.fromUrl(
                    getClass().getResource("/test.properties"), 
                    null
                )
            );
        }

        @Test
        @DisplayName("should throw when URL resource does not exist")
        void urlAndReaderOverloadTest3() {
            assertThrows(
                IOException.class, 
                () -> ResourceResolver.fromUrl(
                    new URL("file://non.existent.properties"), 
                    new PropertiesReader()
                )
            );
        }

        @Test
        @DisplayName("should not return null")
        void urlAndReaderOverloadTest4() throws IOException {
            ResourceResolver resolver = ResourceResolver.fromUrl(
                getClass().getResource("/test.properties"), 
                new PropertiesReader()
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
                IOException.class, 
                () -> ResourceResolver.fromUri(
                    URI.create("file://non.existent.properties")
                )
            );
        }

        @Test
        @DisplayName("should throw when URI is invalid")
        void uriTest3() {
            assertThrows(
                IllegalArgumentException.class, 
                () -> ResourceResolver.fromUri(
                    URI.create("invalid_uri")
                )
            );
        }

        @Test
        @DisplayName("should not return null")
        void uriTest4() throws IOException, URISyntaxException {
            ResourceResolver resolver = ResourceResolver.fromUri(
                getClass().getResource("/test.properties").toURI()
            );

            assertNotNull(resolver);
        }

        @Test
        @DisplayName("should throw when uri argument is null")
        void uriAndReaderOverloadTest1() {
            assertThrows(
                IllegalArgumentException.class, 
                () -> ResourceResolver.fromUri(null, new PropertiesReader())
            );
        }

        @Test
        @DisplayName("should throw when reader argument is null")
        void uriAndReaderOverloadTest2() {
            assertThrows(
                IllegalArgumentException.class, 
                () -> ResourceResolver.fromUri(
                    getClass().getResource("/test.properties").toURI(), 
                    null
                )
            );
        }

        @Test
        @DisplayName("should throw when URI resource does not exist")
        void uriAndReaderOverloadTest3() {
            assertThrows(
                IOException.class, 
                () -> ResourceResolver.fromUri(
                    URI.create("file://non.existent.properties"), 
                    new PropertiesReader()
                )
            );
        }

        @Test
        @DisplayName("should throw when URI is invalid")
        void uriAndReaderOverloadTest4() {
            assertThrows(
                IllegalArgumentException.class, 
                () -> ResourceResolver.fromUri(
                    URI.create("invalid_uri"), 
                    new PropertiesReader()
                )
            );
        }

        @Test
        @DisplayName("should not return null")
        void uriAndReaderOverloadTest5() throws IOException, URISyntaxException {
            ResourceResolver resolver = ResourceResolver.fromUri(
                getClass().getResource("/test.properties").toURI(), 
                new PropertiesReader()
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
                getClass().getResource("/test.properties")
            );

            ProxyMethod proxyMethod = PROXY_METHOD_FACTORY.fromMethodReference(
                ProxyInterface::property
            );
            Optional<String> result = 
                resolver.resolve(proxyMethod, "property");

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
                getClass().getResource("/test.properties")
            );

            ProxyMethod proxyMethod = PROXY_METHOD_FACTORY.fromMethodReference(
                ProxyInterface::property
            );
            Optional<String> result = 
                resolver.resolve(proxyMethod, "non.existent.property");

            assertFalse(result.isPresent());
        }

        @Test
        @DisplayName("should resolve loaded properties from URL using specified JSON reader")
        void test3() throws IOException {
            ResourceResolver resolver = resolverToTest(
                getClass().getResource("/test.json"),
                new JsonReader()
            );

            ProxyMethod proxyMethod = PROXY_METHOD_FACTORY.fromMethodReference(
                ProxyInterface::property
            );
            Optional<String> result = 
                resolver.resolve(proxyMethod, "property");

            assertTrue(result.isPresent());
            // Matches value in test.json.
            assertEquals("property-value", result.get());
        }

        @Test
        @DisplayName("should resolve loaded properties from URL using specified YAML reader")
        void test4() throws IOException {
            ResourceResolver resolver = resolverToTest(
                getClass().getResource("/test.yaml"),
                new YamlReader()
            );

            ProxyMethod proxyMethod = PROXY_METHOD_FACTORY.fromMethodReference(
                ProxyInterface::property
            );
            Optional<String> result = 
                resolver.resolve(proxyMethod, "property");

            assertTrue(result.isPresent());
            // Matches value in test.yaml.
            assertEquals("property-value", result.get());
        }

        @Test
        @DisplayName("should resolve loaded properties from URL using specified XML reader")
        void test5() throws IOException {
            ResourceResolver resolver = resolverToTest(
                getClass().getResource("/test.xml"),
                new XmlReader()
            );

            ProxyMethod proxyMethod = PROXY_METHOD_FACTORY.fromMethodReference(
                ProxyInterface::property
            );
            Optional<String> result = 
                resolver.resolve(proxyMethod, "property");

            assertTrue(result.isPresent());
            // Matches value in test.xml.
            assertEquals("property-value", result.get());
        }

        @Test
        @DisplayName("should resolve raw resource contents loaded from URL")
        void test6() throws IOException {
            URL resourceUrl = getClass().getResource("/test.properties");
            ResourceResolver resolver = resolverToTest(resourceUrl);

            ProxyMethod proxyMethod = PROXY_METHOD_FACTORY.fromMethodReference(
                ProxyInterface::property
            );
            Optional<String> result = 
                resolver.resolve(proxyMethod, resourceUrl.toString());

            assertTrue(result.isPresent());
            // Result is same as the test.properties file contents.
            assertEquals(readAsString(resourceUrl.openStream()), result.get());
        }

        @Test
        @DisplayName("should flatten nested JSON properties")
        void flattenTest1() throws IOException {
            ResourceResolver resolver = resolverToTest(
                getClass().getResource("/test-nested.json"),
                new JsonReader()
            );

            ProxyMethod proxyMethod = PROXY_METHOD_FACTORY.fromMethodReference(
                ProxyInterface::property
            );
            Optional<String> result = 
                resolver.resolve(proxyMethod, "property.nested.awesome");

            assertTrue(result.isPresent());
            // Matches value in test-nested.json.
            assertEquals("property-nested-awesome-value", result.get());
        }

        @Test
        @DisplayName("should flatten nested YAML properties")
        void flattenTest2() throws IOException {
            ResourceResolver resolver = resolverToTest(
                getClass().getResource("/test-nested.yaml"),
                new YamlReader()
            );

            ProxyMethod proxyMethod = PROXY_METHOD_FACTORY.fromMethodReference(
                ProxyInterface::property
            );
            Optional<String> result = 
                resolver.resolve(proxyMethod, "property.nested.awesome");

            assertTrue(result.isPresent());
            // Matches value in test-nested.json.
            assertEquals("property-nested-awesome-value", result.get());
        }

        @Test
        @DisplayName("should flatten nested XML properties")
        void flattenTest3() throws IOException {
            ResourceResolver resolver = resolverToTest(
                getClass().getResource("/test-nested.xml"),
                new XmlReader()
            );

            ProxyMethod proxyMethod = PROXY_METHOD_FACTORY.fromMethodReference(
                ProxyInterface::property
            );
            Optional<String> result = 
                resolver.resolve(proxyMethod, "property.nested.awesome");

            assertTrue(result.isPresent());
            // Matches value in test-nested.json.
            assertEquals("property-nested-awesome-value", result.get());
        }

        @Test
        @DisplayName("should flatten JSON array properties into array notation")
        void flattenArrayTest1() throws IOException {
            ResourceResolver resolver = resolverToTest(
                getClass().getResource("/test-nested.json"),
                new JsonReader()
            );

            ProxyMethod proxyMethod = PROXY_METHOD_FACTORY.fromMethodReference(
                ProxyInterface::property
            );
            Optional<String> result1 = 
                resolver.resolve(proxyMethod, "property.nested.array[0]");
            Optional<String> result2 = 
                resolver.resolve(proxyMethod, "property.nested.array[1]");
            Optional<String> result3 = 
                resolver.resolve(proxyMethod, "property.nested.array[2]");

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
                getClass().getResource("/test-nested.yaml"),
                new YamlReader()
            );

            ProxyMethod proxyMethod = PROXY_METHOD_FACTORY.fromMethodReference(
                ProxyInterface::property
            );
            Optional<String> result1 = 
                resolver.resolve(proxyMethod, "property.nested.array[0]");
            Optional<String> result2 = 
                resolver.resolve(proxyMethod, "property.nested.array[1]");
            Optional<String> result3 = 
                resolver.resolve(proxyMethod, "property.nested.array[2]");

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
                getClass().getResource("/test-nested.xml"),
                new XmlReader()
            );

            ProxyMethod proxyMethod = PROXY_METHOD_FACTORY.fromMethodReference(
                ProxyInterface::property
            );
            Optional<String> result1 = 
                resolver.resolve(proxyMethod, "property.nested.array.value[0]");
            Optional<String> result2 = 
                resolver.resolve(proxyMethod, "property.nested.array.value[1]");
            Optional<String> result3 = 
                resolver.resolve(proxyMethod, "property.nested.array.value[2]");

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
                getClass().getResource("/test-nested.json"),
                new JsonReader()
            );

            ProxyMethod proxyMethod = PROXY_METHOD_FACTORY.fromMethodReference(
                ProxyInterface::property
            );
            Optional<String> result = 
                resolver.resolve(proxyMethod, "property.nested.empty-array");

            assertTrue(result.isPresent());
            // Matches value in test-nested.json.
            // Empty arrays when flattened are converted to empty strings.
            assertEquals("", result.get());
        }

        @Test
        @DisplayName("should flatten YAML array properties into array notation")
        void flattenArrayTest5() throws IOException {
            ResourceResolver resolver = resolverToTest(
                getClass().getResource("/test-nested.yaml"),
                new YamlReader()
            );

            ProxyMethod proxyMethod = PROXY_METHOD_FACTORY.fromMethodReference(
                ProxyInterface::property
            );
            Optional<String> result = 
                resolver.resolve(proxyMethod, "property.nested.empty-array");

            assertTrue(result.isPresent());
            // Matches value in test-nested.yaml.
            // Empty arrays when flattened are converted to empty strings.
            assertEquals("", result.get());
        }

        @Test
        @DisplayName("should flatten XML array properties into array notation")
        void flattenArrayTest6() throws IOException {
            ResourceResolver resolver = resolverToTest(
                getClass().getResource("/test-nested.xml"),
                new XmlReader()
            );

            ProxyMethod proxyMethod = PROXY_METHOD_FACTORY.fromMethodReference(
                ProxyInterface::property
            );
            Optional<String> result = 
                resolver.resolve(proxyMethod, "property.nested.empty-array");

            assertTrue(result.isPresent());
            // Matches value in test-nested.xml.
            // Empty arrays when flattened are converted to empty strings.
            assertEquals("", result.get());
        }

        @Test
        @DisplayName("should convert null to empty String")
        void flattenNullTest1() throws IOException {
            ResourceResolver resolver = resolverToTest(
                getClass().getResource("/test-nested.json"),
                new JsonReader()
            );

            ProxyMethod proxyMethod = PROXY_METHOD_FACTORY.fromMethodReference(
                ProxyInterface::property
            );
            Optional<String> result = 
                resolver.resolve(proxyMethod, "property.null");

            assertTrue(result.isPresent());
            // Matches value in test-nested.json.
            // null is converted to empty String
            assertEquals("", result.get());
        }

        @Test
        @DisplayName("should convert null to empty String")
        void flattenNullTest2() throws IOException {
            ResourceResolver resolver = resolverToTest(
                getClass().getResource("/test-nested.yaml"),
                new YamlReader()
            );

            ProxyMethod proxyMethod = PROXY_METHOD_FACTORY.fromMethodReference(
                ProxyInterface::property
            );
            Optional<String> result = 
                resolver.resolve(proxyMethod, "property.null");

            assertTrue(result.isPresent());
            // Matches value in test-nested.json.
            // null is converted to empty String
            assertEquals("", result.get());
        }

        @Test
        @DisplayName("should convert null to empty String")
        void flattenNullTest3() throws IOException {
            ResourceResolver resolver = resolverToTest(
                getClass().getResource("/test-nested.xml"),
                new XmlReader()
            );

            ProxyMethod proxyMethod = PROXY_METHOD_FACTORY.fromMethodReference(
                ProxyInterface::property
            );
            Optional<String> result = 
                resolver.resolve(proxyMethod, "property.null");

            assertTrue(result.isPresent());
            // Matches value in test-nested.json.
            // null is converted to empty String
            assertEquals("", result.get());
        }
    }

    private ResourceResolver resolverToTest(URL url) throws IOException {
        return ResourceResolver.fromUrl(url);
    }

    private ResourceResolver resolverToTest(URL url, ResourceReader reader) throws IOException {
        return ResourceResolver.fromUrl(url, reader);
    }

    private String readAsString(InputStream inputStream) throws IOException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int bufferLength;
        while ((bufferLength = inputStream.read(buffer)) != -1) {
            output.write(buffer, 0, bufferLength);
        }
        return output.toString(StandardCharsets.UTF_8.name());
    }

    public static interface ProxyInterface {
        @ExternalizedProperty("property")
        String property();
    }
}
