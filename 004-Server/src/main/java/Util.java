import java.io.DataInputStream;
import java.io.IOException;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.channels.Channel;
import java.nio.channels.SocketChannel;
import java.util.Arrays;

public class Util {
    public static String readString(ByteBuffer buffer, SocketChannel channel) throws IOException {
        int len = readInt(buffer, channel);
        System.out.println("len = " + len);
        int currentLen = 0;


        ByteBuffer res = ByteBuffer.allocate(len);

        buffer.flip();
        while (currentLen < len) {
            if (buffer.limit() - buffer.remaining() == 0) {
                buffer.clear();
                channel.read(buffer);
            }
            res.put(buffer.get());
            currentLen++;
        }
        buffer.compact();

        return new String(res.array());
    }

    public static void writeString(String s, SocketChannel channel) throws IOException {
        writeInt(s.length(), channel);
        ByteBuffer buffer = ByteBuffer.wrap(s.getBytes());
        while (buffer.hasRemaining()) {
            channel.write(buffer);
        }
        System.out.println("send " + new String(buffer.array()));
    }

    public static int readInt(ByteBuffer buffer, SocketChannel channel) throws IOException {
        while (buffer.limit() - buffer.remaining() < 4) {
            channel.read(buffer);
        }
        buffer.flip();
        int res = buffer.getInt();
        buffer.compact();
        System.out.println("read " + res);
        return res;
    }

    public static void writeInt(int n, SocketChannel channel) throws IOException {
        System.out.println("n = " + n);
        ByteBuffer buffer1 = ByteBuffer.allocate(4);
        buffer1.putInt(n);
        buffer1.flip();
        while (buffer1.remaining() > 0) {
            channel.write(buffer1);
        }
        System.out.println("send " + Arrays.toString(buffer1.array()));
    }
}
