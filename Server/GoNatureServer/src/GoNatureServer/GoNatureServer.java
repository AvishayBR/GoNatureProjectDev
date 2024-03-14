// This file contains material supporting section 3.7 of the textbook:
// "Object Oriented Software Engineering" and is issued under the open-source
// license found at www.lloseng.com 
package GoNatureServer;

import CommonServer.ocsf.AbstractServer;
import CommonServer.ocsf.ConnectionToClient;
import DataBase.DBConnection;
import Entities.*;
import ServerUIPageController.ServerPortFrameController;

import java.io.IOException;
import java.sql.Array;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


/**
 * This class overrides some of the methods in the abstract
 * superclass in order to give more functionality to the server.
 *
 * @author Dr Timothy C. Lethbridge
 * @author Dr Robert Lagani&egrave;re
 * @author Fran&ccedil;ois B&eacute;langer
 * @author Paul Holden
 * @version July 2000
 */

public class GoNatureServer extends AbstractServer {
    //Class variables *************************************************

    private static GoNatureServer server;
    private ServerPortFrameController controller;
    private DBConnection db;

    private Map<String, ConnectionToClient> authenticatedUsers = new ConcurrentHashMap<>();

    private boolean connected = true;


    //Constructors ****************************************************

    /**
     * Constructs an instance of the echo server.
     *
     * @param port The port number to connect on.
     */

    private GoNatureServer(int port, ServerPortFrameController controller) throws Exception {
        super(port);
        this.controller = controller;
    }

    public void initializeDBConnection(ServerPortFrameController controller) throws Exception {
        try {
            db = DBConnection.getInstance(controller);
        } catch (ClassNotFoundException | SQLException e) {
            controller.addtolog(e.getMessage());
            db = null;
            controller.addtolog("Server failed to initialize");
            connected = false;
        }
    }

    public static GoNatureServer getInstance(int port, ServerPortFrameController controller) throws Exception {
        if (server == null) {
            server = new GoNatureServer(port, controller);
        }
        return server;
    }

    //Instance methods ************************************************

    /**
     * This method handles any messages received from the client.
     *
     * @param msg The message received from the client.
     * @param client The connection from which the message originated.
     * @param
     */

    /**
     * This method overrides the one in the superclass.  Called
     * when the server starts listening for connections.
     */
    protected void serverStarted() {
        if (!connected) {
            try {
                closeServer();
                return;
            } catch (Exception e) {
                e.printStackTrace();
                return;
            }
        }
        this.controller.addtolog("Server listening for connections on port " + getPort());
        // Start a new thread for running the task every second
        new Thread(() -> {
            try {
                while (server != null && server.isListening()) {
                    Thread[] clientConnections = getClientConnections();
                    controller.resetTableClients();
                    // Here you can process all the clients as needed
                    for (Thread clientThread : clientConnections) {
                        ConnectionToClient client = (ConnectionToClient) clientThread;
                        controller.addRow(client.getInetAddress().getHostName(), client.getInetAddress().getHostAddress());
                    }
                    try {
                        Thread.sleep(1000); // Wait for 1 second before the next iteration
                    } catch (InterruptedException e) {
                        System.out.println("The client processing thread was interrupted.");
                    }
                }
            } catch (NullPointerException e) {

            }
        }, "Client Processing Thread").start();
        server.controller.toggleControllers(true);
    }


