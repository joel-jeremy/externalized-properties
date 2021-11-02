# Externalized Properties
A lightweight and extensible library to resolve application properties from various external sources.


## [Twelve Factor Methodology](https://12factor.net)
Externalized Properties was inspired by the [The Twelve Factor Methodology](https://12factor.net)'s section [III. Config](https://12factor.net/config).  

The goal of this library is to make it easy for applications to implement configuration best practices by providing easy-to-use APIs as well as providing the flexibility to choose where to store their configurations/properties. 

## Wiki
- For more information and examples please browse through the wiki: https://github.com/jeyjeyemem/externalized-properties/wiki

## Getting Started

Externalized Properties makes the best of of Java's strong typing by proxying an interface and using that as a facade to resolve properties.

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
// Create the ExternalizedProperties instance.
ExternalizedProperties externalizedProperties = ExternalizedProperties.builder()
    .withDefaultResolvers() // Register default resolvers (system properties + env var)
    .resolvers(...) // Register any custom/additional resolvers
    .build();

// Proxied interface.
ApplicationProperties props = externalizedProperties.initialize(ApplicationProperties.class);

// Start resolving properties from registered resolvers.
String databaseUrl = props.databaseUrl();
String databaseDriver = props.databaseDriver();
```
