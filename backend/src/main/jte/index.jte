@param int nbBoitiers
<!-- if development -->
<%--<script type="module">--%>
<%--    import RefreshRuntime from 'http://localhost:5173/@react-refresh'--%>
<%--    RefreshRuntime.injectIntoGlobalHook(window)--%>
<%--    window.$RefreshReg$ = () => {}--%>
<%--    window.$RefreshSig$ = () => (type) => type--%>
<%--    window.__vite_plugin_react_preamble_installed__ = true--%>
<%--</script>--%>
<%--<script type="module" src="http://localhost:5173/@vite/client"></script>--%>
<%--<script type="module" src="http://localhost:5173/main.jsx"></script>--%>

<!DOCTYPE html>
<html lang="fr">
<head>
  <meta charset="utf-8">
  <title>Supervision des PR</title>
  <style>
    body { font-family: system-ui, sans-serif; margin: 2rem; color: #2c3e50; background: #f5f6f8; }
    h1 { color: #1a237e; }
  </style>
</head>
<body>
  <h1>Supervision des Passive Recorders</h1>
  <p>${nbBoitiers} boîtier(s) suivi(s).</p>
  <form method="post" action="/login">
      <label>Vigiechiro Token : </label>
      <input type="text" name="vigieToken" id="vigieToken" required>
      <input type="submit" value="Envoyer">
  </form>
</body>
</html>
