package CommonClient.controllers;

import CommonClient.ClientUI;
import CommonClient.Utils;
import CommonUtils.InputTextPopup;
import Entities.Message;
import Entities.OpCodes;
import Entities.Order;
import Entities.User;
import VisitorsControllers.ConfirmVisitationPageController;
import VisitorsControllers.HandleOrderDetailsPageController;
import client.ClientCommunicator;
import io.github.palexdev.materialfx.controls.MFXButton;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.ImageView;

import javax.naming.CommunicationException;
import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;

public class HomePageController extends BaseController implements Initializable {
    @FXML
    private MFXButton bookBtn;

    @FXML
    private ImageView centerImg;

    @FXML
    private ImageView img1;

    @FXML
    private ImageView img2;

    @FXML
    private MFXButton signInBtn;

    private InputTextPopup onAuthPopup;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        centerImg.toFront();
        centerImg.setEffect(new DropShadow());
        img1.setEffect(new DropShadow());
        img2.setEffect(new DropShadow());
    }

    public void onButtonClicked() {
        if (applicationWindowController.getUser() != null) {
            applicationWindowController.logout();
            return;
        }
        applicationWindowController.setCenterPage("/CommonClient/gui/LoginPage.fxml");
    }

    private void onAuthWithID(String id, String path) throws CommunicationException {
        if (!Utils.isIDValid(id)) {
            onAuthPopup.setErrorLabel("Invalid ID Format! Try again");
            return;
        }


        // Todo: change hard-coded orderID value to a dynamic value from the user input.
        String[] data = {id, "1"};
        Message message = new Message(OpCodes.OP_GET_USER_ORDERS_BY_USERID, "", data);
        ClientUI.client.accept(message);
        Message response = ClientCommunicator.msg;
        OpCodes returnOpCode = response.getMsgOpcode();

        // Checking if the response from the server is inappropriate.
        if (returnOpCode != OpCodes.OP_GET_USER_ORDERS_BY_USERID) {
            throw new CommunicationException("Response is inappropriate from server");
        }

        Order order = (Order) response.getMsgData();
        if (order == null) {
            onAuthPopup.setErrorLabel("Invalid ID or Link! Try again");
            return;
        }

        onAuthPopup.setErrorLabel("");
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(path));
            Parent page = loader.load();
            Object controller = loader.getController();
            if (controller instanceof BaseController) {
                ((BaseController) controller).setApplicationWindowController(applicationWindowController);
            }

            if (page != null) {
                applicationWindowController.getRoot().setCenter(page);
            }

            assert controller instanceof ConfirmVisitationPageController;
            ((ConfirmVisitationPageController) controller).setOrder(order);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void handleExistingOrder() {
        onAuthPopup = new InputTextPopup("Enter ID to Authenticate ", (inputText) -> {
            try {
                this.onAuthWithID(inputText, "/VisitorsUI/ConfirmVisitationPage.fxml");
            } catch (CommunicationException e) {
                e.printStackTrace();
            }
        }, 500, 300, true, true, true);
        onAuthPopup.show(applicationWindowController.getRoot());
    }

    public void onBookButtonClicked() {
        if (applicationWindowController.getUser() == null) {
            applicationWindowController.setCenterPage("/CommonClient/gui/LoginPage.fxml");
            return;
        }
        applicationWindowController.loadDashboardPage(applicationWindowController.getUser().getRole());
    }
}
