package fr.univ_amu.iut.supervision.persistance;

import static org.junit.jupiter.api.Assertions.*;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.MariaDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers(disabledWithoutDocker = true)
public class DbModelTest {

    @Container
    static final MariaDBContainer<?> MARIADB = new MariaDBContainer<>("mariadb:11");

    static DbModel dbModel;

    private static final String ID_STUB01 = "1";
    private static final String LAST_NAME_STUB01 = "LAST-NAME01";
    private static final String FIRST_NAME_STUB01 = "FIRST-NAME01";
    private static final String SC_NAME_STUB01 = "SC-NAME01";

    static class TestDbConnection extends DbConnection {

        @Override
        public Connection connect() throws SQLException {
            String host = MARIADB.getHost() + ":" + MARIADB.getFirstMappedPort();

            return connect(host, MARIADB.getDatabaseName(), MARIADB.getUsername(), MARIADB.getPassword());
        }
    }

    @BeforeAll
    static void init() throws SQLException {
        dbModel = new DbModel(new TestDbConnection());

        try (Connection conn = new TestDbConnection().connect();
                Statement statement = conn.createStatement()) {

            statement.execute("""
                    CREATE TABLE Utilisateur (
                        idUtilisateur VARCHAR(100),
                        nomUtilisateur VARCHAR(100) NOT NULL,
                        nomChercheur VARCHAR(50),
                        prenomChercheur VARCHAR(50),
                        PRIMARY KEY (idUtilisateur)
                    )
                    """);
        }
    }

    @BeforeEach
    void cleanDatabase() throws SQLException {
        try (Connection conn = new TestDbConnection().connect();
                Statement statement = conn.createStatement()) {

            statement.executeUpdate("DELETE FROM Utilisateur");
        }
    }

    @Test
    void TestRegisterUserThrowsWhenIdIsTooLong() {
        String tooLongId = "a".repeat(101);

        assertThrows(
                SQLException.class,
                () -> dbModel.registerUser(tooLongId, LAST_NAME_STUB01, SC_NAME_STUB01, FIRST_NAME_STUB01));
    }

    @Test
    void TestRegisterUserCorrectly() {
        assertDoesNotThrow(() -> dbModel.registerUser(ID_STUB01, LAST_NAME_STUB01, SC_NAME_STUB01, FIRST_NAME_STUB01));
    }

    @Test
    void TestRegisterUserThrowsWhenFieldEmpty() {
        assertThrows(
                SQLException.class, () -> dbModel.registerUser(ID_STUB01, null, SC_NAME_STUB01, FIRST_NAME_STUB01));
    }

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

    @Test
    void testUserExistCorrectly() throws SQLException {
        dbModel.registerUser(ID_STUB01, LAST_NAME_STUB01, SC_NAME_STUB01, FIRST_NAME_STUB01);

        assertTrue(dbModel.userExist(ID_STUB01));
    }

    @Test
    void testUserExistThrowsWhenUserDoesNotExist() throws SQLException {
        assertFalse(dbModel.userExist("1"));
    }

    @Test
    void testDeleteUserCorrectly() throws SQLException {
        dbModel.registerUser(ID_STUB01, LAST_NAME_STUB01, SC_NAME_STUB01, FIRST_NAME_STUB01);

        assertDoesNotThrow(() -> dbModel.deleteUser(ID_STUB01));
        assertFalse(dbModel.userExist(ID_STUB01));
    }
}
