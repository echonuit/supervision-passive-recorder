package fr.univ_amu.iut.supervision.web;

import fr.univ_amu.iut.supervision.persistance.DbModel;
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
    private static final int sessionIdleTimeOut = -1;

    private ServeurWeb() {}

    /**
     * A record for processing the data of the user profile.
     * @param _id
     * @param pseudo
     * @param email
     */
    record Profil(String _id, String email,String pseudo) {}

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

    public static Javalin creer(DbModel base) {
        LoginService loginService =  new LoginService();
        TemplateEngine moteur =
                TemplateEngine.create(new DirectoryCodeResolver(dossierDesGabarits()), ContentType.Html);

        // Depuis Javalin 7, les routes se déclarent dans la configuration.
        var app = Javalin.create(config -> {
            // create a session handler
            config.jetty.modifyServletContextHandler(handler -> handler.setSessionHandler(createSessionHandler()) );

            // Since the front-end use React+vite, this line may become useless
            config.fileRenderer(new JavalinJte(moteur));

            config.routes.get("/", ctx -> ctx.render("index.jte"));

            // Get all the users id and last name in a JSON
            config.routes.get("/api/users", ctx -> {
                System.out.println(base.getAllUsers());
                ctx.json(base.getAllUsers());
                ctx.status(200);
            });
            // Receive the profil of the user that was obtained by using his vigiechiro token.
            // Write the element of the profil in session var.
            // Register the user in the database if not the case.
            config.routes.post("/api/sentProfil", ctx -> {
                Profil profil = ctx.bodyAsClass(Profil.class);
                // Write the attribute of the user in the session
                ctx.sessionAttribute("UserId", profil._id() );
                ctx.sessionAttribute("UserEmail", profil.email());
                ctx.sessionAttribute("UserPseudo", profil.pseudo());
                // TODO add a sys to prevent having to write the user again in the database.
                if (!base.userExist(profil._id)){
                    System.out.println("LOG : Creating entry in the database for user " + profil._id);
                    base.registerUser(profil._id, profil.pseudo, profil.pseudo, profil.pseudo());
                }
                System.out.println("The session now these attributes : "+ profil);
                ctx.status(201);
            });
            // Send to the front-end server, the attribute of the Session
            config.routes.get("/api/getSessionAttributes", ctx->{
                String id = ctx.sessionAttribute("UserId");
                String email= ctx.sessionAttribute("UserEmail");
                String pseudo = ctx.sessionAttribute("UserPseudo");
                if (id != null && email != null && pseudo != null) {
                    ctx.json(Map.of("_id", id, "email", email, "pseudo", pseudo));
                    ctx.status(200);
                }
            });
            // TODO : implement in the fron-end
            config.routes.get("/api/disconnect", ctx -> {
                // should destroy the session of the user
                System.out.println("LOG : End of the session for user " + ctx.sessionAttribute("UserId"));
                ctx.req().getSession().invalidate();
                // Check if this is correct
                ctx.status(200);
            });
            // Delete the user of the session from the database
            // TODO implement to front-end
            config.routes.get("/api/delete/user", ctx -> {
                if (ctx.sessionAttribute("UserId") != null) {
                    base.deleteUser(ctx.sessionAttribute("UserId"));
                    ctx.req().getSession().invalidate();
                }
                // Check if this is correct
                ctx.status(200);
            });
            // Check if a user already exist in the database, print the result.
            config.routes.get("/api/checkUser", ctx ->{
                String id = ctx.sessionAttribute("UserId");
                if (id != null) {
                   System.out.println("Does user ("+ id +") exist ? : " + base.userExist(id) );
                } else {
                   System.out.println("WARNING : the user id is null");
                }
                ctx.status(200);
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
