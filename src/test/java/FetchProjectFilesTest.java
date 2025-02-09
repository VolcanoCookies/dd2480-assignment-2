import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.stream.Stream;

import org.dd2480.Commit;
import org.dd2480.builder.Builder;
import org.junit.jupiter.api.AfterEach;
import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;

class FetchProjectFilesTest {
    @Test
    void canFetchProjectFiles_givenCorrectRepoInformation() {
        Builder builder = new Builder();

        String hash = "a905432a9aa118e218bc15d1dacf87e939c88f39";
        Commit commit = new Commit("group-15-dd2480", "Assignment-2", hash, null, "main");
        String path = builder.fetchProjectFiles(commit);
        String expectedPath = Paths.get("temp", hash).toAbsolutePath().toString();

        assertEquals(expectedPath, path, "Path is incorrect");
    }

    @Test
    void cannotFetchProjectFiles_givenIncorrectCommitHash() {
        Builder builder = new Builder();

        Commit commit = new Commit("group-15-dd2480", "Assignment-2", "incorrectHash", null, "main");
        String path = builder.fetchProjectFiles(commit);

        assertEquals("", path, "Path should be empty if hash is invalid");
    }

    @Test
    void cannotFetchProjectFiles_givenIncorrectBranch() {
        Builder builder = new Builder();

        Commit commit = new Commit("group-15-dd2480", "Assignment-2", "a905432a9aa118e218bc15d1dacf87e939c88f39", null,
                "incorrectBranch");
        String path = builder.fetchProjectFiles(commit);

        assertEquals("", path, "Path should be empty if branch is invalid");
    }

    @Test
    void cannotFetchProjectFiles_givenIncorrectRepoName() {
        Builder builder = new Builder();

        Commit commit = new Commit("group-15-dd2480", "incorrectRepoName", "a905432a9aa118e218bc15d1dacf87e939c88f39",
                null,
                "main");
        String path = builder.fetchProjectFiles(commit);

        assertEquals("", path, "Path should be empty if repository name is invalid");
    }

    @Test
    void cannotFetchProjectFiles_givenIncorrectRepoOwner() {
        Builder builder = new Builder();

        Commit commit = new Commit("incorrectRepoOwner", "Assignment-2", "a905432a9aa118e218bc15d1dacf87e939c88f39",
                null,
                "main");
        String path = builder.fetchProjectFiles(commit);

        assertEquals("", path, "Path should be empty if repository owner is invalid");
    }

    @AfterEach
    void cleanup() {
        // Deletes the temporary folder and its content
        try {
            deleteDirectory(Paths.get("temp"));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    void deleteDirectory(Path directory) throws IOException {
        try (Stream<Path> pathStream = Files.walk(directory)) {
            pathStream.sorted(Comparator.reverseOrder()) // We walk in reverse order so children are deleted first
                    .map(Path::toFile) // We want a file, not a path
                    .forEach(file -> {
                        if (!file.isDirectory() && !file.setWritable(true)) // Fixes eventual permission errors
                            System.err.println("Permission could not be changed for: " + file.toString());
                        if (!file.delete())
                            System.err.println("File could not be deleted: " + file.toString());
                    });
        }
    }
}
