package controllers;

import classes.ClientSocketHandler;
import classes.PieChartNoPath;
import javafx.beans.binding.DoubleBinding;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.chart.PieChart;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import org.json.JSONArray;
import org.json.JSONObject;
import socket.SelectionDataSocket;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.UUID;

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
    @FXML
    private VBox vbox;

    private ObservableList<PieChart.Data> dataList = FXCollections.observableArrayList();
    private ClientSocketHandler cSHandler;
    private PieChartNoPath customPie;

    public NoPathController() {
    }

    @FXML
    void initialize() throws IOException {
        initList();
        createCustomPie();
        setButtonActions();
        // Responsive-Scaling behaviour
        addChartScaling();
    }

    private void initList() throws IOException {
        executeChartDataUpdate();
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
        try {
            String json = sendGET();
            updatePieDataList(json);
        } catch (IOException ignored){

        }
        customPie = new PieChartNoPath(dataList, paneChart,vbox, true);
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
    public void updatePieDataList(String json) {
        JSONObject jsonObject = new JSONObject(json);
        JSONArray jArray = jsonObject.getJSONArray("chartData");
        dataList.clear();
        if (jArray != null) {
            for ( int i=0; i < jArray.length(); i++ ){
                JSONObject jObj = jArray.getJSONObject(i); //{"Касса":660} ...
                String key = jObj.keys().next();
                dataList.add( new PieChart.Data( key, jObj.getDouble(key)));
            }
        }
    }

    private void executeChartDataUpdate() throws IOException {

        SelectionDataSocket socket = new SelectionDataSocket();
        socket.startTransferData(this);
        //String json = sendGET();
        //updatePieDataList(json);
    }

    public static String sendGET() throws IOException {
        URL obj = new URL("http://127.0.0.1:8080/shop/chart");
        HttpURLConnection con = (HttpURLConnection) obj.openConnection();
        con.setRequestMethod("GET");
        con.setRequestProperty("User-Agent", "Mozilla/5.0");
        int responseCode = con.getResponseCode();
        StringBuilder response = new StringBuilder();
        System.out.println("GET Response Code :: " + responseCode);
        if (responseCode == HttpURLConnection.HTTP_OK) { // success
            BufferedReader in = new BufferedReader(new InputStreamReader(
                    con.getInputStream()));
            String inputLine;


            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();

            // print result
            System.out.println(response.toString());

        } else {
            System.out.println("GET request not worked");
        }
        return response.toString();
    }

    public String getChartUniqueKey() {

        UUID uuid = UUID.randomUUID();
        return uuid.toString();
    }
}

