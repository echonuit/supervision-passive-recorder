package fr.univ_amu.iut.supervision.web;
import fr.univ_amu.iut.supervision.persistance.DbModel;

import io.javalin.Javalin;

import io.javalin.testtools.JavalinTest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

public class ServeurWebTest {
	private Javalin app;
	private DbModel dbModel;

	@BeforeEach
	public void setup (){
		dbModel = mock(DbModel.class);
		app =  ServeurWeb.creer(dbModel);
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
	public void testApiSentProfilCorrectly() throws SQLException {
		// TODO
	}

//	++++++++++ /api/getSessionAttributes ++++++++++
	@Test
	public void testApiGetSessionAttributesCorrectly() throws SQLException {
//		JavalinTest.test(app, (server, client) -> {
//
//		})
	}
}
