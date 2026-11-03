import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DbConnection {

    private static final String DB_HOSTNAME = System.getenv("DB_HOSTNAME");
    private static final String DB_NAME     = System.getenv("DB_NAME");
    private static final String DB_USER     = System.getenv("DB_USER");
    private static final String DB_PASSWORD = System.getenv("DB_PASSWORD");

    public Connection connect() throws SQLException {
        String url = "jdbc:mysql://" + DB_HOSTNAME + "/" + DB_NAME;
        return DriverManager.getConnection(url, DB_USER, DB_PASSWORD);
    }
}