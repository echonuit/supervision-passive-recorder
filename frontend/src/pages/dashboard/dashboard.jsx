import { useState, useEffect } from "react";
import Sidebar from "../../components/sidebar";
import "./dashboard.css";

const libellesEtat = {
  stable: "Stable",
  avertissement: "Avertissement",
  critique: "Critique",
};

function Dashboard() {
  const [modalOuverte, setModalOuverte] = useState(false);

  const [devEUI, setDevEUI] = useState("");
  const [nomCapteur, setNomCapteur] = useState("");
  const [longitude, setLongitude] = useState("");
  const [latitude, setLatitude] = useState("");
  
  const [rechercheChoisie, setRechercheChoisie] = useState("");
  const [nouvelleRecherche, setNouvelleRecherche] = useState("");
  const [groupes, setGroupes] = useState([]); // Liste des groupes existants

  const [capteurs, setCapteurs] = useState([]);
  const [texteRecherche, setTexteRecherche] = useState("");
  const [filtreEtat, setFiltreEtat] = useState("tous");
  const [erreurFormulaire, setErreurFormulaire] = useState("");

  function ouvrirModal() {
    setModalOuverte(true);
  }

  function fermerModal() {
    setModalOuverte(false);
    setDevEUI("");
    setNomCapteur("");
    setLongitude("");
    setLatitude("");
    setRechercheChoisie("");
    setNouvelleRecherche("");
    setErreurFormulaire("");
  }

  async function ajouterCapteur(evenement) {
    evenement.preventDefault();
    setErreurFormulaire("");

    try {
      let groupIdFinal = null;

      if (rechercheChoisie === "__nouvelle__" && nouvelleRecherche.trim() !== "") {
        const reponseGroupe = await fetch("/api/groups", {
          method: "POST",
          headers: { "Content-Type": "application/json" },
          body: JSON.stringify({ name: nouvelleRecherche.trim() }),
        });

        const texteGroupe = await reponseGroupe.text();
        let dataGroupe = null;
        if (texteGroupe) {
          try { dataGroupe = JSON.parse(texteGroupe); } catch (e) { /* ignore */ }
        }

        if (!reponseGroupe.ok) {
          throw new Error(dataGroupe?.error || "Erreur lors de la création du groupe");
        }

        groupIdFinal = dataGroupe?.id;
      } else if (rechercheChoisie && rechercheChoisie !== "__nouvelle__") {
        groupIdFinal = parseInt(rechercheChoisie, 10);
      }

      const nouveauCapteur = {
        devEUI: devEUI.trim(),
        sensorName: nomCapteur.trim(),
        longitude: longitude !== "" ? parseFloat(longitude) : null,
        latitude: latitude !== "" ? parseFloat(latitude) : null,
        groupId: groupIdFinal,
      };

      const reponseCapteur = await fetch("/api/sensors", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(nouveauCapteur),
      });

      const texteCapteur = await reponseCapteur.text();
      let dataCapteur = null;
      if (texteCapteur) {
        try { dataCapteur = JSON.parse(texteCapteur); } catch (e) { /* ignore */ }
      }

      if (!reponseCapteur.ok) {
        throw new Error(dataCapteur?.error || "Erreur lors de l'ajout du capteur");
      }

      fermerModal();
      
    } catch (erreur) {
      setErreurFormulaire(erreur.message);
    }
  }

  const capteursAffiches = capteurs.filter((capteur) => {
    const correspondTexte = (capteur.id || capteur.devEUI || "")
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
                <tr key={capteur.id || capteur.devEUI}>
                  <td>{capteur.id || capteur.devEUI}</td>
                  <td>
                    <span className="jauge-batterie">
                      <span
                        className={`jauge-remplissage jauge-${capteur.etat || "stable"}`}
                        style={{ width: `${capteur.batterie || 100}%` }}
                      ></span>
                    </span>{" "}
                    {capteur.batterie || 100}%
                  </td>
                  <td className={`etat-${capteur.etat || "stable"}`}>
                    {libellesEtat[capteur.etat || "stable"]}
                  </td>
                  <td className={capteur.etat === "stable" ? "" : "alerte"}>
                    {capteur.alerte || "-"}
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

            {erreurFormulaire && (
              <div style={{ color: "#d6335c", fontSize: "12px", marginBottom: "10px", fontWeight: "bold" }}>
                {erreurFormulaire}
              </div>
            )}

            <label htmlFor="dev-eui">DevEUI</label>
            <input
              id="dev-eui"
              type="text"
              maxLength={16}
              required
              value={devEUI}
              onChange={(evenement) => setDevEUI(evenement.target.value)}
              placeholder="Ex: 2CF7F1207380D2D8"
            />

            <label htmlFor="nom-capteur">Nom du capteur</label>
            <input
              id="nom-capteur"
              type="text"
              maxLength={100}
              required
              value={nomCapteur}
              onChange={(evenement) => setNomCapteur(evenement.target.value)}
              placeholder="Ex: Station météo 1"
            />

            <label htmlFor="longitude">Longitude</label>
            <input
              id="longitude"
              type="number"
              step="any"
              value={longitude}
              onChange={(evenement) => setLongitude(evenement.target.value)}
              placeholder="Ex: 3.14"
            />

            <label htmlFor="latitude">Latitude</label>
            <input
              id="latitude"
              type="number"
              step="any"
              value={latitude}
              onChange={(evenement) => setLatitude(evenement.target.value)}
              placeholder="Ex: 1.23"
            />

            <label htmlFor="recherche">Recherche / Groupe</label>
            <select
              id="recherche"
              value={rechercheChoisie}
              onChange={(evenement) => setRechercheChoisie(evenement.target.value)}
            >
              <option value="">Choisir une recherche</option>
              {groupes.map((g) => (
                <option key={g.id} value={g.id}>{g.name}</option>
              ))}
              <option value="__nouvelle__">+ Nouvelle recherche</option>
            </select>

            {rechercheChoisie === "__nouvelle__" && (
              <>
                <label htmlFor="nouvelle-recherche">Nom de la nouvelle recherche</label>
                <input
                  id="nouvelle-recherche"
                  type="text"
                  required
                  value={nouvelleRecherche}
                  onChange={(evenement) => setNouvelleRecherche(evenement.target.value)}
                  placeholder="Nom du groupe..."
                />
              </>
            )}

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