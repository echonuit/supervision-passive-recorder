package fr.univ_amu.iut.supervision.web;

import fr.univ_amu.iut.supervision.persistance.DataBase;
import fr.univ_amu.iut.supervision.web.sensor.NewSensor;
import fr.univ_amu.iut.supervision.web.sensor.SensorService;
import gg.jte.ContentType;
import gg.jte.TemplateEngine;
import gg.jte.resolve.DirectoryCodeResolver;
import io.javalin.Javalin;
import io.javalin.rendering.template.JavalinJte;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

/** Construction de l'application Javalin : routes, rendu, et rien d'autre. */
public final class ServeurWeb {

    private ServeurWeb() {}

    public static Javalin creer(DataBase base, SensorService sensorService) {
        TemplateEngine moteur =
                TemplateEngine.create(new DirectoryCodeResolver(dossierDesGabarits()), ContentType.Html);

        return Javalin.create(config -> {
            config.fileRenderer(new JavalinJte(moteur));

            config.routes.get(
                    "/sante",
                    ctx -> ctx.json(Map.of(
                            "statut",
                            "ok",
                            "base",
                            base == null ? "absente" : (base.isJoinable() ? "joignable" : "injoignable"))));

            config.routes.get("/", ctx -> ctx.render("index.jte", Map.of("nbBoitiers", 0)));

            // Routes API pour les capteurs et groupes
            config.routes.post("/api/sensors", ctx -> {
                NewSensor newSensor = ctx.bodyAsClass(NewSensor.class);
                int id = sensorService.addSensor(newSensor);
                ctx.status(201).json(Map.of("id", id));
            });

            config.routes.post("/api/groups", ctx -> {
                Map<String, String> body = ctx.bodyAsClass(Map.class);
                String name = body.get("name");
                int id = sensorService.createGroup(name);
                ctx.status(201).json(Map.of("id", id));
            });
        });
    }

    private static Path dossierDesGabarits() {
        Path local = Path.of("src", "main", "jte");
        return Files.isDirectory(local) ? local : Path.of("backend", "src", "main", "jte");
    }
}
