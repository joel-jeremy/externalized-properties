# 🙋 Why Dynamic Proxies?

## 📌 Dependency Injection Friendly

Since Externalized Properties works with interfaces, it makes it easy to integrate with dependency injection (DI) frameworks. it's as simple as building [ExternalizedProperties](core/src/main/java/io/github/joeljeremy7/externalizedproperties/core/ExternalizedProperties.java), initializing a dynamic proxy from an interface, and registering the proxy interface to your chosen DI framework.

## 🧪 Testing Friendly

Another side-effect of being dependency injection friendly is that it also makes it easy to mock/stub out configurations/properties on unit tests. It's as simple as creating a stub implementation of the proxy interface or using mocking frameworks to mock the proxy interface.
