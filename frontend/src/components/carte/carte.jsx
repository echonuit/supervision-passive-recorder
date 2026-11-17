import { useEffect, useRef } from "react";
import L from "leaflet";
import { MapContainer, Marker, Popup, TileLayer, useMap } from "react-leaflet";
import "leaflet/dist/leaflet.css";
import { LIBELLES_ETAT } from "../../constantes/etats";
import "./carte.css";

/*
 * Carte des capteurs (Leaflet + OpenStreetMap).
 *
 * Le composant remplit 100 % de son parent : c'est le parent qui fixe la taille.
 * Pour le déplacer, couper-coller la balise dans un autre conteneur qui a une hauteur,
 * et lui donner les données du hook useCapteurs :
 *
 *   const donnees = useCapteurs();
 *   <div style={{ height: 400 }}><CarteCapteurs {...donnees} /></div>
 *
 * Props :
 *   capteurs    tableau de { id, nom, latitude, longitude, etat, batterie, alerte },
 *               latitude et longitude en degrés décimaux
 *   chargement  true : affiche « Chargement… »
 *   erreur      texte d'erreur à afficher, ou null
 *   selection   { id } : centre la carte sur ce capteur et ouvre sa bulle. Passer un
 *               nouvel objet à chaque demande pour recentrer sur le même capteur.
 *   onSelect    appelé avec l'id quand on clique sur un marqueur
 *   centre, zoom   vue de départ tant qu'il n'y a aucun capteur
 *   zoomMolette    true pour zoomer avec la molette (désactivé par défaut : sur un
 *                  tableau de bord qui défile, la carte volerait le défilement)
 */

// Fond de carte. Pour en changer, modifier ces deux constantes.
const FOND_URL = "https://tile.openstreetmap.org/{z}/{x}/{y}.png";
const FOND_ATTRIBUTION = '&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a>';

const CENTRE_PAR_DEFAUT = [46.6, 2.5]; // France métropolitaine
const ZOOM_PAR_DEFAUT = 5;
const ZOOM_MAX_AJUSTEMENT = 13;
const ZOOM_SELECTION = 12;

// Un marqueur rond coloré par état, dessiné en CSS (voir carte.css). On évite ainsi les
// images de marqueur de Leaflet, que les bundlers ne retrouvent pas toujours.
const icones = new Map();

function icone(etat, selectionne) {
  const cle = `${etat}-${selectionne}`;
  if (!icones.has(cle)) {
    icones.set(
      cle,
      L.divIcon({
        className: `marqueur marqueur--${etat}${selectionne ? " marqueur--selectionne" : ""}`,
        iconSize: [22, 22],
        iconAnchor: [11, 11],
        popupAnchor: [0, -12],
      }),
    );
  }
  return icones.get(cle);
}

function aUnePosition(capteur) {
  return Number.isFinite(capteur.latitude) && Number.isFinite(capteur.longitude);
}

/** Cadre la vue sur tous les marqueurs, seulement quand l'ensemble des positions change. */
function AjusterVue({ cle }) {
  const carte = useMap();

  useEffect(() => {
    if (!cle) return;
    const points = cle.split(";").map((point) => point.split(",").map(Number));
    carte.fitBounds(points, { padding: [40, 40], maxZoom: ZOOM_MAX_AJUSTEMENT });
  }, [carte, cle]);

  return null;
}

/** Centre la carte sur le capteur sélectionné et ouvre sa bulle. */
function SuivreSelection({ selection, cible, marqueurs }) {
  const carte = useMap();

  useEffect(() => {
    if (!cible) return;
    carte.flyTo([cible.latitude, cible.longitude], Math.max(carte.getZoom(), ZOOM_SELECTION), {
      duration: 0.6,
    });
    marqueurs.current.get(cible.id)?.openPopup();
  }, [carte, selection, cible, marqueurs]);

  return null;
}

function CarteCapteurs({
  capteurs = [],
  chargement = false,
  erreur = null,
  selection = null,
  onSelect,
  centre = CENTRE_PAR_DEFAUT,
  zoom = ZOOM_PAR_DEFAUT,
  zoomMolette = false,
}) {
  const marqueurs = useRef(new Map());

  const places = capteurs.filter(aUnePosition);
  const sansPosition = capteurs.length - places.length;
  const cle = places.map((capteur) => `${capteur.latitude},${capteur.longitude}`).join(";");
  const idSelectionne = selection?.id;
  const cible = places.find((capteur) => capteur.id === idSelectionne);

  return (
    <div className="carte-capteurs">
      <MapContainer
        className="carte-capteurs__carte"
        center={centre}
        zoom={zoom}
        scrollWheelZoom={zoomMolette}
      >
        <TileLayer url={FOND_URL} attribution={FOND_ATTRIBUTION} />
        <AjusterVue cle={cle} />
        <SuivreSelection selection={selection} cible={cible} marqueurs={marqueurs} />

        {places.map((capteur) => (
          <Marker
            key={capteur.id}
            ref={(marqueur) => {
              if (marqueur) {
                marqueurs.current.set(capteur.id, marqueur);
              } else {
                marqueurs.current.delete(capteur.id);
              }
            }}
            position={[capteur.latitude, capteur.longitude]}
            icon={icone(capteur.etat, capteur.id === idSelectionne)}
            title={`${capteur.nom} (${capteur.id})`}
            eventHandlers={{ click: () => onSelect?.(capteur.id) }}
          >
            <Popup>
              <div className="carte-popup">
                <p className="carte-popup__titre">{capteur.nom}</p>
                <p className="carte-popup__id">{capteur.id}</p>
                <p>
                  État :{" "}
                  <span className={`carte-popup__etat carte-popup__etat--${capteur.etat}`}>
                    {LIBELLES_ETAT[capteur.etat] ?? capteur.etat}
                  </span>
                </p>
                <p>Batterie : {capteur.batterie} %</p>
                {capteur.alerte && <p className="carte-popup__alerte">{capteur.alerte}</p>}
              </div>
            </Popup>
          </Marker>
        ))}
      </MapContainer>

      {chargement && (
        <p className="carte-capteurs__message" role="status">
          Chargement des capteurs…
        </p>
      )}
      {erreur && (
        <p className="carte-capteurs__message carte-capteurs__message--erreur" role="alert">
          Impossible de charger les capteurs : {erreur}
        </p>
      )}
      {sansPosition > 0 && (
        <p className="carte-capteurs__note">
          {sansPosition} capteur{sansPosition > 1 ? "s" : ""} sans position
        </p>
      )}
    </div>
  );
}

export default CarteCapteurs;
