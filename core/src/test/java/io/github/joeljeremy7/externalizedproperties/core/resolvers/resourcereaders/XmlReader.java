package io.github.joeljeremy7.externalizedproperties.core.resolvers.resourcereaders;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlFactory;
import io.github.joeljeremy7.externalizedproperties.core.resolvers.ResourceResolver.ResourceReader;

import java.io.IOException;
import java.util.Map;

/**
 * A {@link ResourceReader} implementation which reads an {@code .xml} resource 
 * to a {@code Map} via {@link ObjectMapper}.
 *
 * <p>
 * This is a simple implementation which serializes an XML resource to a {@code Map}.
 * It is not included in the library as a built-in {@link ResourceReader} because 
 * we want to minimize external dependencies as much as possible to keep the library 
 * size small. It is easy enough to implement so I decided to put this here instead to
 * serve as an example for anyone who wants to implement reading of properties from XML.
 * </p>
 */
public class XmlReader implements ResourceReader {
    private final ObjectMapper yamlMapper = new ObjectMapper(new XmlFactory());

    @Override
    public Map<String, Object> read(String resourceContents) throws IOException {
        return yamlMapper.readValue(
            resourceContents, 
            new TypeReference<Map<String, Object>>() {}
        );
    }
}