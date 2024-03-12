package VisitorsControllers;

import io.github.palexdev.materialfx.controls.MFXButton;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.TableView;
import javafx.scene.text.Text;

public class ActiveOrdersPageController {

    @FXML
    private MFXButton bntHandleOrder;

    @FXML
    private TableView<?> tableOrders;

    @FXML
    private Text txtHeader;

    @FXML
    void OnClickHandleOrderButton(ActionEvent event) {

    }

}
