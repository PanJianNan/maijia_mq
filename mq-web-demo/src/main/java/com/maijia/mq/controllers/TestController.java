/*
package com.maijia.mq.controllers;

import com.maijia.mq.cache.ICacheService;
import com.maijia.mq.consumer.DefaultConsumer;
import com.maijia.mq.producer.DefaultProducer;
import com.maijia.mq.util.ConstantUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

*/
/**
 * TestController
 *
 * @author panjn
 * @date 2016/10/25
 *//*

@RestController
public class TestController {

    @Resource
    private DefaultProducer defaultProducer;
    @Resource
    private DefaultConsumer consumer;
    @Resource
    private ICacheService cacheService;

    @RequestMapping(value = "cache")
    public String cacheTest() {
        String cacheKey = ConstantUtils.CACHE_KEY_PREFIX + "test_message:queue2";
        List<String> strings = new ArrayList<>();
        strings.add("abc");
        strings.add("123");
        //cacheService.set(cacheKey, strings, 5 * 60L);
        //List<String> list = cacheService.get(cacheKey);
        cacheService.del(cacheKey);
        cacheService.lPush(cacheKey, new String("i am msg1"));
        cacheService.lPush(cacheKey, new String("i am msg2"));
        cacheService.lPush(cacheKey, new String("i am msg3"));
        Object obj = cacheService.bRPop(0, cacheKey);
        Object obj2 = cacheService.bRPop(0, cacheKey);
        Object obj3 = cacheService.bRPop(0, cacheKey);
        cacheService.lPush(cacheKey, new String("i am msg4"));
        Object obj4 = cacheService.bRPop(0, cacheKey);
        Object obj5 = cacheService.bRPop(0, cacheKey);
        System.out.println(obj5);
        System.out.println(123);
        return null;
    }

    @RequestMapping(value = "produce")
    public String produce(String msg) {
        String cacheKey = ConstantUtils.CACHE_KEY_PREFIX + "test_message:queue2";
        cacheService.lPush(cacheKey, new String("i am msg5555"));
        defaultProducer.produce("pjn", msg);
        return "produce success";
    }

    @RequestMapping(value = "consume1")
    public String consume1() throws InterruptedException {
        return String.valueOf(consumer.take("pjn"));
    }

    @RequestMapping(value = "consume2")
    public String consume2() throws InterruptedException {
        return String.valueOf(consumer.take("pjn"));
    }

    public void newConnection() {
        try {
            ServerSocket server = null;
            try {
                server = new ServerSocket(4700);
                //创建一个ServerSocket在端口4700监听客户请求
            } catch (Exception e) {
                System.out.println("can not listen to:" + e);
                //出错，打印出错信息
            }

            Socket socket = null;

            try {
                socket = server.accept();
                //使用accept()阻塞等待客户请求，有客户
                //请求到来则产生一个Socket对象，并继续执行
            } catch (Exception e) {
                System.out.println("Error." + e);
                //出错，打印出错信息
            }

            String line;
            BufferedReader is = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            //由Socket对象得到输入流，并构造相应的BufferedReader对象
            PrintWriter os = new PrintWriter(socket.getOutputStream());

            //由Socket对象得到输出流，并构造PrintWriter对象
            BufferedReader sin = new BufferedReader(new InputStreamReader(System.in));

            //由系统标准输入设备构造BufferedReader对象
            System.out.println("Client:" + is.readLine());

            //在标准输出上打印从客户端读入的字符串
            line = sin.readLine();

            //从标准输入读入一字符串
            while (!line.equals("bye")) {
                //如果该字符串为 "bye"，则停止循环
                os.println(line);

                //向客户端输出该字符串
                os.flush();

                //刷新输出流，使Client马上收到该字符串
                System.out.println("Server:" + line);

                //在系统标准输出上打印读入的字符串
                System.out.println("Client:" + is.readLine());

                //从Client读入一字符串，并打印到标准输出上
                line = sin.readLine();

                //从系统标准输入读入一字符串
            } //继续循环
            os.close(); //关闭Socket输出流
            is.close(); //关闭Socket输入流
            socket.close(); //关闭Socket
            server.close(); //关闭ServerSocket
        } catch (Exception e) {
            System.out.println("Error:" + e);
            //出错，打印出错信息
        }

    }
}
*/
