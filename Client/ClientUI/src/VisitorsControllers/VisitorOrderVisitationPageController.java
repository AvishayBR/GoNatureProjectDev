package VisitorsControllers;


import io.github.palexdev.materialfx.controls.MFXButton;
import io.github.palexdev.materialfx.controls.MFXTextField;
import io.github.palexdev.materialfx.controls.legacy.MFXLegacyComboBox;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.text.Text;


public class VisitorOrderVisitationPageController {

    @FXML
    private Text Header;

    @FXML
    private MFXButton createOrderBtn;

    @FXML
    private Label dateLbl;

    @FXML
    private DatePicker datePicker;

    @FXML
    private Label emailLbl;

    @FXML
    private MFXTextField fNameText;

    @FXML
    private Label firstNameLbl;

    @FXML
    private Label lastNameLbl;

    @FXML
    private MFXTextField lastNameText;

    @FXML
    private Label numOfVisitorsLbl;

    @FXML
    private MFXTextField numOfVisitorsText;

    @FXML
    private Pane pane;

    @FXML
    private MFXLegacyComboBox<?> parkCmbBox;

    @FXML
    private Label parkLbl;

    @FXML
    private Label phoneLbl;

    @FXML
    private MFXTextField phoneText;

    @FXML
    private Separator sepOrder;

    @FXML
    private StackPane stackPane;

    @FXML
    private MFXLegacyComboBox<?> timeOfVisitCmbBox;

    @FXML
    private Label timeOfVisitLbl;

    @FXML
    private MFXTextField txtEmail;

    @FXML
    void OnClickCreateOrderButton(ActionEvent event) {
    }

//    public void start(Stage primaryStage) throws Exception {
//        Parent root = (Parent) FXMLLoader.load(this.getClass().getResource("/VisitorsUI/VisitorOrderVisitationPage.fxml"));
//        Scene scene = new Scene(root);
//        primaryStage.setTitle("GoNature - Dashboard");
//        primaryStage.setScene(scene);
//        primaryStage.show();
//    }
}