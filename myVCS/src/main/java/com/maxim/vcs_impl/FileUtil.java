package com.maxim.vcs_impl;

import com.maxim.vcs_objects.*;
import com.sun.xml.internal.ws.policy.privateutil.PolicyUtils;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class FileUtil {
    private static final String vcs_dir = ".vcs";
    private static final String blobs_dir = "blobs";
    private static final String commits_dir = "commits";
    private static final String branches_dir = "branches";
    private static final String index_file = "index";

    public static final Path vcs_dir_path = Paths.get(".", vcs_dir);
    public static final Path blobs_dir_path = Paths.get(".", vcs_dir, blobs_dir);
    public static final Path commits_dir_path = Paths.get(".", vcs_dir, commits_dir);
    public static final Path branches_dir_path = Paths.get(".", vcs_dir, branches_dir);
    public static final Path index_file_path = Paths.get(".", vcs_dir, index_file);

    public static VcsBlobLink addBlob(Path path) {
        try {
            if (!Files.isRegularFile(path)) {
                throw new IOException("not file");
            }
            VcsBlob blob = new VcsBlob(Files.readAllBytes(path));
            Path blob_path = Paths.get(blobs_dir_path + "", blob.md5_hash);
            if (!Files.exists(blob_path)) {
                Files.createFile(blob_path);
                Files.write(blob_path, blob.bytes);
            }
            return new VcsBlobLink(blob.md5_hash);
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    public static void writeObject(Object object, Path path) throws IOException {
        try (ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
            ObjectOutput out = new ObjectOutputStream(bos);
            out.writeObject(object);
            if (!Files.exists(path)) {
                Files.createFile(path);
            } else {
                Files.delete(path);
                Files.createFile(path);
            }
            Files.write(path, bos.toByteArray());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static Object readObject(Path path) throws IOException  {
        try (FileInputStream fin = new FileInputStream(path.toString())) {
            ObjectInput ois = new ObjectInputStream(fin);
            return ois.readObject();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }
}
