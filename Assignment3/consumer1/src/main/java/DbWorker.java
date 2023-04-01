import org.apache.commons.dbcp2.BasicDataSource;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

public class DbWorker implements Runnable {
    private BasicDataSource dataSource;
    private List<String> myMessagges;

    public DbWorker(List<String> myMessagges, BasicDataSource dataSource) {
        this.myMessagges = myMessagges;
        this.dataSource = dataSource;
    }

    @Override
    public void run() {
        Connection conn = null;
        PreparedStatement pstmt = null;
        try {
            conn = dataSource.getConnection();
            pstmt = conn.prepareStatement("INSERT INTO SwipeData (swiper_id, swipee_id, direction) VALUES (?, ?, ?)");
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        for (String m : myMessagges) {
            try {
                String[] message = m.split("#");
                pstmt.setString(1, message[0]);
                pstmt.setString(2, message[1]);
                pstmt.setString(3, message[2]);
                pstmt.addBatch();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }
        try {
            pstmt.executeBatch();
            conn.close();
            pstmt.close();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            try {
                pstmt.close();
                conn.close();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }
    }
}