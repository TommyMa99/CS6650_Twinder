import com.rabbitmq.client.*;
import org.apache.commons.dbcp2.BasicDataSource;
import org.apache.commons.lang3.StringEscapeUtils;

import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.stream.Collectors;

public class ConsumerThread implements Runnable {
    private Connection connection;
    private Channel channel;
    private final static String QUEUE_NAME_1 = "Twinder_queue_1";
    private final static String EXCHANGE_NAME = "swipe_task";
    private BasicDataSource dataSource;
    private java.sql.Connection newConn;
    private PreparedStatement pstmt;
    private ExecutorService executor;


    public ConsumerThread(Connection connection, BasicDataSource dataSource, ExecutorService executor) throws SQLException {
        this.connection = connection;
        this.dataSource = dataSource;
//        newConn = this.dataSource.getConnection();
        System.out.println("here");
        String insertQuery = "INSERT INTO SwipeData (swiper_id, swipee_id, direction) VALUES (?, ?, ?)";
//        pstmt = newConn.prepareStatement(insertQuery);
        this.executor = executor;
    }
    @Override
    public void run() {
        int batchSize = 200;
        try {
            channel = this.connection.createChannel();
            channel.exchangeDeclare(EXCHANGE_NAME, BuiltinExchangeType.FANOUT);
            Map<String, Object> arguments = new HashMap<>();
            arguments.put("x-queue-type", "quorum");
            channel.queueDeclare(QUEUE_NAME_1, true, false, false, arguments);
            channel.queueBind(QUEUE_NAME_1, EXCHANGE_NAME, "");
            channel.basicQos(batchSize, false);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        List<String> myMessagges = new ArrayList<>();
        try {
            channel.basicConsume(QUEUE_NAME_1, false, new DefaultConsumer(channel) {
                @Override
                public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
                    String str = StringEscapeUtils.unescapeJava(new String(body));
                    String[] splits = str.split("comment");
                    int l = splits[0].length();
                    String firstPart = splits[0].substring(1, l-2).replace("\"", "");
                    Map<String, String> headerMap = Arrays.stream(firstPart.split(","))
                            .map(s -> s.split(":"))
                            .collect(Collectors.toMap(s -> s[0], s -> s[1]));
                    String swipeDir = headerMap.get("leftorright");
                    String swiper = headerMap.get("swiper");
                    String swipee = headerMap.get("swipee");
                    myMessagges.add(swiper+"#"+swipee+"#"+swipeDir);
                    if (myMessagges.size() >= batchSize) {
                        List<String> copyList = new ArrayList<>();

                        for (String element : myMessagges) {
                            String copyElement = new String(element);
                            copyList.add(copyElement);
                        }
                        executor.execute(new DbWorker(copyList, dataSource));
//                        for (String m : myMessagges) {
//                            try {
//                                String[] message = m.split("#");
//                                pstmt.setString(1, message[0]);
//                                pstmt.setString(2, message[1]);
//                                pstmt.setString(3, message[2]);
//                                pstmt.addBatch();
//                            } catch (SQLException e) {
//                                throw new RuntimeException(e);
//                            }
//                        }
//                        try {
//                            pstmt.executeBatch();
//                        } catch (SQLException e) {
//                            throw new RuntimeException(e);
//                        }
////                            pstmt.close();
////                            newConn.close();
                        myMessagges.clear();
                        channel.basicAck(envelope.getDeliveryTag(), true);
                    }
                }
            });
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
