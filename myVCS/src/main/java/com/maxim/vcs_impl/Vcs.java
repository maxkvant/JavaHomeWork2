package com.maxim.vcs_impl;

import com.maxim.vcs_objects.VcsCommit;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

/**
 * Interface for an implementation of version control system.
 */

public interface Vcs {
    /**
     * if current branch doesn't exists
     *   throws an exception
     * otherwise
     *   commits all tracking files,
     *   moves current branch head to new commit
     *
     * returns new commit
     */

    VcsCommit commit(String message) throws IOException;

    /**
     * if such commit exists
     *   checkouts commit with id=commit
     * otherwise
     *   throws an Exception
     */

    void checkoutCommit(long commit_id) throws IOException;

    /**
     * if such branch exists
     *   checkouts commit with name=branch_name
     * otherwise
     *   throws an Exception
     */

    void checkoutBranch(String branch_name) throws IOException;

    /**
     * puts file if absent in list tracking files
     */

    void add(Path path) throws IOException;

    /**
     * removes branch
     */

    void deleteBranch(String branch_name) throws IOException;

    /**
     * returns list, which contains all commits
     */

    List<VcsCommit> logCommits() throws IOException;

    /**
     * returns list, which contains all branches names
     */

    List<String> logBranches() throws IOException;

    /**
     * merges current branch with other_branch, deletes other_branch
     */

    void merge(String other_branch) throws IOException;

    /**
     * creates a new branch with name other_branch_name,
     * branch new branch points to current commit
     */

    void createBranch(String other_branch_name) throws IOException;

    /**
     * resets file to previous version, if file staged
     * otherwise throws an exception
     */

    void reset(Path path) throws IOException;

    /**
     * shows status: (path to file, "untracked" | "added" | "removed" | "committed" | "modified"
     */
    Map<Path, String> status(Path path) throws IOException;

    /**
     * removes file from path, also removes file from working copy
     */
    void rm(Path path) throws IOException;

    /**
     * removes all untracked files
     */

    void clean(Path path) throws IOException;

    /**
     * returns name of current branch, if it exists, otherwise "null"
     */
    String getCurrentBranchName();

    /**
     * returns id of current commit,
     * note: id of initial commit is null
     */
    long getCurrentCommitId();
}