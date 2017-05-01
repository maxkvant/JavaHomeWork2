import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class GetAnswer implements Query {
    byte[] bytes;

    public GetAnswer(String path) throws IOException {
        bytes = Files.readAllBytes(Paths.get(path));
    }

    @Override
    public int getId() {
        return 2;
    }
}
