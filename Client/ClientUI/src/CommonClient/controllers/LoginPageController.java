package CommonClient.controllers;

import CommonClient.ClientUI;
import Entities.Message;
import Entities.OpCodes;
import Entities.Role;
import Entities.User;
import client.ClientCommunicator;
import io.github.palexdev.materialfx.controls.MFXButton;
import io.github.palexdev.materialfx.controls.MFXPasswordField;
import io.github.palexdev.materialfx.controls.MFXTextField;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.text.Text;

import javax.naming.CommunicationException;

public class LoginPageController extends BaseController {
    @FXML
    private Text header;

    @FXML
    private MFXButton loginBtn;

    @FXML
    private StackPane loginPage;

    @FXML
    private Pane pane;

    @FXML
    private Label passwordLbl;

    @FXML
    private Separator sep;

    @FXML
    private Label userNameLbl;

    @FXML
    private MFXTextField userNameText;

    @FXML
    private MFXPasswordField passwordText;

    @FXML
    private Label ErrorMsg;

    @FXML
    private ImageView backBtn;


    private String getUserName() {
        return userNameText.getText();
    }

    private String getPassword() {
        return passwordText.getText();
    }

    public void resetAllFields() {
        this.ErrorMsg.setText("");
        this.userNameText.setText("");
        this.passwordText.setText("");
    }

    public void navigateToHomePage() {
        applicationWindowController.setCenterPage("/CommonClient/gui/HomePage.fxml");
    }

    public void onLoginClick() throws CommunicationException {
        if (getUserName().isEmpty() || getPassword().isEmpty()) {
            ErrorMsg.setText("Please fill all fields!");
            return; // Exit the method if any of the fields are empty.
        }
        User user = new User(getUserName(), getPassword());
        Object msg = new Message(OpCodes.OP_SIGN_IN, getUserName(), user);

        ClientUI.client.accept(msg);
        Message respondMsg = ClientCommunicator.msg;

        OpCodes returnOpCode = respondMsg.getMsgOpcode();

        // Checking if the user is already signed in:
        if (returnOpCode == OpCodes.OP_SIGN_IN_ALREADY_LOGGED_IN)
        {
            ErrorMsg.setText(respondMsg.getMsgUserName() + " is already logged in.");
            return;
        }

        // Checking if the response from the server is inappropriate.
        if (returnOpCode != OpCodes.OP_SIGN_IN) {
            throw new CommunicationException("Response is inappropriate from server");
        }

        // Logging user in, unless incorrect user and password.
        user = (User) respondMsg.getMsgData();
        if (respondMsg.getMsgData() != null && user.getRole() != Role.ROLE_GUEST) {
            resetAllFields();
            applicationWindowController.loadDashboardPage(user.getRole());
            applicationWindowController.loadMenu(user);
        } else {
            ErrorMsg.setText("Wrong username or password, Please try again!");
        }
    }
}
