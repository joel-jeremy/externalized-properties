package io.github.joeljeremy.externalizedproperties.git;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.joeljeremy.externalizedproperties.core.ExternalizedPropertiesException;
import io.github.joeljeremy.externalizedproperties.git.testentities.gitservers.LocalHttpGitServer;
import io.github.joeljeremy.externalizedproperties.git.testentities.gitservers.LocalSshGitServer;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Comparator;
import java.util.stream.Stream;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.transport.sshd.JGitKeyCache;
import org.eclipse.jgit.transport.sshd.SshdSessionFactory;
import org.eclipse.jgit.transport.sshd.SshdSessionFactoryBuilder;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

public class GitRepositoryTests {
  private static final String DEFAULT_GIT_BRANCH = "git-resolver-configs";
  private static final String APP_PROPERTIES_FILE_PATH = "app.properties";

  private static Path ROOT_TEST_DIR;
  private static Path CLONE_DIR;

  private static LocalHttpGitServer LOCAL_HTTP_GIT_SERVER;
  private static LocalSshGitServer LOCAL_SSH_GIT_SERVER;

  @BeforeAll
  static void setup() throws Exception {
    ROOT_TEST_DIR = Files.createTempDirectory(GitRepositoryTests.class.getSimpleName());
    CLONE_DIR = Files.createDirectories(ROOT_TEST_DIR.resolve("configs-repo"));

    @SuppressWarnings("resource")
    LocalHttpGitServer httpGitServer =
        new LocalHttpGitServer(DEFAULT_GIT_BRANCH, filesToCommitToGitRepo());
    LOCAL_HTTP_GIT_SERVER = httpGitServer.start();
    ;

    @SuppressWarnings("resource")
    LocalSshGitServer sshGitServer =
        new LocalSshGitServer(DEFAULT_GIT_BRANCH, filesToCommitToGitRepo());
    LOCAL_SSH_GIT_SERVER = sshGitServer.start();
  }

  @AfterAll
  static void cleanup() throws Exception {
    LOCAL_HTTP_GIT_SERVER.stop();
    LOCAL_SSH_GIT_SERVER.stop();
    deleteRecursively(ROOT_TEST_DIR);
  }

  @Nested
  class BuilderTests {
    @Nested
    class UriMethod {
      @ParameterizedTest
      @NullAndEmptySource
      @ValueSource(
          strings = {
            "   " // blank
          })
      @DisplayName("should throw when uri argument is null, empty, or blank")
      void test1(String uri) {
        GitRepository.Builder builder = GitRepository.builder();
        assertThrows(IllegalArgumentException.class, () -> builder.uri(uri));
      }
    }

    @Nested
    class BranchMethod {
      @ParameterizedTest
      @NullAndEmptySource
      @ValueSource(
          strings = {
            "   " // blank
          })
      @DisplayName("should throw when branch argument is null, empty, or blank")
      void test1(String branch) {
        GitRepository.Builder builder = GitRepository.builder();
        assertThrows(IllegalArgumentException.class, () -> builder.branch(branch));
      }
    }

    @Nested
    class CloneDirectoryMethod {
      @Test
      @DisplayName("should throw when clone directory argument is null")
      void test1() {
        GitRepository.Builder builder = GitRepository.builder();
        assertThrows(IllegalArgumentException.class, () -> builder.cloneDirectory(null));
      }
    }

    @Nested
    class CredentialsProviderMethod {
      @Test
      @DisplayName("should throw when credentials provider argument is null")
      void test1() {
        GitRepository.Builder builder = GitRepository.builder();
        assertThrows(IllegalArgumentException.class, () -> builder.credentialsProvider(null));
      }
    }

    @Nested
    class SshSessionFactoryMethod {
      @Test
      @DisplayName("should throw when ssh session factory argument is null")
      void test1() {
        GitRepository.Builder builder = GitRepository.builder();
        assertThrows(IllegalArgumentException.class, () -> builder.sshSessionFactory(null));
      }
    }

    @Nested
    class BuildMethod {
      @Test
      @DisplayName("should throw when uri is not configured")
      void test1() {
        GitRepository.Builder builder =
            GitRepository.builder().branch(DEFAULT_GIT_BRANCH).cloneDirectory(CLONE_DIR);

        assertThrows(IllegalStateException.class, () -> builder.build());
      }

      @Test
      @DisplayName("should throw when branch is not configured")
      void test2() {
        GitRepository.Builder builder =
            GitRepository.builder()
                .uri(LOCAL_HTTP_GIT_SERVER.getRepositoryUri())
                .cloneDirectory(CLONE_DIR);

        assertThrows(IllegalStateException.class, () -> builder.build());
      }

      @Test
      @DisplayName("should throw when clone directory is not configured")
      void test3() {
        GitRepository.Builder builder =
            GitRepository.builder()
                .uri(LOCAL_HTTP_GIT_SERVER.getRepositoryUri())
                .branch(DEFAULT_GIT_BRANCH);

        assertThrows(IllegalStateException.class, () -> builder.build());
      }

