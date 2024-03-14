package EmployeesControllers;

import CommonClient.controllers.BaseController;
import io.github.palexdev.materialfx.controls.MFXButton;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.TableView;

public class CheckAvailableSpotsController extends BaseController {

    @FXML
    private MFXButton btnInsert;

    @FXML
    private TableView<?> tableParkAvailability;

    @FXML
    void OnClickInsertButton(ActionEvent event) {

    }

}
