import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

public class ConsumerLauncher {

    public static final int NUM_THREAD = 10;
    public static void main(String[] args) throws InterruptedException {
        ConcurrentHashMap<String, ArrayList<Integer>> concurrentHashMap = new ConcurrentHashMap<>();
        ConsumerThread[] runnables = new ConsumerThread[NUM_THREAD];
        for (int i = 0; i < NUM_THREAD; i++) {
            runnables[i] = new ConsumerThread(concurrentHashMap);
        }
        Thread[] threads = new Thread[NUM_THREAD];
        for (int i = 0; i < NUM_THREAD; i++) {
            threads[i] = new Thread(runnables[i]);
            threads[i].start();
        }
        for (int i = 0; i < NUM_THREAD; i++) {
            threads[i].join();
        }
    }
}
