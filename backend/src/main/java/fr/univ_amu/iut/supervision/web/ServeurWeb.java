package fr.univ_amu.iut.supervision.web;

import fr.univ_amu.iut.supervision.persistance.DataBase;
import fr.univ_amu.iut.supervision.persistance.DbModel;
import fr.univ_amu.iut.supervision.web.sensor.NewSensor;
import fr.univ_amu.iut.supervision.web.sensor.SensorService;
import fr.univ_amu.iut.supervision.web.table.TableService;
import gg.jte.ContentType;
import gg.jte.TemplateEngine;
import gg.jte.resolve.DirectoryCodeResolver;
import io.javalin.Javalin;
import io.javalin.rendering.template.JavalinJte;
import org.eclipse.jetty.ee10.servlet.SessionHandler;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

public final class ServeurWeb {
    private static final int sessionIdleTimeOut = -1;

    private ServeurWeb() {}

    record Profil(String _id, String email, String pseudo) {}

    public static SessionHandler createSessionHandler() {
        SessionHandler sessionHandler = new SessionHandler();
        sessionHandler.setSecureRequestOnly(false);
        sessionHandler.setHttpOnly(true);
        sessionHandler.setMaxInactiveInterval(sessionIdleTimeOut);
        return sessionHandler;
    }

    public static Javalin creer(DataBase base, DbModel dbModel, SensorService sensorService, TableService tableService) {
        TemplateEngine moteur =
                TemplateEngine.create(new DirectoryCodeResolver(dossierDesGabarits()), ContentType.Html);

        var app = Javalin.create(config -> {
            config.jetty.modifyServletContextHandler(handler -> handler.setSessionHandler(createSessionHandler()));
            config.fileRenderer(new JavalinJte(moteur));

            config.routes.get("/", ctx -> ctx.render("index.jte", Map.of("nbBoitiers", 0)));

            config.routes.get("/api/users", ctx -> {
                ctx.json(dbModel.getAllUsers());
            });

            config.routes.post("/api/sentProfil", ctx -> {
                Profil profil = ctx.bodyAsClass(Profil.class);
                ctx.sessionAttribute("UserId", profil._id());
                ctx.sessionAttribute("UserEmail", profil.email());
                ctx.sessionAttribute("UserPseudo", profil.pseudo());
                if (!dbModel.userExist(profil._id())) {
                    dbModel.registerUser(profil._id(), profil.pseudo(), profil.pseudo(), profil.pseudo());
                }
            });

            config.routes.get("/api/getSessionAttributes", ctx -> {
                String id = ctx.sessionAttribute("UserId");
                String email = ctx.sessionAttribute("UserEmail");
                String pseudo = ctx.sessionAttribute("UserPseudo");
                if (id != null && email != null && pseudo != null) {
                    ctx.json(Map.of("_id", id, "email", email, "pseudo", pseudo));
                }
            });

            config.routes.get("/api/disconnect", ctx -> {
                ctx.req().getSession().invalidate();
            });

            config.routes.get("/api/delete-users", ctx -> {
                if (ctx.sessionAttribute("UserId") != null) {
                    dbModel.deleteUser(ctx.sessionAttribute("UserId"));
                    ctx.req().getSession().invalidate();
                }
            });

            config.routes.get("/api/checkUser", ctx -> {
                String id = ctx.sessionAttribute("UserId");
                if (id != null) {
                    System.out.println("Does user (" + id + ") exist ? : " + dbModel.userExist(id));
                }
            });

            config.routes.post("/api/sensors", ctx -> {
                String userId = ctx.sessionAttribute("UserId");
                if (userId == null) {
                    ctx.status(401).json(Map.of("error", "Utilisateur non authentifié"));
                    return;
                }
                NewSensor newSensor = ctx.bodyAsClass(NewSensor.class);
                int id = sensorService.addSensor(newSensor, userId);
                ctx.status(201).json(Map.of("id", id));
            });

            config.routes.get("/api/table/sensors", ctx -> {
                String userId = ctx.sessionAttribute("UserId");
                if (userId == null) {
                    ctx.status(401).json(Map.of("error", "Utilisateur non authentifié"));
                    return;
                }
                try {
                    ctx.json(tableService.getDashboardSensors(userId));
                } catch (Exception e) {
                    ctx.status(500).json(Map.of("error", e.getMessage()));
                }
            });

            config.routes.post("/api/groups", ctx -> {
                Map<String, String> body = ctx.bodyAsClass(Map.class);
                String name = body.get("name");
                int id = sensorService.createGroup(name);
                ctx.status(201).json(Map.of("id", id));
            });
        });

        return app;
    }

    private static Path dossierDesGabarits() {
        Path local = Path.of("src", "main", "jte");
        return Files.isDirectory(local) ? local : Path.of("backend", "src", "main", "jte");
    }
}