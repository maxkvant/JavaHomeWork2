package com.maxim.vcs_impl;

import com.google.common.collect.ImmutableSet;
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
import java.util.function.Function;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.equalTo;


public class VcsImplTest {
    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    @Test
    public void test() throws Exception {
        Vcs vcs = new VcsImpl(Paths.get(temporaryFolder.getRoot().getPath()));

        Set<String> set1 = new TreeSet<>(
                Arrays.asList("./.idea/uiDesigner.xml",
                        "./.gradle/3.1/taskArtifacts/fileHashes.bin")
        );

        Set<String> set2 = new TreeSet<>(
                Arrays.asList("./.idea/uiDesigner.xml",
                        "./.gradle/3.1/taskArtifacts/fileSnapshots.bin")
        );

        vcs.createBranch("1");
        for (int i = 0; i < 2; i++) {
            for (String path : set1) {
                vcs.add(Paths.get(path));
            }
            VcsCommit commit = vcs.commit("2");
        }

        vcs.checkoutCommit(0);
        vcs.createBranch("2");
        for (int i = 0; i < 2; i++) {
            for (String path : set2) {
                vcs.add(Paths.get(path));
            }
            VcsCommit commit = vcs.commit("2");
        }

        vcs.merge("1");
        vcs.deleteBranch("2");
    }

    @Test
    public void addTest() throws Exception {
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

        assertThat(getAdded.call().size(), equalTo(1));
        assertThat(getAdded.call(), containsInAnyOrder( paths.get(2)));
        assertThat(getAdded.call().size(), equalTo(1));
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