import { useState } from "react";
import Sidebar from "../../components/sidebar";
import CarteCapteurs from "../../components/carte/carte";
import { LIBELLES_ETAT } from "../../constantes/etats";
import { useCapteurs } from "../../hooks/useCapteurs";
import "./dashboard.css";

function Dashboard() {
  const [modalOuverte, setModalOuverte] = useState(false);

  const [nomCapteur, setNomCapteur] = useState("");
  const [localisationCapteur, setLocalisationCapteur] = useState("");
  const [rechercheChoisie, setRechercheChoisie] = useState("");
  const [nouvelleRecherche, setNouvelleRecherche] = useState("");

  const { capteurs, chargement, erreur } = useCapteurs();
  const [selection, setSelection] = useState(null);
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

  // Un nouvel objet à chaque demande : la carte se recentre même sur le capteur déjà sélectionné.
  function selectionner(id) {
    setSelection({ id });
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
      <Sidebar pageActive="dashboard" onAjouterBoite={ouvrirModal} />

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

        {/* Carte : elle remplit ce panneau. Pour la déplacer ou la redimensionner, voir
            components/carte/carte.jsx et --hauteur-carte dans dashboard.css. */}
        <section className="panneau-carte" aria-label="Carte des capteurs">
          <CarteCapteurs
            capteurs={capteursAffiches}
            chargement={chargement}
            erreur={erreur}
            selection={selection}
            onSelect={selectionner}
          />
        </section>

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
                <tr
                  key={capteur.id}
                  className={capteur.id === selection?.id ? "ligne-selectionnee" : ""}
                >
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
                  <td className={`etat-${capteur.etat}`}>{LIBELLES_ETAT[capteur.etat]}</td>
                  <td className={capteur.etat === "stable" ? "" : "alerte"}>
                    {capteur.alerte}
                  </td>
                  <td>
                    <button type="button">Détails</button>
                    <button type="button" onClick={() => selectionner(capteur.id)}>
                      Localiser
                    </button>
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