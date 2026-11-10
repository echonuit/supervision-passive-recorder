public class GroupNotFoundException extends RuntimeException {
    public GroupNotFoundException(Integer groupId) {
        super("Le groupe avec l'id " + groupId + " n'existe pas");
    }
}