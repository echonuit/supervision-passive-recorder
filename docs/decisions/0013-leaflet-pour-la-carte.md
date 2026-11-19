# 0013. Leaflet pour la carte des capteurs

Date : 2026-11-06
Statut : proposée

## Contexte

Le tableau de bord doit montrer où sont posés les capteurs, avec leur état. Les
coordonnées viendront de la base, en degrés décimaux (latitude et longitude, par
exemple 43.2145 et 5.435). La place de la carte dans l'interface n'est pas figée :
elle est dans le dashboard aujourd'hui, elle pourra être ailleurs demain.

## Décision

Leaflet, via react-leaflet, avec le fond de carte OpenStreetMap. Le composant
`CarteCapteurs` (`frontend/src/components/carte/`) remplit son parent et ne connaît
que ses props : la déplacer revient à la coller dans un autre conteneur qui a une
hauteur. Les données arrivent par `GET /api/capteurs`, servi par le backend à partir
d'un `CatalogueCapteurs`. `CatalogueDemo` fournit des données fictives en attendant
que le référentiel de la campagne existe en base.

Contrat de la route : un tableau JSON de `{ id, nom, latitude, longitude, etat,
batterie, alerte }`. `etat` vaut `stable`, `avertissement` ou `critique`. On suppose
des coordonnées WGS84, celles du GPS et de Leaflet. Des coordonnées en Lambert 93
demanderaient une conversion avant d'arriver ici.

## Options écartées

Google Maps : clé d'API, facturation et conditions d'usage lourdes pour un projet
d'étude. MapLibre GL ou Mapbox : cartes vectorielles plus riches, mais plus lourdes,
et il faut un jeton ou héberger ses propres tuiles. Une image statique ou une
iframe OpenStreetMap : pas de marqueurs colorés par état, pas d'interaction avec le
tableau.

## Conséquences

Deux dépendances npm de plus (`leaflet`, `react-leaflet`). Le navigateur charge les
tuiles depuis `tile.openstreetmap.org` : l'attribution est obligatoire (elle est
affichée) et la politique d'usage d'OpenStreetMap limite le trafic. Pour une mise en
production, prévoir un fournisseur de tuiles ou un cache ; il suffit de changer les
deux constantes en tête de `carte.jsx`. En développement, Vite redirige `/api` vers
le backend sur `localhost:7070`. Le jour où la base existe, une seconde implémentation
de `CatalogueCapteurs` remplace `CatalogueDemo` dans `Application`, sans toucher à la
route ni au front.

## Comment on saura qu'elle est tenue

`CapteurPositionTest` refuse une latitude ou une longitude hors des degrés
possibles. `CapteursApiTest` fige le contrat de la route. En revue, la carte ne doit
importer ni le dashboard ni son CSS : elle reste déplaçable.
