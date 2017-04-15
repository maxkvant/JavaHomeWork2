package com.maxim.vcs_impl;

import com.maxim.vcs_objects.VcsCommit;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.function.Consumer;
import java.util.function.Function;

import static junit.framework.TestCase.assertFalse;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;


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

    @Test
    public void mergeTest() throws IOException {
        final Vcs vcs = new VcsImpl(Paths.get(temporaryFolder.getRoot().getPath()));
        List<Path> paths = initFolder();
        Path path = paths.get(0);
        class InitBranch implements Function<String, VcsCommit> {
            final int value;
            final Vcs vcs;

            public InitBranch(int value, Vcs vcs) {
                this.value = value;
                this.vcs = vcs;
            }

            @Override
            public VcsCommit apply(String name) {
                try {
                    vcs.checkoutCommit(VcsCommit.nullCommit.id);
                    vcs.createBranch(name);
                    FileUtil.writeObject(value, path);
                    vcs.add(path);
                    return vcs.commit("commit");
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }
        VcsCommit commit1 = new InitBranch(10, vcs).apply("dev");
        VcsCommit commit2 = new InitBranch(10, vcs).apply("test");
        VcsCommit commit = vcs.merge("dev");
        assertThat(commit.parents_ids, containsInAnyOrder(commit1.id, commit2.id));
        assertEquals("test", vcs.getCurrentBranchName());
        try {
            new InitBranch(11, vcs).apply("dev2");
            vcs.merge("test");
            fail();
        } catch (Exception e) {
            assertEquals(IOException.class, e.getClass());
        }
    }

    @Test
    public void resetTest() throws IOException {
        final Vcs vcs = new VcsImpl(Paths.get(temporaryFolder.getRoot().getPath()));
        List<Path> paths = initFolder();
        Path path = paths.get(0);
        vcs.createBranch("master");
        FileUtil.writeObject("hello", path);
        vcs.add(path);
        vcs.commit("");
        FileUtil.writeObject("hello!", path);
        vcs.reset(path);
        assertThat(FileUtil.readObject(path), equalTo("hello"));
    }

    @Test
    public void statusTest() throws IOException {
        final Vcs vcs = new VcsImpl(Paths.get(temporaryFolder.getRoot().getPath()));
        List<Path> paths = initFolder();
        vcs.createBranch("master");

        vcs.add(paths.get(0));
        vcs.add(paths.get(1));
        assertThat(vcs.status().values(), containsInAnyOrder("added", "added", "untracked", "untracked"));

        vcs.commit("");
        assertThat(vcs.status().values(), containsInAnyOrder("committed", "committed", "untracked", "untracked"));

        vcs.rm(paths.get(0));
        assertThat(vcs.status().values(), containsInAnyOrder("removed", "committed", "untracked", "untracked"));

        FileUtil.writeObject("hey", paths.get(1));
        assertThat(vcs.status().values(), containsInAnyOrder("removed", "modified", "untracked", "untracked"));
    }

    @Test
    public void rmTest() throws IOException {
        final Vcs vcs = new VcsImpl(Paths.get(temporaryFolder.getRoot().getPath()));
        List<Path> paths = initFolder();
        vcs.rm(paths.get(3));
        assertFalse(Files.exists(paths.get(3)));

        vcs.createBranch("master");
        vcs.add(paths.get(0));
        VcsCommit commit = vcs.commit("");
        vcs.rm(paths.get(0));
        vcs.add(paths.get(1));
        vcs.commit("");
        assertFalse(Files.exists(paths.get(0)));

        vcs.checkoutCommit(commit.id);
        assertTrue(Files.exists(paths.get(0)));
        assertFalse(Files.exists(paths.get(1)));
    }

    @Test
    public void cleanTest() throws IOException {
        final Vcs vcs = new VcsImpl(Paths.get(temporaryFolder.getRoot().getPath()));
        List<Path> paths = initFolder();

        vcs.createBranch("master");
        vcs.add(paths.get(0));
        vcs.add(paths.get(2));
        vcs.commit("");
        vcs.clean();

        assertThat(vcs.status().values(), containsInAnyOrder("committed", "committed"));
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