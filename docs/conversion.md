# Conversion

Externalized Properties has powerful support for conversion of values to various types. There are several built-in converters but it is very easy to create a custom converter by implementing the [Converter](../core/src/main/java/io/github/joeljeremy7/externalizedproperties/core/Converter.java) interface.

## 🌟 Automatic Property Conversion

To setup automatic property conversion, just set return types of the proxy interface methods to the target type. The library will handle the conversion behind the scenes - using the registered [Converter](../core/src/main/java/io/github/joeljeremy7/externalizedproperties/core/Converter.java)s e.g.

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
    ApplicationProperties props = externalizedProperties.initialize(ApplicationProperties.class);

    // Use properties.
    int timeoutInMilliseconds = props.timeoutInMilliseconds();
    CustomType customType = props.customTypeProperty();
}

private static ExternalizedProperties buildExternalizedProperties() {
    return ExternalizedProperties.builder()
        .defaults()
        .converters(new CustomTypeConverter())
        .build();
}
```

## 🌟 Conversion to Generic Types

Externalized Properties has support for generic types e.g.

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
```

Each item in the list will be converted to an `Optional<Integer>`.

## 🌟 Conversion of Arbitrary Strings (via [@Convert](../core/src/main/java/io/github/joeljeremy7/externalizedproperties/core/Convert.java))

Externalized Properties has support for dynamic conversion of String values to any type. This is made possible by the [@Convert](../core/src/main/java/io/github/joeljeremy7/externalizedproperties/core/Convert.java) annotation e.g.

(Kindly see [@Convert](../core/src/main/java/io/github/joeljeremy7/externalizedproperties/core/Convert.java) documentation to learn more about the rules of defining a converter method.)

```java
public interface ProxyInterface {
    @Convert
    <T> T convert(String valueToConvert, TypeReference<T> targetType);
    @Convert
    <T> T convert(String valueToConvert, Class<T> targetType);
}
```

Invoking the methods annotated with [@Convert](../core/src/main/java/io/github/joeljeremy7/externalizedproperties/core/Convert.java) will delegate the arguments to the registered [Converter](../core/src/main/java/io/github/joeljeremy7/externalizedproperties/core/Converter.java)s to do the conversion. The converted value will be returned by the method.
