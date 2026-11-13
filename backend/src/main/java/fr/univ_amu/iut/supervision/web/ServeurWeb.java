package fr.univ_amu.iut.supervision.web;

import fr.univ_amu.iut.supervision.persistance.DbModel;
import fr.univ_amu.iut.supervision.web.sensor.NewSensor;
import gg.jte.ContentType;
import gg.jte.TemplateEngine;
import gg.jte.resolve.DirectoryCodeResolver;
import io.javalin.Javalin;
import io.javalin.rendering.template.JavalinJte;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import org.eclipse.jetty.ee10.servlet.SessionHandler;

public final class ServeurWeb {
    private static final int sessionIdleTimeOut = -1;
    private static final String SESSION_USER_ID_KEY = "UserId";
    private static final String SESSION_EMAIL_KEY = "UserEmail";
    private static final String SESSION_PSEUDO_KEY = "UserPseudo";
    private static final String ERROR_KEY = "error";
    private static final String MSG_NON_AUTHENTIFIE = "Utilisateur non authentifié";

    private ServeurWeb() {}

    record Profil(String _id, String email, String pseudo) {}

    public static String getSessionUserIdKey() {
        return SESSION_USER_ID_KEY;
    }

    public static String getSessionPseudoKey() {
        return SESSION_PSEUDO_KEY;
    }

    public static String getSESSION_EMAIL_KEY() {
        return SESSION_EMAIL_KEY;
    }

    public static SessionHandler createSessionHandler() {
        SessionHandler sessionHandler = new SessionHandler();
        sessionHandler.setSecureRequestOnly(false);
        sessionHandler.setHttpOnly(true);
        sessionHandler.setMaxInactiveInterval(sessionIdleTimeOut);
        return sessionHandler;
    }

    public static Javalin creer(DbModel dbModel) {
        TemplateEngine moteur =
                TemplateEngine.create(new DirectoryCodeResolver(dossierDesGabarits()), ContentType.Html);

        var app = Javalin.create(config -> {
            config.jetty.modifyServletContextHandler(handler -> handler.setSessionHandler(createSessionHandler()));
            config.fileRenderer(new JavalinJte(moteur));

            config.routes.get("/", ctx -> ctx.render("index.jte"));

            config.routes.get("/api/users", ctx -> {
                ctx.json(dbModel.getAllUsers());
                ctx.status(200);
            });

            config.routes.post("/api/sentProfil", ctx -> {
                Profil profil = ctx.bodyAsClass(Profil.class);
                ctx.sessionAttribute(SESSION_USER_ID_KEY, profil._id());
                ctx.sessionAttribute(SESSION_EMAIL_KEY, profil.email());
                ctx.sessionAttribute(SESSION_PSEUDO_KEY, profil.pseudo());
                if (!dbModel.userExist(profil._id())) {
                    dbModel.registerUser(profil._id(), profil.pseudo(), profil.pseudo(), profil.pseudo());
                }
                ctx.status(201);
            });

            config.routes.get("/api/getSessionAttributes", ctx -> {
                String id = ctx.sessionAttribute(SESSION_USER_ID_KEY);
                String email = ctx.sessionAttribute(SESSION_EMAIL_KEY);
                String pseudo = ctx.sessionAttribute(SESSION_PSEUDO_KEY);
                if (id != null && email != null && pseudo != null) {
                    ctx.json(Map.of("_id", id, "email", email, "pseudo", pseudo));
                    ctx.status(200);
                }
            });

            config.routes.get("/api/disconnect", ctx -> {
                ctx.req().getSession().invalidate();
                ctx.status(200);
            });

            config.routes.post("/api/delete/user", ctx -> {
                String id = ctx.sessionAttribute(SESSION_USER_ID_KEY);
                if (id != null) {
                    dbModel.deleteUser(id);
                    ctx.req().getSession().invalidate();
                }
                ctx.status(200);
            });

            config.routes.get("/api/checkUser", ctx -> {
                String id = ctx.sessionAttribute(SESSION_USER_ID_KEY);
                if (id != null) {
                    dbModel.userExist(id);
                }
                ctx.status(200);
            });

            config.routes.post("/api/sensors", ctx -> {
                String userId = ctx.sessionAttribute(SESSION_USER_ID_KEY);
                if (userId == null) {
                    ctx.status(401).json(Map.of(ERROR_KEY, MSG_NON_AUTHENTIFIE));
                    return;
                }
                NewSensor newSensor = ctx.bodyAsClass(NewSensor.class);
                int id = dbModel.addSensor(newSensor, userId);
                ctx.status(201).json(Map.of("id", id));
            });

            config.routes.put("/api/sensors/{id}", ctx -> {
                try {
                    int idCapteur = Integer.parseInt(ctx.pathParam("id"));
                    NewSensor sensorData = ctx.bodyAsClass(NewSensor.class);
                    dbModel.updateSensor(idCapteur, sensorData.sensorName(), sensorData.longitude(), sensorData.latitude());
                    ctx.status(200).json(Map.of("success", true));
                } catch (Exception e) {
                    ctx.status(500).json(Map.of(ERROR_KEY, e.getMessage()));
                }
            });

            config.routes.get("/api/table/sensors", ctx -> {
                String userId = ctx.sessionAttribute(SESSION_USER_ID_KEY);
                if (userId == null) {
                    ctx.status(401).json(Map.of(ERROR_KEY, MSG_NON_AUTHENTIFIE));
                    return;
                }
                try {
                    ctx.json(dbModel.getSensorsForTable(userId));
                } catch (Exception e) {
                    ctx.status(500).json(Map.of(ERROR_KEY, e.getMessage()));
                }
            });

            config.routes.post("/api/groups", ctx -> {
                Map<?, ?> body = ctx.bodyAsClass(Map.class);
                String name = (String) body.get("name");
                int id = dbModel.createGroup(name);
                ctx.status(201).json(Map.of("id", id));
            });

            config.routes.get("/api/groups", ctx -> {
                try {
                    ctx.json(dbModel.getAllGroups());
                } catch (Exception e) {
                    ctx.status(500).json(Map.of(ERROR_KEY, e.getMessage()));
                }
            });
        });

        return app;
    }

    private static Path dossierDesGabarits() {
        Path local = Path.of("src", "main", "jte");
        return Files.isDirectory(local) ? local : Path.of("backend", "src", "main", "jte");
    }
}