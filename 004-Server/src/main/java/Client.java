import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.*;
import java.util.List;

public class Client {
    public List<ListAnswer.Node> executeList(String path) throws Exception {
        Query query = new ListQuery(path);
        ListAnswer res = (ListAnswer) execute(query);
        return res.names;
    }

    public byte[] executeGet(String path) throws Exception {
        Query query = new GetQuery(path);
        GetAnswer res = (GetAnswer) execute(query);
        return res.bytes;
    }

    private Query execute(Query query) throws Exception {
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
