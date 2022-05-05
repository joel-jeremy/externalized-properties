# Externalized Properties

[![Gradle Build](https://github.com/joeljeremy7/externalized-properties/actions/workflows/gradle-build.yaml/badge.svg)](https://github.com/joeljeremy7/externalized-properties/actions/workflows/gradle-build.yaml)
[![Coverage Status](https://coveralls.io/repos/github/joeljeremy7/externalized-properties/badge.svg?branch=main)](https://coveralls.io/github/joeljeremy7/externalized-properties?branch=main)
[![License](https://img.shields.io/badge/License-Apache_2.0-blue.svg)](https://github.com/joeljeremy7/externalized-properties/blob/main/LICENSE)

A lightweight and extensible library to resolve application properties from various external sources.

## [Twelve Factor Methodology](https://12factor.net)

Externalized Properties was inspired by the [The Twelve Factor Methodology](https://12factor.net)'s section [III. Config](https://12factor.net/config).  

The goal of this library is to make it easy for applications to implement configuration best practices by providing easy-to-use APIs as well as providing the flexibility to choose where to store their configurations/properties.

## Wiki

- For more information and examples please browse through the wiki: <https://github.com/joeljeremy7/externalized-properties/wiki>

## Getting Started

### Gradle

```gradle
implementation 'io.github.joeljeremy7.externalizedproperties:core:1.0.0-SNAPSHOT'
```

### Maven

```xml
<dependency>
    <groupId>io.github.joeljeremy7.externalizedproperties</groupId>
    <artifactId>core</artifactId>
    <version>1.0.0-SNAPSHOT</version>
</dependency>
```

### Java 9 Module Names

Externalized Properties jars are published with Automatic-Module-Name manifest attribute:

- Core - `io.github.joeljeremy7.externalizedproperties.core`
- Database Resolver - `io.github.joeljeremy7.externalizedproperties.resolvers.database`

Module authors can use above module names in their module-info.java:

```java
module foo.bar {
    requires io.github.joeljeremy7.externalizedproperties.core;
    requires io.github.joeljeremy7.externalizedproperties.resolvers.database;
}
```

## Sample Projects

Sample projects can be found in: <https://github.com/joeljeremy7/externalized-properties-samples>

## Features

Externalized Properties makes the best use of Java's strong typing by proxying an interface and using that as a facade to resolve properties.

### Interface Proxying

Given an interface:

```java
public interface ApplicationProperties {
    @ExternalizedProperty("database.url")
    String databaseUrl();
    @ExternalizedProperty("database.driver")
    String databaseDriver();
}
```

We can initialize and start resolving external configurations/properties by:

```java
public static void main(String[] args) {
    ExternalizedProperties externalizedProperties = buildExternalizedProperties();

    // Proxied interface.
    ApplicationProperties props = externalizedProperties.proxy(ApplicationProperties.class);

    // Use properties.
    String databaseUrl = props.databaseUrl();
    String databaseDriver = props.databaseDriver();

    System.out.println("Database URL: " + databaseUrl);
    System.out.println("Database Driver: " + databaseDriver);
}

private ExternalizedProperties buildExternalizedProperties() {
    // Create the ExternalizedProperties instance with default and additional resolvers.
    // Default resolvers include system properties and environment variable resolvers.

    return ExternalizedProperties.builder()
        .withDefaultResolvers() 
        .resolvers(
            ResourceResolver.provider(getClass().getResource("/app.properties")),
            ResourceResolver.provider(
                getClass().getResource("/app.yaml"),
                // There is no built-in YamlReader class.
                // See: core/src/test/java/io/github/joeljeremy7/externalizedproperties/core/resolvers/resourcereaders
                new YamlReader()
            ),
            // DatabaseResolver is not part of the core module. It is part of a separate resolver-database module.
            DatabaseResolver.provider(new JdbcConnectionProvider(getDataSource())),
            // CustomAwsSsmResolver is an example custom resolver implementation which resolves properties from AWS SSM.
            ResolverProvider.of(new CustomAwsSsmResolver(buildAwsSsmClient()))
        ) 
        .build();
}
```

### Property Processing

Externalized Properties provides a mechanism to selectively apply post-processing to resolved properties.  
This feature may be used to apply transformations to resolved properties such as automatic decryption, masking, validation, etc. This can be achieved via a combination of annotations and `Processor`s.

e.g.

```java
public interface ApplicationProperties {
    @ExternalizedProperty("encrypted.property.aes")
    @Decrypt("MyAESDecryptor")
    String aesEncryptedProperty();

    @ExternalizedProperty("encrypted.property.rsa")
    @Decrypt("MyRSADecryptor")
    String rsaEncryptedProperty();
}

public static void main(String[] args) {
    ExternalizedProperties externalizedProperties = ExternalizedProperties.builder()
        .withDefaultResolvers()
        .processors(
            DecryptProcessor.provider(JceDecryptor.factory().symmetric(
                "MyAESDecryptor",
                "AES/GCM/NoPadding", 
                getSecretKey(),
                getGcmParameterSpec()
            )),
            DecryptProcessor.provider(JceDecryptor.factory().asymmetric(
                "MyRSADecryptor",
                "RSA", 
                getPrivateKey()
            ))
        )
        .build();

     // Proxied interface.
    ApplicationProperties props = externalizedProperties.proxy(ApplicationProperties.class);

    // Automatically decrypted via @Decrypt/DecryptProcessor.
    String decryptedAes = props.aesEncryptedProperty();
    String decryptedRsa = props.rsaEncryptedProperty();

    System.out.println("Decrypted property: " + decryptedAes)
    System.out.println("Decrypted property: " + decryptedRsa)
}
```

Custom processing can be achieved in 3 steps:

1. Create a processor by implemention the `Processor` interface.

    ```java
    public class Base64EncodeProcessor implements Processor {
        @Override
        public String process(ProxyMethod proxyMethod, String valueToProcess) {
            return base64Encode(proxyMethod, valueToProcess);
        }
    }
    ```

2. Create an annotation to annotate methods that should go through the processor.

    Requirements for custom processor annotations:  
    a. The annotation should target methods  
    b. The annotation should have runtime retention  
    c. The annotation should be annotated with @ProcessorWith(...) annotation

    e.g.

    ```java
    @ProcessWith(Base64EncodeProcessor.class)
    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.RUNTIME)
    public @interface Base64Encode {}
    ```

3. Register the processor and use custom processor annotation

    e.g.

    ```java
    public interface ApplicationProperties {
        @ExternalizedProperty("my.property")
        @Base64Encode
        String myProperty();
    }
    
    public static void main(String[] args) {
        ExternalizedProperties externalizedProperties = ExternalizedProperties.builder()
            .withDefaultResolvers()
            .processors(
                // Register custom processor to process @Base64Encode
                ProcessorProvider.of(new Base64EncodeProcessor())
            )
            .build();

        ApplicationProperties props = externalizedProperties.proxy(ApplicationProperties.class);

        // Automatically encoded to Base64.
        String base64EncodedProperty = props.myProperty();

        System.out.println("Base64 encoded property: " + base64EncodedProperty);
    }
    ```

### Property Conversion

Externalized Properties has powerful support for conversion of properties to various types. There are several built-in converters but it is very easy to create a custom converter by implementing the `Converter` interface.

To register converters to the library, it must be done through the builder:

```java
private ExternalizedProperties buildExternalizedProperties() {
    return ExternalizedProperties.builder()
        .withDefaultResolvers()
        .withDefaultConverters()
        .converters(
            ConverterProvider.of(new CustomTypeConverter())
        )
        .build();
}
```

To convert a property via the proxy interface, just set the method return type to the target type, and the library will handle the conversion behind the scenes - using the registered converters.

```java
public interface ApplicationProperties {
    @ExternalizedProperty("timeout.millis")
    int timeoutInMilliseconds();
}

public static void main(String[] args) {
    ExternalizedProperties externalizedProperties = buildExternalizedProperties();

    // Proxied interface.
    ApplicationProperties props = externalizedProperties.proxy(ApplicationProperties.class);

    // Use properties.
    int timeoutInMilliseconds = props.timeoutInMilliseconds();

    System.out.println("Timeout in milliseconds: " + timeoutInMilliseconds);
}
```

### Conversion to Generic Types

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
