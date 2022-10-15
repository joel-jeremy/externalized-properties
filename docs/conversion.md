# Conversion

Externalized Properties has powerful support for conversion of values to various types.

## 🌟 Out-of-the-box Conversion Support

By enabling the default converters, you get out-of-the-box conversion support for:

- Primitives
- Lists / Collections
- Arrays
- Sets
- Enums
- Date/Time types

## 🌟 Automatic Property Conversion

Externalized Properties automatically attempts to convert resolved properties to the declared proxy method (non-String) return type. The library will handle the conversion behind the scenes - using the registered [Converter](../core/src/main/java/io/github/joeljeremy/externalizedproperties/core/Converter.java)s e.g.

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

## 🌟 Conversion of Arbitrary Values

Externalized Properties has support for conversion of arbitrary String values to other types. This is made possible by the [@ConverterFacade](../core/src/main/java/io/github/joeljeremy/externalizedproperties/core/ConverterFacade.java) annotation e.g.

(Kindly see [@ConverterFacade](../core/src/main/java/io/github/joeljeremy/externalizedproperties/core/ConverterFacade.java) documentation to learn more about the rules of defining a converter facade.)

```java
public interface ProxyInterface {
  @ConverterFacade
  <T> T convert(String valueToConvert, TypeReference<T> targetType);
  @ConverterFacade
  <T> T convert(String valueToConvert, Class<T> targetType);
}
```

Invoking the methods annotated with [@ConverterFacade](../core/src/main/java/io/github/joeljeremy/externalizedproperties/core/ConverterFacade.java) will delegate the arguments to the registered [Converter](../core/src/main/java/io/github/joeljeremy/externalizedproperties/core/Converter.java)s to do the conversion. The converted value will be returned by the method.

## 🚀 Custom Converters

There are several built-in converters but it is very easy to create a custom converter by implementing the [Converter](../core/src/main/java/io/github/joeljeremy/externalizedproperties/core/Converter.java) interface and registering the converter via the [ExternalizedProperties](../core/src/main/java/io/github/joeljeremy/externalizedproperties/core/ExternalizedProperties.java) builder.

```java
public class MyCustomConverter implements Converter<MyCustomType> {
  @Override
  public boolean canConvertTo(Class<?> targetType) {
    return MyCustomType.class.equals(targetType);
  }

  @Override
  public ConversionResult<MyCustomType> convert(
      InvocationContext context, 
      String valueToConvert,
      Type targetType
  ) {
    // There is also a ConversionResult.skip() result to skip this converter and move to the next available one.
    return ConversionResult.of(MyCustomType.valueOf(valueToConvert));
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
      .converters(new MyCustomConverter())
      .build();

  ApplicationProperties props = externalizedProperties.initialize(ApplicationProperties.class);

  // Converted using MyCustomConverter.
  MyCustomType myProperty = props.myProperty();
}
```
