package fr.univ_amu.iut.supervision.trame;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

/**
 * Valide le codec contre les vecteurs du contrat.
 *
 * <p>Ce test n'a pas vocation à changer quand le contrat évolue : on ajoute
 * des champs au record {@link Trame}, on adapte {@link TrameCodec}, on
 * complète les vecteurs, et le test compare champ par champ.
 */
class TrameCodecTest {

    private static final Path VECTEURS = Path.of("..", "contrat", "vecteurs.json");
    private static final ObjectMapper JSON = new ObjectMapper();

    record Vecteur(String nom, String hex, JsonNode attendu, boolean invalide) {
        @Override
        public String toString() {
            return nom;
        }
    }

    static Stream<Vecteur> vecteurs() throws IOException {
        JsonNode racine = JSON.readTree(Files.readString(VECTEURS));
        List<Vecteur> liste = new ArrayList<>();
        for (JsonNode v : racine.get("vecteurs")) {
            liste.add(new Vecteur(
                    v.get("nom").asText(),
                    v.get("hex").asText(),
                    v.get("attendu"),
                    v.path("invalide").asBoolean(false)));
        }
        return liste.stream();
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("vecteurs")
    @DisplayName("décodage conforme au vecteur")
    void decode(Vecteur v) {
        byte[] octets = Hex.versOctets(v.hex());
        if (v.invalide()) {
            assertThrows(TrameInvalideException.class, () -> TrameCodec.decoder(octets));
            return;
        }
        Trame trame = TrameCodec.decoder(octets);
        Map<?, ?> obtenu = JSON.convertValue(trame, Map.class);
        Map<?, ?> attendu = JSON.convertValue(v.attendu(), Map.class);
        assertEquals(attendu, obtenu, "champs décodés");
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("vecteurs")
    @DisplayName("aller-retour encoder puis décoder")
    void allerRetour(Vecteur v) {
        assumeTrue(!v.invalide());
        byte[] octets = Hex.versOctets(v.hex());
        Trame trame = TrameCodec.decoder(octets);
        assertArrayEquals(octets, TrameCodec.encoder(trame), "encodage identique aux octets d'origine");
    }
}
