package com.maxim.vcs_impl;

import com.maxim.vcs_objects.VcsCommit;
import org.junit.Test;

import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Set;
import java.util.TreeSet;


public class VcsImplTest {
    @Test
    public void test() throws Exception {
        Vcs vcs = new VcsImpl();

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
}