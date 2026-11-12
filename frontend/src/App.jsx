import { useState, useEffect } from "react";
import Login from "./pages/login/login.jsx";
import Dashboard from "./pages/dashboard/dashboard.jsx";

function App() {
  // On vérifie au lancement s'il y a un token sauvegardé
  const [estConnecte, setEstConnecte] = useState(!!localStorage.getItem("token"));

  function handleLogin() {
    setEstConnecte(true);
  }

  function handleLogout() {
    localStorage.removeItem("token");
    localStorage.removeItem("nomUtilisateur");
    localStorage.removeItem("idUtilisateur");

    setEstConnecte(false);
  }

  return estConnecte ? (
      <Dashboard onLogout={handleLogout} />
  ) : (
      <Login onLogin={handleLogin} />
  );
}

export default App;