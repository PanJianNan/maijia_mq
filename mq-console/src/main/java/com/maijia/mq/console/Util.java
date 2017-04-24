package com.maijia.mq.console;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * 工具类
 *
 * @author cjx
 */
public class Util {

    /**
     * 日志
     */
    private static final Log logger = LogFactory.getLog(Util.class);

    /**
     * 获取绝对路径(项目路径中不可以有空格)
     *
     * @param relPath 相对路径
     */
    public static String getAbsPath(String relPath) {
        String rootPath = Util.class.getResource("/").getPath().substring(1).replaceFirst("target/test-classes/", "");
        StringBuffer result = new StringBuffer(rootPath);
        result.append(relPath);
        return result.toString();
    }

    /**
     * 将对象转化为String，如果对象为null，则返回""
     *
     * @param obj
     * @return String
     */
    public static String getStrOfObj(Object obj) {
        return obj == null ? "" : obj.toString();
    }

    /**
     * 将String转化为int，如果不能转化，则返回0
     *
     * @param str
     * @return int
     */
    public static int getIntOfStr(String str) {
        try {
            if (str.contains(".")) {
                return (int) parseDoubleToDouble(Double.parseDouble(str), "#");
            } else {
                return Integer.parseInt(str);
            }
        } catch (Exception ingore) {
            return 0;
        }
    }

    /**
     * 将对象转化为int，如果不能转化，则返回0
     *
     * @param obj
     * @return int
     */
    public static int getIntOfObj(Object obj) {
        if (obj == null || "".equals(obj)) {
            return 0;
        }

        String str = obj.toString();
        return getIntOfStr(str);

    }

    public static double getDoubleOfObj(Object dou) {
        if (dou == null || dou.equals("")) {
            return 0.0;
        }
        if (dou instanceof Double) {
            return ((Double) dou).doubleValue();
        }
        if (isNumber(dou)) {
            return Double.parseDouble(dou.toString());
        } else {
            return 0.0;
        }
    }

    public static double getPercent(double son, double father) {
        if (father == 0) {
            return 0;
        }
        return son / father;
    }

    public static BigDecimal getBigDecimal(Object dou) {
        if (dou == null) {
            return new BigDecimal(0);
        }
        if (isNumber(dou)) {
            return new BigDecimal(dou.toString());
        } else {
            return new BigDecimal(0);
        }
    }

    /**
     * 将对象转化为long，如果转化不了则返回0
     *
     * @param longObj
     * @return Long
     */
    public static Long getLongOfObj(Object longObj) {
        Long l = null;
        try {
            if (longObj == null) {
                l = Long.valueOf("0");
            } else {
                l = Long.valueOf(longObj.toString());
            }
        } catch (NumberFormatException e) {
            l = Long.valueOf("0");
        }
        return l;
    }

    /**
     * 将BigDecimal转化为String，当BigDecimal为0.0时返回""
     *
     * @param big
     * @return String 保留小数点后两位
     */
    public static String getStrOfZeroBigDecimal(BigDecimal big) {
        if (big.equals(new BigDecimal(0))) {
            return "";
        } else {
            DecimalFormat df = new DecimalFormat("#.##");
            big = big.setScale(2, BigDecimal.ROUND_HALF_UP);
            return getStrOfObj(df.format(big));
        }
    }

    /**
     * 将big转化为String，当double为0.0时返回""
     *
     * @param dou
     * @return String
     */
    public static String getStrOfZeroDouble(double dou) {
        if (dou == 0.0) {
            return "";
        } else {
            return new Double(dou).toString();
        }

    }

    /**
     * 将数转化为String，若值为0时返回""
     * 若非0,当数为Double型时，截取小数点后两位；当数为Integer或Long型时返回其值即可
     *
     * @param obj
     * @return String
     */
    public static String getStrOfNumber(Object obj) {
        double dou = getDoubleOfObj(obj);
        if (dou == 0) {
            return "";
        }
        if (obj instanceof Double || obj instanceof Integer || obj instanceof Long) {
            return new DecimalFormat("#.##").format(dou);
        }
        return Util.getStrOfObj(obj);
    }

    /**
     * 将null的字符串转换为""
     *
     * @param str
     * @return String
     */
    public static String getNullStr(String str) {
        if (str == null) {
            return "";
        }

        if (str.equals("null") || str.equals("NULL")) {
            return "";
        } else {
            return str;
        }
    }

