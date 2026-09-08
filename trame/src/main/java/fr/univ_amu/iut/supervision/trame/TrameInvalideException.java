package fr.univ_amu.iut.supervision.trame;

/** Levée quand une suite d'octets ne respecte pas le contrat de trame. */
public class TrameInvalideException extends RuntimeException {

    public TrameInvalideException(String message) {
        super(message);
    }
}
