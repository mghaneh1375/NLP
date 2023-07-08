import java.util.ArrayList;

public class Verbs {

    private final static ArrayList<SpecialVerb> specialVerbs = new ArrayList<>() {
        {
            {
                add(new SpecialVerb(
                        new ArrayList<>() {
                            {
                                add("نام");
                                add("ببر");
                            }
                        }
                ));
                add(new SpecialVerb(
                        new ArrayList<>() {
                            {
                                add("معرفی");
                                add("کن");
                            }
                        }
                ));
                add(new SpecialVerb(
                        new ArrayList<>() {
                            {
                                add("اسم");
                                add("ببر");
                            }
                        }
                ));
                add(new SpecialVerb(
                        new ArrayList<>() {
                            {
                                add("بر");
                                add("شمار");
                            }
                        }
                ));
                add(new SpecialVerb(
                        new ArrayList<>() {
                            {
                                add("بر");
                                add("بشمار");
                            }
                        }
                ));
                add(new SpecialVerb(
                        new ArrayList<>() {
                            {
                                add("لیست");
                                add("کن");
                            }
                        }
                ));
                add(new SpecialVerb(
                        new ArrayList<>() {
                            {
                                add("بیان");
                                add("کن");
                            }
                        }
                ));
                add(new SpecialVerb(
                        new ArrayList<>() {
                            {
                                add("پیشنهاد");
                                add("بده");
                            }
                        }
                ));
                add(new SpecialVerb(
                        new ArrayList<>() {
                            {
                                add("پیشنهاد");
                                add("ده");
                            }
                        }
                ));

                add(new SpecialVerb(
                        new ArrayList<>() {
                            {
                                add("درست");
                                add("میشه");
                            }
                        }
                ));
            }
        }
    };

    public static boolean isSpecialVerb(String prefix, String verb) {

        for (SpecialVerb specialVerb : specialVerbs) {

            if (specialVerb.isValid(prefix, verb))
                return true;

        }

        return false;
    }


}
