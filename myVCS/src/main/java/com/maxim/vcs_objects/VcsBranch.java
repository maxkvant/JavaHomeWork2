package com.maxim.vcs_objects;

import java.io.Serializable;

/**
 * Stores name of branch, and head commit id
 */
public class VcsBranch implements Serializable {
    public final String name;
    public final long commit_id;

    public VcsBranch(String name, long commit_id) {
        this.name = name;
        this.commit_id = commit_id;
    }

    public VcsBranch changeCommit(long commit_id) {
        return new VcsBranch(name, commit_id);
    }
}
