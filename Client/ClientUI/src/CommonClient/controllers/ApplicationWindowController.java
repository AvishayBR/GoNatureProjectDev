package CommonClient.controllers;

import CommonClient.ClientUI;
import Entities.Message;
import Entities.OpCodes;
import Entities.Role;
import Entities.User;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.ResourceBundle;


public class ApplicationWindowController implements Initializable {

    @FXML
    private BorderPane mainPane;
    private Map<String, Parent> pagesCache = new HashMap<>();
    private Parent menuSider;
    private User user;
    private Object Data;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
    }

    private void setUser(User user) {
        this.user = user;
    }

    public User getUser() {
        return user;
    }

    public BorderPane getRoot() {
        return this.mainPane;
    }

    private Parent loadPage(String fxmlPath) {
        try {
            // If the page we're loading is not cached yet.
            if (!pagesCache.containsKey(fxmlPath)) {
                FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
                Parent page = loader.load();
                Object controller = loader.getController();
                if (controller instanceof BaseController) {
                    ((BaseController) controller).setApplicationWindowController(this);
                }
                pagesCache.put(fxmlPath, page);
            }

            return pagesCache.get(fxmlPath);
        } catch (Exception e) {
            e.printStackTrace();
            return null; // or load an error page
        }
    }

    public void loadMenu(User user) {
        setUser(user);
        try {
            if (menuSider == null) {
                // Ensure FXMLLoader is used to load the FXML and retrieve the controller from it
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/CommonClient/gui/MenuSider.fxml"));
                menuSider = loader.load(); // This loads the FXML and initializes the controller

                // Now retrieve the controller
                MenuSiderController menuController = loader.getController();
                if (menuController instanceof BaseController) {
                    ((BaseController) menuController).setApplicationWindowController(this);
                }
                if (menuController != null) { // This check is technically redundant if load() succeeded without exception
                    menuController.setRole(Role.roleToString(user.getRole()));
                    menuController.setUsername(user.getUsername());
                    menuController.buildMenuItems(this);
                } else {
                    // Handle the case where the controller wasn't retrieved successfully
                    System.out.println("Failed to retrieve MenuSiderController.");
                }
            }
            mainPane.setLeft(menuSider);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setCenterPage(String fxmlPath) {
        mainPane.getChildren().remove(mainPane.getCenter());
        Parent page = loadPage(fxmlPath);
        if (page != null) {
            mainPane.setCenter(page);
            Parent newMenu = null;
            if (!Objects.equals(fxmlPath, "/CommonClient/gui/LoginPage.fxml") &&
                    !Objects.equals(fxmlPath, "/CommonClient/gui/HomePage.fxml")) {
                newMenu = menuSider;
            }
            mainPane.setLeft(newMenu);
        }
    }

    public void logout() {
        Object msg = new Message(OpCodes.OP_LOGOUT, user.getUsername(), null);
        ClientUI.client.accept(msg);
        loadDashboardPage(Role.ROLE_GUEST);
        this.user = null;
        menuSider = null;
    }

    public void loadDashboardPage(Role role) {
        Map<Role, String> roleToFxmlPath = new HashMap<>();
        roleToFxmlPath.put(Role.ROLE_PARK_DEPARTMENT_MGR, "/EmployeesUI/DepartmentManagerDashboardPage.fxml");
        roleToFxmlPath.put(Role.ROLE_PARK_MGR, "/EmployeesUI/ParkManagerDashboardPage.fxml");
        roleToFxmlPath.put(Role.ROLE_PARK_EMPLOYEE, "/EmployeesUI/ParkEmployeeDashboardPage.fxml");
        roleToFxmlPath.put(Role.ROLE_PARK_SUPPORT_REPRESENTATIVE, "/EmployeesUI/SupportRepresentativeDashboardPage.fxml");
        roleToFxmlPath.put(Role.ROLE_SINGLE_VISITOR, "/VisitorsUI/VisitorDashboardPage.fxml");
        roleToFxmlPath.put(Role.ROLE_VISITOR_GROUP_GUIDE, "/VisitorsUI/VisitorGroupGuideDashboardPage.fxml");

        String fxmlPath = roleToFxmlPath.getOrDefault(role, "/CommonClient/gui/LoginPage.fxml");
        setCenterPage(fxmlPath);
    }

    public void start(Stage primaryStage) {
        try {
            // Load the main application window directly here
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/CommonClient/gui/ApplicationWindow.fxml"));
            Parent root = loader.load(); // This should initialize mainPane as part of the loading process


            ApplicationWindowController controller = loader.getController();

            Scene scene = new Scene(root);
            scene.getStylesheets().add(getClass().getResource("/CommonClient/styles.css").toExternalForm());

            Image logo = new Image("/assets/GoNatureLogo.png");
            primaryStage.getIcons().add(logo);
            primaryStage.setTitle("GoNature Parks and Recreation Inc.");
            primaryStage.setScene(scene);
            primaryStage.setResizable(false);
            primaryStage.show();

            controller.setCenterPage("/CommonClient/gui/ConnectClientPage.fxml");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public Object getData() {
        return Data;
    }

    public void setData(Object data) {
        Data = data;
    }
}
