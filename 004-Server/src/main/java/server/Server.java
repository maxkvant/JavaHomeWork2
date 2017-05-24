package server;

import client.GetQuery;
import client.ListQuery;
import core.Query;
import core.QueryReader;
import core.QueryWriter;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.*;
import java.util.Iterator;
import java.util.Random;

public class Server {
    private Thread loopThread;
    private ServerSocketChannel channel;

    public final static int port = new Random()
            .ints(1 << 10, 1 << 15)
            .findAny()
            .orElse(1 << 11);

    private boolean running = false;

    public Server() throws IOException {
    }

    public synchronized void start() throws Exception {
        if (!running) {
            Selector selector;
            running = true;
            channel = ServerSocketChannel.open();
            channel.bind(new InetSocketAddress(port));
            selector = Selector.open();
            channel.configureBlocking(false);
            channel.register(selector, SelectionKey.OP_ACCEPT);

            loopThread = new Thread(() -> {
                try {
                    loop(selector);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
            loopThread.start();
        }
    }

    private void loop(Selector selector) throws IOException {
        while (running) {
            selector.select();
            Iterator<SelectionKey> iterator = selector.selectedKeys().iterator();
            while (iterator.hasNext()) {
                SelectionKey selectionKey = iterator.next();
                iterator.remove();
                if (!selectionKey.isValid()) {
                    continue;
                }
                if (selectionKey.isAcceptable()) {
                    SocketChannel channel1 = channel.accept();
                    channel1.configureBlocking(false);
                    channel1.register(selector, SelectionKey.OP_READ, new QueryReader());
                } else if (selectionKey.isReadable()) {
                    final SocketChannel channel1 = (SocketChannel) selectionKey.channel();
                    QueryReader reader = (QueryReader) selectionKey.attachment();
                    reader.read(channel1);
                    if (reader.isReady()) {
                        Query answer;
                        try {
                            Query res = reader.getObject();
                            if (res instanceof ListQuery) {
                                answer = new ListAnswer(((ListQuery) res).path);
                            } else if (res instanceof GetQuery) {
                                answer = new GetAnswer(((GetQuery) res).path);
                            } else {
                                throw new IllegalArgumentException("wrong query id");
                            }
                        } catch (Exception e) {
                            answer = new ErrorAnswer(e);
                        }
                        channel1.register(selector, SelectionKey.OP_WRITE, new QueryWriter(answer));
                    }
                }
                if (selectionKey.isWritable()) {
                    final SocketChannel channel1 = (SocketChannel) selectionKey.channel();
                    QueryWriter writer = (QueryWriter) selectionKey.attachment();
                    if (!writer.isReady()) {
                        writer.write(channel1);
                    }
                }
            }
        }
    }

    public synchronized void stop() throws IOException {
        running = false;
        channel.close();
        loopThread.interrupt();
    }
}
