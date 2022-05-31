# Variable Expansion

Externalized Properties has support for expansion of variables in externalized property names and/or any String values. This is made possible by [VariableExpander](../core/src/main/java/io/github/joeljeremy7/externalizedproperties/core/VariableExpander.java)s. By default, a simple implementation is already enabled. If a custom/more powerful variable expansion implementation is necessary, a custom variable expander can be created by implementing the [VariableExpander](../core/src/main/java/io/github/joeljeremy7/externalizedproperties/core/VariableExpander.java) interface and registering it to Externalized Properties.

## 🌟 Conversion of Arbitrary Strings (via [@ExpandVariables](../core/src/main/java/io/github/joeljeremy7/externalizedproperties/core/ExpandVariables.java))

Externalized Properties can create proxies that expand variables in any String values. This is made possible by the [@ExpandVariables](../core/src/main/java/io/github/joeljeremy7/externalizedproperties/core/ExpandVariables.java) annotation e.g.

(Kindly see [@ExpandVariables](../core/src/main/java/io/github/joeljeremy7/externalizedproperties/core/ExpandVariables.java) documentation to learn more about the rules of defining a variable expander method.)

```java
public interface ProxyInterface {
    @ExpandVariables
    String expandVariables(String value);
}
```

Invoking the methods annotated with [@ExpandVariables](../core/src/main/java/io/github/joeljeremy7/externalizedproperties/core/ExpandVariables.java) will delegate the arguments to the registered [VariableExpander](../core/src/main/java/io/github/joeljeremy7/externalizedproperties/core/VariableExpander.java) to expand any variables in the String value. The expanded value will be returned by the method.
