/**
 * interface for Lazy calculation.
 * calculation starts after first get() call.
 */
public interface Lazy<T> {
    T get();
}
