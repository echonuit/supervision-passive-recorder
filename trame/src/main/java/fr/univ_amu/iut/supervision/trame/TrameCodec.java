package fr.univ_amu.iut.supervision.trame;

/**
 * Passage entre la suite d'octets transmise par radio et {@link Trame}.
 *
 * <p>Le codec est écrit deux fois : ici en Java pour le serveur et le
 * simulateur, et en C++ dans le firmware. Les deux sont validés par les
 * mêmes vecteurs, dans {@code contrat/vecteurs.json}. C'est ce qui garantit
 * qu'ils lisent les mêmes octets de la même façon.
 *
 * <p>Convention : gros-boutiste pour les champs sur plusieurs octets.
 */
public final class TrameCodec {

    private TrameCodec() {}

    public static Trame decoder(byte[] octets) {
        if (octets == null || octets.length < 1) {
            throw new TrameInvalideException("trame vide");
        }
        int version = octets[0] & 0xFF;
        return new Trame(version);
    }

    public static byte[] encoder(Trame trame) {
        return new byte[] {(byte) trame.version()};
    }
}
