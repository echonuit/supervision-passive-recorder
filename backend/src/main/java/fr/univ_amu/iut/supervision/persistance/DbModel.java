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

    // --- GESTION DES UTILISATEURS ---

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

    public List> getAllUsers() throws SQLException {
        List> users = new ArrayList<>();
        try (Connection conn = DbConnection.connect();
             PreparedStatement stmt = conn.prepareStatement("SELECT idUtilisateur, nomUtilisateur FROM Utilisateur");
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                Map<String, Object> row = new HashMap<>();
                row.put("idUtilisateur", rs.getString("idUtilisateur"));
                row.put("nomUtilisateur", rs.getString("nomUtilisateur"));
                users.add(row);
            }
        }
        return users;
    }

    // --- GESTION DES CAPTEURS ET GROUPES ---

    public int addSensor(NewSensor sensor) throws SQLException {
        String sql = "INSERT INTO Capteur (devEUI, nomCapteur, longitude, latitude, idGroupe) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DbConnection.connect();
             PreparedStatement stmt = conn.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, sensor.devEUI());
            stmt.setString(2, sensor.sensorName());
            
            if (sensor.longitude() != null) {
                stmt.setDouble(3, sensor.longitude());
            } else {
                stmt.setNull(3, java.sql.Types.DOUBLE);
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

    // --- DASHBOARD ET SYNCHRONISATION ---

    private static String formatTimeAgo(LocalDateTime dateHeure) {
        if (dateHeure == null) {
            return "Jamais";
        }
        Duration duration = Duration.between(dateHeure, LocalDateTime.now());
        long seconds = duration.getSeconds();

        if (seconds < 60) {
            return "À l'instant";
        } else if (seconds < 3600) {
            return "Il y a " + (seconds / 60) + " min";
        } else if (seconds < 86400) {
            return "Il y a " + (seconds / 3600) + " h";
        } else {
            return "Il y a " + (seconds / 86400) + " jour(s)";
        }
    }

    public List> getSensorsForTable(int accountId) throws SQLException {
        List> sensors = new ArrayList<>();

        String query = """
            SELECT c.idCapteur, c.devEUI, c.nomCapteur, c.pourcentageBatterie, c.pourcentageStockage, 
                   g.nomGroupe as searchName, MAX(h.dateHeure) as derniereSynchro
            FROM Capteur c
            LEFT JOIN Groupe g ON c.idGroupe = g.idGroupe
            LEFT JOIN Historique h ON c.idCapteur = h.idCapteur
            GROUP BY c.idCapteur, c.devEUI, c.nomCapteur, c.pourcentageBatterie, c.pourcentageStockage, g.nomGroupe
        """;

        try (Connection conn = DbConnection.connect();
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                Map<String, Object> sensor = new HashMap<>();

                int battery = rs.getInt("pourcentageBatterie");
                int storage = rs.getInt("pourcentageStockage");

                sensor.put("id", rs.getInt("idCapteur"));
                sensor.put("devEUI", rs.getString("devEUI"));
                sensor.put("name", rs.getString("nomCapteur"));
                sensor.put("battery", battery);
                sensor.put("storage", storage);
                sensor.put("searchName", rs.getString("searchName"));
                
                Timestamp lastSync = rs.getTimestamp("derniereSynchro");
                LocalDateTime derniereSynchro = (lastSync != null) ? lastSync.toLocalDateTime() : null;
                sensor.put("lastSync", formatTimeAgo(derniereSynchro));

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
        return sensors;
    }
}