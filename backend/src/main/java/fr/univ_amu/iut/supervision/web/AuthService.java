package fr.univ_amu.iut.supervision.web;

/** Logic for the login system **/
public class AuthService {
    private static String vigieToken = null;
    private static final int MAX_LENGTH_VIGIETOKEN = 32;

    /**
     * Method that check if the given token follow the characteristic of the token given by the website https://vigiechiro.herokuapp.com/.
     * The characteristic are :
     *  - Is it null ?
     *  - The length of the token is 32.
     * @param token The token given by the website https://vigiechiro.herokuapp.com/.
     * @return True if token follow the characteristic, false otherwise.
     */
    public boolean checkVigieToken(String token) {
        if (vigieToken == null) {
            return false;
        }
        if (token.length() == MAX_LENGTH_VIGIETOKEN) {
            // return true of the string contains : letter from 'a' to 'z' and 'A' to 'Z', number from 0 to 9, and continue for 32 characters.
            return token.matches("[a-zA-Z0-9]{32}");
        }
        return false;
    }


}
