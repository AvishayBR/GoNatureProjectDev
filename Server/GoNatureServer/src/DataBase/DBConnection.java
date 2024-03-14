package DataBase;

import Entities.*;
import ServerUIPageController.ServerPortFrameController;

import java.sql.*;
import java.util.ArrayList;

/**
 * Manages the database connection for the application.
 * This class is responsible for initializing the JDBC driver,
 * establishing a connection to the database, and providing access
 * to the established connection via a singleton pattern.
 */
public class DBConnection {
    private Connection conn;
    private static DBConnection dbConnection;
    private String schemaName;

    private final ServerPortFrameController serverController;

    private DBController dbController;

    /**
     * Private constructor to prevent instantiation.
     * Initializes the JDBC driver and sets up the database connection
     * using the provided server port frame controller.
     *
     * @param controller The server port frame controller used for retrieving
     *                   connection details and logging.
     */
    private DBConnection(ServerPortFrameController controller) throws ClassNotFoundException, SQLException {
        this.serverController = controller;
        if (!driverDefinition()) {
            throw new ClassNotFoundException("Driver definition failed");
        }
        if (!setConnection(controller.getURLComboBox(), controller.getUserName(), controller.getPassword())) {
            throw new SQLException("SQL connection failed");
        }
        this.dbController = new DBController(conn);
    }

    /**
     * Provides the singleton instance of the DBConnection.
     * If the instance does not exist, it is created using the provided controller.
     *
     * @param controller The server port frame controller used for retrieving
     *                   connection details and logging.
     * @return The singleton instance of DBConnection.
     */
    public static DBConnection getInstance(ServerPortFrameController controller) throws Exception {
        if (dbConnection == null) {
            dbConnection = new DBConnection(controller);
        }
        return dbConnection;
    }

