import { useState } from "react";
import "./sidebar.css";
import photoProfil from "../../asset/pp_placeholder.jpg";

function Sidebar({ pageActive, onAjouterBoite }) {
  const [menuOuvert, setMenuOuvert] = useState(false);

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
            <button type="button">Déconnexion</button>
            <button type="button" className="sidebar__menu-danger">
              Supprimer le compte
            </button>
          </div>
        )}

        <button
          type="button"
          className="sidebar__bouton-utilisateur"
          onClick={() => setMenuOuvert((ouvert) => !ouvert)}
        >
          <img className="sidebar__avatar" src={photoProfil} alt="Photo de profil" />
          <span className="sidebar__nom">I m batman</span>
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