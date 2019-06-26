package io.jenkins.plugins.git.forensics.miner;

import java.io.IOException;
import java.util.Collections;

import org.junit.ClassRule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;

import hudson.model.FreeStyleBuild;
import hudson.model.FreeStyleProject;
import hudson.plugins.git.GitSCM;

import io.jenkins.plugins.git.forensics.util.GitITest;

import static org.assertj.core.api.Assertions.*;

/**
 * Tests the class {@link GitCheckoutListener}.
 */
public class GitCheckoutListenerITest extends GitITest {
    /** Jenkins rule per suite. */
    @ClassRule
    public static final JenkinsRule JENKINS_PER_SUITE = new JenkinsRule();

    /**
     * Verifies that the statistics about a repository are correctly evaluated.
     */
    @Test
    public void shouldInitiateForensicsAnalyzer() {
        initializeGit();
        FreeStyleProject job = createJob();

        FreeStyleBuild build = scheduleSuccessfulBuild(job);

        String consoleLog = getConsoleLog(build);
        System.out.println(consoleLog);
        assertThat(consoleLog).contains(
                "[Git Forensics] Analyzed history of",
                "[Git Forensics] File with most commits",
                "[Git Forensics] File with most number of authors",
                "[Git Forensics] Least recently modified file");
    }

    private void initializeGit() {
        for (int round = 0; round < 4; round++) {
            String path = "";
            for (int j = 1; j < 5 - round; j++) {
                for (int i = 1; i < 5 - round; i++) {
                    String fileName = String.format("%sfile%d.txt", path, i);
                    writeFile(fileName, "Some text in round " +  round);
                    git("add", fileName);
                }
                path += j + "/";
            }
            git("commit", "--message=Additional");

        }
        git("rev-parse", "HEAD");
    }

    private String getConsoleLog(final FreeStyleBuild build) {
        try {
            return JenkinsRule.getLog(build);
        }
        catch (IOException exception) {
            throw new AssertionError(exception);
        }
    }

    @SuppressWarnings("IllegalCatch")
    private FreeStyleBuild scheduleSuccessfulBuild(final FreeStyleProject job) {
        try {
            return JENKINS_PER_SUITE.buildAndAssertSuccess(job);
        }
        catch (Exception exception) {
            throw new AssertionError(exception);
        }
    }

    private FreeStyleProject createJob() {
        try {
            FreeStyleProject project = createFreeStyleProject();
            GitSCM scm = new GitSCM(
                    GitSCM.createRepoList("file:///" + sampleRepo.getRoot(), null),
                    Collections.emptyList(), false, Collections.emptyList(),
                    null, null, Collections.emptyList());
            project.setScm(scm);
            return project;
        }
        catch (IOException exception) {
            throw new AssertionError(exception);
        }
    }

    private  FreeStyleProject createFreeStyleProject() {
        try {
            return JENKINS_PER_SUITE.createProject(FreeStyleProject.class);
        }
        catch (IOException e) {
            throw new AssertionError(e);
        }
    }

}
