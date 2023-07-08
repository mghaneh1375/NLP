import java.util.ArrayList;
import java.util.List;

public class Word {

    List<String> translates;
    List<String> tags;
    String key;

    public Word(String key) {
        this.key = key;
        translates = new ArrayList<>();
        tags = new ArrayList<>();
    }

    public Word(List<String> translates, List<String> tags, String key) {
        this.translates = translates;
        this.tags = tags;
        this.key = key;
    }

    public void addTranslate(String t) {
        translates.add(t);
    }

    public void addTag(String t) {
        tags.add(t);
    }

}
