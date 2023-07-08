package DB;


import org.bson.Document;

import java.util.ArrayList;
import java.util.HashMap;


public class Repository {

    static HashMap<String, ArrayList<Cache>> generalCached = new HashMap<>();

    public static void clearCache(String table) {
        if(generalCached.containsKey(table))
            generalCached.put(table, new ArrayList<>());
    }

    static Document isInCache(String section, Object id) {

        if(!generalCached.containsKey(section))
            return null;

        ArrayList<Cache> cached = generalCached.get(section);

        for (int i = 0; i < cached.size(); i++) {

            if (cached.get(i).equals(id)) {

                if (cached.get(i).checkExpiration())
                    return (Document) cached.get(i).getValue();

                cached.remove(i);
                return null;
            }

        }

        return null;
    }

    static void removeFromCache(String section, Object id) {

        if(!generalCached.containsKey(section))
            return;

        ArrayList<Cache> cached = generalCached.get(section);

        for (int i = 0; i < cached.size(); i++) {

            if (cached.get(i).equals(id)) {
                cached.remove(i);
                return;
            }

        }
    }

    static void addToCache(String section, Document doc, Object secKey, int limit, int expirationSec) {

        ArrayList<Cache> cached;

        if(!generalCached.containsKey(section))
            cached = new ArrayList<>();
        else
            cached = generalCached.get(section);

        if(cached.size() >= limit)
            cached.remove(0);

        cached.add(new Cache(expirationSec, doc, doc.getObjectId("_id"), secKey));
        generalCached.put(section, cached);
    }

    static void addToCache(String section, Document doc, int limit, int expirationSec) {

        ArrayList<Cache> cached;

        if(!generalCached.containsKey(section))
            cached = new ArrayList<>();
        else
            cached = generalCached.get(section);

        if(cached.size() >= limit)
            cached.remove(0);

        cached.add(new Cache(expirationSec, doc, doc.getObjectId("_id")));
        generalCached.put(section, cached);
    }

}