    /**
     * Initializes the JDBC driver.
     * Logs the outcome of the driver initialization process.
     */
    private boolean driverDefinition() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver").getDeclaredConstructor().newInstance();
            this.serverController.addtolog("Driver definition succeed");
            return true;
        } catch (Exception ex) {
            this.serverController.addtolog("Driver definition failed");
            return false;
        }
    }

    /**
     * Sets up the database connection using the provided URL, user name, and password.
     * Logs the outcome of the connection process.
     *
     * @param url      The URL of the database.
     * @param user     The username for the database.
     * @param password The password for the database.
     */
    private boolean setConnection(String url, String user, String password) {
        try {
            this.schemaName = "gonature";
            this.conn = DriverManager.getConnection("jdbc:mysql://" + url + ":3306/" + this.schemaName + "?serverTimezone=Asia/Jerusalem&useSSL=false&allowPublicKeyRetrieval=true", user, password);
            this.serverController.addtolog("SQL connection succeed");
            return true;
        } catch (SQLException ex) {
            logSQLException(ex);
            return false;
        }
    }

    // Utility method to log SQL exceptions
    private void logSQLException(SQLException ex) {
        this.serverController.addtolog("SQLException: " + ex.getMessage());
        this.serverController.addtolog("SQLState: " + ex.getSQLState());
        this.serverController.addtolog("VendorError: " + ex.getErrorCode());
    }

    public void closeConnection() {
        try {
            this.conn.close();
        } catch (Exception ignored) {
        }
        this.serverController.addtolog("SQL connection closed");
        this.conn = null;
        dbConnection = null;
    }

    public User login(String username, String password) {
        try {
            String tableName = this.schemaName + ".users";
            String whereClause = "username='" + username + "' AND password='" + password + "'";
            ResultSet userCredentials = dbController.selectRecords(tableName, whereClause);

            if (userCredentials.next()) {
                int userRole = userCredentials.getInt("role");
                String userTypeTableName = userRole < 3 ? ".visitors" : userRole == 4 ? ".departmentmanagers" : ".parkemployees";
                ResultSet userGoNatureData = dbController.selectRecords(this.schemaName + userTypeTableName, "username='" + username + "'");

                if (userGoNatureData.next()) {
                    switch (userRole) {
                        case 1:
                            return new SingleVisitor(
                                    userCredentials.getString("username"),
                                    "",
                                    userGoNatureData.getString("emailAddress"),
                                    userGoNatureData.getString("VisitorID"),
                                    userGoNatureData.getString("firstName"),
                                    userGoNatureData.getString("lastName")
                            );
                        case 2:
                            return new VisitorGroupGuide(
                                    userCredentials.getString("username"),
                                    "",
                                    userGoNatureData.getString("emailAddress"),
                                    userGoNatureData.getString("VisitorID"),
                                    userGoNatureData.getString("firstName"),
                                    userGoNatureData.getString("lastName")
                            );
                        case 3:
                            ResultSet parkData = dbController.selectRecords(this.schemaName + ".parks", "ParkID=" + userGoNatureData.getString("ParkID"));
                            ResultSet managerData = dbController.selectRecords(this.schemaName + ".parkemployees", "ParkID=" + userGoNatureData.getString("ParkID") + " AND isParkManager=true");
                            if (parkData.next() && managerData.next()) {
                                return new ParkEmployee(
                                        userCredentials.getString("username"),
                                        "",
                                        userGoNatureData.getString("EmailAddress"),
                                        new Park(
                                                parkData.getString("ParkID"),
                                                parkData.getString("ParkName"),
                                                parkData.getInt("Capacity"),
                                                parkData.getInt("GapVisitorsCapacity"),
                                                parkData.getTimestamp("DefaultVisitationTime"),
                                                parkData.getInt("departmentID"),
                                                new ParkManager(
                                                        managerData.getString("username"),
                                                        "",
                                                        managerData.getString("EmailAddress"),
                                                        parkData.getString("ParkID")
                                                )
                                        )
                                );
                            }
                            break;
                        case 4:
                            return new ParkDepartmentManager(
                                    userCredentials.getString("username"),
                                    "",
                                    userGoNatureData.getString("emailAddress"),
                                    null,
                                    null,
                                    userGoNatureData.getInt("departmentID")
                            );
                        case 5:
                            return new ParkManager(
                                    userCredentials.getString("username"),
                                    "",
                                    userGoNatureData.getString("EmailAddress"),
                                    userGoNatureData.getString("ParkID")
                            );
                        case 6:
                            ResultSet parkDataSupport = dbController.selectRecords(this.schemaName + ".parks", "ParkID=" + userGoNatureData.getString("ParkID"));
                            ResultSet supportManagerData = dbController.selectRecords(this.schemaName + ".parkemployees", "ParkID=" + userGoNatureData.getString("ParkID") + " AND isParkManager=true");
                            if (parkDataSupport.next() && supportManagerData.next()) {
                                return new ParkSupportRepresentative(
                                        userCredentials.getString("username"),
                                        "",
                                        userGoNatureData.getString("emailAddress"),
                                        new Park(
                                                parkDataSupport.getString("ParkID"),
                                                parkDataSupport.getString("ParkName"),
                                                parkDataSupport.getInt("Capacity"),
                                                parkDataSupport.getInt("GapVisitorsCapacity"),
                                                parkDataSupport.getTimestamp("DefaultVisitationTime"),
                                                parkDataSupport.getInt("departmentID"),
                                                new ParkManager(
                                                        supportManagerData.getString("username"),
                                                        "",
                                                        supportManagerData.getString("EmailAddress"),
                                                        parkDataSupport.getString("ParkID")
                                                )
                                        )
                                );
                            }
                        default:
                            break;
                    }
                }
            }
            return null;
        } catch (SQLException e) {
            this.serverController.addtolog(e.getMessage());
            return null;
        }
    }

    public Order getUserOrderByUserID(String userID, String orderID) {
        try {
            String tableName = this.schemaName + ".visitors";
            String whereClause = "VisitorID='" + userID + "'";
            ResultSet userGoNatureData = dbController.selectRecords(tableName, whereClause);
            if (userGoNatureData.next()) {
                ResultSet orderData = dbController.selectRecords(this.schemaName + ".orders", "VisitorID='" + userID + "' AND OrderID=' " + orderID + "'");
                if (orderData.next()) {
                    return new Order(
                            orderData.getString("VisitorID"),
                            orderData.getString("ParkID"),
                            orderData.getTimestamp("VisitationDate"),
                            orderData.getString("ClientEmailAddress"),
                            orderData.getString("PhoneNumber"),
                            OrderStatus.values()[orderData.getInt("orderStatus") - 1],
                            orderData.getTimestamp("EnteredTime"),
                            orderData.getTimestamp("ExitedTime"),
                            orderData.getString("OrderID"),
                            OrderType.values()[orderData.getInt("OrderType") - 1],
                            orderData.getInt("NumOfVisitors")
                    );
                }
            }
            return null;
        } catch (SQLException e) {
            this.serverController.addtolog(e.getMessage());
            return null;
        }
    }

    public Order addOrder(Order order) {
        try {
            String tableName = this.schemaName + ".orders";
            String columns = "VisitorID, ParkID, VisitationDate, ClientEmailAddress, PhoneNumber, orderStatus, EnteredTime, ExitedTime, OrderType, NumOfVisitors";
            if (!dbController.insertRecord(tableName, columns, "'" + order.getVisitorID() + "'",
                    "'" + order.getParkID() + "'",
                    "'" + order.getVisitationDate().toString().substring(0, order.getVisitationDate().toString().length() - 2) + "'",
                    "'" + order.getClientEmailAddress() + "'",
                    "'" + order.getPhoneNumber() + "'",
                    String.valueOf(order.getOrderStatus().ordinal() + 1),
                    "'" + order.getEnteredTime().toString().substring(0, order.getVisitationDate().toString().length() - 2) + "'",
                    "'" + order.getExitedTime().toString().substring(0, order.getVisitationDate().toString().length() - 2) + "'",
                    String.valueOf(order.getOrderType().ordinal() + 1),
                    String.valueOf(order.getNumOfVisitors()))) {
                this.serverController.addtolog("Insert into " + tableName + " failed. Insert order:" + order);
                return null;
            }
            this.serverController.addtolog("Insert into " + tableName + " succeeded. Insert order:" + order);

            // Get the order from the DB (extract newly assigned order ID)
            ResultSet results = dbController.selectRecords(tableName, "VisitorID='" + order.getVisitorID() + "' AND ParkID='" + order.getParkID() + "' AND VisitationDate='" + order.getVisitationDate() + "'");
            if (results.next()) {
                return new Order(
                        results.getString("VisitorID"),
                        results.getString("ParkID"),
                        results.getTimestamp("VisitationDate"),
                        results.getString("ClientEmailAddress"),
                        results.getString("PhoneNumber"),
                        OrderStatus.values()[results.getInt("orderStatus") - 1],
                        results.getTimestamp("EnteredTime"),
                        results.getTimestamp("ExitedTime"),
                        results.getString("OrderID"),
                        OrderType.values()[results.getInt("OrderType") - 1],
                        results.getInt("NumOfVisitors")
                );
            }
            return null;
        } catch (SQLException e) {
            this.serverController.addtolog(e.getMessage());
            return null;
        }
    }


    public Order getOrderById(String OrderID) {
        try {
            String tableName = this.schemaName + ".orders";
            String whereClause = "OrderID=" + OrderID;
            ResultSet results = dbController.selectRecords(tableName, whereClause);
            if (results.next()) {
                return new Order(
                        results.getString("VisitorID"),
                        results.getString("ParkID"),
                        results.getTimestamp("VisitationDate"),
                        results.getString("ClientEmailAddress"),
                        results.getString("PhoneNumber"),
                        OrderStatus.values()[results.getInt("orderStatus") - 1],
                        results.getTimestamp("EnteredTime"),
                        results.getTimestamp("ExitedTime"),
                        results.getString("OrderID"),
                        OrderType.values()[results.getInt("OrderType") - 1],
                        results.getInt("NumOfVisitors")
                );
            }

            return new Order("", "", null, "", "", null, null, null, "", null, 0);
        } catch (SQLException e) {
            this.serverController.addtolog(e.getMessage());
            return null;
        }
    }

    public ArrayList<Order> getUserOrders(String visitorID) {
        try {
            String tableName = this.schemaName + ".orders";
            String whereClause = "VisitorID=" + visitorID;
            ResultSet results = dbController.selectRecords(tableName, whereClause);

            ArrayList<Order> orders = new ArrayList<>();
            while (results.next()) {
                orders.add(new Order(
                        results.getString("VisitorID"),
                        results.getString("ParkID"),
                        results.getTimestamp("VisitationDate"),
                        results.getString("ClientEmailAddress"),
                        results.getString("PhoneNumber"),
                        OrderStatus.values()[results.getInt("orderStatus") - 1],
                        results.getTimestamp("EnteredTime"),
                        results.getTimestamp("ExitedTime"),
                        results.getString("OrderID"),
                        OrderType.values()[results.getInt("OrderType") - 1],
                        results.getInt("NumOfVisitors")
                ));
            }

            return orders;
        } catch (SQLException e) {
            this.serverController.addtolog(e.getMessage());
            return null;
        }
    }

    public boolean updateOrderStatus(String orderID, OrderStatus status) {
        try {
            String tableName = this.schemaName + ".orders";
            String setClause = "OrderStatus=" + status;
            String whereClause = "OrderID=" + orderID;
            if (!dbController.updateRecord(tableName, setClause, whereClause)) {
                this.serverController.addtolog("Update in " + tableName + " failed. Update order status:" + orderID);
                return false;
            }
            return true;
        } catch (SQLException e) {
            this.serverController.addtolog(e.getMessage());
            return false;
        }
    }

    public boolean updateOrderEmailAddress(String orderID, String emailAddress) {
        try {
            String tableName = this.schemaName + ".orders";
            String setClause = "ClientEmailAddress=" + emailAddress;
            String whereClause = "OrderID=" + orderID;
            if (!dbController.updateRecord(tableName, setClause, whereClause)) {
                this.serverController.addtolog("Update in " + tableName + " failed. Update order status:" + orderID);
                return false;
            }
            return true;
        } catch (SQLException e) {
            this.serverController.addtolog(e.getMessage());
            return false;
        }
    }

    public boolean updateOrderStatusAsCancelled(String orderID) {
        return updateOrderStatus(orderID, OrderStatus.STATUS_CANCELLED);
    }

    public boolean checkOrderExists(String visitorID, String parkID, Timestamp visitationDate) {
        try {
            String tableName = this.schemaName + ".orders";
            String whereClause = "VisitorID=" + visitorID + " AND ParkID=" + parkID + " AND VisitationDate=" + visitationDate;
            ResultSet results = dbController.selectRecords(tableName, whereClause);
            return results.next();
        } catch (SQLException e) {
            this.serverController.addtolog(e.getMessage());
            return false;
        }
    }

    public String getParkNameByID(String parkID) {
        try {
            String tableName = this.schemaName + ".parks";
            String whereClause = "ParkID=" + parkID;
            ResultSet results = dbController.selectRecords(tableName, whereClause);
            if (results.next()) {
                return results.getString("ParkName");
            }
            return "";
        } catch (Exception e) {
            this.serverController.addtolog(e.getMessage());
            return null;
        }
    }

    public Park getParkDetails(String parkID) {
        try {
            String tableName = this.schemaName + ".parks";
            String whereClause = "ParkID=" + parkID;
            ResultSet results = dbController.selectRecords(tableName, whereClause);
            if (results.next()) {
                String pManagerId = results.getString("ParkManagerID");
                ResultSet managerResults = dbController.selectRecords(this.schemaName + ".parkemployees", "id=" + pManagerId);
                if (managerResults.next()) {
                    return new Park(
                            results.getString("ParkID"),
                            results.getString("ParkName"),
                            results.getInt("Capacity"),
                            results.getInt("GapVisitorsCapacity"),
                            results.getTimestamp("DefaultVisitationTime"),
                            results.getInt("Department"),
                            new ParkManager(
                                    managerResults.getString("username"),
                                    "",
                                    managerResults.getString("EmailAddress"),
                                    results.getString("ParkID")
                            )
                    );
                }
            }
            return new Park("", "", 0, 0, null, 0, null);
        } catch (SQLException e) {
            this.serverController.addtolog(e.getMessage());
            return null;
        }
    }
}
