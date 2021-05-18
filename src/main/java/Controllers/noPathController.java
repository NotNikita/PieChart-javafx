package Controllers;

import Classes.ClientSocketHandler;
import Classes.PieChartNoPath;
import DB.DatabaseHandler;
import javafx.application.Platform;
import javafx.beans.binding.DoubleBinding;
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

import java.io.BufferedReader;
import java.io.PrintStream;
import java.net.Socket;

public class noPathController {
    ObservableList<PieChart.Data> dataList = FXCollections.observableArrayList();
    ClientSocketHandler cSHandler;
    PieChartNoPath customPie;

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

    public noPathController() throws InterruptedException {
        //dataList.add(new PieChart.Data("Jack", 10));
        //dataList.add(new PieChart.Data("Sam", 15));
        //dataList.add(new PieChart.Data("Mike", 20));
        //dataList.add(new PieChart.Data("John", 20));
        //dataList.add(new PieChart.Data("Nik", 35));
        //dataList.add(new PieChart.Data("Suspect", 35));

        ObservableList<PieChart.Data> ListFromDataBase;
        DatabaseHandler dbHandler = new DatabaseHandler();
        ListFromDataBase = dbHandler.getDataNoTime();
        System.out.println(ListFromDataBase);
        dataList = ListFromDataBase;
            // FOR SERVER
        //new Thread(() -> {
        //    cSHandler = new ClientSocketHandler(dataList);
        //    dataList = cSHandler.getDataFromSocket();
        //    while (true){
        //        try {
        //            Thread.sleep( 15000 );
        //            cSHandler = new ClientSocketHandler(dataList);
        //            ObservableList<PieChart.Data> newList = cSHandler.getDataFromSocket();
        //
        //            for (int i = 0; i < dataList.size(); i++) {
        //                if(dataList.get(i).getPieValue() != newList.get(i).getPieValue()){
        //                    customPie.editNode(new PieChart.Data(newList.get(i).getName(), newList.get(i).getPieValue()));
        //                    dataList = newList;
        //                    Platform.runLater(
        //                            // Because we cant update UI from NON-application thread
        //                            // Update UI here.
        //                            this::updateTotalAmountLabel
        //                    );
        //                }
        //            }
        //
        //
        //        } catch (InterruptedException e) {
        //            e.printStackTrace();
        //        }
        //    }
        //
        //}).start();
        //Thread.sleep( 1000 );
    }

    @FXML
    void initialize() {
        Group root = new Group();
        customPie = new PieChartNoPath(dataList, root, true);
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

                updateTotalAmountLabel();
            }
        });
        editButton.setOnAction(event ->{
            if(!nameField.getText().trim().isEmpty() && !valueField.getText().trim().isEmpty()){
                String field_name = nameField.getText();
                double field_value = Double.parseDouble(valueField.getText());
                customPie.editNode(new PieChart.Data(field_name, field_value));

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
            DoubleBinding scaleValue = this.apMain.widthProperty().divide(apMain.maxWidthProperty());
            chartPaneContainer.scaleXProperty().bind(scaleValue);
            chartPaneContainer.scaleYProperty().bind(scaleValue);
        });
        apMain.heightProperty().addListener(e -> {
            DoubleBinding scaleValue = this.apMain.heightProperty().divide(apMain.maxHeightProperty());
            chartPaneContainer.scaleXProperty().bind(scaleValue);
            chartPaneContainer.scaleYProperty().bind(scaleValue);
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
