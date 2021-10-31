# Externalized Properties
A lightweight and extensible library to resolve application properties from various external sources.


## [Twelve Factor Methodology](https://12factor.net)
This library was inspired by the [The Twelve Factor Methodology](https://12factor.net)'s section [III. Config](https://12factor.net/config).  

The goal of this library is to make it easy for applications to implement configuration best practices by providing easy-to-use APIs as well as providing the flexibility to choose where to store their configurations/properties. 

## Quick Start

The library makes the best of Java's strong typing by utilizing Java's Dynamic Proxy feature to create proxies of interfaces and encapsulating the actual property resolution.

It's as simple as creating an interface with methods marked with `@ExternalizedProperty` annotations to specify the property names:
```java
public interface ApplicationProperties {
    @ExternalizedProperty("APP_DATABASE_URL")
    String databaseUrl();
    @ExternalizedProperty("APP_DATABASE_PASSWORD")
    String databasePassword();
}
```
and creating a proxy by using the `ExternalizedProperties` API:

```java 
// Build an ExternalizedProperties instance.
ExternalizedProperties externalizedProperties = ExternalizedProperties.builder()
    .withDefault()
    .build();

// Create the proxy interface.
ApplicationProperties props = externalizedProperties.initialize(ApplicationProperties.class);

// Automatically resolve values from configured source.
String databaseUrl = props.databaseUrl();
String databasePassword = props.databasePassword();
```

## Overview

### ExternalizedProperties
The core API for the Externalized Properties library. Given an interface, it creates a proxy which does the property resolution behind the scenes.

`ExternalizedProperties` can be configured to use a variety of resolvers to resolve property values:

```java
// Configure with default resolvers which resolves from system properties and/or environment variables.
ExternalizedProperties defaultExternalizedProperties = 
    ExternalizedProperties.builder()
        .withDefaultResolvers()
        .build();
```
```java
// Configure with builtin + custom resolvers.
ExternalizedProperties customExternalizedProperties = 
    ExternalizedProperties.builder()
        .resolvers(
            new SystemPropertyResolver(),
            new MyCustomAwsSsmResolver()
        )
        .build();
```

`ExternalizedProperties` can also be configured to use a variety of conversion handlers which will be used to convert resolved property to match the expected property type:

```java
// Configure with default conversion handlers. These can handle conversion of property values to the ff types:
// - Primitives
// - Lists/Collections
// - Arrays
// - Optional
ExternalizedProperties defaultExternalizedProperties = 
    ExternalizedProperties.builder()
        .withDefaultConversionHandlers()
        .build();
```
```java
// Configure with builtin + custom conversion handler.
ExternalizedProperties customExternalizedProperties = 
    ExternalizedProperties.builder()
        .conversionHandlers(
            new PrimitiveConversionHandler(),
            new MyCustomJsonConversionHandler()
        )
        .build();
```
