package io.jenkins.plugins.git.forensics.reference;

import hudson.model.Run;
import io.jenkins.plugins.forensics.reference.VCSCommit;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Action, which writes the information of the revisions into GitCommitLogs.
 *
 * @author Arne Schöntag
 */
@SuppressWarnings({"unused", "checkstyle:HiddenField"})
public class GitCommit extends VCSCommit {

    private static final long serialVersionUID = 8994811233847179343L;
    private transient Run<?, ?> run;

    private static final String NAME = "GitCommit";

    private final GitCommitLog gitCommitLog;

    public GitCommit(final Run<?, ?> run) {
        super();
        this.run = run;
        gitCommitLog = new GitCommitLog();
    }

    public void addGitCommitLogs(final List<String> revisions) {
        gitCommitLog.getRevisions().addAll(revisions);
    }

    public GitCommitLog getGitCommitLog() {
        return gitCommitLog;
    }

    public String getSummary() {
        return gitCommitLog.getRevisions().toString();
    }

    @Override
    public Optional<String> getReferencePoint(final VCSCommit reference, final int maxLogs) {
        if (reference.getClass() != GitCommit.class) {
            // Incompatible version control types.
            // Wont happen if this build and the reference build are from the same VCS repository.
            return Optional.empty();
        }
        GitCommit referenceCommit = (GitCommit) reference;
        List<String> branchCommits = new ArrayList<>(this.getGitCommitLog().getRevisions());
        List<String> masterCommits = new ArrayList<>(referenceCommit.getGitCommitLog().getRevisions());

        Optional<String> referencePoint = Optional.empty();

        // Fill branch commit list
        Run<?, ?> tmp = run;
        while (branchCommits.size() < maxLogs && tmp != null) {
            GitCommit gitCommit = tmp.getAction(GitCommit.class);
            if (gitCommit == null) {
                // Skip build if it has no GitCommit Action.
                continue;
            }
            branchCommits.addAll(gitCommit.getGitCommitLog().getRevisions());
            tmp = tmp.getPreviousBuild();
        }

        // Fill master commit list and check for intersection point
        tmp = referenceCommit.run;
        while (masterCommits.size() < maxLogs && tmp != null) {
            GitCommit gitCommit = tmp.getAction(GitCommit.class);
            if (gitCommit == null) {
                // Skip build if it has no GitCommit Action.
                continue;
            }
            masterCommits.addAll(gitCommit.getGitCommitLog().getRevisions());
            referencePoint = branchCommits.stream().filter(masterCommits::contains).findFirst();
            // If an intersection is found the buildId in Jenkins will be saved
            if (referencePoint.isPresent()) {
                return Optional.of(tmp.getExternalizableId());
            }
            tmp = tmp.getPreviousBuild();
        }

        return Optional.empty();
    }

    @Override
    public String getLatestRevision() {
        return getGitCommitLog().getRevisions().get(0);
    }

    @Override
    public List<String> getRevisions() {
        return getGitCommitLog().getRevisions();
    }

    @Override
    public void addRevisions(final List<String> list) {
        gitCommitLog.addRevisions(list);
    }

    @Override
    public void addRevision(final String rev) {
        gitCommitLog.addRevision(rev);
    }

    @Override
    public void onAttached(final Run<?, ?> run) {
        this.run = run;
    }

    @Override
    public void onLoad(final Run<?, ?> run) {
        onAttached(run);
    }

    @Override
    public String getIconFileName() {
        return null;
    }

    @Override
    public String getDisplayName() {
        return NAME;
    }

    @Override
    public String getUrlName() {
        return null;
    }
}
