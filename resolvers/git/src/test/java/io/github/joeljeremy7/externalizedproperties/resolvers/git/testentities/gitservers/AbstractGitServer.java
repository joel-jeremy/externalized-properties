package io.github.joeljeremy7.externalizedproperties.resolvers.git.testentities.gitservers;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Comparator;
import java.util.stream.Stream;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.lib.Repository;

public abstract class AbstractGitServer implements AutoCloseable {
  public static final String DEFAULT_BARE_GIT_REPO_DIR_NAME = "configs-repo.git";

  /**
   * Start the Git server.
   *
   * @return This git server.
   * @throws Exception if an exception occurred while starting the Git server.
   */
  public abstract AbstractGitServer start() throws Exception;

  /**
   * Stop the Git server.
   *
   * @return This git server.
   * @throws Exception if an exception occurred while stopping the Git server.
   */
  public abstract AbstractGitServer stop() throws Exception;

  /** {@inheritDoc} */
  @Override
  public void close() throws Exception {
    stop();
  }

  /**
   * Get the Git repository URI to connect to this server.
   *
   * @return The Git repository URI to connect to this server.
   */
  public abstract String getRepositoryUri();

  /**
   * The Git repository.
   *
   * @return The Git repository.
   */
  public abstract Repository getRepository();

  /**
   * Initialize the Git bare repository which should be hosted by a child Git server.
   *
   * @param rootDir The root directory to initialize the base Git repository in.
   * @param defaultGitBranch The default Git branch.
   * @return The bare Git repository.
   * @throws Exception if an exception occurred while initializing the base Git repository.
   */
  protected Git initBareGitRepository(
      Path bareGitRepoDir, String defaultGitBranch, Path... filesToCommit) throws Exception {
    Git bareRepo = Git.init().setBare(true).setDirectory(bareGitRepoDir.toFile()).call();

    // Push app.properties to a clone of Git bare repo and push it.
    Path appPropertiesPushCloneDir = bareGitRepoDir.getParent().resolve("initial-push-dir");
    Git cloned =
        Git.cloneRepository()
            .setURI(bareRepo.getRepository().getDirectory().getAbsolutePath())
            .setDirectory(appPropertiesPushCloneDir.toFile())
            .call();

    pushInitial(cloned);
    pushFiles(cloned, defaultGitBranch, filesToCommit);

    return bareRepo;
  }

  protected static void deleteRecursively(Path pathToDelete) throws IOException {
    try (Stream<Path> paths = Files.walk(pathToDelete)) {
      for (Path path : paths.sorted(Comparator.reverseOrder()).toArray(Path[]::new)) {
        Files.delete(path);
      }
    }
  }

  private static void pushInitial(Git cloned) throws Exception {
    Files.createFile(cloned.getRepository().getWorkTree().toPath().resolve("README.md"));

    cloned.add().addFilepattern(".").call();
    cloned.commit().setMessage("Initial commit").call();
    cloned.push().call();
  }

  private static void pushFiles(Git cloned, String gitBranch, Path... filesToCommit)
      throws Exception {
    // Copy files to work tree.
    for (Path fileToCommit : filesToCommit) {
      String fileName = fileToCommit.getFileName().toString();
      Files.copy(
          fileToCommit,
          cloned.getRepository().getWorkTree().toPath().resolve(fileName),
          StandardCopyOption.REPLACE_EXISTING);
    }

    cloned.checkout().setName(gitBranch).setCreateBranch(true).call();
    cloned.add().addFilepattern(".").call();
    cloned.commit().setMessage("Commit files").call();
    cloned.push().call();
  }
}
