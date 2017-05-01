public class GetQuery implements Query {
    public final String path;

    public GetQuery(String path) {
        this.path = path;
    }

    @Override
    public int getId() {
        return 2;
    }
}
