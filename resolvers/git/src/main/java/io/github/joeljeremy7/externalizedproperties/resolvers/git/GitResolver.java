package io.github.joeljeremy7.externalizedproperties.resolvers.git;

import io.github.joeljeremy7.externalizedproperties.core.ExternalizedProperties;
import io.github.joeljeremy7.externalizedproperties.core.ExternalizedPropertiesException;
import io.github.joeljeremy7.externalizedproperties.core.ExternalizedProperty;
import io.github.joeljeremy7.externalizedproperties.core.InvocationContext;
import io.github.joeljeremy7.externalizedproperties.core.Resolver;
import io.github.joeljeremy7.externalizedproperties.core.resolvers.ResourceResolver;
import io.github.joeljeremy7.externalizedproperties.core.resolvers.ResourceResolver.PropertiesReader;
import io.github.joeljeremy7.externalizedproperties.core.resolvers.ResourceResolver.ResourceReader;
import org.eclipse.jgit.api.CloneCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.transport.CredentialsProvider;
import org.eclipse.jgit.transport.SshSessionFactory;
import org.eclipse.jgit.transport.SshTransport;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.Optional;
import java.util.stream.Stream;

import static io.github.joeljeremy7.externalizedproperties.core.internal.Arguments.requireNonNull;
import static io.github.joeljeremy7.externalizedproperties.core.internal.Arguments.requireNonNullOrEmpty;

public class GitResolver implements Resolver {

    private final ResourceResolver resourceResolver;

    /**
     * Constructor.
     * 
     * @param resourceResolver The underlying resource resolver.
     */
    private GitResolver(ResourceResolver resourceResolver) {
        this.resourceResolver = requireNonNull(
            resourceResolver, 
            "resourceResolvers"
        );
    }

    /** {@inheritDoc} */
    @Override
    public Optional<String> resolve(InvocationContext context, String propertyName) {
        return resourceResolver.resolve(context, propertyName);
    }

    /**
     * Create a new {@link Builder} to facilitate building of an
     * {@link GitResolver} instance.
     * 
     * @return The builder for {@link GitResolver}.
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * The builder for {@link GitResolver}.
     */
    public static class Builder {
        private String gitRepositoryUri;
        private String gitBranch;
        private Path gitCloneDirectory;
        private String resourceFilePath;
        private CredentialsProvider credentialsProvider;
        private SshSessionFactory sshSessionFactory;
        private ResourceReader resourceReader = new PropertiesReader();

        @SuppressWarnings("NullAway.Init")
        private Builder() {}

        /**
         * The Git repository URI.
         * 
         * @param gitRepositoryUri The Git repository URI.
         * @return This builder.
         */
        public Builder gitRepositoryUri(String gitRepositoryUri) {
            this.gitRepositoryUri = requireNonNullOrEmpty(
                gitRepositoryUri, 
                "gitRepositoryUri"
            );
            return this;
        }

        /**
         * The Git branch to checkout configuration resource files from. The argument
         * can either have a remote i.e. {@code origin/main} or just the branch name
         * i.e. {@code main} in which case the remote will be assumed as {@code origin}.
         * 
         * @param gitBranch The Git branch to checkout configuration resource files from. 
         * The argument can either have a remote i.e. {@code origin/main} or just the 
         * branch name i.e. {@code main} in which case the remote will be assumed as 
         * {@code origin}.
         * @return This builder.
         */
        public Builder gitBranch(String gitBranch) {
            this.gitBranch = requireNonNullOrEmpty(
                gitBranch, 
                "gitBranch"
            );
            return this;
        }

        /**
         * The directory to clone the Git repository to.
         * 
         * @param gitCloneDirectory The directory to clone the Git repository to.
         * @return This builder.
         */
        public Builder gitCloneDirectory(Path gitCloneDirectory) {
            this.gitCloneDirectory = requireNonNull(
                gitCloneDirectory, 
                "gitCloneDirectory"
            );
            return this;
        }

        /**
         * The path of the resource file to resolve properties from. Relative to the 
         * Git clone directory as specified in {@link #gitCloneDirectory(Path)}.
         * 
         * @param resourceFilePath The path of the resource file to resolve properties 
         * from. Relative to the Git clone directory as specified in 
         * {@link #gitCloneDirectory(Path)}.
         * @return This builder.
         */
        public Builder resourceFilePath(String resourceFilePath) {
            this.resourceFilePath = requireNonNullOrEmpty(
                resourceFilePath, 
                "resourceFilePath"
            );
            return this;
        }

        /**
         * The Git credentials provider.
         * 
         * @param credentialsProvider The Git credentials provider.
         * @return This builder.
         */
        public Builder credentialsProvider(CredentialsProvider credentialsProvider) {
            this.credentialsProvider = requireNonNull(
                credentialsProvider, 
                "credentialsProvider"
            );
            return this;
        }

