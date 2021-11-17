# Externalized Properties

[![Gradle Build](https://github.com/jeyjeyemem/externalized-properties/actions/workflows/gradle-build.yaml/badge.svg)](https://github.com/jeyjeyemem/externalized-properties/actions/workflows/gradle-build.yaml)
[![License](https://img.shields.io/badge/License-Apache_2.0-blue.svg)](https://github.com/jeyjeyemem/externalized-properties/blob/main/LICENSE)

A lightweight and extensible library to resolve application properties from various external sources.

## [Twelve Factor Methodology](https://12factor.net)

Externalized Properties was inspired by the [The Twelve Factor Methodology](https://12factor.net)'s section [III. Config](https://12factor.net/config).  

The goal of this library is to make it easy for applications to implement configuration best practices by providing easy-to-use APIs as well as providing the flexibility to choose where to store their configurations/properties.

## Wiki

- For more information and examples please browse through the wiki: <https://github.com/jeyjeyemem/externalized-properties/wiki>

## Getting Started

### Gradle

```gradle
implementation 'io.github.jeyjeyemem.externalizedproperties:core:1.0.0-SNAPSHOT'
```

### Maven

```xml
<dependency>
    <groupId>io.github.jeyjeyemem.externalizedproperties</groupId>
    <artifactId>core</artifactId>
    <version>1.0.0-SNAPSHOT</version>
</dependency>
```

Externalized Properties makes the best of of Java's strong typing by proxying an interface and using that as a facade to resolve properties.

### Interface Proxying

Given an interface:

```java
public interface ApplicationProperties {
    @ExternalizedProperty("DATABASE_URL")
    String databaseUrl();
    @ExternalizedProperty("DATABASE_DRIVER")
    String databaseDriver();
}
```

We can initialize and start resolving external configurations/properties by:

```java
public static void main(String[] args) {
    ExternalizedProperties externalizedProperties = buildExternalizedProperties();

    // Proxied interface.
    ApplicationProperties props = externalizedProperties.proxy(ApplicationProperties.class);

    // Use properties.
    String databaseUrl = props.databaseUrl();
    String databaseDriver = props.databaseDriver();

    System.out.println("Database URL: " + databaseUrl);
    System.out.println("Database Driver: " + databaseDriver);
}

private ExternalizedProperties buildExternalizedProperties() {
    // Create the ExternalizedProperties instance with default and additional resolvers.
    // Default resolvers include system properties and environment variable resolvers.
    // AWS SSM Resolver and Database Resolver are not part of the core module. They 
    // are part of a separate resolver-aws-ssm and resolver-database modules.

    ExternalizedProperties externalizedProperties = ExternalizedProperties.builder()
        .withDefaultResolvers() 
        .resolvers( 
            new AwsSsmPropertyResolver(ssmClient),
            new DatabaseResolver(entityManagerFactory)
        ) 
        .build();
    
    return externalizedProperties;
}
```

### Direct Property Resolution

Another option is to resolve properties directly from the `ExternalizedProperties` instance if you want to avoid overhead of using proxies:

```java
public static void main(String[] args) {
    ExternalizedProperties externalizedProperties = buildExternalizedProperties();

    // Proxied interface.
    Optional<ResolvedProperty> property = externalizedProperties.resolveProperty("database.url");

    // Use property:
    System.out.println("Property Name: " + property.name());
    System.out.println("Property Value: " + property.value());
}
```
