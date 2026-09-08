package fr.univ_amu.iut.supervision.trame;

import java.util.HexFormat;

/** Aller-retour entre octets et représentation hexadécimale, pour les journaux et les vecteurs. */
public final class Hex {

    private static final HexFormat FORMAT = HexFormat.of().withUpperCase();

    private Hex() {}

    public static byte[] versOctets(String hex) {
        return FORMAT.parseHex(hex == null ? "" : hex.strip());
    }

    public static String depuisOctets(byte[] octets) {
        return FORMAT.formatHex(octets);
    }
}
