import com.mongodb.BasicDBObject;
import org.bson.Document;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static com.mongodb.client.model.Filters.eq;

public class Utility {

    private final static ArrayList<String> redundants = new ArrayList<>() {
        {add("را");}
        {add("رو");}
        {add("در");}
        {add("از");}
        {add("می");}
        {add("خوب");}
        {add("های");}
        {add("لطفا");}
    };

    static String convertPersianDigits(String number) {

        char[] chars = new char[number.length()];
        for (int i = 0; i < number.length(); i++) {

            char ch = number.charAt(i);

            if (ch >= 0x0660 && ch <= 0x0669)
                ch -= 0x0660 - '0';
            else if (ch >= 0x06f0 && ch <= 0x06F9)
                ch -= 0x06f0 - '0';

            chars[i] = ch;
        }

        return new String(chars);
    }

    private static ArrayList<String> pronouns = new ArrayList<>() {
        {
            add("م");
            add("ی");
            add("د");
            add("یم");
            add("ید");
            add("ند");
        }
    };

    static String isInVerbs(String token) {

        for(Verb verb : Main.verbs) {

            if(verb.verb.equals(token)) {
                if(verb.needRemoveLast)
                   return token.substring(0, token.length() - 2);

                return token;
            }

            if(verb.infinitive && token.startsWith("ب" + verb.verb) && token.length() - verb.verb.length() < 4)
                return verb.verb;

            if(verb.infinitive && token.startsWith(verb.verb) && token.length() - verb.verb.length() < 3)
                return verb.verb;

        }

        return token;
    }

    static ArrayList<String> normalize(String str) {

        str = convertPersianDigits(str);

        str = str
                .replace("?", "")
                .replace("'", "")
                .replace("\"", "")
                .replace("&", "")
                .replace("*", "")
                .replace("(", "")
                .replace(")", "")
        ;

        str = str.replace("داشته باش", "باش");
        str = str.replace("طرز پخت", "طرزپخت")
                .replace("نحوه تهیه", "نحوهتیه")
                .replace("طرز تهیه", "طرزتهیه")
                .replace("دستور پخت", "دستورپخت")
                .replace("مواد اولیه", "مواداولیه")
                .replace("مواد لازم", "موادلازم");

        String[] splitedTmp = str.split("\\s+");

        int idx = -1;
        ArrayList<String> splited = new ArrayList<>();

        for(String in : splitedTmp) {

            if(in.endsWith("های"))
                in = in.replace("های", "");

            splited.add(isInVerbs(in));
        }

        ArrayList<String> output = new ArrayList<>();
        boolean needCheck = true;

        for(String in : splited) {

            idx++;

            if(!needCheck || redundants.contains(in)) {
                needCheck = true;
                continue;
            }

            if(idx < splited.size() - 1) {

                if(Verbs.isSpecialVerb(splited.get(idx), splited.get(idx + 1))) {
                    output.add(splited.get(idx) + splited.get(idx + 1));
                    needCheck = false;
                    continue;
                }
            }
            output.add(in);

        }

        return output;
    }

    static void initWords() {

        JSONParser parser = new JSONParser();
        try {

            JSONArray a = (JSONArray) parser.parse(new FileReader("model.json"));

            for (int i = 0; i < a.size(); i++) {
                JSONObject word = (JSONObject) a.get(i);
                Word w = new Word((String) word.get("key"));

                JSONArray translates = (JSONArray) word.get("translates");
                if(translates.size() == 1 && translates.get(0).toString().startsWith("%") &&
                        translates.get(0).toString().endsWith("%")
                ) {

                    String[] splited = translates.get(0).toString().replace("%", "").split("__");

                    List<Document> docs = Main.placeRepository.find(eq("place_mode", splited[0].toLowerCase(Locale.ROOT)), new BasicDBObject(splited[1].toLowerCase(Locale.ROOT), 1));
                    for(Document doc : docs) {
                        w.addTranslate(doc.getString(splited[1].toLowerCase(Locale.ROOT)));
                    }
                }
                else{
                    for (int j = 0; j < translates.size(); j++)
                        w.addTranslate((String) translates.get(j));
                }

                JSONArray tags = (JSONArray) word.get("tags");
                for(int j = 0; j < tags.size(); j++)
                    w.addTag((String) tags.get(j));

                Main.words.add(w);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    static void initVerbs() {

        JSONParser parser = new JSONParser();
        try {

            JSONArray a = (JSONArray) parser.parse(new FileReader("verbs.json"));

            for (int i = 0; i < a.size(); i++) {
                JSONObject verb = (JSONObject) a.get(i);
                Verb v = new Verb(
                        (String) verb.get("verb"),
                        (Boolean) verb.get("needRemoveLast"),
                        (Boolean) verb.get("infinitive")
                );
                Main.verbs.add(v);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    static void initQuestions() {

        JSONParser parser = new JSONParser();
        try {

            JSONArray a = (JSONArray) parser.parse(new FileReader("questions.json"));

            for (int i = 0; i < a.size(); i++) {

                JSONObject question = (JSONObject) a.get(i);
                Question q = new Question(
                        question.get("repository").toString().equals("place") ? Main.placeRepository : null,
                        (String) question.get("name")
                );

                JSONArray filters = (JSONArray) question.get("filters");
                for(int j = 0; j < filters.size(); j++) {
                    JSONObject jsonObject = (JSONObject) filters.get(j);
                    q.addNewFilter(eq(jsonObject.get("key").toString(), jsonObject.get("value").toString()));
                }

                JSONArray tags = (JSONArray) question.get("tags");
                for(int j = 0; j < tags.size(); j++) {

                    JSONArray tmp = (JSONArray) tags.get(j);
                    List<String> list = new ArrayList<>();
                    for(int k = 0; k < tmp.size(); k++)
                        list.add(tmp.get(k).toString());

                    q.addNewModel(list);
                }

                if(question.containsKey("projections")) {

                    BasicDBObject basicDBObject = new BasicDBObject();
                    JSONArray projections = (JSONArray) question.get("projections");

                    for(int j = 0; j < projections.size(); j++)
                        basicDBObject.append(projections.get(j).toString(), 1);

                    q.setProjection(basicDBObject);
                }

                Main.questions.add(q);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
