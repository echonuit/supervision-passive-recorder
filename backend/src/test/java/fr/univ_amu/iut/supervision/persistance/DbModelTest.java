package fr.univ_amu.iut.supervision.persistance;

import static org.junit.jupiter.api.Assertions.*;

import com.zaxxer.hikari.*;
import java.sql.*;
import org.junit.jupiter.api.*;
import org.testcontainers.containers.MariaDBContainer;
import org.testcontainers.junit.jupiter.*;

/**
 * Class used to do unit test on the DbConnection and DbModel class.
 * WARNING : require docker, at the start an image of mariadb will be pulled.
 */
@Testcontainers
public class DbModelTest {
    @Container
    static final MariaDBContainer MARIADB = new MariaDBContainer("mariadb:11");

    static DbModel dbModel;

    // Used to override the method connect, used at the start of each DbModel method that create and execute a SQL
    // query.
    // Necessary because we want to connect to a database that is in the memory, not the real one.
    static class TestDbConnection extends DbConnection {
        @Override
        public Connection connect() throws SQLException {
            String host = MARIADB.getHost() + ":" + MARIADB.getFirstMappedPort();
            return connect(host, MARIADB.getDatabaseName(), MARIADB.getUsername(), MARIADB.getPassword());
        }
    }

    @BeforeAll
    static void init() throws SQLException {
        // create a Dbmodel object, use TestDbConnection
        dbModel = new DbModel(new TestDbConnection());
        try (Connection conn = new TestDbConnection().connect()) {
            Statement statement = conn.createStatement();
            statement.execute("""
					CREATE TABLE Utilisateur(
					idUtilisateur VARCHAR(100),
					nomUtilisateur VARCHAR(100) NOT NULL,
					nomChercheur VARCHAR(50),
					prenomChercheur VARCHAR(50),
					PRIMARY KEY(idUtilisateur));
					""");
        }
        // Asset to populate the Table :
        //		INSERT INTO Utilisateur (idUtilisateur, nomUtilisateur, nomChercheur, prenomChercheur) VALUES
        //				(1, 'normaline',   'dumont',   'aline'),
        //				(2, 'daltoline',   'dunoir',   'alinevoit'),
        //		(3, 'sourdaline',  'nentend',  'aline'),
        //		(4, 'aveuglaline', 'voitmal',  'aline'),
        //		(0, 'jamal',       'jamal',    'jamal');
    }

    //	++++++++++ RegisterUser() ++++++++++
    @Test
    void TestRegisterUserThrowsWhenIdIsTooLong() {
        String tooLongId = "a".repeat(101);
        assertThrows(
                SQLException.class, () -> dbModel.registerUser(tooLongId, "LAST-NAME01", "SC-NAME01", "FIRST-NAME01"));
    }

    @Test
    void TestRegisterUserCorrectly() {
        assertDoesNotThrow(() -> dbModel.registerUser("1", "LAST-NAME01", "SC-NAME01", "FIRST-NAME01"));
    }

    @Test
    void TestRegisterUserThrowsWhenFielEmpty() {
        // NOTE : at the moment in the Utilisateur Table only the fiel nomUtilisateur and idUtilisateur require a no
        // null value
        assertThrows(SQLException.class, () -> dbModel.registerUser("1", null, "SC-NAME01", "FIRST-NAME01"));
    }

    //	++++++++++ getAllUser() ++++++++++
    @Test
    void testGetAllUsersReturnEmptyIfTableEmpty() throws SQLException {
        assertTrue(dbModel.getAllUsers().isEmpty());
    }

    @Test
    void testGetAllUsers() throws SQLException {
        dbModel.registerUser("1", "LAST-NAME01", "SC-NAME01", "FIRST-NAME01");
        dbModel.registerUser("2", "LAST-NAME02", "SC-NAME02", "FIRST-NAME02");
        assertFalse(dbModel.getAllUsers().isEmpty());
        dbModel.deleteUser("1");
        dbModel.deleteUser("2");
    }

    //	++++++++++ userExist() ++++++++++
    @Test
    void testUserExistCorrectly() throws SQLException {
        dbModel.registerUser("1", "LAST-NAME01", "SC-NAME01", "FIRST-NAME01");
        assertTrue(dbModel.userExist("1"));
    }

    @Test
    void testUserExistThrowsWhenUserDoesNotExist() throws SQLException {
        assertFalse(dbModel.userExist("1"));
    }

    //	++++++++++ deleteUser() ++++++++++
    @Test
    void testDeleteUserCorrectly() throws SQLException {
        dbModel.registerUser("1", "LAST-NAME01", "SC-NAME01", "FIRST-NAME01");
        assertDoesNotThrow(() -> dbModel.deleteUser("1"));
    }
    // NOTE : Deleting an element that does not exist will yield the same output as if we delete an element that exist.

}
