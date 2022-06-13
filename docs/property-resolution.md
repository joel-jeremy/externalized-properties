# Property Resolution

Externalized Properties makes the best use of Java's strong typing by using Java's dynamic proxy feature.

It works by creating dynamic/configurable proxy instances (created at runtime by Java) that implement user-defined interfaces as facade to resolve properties.

## 🙋 [Why Dynamic Proxies?](why-dynamic-proxies.md)

## 🌟 Proxy Interface Property Mapping (via [@ExternalizedProperty](../core/src/main/java/io/github/joeljeremy7/externalizedproperties/core/ExternalizedProperty.java))  

Properties are mapped to proxy interface methods by using the [@ExternalizedProperty](../core/src/main/java/io/github/joeljeremy7/externalizedproperties/core/ExternalizedProperty.java) annotation.

```java
public interface ApplicationProperties {
    @ExternalizedProperty("java.home")
    String javaHome();

    @ExternalizedProperty("java.version")
    String javaVersion();
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

## 🌟 Non-static/Dynamic Property Names (via [@ResolverFacade](../core/src/main/java/io/github/joeljeremy7/externalizedproperties/core/ResolverFacade.java))  

Externalized Properties supports resolution of properties whose names are not known at compile time e.g.

```java
public interface ApplicationProperties {
    @ResolverFacade
    String resolve(String propertyName);

    @ResolverFacade
    int resolveInt(String propertyName);
}
```

## 🌟 Caching

Caching is enabled by default, but when not using defaults, it can be enabled via the [@ExternalizedProperties](../core/src/main/java/io/github/joeljeremy7/externalizedproperties/core/ExternalizedProperties.java) builder. All proxies created by the resulting [@ExternalizedProperties](../core/src/main/java/io/github/joeljeremy7/externalizedproperties/core/ExternalizedProperties.java) instance will cache resolved properties.

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

Eager loading is opt-in and can be enabled via the [@ExternalizedProperties](../core/src/main/java/io/github/joeljeremy7/externalizedproperties/core/ExternalizedProperties.java) builder. All proxies created by the resulting [@ExternalizedProperties](../core/src/main/java/io/github/joeljeremy7/externalizedproperties/core/ExternalizedProperties.java) instance will eagerly load properties on initialization.

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

## 🚀 Custom Resolvers

At the heart of Externalized Properties are the [Resolver](../core/src/main/java/io/github/joeljeremy7/externalizedproperties/core/Resolver.java)s. Instances of these interface are responsible for resolving requested properties.

Creating a custom resolver is as easy as implementing the [Resolver](../core/src/main/java/io/github/joeljeremy7/externalizedproperties/core/Resolver.java) interface and registering the resolver via the [ExternalizedProperties](../core/src/main/java/io/github/joeljeremy7/externalizedproperties/core/ExternalizedProperties.java) builder.

```java
public class MyCustomResolver implements Resolver {
    @Override
    public Optional<String> resolve(InvocationContext context, String propertyName) {
        return Optional.ofNullable(...);
    }
}
```

```java
public interface ApplicationProperties {
    @ExternalizedProperty("my.property")
    MyCustomType myProperty();
}
```

```java
private static void main(String[] args) {
    ExternalizedProperties externalizedProperties = ExternalizedProperties.builder()
        // Register custom resolvers here.
        .resolvers(new MyCustomResolver())
        .build();

    ApplicationProperties props = externalizedProperties.initialize(ApplicationProperties.class);

    // Resolved from MyCustomResolver.
    String myProperty = props.myProperty();
}
```
