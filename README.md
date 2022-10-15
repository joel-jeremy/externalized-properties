# Externalized Properties

[![Gradle Build](https://github.com/joel-jeremy/externalized-properties/actions/workflows/gradle-build.yaml/badge.svg)](https://github.com/joel-jeremy/externalized-properties/actions/workflows/gradle-build.yaml)
[![CodeQL](https://github.com/joel-jeremy/externalized-properties/actions/workflows/codeql.yaml/badge.svg)](https://github.com/joel-jeremy/externalized-properties/actions/workflows/codeql.yaml)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/io.github.joel-jeremy.externalized-properties/core/badge.svg)](https://search.maven.org/search?q=g:%22io.github.joel-jeremy.externalized-properties%22)
[![Coverage Status](https://coveralls.io/repos/github/joel-jeremy/externalized-properties/badge.svg?branch=main)](https://coveralls.io/github/joel-jeremy/externalized-properties?branch=main)
[![Known Vulnerabilities](https://snyk.io/test/github/joel-jeremy/externalized-properties/badge.svg)](https://snyk.io/test/github/joel-jeremy/externalized-properties)
[![License](https://img.shields.io/badge/License-Apache_2.0-blue.svg)](https://github.com/joel-jeremy/externalized-properties/blob/main/LICENSE)
[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=io.github.joel-jeremy.externalized-properties&metric=alert_status)](https://sonarcloud.io/summary/new_code?id=io.github.joel-jeremy.externalized-properties)
[![Maintainability Rating](https://sonarcloud.io/api/project_badges/measure?project=io.github.joel-jeremy.externalized-properties&metric=sqale_rating)](https://sonarcloud.io/summary/new_code?id=io.github.joel-jeremy.externalized-properties)
[![Reliability Rating](https://sonarcloud.io/api/project_badges/measure?project=io.github.joel-jeremy.externalized-properties&metric=reliability_rating)](https://sonarcloud.io/summary/new_code?id=io.github.joel-jeremy.externalized-properties)
[![Security Rating](https://sonarcloud.io/api/project_badges/measure?project=io.github.joel-jeremy.externalized-properties&metric=security_rating)](https://sonarcloud.io/summary/new_code?id=io.github.joel-jeremy.externalized-properties)
[![Vulnerabilities](https://sonarcloud.io/api/project_badges/measure?project=io.github.joel-jeremy.externalized-properties&metric=vulnerabilities)](https://sonarcloud.io/summary/new_code?id=io.github.joel-jeremy.externalized-properties)
[![Coverage](https://sonarcloud.io/api/project_badges/measure?project=io.github.joel-jeremy.externalized-properties&metric=coverage)](https://sonarcloud.io/summary/new_code?id=io.github.joel-jeremy.externalized-properties)
[![Discord](https://img.shields.io/discord/1025648239162175578.svg?logo=discord&logoColor=white&logoWidth=20&labelColor=7289DA&label=Discord&color=17cf48)](https://discord.gg/SVfahQGMmx)

A lightweight and extensible library to resolve application properties from various external sources.

## [Twelve Factor Methodology](https://12factor.net)

Externalized Properties was inspired by the [The Twelve Factor Methodology](https://12factor.net)'s section [III. Config](https://12factor.net/config).  

The goal of this library is to make it easy for applications to implement configuration best practices by providing easy-to-use APIs as well as providing the flexibility to choose where to store their configurations/properties.

## 🛠️ Installation

### Gradle

```groovy
implementation "io.github.joel-jeremy.externalized-properties:externalized-properties-core:${version}"
// Optional/additional resolvers
implementation "io.github.joel-jeremy.externalized-properties:externalized-properties-database:${version}"
implementation "io.github.joel-jeremy.externalized-properties:externalized-properties-git:${version}"
```

### Maven

```xml
<dependency>
  <groupId>io.github.joel-jeremy.externalized-properties</groupId>
  <artifactId>externalized-properties-core</artifactId>
  <version>${version}</version>
</dependency>
<!-- Optional/additional resolvers -->
<dependency>
  <groupId>io.github.joel-jeremy.externalized-properties</groupId>
  <artifactId>externalized-properties-database</artifactId>
  <version>${version}</version>
</dependency>
<dependency>
  <groupId>io.github.joel-jeremy.externalized-properties</groupId>
  <artifactId>externalized-properties-git</artifactId>
  <version>${version}</version>
</dependency>
```

### 🧩 Java 9 Module Names

Externalized Properties jars are published with Automatic-Module-Name manifest attribute:

- Core - `io.github.joeljeremy.externalizedproperties.core`
- Database Resolver - `io.github.joeljeremy.externalizedproperties.database`
- Git Resolver - `io.github.joeljeremy.externalizedproperties.git`

Module authors can use above module names in their module-info.java:

```java
module foo.bar {
  requires io.github.joeljeremy.externalizedproperties.core;
  requires io.github.joeljeremy.externalizedproperties.database;
  requires io.github.joeljeremy.externalizedproperties.git;
}
```

## 🌟 Features

Externalized Properties takes full advantage of Java's [Dynamic Proxies](https://docs.oracle.com/javase/8/docs/technotes/guides/reflection/proxy.html) ([Why Dynamic Proxies?](docs/why-dynamic-proxies.md)).

### ✔️ Property Resolution

- [Map Properties to Dynamic Proxy Interface Methods](docs/property-resolution.md#-map-properties-to-dynamic-proxy-interface-methods)
  - via [@ExternalizedProperty](core/src/main/java/io/github/joeljeremy/externalizedproperties/core/ExternalizedProperty.java)
- [Default/Fallback Values](docs/property-resolution.md#-defaultfallback-values)  
- [Support for Property Names Known at Runtime](docs/property-resolution.md#-support-for-property-names-known-at-runtime)
  - via [@ResolverFacade](core/src/main/java/io/github/joeljeremy/externalizedproperties/core/ResolverFacade.java)
- [Support for Various Configuration File/Resource Formats](docs/property-resolution.md#-support-for-various-configuration-fileresource-formats)  
- [Caching](docs/property-resolution.md#-caching)  
- [Eager Loading](docs/property-resolution.md#-eager-loading)  
- [Custom Resolvers](docs/property-resolution.md#-custom-resolvers)  

### ✔️ Conversion

- [Automatic Property Conversion](docs/conversion.md#-automatic-property-conversion)  
- [Conversion to Generic Types](docs/conversion.md#-conversion-to-generic-types)  
- [Conversion of Arbitrary Values](docs/conversion.md#-conversion-of-arbitrary-values)
  - via [@ConverterFacade](core/src/main/java/io/github/joeljeremy/externalizedproperties/core/ConverterFacade.java)
- [Custom Converters](docs/conversion.md#-custom-converters)

### ✔️ Variable Expansion

- [Automatic Variable Expansion in Property Names](docs/variable-expansion.md#-automatic-variable-expansion-in-property-names)  
- [Automatic Variable Expansion in Properties](docs/variable-expansion.md#-automatic-variable-expansion-in-properties)  
- [Variable Expansion in Arbitrary Values](docs/variable-expansion.md#-variable-expansion-in-arbitrary-values)
  - via [@VariableExpanderFacade](core/src/main/java/io/github/joeljeremy/externalizedproperties/core/VariableExpanderFacade.java)

### ✔️ Processing

- [Targeted Processing of Properties](docs/processing.md#-targeted-processing-of-properties)
- [Custom Processors](docs/processing.md#-custom-processors)

### ✔️ Profiles

- [Profile-Specific Configurations](docs/profiles.md#-profile-specific-configurations)

### ✔️ Ordinal Components

- [Ordinal Resolvers](docs/ordinal-components.md#-ordinal-resolvers)  
- [Ordinal Converters](docs/ordinal-components.md#-ordinal-converters)

## 🏎️ Quick Start

Properties are mapped to proxy interface methods by using the [@ExternalizedProperty](core/src/main/java/io/github/joeljeremy/externalizedproperties/core/ExternalizedProperty.java) annotation.

(For more advanced scenarios, please see the feature documentations.)

Given an interface:

```java
public interface ApplicationProperties {
  @ExternalizedProperty("java.home")
  String javaHome();

  @ExternalizedProperty("encrypted.property")
  @Decrypt("MyDecryptor")
  String encryptedProperty();

  @ExternalizedProperty("java.version")
  int javaVersion();
}
```

We can initialize and start resolving external configurations/properties by:

```java
public static void main(String[] args) {
  // 1. Configure and build the ExternalizedProperties instance.
  ExternalizedProperties externalizedProperties = buildExternalizedProperties();

  // 2. Initialize a proxy using the ExternalizedProperties.
  ApplicationProperties props = externalizedProperties.initialize(ApplicationProperties.class);

  // 3. Resolve the properties.
  String javaHome = props.javaHome();
  String encryptedProperty = props.encryptedProperty();
  int javaVersion = props.javaVersion();
}

private static ExternalizedProperties buildExternalizedProperties() {
  return ExternalizedProperties.builder()
      .defaults() 
      .resolvers(...)
      .processors(...)
      .converters(...) 
      .build();
}
```

## 🧪 Samples

More sample can be found here: <https://github.com/joel-jeremy/externalized-properties-samples>
