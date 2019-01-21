package com.maijia.mq;

import com.caucho.hessian.io.Hessian2Input;
import com.caucho.hessian.io.Hessian2Output;
import com.caucho.hessian.io.SerializerFactory;
import com.maijia.mq.util.HessianSerializeUtils;
import com.maijia.mq.util.JavaSerializeUtils;
import org.junit.Assert;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.Serializable;

import static org.junit.Assert.*;

/**
 * HessianSerializerUtilTest
 *
 * @author panjn
 * @date 2019/1/18
 */
public class HessianSerializerUtilTest {
    @Test
    public void test1() throws Exception {
        String serialStr = "fjlj-12.12";
        byte[] data = HessianSerializeUtils.serialize(serialStr);
        String deSerialStr = HessianSerializeUtils.deserialize(data);
        Assert.assertEquals(serialStr, deSerialStr);
    }

    @Test
    public void test2() throws Exception {
        StreamUser streamUser = new StreamUser(18, "Ross");
        byte[] data = HessianSerializeUtils.serialize(streamUser);
        StreamUser deSerialObj = HessianSerializeUtils.deserialize(data);
        Assert.assertEquals(streamUser, deSerialObj);

    }

    @Test
    public void testString() throws Exception {
//        String str = "abc123+-/潘建南 」』」@#";
//        for (int i=0; i < 1000000;i++){
//            /*byte[] jsBytes =*/ str.getBytes();
//        }
        //COST 480ms

//        for (int i=0; i < 1000000;i++){
//            /*byte[] hsBytes = */HessianSerializeUtils.serialize(str);
//        }
        //cost 5s44ms


        StreamUser maleUser = new StreamUser(18, "Ross");

//        System.out.println( JavaSerializeUtils.serialize(streamUser).length);
//          90 bytes
//        System.out.println(HessianSerializeUtils.serialize(streamUser).length);
//          43 bytes

//        long start1 = System.currentTimeMillis();
//        for (int i = 0; i < 10000000; i++) {
//            JavaSerializeUtils.serialize(maleUser);
//        }
//        //COST 11.5s
//        long end1 = System.currentTimeMillis();
//        System.out.println("js cost:" + (end1-start1) );
//
//
//        long start2 = System.currentTimeMillis();
//        for (int i=0; i < 10000000;i++){
//            HessianSerializeUtils.serialize(maleUser);
//        }
//        //cost 16.8 ???  我试了下4.0.7的hessian是20s左右，阿里的sofa-hessian的确在性能和压缩率上有所提高，但是为什么还是比java自带的序列化性能差呢，如果换成复杂对象呢？
//
//        long end2 = System.currentTimeMillis();
//        System.out.println("hs cost:" + (end2-start2) );

//      ========  复杂对象==========
//        StreamUser femaleUser = new StreamUser(17, "Lucy");
//        House house = new House(maleUser, femaleUser, "北二环");
//
//        System.out.println(JavaSerializeUtils.serialize(house).length);
//        long start1 = System.currentTimeMillis();
//        for (int i = 0; i < 10000000; i++) {
//            JavaSerializeUtils.serialize(house);
//        }
//        //COST 11.5s
//        long end1 = System.currentTimeMillis();
//        System.out.println("js cost:" + (end1-start1) );//20.3s
//
//        System.out.println(HessianSerializeUtils.serialize(house).length);
//        long start2 = System.currentTimeMillis();
//        for (int i=0; i < 10000000;i++){
//            HessianSerializeUtils.serialize(house);
//        }
//        long end2 = System.currentTimeMillis();
//        System.out.println("hs cost:" + (end2-start2) );//19.9s 4.0.7hessian约22s

        //暂时结论sofa-hessian序列化比java序列化能节省一半左右的空间，而性能方面，简单对象java序列化占优，但是在复杂对象上sofa-hessian占优
        //sofa-hessian 比 hessian表现的要好一些

        //todo 那么问题来了，MJMQ传递的消息都是简单对象，甚至今后改成String，那么选哪个呢？
        //但是106227条消息，消费只用了55s，iops达到了1931.4，比其前面500左右的iops，高了好多，应该是因为传输的字节数小了，网络开销变小。
        //所以目前更倾向sofa-hessian
    }

}

class StreamUser implements Serializable {
    private static final long serialVersionUID = 1043292910191363562L;

    private int age;
    private String name;

    public StreamUser(int age, String name) {
        this.age = age;
        this.name = name;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof StreamUser)) {
            return false;
        }

        StreamUser target = (StreamUser) obj;
        if (this.age == target.age && this.name.equals(target.name)){
            return true;
        }
        return false;
    }


    @Override
    public int hashCode() {
        int result = 17;
        result = 31 * result + age;
        result = 31 * result + name.hashCode();
        return result;
    }
}

class House implements Serializable {

    private static final long serialVersionUID = 2404699219078923169L;

    private StreamUser maleMaster;

    private StreamUser femaleMaster;

    String address;

    public House(StreamUser maleMaster, StreamUser femaleMaster, String address) {
        this.maleMaster = maleMaster;
        this.femaleMaster = femaleMaster;
        this.address = address;
    }
}