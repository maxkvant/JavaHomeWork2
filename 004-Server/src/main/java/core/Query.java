package core;

import java.io.Serializable;

/**
 * interface for query (client and server can send Query to each other)
 */
public interface Query extends Serializable {
    /**
     * returns id of type query, must return the same value
     */
    int getId();
}
