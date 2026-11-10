package fr.univ_amu.iut.supervision.web;

import fr.univ_amu.iut.supervision.persistance.DataBase;
import gg.jte.ContentType;
import gg.jte.TemplateEngine;
import gg.jte.resolve.DirectoryCodeResolver;
import io.javalin.Javalin;
import io.javalin.rendering.template.JavalinJte;
import kotlin.reflect.KFunction;
import org.eclipse.jetty.ee10.servlet.SessionHandler;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

/** Construction de l'application Javalin : routes, rendu, et rien d'autre. */
public final class ServeurWeb {
    /** Idle timeout (in second) of a session. For no timeout use the value -1.*/
    private static final int sessionIdleTimeOut = 60 * 60;

    private ServeurWeb() {}

    /**
     * A record for processing the data of the user profile.
     * @param _id
     * @param pseudo
     * @param email
     */
    record Profil(String _id, String email,String pseudo) {}

    private String UserId;
    private String UserEmail;
    private String UserPseudo;

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
            // create a session handler
            config.jetty.modifyServletContextHandler(handler -> handler.setSessionHandler(createSessionHandler()) );

            // API path that receive information from the front-end
//            config.routes.post("/api/messages", ctx -> {
//                Message msg = ctx.bodyAsClass(Message.class);
//                System.out.println(msg.name() + ": " + msg.text());
//                // Return to the front-end a message (optional)
//                ctx.json(Map.of("status", "success"));
//            });
            // API path that return a JSON map - TEST
//            config.routes.get("/api/hello", ctx -> ctx.json(Map.of("message", "Hello from Javalin!")));

            // Since the front-end use React+vite, this line may become useless
            config.fileRenderer(new JavalinJte(moteur));
            config.routes.get(
                    "/sante",
                    ctx -> ctx.json(Map.of(
                            "statut",
                            "ok",
                            "base",
                            base == null ? "absente" : (base.isJoinable() ? "joignable" : "injoignable"))));

            config.routes.get("/", ctx -> ctx.render("index.jte", Map.of("nbBoitiers", 0)));

            // Deprecated route TODO : remove
//            config.routes.post("/login", ctx -> {
//                var vigieToken = ctx.formParam("vigieToken");
//                // check if vigieToken is null
//                if ( vigieToken != null && loginService.checkVigieToken(vigieToken) ) {
//                    ctx.sessionAttribute("userVigieToken", vigieToken);
//                    // Test if the token is well written as a session attribute
//                    System.out.println("VigieToken: " + ctx.sessionAttribute("userVigieToken"));
//                }
//            });

            // Receive the profil of the user that was obtained by using his vigiechiro token
            config.routes.post("/api/sentProfil", ctx -> {
                Profil profil = ctx.bodyAsClass(Profil.class);
                // Write the attribute of the user in the session
                ctx.sessionAttribute("UserId", profil._id() );
                ctx.sessionAttribute("UserEmail", profil.email());
                ctx.sessionAttribute("UserPseudo", profil.pseudo());
                System.out.println("The session now these attributes : "+ profil);
            });
            // Send to the front-end server, the attribute of the Session
            config.routes.get("/api/getSessionAttributes", ctx->{
                String id = ctx.sessionAttribute("UserId");
                String email= ctx.sessionAttribute("UserEmail");
                String pseudo = ctx.sessionAttribute("UserPseudo");
                if (id != null && email != null && pseudo != null) {
                    ctx.json(Map.of("_id", id, "email", email, "pseudo", pseudo));
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

// GETTER AND SETTER

    public String getUserId() {
        return UserId;
    }
    public String getUserEmail() {
        return UserEmail;
    }
    public String getUserPseudo() {
        return UserPseudo;
    }

    public void setUserId(String userId) {
        UserId = userId;
    }
    public void setUserEmail(String userEmail) {
        UserEmail = userEmail;
    }
    public void setUserPseudo(String userPseudo) {
        UserPseudo = userPseudo;
    }
}
