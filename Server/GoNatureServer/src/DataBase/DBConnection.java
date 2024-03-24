package DataBase;

import Entities.*;
import GoNatureServer.GmailSender;
import ServerUIPageController.ServerUIFrameController;
import com.itextpdf.text.DocumentException;

import java.awt.*;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static CommonUtils.CommonUtils.convertMinutesToTimestamp;
import static CommonUtils.CommonUtils.getNextWeekHours;
import static CommonUtils.OrderProcessor.findBestCombination;
import static GoNatureServer.GoNatureServer.createExitTime;

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

    private final ServerUIFrameController serverController;

    private DBController dbController;

    /**
     * Private constructor to prevent instantiation.
     * Initializes the JDBC driver and sets up the database connection
     * using the provided server port frame controller.
     *
     * @param controller The server port frame controller used for retrieving
     *                   connection details and logging.
     */
    private DBConnection(ServerUIFrameController controller) throws ClassNotFoundException, SQLException {
        this.serverController = controller;
        if (!driverDefinition()) {
            throw new ClassNotFoundException("Driver definition failed");
        }
        if (!setConnection(controller.getURLComboBox(), controller.getUserName(), controller.getPassword())) {
            throw new SQLException("SQL connection failed");
        }
        this.dbController = new DBController(conn);
    }

    public DBController getDbController() {
        return dbController;
    }

    /**
     * Provides the singleton instance of the DBConnection.
     * If the instance does not exist, it is created using the provided controller.
     *
     * @param controller The server port frame controller used for retrieving
     *                   connection details and logging.
     * @return The singleton instance of DBConnection.
     */
    public static DBConnection getInstance(ServerUIFrameController controller) throws Exception {
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
     * Sets up the database connection using the provided URL, username, and password.
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


    //=================================================================================================================//
    //                                                                                                                 //
    //                                           VISITOR & USERS METHODS                                               //
    //                                                                                                                 //
    //=================================================================================================================//

    /**
     * Logs in a user with the provided username and password.
     *
     * @param username The username of the user.
     * @param password The password of the user.
     * @return A User object representing the logged-in user, or null if login fails or there is an error.
     */
    public User login(String username, String password) {
        try {
            String tableName = this.schemaName + ".users";
            String whereClause = "username='" + username + "' AND password='" + password + "'";
            ResultSet userCredentials = dbController.selectRecords(tableName, whereClause);

            if (userCredentials.next()) {
                int userRole = userCredentials.getInt("role");
                String userTypeTableName = userRole < 2 ? ".group_guides" : userRole == 3 ? ".department_managers" : ".park_employees";
                ResultSet userGoNatureData =
                        dbController.selectRecords(this.schemaName + userTypeTableName, "username='" + username + "'");

                if (userGoNatureData.next()) {
                    switch (userRole) {
                        case 1:
                            if (checkGroupGuide(userGoNatureData.getString("ID")) ) {
                                return new VisitorGroupGuide(
                                        "",
                                        "",
                                        "",
                                        "",
                                        "",
                                        ""
                                );
                            }
                            return new VisitorGroupGuide(
                                    userCredentials.getString("username"),
                                    "",
                                    userGoNatureData.getString("emailAddress"),
                                    userGoNatureData.getString("ID"),
                                    userGoNatureData.getString("firstName"),
                                    userGoNatureData.getString("lastName")
                            );
                        case 2:
                            ResultSet parkData = dbController.selectRecords(this.schemaName + ".parks", "ParkID=" + userGoNatureData.getString("ParkID"));
                            ResultSet managerData = dbController.selectRecords(this.schemaName + ".park_employees", "ParkID=" + userGoNatureData.getString("ParkID") + " AND isParkManager=true");
                            if (parkData.next() && managerData.next()) {
                                return new ParkEmployee(
                                        userGoNatureData.getString("firstname"),
                                        userGoNatureData.getString("lastname"),
                                        userCredentials.getString("username"),
                                        "",
                                        userGoNatureData.getString("EmailAddress"),
                                        new Park(
                                                parkData.getString("ParkID"),
                                                parkData.getString("ParkName"),
                                                parkData.getInt("Capacity"),
                                                parkData.getInt("GapVisitorsCapacity"),
                                                convertMinutesToTimestamp(parkData.getInt("DefaultVisitationTime")),
                                                parkData.getInt("departmentID"),
                                                new ParkManager(
                                                        managerData.getString("username"),
                                                        "",
                                                        managerData.getString("EmailAddress"),
                                                        parkData.getString("ParkID"),
                                                        managerData.getString("firstName"),
                                                        managerData.getString("lastName")
                                                )
                                        )
                                );
                            }
                            break;
                        case 3:
                            return new ParkDepartmentManager(
                                    userCredentials.getString("username"),
                                    "",
                                    userGoNatureData.getString("emailAddress"),
                                    null,
                                    null,
                                    userGoNatureData.getInt("departmentID"),
                                    userGoNatureData.getString("firstName"),
                                    userGoNatureData.getString("lastName")
                            );
                        case 4:
                            return new ParkManager(
                                    userCredentials.getString("username"),
                                    "",
                                    userGoNatureData.getString("EmailAddress"),
                                    userGoNatureData.getString("ParkID"),
                                    userGoNatureData.getString("firstName"),
                                    userGoNatureData.getString("lastName")
                            );
                        case 5:
                            ResultSet parkDataSupport = dbController.selectRecords(this.schemaName + ".parks", "ParkID=" + userGoNatureData.getString("ParkID"));
                            ResultSet parkManagerData = dbController.selectRecords(this.schemaName + ".park_employees", "ParkID=" + userGoNatureData.getString("ParkID") + " AND isParkManager=true");
                            if (parkDataSupport.next() && parkManagerData.next()) {
                                return new ParkSupportRepresentative(
                                        userCredentials.getString("username"),
                                        "",
                                        userGoNatureData.getString("emailAddress"),
                                        new Park(
                                                parkDataSupport.getString("ParkID"),
                                                parkDataSupport.getString("ParkName"),
                                                parkDataSupport.getInt("Capacity"),
                                                parkDataSupport.getInt("GapVisitorsCapacity"),
                                                convertMinutesToTimestamp(parkDataSupport.getInt("DefaultVisitationTime")),
                                                parkDataSupport.getInt("departmentID"),
                                                new ParkManager(
                                                        parkManagerData.getString("username"),
                                                        "",
                                                        parkManagerData.getString("EmailAddress"),
                                                        parkDataSupport.getString("ParkID"),
                                                        parkManagerData.getString("firstName"),
                                                        parkManagerData.getString("lastName")

                                                )
                                        ),
                                        userGoNatureData.getString("firstName"),
                                        userGoNatureData.getString("lastName")
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

    /**
     * Retrieves an order by user ID and order ID.
     *
     * @param userID  The ID of the user.
     * @param orderID The ID of the order.
     * @return The order associated with the specified user ID and order ID, or null if not found or there is an error.
     */
    public Order getUserOrderByUserID(String userID, String orderID) {
        try {
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
            return null;
        } catch (SQLException e) {
            this.serverController.addtolog(e.getMessage());
            return null;
        }
    }

    /**
     * Adds an order to the database.
     *
     * @param order The order to be added.
     * @return The added order with the assigned order ID, or null if the insertion fails or there is an error.
     */
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
            ResultSet results = dbController.selectRecordsFields(tableName, "VisitorID='" + order.getVisitorID() + "' AND ParkID='" + order.getParkID() + "' AND VisitationDate='" + order.getVisitationDate() + "'", "OrderID");
            if (results.next()) {
                order.setOrderID(results.getString("OrderID"));
                return order;
            }
            return null;
        } catch (SQLException e) {
            this.serverController.addtolog(e.getMessage());
            return null;
        }
    }

    public boolean checkOrderPayment(Order order) {
        try {
            String orderID = order.getOrderID();
            String tableName1 = this.schemaName + ".payments";
            ResultSet orderPayment = dbController.selectRecordsFields(tableName1, "OrderID=" + orderID, "paid");
            if (!orderPayment.next()) {
                return false;
            }
            return orderPayment.getInt("paid") == 1;
        } catch (SQLException e) {
            this.serverController.addtolog(e.getMessage());
            return false;
        }
    }

    /**
     * Retrieves an order by its ID.
     *
     * @param OrderID The ID of the order to retrieve.
     * @return The order with the specified ID, or null if the order is not found or there is an error.
     */
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

    /**
     * Retrieves all orders associated with a specific visitor.
     *
     * @param visitorID The ID of the visitor.
     * @return An ArrayList containing orders associated with the specified visitor, or null if there is an error.
     */
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

    /**
     * Updates the status of an order.
     *
     * @param orderID The ID of the order to be updated.
     * @param status  The new status of the order.
     * @return True if the order status is successfully updated, false otherwise.
     */
    public boolean updateOrderStatus(String orderID, OrderStatus status) {
        try {
            String tableName = this.schemaName + ".orders";
            String setClause = "orderStatus=" + status.getOrderStatus();
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

    /**
     * Updates the details of an order.
     *
     * @param details An array containing the order details in the following format: [orderID, phoneNumber, clientEmailAddress].
     * @return True if the order details are successfully updated, false otherwise.
     */
    public boolean updateOrderDetails(String[] details) {
        try {
            String tableName = this.schemaName + ".orders";
            String setClause = "ClientEmailAddress='" + details[2] + "' , PhoneNumber='" + details[1] + "'";
            String whereClause = "OrderID='" + details[0] + "'";
            if (!this.dbController.updateRecord(tableName, setClause, whereClause)) {
                this.serverController.addtolog("Update in " + tableName + " failed. Update order status:" + details[0]);
                return false;
            } else {
                return true;
            }
        } catch (SQLException e) {
            this.serverController.addtolog(e.getMessage());
            return false;
        }
    }

    /**
     * Updates the status of an order to "cancelled".
     *
     * @param orderID The ID of the order to be updated.
     * @return True if the order status is successfully updated to "cancelled", false otherwise.
     */
    public boolean updateOrderStatusAsCancelled(String orderID) {
        return updateOrderStatus(orderID, OrderStatus.STATUS_CANCELLED);
    }

    /**
     * Checks if an order exists for a specific visitor, park, and visitation date.
     *
     * @param visitorID      The ID of the visitor.
     * @param parkID         The ID of the park.
     * @param visitationDate The visitation date.
     * @return True if an order exists for the specified visitor, park, and visitation date, false otherwise.
     */
    public boolean checkOrderExists(String visitorID, String parkID, Timestamp visitationDate) {
        try {
            String tableName = this.schemaName + ".orders";
            String whereClause = "VisitorID=" + visitorID + " AND ParkID=" + parkID + " AND VisitationDate= '" + visitationDate + "'";
            ResultSet results = dbController.selectRecords(tableName, whereClause);
            return results.next();
        } catch (SQLException e) {
            this.serverController.addtolog(e.getMessage());
            return false;
        }
    }

    /**
     * Retrieves the details of a specific park from the database.
     * The method takes a park ID as a parameter and fetches the corresponding park details.
     * It also fetches the details of the park manager associated with the park.
     *
     * @param parkID The ID of the park for which to fetch the details.
     * @return A Park object containing the details of the park and its manager.
     * Returns a new Park object with default values if no matching park is found.
     * Returns null if a SQLException is thrown.
     */
    public Park getParkDetails(String parkID) {
        try {
            String tableName = this.schemaName + ".parks";
            String whereClause = "ParkID=" + parkID;
            ResultSet results = dbController.selectRecords(tableName, whereClause);
            if (results.next()) {
                String pManagerId = results.getString("ParkManagerID");
                ResultSet managerResults = dbController.selectRecords(this.schemaName + ".park_employees", "id=" + pManagerId);
                if (managerResults.next()) {
                    return new Park(
                            results.getString("ParkID"),
                            results.getString("ParkName"),
                            results.getInt("Capacity"),
                            results.getInt("GapVisitorsCapacity"),
                            convertMinutesToTimestamp(results.getInt("DefaultVisitationTime")),
                            results.getInt("departmentID"),
                            new ParkManager(
                                    managerResults.getString("username"),
                                    "",
                                    managerResults.getString("EmailAddress"),
                                    results.getString("ParkID"),
                                    managerResults.getString("firstName"),
                                    managerResults.getString("lastName")
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

    //=================================================================================================================//
    //                                                                                                                 //
    //                                           PARK EMPLOYEES EXCLUSIVE METHODS                                      //
    //                                                                                                                 //
    //=================================================================================================================//

    /**
     * Marks an order as paid by inserting a record into the payments table and updating the order status.
     *
     * @param order The order to be marked as paid.
     * @return True if the order is successfully marked as paid, false otherwise.
     */
    public boolean markOrderAsPaid(Order order) {
        try {
            String orderID = order.getOrderID();
            String tableName = this.schemaName + ".orders";
            String setClause = "orderStatus=" + OrderStatus.STATUS_CONFIRMED_PAID.getOrderStatus();
            String whereClause = "OrderID=" + orderID;
            if (!dbController.updateRecord(tableName, setClause, whereClause)) {
                this.serverController.addtolog("Update in " + tableName + " failed. Mark order as paid:" + orderID);
                return false;
            }

            String tableName2 = this.schemaName + ".payments";
            String columns = "OrderID, paid, price";
            ResultSet orderPayment = dbController.selectRecordsFields(tableName2, "OrderID=" + orderID, "paid");
            if (orderPayment.next()) {
                return true;
            }

            if (!dbController.insertRecord(tableName2, columns, orderID, "true", String.valueOf(order.getNumOfVisitors() * Order.pricePerVisitor))) {
                this.serverController.addtolog("Insert into " + this.schemaName + ".payments failed. Mark order as paid:" + orderID);
                return false;
            }
            return true;
        } catch (SQLException e) {
            this.serverController.addtolog(e.getMessage());
            return false;
        }
    }

    /**
     * Sets the exit time of an order to now.
     *
     * @param orderID the order ID.
     * @return a message indicating the result of the operation, null if successful.
     */
    public String setExitTimeOfOrder(String orderID)
    {
        try {
            String tableName = this.schemaName + ".orders";
            String whereClause = "orderStatus <> " + OrderStatus.STATUS_SPONTANEOUS_ORDER.getOrderStatus() + " AND OrderID='" + orderID + "'";
            ResultSet results = dbController.selectRecordsFields(tableName, whereClause, "ExitedTime", "VisitationDate");
            if (!results.next()) {
                return "Order ID does not exist.";
            }

            Timestamp currentTime = new Timestamp(System.currentTimeMillis());
            Timestamp exitTime = results.getTimestamp("ExitedTime");
            // Extract year and month components from the current time
            LocalDate currentDate = currentTime.toLocalDateTime().toLocalDate();
            int currentYear = currentDate.getYear();
            int currentMonth = currentDate.getMonthValue();

            // Extract year and month components from the exit time
            LocalDate exitDate = exitTime.toLocalDateTime().toLocalDate();
            int exitYear = exitDate.getYear();
            int exitMonth = exitDate.getMonthValue();
            if (currentYear != exitYear || currentMonth != exitMonth) {
                return "Order is not for today.";
            }

            if (currentTime.compareTo(exitTime) > 0) {
                return "Order has already been fulfilled.";
            }

            if (!dbController.updateRecord(tableName, "ExitedTime=CURRENT_TIMESTAMP(), orderStatus=" + OrderStatus.STATUS_FULFILLED.getOrderStatus(), whereClause)) {
                return "Update failed. Please try again.";
            }

            return null;
        } catch (SQLException e) {
            this.serverController.addtolog(e.getMessage());
            return "Something went wrong... please try again later.";
        }
    }

    public String activateGroupGuide(String groupGuideID)
    {
        try {
            String tableName = this.schemaName + ".group_guides";
            String whereClause = "ID='" + groupGuideID + "'";
            ResultSet resultSet = dbController.selectRecordsFields(tableName, whereClause, "pendingStatus");
            if (!resultSet.next())
                return "Group guide ID does not exist.";
            if (!resultSet.getBoolean("pendingStatus"))
                return  "Group guide has already been authorized.";

            if (!dbController.updateRecord(tableName, "pendingStatus=false", whereClause))
                return "Group guide authorization failed. Try again later.";
            return null;
        } catch (SQLException e) {
            this.serverController.addtolog(e.getMessage());
            return "Something went wrong... Please try again later.";
        }
    }

    //=================================================================================================================//
    //                                                                                                                 //
    //                                           MANAGERS EXCLUSIVE METHODS                                            //
    //                                                                                                                 //
    //=================================================================================================================//

    /**
     * Retrieves requests from a park manager for a specific department.
     *
     * @param departmentID The ID of the department.
     * @return An ArrayList of requests from the park manager, or null if there is an error.
     */
    public ArrayList<RequestChangingParkParameters> getRequestsFromParkManager(Integer departmentID) {
        try {
            String tableName = this.schemaName + ".park_parameters_requests";
            String whereClause = "DepartmentID=" + departmentID + " AND status=" + RequestStatus.REQUEST_PENDING.getRequestStatus();
            ResultSet results = dbController.selectRecords(tableName, whereClause);
            ArrayList<RequestChangingParkParameters> requests = new ArrayList<>();
            while (results.next()) {
                ResultSet parkResults = dbController.selectRecords(this.schemaName + ".parks", "ParkID=" + results.getString("ParkID"));
                if (parkResults.next()) {
                    ResultSet parkManagerResults = dbController.selectRecords(this.schemaName + ".park_employees", "id=" + parkResults.getString("ParkManagerID") + " AND isParkManager=true");
                    if (parkManagerResults.next()) {
                        requests.add(new RequestChangingParkParameters(
                                new Park(
                                        parkResults.getString("ParkID"),
                                        parkResults.getString("ParkName"),
                                        parkResults.getInt("Capacity"),
                                        parkResults.getInt("GapVisitorsCapacity"),
                                        convertMinutesToTimestamp(parkResults.getInt("DefaultVisitationTime")),
                                        parkResults.getInt("DepartmentID"),
                                        new ParkManager(
                                                parkManagerResults.getString("username"),
                                                "",
                                                parkManagerResults.getString("EmailAddress"),
                                                parkManagerResults.getString("ParkID"),
                                                parkManagerResults.getString("firstName"),
                                                parkManagerResults.getString("lastName")
                                        )
                                ),
                                ParkParameters.values()[results.getInt("Parameter") - 1],
                                results.getDouble("RequestedValue"),
                                RequestStatus.values()[results.getInt("Status") - 1]
                        ));
                    }
                }
            }
            return requests;
        } catch (SQLException e) {
            this.serverController.addtolog(e.getMessage());
            return null;
        }
    }

    /**
     * Authorizes a park request, updating the park parameters and request status in the database.
     *
     * @param req The request to authorize, containing details about the park, parameter, and requested value.
     * @return {@code true} if the authorization process succeeds, {@code false} otherwise.
     */
    public boolean authorizeParkRequest(RequestChangingParkParameters req) {
        try {
            String tableName = this.schemaName + ".park_parameters_requests";
            String setClause = "handleDate = CURRENT_TIMESTAMP(), status=" + RequestStatus.REQUEST_ACCEPTED.getRequestStatus();
            String whereClause = "ParkID=" + req.getPark().getParkID() + " AND parameter=" + req.getParameter().getParameterVal() + " AND requestedValue=" + req.getRequestedValue();
            if (!dbController.updateRecord(tableName, setClause, whereClause)) {
                this.serverController.addtolog("Update in " + tableName + " failed. Authorize park request:" + req);
                return false;
            }

            String tableName2 = this.schemaName + ".parks";
            String setClause2 = req.getParameter().getColumnName() + "=" + req.getRequestedValue();
            String whereClause2 = "ParkID=" + req.getPark().getParkID();
            if (!dbController.updateRecord(tableName2, setClause2, whereClause2)) {
                this.serverController.addtolog("Update in " + tableName2 + " failed. Authorize park request:" + req);
                return false;
            }
            return true;
        } catch (SQLException e) {
            this.serverController.addtolog(e.getMessage());
            return false;
        }
    }

    /**
     * Unauthorizes a park parameter change request.
     *
     * @param req The request to be unauthorized.
     * @return {@code true} if the request is successfully unauthorized, {@code false} otherwise.
     */
    public boolean unauthorizeParkRequest(RequestChangingParkParameters req) {
        try {
            String tableName = this.schemaName + ".park_parameters_requests";
            String setClause = "Status=" + RequestStatus.REQUEST_DECLINED.getRequestStatus() + ", handleDate=CURRENT_TIMESTAMP()";
            String whereClause = "ParkID=" + req.getPark().getParkID() + " AND parameter=" + req.getParameter().getParameterVal() + " AND requestedValue=" + req.getRequestedValue();
            if (!dbController.updateRecord(tableName, setClause, whereClause)) {
                this.serverController.addtolog("Update in " + tableName + " failed. Authorize park request:" + req);
                return false;
            }
            return true;
        } catch (SQLException e) {
            this.serverController.addtolog(e.getMessage());
            return false;
        }
    }

    /**
     * Submits requests to the department for changing park parameters.
     *
     * @param requests A map containing the park parameters to be changed and their corresponding request objects.
     * @return {@code true} if all requests were successfully submitted, {@code false} otherwise.
     */
    public boolean submitRequestsToDepartment(Map<ParkParameters, RequestChangingParkParameters> requests) {
        try {
            String tableName = this.schemaName + ".park_parameters_requests";
            for (RequestChangingParkParameters req : requests.values()) {
                String columns = "ParkID, DepartmentID, parameter, requestedValue, status";
                if (!dbController.insertRecord(tableName, columns, "'" + req.getPark().getParkID() + "'",
                        String.valueOf(req.getPark().getDepartment()),
                        String.valueOf(req.getParameter().getParameterVal()),
                        String.valueOf(req.getRequestedValue()),
                        String.valueOf(req.getStatus().getRequestStatus()))) {
                    this.serverController.addtolog("Insert into " + tableName + " failed. Insert request:" + req);
                    return false;
                }
            }
            return true;
        } catch (SQLException e) {
            this.serverController.addtolog(e.getMessage());
            return false;
        }
    }

    public byte[] getReportBlob(boolean isDepartmentReport, String type, String month, String year, String bodyID) {
        try {
            String reportTable = isDepartmentReport ? ".department_manager_reports" : ".park_manager_reports";
            String bodyColumn = isDepartmentReport ? "departmentId" : "parkID";
            String tableName = this.schemaName + reportTable;
            String whereClause = "reportType='" + type + "' AND month='" + month + "' AND year='" + year + "'" + " AND " + bodyColumn + "='" + bodyID + "'";
            ResultSet results = dbController.selectRecordsFields(tableName, whereClause, "blobData");
            if (results.next()) {
                Blob blob = results.getBlob("blobData");
                byte[] bytes = blob.getBytes(1, (int) blob.length());
                blob.free();
                return bytes;
            }
            return null;
        } catch (SQLException e) {
            this.serverController.addtolog(e.getMessage());
            return null;
        }
    }

    public String getDepartmentIDByManagerID(String managerID) {
        try {
            String tableName = this.schemaName + ".department_managers";
            String whereClause = "username='" + managerID + "'";
            ResultSet results = dbController.selectRecordsFields(tableName, whereClause, "departmentID");
            if (results.next()) {
                return results.getString("departmentID");
            }
            return null;
        } catch (SQLException e) {
            this.serverController.addtolog(e.getMessage());
            return null;
        }
    }

    /**
     * Generates a visitation report for a specific department.
     * The report contains the average time spent by visitors in the park for each day of the current month.
     * As well as the distribution of visitors by order type (single family/group) for the month.
     * The report is saved as a PDF file and stored in the database.
     * The report is generated for the current month only.
     * The report is generated for the department with the specified ID.
     * @param departmentID The ID of the department for which to generate the report.
     * @return {@code true} if the report is successfully generated and stored, {@code false} otherwise.
     */
    public boolean generateVisitationReport(String departmentID) {
        try {
            String reportName = "visitations";

            // Retrieve ParkIDs matching the departmentID
            StringBuilder parkIDs = this.getAllDepartmentParksChained(departmentID);

            String ordersTableName = this.schemaName + ".orders" + " AS o";
            String joinClause = " JOIN gonature.parks AS p ON o.ParkID = p.ParkID";
            String visitationDateField = "DATE(o.VisitationDate) AS VisitationDate";
            String avgTimeSpentFieldSingleFamilyOrders = "CAST(AVG(CASE WHEN o.OrderType = 1 THEN TIMESTAMPDIFF(MINUTE, o.EnteredTime, o.ExitedTime) END) AS DOUBLE) AS AverageTimeSpent_1";
            String avgTimeSpentFieldGroupOrders = "CAST(AVG(CASE WHEN o.OrderType = 2 THEN TIMESTAMPDIFF(MINUTE, o.EnteredTime, o.ExitedTime) END) AS DOUBLE) AS AverageTimeSpent_2";
            String ordersWhereClause =
                    "p.departmentID='" + departmentID + "' AND YEAR(o.VisitationDate) = YEAR(CURRENT_DATE) AND MONTH(o.VisitationDate) = MONTH(CURRENT_DATE) AND DAY(o.VisitationDate) <= DAY(CURRENT_DATE) AND (orderStatus=" + OrderStatus.STATUS_FULFILLED.getOrderStatus() + " OR orderStatus=" + OrderStatus.STATUS_SPONTANEOUS_ORDER.getOrderStatus() + ") GROUP BY DATE(o.VisitationDate)";
            String orderByClause = " ORDER BY DATE(VisitationDate) ASC";
            ResultSet results =
                    dbController.selectRecordsFields(ordersTableName + joinClause, ordersWhereClause + orderByClause, visitationDateField, avgTimeSpentFieldSingleFamilyOrders, avgTimeSpentFieldGroupOrders);

            String entranceSelectFields = "o.ParkID, p.ParkName, o.OrderType, o.VisitationDate AS ReservationTime, o.EnteredTime AS EntranceTime, TIMEDIFF(o.ExitedTime, o.EnteredTime) AS TimeSpent";
            String entranceWhereClause = "o.ParkID IN (" + parkIDs + ") AND YEAR(o.VisitationDate) = YEAR(CURRENT_DATE) AND MONTH(o.VisitationDate) = MONTH(CURRENT_DATE) AND DAY(o.VisitationDate) <= DAY(CURRENT_DATE)";
            String entranceJoinClause = " JOIN gonature.parks AS p ON o.ParkID = p.ParkID";
            String entranceOrderByClause = " ORDER BY o.ParkID, ReservationTime ASC";
            ResultSet entranceResults = dbController.selectRecordsFields(ordersTableName + entranceJoinClause, entranceWhereClause + entranceOrderByClause, entranceSelectFields);

            VisitationReport visitationReport = new VisitationReport(Integer.valueOf(departmentID), "timespent", results);
            visitationReport.addReportData("entrance", entranceResults);


            Blob generatedBlob = visitationReport.createPDFBlob();
            results.close();
            entranceResults.close();
            return this.handleInsertionDepartmentReports(departmentID, reportName, generatedBlob);
        } catch (SQLException | DocumentException | IOException e) {
            this.serverController.addtolog(e.getMessage());
            return false;
        }
    }

    /**
     * Generates a visitation report for a specific park.
     * The report contains the distribution of cancelled and unfulfilled orders per day for the current month.
     * As well as the distribution of cancelled and unfulfilled orders by order type (single family/group) and by park.
     * The report is saved as a PDF file and stored in the database.
     * The report is generated for the current month only.
     * The report is generated for the park with the specified ID.
     * @param departmentID The ID of the department for which to generate the report.
     * @return {@code true} if the report is successfully generated and stored, {@code false} otherwise.
     */
    public boolean generateCancellationsReport(String departmentID) {
        try {
            String reportName = "cancellations";

            // Retrieve ParkIDs matching the departmentID
            ResultSet parkIDs = this.getDepartmentParkIDs(departmentID);

            HashMap<String, ResultSet> departmentData = new HashMap<>();

            // Retrieve Distribution of Cancelled Orders for the Entire Department
            String ordersTableName = this.schemaName + ".orders" + " AS o";
            String joinClause = " JOIN gonature.parks AS p ON o.ParkID = p.ParkID";
            String selectFields = "DAY(o.VisitationDate) AS date, COUNT(*) as TotalCancelledOrders";
            String ordersWhereClause =
                    "o.ParkID IN (" + this.getAllDepartmentParksChained(departmentID) + ")";
            String ordersWhereVisitationDate = " AND YEAR(o.VisitationDate) = YEAR(CURRENT_DATE) AND MONTH(o.VisitationDate) = MONTH(CURRENT_DATE) AND DAY(o.VisitationDate) <= DAY(CURRENT_DATE)";
            String ordersWhereStatus = " AND orderStatus IN ('" +
                            OrderStatus.STATUS_CONFIRMED_AND_ABSENT.getOrderStatus() + "', '" + OrderStatus.STATUS_CANCELLED.getOrderStatus() +
                            "')";
            String groupByClause = " GROUP BY o.ParkID, p.ParkName, DAY(o.VisitationDate)";
            String orderByClause = " ORDER BY date ASC";
            ResultSet departmentResults =
                    dbController.selectRecordsFields(ordersTableName + joinClause, ordersWhereClause + ordersWhereStatus + ordersWhereVisitationDate + groupByClause + orderByClause, selectFields);
            if (!departmentResults.next()) {
                return false;
            }
            departmentData.put("distribution", departmentResults);

            // Retrieve Average Distribution of Cancelled Orders for the Entire Department
            groupByClause = " GROUP BY DAY(o.VisitationDate)";
            selectFields = "DAY(o.VisitationDate) AS date, AVG(CASE WHEN orderStatus IN ('"
                    + OrderStatus.STATUS_CONFIRMED_AND_ABSENT.getOrderStatus() + "', '" + OrderStatus.STATUS_CANCELLED.getOrderStatus() +
                    "') THEN 1 ELSE 0 END) as AverageCancelledOrders";
            ResultSet departmentAvgResults =
                    dbController.selectRecordsFields(ordersTableName + joinClause, ordersWhereClause + ordersWhereVisitationDate + groupByClause + orderByClause, selectFields);
            if (!departmentAvgResults.next()) {
                return false;
            }
            departmentData.put("average", departmentAvgResults);

            CancellationReport cancellationReport = new CancellationReport(Integer.valueOf(departmentID), departmentData);

            // Retrieve Distribution and Average of Cancelled Orders for Each Park In the Department
            String parkID = "";
            ordersTableName = this.schemaName + ".orders" + " AS o";
            joinClause = " JOIN gonature.parks AS p ON o.ParkID = p.ParkID";
            selectFields = "o.ParkID, p.ParkName, DAY(o.VisitationDate) AS date, COUNT(*) as TotalCancelledOrders";
            String selectFieldsAvg = "DAY(o.VisitationDate) AS date, AVG(CASE WHEN orderStatus IN ("
                    + OrderStatus.STATUS_CONFIRMED_AND_ABSENT.getOrderStatus() + "," + OrderStatus.STATUS_CANCELLED.getOrderStatus() +
                    ") THEN 1 ELSE 0 END) as AverageCancelledOrders";
            String orderByClauseAvg = " ORDER BY date ASC";
            String groupByClauseAvg = " GROUP BY DAY(o.VisitationDate)";
            groupByClause = " GROUP BY o.ParkID, p.ParkName, DAY(o.VisitationDate)";
            orderByClause = " ORDER BY o.ParkID ASC, date ASC";

            HashMap<String, ResultSet> parkCancellations = new HashMap<>();
            while (parkIDs.next()) {
                parkID = parkIDs.getString("ParkID");
                ordersWhereClause = "o.ParkID='" + parkID + "'";
                ResultSet parkDistResults =
                        dbController.selectRecordsFields(ordersTableName + joinClause, ordersWhereClause + ordersWhereStatus + ordersWhereVisitationDate + groupByClause + orderByClause, selectFields);
                if (!parkDistResults.next()) {
                    return false;
                }
                parkCancellations.put("distribution_" + parkID, parkDistResults);

                ResultSet parkAvgResults =
                        dbController.selectRecordsFields(ordersTableName + joinClause, ordersWhereClause + ordersWhereVisitationDate + groupByClauseAvg + orderByClauseAvg, selectFieldsAvg);
                if (!parkAvgResults.next()) {
                    return false;
                }
                parkCancellations.put("average_" + parkID, parkAvgResults);
            }
            cancellationReport.setParksData(parkCancellations);


            Blob generatedBlob = cancellationReport.createPDFBlob();
            departmentResults.close();
            departmentAvgResults.close();

            return this.handleInsertionDepartmentReports(departmentID, reportName, generatedBlob);
        } catch (SQLException | DocumentException | IOException e) {
            this.serverController.addtolog(e.getMessage());
            return false;
        }
    }

    /**
     * Handles the update and insertion of department reports in the database.
     * If a report for the current month already exists, the method updates the existing report.
     * If a report for the current month does not exist, the method inserts a new report.
     * @param departmentID The ID of the department for which to update or insert the report.
     * @param reportName The name of the report to update or insert.
     * @param generatedBlob The PDF blob containing the report data.
     * @return {@code true} if the report is successfully updated or inserted, {@code false} otherwise.
     * @throws SQLException If an SQL exception occurs.
     */
    private boolean handleInsertionDepartmentReports(String departmentID, String reportName, Blob generatedBlob) throws SQLException {
        ResultSet blobData = dbController.selectRecordsFields(this.schemaName + ".department_manager_reports", "departmentId='" + departmentID + "' AND reportType='" + reportName + "' AND month='" + LocalDate.now().getMonthValue() + "' AND year='" + LocalDate.now().getYear() + "'", "blobData");
        String reportTableName = this.schemaName + ".department_manager_reports";
        if (blobData.next()) {
            if (!dbController.updateBlobRecord(reportTableName, "blobData", generatedBlob, "departmentId='" + departmentID + "' AND reportType='" + reportName + "' AND month='" + LocalDate.now().getMonthValue() + "' AND year='" + LocalDate.now().getYear() + "'")) {
                this.serverController.addtolog("Update in " + reportTableName + " failed. Update visitation report");
                return false;
            }
            return true;
        }

        String columns = "departmentId, reportType, month, year, blobData";
        String[] values = {departmentID, reportName, String.valueOf(LocalDate.now().getMonthValue()), String.valueOf(LocalDate.now().getYear()) };
        if (!dbController.insertBlobRecord(reportTableName, columns, generatedBlob, values)) {
            this.serverController.addtolog("Insert into " + reportTableName + " failed. Insert visitation report");
            return false;
        }
        return true;
    }

    /**
     * Retrieves all park IDs associated with a specific department.
     * @param departmentID The ID of the department.
     * @return A ResultSet containing all park IDs associated with the specified department.
     *         Returns null if there is an error.
     */
    private ResultSet getDepartmentParkIDs(String departmentID) throws SQLException {
        String parkTableName = this.schemaName + ".parks";
        String parkWhereClause = "departmentID='" + departmentID + "'";
        return dbController.selectRecordsFields(parkTableName, parkWhereClause, "ParkID");
    }

    /**
     * Retrieves all park IDs associated with a specific department.
     * @param departmentID The ID of the department.
     * @return A StringBuilder containing all park IDs associated with the specified department.
     *         Returns null if there is an error.
     */
    private StringBuilder getAllDepartmentParksChained(String departmentID) throws SQLException {
        ResultSet parkResults = this.getDepartmentParkIDs(departmentID);
        StringBuilder parkIDs = new StringBuilder();
        while(parkResults.next()) {
            if (parkIDs.length() > 0) {
                parkIDs.append(", ");
            }
            parkIDs.append("'").append(parkResults.getString("ParkID")).append("'");
        }
        return parkIDs;
    }

    // =================================================================================================================//
    //                                                                                                                 //
    //                                           WORKER EXCLUSIVE METHODS                                              //
    //                                                                                                                 //
    //=================================================================================================================//
    public void updateOrderStatusForUpcomingVisits() {
        try {
            ArrayList<ArrayList<String>> Orders = new ArrayList<>();
            String tableName = this.schemaName + ".orders";
            String whereClause = "VisitationDate BETWEEN NOW() + INTERVAL 23 HOUR + INTERVAL 59 MINUTE AND NOW() + INTERVAL 24 HOUR + INTERVAL 1 MINUTE AND orderStatus = " + (OrderStatus.STATUS_ACCEPTED.getOrderStatus());
            try {
                ResultSet rs = dbController.selectRecordsFields(tableName, whereClause, "OrderID", "ClientEmailAddress", "VisitorID");
                while (rs.next()) {
                    Orders.add(new ArrayList<>());
                    Orders.get(Orders.size() - 1).add(rs.getString("OrderID"));
                    Orders.get(Orders.size() - 1).add(rs.getString("ClientEmailAddress"));
                    Orders.get(Orders.size() - 1).add(rs.getString("VisitorID"));

                }
            } catch (SQLException e) {
                serverController.addtolog("Select upcoming orders failed: " + e.getMessage());
                return;
            }

            // Update the status of selected orders to pending confirmation
            if (!Orders.isEmpty()) {
                ArrayList<ArrayList<String>> PendingConfirmationOrders = new ArrayList<>();
                for (ArrayList<String> order : Orders) {
                    if (!updateOrderStatus(order.get(0), OrderStatus.STATUS_PENDING_CONFIRMATION)) {
                        serverController.addtolog("Failed to update order status for OrderID: " + order.get(0));
                    } else {
                        PendingConfirmationOrders.add(order);
                    }
                }
                sendMails(PendingConfirmationOrders,"Order Confirmation Notification","awaiting confirmation");
            }
        } catch (Exception e) {
            serverController.addtolog("Error updating order status for upcoming visits: " + e.getMessage());
        }
    }
    public void cancelOrdersInWaitlist24HoursBefore() {
        try {
            ArrayList<ArrayList<String>> Orders = new ArrayList<>();
            String tableName = this.schemaName + ".orders";
            String whereClause = "VisitationDate BETWEEN NOW() + INTERVAL 23 HOUR + INTERVAL 59 MINUTE AND NOW() + INTERVAL 24 HOUR + INTERVAL 1 MINUTE AND orderStatus = " + (OrderStatus.STATUS_WAITLIST.getOrderStatus());
            try {
                ResultSet rs = dbController.selectRecordsFields(tableName, whereClause, "OrderID", "ClientEmailAddress", "VisitorID");
                while (rs.next()) {
                    Orders.add(new ArrayList<>());
                    Orders.get(Orders.size() - 1).add(rs.getString("OrderID"));
                    Orders.get(Orders.size() - 1).add(rs.getString("ClientEmailAddress"));
                    Orders.get(Orders.size() - 1).add(rs.getString("VisitorID"));

                }
            } catch (SQLException e) {
                serverController.addtolog("Select upcoming orders failed: " + e.getMessage());
                return;
            }

            // Update the status of selected orders to pending confirmation
            if (!Orders.isEmpty()) {
                ArrayList<ArrayList<String>> WaitListOrdersCancelled = new ArrayList<>();
                for (ArrayList<String> order : Orders) {
                    if (!updateOrderStatus(order.get(0), OrderStatus.STATUS_CANCELLED)) {
                        serverController.addtolog("Failed to update order status for OrderID: " + order.get(0));
                    } else {
                        WaitListOrdersCancelled.add(order);
                    }
                }
                sendMails(WaitListOrdersCancelled, "Order Cancel Notification","canceled");
            }
        } catch (Exception e) {
            serverController.addtolog("Error updating order status for waitlist orders: " + e.getMessage());
        }
    }

    public void ChangeLatePendingConfirmationToCancelled() {
        try {
            ArrayList<ArrayList<String>> Orders = new ArrayList<>();
            String tableName = this.schemaName + ".orders";
            String whereClause = "VisitationDate BETWEEN NOW() + INTERVAL 79199 SECOND AND NOW() + INTERVAL 79320 SECOND AND orderStatus = " + (OrderStatus.STATUS_PENDING_CONFIRMATION.getOrderStatus());
            try {
                ResultSet rs = dbController.selectRecordsFields(tableName, whereClause, "OrderID", "ClientEmailAddress","VisitorID");
                while (rs.next()) {
                    Orders.add(new ArrayList<>());
                    Orders.get(Orders.size() - 1).add(rs.getString("OrderID"));
                    Orders.get(Orders.size() - 1).add(rs.getString("ClientEmailAddress"));
                    Orders.get(Orders.size() - 1).add(rs.getString("VisitorID"));
                }
            } catch (SQLException e) {
                serverController.addtolog("Select upcoming orders failed: " + e.getMessage());
                return;
            }

            // Update the status of selected orders to cancelled
            if (!Orders.isEmpty()) {
                ArrayList<ArrayList<String>> CancelledOrders = new ArrayList<>();
                for (ArrayList<String> order : Orders) {
                    if (!updateOrderStatus(order.get(0), OrderStatus.STATUS_CANCELLED)) {
                        serverController.addtolog("Failed to cancel order status for OrderID: " + order.get(0));
                    } else {
                        CancelledOrders.add(order);
                    }
                }
                sendMails(CancelledOrders, "Order Cancelled Notification", "cancelled");
            }
        } catch (Exception e) {
            serverController.addtolog("Error updating order status for upcoming visits: " + e.getMessage());
        }
    }

    public boolean checkGroupGuide(String groupGuideID) {
        try {
            String tableName = this.schemaName + ".group_guides";
            String whereClause = "ID='" + groupGuideID + "'";
            ResultSet resultSet = dbController.selectRecordsFields(tableName, whereClause, "pendingStatus");
            if (!resultSet.next())
                return false;
            int result = resultSet.getInt("pendingStatus");
            return result == 1;
        } catch (SQLException e) {
            this.serverController.addtolog(e.getMessage());
            return false;
        }
    }

    private void sendMails(ArrayList<ArrayList<String>> Orders, String Subject, String Type) {
        try {
            new Thread(() -> {
                for (ArrayList<String> order : Orders) {
                    GmailSender.sendEmail(order.get(1), Subject, "Hello Visitor " + order.get(2) + "\n" + "Your order id : " + order.get(0) + " is now " + Type);
                }
            }).start();
        } catch (Exception e) {
            serverController.addtolog("Error sending email: " + e.getMessage());
        }
    }


    //=================================================================================================================//
    //                                                                                                                 //
    //                                           END OF WORKER EXCLUSIVE METHODS                                       //
    //                                                                                                                 //
    //=================================================================================================================//

    public Boolean CheckAvailabilityBeforeReservationTime(Order checkOrder) {
        try {
            String tableName = this.schemaName + ".orders o " + "JOIN " + this.schemaName + ".parks p ON o.ParkID = p.ParkID";
            String field = "CASE WHEN SUM(o.NumOfVisitors) + " + checkOrder.getNumOfVisitors().toString() + " > (p.Capacity - p.GapVisitorsCapacity) THEN 0 ELSE 1 END AS IsWithinCapacity";
            String whereClause = "'" + checkOrder.getEnteredTime().toString().substring(0, checkOrder.getEnteredTime().toString().length() - 2) +
                    "' BETWEEN o.EnteredTime AND o.ExitedTime AND o.orderStatus NOT IN (1, 6) AND o.ParkID = '"
                    + checkOrder.getParkID().toString() + "';";
            ResultSet resultSet = dbController.selectRecordsFields(tableName, whereClause, field);
            if (!resultSet.next())
                return false;
            int result = resultSet.getInt("IsWithinCapacity");
            return result != 0;
        } catch (SQLException e) {
            this.serverController.addtolog(e.getMessage());
            return null;
        }
    }

    public Boolean CheckAvailabilityAfterReservationTime(Order checkOrder) {
        try {
            String tableName = this.schemaName + ".orders o " + "JOIN " + this.schemaName + ".parks p ON o.ParkID = p.ParkID";
            String field = "CASE WHEN SUM(o.NumOfVisitors) + " + checkOrder.getNumOfVisitors().toString() + " > (p.Capacity - p.GapVisitorsCapacity) THEN 0 ELSE 1 END AS IsWithinCapacity";
            String whereClause = "'" + checkOrder.getExitedTime().toString().substring(0, checkOrder.getExitedTime().toString().length() - 2) +
                    "' BETWEEN o.EnteredTime AND o.ExitedTime AND o.orderStatus NOT IN (1, 6) AND o.ParkID = '"
                    + checkOrder.getParkID().toString() + "';";
            ResultSet resultSet = dbController.selectRecordsFields(tableName, whereClause, field);
            if (!resultSet.next())
                return false;
            int result = resultSet.getInt("IsWithinCapacity");
            return result != 0;
        } catch (SQLException e) {
            this.serverController.addtolog(e.getMessage());
            return null;
        }
    }

    public Integer getExpectedTime(String parkID) {
        try {
            String tableName = this.schemaName + ".parks";
            String whereClause = "ParkID='" + parkID + "'";
            ResultSet resultSet = dbController.selectRecordsFields(tableName, whereClause, "DefaultVisitationTime");
            if (!resultSet.next())
                return null;
            return resultSet.getInt("DefaultVisitationTime");
        } catch (SQLException e) {
            this.serverController.addtolog(e.getMessage());
            return null;
        }
    }

    public ArrayList<Order> getMatchingWaitlistOrders(String parkID, Timestamp startTime) {
        try {
            ArrayList<Order> orders = new ArrayList<>();
            String tableName = this.schemaName + ".orders";
            String whereClause = "ParkID='" + parkID + "' AND EnteredTime = '" + startTime.toString().substring(0, startTime.toString().length() - 2) + "'AND orderStatus = 1";
            ResultSet results = dbController.selectRecords(tableName, whereClause);
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
            results.close();
            return orders;
        } catch (SQLException e) {
            this.serverController.addtolog(e.getMessage());
            return new ArrayList<>();
        }
    }

    public boolean extractFromWaitList(Order order){
        ArrayList<Order> ordersToWorkWith = getMatchingWaitlistOrders(order.getParkID(),order.getEnteredTime());
        List<Order> extractedOrders = findBestCombination(ordersToWorkWith, order.getNumOfVisitors());
        for (Order extractedOrder : extractedOrders){
            if(!updateOrderStatus(extractedOrder.getOrderID(),OrderStatus.STATUS_ACCEPTED)){
                return false;
            }
            GmailSender.sendEmail(extractedOrder.getClientEmailAddress(),"Your order has been accepted","Your order for date "+extractedOrder.getVisitationDate()+" has been accepted");
        }
        return true;
    }

    public ArrayList<Timestamp> getAvailableTimeStamps(Order order) {
        Order WorkingOrder = order;
        List<Timestamp> availableHours = getNextWeekHours(order.getEnteredTime());
        ArrayList<Timestamp> availableTimestamps = new ArrayList<>();
        for (Timestamp hourDate : availableHours) {
            WorkingOrder.setEnteredTime(hourDate);
            WorkingOrder.setExitedTime(createExitTime(hourDate, getExpectedTime(WorkingOrder.getParkID())));
            if (CheckAvailabilityAfterReservationTime(WorkingOrder) && CheckAvailabilityBeforeReservationTime(WorkingOrder)) {
                availableTimestamps.add(hourDate);
            }
        }
        return availableTimestamps;
    }


    //=================================================================================================================//
    //                                                                                                                 //
    //                                           IMPORT SIMULATOR METHODS                                              //
    //                                                                                                                 //
    //=================================================================================================================//

    public void insertUser(String username, String password, int role) {
        try {
            String tableName = this.schemaName + ".users";
            String columns = "username, password, role";
            if (!dbController.insertRecord(tableName, columns, "'" + username + "'", "'" + password + "'", String.valueOf(role))) {
                this.serverController.addtolog("Insert into " + tableName + " failed. Insert user:" + username);
            }
        } catch (SQLException e) {
            this.serverController.addtolog(e.getMessage());
        }
    }

    public void insertGroupGuide(String username, String ID, String email, String firstName, String lastName) {
        try {
            String tableName = this.schemaName + ".group_guides";
            String columns = "ID, username, FirstName, LastName, EmailAddress, pendingStatus";
            if (!dbController.insertRecord(tableName, columns, "'" + ID + "'", "'" + username + "'", "'" + firstName + "'", "'" + lastName + "'", "'" + email + "'", "true")) {
                this.serverController.addtolog("Insert into " + tableName + " failed. Insert group guide:" + username);
            }
        } catch (SQLException e) {
            this.serverController.addtolog(e.getMessage());
        }
    }

    public void insertParkEmployee(String username, String email, String parkID, String firstName, String lastName, boolean isParkManager, String ID) {
        try {
            String tableName = this.schemaName + ".park_employees";
            String columns = "username, EmailAddress, ParkID, firstName, lastName, isParkManager, employeeID";
            if (!dbController.insertRecord(tableName, columns, "'" + username + "'", "'" + email + "'", "'" + parkID + "'", "'" + firstName + "'", "'" + lastName + "'", String.valueOf(isParkManager), "'" + ID + "'")) {
                this.serverController.addtolog("Insert into " + tableName + " failed. Insert park manager:" + username);
            }
        } catch (SQLException e) {
            this.serverController.addtolog(e.getMessage());
        }
    }

    public void insertDepartmentManager(String username, String email, String departmentID, String firstName, String lastName, String ID) {
        try {
            String tableName = this.schemaName + ".department_managers";
            String columns = "username, EmailAddress, departmentID, firstName, lastName, employeeID";
            if (!dbController.insertRecord(tableName, columns, "'" + username + "'", "'" + email + "'", "'" + departmentID + "'", "'" + firstName + "'", "'" + lastName + "'", "'" + ID + "'")) {
                this.serverController.addtolog("Insert into " + tableName + " failed. Insert department manager:" + username);
            }
        } catch (SQLException e) {
            this.serverController.addtolog(e.getMessage());
        }
    }

}

