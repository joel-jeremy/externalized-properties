# Property Resolution

Externalized Properties makes the best use of Java's strong typing by using Java's dynamic proxy feature.

It works by creating dynamic/configurable proxy instances (created at runtime by Java) that implement user-defined interfaces as facade to resolve properties.

## 🙋 [Why Dynamic Proxies?](why-dynamic-proxies.md)

## 🏎️ Quick Start

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

## 🌟 Default property values

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

## 🌟 Variable Expansion

Variable expansion is supported in property names and is enabled by default e.g.

```java
public interface ApplicationProperties {
    @ExternalizedProperty("environment")
    default String environment() {
        return "dev";
    }

    // ${environment} will be replaced with whatever the 
    // value of the "environment" property is e.g. dev.my.property
    @ExternalizedProperty("${environment}.my.property")
    String myProperty();
}
```

If custom variable expansion if required, the default variable expander can be overriden via `ExternalizedProperties.Builder` e.g.

```java
public static void main(String[] args) {
    ExternalizedProperties externalizedProperties = ExternalizedProperties.builder()
        .withDefaults() 
        .variableExpander(
            // Format: #(variable)
            SimpleVariableExpander.provider("#(", ")")
        )
        .build();
    
    ApplicationProperties appProperties = externalizedProperties.proxy(ApplicationProperties.class);
}
```

Built-in variable expander implementations:

- [SimpleVariableExpander](../core/src/main/java/io/github/joeljeremy7/externalizedproperties/core/variableexpansion/SimpleVariableExpander.java) - Uses a speficied prefix and suffix to match variables.
- [PatternVariableExpander](../core/src/main/java/io/github/joeljeremy7/externalizedproperties/core/variableexpansion/PatternVariableExpander.java) - Uses a regex to match variables.
- [NoOpVariableExpander](../core/src/main/java/io/github/joeljeremy7/externalizedproperties/core/variableexpansion/NoOpVariableExpander.java) - Disables variable expansion.

## 🌟 Caching

Caching is enabled by default, but when not using defaults, it can be enabled via `ExternalizedProperties.Builder`. All proxies created by the resulting `ExternalizedProperties` instance will cache any resolved properties.

```java
public static void main(String[] args) {
    ExternalizedProperties externalizedProperties = ExternalizedProperties.builder()
        .withDefaults() 
        // Cache results of proxy method invocations.
        .withProxyInvocationCaching()
        // Cache proxy instances.
        .withProxyCaching()
        // Default is 30 minutes.
        .withCacheDuration(Duration.ofMinutes(10))
        .build();
    
    // This proxy will cache any resolved properties.
    ApplicationProperties appProperties = externalizedProperties.proxy(ApplicationProperties.class);
}
```

## 🌟 Eager Loading

Eager loading is opt-in and can be enabled via `ExternalizedProperties.Builder`. All proxies created by the resulting `ExternalizedProperties` instance will eagerly load properties upon creation.

```java
private static void main(String[] args) {
    ExternalizedProperties externalizedProperties = ExternalizedProperties.builder()
        .withDefaults() 
        // Eager load properties.
        .withProxyEagerLoading()
        // Default is 30 minutes.
        .withCacheDuration(Duration.ofMinutes(10))
        .build();

    // This proxy should already have its properties loaded.
    ApplicationProperties appProperties = externalizedProperties.proxy(ApplicationProperties.class);
}
```
