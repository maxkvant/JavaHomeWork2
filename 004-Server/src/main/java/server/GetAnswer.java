package server;

import core.Query;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class GetAnswer implements Query {
    public @NotNull final byte[] bytes;

    public GetAnswer(@NotNull String path) throws IOException {
        Path path1 = Paths.get(path);
        if (Files.exists(path1) && Files.isRegularFile(path1) && Files.isReadable(path1)) {
            bytes = Files.readAllBytes(Paths.get(path));
        } else {
            bytes = new byte[0];
        }
    }

    @Override
    public int getId() {
        return 2;
    }
}
