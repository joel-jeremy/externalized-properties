package io.github.joeljeremy7.externalizedproperties.resolvers.git.testentities.resourcereaders;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.joeljeremy7.externalizedproperties.core.resolvers.ResourceResolver.ResourceReader;

import java.io.IOException;
import java.util.Map;

public class JsonReader implements ResourceReader {
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public Map<String, Object> read(String resourceContents) throws IOException {
        return objectMapper.readValue(
            resourceContents, 
            new TypeReference<Map<String, Object>>(){}
        );
    }
}
