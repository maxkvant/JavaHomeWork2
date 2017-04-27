import java.io.DataInputStream;
import java.io.IOException;
import java.io.PushbackInputStream;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Iterator;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Supplier;

public class Server {
    private final ServerSocketChannel channel = ServerSocketChannel.open();
    public final static int port = new Random()
            .ints(1 << 10, 1 << 15)
            .findAny()
            .orElse(1 << 11);

    public Server() throws IOException {
    }

    public void start() throws Exception {
        channel.bind(new InetSocketAddress(port));
        Selector selector = Selector.open();
        channel.configureBlocking(false);
        channel.register(selector, SelectionKey.OP_ACCEPT);

        System.out.println("port: " + port);

        ExecutorService threadPool = Executors.newFixedThreadPool(7);

        for (int k = 0; k < 10; k++) {
            selector.select();

            Iterator<SelectionKey> iterator = selector.selectedKeys().iterator();
            while (iterator.hasNext()) {
                SelectionKey selectionKey = iterator.next();
                iterator.remove();
                if (!selectionKey.isValid()) {
                    continue;
                }
                System.out.println(selectionKey);
                if (selectionKey.isAcceptable()) {
                    SocketChannel channel1 = channel.accept();

                    channel1.configureBlocking(false);
                    ByteBuffer buffer1 = ByteBuffer.allocate(48);
                    channel1.register(selector, SelectionKey.OP_READ, buffer1);
                } else if (selectionKey.isReadable()) {
                    System.out.println("readable");
                    final SocketChannel channel1 = (SocketChannel) selectionKey.channel();
                    final ByteBuffer buf = (ByteBuffer) selectionKey.attachment();
                    try {
                        int type = Util.readInt(buf, channel1);
                        System.out.println("type = " + type);
                        if (type == 1) {
                            String s = Util.readString(buf, channel1);
                            System.out.println("s = " + s);
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } if (selectionKey.isWritable()) {

                }
            }
        }
    }
}
