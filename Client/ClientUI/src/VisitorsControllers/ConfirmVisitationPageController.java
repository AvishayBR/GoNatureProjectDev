package VisitorsControllers;

import io.github.palexdev.materialfx.controls.MFXButton;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.text.Text;

public class ConfirmVisitationPageController
{

    @FXML
    private MFXButton btnConfirmVisitation;

    @FXML
    private MFXButton btnDeclineVisitation;

    @FXML
    private Label lblDate;

    @FXML
    private Label lblEmail;

    @FXML
    private Label lblNumOfVisitors;

    @FXML
    private Label lblOrderNumber;

    @FXML
    private Label lblParkName;

    @FXML
    private Label lblTelephone;

    @FXML
    private Label lblTime;

    @FXML
    private Text txtHeader;

    @FXML
    void OnClickConfirmVisitationButton(ActionEvent event) {

    }

    @FXML
    void OnClickDeclineVisitationButton(ActionEvent event) {

    }

}
