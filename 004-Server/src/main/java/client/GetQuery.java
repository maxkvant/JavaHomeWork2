package client;

import core.Query;
import org.jetbrains.annotations.NotNull;

/**
 * Class for query to get file by path
 */
public class GetQuery implements Query {
    public @NotNull final String path;

    GetQuery(@NotNull String path) {
        this.path = path;
    }

    @Override
    public int getId() {
        return 2;
    }
}
