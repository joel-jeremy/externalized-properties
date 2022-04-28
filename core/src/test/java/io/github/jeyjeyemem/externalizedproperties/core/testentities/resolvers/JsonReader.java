package io.github.jeyjeyemem.externalizedproperties.core.testentities.resolvers;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.jeyjeyemem.externalizedproperties.core.resolvers.ResourceResolver.ResourceReader;

import java.io.IOException;
import java.util.Map;

public class JsonReader implements ResourceReader {
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public Map<String, Object> read(String resourceContent) throws IOException {
        return objectMapper.readValue(
            resourceContent, 
            new TypeReference<Map<String, Object>>() {}
        );
    }
}
