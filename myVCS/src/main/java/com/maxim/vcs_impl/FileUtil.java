package com.maxim.vcs_impl;

import com.maxim.vcs_objects.*;
import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Class contains static methods to work with files
 */
public class FileUtil {
    public static final String vcs_dir = ".vcs";
    public static final String blobs_dir = "blobs";
    public static final String commits_dir = "commits";
    public static final String branches_dir = "branches";
    public static final String index_file = "index";

    /**
     * creates VcsBlob from file
     * throws RuntimeException
     */
    @NotNull
    public static VcsBlob getBlob(@NotNull Path path) {
        try {
            if (!Files.isRegularFile(path)) {
                throw new IOException("not such file");
            }
            return new VcsBlob(Files.readAllBytes(path));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * creates VcsBlobLink from file
     * throws RuntimeException
     */
    @NotNull
    public static VcsBlobLink addBlob(@NotNull Path path, @NotNull Path blobs_dir) {
        try {
            VcsBlob blob = getBlob(path);
            Path blob_path = Paths.get(blobs_dir + "", blob.md5_hash);
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

    /**
     * serializes object to path
     */
    public static void writeObject(@NotNull Object object, @NotNull Path path) throws IOException {
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

    /**
     * deserialize object from path
     */
    @NotNull
    public static Object readObject(@NotNull Path path) throws IOException  {
        try (FileInputStream fin = new FileInputStream(path.toString())) {
            ObjectInput ois = new ObjectInputStream(fin);
            return ois.readObject();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        throw new RuntimeException();
    }
}
