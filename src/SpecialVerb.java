import java.util.ArrayList;

public class SpecialVerb {

    ArrayList<String> prefixes;

    public SpecialVerb(ArrayList<String> prefixes) {
        this.prefixes = prefixes;
    }

    public boolean isValid(String prefix, String verb) {
        return prefixes.size() == 2 && prefixes.get(0).equals(prefix) && prefixes.get(1).equals(verb);
    }
}
