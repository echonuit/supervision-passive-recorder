package fr.univ_amu.iut.supervision.web.sensor;

public record NewSensor(String devEUI, String sensorName, Double longitude, Double latitude, Integer groupId) {}
