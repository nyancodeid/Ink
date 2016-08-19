package ink.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by PC-Comp on 8/19/2016.
 */
public class Regex {
    public static final String URL_REGEX = "^((https?|ftp)://|(www|ftp)\\.)?[a-z0-9-]+(\\.[a-z0-9-]+)+([/?].*)?$";


    public static boolean isLink(String value) {
        boolean success = false;
        Pattern pattern = Pattern.compile(URL_REGEX);
        Matcher matcher = pattern.matcher(value);
        if (matcher.find()) {
            success = true;
        }
        return success;
    }
}
