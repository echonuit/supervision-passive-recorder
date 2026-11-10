import {useState} from "react";
import {use} from "react";
import {useNavigate} from "react-router";

import "./login.css";
import Dashboard from "../dashboard/dashboard.jsx";

const TOKEN_LENGTH = 32;
// Chemin relais : en développement, Vite le redirige vers https://vigiechiro.herokuapp.com
// (voir vite.config.js). Évite un éventuel blocage CORS du navigateur.
const API_VIGIECHIRO = "/vigiechiro";
//const API_VIGIECHIRO = "https://vigiechiro.herokuapp.com";

const BOOKMARKLET_CODE = "javascript:(function(){var t=localStorage.getItem('auth-session-token');if(!t){alert('Aucun token : connectez-vous sur Vigie-Chiro puis recliquez ce marque-page.');return;}if(navigator.clipboard){navigator.clipboard.writeText(t).then(function(){alert('Token Vigie-Chiro copie ('+t.length+' caracteres).');},function(){window.prompt('Copiez votre token Vigie-Chiro :',t);});}else{window.prompt('Copiez votre token Vigie-Chiro :',t);}})();";

/**
 * Basic = base64("identifiant : motdepasse"). Ici le token est l'identifiant et le mot de passe
 * est vide, d'où le " : " final. C'est l'équivalent de `curl -u TOKEN : `.
 */
function enteteBasic(token) {
    return "Basic " + btoa(token + ":");
}

/** Équivalent de : curl -u TOKEN: https://vigiechiro.herokuapp.com/api/v1/moi */
async function recupererProfil(token) {
    const reponse = await fetch(`${API_VIGIECHIRO}/api/v1/moi`, {
        headers: { Authorization: enteteBasic(token) },
    });

    if (!reponse.ok) {
        const erreur = new Error(`HTTP ${reponse.status}`);
        erreur.status = reponse.status;
        throw erreur;
    }

    const type = reponse.headers.get("content-type") ?? "";
    if (!type.includes("application/json")) {
        throw new Error("Réponse inattendue (pas du JSON) : le relais /vigiechiro est-il actif ?");
    }
    return reponse.json();
}

/**
 * Envoie le résultat du profil sous forme d'un json au serveur Javalin (back-end).
 * @param event
 * @param profil Le Profil de l'utilisateur (JSON), obtenu via la fonction recupererProfil().
 * @returns {Promise<void>}
 */
async function envoieProfilBackend(event,profil){
  // Not sure why it is necessary, need to learn
  event.preventDefault()
  try {
    const res = await fetch('/api/sentProfil', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(profil),
    })
  }
  catch (err) {
    console.log('Error: ' + err.message)
  }
}

/**
 * Obtiens les attributs de la session utilisateur.
 * @param event
 * @returns {Promise<void>}
 */
async function ObtientAttributSession(event){
    event.preventDefault()
    try {
        return await fetch('/api/getSessionAttributes')
          .then(res => res.json())
    } catch (err){
        console.log('Error: ' + err.message)
    }
}

/**
 * @param onLogin appelé avec { profil, token } quand Vigie-Chiro a accepté le token.
 *                C'est au composant parent de ranger ces données et de changer d'écran.
 */
function Login({ onLogin = () => {} }) {
    const [token, setToken] = useState("");
    const [enCours, setEnCours] = useState(false);
    const [erreur, setErreur] = useState("");
    const [isTutorialOpen, setIsTutorialOpen] = useState(false);

    async function handleSubmit(event) {
        event.preventDefault();

        // Un copier-coller peut embarquer des espaces autour du token.
        const tokenNettoye = token.trim();
        if (tokenNettoye.length !== TOKEN_LENGTH) {
          setErreur(`Le token fait ${TOKEN_LENGTH} caractères (${tokenNettoye.length} saisis).`);
          return;
        }

        setEnCours(true);
        setErreur("");
        try {
            const raw_profil = await recupererProfil(tokenNettoye);
            const profil = {};
            profil._id = raw_profil._id;
            profil.pseudo = raw_profil.pseudo;
            profil.email = raw_profil.email;
            console.log(profil);
            await envoieProfilBackend(event, profil);
            onLogin({ profil, token: tokenNettoye });
        } catch (e) {
            if (e.status === 401 || e.status === 403) {
                setErreur("Token refusé. Vérifiez qu'il est complet, ou récupérez-en un nouveau. "+ e);
            } else {
                setErreur("Vigie-Chiro est injoignable. Réessayez dans un instant. "+ e);
            }
        } finally {
            setEnCours(false);
        }
    }

    return (
        <main className="conteneur-login">
          <form className="login-card" onSubmit={handleSubmit}>
              <h1>Supervision Passive Recorder</h1>
              <label htmlFor="token">Token ({TOKEN_LENGTH} caractères)</label>
              <input
                  id="token"
                  type="password"
                  autoComplete="off"
                  maxLength={32}
                  placeholder="••••••••••••••••••••••••••••••••••••••••"
                  value={token}
                  onChange={(event) => setToken(event.target.value)}
              />

              {erreur && (
                  <p role="alert" className="login-erreur">
                      {erreur}
                  </p>
              )}

              <button type="submit" disabled={enCours}>
                  {enCours ? "Connexion…" : "Login"}
              </button>
              <button type="button" className="token-link" onClick={() => setIsTutorialOpen(true)}>
                  Obtenir Token
              </button>
          </form>

          {isTutorialOpen && (
              <div className="tutorial-overlay" onClick={() => setIsTutorialOpen(false)}>
                  <div
                      className="tutorial-modal"
                      role="dialog"
                      aria-modal="true"
                      aria-labelledby="tutorial-title"
                      onClick={(event) => event.stopPropagation()}
                  >
                      <button
                          type="button"
                          className="tutorial-close"
                          onClick={() => setIsTutorialOpen(false)}
                          aria-label="Fermer"
                      >
                          X
                      </button>

                      <h2 id="tutorial-title" className="tutorial-title">
                          Supervision Passive Recorder
                      </h2>
                      <p className="tutorial-intro">Reliez l'application à votre compte Vigie-chiro en quatre étapes :</p>
                      <p>1- Connectez-vous sur vigiechiro.herokuapp.com/</p>
                      <p>2- Créez un favori et mettez ce code JavaScript dans l'URL du favori.</p>
                      <pre className="tutorial-code">
            <code>{BOOKMARKLET_CODE}</code>
          </pre>
                      <p>3- Cliquez sur le favori : le token est copié dans votre presse-papiers.</p>
                      <p>4- Collez le token dans le champ « Token ({TOKEN_LENGTH} caractères) ».</p>
                  </div>
              </div>
          )}
      </main>
    );
}

export default Login;