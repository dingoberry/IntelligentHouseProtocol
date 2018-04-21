
public class TextUtils {

    public static boolean isEmpty(CharSequence str) {
        return null == str || "".equals(str.toString().trim());
    }
}
