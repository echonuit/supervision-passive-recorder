import "./login.css";

function Login() {
  return (
    <main className="conteneur-login">
      <section className="login-card">
        <h1>Supervision Passive Recorder</h1>
        <label htmlFor="token">Token (24 characters)</label>
        <input id="token" type="password" maxLength={24} placeholder="••••••••••••••••••••••••" />

        <button>Login</button>
        <a href="#">Obtenir Token</a>
      </section>
    </main>
  );
}

export default Login;