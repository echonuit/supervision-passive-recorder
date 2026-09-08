package fr.univ_amu.iut.supervision;

/** Lecture de la configuration depuis l'environnement, avec des valeurs par défaut pour le poste de développement. */
public record Configuration(
        int port,
        String dbUrl,
        String dbUser,
        String dbPassword,
        String mongoUrl,
        String mqttHost,
        int mqttPort,
        String mqttTopic) {

    public static Configuration depuisEnvironnement() {
        return new Configuration(
                Integer.parseInt(env("PORT", "7070")),
                env("DB_URL", "jdbc:postgresql://localhost:5432/supervision"),
                env("DB_USER", "supervision"),
                env("DB_PASSWORD", "supervision"),
                env("MONGO_URL", "mongodb://localhost:27017"),
                env("MQTT_HOST", "localhost"),
                Integer.parseInt(env("MQTT_PORT", "1883")),
                env("MQTT_TOPIC", "simulateur/#"));
    }

    private static String env(String nom, String defaut) {
        String v = System.getenv(nom);
        return v == null || v.isBlank() ? defaut : v;
    }
}
