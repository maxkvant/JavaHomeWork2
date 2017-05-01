package client;

import core.Query;
import org.jetbrains.annotations.NotNull;

public class ListQuery implements Query {
    public @NotNull final String path;

    public ListQuery(@NotNull String path) {
        this.path = path;
    }

    @Override
    public int getId() {
        return 1;
    }
}