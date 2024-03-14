package DataBase;


import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Provides methods for performing actions on the database, with integration
 * to update or log through the ServerPortFrameController.
 */
public class DBController {
    private final Connection conn;

    public DBController(Connection conn) {
        this.conn = conn;
    }


    /**
     * Inserts a new record into the specified table and logs the action.
     *
     * @param tableName The name of the table where the record will be inserted.
     * @param values    The values to insert into the table. Assumes a prepared statement format.
     * @return true if the insert operation was successful, false otherwise.
     */
    public boolean insertRecord(String tableName, String columns, String... values) throws SQLException {
        if (tableName.isEmpty()) {
            throw new SQLException("Table name cannot be empty");
        }
        if (values.length == 0) {
            throw new SQLException("No values provided");
        }
        String sql = "INSERT INTO " + tableName + "(" + columns + ")" +" VALUES (" + String.join(", ", values) + ")";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                return true;
            }
        } catch (SQLException e) {
            throw new SQLException("Insert into " + tableName + " failed: " + e.getMessage());
        }
        return false;
    }

    /**
     * Updates records in the specified table and logs the action.
     *
     * @param tableName   The name of the table to update.
     * @param setClause   The SET clause specifying the columns to update and their new values.
     * @param whereClause The WHERE clause to specify which records to update.
     * @return true if the update operation was successful, false otherwise.
     */
    public boolean updateRecord(String tableName, String setClause, String whereClause) throws SQLException {
        if (tableName.isEmpty()) {
            throw new SQLException("Table name cannot be empty");
        }
        String sql = "UPDATE " + tableName + " SET " + setClause + " WHERE " + whereClause;
        System.out.println("Running Query: " + sql);
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new SQLException("Update in " + tableName + " failed: " + e.getMessage());
        }
    }


    /**
     * Deletes records from the specified table and logs the action.
     *
     * @param tableName   The name of the table from which to delete records.
     * @param whereClause The WHERE clause to specify which records to delete.
     * @return true if the delete operation was successful, false otherwise.
     */
    public boolean deleteRecord(String tableName, String whereClause) throws SQLException {
        if (tableName.isEmpty()) {
            throw new SQLException("Table name cannot be empty");
        }
        String sql = "DELETE FROM " + tableName + " WHERE " + whereClause;
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                return true;
            }
        } catch (SQLException e) {
            throw new SQLException("Delete from " + tableName + " failed: " + e.getMessage());
        }
        return false;
    }

    /**
     * Selects records from the specified table and logs the action.
     *
     * @param tableName   The name of the table from which to select records.
     * @param whereClause The WHERE clause to specify which records to select.
     * @return A ResultSet containing the selected records, or null if an error occurs.
     */
    public ResultSet selectRecords(String tableName, String whereClause) throws SQLException {
        String sql = "SELECT * FROM " + tableName + (whereClause.isEmpty() ? "" : " WHERE " + whereClause);
        try {
            PreparedStatement pstmt = conn.prepareStatement(sql);
            return pstmt.executeQuery();
        } catch (SQLException e) {
            throw new SQLException("Select from " + tableName + " failed: " + e.getMessage());
        }
    }
}
