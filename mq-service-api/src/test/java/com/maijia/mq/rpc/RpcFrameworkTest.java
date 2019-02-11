package com.maijia.mq.rpc;

import com.maijia.mq.constant.CommonConstant;
import org.apache.commons.lang3.StringUtils;
import org.junit.Test;
import org.springframework.util.Assert;

/**
 * Created by panjiannan on 2019/1/7.
 */
public class RpcFrameworkTest {
    //server
    @Test
    public void export() throws Exception {
        RpcFramework.export(A.class, new B().getClass(), CommonConstant.NIO_RPC_PORT, "1.0.0");
    }

    //client
    @Test
    public void refer() throws Exception {
        A a = RpcFramework.refer(A.class, "127.0.0.1", CommonConstant.NIO_RPC_PORT, "1.0.0");
        Assert.isTrue(!a.print(""));
        Assert.isTrue(a.print("hello 大兄弟"));
    }

    interface A {
        boolean print(String txt);
    }

    class B implements A {

        @Override
        public boolean print(String txt) {
            if (StringUtils.isBlank(txt)) {
                return false;
            }
            System.out.println(txt);
            return true;
        }
    }
}