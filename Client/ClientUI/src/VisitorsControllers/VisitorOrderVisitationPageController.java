package VisitorsControllers;

import CommonClient.ClientUI;
import CommonClient.controllers.BaseController;
import CommonUtils.CommonUtils;
import CommonUtils.ConfirmationPopup;
import Entities.*;
import client.ClientCommunicator;
import io.github.palexdev.materialfx.controls.legacy.MFXLegacyComboBox;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.DateCell;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

import javax.naming.CommunicationException;
import java.net.URL;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.ResourceBundle;

import static CommonClient.Utils.setComboBoxHours;

/**
 * This controller manages the order visitation page for single visitors.
 * It handles the form inputs for selecting the date, park, time, and other details
 * required to create a visitation order. The controller validates user inputs,
 * creates an order object based on the input data, and sends it to the server for processing.
 * Depending on the server response, it displays confirmation messages, error messages,
 * or prompts the user to join a waitlist or view alternative times if the park is at full capacity.
 * Additionally, it provides methods to initialize the form components, handle button clicks,
 * and clear/reset the input fields.
 */

public class VisitorOrderVisitationPageController extends BaseController implements Initializable {

    /**
     * Observable list for storing strings.
     */
    ObservableList<String> list;

    /**
     * Label for displaying error messages.
     */
    @FXML
    private Label errorLbl;

    /**
     * DatePicker for selecting the date of visit.
     */
    @FXML
    private DatePicker datePicker;

    /**
     * ComboBox for selecting the park.
     */
    @FXML
    private MFXLegacyComboBox<String> parkCmbBox;

    /**
     * ComboBox for selecting the time of visit.
     */
    @FXML
    private MFXLegacyComboBox<String> timeOfVisitCmbBox;

    /**
     * TextField for entering the visitor's email.
     */
    @FXML
    private TextField txtEmail;

    /**
     * TextField for entering the visitor's first name.
     */
    @FXML
    private TextField txtFirstName;

    /**
     * TextField for entering the visitor's last name.
     */
    @FXML
    private TextField txtLastName;

    /**
     * TextField for entering the number of visitors.
     */
    @FXML
    private TextField txtNumOfVisitors;

    /**
     * TextField for entering the visitor's phone number.
     */
    @FXML
    private TextField txtPhone;


    public void cleanup() {
        clearFields();
    }

    /**
     * Populates the park selection combo box with available parks.
     * The parks are retrieved from a static data source.
     */
    private void setParkCmbBox() {
        ArrayList<String> al = new ArrayList<String>();
        for (String key : ParkBank.getUnmodifiableMap().keySet()) {
            al.add(key);
        }
        list = FXCollections.observableArrayList(al);
        parkCmbBox.setItems(list);
    }

