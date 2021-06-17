package db;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.chart.PieChart.Data;

import java.sql.*;

public class DatabaseHandler extends Configs {
    Connection dbconnection;

    public Connection getDbconnection()
            throws ClassNotFoundException, SQLException {

        String connectionString = "jdbc:mysql://" + dbHost + ":" + dbPort + "/" + dbName
                + "?useUnicode=true&useJDBCCompliantTimezoneShift=true&useLegacyDatetimeCode=false&serverTimezone=UTC";

        Class.forName("com.mysql.cj.jdbc.Driver");

        dbconnection = DriverManager.getConnection(connectionString, dbUser,
                dbPass);

        if (dbconnection != null) {
            System.out.println("Successfully connected to MySQL database test");
        }

        return dbconnection;
    }

    public ObservableList<Data> getDataNoTime() {
        ObservableList<Data> ListToReturn = FXCollections.observableArrayList();
        String query = "SELECT " + Const.REGISTERS_TABLE + '.' +Const.REGISTERS_TYPE + ", SUM("+Const.ADMISSIONS_SOLD+") FROM " + Const.ADMISSIONS_TABLE+
                " RIGHT JOIN " + Const.REGISTERS_TABLE + " ON " + Const.ADMISSIONS_TABLE + '.' + Const.ADMISSIONS_REGISTER +'='+ Const.REGISTERS_TABLE + '.' + Const.REGISTERS_ID+
                " GROUP BY "+ Const.ADMISSIONS_REGISTER;

        try {
            PreparedStatement prSt = getDbconnection().prepareStatement(query);
            ResultSet resultSet = prSt.executeQuery(query);

            while(resultSet.next()){
                Data dataNode = new Data(resultSet.getString(1), resultSet.getInt(2));
                ListToReturn.add(dataNode);
            }

            System.out.println("Closing connection and releasing resources...");
            resultSet.close();
            prSt.close();
        }
        catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();
        }

        return ListToReturn;
    }

}