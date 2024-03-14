package VisitorsControllers;

import CommonClient.controllers.BaseController;
import Entities.Order;
import io.github.palexdev.materialfx.controls.MFXButton;
import io.github.palexdev.materialfx.controls.MFXTextField;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.text.Text;

public class HandleOrderDetailsPageController extends BaseController {
    @FXML
    private MFXButton btnCancelOrder;

    @FXML
    private MFXButton btnSubmitChanges;

    @FXML
    private Label lblErrorMsg;

    @FXML
    private Text txtDescription;

    @FXML
    private MFXTextField txtEmail;

    @FXML
    private Text txtHeader;

    @FXML
    void OnClickCancelOrderButton(ActionEvent event) {

    }

    @FXML
    void OnClickSubmitChangesButton(ActionEvent event) {

    }

}

