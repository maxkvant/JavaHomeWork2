package com.maxim.vcs_objects;

import com.maxim.vcs_impl.Vcs;

import java.io.Serializable;
import java.util.Random;

public class VcsObject implements Serializable {
    public final long id;

    public VcsObject() {
         id = Math.abs(new Random().nextLong());
    }

    protected VcsObject(long id) {
        this.id = id;
    }
}
