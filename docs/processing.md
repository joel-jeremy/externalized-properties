# Processing

Externalized Properties provides a mechanism to do targeted processing of resolved properties.

This feature may be used to selectively apply transformations to properties such as automatic decryption, masking, validation, etc. This can be achieved via a combination of the [ProcessWith](../core/src/main/java/io/github/joeljeremy7/externalizedproperties/core/processing/ProcessWith.java) and [Processor](../core/src/main/java/io/github/joeljeremy7/externalizedproperties/core/Processor.java) classes.

## ✨ Targeted Processing

Externalized Properties inspects the annotations on proxy methods to see if the property should undergo processing.

To mark a proxy method (effectively the property assigned to it) as candidate for processing (via [Processor](../core/src/main/java/io/github/joeljeremy7/externalizedproperties/core/Processor.java)s), an annotation that is meta-annotated with [ProcessWith](../core/src/main/java/io/github/joeljeremy7/externalizedproperties/core/processing/ProcessWith.java)) should be used e.g.

```java
public interface ApplicationProperties {
    @ExternalizedProperty("encrypted.property.aes")
    // @Decrypt is meta-annotated with @ProcessWith. 
    // This can be verified in source code.
    @Decrypt("MyAESDecryptor")
    String aesEncryptedProperty();

    @ExternalizedProperty("encrypted.property.rsa")
    // @Decrypt is meta-annotated with @ProcessWith. 
    // This can be verified in source code.
    @Decrypt("MyRSADecryptor")
    String rsaEncryptedProperty();
}

public static void main(String[] args) {
    // Processor to decrypt @Decrypt("MyAESDecryptor")
    DecryptProcessor aesDecryptProcessor = aesDecryptProcessor();
    // Processor to decrypt @Decrypt("MyRSADecryptor")
    DecryptProcessor rsaDecryptProcessor = rsaDecryptProcessor();

    ExternalizedProperties externalizedProperties = ExternalizedProperties.builder()
        .defaults()
        .processors(aesDecryptProcessor, rsaDecryptProcessor)
        .build();

     // Proxied interface.
    ApplicationProperties props = externalizedProperties.initialize(ApplicationProperties.class);

    // Automatically decrypted via @Decrypt/DecryptProcessor.
    String decryptedAesProperty = props.aesEncryptedProperty();
    String decryptedRsaProperty = props.rsaEncryptedProperty();
}

private static ProcessorProvider<DecryptProcessor> aesDecryptProcessor() {
    return new DecryptProcessor(
        JceDecryptor.factory().symmetric(
            "MyAESDecryptor",
            "AES/GCM/NoPadding", 
            getSecretKey(),
            getGcmParameterSpec()
        )
    );
}

private static ProcessorProvider<DecryptProcessor> rsaDecryptProcessor() {
    return new DecryptProcessor(
        JceDecryptor.factory().asymmetric(
            "MyRSADecryptor",
            "RSA", 
            getPrivateKey()
        )
    );
}
```

## 🚀 Custom Processors

At the heart of Externalized Properties are the [Processor](../core/src/main/java/io/github/joeljeremy7/externalizedproperties/core/Processor.java)s. Instances of these interface are responsible for the targeted processing of resolved properties.

To create custom processors, you need to:

### 1. Create a processor by implementing the [Processor](../core/src/main/java/io/github/joeljeremy7/externalizedproperties/core/Processor.java) interface

```java
public class Base64EncodeProcessor implements Processor {
    @Override
    public String process(ProxyMethod proxyMethod, String valueToProcess) {
        return base64Encode(proxyMethod, valueToProcess);
    }
}
```

### 2. Create an annotation to target methods that should go through the processor

Requirements for custom processor annotations:  

a. The annotation should target methods  
b. The annotation should have runtime retention  
c. The annotation should be annotated with the [ProcessWith](../core/src/main/java/io/github/joeljeremy7/externalizedproperties/core/processing/ProcessWith.java) annotation

e.g.

```java
@ProcessWith(Base64EncodeProcessor.class)
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Base64Encode {}
```

### 3. Annotate proxy methods using the custom processor annotation

```java
public interface ApplicationProperties {
    @ExternalizedProperty("my.property")
    // This property will be processed by the Base64EncodeProcessor.
    @Base64Encode
    String myProperty();
}
```

### 4. Register the processor and fire away

Registration of [Processor](../core/src/main/java/io/github/joeljeremy7/externalizedproperties/core/Processor.java)s can be done through the [ExternalizedProperties](../core/src/main/java/io/github/joeljeremy7/externalizedproperties/core/ExternalizedProperties.java) builder e.g.

```java
public static void main(String[] args) {
    ExternalizedProperties externalizedProperties = ExternalizedProperties.builder()
        // Register custom processor to process @Base64Encode
        .processors(new Base64EncodeProcessor())
        .build();

    ApplicationProperties props = externalizedProperties.initialize(ApplicationProperties.class);

    // Automatically encoded to Base64.
    String base64EncodedProperty = props.myProperty();
}
```
