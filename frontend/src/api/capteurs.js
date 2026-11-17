// Contrat avec le backend : GET /api/capteurs renvoie un tableau de
//   { id, nom, latitude, longitude, etat, batterie, alerte }
// latitude et longitude sont en degrés décimaux (ex. 43.2145 et 5.435).
const URL_CAPTEURS = "/api/capteurs";

export async function chargerCapteurs(signal) {
  const reponse = await fetch(URL_CAPTEURS, {
    signal,
    headers: { Accept: "application/json" },
  });

  if (!reponse.ok) {
    throw new Error(`le serveur a répondu ${reponse.status}`);
  }

  // Sans le proxy /api de Vite, la requête tombe sur index.html : on le dit clairement.
  const type = reponse.headers.get("content-type") ?? "";
  if (!type.includes("application/json")) {
    throw new Error("réponse inattendue (pas du JSON) : le backend est-il lancé, et le proxy /api actif ?");
  }

  return reponse.json();
}
