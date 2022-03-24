package io.github.jeyjeyemem.externalizedproperties.resolvers.awsssm;

import com.github.dockerjava.zerodep.shaded.org.apache.hc.core5.function.Supplier;
import io.github.jeyjeyemem.externalizedproperties.core.ResolverResult;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.localstack.LocalStackContainer;
import org.testcontainers.containers.localstack.LocalStackContainer.Service;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.ssm.SsmClient;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Testcontainers
public class AwsSsmResolverIntegrationTests {

    static final DockerImageName LOCALSTACK_IMAGE = DockerImageName.parse(
        "localstack/localstack:0.14.1"
    );
    
    @Container
    static final LocalStackContainer LOCALSTACK = 
        new LocalStackContainer(LOCALSTACK_IMAGE)
            .withServices(Service.SSM);

    // Should only be called after container has started 
    // e.g. in @Nested classes or @Test methods.
    static final Supplier<SsmClient> SSM_CLIENT_FACTORY = () -> SsmClient.builder()
        .endpointOverride(LOCALSTACK.getEndpointOverride(Service.SSM))
        .credentialsProvider(StaticCredentialsProvider.create(
            AwsBasicCredentials.create(
                LOCALSTACK.getAccessKey(), 
                LOCALSTACK.getSecretKey()
            )
        ))
        .region(Region.of(LOCALSTACK.getRegion()))
        .build();

    @BeforeAll
    static void setup() throws IOException, InterruptedException {
        createTestSsmParameters();
    }

    @Nested
    class Constructor {
        @Test
        @DisplayName("should throw when ssm client argument is null")
        void test1() {
            assertThrows(
                IllegalArgumentException.class, 
                () -> new AwsSsmResolver(null)
            );
        }
    }

    @Nested
    class ResolveMethod {

        private final SsmClient ssmClient = SSM_CLIENT_FACTORY.get();
            
        @Test
        @DisplayName("should throw when propertyName argument is null")
        void test1() {
            AwsSsmResolver awsSsmPropertyResolver = 
                new AwsSsmResolver(ssmClient);

            assertThrows(IllegalArgumentException.class, () -> {
                awsSsmPropertyResolver.resolve((String)null);
            });
        }

        @Test
        @DisplayName("should throw when propertyName argument is empty")
        void test2() {
            AwsSsmResolver awsSsmPropertyResolver = 
                new AwsSsmResolver(ssmClient);
                
            assertThrows(IllegalArgumentException.class, () -> {
                awsSsmPropertyResolver.resolve("");
            });
        }

        @Test
        @DisplayName("should resolve property from AWS SSM")
        void test3() {
            AwsSsmResolver awsSsmPropertyResolver = 
                new AwsSsmResolver(ssmClient);

            Optional<String> resolved = awsSsmPropertyResolver.resolve("/test/property");

            assertTrue(resolved.isPresent());
            assertEquals("test.property.value", resolved.get());
        }

        @Test
        @DisplayName("should return empty Optional when property is not found in AWS SSM")
        void tes4() {
            AwsSsmResolver awsSsmPropertyResolver = 
                new AwsSsmResolver(ssmClient);

            Optional<String> resolved = awsSsmPropertyResolver.resolve(
                "non/existing/property"
            );

            assertFalse(resolved.isPresent());
        }
    }

    @Nested
    class ResolveMethodWithCollectionOverload {

        private final SsmClient ssmClient = SSM_CLIENT_FACTORY.get();
            
        @Test
        @DisplayName("should throw when propertyNames argument is null")
        void test1() {
            AwsSsmResolver awsSsmPropertyResolver = 
                new AwsSsmResolver(ssmClient);

            assertThrows(IllegalArgumentException.class, () -> {
                awsSsmPropertyResolver.resolve((Collection<String>)null);
            });
        }

        @Test
        @DisplayName("should throw when propertyNames argument is empty")
        void test2() {
            AwsSsmResolver awsSsmPropertyResolver = 
                new AwsSsmResolver(ssmClient);
                
            assertThrows(IllegalArgumentException.class, () -> {
                awsSsmPropertyResolver.resolve(Collections.emptyList());
            });
        }

        @Test
        @DisplayName("should resolve all properties from AWS SSM")
        void test3() {
            AwsSsmResolver awsSsmPropertyResolver = 
                new AwsSsmResolver(ssmClient);

            List<String> propertiesToResolve = Arrays.asList(
                "/test/property",
                "/test/property/2"
            );

            ResolverResult result = 
                awsSsmPropertyResolver.resolve(propertiesToResolve);

            assertTrue(result.hasResolvedProperties());
            assertFalse(result.hasUnresolvedProperties());

            assertTrue(result.resolvedPropertyNames().stream()
                .allMatch(resolved -> propertiesToResolve.contains(resolved))
            );

            assertEquals(
                "test.property.value", 
                result.findRequiredProperty("/test/property")
            );

            assertEquals(
                "test.property.value.2", 
                result.findRequiredProperty("/test/property/2")
            );
        }

