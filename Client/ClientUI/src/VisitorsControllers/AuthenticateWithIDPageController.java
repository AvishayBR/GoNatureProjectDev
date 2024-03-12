//package VisitorsControllers;
//
//import io.github.palexdev.materialfx.controls.MFXButton;
//import io.github.palexdev.materialfx.controls.MFXTextField;
//import javafx.event.ActionEvent;
//import javafx.fxml.FXML;
//import javafx.scene.control.Separator;
//import javafx.scene.layout.Pane;
//import javafx.scene.layout.StackPane;
//import javafx.scene.text.Text;
//
//public class AuthenticateWithIDPageController {
//
//    @FXML
//    private Text ErrorText;
//
//    @FXML
//    private MFXTextField IDTextField;
//
//    @FXML
//    private Pane Pane;
//
//    @FXML
//    private StackPane StackPane;
//
//    @FXML
//    private MFXButton btnSubmit;
//
//    @FXML
//    private Text header;
//
//    @FXML
//    private Separator sep;
//
//    @FXML
//    private Text text1;
//
//    @FXML
//    private Text text2;
//
//    @FXML
//    void OnClickSubmitAuthButton(ActionEvent event) {
//
//    }
//
//    private String getIDFromTxtBox()
//    {
//        return IDTextField.getText();
//    }
//
//    @FXML
//    private void checkTheID(ActionEvent event) {
//        String enteredID = IDTextField.getText();
//        // Replace "expectedID" with the actual ID you're expecting
//        String expectedID = "your_expected_id_here";
//
//        if (enteredID.equals(expectedID)) {
//            // ID is correct, do something (e.g., navigate to the next page)
//            System.out.println("ID is correct!");
//        } else {
//            // ID is incorrect, show error message
//            ErrorText.setText("Incorrect ID. Please try again.");
//        }
//    }
//}
