package VisitorsControllers;

import CommonUtils.InputTextPopup;
import Entities.VisitorGroupGuide;
import io.github.palexdev.materialfx.controls.MFXButton;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Separator;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.text.Text;

public class VisitorGroupGuideDashboardPageController extends GeneralVisitorDashboard {

    @FXML
    private StackPane StackPane;

    @FXML
    private Pane Pane;

    @FXML
    private Separator sep;

    @FXML
    private Text header;

    @FXML
    private Text text2;

    @FXML
    private Text text1;

    @FXML
    private Text text11;

    @FXML
    private Text text12;

    @FXML
    private Text text1211;

    @FXML
    private ImageView pngOrderVisit;

    @FXML
    private ImageView pngViewOrders;

    @FXML
    private MFXButton btnOrderVisit;

    @FXML
    private MFXButton bntViewOrders;
    private final String orderPage = "/VisitorsUI/GroupGuideOrderVisitationPage.fxml";
    private final String activeOrdersPage = "/VisitorsUI/ActiveOrdersPage.fxml";

    @Override
    public String getUserID() {
        VisitorGroupGuide visitorGroupGuide = (VisitorGroupGuide) applicationWindowController.getUser();
        return visitorGroupGuide.getID();
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
