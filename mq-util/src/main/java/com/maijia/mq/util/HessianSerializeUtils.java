package com.maijia.mq.util;

import com.caucho.hessian.io.Hessian2Input;
import com.caucho.hessian.io.Hessian2Output;
import com.caucho.hessian.io.SerializerFactory;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * 蚂蚁金服的Hessian序列化工具
 *
 * @author panjn
 * @date 2019/1/18
 */
public class HessianSerializeUtils {

    private HessianSerializeUtils() {
    }

    /**
     * 序列化对象
     *
     * @param obj
     * @param <T>
     * @return
     * @throws IOException
     */
    public static <T> byte[] serialize(T obj) throws IOException {
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        Hessian2Output hout = new Hessian2Output(bout);
        hout.setSerializerFactory(SerializerFactorySingleton.instance);
        hout.writeObject(obj);
        hout.close();
        byte[] data = bout.toByteArray();

        return data;
    }


    /**
     * 反序列化
     *
     * @param data
     * @param <T>
     * @return
     * @throws IOException
     */
    public static <T> T deserialize(byte[] data) throws IOException {
        if (data == null || data.length == 0) {
            return null;
        }

        // Do deserializer
        ByteArrayInputStream bin = new ByteArrayInputStream(data);
        Hessian2Input hin = new Hessian2Input(bin);
        hin.setSerializerFactory(SerializerFactorySingleton.instance);
        Object dst = hin.readObject();
        hin.close();

        return (T) dst;
    }

    public static SerializerFactory getSerializerFactory() {
        return SerializerFactorySingleton.instance;
    }

    /**
     * 使用静态内部类实现的[单例模式]
     */
    private static class SerializerFactorySingleton {
        private static final SerializerFactory instance = new SerializerFactory();
    }

}
