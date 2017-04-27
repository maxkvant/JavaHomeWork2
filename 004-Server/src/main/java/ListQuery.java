import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.*;

public class ListQuery {
    public final static int id = 1;
    public final String path;

    public ListQuery(String path) {
        this.path = path;
    }

    public ListQuery(ByteBuffer buf, SocketChannel channel) throws IOException {
        path = Util.readString(buf, channel);
    }

    public void send(SocketChannel channel) throws IOException {
        Util.writeInt(id, channel);
        Util.writeString(path, channel);
    }
}
