package CommonUtils;

import io.github.palexdev.materialfx.controls.MFXButton;
import io.github.palexdev.materialfx.controls.MFXTextField;
import javafx.scene.control.Label;

import java.util.function.Consumer;

public class InputTextPopup extends BasePopup {
    private MFXTextField textField = new MFXTextField();
    private MFXButton confirmButton = new MFXButton("Confirm");
    private MFXButton backButton;
    private Label errorLabel = new Label();

    public void setErrorLabel(String msg) {
        errorLabel.setText(msg);
    }

    public InputTextPopup(String prompt, Consumer<String> onConfirm, int width, int height, boolean fullScreenMode, boolean displayBackButton, boolean onCloseNavigateToPage) {
        super(fullScreenMode, width, height);
        if (displayBackButton) {
            backButton = new MFXButton("Go Back");
            backButton.getStyleClass().add("menu-item-big");
            backButton.setOnAction(e -> closePopup(false));
        }
        Label promptLabel = new Label(prompt);
        promptLabel.setStyle("-fx-text-fill: #FCFCFC; -fx-font-size: 24px; -fx-padding: 10px 10px");
        errorLabel.setStyle("-fx-text-fill: #B22222; " +
                "-fx-font-size: 20px; " +
                "-fx-padding: 10px 10px; " +
                "-fx-border-width: 2px; " +
                "-fx-border-radius: 5px; " +
                "-fx-alignment: center;");
        textField.setMaxWidth(320);

        confirmButton.getStyleClass().add("menu-item-big");
        if (backButton != null) {
            popup.getChildren().add(backButton);
        }
        popup.getChildren().addAll(promptLabel, textField, confirmButton, errorLabel);

        confirmButton.setOnAction(e -> {
            if (onConfirm != null) {
                onConfirm.accept(getText());
            }
            if (errorLabel.getText().isEmpty()) {
                closePopup(onCloseNavigateToPage);
            }
        });

        if (!fullScreenMode) {
            modalLayer.setOnMouseClicked(e -> {
                if (!popup.getBoundsInParent().contains(e.getSceneX(), e.getSceneY())) {
                    closePopup(onCloseNavigateToPage);
                }
            });
        }
    }

    public String getText() {
        return textField.getText();
    }
}
