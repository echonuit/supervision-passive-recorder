package fr.univ_amu.iut.supervision.web;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

public class LoginServiceTest {
    @Test
    public void testCheckVigieToken() {
        LoginService loginService = new LoginService();
        String correctToken = "UUXX88XX2JXXU9XXSSXXNEXX34XX95XX";
        String shortToken = "UUXX88XX2JXXU9XXSSXXNEXX34XX95X";
        String lowAndNumToken = "uuxx88xx2jxxu9xxssxxnexx34xx95xx";
        String noNumToken = "UUXXOOXXDJXXUGXXSSXXNEXXEFXXGFXX";
        String noAllowedCharToken = "&&X88XX2JXXU9XXSSXXNEXX34XX95XX";

        assertThat(loginService.checkVigieToken(correctToken)).isTrue();
        assertThat(loginService.checkVigieToken(lowAndNumToken)).isTrue();
        assertThat(loginService.checkVigieToken(noNumToken)).isTrue();
        assertThat(loginService.checkVigieToken(shortToken)).isFalse();
        assertThat(loginService.checkVigieToken(noAllowedCharToken)).isFalse();
    }
}
