package fr.univ_amu.iut.supervision.web;

import fr.univ_amu.iut.supervision.persistance.DataBase;
import io.javalin.Javalin;
import io.javalin.testtools.JavalinTest;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.assertj.core.api.Assertions.assertThat;

public class LoginServiceTest {
    @Test
    public void testCheckVigieToken() {
        LoginService loginService = new LoginService();
        String correctToken = "UUXX88XX2JXXU9XXSSXXNEXX34XX95XX";
        String shortToken = "UUXX88XX2JXXU9XXSSXXNEXX34XX95X";
        String lowAndNumToken = "uuxx88xx2jxxu9xxssxxnexx34xx95xx";
        String noNumToken = "UUXXOOXXDJXXUGXXSSXXNEXXEFXXGFXX";

        assertThat(loginService.checkVigieToken(correctToken)).isTrue();
        assertThat(loginService.checkVigieToken(lowAndNumToken)).isTrue();
        assertThat(loginService.checkVigieToken(noNumToken)).isTrue();
        assertThat(loginService.checkVigieToken(shortToken)).isFalse();
    }
}
