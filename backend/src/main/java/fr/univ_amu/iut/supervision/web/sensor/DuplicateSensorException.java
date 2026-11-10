package fr.univ_amu.iut.supervision.web.sensor;

public class DuplicateSensorException extends RuntimeException {
    public DuplicateSensorException(String devEUI) {
        super("A sensor with devEUI " + devEUI + " already exists");
    }
}