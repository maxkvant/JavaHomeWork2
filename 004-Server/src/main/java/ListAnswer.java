import com.sun.imageio.stream.StreamFinalizer;

import java.io.IOException;
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

public class ListAnswer {
    public final List<Node> names;

    public ListAnswer(ListQuery query) throws IOException {
        names = Files.list(Paths.get(query.path))
                .filter(Files::isReadable)
                .map(path -> new Node(path.getFileName().toString(), Files.isDirectory(path)))
                .collect(Collectors.toList());
    }

    public ListAnswer(ByteBuffer buffer, SocketChannel channel) throws IOException {
        int size = Util.readInt(buffer, channel);
        Node[] res = new Node[size];
        for (int i = 0; i < size; i++) {
            boolean isDirectory = Util.readInt(buffer, channel) == 1;
            String name = Util.readString(buffer, channel);
            res[i] = new Node(name, isDirectory);
        }
        names = Arrays.asList(res);
    }

    public class Node {
        public final String name;
        public final boolean isDirectory;

        public Node(String name, boolean isDirectory) {
            this.name = name;
            this.isDirectory = isDirectory;
        }
    }

    public void send(SocketChannel channel) throws IOException {
        Util.writeInt(names.size(), channel);
        for (Node node : names) {
            Util.writeString(node.name, channel);
            Util.writeInt(node.isDirectory ? 1 : 0, channel);
        }
    }
}
