package fr.univ_amu.iut.supervision.web;
import fr.univ_amu.iut.supervision.persistance.DbModel;

import io.javalin.Javalin;
import io.javalin.testtools.JavalinTest;
import org.eclipse.jetty.ee10.servlet.SessionHandler;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import io.javalin.testtools.HttpClient;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

public class ServeurWebTest {
	private Javalin app;
	private DbModel dbModel;

	private Map<String,String> profil = Map.of(
			"_id", "1", "email", "EMAIL01", "pseudo", "PSEUDO01"
	);

	@BeforeEach
	public void setup (){
		dbModel = mock(DbModel.class);
		app =  ServeurWeb.creer(dbModel);
	}

	/**
	 * Simulate the React+Vite front-end server sending the profil, write them in the session
	 * @param client The http client used to write the information in his session variable
	 */
	private void login(HttpClient client) {
		assertEquals(201, client.post("/api/sentProfil", profil).code());
	}

	@Test
	public void testRootPathCorrectly() {
		JavalinTest.test(app, (server, client) -> {
			assertThat(client.get("/").body().string()).contains("If you are seeing this, something went wrong...");
		});
	}

//	++++++++++ /api/users ++++++++++
	@Test
	public void testApiUsersCorrectly() throws SQLException {
//		["{nomUtilisateur=jamal, idUtilisateur=0}, {nomUtilisateur=normaline, idUtilisateur=1}, {nomUtilisateur=daltoline, idUtilisateur=2}, {nomUtilisateur=sourdaline, idUtilisateur=3}, {nomUtilisateur=aveuglaline, idUtilisateur=4}"]
		when(dbModel.getAllUsers()).thenReturn(List.of(
				Map.of("0","Jamal")
		));
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

//	++++++++++ /api/sentProfil ++++++++++
	@Test
	public void testApiSentProfilWhenUserIsNotInDatabase() throws SQLException {
		when(dbModel.userExist(profil.get("_id"))).thenReturn(false);
		JavalinTest.test(app, (server, client) -> {
			assertEquals(201, client.post("/api/sentProfil", profil).code());
		});
	}
	@Test
	public void testApiSentProfilWhenUserIsInDatabase() throws SQLException {
		when(dbModel.userExist(profil.get("_id"))).thenReturn(true);
		JavalinTest.test(app, (server, client) -> {
			assertEquals(201, client.post("/api/sentProfil", profil).code());
		});
	}

//	++++++++++ /api/getSessionAttributes ++++++++++
	@Test
	public void testApiGetSessionAttributesCorrectly() {
		JavalinTest.test(app, (server, client) -> {
			login(client);
			var res = client.get("/api/getSessionAttributes");
			System.out.println(res.body().string());
			assertEquals(200, res.code());
			assertThat(res.body().string().contains("\"_id\"=\"1\""));
			assertThat(res.body().string().contains("\"pseudo\"=\"PSEUDO01\""));
			assertThat(res.body().string().contains("\"email\"=\"EMAIL01\""));
		});
	}
	@Test
	public void testApiGetSessionAttributesButSessionVarAreEmpty(){
		JavalinTest.test(app, (server, client) -> {
			var res = client.get("/api/getSessionAttributes");
			assertTrue(res.body().string().isEmpty());
		});
	}

//	++++++++++ /api/disconnect ++++++++++
	@Test
	public void testApiDisconnectCorrectly() {
		JavalinTest.test(app, (server, client) -> {
			login(client);
			var res = client.get("/api/disconnect");
			assertEquals(200, res.code());
			assertTrue(res.body().string().isEmpty());
		});
	}
//	++++++++++ /api/delete/user ++++++++++
	@Test
	public void testApiDeleteUserCorrectly() throws SQLException {
		when(dbModel.userExist(profil.get("_id"))).thenReturn(false);

		JavalinTest.test(app, (server, client) -> {
			login(client);
			assertEquals(200, client.post("/api/delete/user", profil).code());
			assertEquals("", client.get("/api/getSessionAttributes").body().string());
		});
	}
// ++++++++++ /api/checkUser ++++++++++
	@Test
	public void testApiCheckUserCorrectly() throws SQLException {
		JavalinTest.test(app, (server, client) -> {
			login(client);
			// since /api/checkUser will only for the moment print the output we only need to check if the SQL query has been executed
			assertEquals(200, client.get("/api/checkUser").code());
			// Two 2 time userExis() is called when /api/sentProfil and /api/checkUser
			verify(dbModel, times(2)).userExist(profil.get("_id"));
		});
	}

// ++++++++++ createSessionHandler() ++++++++++
	@Test
	public void testCreateSessionHandlerCorrectly() throws SQLException {
		SessionHandler sessionHandler = ServeurWeb.createSessionHandler();
		assertEquals(-1, sessionHandler.getMaxInactiveInterval());
		assertTrue(sessionHandler.isHttpOnly());
	}
}
