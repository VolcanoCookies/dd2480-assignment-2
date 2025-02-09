package org.dd2480.builder;

import java.io.*;
import java.nio.file.Paths;

import org.dd2480.Commit;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.slf4j.Logger;

import java.io.File;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class Builder {
    private static final Logger log = org.slf4j.LoggerFactory.getLogger(Builder.class);

    /**
     * Build a project from the repository on a specific commit.
     * @param commit A commit object
     *
     */
    public void buildProject(Commit commit) {
        Instant startTime = Instant.now();

        // First fetch project files
        String projectPath = fetchProjectFiles(commit);
        if (projectPath == null) {
            System.out.println("Failed to fetch the project files for commit: " + commit.hash);
            BuildResult result = new BuildResult(commit, BuildStatus.ERROR, List.of("Failed to fetch project files"), startTime, Instant.now());
            saveResult(result);
            return;
        }

        // Run maven test and package
        List<String> testOutput = new ArrayList<>();
        List<String> packageOutput = new ArrayList<>();

        boolean testsSuccessful = runCommand("mvn test", projectPath, testOutput);
        boolean buildSuccessful = runCommand("mvn package", projectPath, packageOutput);

        boolean success = testsSuccessful && buildSuccessful;

        // Determine final build status
        org.dd2480.builder.BuildStatus status;
        if (success)
            status = BuildStatus.SUCCESS;
        else
            status = BuildStatus.FAILURE;
        List<String> allLogs = new ArrayList<>();
        allLogs.addAll(testOutput);
        allLogs.addAll(packageOutput);

        Instant endTime = Instant.now();
        // Save the result
        BuildResult result = new BuildResult(commit, status, allLogs, startTime, endTime);
        saveResult(result);
    }

    public boolean runCommand(String command, String workingDir, List<String> output) {
        try {
            ProcessBuilder pb = new ProcessBuilder(command.split(" "));
            pb.directory(new File(workingDir));
            pb.redirectErrorStream(true);

            Process process = pb.start();
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));

            String line;
            while ((line = reader.readLine()) != null) {
                output.add(line);
            }

            int exitCode = process.waitFor();
            return exitCode == 0;
        } catch (Exception e) {
            output.add("Error executing command: " + command);
            output.add(e.getMessage());
            return false;
        }
    }

    /**
     * Fetch project files from a remote repository for a specific branch and
     * commit. Files are put in a temporary folder and named after the commit hash.
     *
     * @param commit A commit object
     * @return The absolute path for the project files, or an empty string if
     *         something went wrong
     */
    public String fetchProjectFiles(Commit commit) {

        String repoUrl = "https://github.com/" + commit.repositoryOwner + "/" + commit.repositoryName + ".git";

        // Absolute path to project file location
        String filePath = Paths.get("temp", commit.hash).toAbsolutePath().toString();

        /*
         * Clones the directory and switches to correct branch and commit.
         * 
         * Is a try-with-resource block that automatically runs git.close() whenever the
         * block finishes, even if an exception happens. This is important, otherwise
         * it might cause issues when deleting the repo files.
         */
        try (Git git = Git
                .cloneRepository()
                .setURI(repoUrl)
                .setDirectory(new File(filePath))
                .call()) {
            // Checkout specific branch and commit
            git.checkout().setName(commit.branch).call();
            git.checkout().setName(commit.hash).call();
        } catch (Exception e) { // Incorrect commit info or other issue
            return "";
        }

        return filePath;
    }

    /**
     * Save the build result so that it can be accessed at a later date.
     * @param result the BuildResult object from buildProject
     *
     */
    public void saveResult(BuildResult result) {
        try {
            FileOutputStream stream = new FileOutputStream(result.commitHash + ".dat");
            ObjectOutputStream out = new ObjectOutputStream(stream);
            out.writeObject(result);
            out.flush();
            out.close();
        } catch (FileNotFoundException e) {
            log.warn("Failed create file for build result: " + e.getMessage());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    /**
     * Get the build result for a specific commit.
     * @param commitHash a commit hash
     *
     * @return the result of the build
     */
    public BuildResult getResult(String commitHash) {
        try {
            FileInputStream stream = new FileInputStream(commitHash + ".dat");
            ObjectInputStream in = new ObjectInputStream(stream);
            BuildResult result = (BuildResult) in.readObject();
            in.close();
            return result;
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            return null;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}
