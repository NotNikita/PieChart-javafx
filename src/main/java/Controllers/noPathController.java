package Controllers;

import Classes.PieChartNoPath;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.Group;
import javafx.scene.chart.PieChart;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.scene.shape.Circle;

public class noPathController {
    ObservableList<PieChart.Data> dataList = FXCollections.observableArrayList();

    @FXML
    private AnchorPane apMain;

    @FXML
    private Button deleteButton;
    @FXML
    private Button addButton;
    @FXML
    private Button editButton;

    @FXML
    private Circle chartCircle;
    @FXML
    private Label totalNumberLabel;
    @FXML
    private TextField valueField;
    @FXML
    private TextField nameField;
    @FXML
    private Label peopleLabel;

    public noPathController() {
        dataList.add(new PieChart.Data("Jack", 10));
        dataList.add(new PieChart.Data("Sam", 15));
        dataList.add(new PieChart.Data("Mike", 20));
        dataList.add(new PieChart.Data("John", 20));
        dataList.add(new PieChart.Data("Nik", 35));
        dataList.add(new PieChart.Data("Suspect", 35));
    }

    @FXML
    void initialize() {
        Group root = new Group();
        PieChartNoPath customPie = new PieChartNoPath(dataList, root);
        root = customPie.paint();
        apMain.getChildren().add(root);
        chartCircle.toFront();
        peopleLabel.toFront();
        totalNumberLabel.toFront();

        addButton.setOnAction(event ->{
            // Adding items to Pie chart
            String field_name = nameField.getText();
            Double field_value = Double.valueOf(valueField.getText());
            customPie.addNode(new PieChart.Data(field_name, field_value));

            customPie.paint();
        });
        editButton.setOnAction(event ->{
            String field_name = nameField.getText();
            Double field_value = Double.valueOf(valueField.getText());
            customPie.editNode(new PieChart.Data(field_name, field_value));

            customPie.paint();
        });
        deleteButton.setOnAction(event ->{
            String field_name = nameField.getText();
            if (customPie.deleteNode(field_name))
                customPie.paint();
        });
    }
}
