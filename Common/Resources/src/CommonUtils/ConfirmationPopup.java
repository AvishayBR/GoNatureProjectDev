package CommonUtils;

import javafx.scene.control.Button;
import javafx.scene.control.Label;

public class ConfirmationPopup extends BasePopup {
    private Button yesButton = new Button();
    private Button noButton = new Button();


    public ConfirmationPopup(String question, Runnable onConfirm, Runnable onCancel, int width, int height, boolean fullScreenMode, String FirstBtn, String SecondBtn, boolean exitOnOut) {
        super(fullScreenMode, width, height);
        yesButton.setText(FirstBtn);
        noButton.setText(SecondBtn);
        Label questionLabel = new Label(question);
        popup.getChildren().addAll(questionLabel, yesButton, noButton);

        yesButton.setOnAction(e -> {
            onConfirm.run();
            closePopup(false);
        });

        noButton.setOnAction(e -> {
            if (onCancel != null) onCancel.run();
            closePopup(false);
        });
        if (exitOnOut) {
            modalLayer.setOnMouseClicked(e -> {
                if (!popup.getBoundsInParent().contains(e.getSceneX(), e.getSceneY())) {
                    closePopup(false);
                }
            });
        }
    }

    public ConfirmationPopup(String question, Runnable onConfirm, int width, int height, boolean fullScreenMode, String FirstBtn, boolean exitOnOut) {
        super(fullScreenMode, width, height);
        yesButton.setText(FirstBtn);
        yesButton.getStyleClass().add("menu-item-big");
        Label questionLabel = new Label(question);
        questionLabel.setStyle("-fx-font-size: 20px;-fx-text-fill: white;");
        popup.getChildren().addAll(questionLabel, yesButton);

        yesButton.setOnAction(e -> {
            onConfirm.run();
            closePopup(false);
        });
        if (exitOnOut) {
            modalLayer.setOnMouseClicked(e -> {
                if (!popup.getBoundsInParent().contains(e.getSceneX(), e.getSceneY())) {
                    closePopup(false);
                }
            });
        }
    }
}
