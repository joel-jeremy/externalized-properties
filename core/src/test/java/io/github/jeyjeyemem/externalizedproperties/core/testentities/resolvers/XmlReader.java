package io.github.jeyjeyemem.externalizedproperties.core.testentities.resolvers;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlFactory;
import io.github.jeyjeyemem.externalizedproperties.core.resolvers.ResourceResolver.ResourceReader;

import java.io.IOException;
import java.util.Map;

public class XmlReader implements ResourceReader {
    private final ObjectMapper xmlMapper = new ObjectMapper(new XmlFactory());

    @Override
    public Map<String, Object> read(String resourceContent) throws IOException {
        return xmlMapper.readValue(
            resourceContent, 
            new TypeReference<Map<String, Object>>() {}
        );
    }
}
