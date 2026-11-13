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

    private String DB_HOSTNAME = System.getenv("DB_HOSTNAME");
    private String DB_NAME = System.getenv("DB_NAME");
    private String DB_USER = System.getenv("DB_USER");
    private String DB_PASSWORD = System.getenv("DB_PASSWORD");

    private static final Map<String, String> VALUES = new HashMap<>();

    private void parseLine(String line) {
        int equals = line.indexOf("=");
        if (equals != -1) {
            String key = line.substring(0, equals).trim();
            String value = line.substring(equals + 1).trim();
            VALUES.put(key, value);
        }
    }

    private void updateConfigFromValues() {
        if (VALUES.containsKey("DB_HOSTNAME")) {
            DB_HOSTNAME = VALUES.get("DB_HOSTNAME");
        }
        if (VALUES.containsKey("DB_NAME")) {
            DB_NAME = VALUES.get("DB_NAME");
        }
        if (VALUES.containsKey("DB_USER")) {
            DB_USER = VALUES.get("DB_USER");
        }
        if (VALUES.containsKey("DB_PASSWORD")) {
            DB_PASSWORD = VALUES.get("DB_PASSWORD");
        }
    }

    public void readDotEnv() {
        try {
            for (String lines : Files.readAllLines(Path.of(ENVPATH))) {
                parseLine(lines);
            }
            updateConfigFromValues();
        } catch (IOException e) {
            System.err.println(".env not found, falling back to system environment");
        }
    }

    private void loadDriver() throws SQLException {
        try {
            // Keep the MySQL driver for Alwaysdata
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            throw new SQLException("Pilote JDBC MySQL introuvable", e);
        }
    }

    public Connection connect() throws SQLException {
        loadDriver();

        if (DB_HOSTNAME == null) {
            readDotEnv();
        }

        // Keep the MySQL protocol for Alwaysdata
        String url = "jdbc:mysql://" + DB_HOSTNAME + "/" + DB_NAME;
        return DriverManager.getConnection(url, DB_USER, DB_PASSWORD);
    }

    public Connection connect(String dbHostname, String dbName, String dbUser, String dbPassword) throws SQLException {
        loadDriver();
        // Keep the MySQL protocol for Alwaysdata
        String url = "jdbc:mysql://" + dbHostname + "/" + dbName;
        return DriverManager.getConnection(url, dbUser, dbPassword);
    }
}
