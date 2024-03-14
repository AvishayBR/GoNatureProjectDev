package CommonClient.controllers;

import CommonClient.ClientUI;
import Entities.Message;
import Entities.OpCodes;
import Entities.Role;
import client.ClientCommunicator;
import client.ClientController;
import io.github.palexdev.materialfx.controls.MFXButton;
import io.github.palexdev.materialfx.controls.MFXTextField;
import javafx.fxml.FXML;

public class ConnectClientPageController extends BaseController {
    @FXML
    private MFXButton connectBtn;

    @FXML
    private MFXTextField ipTxt;
    private static String CurrentIP = "localhost";

    public void onConnect() {
        try {
            buildConnection();
            performHandshake();
            applicationWindowController.setCenterPage("/CommonClient/gui/HomePage.fxml");
        } catch (Exception e) {
            e.printStackTrace();
            // Todo: Set text label to "Something went wrong..."
        }
    }

    private void buildConnection() {
        if (ClientUI.client == null) {
            System.out.println("Connecting to " + ipTxt.getText() + " on port 5555...");
            CurrentIP = ipTxt.getText();
            ClientUI.client = new ClientController(ipTxt.getText(), 5555);
        }
    }

    private void performHandshake() {
        Message msg = new Message(OpCodes.OP_SYNC_HANDSHAKE);
        ClientUI.client.accept(msg);
        if (ClientCommunicator.msg.getMsgOpcode() == OpCodes.OP_SYNC_HANDSHAKE) {
            return;
        }
        System.exit(0); // Server is not running
    }

}
