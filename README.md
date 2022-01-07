# Externalized Properties

[![Gradle Build](https://github.com/jeyjeyemem/externalized-properties/actions/workflows/gradle-build.yaml/badge.svg)](https://github.com/jeyjeyemem/externalized-properties/actions/workflows/gradle-build.yaml)
[![Coverage Status](https://coveralls.io/repos/github/jeyjeyemem/externalized-properties/badge.svg?branch=main)](https://coveralls.io/github/jeyjeyemem/externalized-properties?branch=main)
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

### Java 9 Module Names

Externalized Properties jars are published with Automatic-Module-Name manifest attribute:

- Core - `io.github.jeyjeyemem.externalizedproperties.core`
- AWS SSM Resolver - `io.github.jeyjeyemem.externalizedproperties.resolvers.awsssm`
- Database Resolver - `io.github.jeyjeyemem.externalizedproperties.resolvers.database`

Module authors can use above module names in their module-info.java:

```java
module foo.bar {
    requires io.github.jeyjeyemem.externalizedproperties.core;
    requires io.github.jeyjeyemem.externalizedproperties.resolvers.awsssm;
    requires io.github.jeyjeyemem.externalizedproperties.resolvers.database;
}
```

## Sample Projects

Sample projects can be found in: <https://github.com/jeyjeyemem/externalized-properties-samples>

## Features

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

    ExternalizedProperties externalizedProperties = ExternalizedPropertiesBuilder.newBuilder()
        .withDefaultResolvers() 
        .resolvers( 
            new AwsSsmResolver(getAwsSsmClient()),
            new DatabaseResolver(getEntityManagerFactory())
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

    // Direct resolution via ExternalizedProperties API.
    Optional<String> databaseUrl = externalizedProperties.resolveProperty("DATABASE_URL");
    Optional<String> databaseDriver = externalizedProperties.resolveProperty("DATABASE_DRIVER");

    // Use property:
    System.out.println("Database URL: " + databaseUrl.get());
    System.out.println("Database Driver: " + databaseDriver.get());
}
```

### Property Conversion

Externalized Properties has powerful support for conversion of properties to various types. There are several build-in conversion handlers but developers are free to create a custom conversion handler by implementing the `ConversionHandler` interface.

To register conversion handlers to the library, it must be done through the builder:

```java
private ExternalizedProperties buildExternalizedProperties() {
    ExternalizedProperties externalizedProperties = ExternalizedPropertiesBuilder.newBuilder()
        .withDefaultResolvers()
        .conversionHandlers(
            new PrimitiveConversionHandler(),
            new CustomConversionHandler()
        )
        .build();

    return externalizedProperties;
}
```

To convert a property via the proxy interface, just set the method return type to the target type, and the library will handle the conversion behind the scenes - using the registered conversion handlers.

```java
public interface ApplicationProperties {
    @ExternalizedProperty("thread-count")
    int numberOfThreads();
}

public static void main(String[] args) {
    ExternalizedProperties externalizedProperties = buildExternalizedProperties();

    // Proxied interface.
    ApplicationProperties props = externalizedProperties.proxy(ApplicationProperties.class);

    // Use properties.
    int numberOfThreads = props.numberOfThreads();

    System.out.println("Number of threads: " + numberOfThreads);
}
```

To convert a property via the `ExternalizedProperties` API, the `resolveProperty` method which accepts a target type must be used:

```java
public static void main(String[] args) {
    ExternalizedProperties externalizedProperties = buildExternalizedProperties();

    // Use properties.
    Optional<Integer> numberOfThreads = externalizedProperties.resolveProperty("number-of-threads", Integer.class);
    // TypeReference class can be used to specify generic target types.
    Optional<List<Integer>> validNumbers = externalizedProperties.resolveProperty(
        "valid-numbers", 
        new TypeReference<List<Integer>>(){}
    );

    System.out.println("Number of threads: " + numberOfThreads.get());
    System.out.println("Valid numbers: " + validNumbers.get());
}
```

### Conversion to Generic Types

Externalized Properties has good support for generic types. Given the proxy interface:

```java
public interface ApplicationProperties {
    @ExternalizedProperty("list-of-numbers")
    List<Integer> listOfNumbers();
}
```

Externalized Properties provides enough information to conversion handlers for them to be able to convert each item from the `list-of-numbers` property to an integer (provided a conversion handler is registered to convert to an integer).

An arbitraty generic type parameter depth is supported. For example,

```java
public interface ApplicationProperties {
    @ExternalizedProperty("list-of-numbers")
    List<Optional<Integer>> listOfOptionalNumbers();
}
````

Conversion handlers should be able to extract the generic type information from the conversion context and convert each item to an `Optional<Integer>`.
