import { useEffect, useState } from "react";
import { chargerCapteurs } from "../api/capteurs";

/**
 * Charge la liste des capteurs une fois, au montage du composant qui l'appelle.
 * Renvoie {capteurs, chargement, erreur}.
 *
 * Pour afficher la carte sur une autre page, appelez ce hook dans cette page
 * et passez le résultat à <CarteCapteurs capteurs={capteurs} />.
 */
export function useCapteurs() {
  const [etat, setEtat] = useState({ capteurs: [], chargement: true, erreur: null });

  useEffect(() => {
    const controleur = new AbortController();

    chargerCapteurs(controleur.signal)
      .then((capteurs) => setEtat({ capteurs, chargement: false, erreur: null }))
      .catch((erreur) => {
        if (erreur.name !== "AbortError") {
          setEtat({ capteurs: [], chargement: false, erreur: erreur.message });
        }
      });

    return () => controleur.abort();
  }, []);

  return etat;
}
