package com.maxim.vcs_objects;

import org.apache.commons.codec.digest.DigestUtils;

import java.io.Serializable;

/**
 * stores file, it's hash
 */
public class VcsBlob implements Serializable {
    public final byte[] bytes;
    public final String md5_hash;

    public VcsBlob(byte[] bytes) {
        this.bytes = bytes;
        md5_hash = DigestUtils.md5Hex(bytes);
    }
}
