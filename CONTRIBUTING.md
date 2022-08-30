# Contributing Guidelines

First of all, thank you for having the interest in contributing to this project!

## Found a bug?

Please create an issue describing the following:

- The version the bug was discovered.
- A scenario to reproduce the bug.

## Enhancement ideas?

Got an idea to enhance the library? Please feel free to create an issue describing the feature proposal. Any ideas are welcome! :)

## Build

The project uses Java 11 as runtime for Gradle but compiles source code to Java 8.

To build the project, run the command:

```sh
./gradlew clean build
```

To create reports, run the commands:

```sh
./gradlew clean build testAggregateTestReport
```

```sh
./gradlew clean build testCodeCoverageReport
```

Tests are run in multiple JVM runtimes. By default, it is run in LTS versions (succeeding the version used in source compilation) + the latest released non-LTS version. Test runtimes are overrideable by setting the `ADDITIONAL_TEST_RUNS_ON_JVM_VERSIONS` environment variable or `additionalTestRunsOnJvmVersions` system property e.g. `ADDITIONAL_TEST_RUNS_ON_JVM_VERSIONS=8,17,18` / `additionalTestRunsOnJvmVersions=8,17,18`.

## Development Guidelines

### Git Branching Strategy

The project follows the [GitHub flow](https://docs.github.com/en/get-started/quickstart/github-flow) branching strategy.

### Unit Test Structure

Unit tests in this project follow a specific structure.

- Classes must have a corresponding test class i.e. `MapResolver` -> `MapResolverTests`. The test class must be in the exact same java package as the class it corresponds to.
- Test classes are nested in structure. Each method in the class under test must have a corresponding `@Nested` test class. Each `@Nested` test class must test scenarios that is supported by the method it corresponds to.

    ```java
    // Class under test: io.github.joeljeremy7.externalizedproperties.resolver.my.MyResolver
    class MyResolver implements Resolver {
        public MyResolver(...) {
            ...
        }
        
        public Optional<String> resolve(InvocationContext context, String propertyName) {
            ...
        }

        public String someOtherMethod(...) {
            ...
        }

        public static class Builder {
            ...
            public MyResolver build() {
                ...
            }
        }
    }

    // Test class: io.github.joeljeremy7.externalizedproperties.resolver.my.MyResolverTests
    class MyResolverTests {
        @Nested
        class Constructor {
            // @Test methods here...
        }

        @Nested
        class ResolveMethod {
            // @Test methods here...
        }

        @Nested
        class SomeOtherMethod {
            // @Test methods here...
        }

        // Nested class must also have corresponding test classes
        @Nested
        class BuilderTests {
            ...
            @Nested
            class BuildMethod {
                // @Test methods here...
            }
        }
    }
    ```
