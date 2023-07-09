import DB.Common;
import com.mongodb.BasicDBObject;
import org.bson.Document;
import org.bson.conversions.Bson;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import static com.mongodb.client.model.Filters.*;

public class Question {

    String name;
    List<List<String>> tags;
    List<List<Boolean>> mandatoryOrOptionalList;
    List<Bson> initFilters;

    public void setProjection(BasicDBObject projection) {
        this.projection = projection;
    }

    BasicDBObject projection = null;
    Common db;

    int minRequiredMatch = 80;

    public Question(Common db, String n) {
        tags = new ArrayList<>();
        mandatoryOrOptionalList = new ArrayList<>();
        initFilters = new ArrayList<>();
        this.db = db;
        this.name = n;
    }

    public void addNewFilter(Bson filter) {
        initFilters.add(filter);
    }

    public void addNewModel(List<String> in) {

        List<Boolean> tmp = new ArrayList<>();
        List<String> tmp2 = new ArrayList<>();

        for(String s : in) {
            tmp.add(!s.endsWith("?"));
            tmp2.add(s.endsWith("?") ? s.substring(0, s.length() - 1) : s);
        }

        tags.add(tmp2);
        mandatoryOrOptionalList.add(tmp);
    }

    private List<String> inputKeys;

    private double localMatch(int idx) {

//        System.out.println("matching in : ");
//        for(String itr : tags.get(idx))
//            System.out.print(itr + " ");
//        System.out.println();

        int tagsSize = 0;
        for(boolean itr : mandatoryOrOptionalList.get(idx)) {
            if(itr)
                tagsSize++;
        }

        List<String> lessSentence = tagsSize < inputKeys.size() ? tags.get(idx) : inputKeys;
        List<String> largeSentence = tagsSize < inputKeys.size() ? inputKeys : tags.get(idx);
        boolean isTagLess = tagsSize < inputKeys.size();

        int currIdx = 0;
        int matches = 0;
        int weakMatches = 0;

        for(int i = 0; i < lessSentence.size(); i++) {

            boolean find = false;
            boolean isMandatory = isTagLess ? mandatoryOrOptionalList.get(idx).get(i) : false;

            for(int j = currIdx; j < largeSentence.size(); j++) {

                if(largeSentence.get(j).equals(lessSentence.get(i))) {

                    if(!isTagLess)
                        isMandatory = mandatoryOrOptionalList.get(idx).get(j);

                    currIdx = j + 1;
                    find = true;
                    break;
                }

            }

            if(find && isMandatory)
                matches++;
            else if(find)
                weakMatches++;
        }

        System.out.println("matches: " + matches);
        System.out.println("tagSize: " + tagsSize);

        return Math.min(matches * 1.0 / tagsSize, 1.0);
    }

    private boolean findSubSeq(String[] list, List<String> wanted) {

        int currIdx = 0;

        for(String itr : wanted) {

            boolean find = false;

            for(int j = currIdx; j < list.length; j++) {

                if(list[j].equals(itr)) {
                    currIdx = j + 1;
                    find = true;
                    break;
                }

            }

            if(!find)
                return false;
        }

        return true;
    }

    private String searchSubSeq(String[] translate, List<String> subList) {

        int idx = 1;
        String res = null;

        while (idx < subList.size()) {

//            System.out.println("testing " + String.join(" ", subList.subList(0, idx)));
            if(findSubSeq(translate, subList.subList(0, idx))) {
                res = String.join(" ", subList.subList(0, idx));
                System.out.println("res is " + res);
            }
            else if(res != null) {
                System.out.println("returning");
                return res;
            }

            idx++;
        }

        return res;
    }

    private String findWithSpace(List<String> list, List<String> subList) {


//        if(itr.equals("حلیم گوشت عدس"))

        for(String itr : list) {

            String tmp = searchSubSeq(itr.split("\\s+"), subList);

            if (tmp != null) {
                System.out.println(tmp);
                return tmp;
            }
        }

        return null;
    }

    private List<String> secondaryList;

    private void findKeys(ArrayList<String> in, boolean needSecondaryList) {

        inputKeys = new ArrayList<>();
        if(needSecondaryList)
            secondaryList = new ArrayList<>();

        int idx = 0;
        int skip = 0;

        for(String itr : in) {

            if(skip > 0) {
                skip--;
                idx++;
                continue;
            }

            String key = "unknown";

            for (Word word : Main.words) {


                if (word.translates.contains(itr)) {

                    key = word.key;
                    if(needSecondaryList)
                        secondaryList.add(itr);

                    break;
                }

                if(word.key.equals("food_name") || word.key.equals("drink_name")) {

                    String tmp = findWithSpace(word.translates, in.subList(idx, Math.min(idx + 4, in.size())));
                    System.out.println("Tmp is " + tmp);

                    if(tmp != null) {

                        key = word.key;
                        skip = tmp.split("\\s+").length - 1;

                        if(needSecondaryList)
                            secondaryList.add(tmp);

                        break;
                    }
                }

            }

            inputKeys.add(key);
            idx++;
        }

    }

    public double match(ArrayList<String> in) {

        findKeys(in, false);

        double max = -1;
        int maxIdx = 0;

        for(int i = 0; i < tags.size(); i++) {
            double tmp = localMatch(i);
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

    public void doFilter(ArrayList<String> in) {

        findKeys(in, true);
        ArrayList<Bson> filters = new ArrayList<>(initFilters);
        String specLoc = null;
        String loc = null;
        String name = null;
        Boolean isHot = null;

        int idx = 0;

        for (String key : inputKeys) {

            if(key.equalsIgnoreCase("city_or_state_name"))
                loc = secondaryList.get(idx);
            else if(key.equalsIgnoreCase("loc-spec"))
                specLoc = secondaryList.get(idx);
            else if(key.equalsIgnoreCase("food_name"))
                name = secondaryList.get(idx);
            else if(key.equalsIgnoreCase("drink-adjective")) {
                System.out.println(secondaryList.get(idx));
                if(secondaryList.get(idx).equalsIgnoreCase("سرد") ||
                        secondaryList.get(idx).equalsIgnoreCase("خنک") ||
                        secondaryList.get(idx).equalsIgnoreCase("تگری")
                )
                    isHot = false;
                else if(secondaryList.get(idx).equalsIgnoreCase("گرم") ||
                        secondaryList.get(idx).equalsIgnoreCase("داغ")
                )
                    isHot = true;
            }

            idx++;
        }

        if(loc != null) {
            if(specLoc == null)
                filters.add(or(
                        eq("city", loc),
                        eq("state", loc)
                ));
            else if(specLoc.equals("استان"))
                filters.add(eq("state", loc));
            else if(specLoc.equals("شهر"))
                filters.add(eq("city", loc));
        }

        if(name != null)
            filters.add(regex("name", Pattern.compile(Pattern.quote(name), Pattern.CASE_INSENSITIVE)));

        if(isHot != null)
            filters.add(eq("features.is_hot", isHot));

        List<Document> places = db.find(and(filters), projection == null ?
                new BasicDBObject("name", 1).append("state", 1)
                    .append("city", 1) : projection
        );
        idx = 0;

        for(Document place : places) {

            if(idx > 10)
                break;

            for(String key : place.keySet()) {
//                System.out.println(key);
                System.out.println(place.get(key));
            }
//            System.out.println(place.getString("name") + " " + place.getString("state") + " " + place.getString("city"));
            idx++;
        }

    }
}
