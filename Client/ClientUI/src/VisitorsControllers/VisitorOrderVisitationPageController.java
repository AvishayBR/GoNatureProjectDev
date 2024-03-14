package VisitorsControllers;

import CommonClient.ClientUI;
import CommonClient.controllers.BaseController;
import CommonUtils.ConfirmationPopup;
import CommonUtils.CommonUtils;
import Entities.*;
import client.ClientCommunicator;
import io.github.palexdev.materialfx.controls.MFXButton;
import io.github.palexdev.materialfx.controls.MFXTextField;
import io.github.palexdev.materialfx.controls.legacy.MFXLegacyComboBox;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import javax.naming.CommunicationException;
import java.net.URL;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.ResourceBundle;
import java.util.regex.Pattern;


public class VisitorOrderVisitationPageController extends BaseController implements Initializable {

    ObservableList<String> list;

    @FXML
    private Text Header;

    @FXML
    private MFXButton btnCreateOrder;

    @FXML
    private DatePicker datePicker;

    @FXML
    private Pane pane;

    @FXML
    private MFXLegacyComboBox<String> parkCmbBox;

    @FXML
    private Separator sepOrder;

    @FXML
    private StackPane stackPane;

    @FXML
    private MFXLegacyComboBox<String> timeOfVisitCmbBox;

    @FXML
    private MFXTextField txtEmail;

    @FXML
    private MFXTextField txtFirstName;

    @FXML
    private MFXTextField txtLastName;

    @FXML
    private MFXTextField txtNumOfVisitors;

    @FXML
    private MFXTextField txtPhone;


    private void setParkCmbBox() {
        ArrayList<String> al = new ArrayList<String>();
        for (String key : ParkBank.getUnmodifiableMap().keySet()) {
            al.add(key);
        }
        list = FXCollections.observableArrayList(al);
        parkCmbBox.setItems(list);
    }

    private void setTimeOfVisitCmbBox() {
        ArrayList<String> al = new ArrayList<String>();
        al.add("08:00");
        al.add("12:00");
        al.add("16:00");
        list = FXCollections.observableArrayList(al);
        timeOfVisitCmbBox.setItems(list);
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setParkCmbBox();
        setTimeOfVisitCmbBox();
    }

    @FXML
    void OnClickCreateOrderButton(ActionEvent event) throws CommunicationException {
        if (!validateFields()) {
            System.out.println("one or more details are empty ");
            return;
        }

        if (!(applicationWindowController.getUser() instanceof SingleVisitor)) {
            System.out.println("The user isn't visitor");
            return;
        }

        SingleVisitor visitor = (SingleVisitor) applicationWindowController.getUser();
        Timestamp timeOfVisit = convertStringToTimestamp(datePicker.getValue().toString(), timeOfVisitCmbBox.getValue());
        Order order = new Order(visitor.getID(), ParkBank.getUnmodifiableMap().get(parkCmbBox.getValue()), timeOfVisit, txtEmail.getText(), txtPhone.getText(), null, timeOfVisit, timeOfVisit, null, OrderType.ORD_TYPE_SINGLE, (CommonUtils.convertStringToInt(txtNumOfVisitors.getText())));
        Object msg = new Message(OpCodes.OP_CREATE_NEW_VISITATION, visitor.getUsername(), order);
        ClientUI.client.accept(msg);
        Message respondMsg = ClientCommunicator.msg;
        OpCodes returnOpCode = respondMsg.getMsgOpcode();
        if (returnOpCode != OpCodes.OP_CREATE_NEW_VISITATION) {
            throw new CommunicationException("Respond not appropriate from server");
        }
        if (respondMsg.getMsgData() instanceof Order) {
            if (((Order) respondMsg.getMsgData()).getOrderID() != null) {
                Order cnfrmorder = (Order) respondMsg.getMsgData();
                String strForPopup = "The order " + cnfrmorder.getOrderID() + " has been created successfully";
                ConfirmationPopup confirmPopup = new ConfirmationPopup(strForPopup, () ->
                {
                    applicationWindowController.loadDashboardPage(applicationWindowController.getUser().getRole());
                    applicationWindowController.loadMenu(applicationWindowController.getUser());
                    clearFields();
                }
                        , 600, 300, false, "OK", false);
                confirmPopup.show(applicationWindowController.getRoot());
            } else {
                String strForPopup = "The park is Full Do you want to enter for waitlist?";
                ConfirmationPopup confirmPopup;
                confirmPopup = new ConfirmationPopup(strForPopup, () ->
                {
                    applicationWindowController.setCenterPage("/VisitorsUI/WaitListPage");
                    applicationWindowController.loadMenu(applicationWindowController.getUser());
                    clearFields();
                }, () -> {
                    applicationWindowController.loadDashboardPage(applicationWindowController.getUser().getRole());
                    applicationWindowController.loadMenu(applicationWindowController.getUser());
                    clearFields();
                },
                        300, 150, false, "Yes", "No", false);
                confirmPopup.show(applicationWindowController.getRoot());
                return;
            }
        } else {
            System.out.println("The order is not created");
        }
    }


    private boolean validateFields() {
        if (CommonUtils.anyStringEmpty(txtFirstName.getText(), txtLastName.getText(), txtPhone.getText(), txtEmail.getText(), txtNumOfVisitors.getText())
                || parkCmbBox.getValue() == null
                || datePicker.getValue() == null
                || timeOfVisitCmbBox.getValue() == null
                || !CommonUtils.isValidPhone(txtPhone.getText())
                || !CommonUtils.isValidName(txtFirstName.getText())
                || !CommonUtils.isValidName(txtLastName.getText())
                || !CommonUtils.isEmailAddressValid(txtEmail.getText())
                || CommonUtils.convertStringToInt(txtNumOfVisitors.getText()) <= 0) {
            System.out.println("Validation failed. Please check your input.");
            return false;
        }
        return true;
    }

    private void clearFields() {
        // Clear text fields
        txtEmail.clear();
        txtFirstName.clear();
        txtLastName.clear();
        txtPhone.clear();
        txtNumOfVisitors.clear();

        // Reset combo boxes
        parkCmbBox.getSelectionModel().clearSelection();
        timeOfVisitCmbBox.getSelectionModel().clearSelection();
        // Use this if the combo box text does not clear with clearSelection()
        parkCmbBox.setValue(null);
        timeOfVisitCmbBox.setValue(null);

        // Reset the date picker
        datePicker.setValue(null);
    }


    private Timestamp convertStringToTimestamp(String date, String time) {
        // Combine Date and Time Strings
        String dateTimeString = date + "T" + time;

        // Define the formatter for LocalDateTime
        DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

        // Parse the String to LocalDateTime
        LocalDateTime dateTime = LocalDateTime.parse(dateTimeString, formatter);

        // Convert LocalDateTime to java.sql.Timestamp
        Timestamp timestamp = Timestamp.valueOf(dateTime);

        return timestamp;
    }
}
