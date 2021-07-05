package classes;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.chart.PieChart.Data;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.Socket;
import java.util.Iterator;

public class ClientSocketHandler {
    String ListInString;
    ObservableList<Data> list;

    public ClientSocketHandler(ObservableList<Data> listFromController )
    {
        list = listFromController;
        System.out.println( "Loading contents of URL: localhost");

        try
        {
            Socket socket = new Socket( "localhost", 8000 );

            // Create input and output streams to read from and write to the server
            PrintStream out = new PrintStream( socket.getOutputStream() );
            BufferedReader in = new BufferedReader( new InputStreamReader( socket.getInputStream() ) );

            // Follow the HTTP protocol of GET <path> HTTP/1.0 followed by an empty line
            out.println();

            // Read data from the server until we finish reading the document
            String line = in.readLine();
            while( line != null )
            {
                System.out.println( line );
                ListInString = line;
                line = in.readLine();
            }

            // Close our streams
            //in.close();
            //out.close();
            //socket.close();

        }
        catch( Exception e )
        {
            e.printStackTrace();
        }
    }

    public ObservableList<Data> getDataFromSocket()
    {
        ObservableList<Data> ListFromDataBase = FXCollections.observableArrayList();
        // 1. String to json
        JSONObject json = new JSONObject(ListInString);
        // 2. iterate using enumeration object
        Iterator<String> enumeration = json.keys();
        while(enumeration.hasNext()) {
            String key = enumeration.next();
            ListFromDataBase.add(new Data(key, (Integer) json.get(key)));
        }

        return ListFromDataBase;
    }
}
