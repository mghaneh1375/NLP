import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

public class Utility {

    static void initWords() {

        JSONParser parser = new JSONParser();
        try {

            JSONArray a = (JSONArray) parser.parse(new FileReader("model.json"));

            for (int i = 0; i < a.size(); i++) {
                JSONObject word = (JSONObject) a.get(i);
                Word w = new Word((String) word.get("key"));

                JSONArray translates = (JSONArray) word.get("translates");
                for(int j = 0; j < translates.size(); j++)
                    w.addTranslate((String) translates.get(j));

                JSONArray tags = (JSONArray) word.get("tags");
                for(int j = 0; j < tags.size(); j++)
                    w.addTag((String) tags.get(j));

                Main.words.add(w);
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
                Question q = new Question();

                JSONArray tags = (JSONArray) question.get("tags");
                for(int j = 0; j < tags.size(); j++) {

                    JSONArray tmp = (JSONArray) tags.get(j);
                    List<String> list = new ArrayList<>();
                    for(int k = 0; k < tmp.size(); k++)
                        list.add(tmp.get(k).toString());

                    q.addNewModel(list);
                }

                Main.questions.add(q);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
