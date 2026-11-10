import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class DbModel {

    private final DbConnection dbConnection = new DbConnection();

/*     public List<Media> getAllMedia() throws SQLException {
        List<Media> result = new ArrayList<>();

        try (Connection conn = dbConnection.connect();
             PreparedStatement stmt = conn.prepareStatement("SELECT * FROM media");
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                result.add(new Media(
                    rs.getInt("id"),
                    rs.getString("title"),
                    rs.getString("type")
                ));
            }
        }
        return result;
    } */
}