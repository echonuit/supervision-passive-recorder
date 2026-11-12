import { useState } from "react";
import "./sidebar.css";
// On garde le placeholder en cas d'erreur ou d'absence de photo
import photoProfilPlaceholder from "../asset/pp_placeholder.jpg";

function Sidebar({ pageActive, onAjouterBoite, onLogout }) {
  const [menuOuvert, setMenuOuvert] = useState(false);

  // Récupération des données sauvegardées
  const [nomAffichage] = useState(localStorage.getItem("nomUtilisateur") || "Utilisateur");
  const idUtilisateur = localStorage.getItem("idUtilisateur");

  // Chemin mis à jour vers le dossier public/pp/
  const photoUrl = idUtilisateur ? `/pp/${idUtilisateur}.jpg` : photoProfilPlaceholder;

  /**
   * Handle the logout logic front-end side
   */
  async function handleLogoutClick() {
    try {
      await fetch('/api/disconnect', {
        method: 'GET',
        credentials: 'include' // 👇 OBLIGATOIRE pour que Javalin reconnaisse la session
      });
      console.log("LOG : End session");
    } catch (err) {
      console.error('Error: ' + err.message);
    } finally {
      onLogout();
    }
  }

  /**
   * Handle the account deletion logic front-end side
   */
  async function handleAccountDeletion() {
    const confirmation = window.confirm("Êtes-vous sûr de vouloir supprimer votre compte ? Cette action est irréversible.");
    if (!confirmation) return;

    try {
      // On rajoute le corps JSON (comme l'a conseillé ton collègue avec envoieProfilBackend)
      // ET on rajoute credentials: 'include' pour la session
      const res = await fetch('/api/delete/user', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        credentials: 'include', // 👇 OBLIGATOIRE pour la session Javalin
        body: JSON.stringify({ _id: idUtilisateur }),
      });

      if (res.ok) {
        console.log("LOG : Demande de suppression envoyée au serveur");
      } else {
        console.error("Erreur côté serveur :", res.status);
      }
    } catch (err) {
      console.error('Erreur réseau :', err.message);
    } finally {
      onLogout();
    }
  }

  return (
      <aside className="sidebar">
        <p className="sidebar__marque">Passive Recorder</p>

        <nav>
          <p className="sidebar__titre">Navigation</p>
          <ul className="sidebar__liste">
            <li>
              <button
                  type="button"
                  className={`sidebar__item ${pageActive === "dashboard" ? "sidebar__item--actif" : ""}`}
              >
                Dashboard
              </button>
            </li>
            <li>
              <button type="button" className="sidebar__item" onClick={onAjouterBoite}>
                Ajouter une boîte
              </button>
            </li>
          </ul>

          <p className="sidebar__titre">Administration</p>
          <ul className="sidebar__liste">
            <li>
              <button
                  type="button"
                  className={`sidebar__item ${pageActive === "gestion" ? "sidebar__item--actif" : ""}`}
              >
                <svg
                    viewBox="0 0 24 24"
                    width="16"
                    height="16"
                    fill="none"
                    stroke="currentColor"
                    strokeWidth="2"
                    strokeLinecap="round"
                    strokeLinejoin="round"
                    aria-hidden="true"
                >
                  <circle cx="9" cy="8" r="4" />
                  <path d="M2 21v-1a6 6 0 0 1 6-6h2" />
                  <circle cx="18" cy="17" r="2.5" />
                  <path d="M18 12.5v1M18 20.5v1M13.5 17h1M21.5 17h1" />
                </svg>
                Gestion &amp; Droits
              </button>
            </li>
          </ul>
        </nav>

        <div className="sidebar__utilisateur">
          {menuOuvert && (
              <div className="sidebar__menu">
                {/* Appel de handleLogoutClick au lieu de onLogout direct */}
                <button type="button" onClick={handleLogoutClick}>
                  Déconnexion
                </button>

                {/* Appel de handleAccountDeletion ici */}
                <button type="button" className="sidebar__menu-danger" onClick={handleAccountDeletion}>
                  Supprimer le compte
                </button>
              </div>
          )}

          <button
              type="button"
              className="sidebar__bouton-utilisateur"
              onClick={() => setMenuOuvert((ouvert) => !ouvert)}
          >
            <img
                className="sidebar__avatar"
                src={photoUrl}
                alt="Photo de profil"
                onError={(e) => {
                  e.target.onerror = null;
                  e.target.src = photoProfilPlaceholder;
                }}
            />
            <span className="sidebar__nom">{nomAffichage}</span>
            <svg
                className={`sidebar__chevron ${menuOuvert ? "sidebar__chevron--ouvert" : ""}`}
                viewBox="0 0 24 24"
                width="18"
                height="18"
                fill="none"
                stroke="currentColor"
                strokeWidth="2.5"
                strokeLinecap="round"
                strokeLinejoin="round"
                aria-hidden="true"
            >
              <path d="M6 15l6-6 6 6" />
            </svg>
          </button>
        </div>
      </aside>
  );
}

export default Sidebar;