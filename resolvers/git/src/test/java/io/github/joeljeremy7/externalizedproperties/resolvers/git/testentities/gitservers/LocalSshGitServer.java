package io.github.joeljeremy7.externalizedproperties.resolvers.git.testentities.gitservers;

import io.github.joeljeremy7.externalizedproperties.resolvers.git.GitRepositoryTests;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.junit.ssh.SshTestGitServer;
import org.eclipse.jgit.lib.Repository;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;

public class LocalSshGitServer extends AbstractGitServer {

    private final Path rootDir = Files.createTempDirectory(String.valueOf(hashCode()));
    private final KeyPair rsaKeyPair;
    private final SshTestGitServer localGitServer;
    private final Git bareGitRepo;
    private int port = -1;
    private String repositoryUri;
    
    public LocalSshGitServer(
            String defaultGitBranch,
            Path... filesToCommit
    ) throws Exception {
        setupSshDir();
        bareGitRepo = initBareGitRepository(
            rootDir.resolve(DEFAULT_BARE_GIT_REPO_DIR_NAME), 
            defaultGitBranch,
            filesToCommit
        );
        rsaKeyPair = createTestRsaKeyPair();
        localGitServer = new SshTestGitServer(
            "git",
            rsaKeyPair.getPublic(),
            bareGitRepo.getRepository(),
            rsaKeyPair
        );
    }

    private void setupSshDir() throws IOException {
        Path sshDir = rootDir.resolve(".ssh");
        Path sshConfigFile = sshDir.resolve("config");

        // We need the config file to disable StrictHostChecking
        Files.createDirectories(sshDir);
        // Use the config file in resources.
        Files.copy(
            GitRepositoryTests.class.getResourceAsStream("/config"), 
            sshConfigFile,
            StandardCopyOption.REPLACE_EXISTING
        );
    }

    @Override
    public LocalSshGitServer start() throws Exception {
        port = localGitServer.start();
        repositoryUri = "ssh://git@localhost:" + port + 
            bareGitRepo.getRepository().getDirectory().getPath();
        return this;
    }

    @Override
    public LocalSshGitServer stop() throws Exception {
        localGitServer.stop();
        bareGitRepo.close();
        // Cleanup.
        deleteRecursively(rootDir);
        return this;
    }

    @Override
    public String getRepositoryUri() {
        if (repositoryUri == null) {
            throw new IllegalStateException("Git server not yet started.");
        }
        return repositoryUri;
    }

    public int getPort() {
        if (port == -1) {
            throw new IllegalStateException("Git server not yet started.");
        }
        return port;
    }

    @Override
    public Repository getRepository() {
        return bareGitRepo.getRepository();
    }

    public Path getSshDirectory() {
        return rootDir.resolve(".ssh");
    }

    public KeyPair getRsaKeyPair() {
        return rsaKeyPair;
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
}
