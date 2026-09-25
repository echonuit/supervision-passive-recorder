@param int nbBoitiers
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

  <h2>Connect with token:</h2>
  <form method="post" action="/connect-with-token">
      <label>
              <input type="text" name="token" required class="input-class"><br>
      </label>
      <br>
      <button>Submit</button>
  </form>
</body>
</html>
