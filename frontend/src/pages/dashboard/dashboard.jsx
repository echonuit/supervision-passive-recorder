import { useState, useEffect } from "react";
import Sidebar from "../../components/sidebar/sidebar";
import "./dashboard.css";

const libellesEtat = {
  Stable: "Stable",
  Avertissement: "Avertissement",
  Critique: "Critique",
  stable: "Stable",
  avertissement: "Avertissement",
  critique: "Critique",
};

function Dashboard({ onLogout }) {
  const [modalOuverte, setModalOuverte] = useState(false);

  const [devEUI, setDevEUI] = useState("");
  const [nomCapteur, setNomCapteur] = useState("");
  const [longitude, setLongitude] = useState("");
  const [latitude, setLatitude] = useState("");
  
  const [rechercheChoisie, setRechercheChoisie] = useState("");
  const [nouvelleRecherche, setNouvelleRecherche] = useState("");
  const [groupes, setGroupes] = useState([]);

  const [capteurs, setCapteurs] = useState([]);
  const [texteRecherche, setTexteRecherche] = useState("");
  const [filtreEtat, setFiltreEtat] = useState("tous");
  const [erreurFormulaire, setErreurFormulaire] = useState("");

  const chargerCapteurs = () => {
    fetch('/api/table/sensors')
      .then(res => {
        if (!res.ok) throw new Error("Erreur lors de la récupération des capteurs");
        return res.json();
      })
      .then(data => setCapteurs(data))
      .catch(err => console.error(err));
  };

  useEffect(() => {
    chargerCapteurs();
    const interval = setInterval(chargerCapteurs, 5000);
    return () => clearInterval(interval);
  }, []);

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
          try { dataGroupe = JSON.parse(texteGroupe); } catch (e) {}
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
        try { dataCapteur = JSON.parse(texteCapteur); } catch (e) {}
      }

      if (!reponseCapteur.ok) {
        throw new Error(dataCapteur?.error || "Erreur lors de l'ajout du capteur");
      }

      fermerModal();
      chargerCapteurs();
      
    } catch (erreur) {
      setErreurFormulaire(erreur.message);
    }
  }

  const capteursAffiches = capteurs.filter((capteur) => {
    const nomOuId = capteur.name || capteur.devEUI || "";
    const correspondTexte = nomOuId.toLowerCase().includes(texteRecherche.toLowerCase());
    const statutCapteur = (capteur.status || capteur.etat || "").toLowerCase();
    const correspondEtat = filtreEtat === "tous" || statutCapteur === filtreEtat.toLowerCase();
    return correspondTexte && correspondEtat;
  });

  return (
    <div className="page-tableau-de-bord">
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
              capteursAffiches.map((capteur) => {
                const etatKey = capteur.status || capteur.etat || "Stable";
                const batterieVal = capteur.battery !== undefined ? capteur.battery : (capteur.batterie || 100);
                return (
                  <tr key={capteur.id || capteur.devEUI}>
                    <td>{capteur.name || capteur.devEUI}</td>
                    <td>
                      <span className="jauge-batterie">
                        <span
                          className={`jauge-remplissage jauge-${etatKey.toLowerCase()}`}
                          style={{ width: `${batterieVal}%` }}
                        ></span>
                      </span>{" "}
                      {batterieVal}%
                    </td>
                    <td className={`etat-${etatKey.toLowerCase()}`}>
                      {libellesEtat[etatKey] || etatKey}
                    </td>
                    <td className={etatKey.toLowerCase() === "stable" ? "" : "alerte"}>
                      {capteur.alert || capteur.alerte || "-"}
                    </td>
                    <td>
                      <button type="button" onClick={() => alert(`Détails du capteur : ${capteur.name}`)}>Détails</button>
                    </td>
                  </tr>
                );
              })
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