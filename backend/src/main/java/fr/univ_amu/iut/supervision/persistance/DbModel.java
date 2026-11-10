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

    /**
     * Register the user in the database.
     * @param id The id of the user, alphanumerical string, unique and of maximum 100 characters.
     * @param lastName His last name, alphanumerical string of maximum 100 characters.
     * @param ScientistName His scientist name, alphanumerical string of maximum 50 characters.
     * @param firstName His first name, alphanumerical string of maximum 50 characters.
     * @throws SQLException
     */
    public void registerUser(String id,String lastName, String ScientistName, String firstName) throws SQLException {
        try (Connection conn = dbConnection.connect()) {
            // TODO : report field in the Utilisateur Table that are
            PreparedStatement stmt = conn.prepareStatement(
                    "REPLACE INTO Utilisateur (idUtilisateur, nomUtilisateur, nomChercheur, prenomChercheur) VALUES (?,?,?,?)");
            stmt.setString(1, id);
            stmt.setString(2, lastName);
            stmt.setString(3, ScientistName);
            stmt.setString(4, firstName);
            System.out.println(stmt);
            stmt.executeUpdate();
        }
    }

    /**
     * Delete a user from the database.
     * @param id The id of the user, alphanumerical string, unique and of maximum 100 characters
     * @throws SQLException
     */
    public void deleteUser(String id) throws SQLException {
        // TODO : test it
        try (Connection conn = dbConnection.connect()) {
            PreparedStatement stmt = conn.prepareStatement("DELETE FROM Utilisateur WHERE idUtilisateur = ?");
            stmt.setString(1, id);
            System.out.println(stmt);
            stmt.executeUpdate();
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