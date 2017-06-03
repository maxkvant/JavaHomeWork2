package server;

import core.Query;
import org.jetbrains.annotations.NotNull;

/**
 * class for an answer if there was an error during query execution
 * @see Query
 */
public class ErrorAnswer implements Query {
    private final @NotNull Exception e;

    ErrorAnswer(@NotNull Exception e) {
        this.e = e;
    }

    @Override
    public int getId() {
        return 3;
    }

    /**
     * throws exception, that cause error
     */
    public void Execute() throws Exception {
        throw e;
    }
}
