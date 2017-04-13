package com.maxim.vcs_objects;

import java.io.Serializable;

/**
 * Stores md5 hash of file
 */
public class VcsBlobLink implements Comparable, Serializable {
    public final String md5_hash;

    public VcsBlobLink(String md5_hash) {
        this.md5_hash = md5_hash;
    }

    @Override
    public boolean equals(Object other) {
        return !(other == null || !(other instanceof VcsBlobLink)) && md5_hash.equals(((VcsBlobLink) other).md5_hash);
    }

    @Override
    public int compareTo(Object other) {
        if (other == null || !(other instanceof VcsBlobLink)) {
            return -1;
        }
        return md5_hash.compareTo(((VcsBlobLink) other).md5_hash);
    }
}
