import com.opencsv.CSVWriter;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;

import java.io.*;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class apiCaller {

    private static int NUMTHREADS = 99;

    public static HashMap<String, Double> findMetrics() throws IOException {
        Reader in = new FileReader("stat.csv");
        CSVParser csvParser = CSVParser.parse(in, CSVFormat.DEFAULT.withFirstRecordAsHeader());
        HashMap<String, Double> map = new HashMap<>();
        ArrayList<Integer> arr = new ArrayList<>();
        double total = 0;
        double minimum = 999999;
        double maximum = -1;
        int counter = 0;
        int start_time = 0;
        int end_time = 0;
        for (CSVRecord record:csvParser){
//            System.out.println(arr.size());
            counter = counter + 1;
            List<String> element = record.toList();
//            System.out.println(element);
            if(counter == 1){
                start_time = Integer.parseInt(element.get(0));
            }
            int latency = Integer.parseInt(element.get(2));
            total = total + latency;
            maximum = Math.max(latency, maximum);
            minimum = Math.min(latency, minimum);
            arr.add(latency);
            if(counter == 500000){
                end_time = Integer.parseInt(element.get(0)) + Integer.parseInt(element.get(2));
                break;
            }
        }
        arr.sort(Comparator.naturalOrder());
        double median = (double)(arr.get((arr.size() - 1) / 2) + arr.get(arr.size() / 2)) / 2.0;
        double average = total / arr.size();
        int index = (int) Math.ceil(99 / 100.0 * arr.size());
        double percentile_ninetynine = arr.get(index - 1);
        double throughput = (arr.size() / ((end_time - start_time)/1000.0));
        map.put("mean_response_time", average);
        map.put("median_response_time", median);
        map.put("percentile_99th", percentile_ninetynine);
        map.put("throughput", throughput);
        map.put("max_latency", maximum);
        map.put("min_latency", minimum);
        return map;
    }
    public static void main(String[] args) throws InterruptedException, IOException {
        File file = new File("stat.csv");
        if (file.exists()) {
            file.delete();
        }
        FileWriter outputfile = new FileWriter(file);
        CSVWriter writer = new CSVWriter(outputfile);
        String[] header = {"start_time(ms)", "request_type", "latency(ms)", "response_code"};
        writer.writeNext(header);

//        CSVPrinter printer = new CSVPrinter(new FileWriter("stat.csv"), CSVFormat.EXCEL);
//        printer.printRecord("start_time(ms)", "request_type", "latency(ms)", "response_code");
        int succeed_request = 0;
        int failed_request = 0;
        TwinderThread[] runnables = new TwinderThread[NUMTHREADS];
        long start = System.currentTimeMillis();
        for(int i = 0; i < NUMTHREADS; i++){
            runnables[i] = new TwinderThread("thread:" + i, writer, outputfile, start);
        }
        Thread[] threads = new Thread[NUMTHREADS];
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
        System.out.println("Output from part 2:");
        System.out.println("Number of Threads: "+NUMTHREADS);
        System.out.println("Total time spent: "+ timeElapsed/1000.0+"s");
        System.out.println((int)(500000 / (timeElapsed/1000.0)) +" requests per second based on total time");
        System.out.println("Total Successful Request: "+succeed_request);
        System.out.println("Total Failed Request: "+failed_request);
        System.out.println();

        System.out.println("Output from part 3: ");
        HashMap<String, Double> metrics = findMetrics();
        System.out.println("Mean Response Time: "+metrics.get("mean_response_time")+"ms");
        System.out.println("Median Response Time: "+metrics.get("median_response_time")+"ms");
        System.out.println("Throughput: "+metrics.get("throughput")+" requests per second");
        System.out.println("p99 Response Time: "+metrics.get("percentile_99th")+"ms");
        System.out.println("Minimum Response Time: "+metrics.get("min_latency")+"ms");
        System.out.println("Maximum Response Time: "+metrics.get("max_latency")+"ms");

        writer.close();
    }
}