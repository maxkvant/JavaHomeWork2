package com.maxim.vcs_objects;

import org.apache.commons.codec.digest.DigestUtils;

public class VcsBlob extends VcsObject {
    public final byte[] bytes;
    public final String md5_hash;

    public VcsBlob(byte[] bytes) {
        this.bytes = bytes;
        md5_hash = DigestUtils.md5Hex(bytes);
    }
}
