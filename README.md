# Externalized Properties
A lightweight and extensible library to resolve application properties from various external sources.


## [Twelve Factor Methodology](https://12factor.net)
This library was inspired by the [The Twelve Factor Methodology](https://12factor.net)'s section [III. Config](https://12factor.net/config).  

The goal of this library is to make it easy for applications to implement configuration best practices by providing easy-to-use APIs as well as providing the flexibility to choose where to store their configurations/properties. 

## Getting Started

The library makes the best of of Java's strong typing by proxying an interface and using that to resolve properties.

Given an interface:
```java
public interface ApplicationProperties {
    @ExternalizedProperty("APP_DATABASE_URL")
    String databaseUrl();
    @ExternalizedProeprty("APP_DATABASE_PASSWORD")
    String databasePassword();
}
```
We can initialize and start resolving external configurations/properties by:

```java 
// Make proxy interfaces resolve properties from environment variables
ExternalizedProperties externalizedProperties = ExternalizedProperties.builder()
    .resolvers(new EnvironmentVariableResolver())
    .build();

// Create the proxy interface.
ApplicationProperties props = externalizedProperties.initialize(ApplicationProperties.class);

// Resolves value from environment variables.
String databaseUrl = props.databaseUrl();
String databasePassword = props.databasePassword();
```