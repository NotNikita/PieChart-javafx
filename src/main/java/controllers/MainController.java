package controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class MainController implements Initializable {

    @FXML
    private Button noPathChartButton;

    public MainController(){}

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        noPathChartButton.setOnAction(event ->{
            try {
                String fxmlFile = "/Views/noPathChart.fxml";
                FXMLLoader loader = new FXMLLoader();
                Parent parent = loader.load(getClass().getResourceAsStream(fxmlFile));
                Scene scene = new Scene(parent, 1250,634);
                scene.getStylesheets().add("Views/styles.css");
                Stage stage = new Stage();
                stage.setScene(scene);
                stage.setTitle("PieChart with Arcs from shape.Arc");
                stage.show();
            } catch (NullPointerException | IOException e) {
                e.printStackTrace();
            }
        });
    }
}
