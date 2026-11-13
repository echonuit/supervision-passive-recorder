package fr.univ_amu.iut.supervision.web.table;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import fr.univ_amu.iut.supervision.persistance.DbModel;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class TableServiceTest {

    private DbModel dbModelMock;
    private TableService tableService;

    @BeforeEach
    void setUp() {
        dbModelMock = Mockito.mock(DbModel.class);
        tableService = new TableService(dbModelMock);
    }

    @Test
    void testGetDashboardSensorsSuccess() throws SQLException {
        String userId = "user123";
        List<Map<String, Object>> mockSensors =
                List.of(Map.of("id", 1, "name", "Capteur A", "battery", 85, "storage", 40, "status", "Stable"));

        when(dbModelMock.getSensorsForTable(userId)).thenReturn(mockSensors);

        List<Map<String, Object>> result = tableService.getDashboardSensors(userId);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Capteur A", result.get(0).get("name"));

        verify(dbModelMock, times(1)).getSensorsForTable(userId);
    }

    @Test
    void testGetDashboardSensorsSqlException() throws SQLException {
        String userId = "user123";
        when(dbModelMock.getSensorsForTable(userId)).thenThrow(new SQLException("Erreur BD"));

        assertThrows(SQLException.class, () -> {
            tableService.getDashboardSensors(userId);
        });

        verify(dbModelMock, times(1)).getSensorsForTable(userId);
    }
}
