package fr.univ_amu.iut.supervision.web.sensor;

import fr.univ_amu.iut.supervision.persistance.DbModel;
import java.sql.SQLException;

public class SensorService {

    private static final int DEV_EUI_MAX_LENGTH = 16;
    private static final int NAME_MAX_LENGTH = 100;

    private final DbModel dbModel;

    public SensorService(DbModel dbModel) {
        this.dbModel = dbModel;
    }

    public int addSensor(NewSensor sensor, String userId) throws SQLException {
        validerDevEui(sensor.devEUI());
        validerNom(sensor.sensorName());
        return dbModel.addSensor(sensor, userId);
    }

    private void validerDevEui(String devEUI) {
        if (devEUI == null || devEUI.isBlank()) {
            throw new IllegalArgumentException("devEUI est requis");
        }
        if (devEUI.length() > DEV_EUI_MAX_LENGTH) {
            throw new IllegalArgumentException("devEUI doit contenir au maximum 16 caractères");
        }
    }

    private void validerNom(String nom) {
        if (nom == null || nom.isBlank()) {
            throw new IllegalArgumentException("Nom du capteur est requis");
        }
        if (nom.length() > NAME_MAX_LENGTH) {
            throw new IllegalArgumentException("Nom du capteur doit contenir au maximum 100 caractères");
        }
    }

    public int createGroup(String groupName) throws SQLException {
        if (groupName == null || groupName.isBlank()) {
            throw new IllegalArgumentException("Nom du groupe est requis");
        }
        return dbModel.createGroup(groupName.trim());
    }
}