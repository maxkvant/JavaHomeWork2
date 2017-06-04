package core;

import org.jetbrains.annotations.NotNull;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

/**
 * class for reading Query in server
 */
public class QueryReader {
    private static final int bufferSize = 48;
    private int id;
    private int currentSize = 0;
    private boolean firstRead = true;
    private byte[] bytes;
    private @NotNull ByteBuffer buffer = ByteBuffer.allocate(bufferSize);

    /**
     * Reads from channel part of query
     */
    public void read(@NotNull SocketChannel channel) throws IOException {
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

    /**
     * checks is query read
     */
    public boolean isReady() {
        return !firstRead && currentSize == bytes.length;
    }

    public int getId() throws IllegalAccessException {
        if (!isReady()) {
            throw new IllegalAccessException();
        }
        return id;
    }

    /**
     * if query is read, returns query
     * otherwise throws IllegalAccessException
     */
    public Query getObject() throws IOException, IllegalAccessException, ClassNotFoundException {
        if (!isReady()) {
            throw new IllegalAccessException("query is not ready");
        }
        try (ObjectInputStream objectInputStream = new ObjectInputStream(new ByteArrayInputStream(bytes))) {
            return (Query) objectInputStream.readObject();
        }
    }
}
