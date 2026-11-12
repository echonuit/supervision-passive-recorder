package fr.univ_amu.iut.supervision.persistance;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Class used to run SQL query and manipulate the database.
 */
public class DbModel {

    /**
     * Class Attribute that represent the connection to the database
     */
    private final DbConnection dbConnection;

    /**
     * Constructor for this class, create a DbConnection object automatically.
     */
    public DbModel() {
        this.dbConnection = new DbConnection();
    }

    // Used for unit test
    /**
     * Constructor for this class, demand a DbConnection object.
     * @param dbConnection A DbConnection object.
     */
    public DbModel(DbConnection dbConnection) {
        this.dbConnection = dbConnection;
    }

    /**
     * Getter for the dbConnection class attribute.
     * @return A dbConnection object.
     */
    public DbConnection getDbConnection() {
        return dbConnection;
    }

    /**
     * Check if the user (identified by his id) exist in the database.
     * @param id The id of the user, alphanumerical string, unique and of maximum 100 characters.
     * @return True of the user exist in the database, false otherwise.
     * @throws SQLException
     */
    public boolean userExist(String id) throws SQLException {
        try (Connection conn = dbConnection.connect()) {
            PreparedStatement stmt = conn.prepareStatement(
                    "SELECT idUtilisateur FROM Utilisateur WHERE idUtilisateur = ?;");
            stmt.setString(1, id);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return true;
            } else return false;
        }
    }

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
            PreparedStatement stmt = conn.prepareStatement(
                    "REPLACE INTO Utilisateur (idUtilisateur, nomUtilisateur, nomChercheur, prenomChercheur) VALUES (?,?,?,?)");
            stmt.setString(1, id);
            stmt.setString(2, lastName);
            stmt.setString(3, ScientistName);
            stmt.setString(4, firstName);
            System.out.println("LOG : " + stmt);
            stmt.executeUpdate();
        }
    }

    /**
     * Delete a user from the database.
     * @param id The id of the user, alphanumerical string, unique and of maximum 100 characters
     * @throws SQLException
     */
    public void deleteUser(String id) throws SQLException {
        try (Connection conn = dbConnection.connect()) {
            PreparedStatement stmt = conn.prepareStatement("DELETE FROM Utilisateur WHERE idUtilisateur = ?");
            stmt.setString(1, id);
            System.out.println("LOG : " + stmt);
            stmt.executeUpdate();
        }
    }

    /**
     * Return a list of all the users.
     * @return A list of pair.
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