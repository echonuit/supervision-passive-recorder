package fr.univ_amu.iut.supervision.ingestion;

import java.time.Instant;

/**
 * Une trame telle qu'elle arrive du réseau, avant décodage.
 *
 * @param idBox identifiant du boîtier émetteur, tel que fourni par la source
 * @param recueLe   instant de réception côté serveur
 * @param octets    charge utile brute
 */
public record TrameReceived(String idBox, Instant recueLe, byte[] octets) {}
