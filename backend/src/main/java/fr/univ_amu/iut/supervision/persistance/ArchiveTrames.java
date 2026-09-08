package fr.univ_amu.iut.supervision.persistance;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import fr.univ_amu.iut.supervision.Configuration;
import fr.univ_amu.iut.supervision.ingestion.TrameRecue;
import fr.univ_amu.iut.supervision.trame.Hex;
import java.util.Date;
import org.bson.Document;

/**
 * Archivage brut de tout ce qui arrive du réseau, tel quel, dans la base
 * documentaire. On ne décode rien ici : l'archive sert à rejouer, à
 * enquêter, et à recalculer si une échelle se révèle fausse.
 */
public final class ArchiveTrames {

    private final MongoCollection<Document> trames;

    private ArchiveTrames(MongoCollection<Document> trames) {
        this.trames = trames;
    }

    public static ArchiveTrames connecter(Configuration config) {
        MongoClient client = MongoClients.create(config.mongoUrl());
        return new ArchiveTrames(client.getDatabase("supervision").getCollection("trames"));
    }

    public void archiver(TrameRecue trame) {
        trames.insertOne(new Document()
                .append("boitier", trame.idBoitier())
                .append("recueLe", Date.from(trame.recueLe()))
                .append("hex", Hex.depuisOctets(trame.octets())));
    }
}