    /**
     * Disables date selection for past dates and dates more than one year into the future
     * in the {@link DatePicker}.
     */
    private void setDatePicker() {
        datePicker.setDayCellFactory(picker -> new DateCell() {
            LocalDate maxDate = LocalDate.now().plusYears(1); // Setting maximum date to one year from now

            @Override
            public void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);
                setDisable(empty || date.compareTo(LocalDate.now()) < 0 || date.compareTo(maxDate) > 0);
            }
        });
    }

    /**
     * Initializes the controller class. This method is automatically called
     * after the FXML file has been loaded. It initializes the form components
     * and sets up any necessary data bindings or event handlers.
     *
     * @param location  The location used to resolve relative paths for the root object, or null if unknown.
     * @param resources The resources used to localize the root object, or null if not localized.
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setParkCmbBox();
        timeOfVisitCmbBox.setItems(setComboBoxHours(8, 20));
        setDatePicker();
        clearFields();
    }

    /**
     * Handles the "Create Order" button click event.
     * Validates form inputs, creates an order based on user input, and sends it to the server.
     * Displays confirmation or error messages as appropriate.
     *
     * @param event The action event triggered by clicking the button.
     * @throws CommunicationException If there is a communication issue with the server.
     */
    @FXML
    void OnClickCreateOrderButton(ActionEvent event) throws CommunicationException {
        if (!validateFields()) {
            return;
        }

        if (!(applicationWindowController.getUser() instanceof SingleVisitor)) {
            System.out.println("The user isn't visitor");
            return;
        }
        errorLbl.setText("");
        SingleVisitor visitor = (SingleVisitor) applicationWindowController.getUser();
        Timestamp timeOfVisit = CommonUtils.convertStringToTimestamp(datePicker.getValue().toString(), timeOfVisitCmbBox.getValue());
        Order order = new Order(visitor.getID(), ParkBank.getUnmodifiableMap().get(parkCmbBox.getValue()), timeOfVisit, txtEmail.getText(), txtPhone.getText(), null, timeOfVisit, null, null, OrderType.ORD_TYPE_SINGLE, (CommonUtils.convertStringToInt(txtNumOfVisitors.getText())));
        Object msg = new Message(OpCodes.OP_CREATE_NEW_VISITATION, visitor.getUsername(), order);
        ClientUI.client.accept(msg);
        Message respondMsg = ClientCommunicator.msg;
        OpCodes returnOpCode = respondMsg.getMsgOpcode();
        if (returnOpCode == OpCodes.OP_DB_ERR) {
            ConfirmationPopup confirmationPopup = new ConfirmationPopup(CommonUtils.DB_ERROR, applicationWindowController, 800, 400, true, "OK", true);
            confirmationPopup.show(applicationWindowController.getRoot());
            return;
        }
        if (returnOpCode != OpCodes.OP_CREATE_NEW_VISITATION && returnOpCode != OpCodes.OP_NO_AVAILABLE_SPOT && returnOpCode != OpCodes.OP_ORDER_ALREADY_EXIST) {
            ConfirmationPopup confirmationPopup = new ConfirmationPopup(CommonUtils.SERVER_ERROR, applicationWindowController, 800, 400, true, "OK", true);
            confirmationPopup.show(applicationWindowController.getRoot());
            return;
        }
        if (returnOpCode == OpCodes.OP_CREATE_NEW_VISITATION && !(respondMsg.getMsgData() instanceof Order)) {
            ConfirmationPopup confirmationPopup = new ConfirmationPopup(CommonUtils.SERVER_ERROR, applicationWindowController, 800, 400, true, "OK", true);
            confirmationPopup.show(applicationWindowController.getRoot());
            return;
        }
        if (returnOpCode == OpCodes.OP_CREATE_NEW_VISITATION) {
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
        } else if (returnOpCode == OpCodes.OP_ORDER_ALREADY_EXIST) {
            String strForPopup = "you already have an order with these details";
            ConfirmationPopup confirmPopup = new ConfirmationPopup(strForPopup, () ->
            {
            }
                    , 600, 300, false, "OK", false);
            confirmPopup.show(applicationWindowController.getRoot());

        } else {
            String strForPopup = "The park is at full capacity. Would you like to signup to the waitlist or view alternative times?";
            ConfirmationPopup confirmPopup;
            String fullName = "" + txtFirstName.getText() + " " + txtLastName.getText();
            boolean menuSliderActivatedFlag = applicationWindowController.isMenuSlider();
            confirmPopup = new ConfirmationPopup(strForPopup, () ->
            {
                if (menuSliderActivatedFlag) {
                    applicationWindowController.loadVisitorsPage("WaitListPage");
                } else {
                    applicationWindowController.setCenterPageForNewVisitor("/VisitorsUI/WaitListPage.fxml", applicationWindowController.getUser(), "/CommonClient/gui/LeftBackground.fxml");
                }
                Object controller = applicationWindowController.getCurrentActiveController();
                if (controller instanceof WaitListPageController) {
                    ((WaitListPageController) controller).setFields(order, fullName);
                }
                clearFields();
            }, () -> {
                if (menuSliderActivatedFlag) {
                    applicationWindowController.loadVisitorsPage("AlternativeTimesTable");
                } else {
                    applicationWindowController.setCenterPageForNewVisitor("/VisitorsUI/AlternativeTimesTable.fxml", applicationWindowController.getUser(), "/CommonClient/gui/LeftBackground.fxml");
                }
                Object controller = applicationWindowController.getCurrentActiveController();
                if (controller instanceof AlternativeTimesTableController) {
                    ((AlternativeTimesTableController) controller).start(order, fullName);
                }
                clearFields();
            },
                    850, 400, false, "WaitList", "Alternative times", false);
            confirmPopup.show(applicationWindowController.getRoot());
        }

    }


    /**
     * Validates the input fields of the form. Checks for empty fields, valid email, phone number,
     * and the number of visitors.
     *
     * @return true if all inputs are valid, false otherwise.
     */
    private boolean validateFields() {
        if (CommonUtils.anyStringEmpty(txtFirstName.getText(), txtLastName.getText(), txtPhone.getText(), txtEmail.getText(), txtNumOfVisitors.getText()) || parkCmbBox.getValue() == null || datePicker.getValue() == null || timeOfVisitCmbBox.getValue() == null) {
            errorLbl.setText("One or more fields are empty.");
            return false;
        }
        if (!CommonUtils.isValidPhone(txtPhone.getText())) {
            errorLbl.setText("Invalid phone. Please check your input.");
            return false;
        }

        if (CommonClient.Utils.isOrderTimeValid(datePicker.getValue().toString(), timeOfVisitCmbBox.getValue().toString())) {
            errorLbl.setText("Invalid visitation date. You cannot book an order less than 24 hours of the chosen visitation time.");
            return false;
        }
        if (!CommonUtils.isValidName(txtFirstName.getText()) || !CommonUtils.isValidName(txtLastName.getText()))
            errorLbl.setText("Please enter a valid first and last name.");
        if (!CommonUtils.isEmailAddressValid(txtEmail.getText())) {
            errorLbl.setText("Invalid email. Please check your input.");
            return false;
        }
        if (CommonUtils.convertStringToInt(txtNumOfVisitors.getText()) <= 0) {
            errorLbl.setText("Invalid number of visitors. Please check your input.");
            return false;
        }
        return true;
    }

    /**
     * Clears all input fields and resets the form to its initial state.
     */
    public void clearFields() {
        // Clear text fields
        txtEmail.clear();
        txtFirstName.clear();
        txtLastName.clear();
        txtPhone.clear();
        txtNumOfVisitors.setText("1");
        errorLbl.setText("");

        // Reset combo boxes
        parkCmbBox.getSelectionModel().clearSelection();
        timeOfVisitCmbBox.getSelectionModel().clearSelection();
        // Use this if the combo box text does not clear with clearSelection()
        parkCmbBox.setValue(null);
        timeOfVisitCmbBox.setValue(null);

        // Reset the date picker
        datePicker.setValue(null);
    }

    public void navigateToHomePage() {
        applicationWindowController.setCenterPage("/CommonClient/gui/HomePage.fxml");
    }

}