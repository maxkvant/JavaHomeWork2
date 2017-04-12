package com.maxim.vcs_impl;

import com.maxim.vcs_objects.VcsCommit;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

public interface Vcs {
    VcsCommit commit(String message) throws IOException;
    void checkoutCommit(long commit_id) throws IOException;
    void checkoutBranch(String branch_name) throws IOException;
    void add(Path path) throws IOException;

    void deleteBranch(String branch_name) throws IOException;

    List<VcsCommit> logCommits() throws IOException;
    List<String> logBranches() throws IOException;
    void merge(String other_branch) throws IOException;
    void createBranch(String other_branch_name) throws IOException;

    void reset(Path path) throws IOException;
    Map<String, String> status(Path path) throws IOException;
    void rm(Path path) throws IOException;
    void clean(Path path) throws IOException;
}