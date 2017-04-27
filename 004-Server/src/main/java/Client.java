import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.util.Arrays;
import java.util.Iterator;

public class Client {
    public void start() throws IOException, InterruptedException {
        SocketChannel channel = SocketChannel.open();
        channel.connect(new InetSocketAddress(Server.port));

        ListQuery query = new ListQuery("hello");

        Thread.currentThread().sleep(300);

        System.out.println("before send\n");

        query.send(channel);

        System.out.println("send\n");

        //ByteBuffer buffer = ByteBuffer.allocate(4);

        //while (channel.read(buffer) != -1) {
        //    System.out.print(new String(buffer.array()).substring(0, buffer.limit()));
        //    buffer.clear();
        //}
    }
}
