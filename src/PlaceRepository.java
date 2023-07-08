import DB.Common;

public class PlaceRepository extends Common {

    public PlaceRepository() {
        init();
    }

    @Override
    public void init() {
        documentMongoCollection = Main.mongoDatabase.getCollection("place");
    }
}
