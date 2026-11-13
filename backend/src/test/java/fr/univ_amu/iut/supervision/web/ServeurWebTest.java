package fr.univ_amu.iut.supervision.web;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

import fr.univ_amu.iut.supervision.persistance.DbModel;
import io.javalin.Javalin;
import io.javalin.testtools.HttpClient;
import io.javalin.testtools.JavalinTest;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import org.eclipse.jetty.ee10.servlet.SessionHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class ServeurWebTest {
    private Javalin app;
    private DbModel dbModel;

    private static final String PROFIL_ID_KEY = "_id";
    private static final String PROFIL_EMAIL_KEY = "email";
    private static final String PROFIL_PSEUDO_KEY = "pseudo";

    private Map<String, String> profil =
            Map.of(PROFIL_ID_KEY, "1", PROFIL_EMAIL_KEY, "EMAIL01", PROFIL_PSEUDO_KEY, "PSEUDO01");

    @BeforeEach
    public void setup() {
        dbModel = mock(DbModel.class);
        app = ServeurWeb.creer(dbModel);
    }

    private void login(HttpClient client) {
        assertEquals(201, client.post("/api/sentProfil", profil).code());
    }

    @Test
    public void testRootPathCorrectly() {
        JavalinTest.test(app, (server, client) -> {
            assertThat(client.get("/").body().string()).contains("If you are seeing this, something went wrong...");
        });
    }

    @Test
    public void testApiUsersCorrectly() throws SQLException {
        when(dbModel.getAllUsers()).thenReturn(List.of(Map.of("idUtilisateur", "0", "nomUtilisateur", "Jamal")));
        JavalinTest.test(app, (server, client) -> {
            assertThat(client.get("/api/users").body().string()).contains("Jamal");
        });
    }

    @Test
    public void testApiUserWithoutUser() throws SQLException {
        JavalinTest.test(app, (server, client) -> {
            assertThat(client.get("/api/users").body().string()).contains("[]");
        });
    }

    @Test
    public void testApiSentProfilWhenUserIsNotInDatabase() throws SQLException {
        when(dbModel.userExist(profil.get(PROFIL_ID_KEY))).thenReturn(false);
        JavalinTest.test(app, (server, client) -> {
            assertEquals(201, client.post("/api/sentProfil", profil).code());
        });
    }

    @Test
    public void testApiSentProfilWhenUserIsInDatabase() throws SQLException {
        when(dbModel.userExist(profil.get(PROFIL_ID_KEY))).thenReturn(true);
        JavalinTest.test(app, (server, client) -> {
            assertEquals(201, client.post("/api/sentProfil", profil).code());
        });
    }

    @Test
    public void testApiGetSessionAttributesCorrectly() {
        JavalinTest.test(app, (server, client) -> {
            login(client);
            var res = client.get("/api/getSessionAttributes");
            assertEquals(200, res.code());
            String body = res.body().string();
            assertThat(body).contains(PROFIL_ID_KEY);
            assertThat(body).contains("1");
            assertThat(body).contains("EMAIL01");
        });
    }

    @Test
    public void testApiGetSessionAttributesButSessionVarAreEmpty() {
        JavalinTest.test(app, (server, client) -> {
            var res = client.get("/api/getSessionAttributes");
            assertTrue(res.body().string().isEmpty());
        });
    }

    @Test
    public void testApiDisconnectCorrectly() {
        JavalinTest.test(app, (server, client) -> {
            login(client);
            var res = client.get("/api/disconnect");
            assertEquals(200, res.code());
            assertTrue(res.body().string().isEmpty());
        });
    }

    @Test
    public void testApiDeleteUserCorrectly() throws SQLException {
        when(dbModel.userExist(profil.get(PROFIL_ID_KEY))).thenReturn(false);

        JavalinTest.test(app, (server, client) -> {
            login(client);
            assertEquals(200, client.post("/api/delete/user", profil).code());
            assertEquals("", client.get("/api/getSessionAttributes").body().string());
        });
    }

    @Test
    public void testApiCheckUserCorrectly() throws SQLException {
        JavalinTest.test(app, (server, client) -> {
            login(client);
            assertEquals(200, client.get("/api/checkUser").code());
            verify(dbModel, times(2)).userExist(profil.get(PROFIL_ID_KEY));
        });
    }

    @Test
    public void testCreateSessionHandlerCorrectly() throws SQLException {
        SessionHandler sessionHandler = ServeurWeb.createSessionHandler();
        assertEquals(-1, sessionHandler.getMaxInactiveInterval());
        assertTrue(sessionHandler.isHttpOnly());
    }
}