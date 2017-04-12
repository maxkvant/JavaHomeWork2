package com.maxim.vcs_impl;

import com.google.common.collect.*;
import com.maxim.vcs_objects.VcsBlobLink;
import com.maxim.vcs_objects.VcsBranch;
import com.maxim.vcs_objects.VcsCommit;
import com.sun.xml.internal.ws.policy.privateutil.PolicyUtils;

import java.io.IOException;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

import static com.maxim.vcs_impl.FileUtil.*;
import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

public class VcsImpl implements Vcs {
    private Index index = new Index(VcsCommit.nullCommit.id);

    public VcsImpl() throws IOException {
        Files.createDirectories(commits_dir_path);
        Files.createDirectories(blobs_dir_path);
        Files.createDirectories(branches_dir_path);

        writeCommit(VcsCommit.nullCommit);

        if (!Files.exists(index_file_path)) {
            writeObject(index, index_file_path);
        }
    }

    @Override
    public VcsCommit commit(String message) throws IOException {
        readIndex();

        if (index.branch == null) {
            throw new IOException("Please, checkout branch or create branch");
        }

        Map<String, VcsBlobLink> oldFiles = getCommittedFiles();
        List<Long> parents_ids = Collections.singletonList(index.commit_id);

        Map<String, VcsBlobLink> currentFiles = new TreeMap<>(loadAdded());
        oldFiles.forEach(currentFiles::putIfAbsent);

        VcsCommit new_commit = new VcsCommit(message, parents_ids, currentFiles);
        writeCommit(new_commit);

        index.branch = index.branch.changeCommit(new_commit.id);
        index = new Index(index.branch);

        writeBranch(index.branch);
        writeIndex();
        return new_commit;
    }

