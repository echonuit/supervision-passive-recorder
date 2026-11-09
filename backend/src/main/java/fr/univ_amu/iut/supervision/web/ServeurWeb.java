package fr.univ_amu.iut.supervision.web;

import fr.univ_amu.iut.supervision.capteurs.CatalogueCapteurs;
import fr.univ_amu.iut.supervision.persistance.DataBase;
import gg.jte.ContentType;
import gg.jte.TemplateEngine;
import gg.jte.resolve.DirectoryCodeResolver;
import io.javalin.Javalin;
import io.javalin.rendering.template.JavalinJte;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

/** Construction de l'application Javalin : routes, rendu, et rien d'autre. */
public final class ServeurWeb {

    private ServeurWeb() {}

    /** Serveur sans catalogue de capteurs : {@code /api/capteurs} répond une liste vide. */
    public static Javalin creer(DataBase base) {
        return creer(base, () -> List.of());
    }

    public static Javalin creer(DataBase base, CatalogueCapteurs capteurs) {
        TemplateEngine moteur =
                TemplateEngine.create(new DirectoryCodeResolver(dossierDesGabarits()), ContentType.Html);

        // Depuis Javalin 7, les routes se déclarent dans la configuration.
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

            // Contrat avec le front : un tableau JSON de CapteurPosition.
            config.routes.get("/api/capteurs", ctx -> ctx.json(capteurs.lister()));
        });
    }

    /** Permet de lancer depuis la racine du dépôt ou depuis backend/. */
    private static Path dossierDesGabarits() {
        Path local = Path.of("src", "main", "jte");
        return Files.isDirectory(local) ? local : Path.of("backend", "src", "main", "jte");
    }
}
