// This file contains material supporting section 3.7 of the textbook:
// "Object Oriented Software Engineering" and is issued under the open-source
// license found at www.lloseng.com 

package client;

import CommonClient.ocsf.AbstractClient;
import CommonClient.ChatCommunicatorIF;
import Entities.Message;
import Entities.OpCodes;

import java.io.IOException;

/**
 * This class overrides some of the methods defined in the abstract
 * superclass in order to give more functionality to the client.
 *
 * @author Dr Timothy C. Lethbridge
 * @author Dr Robert Lagani&egrave;
 * @author Fran&ccedil;ois B&eacute;langer
 * @version July 2000
 */
public class ClientCommunicator extends AbstractClient {
    //Instance variables **********************************************

    /**
     * The interface type variable.  It allows the implementation of
     * the display method in the client.
     */
    public static Message msg = new Message(null);
    ChatCommunicatorIF clientUI;
    public static boolean awaitResponse = false;

    //Constructors ****************************************************

    /**
     * Constructs an instance of the chat client.
     *
     * @param host     The server to connect to.
     * @param port     The port number to connect on.
     * @param clientUI The interface type variable.
     */

    public ClientCommunicator(String host, int port, ChatCommunicatorIF clientUI)
            throws IOException {
        super(host, port); //Call the superclass constructor
        this.clientUI = clientUI;
        //openConnection();
    }

    //Instance methods ************************************************

    /**
     * This method handles all data that comes in from the server.
     *
     * @param msg The message from the server.
     */
    public void handleMessageFromServer(Object msg) {
        System.out.println("--> handleMessageFromServer");
        if (msg instanceof Message){
            ClientCommunicator.msg = (Message) msg;
            clientUI.respond(msg);
        }
        if (msg instanceof String){
            clientUI.respond(msg);
        }
        awaitResponse = false;
    }

    /**
     * This method handles all data coming from the UI
     *
     * @param message The message from the UI.
     */

    public void handleMessageFromClientUI(Object message) {
        try {
            openConnection();
            awaitResponse = true;
            sendToServer(message);
            // wait for response
            while (awaitResponse) {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        } catch (IOException e) {
            clientUI.respond("Could not send message to server: Terminating client." + e);
            quit();
        }
    }


    /**
     * This method terminates the client.
     */
    public void quit() {
        try {
            Object msg = new Message(OpCodes.OP_QUIT, null, null);
            sendToServer(msg);
            closeConnection();
        } catch (IOException e) {
        }
        System.exit(0);
    }
}

//End of ChatClient class
