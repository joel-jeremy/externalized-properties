package io.github.joeljeremy7.externalizedproperties.resolvers.git;

import io.github.joeljeremy7.externalizedproperties.core.ExternalizedPropertiesException;
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
import java.util.Comparator;
import java.util.Optional;
import java.util.stream.Stream;

import static io.github.joeljeremy7.externalizedproperties.core.internal.Arguments.requireNonNull;
import static io.github.joeljeremy7.externalizedproperties.core.internal.Arguments.requireNonNullOrBlank;

/**
 * A {@link Resolver} implementation which reads properties from a resource file that is
 * stored in a Git repository.
 */
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
            "resourceResolver"
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
        private CredentialsProvider gitCredentialsProvider;
        private SshSessionFactory gitSshSessionFactory;
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
            this.gitRepositoryUri = requireNonNullOrBlank(
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
            this.gitBranch = requireNonNullOrBlank(
                gitBranch, 
                "gitBranch"
            );
            return this;
        }

        /**
         * The directory to clone the Git repository to. 
         * 
         * @implNote If the directory does not exist, it will automatically be created. 
         * Otherwise, the contents of the existing directory will be cleaned/deleted
         * before attempting to clone the Git repository.
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
            this.resourceFilePath = requireNonNullOrBlank(
                resourceFilePath, 
                "resourceFilePath"
            );
            return this;
        }

        /**
         * The Git credentials provider. This will only be used when cloning a Git repository
         * via HTTP(s).
         * 
         * @param gitCredentialsProvider The Git credentials provider. This will only be used 
         * when cloning a Git repository via HTTP(s).
         * @return This builder.
         */
        public Builder gitCredentialsProvider(CredentialsProvider gitCredentialsProvider) {
            this.gitCredentialsProvider = requireNonNull(
                gitCredentialsProvider, 
                "gitCredentialsProvider"
            );
            return this;
        }

        /**
         * The Git SSH session factory. This will only be used when cloning a Git repository
         * via SSH.
         * 
         * @param gitSshSessionFactory The Git SSH session factory. This will only be used when 
         * cloning a Git repository via SSH.
         * @return This builder.
         */
        public Builder gitSshSessionFactory(SshSessionFactory gitSshSessionFactory) {
            this.gitSshSessionFactory = requireNonNull(
                gitSshSessionFactory, 
                "gitSshSessionFactory"
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
         */
        public GitResolver build() {
            validate();

            try (Git git = cloneOrOpenGitRepo()) {
                Path checkedOutResourceFile = gitCheckoutResourceFile(
                    git,
                    gitBranch,
                    resourceFilePath
                );
                return new GitResolver(ResourceResolver.fromPath(
                    checkedOutResourceFile,
                    resourceReader
                ));
            }
            catch (Exception e) {
                throw new ExternalizedPropertiesException(
                    "An exception occurred while building GitResolver.", 
                    e
                );
            }
        }

        private void validate() {
            if (gitRepositoryUri == null) {
                throw new IllegalStateException("Git repository URI is required.");
            }

            if (gitBranch == null) {
                throw new IllegalStateException("Git branch is required.");
            }

            if (gitCloneDirectory == null) {
                throw new IllegalStateException("Git clone directory path is required.");
            }

            if (resourceFilePath == null) {
                throw new IllegalStateException("Resource file path is required.");
            }
        }

        private Git cloneOrOpenGitRepo() throws IOException, GitAPIException {
            if (Files.exists(gitCloneDirectory.resolve(".git"))) {
                // Open existing git repo.
                return Git.open(gitCloneDirectory.toFile());
            }
            else {
                cleanDirectory(gitCloneDirectory);

                // Clone repo but don't checkout anything yet.
                CloneCommand clone = Git.cloneRepository()
                    .setURI(gitRepositoryUri)
                    .setDirectory(gitCloneDirectory.toFile())
                    .setNoCheckout(true)
                    .setCloneAllBranches(false);

                if (gitCredentialsProvider != null) {
                    clone.setCredentialsProvider(gitCredentialsProvider);
                } 
                
                if (gitSshSessionFactory != null) {
                    clone.setTransportConfigCallback(transport -> {
                        if (transport instanceof SshTransport) {
                            ((SshTransport)transport).setSshSessionFactory(gitSshSessionFactory);
                        }
                    });
                }

                return clone.call();
            }
        }

        private static Path gitCheckoutResourceFile(
                Git git, 
                String branchToCheckout,
                String resourceFilePath
        ) throws GitAPIException {
            boolean branchHasRemote = branchToCheckout.indexOf('/') != -1;
            
            // Checkout specific files.
            git.checkout()
                // Default remote name is origin.
                .setStartPoint(branchHasRemote ? 
                    branchToCheckout : "origin/" + branchToCheckout)
                .addPath(resourceFilePath)
                .call();
            
            return git.getRepository().getWorkTree().toPath().resolve(resourceFilePath);
        }

        private static void cleanDirectory(Path dir) throws IOException {
            if (Files.exists(dir)) {
                try (Stream<Path> paths = Files.walk(dir)) {
                    // Delete from bottom up.
                    for (Path path : paths.sorted(Comparator.reverseOrder())
                            .toArray(Path[]::new)) {
                        Files.delete(path);
                    }
                }
            }

            Files.createDirectories(dir);
        }
    }
}
