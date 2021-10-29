package io.github.jeyjeyemem.externalizedproperties.resolvers.awsssm;

import io.github.jeyjeyemem.externalizedproperties.core.ExternalizedPropertyResolver;
import io.github.jeyjeyemem.externalizedproperties.core.ExternalizedPropertyResolverResult;
import io.github.jeyjeyemem.externalizedproperties.core.ResolvedProperty;
import software.amazon.awssdk.services.ssm.SsmClient;
import software.amazon.awssdk.services.ssm.model.GetParametersResponse;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * {@link ExternalizedPropertyResolver} implementation which resolves requested properties 
 * from AWS SSM.
 */
public class AwsSsmPropertyResolver implements ExternalizedPropertyResolver {
    private final SsmClient awsSsmClient;

    /**
     * Constructor.
     * 
     * @param awsSsmClient The AWS SSM client.
     */
    public AwsSsmPropertyResolver(SsmClient awsSsmClient) {
        if (awsSsmClient == null) {
            throw new IllegalArgumentException("awsSsmClient must not be null.");
        }

        this.awsSsmClient = awsSsmClient;
    }

    /**
     * Resolve properties from AWS SSM.
     * 
     * @return The {@link ExternalizedPropertyResolverResult} which contains the resolved properties
     * and unresolved properties, if there are any.
     */
    @Override
    public ExternalizedPropertyResolverResult resolve(Collection<String> propertyNames) {
        if (propertyNames == null || propertyNames.isEmpty()) {
            throw new IllegalArgumentException("propertyNames must not be null or empty.");
        }

        GetParametersResponse response = awsSsmClient.getParameters(
            request -> request.names(propertyNames).withDecryption(true)
        );
        
        // if (!response.invalidParameters().isEmpty()) {
        //     String exceptionMessage = 
        //         "Invalid SSM parameters: " + response.invalidParameters() + ". " +
        //         "Please make sure property names are correct or properties have been created.";
        //     LOGGER.log(Level.SEVERE, exceptionMessage);
        //     throw new AwsSsmInvalidPropertyException(exceptionMessage);
        // }

        List<ResolvedProperty> resolvedProperties = response.parameters().stream()
            .map(p -> ResolvedProperty.with(p.name(), p.value()))
            .collect(Collectors.toList());

        return new ExternalizedPropertyResolverResult(
            propertyNames,
            resolvedProperties
        );
    }
}