    @Override
    public void checkoutCommit(long commit_id) throws IOException {
        readIndex();

        Map<String, VcsBlobLink> committedFiles = getCommittedFiles();
        for (String file_path : committedFiles.keySet()) {
            try {
                Files.delete(Paths.get(file_path));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        VcsCommit commit = readCommit(commit_id);
        for (Map.Entry<String, VcsBlobLink> entry : commit.files.entrySet()) {
            Path path = Paths.get(entry.getKey());
            Files.createDirectories(path.getParent());
            if (!Files.exists(path)) {
                Files.createFile(path);
            }
            String blob_name = entry.getValue().md5_hash;
            Path blob_path = Paths.get(blobs_dir_path.toString(), blob_name);
            Files.copy(blob_path, path, REPLACE_EXISTING);
        }

        index = new Index(commit_id);

        writeIndex();
    }

    @Override
    public void checkoutBranch(String branch_name) throws IOException {
        readIndex();

        VcsBranch branch = readBranch(branch_name);
        checkoutCommit(branch.commit_id);
        writeBranch(branch);
        index = new Index(branch);

        writeIndex();
    }

    @Override
    public void add(Path path) throws IOException {
        readIndex();
        index.added.add(path.toString());
        writeIndex();
    }

    @Override
    public void merge(String other_branch_name) throws IOException {
        //TODO fix repeating code form commit(...)

        readIndex();

        if (index.branch == null) {
            throw new IOException("Please, checkout branch or create branch");
        }

        if (index.branch.name.equals(other_branch_name)) {
            return;
        }

        VcsBranch other_branch = readBranch(other_branch_name);
        VcsCommit other_commit = readCommit(other_branch.commit_id);
        Map<String, VcsBlobLink> cur_files = getCommittedFiles();

        MapDifference<String, VcsBlobLink> map_difference = Maps.difference(cur_files, other_commit.files);
        if (map_difference.entriesDiffering().size() > 0) {
            throw new IOException("files are differ:" + map_difference.entriesDiffering());
        }

        Map<String, VcsBlobLink> res_files = new TreeMap<>();
        cur_files.forEach(res_files::putIfAbsent);
        other_commit.files.forEach(res_files::putIfAbsent);

        List<Long> parents_ids = ImmutableList.of(index.commit_id, other_commit.id);
        VcsCommit new_commit = new VcsCommit("merged " + other_branch_name, parents_ids, res_files);
        writeCommit(new_commit);

        VcsBranch branch = index.branch.changeCommit(new_commit.id);
        writeBranch(branch);

        index = new Index(branch);

        deleteBranch(other_branch_name);

        writeIndex();
    }

    @Override
    public void createBranch(String branch_name) throws IOException {
        readIndex();

        Path path = Paths.get(branches_dir_path + "", branch_name);
        System.out.println(path);
        if (Files.exists(path)) {
            throw new IOException("branch exits");
        }
        VcsBranch branch = new VcsBranch(branch_name, index.commit_id);
        writeBranch(branch);
        checkoutBranch(branch_name);

        writeIndex();
    }

    @Override
    public void deleteBranch(String branch_name) throws IOException {
        readIndex();

        Path branch_path = Paths.get(branches_dir_path + "", branch_name);
        if (Files.exists(branch_path)) {
            Files.delete(branch_path);
        }

        writeIndex();
    }

    @Override
    public List<VcsCommit> logCommits() throws IOException {
        try {
            return Files.walk(commits_dir_path)
                    .filter(Files::isRegularFile)
                    .map(path -> {
                        try {
                            return (VcsCommit) readObject(path);
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    })
                    .sorted((commit1, commit2) -> {
                            if (commit1.date < commit2.date) {
                                return 0;
                            }
                            return commit1.date < commit2.date ? 1 : -1;
                        })
                    .collect(Collectors.toList());
        } catch (RuntimeException e) {
            throw (IOException) e.getCause();
        }
    }

    @Override
    public List<String> logBranches() throws IOException {
        return Files.walk(branches_dir_path)
                .filter(Files::isRegularFile)
                .map(Path::getFileName)
                .map(Path::toString)
                .collect(Collectors.toList());
    }

    @Override
    public void clean(Path path) throws IOException {
        Set<String> tracking = getTrackingNames();
        Files.walk(path)
                .filter(Files::isRegularFile)
                .filter(entry -> !tracking.contains(entry.toString()))
                .forEach(entry -> {
                    try {
                        Files.delete(entry);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                });
    }


    @Override
    public void reset(Path path) throws IOException {
        Map<String, VcsBlobLink> committed = getCommittedFiles();
        if (!committed.containsKey(path.toString())) {
            throw new IOException("no such committed file");
        }
        String blob_name = committed.get(path.toString()).md5_hash;
        Path blob_path = Paths.get(blobs_dir_path.toString(), blob_name);
        Files.copy(blob_path, path);
    }

    @Override
    public Map<String, String> status(Path path) throws IOException {
        Map<String, VcsBlobLink> committedFiles = getCommittedFiles();
        Set<String> committedNames = committedFiles.keySet();
        ImmutableMap.Builder<String, String> mapBuilder = ImmutableMap.builder();
        Files.walk(path)
                .filter(Files::isRegularFile)
                .forEach(entry -> {
                    String state = "untracked";
                    if (index.added.contains(entry.toString())) {
                        state = "added";
                    } else if (index.removed.contains(entry.toString())) {
                        state = "removed";
                    } else if (committedNames.contains(entry.toString())) {
                        String fileHash = committedFiles.get(entry.toString()).md5_hash;
                        if (getBlob(entry).md5_hash.equals(fileHash)) {
                            state = "committed";
                        } else {
                            state = "modified";
                        }
                    }
                    mapBuilder.put(entry.toString(), state);
                });
        return mapBuilder.build();
    }

    @Override
    public void rm(Path path) throws IOException {
        readIndex();

        if (!getTrackingNames().contains(path.toString())) {
            throw new IOException("no such tracking file");
        }
        Files.delete(path);
        if (index.added.contains(path.toString())) {
            index.added.remove(path.toString());
        } else {
            index.removed.add(path.toString());
        }

        writeIndex();
    }

    private void writeIndex() throws IOException {
        if (index.branch != null) {
            index.commit_id = index.branch.commit_id;
        }
        writeObject(index, index_file_path);
    }

    private void readIndex() throws IOException {
        index = (Index) readObject(index_file_path);
    }

    private void writeCommit(VcsCommit commit) throws IOException {
        Path path = Paths.get(commits_dir_path + "", commit.id + "");
        writeObject(commit, path);
    }

    private VcsCommit readCommit(long commit_id) throws IOException {
        Path path = Paths.get(commits_dir_path + "", commit_id + "");
        return (VcsCommit) FileUtil.readObject(path);
    }

    private Map<String, VcsBlobLink> loadAdded() throws IOException {
        Path path = Paths.get(".");
        try {
            return Files.walk(path)
                    .filter(Files::isRegularFile)
                    .filter(entry -> index.added.contains(entry.toString()))
                    .collect(Collectors.toMap(
                            Path::toString,
                            FileUtil::addBlob));
        } catch (RuntimeException e) {
            throw (IOException) e.getCause();
        }
    }

    private void writeBranch(VcsBranch branch) throws IOException {
        Path branch_path = Paths.get(branches_dir_path.toString(), branch.name);
        writeObject(branch, branch_path);
    }

    private VcsBranch readBranch(String branch_name) throws IOException {
        Path branch_path = Paths.get(branches_dir_path.toString(), branch_name);
        return (VcsBranch) readObject(branch_path);
    }

    private Set<String> getTrackingNames() throws IOException {
        Set<String> added = index.added;
        Set<String> committed = getCommittedFiles().keySet();
        return new ImmutableSet.Builder<String>().addAll(added).addAll(committed).build();
    }

    private Map<String, VcsBlobLink> getCommittedFiles() throws IOException {
        Map<String, VcsBlobLink> committed = readCommit(index.commit_id).files;
        return Maps.filterKeys(committed, path -> !index.removed.contains(path));
    }

    private static class Index implements Serializable {
        private Set<String> added = new TreeSet<>();
        private Set<String> removed = new TreeSet<>();
        private VcsBranch branch;
        private long commit_id = VcsCommit.nullCommit.id;

        Index(long commit_id) {
            this.commit_id = commit_id;
            this.branch = null;
        }

        Index(VcsBranch branch) {
            this.branch = branch;
            this.commit_id = branch.commit_id;
        }
    }

}
