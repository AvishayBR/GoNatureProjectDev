package HomePage;

import io.github.palexdev.materialfx.controls.MFXButton;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.control.Label;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import java.io.IOException;
import javafx.stage.Stage;
import javafx.scene.image.Image;
import javafx.scene.Scene;

/**
 * Controller class for the home page of the application.
 */
public class HomePageController
{
    @FXML
    private MFXButton btnOrderNowHome;

    @FXML
    private Pane homePagePane;

    @FXML
    private StackPane homePageStack;

    @FXML
    private Label lblHeaderHome;

    @FXML
    private ImageView pngBackGround;

    /**
     * Handles the "Order Now" button click event.
     * Opens the VisitorOrderVisitationPage.fxml in a new stage and closes the current window.
     * @param event The ActionEvent triggered by the button click.
     */
    @FXML
    void onClickOrderNowHomeButton(ActionEvent event) {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("VisitorOrderVisitationPage.fxml"));
        Parent root = null;
        try {
            root = loader.load();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        Stage stage = new Stage();
        stage.setTitle("Order Visitation");
        stage.setScene(new Scene(root));
        stage.show();

        // Close the current window
        Stage currentStage = (Stage) btnOrderNowHome.getScene().getWindow();
        currentStage.close();
    }

    /**
     * Initializes the controller.
     * Sets the background image for the home page.
     */
    @FXML
    public void initialize() {
        // Set background image
        Image backgroundImage = new Image(getClass().getResourceAsStream("Background.png"));
        pngBackGround.setImage(backgroundImage);
    }
}
