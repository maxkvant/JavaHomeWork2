package client;

import core.Query;
import org.jetbrains.annotations.NotNull;

/**
 * Class for query to get list files and folder in path
 */
public class ListQuery implements Query {
    public @NotNull final String path;

    ListQuery(@NotNull String path) {
        this.path = path;
    }

    @Override
    public int getId() {
        return 1;
    }
}