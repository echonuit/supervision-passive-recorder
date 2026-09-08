package fr.univ_amu.iut.supervision.ingestion;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import org.junit.jupiter.api.Test;

class TrameRecueTest {

    @Test
    void porteLIdentifiantLInstantEtLesOctets() {
        Instant maintenant = Instant.now();
        TrameRecue trame = new TrameRecue("PR-01", maintenant, new byte[] {0x01, 0x02});

        assertThat(trame.idBoitier()).isEqualTo("PR-01");
        assertThat(trame.recueLe()).isEqualTo(maintenant);
        assertThat(trame.octets()).containsExactly(0x01, 0x02);
    }
}
