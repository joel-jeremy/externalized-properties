# Ordinal Components

Externalized Properties allows registration of custom components such as [Resolver](../core/src/main/java/io/github/joeljeremy7/externalizedproperties/core/Resolver.java)s and [Converter](../core/src/main/java/io/github/joeljeremy7/externalizedproperties/core/Converter.java)s. In some cases, we want to assign a higher priority to some than the others e.g. lookup system properties before environment variables.

This is where the [Ordinals](../core/src/main/java/io/github/joeljeremy7/externalizedproperties/core/Ordinals.java) class can help. It allows clients to assign resolver/converter instances with an ordinal. The ordinals will be considered when building the [ExternalizedProperties](../core/src/main/java/io/github/joeljeremy7/externalizedproperties/core/ExternalizedProperties.java) instance to sort the registered resolvers and converters accordingly e.g.

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

Note: Resolvers/converters that were assigned an ordinal will be placed earlier in the sequence than those that were not assigned one. In other words, Ordinal resolvers/converters will be given higher priority.
