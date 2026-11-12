import { useState, useEffect } from "react";
import Login from "./pages/login/login.jsx";
import Dashboard from "./pages/dashboard/dashboard.jsx";

function App() {
  // On vérifie au lancement s'il y a un token sauvegardé
  const [estConnecte, setEstConnecte] = useState(!!localStorage.getItem("token"));

  // Fonction appelée quand le login réussit
  function handleLogin() {
    setEstConnecte(true);
  }

  // Fonction appelée quand on clique sur déconnexion
  function handleLogout() {
    // 1. On supprime les données sauvegardées
    localStorage.removeItem("token");
    localStorage.removeItem("nomUtilisateur");
    localStorage.removeItem("idUtilisateur");

    // 2. On indique qu'on n'est plus connecté (ce qui ramènera au Login)
    setEstConnecte(false);
  }

  // Si connecté -> affiche le Dashboard, sinon -> affiche le Login
  return estConnecte ? (
    <Dashboard onLogout={handleLogout} />
  ) : (
    <Login onLogin={handleLogin} />
  );
}

export default App;