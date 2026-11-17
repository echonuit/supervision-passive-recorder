import L from "leaflet";
import { MapContainer, Marker, TileLayer, useMapEvents } from "react-leaflet";
import "leaflet/dist/leaflet.css";
import "./selecteurPosition.css";

/*
 * Carte pour choisir une position : un clic place le point, on peut ensuite le faire glisser.
 *
 * Props :
 *   latitude, longitude   position actuelle (nombre ou texte), "" quand il n'y en a pas
 *   onChange              appelé avec (latitude, longitude) ; ("", "") quand on retire la position
 *   centre, zoom          vue de départ quand aucune position n'est choisie
 *
 * Les coordonnées sont en degrés décimaux, arrondies à 6 décimales (environ 10 cm).
 * Le clic est ramené dans [-180 ; 180] : si on a fait défiler la carte d'un tour du monde,
 * Leaflet donnerait sinon une longitude hors limites, que le serveur refuserait.
 */

const FOND_URL = "https://tile.openstreetmap.org/{z}/{x}/{y}.png";
const FOND_ATTRIBUTION = '&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a>';

const CENTRE_PAR_DEFAUT = [46.6, 2.5]; // France métropolitaine
const ZOOM_PAR_DEFAUT = 5;
const ZOOM_AVEC_POSITION = 12;

const icone = L.divIcon({
    className: "selecteur-position__point",
    iconSize: [22, 22],
    iconAnchor: [11, 11],
});

function arrondir(valeur) {
    return Math.round(valeur * 1e6) / 1e6;
}

function ClicSurCarte({ onChoisir }) {
    useMapEvents({
        click(evenement) {
            onChoisir(evenement.latlng.wrap());
        },
    });
    return null;
}

function SelecteurPosition({
                               latitude,
                               longitude,
                               onChange,
                               centre = CENTRE_PAR_DEFAUT,
                               zoom = ZOOM_PAR_DEFAUT,
                           }) {
    const lat = parseFloat(latitude);
    const lon = parseFloat(longitude);
    const aUnePosition = Number.isFinite(lat) && Number.isFinite(lon);

    function choisir(point) {
        onChange(arrondir(point.lat), arrondir(point.lng));
    }

    return (
        <div className="selecteur-position">
            <div className="selecteur-position__carte">
                <MapContainer
                    center={aUnePosition ? [lat, lon] : centre}
                    zoom={aUnePosition ? ZOOM_AVEC_POSITION : zoom}
                    scrollWheelZoom
                    style={{ width: "100%", height: "100%" }}
                >
                    <TileLayer url={FOND_URL} attribution={FOND_ATTRIBUTION} />
                    <ClicSurCarte onChoisir={choisir} />
                    {aUnePosition && (
                        <Marker
                            position={[lat, lon]}
                            icon={icone}
                            draggable
                            eventHandlers={{
                                dragend: (evenement) => choisir(evenement.target.getLatLng().wrap()),
                            }}
                        />
                    )}
                </MapContainer>
            </div>

            <div className="selecteur-position__pied">
                {aUnePosition ? (
                    <>
            <span>
              Latitude {lat.toFixed(6)}, longitude {lon.toFixed(6)}
            </span>
                        <button type="button" onClick={() => onChange("", "")}>
                            Retirer la position
                        </button>
                    </>
                ) : (
                    <span>Cliquez sur la carte pour placer le capteur (facultatif).</span>
                )}
            </div>
        </div>
    );
}

export default SelecteurPosition;