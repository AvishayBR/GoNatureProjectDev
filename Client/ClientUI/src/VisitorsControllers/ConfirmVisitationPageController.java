package VisitorsControllers;

import CommonClient.controllers.BaseController;
import Entities.Order;
import Entities.ParkBank;
import io.github.palexdev.materialfx.controls.MFXButton;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.text.Text;

import static CommonClient.Utils.parseVisitDate;
import static CommonClient.Utils.parseVisitTime;

public class ConfirmVisitationPageController extends BaseController {

    @FXML
    private Text txtHeader;

    @FXML
    private Text txtDescription;

    @FXML
    private Label lblParkName;

    @FXML
    private Label lblNumOfVisitors;

    @FXML
    private Label lblTelephone;

    @FXML
    private Label lblEmail;

    @FXML
    private Label lblDate;

    @FXML
    private Label lblTime;

    @FXML
    private Label lblOrderNumber;

    @FXML
    private MFXButton btnDeclineVisitation;

    @FXML
    private MFXButton btnConfirmVisitation;

    private Order order;

    public void setOrder(Order order) {
        this.order = order;

        lblParkName.setText(ParkBank.getParkNameByID(order.getParkID()));
        lblNumOfVisitors.setText(String.valueOf(order.getNumOfVisitors()));
        lblTelephone.setText(order.getPhoneNumber());
        lblEmail.setText(order.getClientEmailAddress());
        lblDate.setText(parseVisitDate(order.getVisitationDate()));
        lblTime.setText(parseVisitTime(order.getVisitationDate()));
        lblOrderNumber.setText(order.getOrderID());
    }

    @FXML
    void OnClickConfirmVisitationButton(ActionEvent event) {

    }

    @FXML
    void OnClickDeclineVisitationButton(ActionEvent event) {

    }

}
