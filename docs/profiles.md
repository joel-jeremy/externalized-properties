# Profiles

Externalized Properties has the concept of profiles. Applications typically are deployed to multiple environments and more often than not, these environment needs different configurations. This is where profiles can help. It allows applications to define different configurations per environment e.g.

```java
public static void main(String[] args) {
    ExternalizedProperties externalizedProperties = ExternalizedProperties.builder()
        .onProfiles("test").apply(new MyTestProfileConfigurator())
        .onProfiles("staging").apply(new MyStagingProfileConfigurator())
        .onProfiles("prod").apply(new MyProdProfileConfigurator())
        // This will be applied to both test and staging.
        .onProfiles("test", "staging").apply(new MyNonProdProfileConfigurator())
        // If there are no specified profiles, this will treated as a wildcard 
        // profile configuration and will be applied to any profiles. 
        // However, if no active profile is set, this will not be applied.
        .onProfiles().apply(new MyWildcardProfileConfigurator())
        .build();
}

public class MyTestProfileConfigurator implements ProfileConfigurator {
    @Override
    public void configure(String activeProfile, BuilderConfiguration builder) {
        // activeProfile is "test".

        builder.resolvers(...)
            .converters(...)
            .processors(...);
    }
}

public class MyStagingProfileConfigurator implements ProfileConfigurator {
    @Override
    public void configure(String activeProfile, BuilderConfiguration builder) {
        // activeProfile is "staging".

        builder.resolvers(...)
            .converters(...)
            .processors(...);
    }
}

public class MyProdProfileConfigurator implements ProfileConfigurator {
    @Override
    public void configure(String activeProfile, BuilderConfiguration builder) {
        // activeProfile is "prod".

        builder.resolvers(...)
            .converters(...)
            .processors(...);
    }
}

public class MyNonProdProfileConfigurator implements ProfileConfigurator {
    @Override
    public void configure(String activeProfile, BuilderConfiguration builder) {
        // activeProfile can be "test" or "staging" depending on the active profile.

        builder.resolvers(...)
            .converters(...)
            .processors(...);
    }
}

public class MyWildcardProfileConfigurator implements ProfileConfigurator {
    @Override
    public void configure(String activeProfile, BuilderConfiguration builder) {
        // This will be applied regardless of the active profile.
        builder.resolvers(applicationProperties(activeProfile));
    }

    private ResourceResolver applicationProperties(String activeProfile) {
        // Changes based on the active profile:
        // application-test.properties
        // application-staging.properties
        // application-prod.properties
        String resourceName = "/application-" + activeProfile + ".properties";
        try {
            return ResourceResolver.fromUrl(getClass().getResource(resourceName))
        } catch (IOException ex) {
            throw new RuntimeException("Failed to load " + resourceName, ex);
        }
    }
}
```

## Activating an Externalized Properties Profile

The active Externalized Properties profile can be set via a property with the name: `externalizedproperties.profile`.  

Externalized Properties will look for the profile property in all the registered non-profile-specific resolvers and the default resolvers.

If the profile property is found, all the applicable profile configurations will then be applied. Otherwise, they will be ignored.