      @Test
      @DisplayName("should never return null")
      void test4() {
        GitRepository.Builder builder =
            GitRepository.builder()
                .uri(LOCAL_HTTP_GIT_SERVER.getRepositoryUri())
                .branch(DEFAULT_GIT_BRANCH)
                .cloneDirectory(CLONE_DIR)
                .credentialsProvider(LocalHttpGitServer.DEFAULT_CREDENTIALS);

        GitRepository gitRepository = builder.build();
        assertNotNull(gitRepository);
      }
    }
  }

  @Nested
  class CheckoutMethod {
    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(
        strings = {
          "   " // blank
        })
    @DisplayName("should throw when path to checkout argument is null, empty, or blank")
    void validationTest1(String pathToCheckout) {
      GitRepository gitRepository =
          GitRepository.builder()
              .uri(LOCAL_HTTP_GIT_SERVER.getRepositoryUri())
              .branch(DEFAULT_GIT_BRANCH)
              .cloneDirectory(CLONE_DIR)
              .credentialsProvider(LocalHttpGitServer.DEFAULT_CREDENTIALS)
              .build();

      assertThrows(IllegalArgumentException.class, () -> gitRepository.checkout(pathToCheckout));
    }

    @Test
    @DisplayName("should checkout file from Git repository")
    void test1() {
      GitRepository gitRepository =
          GitRepository.builder()
              .uri(LOCAL_HTTP_GIT_SERVER.getRepositoryUri())
              .branch(DEFAULT_GIT_BRANCH)
              .cloneDirectory(CLONE_DIR)
              .credentialsProvider(LocalHttpGitServer.DEFAULT_CREDENTIALS)
              .build();

      Path checkedOutResourceFile = gitRepository.checkout(APP_PROPERTIES_FILE_PATH);
      assertTrue(Files.exists(checkedOutResourceFile));
    }

    @Test
    @DisplayName("should work when specified git branch has remote name e.g. origin/main")
    void test2() throws Exception {
      GitRepository gitRepository =
          GitRepository.builder()
              .uri(LOCAL_HTTP_GIT_SERVER.getRepositoryUri())
              // Has remote e.g. origin/main
              .branch("origin/" + DEFAULT_GIT_BRANCH)
              .cloneDirectory(CLONE_DIR)
              .credentialsProvider(LocalHttpGitServer.DEFAULT_CREDENTIALS)
              .build();

      Path checkedOutResourceFile = gitRepository.checkout(APP_PROPERTIES_FILE_PATH);
      assertTrue(Files.exists(checkedOutResourceFile));
    }

    @Test
    @DisplayName("should throw when git branch does not exist")
    void test3() {
      GitRepository gitRepository =
          GitRepository.builder()
              .uri(LOCAL_HTTP_GIT_SERVER.getRepositoryUri())
              .branch("non-existent-branch")
              .cloneDirectory(CLONE_DIR)
              .credentialsProvider(LocalHttpGitServer.DEFAULT_CREDENTIALS)
              .build();

      assertThrows(
          ExternalizedPropertiesException.class,
          () -> gitRepository.checkout(APP_PROPERTIES_FILE_PATH));
    }

    @Test
    @DisplayName("should throw when uri is invalid")
    void test4() {
      // Make sure clone directory is clean so that git resolver will clone
      // instead of open.
      Path freshCloneDir = CLONE_DIR.resolve("test4");

      GitRepository gitRepository =
          GitRepository.builder()
              .uri(
                  // Invalid URI.
                  LOCAL_HTTP_GIT_SERVER.getRepositoryUri() + ".invalid.git")
              .branch(DEFAULT_GIT_BRANCH)
              .cloneDirectory(freshCloneDir)
              .build();

      assertThrows(
          ExternalizedPropertiesException.class,
          () -> gitRepository.checkout(APP_PROPERTIES_FILE_PATH));
    }

    @Test
    @DisplayName("should use SSH session factory when connecting to SSH git server")
    void test5() throws Exception {
      // Make sure clone directory is clean so that git resolver will clone
      // instead of open.
      Path freshCloneDir = CLONE_DIR.resolve("test5");
      Path serverSshDir = LOCAL_SSH_GIT_SERVER.getSshDirectory();

      SshdSessionFactory sshSessionFactory =
          new SshdSessionFactoryBuilder()
              .setHomeDirectory(serverSshDir.getParent().toFile())
              .setSshDirectory(serverSshDir.toFile())
              .setConfigFile(sshDir -> serverSshDir.resolve("config").toFile())
              .setDefaultKeysProvider(sshDir -> Arrays.asList(LOCAL_SSH_GIT_SERVER.getRsaKeyPair()))
              .build(new JGitKeyCache());

      GitRepository gitRepository =
          GitRepository.builder()
              .uri(LOCAL_SSH_GIT_SERVER.getRepositoryUri())
              .branch(DEFAULT_GIT_BRANCH)
              .cloneDirectory(freshCloneDir)
              .sshSessionFactory(sshSessionFactory)
              .build();

      Path checkedOutResourceFile = gitRepository.checkout(APP_PROPERTIES_FILE_PATH);
      assertTrue(Files.exists(checkedOutResourceFile));
    }

