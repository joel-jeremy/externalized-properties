# Property Resolution

Externalized Properties makes the best use of Java's strong typing by using Java's dynamic proxy feature.

It works by creating dynamic/configurable proxy instances (created at runtime by Java) that implement user-defined interfaces as facade to resolve properties.

## Dynamic Proxies

Given an interface:

```java
public interface ApplicationProperties {
    @ExternalizedProperty("java.home")
    String javaHome();
    @ExternalizedProperty("java.version")
    String javaVersion();
}
```

We can initialize and start resolving external configurations/properties by:

```java
public static void main(String[] args) {
    ExternalizedProperties externalizedProperties = buildExternalizedProperties();

    // Proxied interface.
    ApplicationProperties props = externalizedProperties.proxy(ApplicationProperties.class);

    // Use properties.
    String javaHome = props.javaHome();
    String javaVersion = props.javaVersion();
}

private static ExternalizedProperties buildExternalizedProperties() {
    // Create the ExternalizedProperties instance with default and additional resolvers.
    // Default resolvers include system properties and environment variable resolvers.
    
    return ExternalizedProperties.builder()
        .withDefaults() 
        .resolvers(
            ResourceResolver.provider(getClass().getResource("/app.properties")),
            ResolverProvider.of(new CustomAwsSsmResolver(buildAwsSsmClient()))
        ) 
        .build();
}
```

## Why Dynamic Proxies?

### 1. Dependency Injection

Using dynamic proxies, which implements user-defined interfaces, makes it easy to integrate with dependency injection frameworks. Just inject the user-defined interfaces to your classes and your chosen framework will handle the rest.

### 2. Testability

Another side-effect of being dependency injection friendly is that it also makes it easy to mock/stub out configurations on unit tests by using mocking frameworks or creating a stub implementation of the proxy interface.

## Default property values

Externalized Properties supports default values by using Java's default interface methods e.g.

```java
public interface ApplicationProperties {
    @ExternalizedProperty("my.property")
    default String myProperty() {
        // If "my.property" cannot be resolved, 
        // "Default Value" will be returned.
        return "Default Value";
    }

    @ExternalizedProperty("my.property")
    default String myPropertyOrDefault(String defaultValue) {
        // If "my.property" cannot be resolved, 
        // defaultValue will be returned.
        return defaultValue;
    }
}
```

## Variable Expansion

Externalized Properties supports variable expansion in property names e.g.

```java
public interface ApplicationProperties {
    @ExternalizedProperty("environment")
    default String environment() {
        return "dev";
    }

    // ${environment} will be replaced with whatever the 
    // value of the "environment" property is. 
    @ExternalizedProperty("${environment}.my.property")
    String myProperty();
}
```

## Caching

Caching can be enabled via `ExternalizedProperties.Builder`.

```java
private static ExternalizedProperties buildExternalizedProperties() {
    return ExternalizedProperties.builder()
        .withDefaults() 
        // Cache results of proxy method invocations.
        .withProxyInvocationCaching()
        // Cache proxy instances.
        .withProxyCaching()
        // Default is 30 minutes.
        .withCacheDuration(Duration.ofMinutes(10))
        .build();
}
```

## Eager Loading

Eager loading can be enabled via `ExternalizedProperties.Builder`.

```java
private static ExternalizedProperties buildExternalizedProperties() {
    return ExternalizedProperties.builder()
        .withDefaults() 
        // Eager load properties.
        .withProxyEagerLoading()
        // Default is 30 minutes.
        .withCacheDuration(Duration.ofMinutes(10))
        .build();
}
```