    /**
     * 将null的对象转换为""
     *
     * @param obj
     * @return String
     */
    public static String getNullStr(Object obj) {
        if (obj == null) {
            return "";
        }

        if ("null".equals(obj) || "NULL".equals(obj)) {
            return "";
        } else {
            return obj.toString();
        }
    }

    /**
     * 从.properties文件中读取配置属性放入Map中
     *
     * @param filePath
     * @param aes      是否解密
     * @return Map
     * @throws Exception
     */
    public static Map<String, String> getMsgFromProperties(String filePath, boolean aes) {

        Map<String, String> map = new HashMap<String, String>();
        // 获取资源文件
        InputStream is = ClassLoader.getSystemResourceAsStream(filePath);
        // 属性列表
        Properties prop = new Properties();
        try {
            // 从输入流中读取属性列表
            prop.load(is);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        // 返回Properties中包含的key-value的Set视图
        Set<Entry<Object, Object>> set = prop.entrySet();
        // 返回在此Set中的元素上进行迭代的迭代器
        Iterator<Entry<Object, Object>> it = set.iterator();
        String key = null, value = null;
        // 循环取出key-value
        while (it.hasNext()) {
            Entry<Object, Object> entry = it.next();
            key = String.valueOf(entry.getKey());
            value = String.valueOf(entry.getValue());
            key = key == null ? key : key.trim().toUpperCase(Locale.getDefault());
            value = value == null ? value : value.trim().toUpperCase(Locale.getDefault());
            // 将key-value放入map中
            if (aes) {
                try {
                    map.put(AESUtil.decryptStr(key), AESUtil.decryptStr(value));
                } catch (Exception e) {
                    logger.info(e.getMessage());
                }
            } else {
                map.put(key, value);
            }
        }
        return map;
    }

    /**
     * 根据传入的提示信息编号返回提示信息内容
     * @return String
     */
    //	public static String getMsgFromID(String msdID){
    //
    ////		String msg = Constants.msgMap.get(msdID);
    //		return "";
    //	}

    /**
     * 将对象数组的List封装为Map对象
     *
     * @param list
     * @return Map 封装好的Map对象
     */
    @SuppressWarnings({"rawtypes", "unchecked"})
    public static Map listToMap(List<Object[]> list) {

        Map map = new HashMap();
        if (list == null || list.size() == 0) {
            return map;
        }
        for (int i = 0; i < list.size(); i++) {
            Object[] obj = list.get(i);

            if (obj.length == 2) {
                if (obj[0] == null || obj[1] == null) {
                    return null;
                }
                map.put(obj[0], obj[1]);
            } else {
                map.put(obj[0], obj);
            }
        }
        return map;
    }

    /**
     * list转为数组
     *
     * @param list
     * @return
     */

    @SuppressWarnings("rawtypes")
    public static String[] listToArray(List list) {
        if (list == null || list.size() == 0)
            return new String[]{};
        String a[] = new String[list.size()];
        for (int i = 0; i < list.size(); i++)
            a[i] = list.get(i).toString();
        return a;
    }

    /**
     * 比较两个对象（字段类型均为简单类型或字符串类型）的所有字段值是否相等，一般用于测试
     *
     * @param expected      目标对象
     * @param actual        被比较的对象
     * @param exludedFields 排除字段，即不比较的字段
     * @return boolean
     */
    @SuppressWarnings("rawtypes")
    public static boolean equals(Object expected, Object actual, String... exludedFields) {
        List<String> errorList = new ArrayList<String>();
        Class<?> exp = expected.getClass();
        Class<?> act = actual.getClass();

        Method method[] = exp.getMethods();

        for (int i = 0; i < method.length; i++) {
            String methodName = method[i].getName();

            boolean isBreak = false;
            if (methodName.indexOf("get") == 0 && !methodName.equals("getClass")) {
                Class<?> type = method[i].getReturnType();
                if (exludedFields != null) {
                    for (int j = 0; j < exludedFields.length; j++) {//不比较字段
                        if (methodName.substring(3).toLowerCase(Locale.getDefault()).equals(exludedFields[j].toLowerCase(Locale.getDefault()))) {
                            isBreak = true;
                            break;
                        }
                    }
                    if (isBreak) {
                        continue;
                    }
                }
                try {
                    if (type.equals(int.class)) {
                        int expValue = (Integer) method[i].invoke(expected, new Object[0]);
                        int actValue = (Integer) act.getMethod(methodName, new Class[0]).invoke(actual, new Object[0]);
                        if (expValue - actValue != 0) {
                            errorList.add(methodName.substring(3) + "字段的值不相等：expected was：" + expValue + ",bus was " + actValue);
                        }

                    } else if (type.equals(double.class)) {
                        double expValue = (Double) method[i].invoke(expected, new Object[0]);
                        double actValue = (Double) act.getMethod(methodName, new Class[0]).invoke(actual, new Object[0]);

                        if (Math.abs(expValue - actValue) > 0.00001) {
                            errorList.add(methodName.substring(3) + "字段的值不相等：expected was：" + expValue + ",bus was " + actValue);
                        }

                    } else if (type.equals(float.class)) {
                        float expValue = (Float) method[i].invoke(expected, new Object[0]);
                        float actValue = (Float) act.getMethod(methodName, new Class[0]).invoke(actual, new Object[0]);

                        if (Math.abs(expValue - actValue) < 0.00001) {
                            errorList.add(methodName.substring(3) + "字段的值不相等：expected was：" + expValue + ",bus was " + actValue);
                        }

                    } else if (type.equals(long.class)) {
                        long expValue = (Long) method[i].invoke(expected, new Object[0]);
                        long actValue = (Long) act.getMethod(methodName, new Class[0]).invoke(actual, new Object[0]);
                        if (expValue - actValue != 0) {
                            errorList.add(methodName.substring(3) + "字段的值不相等：expected was：" + expValue + ",bus was " + actValue);
                        }

                    } else {
                        Object expValue = method[i].invoke(expected, new Object[0]);
                        Object actValue = act.getMethod(methodName, new Class[0]).invoke(actual, new Object[0]);
                        if (expValue != null) {
                            if (!expValue.equals(actValue)) {
                                System.out.println(methodName.substring(3) + "字段的值不相等：expected was：" + expValue + ",bus was " + actValue);
                            }
                        } else if (actValue != null) {
                            System.out.println(methodName.substring(3) + "字段的值不相等：expected was：" + expValue + ",bus was " + actValue);
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        if (errorList.size() > 0) {//打印不相等信息，并返回
            Iterator it = errorList.iterator();
            while (it.hasNext()) {
                System.out.println(it.next());
            }
            return false;
        } else {
            return true;
        }
    }

    /**
     * 以统一值如初始化一个对象
     *
     * @param cla
     * @param value
     * @param exludedFields
     * @return <T> T
     */
    public static <T> T createUnifedObject(Class<T> cla, String value, String... exludedFields) {

        Method method[] = cla.getMethods();
        T retObj = null;
        try {
            retObj = cla.newInstance();
        } catch (Exception e1) {
            e1.printStackTrace();
        }

        for (int i = 0; i < method.length; i++) {
            String methodName = method[i].getName();
            if (methodName.indexOf("set") == 0) {

                Class<?>[] type = method[i].getParameterTypes();
                boolean isBreak = false;
                if (exludedFields != null) {
                    for (int j = 0; j < exludedFields.length; j++) {//不初始化字段
                        if (methodName.substring(3).toLowerCase(Locale.getDefault()).equals(exludedFields[j].toLowerCase(Locale.getDefault()))) {
                            isBreak = true;
                            break;
                        }
                    }
                    if (isBreak) {
                        continue;
                    }
                }

                try {
                    if (type[0].equals(int.class)) {
                        method[i].invoke(retObj, Integer.parseInt(value));

                    } else if (type[0].equals(double.class)) {
                        method[i].invoke(retObj, Double.parseDouble(value));

                    } else if (type[0].equals(float.class)) {
                        method[i].invoke(retObj, Float.parseFloat(value));

                    } else if (type[0].equals(long.class)) {
                        method[i].invoke(retObj, Long.parseLong(value));

                    } else {
                        method[i].invoke(retObj, value);
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        }
        return retObj;
    }

    /**
     * 获取本地IP地址
     *
     * @return String
     */
    public static String getLocalHost() {

        String localhost = "";
        InetAddress net;
        try {
            net = InetAddress.getLocalHost();
            localhost = net.getHostAddress();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        return localhost;
    }

    /**
     * 比较两个map 的所有字段是否相等，一般用于测试
     *
     * @param expected
     * @param actual
     * @param exludedFields 排除字段
     * @return boolean
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    public static boolean equals(Map expected, Map actual, String... exludedFields) {

        List errorList = new ArrayList();
        if (expected.size() == 0) {
            errorList.add("expected 对象的长度为0！");
            return false;
        }
        Iterator it = expected.keySet().iterator();
        while (it.hasNext()) {
            String key = (String) it.next();
            boolean isBreak = false;
            if (exludedFields != null) {//排除字段
                for (int i = 0; i < exludedFields.length; i++) {
                    if (key.toLowerCase(Locale.getDefault()).endsWith(exludedFields[i].toLowerCase(Locale.getDefault()))) {
                        isBreak = true;
                    }
                }
            }
            if (isBreak) {
                continue;
            }
            String expectedValue = String.valueOf(expected.get(key));
            String actualValue = String.valueOf(actual.get(key));
            if (expectedValue == null) {
                if (actualValue != null) {
                    errorList.add("字段" + key + "的值不相等,expected " + expectedValue + ", but was " + actualValue);
                }
            } else if (!expectedValue.equals(actualValue)) {
                errorList.add("字段" + key + "的值不相等,expected " + expectedValue + ", but was " + actualValue);
            }
        }
        if (errorList.size() == 0) {
            return true;
        } else {
            for (int i = 0; i < errorList.size(); i++) {
                System.out.println(errorList.get(i).toString());
            }
            return false;
        }
    }
    //

    /**
     * uri处理工具，用于截取uri的某个部分
     *
     * @param uri
     * @param hierachy 制定uri的层级，层级从0开始
     * @return String
     */
    public static String getPartOfUri(String uri, int hierachy) {

        String uriPaths[] = uri.split("/");
        String str = "";
        if (!uri.startsWith("/")) {
            str = "/" + uriPaths[hierachy];
        } else {
            str = "/" + uriPaths[hierachy + 1];
        }
        return str;
    }

    /**
     * 将查询出的列名List转化为String，用于拼装SQL
     *
     * @param list
     * @return String
     */
    @SuppressWarnings("rawtypes")
    public static String columnListToString(List list) {

        String str = "";
        for (int i = 0; i < list.size(); i++) {
            Object[] o = (Object[]) list.get(i);
            String s = Util.getStrOfObj(o[0]);
            str += s;
        }
        return str;
    }

    /**
     * 判断某字符串是否是纯数字,如“12345”或“100.222”
     *
     * @param string
     * @return boolean
     */
    public static boolean isNumber(Object string) {
        String regex = "-?\\d+\\.?\\d*";
        String str = String.valueOf(string);
        if (str.endsWith(".")) {
            return false;
        }
        if (str.matches(regex)) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * 验证是否是“数字，数字，数字”格式，如“100,200” 或 “100.11,200” 等
     *
     * @param string
     * @return boolean
     */
    public static boolean isNumberSplitedByComma(Object string) {

        String regex = "(-?\\d+\\.?\\d*)+(,-?\\d+\\.?\\d*)*";
        String str = String.valueOf(string);
        if (str.endsWith(".")) {
            return false;
        }
        if (str.matches(regex)) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * 将数组转为字符串
     *
     * @param objs
     * @return String
     */
    public static String arrayToString(Object objs[]) {
        String str = "";
        for (int i = 0; i < objs.length; i++) {
            str += objs[i];
            str += i < objs.length - 1 ? "," : "";
        }
        return str;
    }

    /**
     * 判断字符串数组是否相同
     *
     * @param target
     * @return String
     */
    public static boolean arrayEqual(String target, String compare) {
        if (target.length() != compare.length()) {
            return false;
        }
        String[] com = compare.split(",");
        for (int i = 0; i < com.length; i++) {
            if (target.indexOf(com[i]) < 0) {
                return false;
            }
        }
        return true;
    }

    /**
     * 验证map里的必备字段，如果某个key缺失，则返回第一个缺失key的名字；如果全都包含,则返回"TRUE"
     *
     * @param map
     * @param keys
     * @return String:如果某个key缺失，则返回第一个缺失key的名字；如果全都包含,则返回"TRUE"
     */
    @SuppressWarnings("rawtypes")
    public static String checkNullKeys(Map map, String... keys) {

        for (int i = 0; i < keys.length; i++) {
            if (!map.containsKey(keys[i])) {
                return keys[i];
            }
        }
        return "TRUE";
    }


    /**
     * 验证map里某些key的值是否为空白字符""，如果某个key的值为"",则返回该key的名字;如果全都不为"",则返回"TRUE"
     *
     * @param map
     * @param keys
     * @return String
     */
    @SuppressWarnings("rawtypes")
    public static String checkBlankValues(Map map, String... keys) {

        for (int i = 0; i < keys.length; i++) {
            if ("".equals(map.get(keys[i]))) {
                return keys[i] + "的值为空！";
            }
        }
        return "TRUE";
    }

    /**
     * 验证map里某些key是否必备且不为空白字符""
     *
     * @param map
     * @param keys
     * @return
     */
    @SuppressWarnings("rawtypes")
    public static String checkNullAndBlankValues(Map map, String... keys) {
        for (int i = 0; i < keys.length; i++) {
            if (!map.containsKey(keys[i])) {
                return "缺少必传参数" + keys[i];
            }
            if ("".equals(map.get(keys[i]))) {
                return keys[i] + "的值为空！";
            }
        }
        return "TRUE";
    }

    /**
     * 验证某些key的值是否是数字，如果是某个key的值不为数字，则返回该key的名字;如果全都为数字,则返回"TRUE"
     *
     * @param map
     * @param keys
     * @return String
     */
    @SuppressWarnings("rawtypes")
    public static String checkIsNumber(Map map, String... keys) {

        for (int i = 0; i < keys.length; i++) {
            if (!isNumber(map.get(keys[i]))) {
                return keys[i] + "含有非法字符！";
            }
        }
        return "TRUE";
    }

    /**
     * 根据目录获取文件列表
     *
     * @return List
     */
    @SuppressWarnings({"rawtypes", "unchecked"})
    public static List getFiles(String path) {

        List list = new ArrayList();
        File file = new File(path);
        if (!file.exists()) {
            return list;
        }
        File[] files = file.listFiles();
        for (int i = 0; i < files.length; i++) {
            if (files[i].isFile()) {
                list.add(files[i]);
            }
        }

        Collections.sort(list, new Comparator<File>() {

            @Override
            public int compare(final File f1, final File f2) {

                if (f1.lastModified() - f2.lastModified() < 0) {
                    return 1;
                } else if (f1.lastModified() - f2.lastModified() > 0) {
                    return -1;
                } else {
                    return 0;
                }
            }
        });

        if (list.size() > 30) {
            return list.subList(0, 30);
        }
        return list;
    }

    /**
     * 下载/复制文件
     */
    public static void downloadFile(String respath, String despath) {

	/*	File f1 = new File(respath);
        File f2 = new File(despath);*/

        //		try {
        //			FileUtils.copyFileToDirectory(f1, f2);
        //		} catch (IOException e) {
        //			e.printStackTrace();
        //		}
    }

    /**
     * 删除指定路径的文件
     *
     * @param path
     * @return boolean 删除成功返回true，删除失败返回false
     */
    public static boolean deleteFile(String path) {

        File file = new File(path);
        return file.delete();
    }

    /**
     * 将一个源map的值copy到目标map,如果目标map存在某key,则该key的值被source map的同名key覆盖，
     * 如果目标map 里没有某key,则新增
     *
     * @param source
     * @param target
     * @param ignoreKeys 忽略的key，即不拷贝该key,如果ignoreKeys不传或为空，则拷贝全部字段
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    public static void copyMapValue(Map source, Map target, String... ignoreKeys) {

        for (String key : (Set<String>) source.keySet()) {

            boolean isBreak = false;
            if (ignoreKeys != null) {
                for (String ignoreKey : ignoreKeys) {
                    if (key.equals(ignoreKey)) {
                        isBreak = true;
                        break;
                    }
                }
            }
            if (!isBreak) {
                target.put(key, source.get(key));
            }
        }
    }

    /**
     * 将一个源map的值copy到目标map,如果目标map存在某key,则该key的值被source map的同名key覆盖，
     * 如果目标map 里没有某key,则新增
     *
     * @param source
     * @param target
     * @param includedKeys 需要拷贝的key
     */
    @SuppressWarnings({"rawtypes", "unchecked"})
    public static void copyMapCertainValue(Map source, Map target, String... includedKeys) {

        for (String key : (Set<String>) source.keySet()) {

            if (includedKeys != null) {
                for (String ignoreKey : includedKeys) {
                    if (key.equals(ignoreKey)) {
                        target.put(key, source.get(key));
                        break;
                    }
                }
            }
        }
    }

    /**
     * 格式化数字
     *
     * @param number
     * @param pattern 保留小数点格式
     * @return 返回double类型
     */
    public static double parseDoubleToDouble(double number, String pattern) {

        DecimalFormat df = new DecimalFormat(pattern);
        return Double.parseDouble(df.format(number));
    }

    /**
     * 格式化数字
     *
     * @param number
     * @param pattern 保留小数点格式，如保留4位小数，不够位则补0，则格式为"#.####",如100.123，保留4为小数的结果为100.1230
     *                保留4位小数，不够位不补0，则格式为"0.000",如100.123，保留4为小数的结果为100.123
     *                保留0为小数，格式为"#"
     * @return 返回字符串类型
     */
    public static String parseDoubleToStr(double number, String pattern) {
        DecimalFormat df = new DecimalFormat(pattern);
        return df.format(number);
    }

    /**
     * 将小数转为百分数
     *
     * @param number
     * @return String
     */
    public static String parseDoubleToPercenge(double number, String pattern) {

        return parseDoubleToStr(100 * number, pattern) + "%";
    }

    /**
     * 将list记录里的某个字段转为字符串
     *
     * @param list
     * @param index            Object[] 的第几个元素
     * @param isHasSingleQuote
     * @return
     */
    public static String convertItemsToStr(List<Object[]> list, int index, boolean isHasSingleQuote) {
        String str = "";
        if (isHasSingleQuote) {
            for (int i = 0; i < list.size(); i++) {
                Object obj[] = list.get(i);
                str += "'";
                str += obj[index];
                str += "'";
                str += ",";
            }
        } else {
            for (int i = 0; i < list.size(); i++) {
                Object obj[] = list.get(i);
                str += obj[index] + ",";
            }
        }
        if (str.length() > 0) {
            str = str.substring(0, str.length() - 1);
        }
        return str;
    }

    /**
     * 将包含byte数组键值对的Map集合转成字符串数组
     *
     * @param map
     * @return String[]或null
     */
    public static String[] smapToArray(Map<String, String> map) {
        String[] paramByte = null;
        if (map != null && map.size() > 0) {
            paramByte = new String[map.size() * 2];
            Iterator<Entry<String, String>> it = map.entrySet().iterator();
            int index = 0;
            while (it.hasNext()) {
                Entry<String, String> entry = it.next();
                paramByte[index++] = entry.getKey();
                paramByte[index++] = entry.getValue();
            }
        }
        return paramByte;
    }

    /**
     * 将字符串中的非法字符过滤掉
     *
     * @param fields
     * @return String
     */
    public static String filterStr(String fields) {

        return fields.replaceAll("/", "").replaceAll("-", "").replaceAll("、", "").replaceAll("（", "").replaceAll("）", "");
    }

    /**
     * 将数组转为字符串
     *
     * @param list
     * @return String
     */
    public static String[] stringListToArray(List<String> list) {
        String[] paramByte = null;
        if (list != null && list.size() > 0) {
            paramByte = new String[list.size()];
            int index = 0;
            for (int i = 0; i < list.size(); i++) {
                paramByte[index++] = list.get(i);
            }
        }
        return paramByte;
    }


    /**
     * @param list
     * @return
     */
    public static Map<String, String> parseListToMap(List<Object[]> list) {
        Map<String, String> map = new HashMap<String, String>();
        for (Object[] obj : list) {
            map.put(obj[0].toString(), obj[1].toString());
        }
        return map;
    }

    /**
     * 过滤特殊字符
     *
     * @param str
     * @return 如果有特殊字符 则返回true
     * @author ycj Create On 2014-5-5
     */
    public static boolean checkIllegalChars(String str) {
        String regEx = "[`~!@#$%^&*()+=|{}':;',//[//].<>/?~！@#￥%……&*（）——+|{}【】‘；：”“’。，、？]";
        Pattern p = Pattern.compile(regEx);
        Matcher m = p.matcher(str);
        return m.find();

    }

    /**
     * 先将dou类型转化为BigDecimal类型，然后按pattern再格式化
     *
     * @param dou
     * @param pattern 保留小数点格式，如保留4位小数，不够位则补0，则格式为"#.####",如100.123，保留4为小数的结果为100.1230
     *                保留4位小数，不够位不补0，则格式为"0.000",如100.123，保留4为小数的结果为100.123
     *                保留0为小数，格式为"#"
     * @return 返回字符串类型
     * @author jc
     */
    public static String parseBigDecimalFromDouToStr(double dou, String pattern) {
        BigDecimal bd = (new BigDecimal(dou));
        DecimalFormat df = new DecimalFormat(pattern);
        return df.format(bd);
    }

    /**
     * 将不符合Json格式的非法字符替换成合法字符</br>
     * 目前非法字符替换方式如下:</br>
     * 1、英文状态下的"------>中文状态下的“
     *
     * @param str
     * @return
     */
    public static String replaceIllegalChars(String str) {
        return str.replaceAll("\"", "“");
    }

    /**
     * @param map
     * @param list
     * @param index
     * @param keyword
     * @return
     * @author jc
     */
    public static <T> int compareTo(Map<String, Object> map, List<Map<String, Object>> list, int index, String keyword) {
        String str1 = Util.getStrOfObj(map.get(keyword));
        String str2 = Util.getStrOfObj(list.get(index).get(keyword));
        for (int i = index; i < list.size(); i++) {
            str2 = Util.getStrOfObj(list.get(i).get(keyword));
            if (str1.compareTo(str2) == 0) {
                return i + 1;
            } else if (str1.compareTo(str2) < 0) {
                return -(i + 1);
            }
        }
        return 0;
    }

    /**
     * @param list Integer类型的List
     * @return int类型的数组
     * @author jc
     */
    public static int[] getIntArrayFromIntegetList(List<Integer> list) {
        int[] intArr = new int[list.size()];
        for (int i = 0; i < list.size(); i++) {
            intArr[i] = list.get(i).intValue();
        }
        return intArr;
    }

    /**
     * @param arr Object类型的数组
     * @return int类型的数组
     * @author jc
     */
    public static int[] getIntArrayFromArray(Object[] arr) {
        int[] intArr = new int[arr.length];
        for (int i = 0; i < arr.length; i++) {
            intArr[i] = Util.getIntOfObj(arr[i]);
        }
        return intArr;
    }

    /**
     * 比较两个时间类型的字符串的大小，只比较共有部分，例如XXXX年-XX月和NNNN年比，只比较年份。
     *
     * @param date1 格式："2015","2015-03","2015-3","2015-03-03"均可以。
     * @param date2 同上
     * @return date1小于date2返回-1;</br>
     * date1等于date2返回0;</br>
     * date1大于date2返回1</br>
     * 例如compareTo("2014-3","2015-04-04")返回-1
     * @author jc
     */
    public static int compareTo(String date1, String date2) {
        int[] date1Arr = getIntArrayFromArray(date1.split("-"));
        int[] date2Arr = getIntArrayFromArray(date2.split("-"));
        int length = date1Arr.length > date2Arr.length ? date2Arr.length : date1Arr.length;
        for (int i = 0; i < length; i++) {
            if (date1Arr[i] > date2Arr[i]) {
                return 1;
            } else if (date1Arr[i] < date2Arr[i]) {
                return -1;
            }
        }
        return 0;
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    public static HashMap getHashMapFromMap(Map map) {
        if (null == map) {
            return new HashMap();
        } else {
            return new HashMap(map);
        }
    }

    /**
     * 将百分数转化为小数，例如：0.12%转化为0.0012。
     *
     * @param obj
     * @return
     */
    public static double parsePercentToDouble(Object obj) {
        String str = Util.getStrOfObj(obj);
        int index = str.indexOf("%");
        if (index == str.length() - 1) {
            return Util.getDoubleOfObj(str.substring(0, index)) / 100;
        }
        return Util.getDoubleOfObj(str);
    }
}
