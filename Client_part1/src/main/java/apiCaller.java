public class apiCaller {

    private static int NUMTHREADS = 100;
    public static void main(String[] args) throws InterruptedException {
        int succeed_request = 0;
        int failed_request = 0;
        try {
            TwinderThread[] runnables = new TwinderThread[NUMTHREADS];
            for(int i = 0; i < NUMTHREADS; i++){
                runnables[i] = new TwinderThread("thread:" + i);
            }
            Thread[] threads = new Thread[NUMTHREADS];
            long start = System.currentTimeMillis();
            for (int i=0; i < NUMTHREADS; i++ ) {
                threads[i] = new Thread (runnables[i]);
                threads[i].start();
            }
            for (int i=0; i < NUMTHREADS; i++) {
                threads[i].join();
                succeed_request = succeed_request + runnables[i].getSucceedRequest();
                failed_request = failed_request + runnables[i].getFailedRequest();
            }
            long finish = System.currentTimeMillis();
            long timeElapsed = finish - start;

            System.out.println("Number of Threads: "+NUMTHREADS);
            System.out.println("Total time spent: "+ timeElapsed/1000.0+"s");
            System.out.println((int)(500000 / (timeElapsed/1000.0)) +" requests per second");
            System.out.println("Total Successful Request: "+succeed_request);
            System.out.println("Total Failed Request: "+failed_request);
        } catch (InterruptedException e) {
            System.err.println("Exception when calling SwipeApi#swipe");
            e.printStackTrace();
        }
    }
}