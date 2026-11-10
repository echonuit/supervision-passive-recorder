public class DuplicateSensorExeption enxtends RuntimeException {
    public DuplicateSensorExeption(String devEUI) {
        super("un capteur avec le devEUI " + devEUI + " existe ");
    }
}