package core;

import org.jetbrains.annotations.NotNull;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

/**
 * class for writing query to channel
 */
public class QueryWriter {
    private final @NotNull ByteBuffer buffer;

    /**
     * Creates writer of query
     */
    public QueryWriter(Query query) throws IOException {
        ByteArrayOutputStream bosObject = new ByteArrayOutputStream();
        ByteArrayOutputStream bosData = new ByteArrayOutputStream();

        try (ObjectOutputStream objectOutputStream = new ObjectOutputStream(bosObject);
             DataOutputStream res = new DataOutputStream(bosData)) {
            objectOutputStream.writeObject(query);
            objectOutputStream.flush();

            res.writeInt(query.getId());
            res.writeInt(bosObject.toByteArray().length);
            res.write(bosObject.toByteArray());
            res.flush();
            buffer = ByteBuffer.wrap(bosData.toByteArray());
        }
    }

    /**
     * writes to channel part of query
     */
    public void write(@NotNull SocketChannel channel) throws IOException {
        channel.write(buffer);
    }

    /**
     * checks is query written
     */
    public boolean isReady() {
        return !buffer.hasRemaining();
    }
}
