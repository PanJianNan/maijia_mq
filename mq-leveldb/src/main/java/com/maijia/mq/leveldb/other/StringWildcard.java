package com.maijia.mq.leveldb.other;

/**
 * 使用'*'，匹配字符串
 *
 * @author panjn
 * @date 2016/12/16
 */
public class StringWildcard {

    public static boolean wildMatch(String pattern, String str) {
        pattern = toJavaPattern(pattern);
        return java.util.regex.Pattern.matches(pattern, str);
    }

    private static String toJavaPattern(String pattern) {
        String result = "^";
        char metachar[] = {'$', '^', '[', ']', '(', ')', '{', '|', '+', '?', '.', '/'};
        for (int i = 0; i < pattern.length(); i++) {
            char ch = pattern.charAt(i);
            boolean isMeta = false;
            for (int j = 0; j < metachar.length; j++) {
                if (ch == metachar[j]) {
                    result += "\\" + ch;
                    isMeta = true;
                    break;
                }
            }
            if (!isMeta) {
                if (ch == '*') {
                    result += ".*";
                } else {
                    result += ch;
                }

            }
        }
        result += "$";
        return result;
    }
}
