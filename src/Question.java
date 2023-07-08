import java.util.ArrayList;
import java.util.List;

public class Question {

    List<List<String>> tags;
    int minRequiredMatch = 80;

    public Question() {
        tags = new ArrayList<>();
    }

    public void addNewModel(List<String> in) {
        tags.add(in);
    }

    private List<String> inputKeys;

    private int localMatch(int idx) {

        List<String> lessSentence = tags.get(idx).size() < inputKeys.size() ? tags.get(idx) : inputKeys;
        List<String> largeSentence = tags.get(idx).size() < inputKeys.size() ? inputKeys : tags.get(idx);

        int currIdx = 0;
        int matches = 0;

        for(int i = 0; i < lessSentence.size(); i++) {

            boolean find = false;

            for(int j = currIdx; j < largeSentence.size(); j++) {

                if(largeSentence.get(j).equals(lessSentence.get(i))) {
                    currIdx = j + 1;
                    find = true;
                    break;
                }

            }

            if(find)
                matches++;
        }

        return matches;
    }

    private void findKeys(String[] in) {

        inputKeys = new ArrayList<>();

        for(String itr : in) {

            String key = "unknown";

            for (Word word : Main.words) {

                if (word.translates.contains(itr)) {
                    key = word.key;
                    break;
                }

            }

            inputKeys.add(key);
        }

    }

    public int match(String[] in) {

        findKeys(in);

        int max = -1;
        int maxIdx = 0;

        for(int i = 0; i < tags.size(); i++) {
            int tmp = localMatch(i);
            System.out.println("match rate " + tmp);
            if(tmp > max) {
                maxIdx = i;
                max = tmp;
            }
        }

        System.out.println("max matches " + max);
        System.out.println(maxIdx);

        return max;
    }

}
