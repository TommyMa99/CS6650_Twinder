import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import org.apache.commons.dbcp2.BasicDataSource;

import java.io.IOException;
import java.sql.SQLException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeoutException;

public class ConsumerLauncher {

    public static final int NUM_THREAD = 20;
    private final static String HOST_NAME = "ec2-52-25-109-172.us-west-2.compute.amazonaws.com";
    private final static String USER_NAME = "rabbit_user";
    private final static String PASSWORD = "GLANT123,./";
    private final static String VIRTUAL_HOST_NAME = "broker";

    private static final String JDBC_URL = "jdbc:mysql://twinder-mysql-db.cswcvfjifpvk.us-east-2.rds.amazonaws.com/swipeInfoDB";
    private static final String USERNAME = "admin";
    private static final String JPASSWORD = "password";
    private static final int MAX_TOTAL_CONNECTIONS = 60;
    public static void main(String[] args) throws InterruptedException, IOException, TimeoutException, SQLException {
        ExecutorService executor = Executors.newFixedThreadPool(30);
        BasicDataSource dataSource = new BasicDataSource();
        dataSource.setUrl(JDBC_URL);
        dataSource.setUsername(USERNAME);
        dataSource.setPassword(JPASSWORD);
        dataSource.setMaxTotal(MAX_TOTAL_CONNECTIONS);

        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost(HOST_NAME);
        factory.setUsername(USER_NAME);
        factory.setPassword(PASSWORD);
        factory.setVirtualHost(VIRTUAL_HOST_NAME);
        Connection connection = factory.newConnection();
        DatabaseInitTool.initDatabase();
        ConsumerThread[] runnables = new ConsumerThread[NUM_THREAD];
        for (int i = 0; i < NUM_THREAD; i++) {
            runnables[i] = new ConsumerThread(connection, dataSource, executor);
        }
        Thread[] threads = new Thread[NUM_THREAD];
        for (int i = 0; i < NUM_THREAD; i++) {
            threads[i] = new Thread(runnables[i]);
            threads[i].start();
        }
        for (int i = 0; i < NUM_THREAD; i++) {
            threads[i].join();
        }
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            // Code to be executed when the program is about to terminate
            System.out.println("Program is about to terminate...");
            try {
                dataSource.close();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }));
    }
}
