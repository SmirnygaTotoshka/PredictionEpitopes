import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class Main {

    private static final String url = "jdbc:mysql://192.168.0.11:3306/experiments";
    private static final String user = "root";
    private static final String password = "meow_root";

    // JDBC variables for opening and managing connection
    private static Connection con;

    public synchronized static Connection getConnection() {
        return con;
    }


    public static void main(String[] args) throws SQLException {
     //   try {
            con = DriverManager.getConnection(url, user, password);
            System.out.println("Success");
      /*  } catch (SQLException e) {
            System.out.println("Failed " + e.getErrorCode());
        }*/

    }
}