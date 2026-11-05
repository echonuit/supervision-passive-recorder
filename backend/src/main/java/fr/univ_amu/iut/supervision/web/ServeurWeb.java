package fr.univ_amu.iut.supervision.web;

import fr.univ_amu.iut.supervision.persistance.DataBase;
import gg.jte.ContentType;
import gg.jte.TemplateEngine;
import gg.jte.resolve.DirectoryCodeResolver;
import io.javalin.Javalin;
import io.javalin.rendering.template.JavalinJte;
import org.eclipse.jetty.ee10.servlet.SessionHandler;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

/** Construction de l'application Javalin : routes, rendu, et rien d'autre. */
public final class ServeurWeb {
    /** Idle timeout (in second) of a session. For no timeout use the value -1.*/
    private static final int sessionIdleTimeOut = 30 * 60;

    private ServeurWeb() {}

    /**
     * Create and configure a session handler.
     * @return An object of class SessionHandler
     */
    public static SessionHandler createSessionHandler() {
        SessionHandler sessionHandler = new SessionHandler();
                /*  <--- SESSION HANDLER OPTION / SETTING --->  */
        // HTTPS only (disable for local http dev) ; TODO : set to TRUE before release
        sessionHandler.setSecureRequestOnly(false);
        // If set to true, the session cookie will not be exposed to client-side scripting code.
        sessionHandler.setHttpOnly(true);
        // idle timeout (in second), default -1
        sessionHandler.setMaxInactiveInterval(sessionIdleTimeOut);
        return sessionHandler;
    }

    public static Javalin creer(DataBase base) {
        LoginService loginService =  new LoginService();
        TemplateEngine moteur =
                TemplateEngine.create(new DirectoryCodeResolver(dossierDesGabarits()), ContentType.Html);

        // Depuis Javalin 7, les routes se déclarent dans la configuration.
        var app = Javalin.create(config -> {
            config.jetty.modifyServletContextHandler(handler -> handler.setSessionHandler(createSessionHandler()) );
            config.fileRenderer(new JavalinJte(moteur));
            config.routes.get(
                    "/sante",
                    ctx -> ctx.json(Map.of(
                            "statut",
                            "ok",
                            "base",
                            base == null ? "absente" : (base.isJoinable() ? "joignable" : "injoignable"))));

            config.routes.get("/", ctx -> ctx.render("index.jte", Map.of("nbBoitiers", 0)));

            config.routes.get("/write", ctx -> ctx.sessionAttribute("my-key", "My value"));
            config.routes.get("/read", ctx -> {
                // values on the session will be available on all your instances if you use a session db
                String myValue = ctx.sessionAttribute("my-key");
                System.out.println(myValue);
            });

            // TODO : implement database access and use of the API for the username, email and id of the user
            config.routes.post("/login", ctx -> {
                var vigieToken = ctx.formParam("vigieToken");
                // check if vigieToken is null
                if ( vigieToken != null && loginService.checkVigieToken(vigieToken) ) {
                    ctx.sessionAttribute("userVigieToken", vigieToken);
                    // Test if the token is well written as a session attribute
                    System.out.println("VigieToken: " + ctx.sessionAttribute("userVigieToken"));
                    // redirect to "/"
                    //TODO : redirect to the dashboard (if user exist in database)
                    ctx.redirect("/");
                }
            });

        });
        return app;
    }

    /** Permet de lancer depuis la racine du dépôt ou depuis backend/. */
    private static Path dossierDesGabarits() {
        Path local = Path.of("src", "main", "jte");
        return Files.isDirectory(local) ? local : Path.of("backend", "src", "main", "jte");
    }
}
