package com.maijia.mq.util;

import java.io.*;

/**
 * java自带序列化工具
 *
 * @author panjn
 * @date 2016/12/16
 */
public class JavaSerializeUtils {

    public static byte[] serialize(Serializable obj) throws IOException {
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(os);
        try {
            oos.writeObject(obj);
            oos.flush();
            return os.toByteArray();
        } finally {
            oos.close();
        }
    }

    public static Serializable deserialize(byte[] evt) throws IOException {
        if (null == evt || evt.length == 0)
            return null;
        ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(evt));
        try {
            return (Serializable) ois.readObject();
        } catch (ClassNotFoundException e) {
            throw new IOException(e);
        } finally {
            ois.close();
        }
    }
}
