# Externalized Properties

[![Gradle Build](https://github.com/joeljeremy7/externalized-properties/actions/workflows/gradle-build.yaml/badge.svg)](https://github.com/joeljeremy7/externalized-properties/actions/workflows/gradle-build.yaml)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/io.github.joeljeremy7.externalizedproperties/core/badge.svg)](https://maven-badges.herokuapp.com/maven-central/io.github.joeljeremy7.externalizedproperties/core)
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

### 🌟 [Property Resolution](docs/property-resolution.md)

✔️ Default/Fallback Values  
✔️ Variable Expansion  
✔️ Caching  
✔️ Eager Loading

### 🌟 [Property Post-Processing](docs/property-post-processing.md)

### 🌟 [Property Conversion](docs/property-conversion.md)
