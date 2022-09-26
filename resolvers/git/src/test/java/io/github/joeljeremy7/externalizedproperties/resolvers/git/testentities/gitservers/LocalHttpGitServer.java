package io.github.joeljeremy7.externalizedproperties.resolvers.git.testentities.gitservers;

import java.nio.file.Files;
import java.nio.file.Path;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.junit.http.SimpleHttpServer;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.transport.CredentialsProvider;
import org.eclipse.jgit.transport.URIish;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;

public class LocalHttpGitServer extends AbstractGitServer {
  public static final CredentialsProvider DEFAULT_CREDENTIALS =
      new UsernamePasswordCredentialsProvider("agitter", "letmein");

  private final Path rootDir = Files.createTempDirectory(String.valueOf(hashCode()));
  private final SimpleHttpServer httpServer;
  private final Git bareGitRepo;

  public LocalHttpGitServer(String defaultGitBranch, Path... filesToCommit) throws Exception {
    bareGitRepo =
        initBareGitRepository(
            rootDir.resolve(DEFAULT_BARE_GIT_REPO_DIR_NAME), defaultGitBranch, filesToCommit);
    httpServer = new SimpleHttpServer(bareGitRepo.getRepository());
  }

  @Override
  public LocalHttpGitServer start() throws Exception {
    httpServer.start();
    return this;
  }

  @Override
  public LocalHttpGitServer stop() throws Exception {
    httpServer.stop();
    bareGitRepo.close();
    // Cleanup.
    deleteRecursively(rootDir);
    return this;
  }

  @Override
  public String getRepositoryUri() {
    URIish uri = httpServer.getUri();
    if (uri == null) {
      throw new IllegalStateException("Git server not yet started.");
    }
    return uri.toString();
  }

  public int getPort() {
    URIish uri = httpServer.getUri();
    if (uri == null) {
      throw new IllegalStateException("Git server not yet started.");
    }
    return uri.getPort();
  }

  @Override
  public Repository getRepository() {
    return bareGitRepo.getRepository();
  }
}
