package com.maxim.vcs_objects;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

import java.io.Serializable;
import java.util.*;

/**
 * Represent commit:
 * stores id, message, date, parent commits id, links to committed files
 */

public class VcsCommit implements Serializable {
    public final long id;
    public final String message;
    public final long date;
    public final List<Long> parents_ids;
    public final Map<String, VcsBlobLink> files;
    public final static VcsCommit nullCommit = new VcsCommit();

    public VcsCommit(String message, List<Long> parents_ids, Map<String, VcsBlobLink> files) {
        id = Math.abs(new Random().nextLong());
        this.message = message;
        this.parents_ids = ImmutableList.copyOf(parents_ids);
        this.files = ImmutableMap.copyOf(files);
        this.date = new Date().getTime();
    }

    @Override
    public String toString() {
        return String.valueOf(id) + "\n" +
                "  message: " + message + "\n" +
                "  date:" + new Date(date) + "\n" +
                "  parents: " + parents_ids + "\n" +
                "\n";
    }

    private VcsCommit() {
        id = 0;
        this.message = "initial";
        this.parents_ids = ImmutableList.of();
        this.files = ImmutableMap.of();
        this.date = 0;
    }
}
