package io.github.joeljeremy7.externalizedproperties.resolvers.git;

import io.github.joeljeremy7.externalizedproperties.core.ExternalizedProperties;
import io.github.joeljeremy7.externalizedproperties.core.ExternalizedPropertiesException;
import io.github.joeljeremy7.externalizedproperties.core.ExternalizedProperty;
import io.github.joeljeremy7.externalizedproperties.core.InvocationContext;
import io.github.joeljeremy7.externalizedproperties.core.Resolver;
import io.github.joeljeremy7.externalizedproperties.core.resolvers.ResourceResolver.PropertiesReader;
import io.github.joeljeremy7.externalizedproperties.core.resolvers.ResourceResolver.ResourceReader;
import io.github.joeljeremy7.externalizedproperties.core.testfixtures.InvocationContextUtils;
import io.github.joeljeremy7.externalizedproperties.core.testfixtures.InvocationContextUtils.InvocationContextTestFactory;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.junit.http.SimpleHttpServer;
import org.eclipse.jgit.junit.ssh.SshTestGitServer;
import org.eclipse.jgit.transport.CredentialsProvider;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import org.eclipse.jgit.transport.sshd.JGitKeyCache;
import org.eclipse.jgit.transport.sshd.SshdSessionFactory;
import org.eclipse.jgit.transport.sshd.SshdSessionFactoryBuilder;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Optional;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class GitResolverTests {
    private static final KeyPair TEST_RSA_KEY_PAIR = createTestRsaKeyPair();

    private static final InvocationContextTestFactory<ProxyInterface> INVOCATION_CONTEXT_FACTORY = 
        InvocationContextUtils.testFactory(ProxyInterface.class);
    
    private static Path ROOT_TEST_DIR;
    private static Path BARE_REPO_DIR;
    private static Path CLONE_DIR;
    private static Path SSH_DIR;
    private static Path SSH_CONFIG_FILE;
    private static Git GIT_BARE_REPO;

    // Local Git servers for testing.
    private static SimpleHttpServer LOCAL_GIT_HTTP_SERVER;
    private static SshTestGitServer LOCAL_GIT_SSH_SERVER;

    private static String LOCAL_GIT_HTTP_SERVER_REPO_URI;
    private static String LOCAL_GIT_SSH_SERVER_REPO_URI;

    private static final CredentialsProvider DEFAULT_CREDENTIALS =
        new UsernamePasswordCredentialsProvider(
            "agitter", 
            "letmein"
        );
    private static final String DEFAULT_RESOURCE_FILE_PATH = "app.properties";
    private static final String DEFAULT_GIT_BRANCH = "git-resolver-configs";

    @BeforeAll
    static void setup() throws Exception {
        ROOT_TEST_DIR = Files.createTempDirectory(
            "git-resolver-root-test-dir"
        );
        BARE_REPO_DIR = Files.createDirectories(
            ROOT_TEST_DIR.resolve("configs-repo.git")
        );
        CLONE_DIR = Files.createDirectories(
            ROOT_TEST_DIR.resolve("configs-repo")
        );

        setupGitRepo();
        setupLocalGitHttpServer();
        setupLocalGitSshServer();
    }

    @AfterAll
    static void cleanup() throws Exception {
        deleteRecursively(ROOT_TEST_DIR);
        LOCAL_GIT_HTTP_SERVER.stop();
        LOCAL_GIT_SSH_SERVER.stop();
        GIT_BARE_REPO.close();
    }

    @Nested
    class BuilderTests {
        @Nested
        class GitRepositoryUriMethod {
            @Test
            @DisplayName("should throw when git repository URI argument is null")
            void test1() {
                GitResolver.Builder builder = GitResolver.builder();
                assertThrows(
                    IllegalArgumentException.class, 
                    () -> builder.gitRepositoryUri(null)
                );
            }

            @Test
            @DisplayName("should throw when git repository URI argument is empty")
            void test2() {
                GitResolver.Builder builder = GitResolver.builder();
                assertThrows(
                    IllegalArgumentException.class, 
                    () -> builder.gitRepositoryUri("")
                );
            }

            @Test
            @DisplayName("should throw when git repository URI argument is blank")
            void test3() {
                GitResolver.Builder builder = GitResolver.builder();
                assertThrows(
                    IllegalArgumentException.class, 
                    () -> builder.gitRepositoryUri("   ")
                );
            }
        }

        @Nested
        class GitBranchMethod {
            @Test
            @DisplayName("should throw when git branch argument is null")
            void test1() {
                GitResolver.Builder builder = GitResolver.builder();
                assertThrows(
                    IllegalArgumentException.class, 
                    () -> builder.gitBranch(null)
                );
            }

            @Test
            @DisplayName("should throw when git branch argument is empty")
            void test2() {
                GitResolver.Builder builder = GitResolver.builder();
                assertThrows(
                    IllegalArgumentException.class, 
                    () -> builder.gitBranch("")
                );
            }

            @Test
            @DisplayName("should throw when git branch argument is blank")
            void test3() {
                GitResolver.Builder builder = GitResolver.builder();
                assertThrows(
                    IllegalArgumentException.class, 
                    () -> builder.gitBranch("   ")
                );
            }
        }

        @Nested
        class GitCloneDirectoryMethod {
            @Test
            @DisplayName("should throw when git clone directory argument is null")
            void test1() {
                GitResolver.Builder builder = GitResolver.builder();
                assertThrows(
                    IllegalArgumentException.class, 
                    () -> builder.gitCloneDirectory(null)
                );
            }
        }

        @Nested
        class ResourceFilePathMethod {
            @Test
            @DisplayName("should throw when resource file path argument is null")
            void test1() {
                GitResolver.Builder builder = GitResolver.builder();
                assertThrows(
                    IllegalArgumentException.class, 
                    () -> builder.resourceFilePath(null)
                );
            }

            @Test
            @DisplayName("should throw when resource file path argument is empty")
            void test2() {
                GitResolver.Builder builder = GitResolver.builder();
                assertThrows(
                    IllegalArgumentException.class, 
                    () -> builder.resourceFilePath("")
                );
            }

            @Test
            @DisplayName("should throw when resource file path argument is blank")
            void test3() {
                GitResolver.Builder builder = GitResolver.builder();
                assertThrows(
                    IllegalArgumentException.class, 
                    () -> builder.resourceFilePath("   ")
                );
            }
        }

        @Nested
        class ResourceReaderMethod {
            @Test
            @DisplayName("should throw when resource reader argument is null")
            void test1() {
                GitResolver.Builder builder = GitResolver.builder();
                assertThrows(
                    IllegalArgumentException.class, 
                    () -> builder.resourceReader(null)
                );
            }
        }

        @Nested
        class GitCredentialsProviderMethod {
            @Test
            @DisplayName("should throw when git credentials provider argument is null")
            void test1() {
                GitResolver.Builder builder = GitResolver.builder();
                assertThrows(
                    IllegalArgumentException.class, 
                    () -> builder.gitCredentialsProvider(null)
                );
            }
        }

        @Nested
        class GitSshSessionFactoryMethod {
            @Test
            @DisplayName("should throw when git ssh session factory argument is null")
            void test1() {
                GitResolver.Builder builder = GitResolver.builder();
                assertThrows(
                    IllegalArgumentException.class, 
                    () -> builder.gitSshSessionFactory(null)
                );
            }
        }

        @Nested
        class BuildMethod {
            @Test
            @DisplayName("should throw when git repository URI is not configured")
            void test1() {
                GitResolver.Builder builder = GitResolver.builder()
                    .gitBranch(DEFAULT_GIT_BRANCH)
                    .gitCloneDirectory(CLONE_DIR)
                    .resourceFilePath(DEFAULT_RESOURCE_FILE_PATH);
                
                assertThrows(
                    IllegalStateException.class, 
                    () -> builder.build()
                );
            }

            @Test
            @DisplayName("should throw when git branch is not configured")
            void test2() {
                GitResolver.Builder builder = GitResolver.builder()
                    .gitRepositoryUri(LOCAL_GIT_HTTP_SERVER_REPO_URI)
                    .gitCloneDirectory(CLONE_DIR)
                    .resourceFilePath(DEFAULT_RESOURCE_FILE_PATH);
                
                assertThrows(
                    IllegalStateException.class, 
                    () -> builder.build()
                );
            }

            @Test
            @DisplayName("should throw when git clone directory is not configured")
            void test3() {
                GitResolver.Builder builder = GitResolver.builder()
                    .gitRepositoryUri(LOCAL_GIT_HTTP_SERVER_REPO_URI)
                    .gitBranch(DEFAULT_GIT_BRANCH)
                    .resourceFilePath(DEFAULT_RESOURCE_FILE_PATH);
                
                assertThrows(
                    IllegalStateException.class, 
                    () -> builder.build()
                );
            }

            @Test
            @DisplayName("should throw when resource file path is not configured")
            void test4() {
                GitResolver.Builder builder = GitResolver.builder()
                    .gitRepositoryUri(LOCAL_GIT_HTTP_SERVER_REPO_URI)
                    .gitBranch(DEFAULT_GIT_BRANCH)
                    .gitCloneDirectory(CLONE_DIR);
                
                assertThrows(
                    IllegalStateException.class, 
                    () -> builder.build()
                );
            }

            @Test
            @DisplayName("should throw when git repository URI is invalid")
            void test5() {
                // Make sure clone directory is clean so that git resolver will clone 
                // instead of open.
                Path freshCloneDir = CLONE_DIR.resolve("test5");

                GitResolver.Builder builder = GitResolver.builder()
                    .gitRepositoryUri(
                        // Invalid URI.
                        LOCAL_GIT_HTTP_SERVER_REPO_URI + ".invalid.git"
                    )
                    .gitBranch(DEFAULT_GIT_BRANCH)
                    .gitCloneDirectory(freshCloneDir)
                    .resourceFilePath(DEFAULT_RESOURCE_FILE_PATH);
                
                assertThrows(
                    ExternalizedPropertiesException.class, 
                    () -> builder.build()
                );
            }

            @Test
            @DisplayName("should throw when git branch does not exist")
            void test6() {
                GitResolver.Builder builder = GitResolver.builder()
                    .gitRepositoryUri(LOCAL_GIT_HTTP_SERVER_REPO_URI)
                    .gitBranch("non-existent-branch")
                    .gitCloneDirectory(CLONE_DIR)
                    .resourceFilePath(DEFAULT_RESOURCE_FILE_PATH);
                
                assertThrows(
                    ExternalizedPropertiesException.class, 
                    () -> builder.build()
                );
            }

            @Test
            @DisplayName("should throw when resource file does not exist")
            void test7() {
                GitResolver.Builder builder = GitResolver.builder()
                    .gitRepositoryUri(LOCAL_GIT_HTTP_SERVER_REPO_URI)
                    .gitBranch(DEFAULT_GIT_BRANCH)
                    .gitCloneDirectory(CLONE_DIR)
                    .resourceFilePath("non-existent.properties");
                
                assertThrows(
                    ExternalizedPropertiesException.class, 
                    () -> builder.build()
                );
            }

            @Test
            @DisplayName(
                "should work when specified git branch has remote name e.g. origin/main"
            )
            void test8() throws Exception {
                GitResolver.Builder builder = GitResolver.builder()
                    .gitRepositoryUri(LOCAL_GIT_HTTP_SERVER_REPO_URI)
                    // Has remote e.g. origin/main
                    .gitBranch("origin/" + DEFAULT_GIT_BRANCH)
                    .gitCloneDirectory(CLONE_DIR)
                    .resourceFilePath(DEFAULT_RESOURCE_FILE_PATH)
                    .gitCredentialsProvider(DEFAULT_CREDENTIALS);
                
                GitResolver gitResolver = builder.build();
                assertNotNull(gitResolver);
            }

            @Test
            @DisplayName("should use SSH session factory when connecting to SSH git server")
            void test9() throws Exception {
                // Make sure clone directory is clean so that git resolver will clone 
                // instead of open.
                Path freshCloneDir = CLONE_DIR.resolve("test9");

                SshdSessionFactory sshSessionFactory = new SshdSessionFactoryBuilder()
                    .setHomeDirectory(SSH_DIR.getParent().toFile())
                    .setSshDirectory(SSH_DIR.toFile())
                    .setConfigFile(sshDir -> SSH_CONFIG_FILE.toFile())
                    .setDefaultKeysProvider(sshDir -> Arrays.asList(TEST_RSA_KEY_PAIR))
                    .build(new JGitKeyCache());

                GitResolver.Builder builder = GitResolver.builder()
                    .gitRepositoryUri(LOCAL_GIT_SSH_SERVER_REPO_URI)
                    .gitBranch(DEFAULT_GIT_BRANCH)
                    .gitCloneDirectory(freshCloneDir)
                    .resourceFilePath(DEFAULT_RESOURCE_FILE_PATH)
                    .gitSshSessionFactory(sshSessionFactory);
                
                GitResolver gitResolver = builder.build();
                assertNotNull(gitResolver);
            }

            @Test
            @DisplayName("should ignore SSH session factory when connecting to HTTP git server")
            void test10() throws Exception {
                // Make sure clone directory is clean so that git resolver will clone 
                // instead of open.
                Path freshCloneDir = CLONE_DIR.resolve("test10");

                SshdSessionFactory sshSessionFactory = new SshdSessionFactoryBuilder()
                    .setHomeDirectory(SSH_DIR.getParent().toFile())
                    .setSshDirectory(SSH_DIR.toFile())
                    .setConfigFile(sshDir -> SSH_CONFIG_FILE.toFile())
                    .setDefaultKeysProvider(sshDir -> Arrays.asList(TEST_RSA_KEY_PAIR))
                    .build(new JGitKeyCache());

                GitResolver.Builder builder = GitResolver.builder()
                    // Connecting to HTTP Git server (not SSH Git server).
                    .gitRepositoryUri(LOCAL_GIT_HTTP_SERVER_REPO_URI)
                    .gitBranch(DEFAULT_GIT_BRANCH)
                    .gitCloneDirectory(freshCloneDir)
                    .resourceFilePath(DEFAULT_RESOURCE_FILE_PATH)
                    .gitCredentialsProvider(DEFAULT_CREDENTIALS)
                    // Should just be ignored.
                    .gitSshSessionFactory(sshSessionFactory);
                
                GitResolver gitResolver = builder.build();
                assertNotNull(gitResolver);
            }

            @Test
            @DisplayName("should use credentials provider when connecting to HTTP git server")
            void test11() throws Exception {
                // Make sure clone directory is clean so that git resolver will clone 
                // instead of open.
                Path freshCloneDir = CLONE_DIR.resolve("test11");

                GitResolver.Builder builder = GitResolver.builder()
                    .gitRepositoryUri(LOCAL_GIT_HTTP_SERVER_REPO_URI)
                    .gitBranch(DEFAULT_GIT_BRANCH)
                    .gitCloneDirectory(freshCloneDir)
                    .resourceFilePath(DEFAULT_RESOURCE_FILE_PATH)
                    .gitCredentialsProvider(DEFAULT_CREDENTIALS);
                
                GitResolver gitResolver = builder.build();
                assertNotNull(gitResolver);
            }

            @Test
            @DisplayName(
                "should clone git repository when clone directory does not contain a git repo"
            )
            void test12() throws IOException {
                // Make sure clone directory is clean so that git resolver will clone 
                // instead of open.
                Path freshCloneDir = CLONE_DIR.resolve("test12");

                GitResolver.Builder builder = GitResolver.builder()
                    .gitRepositoryUri(LOCAL_GIT_HTTP_SERVER_REPO_URI)
                    .gitBranch(DEFAULT_GIT_BRANCH)
                    .gitCloneDirectory(freshCloneDir)
                    .resourceFilePath(DEFAULT_RESOURCE_FILE_PATH)
                    .gitCredentialsProvider(DEFAULT_CREDENTIALS);
                
                GitResolver gitResolver = builder.build();
                assertNotNull(gitResolver);
            }

            @Test
            @DisplayName(
                "should open git in the clone directory when it already contains a git repository"
            )
            void test13() throws Exception {
                if (!Files.exists(CLONE_DIR)) {
                    // Let's clone here.
                    Git.cloneRepository()
                        .setURI(GIT_BARE_REPO.getRepository().getDirectory().getAbsolutePath())
                        .setDirectory(CLONE_DIR.toFile())
                        .call();
                }

                GitResolver.Builder builder = GitResolver.builder()
                    .gitRepositoryUri(LOCAL_GIT_HTTP_SERVER_REPO_URI)
                    .gitBranch(DEFAULT_GIT_BRANCH)
                    .gitCloneDirectory(CLONE_DIR)
                    .resourceFilePath(DEFAULT_RESOURCE_FILE_PATH)
                    .gitCredentialsProvider(DEFAULT_CREDENTIALS);
                
                GitResolver gitResolver = builder.build();
                assertNotNull(gitResolver);
            }

            @Test
            @DisplayName("should never return null")
            void test14() {
                GitResolver.Builder builder = GitResolver.builder()
                    .gitRepositoryUri(LOCAL_GIT_HTTP_SERVER_REPO_URI)
                    .gitBranch(DEFAULT_GIT_BRANCH)
                    .gitCloneDirectory(CLONE_DIR)
                    .resourceFilePath(DEFAULT_RESOURCE_FILE_PATH)
                    .gitCredentialsProvider(DEFAULT_CREDENTIALS);
                
                GitResolver gitResolver = builder.build();
                assertNotNull(gitResolver);
            }
        }
    }

    @Nested
    class ResolveMethod {
        @Test
        @DisplayName("should resolve property from resource file")
        void test1() {
            GitResolver gitResolver = gitResolver();

            InvocationContext context = INVOCATION_CONTEXT_FACTORY.fromMethodReference(
                ProxyInterface::property, 
                externalizedProperties(gitResolver)
            );
            
            Optional<String> result = gitResolver.resolve(context, "property");
            assertTrue(result.isPresent());
            assertEquals("property-value", result.get());
        }

        @Test
        @DisplayName(
            "should return empty Optional when property is not in resource file"
        )
        void test2() {
            GitResolver gitResolver = gitResolver();

            InvocationContext context = INVOCATION_CONTEXT_FACTORY.fromMethodReference(
                ProxyInterface::property, 
                externalizedProperties(gitResolver)
            );
            
            Optional<String> result = gitResolver.resolve(context, "non.existent");
            assertFalse(result.isPresent());
        }
    }

    private static GitResolver gitResolver() {
        return gitResolver(
            DEFAULT_RESOURCE_FILE_PATH, 
            new PropertiesReader()
        );
    }

    private static GitResolver gitResolver(
            String resourceFile, 
            ResourceReader resourceReader
    ) {
        return GitResolver.builder()
            .gitRepositoryUri(LOCAL_GIT_HTTP_SERVER_REPO_URI)
            .gitBranch(DEFAULT_GIT_BRANCH)
            .gitCloneDirectory(CLONE_DIR)
            .gitCredentialsProvider(DEFAULT_CREDENTIALS)
            .resourceFilePath(resourceFile)
            .resourceReader(resourceReader)
            .build();
    }

    private static ExternalizedProperties externalizedProperties(Resolver... resolvers) {
        return ExternalizedProperties.builder().resolvers(resolvers).build();
    }

    private static KeyPair createTestRsaKeyPair() {
        try {
            KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
            keyPairGenerator.initialize(1024);
            return keyPairGenerator.generateKeyPair();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }

    private static void deleteRecursively(Path pathToDelete) throws IOException {
        try (Stream<Path> paths = Files.walk(pathToDelete)) {
            for (Path path : paths.sorted(Comparator.reverseOrder())
                    .toArray(Path[]::new)) {
                Files.delete(path);
            }
        }
    }

    private static void setupGitRepo() throws Exception {
        GIT_BARE_REPO = Git.init()
            .setBare(true)
            .setDirectory(BARE_REPO_DIR.toFile())
            .call();

        // Push app.properties to a clone of Git bare repo and push it.
        Path appPropertiesPushCloneDir = ROOT_TEST_DIR.resolve("app-properties-push");
        Git cloned = Git.cloneRepository()
            .setURI(GIT_BARE_REPO.getRepository().getDirectory().getAbsolutePath())
            .setDirectory(appPropertiesPushCloneDir.toFile())
            .call();

        pushInitial(cloned);
        pushAppProperties(cloned);
    }

    private static void setupLocalGitHttpServer() throws Exception {
        LOCAL_GIT_HTTP_SERVER = new SimpleHttpServer(GIT_BARE_REPO.getRepository());
        LOCAL_GIT_HTTP_SERVER.start();

        // Use this git http server in all tests (except ssh git server tests).
        LOCAL_GIT_HTTP_SERVER_REPO_URI = LOCAL_GIT_HTTP_SERVER.getUri().toString();
    }

    private static void setupLocalGitSshServer() throws Exception {
        SSH_DIR = ROOT_TEST_DIR.resolve(".ssh");
        SSH_CONFIG_FILE = SSH_DIR.resolve("config");

        // We need the config file to disable StrictHostChecking
        Files.createDirectories(SSH_DIR);
        // Use the config file in resources.
        Files.copy(
            GitResolverTests.class.getResourceAsStream("/config"), 
            SSH_CONFIG_FILE,
            StandardCopyOption.REPLACE_EXISTING
        );

        LOCAL_GIT_SSH_SERVER = new SshTestGitServer(
            "git",
            TEST_RSA_KEY_PAIR.getPublic(),
            GIT_BARE_REPO.getRepository(),
            TEST_RSA_KEY_PAIR
        );
        int serverPort = LOCAL_GIT_SSH_SERVER.start();
        LOCAL_GIT_SSH_SERVER_REPO_URI = "ssh://git@localhost:" + serverPort +
            GIT_BARE_REPO.getRepository().getDirectory().getPath();
    }

    private static void pushInitial(Git cloned) throws Exception {
        Files.createFile(
            cloned.getRepository().getWorkTree().toPath().resolve("README.md")
        );

        cloned.add().addFilepattern(".").call();
        cloned.commit().setMessage("Initial commit").call();
        cloned.push().call();
    }

    private static void pushAppProperties(Git cloned) throws Exception {
        Path appPropertiesPath = 
            Paths.get(GitResolverTests.class.getResource("/app.properties").toURI());
        
        Files.copy(
            appPropertiesPath, 
            cloned.getRepository().getWorkTree().toPath().resolve(DEFAULT_RESOURCE_FILE_PATH), 
            StandardCopyOption.REPLACE_EXISTING
        );

        cloned.checkout().setName(DEFAULT_GIT_BRANCH).setCreateBranch(true).call();
        cloned.add().addFilepattern(".").call();
        cloned.commit().setMessage(DEFAULT_RESOURCE_FILE_PATH).call();
        cloned.push().call();
    }

    private static interface ProxyInterface {
        @ExternalizedProperty("property")
        String property();
    }
}
