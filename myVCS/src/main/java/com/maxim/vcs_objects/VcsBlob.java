package com.maxim.vcs_objects;

import org.apache.commons.codec.digest.DigestUtils;
import org.jetbrains.annotations.NotNull;

import java.io.Serializable;

/**
 * stores file, it's hash
 */
public class VcsBlob implements Serializable {
    public final @NotNull byte[] bytes;
    public final @NotNull String md5_hash;

    public VcsBlob(@NotNull byte[] bytes) {
        this.bytes = bytes;
        md5_hash = DigestUtils.md5Hex(bytes);
    }
}
