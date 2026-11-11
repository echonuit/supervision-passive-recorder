package fr.univ_amu.iut.supervision.persistance;

import fr.univ_amu.iut.supervision.web.sensor.NewSensor;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class DbModel {

    private static final int MYSQL_DUPLICATE_ERROR = 1062;
    private static final int MYSQL_FOREIGN_ERROR = 1452;

    private final DbConnection dbConnection = new DbConnection();
    /*     public List<Media> getAllMedia() throws SQLException {
        List<Media> result = new ArrayList<>();

        try (Connection conn = dbConnection.connect();
             PreparedStatement stmt = conn.prepareStatement("SELECT * FROM media");
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                result.add(new Media(
                    rs.getInt("id"),
                    rs.getString("title"),
                    rs.getString("type")
                ));
            }
        }
        return result;
    } */

    public int addSensor(NewSensor sensor) throws SQLException {
        String sql = "INSERT INTO Capteur (devEUI, nomCapteur, longitude, latitude, idGroupe) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = dbConnection.connect();
                PreparedStatement stmt = conn.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, sensor.devEUI());
            stmt.setString(2, sensor.sensorName());
            if (sensor.longitude() != null) {
                stmt.setDouble(3, sensor.longitude());
            } else {
                stmt.setNull(4, java.sql.Types.DOUBLE);
            }
            if (sensor.latitude() != null) {
                stmt.setDouble(4, sensor.latitude());
            } else {
                stmt.setNull(4, java.sql.Types.DOUBLE);
            }
            if (sensor.groupId() != null) {
                stmt.setInt(5, sensor.groupId());
            } else {
                stmt.setNull(5, java.sql.Types.INTEGER);
            }
            stmt.executeUpdate();
            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    return generatedKeys.getInt(1);
                }
            }
        }
        return -1;
    }

    public int createGroup(String groupName) throws SQLException {
        String sql = "INSERT INTO Groupe (nomGroupe) VALUES (?)";
        try (Connection conn = dbConnection.connect();
                PreparedStatement stmt = conn.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, groupName);
            stmt.executeUpdate();
            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    return generatedKeys.getInt(1);
                }
            }
        }
        return -1;
    }
}
