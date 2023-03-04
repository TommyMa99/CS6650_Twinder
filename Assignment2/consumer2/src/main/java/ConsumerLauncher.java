import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.PriorityBlockingQueue;

public class ConsumerLauncher {

    public static final int NUM_THREAD = 10;
    public static void main(String[] args) throws InterruptedException {
        ConcurrentHashMap<String, PriorityBlockingQueue<ConcurrentHashMap.Entry<String, Integer>>> concurrentHashMap = new ConcurrentHashMap<>();
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
