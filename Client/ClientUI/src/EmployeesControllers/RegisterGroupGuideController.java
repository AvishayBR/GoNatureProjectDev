package EmployeesControllers;

import CommonClient.controllers.BaseController;
import io.github.palexdev.materialfx.controls.MFXButton;
import io.github.palexdev.materialfx.controls.MFXTextField;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;

public class RegisterGroupGuideController extends BaseController {

    @FXML
    private MFXButton btnSubmit;

    @FXML
    private Label lblErrorMsg;

    @FXML
    private MFXTextField txtGroupGuideID;

    @FXML
    void OnClickSubmitButton(ActionEvent event) {

    }

}
