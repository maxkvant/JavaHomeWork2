package client;

import core.Query;
import org.jetbrains.annotations.NotNull;

public class GetQuery implements Query {
    public @NotNull final String path;

    public GetQuery(@NotNull String path) {
        this.path = path;
    }

    @Override
    public int getId() {
        return 2;
    }
}
