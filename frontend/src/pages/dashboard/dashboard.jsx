import { useState } from "react";
import Sidebar from "../../components/sidebar";
import "./dashboard.css";

const libellesEtat = {
  stable: "Stable",
  avertissement: "Avertissement",
  critique: "Critique",
};

function Dashboard({ onLogout }) {
  const [nomAffichage] = useState(localStorage.getItem("nomUtilisateur") || "Utilisateur");
  const [modalOuverte, setModalOuverte] = useState(false);

  const [nomCapteur, setNomCapteur] = useState("");
  const [localisationCapteur, setLocalisationCapteur] = useState("");
  const [rechercheChoisie, setRechercheChoisie] = useState("");
  const [nouvelleRecherche, setNouvelleRecherche] = useState("");

  const [capteurs] = useState([]);
  const [texteRecherche, setTexteRecherche] = useState("");
  const [filtreEtat, setFiltreEtat] = useState("tous");

  function ouvrirModal() {
    setModalOuverte(true);
  }

  function fermerModal() {
    setModalOuverte(false);
    setNomCapteur("");
    setLocalisationCapteur("");
    setRechercheChoisie("");
    setNouvelleRecherche("");
  }

  function ajouterCapteur(evenement) {
    evenement.preventDefault();
    fermerModal();
  }

  const capteursAffiches = capteurs.filter((capteur) => {
    const correspondTexte = capteur.id
      .toLowerCase()
      .includes(texteRecherche.toLowerCase());
    const correspondEtat = filtreEtat === "tous" || capteur.etat === filtreEtat;
    return correspondTexte && correspondEtat;
  });
  
  return (
      <div className="page-tableau-de-bord">
        {/* 👇 2. Ajoute onLogout={onLogout} ici */}
        <Sidebar pageActive="dashboard" onAjouterBoite={ouvrirModal} onLogout={onLogout} />

        <main className="contenu-principal">
        <h1>Dashboard</h1>

        <div className="barre-outils">
          <input
            type="search"
            placeholder="Rechercher une station, ID..."
            value={texteRecherche}
            onChange={(evenement) => setTexteRecherche(evenement.target.value)}
          />
          <select
            value={filtreEtat}
            onChange={(evenement) => setFiltreEtat(evenement.target.value)}
          >
            <option value="tous">État : Tous</option>
            <option value="stable">Stable</option>
            <option value="avertissement">Avertissement</option>
            <option value="critique">Critique</option>
          </select>
        </div>

        <table>
          <thead>
            <tr>
              <th>Station / ID</th>
              <th>Batterie</th>
              <th>État système</th>
              <th>Alertes</th>
              <th>Action</th>
            </tr>
          </thead>
          <tbody>
            {capteursAffiches.length === 0 ? (
              <tr>
                <td colSpan={5}>Aucun capteur</td>
              </tr>
            ) : (
              capteursAffiches.map((capteur) => (
                <tr key={capteur.id}>
                  <td>{capteur.id}</td>
                  <td>
                    <span className="jauge-batterie">
                      <span
                        className={`jauge-remplissage jauge-${capteur.etat}`}
                        style={{ width: `${capteur.batterie}%` }}
                      ></span>
                    </span>{" "}
                    {capteur.batterie}%
                  </td>
                  <td className={`etat-${capteur.etat}`}>{libellesEtat[capteur.etat]}</td>
                  <td className={capteur.etat === "stable" ? "" : "alerte"}>
                    {capteur.alerte}
                  </td>
                  <td>
                    <button type="button">Détails</button>
                  </td>
                </tr>
              ))
            )}
          </tbody>
        </table>
      </main>

      {modalOuverte && (
        <div className="fond-modal">
          <form className="modal-ajout-capteur" onSubmit={ajouterCapteur}>
            <h2>Ajouter un capteur</h2>

            <label htmlFor="nom-capteur">Nom du capteur</label>
            <input
              id="nom-capteur"
              type="text"
              value={nomCapteur}
              onChange={(evenement) => setNomCapteur(evenement.target.value)}
            />

            <label htmlFor="recherche">Recherche</label>
            <select
              id="recherche"
              value={rechercheChoisie}
              onChange={(evenement) => setRechercheChoisie(evenement.target.value)}
            >
              <option value="">Choisir une recherche</option>
              <option value="__nouvelle__">+ Nouvelle recherche</option>
            </select>

            {rechercheChoisie === "__nouvelle__" && (
              <>
                <label htmlFor="nouvelle-recherche">Nom de la nouvelle recherche</label>
                <input
                  id="nouvelle-recherche"
                  type="text"
                  value={nouvelleRecherche}
                  onChange={(evenement) => setNouvelleRecherche(evenement.target.value)}
                />
              </>
            )}

            <label htmlFor="localisation">Localisation</label>
            <input
              id="localisation"
              type="text"
              value={localisationCapteur}
              onChange={(evenement) => setLocalisationCapteur(evenement.target.value)}
            />

            <div className="boutons-modal">
              <button type="button" onClick={fermerModal}>
                Annuler
              </button>
              <button type="submit">Ajouter</button>
            </div>
          </form>
        </div>
      )}
    </div>
  );
}

export default Dashboard;