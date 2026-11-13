package fr.univ_amu.iut.supervision.persistance;

import fr.univ_amu.iut.supervision.web.sensor.NewSensor;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DbModel {

    public boolean userExist(String id) throws SQLException {
        try (Connection conn = DbConnection.connect();
             PreparedStatement stmt = conn.prepareStatement("SELECT idUtilisateur FROM Utilisateur WHERE idUtilisateur = ?;")) {
            stmt.setString(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }
        }
    }

    public void registerUser(String id, String lastName, String scientistName, String firstName) throws SQLException {
        try (Connection conn = DbConnection.connect();
             PreparedStatement stmt = conn.prepareStatement(
                     "REPLACE INTO Utilisateur (idUtilisateur, nomUtilisateur, nomChercheur, prenomChercheur) VALUES (?,?,?,?)")) {
            stmt.setString(1, id);
            stmt.setString(2, lastName);
            stmt.setString(3, scientistName);
            stmt.setString(4, firstName);
            stmt.executeUpdate();
        }
    }

    public void deleteUser(String id) throws SQLException {
        try (Connection conn = DbConnection.connect();
             PreparedStatement stmt = conn.prepareStatement("DELETE FROM Utilisateur WHERE idUtilisateur = ?")) {
            stmt.setString(1, id);
            stmt.executeUpdate();
        }
    }

    public List getAllUsers() throws SQLException {
        List users = new ArrayList();
        try (Connection conn = DbConnection.connect();
             PreparedStatement stmt = conn.prepareStatement("SELECT idUtilisateur, nomUtilisateur FROM Utilisateur");
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                Map row = new HashMap();
                row.put("idUtilisateur", rs.getString("idUtilisateur"));
                row.put("nomUtilisateur", rs.getString("nomUtilisateur"));
                users.add(row);
            }
        }
        return users;
    }

    public int addSensor(NewSensor sensor, String userId) throws SQLException {
        String sqlSensor = "INSERT INTO Capteur (devEUI, nomCapteur, longitude, latitude, idGroupe) VALUES (?, ?, ?, ?, ?)";
        String sqlLink = "INSERT INTO `est_lie_a` (idCapteur, idUtilisateur) VALUES (?, ?)";

        try (Connection conn = DbConnection.connect()) {
            conn.setAutoCommit(false);
            try {
                int sensorId = -1;
                try (PreparedStatement stmt = conn.prepareStatement(sqlSensor, PreparedStatement.RETURN_GENERATED_KEYS)) {
                    stmt.setString(1, sensor.devEUI());
                    stmt.setString(2, sensor.sensorName());
                    if (sensor.longitude() != null) stmt.setDouble(3, sensor.longitude());
                    else stmt.setNull(3, java.sql.Types.DOUBLE);
                    if (sensor.latitude() != null) stmt.setDouble(4, sensor.latitude());
                    else stmt.setNull(4, java.sql.Types.DOUBLE);
                    if (sensor.groupId() != null) stmt.setInt(5, sensor.groupId());
                    else stmt.setNull(5, java.sql.Types.INTEGER);

                    stmt.executeUpdate();
                    try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                        if (generatedKeys.next()) {
                            sensorId = generatedKeys.getInt(1);
                        }
                    }
                }

                if (sensorId != -1 && userId != null) {
                    try (PreparedStatement linkStmt = conn.prepareStatement(sqlLink)) {
                        linkStmt.setInt(1, sensorId);
                        linkStmt.setString(2, userId);
                        linkStmt.executeUpdate();
                    }
                }
                conn.commit();
                return sensorId;
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
        }
    }

    public void updateSensor(int idCapteur, String nomCapteur, Double longitude, Double latitude) throws SQLException {
        String sql = "UPDATE Capteur SET nomCapteur = ?, longitude = ?, latitude = ? WHERE idCapteur = ?";
        try (Connection conn = DbConnection.connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, nomCapteur);
            
            if (longitude != null) {
                stmt.setDouble(2, longitude);
            } else {
                stmt.setNull(2, java.sql.Types.DOUBLE);
            }

            if (latitude != null) {
                stmt.setDouble(3, latitude);
            } else {
                stmt.setNull(3, java.sql.Types.DOUBLE);
            }

            stmt.setInt(4, idCapteur);
            stmt.executeUpdate();
        }
    }

    public int createGroup(String groupName) throws SQLException {
        String sql = "INSERT INTO Groupe (nomGroupe) VALUES (?)";
        try (Connection conn = DbConnection.connect();
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

    public List getAllGroups() throws SQLException {
        List groups = new ArrayList();
        String sql = "SELECT idGroupe, nomGroupe FROM Groupe";
        try (Connection conn = DbConnection.connect();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                Map group = new HashMap();
                group.put("id", rs.getInt("idGroupe"));
                group.put("name", rs.getString("nomGroupe"));
                groups.add(group);
            }
        }
        return groups;
    }

    private static String formatTimeAgo(LocalDateTime dateHeure) {
        if (dateHeure == null) return "Jamais";
        Duration duration = Duration.between(dateHeure, LocalDateTime.now());
        long seconds = duration.getSeconds();

        if (seconds < 60) return "À l'instant";
        if (seconds < 3600) return "Il y a " + (seconds / 60) + " min";
        if (seconds < 86400) return "Il y a " + (seconds / 3600) + " h";
        return "Il y a " + (seconds / 86400) + " jour(s)";
    }

    public List getSensorsForTable(String userId) throws SQLException {
        List sensors = new ArrayList();

        String query = "SELECT c.idCapteur, c.devEUI, c.nomCapteur, c.pourcentageBatterie, c.pourcentageStockage, " +
                       "c.longitude, c.latitude, g.nomGroupe as searchName " +
                       "FROM Capteur c " +
                       "JOIN `est_lie_a` l ON c.idCapteur = l.idCapteur " +
                       "LEFT JOIN Groupe g ON c.idGroupe = g.idGroupe " +
                       "WHERE l.idUtilisateur = ?";

        try (Connection conn = DbConnection.connect();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Map sensor = new HashMap();
                    int battery = rs.getInt("pourcentageBatterie");
                    int storage = rs.getInt("pourcentageStockage");

                    sensor.put("id", rs.getInt("idCapteur"));
                    sensor.put("devEUI", rs.getString("devEUI"));
                    sensor.put("name", rs.getString("nomCapteur"));
                    sensor.put("battery", battery);
                    sensor.put("storage", storage);
                    sensor.put("searchName", rs.getString("searchName"));
                    
                    Object lon = rs.getObject("longitude");
                    Object lat = rs.getObject("latitude");
                    sensor.put("longitude", lon != null ? lon : "");
                    sensor.put("latitude", lat != null ? lat : "");
                    sensor.put("lastSync", "Il y a 4 min"); // Valeur par défaut propre ou historique

                    if (battery < 20 || storage < 15) {
                        sensor.put("status", "Critique");
                        if (battery < 20 && storage < 15) {
                            sensor.put("alert", "Batterie faible et stockage critique");
                        } else if (battery < 20) {
                            sensor.put("alert", "Batterie faible (<20%)");
                        } else {
                            sensor.put("alert", "Stockage presque saturé");
                        }
                    } else if (battery < 40 || storage < 30) {
                        sensor.put("status", "Avertissement");
                        sensor.put("alert", "Niveau de ressource moyen");
                    } else {
                        sensor.put("status", "Stable");
                        sensor.put("alert", "Aucune");
                    }
                    sensors.add(sensor);
                }
            }
        }
        return sensors;
    }
}