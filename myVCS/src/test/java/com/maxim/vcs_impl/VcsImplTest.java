package com.maxim.vcs_impl;

import com.google.common.collect.ImmutableSet;
import com.maxim.vcs_objects.VcsCommit;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;


public class VcsImplTest {
    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    @Test
    public void addTest1() throws Exception {
        final Vcs vcs = new VcsImpl(Paths.get(temporaryFolder.getRoot().getPath()));
        List<Path> paths = initFolder();
        vcs.createBranch("branch");

        Callable<List<Path>> getAdded = () -> {
                Map < Path, String > cur_status = vcs.status();
                List<Path> added = new ArrayList<>();
                    cur_status.forEach((path, state) -> {
                    if (state.equals("added")) {
                        added.add(path);
                    }
                });
                return added;
        };

        vcs.add(paths.get(0));
        vcs.add(paths.get(1));
        assertThat(getAdded.call(), containsInAnyOrder(paths.get(0), paths.get(1)));
        vcs.commit("message");
        assertThat(getAdded.call().size(), equalTo(0));
        vcs.add(paths.get(0));
        vcs.add(paths.get(2));
        assertThat(getAdded.call(), containsInAnyOrder( paths.get(2)));
    }

    @Test(expected = IOException.class)
    public void addTest2() throws Exception {
        final Vcs vcs = new VcsImpl(Paths.get(temporaryFolder.getRoot().getPath()));
        List<Path> paths = initFolder();
        vcs.add(Paths.get(paths.get(0) + "", "abrcadabra"));
    }

    @Test
    public void logTest() throws IOException {
        final Vcs vcs = new VcsImpl(Paths.get(temporaryFolder.getRoot().getPath()));
        List<Path> paths = initFolder();

        assertThat(vcs.getCurrentBranchName(), equalTo("null"));
        assertThat(vcs.logBranches().size(), equalTo(0));

        vcs.createBranch("master");
        vcs.createBranch("develop");

        assertThat(vcs.logBranches(), containsInAnyOrder("master", "develop"));
        assertThat(vcs.getCurrentBranchName(), equalTo("develop"));

        assertThat(vcs.logCommits(), containsInAnyOrder(VcsCommit.nullCommit));

        List<Long> commits = new ArrayList<>();
        commits.add(VcsCommit.nullCommit.id);

        for (int i = 0; i < 10; i++) {
            commits.add(vcs.commit("message").id);
        }

        assertThat(vcs.logCommits().size(), equalTo(commits.size()));

        vcs.merge("master");
        assertThat(vcs.logBranches(), containsInAnyOrder("develop"));
    }

    @Test
    public void commitTest1() throws IOException {
        final Vcs vcs = new VcsImpl(Paths.get(temporaryFolder.getRoot().getPath()));
        List<Path> paths = initFolder();
        vcs.createBranch("master");
        Path path1 = paths.get(0);
        Date date1 = new Date();
        vcs.add(path1);
        VcsCommit commit1 = vcs.commit("Hello, world");
        assertThat(commit1.message, equalTo("Hello, world"));
        assertThat(commit1.parents_ids, containsInAnyOrder(0L));
        assertThat(commit1.files.keySet(), containsInAnyOrder(path1.toString()));
        assertTrue(commit1.date > date1.getTime());
    }

    @Test(expected=IOException.class)
    public void commitTest2() throws IOException {
        final Vcs vcs = new VcsImpl(Paths.get(temporaryFolder.getRoot().getPath()));
        vcs.commit("message");
    }

    @Test
    public void checkoutTest() throws IOException {
        final Vcs vcs = new VcsImpl(Paths.get(temporaryFolder.getRoot().getPath()));
        List<Path> paths = initFolder();
        Path path = paths.get(0);
        vcs.createBranch("master");
        List<VcsCommit> commits = new ArrayList<>();
        final int n = 10;
        for (long i = 0; i < n; i++) {
            FileUtil.writeObject(i, path);
            vcs.add(path);
            commits.add(vcs.commit("Hi"));
            if (i == 0) {
                vcs.createBranch("dev");
            }
        }
        for (int i = 0; i < n; i++) {
            vcs.checkoutCommit(commits.get(i).id);
            Long value = (Long) FileUtil.readObject(path);
            assertEquals(Long.valueOf(i), value);
        }
        vcs.checkoutBranch("master");
        assertEquals(0L,  FileUtil.readObject(path));
    }

    private List<Path> initFolder() throws IOException {
        List<Path> res = new ArrayList<>();
        res.add(Paths.get(temporaryFolder.getRoot().getPath(), "1"));
        FileUtil.writeObject(1, res.get(0));

        File folder = temporaryFolder.newFolder("folder");
        for (int i = 2; i <= 4; i++) {
            res.add(Paths.get(folder.getPath(), i + ""));
            FileUtil.writeObject(i, res.get(res.size() - 1));
        }
        return res;
    }
}