package kami.gg.souppvp.util;

public class MathUtil {

    public static boolean isNumeric(String string) {
        try {
            Integer.parseInt(string);
            return true;
        } catch(NumberFormatException e){
            return false;
        }
    }

}
