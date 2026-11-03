import { useState } from "react";
import "./login.css";

const BOOKMARKLET_CODE =
  "javascript:(function(){var t=localStorage.getItem('auth-session-token');if(!t){alert('Aucun token : connectez-vous sur Vigie-Chiro puis recliquez ce marque-page.');return;}if(navigator.clipboard){navigator.clipboard.writeText(t).then(function(){alert('Token Vigie-Chiro copie ('+t.length+' caracteres).');},function(){window.prompt('Copiez votre token Vigie-Chiro :',t);});}else{window.prompt('Copiez votre token Vigie-Chiro :',t);}})();";

function Login() {
  const [isTutorialOpen, setIsTutorialOpen] = useState(false);

  function openTutorial() {
    setIsTutorialOpen(true);
  }

  function closeTutorial() {
    setIsTutorialOpen(false);
  }

  return (
    <main className="conteneur-login">
      <section className="login-card">
        <h1>Supervision Passive Recorder</h1>
        <label htmlFor="token">Token (24 characters)</label>
        <input id="token" type="password" maxLength={24} placeholder="••••••••••••••••••••••••" />

        <button type="button">Login</button>
        <button type="button" className="token-link" onClick={openTutorial}>
          Obtenir Token
        </button>
      </section>

      {isTutorialOpen && (
        <div className="tutorial-overlay" onClick={closeTutorial}>
          <div className="tutorial-modal" role="dialog" aria-modal="true" aria-labelledby="tutorial-title" onClick={(event) => event.stopPropagation()}>
            <button type="button" className="tutorial-close" onClick={closeTutorial} aria-label="Fermer" > X </button>

            <h2 id="tutorial-title" className="tutorial-title">
              Supervision Passive Recorder
            </h2>
            <p className="tutorial-intro">Reliez l'application à votre compte Vigie-chiro en trois étapes :</p>
            <p>1- connectez-vous sur vigiechiro.herokuapp.com/</p>
            <p>2- Crée un favori et mets ce code JavaScript dans l'URL du favori.</p>
            <pre className="tutorial-code">
              <code>{BOOKMARKLET_CODE}</code>
            </pre>
            <p> 3- Utilisez le favori, exécutez le code JavaScript et écrivez le token dans votre presse papier. </p>
            <p>4- Coller le token dans le champ “TOKEN (24 CHARACTERS)”.</p>
          </div>
        </div>
      )}
    </main>
  );
}

export default Login;