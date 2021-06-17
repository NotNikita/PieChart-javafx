package controllers;

import classes.ClientSocketHandler;
import classes.PieChartNoPath;
import db.DatabaseHandler;
import javafx.beans.binding.DoubleBinding;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.chart.PieChart;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;

public class NoPathController {

    @FXML
    private AnchorPane apMain;
    @FXML
    private Pane paneChart;
    @FXML
    private Button btnDelete;
    @FXML
    private Button btnAdd;
    @FXML
    private Button btnEdit;
    @FXML
    private TextField fieldValue;
    @FXML
    private TextField fieldName;

    private ObservableList<PieChart.Data> dataList;
    private ClientSocketHandler cSHandler;
    private PieChartNoPath customPie;

    public NoPathController() {
        //createClientSocket();
    }

    @FXML
    void initialize() {
        initList();
        createCustomPie();
        setButtonActions();
        // Responsive-Scaling behaviour
        addChartScaling();
    }

    private void initList() {
        DatabaseHandler dbHandler = new DatabaseHandler();
        dataList = dbHandler.getDataNoTime();
        System.out.println(dataList);
    }

    private void setButtonActions() {
        btnAdd.setOnAction(event ->{
            // Adding items to Pie chart
            if(!fieldName.getText().trim().isEmpty() && !fieldValue.getText().trim().isEmpty()){
                String field_name = fieldName.getText();
                double field_value = Double.parseDouble(fieldValue.getText());
                customPie.addChartSlice(new PieChart.Data(field_name, field_value));
            }
        });
        btnEdit.setOnAction(event ->{
            if(!fieldName.getText().trim().isEmpty() && !fieldValue.getText().trim().isEmpty()){
                String field_name = fieldName.getText();
                double field_value = Double.parseDouble(fieldValue.getText());
                customPie.editChartSlice(new PieChart.Data(field_name, field_value));
            }
        });
        btnDelete.setOnAction(event ->{
            if(!fieldName.getText().trim().isEmpty()){
                String field_name = fieldName.getText();
                customPie.deleteChartSlice(field_name);
            }
        });
    }

    private void createCustomPie() {
        customPie = new PieChartNoPath(dataList, paneChart, true);
        customPie.paint();
        customPie.updateTotalAmountLabel();
    }

    private void addChartScaling() {
        apMain.widthProperty().addListener(e -> {
            DoubleBinding scaleValue = this.apMain.widthProperty().divide(apMain.maxWidthProperty());
            scaleChartPane(scaleValue);
        });
        apMain.heightProperty().addListener(e -> {
            DoubleBinding scaleValue = this.apMain.heightProperty().divide(apMain.maxHeightProperty());
            scaleChartPane(scaleValue);
        });
    }

    void scaleChartPane(DoubleBinding scaleValue){
        paneChart.scaleXProperty().bind(scaleValue);
        paneChart.scaleYProperty().bind(scaleValue);
    }
}
