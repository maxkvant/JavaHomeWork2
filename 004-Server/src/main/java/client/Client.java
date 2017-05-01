package client;

import core.Query;
import core.QueryReader;
import core.QueryWriter;
import org.jetbrains.annotations.NotNull;
import server.ErrorAnswer;
import server.GetAnswer;
import server.ListAnswer;
import server.Server;

import java.net.InetSocketAddress;
import java.nio.channels.*;
import java.util.List;

public class Client {
    public @NotNull List<ListAnswer.Node> executeList(@NotNull String path) throws Exception {
        Query query = new ListQuery(path);
        ListAnswer res = (ListAnswer) execute(query);
        return res.names;
    }

    public @NotNull byte[] executeGet(@NotNull String path) throws Exception {
        Query query = new GetQuery(path);
        GetAnswer res = (GetAnswer) execute(query);
        return res.bytes;
    }

    private @NotNull Query execute(@NotNull Query query) throws Exception {
        SocketChannel channel = SocketChannel.open();
        channel.connect(new InetSocketAddress(Server.port));

        QueryWriter writer = new QueryWriter(query);
        while (!writer.isReady()) {
            writer.write(channel);
        }
        QueryReader reader = new QueryReader();
        while (!reader.isReady()) {
            reader.read(channel);
        }

        channel.finishConnect();
        Query res = reader.getObject();
        if (res instanceof ErrorAnswer) {
            ((ErrorAnswer) res).Execute();
        }
        return res;
    }
}
