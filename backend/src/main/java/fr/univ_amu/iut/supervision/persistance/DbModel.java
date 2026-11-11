package fr.univ_amu.iut.supervision.persistance;

import fr.univ_amu.iut.supervision.web.sensor.DuplicateSensorException;
import fr.univ_amu.iut.supervision.web.sensor.GroupNotFoundException;
import fr.univ_amu.iut.supervision.web.sensor.NewSensor;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;

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
                PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, sensor.devEUI());
            stmt.setString(2, sensor.sensorName());
            stmt.setObject(3, sensor.longitude(), Types.DECIMAL);
            stmt.setObject(4, sensor.latitude(), Types.DECIMAL);
            stmt.setObject(5, sensor.groupId(), Types.INTEGER);
            stmt.executeUpdate();

            try (ResultSet key = stmt.getGeneratedKeys()) {
                key.next();
                return key.getInt(1);
            }
        } catch (SQLException e) {
            if (e.getErrorCode() == MYSQL_DUPLICATE_ERROR) {
                throw new DuplicateSensorException(sensor.devEUI());
            }
            if (e.getErrorCode() == MYSQL_FOREIGN_ERROR) {
                throw new GroupNotFoundException(sensor.groupId());
            }
            throw e;
        }
    }

    public int createGroup(String groupName) throws SQLException {
        String sql = "INSERT INTO Groupe (nomGroupe) VALUES (?)";

        try (Connection conn = dbConnection.connect();
                PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, groupName);
            stmt.executeUpdate();

            try (ResultSet key = stmt.getGeneratedKeys()) {
                key.next();
                return key.getInt(1);
            }
        }
    }
}