        @Test
        @DisplayName(
            "should return result with resolved and unresolved properties from AWS SSM"
        )
        void test4() {
            AwsSsmResolver awsSsmPropertyResolver = 
                new AwsSsmResolver(ssmClient);

            List<String> propertiesToResolve = Arrays.asList(
                "test/property", 
                "test/property/2",
                "non/existent/property"
            );

            ResolverResult result = 
                awsSsmPropertyResolver.resolve(propertiesToResolve);

            assertTrue(result.hasResolvedProperties());
            assertTrue(result.hasUnresolvedProperties());

            assertEquals(
                "test.property.value", 
                result.findRequiredProperty("/test/property")
            );

            assertEquals(
                "test.property.value.2", 
                result.findRequiredProperty("/test/property/2")
            );

            assertTrue(result.unresolvedPropertyNames().contains("non/existent/property"));
        }
    }

    @Nested
    class ResolveMethodWithVarArgsOverload {

        private final SsmClient ssmClient = SSM_CLIENT_FACTORY.get();
            
        @Test
        @DisplayName("should throw when propertyNames argument is null")
        void test1() {
            AwsSsmResolver awsSsmPropertyResolver = 
                new AwsSsmResolver(ssmClient);

            assertThrows(IllegalArgumentException.class, () -> {
                awsSsmPropertyResolver.resolve((String[])null);
            });
        }

        @Test
        @DisplayName("should throw when propertyNames argument is empty")
        void test2() {
            AwsSsmResolver awsSsmPropertyResolver = 
                new AwsSsmResolver(ssmClient);
                
            assertThrows(IllegalArgumentException.class, () -> {
                awsSsmPropertyResolver.resolve(new String[0]);
            });
        }

        @Test
        @DisplayName("should resolve all properties from AWS SSM")
        void test3() {
            AwsSsmResolver awsSsmPropertyResolver = 
                new AwsSsmResolver(ssmClient);

            String[] propertiesToResolve = new String[] {
                "/test/property",
                "/test/property/2"
            };

            ResolverResult result = 
                awsSsmPropertyResolver.resolve(propertiesToResolve);

            assertTrue(result.hasResolvedProperties());
            assertFalse(result.hasUnresolvedProperties());

            assertTrue(result.resolvedPropertyNames().stream()
                .allMatch(resolved -> Arrays.asList(propertiesToResolve).contains(resolved))
            );

            assertEquals(
                "test.property.value", 
                result.findRequiredProperty("/test/property")
            );

            assertEquals(
                "test.property.value.2", 
                result.findRequiredProperty("/test/property/2")
            );
        }

        @Test
        @DisplayName(
            "should return result with resolved and unresolved properties from AWS SSM"
        )
        void test4() {
            AwsSsmResolver awsSsmPropertyResolver = 
                new AwsSsmResolver(ssmClient);

            String[] propertiesToResolve = new String[] {
                "test/property", 
                "test/property/2",
                "non/existent/property"
            };

            ResolverResult result = 
                awsSsmPropertyResolver.resolve(propertiesToResolve);

            assertTrue(result.hasResolvedProperties());
            assertTrue(result.hasUnresolvedProperties());

            assertEquals(
                "test.property.value", 
                result.findRequiredProperty("/test/property")
            );

            assertEquals(
                "test.property.value.2", 
                result.findRequiredProperty("/test/property/2")
            );

            assertTrue(result.unresolvedPropertyNames().contains("non/existent/property"));
        }
    }
    
    // Create parameters for testing.
    static void createTestSsmParameters() throws IOException, InterruptedException {
        LOCALSTACK.execInContainer(
            "awslocal",
            "ssm",
            "put-parameter",
            "--name",
            "/test/property",
            "--value",
            "test.property.value",
            "--type", 
            "String"
        );

        LOCALSTACK.execInContainer(
            "awslocal",
            "ssm",
            "put-parameter",
            "--name",
            "/test/property/2",
            "--value",
            "test.property.2.value",
            "--type", 
            "String"
        );

        LOCALSTACK.execInContainer(
            "awslocal",
            "ssm",
            "put-parameter",
            "--name",
            "/test/property/secure",
            "--value",
            "test.property.secure.value",
            "--type", 
            "SecureString"
        );

        LOCALSTACK.execInContainer(
            "awslocal",
            "ssm",
            "put-parameter",
            "--name",
            "/test/property/list",
            "--value",
            "test.property.list.value",
            "--type", 
            "StringList"
        );
    }

}
