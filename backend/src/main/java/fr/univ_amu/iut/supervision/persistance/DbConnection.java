package fr.univ_amu.iut.supervision.persistance;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

/**
 * Class used to connect to the database, use a .env file.
 */
public class DbConnection {
    private final String ENVPATH = ".env";
    // We default to system env variable
    private String DB_HOSTNAME = System.getenv("DB_HOSTNAME");
    private String DB_NAME = System.getenv("DB_NAME");
    private String DB_USER = System.getenv("DB_USER");
    private String DB_PASSWORD = System.getenv("DB_PASSWORD");

    private static Map<String, String> values = new HashMap<>();

    /**
     * Read the .env file and write in the class attributes "values" the values
     * of the parameter, and write them in their respective class attributes.
     */
    public void readDotEnv() {
        try {
            // Read all the line of the .env file
            for (String lines : Files.readAllLines(Path.of(ENVPATH))) {
                int equals = lines.indexOf("=");
                String key = lines.substring(0, equals);
                String value = lines.substring(equals + 1);

                values.put(key, value);
            }
            DB_HOSTNAME = values.get("DB_HOSTNAME");
            DB_NAME = values.get("DB_NAME");
            DB_USER = values.get("DB_USER");
            DB_PASSWORD = values.get("DB_PASSWORD");
        } catch (IOException e) {
            System.err.println(".env not found, falling back to system environment");
        }
    }

    /**
     * Connect to the database. Use the method readDotEnv() to get authentication arguments.
     * @return A Java.sql.Connection object, represent the connection to the database.
     * @throws SQLException Throw an exception if the connection failed.
     */
    public Connection connect() throws SQLException {
        readDotEnv();
        String url = "jdbc:mariadb://" + DB_HOSTNAME + "/" + DB_NAME;
        return DriverManager.getConnection(url, DB_USER, DB_PASSWORD);
    }

    // This function is used only for testing this class and DbModel.
    /**
     * Connect to the database.
     * @param dbHostname The name of the host.
     * @param dbName The name of the database.
     * @param dbUser The name of the user.
     * @param dbPassword The password of the user.
     * @return A Java.sql.Connection object, represent the connection to the database.
     * @throws SQLException Throw an exception if the connection failed.
     */
    public Connection connect(String dbHostname, String dbName, String dbUser, String dbPassword) throws SQLException {
        String url = "jdbc:mariadb://" + dbHostname + "/" + dbName;
        return DriverManager.getConnection(url, dbUser, dbPassword);
    }
}
