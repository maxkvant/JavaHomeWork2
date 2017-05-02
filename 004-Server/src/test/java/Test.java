import client.Client;
import org.junit.Rule;
import org.junit.rules.TemporaryFolder;
import server.ListAnswer;
import server.Server;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public class Test {
    @Rule
    public TemporaryFolder folder = new TemporaryFolder();
    private static Server server;
    static {
        Server server1 = null;
        try {
            server1 = new Server();
        } catch (IOException e) {
            e.printStackTrace();
        }

        server = server1;

        new Thread(() -> {
            try {
                server.start();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    @org.junit.Test
    public void listAnswerTest() throws Exception {
        initFolder();

        List<ListAnswer.Node> res = new ListAnswer(folder.getRoot().toString()).names;

        List<String> names = res.stream().map(node -> node.name).collect(Collectors.toList());
        List<Boolean> isDirs = res.stream().map(node -> node.isDirectory).collect(Collectors.toList());

        assertThat(names, containsInAnyOrder("B", "A", "abracadabra", "abacaba", "cat"));
        assertThat(isDirs, containsInAnyOrder(true, false, false, false, true));
    }

    @org.junit.Test
    public void executeListTest() throws Exception {
        Client client = new Client();
        initFolder();

        List<ListAnswer.Node> res = client.executeList(folder.getRoot().toString());

        List<String> names = res.stream().map(node -> node.name).collect(Collectors.toList());
        List<Boolean> isDirs = res.stream().map(node -> node.isDirectory).collect(Collectors.toList());

        assertThat(names, containsInAnyOrder("B", "A", "abracadabra", "abacaba", "cat"));
        assertThat(isDirs, containsInAnyOrder(true, false, false, false, true));
    }

    @org.junit.Test
    public void executeGetTest() throws Exception {
        Client client = new Client();
        List<Path> paths = initFolder();

        int i = 0;
        for (int size : Arrays.asList(10, 1000, 100000)) {
            byte[] bytes1 = new byte[size];
            new Random().nextBytes(bytes1);
            Path path = paths.get(i);
            Files.write(path, bytes1);
            byte[] res = client.executeGet(path.toString());
            byte[] res1 = client.executeGet(path.toString());
            assertThat(res, is(equalTo(bytes1)));
            assertThat(res1, is(equalTo(bytes1)));
            i++;
        }
    }

    @org.junit.Test(expected=java.nio.file.NoSuchFileException.class)
    public void executeGetTest2() throws Exception {
        Client client = new Client();
        List<Path> paths = initFolder();
        client.executeGet(paths.get(0).toString() + "[]");
    }

    @org.junit.Test()
    public void mulitiClientTest() throws Exception {
        List<Client> clients = Arrays.asList(new Client(), new Client(), new Client());
        List<Path> paths = initFolder();

        int size = 10000;
        final byte[] bytes1 = new byte[size];
        Path path = paths.get(0);
        new Random().nextBytes(bytes1);
        Files.write(path, bytes1);
        List<Thread> threads = new ArrayList<>();
        for (final Client client : clients) {
            Thread thread = new Thread(() -> {
                try {
                    byte[] res = client.executeGet(path.toString());
                    List<String> names = client.executeList(folder.getRoot().toString())
                            .stream()
                            .map(node -> node.name).collect(Collectors.toList());
                    assertThat(names, containsInAnyOrder("B", "A", "abracadabra", "abacaba", "cat"));
                    assertThat(res, is(equalTo(bytes1)));
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            });
            threads.add(thread);
            thread.start();
        }
        for (Thread thread : threads) {
            thread.join();
        }
    }

    private List<Path> initFolder() throws IOException {
        List<File> files = new ArrayList<>();
        folder.newFolder("A");
        files.add(folder.newFile("B"));
        files.add(folder.newFile("abracadabra"));
        files.add(folder.newFile("abacaba"));
        folder.newFolder("cat");

        return files.stream()
                .map(file -> Paths.get(file.getPath()))
                .collect(Collectors.toList());
    }
}