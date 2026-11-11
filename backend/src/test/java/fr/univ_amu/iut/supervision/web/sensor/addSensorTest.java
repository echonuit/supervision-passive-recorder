package fr.univ_amu.iut.supervision.web.sensor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import fr.univ_amu.iut.supervision.persistance.DbModel;
import java.sql.SQLException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

class SensorServiceTest {

    private static final String EUI = "AMVIUS";
    private static final String NAME = "capteur";

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
        NewSensor s = sensor(EUI, "batman_exterminator");

        int id = service.addSensor(s);

        assertThat(id).isEqualTo(42);
        assertThat(fakeDb.lastSensor).isEqualTo(s);
    }

    @Test
    void addSensor_withoutGroupAndCoordinates_isAccepted() throws SQLException {
        NewSensor s = new NewSensor(EUI, NAME, null, null, null);

        int id = service.addSensor(s);

        assertThat(id).isEqualTo(42);
        assertThat(fakeDb.lastSensor).isEqualTo(s);
    }

    @Test
    void addSensor_withDevEuiOfExactlyMaxLength_isAccepted() throws SQLException {
        service.addSensor(sensor("A".repeat(16), NAME));

        assertThat(fakeDb.lastSensor).isNotNull();
    }

    @Test
    void addSensor_withNameOfExactlyMaxLength_isAccepted() throws SQLException {
        service.addSensor(sensor(EUI, "n".repeat(100)));

        assertThat(fakeDb.lastSensor).isNotNull();
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "   "})
    void addSensor_withMissingDevEui_throwsAndDoesNotCallDatabase(String devEUI) {
        assertThatThrownBy(() -> service.addSensor(sensor(devEUI, NAME)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("devEUI est requis");

        assertThat(fakeDb.lastSensor).isNull();
    }

    @Test
    void addSensor_withTooLongDevEui_throwsAndDoesNotCallDatabase() {
        assertThatThrownBy(() -> service.addSensor(sensor("A".repeat(17), NAME)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("devEUI doit contenir au maximum 16 caractères");

        assertThat(fakeDb.lastSensor).isNull();
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "   "})
    void addSensor_withMissingName_throwsAndDoesNotCallDatabase(String name) {
        assertThatThrownBy(() -> service.addSensor(sensor(EUI, name)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Nom du capteur est requis");

        assertThat(fakeDb.lastSensor).isNull();
    }

    @Test
    void addSensor_withTooLongName_throwsAndDoesNotCallDatabase() {
        assertThatThrownBy(() -> service.addSensor(sensor(EUI, "n".repeat(101))))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Nom du capteur doit contenir au maximum 100 caractères");

        assertThat(fakeDb.lastSensor).isNull();
    }

    @Test
    void addSensor_whenDevEuiAlreadyExists_propagatesDuplicateException() {
        fakeDb.exceptionToThrow = new DuplicateSensorException(EUI);

        assertThatThrownBy(() -> service.addSensor(sensor(EUI, NAME)))
                .isInstanceOf(DuplicateSensorException.class)
                .hasMessageContaining(EUI);
    }

    @Test
    void addSensor_whenGroupDoesNotExist_propagatesGroupNotFoundException() {
        fakeDb.exceptionToThrow = new GroupNotFoundException(99);

        assertThatThrownBy(() -> service.addSensor(sensor(EUI, NAME)))
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
                .hasMessage("Nom du groupe est requis");

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