    @Test
    @DisplayName("should ignore SSH session factory when connecting to HTTP git server")
    void test6() throws Exception {
      // Make sure clone directory is clean so that git resolver will clone
      // instead of open.
      Path freshCloneDir = CLONE_DIR.resolve("test6");
      Path serverSshDir = LOCAL_SSH_GIT_SERVER.getSshDirectory();

      SshdSessionFactory sshSessionFactory =
          new SshdSessionFactoryBuilder()
              .setHomeDirectory(serverSshDir.getParent().toFile())
              .setSshDirectory(serverSshDir.toFile())
              .setConfigFile(sshDir -> serverSshDir.resolve("config").toFile())
              .setDefaultKeysProvider(sshDir -> Arrays.asList(LOCAL_SSH_GIT_SERVER.getRsaKeyPair()))
              .build(new JGitKeyCache());

      GitRepository gitRepository =
          GitRepository.builder()
              // Connecting to HTTP Git server (not SSH Git server).
              .uri(LOCAL_HTTP_GIT_SERVER.getRepositoryUri())
              .branch(DEFAULT_GIT_BRANCH)
              .cloneDirectory(freshCloneDir)
              .credentialsProvider(LocalHttpGitServer.DEFAULT_CREDENTIALS)
              // Should just be ignored.
              .sshSessionFactory(sshSessionFactory)
              .build();

      Path checkedOutResourceFile = gitRepository.checkout(APP_PROPERTIES_FILE_PATH);
      assertTrue(Files.exists(checkedOutResourceFile));
    }

    @Test
    @DisplayName("should use credentials provider when connecting to HTTP git server")
    void test7() throws Exception {
      // Make sure clone directory is clean so that git resolver will clone
      // instead of open.
      Path freshCloneDir = CLONE_DIR.resolve("test7");

      GitRepository gitRepository =
          GitRepository.builder()
              .uri(LOCAL_HTTP_GIT_SERVER.getRepositoryUri())
              .branch(DEFAULT_GIT_BRANCH)
              .cloneDirectory(freshCloneDir)
              .credentialsProvider(LocalHttpGitServer.DEFAULT_CREDENTIALS)
              .build();

      Path checkedOutResourceFile = gitRepository.checkout(APP_PROPERTIES_FILE_PATH);
      assertTrue(Files.exists(checkedOutResourceFile));
    }

    @Test
    @DisplayName("should clone git repository when clone directory does not contain a git repo")
    void test8() throws IOException {
      // Make sure clone directory is clean so that git resolver will clone
      // instead of open.
      Path freshCloneDir = CLONE_DIR.resolve("test8");

      GitRepository gitRepository =
          GitRepository.builder()
              .uri(LOCAL_HTTP_GIT_SERVER.getRepositoryUri())
              .branch(DEFAULT_GIT_BRANCH)
              .cloneDirectory(freshCloneDir)
              .credentialsProvider(LocalHttpGitServer.DEFAULT_CREDENTIALS)
              .build();

      Path checkedOutResourceFile = gitRepository.checkout(APP_PROPERTIES_FILE_PATH);
      assertTrue(Files.exists(checkedOutResourceFile));
    }

    @Test
    @DisplayName("should open git in the clone directory when it already contains a git repository")
    void test9() throws Exception {
      if (!Files.exists(CLONE_DIR)) {
        // Let's clone here.
        Git.cloneRepository()
            .setURI(LOCAL_HTTP_GIT_SERVER.getRepository().getDirectory().getAbsolutePath())
            .setDirectory(CLONE_DIR.toFile())
            .call();
      }

      GitRepository gitRepository =
          GitRepository.builder()
              .uri(LOCAL_HTTP_GIT_SERVER.getRepositoryUri())
              .branch(DEFAULT_GIT_BRANCH)
              .cloneDirectory(CLONE_DIR)
              .credentialsProvider(LocalHttpGitServer.DEFAULT_CREDENTIALS)
              .build();

      Path checkedOutResourceFile = gitRepository.checkout(APP_PROPERTIES_FILE_PATH);
      assertTrue(Files.exists(checkedOutResourceFile));
    }
  }

  private static Path[] filesToCommitToGitRepo() throws URISyntaxException {
    return new Path[] {
      getResourceAsPath("/" + APP_PROPERTIES_FILE_PATH),
    };
  }

  private static Path getResourceAsPath(String resourceName) throws URISyntaxException {
    return Paths.get(GitRepositoryTests.class.getResource(resourceName).toURI());
  }

  private static void deleteRecursively(Path pathToDelete) throws IOException {
    try (Stream<Path> paths = Files.walk(pathToDelete)) {
      for (Path path : paths.sorted(Comparator.reverseOrder()).toArray(Path[]::new)) {
        Files.delete(path);
      }
    }
  }
}
