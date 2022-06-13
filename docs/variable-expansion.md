# Variable Expansion

Externalized Properties has support for expansion of variables in externalized property names and/or any String values. This is made possible by [VariableExpander](../core/src/main/java/io/github/joeljeremy7/externalizedproperties/core/VariableExpander.java)s. By default, a simple implementation is already enabled. If a custom/more powerful variable expansion implementation is necessary, a custom variable expander can be created by implementing the [VariableExpander](../core/src/main/java/io/github/joeljeremy7/externalizedproperties/core/VariableExpander.java) interface and registering it to Externalized Properties.

## 🌟 Automatic Variable Expansion in Property Names

Variable expansion is supported in property names and is enabled by default e.g.

```java
public interface ApplicationProperties {
    @ExternalizedProperty("environment")
    default String environment() {
        return "dev";
    }

    // ${environment} will be replaced with whatever the 
    // value of the "environment" property is e.g. dev.my.property
    @ExternalizedProperty("${environment}.my.property")
    String myProperty();
}
```

If custom variable expansion is required, the default variable expander can be overriden via `ExternalizedProperties.Builder` e.g.

```java
public static void main(String[] args) {
    ExternalizedProperties externalizedProperties = ExternalizedProperties.builder()
        .defaults()
        // Format: #(variable)
        .variableExpander(new SimpleVariableExpander("#(", ")"))
        .build();
    
    ApplicationProperties appProperties = externalizedProperties.initialize(ApplicationProperties.class);
}
```

Built-in variable expander implementations:

- [SimpleVariableExpander](../core/src/main/java/io/github/joeljeremy7/externalizedproperties/core/variableexpansion/SimpleVariableExpander.java) - Uses a speficied prefix and suffix to match variables.
- [PatternVariableExpander](../core/src/main/java/io/github/joeljeremy7/externalizedproperties/core/variableexpansion/PatternVariableExpander.java) - Uses a regex to match variables.
- [NoOpVariableExpander](../core/src/main/java/io/github/joeljeremy7/externalizedproperties/core/variableexpansion/NoOpVariableExpander.java) - Disables variable expansion.

## 🌟 Dynamic Variable Expansion (via [@VariableExpanderFacade](../core/src/main/java/io/github/joeljeremy7/externalizedproperties/core/VariableExpanderFacade.java))

Externalized Properties can create proxies that expand variables in any String values. This is made possible by the [@VariableExpanderFacade](../core/src/main/java/io/github/joeljeremy7/externalizedproperties/core/VariableExpanderFacade.java) annotation e.g.

(Kindly see [@VariableExpanderFacade](../core/src/main/java/io/github/joeljeremy7/externalizedproperties/core/VariableExpanderFacade.java) documentation to learn more about the rules of defining a variable expander method.)

```java
public interface ProxyInterface {
    @VariableExpanderFacade
    String expandVariables(String value);
}
```

Invoking the methods annotated with [@VariableExpanderFacade](../core/src/main/java/io/github/joeljeremy7/externalizedproperties/core/VariableExpanderFacade.java) will delegate the arguments to the registered [VariableExpander](../core/src/main/java/io/github/joeljeremy7/externalizedproperties/core/VariableExpander.java) to expand any variables in the String value. The expanded value will be returned by the method.
