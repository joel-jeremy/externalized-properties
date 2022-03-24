package io.github.jeyjeyemem.externalizedproperties.resolvers.awsssm;

import io.github.jeyjeyemem.externalizedproperties.core.Resolver;
import io.github.jeyjeyemem.externalizedproperties.core.ResolverResult;
import software.amazon.awssdk.services.ssm.SsmClient;
import software.amazon.awssdk.services.ssm.model.GetParameterResponse;
import software.amazon.awssdk.services.ssm.model.GetParametersResponse;
import software.amazon.awssdk.services.ssm.model.ParameterNotFoundException;

import java.util.Collection;
import java.util.Optional;

/**
 * {@link Resolver} implementation which resolves requested properties 
 * from AWS SSM.
 */
public class AwsSsmResolver implements Resolver {
    private final SsmClient awsSsmClient;

    /**
     * Constructor.
     * 
     * @param awsSsmClient The AWS SSM client.
     */
    public AwsSsmResolver(SsmClient awsSsmClient) {
        if (awsSsmClient == null) {
            throw new IllegalArgumentException("awsSsmClient must not be null.");
        }

        this.awsSsmClient = awsSsmClient;
    }
    
    /**
     * Resolve properties from AWS SSM.
     * 
     * @return The {@link ResolverResult} which contains the resolved properties
     * and unresolved properties, if there are any.
     */
    @Override
    public Optional<String> resolve(String propertyName) {
        if (propertyName == null || propertyName.isEmpty()) {
            throw new IllegalArgumentException("propertyName must not be null or empty.");
        }

        try {
            GetParameterResponse response = awsSsmClient.getParameter(
                request -> request.name(propertyName).withDecryption(true)
            );
            return Optional.ofNullable(response.parameter().value());
        } catch (ParameterNotFoundException ex) {
            return Optional.empty();
        }
    }

    /**
     * Resolve properties from AWS SSM.
     * 
     * @return The {@link ResolverResult} which contains the resolved properties
     * and unresolved properties, if there are any.
     */
    @Override
    public ResolverResult resolve(Collection<String> propertyNames) {
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

        ResolverResult.Builder resultBuilder = ResolverResult.builder(propertyNames);

        response.parameters().forEach(p -> 
            resultBuilder.add(p.name(), p.value())
        );

        return resultBuilder.build();
    }
}
