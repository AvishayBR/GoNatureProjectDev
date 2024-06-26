package EmployeesControllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;

/**
 * This class represents the controller for the support representative dashboard page.
 * It extends the GeneralEmployeeDashboardController and provides specific functionality for support representatives,
 * including handling actions such as registering guides and updating exit times.
 * The class contains methods to handle the action events triggered by clicking the corresponding buttons on the dashboard.
 */

public class SupportRepresentativeDashboardControllerPageController extends GeneralEmployeeDashboardController {

    /**
     * Handles the action event triggered by clicking the "Register Guide" button.
     * This method navigates to the "RegisterGroupGuidePage" by loading the corresponding page in the application window controller.
     *
     * @param ignoredEvent The ActionEvent triggered by clicking the "Register Guide" button (ignored).
     */
    @FXML
    public void OnClickRegisterGuideButton(ActionEvent ignoredEvent) {
        applicationWindowController.loadEmployeesPage("RegisterGroupGuidePage");
    }

    /**
     * Handles the action event triggered by clicking the "Update Exit Time" button.
     * This method invokes the {@code OnClickExitButton} method of the superclass, passing a {@code null} argument.
     * It essentially delegates the update exit time action to the superclass.
     *
     * @param event The ActionEvent triggered by clicking the "Update Exit Time" button.
     */
    public void OnClickUpdateExitTime(ActionEvent event) {
        super.OnClickExitButton(null);
    }
}
