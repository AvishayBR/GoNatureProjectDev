package CommonUtils;

import CommonClient.controllers.BaseController;
import javafx.animation.TranslateTransition;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

import java.util.Objects;

public abstract class BasePopup {
    protected BorderPane root;
    protected VBox popup = new VBox(10);
    protected StackPane modalLayer = new StackPane();
    protected boolean fullScreenMode;
    private Node previousPageChild;
    protected int popupWidth;
    protected int popupHeight;


    public BasePopup(boolean fullScreen, int width, int height) {
        this.fullScreenMode = fullScreen;
        this.popupWidth = fullScreen ? 1500 : width;
        this.popupHeight = fullScreen ? 1100 : height;
        setupPopup();
    }

    private void setupPopup() {
        popup.setMaxSize(popupWidth, popupHeight);
        popup.setPrefSize(popupWidth, popupHeight);
        popup.setAlignment(Pos.CENTER);
        popup.setStyle("-fx-background-color: #263238;");
        //modalLayer.setStyle("-fx-background-color: rgba(0, 0, 0, 0.4);");
        modalLayer.setAlignment(Pos.CENTER);
        modalLayer.getChildren().add(popup);
        modalLayer.setVisible(false);
        popup.setTranslateY(-1200); // Initially off-screen
    }

    public void show(BorderPane parent) {
        this.root = parent;
        if (!parent.getChildren().contains(modalLayer)) {
            previousPageChild = parent.getCenter();
            if (!fullScreenMode) {
                modalLayer.getChildren().add(parent.getCenter());
                popup.toFront();
            }
            parent.setCenter(modalLayer);
        }
        modalLayer.setVisible(true);
        TranslateTransition transition = new TranslateTransition(Duration.seconds(0.3), popup);
        transition.setFromY(-1200);
        transition.setToY(0);
        transition.play();
    }

    public void closePopup(boolean onCloseNavigateToPage) {
        TranslateTransition closeTransition = new TranslateTransition(Duration.seconds(0.3), popup);
        closeTransition.setToY(-1200);
        closeTransition.setOnFinished(event -> {
            modalLayer.setVisible(false);
            if (onCloseNavigateToPage) {
                return;
            }
            if (this.fullScreenMode) {
                this.root.setCenter(previousPageChild);
            }
        });
         closeTransition.play();
    }
}
