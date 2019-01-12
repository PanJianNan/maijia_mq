import org.junit.Test;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

/**
 * SocketTest
 *
 * @author panjn
 * @date 2019/1/12
 */
public class SocketTest {

    @Test
    public void client() {
        String host = "127.0.0.1";
        int port = 3198;
        try (Socket socket = new Socket(host, port);
             ObjectOutputStream objectOutputStream = new ObjectOutputStream(socket.getOutputStream());
             ObjectInputStream objectInputStream = new ObjectInputStream(socket.getInputStream())) {

            objectOutputStream.writeUTF("interfaceClass.getName()");
            objectOutputStream.writeUTF("method.getName()");
            objectOutputStream.writeUTF("version");
            objectOutputStream.writeObject(new Class[]{});
            objectOutputStream.writeObject(null);
            socket.shutdownOutput();

            Object result = objectInputStream.readObject();
            socket.shutdownInput();
            if (result instanceof Exception) {
                throw (Exception) result;
            }
            System.out.println(result);

        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
