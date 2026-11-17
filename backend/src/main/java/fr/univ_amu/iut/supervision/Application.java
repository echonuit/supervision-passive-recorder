package fr.univ_amu.iut.supervision;

import fr.univ_amu.iut.supervision.ingestion.SourceLiveObjects;
import fr.univ_amu.iut.supervision.ingestion.SourceMqtt;
import fr.univ_amu.iut.supervision.ingestion.TramesSource;
import fr.univ_amu.iut.supervision.persistance.ArchiveTrames;
import fr.univ_amu.iut.supervision.persistance.DataBase;
import fr.univ_amu.iut.supervision.persistance.DbModel;
import fr.univ_amu.iut.supervision.supervision.Superviseur;
import fr.univ_amu.iut.supervision.web.ServeurWeb;
import fr.univ_amu.iut.supervision.web.sensor.SensorService;
import fr.univ_amu.iut.supervision.web.table.TableService;
import io.javalin.Javalin;

public final class Application {

    public static void main(String[] args) {
        Configuration config = Configuration.fromEnvironment();

        DataBase base = DataBase.connect(config);
        DbModel dbModel = new DbModel();
        ArchiveTrames archive = ArchiveTrames.connecter(config);

        TramesSource source = createSource(config);
        source.start(archive::archiver);

        Superviseur superviseur = new Superviseur(base);
        superviseur.start();

        SensorService sensorService = new SensorService(dbModel);
        TableService tableService = new TableService(dbModel);

        Javalin app = ServeurWeb.creer(dbModel);
        app.start(config.port());
    }

    private static TramesSource createSource(Configuration config) {
        if ("live-objects".equals(config.ingestionMode())) {
            return new SourceLiveObjects(config);
        }
        return new SourceMqtt(config);
    }
}
