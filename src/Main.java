import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;
import com.mongodb.ConnectionString;
import org.bson.Document;

import java.util.ArrayList;
import java.util.List;

import static com.mongodb.client.model.Filters.eq;

public class Main {

    public static List<Verb> verbs = new ArrayList<>();
    public static List<Word> words = new ArrayList<>();
    public static List<Question> questions = new ArrayList<>();
    final static public ConnectionString connString = new ConnectionString(
            "mongodb://localhost:27017/koochita"
    );

    public static MongoDatabase mongoDatabase;

    public static PlaceRepository placeRepository;


    public static void main(String[] args) {

        MongoClientSettings settings = MongoClientSettings.builder()
                .applyConnectionString(connString)
                .retryWrites(true)
                .build();

        com.mongodb.client.MongoClient mongoClient = MongoClients.create(settings);
        mongoDatabase = mongoClient.getDatabase("koochita");

        placeRepository = new PlaceRepository();

        Utility.initWords();
        Utility.initVerbs();
        Utility.initQuestions();

//        String question = "طرز تهیه آش شیله عدس را بیان کنید";
//        String question = "آش شیله عدس چطور درست میشه؟";
        String question = "آش شیله عدس چقدر کالری دارد؟";
//        String question = "نوشیدنی پیشنهاد بده";
//        String question = "در اصفهان چی بخوریم";
//        String question = "در شهر اصفهان چی بخوریم";
//        String question = "در اصفهان کجا بریم";
//        String question = "در شهر اصفهان کجا بریم";
//        String question = "در یزد کجا بریم";
//        String question = "جاذبه های دیدنی اصفهان را نام ببرید";
//        String question = "جاذبه های استان اصفهان را نام ببرید";
//        String question = "جاذبه های دیدنی استان اصفهان را نام ببرید";
//        String question = "جاذبه های دیدنی شهر اصفهان را نام ببرید";
//        String question = "هتل های خوب استان یزد را لطفا معرفی کنید.";
//        String question = "غذاهای سنتی شهر اصفهان رو لیست کن";
//        String question = "در یزد کچا بمانیم";
//        String question = "در تهران کچا بخوابیم";

        ArrayList<String> tokens = Utility.normalize(question);
        for(String token : tokens)
            System.out.println(token);

        Question best = null;
        double max = -1;

        for(Question q : questions) {

            double res = q.match(tokens);

            if(res > max) {
                best = q;
                max = res;
            }

        }

        System.out.println("max is " + max);

        if(best != null) {
            System.out.println(best.name);
            best.doFilter(tokens);
        }
    }

}
