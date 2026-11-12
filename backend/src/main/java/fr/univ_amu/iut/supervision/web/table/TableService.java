package fr.univ_amu.iut.supervision.web.table;

import fr.univ_amu.iut.supervision.persistance.DbModel;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

public class TableService {
    private final DbModel dbModel;

    public TableService(DbModel dbModel) {
        this.dbModel = dbModel;
    }

    public List<Map<String, Object>> getDashboardSensors(String userId) throws SQLException {
        return dbModel.getSensorsForTable(userId);
    }
}