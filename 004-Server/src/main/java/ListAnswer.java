import com.google.common.collect.ImmutableList;
import com.sun.imageio.stream.StreamFinalizer;

import java.io.IOException;
import java.io.Serializable;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class ListAnswer implements Query {
    public final List<Node> names;

    public ListAnswer(String path) throws IOException {
        List<Node> names = Files.list(Paths.get(path))
                .filter(Files::isReadable)
                .map(path1 -> new Node(path1.getFileName().toString(), Files.isDirectory(path1)))
                .collect(Collectors.toList());
        this.names = ImmutableList.copyOf(names);
    }

    @Override
    public int getId() {
        return 1;
    }

    public class Node implements Serializable {
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
