package fr.univ_amu.iut.supervision.web;

/** Logic for the login system **/
public class LoginService {
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
        // return true of the string contains : letter from 'a' to 'z' and 'A' to 'Z', number from 0 to 9, and continue for 32 characters.
        return token.matches(String.format("[a-zA-Z0-9]{%s}", MAX_LENGTH_VIGIETOKEN));
    }

    public void login(String token) {
        if (checkVigieToken(token)) {

        }
    }


}