    public void handleMessageFromClient(Object msg, ConnectionToClient client) throws IOException {
        Message newMsg = new Message(null, null, null);

        if (msg instanceof Message) {
            switch (((Message) msg).getMsgOpcode()) {
                case OP_SYNC_HANDSHAKE:
                    client.sendToClient(msg);
                    break;

                case OP_LOGOUT:
                    authenticatedUsers.remove(((Message) msg).getMsgUserName());
                    client.sendToClient("logged out successfully");
                    break;

                case OP_SIGN_IN:
                    User userCredentials = (User) ((Message) msg).getMsgData();
                    if (authenticatedUsers.containsKey(userCredentials.getUsername())) {
                        if (authenticatedUsers.get(userCredentials.getUsername()).getInetAddress() == null) {
                            authenticatedUsers.remove(userCredentials.getUsername());
                        } else {
                            Message respondMsg = new Message(OpCodes.OP_SIGN_IN_ALREADY_LOGGED_IN, userCredentials.getUsername(), null);
                            client.sendToClient(respondMsg);
                            return;
                        }
                    }
                    User authenticatedUser = db.login(userCredentials.getUsername(), userCredentials.getPassword());
                    //User authenticatedUser= new User("test","123",Role.ROLE_SINGLE_VISITOR);
                    if (authenticatedUser == null) {
                        Message respondMsg = new Message(OpCodes.OP_SIGN_IN, "", null);
                        client.sendToClient(respondMsg);
                        return;
                    }
                    authenticatedUsers.put(authenticatedUser.getUsername(), client);
                    Message respondMsg = new Message(OpCodes.OP_SIGN_IN, authenticatedUser.getUsername(), authenticatedUser);
                    client.sendToClient(respondMsg);
                    break;
                case OP_GET_VISITOR_ORDERS:
                    AbstractVisitor visitor = (AbstractVisitor) ((Message) msg).getMsgData();

                    String visitorID = visitor.getID();
                    String visitorUserName = visitor.getUsername();

                    ArrayList<Order> requestedOrders = db.getUserOrders(visitorID);
                    OrderBank orders;
                    if (visitor instanceof SingleVisitor) {
                        orders = new OrderBank(OrderType.ORD_TYPE_SINGLE);
                    } else {
                        orders = new OrderBank(OrderType.ORD_TYPE_GROUP);
                    }
                    if (orders.insertOrderArray(requestedOrders)) {
                        Message getVisitorOrdersMsg = new Message(OpCodes.OP_GET_VISITOR_ORDERS, visitorUserName, requestedOrders);
                        client.sendToClient(getVisitorOrdersMsg);
                    } else {
                        Message getVisitorOrdersMsg = new Message(OpCodes.OP_DB_ERR);
                        client.sendToClient(getVisitorOrdersMsg);
                    }
                case OP_CREATE_NEW_VISITATION:
                    Order order = (Order) ((Message) msg).getMsgData();
                    order.setOrderStatus(OrderStatus.STATUS_CONFIRMED_PENDING_PAYMENT);

                    if (db.checkOrderExists(order.getVisitorID(), order.getParkID(), order.getVisitationDate())) {
                        Message createOrderMsg = new Message(OpCodes.OP_ORDER_ALREADY_EXIST);
                        client.sendToClient(createOrderMsg);
                        return;
                    }
                    Order newOrder = db.addOrder(order);
                    Message createOrderMsg = new Message(OpCodes.OP_CREATE_NEW_VISITATION, ((Message) msg).getMsgUserName(), newOrder);
                    client.sendToClient(createOrderMsg);
                case OP_GET_USER_ORDERS_BY_USERID:
                    String[] data = (String[]) ((Message) msg).getMsgData();
                    Order userOrder = db.getUserOrderByUserID(data[0], data[1]);
                    Message getUserMsg = new Message(OpCodes.OP_GET_USER_ORDERS_BY_USERID, "", userOrder);
                    client.sendToClient(getUserMsg);
                    break;
                case OP_QUIT:
                    if (authenticatedUsers.containsValue(client)) {
                        for (Map.Entry<String, ConnectionToClient> entry : authenticatedUsers.entrySet()) {
                            if (entry.getValue() == client) {
                                authenticatedUsers.remove(entry.getKey());
                                break;
                            }
                        }
                    }
                    this.controller.addtolog("Client " + client + " Disconnected");
                    controller.removeRowByIP(client.getInetAddress().getHostAddress());
                    client.close();
                    break;

                default:
                    controller.addtolog("Error Unknown Opcode");
            }
        }
    }


    /**
     * This method overrides the one in the superclass.  Called
     * when the server stops listening for connections.
     */
    @Override
    protected void serverStopped() {
        this.controller.addtolog("Server has stopped listening for connections.");
    }

    @Override
    protected void serverClosed() {
        controller.toggleControllers(false);
        this.controller.addtolog("Server has closed.");
        if (db != null) {
            db.closeConnection();
        }
    }

    public static void closeServer() {
        try {
            if (server == null) {
                System.out.println("Server isn't initialized");
                return;
            }
            server.sendToAllClients("Disconnect");
            server.stopListening();
            server.close();
            server.controller.toggleControllers(false);
            server = null;
        } catch (Exception e) {
            server.controller.addtolog("Server isn't initialized");
        }
    }

    @Override
    protected void clientConnected(ConnectionToClient client) {
        //controller.addRow(client.getInetAddress().getHostName(), client.getInetAddress().getHostAddress());
    }


}
//End of EchoServer class
