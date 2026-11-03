package fr.univ_amu.iut.supervision.ingestion;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import org.junit.jupiter.api.Test;

class TrameReceivedTest {

    @Test
    void porteLIdentifiantLInstantEtLesOctets() {
        Instant maintenant = Instant.now();
        TrameReceived trame = new TrameReceived("PR-01", maintenant, new byte[] {0x01, 0x02});

        assertThat(trame.idBox()).isEqualTo("PR-01");
        assertThat(trame.recueLe()).isEqualTo(maintenant);
        assertThat(trame.octets()).containsExactly(0x01, 0x02);
    }
}
