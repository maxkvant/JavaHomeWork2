public class ListQuery implements Query {
    public final String path;

    public ListQuery(String path) {
        this.path = path;
    }

    @Override
    public int getId() {
        return 1;
    }
}