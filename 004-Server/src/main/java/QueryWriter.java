import com.sun.xml.internal.messaging.saaj.util.ByteOutputStream;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.Arrays;

public class QueryWriter {
    private final ByteBuffer buffer;

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

    public void write(SocketChannel channel) throws IOException {
        channel.write(buffer);
    }

    public boolean isReady() {
        return !buffer.hasRemaining();
    }
}
