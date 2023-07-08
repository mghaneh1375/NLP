import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;
import com.mongodb.ConnectionString;

import java.util.ArrayList;
import java.util.List;

public class Main {

    public static List<Word> words = new ArrayList<>();
    public static List<Question> questions = new ArrayList<>();
    final static private ConnectionString connString = new ConnectionString(
            "mongodb://localhost:27017/gachesefid"
    );

    public static MongoDatabase mongoDatabase;


    public static void main(String[] args) {

        MongoClientSettings settings = MongoClientSettings.builder()
                .applyConnectionString(connString)
                .retryWrites(true)
                .build();

        com.mongodb.client.MongoClient mongoClient = MongoClients.create(settings);
        mongoDatabase = mongoClient.getDatabase("koochita");

        Utility.initWords();
        Utility.initQuestions();

//        String question = "در اصفهان چی بخوریم";
        String question = "در اصفهان کجا بریم";
        String[] splited = question.split("\\s+");

        for(Question q : questions)
            q.match(splited);

    }

}
