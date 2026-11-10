import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.sql.SQLException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

class SensorServiceTest {
    
    private static class FakeDbModel extends DbModel {
        NewSensor lastSensor;
        String lastGroupName;
        int idToReturn = 42;
        RuntimeException exceptionToThrow;

        @Override
        public int addSensor(NewSensor sensor) throws SQLException {
            if (exceptionToThrow != null) {
                throw exceptionToThrow;
            }
            lastSensor = sensor;
            return idToReturn;
        }

        @Override
        public int createGroup(String groupName) throws SQLException {
            lastGroupName = groupName;
            return idToReturn;
        }
    }

    private FakeDbModel fakeDb;
    private SensorService service;

    @BeforeEach
    void setUp() {
        fakeDb = new FakeDbModel();
        service = new SensorService(fakeDb);
    }

    private NewSensor sensor(String devEUI, String name) {
        return new NewSensor(devEUI, name, 3.14, 1.23, 2);
    }

    @Test
    void addSensor_withValidData_returnsGeneratedIdAndCallsDatabase() throws SQLException {
        NewSensor sensor = sensor("AMVIUS", "batman_exterminator");

        int id = service.addSensor(sensor);

        assertThat(id).isEqualTo(42);
        assertThat(fakeDb.lastSensor).isEqualTo(sensor);
    }

    @Test
    void addSensor_withoutGroupAndCoordinates_isAccepted() throws SQLException {
        NewSensor sensor = new NewSensor("AMVIUS", "capteur", null, null, null);

        int id = service.addSensor(sensor);

        assertThat(id).isEqualTo(42);
        assertThat(fakeDb.lastSensor).isEqualTo(sensor);
    }

    @Test
    void addSensor_withDevEuiOfExactlyMaxLength_isAccepted() throws SQLException {
        service.addSensor(sensor("A".repeat(16), "capteur"));

        assertThat(fakeDb.lastSensor).isNotNull();
    }

    @Test
    void addSensor_withNameOfExactlyMaxLength_isAccepted() throws SQLException {
        service.addSensor(sensor("AMVIUS", "n".repeat(100)));

        assertThat(fakeDb.lastSensor).isNotNull();
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "   "})
    void addSensor_withMissingDevEui_throwsAndDoesNotCallDatabase(String devEUI) {
        assertThatThrownBy(() -> service.addSensor(sensor(devEUI, "capteur")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("devEUI is required");

        assertThat(fakeDb.lastSensor).isNull();
    }

    @Test
    void addSensor_withTooLongDevEui_throwsAndDoesNotCallDatabase() {
        assertThatThrownBy(() -> service.addSensor(sensor("A".repeat(17), "capteur")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("devEUI must be at most 16 characters");

        assertThat(fakeDb.lastSensor).isNull();
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "   "})
    void addSensor_withMissingName_throwsAndDoesNotCallDatabase(String name) {
        assertThatThrownBy(() -> service.addSensor(sensor("AMVIUS", name)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Sensor name is required");

        assertThat(fakeDb.lastSensor).isNull();
    }

    @Test
    void addSensor_withTooLongName_throwsAndDoesNotCallDatabase() {
        assertThatThrownBy(() -> service.addSensor(sensor("AMVIUS", "n".repeat(101))))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Sensor name must be at most 100 characters");

        assertThat(fakeDb.lastSensor).isNull();
    }

    @Test
    void addSensor_whenDevEuiAlreadyExists_propagatesDuplicateException() {
        fakeDb.exceptionToThrow = new DuplicateSensorException("AMVIUS");

        assertThatThrownBy(() -> service.addSensor(sensor("AMVIUS", "capteur")))
                .isInstanceOf(DuplicateSensorException.class)
                .hasMessageContaining("AMVIUS");
    }

    @Test
    void addSensor_whenGroupDoesNotExist_propagatesGroupNotFoundException() {
        fakeDb.exceptionToThrow = new GroupNotFoundException(99);

        assertThatThrownBy(() -> service.addSensor(sensor("AMVIUS", "capteur")))
                .isInstanceOf(GroupNotFoundException.class)
                .hasMessageContaining("99");
    }

    @Test
    void createGroup_withValidName_returnsGeneratedId() throws SQLException {
        int id = service.createGroup("ma_recherche");

        assertThat(id).isEqualTo(42);
        assertThat(fakeDb.lastGroupName).isEqualTo("ma_recherche");
    }

    @Test
    void createGroup_trimsSurroundingSpaces() throws SQLException {
        service.createGroup("  ma_recherche  ");

        assertThat(fakeDb.lastGroupName).isEqualTo("ma_recherche");
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "   "})
    void createGroup_withMissingName_throwsAndDoesNotCallDatabase(String name) {
        assertThatThrownBy(() -> service.createGroup(name))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Group name is required");

        assertThat(fakeDb.lastGroupName).isNull();
    }

    @Test
    void duplicateSensorException_messageContainsDevEui() {
        assertThat(new DuplicateSensorException("ABC").getMessage())
                .isEqualTo("A sensor with devEUI ABC already exists");
    }

    @Test
    void groupNotFoundException_messageContainsGroupId() {
        assertThat(new GroupNotFoundException(7).getMessage()).isEqualTo("Group 7 does not exist");
    }
}