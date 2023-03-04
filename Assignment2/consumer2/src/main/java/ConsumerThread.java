import com.rabbitmq.client.*;
import org.apache.commons.lang3.StringEscapeUtils;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

public class ConsumerThread implements Runnable {
    public ConcurrentHashMap<String, PriorityBlockingQueue<ConcurrentHashMap.Entry<String, Integer>>> concurrentHashMap;
    private Connection connection;
    private Channel channel;
    private final static String QUEUE_NAME_2 = "Twinder_queue_2";
    private final static String HOST_NAME = "ec2-52-25-109-172.us-west-2.compute.amazonaws.com";
    private final static String USER_NAME = "rabbit_user";
    private final static String PASSWORD = "GLANT123,./";
    private final static String VIRTUAL_HOST_NAME = "broker";
    private final static String EXCHANGE_NAME = "swipe_task";

    public ConsumerThread(ConcurrentHashMap<String, PriorityBlockingQueue<ConcurrentHashMap.Entry<String, Integer>>> concurrentHashMap) {
        this.concurrentHashMap = concurrentHashMap;
    }
    @Override
    public void run() {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost(HOST_NAME);
        factory.setUsername(USER_NAME);
        factory.setPassword(PASSWORD);
        factory.setVirtualHost(VIRTUAL_HOST_NAME);
        try {
            connection = factory.newConnection();
            channel = this.connection.createChannel();
            channel.exchangeDeclare(EXCHANGE_NAME, BuiltinExchangeType.FANOUT);
            channel.queueDeclare(QUEUE_NAME_2, true, false, false, null);
            channel.queueBind(QUEUE_NAME_2, EXCHANGE_NAME, "");
        } catch (IOException | TimeoutException e) {
            throw new RuntimeException(e);
        }
        DefaultConsumer consumer = new DefaultConsumer(channel) {
            @Override
            public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
                String str = StringEscapeUtils.unescapeJava(new String(body));
                String[] splits = str.split("comment");
                int l = splits[0].length();
//                String comment = str.substring(l+10);
//                int offset = comment.length();
//                comment = comment.substring(0, offset-2);
                String firstPart = splits[0].substring(1, l-2).replace("\"", "");
                Map<String, String> headerMap = Arrays.stream(firstPart.split(","))
                        .map(s -> s.split(":"))
                        .collect(Collectors.toMap(s -> s[0], s -> s[1]));
                String swipeDir = headerMap.get("leftorright");
                String swiper = headerMap.get("swiper");
                String swipee = headerMap.get("swipee");
                if (Objects.equals(swipeDir, "right")){
                    enqueue(swiper, swipee);
                }
//                System.out.println(concurrentHashMap);
            }
        };
        try {
            channel.basicConsume(QUEUE_NAME_2, true, consumer);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void enqueue(String swiper, String swipee){
//        ConcurrentHashMap<String, PriorityBlockingQueue<ConcurrentHashMap.Entry<String, Integer>>>

        if (!concurrentHashMap.containsKey(swiper)) {
            PriorityBlockingQueue<ConcurrentHashMap.Entry<String, Integer>> newQueue =
                    new PriorityBlockingQueue<>(100, (a,b)-> b.getValue()-a.getValue());
            concurrentHashMap.put(swiper, newQueue);
        }
        PriorityBlockingQueue<ConcurrentHashMap.Entry<String, Integer>> queueTmp = concurrentHashMap.get(swiper);
        boolean flag = true;
        for (Map.Entry<String, Integer> e: queueTmp) {
            if (e.getKey().equals(swipee)) {
                flag = false;
                int num = e.getValue();
                queueTmp.remove(e);
                e.setValue(num+1);
                queueTmp.offer(e);
                break;
            }
        }
        if (flag) {
            Map.Entry<String, Integer> entry = new AbstractMap.SimpleEntry<>(swipee, 1);
            queueTmp.offer(entry);
        }
        concurrentHashMap.put(swiper, queueTmp);
    }
}
