import org.junit.Rule;
import org.junit.rules.TemporaryFolder;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;

import static org.junit.Assert.*;

public class Test {
    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    @org.junit.Test
    public void test() throws Exception {
        new Thread(() -> {
            try {
                new Server().start();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
        Thread.currentThread().sleep(500);


        new Client().start();
        Thread.currentThread().sleep(3000);
    }

    @org.junit.Test
    public void listAnswerTest() throws Exception {
        folder.newFolder("A");
        folder.newFile("B");
        folder.newFile("abracadabra");
        folder.newFile("abcaba");
        folder.newFolder("cat");

        List<ListAnswer.Node> res = new ListAnswer(new ListQuery(folder.getRoot().toString())).names;

        List<String> names = res.stream().map(node -> node.name).collect(Collectors.toList());
        List<Boolean> isDirs = res.stream().map(node -> node.isDirectory).collect(Collectors.toList());

        assertThat(names, containsInAnyOrder("A", "B", "abracadabra", "abacaba", "cat"));
        assertThat(isDirs, containsInAnyOrder(true, false, false, false, false, true));
    }
}