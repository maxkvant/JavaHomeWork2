import org.jetbrains.annotations.NotNull;

public class ErrorAnswer implements Query {
    private final @NotNull Exception e;

    public ErrorAnswer(@NotNull Exception e) {
        this.e = e;
    }

    @Override
    public int getId() {
        return 3;
    }

    public void Execute() throws Exception {
        throw e;
    }
}
