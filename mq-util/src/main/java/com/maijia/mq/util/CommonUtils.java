package com.maijia.mq.util;

/**
 * CommonUtils
 *
 * @author panjn
 * @date 2019/1/23
 */
public class CommonUtils {

    public static boolean isIp(String ip) {//判断是否是一个IP
        if ("localhost".equals(ip)) {
            return true;
        }
        boolean result = false;
        if (ip.matches("\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}")) {
            String s[] = ip.split("\\.");
            if (Integer.parseInt(s[0]) < 255)
                if (Integer.parseInt(s[1]) < 255)
                    if (Integer.parseInt(s[2]) < 255)
                        if (Integer.parseInt(s[3]) < 255)
                            result = true;
        }
        return result;
    }

}
