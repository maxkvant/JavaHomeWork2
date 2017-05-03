package server;

import com.google.common.collect.ImmutableList;
import core.Query;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

public class ListAnswer implements Query {
    public @NotNull final List<Node> names;

    public ListAnswer(@NotNull String path) throws IOException {
        Path pathCur = Paths.get(path);


        if (Files.exists(pathCur) && Files.isDirectory(pathCur)) {
            List<Node> names = Files.list(pathCur)
                    .filter(Files::isReadable)
                    .map(path1 -> new Node(path1.getFileName().toString(), Files.isDirectory(path1)))
                    .collect(Collectors.toList());
            this.names = ImmutableList.copyOf(names);
        } else {
            names = ImmutableList.of();
        }

    }

    @Override
    public int getId() {
        return 1;
    }

    public static class Node implements Serializable {
        public final String name;
        public final boolean isDirectory;

        public Node(String name, boolean isDirectory) {
            this.name = name;
            this.isDirectory = isDirectory;
        }

        @Override
        public String toString() {
            return "<" + name + (isDirectory ? ", directory" : "")  + ">";
        }
    }
}
