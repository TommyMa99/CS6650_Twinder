import org.apache.commons.dbcp2.BasicDataSource;

import java.sql.*;

public class DatabaseInitTool {
    private static final String JDBC_URL = "jdbc:mysql://twinder-mysql-db.cswcvfjifpvk.us-east-2.rds.amazonaws.com/swipeInfoDB";
    private static final String USERNAME = "admin";
    private static final String PASSWORD = "password";
    private static final int MAX_TOTAL_CONNECTIONS = 65;

    private static BasicDataSource dataSource;

    static {
        dataSource = new BasicDataSource();
        dataSource.setUrl(JDBC_URL);
        dataSource.setUsername(USERNAME);
        dataSource.setPassword(PASSWORD);
        dataSource.setMaxTotal(MAX_TOTAL_CONNECTIONS);
    }
    public static synchronized Connection getConnection() throws SQLException, ClassNotFoundException {
        // Get a connection from the pool
        return dataSource.getConnection();
    }

    public static void closeConnection(Connection conn) throws SQLException {
        if (conn != null) {
            conn.close();
        }
    }

    public static void initDatabase() {
        Connection conn = null;
        ResultSet rs = null;
        PreparedStatement pstmt = null;
        Statement stmt = null;
        try {
            conn = DatabaseInitTool.getConnection();
            stmt = conn.createStatement();
            String sql = "CREATE DATABASE IF NOT EXISTS swipeInfoDB";
            stmt.executeUpdate(sql);
            System.out.println("Initialized database");

            DatabaseMetaData meta = conn.getMetaData();
            rs = meta.getTables(null, null, "SwipeData", null);
            boolean tableExists = rs.next();
            if (tableExists) {
                String dropTable = "DROP TABLE SwipeData";
                pstmt = conn.prepareStatement(dropTable);
                pstmt.executeUpdate();
                System.out.println("Table dropped");
            }
            String initsql = "CREATE TABLE SwipeData (id INT AUTO_INCREMENT PRIMARY KEY, swiper_id VARCHAR(20), swipee_id VARCHAR(20), direction VARCHAR(10))";
            pstmt = conn.prepareStatement(initsql);
            pstmt.executeUpdate();
            System.out.println("Initialized table");
        } catch (SQLException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        } finally {
            // Close the database resources
            if (rs != null) {
                try {
                    rs.close();
                } catch (SQLException e) {
                    System.out.println(e.getMessage());
                }
            }
            if (pstmt != null) {
                try {
                    pstmt.close();
                } catch (SQLException e) {
                    System.out.println(e.getMessage());
                }
            }
            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException e) {
                    System.out.println(e.getMessage());
                }
            }
        }
    }
}
