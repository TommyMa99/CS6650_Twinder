import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.apache.http.HttpEntity;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import static java.lang.Math.max;
import static java.lang.Math.min;

public class TwinderGetThread implements Runnable{
    private volatile boolean running = true;
    private long minLatency;
    private long maxLatency;
    private long avgLatency;
    private boolean firstFlag;

    public TwinderGetThread() {
        minLatency = Long.MAX_VALUE;
        maxLatency = -1;
        avgLatency = 0;
        firstFlag = true;
    }
    @Override
    public void run() {
        while (running) {
            for (int i = 0; i <= 4; i++) {
                long start = System.currentTimeMillis();
                int p = ThreadLocalRandom.current().nextInt(1, 3);
                if (p == 1) {
                    // Send get stat
                    try {
                        HttpClient httpClient = HttpClientBuilder.create().build();
                        String swipeeGenerated = String.valueOf(ThreadLocalRandom.current().nextInt(1, 50001));
                        HttpGet statRequest = new HttpGet("http://44.235.76.169:8080/Twinder_war%20exploded_2/stats/" + swipeeGenerated);
                        HttpResponse statResponse = httpClient.execute(statRequest);
                        HttpEntity entity = statResponse.getEntity();
                        String responseBody = EntityUtils.toString(entity);
                        EntityUtils.consume(entity);
                    } catch (Exception e) {
                        System.out.println("Error: " + e.getMessage());
                    }
                } else {
                    // Send get match
                    try {
                        HttpClient httpClient = HttpClientBuilder.create().build();
                        String swiperGenerated = String.valueOf(ThreadLocalRandom.current().nextInt(1, 50001));
                        HttpGet matchRequest = new HttpGet("http://44.235.76.169:8080/Twinder_war%20exploded_2/matches/" + swiperGenerated);
                        HttpResponse matchResponse = httpClient.execute(matchRequest);
                        HttpEntity entity = matchResponse.getEntity();
                        String responseBody = EntityUtils.toString(entity);
                        EntityUtils.consume(entity);
                    } catch (Exception e) {
                        System.out.println("Error: " + e.getMessage());
                    }
                }
                long finish = System.currentTimeMillis();
                long timeElapsed = finish - start;
                minLatency = min(minLatency, timeElapsed);
                maxLatency = max(maxLatency, timeElapsed);
                if (firstFlag) {
                    firstFlag = false;
                    avgLatency = timeElapsed;
                } else {
                    avgLatency = (long) ((timeElapsed + avgLatency) / 2.0);
                }
            }
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public long getMinLatency() {
        return minLatency;
    }

    public long getMaxLatency() {
        return maxLatency;
    }

    public long getAvgLatency() {
        return avgLatency;
    }

    public void stopThread() {
        running = false;
    }
}
