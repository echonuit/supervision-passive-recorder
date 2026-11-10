package fr.univ_amu.iut.supervision.persistance;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DbModel {

    private final DbConnection dbConnection = new DbConnection();

//    public List<Media> getAllMedia() throws SQLException {
//        List<Media> result = new ArrayList<>();
//
//        try (Connection conn = dbConnection.connect();
//             PreparedStatement stmt = conn.prepareStatement("SELECT * FROM media");
//             ResultSet rs = stmt.executeQuery()) {
//
//            while (rs.next()) {
//                result.add(new Media(
//                    rs.getInt("id"),
//                    rs.getString("title"),
//                    rs.getString("type")
//                ));
//            }
//        }
//        return result;
//    }

    public void registerUser(String username, String email, String _id) throws SQLException {
        try (Connection conn = dbConnection.connect()) {
            PreparedStatement stmt = conn.prepareStatement("");
            ResultSet rs = stmt.executeQuery();
        }
    }

    /**
     * Return a list of all the users
     * @return A list of pair
     * @throws SQLException
     */
    public List<Map<String, Object>> getAllUsers() throws SQLException {
        List<Map<String, Object>> users = new ArrayList<>();
        try (Connection conn = dbConnection.connect()) {
            PreparedStatement stmt = conn.prepareStatement("SELECT idUtilisateur, nomUtilisateur FROM Utilisateur");
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Map<String, Object> row = new HashMap<>();
                row.put("idUtilisateur", rs.getString("idUtilisateur"));
                row.put("nomUtilisateur", rs.getString("nomUtilisateur"));
                users.add(row);
            }
        }
        return users;
    }
}