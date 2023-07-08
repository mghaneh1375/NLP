package DB;

import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;

import java.util.ArrayList;


import static com.mongodb.client.model.Filters.*;

public abstract class Common extends Repository {

    public MongoCollection<Document> documentMongoCollection = null;
    String table = "";
    String secKey = "";

    public boolean exist(Bson filter) {
        return documentMongoCollection.countDocuments(filter) > 0;
    }

    public ArrayList<Document> find(Bson filter, Bson project) {

        FindIterable<Document> cursor;

        if(project == null) {
            if(filter == null)
                cursor = documentMongoCollection.find();
            else
                cursor = documentMongoCollection.find(filter);
        }
        else {
            if(filter == null)
                cursor = documentMongoCollection.find().projection(project);
            else
                cursor = documentMongoCollection.find(filter).projection(project);
        }

        ArrayList<Document> result = new ArrayList<>();

        for(Document doc : cursor)
            result.add(doc);

        return result;
    }

    public int count(Bson filter) {
        if(filter == null)
            return (int) documentMongoCollection.countDocuments();

        return (int) documentMongoCollection.countDocuments(filter);
    }

    public ArrayList<Document> find(Bson filter, Bson project, Bson orderBy) {

        FindIterable<Document> cursor;

        if(project == null) {
            if(filter == null)
                cursor = documentMongoCollection.find().sort(orderBy);
            else
                cursor = documentMongoCollection.find(filter).sort(orderBy);
        }
        else {
            if(filter == null)
                cursor = documentMongoCollection.find().projection(project).sort(orderBy);
            else
                cursor = documentMongoCollection.find(filter).projection(project).sort(orderBy);
        }

        ArrayList<Document> result = new ArrayList<>();

        for(Document doc : cursor)
            result.add(doc);

        return result;
    }

    public Document findOne(Bson filter, Bson project) {

        FindIterable<Document> cursor;
        if(project == null)
            cursor = documentMongoCollection.find(filter);
        else
            cursor = documentMongoCollection.find(filter).projection(project);

        if(cursor.iterator().hasNext())
            return cursor.iterator().next();

        return null;
    }

    public Document findOne(Bson filter, Bson project, Bson orderBy) {

        FindIterable<Document> cursor;
        cursor = documentMongoCollection.find(filter).projection(project);

        if(cursor.iterator().hasNext())
            return cursor.iterator().next();

        return null;
    }


    public synchronized Document findById(ObjectId id) {

        Document cached = isInCache(table, id);
        if(cached != null)
            return cached;

        FindIterable<Document> cursor = documentMongoCollection.find(eq("_id", id));
        if(cursor.iterator().hasNext()) {
            Document doc = cursor.iterator().next();

            if(!table.isEmpty()) {
                if(secKey.isEmpty() || !doc.containsKey(secKey))
                    addToCache(table, doc, 200, 60 * 60 * 24 * 7);
                else
                    addToCache(table, doc, doc.get(secKey), 200, 60 * 60 * 24 * 7);
            }

            return doc;
        }

        return null;
    }

    public abstract void init();

}
