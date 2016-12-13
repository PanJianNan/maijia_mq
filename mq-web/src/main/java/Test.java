/**
 * Test
 *
 * @author panjn
 * @date 2016/11/8
 */
public class Test {
    public static void main(String[] args) {
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(3000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                System.out.println("线程1");
            }
        });

        t.start();

        Thread t2 = new Thread(new Runnable() {
            @Override
            public void run() {
                int a = 0;
                int b = 10/a;
            }
        });

        t2.start();

        Thread t3 = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(3000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                System.out.println("线程3");

            }
        });

        t3.start();
    }
}
