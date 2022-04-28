package io.github.jeyjeyemem.externalizedproperties.core.testentities.resolvers;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import io.github.jeyjeyemem.externalizedproperties.core.resolvers.ResourceResolver.ResourceReader;

import java.io.IOException;
import java.util.Map;

public class YamlReader implements ResourceReader {
    private final ObjectMapper yamlMapper = new ObjectMapper(new YAMLFactory());

    @Override
    public Map<String, Object> read(String resourceContent) throws IOException {
        return yamlMapper.readValue(
            resourceContent, 
            new TypeReference<Map<String, Object>>() {}
        );
    }
}
