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
import javafx.scene.layout.Pane;
import javafx.scene.shape.Circle;
import org.apache.commons.math3.util.Precision;

public class noPathController {
    ObservableList<PieChart.Data> dataList = FXCollections.observableArrayList();

    @FXML
    private AnchorPane apMain;
    @FXML
    private Pane chartPaneContainer;

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
        PieChartNoPath customPie = new PieChartNoPath(dataList, root, true);
        root = customPie.paint();
        //apMain.getChildren().add(root);
        chartPaneContainer.getChildren().add(root);
        chartPaneContainer.getChildren().add(chartCircle);
        chartPaneContainer.getChildren().add(peopleLabel); // was x:308 y:316
        chartPaneContainer.getChildren().add(totalNumberLabel); // was x:267 y:245
        chartCircle.toFront();
        peopleLabel.toFront();
        updateTotalAmountLabel();
        totalNumberLabel.toFront();

        addButton.setOnAction(event ->{
            // Adding items to Pie chart
            if(!nameField.getText().trim().isEmpty() && !valueField.getText().trim().isEmpty()){
                String field_name = nameField.getText();
                double field_value = Double.parseDouble(valueField.getText());
                customPie.addNode(new PieChart.Data(field_name, field_value));

                customPie.paint();
                updateTotalAmountLabel();
            }
        });
        editButton.setOnAction(event ->{
            if(!nameField.getText().trim().isEmpty() && !valueField.getText().trim().isEmpty()){
                String field_name = nameField.getText();
                double field_value = Double.parseDouble(valueField.getText());
                customPie.editNode(new PieChart.Data(field_name, field_value));

                //customPie.paint();
                updateTotalAmountLabel();
            }
        });
        deleteButton.setOnAction(event ->{
            if(!nameField.getText().trim().isEmpty()){
                String field_name = nameField.getText();
                customPie.deleteNode(field_name);

                updateTotalAmountLabel();
            }
        });
        
        // Responsive-Scaling behaviour
        apMain.widthProperty().addListener(e -> {
            //chartPaneContainer.prefWidthProperty().bind(apMain.widthProperty());
            //chartPaneContainer.setScaleX(); // its void
            chartPaneContainer.scaleXProperty().bind(this.apMain.widthProperty().divide(apMain.maxWidthProperty()));
        });
        apMain.heightProperty().addListener(e -> {
            chartPaneContainer.scaleYProperty().bind(this.apMain.heightProperty().divide(apMain.maxHeightProperty()));

            double scalingValue = this.apMain.widthProperty().divide(apMain.maxWidthProperty()).get();
            System.out.println("Scaling by " + Precision.round(scalingValue, 2));
        });
    }

    void updateTotalAmountLabel(){
        int totalDataSum = 0;
        for (PieChart.Data node: dataList) {
            totalDataSum += node.getPieValue();
        }

        totalNumberLabel.setText(String.valueOf(totalDataSum));
    }
}
