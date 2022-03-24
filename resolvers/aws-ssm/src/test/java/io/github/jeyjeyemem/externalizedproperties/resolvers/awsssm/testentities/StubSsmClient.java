package io.github.jeyjeyemem.externalizedproperties.resolvers.awsssm.testentities;

import software.amazon.awssdk.awscore.exception.AwsServiceException;
import software.amazon.awssdk.core.exception.SdkClientException;
import software.amazon.awssdk.services.ssm.SsmClient;
import software.amazon.awssdk.services.ssm.model.GetParameterRequest;
import software.amazon.awssdk.services.ssm.model.GetParameterResponse;
import software.amazon.awssdk.services.ssm.model.GetParametersRequest;
import software.amazon.awssdk.services.ssm.model.GetParametersResponse;
import software.amazon.awssdk.services.ssm.model.InternalServerErrorException;
import software.amazon.awssdk.services.ssm.model.InvalidKeyIdException;
import software.amazon.awssdk.services.ssm.model.Parameter;
import software.amazon.awssdk.services.ssm.model.ParameterNotFoundException;
import software.amazon.awssdk.services.ssm.model.ParameterVersionNotFoundException;
import software.amazon.awssdk.services.ssm.model.SsmException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

public class StubSsmClient implements SsmClient {

    // Convert from /test/property --> test.property.value
    public static final Function<String, String> DEFAULT_DELEGATE = 
        propertyName -> {
            if (propertyName.startsWith("/")) {
                propertyName = propertyName.substring(1);
            }
            return propertyName.replace("/", ".") + ".value";
        };
    public static final StubSsmClient NO_PARAMETERS = new StubSsmClient(p -> null);

    private final Function<String, String> getParameterDelegate;

    public StubSsmClient() {
        this(DEFAULT_DELEGATE);
    }

    public StubSsmClient(
            Function<String, String> getParameterDelegate
    ) {
        this.getParameterDelegate = getParameterDelegate;
    }

    @Override
    public GetParameterResponse getParameter(GetParameterRequest getParameterRequest)
            throws InternalServerErrorException, 
                InvalidKeyIdException, 
                ParameterNotFoundException,
                ParameterVersionNotFoundException, 
                AwsServiceException, 
                SdkClientException, 
                SsmException {
        GetParametersResponse response = getParameters(Arrays.asList(getParameterRequest.name()));
        if (response.hasInvalidParameters()) {
            throw ParameterNotFoundException.builder()
                .message(getParameterRequest.name() + " not found. This is an emulated exception.")
                .build();
        }
        return GetParameterResponse.builder()
            .parameter(response.parameters().get(0))
            .build();
    }

    @Override
    public GetParametersResponse getParameters(GetParametersRequest getParametersRequest) 
            throws InvalidKeyIdException,
                InternalServerErrorException, 
                AwsServiceException, 
                SdkClientException, 
                SsmException {
        return getParameters(getParametersRequest.names());
    }

    @Override
    public String serviceName() {
        return SsmClient.SERVICE_NAME;
    }

    @Override
    public void close() {}

    private GetParametersResponse getParameters(List<String> parameterNames) {
        List<Parameter> stubParameters = new ArrayList<>(parameterNames.size());
        List<String> invalidParameterNames = new ArrayList<>(parameterNames.size());
        for (String parameterName : parameterNames) {
            String parameterValue = getParameterDelegate.apply(parameterName);

            if (parameterValue == null) {
                invalidParameterNames.add(parameterName);
            }
            else {
                stubParameters.add(Parameter.builder()
                    .name(parameterName)
                    .value(parameterValue)
                    .build()
                );
            }
        }
        GetParametersResponse.Builder responseBuilder = GetParametersResponse.builder();
        if (!stubParameters.isEmpty()) {
            responseBuilder.parameters(stubParameters);
        }
        if (!invalidParameterNames.isEmpty()) {
            responseBuilder.invalidParameters(invalidParameterNames);
        }
        return responseBuilder.build();
    }
}
