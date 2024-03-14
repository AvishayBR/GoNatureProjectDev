package EmployeesControllers;

import CommonClient.controllers.BaseController;
import io.github.palexdev.materialfx.controls.MFXButton;
import io.github.palexdev.materialfx.controls.MFXCheckbox;
import io.github.palexdev.materialfx.controls.MFXTextField;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.text.Text;

public class UnplannedVisitInsertionController extends BaseController {

    @FXML
    private MFXButton btnSubmit;

    @FXML
    private MFXCheckbox cbGroupOrder;

    @FXML
    private Label lblErrorMsg;

    @FXML
    private MFXTextField txtNumOfVisitors;

    @FXML
    private Text txtResult;

    @FXML
    void OnClickSubmitButton(ActionEvent event) {

    }

}
