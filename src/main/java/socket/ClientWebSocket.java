package socket;

import classes.PieChartNoPath;

import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import com.neovisionaries.ws.client.WebSocket;
import controllers.NoPathController;
import org.springframework.web.socket.client.WebSocketClient;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;

public class ClientWebSocket {

    WebSocketClient client = new StandardWebSocketClient();

    public static class SingletonHolder{
        public static final ClientWebSocket INSTANCE = new ClientWebSocket();
    }
    public static ClientWebSocket getInstance(){
        return SingletonHolder.INSTANCE;
    }

    private volatile WebSocket webSocket = null;

    private final String socketPath = "wss://localhost:8080/websocket";
    /**
     *  checkConnectionScheduler SingleThreadScheduledExecutor которые проверяет isSocketOpen.
     *  Запускается после старта Socket и останавливается на время установления соединения
     */
    private ScheduledExecutorService checkConnectionScheduler = Executors.newSingleThreadScheduledExecutor();

    /**
     *  checkConnectionScheduler SingleThreadScheduledExecutor, который установливает соединение
     */
    private ScheduledExecutorService checkOpen = Executors.newSingleThreadScheduledExecutor();

    public static void start() {
        ClientWebSocket.start();
    }

    /**
     * Завершение работы сокета
     */
    public void closeSocket() {
        if(webSocket != null) {
            try {
                webSocket.disconnect();
                webSocket = null;
            }catch(Exception e) {
                e.printStackTrace();
            }
        }
    }

    public ScheduledExecutorService getCheckConnectionScheduler() {
        return checkConnectionScheduler;
    }

    public void setCheckConnectionScheduler(ScheduledExecutorService checkConnectionScheduler) {
        this.checkConnectionScheduler = checkConnectionScheduler;
    }

    public ScheduledExecutorService getCheckOpen() {
        return checkOpen;
    }
    public void setCheckOpen(ScheduledExecutorService checkOpen) {
        this.checkOpen = checkOpen;
    }
}
