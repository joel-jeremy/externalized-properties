# Property Conversion

Externalized Properties has powerful support for conversion of properties to various types. There are several built-in converters but it is very easy to create a custom converter by implementing the `Converter` interface.

To convert a property via the proxy interface, just set the method return type to the target type, and the library will handle the conversion behind the scenes - using the registered converters.

```java
public interface ApplicationProperties {
    @ExternalizedProperty("timeout.millis")
    int timeoutInMilliseconds();

    @ExternalizedProperty("custom.type.property")
    CustomType customTypeProperty();
}

public static void main(String[] args) {
    ExternalizedProperties externalizedProperties = buildExternalizedProperties();

    // Proxied interface.
    ApplicationProperties props = externalizedProperties.proxy(ApplicationProperties.class);

    // Use properties.
    int timeoutInMilliseconds = props.timeoutInMilliseconds();
    CustomType customType = props.customTypeProperty();
}

private static ExternalizedProperties buildExternalizedProperties() {
    return ExternalizedProperties.builder()
        .withDefaults()
        .converters(ConverterProvider.of(new CustomTypeConverter()))
        .build();
}
```

## Conversion to Generic Types

Externalized Properties has support for generic types. Given the proxy interface:

```java
public interface ApplicationProperties {
    @ExternalizedProperty("list-of-numbers")
    List<Integer> listOfNumbers();
}
```

Externalized Properties is capable of converting each item from the `list-of-numbers` property to an Integer (provided a converter is registered to convert to an Integer).

An arbitraty generic type parameter depth is supported. For example,

```java
public interface ApplicationProperties {
    @ExternalizedProperty("list-of-numbers")
    List<Optional<Integer>> listOfOptionalNumbers();
}
````

Each item in the list will be converted to an `Optional<Integer>`.
