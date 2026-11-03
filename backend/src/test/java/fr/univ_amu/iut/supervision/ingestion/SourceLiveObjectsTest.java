package fr.univ_amu.iut.supervision.ingestion;

import static org.assertj.core.api.Assertions.assertThat;

import fr.univ_amu.iut.supervision.Configuration;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicBoolean;
import org.junit.jupiter.api.Test;

/**
 * Tests envelope decoding and the API-key guard for Live Objects, without a
 * real network connection (the host and port are hardcoded in the class, so
 * {@code demarrer} with a valid key isn't testable here).
 */
class SourceLiveObjectsTest {

    private static Configuration configWithApiKey(String key) {
        return new Configuration(0, "", "", "", "", "", 0, "", "", key);
    }

    @Test
    void startingWithoutApiKeyNeverInvokesReceiver() {
        SourceLiveObjects source = new SourceLiveObjects(configWithApiKey(""));
        AtomicBoolean invoked = new AtomicBoolean(false);

        source.start(trame -> invoked.set(true));

        assertThat(invoked).isFalse();
    }

    @Test
    void startingWithNullApiKeyNeverInvokesReceiver() {
        SourceLiveObjects source = new SourceLiveObjects(configWithApiKey(null));
        AtomicBoolean invoked = new AtomicBoolean(false);

        source.start(trame -> invoked.set(true));

        assertThat(invoked).isFalse();
    }

    @Test
    void stoppingWithoutHavingStartedThrowsNothing() {
        new SourceLiveObjects(configWithApiKey("")).stop();
    }

    @Test
    void decodeEnvelopeUsesStreamIdWhenPresent() throws Exception {
        SourceLiveObjects source = new SourceLiveObjects(configWithApiKey("x"));
        byte[] wrapper = """
                {"streamId":"PR-99","value":{"payload":"0AFF"}}
                """.getBytes(StandardCharsets.UTF_8);

        TrameReceived trame = source.decodeWrapper(wrapper);

        assertThat(trame.idBox()).isEqualTo("PR-99");
        assertThat(trame.octets()).containsExactly(0x0A, 0xFF);
        assertThat(trame.recueLe()).isNotNull();
    }

    @Test
    void decodeEnvelopeFallsBackToDevEuiWhenStreamIdAbsent() throws Exception {
        SourceLiveObjects source = new SourceLiveObjects(configWithApiKey("x"));
        byte[] wrapper = """
                {"value":{"devEUI":"PR-77","payload":"01"}}
                """.getBytes(StandardCharsets.UTF_8);

        TrameReceived trame = source.decodeWrapper(wrapper);

        assertThat(trame.idBox()).isEqualTo("PR-77");
        assertThat(trame.octets()).containsExactly(0x01);
    }

    @Test
    void decodeEnvelopeFallsBackToUnknownWhenNoIdentifierPresent() throws Exception {
        SourceLiveObjects source = new SourceLiveObjects(configWithApiKey("x"));
        byte[] wrapper = """
                {"value":{"payload":"01"}}
                """.getBytes(StandardCharsets.UTF_8);

        TrameReceived trame = source.decodeWrapper(wrapper);

        assertThat(trame.idBox()).isEqualTo("inconnu");
    }
}
