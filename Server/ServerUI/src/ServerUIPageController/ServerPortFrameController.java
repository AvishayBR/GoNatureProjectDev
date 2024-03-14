package ServerUIPageController;

import GoNatureServer.GoNatureServer;
import ServerUIPage.ServerUI;

import io.github.palexdev.materialfx.controls.MFXButton;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.MapValueFactory;
import javafx.scene.image.Image;
import javafx.scene.input.MouseButton;
import javafx.stage.Stage;

import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;

public class ServerPortFrameController implements Initializable {
    ObservableList<String> list;

    @FXML
    private Button btnExit = null;

    @FXML
    private Button btnStart;

    @FXML
    private Label lbllist;

    @FXML
    private TextArea loggerTextArea;

    @FXML
    private Label lblLogger;

    @FXML
    private Label lblSQLUser;

    @FXML
    private Label lblSQLPassword;

    @FXML
    private Label lblSQLURL;

    @FXML
    private ComboBox URLComboBox;

    @FXML
    private TextField TextfieldUserName;

    @FXML
    private PasswordField TextFieldPassword;

    @FXML
    private Button BtnStop;

    @FXML
    private TextField portxt;

    @FXML
    private TableView<Map> tableClients;

    @FXML
    private TableColumn<Map, String> colName;

    @FXML
    private TableColumn<Map, String> colIP;

    @FXML
    private TableColumn<Map, String> colStatus;




    public String getPort() {
        return portxt.getText();
    }

    public void Done(ActionEvent event) throws Exception {
        String p;
        p = getPort();
        if (p.trim().isEmpty()) {
            addtolog("You must enter a port number");

        } else if (getURLComboBox().isEmpty() || getUserName().isEmpty() || getPassword().isEmpty()) {
            addtolog("You must enter a URL, username and password");
            addtolog("Please try again");
        } else {
            ServerUI.runServer(this);
        }
    }

    public void start(Stage primaryStage) throws Exception {
        Parent root = FXMLLoader.load(getClass().getResource("/ServerUIPageController/ServerPort.fxml"));
        Scene scene = new Scene(root);
        scene.getStylesheets().add(getClass().getResource("/ServerUIPageController/ServerPort.css").toExternalForm());

        Image windowImage = new Image("/assets/GoNatureServerLogo.png");
        primaryStage.getIcons().add(windowImage);

        primaryStage.setTitle("GoNature - Server");

        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public void setDefaultValues() {
        this.TextFieldPassword.setText("Aa123456");
        this.TextfieldUserName.setText("root");
        this.portxt.setText("5555");
        // Set default value for the ComboBox to "localhost" if it exists in the items list
        String defaultURL = "localhost";
        if (list.contains(defaultURL)) {
            this.URLComboBox.setValue(defaultURL);
        } else {
            // Handle the case where the default value doesn't exist in the items list
            // You might want to set a different default value or handle this case differently
            System.err.println("Default URL not found in the list: " + defaultURL);
        }
    }
    @FXML
    public void getExitBtn(ActionEvent event) throws Exception {
        addtolog("Exit Server");
        System.exit(0);
    }

    @FXML
    void stopServer(ActionEvent event) throws Exception {
        ServerUI.closeServer();
    }

    public synchronized void addtolog(String str) {
        System.out.println(str);
        Platform.runLater(() -> loggerTextArea.appendText(str + "\n"));
    }
    private void setURLComboBox() {
        ArrayList<String> UrlComboList = new ArrayList<>();
        UrlComboList.add("localhost");
        UrlComboList.add("192.168.194.1");
        list = FXCollections.observableArrayList(UrlComboList);
        URLComboBox.setItems(list);
    }




    public String getURLComboBox() {
        return (String) URLComboBox.getValue();
    }

    public String getUserName() {
        return TextfieldUserName.getText();
    }

    public String getPassword() {
        return TextFieldPassword.getText();
    }

    @Override
    public void initialize(URL arg0, ResourceBundle arg1) {
        setURLComboBox();
        setDefaultValues();
        colName.setCellValueFactory(new MapValueFactory<>("name"));
        colIP.setCellValueFactory(new MapValueFactory<>("ip"));
        colStatus.setCellValueFactory(new MapValueFactory<>("status"));
        tableClients.setRowFactory(tv -> {
            TableRow<Map> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (!row.isEmpty() && event.getButton() == MouseButton.PRIMARY && event.getClickCount() == 1) {
                    Map clickedRowData = row.getItem();
                    // Handle the clicked row data, e.g., display it
                    System.out.println("Selected row data: " + clickedRowData);
                }
            });
            return row;
        });
    }
    public void resetTableClients() {
        tableClients.getItems().clear();
    }

    public void addRow(String name, String ip) {
        // Todo: instead of ip, map the SOCKET, so that we can use it to send messages to the client
        Map<String, String> newRow = new HashMap<>();
        newRow.put("name", name);
        newRow.put("ip", ip);
        newRow.put("status", "Connected");

        tableClients.getItems().add(newRow);
    }

    public void removeRowByIP(String ip) {
        // Use removeIf with a predicate to remove rows matching the condition
        tableClients.getItems().removeIf(row -> ip.equals(row.get("ip")));
    }
    @FXML
    public void toggleControllers(boolean flag) {
        btnStart.setDisable(flag);
        TextfieldUserName.setDisable(flag);
        TextFieldPassword.setDisable(flag);
        URLComboBox.setDisable(flag);
        portxt.setDisable(flag);
        BtnStop.setDisable(!flag);
    }
}