package VisitorsControllers;

import io.github.palexdev.materialfx.controls.MFXButton;
import io.github.palexdev.materialfx.controls.MFXTextField;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.text.Text;

public class WaitListPageController {

    @FXML
    private MFXButton btnSignUp;

    @FXML
    private Label lblDate;

    @FXML
    private Label lblEmail;

    @FXML
    private Label lblErrorEmail;

    @FXML
    private Label lblErrorName;

    @FXML
    private Label lblErrorPhone;

    @FXML
    private Label lblNumOfVisitors;

    @FXML
    private Label lblParkName;

    @FXML
    private Label lblTelephone;

    @FXML
    private Label lblTime;

    @FXML
    private Text txtDescription;

    @FXML
    private MFXTextField txtEmail;

    @FXML
    private MFXTextField txtFullName;

    @FXML
    private Text txtHeader;

    @FXML
    private MFXTextField txtPhone;

    @FXML
    void OnClickSignUpButton(ActionEvent event) {

    }

}
