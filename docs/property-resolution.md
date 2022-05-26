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

(Kindly see [@ExternalizedProperty](../core/src/main/java/io/github/joeljeremy7/externalizedproperties/core/ExternalizedProperty.java) documentation to learn more about the rules of defining an externalized property method.)

We can initialize and start resolving external configurations/properties by:

```java
public static void main(String[] args) {
    ExternalizedProperties externalizedProperties = buildExternalizedProperties();

    // Proxied interface.
    ApplicationProperties props = externalizedProperties.initialize(ApplicationProperties.class);

    // Use properties.
    String javaHome = props.javaHome();
    String javaVersion = props.javaVersion();
}

private static ExternalizedProperties buildExternalizedProperties() {
    // Default resolvers include system properties and environment variable resolvers.
    return ExternalizedProperties.builder()
        .defaults() 
        .resolvers(
            new ResourceResolver(getClass().getResource("/app.properties")),
            new CustomAwsSsmResolver(buildAwsSsmClient())
        ) 
        .build();
}
```

## 🌟 Default/Fallback Values

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
        // The variable defaultValue will be returned.
        return defaultValue;
    }
}
```

## 🌟 Non-static/Dynamic Property Names

Externalized Properties supports resolution of properties whose names are not known at compile time e.g.

```java
/**
 * No property name in annotation. 
 * Property name is provided on runtime.
 */
public interface ApplicationProperties {
    @ExternalizedProperty
    String resolve(String propertyName);

    @ExternalizedProperty
    int resolveInt(String propertyName);
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

If custom variable expansion is required, the default variable expander can be overriden via `ExternalizedProperties.Builder` e.g.

```java
public static void main(String[] args) {
    ExternalizedProperties externalizedProperties = ExternalizedProperties.builder()
        .defaults() 
        .variableExpander(
            // Format: #(variable)
            new SimpleVariableExpander("#(", ")")
        )
        .build();
    
    ApplicationProperties appProperties = externalizedProperties.initialize(ApplicationProperties.class);
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
        .defaults() 
        // Cache initialized proxy instances.
        .enableInitializeCaching()
        // Cache results of proxy method invocations.
        .enableInvocationCaching()
        // Default is 30 minutes.
        .cacheDuration(Duration.ofMinutes(10))
        .build();
    
    // This proxy will cache any resolved properties.
    ApplicationProperties appProperties = externalizedProperties.initialize(ApplicationProperties.class);
}
```

## 🌟 Eager Loading

Eager loading is opt-in and can be enabled via `ExternalizedProperties.Builder`. All proxies created by the resulting `ExternalizedProperties` instance will eagerly load properties upon creation.

```java
private static void main(String[] args) {
    ExternalizedProperties externalizedProperties = ExternalizedProperties.builder()
        .defaults() 
        // Eager load properties.
        .enableEagerLoading()
        // Default is 30 minutes.
        .cacheDuration(Duration.ofMinutes(10))
        .build();

    // This proxy should already have its properties loaded.
    ApplicationProperties appProperties = externalizedProperties.initialize(ApplicationProperties.class);
}
```
