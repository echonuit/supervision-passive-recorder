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
  const [modalDetailOuverte, setModalDetailOuverte] = useState(false);
  const [capteurSelectionne, setCapteurSelectionne] = useState(null);

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

  const chargerGroupes = () => {
    fetch('/api/groups')
      .then(res => res.json())
      .then(data => setGroupes(data))
      .catch(err => console.error("Erreur chargement groupes :", err));
  };

  useEffect(() => {
    chargerCapteurs();
    chargerGroupes();
    const interval = setInterval(chargerCapteurs, 5000);
    return () => clearInterval(interval);
  }, []);

  function ouvrirModal() {
    chargerGroupes();
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

  function ouvrirModalDetail(capteur) {
    setCapteurSelectionne(capteur);
    setNomCapteur(capteur.name || "");
    setLongitude(capteur.longitude !== undefined && capteur.longitude !== null && capteur.longitude !== "" ? capteur.longitude : "");
    setLatitude(capteur.latitude !== undefined && capteur.latitude !== null && capteur.latitude !== "" ? capteur.latitude : "");
    setModalDetailOuverte(true);
  }

  function fermerModalDetail() {
    setModalDetailOuverte(false);
    setCapteurSelectionne(null);
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
        chargerGroupes();
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

  async function modifierCapteur(evenement) {
    evenement.preventDefault();
    if (!capteurSelectionne) return;

    try {
      const idCapteur = capteurSelectionne.id;
      const reponse = await fetch(`/api/sensors/${idCapteur}`, {
        method: "PUT",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({
          sensorName: nomCapteur.trim(),
          longitude: longitude !== "" ? parseFloat(longitude) : null,
          latitude: latitude !== "" ? parseFloat(latitude) : null,
        }),
      });

      if (!reponse.ok) {
        throw new Error("Erreur lors de la modification du capteur");
      }

      fermerModalDetail();
      chargerCapteurs();
    } catch (erreur) {
      console.error(erreur);
      alert("Erreur : Impossible d'enregistrer les modifications.");
    }
  }

  const capteursAffiches = capteurs.filter((capteur) => {
    const nomOuId = capteur.name || capteur.devEUI || "";
    const correspondTexte = nomOuId.toLowerCase().includes(texteRecherche.toLowerCase());
    const statutCapteur = (capteur.status || capteur.etat || "").toLowerCase();
    const correspondEtat = filtreEtat === "tous" || statutCapteur === filtreEtat.toLowerCase();
    return correspondTexte && correspondEtat;
  });

  const criticalSensors = capteursAffiches.filter(s => (s.battery !== undefined ? s.battery : s.batterie) < 20 || (s.storage !== undefined ? s.storage : s.stockage) < 15);
  const unassignedSensors = capteursAffiches.filter(s => !s.searchName);

  const searchesMap = capteursAffiches.reduce((acc, sensor) => {
    if (sensor.searchName) {
      if (!acc[sensor.searchName]) {
        acc[sensor.searchName] = [];
      }
      acc[sensor.searchName].push(sensor);
    }
    return acc;
  }, {});

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

        {criticalSensors.length > 0 && (
          <div className="table-card" style={{ marginBottom: '30px' }}>
            <SensorTable listSensors={criticalSensors} libellesEtat={libellesEtat} isUrgent={true} onDetail={ouvrirModalDetail} />
          </div>
        )}

        {Object.entries(searchesMap).map(([searchName, groupSensors]) => (
          <div key={searchName} className="table-card" style={{ marginBottom: '30px' }}>
            <div className="table-card-header">
              <h2>{searchName}</h2>
            </div>
            <SensorTable listSensors={groupSensors} libellesEtat={libellesEtat} isUrgent={false} onDetail={ouvrirModalDetail} />
          </div>
        ))}

        {unassignedSensors.length > 0 && (
          <div className="table-card" style={{ marginBottom: '30px' }}>
            <div className="table-card-header">
              <h2>Capteurs non assignés</h2>
            </div>
            <SensorTable listSensors={unassignedSensors} libellesEtat={libellesEtat} isUrgent={false} onDetail={ouvrirModalDetail} />
          </div>
        )}

        {capteursAffiches.length === 0 && (
          <table>
            <tbody>
              <tr>
                <td colSpan={7} style={{ textAlign: 'center' }}>Aucun capteur trouvé</td>
              </tr>
            </tbody>
          </table>
        )}
      </main>

      {modalOuverte && (
        <div className="fond-modal">
          <div className="conteneur-modal">
            <button className="bouton-croix" onClick={fermerModal}>×</button>
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
        </div>
      )}

      {modalDetailOuverte && capteurSelectionne && (
        <div className="fond-modal">
          <div className="conteneur-modal">
            <button className="bouton-croix" onClick={fermerModalDetail}>×</button>
            <form className="modal-ajout-capteur" onSubmit={modifierCapteur}>
              <h2>Détails & Modification</h2>

              <div className="bloc-localisation">
                <p><strong>DevEUI :</strong> {capteurSelectionne.devEUI}</p>
                <p><strong>Localisation actuelle :</strong></p>
                <ul>
                  <li>Longitude : {capteurSelectionne.longitude !== "" && capteurSelectionne.longitude !== null ? capteurSelectionne.longitude : "Non définie"}</li>
                  <li>Latitude : {capteurSelectionne.latitude !== "" && capteurSelectionne.latitude !== null ? capteurSelectionne.latitude : "Non définie"}</li>
                </ul>
              </div>

              <label htmlFor="edit-nom">Nom du capteur</label>
              <input
                id="edit-nom"
                type="text"
                required
                value={nomCapteur}
                onChange={(e) => setNomCapteur(e.target.value)}
              />

              <label htmlFor="edit-long">Modifier Longitude</label>
              <input
                id="edit-long"
                type="number"
                step="any"
                value={longitude}
                onChange={(e) => setLongitude(e.target.value)}
              />

              <label htmlFor="edit-lat">Modifier Latitude</label>
              <input
                id="edit-lat"
                type="number"
                step="any"
                value={latitude}
                onChange={(e) => setLatitude(e.target.value)}
              />

              <div className="boutons-modal">
                <button type="button" onClick={fermerModalDetail}>
                  Fermer
                </button>
                <button type="submit">Enregistrer</button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
}

function SensorTable({ listSensors, libellesEtat, isUrgent, onDetail }) {
  return (
    <table className={isUrgent ? "table-urgent" : ""}>
      <thead>
        <tr>
          <th>NOM DU RECORDER</th>
          <th>STOCKAGE</th>
          <th>BATTERIE</th>
          <th>ÉTAT SYSTÈME</th>
          <th>DERNIÈRE SYNCHRO</th>
          <th>ALERTES / PROBLÈMES</th>
          <th>ACTION</th>
        </tr>
      </thead>
      <tbody>
        {listSensors.map((capteur) => {
          const etatKey = capteur.status || capteur.etat || "Stable";
          const stockageVal = capteur.storage !== undefined ? capteur.storage : (capteur.stockage || 0);
          const batterieVal = capteur.battery !== undefined ? capteur.battery : (capteur.batterie || 100);

          return (
            <tr key={capteur.id || capteur.devEUI}>
              <td>{capteur.name || capteur.devEUI}</td>
              <td>{stockageVal}%</td>
              <td>
                <span className="jauge-batterie">
                  <span
                    className={`jauge-remplissage ${isUrgent ? 'jauge-critique' : `jauge-${etatKey.toLowerCase()}`}`}
                    style={{ width: `${batterieVal}%` }}
                  ></span>
                </span>{" "}
                {batterieVal}%
              </td>
              <td className={`etat-${etatKey.toLowerCase()}`}>
                • {libellesEtat[etatKey] || etatKey}
              </td>
              <td>{capteur.lastSync || "Jamais"}</td>
              <td className={etatKey.toLowerCase() === "stable" ? "" : "alerte"} style={{ color: isUrgent && etatKey.toLowerCase() === "critique" ? '#d6335c' : 'inherit' }}>
                {capteur.alert || capteur.alerte || "Aucune"}
              </td>
              <td>
                <button type="button" className="btn-detail" onClick={() => onDetail(capteur)}>DÉTAIL</button>
              </td>
            </tr>
          );
        })}
      </tbody>
    </table>
  );
}

export default Dashboard;