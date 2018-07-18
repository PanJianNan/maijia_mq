//import com.maijia.mq.domain.Message;
//import org.junit.Test;
//
//import java.io.ByteArrayInputStream;
//import java.io.IOException;
//import java.io.ObjectInputStream;
//import java.net.InetSocketAddress;
//import java.nio.ByteBuffer;
//import java.nio.channels.SocketChannel;
//import java.util.Date;
//import java.util.Scanner;
//
///**
// * NioTest
// *
// * @author panjn
// * @date 2017/4/24
// */
//public class NioTest {
//    //客户端
//    @Test
//    public void client() throws IOException {
//        SocketChannel sChannel = null;
//        try {
//            //1. 获取通道
//            sChannel = SocketChannel.open(new InetSocketAddress("127.0.0.1", 3985));
//
//            //2. 切换非阻塞模式
//            sChannel.configureBlocking(false);
//
//            //3. 分配指定大小的缓冲区
//            ByteBuffer buf = ByteBuffer.allocate(1024);
//
//            //4. 发送数据给服务端
////            for (int i = 0; i < 100; i++) {
////                buf.put((": " + new Date().toString() + "\n" + "message!").getBytes());
////                buf.flip();
////                sChannel.write(buf);
////                buf.clear();
////                Thread.sleep(1000);
////            }
//            while (true) {
//                buf.put(("test.file.publish1-1").getBytes());
//                buf.flip();
//                sChannel.write(buf);
//                buf.clear();
//
//                ByteBuffer buf2 = ByteBuffer.allocate(1024);
//                int len = sChannel.read(buf2);
//                buf2.flip();
////                String message = new String(buf.array(), 0, len);
//
//                ByteArrayInputStream byteInt=new ByteArrayInputStream(buf2.array());
//                ObjectInputStream objInt=new ObjectInputStream(byteInt);
//                Message msg = (Message) objInt.readObject();
//                buf2.clear();
//                System.out.println(msg);
//                Thread.sleep(1000);
//            }
//
//            //5. 关闭通道
////            sChannel.close();
//        } catch (IOException e) {
//            e.printStackTrace();
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        } catch (ClassNotFoundException e) {
//            e.printStackTrace();
//        } finally {
//            //5. 关闭通道
//            sChannel.close();
//        }
//    }
//}
