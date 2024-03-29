package io.github.joeljeremy.externalizedproperties.git;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.joeljeremy.externalizedproperties.core.ExternalizedProperties;
import io.github.joeljeremy.externalizedproperties.core.ExternalizedPropertiesException;
import io.github.joeljeremy.externalizedproperties.core.ExternalizedProperty;
import io.github.joeljeremy.externalizedproperties.core.InvocationContext;
import io.github.joeljeremy.externalizedproperties.core.Resolver;
import io.github.joeljeremy.externalizedproperties.core.testfixtures.InvocationContextUtils;
import io.github.joeljeremy.externalizedproperties.core.testfixtures.InvocationContextUtils.InvocationContextTestFactory;
import io.github.joeljeremy.externalizedproperties.git.testentities.gitservers.LocalHttpGitServer;
import io.github.joeljeremy.externalizedproperties.git.testentities.resourcereaders.JsonReader;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.Optional;
import java.util.stream.Stream;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

public class GitResolverTests {
  static final InvocationContextTestFactory<ProxyInterface> INVOCATION_CONTEXT_FACTORY =
      InvocationContextUtils.testFactory(ProxyInterface.class);

  static Path ROOT_TEST_DIR;
  static Path CLONE_DIR;

  // Local Git servers for testing.
  static LocalHttpGitServer LOCAL_HTTP_GIT_SERVER;
  static GitRepository GIT_REPOSITORY;

  static final String DEFAULT_GIT_BRANCH = "git-resolver-configs";
  static final String APP_PROPERTIES_FILE_PATH = "app.properties";
  static final String APP_JSON_FILE_PATH = "app.json";

  @BeforeAll
  static void setup() throws Exception {
    ROOT_TEST_DIR = Files.createTempDirectory(GitResolverTests.class.getSimpleName());
    CLONE_DIR = Files.createDirectories(ROOT_TEST_DIR.resolve("configs-repo"));

    @SuppressWarnings("resource")
    LocalHttpGitServer httpGitServer =
        new LocalHttpGitServer(DEFAULT_GIT_BRANCH, filesToCommitToGitRepo());
    LOCAL_HTTP_GIT_SERVER = httpGitServer.start();

    GIT_REPOSITORY =
        GitRepository.builder()
            .uri(LOCAL_HTTP_GIT_SERVER.getRepositoryUri())
            .branch(DEFAULT_GIT_BRANCH)
            .cloneDirectory(CLONE_DIR)
            .credentialsProvider(LocalHttpGitServer.DEFAULT_CREDENTIALS)
            .build();
  }

  @AfterAll
  static void cleanup() throws Exception {
    LOCAL_HTTP_GIT_SERVER.stop();
    deleteRecursively(ROOT_TEST_DIR);
  }

  @Nested
  class BuilderTests {
    @Nested
    class ResourceFilePathMethod {
      @ParameterizedTest
      @NullAndEmptySource
      @ValueSource(
          strings = {
            "   " // blank
          })
      @DisplayName("should throw when path to resource file path argument is null, empty, or blank")
      void test1(String resourceFilePath) {
        GitResolver.Builder builder = GitResolver.builder();
        assertThrows(
            IllegalArgumentException.class, () -> builder.resourceFilePath(resourceFilePath));
      }
    }

    @Nested
    class ResourceReaderMethod {
      @Test
      @DisplayName("should throw when resource reader argument is null")
      void test1() {
        GitResolver.Builder builder = GitResolver.builder();
        assertThrows(IllegalArgumentException.class, () -> builder.resourceReader(null));
      }
    }

    @Nested
    class BuildMethod {
      @Test
      @DisplayName("should throw when git repository is not configured")
      void test1() {
        GitResolver.Builder builder =
            GitResolver.builder().resourceFilePath(APP_PROPERTIES_FILE_PATH);

        assertThrows(IllegalStateException.class, () -> builder.build());
      }

      @Test
      @DisplayName("should throw when resource file path is not configured")
      void test2() {
        GitResolver.Builder builder = GitResolver.builder().gitRepository(GIT_REPOSITORY);

        assertThrows(IllegalStateException.class, () -> builder.build());
      }

      @Test
      @DisplayName("should throw when resource file does not exist")
      void test3() {
        GitResolver.Builder builder =
            GitResolver.builder()
                .gitRepository(GIT_REPOSITORY)
                .resourceFilePath("non-existent.properties");

        assertThrows(ExternalizedPropertiesException.class, () -> builder.build());
      }

      @Test
      @DisplayName("should never return null")
      void test4() {
        GitResolver.Builder builder =
            GitResolver.builder()
                .gitRepository(GIT_REPOSITORY)
                .resourceFilePath(APP_PROPERTIES_FILE_PATH);

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
      GitResolver gitResolver =
          GitResolver.builder()
              .gitRepository(GIT_REPOSITORY)
              .resourceFilePath(APP_PROPERTIES_FILE_PATH)
              .build();

      InvocationContext context =
          INVOCATION_CONTEXT_FACTORY.fromMethodReference(
              ProxyInterface::property, externalizedProperties(gitResolver));

      Optional<String> result = gitResolver.resolve(context, "property");
      assertTrue(result.isPresent());
      assertEquals("property-value", result.get());
    }

    @Test
    @DisplayName("should return empty Optional when property is not in resource file")
    void test2() {
      GitResolver gitResolver =
          GitResolver.builder()
              .gitRepository(GIT_REPOSITORY)
              .resourceFilePath(APP_PROPERTIES_FILE_PATH)
              .build();

      InvocationContext context =
          INVOCATION_CONTEXT_FACTORY.fromMethodReference(
              ProxyInterface::property, externalizedProperties(gitResolver));

      Optional<String> result = gitResolver.resolve(context, "non.existent");
      assertFalse(result.isPresent());
    }

    @Test
    @DisplayName("should use resource reader to read resource file")
    void test3() {
      GitResolver gitResolver =
          GitResolver.builder()
              .gitRepository(GIT_REPOSITORY)
              .resourceFilePath(APP_JSON_FILE_PATH)
              .resourceReader(new JsonReader())
              .build();

      InvocationContext context =
          INVOCATION_CONTEXT_FACTORY.fromMethodReference(
              ProxyInterface::property, externalizedProperties(gitResolver));

      Optional<String> result = gitResolver.resolve(context, "property");
      assertTrue(result.isPresent());
      assertEquals("property-value", result.get());
    }
  }

  static ExternalizedProperties externalizedProperties(Resolver... resolvers) {
    return ExternalizedProperties.builder().resolvers(resolvers).build();
  }

  static Path[] filesToCommitToGitRepo() throws URISyntaxException {
    return new Path[] {
      getResourceAsPath("/" + APP_PROPERTIES_FILE_PATH), getResourceAsPath("/" + APP_JSON_FILE_PATH)
    };
  }

  static Path getResourceAsPath(String resourceName) throws URISyntaxException {
    return Paths.get(GitRepositoryTests.class.getResource(resourceName).toURI());
  }

  static void deleteRecursively(Path pathToDelete) throws IOException {
    try (Stream<Path> paths = Files.walk(pathToDelete)) {
      for (Path path : paths.sorted(Comparator.reverseOrder()).toArray(Path[]::new)) {
        Files.delete(path);
      }
    }
  }

  static interface ProxyInterface {
    @ExternalizedProperty("property")
    String property();
  }
}
