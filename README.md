# Externalized Properties

[![Gradle Build](https://github.com/joeljeremy7/externalized-properties/actions/workflows/gradle-build.yaml/badge.svg)](https://github.com/joeljeremy7/externalized-properties/actions/workflows/gradle-build.yaml)
[![Coverage Status](https://coveralls.io/repos/github/joeljeremy7/externalized-properties/badge.svg?branch=main)](https://coveralls.io/github/joeljeremy7/externalized-properties?branch=main)
[![Known Vulnerabilities](https://snyk.io/test/github/joeljeremy7/externalized-properties/badge.svg)](https://snyk.io/test/github/joeljeremy7/externalized-properties)
[![License](https://img.shields.io/badge/License-Apache_2.0-blue.svg)](https://github.com/joeljeremy7/externalized-properties/blob/main/LICENSE)
[![Total alerts](https://img.shields.io/lgtm/alerts/g/joeljeremy7/externalized-properties.svg?logo=lgtm&logoWidth=18)](https://lgtm.com/projects/g/joeljeremy7/externalized-properties/alerts/)
[![Language grade: Java](https://img.shields.io/lgtm/grade/java/g/joeljeremy7/externalized-properties.svg?logo=lgtm&logoWidth=18)](https://lgtm.com/projects/g/joeljeremy7/externalized-properties/context:java)
[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=io.github.joeljeremy7.externalizedproperties&metric=alert_status)](https://sonarcloud.io/summary/new_code?id=io.github.joeljeremy7.externalizedproperties)
[![Maintainability Rating](https://sonarcloud.io/api/project_badges/measure?project=io.github.joeljeremy7.externalizedproperties&metric=sqale_rating)](https://sonarcloud.io/summary/new_code?id=io.github.joeljeremy7.externalizedproperties)
[![Reliability Rating](https://sonarcloud.io/api/project_badges/measure?project=io.github.joeljeremy7.externalizedproperties&metric=reliability_rating)](https://sonarcloud.io/summary/new_code?id=io.github.joeljeremy7.externalizedproperties)
[![Security Rating](https://sonarcloud.io/api/project_badges/measure?project=io.github.joeljeremy7.externalizedproperties&metric=security_rating)](https://sonarcloud.io/summary/new_code?id=io.github.joeljeremy7.externalizedproperties)
[![Vulnerabilities](https://sonarcloud.io/api/project_badges/measure?project=io.github.joeljeremy7.externalizedproperties&metric=vulnerabilities)](https://sonarcloud.io/summary/new_code?id=io.github.joeljeremy7.externalizedproperties)

A lightweight and extensible library to resolve application properties from various external sources.

## [Twelve Factor Methodology](https://12factor.net)

Externalized Properties was inspired by the [The Twelve Factor Methodology](https://12factor.net)'s section [III. Config](https://12factor.net/config).  

The goal of this library is to make it easy for applications to implement configuration best practices by providing easy-to-use APIs as well as providing the flexibility to choose where to store their configurations/properties.

## Getting Started

### Gradle

```gradle
implementation 'io.github.joeljeremy7.externalizedproperties:core:${version}'
```

### Maven

```xml
<dependency>
    <groupId>io.github.joeljeremy7.externalizedproperties</groupId>
    <artifactId>core</artifactId>
    <version>${version}</version>
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

## Features

Externalized Properties makes the best use of Java's strong typing by proxying an interface and using that as a facade to resolve properties.

### Dynamic Proxies

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

    System.out.println("java.home: " + javaHome);
    System.out.println("java.version: " + javaVersion);
}

private static ExternalizedProperties buildExternalizedProperties() {
    // Create the ExternalizedProperties instance with default and additional resolvers.
    // Default resolvers include system properties and environment variable resolvers.
    
    return ExternalizedProperties.builder()
        .withDefaults() 
        .resolvers(
            ResourceResolver.provider(getClass().getResource("/app.properties")),
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
    // Processor to decrypt @Decrypt("MyAESDecryptor")
    ProcessorProvider<DecryptProcessor> aesDecryptProcessor = aesDecryptProcessor();
    // Processor to decrypt @Decrypt("MyRSADecryptor")
    ProcessorProvider<DecryptProcessor> rsaDecryptProcessor = rsaDecryptProcessor();

    ExternalizedProperties externalizedProperties = ExternalizedProperties.builder()
        .withDefaults()
        .processors(aesDecryptProcessor, rsaDecryptProcessor)
        .build();

     // Proxied interface.
    ApplicationProperties props = externalizedProperties.proxy(ApplicationProperties.class);

    // Automatically decrypted via @Decrypt/DecryptProcessor.
    String decryptedAesProperty = props.aesEncryptedProperty();
    String decryptedRsaProperty = props.rsaEncryptedProperty();

    System.out.println("Decrypted AES encrypted property: " + decryptedAesProperty)
    System.out.println("Decrypted RSA encrypted property: " + decryptedRsaProperty)
}

private static ProcessorProvider<DecryptProcessor> aesDecryptProcessor() {
    return DecryptProcessor.provider(
        JceDecryptor.factory().symmetric(
            "MyAESDecryptor",
            "AES/GCM/NoPadding", 
            getSecretKey(),
            getGcmParameterSpec()
        )
    );
}

private static ProcessorProvider<DecryptProcessor> rsaDecryptProcessor() {
    return DecryptProcessor.provider(
        JceDecryptor.factory().asymmetric(
            "MyRSADecryptor",
            "RSA", 
            getPrivateKey()
        )
    );
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
    c. The annotation should be annotated with @ProcessWith(...) annotation

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
            .withDefaults()
            // Register custom processor to process @Base64Encode
            .processors(ProcessorProvider.of(new Base64EncodeProcessor()))
            .build();

        ApplicationProperties props = externalizedProperties.proxy(ApplicationProperties.class);

        // Automatically encoded to Base64.
        String base64EncodedProperty = props.myProperty();

        System.out.println("Base64 encoded property: " + base64EncodedProperty);
    }
    ```

### Property Conversion

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

    System.out.println("Timeout in milliseconds: " + timeoutInMilliseconds);
    System.out.println("Custom type property: " + customType);
}

private static ExternalizedProperties buildExternalizedProperties() {
    return ExternalizedProperties.builder()
        .withDefaults()
        .converters(ConverterProvider.of(new CustomTypeConverter()))
        .build();
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
