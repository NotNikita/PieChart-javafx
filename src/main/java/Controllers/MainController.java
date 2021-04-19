package Controllers;

import Classes.DoughnutChart;
import Classes.PieChartNoPath;
import Classes.PieChartWithPath;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Group;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.PieChart.Data;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import java.io.IOException;

public class MainController {
    ObservableList<Data> dataList = FXCollections.observableArrayList();

    @FXML
    private AnchorPane apMain;

    @FXML
    private TextField valueField;

    @FXML
    private TextField nameField;

    @FXML
    private Button DoughnutChartButton;

    @FXML
    private Button withPathChartButton;

    @FXML
    private Button noPathChartButton;

    @FXML
    private Button setUpPieChart;

    @FXML
    private PieChart pieChart;

    @FXML
    private Label totalNumberLabel;


    public MainController() {
        dataList.add(new Data("Jack", 10));
        dataList.add(new Data("Sam", 15));
        dataList.add(new Data("Mike", 20));
        dataList.add(new Data("John", 20));
        dataList.add(new Data("Nik", 35));
        dataList.add(new Data("Suspect", 35));
    }


    @FXML
    void initialize() {
        // example of iterating the data set
        //for (Data node: dataList) {
        //    System.out.println("name is: "+node.getName()+" and value is: "+ node.getPieValue());
        //}



        withPathChartButton.setOnAction(event ->{
            //Group root = new Group();
            //PieChartWithPath pathPie = new PieChartWithPath(dataList, root);
            //
            //Scene scene = new Scene(root, 800,500, Color.AZURE);
            //Stage stage = new Stage();
            //stage.setScene(scene);
            //stage.setTitle("PieChart with Arcs from PathElements");
            //stage.show();
            //root = pathPie.paint();
            try {
                String fxmlFile = "/Views/withPathChart.fxml";
                FXMLLoader loader = new FXMLLoader();
                Parent parent = loader.load(getClass().getResourceAsStream(fxmlFile));
                Scene scene = new Scene(parent, 1048,634);
                Stage stage = new Stage();
                stage.setScene(scene);
                stage.setTitle("PieChart with Arcs from PathElements");
                stage.show();
            } catch (NullPointerException | IOException e) {
                e.printStackTrace();
            }
        });

        noPathChartButton.setOnAction(event ->{
            try {
                String fxmlFile = "/Views/noPathChart.fxml";
                FXMLLoader loader = new FXMLLoader();
                Parent parent = loader.load(getClass().getResourceAsStream(fxmlFile));
                Scene scene = new Scene(parent, 1048,634);
                Stage stage = new Stage();
                stage.setScene(scene);
                stage.setTitle("PieChart with Arcs from shape.Arc");
                stage.show();
            } catch (NullPointerException | IOException e) {
                e.printStackTrace();
            }
        });
        DoughnutChartButton.setOnAction(event -> {
            try {
                final DoughnutChart chart = new DoughnutChart(dataList);
                chart.setTitle("Imported Fruits");
                chart.setLegendVisible(false);
                Stage stage = new Stage();
                Scene scene = new Scene(new StackPane(chart));
                stage.setScene(scene);
                stage.show();
            } catch (NullPointerException e) {
                e.printStackTrace();
            }
        });
        setUpPieChart.setOnAction(event -> {
            pieChart.setData(dataList);
            totalNumberLabel.setText(String.valueOf(dataList.size()));
        });
    }

}