        /**
         * The Git SSH session factory.
         * 
         * @param sshSessionFactory The Git SSH session factory.
         * @return This builder.
         */
        public Builder sshSessionFactory(SshSessionFactory sshSessionFactory) {
            this.sshSessionFactory = requireNonNull(
                sshSessionFactory, 
                "sshSessionFactory"
            );
            return this;
        }

        /**
         * The resource reader to use in reading the configuration resource. By default,
         * the configuration resource is expected to be in {@code .properties} file format.
         * 
         * @param resourceReader The resource reader to use in reading the configuration 
         * resource.
         * @return This builder.
         */
        public Builder resourceReader(ResourceReader resourceReader) {
            this.resourceReader = requireNonNull(
                resourceReader, 
                "resourceReader"
            );
            return this;
        }

        /**
         * Build the {@link GitResolver} by initializing the Git repository and reading
         * the target configuration resource.
         * 
         * @return The built {@link GitResolver}.
         * @throws IOException if an I/O exception occurs.
         * @throws GitAPIException if a Git-related exception occurs.
         */
        public GitResolver build() {
            validate();
            try (Git gitRepo = initializeGitRepo()) {
                Path configResourcePath = gitCheckoutConfigurationResource(gitRepo);
                return new GitResolver(ResourceResolver.fromPath(
                    configResourcePath,
                    resourceReader
                ));
            }
            catch (GitAPIException | IOException e) {
                throw new ExternalizedPropertiesException(
                    "An exception error occurred while building Git resolver.", 
                    e
                );
            }
        }

        private void validate() {
            if (gitRepositoryUri == null) {
                throw new IllegalStateException("Git Repository URI is required.");
            }

            if (gitBranch == null) {
                throw new IllegalStateException("Git branch is required.");
            }

            if (gitCloneDirectory == null) {
                throw new IllegalStateException("Git clone directory path is required.");
            }

            if (resourceFilePath == null) {
                throw new IllegalStateException("Configuration resource path is required.");
            }
        }

        private Git initializeGitRepo() throws IOException, GitAPIException {
            if (Files.exists(gitCloneDirectory.resolve(".git"))) {
                // Open existing git repo.
                return Git.open(gitCloneDirectory.toFile());
            }
            else {
                cleanDirectory(gitCloneDirectory);

                // Clone repo but don't checkout anything yet.
                CloneCommand builder = Git.cloneRepository()
                    .setURI(gitRepositoryUri)
                    .setDirectory(gitCloneDirectory.toFile())
                    .setNoCheckout(true)
                    .setCloneAllBranches(false);

                if (credentialsProvider != null) {
                    builder.setCredentialsProvider(credentialsProvider);
                } 
                else if (sshSessionFactory != null) {
                    builder.setTransportConfigCallback(transport -> {
                        if (transport instanceof SshTransport) {
                            ((SshTransport) transport).setSshSessionFactory(sshSessionFactory);
                        }
                    });
                }

                return builder.call();
            }
        }

        private Path gitCheckoutConfigurationResource(Git gitRepo) throws GitAPIException {
            if (sshSessionFactory != null) {
                SshSessionFactory.setInstance(sshSessionFactory);
            }

            boolean hasRemote = gitBranch.indexOf('/') != -1;
            
            // Checkout specific files.
            gitRepo.checkout()
                // Default remote name is origin.
                .setStartPoint(hasRemote ? gitBranch : "origin/" + gitBranch)
                .addPath(resourceFilePath)
                .call();
            
            return gitCloneDirectory.resolve(resourceFilePath);
        }

        private void cleanDirectory(Path dir) throws IOException {
            if (Files.exists(dir)) {
                // Delete from bottom up.
                try (Stream<Path> paths = Files.walk(dir).sorted(Comparator.reverseOrder())) {
                    for (Path path : paths.toArray(Path[]::new)) {
                        Files.delete(path);
                    }
                }
            }

            Files.createDirectory(dir);
        }
    }

    public static void main(String[] args) {
        GitResolver resolver = GitResolver.builder()
            .gitRepositoryUri("https://github.com/spring-cloud-samples/config-repo.git")
            .gitBranch("main")
            .gitCloneDirectory(Paths.get("").resolve("cloneDir"))
            .resourceFilePath("bar.properties")
            .build();

        ExternalizedProperties ep = ExternalizedProperties.builder()
            .resolvers(resolver)
            .build();

        ProxyInterface p = ep.initialize(ProxyInterface.class);
        System.out.println(p.foo());
    }

    private static interface ProxyInterface {
        @ExternalizedProperty("foo")
        String foo();
    }
}
