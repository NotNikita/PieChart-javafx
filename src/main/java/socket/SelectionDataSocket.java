package socket;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.neovisionaries.ws.client.*;
import controllers.NoPathController;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class SelectionDataSocket {

    public static class SingletonHolder{
        public static final SelectionDataSocket INSTANCE = new SelectionDataSocket();
    }
    public static SelectionDataSocket getInstance(){
        return SingletonHolder.INSTANCE;
    }

    /**
     *  экземпляр WebSocketMap, хранит сокеты и их ключи
     *  для обращения и закрытия
     *  */
    private final HashMap <String, WebSocket> webSocketMap = new HashMap<>();

    /**
     *  адрес для подключения к серверу
     */
    private final String socketPath = "ws://localhost:8080/websocket/";
    private static ObjectMapper mapper = new ObjectMapper();

    public void startTransferData(NoPathController chartController) {

        String uniqueKey = chartController.getChartUniqueKey();

        Thread startThread = new Thread() {
            public void run() {
                try {
                    String fullSocketPath = socketPath;

                    WebSocket webSocket = new WebSocketFactory()
                            .setConnectionTimeout(2000)
                            .createSocket(fullSocketPath + "hello")
                            .addListener(getWebSocketAdapter())
                            .addExtension(WebSocketExtension.PERMESSAGE_DEFLATE)
                            .addHeader("key", uniqueKey);
                            //.connect();

                    System.out.println(" URI IS " + webSocket.getURI());
                    webSocket.connect();

                    if(webSocket.isOpen()){
                        webSocketMap.put(uniqueKey,webSocket);
                    }
                } catch (WebSocketException | IOException e) {
                    e.printStackTrace();
                }
            }

            private WebSocketListener getWebSocketAdapter() {
                return new WebSocketAdapter() {
                    @Override
                    public void onTextMessage(WebSocket websocket, String text) throws Exception {
                        try{
                            if (!text.isEmpty()) {
                                // TODO: SEND DATA IN TEXT TO CONTROLLER AND UPDATE THE CHART SOMEHOW
                                System.out.println("I'VE JUST GOT THIS FROM SERVER: " + text);

                                if (text.equals("welcome")){
                                    ScheduledExecutorService executorService;
                                    executorService = Executors.newSingleThreadScheduledExecutor();
                                    executorService.scheduleAtFixedRate(() -> {
                                        websocket.sendText("give me data");
                                    }, 10, 5, TimeUnit.SECONDS);
                                } else
                                {
                                    System.out.println("UPDATING THE CHART WITH NEW DATA");
                                    chartController.updatePieDataList(text);
                                }
                            }

                        }catch (Exception e){
                            e.printStackTrace();
                        }
                    }
                };
            }
        };
        startThread.start();
    }

    public void triggerFillDataStart(NoPathController chartController) {
        try {
            StringBuilder fullTriggerPath = new StringBuilder();
            //creating url with all parametres in fullTriggerPAth
            NoPathController.sendGET();
        }catch (UnsupportedEncodingException e){
            e.printStackTrace();
        } catch (IOException e){
            e.printStackTrace();
        }
    }


    /**
     * Завершение работы сокета
     */
    public void closeSocket(UUID uniqueKey) {
        WebSocket webSocket = webSocketMap.get(uniqueKey);
        if(webSocket != null) {
            try {
                webSocket.disconnect();
                webSocket = null;
            }catch(Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Получение состояния сокета
     * @return true - сокет открыт, false - закрыт
     */
    public boolean isSocketOpen(UUID uniqueKey) {
        WebSocket webSocket = webSocketMap.get(uniqueKey);
        if(webSocket != null) {
            return webSocket != null && webSocket.isOpen();
        } else {
            return false;
        }
    }

    public WebSocket getWebSocket(UUID uniqueKey){
        return webSocketMap.get(uniqueKey);
    }

}


