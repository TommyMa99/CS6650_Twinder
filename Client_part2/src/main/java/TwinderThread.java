import com.opencsv.CSVWriter;
import io.swagger.client.ApiClient;
import io.swagger.client.ApiException;
import io.swagger.client.ApiResponse;
import io.swagger.client.api.SwipeApi;
import io.swagger.client.model.SwipeDetails;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Objects;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

public class TwinderThread implements Runnable{

    private String name;
    private int succeedRequest;
    private int failedRequest;

    private long init_time;

    private CSVWriter writer;
    private FileWriter outputfile;

    public TwinderThread(){}
    public TwinderThread(String name, CSVWriter writer, FileWriter outputfile, long init_time) throws IOException {
        this.name = name;
        this.succeedRequest = 0;
        this.failedRequest = 0;
        this.writer = writer;
        this.init_time = init_time;
        this.outputfile = outputfile;
    }

    public int getSucceedRequest() {
        return succeedRequest;
    }

    public int getFailedRequest() {
        return failedRequest;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public void run() {
        // Send 5000 POST request
        ApiClient client = new ApiClient();
        client.setBasePath("http://54.218.123.117:8080/Twinder_war%20exploded");
        SwipeApi apiInstance = new SwipeApi();
        apiInstance.setApiClient(client);

        try {
            int number = 5075;
            for(int i = 0; i < number; i ++){
                SwipeDetails body = new SwipeDetails();
                String swiperGenerated = String.valueOf(ThreadLocalRandom.current().nextInt(1, 5001));
                String swipeeGenerated = String.valueOf(ThreadLocalRandom.current().nextInt(1, 1000001));
                StringBuilder builder = new StringBuilder(256);
                for (int j = 0; j < 256; j++) {
                    builder.append((char) (ThreadLocalRandom.current().nextInt(33, 128)));
                }
                String messageGenerated = builder.toString();
                String swiptOption = "";
                int p = ThreadLocalRandom.current().nextInt(1, 3);
                if (p == 1) {
                    swiptOption = "left";
                } else {
                    swiptOption = "right";
                }
                body.setComment(messageGenerated);
                body.setSwipee(swipeeGenerated);
                body.setSwiper(swiperGenerated);
                String leftorright = swiptOption;
                long start = System.currentTimeMillis();
                ApiResponse<Void> r = apiInstance.swipeWithHttpInfo(body, leftorright);
                long finish = System.currentTimeMillis();
                long timeElapsed = finish - start;
                if (r.getStatusCode() != 201) {
                    boolean flag = true;
                    int counter = 0;
                    while(flag) {
                        counter = counter + 1;
                        if(counter == 6) {
                            this.failedRequest = this.failedRequest+1;
                            break;
                        }
                        r = apiInstance.swipeWithHttpInfo(body, leftorright);
                        if (r.getStatusCode() == 201) {
                            flag = false;
                            this.succeedRequest = this.succeedRequest + 1;
                        }
                    }
                } else {
                    this.succeedRequest = this.succeedRequest + 1;
                }
                String[] data = {String.valueOf(start-this.init_time), "POST", String.valueOf(timeElapsed), String.valueOf(r.getStatusCode())};
                synchronized (outputfile) {
                    writer.writeNext(data);
                }
            }
        } catch (ApiException e) {
            System.err.println("Exception when calling SwipeApi#swipe");
            e.printStackTrace();
        }
    }
}
