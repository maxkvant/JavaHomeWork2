import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

public class QueryReader {
    public static final int bufferSize = 48;
    private int id;
    private int currentSize = 0;
    private boolean firstRead = true;
    private byte[] bytes;
    private ByteBuffer buffer = ByteBuffer.allocate(bufferSize);

    public void read(SocketChannel channel) throws IOException {
        buffer.clear();
        channel.read(buffer);
        buffer.flip();

        if (firstRead) {
            assert buffer.remaining() >= 8;
            id = buffer.getInt();
            int n = buffer.getInt();
            bytes = new byte[n];
            firstRead = false;
        }
        while (currentSize < bytes.length && buffer.remaining() > 0) {
            bytes[currentSize++] = buffer.get();
        }
        buffer.clear();
    }

    public boolean isReady() {
        return !firstRead && currentSize == bytes.length;
    }

    public int getId() throws IllegalAccessException {
        if (!isReady()) {
            throw new IllegalAccessException();
        }
        return id;
    }

    public Query getObject() throws IOException, IllegalAccessException, ClassNotFoundException {
        if (!isReady()) {
            throw new IllegalAccessException();
        }
        try (ObjectInputStream objectInputStream = new ObjectInputStream(new ByteArrayInputStream(bytes))) {
            return (Query) objectInputStream.readObject();
        }
    }
}
