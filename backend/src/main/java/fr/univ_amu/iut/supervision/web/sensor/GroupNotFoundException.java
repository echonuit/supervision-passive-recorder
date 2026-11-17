package fr.univ_amu.iut.supervision.web.sensor;

public class GroupNotFoundException extends RuntimeException {
    public GroupNotFoundException(Integer groupId) {
        super("Group " + groupId + " does not exist");
    }
}
