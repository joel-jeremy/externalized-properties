# Ordinal Components

Externalized Properties allows registration of custom components such as resolvers and converters. In some cases, we want to assign a higher priority to some than the others e.g. lookup system properties before environment variables.

This is where the [Ordinals](core/src/main/java/io/github/joeljeremy7/externalizedproperties/core/Ordinals.java) class can help. It allows clients to decorate resolver/converter instances with an ordinal. The ordinal will be used by Externalized Properties at build time to sort accordingly the registered resolvers and converters accordingly.

```java
public static void main(String[] args) {
    ExternalizedProperties externalizedProperties = ExternalizedProperties.builder()
        .resolvers(
            new MapResolver(buildMap()),
            Ordinals.ordinalResolver(1, new SystemPropertyResolver()),
            Ordinals.ordinalResolver(2, new EnvironmentVariableResolver())
        )
        .converters(
            new ListConverter(),
            Ordinals.ordinalConverter(1, new PrimitiveConverter()),
            Ordinals.ordinalConverter(2, new SomeLegacyPrimitiveConverter())
        )
        .build();
}
```

The resulting resolver order will be:  

1. SystemPropertyResolver
2. EnvironmentVariableResolver
3. MapResolver

The resulting converter order will be:  

1. PrimitiveConverter
2. SomeLegacyPrimitiveConverter
3. ListConverter

Note: Resolvers/converters that were not assigned an ordinal will have lower priority than those that were assigned one.
