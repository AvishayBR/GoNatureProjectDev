package EmployeesControllers;

import CommonClient.controllers.BaseController;
import io.github.palexdev.materialfx.controls.MFXButton;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.chart.StackedBarChart;
import javafx.scene.text.Text;

public class IssueCancellationReportController extends BaseController {

    @FXML
    private MFXButton btnDownloadCSV;

    @FXML
    private StackedBarChart<?, ?> chartCancellationStatistics;

    @FXML
    private Text txtDate;

    @FXML
    private Text txtNoReport;

    @FXML
    void OnClickDownloadCSVButton(ActionEvent event) {

    }

}
