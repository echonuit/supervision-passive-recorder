package fr.univ_amu.iut.supervision.web.sensor;

import java.sql.SQLException;

public class SensorService {

    private static final int DEV_EUI_MAX_LENGTH = 16;
    private static final int NAME_MAX_LENGTH = 100;

    private final DbModel dbModel;

    public SensorService(DbModel dbModel) {
        this.dbModel = dbModel;
    }

    public int addSensor(NewSensor sensor) throws SQLException {
        if (sensor.devEUI() == null || sensor.devEUI().isBlank()) {
            throw new IllegalArgumentException("devEUI is required");
        }
        if (sensor.devEUI().length() > DEV_EUI_MAX_LENGTH) {
            throw new IllegalArgumentException("devEUI must be at most 16 characters");
        }
        if (sensor.sensorName() == null || sensor.sensorName().isBlank()) {
            throw new IllegalArgumentException("Sensor name is required");
        }
        if (sensor.sensorName().length() > NAME_MAX_LENGTH) {
            throw new IllegalArgumentException("Sensor name must be at most 100 characters");
        }
        return dbModel.addSensor(sensor);
    }

    public int createGroup(String groupName) throws SQLException {
        if (groupName == null || groupName.isBlank()) {
            throw new IllegalArgumentException("Group name is required");
        }
        return dbModel.createGroup(groupName.trim());
    }
}