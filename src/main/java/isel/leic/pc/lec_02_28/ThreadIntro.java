package isel.leic.pc.lec_02_28;

public class ThreadIntro {
    private static void mysleep(long millis) {
        try {
            Thread.sleep(millis);
        }
        catch(InterruptedException e) {

        }
    }
    public static void main(String[] args) {
        Thread t = new Thread(() -> {
            mysleep(2000);
            System.out.println("hello from thread " +  Thread.currentThread().getName());
        });
        t.setDaemon(true);
        t.start();
        System.out.println("main thread " +  Thread.currentThread().getName());
        System.out.flush();

    }
}
