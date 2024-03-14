package VisitorsControllers;

import CommonUtils.InputTextPopup;
import Entities.SingleVisitor;
import io.github.palexdev.materialfx.controls.MFXButton;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Separator;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.text.Text;

public class VisitorDashboardPageController extends GeneralVisitorDashboard {
    public VisitorDashboardPageController() {
        super();
    }

    @FXML
    private StackPane StackPane;

    @FXML
    private Pane Pane;

    @FXML
    private Separator sep;

    @FXML
    private Text header;

    @FXML
    private MFXButton btnOrderVisit;

    @FXML
    private MFXButton btnViewOrders;

    @FXML
    private ImageView pngOrderVisit;

    @FXML
    private ImageView pngViewOrders;


    private final String orderPage = "/VisitorsUI/VisitorOrderVisitationPage.fxml";
    private final String activeOrdersPage = "/VisitorsUI/ActiveOrdersPage.fxml";

    @Override
    public String getUserID() {
        SingleVisitor visitor = (SingleVisitor) applicationWindowController.getUser();
        return visitor.getID();
    }

    @FXML
    public void OnClickOrderVisitButton(ActionEvent event) {
        onAuthPopup = new InputTextPopup("Enter ID to Authenticate ", (inputText) -> this.onAuth(inputText, orderPage), 500, 300, true, false, true);
        onAuthPopup.show(applicationWindowController.getRoot());
    }

    @FXML
    public void OnClickViewOrdersButton(ActionEvent event) {
        onAuthPopup = new InputTextPopup("Enter ID to Authenticate ", (inputText) -> this.onAuth(inputText, activeOrdersPage), 500, 300, true, false, true);
        onAuthPopup.show(applicationWindowController.getRoot());
    }


}
