package server;

import core.Query;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class GetAnswer implements Query {
    public @NotNull byte[] bytes;

    public GetAnswer(@NotNull String path) throws IOException {
        bytes = Files.readAllBytes(Paths.get(path));
    }

    @Override
    public int getId() {
        return 2;
    }
}
