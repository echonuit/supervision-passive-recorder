package fr.univ_amu.iut.supervision.web;

import fr.univ_amu.iut.supervision.persistance.BaseDeDonnees;
import gg.jte.ContentType;
import gg.jte.TemplateEngine;
import gg.jte.resolve.DirectoryCodeResolver;
import io.javalin.Javalin;
import io.javalin.rendering.template.JavalinJte;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Map;

/** Construction de l'application Javalin : routes, rendu, et rien d'autre. */
public final class ServeurWeb {

    private ServeurWeb() {}

    /**
     * Used to store reservation as a map
     */
    private static final ArrayList<String> tokenList = new ArrayList<>();

    public static Javalin creer(BaseDeDonnees base) {
        TemplateEngine moteur =
                TemplateEngine.create(new DirectoryCodeResolver(dossierDesGabarits()), ContentType.Html);
        // def the location where the file for the front-end are stored

        // Depuis Javalin 7, les routes se déclarent dans la configuration.
        return Javalin.create(config -> {
            config.fileRenderer(new JavalinJte(moteur));

            config.routes.get(
                    "/sante",
                    ctx -> ctx.json(Map.of(
                            "statut",
                            "ok",
                            "base",
                            base == null ? "absente" : (base.estJoignable() ? "joignable" : "injoignable"))));
            config.routes.get("/", ctx -> ctx.render("index.jte", Map.of("nbBoitiers", 0)));

            // config.routes.get()
            // Args :
            // String path : the path in the url bar
            // Context : an object created on the spot, call the method render()
            // Args :
            // String filePath : the path of the file (can be a .html or a .jte file), note : the root is by default the
            // jte folder.
            config.routes.get("/test2", ctx -> ctx.render("test2.html", Map.of()));
            config.routes.get("/test1", ctx -> ctx.render("test1.html", Map.of()));

            config.routes.get("/check-token", ctx -> {
                ctx.html(tokenList.toString());
            });
            // Use POST method to take the data in the form and store it in the map reservation
            config.routes.post("/connect-with-token", ctx -> {
                tokenList.add(ctx.formParam("token"));
                ctx.html("Your token has been saved");
            });
        });
    }

    /** Permet de lancer depuis la racine du dépôt ou depuis backend/. */
    private static Path dossierDesGabarits() {
        Path local = Path.of("src", "main", "jte");
        return Files.isDirectory(local) ? local : Path.of("backend", "src", "main", "jte");
    }
}
