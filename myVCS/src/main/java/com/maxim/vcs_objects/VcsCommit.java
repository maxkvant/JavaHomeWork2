package com.maxim.vcs_objects;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import org.jetbrains.annotations.NotNull;

import java.io.Serializable;
import java.util.*;

/**
 * Represent commit:
 * stores id, message, date, parent commits id, links to committed files
 */

public class VcsCommit implements Serializable {
    public final long id;
    public final @NotNull String message;
    public final long date;
    public final @NotNull List<Long> parents_ids;
    public final @NotNull Map<String, VcsBlobLink> files;
    public final static VcsCommit nullCommit = new VcsCommit();

    public VcsCommit(@NotNull String message, @NotNull List<Long> parents_ids, @NotNull Map<String, VcsBlobLink> files) {
        id = Math.abs(new Random().nextLong());
        this.message = message;
        this.parents_ids = ImmutableList.copyOf(parents_ids);
        this.files = ImmutableMap.copyOf(files);
        this.date = new Date().getTime();
    }

    @NotNull
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

    @Override
    public boolean equals(Object other) {
        return !(other == null || !(other instanceof VcsCommit)) && id == ((VcsCommit) other).id;
    }
}
