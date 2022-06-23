// package io.github.joeljeremy7.externalizedproperties.resolvers.git;

// import io.github.joeljeremy7.externalizedproperties.core.resolvers.ResourceResolver.PropertiesReader;
// import io.github.joeljeremy7.externalizedproperties.core.resolvers.ResourceResolver.ResourceReader;
// import org.junit.jupiter.api.DisplayName;
// import org.junit.jupiter.api.Nested;
// import org.junit.jupiter.api.Test;

// import java.nio.file.Paths;

// public class GitResolverTests {
//     @Nested
//     class BuilderTests {
//         @Test
//         @DisplayName("should throw ...")
//         void test1() {
//             GitResolver gitResolver = gitResolver();
//         }
//     }

//     private GitResolver gitResolver() {
//         return gitResolver(
//             "resolvers/git/src/main/resources/app.properties", 
//             new PropertiesReader()
//         );
//     }

//     private GitResolver gitResolver(String resourceFile, ResourceReader resourceReader) {
//         return GitResolver.builder()
//             .gitRepositoryUri("https://github.com/joeljeremy7/externalized-properties.git")
//             // Change to main before MR to main.
//             .gitBranch("git-resolver")
//             .gitCloneDirectory(Paths.get("").resolve("git-resolver-clone-dir"))
//             .resourceFilePath(resourceFile)
//             .resourceReader(new PropertiesReader())
//             .build();
//     }
// }
